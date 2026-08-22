package com.interview.yuzhi.service.draw.dto;

import com.interview.yuzhi.domain.PrizeType;
import java.math.BigDecimal;

public record DrawPrizeBo(Long id, String prizeCode, String name, PrizeType prizeType,
                          BigDecimal probability, long remainingStock) {
}
