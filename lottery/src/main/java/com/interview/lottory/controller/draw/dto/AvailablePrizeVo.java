package com.interview.lottory.controller.draw.dto;

import com.interview.lottory.enums.PrizeType;
import io.swagger.v3.oas.annotations.media.Schema;

public record AvailablePrizeVo(
        @Schema(example = "987654321") Long id,
        @Schema(example = "ANNIVERSARY_IPHONE") String prizeCode,
        @Schema(example = "iPhone") String name,
        @Schema(example = "PRIZE") PrizeType prizeType,
        @Schema(example = "1") int displayOrder) {
}
