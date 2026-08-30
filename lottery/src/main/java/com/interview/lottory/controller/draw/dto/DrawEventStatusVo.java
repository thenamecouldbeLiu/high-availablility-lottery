package com.interview.lottory.controller.draw.dto;

import com.interview.lottory.enums.LotteryEventStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DrawEventStatusVo(UUID eventId, String requestId, String campaignId, String userId,
                                int drawCount, LotteryEventStatus status, String failureCode,
                                List<DrawItemResponseVo> results, Instant createdAt, Instant processedAt) {
}
