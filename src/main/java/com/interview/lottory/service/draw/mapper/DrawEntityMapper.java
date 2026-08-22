package com.interview.lottory.service.draw.mapper;

import com.interview.lottory.domain.LotteryCampaign;
import com.interview.lottory.domain.LotteryDraw;
import com.interview.lottory.domain.LotteryEvent;
import com.interview.lottory.domain.LotteryPrize;
import com.interview.lottory.enums.LotteryEventStatus;
import com.interview.lottory.infra.Constants;
import com.interview.lottory.service.draw.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DrawEntityMapper {
    DrawCampaignBo toCampaignBo(LotteryCampaign entity);
    DrawPrizeBo toPrizeBo(LotteryPrize entity);
    List<DrawPrizeBo> toPrizeBos(List<LotteryPrize> entities);
    @Mapping(target = "sequence", source = "drawSequence")
    DrawItemBo toItemBo(LotteryDraw entity);
    List<DrawItemBo> toItemBos(List<LotteryDraw> entities);
    LotteryEventMessageBo toMessageBo(LotteryEvent entity);

    @Mapping(target = "status", source = "status")
    DrawAcceptedBo toAcceptedBo(LotteryEvent entity);

    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "requestId", source = "command.requestId")
    @Mapping(target = "campaignId", source = "command.campaignId")
    @Mapping(target = "userId", source = "command.userId")
    @Mapping(target = "drawCount", source = "command.drawCount")
    @Mapping(target = "eventType", constant = Constants.EventType.LOTTERY_DRAW_REQUESTED)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "payload", source = "payload")
    LotteryEvent toEvent(DrawCommandBo command, UUID eventId, String payload);

    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "drawSequence", source = "item.sequence")
    @Mapping(target = "campaignId", source = "command.campaignId")
    @Mapping(target = "userId", source = "command.userId")
    @Mapping(target = "prizeId", source = "item.prizeId")
    @Mapping(target = "prizeCode", source = "item.prizeCode")
    @Mapping(target = "prizeName", source = "item.prizeName")
    @Mapping(target = "won", source = "item.won")
    LotteryDraw toDraw(DrawItemBo item, DrawCommandBo command, UUID eventId);

    default void attachResult(LotteryEvent event, String resultPayload) {
        event.setResultPayload(resultPayload);
        event.setStatus(LotteryEventStatus.COMPLETED);
        event.setProcessedAt(Instant.now());
    }

    default void markFailed(LotteryEvent event, String failureCode) {
        event.setStatus(LotteryEventStatus.FAILED);
        event.setFailureCode(failureCode);
        event.setProcessedAt(Instant.now());
    }

    default void markPublished(LotteryEvent event) {
        event.setStatus(LotteryEventStatus.PUBLISHED);
        event.setPublishedAt(Instant.now());
    }

    default void markPublishFailed(LotteryEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setNextRetryAt(Instant.now().plusSeconds(Math.min(300, 1L << Math.min(8, event.getRetryCount()))));
    }
}
