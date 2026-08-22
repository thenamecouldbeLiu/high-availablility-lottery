package com.interview.yuzhi.controller.draw.mapper;

import com.interview.yuzhi.controller.draw.dto.*;
import com.interview.yuzhi.service.draw.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DrawControllerMapper {
    DrawCommandBo toBo(DrawRequest request);
    DrawResponseVo toVo(DrawResultBo bo);
    DrawItemResponseVo toVo(DrawItemBo bo);
}
