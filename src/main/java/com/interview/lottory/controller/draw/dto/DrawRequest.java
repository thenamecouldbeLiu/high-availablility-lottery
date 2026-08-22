package com.interview.lottory.controller.draw.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DrawRequest(
        @NotBlank @Size(max = 128) String requestId,
        @NotNull Long campaignId,
        @NotBlank @Size(max = 128) String userId,
        @Min(1) int drawCount) {
}
