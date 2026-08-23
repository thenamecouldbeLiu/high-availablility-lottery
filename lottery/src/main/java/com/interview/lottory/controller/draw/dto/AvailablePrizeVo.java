package com.interview.lottory.controller.draw.dto;

import com.interview.lottory.enums.PrizeType;

public record AvailablePrizeVo(Long id, String prizeCode, String name,
                               PrizeType prizeType, int displayOrder) {
}
