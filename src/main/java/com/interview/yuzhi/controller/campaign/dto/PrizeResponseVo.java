package com.interview.yuzhi.controller.campaign.dto;

import com.interview.yuzhi.domain.PrizeType;
import java.math.BigDecimal;

public record PrizeResponseVo(Long id, String prizeCode, String name, PrizeType prizeType,
                              BigDecimal probability, long totalStock, long remainingStock,
                              int displayOrder, boolean enabled) {
}
