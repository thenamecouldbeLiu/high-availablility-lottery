package com.interview.lottory.service.campaign.dto;

import java.time.Instant;

public record UpdateCampaignBo(String name, int maxDrawsPerUser, Instant startsAt, Instant endsAt) {
}
