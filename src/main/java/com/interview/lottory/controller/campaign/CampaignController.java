package com.interview.lottory.controller.campaign;

import com.interview.lottory.controller.campaign.dto.*;
import com.interview.lottory.controller.campaign.mapper.CampaignControllerMapper;
import com.interview.lottory.service.campaign.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService service;
    private final CampaignControllerMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponseVo create(@Valid @RequestBody CreateCampaignRequest request) {
        return mapper.toVo(service.create(mapper.toBo(request)));
    }

    @PutMapping("/{campaignId}")
    public CampaignResponseVo update(@PathVariable Long campaignId,
                                     @Valid @RequestBody UpdateCampaignRequest request) {
        return mapper.toVo(service.update(campaignId, mapper.toBo(request)));
    }

    @GetMapping("/{campaignId}")
    public CampaignResponseVo get(@PathVariable Long campaignId) {
        return mapper.toVo(service.get(campaignId));
    }

    @PostMapping("/{campaignId}/prizes")
    @ResponseStatus(HttpStatus.CREATED)
    public PrizeResponseVo addPrize(@PathVariable Long campaignId,
                                    @Valid @RequestBody PrizeConfigRequest request) {
        return mapper.toVo(service.addPrize(campaignId, mapper.toBo(request)));
    }

    @PutMapping("/{campaignId}/prizes/{prizeId}")
    public PrizeResponseVo updatePrize(@PathVariable Long campaignId, @PathVariable Long prizeId,
                                       @Valid @RequestBody PrizeConfigRequest request) {
        return mapper.toVo(service.updatePrize(campaignId, prizeId, mapper.toBo(request)));
    }

    @PostMapping("/{campaignId}/activate")
    public CampaignResponseVo activate(@PathVariable Long campaignId) {
        return mapper.toVo(service.activate(campaignId));
    }

    @PutMapping("/{campaignId}/status")
    public CampaignResponseVo changeStatus(@PathVariable Long campaignId,
                                           @Valid @RequestBody ChangeCampaignStatusRequest request) {
        return mapper.toVo(service.changeStatus(campaignId, request.status()));
    }

    @DeleteMapping("/{campaignId}/prizes/{prizeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePrize(@PathVariable Long campaignId, @PathVariable Long prizeId) {
        service.deletePrize(campaignId, prizeId);
    }
}
