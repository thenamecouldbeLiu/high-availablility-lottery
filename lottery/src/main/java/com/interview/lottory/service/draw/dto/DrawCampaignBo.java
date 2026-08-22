package com.interview.lottory.service.draw.dto;

import com.interview.lottory.enums.CampaignStatus;
import java.time.Instant;

public record DrawCampaignBo(Long id, CampaignStatus status, int maxDrawsPerUser,
                             Instant startsAt, Instant endsAt) {
}
