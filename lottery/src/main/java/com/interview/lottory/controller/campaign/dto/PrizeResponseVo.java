package com.interview.lottory.controller.campaign.dto;

import com.interview.lottory.enums.PrizeType;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

public record PrizeResponseVo(@Schema(example = "987654321") Long id,
                              @Schema(example = "ANNIVERSARY_IPHONE") String prizeCode,
                              @Schema(example = "iPhone") String name,
                              @Schema(example = "PRIZE") PrizeType prizeType,
                              BigDecimal probability, long totalStock, long remainingStock,
                              int displayOrder, boolean enabled) {
}
