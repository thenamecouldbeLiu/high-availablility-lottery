package com.interview.lottory.controller.campaign.dto;

import com.interview.lottory.domain.CampaignStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeCampaignStatusRequest(@NotNull CampaignStatus status) {
}
