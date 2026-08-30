package com.interview.lottory.controller.draw.dto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "單次抽獎結果")
public record DrawItemResponseVo(
        @Schema(example = "1") int sequence,
        @Schema(example = "987654321", type = "string", nullable = true) String prizeId,
        @Schema(example = "ANNIVERSARY_IPHONE") String prizeCode,
        @Schema(example = "iPhone") String prizeName,
        @Schema(example = "true") boolean won) {
}
