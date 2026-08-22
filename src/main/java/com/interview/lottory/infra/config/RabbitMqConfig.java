package com.interview.lottory.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    TopicExchange lotteryExchange(MessagingProperties properties) {
        return new TopicExchange(properties.exchange(), true, false);
    }

    @Bean
    TopicExchange lotteryDeadLetterExchange(MessagingProperties properties) {
        return new TopicExchange(properties.deadLetterExchange(), true, false);
    }

    @Bean
    Queue lotteryQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.queue())
                .deadLetterExchange(properties.deadLetterExchange())
                .deadLetterRoutingKey(properties.deadLetterRoutingKey())
                .build();
    }

    @Bean
    Queue lotteryDeadLetterQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.deadLetterQueue()).build();
    }

    @Bean
    Binding lotteryBinding(Queue lotteryQueue, TopicExchange lotteryExchange,
                           MessagingProperties properties) {
        return BindingBuilder.bind(lotteryQueue)
                .to(lotteryExchange)
                .with(properties.routingKey());
    }

    @Bean
    Binding lotteryDeadLetterBinding(Queue lotteryDeadLetterQueue,
                                     TopicExchange lotteryDeadLetterExchange,
                                     MessagingProperties properties) {
        return BindingBuilder.bind(lotteryDeadLetterQueue)
                .to(lotteryDeadLetterExchange)
                .with(properties.deadLetterRoutingKey());
    }

    @Bean
    MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
