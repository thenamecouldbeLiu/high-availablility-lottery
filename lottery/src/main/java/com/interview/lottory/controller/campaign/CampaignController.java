package com.interview.lottory.controller.campaign;

import com.interview.common.response.Response;
import com.interview.lottory.controller.campaign.dto.*;
import com.interview.lottory.controller.campaign.mapper.CampaignControllerMapper;
import com.interview.lottory.service.campaign.CampaignService;
import com.interview.lottory.infra.security.RequireCurrentUser;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
@RequireCurrentUser(roles = "ADMIN")
@Tag(name = "Campaign Administration", description = "建立、調整、啟用及刪除抽獎活動與獎項；僅限 ADMIN")
public class CampaignController {
    private final CampaignService service;
    private final CampaignControllerMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "建立活動", description = "建立 DRAFT 活動；成功 response 的 data.id 為新活動 ID。")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "活動建立成功，回傳包含 ID 的活動"),
            @ApiResponse(responseCode = "400", description = "欄位或活動期間不合法"),
            @ApiResponse(responseCode = "409", description = "campaignCode 已存在")
    })
    public Response<CampaignResponseVo> create(@Valid @RequestBody CreateCampaignRequest request) {
        return Response.success(mapper.toVo(service.create(mapper.toBo(request))));
    }

    @PutMapping("/{campaignId}")
    @Operation(summary = "更新活動", description = "更新名稱、每人抽獎上限及活動期間。")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "活動不存在")})
    public Response<CampaignResponseVo> update(
                                               @Parameter(description = "活動 ID", example = "123456789")
                                               @PathVariable Long campaignId,
                                               @Valid @RequestBody UpdateCampaignRequest request) {
        return Response.success(mapper.toVo(service.update(campaignId, mapper.toBo(request))));
    }

    @GetMapping("/{campaignId}")
    @Operation(summary = "查詢活動", description = "回傳活動及其所有未刪除獎項。")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "404", description = "活動不存在")})
    public Response<CampaignResponseVo> get(
            @Parameter(description = "活動 ID", example = "123456789") @PathVariable Long campaignId) {
        return Response.success(mapper.toVo(service.get(campaignId)));
    }

    @PostMapping("/{campaignId}/prizes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增獎項", description = "新增活動獎項；成功 response 的 data.id 為新獎項 ID。")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "獎項建立成功，回傳包含 ID 的獎項"),
            @ApiResponse(responseCode = "404", description = "活動不存在"),
            @ApiResponse(responseCode = "400", description = "獎項設定不合法")})
    public Response<PrizeResponseVo> addPrize(
                                              @Parameter(description = "活動 ID", example = "123456789")
                                              @PathVariable Long campaignId,
                                              @Valid @RequestBody PrizeConfigRequest request) {
        return Response.success(mapper.toVo(service.addPrize(campaignId, mapper.toBo(request))));
    }

    @PostMapping("/{campaignId}/prizes/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "批次新增獎項", description = "在同一個 transaction 一次建立全部獎項；response data 中每筆都包含字串 ID。")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "整批建立成功，回傳所有包含 ID 的新獎項"),
            @ApiResponse(responseCode = "400", description = "清單為空、獎項代碼重複或獎項設定不合法"),
            @ApiResponse(responseCode = "404", description = "活動不存在")
    })
    public Response<java.util.List<PrizeResponseVo>> addPrizes(
            @Parameter(description = "活動 ID", example = "123456789") @PathVariable Long campaignId,
            @Valid @RequestBody CreatePrizesRequest request) {
        return Response.success(mapper.toVos(service.addPrizes(campaignId, mapper.toBos(request.prizes()))));
    }

    @PutMapping("/{campaignId}/prizes/{prizeId}")
    @Operation(summary = "更新獎項", description = "更新獎項內容；庫存不可低於已發出的數量。")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "活動或獎項不存在")})
    public Response<PrizeResponseVo> updatePrize(
                                                 @Parameter(description = "活動 ID", example = "123456789")
                                                 @PathVariable Long campaignId,
                                                 @Parameter(description = "獎項 ID", example = "987654321")
                                                 @PathVariable Long prizeId,
                                                 @Valid @RequestBody PrizeConfigRequest request) {
        return Response.success(mapper.toVo(service.updatePrize(campaignId, prizeId, mapper.toBo(request))));
    }

    @PostMapping("/{campaignId}/activate")
    @Operation(summary = "啟用活動", description = "活動必須有 3 個實體獎項與 1 個未中獎項，啟用獎項機率總和須為 1.0。")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "活動啟用成功"),
            @ApiResponse(responseCode = "400", description = "獎項設定不符合啟用條件"),
            @ApiResponse(responseCode = "404", description = "活動不存在")})
    public Response<CampaignResponseVo> activate(
            @Parameter(description = "活動 ID", example = "123456789") @PathVariable Long campaignId) {
        return Response.success(mapper.toVo(service.activate(campaignId)));
    }

    @PutMapping("/{campaignId}/status")
    @Operation(summary = "變更活動狀態", description = "將活動設為 DRAFT、ACTIVE、PAUSED 或 ENDED；改為 ACTIVE 時會驗證獎項設定。")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "狀態變更成功"),
            @ApiResponse(responseCode = "404", description = "活動不存在")})
    public Response<CampaignResponseVo> changeStatus(
                                                     @Parameter(description = "活動 ID", example = "123456789")
                                                     @PathVariable Long campaignId,
                                                     @Valid @RequestBody ChangeCampaignStatusRequest request) {
        return Response.success(mapper.toVo(service.changeStatus(campaignId, request.status())));
    }

    @DeleteMapping("/{campaignId}/prizes/{prizeId}")
    @Operation(summary = "刪除獎項", description = "軟刪除指定獎項；若活動仍為 ACTIVE，刪除後設定仍須符合啟用規則。")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "刪除成功"),
            @ApiResponse(responseCode = "404", description = "活動或獎項不存在")})
    public Response<Void> deletePrize(
            @Parameter(description = "活動 ID", example = "123456789") @PathVariable Long campaignId,
            @Parameter(description = "獎項 ID", example = "987654321") @PathVariable Long prizeId) {
        service.deletePrize(campaignId, prizeId);
        return Response.success();
    }

    @DeleteMapping("/{campaignId}")
    @Operation(summary = "刪除活動", description = "軟刪除活動與其所有獎項。")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "刪除成功"),
            @ApiResponse(responseCode = "404", description = "活動不存在")})
    public Response<Void> deleteCampaign(
            @Parameter(description = "活動 ID", example = "123456789") @PathVariable Long campaignId) {
        service.deleteCampaign(campaignId);
        return Response.success();
    }
}
