package com.interview.lottory.infra;

import java.math.BigDecimal;

public final class Constants {
    private Constants() {
    }
    public static final BigDecimal TOTAL_PROBABILITY = new BigDecimal("1.0000000");

    public static final class RedisKey {
        public static final String IDEMPOTENCY_PREFIX = "lottery:idempotency:";
        public static final String RESULT_PREFIX = "lottery:result:";

        private RedisKey() {
        }
    }

    public static final class EventType {
        public static final String LOTTERY_DRAW_REQUESTED = "LOTTERY_DRAW_REQUESTED";

        private EventType() {
        }
    }

    public static final class MessageKey {
        public static final String SUCCESS = "error.success";
        public static final String INVALID_REQUEST = "error.invalid-request";
        public static final String CAMPAIGN_NOT_FOUND = "error.campaign-not-found";
        public static final String CAMPAIGN_NOT_ACTIVE = "error.campaign-not-active";
        public static final String PRIZE_NOT_FOUND = "error.prize-not-found";
        public static final String DRAW_LIMIT_EXCEEDED = "error.draw-limit-exceeded";
        public static final String OUT_OF_STOCK = "error.out-of-stock";
        public static final String INVALID_PRIZE_CONFIGURATION = "error.invalid-prize-configuration";
        public static final String DUPLICATE_REQUEST = "error.duplicate-request";
        public static final String MQ_OPERATION_FAILED = "error.mq-operation-failed";
        public static final String REDIS_OPERATION_FAILED = "error.redis-operation-failed";
        public static final String DATABASE_OPERATION_FAILED = "error.database-operation-failed";
        public static final String INTERNAL_ERROR = "error.internal";
        public static final String CAMPAIGN_CODE_EXISTS = "validation.campaign-code-exists";
        public static final String STOCK_BELOW_AWARDED = "validation.stock-below-awarded";
        public static final String INVALID_CAMPAIGN_PERIOD = "validation.invalid-campaign-period";
        public static final String INVALID_DRAW_COUNT = "validation.invalid-draw-count";
        public static final String DRAW_SERIALIZATION_FAILED = "internal.draw-serialization-failed";
        public static final String DRAW_DESERIALIZATION_FAILED = "internal.draw-deserialization-failed";

        private MessageKey() {
        }
    }
}
