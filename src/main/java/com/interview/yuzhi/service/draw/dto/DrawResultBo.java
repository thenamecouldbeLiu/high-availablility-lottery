package com.interview.yuzhi.service.draw.dto;

import java.util.List;
import java.util.UUID;

public record DrawResultBo(UUID eventId, String requestId, Long campaignId, String userId,
                           int drawCount, List<DrawItemBo> results) {
}
