package com.interview.lottory.service.draw;

import com.interview.common.exception.ErrorCode;
import com.interview.common.exception.InterviewException;
import com.interview.lottory.domain.LotteryCampaign;
import com.interview.lottory.domain.LotteryEvent;
import com.interview.lottory.enums.CampaignStatus;
import com.interview.lottory.enums.LotteryEventStatus;
import com.interview.lottory.enums.PrizeType;
import com.interview.lottory.infra.config.DrawProperties;
import com.interview.lottory.repository.*;
import com.interview.lottory.service.draw.dto.*;
import com.interview.lottory.service.draw.mapper.DrawEntityMapper;
import com.interview.lottory.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {
    @Mock LotteryCampaignRepository campaigns;
    @Mock LotteryPrizeRepository prizes;
    @Mock LotteryUserQuotaRepository quotas;
    @Mock LotteryEventRepository events;
    @Mock LotteryDrawRepository draws;
    @Mock DrawEntityMapper mapper;
    @Mock RedisTemplate<String, Object> redis;
    @Mock StringRedisTemplate stringRedis;
    @Mock RedisScript<Long> releaseScript;
    @Mock TransactionTemplate transactions;
    @Mock JsonUtil json;
    @Mock ValueOperations<String, String> stringValues;
    @Mock ValueOperations<String, Object> objectValues;
    DrawService service;

    @BeforeEach
    void setUp() {
        var properties = new DrawProperties(10, 10_000_000, Duration.ofHours(1), Duration.ofHours(1),
                Duration.ofMinutes(5), Duration.ofSeconds(1));
        service = new DrawService(campaigns, prizes, quotas, events, draws, mapper, redis, stringRedis,
                releaseScript, transactions, json, properties);
    }

    @Test
    void rejectsInvalidSubmitCommandBeforeUsingRedis() {
        assertThatThrownBy(() -> service.submit(new DrawCommandBo("", 1L, "user", 1)))
                .isInstanceOf(InterviewException.class)
                .extracting(e -> ((InterviewException) e).getErrorCode().getCode())
                .isEqualTo("INVALID_REQUEST");
        verifyNoInteractions(stringRedis);
    }

    @Test
    void returnsExistingRequestWhenIdempotencyLockIsHeld() {
        var accepted = new DrawAcceptedBo(UUID.randomUUID(), "request-1", LotteryEventStatus.PENDING);
        var existing = new LotteryEvent();
        when(stringRedis.opsForValue()).thenReturn(stringValues);
        when(stringValues.setIfAbsent(anyString(), anyString(), any())).thenReturn(false);
        when(events.findByRequestId("request-1")).thenReturn(Optional.of(existing));
        when(mapper.toAcceptedBo(existing)).thenReturn(accepted);

        assertThat(service.submit(new DrawCommandBo("request-1", 1L, "user", 1))).isEqualTo(accepted);
        verify(transactions, never()).execute(any());
    }

    @Test
    void createsPendingRequestWhenLockIsAcquired() {
        var command = new DrawCommandBo("request-1", 1L, "user", 1);
        var campaign = new LotteryCampaign();
        var event = new LotteryEvent();
        var accepted = new DrawAcceptedBo(event.getEventId(), command.requestId(), LotteryEventStatus.PENDING);
        when(stringRedis.opsForValue()).thenReturn(stringValues);
        when(stringValues.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        when(transactions.execute(any())).thenAnswer(invocation ->
                invocation.<org.springframework.transaction.support.TransactionCallback<DrawAcceptedBo>>getArgument(0)
                        .doInTransaction(null));
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(campaign));
        when(mapper.toCampaignBo(campaign)).thenReturn(activeCampaign());
        when(json.writeJson(command)).thenReturn("{}");
        when(mapper.toEvent(eq(command), any(UUID.class), eq("{}"))).thenReturn(event);
        when(events.saveAndFlush(event)).thenReturn(event);
        when(mapper.toAcceptedBo(event)).thenReturn(accepted);

        assertThat(service.submit(command)).isEqualTo(accepted);
    }

    @Test
    void processesDrawAndFallsBackToNoPrizeWhenStockIsUnavailable() {
        UUID eventId = UUID.randomUUID();
        var event = new LotteryEvent();
        event.setPayload("{}");
        var command = new DrawCommandBo("request-1", 1L, "user", 1);
        var campaign = new LotteryCampaign();
        var selected = new DrawPrizeBo(10L, "WIN", "Winner", PrizeType.PRIZE, BigDecimal.ONE, 1);
        var noPrize = new DrawPrizeBo(20L, "LOSE", "No prize", PrizeType.NO_PRIZE, BigDecimal.ZERO, 0);
        when(events.claimForProcessing(eventId)).thenReturn(1);
        when(events.findById(eventId)).thenReturn(Optional.of(event));
        when(json.readJson("{}", DrawCommandBo.class)).thenReturn(command);
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(campaign));
        when(mapper.toCampaignBo(campaign)).thenReturn(activeCampaign());
        when(quotas.consumeIfAvailable(1L, "user", 1, 5)).thenReturn(1);
        when(mapper.toPrizeBos(any())).thenReturn(List.of(selected, noPrize));
        when(prizes.deductStockIfAvailable(10L, 1)).thenReturn(0);
        when(json.writeJson(any(DrawResultBo.class))).thenReturn("result");

        DrawResultBo result = service.process(eventId);

        assertThat(result.results()).singleElement().satisfies(item -> {
            assertThat(item.won()).isFalse();
            assertThat(item.prizeCode()).isEqualTo("LOSE");
            assertThat(item.prizeId()).isNull();
        });
        verify(draws).saveAll(anyList());
        verify(mapper).attachResult(event, "result");
        verify(events).save(event);
    }

    @Test
    void returnsNullWhenEventCannotBeClaimed() {
        UUID eventId = UUID.randomUUID();
        assertThat(service.process(eventId)).isNull();
        verify(events, never()).findById(eventId);
    }

    @Test
    void rejectsDrawWhenQuotaIsExhausted() {
        UUID eventId = UUID.randomUUID();
        var event = new LotteryEvent(); event.setPayload("{}");
        var command = new DrawCommandBo("request-1", 1L, "user", 2);
        when(events.claimForProcessing(eventId)).thenReturn(1);
        when(events.findById(eventId)).thenReturn(Optional.of(event));
        when(json.readJson("{}", DrawCommandBo.class)).thenReturn(command);
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(new LotteryCampaign()));
        when(mapper.toCampaignBo(any())).thenReturn(activeCampaign());

        assertThatThrownBy(() -> service.process(eventId)).isInstanceOf(InterviewException.class)
                .extracting(e -> ((InterviewException) e).getErrorCode().getCode())
                .isEqualTo("DRAW_LIMIT_EXCEEDED");
    }

    @Test
    void marksExistingEventAsFailed() {
        UUID eventId = UUID.randomUUID();
        var event = new LotteryEvent();
        when(events.findById(eventId)).thenReturn(Optional.of(event));

        service.markFailed(eventId, ErrorCode.INTERNAL_ERROR);

        verify(mapper).markFailed(event, "INTERNAL_ERROR");
        verify(events).save(event);
    }

    @Test
    void cachesCompletedResult() {
        var result = new DrawResultBo(UUID.randomUUID(), "request-1", 1L, "user", 1, List.of());
        when(redis.opsForValue()).thenReturn(objectValues);
        service.cacheResult(result);
        verify(objectValues).set(contains("request-1"), eq(result), any());
    }

    private DrawCampaignBo activeCampaign() {
        return new DrawCampaignBo(1L, CampaignStatus.ACTIVE, 5,
                Instant.now().minusSeconds(60), Instant.now().plusSeconds(60));
    }
}
