package com.interview.lottory.controller.campaign.dto;

import com.interview.lottory.enums.PrizeType;
import java.math.BigDecimal;

public record PrizeResponseVo(Long id, String prizeCode, String name, PrizeType prizeType,
                              BigDecimal probability, long totalStock, long remainingStock,
                              int displayOrder, boolean enabled) {
}
