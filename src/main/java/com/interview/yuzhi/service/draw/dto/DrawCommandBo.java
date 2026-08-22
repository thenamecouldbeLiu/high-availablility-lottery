package com.interview.yuzhi.service.draw.dto;

public record DrawCommandBo(String requestId, Long campaignId, String userId, int drawCount) {
}
