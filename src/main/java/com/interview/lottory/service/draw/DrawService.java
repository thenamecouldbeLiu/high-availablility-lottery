package com.interview.lottory.service.draw;

import com.interview.lottory.domain.*;
import com.interview.lottory.enums.CampaignStatus;
import com.interview.lottory.enums.PrizeType;
import com.interview.lottory.infra.exception.ErrorCode;
import com.interview.lottory.infra.exception.InterviewException;
import com.interview.lottory.infra.Constants;
import com.interview.lottory.infra.config.DrawProperties;
import com.interview.lottory.repository.*;
import com.interview.lottory.service.draw.dto.*;
import com.interview.lottory.service.draw.mapper.DrawEntityMapper;
import com.interview.lottory.util.IdGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
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
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private final DrawProperties properties;

    public DrawResultBo draw(DrawCommandBo command) {
        validateCommand(command);
        DrawResultBo cached = readCachedResult(command.requestId());
        if (cached != null) {
            return cached;
        }

        String lockKey = Constants.RedisKey.IDEMPOTENCY_PREFIX + command.requestId();
        boolean acquired = acquireRequest(lockKey);
        if (!acquired) {
            return findExisting(command.requestId()).orElseThrow(
                    () -> new InterviewException(ErrorCode.DUPLICATE_REQUEST));
        }

        try {
            DrawResultBo result = transactionTemplate.execute(status -> executeDraw(command));
            cacheResult(command.requestId(), result);
            return result;
        } catch (DataIntegrityViolationException exception) {
            return findExisting(command.requestId()).orElseThrow(
                    () -> new InterviewException(ErrorCode.DUPLICATE_REQUEST));
        } catch (RuntimeException exception) {
            releaseRequest(lockKey);
            throw exception;
        }
    }

    private DrawResultBo executeDraw(DrawCommandBo command) {
        DrawCampaignBo campaign = campaignRepository.findByIdAndDeletedFalse(command.campaignId())
                .map(mapper::toCampaignBo)
                .orElseThrow(() -> new InterviewException(ErrorCode.CAMPAIGN_NOT_FOUND));
        validateCampaign(campaign);

        UUID eventId = IdGeneratorUtil.nextUuid();
        LotteryEvent event = mapper.toEvent(command, eventId, writeJson(command));
        eventRepository.saveAndFlush(event);

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

        List<DrawItemBo> items = new ArrayList<>(command.drawCount());
        for (int sequence = 1; sequence <= command.drawCount(); sequence++) {
            DrawPrizeBo selected = selectPrize(prizes);
            boolean won = selected.prizeType() == PrizeType.PRIZE
                    && prizeRepository.deductStockIfAvailable(selected.id(), 1) == 1;
            DrawPrizeBo actual = won ? selected : noPrize;
            items.add(new DrawItemBo(sequence, won ? actual.id() : null,
                    actual.prizeCode(), actual.name(), won));
        }

        DrawResultBo result = new DrawResultBo(eventId, command.requestId(), command.campaignId(),
                command.userId(), command.drawCount(), List.copyOf(items));
        drawRepository.saveAll(items.stream().map(item -> mapper.toDraw(item, command, eventId)).toList());
        mapper.attachResult(event, writeJson(result));
        eventRepository.save(event);
        return result;
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

    private Optional<DrawResultBo> findExisting(String requestId) {
        return eventRepository.findByRequestId(requestId)
                .map(mapper::toMessageBo)
                .filter(event -> event.resultPayload() != null)
                .map(event -> readJson(event.resultPayload(), DrawResultBo.class));
    }

    private DrawResultBo readCachedResult(String requestId) {
        try {
            Object value = redisTemplate.opsForValue().get(Constants.RedisKey.RESULT_PREFIX + requestId);
            return value instanceof DrawResultBo result ? result : null;
        } catch (RedisConnectionFailureException ignored) {
            return null;
        }
    }

    private boolean acquireRequest(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                    key, Constants.ProcessStatus.PROCESSING, properties.idempotencyTtl()));
        } catch (RedisConnectionFailureException ignored) {
            return true;
        }
    }

    private void cacheResult(String requestId, DrawResultBo result) {
        try {
            redisTemplate.opsForValue().set(
                    Constants.RedisKey.RESULT_PREFIX + requestId, result, properties.resultTtl());
        } catch (RedisConnectionFailureException ignored) {
            // PostgreSQL remains the authoritative idempotency store.
        }
    }

    private void releaseRequest(String key) {
        try {
            redisTemplate.delete(key);
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

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new InterviewException(ErrorCode.INTERNAL_ERROR, exception,
                    Constants.MessageKey.DRAW_SERIALIZATION_FAILED);
        }
    }

    private <T> T readJson(String value, Class<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (JacksonException exception) {
            throw new InterviewException(ErrorCode.INTERNAL_ERROR, exception,
                    Constants.MessageKey.DRAW_DESERIALIZATION_FAILED);
        }
    }
}
