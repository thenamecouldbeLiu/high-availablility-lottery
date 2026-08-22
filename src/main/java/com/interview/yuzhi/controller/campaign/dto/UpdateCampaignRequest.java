package com.interview.yuzhi.controller.campaign.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record UpdateCampaignRequest(
        @NotBlank @Size(max = 128) String name,
        @Min(1) int maxDrawsPerUser,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt) {
}
