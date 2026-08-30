package com.interview.lottory.service.draw;

import com.interview.lottory.domain.LotteryEvent;
import com.interview.lottory.enums.LotteryEventStatus;
import com.interview.lottory.infra.config.MessagingProperties;
import com.interview.lottory.repository.LotteryEventRepository;
import com.interview.lottory.service.draw.dto.LotteryEventMessageBo;
import com.interview.lottory.service.draw.mapper.DrawEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotteryOutboxPublisherTest {
    @Mock LotteryEventRepository events;
    @Mock DrawEntityMapper mapper;
    @Mock RabbitTemplate rabbit;
    @Mock TransactionTemplate transactions;
    LotteryOutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        var properties = new MessagingProperties("exchange", "queue", "route", "dlx", "dlq", "dead");
        publisher = new LotteryOutboxPublisher(events, mapper, rabbit, properties, transactions);
        lenient().when(transactions.execute(any())).thenAnswer(invocation ->
                invocation.<TransactionCallback<Boolean>>getArgument(0).doInTransaction(null));
        lenient().doAnswer(invocation -> {
            invocation.<java.util.function.Consumer<org.springframework.transaction.TransactionStatus>>getArgument(0)
                    .accept(null);
            return null;
        }).when(transactions).executeWithoutResult(any());
    }

    @Test
    void publishesClaimedEventAndMarksItPublished() {
        var event = event();
        var message = message(event.getEventId());
        when(events.findReadyToPublish(eq(LotteryEventStatus.PENDING), any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(event));
        when(mapper.toMessageBo(event)).thenReturn(message);
        when(events.claimForPublishing(event.getEventId())).thenReturn(1);
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbit).convertAndSend(eq("exchange"), eq("route"), eq(message), any(CorrelationData.class));

        publisher.publishPendingEvents();

        verify(events).markPublishedIfDispatching(eq(event.getEventId()), any(Instant.class));
        verify(events, never()).reschedulePublishing(any(), any());
    }

    @Test
    void skipsEventThatCannotBeClaimed() {
        var event = event();
        when(events.findReadyToPublish(any(), any(), any())).thenReturn(List.of(event));
        when(mapper.toMessageBo(event)).thenReturn(message(event.getEventId()));

        publisher.publishPendingEvents();

        verifyNoInteractions(rabbit);
    }

    @Test
    void reschedulesEventWhenRabbitMqRejectsIt() {
        var event = event(); event.setRetryCount(2);
        var message = message(event.getEventId());
        when(events.findReadyToPublish(any(), any(), any())).thenReturn(List.of(event));
        when(mapper.toMessageBo(event)).thenReturn(message);
        when(events.claimForPublishing(event.getEventId())).thenReturn(1);
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(false, "rejected"));
            return null;
        }).when(rabbit).convertAndSend(anyString(), anyString(), eq(message), any(CorrelationData.class));

        publisher.publishPendingEvents();

        verify(events).reschedulePublishing(eq(event.getEventId()), any(Instant.class));
    }

    private LotteryEvent event() { return new LotteryEvent(); }

    private LotteryEventMessageBo message(UUID id) {
        return new LotteryEventMessageBo(id, "request", 1L, "user", "DRAW", 1, "{}", null);
    }
}
