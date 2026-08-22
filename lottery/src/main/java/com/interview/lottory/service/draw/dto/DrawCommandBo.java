package com.interview.lottory.service.draw.dto;

public record DrawCommandBo(String requestId, Long campaignId, String userId, int drawCount) {
}
