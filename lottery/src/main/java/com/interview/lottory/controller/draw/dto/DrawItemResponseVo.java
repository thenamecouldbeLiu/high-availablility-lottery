package com.interview.lottory.controller.draw.dto;

public record DrawItemResponseVo(int sequence, Long prizeId, String prizeCode,
                                 String prizeName, boolean won) {
}
