package com.interview.yuzhi.service.draw.dto;

import java.util.UUID;

public record LotteryEventMessageBo(UUID eventId, String requestId, Long campaignId,
                                    String userId, String eventType, int drawCount,
                                    String resultPayload) {
}
