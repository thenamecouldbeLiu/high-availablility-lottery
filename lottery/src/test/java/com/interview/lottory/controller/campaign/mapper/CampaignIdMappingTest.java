package com.interview.lottory.controller.campaign.mapper;

import com.interview.lottory.domain.LotteryCampaign;
import com.interview.lottory.domain.LotteryPrize;
import com.interview.lottory.enums.CampaignStatus;
import com.interview.lottory.enums.PrizeType;
import com.interview.lottory.service.campaign.mapper.CampaignEntityMapperImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CampaignIdMappingTest {
    private final CampaignEntityMapperImpl entityMapper = new CampaignEntityMapperImpl();
    private final CampaignControllerMapperImpl controllerMapper = new CampaignControllerMapperImpl();

    @Test
    void campaignCreateResultKeepsGeneratedIdThroughResponseMapping() {
        var entity = new LotteryCampaign();
        entity.setCampaignCode("C1");
        entity.setName("Campaign");
        entity.setStatus(CampaignStatus.DRAFT);

        var response = controllerMapper.toVo(entityMapper.toBo(entity));

        assertThat(response.id()).isNotNull().isEqualTo(entity.getId().toString());
    }

    @Test
    void prizeCreateResultKeepsGeneratedIdThroughResponseMapping() {
        var entity = new LotteryPrize();
        entity.setCampaignId(1L);
        entity.setPrizeCode("P1");
        entity.setName("Prize");
        entity.setPrizeType(PrizeType.PRIZE);
        entity.setProbability(BigDecimal.ONE);
        entity.setTotalStock(1);
        entity.setRemainingStock(1);

        var response = controllerMapper.toVo(entityMapper.toBo(entity));

        assertThat(response.id()).isNotNull().isEqualTo(entity.getId().toString());
    }
}
