package com.interview.lottory.service.sse;

import com.interview.lottory.controller.draw.dto.DrawEventStatusVo;
import com.interview.lottory.controller.draw.mapper.DrawControllerMapper;
import com.interview.lottory.enums.LotteryEventStatus;
import com.interview.lottory.infra.config.DrawProperties;
import com.interview.lottory.service.draw.DrawEventQueryService;
import com.interview.lottory.service.draw.dto.DrawEventStatusBo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrawSseServiceTest {
    @Mock DrawEventQueryService queries;
    @Mock DrawControllerMapper mapper;
    @Mock TaskScheduler scheduler;
    @Mock ScheduledFuture<?> future;
    DrawSseService service;

    @BeforeEach
    void setUp() {
        var properties = new DrawProperties(10, 10_000_000, Duration.ofHours(1), Duration.ofHours(1),
                Duration.ofMinutes(10),
                Duration.ofMinutes(5), Duration.ofSeconds(1));
        service = new DrawSseService(queries, mapper, properties, scheduler);
    }

    @Test
    void completesImmediatelyForTerminalEvent() {
        UUID eventId = UUID.randomUUID();
        var status = status(eventId, LotteryEventStatus.COMPLETED);
        when(queries.getEventByEventIdAndUserId(eventId, "user")).thenReturn(status);
        when(mapper.toVo(status)).thenReturn(mock(DrawEventStatusVo.class));

        var emitter = service.subscribe(eventId, "user");

        assertThat(emitter.getTimeout()).isEqualTo(Duration.ofMinutes(5).toMillis());
        verifyNoInteractions(scheduler);
    }

    @Test
    void pollsUntilEventBecomesTerminal() {
        UUID eventId = UUID.randomUUID();
        var pending = status(eventId, LotteryEventStatus.PENDING);
        var completed = status(eventId, LotteryEventStatus.COMPLETED);
        when(queries.getEventByEventIdAndUserId(eventId, "user")).thenReturn(pending, completed);
        when(mapper.toVo(any(DrawEventStatusBo.class))).thenReturn(mock(DrawEventStatusVo.class));
        doReturn(future).when(scheduler)
                .scheduleAtFixedRate(any(Runnable.class), eq(Duration.ofSeconds(1)));

        service.subscribe(eventId, "user");
        var runnable = org.mockito.ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).scheduleAtFixedRate(runnable.capture(), eq(Duration.ofSeconds(1)));
        runnable.getValue().run();

        verify(future).cancel(false);
        verify(queries, times(2)).getEventByEventIdAndUserId(eventId, "user");
    }

    private DrawEventStatusBo status(UUID id, LotteryEventStatus status) {
        return new DrawEventStatusBo(id, "request", 1L, "user", 1, status, null, List.of(), null, null);
    }
}
