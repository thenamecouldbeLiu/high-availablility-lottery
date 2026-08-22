package com.interview.yuzhi.controller.campaign.dto;

import com.interview.yuzhi.domain.CampaignStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeCampaignStatusRequest(@NotNull CampaignStatus status) {
}
