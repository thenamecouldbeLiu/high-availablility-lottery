package com.interview.lottory.service.draw.dto;

import com.interview.lottory.domain.CampaignStatus;
import java.time.Instant;

public record DrawCampaignBo(Long id, CampaignStatus status, int maxDrawsPerUser,
                             Instant startsAt, Instant endsAt) {
}
