package com.interview.lottory.service.campaign.dto;

import com.interview.lottory.enums.PrizeType;

import java.math.BigDecimal;

public record PrizeConfigBo(Long id, Long campaignId, String prizeCode, String name,
                            PrizeType prizeType, BigDecimal probability, long totalStock,
                            long remainingStock, int displayOrder, boolean enabled) {
}
