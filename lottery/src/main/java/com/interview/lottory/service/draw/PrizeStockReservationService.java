package com.interview.lottory.service.draw;

import com.interview.lottory.infra.Constants;
import com.interview.lottory.infra.config.DrawProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrizeStockReservationService {
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List<Long>> reservePrizeStockScript;
    private final RedisScript<Long> confirmPrizeStockScript;
    private final RedisScript<Long> releasePrizeStockScript;
    private final DrawProperties properties;

    public Map<Long, Long> reserve(long campaignId, UUID eventId,
                                   Map<Long, Long> requested, Map<Long, Long> databaseStock) {
        if (requested.isEmpty()) {
            return Map.of();
        }
        try {
            List<Long> prizeIds = requested.keySet().stream().sorted().toList();
            String reservationKey = reservationKey(campaignId, eventId);
            List<String> keys = new ArrayList<>(prizeIds.size() + 1);
            keys.add(reservationKey);
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
            completeWithTransaction(reservationKey);
            return reserved;
        } catch (RedisConnectionFailureException exception) {
            // PostgreSQL remains authoritative and provides a safe degraded path.
            Map<Long, Long> fallback = new LinkedHashMap<>();
            requested.keySet().stream().sorted().forEach(prizeId -> fallback.put(prizeId,
                    Math.min(requested.get(prizeId), databaseStock.getOrDefault(prizeId, 0L))));
            return fallback;
        }
    }

    private void completeWithTransaction(String reservationKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            confirm(reservationKey);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    confirm(reservationKey);
                } else {
                    release(reservationKey);
                }
            }
        });
    }

    private void confirm(String reservationKey) {
        executeQuietly(confirmPrizeStockScript, reservationKey);
    }

    private void release(String reservationKey) {
        executeQuietly(releasePrizeStockScript, reservationKey);
    }

    private void executeQuietly(RedisScript<Long> script, String reservationKey) {
        try {
            redisTemplate.execute(script, List.of(reservationKey),
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
}
