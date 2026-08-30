package com.interview.lottory.controller.draw.dto;

import java.time.Instant;
import java.util.List;

public record AvailableCampaignVo(String id, String campaignCode, String name,
                                  int maxDrawsPerUser, Instant startsAt, Instant endsAt,
                                  List<AvailablePrizeVo> prizes) {
}
