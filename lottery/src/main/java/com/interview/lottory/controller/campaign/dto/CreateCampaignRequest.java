package com.interview.lottory.controller.campaign.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "建立活動 request")
public record CreateCampaignRequest(
        @Schema(description = "唯一活動代碼", example = "ANNIVERSARY_2026")
        @NotBlank @Size(max = 64) String campaignCode,
        @Schema(description = "活動名稱", example = "2026 週年慶幸運抽獎")
        @NotBlank @Size(max = 128) String name,
        @Schema(description = "每位使用者最多抽獎次數", example = "5")
        @Min(1) int maxDrawsPerUser,
        @Schema(description = "活動開始時間", example = "2026-01-01T00:00:00Z")
        @NotNull Instant startsAt,
        @Schema(description = "活動結束時間，必須晚於開始時間", example = "2030-12-31T23:59:59Z")
        @NotNull Instant endsAt) {
}
