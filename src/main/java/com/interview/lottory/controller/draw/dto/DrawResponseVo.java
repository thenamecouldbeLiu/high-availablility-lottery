package com.interview.lottory.controller.draw.dto;

import java.util.List;
import java.util.UUID;

public record DrawResponseVo(UUID eventId, String requestId, Long campaignId, String userId,
                             int drawCount, List<DrawItemResponseVo> results) {
}
