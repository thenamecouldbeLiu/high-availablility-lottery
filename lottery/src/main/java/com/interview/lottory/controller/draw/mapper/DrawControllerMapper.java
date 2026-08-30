package com.interview.lottory.controller.draw.mapper;

import com.interview.lottory.controller.draw.dto.DrawAcceptedVo;
import com.interview.lottory.controller.draw.dto.DrawEventStatusVo;
import com.interview.lottory.controller.draw.dto.DrawItemResponseVo;
import com.interview.lottory.controller.draw.dto.DrawRequest;
import com.interview.lottory.controller.draw.dto.AvailableCampaignVo;
import com.interview.lottory.controller.draw.dto.AvailablePrizeVo;
import com.interview.lottory.service.campaign.dto.CampaignBo;
import com.interview.lottory.service.campaign.dto.PrizeConfigBo;
import com.interview.lottory.service.draw.dto.DrawAcceptedBo;
import com.interview.lottory.service.draw.dto.DrawCommandBo;
import com.interview.lottory.service.draw.dto.DrawEventStatusBo;
import com.interview.lottory.service.draw.dto.DrawItemBo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DrawControllerMapper {
    @Mapping(target = "userId", source = "userId")
    DrawCommandBo toBo(DrawRequest request, String userId);
    DrawAcceptedVo toVo(DrawAcceptedBo bo);
    DrawEventStatusVo toVo(DrawEventStatusBo bo);
    java.util.List<DrawEventStatusVo> toVos(java.util.List<DrawEventStatusBo> bos);
    DrawItemResponseVo toVo(DrawItemBo bo);
    AvailableCampaignVo toAvailableVo(CampaignBo bo);
    AvailablePrizeVo toAvailableVo(PrizeConfigBo bo);
    java.util.List<AvailableCampaignVo> toAvailableVos(java.util.List<CampaignBo> bos);

    default String mapId(Long id) {
        return id == null ? null : id.toString();
    }
}
