package com.interview.lottory.controller.campaign;

import com.interview.common.response.Response;
import com.interview.lottory.controller.campaign.dto.*;
import com.interview.lottory.controller.campaign.mapper.CampaignControllerMapper;
import com.interview.lottory.service.campaign.CampaignService;
import com.interview.lottory.infra.security.RequireCurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
@RequireCurrentUser(roles = "ADMIN")
public class CampaignController {
    private final CampaignService service;
    private final CampaignControllerMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response<CampaignResponseVo> create(@Valid @RequestBody CreateCampaignRequest request) {
        return Response.success(mapper.toVo(service.create(mapper.toBo(request))));
    }

    @PutMapping("/{campaignId}")
    public Response<CampaignResponseVo> update(@PathVariable Long campaignId,
                                               @Valid @RequestBody UpdateCampaignRequest request) {
        return Response.success(mapper.toVo(service.update(campaignId, mapper.toBo(request))));
    }

    @GetMapping("/{campaignId}")
    public Response<CampaignResponseVo> get(@PathVariable Long campaignId) {
        return Response.success(mapper.toVo(service.get(campaignId)));
    }

    @PostMapping("/{campaignId}/prizes")
    @ResponseStatus(HttpStatus.CREATED)
    public Response<PrizeResponseVo> addPrize(@PathVariable Long campaignId,
                                              @Valid @RequestBody PrizeConfigRequest request) {
        return Response.success(mapper.toVo(service.addPrize(campaignId, mapper.toBo(request))));
    }

    @PutMapping("/{campaignId}/prizes/{prizeId}")
    public Response<PrizeResponseVo> updatePrize(@PathVariable Long campaignId, @PathVariable Long prizeId,
                                                 @Valid @RequestBody PrizeConfigRequest request) {
        return Response.success(mapper.toVo(service.updatePrize(campaignId, prizeId, mapper.toBo(request))));
    }

    @PostMapping("/{campaignId}/activate")
    public Response<CampaignResponseVo> activate(@PathVariable Long campaignId) {
        return Response.success(mapper.toVo(service.activate(campaignId)));
    }

    @PutMapping("/{campaignId}/status")
    public Response<CampaignResponseVo> changeStatus(@PathVariable Long campaignId,
                                                     @Valid @RequestBody ChangeCampaignStatusRequest request) {
        return Response.success(mapper.toVo(service.changeStatus(campaignId, request.status())));
    }

    @DeleteMapping("/{campaignId}/prizes/{prizeId}")
    public Response<Void> deletePrize(@PathVariable Long campaignId, @PathVariable Long prizeId) {
        service.deletePrize(campaignId, prizeId);
        return Response.success();
    }

    @DeleteMapping("/{campaignId}")
    public Response<Void> deleteCampaign(@PathVariable Long campaignId) {
        service.deleteCampaign(campaignId);
        return Response.success();
    }
}
