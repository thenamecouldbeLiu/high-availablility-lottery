package com.interview.lottory.controller.campaign.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record CreateCampaignRequest(
        @NotBlank @Size(max = 64) String campaignCode,
        @NotBlank @Size(max = 128) String name,
        @Min(1) int maxDrawsPerUser,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt) {
}
