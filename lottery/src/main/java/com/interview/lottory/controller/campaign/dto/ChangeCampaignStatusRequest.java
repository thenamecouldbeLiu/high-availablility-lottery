package com.interview.lottory.controller.campaign.dto;

import com.interview.lottory.enums.CampaignStatus;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "變更活動狀態 request")
public record ChangeCampaignStatusRequest(
        @Schema(description = "目標狀態", example = "PAUSED") @NotNull CampaignStatus status) {
}
