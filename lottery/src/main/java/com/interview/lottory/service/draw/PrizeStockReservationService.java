package com.interview.lottory.service.draw;

import com.interview.lottory.infra.Constants;
import com.interview.lottory.infra.config.DrawProperties;
import com.interview.lottory.enums.LotteryEventStatus;
import com.interview.lottory.repository.LotteryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PrizeStockReservationService {
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List<Long>> reservePrizeStockScript;
    private final RedisScript<Long> confirmPrizeStockScript;
    private final RedisScript<Long> releasePrizeStockScript;
    private final DrawProperties properties;
    private final LotteryEventRepository eventRepository;

    public Map<Long, Long> reserve(long campaignId, UUID eventId,
                                   Map<Long, Long> requested, Map<Long, Long> databaseStock) {
        if (requested.isEmpty()) {
            return Map.of();
        }
        try {
            List<Long> prizeIds = requested.keySet().stream().sorted().toList();
            String reservationKey = reservationKey(campaignId, eventId);
            String pendingKey = pendingKey(campaignId);
            List<String> keys = new ArrayList<>(prizeIds.size() + 2);
            keys.add(reservationKey);
            keys.add(pendingKey);
            List<String> arguments = new ArrayList<>(prizeIds.size() + 1);
            arguments.add(String.valueOf(properties.stockReservationTtl().toMillis()));

            for (Long prizeId : prizeIds) {
                String stockKey = stockKey(campaignId, prizeId);
                redisTemplate.opsForValue().setIfAbsent(stockKey,
                        String.valueOf(databaseStock.getOrDefault(prizeId, 0L)));
                keys.add(stockKey);
                arguments.add(String.valueOf(requested.get(prizeId)));
            }

            List<Long> values = redisTemplate.execute(reservePrizeStockScript, keys, arguments.toArray());
            if (values == null || values.size() != prizeIds.size()) {
                throw new RedisConnectionFailureException("Invalid Redis stock reservation response");
            }
            Map<Long, Long> reserved = new LinkedHashMap<>();
            for (int index = 0; index < prizeIds.size(); index++) {
                reserved.put(prizeIds.get(index), Long.parseLong(values.get(index).toString()));
            }
            redisTemplate.opsForSet().add(Constants.RedisKey.STOCK_RESERVATION_CAMPAIGNS_KEY,
                    String.valueOf(campaignId));
            completeWithTransaction(reservationKey, pendingKey);
            return reserved;
        } catch (RedisConnectionFailureException exception) {
            // PostgreSQL remains authoritative and provides a safe degraded path.
            Map<Long, Long> fallback = new LinkedHashMap<>();
            requested.keySet().stream().sorted().forEach(prizeId -> fallback.put(prizeId,
                    Math.min(requested.get(prizeId), databaseStock.getOrDefault(prizeId, 0L))));
            return fallback;
        }
    }

    @Scheduled(fixedDelayString = "${app.lottery.draw.stock-reservation-cleanup-interval:1m}")
    public void cleanupExpiredReservations() {
        try {
            Set<String> campaigns = redisTemplate.opsForSet()
                    .members(Constants.RedisKey.STOCK_RESERVATION_CAMPAIGNS_KEY);
            if (campaigns == null) {
                return;
            }
            long now = System.currentTimeMillis();
            for (String campaign : campaigns) {
                String pendingKey = pendingKey(Long.parseLong(campaign));
                Set<String> expired = redisTemplate.opsForZSet().rangeByScore(pendingKey, 0, now);
                if (expired != null) {
                    expired.forEach(reservationKey -> cleanupReservation(reservationKey, pendingKey));
                }
                Long remaining = redisTemplate.opsForZSet().size(pendingKey);
                if (remaining == null || remaining == 0) {
                    redisTemplate.opsForSet().remove(Constants.RedisKey.STOCK_RESERVATION_CAMPAIGNS_KEY, campaign);
                }
            }
        } catch (RedisConnectionFailureException ignored) {
            // The next scheduled run retries cleanup; stock cache TTL remains the final self-healing mechanism.
        }
    }

    private void cleanupReservation(String reservationKey, String pendingKey) {
        try {
            UUID eventId = UUID.fromString(reservationKey.substring(reservationKey.lastIndexOf(':') + 1));
            boolean completed = eventRepository.findById(eventId)
                    .map(event -> event.getStatus() == LotteryEventStatus.COMPLETED)
                    .orElse(false);
            if (completed) {
                confirm(reservationKey, pendingKey);
            } else {
                release(reservationKey, pendingKey);
            }
        } catch (IllegalArgumentException ignored) {
            release(reservationKey, pendingKey);
        }
    }

    private void completeWithTransaction(String reservationKey, String pendingKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            confirm(reservationKey, pendingKey);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    confirm(reservationKey, pendingKey);
                } else {
                    release(reservationKey, pendingKey);
                }
            }
        });
    }

    private void confirm(String reservationKey, String pendingKey) {
        executeQuietly(confirmPrizeStockScript, reservationKey, pendingKey);
    }

    private void release(String reservationKey, String pendingKey) {
        executeQuietly(releasePrizeStockScript, reservationKey, pendingKey);
    }

    private void executeQuietly(RedisScript<Long> script, String reservationKey, String pendingKey) {
        try {
            redisTemplate.execute(script, List.of(reservationKey, pendingKey),
                    String.valueOf(properties.stockReservationTtl().toMillis()));
        } catch (RedisConnectionFailureException ignored) {
            // DB is authoritative; a later stock cache refresh can repair stale Redis state.
        }
    }

    private String stockKey(long campaignId, long prizeId) {
        return Constants.RedisKey.STOCK_PREFIX + "{" + campaignId + "}:" + prizeId;
    }

    private String reservationKey(long campaignId, UUID eventId) {
        return Constants.RedisKey.STOCK_RESERVATION_PREFIX + "{" + campaignId + "}:" + eventId;
    }

    private String pendingKey(long campaignId) {
        return Constants.RedisKey.STOCK_RESERVATION_PREFIX + "{" + campaignId + "}:pending";
    }
}
