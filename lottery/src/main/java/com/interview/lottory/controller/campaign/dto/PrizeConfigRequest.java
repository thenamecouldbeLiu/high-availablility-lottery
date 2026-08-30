package com.interview.lottory.controller.campaign.dto;

import com.interview.lottory.enums.PrizeType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "建立或更新獎項 request")
public record PrizeConfigRequest(
        @Schema(description = "活動內唯一的獎項代碼", example = "ANNIVERSARY_IPHONE")
        @NotBlank @Size(max = 64) String prizeCode,
        @Schema(description = "獎項名稱", example = "iPhone")
        @NotBlank @Size(max = 128) String name,
        @Schema(description = "PRIZE 為實體獎項，NO_PRIZE 為未中獎", example = "PRIZE")
        @NotNull PrizeType prizeType,
        @Schema(description = "中獎機率；所有啟用獎項總和須為 1.0", example = "0.01")
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") @Digits(integer = 1, fraction = 7)
        BigDecimal probability,
        @Schema(description = "總庫存；實體獎項必須大於 0，未中獎可為 0", example = "10")
        @PositiveOrZero long totalStock,
        @Schema(description = "顯示順序", example = "1")
        @PositiveOrZero int displayOrder,
        @Schema(description = "是否啟用此獎項", example = "true")
        boolean enabled) {
}
