package com.interview.lottory.infra.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.lottery.draw")
public record DrawProperties(
        @Min(1) int maxBatchDraws,
        @Min(1) long probabilityScale,
        @NotNull Duration idempotencyTtl,
        @NotNull Duration resultTtl,
        @NotNull Duration stockReservationTtl,
        @NotNull Duration sseTimeout,
        @NotNull Duration ssePollInterval
) {
}
