package com.interview.lottory.controller.draw.mapper;

import com.interview.lottory.controller.draw.dto.*;
import com.interview.lottory.service.draw.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DrawControllerMapper {
    DrawCommandBo toBo(DrawRequest request);
    DrawAcceptedVo toVo(DrawAcceptedBo bo);
    DrawEventStatusVo toVo(DrawEventStatusBo bo);
    java.util.List<DrawEventStatusVo> toVos(java.util.List<DrawEventStatusBo> bos);
    DrawItemResponseVo toVo(DrawItemBo bo);
}
