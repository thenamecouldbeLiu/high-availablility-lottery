package com.interview.lottory.controller.draw.dto;

import com.interview.lottory.enums.LotteryEventStatus;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "已接受的抽獎事件")
public record DrawAcceptedVo(
        @Schema(example = "0198f123-4567-7abc-8123-123456789abc") UUID eventId,
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000") String requestId,
        @Schema(example = "PENDING") LotteryEventStatus status) {
}
