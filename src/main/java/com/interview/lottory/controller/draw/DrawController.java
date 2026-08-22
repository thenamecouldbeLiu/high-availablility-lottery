package com.interview.lottory.controller.draw;

import com.interview.lottory.controller.draw.dto.DrawRequest;
import com.interview.lottory.controller.draw.dto.DrawAcceptedVo;
import com.interview.lottory.controller.draw.dto.DrawEventStatusVo;
import com.interview.lottory.controller.draw.mapper.DrawControllerMapper;
import com.interview.lottory.service.draw.DrawService;
import com.interview.lottory.service.draw.DrawEventQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class DrawController {
    private final DrawService service;
    private final DrawEventQueryService queryService;
    private final DrawSseService sseService;
    private final DrawControllerMapper mapper;

    @PostMapping("/draw")
    public ResponseEntity<DrawAcceptedVo> draw(@Valid @RequestBody DrawRequest request) {
        return ResponseEntity.accepted().body(mapper.toVo(service.submit(mapper.toBo(request))));
    }

    @GetMapping(value = "/events/{eventId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable UUID eventId, @RequestParam @NotBlank String userId) {
        return sseService.subscribe(eventId, userId);
    }

    @GetMapping("/events/{eventId}")
    public DrawEventStatusVo event(@PathVariable UUID eventId, @RequestParam @NotBlank String userId) {
        return mapper.toVo(queryService.get(eventId, userId));
    }

    @GetMapping("/users/{userId}/draws")
    public List<DrawEventStatusVo> history(
            @PathVariable @NotBlank String userId,
            @RequestParam(required = false) Long campaignId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
        return mapper.toVos(queryService.history(userId, campaignId, limit));
    }
}
