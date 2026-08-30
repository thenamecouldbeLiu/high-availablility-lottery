package com.interview.lottory.service.draw;

import com.interview.common.exception.ErrorCode;
import com.interview.common.exception.InterviewException;
import com.interview.lottory.service.draw.dto.DrawResultBo;
import com.interview.lottory.service.draw.dto.LotteryEventMessageBo;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LotteryDrawConsumerTest {
    private final DrawService drawService = mock(DrawService.class);
    private final LotteryDrawConsumer consumer = new LotteryDrawConsumer(drawService);
    private final Channel channel = mock(Channel.class);

    @Test
    void processesCachesAndAcknowledgesMessage() throws IOException {
        var message = message();
        var result = new DrawResultBo(message.eventId(), message.requestId(), 1L, "user", 1, List.of());
        when(drawService.process(message.eventId())).thenReturn(result);

        consumer.consume(message, amqpMessage(7), channel);

        verify(drawService).cacheResult(result);
        verify(channel).basicAck(7, false);
    }

    @Test
    void marksBusinessFailureAndAcknowledgesMessage() throws IOException {
        var message = message();
        doThrow(new InterviewException(ErrorCode.DRAW_LIMIT_EXCEEDED))
                .when(drawService).process(message.eventId());

        consumer.consume(message, amqpMessage(8), channel);

        verify(drawService).markFailed(message.eventId(), ErrorCode.DRAW_LIMIT_EXCEEDED);
        verify(channel).basicAck(8, false);
    }

    @Test
    void marksUnexpectedFailureAndRethrowsForBrokerRetry() {
        var message = message();
        var failure = new IllegalStateException("failure");
        when(drawService.process(message.eventId())).thenThrow(failure);

        assertThatThrownBy(() -> consumer.consume(message, amqpMessage(9), channel)).isSameAs(failure);
        verify(drawService).markFailed(message.eventId(), ErrorCode.INTERNAL_ERROR);
    }

    private LotteryEventMessageBo message() {
        return new LotteryEventMessageBo(UUID.randomUUID(), "request", 1L, "user", "DRAW", 1, "{}", null);
    }

    private Message amqpMessage(long tag) {
        var properties = new MessageProperties(); properties.setDeliveryTag(tag);
        return new Message(new byte[0], properties);
    }
}
