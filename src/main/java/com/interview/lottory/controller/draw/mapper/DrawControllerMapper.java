package com.interview.lottory.controller.draw.mapper;

import com.interview.lottory.controller.draw.dto.DrawAcceptedVo;
import com.interview.lottory.controller.draw.dto.DrawEventStatusVo;
import com.interview.lottory.controller.draw.dto.DrawItemResponseVo;
import com.interview.lottory.controller.draw.dto.DrawRequest;
import com.interview.lottory.service.draw.dto.DrawAcceptedBo;
import com.interview.lottory.service.draw.dto.DrawCommandBo;
import com.interview.lottory.service.draw.dto.DrawEventStatusBo;
import com.interview.lottory.service.draw.dto.DrawItemBo;
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
