package com.interview.lottory.service.draw.dto;

public record DrawItemBo(int sequence, Long prizeId, String prizeCode, String prizeName, boolean won) {
}
