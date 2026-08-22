package com.interview.lottory.service.draw;

import com.interview.lottory.domain.LotteryEvent;
import com.interview.lottory.enums.LotteryEventStatus;
import com.interview.lottory.infra.config.MessagingProperties;
import com.interview.lottory.repository.LotteryEventRepository;
import com.interview.lottory.service.draw.mapper.DrawEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LotteryOutboxPublisher {
    private final LotteryEventRepository eventRepository;
    private final DrawEntityMapper mapper;
    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${app.messaging.lottery.outbox-poll-interval-ms:1000}")
    public void publishPendingEvents() {
        eventRepository.findReadyToPublish(LotteryEventStatus.PENDING, Instant.now(), PageRequest.of(0, 100))
                .forEach(this::publish);
    }

    private void publish(LotteryEvent event) {
        var message = mapper.toMessageBo(event);
        boolean claimed = transactionTemplate.execute(status ->
                eventRepository.claimForPublishing(message.eventId()) == 1);
        if (!claimed) {
            return;
        }
        try {
            CorrelationData correlation = new CorrelationData(message.eventId().toString());
            rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(),
                    message, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.ack() || correlation.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ did not confirm event " + message.eventId());
            }
            transactionTemplate.executeWithoutResult(status ->
                    eventRepository.markPublishedIfDispatching(message.eventId()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            markFailed(event);
        } catch (Exception exception) {
            markFailed(event);
        }
    }

    private void markFailed(LotteryEvent event) {
        var message = mapper.toMessageBo(event);
        long delaySeconds = Math.min(300, 1L << Math.min(8, event.getRetryCount()));
        transactionTemplate.executeWithoutResult(status ->
                eventRepository.reschedulePublishing(message.eventId(), Instant.now().plusSeconds(delaySeconds)));
    }
}
