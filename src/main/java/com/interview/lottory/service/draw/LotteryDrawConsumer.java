package com.interview.lottory.service.draw;

import com.interview.lottory.infra.exception.InterviewException;
import com.interview.lottory.infra.exception.ErrorCode;
import com.interview.lottory.service.draw.dto.DrawResultBo;
import com.interview.lottory.service.draw.dto.LotteryEventMessageBo;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class LotteryDrawConsumer {
    private final DrawService drawService;

    @RabbitListener(queues = "${app.messaging.lottery.queue}")
    public void consume(LotteryEventMessageBo message, Message amqpMessage, Channel channel)
            throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            DrawResultBo result = drawService.process(message.eventId());
            if (result != null) {
                drawService.cacheResult(result);
            }
            channel.basicAck(deliveryTag, false);
        } catch (InterviewException exception) {
            drawService.markFailed(message.eventId(), exception.getErrorCode());
            channel.basicAck(deliveryTag, false);
        } catch (RuntimeException exception) {
            drawService.markFailed(message.eventId(), ErrorCode.INTERNAL_ERROR);
            throw exception;
        }
    }
}
