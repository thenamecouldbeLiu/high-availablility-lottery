package com.interview.lottory.controller.draw;

import com.interview.common.response.Response;
import com.interview.lottory.controller.draw.dto.DrawAcceptedVo;
import com.interview.lottory.controller.draw.dto.DrawEventStatusVo;
import com.interview.lottory.controller.draw.dto.DrawRequest;
import com.interview.lottory.controller.draw.mapper.DrawControllerMapper;
import com.interview.lottory.service.draw.DrawEventQueryService;
import com.interview.lottory.service.draw.DrawService;
import com.interview.lottory.service.sse.DrawSseService;
import com.interview.lottory.infra.security.CurrentUserUtil;
import com.interview.lottory.infra.security.RequireCurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
@RequireCurrentUser(roles = {"ADMIN", "NORMAL_USER"})
public class DrawController {
    private final DrawService service;
    private final DrawEventQueryService queryService;
    private final DrawSseService sseService;
    private final DrawControllerMapper mapper;

    @PostMapping("/draw")
    public ResponseEntity<Response<DrawAcceptedVo>> draw(@Valid @RequestBody DrawRequest request) {
        return ResponseEntity.accepted()
                .body(Response.success(mapper.toVo(service.submit(
                        mapper.toBo(request, CurrentUserUtil.currentSubject())))));
    }

    @GetMapping(value = "/events/{eventId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable UUID eventId) {
        return sseService.subscribe(eventId, CurrentUserUtil.currentSubject());
    }

    @GetMapping("/events/{eventId}")
    public Response<DrawEventStatusVo> event(
            @PathVariable UUID eventId) {
        return Response.success(mapper.toVo(
                queryService.getEventByEventIdAndUserId(eventId, CurrentUserUtil.currentSubject())));
    }

    @GetMapping("/users/me/draws")
    public Response<List<DrawEventStatusVo>> history(
            @RequestParam(required = false) Long campaignId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
        return Response.success(
                mapper.toVos(queryService.getUserEventHistoryByCampaignId(
                        CurrentUserUtil.currentSubject(), campaignId, limit)));
    }
}
