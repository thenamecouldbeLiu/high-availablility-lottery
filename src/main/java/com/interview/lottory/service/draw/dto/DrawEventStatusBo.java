package com.interview.lottory.service.draw.dto;

import com.interview.lottory.enums.LotteryEventStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DrawEventStatusBo(UUID eventId, String requestId, Long campaignId, String userId,
                                int drawCount, LotteryEventStatus status, String failureCode,
                                List<DrawItemBo> results, Instant createdAt, Instant processedAt) {
    public boolean terminal() {
        return status == LotteryEventStatus.COMPLETED || status == LotteryEventStatus.FAILED;
    }
}
