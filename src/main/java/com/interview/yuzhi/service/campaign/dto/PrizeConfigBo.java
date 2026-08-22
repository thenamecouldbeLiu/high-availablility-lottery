package com.interview.yuzhi.service.campaign.dto;

import com.interview.yuzhi.domain.PrizeType;

import java.math.BigDecimal;

public record PrizeConfigBo(Long id, Long campaignId, String prizeCode, String name,
                            PrizeType prizeType, BigDecimal probability, long totalStock,
                            long remainingStock, int displayOrder, boolean enabled) {
}
