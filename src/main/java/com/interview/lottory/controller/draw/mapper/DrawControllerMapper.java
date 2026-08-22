package com.interview.lottory.controller.draw.mapper;

import com.interview.lottory.controller.draw.dto.*;
import com.interview.lottory.service.draw.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DrawControllerMapper {
    DrawCommandBo toBo(DrawRequest request);
    DrawResponseVo toVo(DrawResultBo bo);
    DrawItemResponseVo toVo(DrawItemBo bo);
}
