package com.interview.lottory.controller.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "一次建立多個獎項的 request")
public record CreatePrizesRequest(
        @Schema(description = "要建立的獎項；每批最多 100 筆")
        @NotEmpty @Size(max = 100) List<@Valid PrizeConfigRequest> prizes
) {
}
