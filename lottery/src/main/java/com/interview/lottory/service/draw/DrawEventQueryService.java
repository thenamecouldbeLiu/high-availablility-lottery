package com.interview.lottory.service.draw;

import com.interview.lottory.domain.LotteryEvent;
import com.interview.lottory.infra.exception.ErrorCode;
import com.interview.lottory.infra.exception.InterviewException;
import com.interview.lottory.repository.LotteryDrawRepository;
import com.interview.lottory.repository.LotteryEventRepository;
import com.interview.lottory.service.draw.dto.DrawEventStatusBo;
import com.interview.lottory.service.draw.mapper.DrawEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DrawEventQueryService {
    private final LotteryEventRepository eventRepository;
    private final LotteryDrawRepository drawRepository;
    private final DrawEntityMapper mapper;

    @Transactional(readOnly = true)
    public DrawEventStatusBo getEventByEventIdAndUserId(UUID eventId, String userId) {
        LotteryEvent event = eventRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new InterviewException(ErrorCode.INVALID_REQUEST));
        return toBo(event);
    }

    @Transactional(readOnly = true)
    public List<DrawEventStatusBo> getUserEventHistoryByCampaignId(String userId, Long campaignId, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        List<LotteryEvent> events = campaignId == null
                ? eventRepository.findByUserIdOrderByCreatedAtDesc(userId, page)
                : eventRepository.findByUserIdAndCampaignIdOrderByCreatedAtDesc(userId, campaignId, page);
        return events.stream().map(this::toBo).toList();
    }

    private DrawEventStatusBo toBo(LotteryEvent event) {
        var message = mapper.toMessageBo(event);
        return new DrawEventStatusBo(message.eventId(), message.requestId(), message.campaignId(),
                message.userId(), message.drawCount(), event.getStatus(), event.getFailureCode(),
                mapper.toItemBos(drawRepository.findByEventIdOrderByDrawSequence(message.eventId())),
                event.getCreatedAt(), event.getProcessedAt());
    }
}
