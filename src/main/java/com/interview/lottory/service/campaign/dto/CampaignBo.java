package com.interview.lottory.service.campaign.dto;

import com.interview.lottory.enums.CampaignStatus;

import java.time.Instant;
import java.util.List;

public record CampaignBo(Long id, String campaignCode, String name, CampaignStatus status,
                         int maxDrawsPerUser, Instant startsAt, Instant endsAt,
                         List<PrizeConfigBo> prizes) {
}
