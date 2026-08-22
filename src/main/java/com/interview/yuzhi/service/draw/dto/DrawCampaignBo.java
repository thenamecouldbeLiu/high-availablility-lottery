package com.interview.yuzhi.service.draw.dto;

import com.interview.yuzhi.domain.CampaignStatus;
import java.time.Instant;

public record DrawCampaignBo(Long id, CampaignStatus status, int maxDrawsPerUser,
                             Instant startsAt, Instant endsAt) {
}
