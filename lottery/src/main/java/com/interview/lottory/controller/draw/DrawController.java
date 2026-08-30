package com.interview.lottory.controller.draw;

import com.interview.common.response.Response;
import com.interview.lottory.controller.draw.dto.DrawAcceptedVo;
import com.interview.lottory.controller.draw.dto.DrawEventStatusVo;
import com.interview.lottory.controller.draw.dto.DrawRequest;
import com.interview.lottory.controller.draw.dto.AvailableCampaignVo;
import com.interview.lottory.controller.draw.mapper.DrawControllerMapper;
import com.interview.lottory.service.draw.DrawEventQueryService;
import com.interview.lottory.service.draw.DrawService;
import com.interview.lottory.service.campaign.CampaignService;
import com.interview.lottory.service.sse.DrawSseService;
import com.interview.lottory.infra.security.CurrentUserUtil;
import com.interview.lottory.infra.security.RequireCurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Lottery Draw", description = "查詢可用活動、送出非同步抽獎及查詢抽獎結果")
public class DrawController {
    private final DrawService service;
    private final DrawEventQueryService queryService;
    private final DrawSseService sseService;
    private final CampaignService campaignService;
    private final DrawControllerMapper mapper;

    @GetMapping("/campaigns")
    @Operation(summary = "查詢可用活動", description = "回傳目前為 ACTIVE 且在有效期間內的活動與啟用獎項。")
    @ApiResponse(responseCode = "200", description = "成功取得可用活動")
    public Response<List<AvailableCampaignVo>> availableCampaigns() {
        return Response.success(mapper.toAvailableVos(campaignService.getAvailableCampaigns()));
    }

    @PostMapping("/draw")
    @Operation(summary = "送出抽獎請求", description = """
            非同步接受抽獎並回傳 eventId。requestId 是由前端產生的冪等鍵，用來避免重複點擊或網路重試造成重複抽獎；
            同一次操作重試時必須沿用相同 requestId，新的抽獎操作才產生新的 requestId（建議使用 UUID）。
            後續可使用 eventId 查詢結果或訂閱 SSE。
            """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = DrawRequest.class), examples = @ExampleObject(value = """
                    {"requestId":"550e8400-e29b-41d4-a716-446655440000","campaignId":123456789,"drawCount":1}
                    """))))
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "抽獎請求已接受，回傳 eventId 與 PENDING 狀態"),
            @ApiResponse(responseCode = "400", description = "requestId、campaignId 或 drawCount 不合法"),
            @ApiResponse(responseCode = "404", description = "活動不存在"),
            @ApiResponse(responseCode = "409", description = "重複 requestId 且既有事件尚無法取得"),
            @ApiResponse(responseCode = "422", description = "活動未啟用或抽獎額度已用完")
    })
    public ResponseEntity<Response<DrawAcceptedVo>> draw(@Valid @RequestBody DrawRequest request) {
        return ResponseEntity.accepted()
                .body(Response.success(mapper.toVo(service.submit(
                        mapper.toBo(request, CurrentUserUtil.currentSubject())))));
    }

    @GetMapping(value = "/events/{eventId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "訂閱抽獎狀態", description = "以 SSE 推送 draw-status 事件，直到狀態為 COMPLETED 或 FAILED。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 連線建立成功",
                    content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            examples = @ExampleObject(value = "event:draw-status\nid:0198f123-4567-7abc-8123-123456789abc:COMPLETED\ndata:{...}\n"))),
            @ApiResponse(responseCode = "400", description = "事件不存在或不屬於目前使用者")
    })
    public SseEmitter stream(
            @Parameter(description = "抽獎事件 ID", example = "0198f123-4567-7abc-8123-123456789abc")
            @PathVariable UUID eventId) {
        return sseService.subscribe(eventId, CurrentUserUtil.currentSubject());
    }

    @GetMapping("/events/{eventId}")
    @Operation(summary = "查詢抽獎事件", description = "查詢目前使用者的抽獎狀態與結果。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功取得事件"),
            @ApiResponse(responseCode = "400", description = "事件不存在或不屬於目前使用者")
    })
    public Response<DrawEventStatusVo> event(
            @Parameter(description = "抽獎事件 ID", example = "0198f123-4567-7abc-8123-123456789abc")
            @PathVariable UUID eventId) {
        return Response.success(mapper.toVo(
                queryService.getEventByEventIdAndUserId(eventId, CurrentUserUtil.currentSubject())));
    }

    @GetMapping("/users/me/draws")
    @Operation(summary = "查詢我的抽獎紀錄", description = "依建立時間由新到舊回傳目前使用者的抽獎紀錄，可選擇限定活動。")
    @ApiResponse(responseCode = "200", description = "成功取得抽獎紀錄")
    public Response<List<DrawEventStatusVo>> history(
            @Parameter(description = "選填的活動 ID", example = "123456789")
            @RequestParam(required = false) Long campaignId,
            @Parameter(description = "回傳筆數，範圍 1 到 100", example = "20")
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
        return Response.success(
                mapper.toVos(queryService.getUserEventHistoryByCampaignId(
                        CurrentUserUtil.currentSubject(), campaignId, limit)));
    }
}
