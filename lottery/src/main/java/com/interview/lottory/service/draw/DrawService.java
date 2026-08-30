package com.interview.lottory.service.draw;

import com.interview.lottory.domain.LotteryEvent;
import com.interview.lottory.enums.CampaignStatus;
import com.interview.lottory.enums.PrizeType;
import com.interview.lottory.infra.Constants;
import com.interview.lottory.infra.config.DrawProperties;
import com.interview.common.exception.ErrorCode;
import com.interview.common.exception.InterviewException;
import com.interview.lottory.repository.*;
import com.interview.lottory.service.draw.dto.*;
import com.interview.lottory.service.draw.mapper.DrawEntityMapper;
import com.interview.lottory.util.IdGeneratorUtil;
import com.interview.lottory.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class DrawService {
    private final LotteryCampaignRepository campaignRepository;
    private final LotteryPrizeRepository prizeRepository;
    private final LotteryUserQuotaRepository quotaRepository;
    private final LotteryEventRepository eventRepository;
    private final LotteryDrawRepository drawRepository;
    private final DrawEntityMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<Long> releaseIdempotencyScript;
    private final TransactionTemplate transactionTemplate;
    private final JsonUtil jsonUtil;
    private final DrawProperties properties;
    private final PrizeStockReservationService stockReservationService;

    public DrawAcceptedBo submit(DrawCommandBo command) {
        validateCommand(command);
        String lockKey = Constants.RedisKey.IDEMPOTENCY_PREFIX + command.requestId();
        String lockToken = IdGeneratorUtil.nextUuidString();
        boolean acquired = acquireRequest(lockKey, lockToken);
        if (!acquired) {
            return findExistingRequest(command.requestId()).orElseThrow(
                    () -> new InterviewException(ErrorCode.DUPLICATE_REQUEST));
        }

        try {
            return transactionTemplate.execute(status -> createRequest(command));
        } catch (DataIntegrityViolationException exception) {
            return findExistingRequest(command.requestId()).orElseThrow(
                    () -> new InterviewException(ErrorCode.DUPLICATE_REQUEST));
        } catch (RuntimeException exception) {
            releaseRequest(lockKey, lockToken);
            throw exception;
        }
    }

    private DrawAcceptedBo createRequest(DrawCommandBo command) {
        DrawCampaignBo campaign = campaignRepository.findByIdAndDeletedFalse(command.campaignId())
                .map(mapper::toCampaignBo)
                .orElseThrow(() -> new InterviewException(ErrorCode.CAMPAIGN_NOT_FOUND));
        validateCampaign(campaign);

        UUID eventId = IdGeneratorUtil.nextUuid();
        LotteryEvent event = mapper.toEvent(command, eventId, jsonUtil.writeJson(command));
        return mapper.toAcceptedBo(eventRepository.saveAndFlush(event));
    }

    @Transactional
    public DrawResultBo process(UUID eventId) {
        if (eventRepository.claimForProcessing(eventId) != 1) {
            return null;
        }
        LotteryEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new InterviewException(ErrorCode.INVALID_REQUEST));
        DrawCommandBo command = jsonUtil.readJson(event.getPayload(), DrawCommandBo.class);

        DrawCampaignBo campaign = campaignRepository.findByIdAndDeletedFalse(command.campaignId())
                .map(mapper::toCampaignBo)
                .orElseThrow(() -> new InterviewException(ErrorCode.CAMPAIGN_NOT_FOUND));
        validateCampaign(campaign);

        quotaRepository.createIfAbsent(command.campaignId(), command.userId());
        if (quotaRepository.consumeIfAvailable(command.campaignId(), command.userId(),
                command.drawCount(), campaign.maxDrawsPerUser()) != 1) {
            throw new InterviewException(ErrorCode.DRAW_LIMIT_EXCEEDED);
        }

        List<DrawPrizeBo> prizes = mapper.toPrizeBos(
                prizeRepository.findByCampaignIdAndEnabledTrueAndDeletedFalseOrderByDisplayOrderAsc(
                        command.campaignId()));
        DrawPrizeBo noPrize = prizes.stream().filter(p -> p.prizeType() == PrizeType.NO_PRIZE)
                .findFirst().orElseThrow(() -> new InterviewException(ErrorCode.INVALID_PRIZE_CONFIGURATION));

        List<DrawCandidate> candidates = new ArrayList<>(command.drawCount());
        for (int sequence = 1; sequence <= command.drawCount(); sequence++) {
            candidates.add(new DrawCandidate(sequence, selectPrize(prizes)));
        }

        Map<Long, List<DrawCandidate>> candidatesByPrize = new LinkedHashMap<>();
        candidates.stream()
                .filter(candidate -> candidate.selected().prizeType() == PrizeType.PRIZE)
                .forEach(candidate -> candidatesByPrize
                        .computeIfAbsent(candidate.selected().id(), ignored -> new ArrayList<>())
                        .add(candidate));
        Map<Long, Long> requested = new LinkedHashMap<>();
        Map<Long, Long> databaseStock = new LinkedHashMap<>();
        candidatesByPrize.forEach((prizeId, values) -> {
            requested.put(prizeId, (long) values.size());
            databaseStock.put(prizeId, values.getFirst().selected().remainingStock());
        });

        Map<Long, Long> reserved = stockReservationService.reserve(
                command.campaignId(), eventId, requested, databaseStock);
        Set<Integer> winningSequences = new HashSet<>();
        candidatesByPrize.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    long quantity = reserved.getOrDefault(entry.getKey(), 0L);
                    if (quantity > 0
                            && prizeRepository.deductStockIfAvailable(entry.getKey(), quantity) != 1) {
                        throw new InterviewException(ErrorCode.DATABASE_OPERATION_FAILED);
                    }
                    entry.getValue().stream().limit(quantity)
                            .map(DrawCandidate::sequence)
                            .forEach(winningSequences::add);
                });

        List<DrawItemBo> items = candidates.stream().map(candidate -> {
            boolean won = candidate.selected().prizeType() == PrizeType.PRIZE
                    && winningSequences.contains(candidate.sequence());
            DrawPrizeBo actual = won ? candidate.selected() : noPrize;
            return new DrawItemBo(candidate.sequence(), won ? actual.id() : null,
                    actual.prizeCode(), actual.name(), won);
        }).toList();

        DrawResultBo result = new DrawResultBo(eventId, command.requestId(), command.campaignId(),
                command.userId(), command.drawCount(), List.copyOf(items));
        drawRepository.saveAll(items.stream().map(item -> mapper.toDraw(item, command, eventId)).toList());
        mapper.attachResult(event, jsonUtil.writeJson(result));
        eventRepository.save(event);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID eventId, ErrorCode errorCode) {
        eventRepository.findById(eventId).ifPresent(event -> {
            mapper.markFailed(event, errorCode.getCode());
            eventRepository.save(event);
        });
    }

    private DrawPrizeBo selectPrize(List<DrawPrizeBo> prizes) {
        long probabilityScale = properties.probabilityScale();
        long ticket = ThreadLocalRandom.current().nextLong(probabilityScale);
        long cumulative = 0;
        for (DrawPrizeBo prize : prizes) {
            cumulative += prize.probability().multiply(BigDecimal.valueOf(probabilityScale)).longValueExact();
            if (ticket < cumulative) {
                return prize;
            }
        }
        throw new InterviewException(ErrorCode.INVALID_PRIZE_CONFIGURATION);
    }

    private Optional<DrawAcceptedBo> findExistingRequest(String requestId) {
        return eventRepository.findByRequestId(requestId)
                .map(mapper::toAcceptedBo);
    }

    private boolean acquireRequest(String key, String token) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(
                    key, token, properties.idempotencyTtl()));
        } catch (RedisConnectionFailureException ignored) {
            return true;
        }
    }

    public void cacheResult(DrawResultBo result) {
        try {
            redisTemplate.opsForValue().set(
                    Constants.RedisKey.RESULT_PREFIX + result.requestId(), result, properties.resultTtl());
        } catch (RedisConnectionFailureException ignored) {
            // PostgreSQL remains the authoritative idempotency store.
        }
    }

    private void releaseRequest(String key, String token) {
        try {
            stringRedisTemplate.execute(releaseIdempotencyScript, List.of(key), token);
        } catch (RedisConnectionFailureException ignored) {
            // The key expires automatically; DB uniqueness remains authoritative.
        }
    }

    private void validateCommand(DrawCommandBo command) {
        if (command.requestId() == null || command.requestId().isBlank()
                || command.userId() == null || command.userId().isBlank()
                || command.campaignId() == null || command.drawCount() < 1
                || command.drawCount() > properties.maxBatchDraws()) {
            throw new InterviewException(ErrorCode.INVALID_REQUEST, Constants.MessageKey.INVALID_DRAW_COUNT,
                    1, properties.maxBatchDraws());
        }
    }

    private void validateCampaign(DrawCampaignBo campaign) {
        Instant now = Instant.now();
        if (campaign.status() != CampaignStatus.ACTIVE
                || now.isBefore(campaign.startsAt()) || !now.isBefore(campaign.endsAt())) {
            throw new InterviewException(ErrorCode.CAMPAIGN_NOT_ACTIVE);
        }
    }

    private record DrawCandidate(int sequence, DrawPrizeBo selected) {
    }

}
