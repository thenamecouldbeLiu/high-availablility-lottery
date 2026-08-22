package com.interview.yuzhi.controller.draw;

import com.interview.yuzhi.controller.draw.dto.DrawRequest;
import com.interview.yuzhi.controller.draw.dto.DrawResponseVo;
import com.interview.yuzhi.controller.draw.mapper.DrawControllerMapper;
import com.interview.yuzhi.service.draw.DrawService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
public class DrawController {
    private final DrawService service;
    private final DrawControllerMapper mapper;

    @PostMapping("/draw")
    public DrawResponseVo draw(@Valid @RequestBody DrawRequest request) {
        return mapper.toVo(service.draw(mapper.toBo(request)));
    }
}
