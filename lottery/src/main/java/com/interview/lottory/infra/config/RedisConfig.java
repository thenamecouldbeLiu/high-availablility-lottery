package com.interview.lottory.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        var stringSerializer = new StringRedisSerializer();
        var jsonSerializer = GenericJacksonJsonRedisSerializer.builder().build();

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    RedisScript<Long> releaseIdempotencyScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/release-idempotency-lock.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    RedisScript<java.util.List<Long>> reservePrizeStockScript() {
        DefaultRedisScript<java.util.List<Long>> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/reserve-prize-stock.lua"));
        script.setResultType(longListType());
        return script;
    }

    @SuppressWarnings("unchecked")
    private Class<java.util.List<Long>> longListType() {
        return (Class<java.util.List<Long>>) (Class<?>) java.util.List.class;
    }

    @Bean
    RedisScript<Long> confirmPrizeStockScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/confirm-prize-stock.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    RedisScript<Long> releasePrizeStockScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/release-prize-stock.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
