package com.interview.lottory.controller.campaign.dto;

import com.interview.lottory.enums.CampaignStatus;
import java.time.Instant;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

public record CampaignResponseVo(@Schema(example = "123456789", type = "string") String id,
                                 @Schema(example = "ANNIVERSARY_2026") String campaignCode,
                                 @Schema(example = "2026 週年慶幸運抽獎") String name,
                                 @Schema(example = "DRAFT") CampaignStatus status,
                                 int maxDrawsPerUser, Instant startsAt, Instant endsAt,
                                 List<PrizeResponseVo> prizes) {
}
