package com.interview.lottory.controller.draw.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "非同步抽獎請求")
public record DrawRequest(
        @Schema(description = "前端產生的冪等鍵；同一次操作重試必須沿用相同值", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank @Size(max = 128) String requestId,
        @Schema(description = "活動 ID", example = "123456789")
        @NotNull Long campaignId,
        @Schema(description = "本次抽獎次數", example = "1", minimum = "1")
        @Min(1) int drawCount) {
}
