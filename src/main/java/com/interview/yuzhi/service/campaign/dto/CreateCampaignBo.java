package com.interview.yuzhi.service.campaign.dto;

import java.time.Instant;

public record CreateCampaignBo(String campaignCode, String name, int maxDrawsPerUser,
                               Instant startsAt, Instant endsAt) {
}
