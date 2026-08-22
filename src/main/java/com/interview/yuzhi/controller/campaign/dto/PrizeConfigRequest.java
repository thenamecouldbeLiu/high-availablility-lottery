package com.interview.yuzhi.controller.campaign.dto;

import com.interview.yuzhi.domain.PrizeType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PrizeConfigRequest(
        @NotBlank @Size(max = 64) String prizeCode,
        @NotBlank @Size(max = 128) String name,
        @NotNull PrizeType prizeType,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") @Digits(integer = 1, fraction = 7)
        BigDecimal probability,
        @PositiveOrZero long totalStock,
        @PositiveOrZero int displayOrder,
        boolean enabled) {
}
