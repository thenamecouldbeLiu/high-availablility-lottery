package com.interview.yuzhi.service.campaign.dto;

import com.interview.yuzhi.domain.CampaignStatus;

import java.time.Instant;
import java.util.List;

public record CampaignBo(Long id, String campaignCode, String name, CampaignStatus status,
                         int maxDrawsPerUser, Instant startsAt, Instant endsAt,
                         List<PrizeConfigBo> prizes) {
}
