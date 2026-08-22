package com.interview.yuzhi.service.campaign.mapper;

import com.interview.yuzhi.domain.CampaignStatus;
import com.interview.yuzhi.domain.LotteryCampaign;
import com.interview.yuzhi.domain.LotteryPrize;
import com.interview.yuzhi.service.campaign.dto.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CampaignEntityMapper {
    CampaignBo toBo(LotteryCampaign entity);
    PrizeConfigBo toBo(LotteryPrize entity);
    List<PrizeConfigBo> toPrizeBos(List<LotteryPrize> entities);

    @Mapping(target = "status", constant = "DRAFT")
    LotteryCampaign toEntity(CreateCampaignBo bo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campaignId", source = "campaignId")
    @Mapping(target = "remainingStock", source = "bo.totalStock")
    LotteryPrize toEntity(PrizeConfigBo bo, Long campaignId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCampaign(UpdateCampaignBo bo, @MappingTarget LotteryCampaign entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campaignId", ignore = true)
    void updatePrize(PrizeConfigBo bo, @MappingTarget LotteryPrize entity);

    default void changeStatus(CampaignStatus status, LotteryCampaign entity) {
        entity.setStatus(status);
    }
}
