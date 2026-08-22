package com.interview.lottory.controller.campaign.dto;

import com.interview.lottory.enums.CampaignStatus;
import java.time.Instant;
import java.util.List;

public record CampaignResponseVo(Long id, String campaignCode, String name, CampaignStatus status,
                                 int maxDrawsPerUser, Instant startsAt, Instant endsAt,
                                 List<PrizeResponseVo> prizes) {
}
