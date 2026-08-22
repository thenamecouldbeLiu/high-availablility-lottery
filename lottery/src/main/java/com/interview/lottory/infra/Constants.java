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
