package com.interview.lottory.service.draw;

import com.interview.lottory.infra.config.DrawProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import com.interview.lottory.repository.LotteryEventRepository;
import com.interview.lottory.domain.LotteryEvent;
import com.interview.lottory.enums.LotteryEventStatus;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrizeStockReservationServiceTest {
    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> values;
    @Mock RedisScript<List<Long>> reserveScript;
    @Mock RedisScript<Long> confirmScript;
    @Mock RedisScript<Long> releaseScript;
    @Mock SetOperations<String, String> sets;
    @Mock ZSetOperations<String, String> sortedSets;
    @Mock LotteryEventRepository events;
    PrizeStockReservationService service;

    @BeforeEach
    void setUp() {
        var properties = new DrawProperties(100, 10_000_000, Duration.ofHours(1), Duration.ofHours(1),
                Duration.ofMinutes(10), Duration.ofMinutes(5), Duration.ofSeconds(1));
        service = new PrizeStockReservationService(redis, reserveScript, confirmScript, releaseScript,
                properties, events);
    }

    @Test
    void initializesCacheAndReservesAllPrizeQuantitiesInOneLuaExecution() {
        UUID eventId = UUID.randomUUID();
        when(redis.opsForValue()).thenReturn(values);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.execute(eq(reserveScript), anyList(), any(Object[].class)))
                .thenReturn(List.of(2L, 1L));

        Map<Long, Long> reserved = service.reserve(7L, eventId,
                Map.of(20L, 3L, 10L, 2L), Map.of(20L, 1L, 10L, 5L));

        assertThat(reserved).containsExactlyInAnyOrderEntriesOf(Map.of(10L, 2L, 20L, 1L));
        verify(values).setIfAbsent("lottery:stock:{7}:10", "5");
        verify(values).setIfAbsent("lottery:stock:{7}:20", "1");
        verify(redis).execute(eq(reserveScript), anyList(), any(Object[].class));
        verify(redis).execute(eq(confirmScript), anyList(), any(Object[].class));
    }

    @Test
    void safelyFallsBackToDatabaseStockWhenRedisIsUnavailable() {
        when(redis.opsForValue()).thenThrow(new RedisConnectionFailureException("offline"));

        Map<Long, Long> reserved = service.reserve(7L, UUID.randomUUID(),
                Map.of(10L, 5L), Map.of(10L, 2L));

        assertThat(reserved).containsEntry(10L, 2L);
    }

    @Test
    void releasesExpiredReservationWhenDrawDidNotComplete() {
        UUID eventId = UUID.randomUUID();
        String reservation = "lottery:stock-reservation:{7}:" + eventId;
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForZSet()).thenReturn(sortedSets);
        when(sets.members("lottery:stock-reservation:campaigns")).thenReturn(Set.of("7"));
        when(sortedSets.rangeByScore(eq("lottery:stock-reservation:{7}:pending"), eq(0.0), anyDouble()))
                .thenReturn(Set.of(reservation));
        when(sortedSets.size("lottery:stock-reservation:{7}:pending")).thenReturn(0L);
        when(events.findById(eventId)).thenReturn(Optional.empty());

        service.cleanupExpiredReservations();

        verify(redis).execute(eq(releaseScript),
                eq(List.of(reservation, "lottery:stock-reservation:{7}:pending")), any(Object[].class));
        verify(sets).remove("lottery:stock-reservation:campaigns", "7");
    }

    @Test
    void confirmsExpiredReservationWhenDrawCompleted() {
        UUID eventId = UUID.randomUUID();
        String reservation = "lottery:stock-reservation:{7}:" + eventId;
        var event = new LotteryEvent(); event.setStatus(LotteryEventStatus.COMPLETED);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForZSet()).thenReturn(sortedSets);
        when(sets.members(anyString())).thenReturn(Set.of("7"));
        when(sortedSets.rangeByScore(anyString(), eq(0.0), anyDouble())).thenReturn(Set.of(reservation));
        when(sortedSets.size(anyString())).thenReturn(1L);
        when(events.findById(eventId)).thenReturn(Optional.of(event));

        service.cleanupExpiredReservations();

        verify(redis).execute(eq(confirmScript), anyList(), any(Object[].class));
        verify(redis, never()).execute(eq(releaseScript), anyList(), any(Object[].class));
    }
}
