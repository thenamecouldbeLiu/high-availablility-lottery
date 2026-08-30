package com.interview.lottory.controller.campaign.mapper;

import com.interview.lottory.controller.campaign.dto.*;
import com.interview.lottory.service.campaign.dto.CampaignBo;
import com.interview.lottory.service.campaign.dto.CreateCampaignBo;
import com.interview.lottory.service.campaign.dto.PrizeConfigBo;
import com.interview.lottory.service.campaign.dto.UpdateCampaignBo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CampaignControllerMapper {
    CreateCampaignBo toBo(CreateCampaignRequest request);
    UpdateCampaignBo toBo(UpdateCampaignRequest request);

    @Mapping(target = "remainingStock", source = "totalStock")
    PrizeConfigBo toBo(PrizeConfigRequest request);

    java.util.List<PrizeConfigBo> toBos(java.util.List<PrizeConfigRequest> requests);

    @Mapping(target = "id", source = "id")
    CampaignResponseVo toVo(CampaignBo bo);

    @Mapping(target = "id", source = "id")
    PrizeResponseVo toVo(PrizeConfigBo bo);

    java.util.List<PrizeResponseVo> toVos(java.util.List<PrizeConfigBo> bos);

    default String mapId(Long id) {
        return id == null ? null : id.toString();
    }
}
