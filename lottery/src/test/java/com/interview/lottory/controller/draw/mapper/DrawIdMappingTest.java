package com.interview.lottory.controller.draw.mapper;

import com.interview.lottory.enums.LotteryEventStatus;
import com.interview.lottory.service.draw.dto.DrawEventStatusBo;
import com.interview.lottory.service.draw.dto.DrawItemBo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DrawIdMappingTest {
    private final DrawControllerMapperImpl mapper = new DrawControllerMapperImpl();

    @Test
    void convertsCampaignAndPrizeLongIdsToStrings() {
        var item = new DrawItemBo(1, 987654321L, "P1", "Prize", true);
        var status = new DrawEventStatusBo(UUID.randomUUID(), "request", 123456789L, "user", 1,
                LotteryEventStatus.COMPLETED, null, List.of(item), null, null);

        var response = mapper.toVo(status);

        assertThat(response.campaignId()).isEqualTo("123456789");
        assertThat(response.results()).singleElement()
                .satisfies(result -> assertThat(result.prizeId()).isEqualTo("987654321"));
    }
}
