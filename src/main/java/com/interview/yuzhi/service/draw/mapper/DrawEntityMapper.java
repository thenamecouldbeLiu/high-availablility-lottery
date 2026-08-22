package com.interview.yuzhi.service.draw.mapper;

import com.interview.yuzhi.domain.*;
import com.interview.yuzhi.service.draw.dto.*;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DrawEntityMapper {
    DrawCampaignBo toCampaignBo(LotteryCampaign entity);
    DrawPrizeBo toPrizeBo(LotteryPrize entity);
    List<DrawPrizeBo> toPrizeBos(List<LotteryPrize> entities);
    LotteryEventMessageBo toMessageBo(LotteryEvent entity);

    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "requestId", source = "command.requestId")
    @Mapping(target = "campaignId", source = "command.campaignId")
    @Mapping(target = "userId", source = "command.userId")
    @Mapping(target = "drawCount", source = "command.drawCount")
    @Mapping(target = "eventType", constant = "LOTTERY_DRAW_COMPLETED")
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
        event.setStatus(LotteryEventStatus.PENDING);
        event.setNextRetryAt(Instant.now());
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
