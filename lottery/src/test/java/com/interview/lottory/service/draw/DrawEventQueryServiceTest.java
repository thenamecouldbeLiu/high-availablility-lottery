package com.interview.lottory.service.draw;

import com.interview.common.exception.InterviewException;
import com.interview.lottory.domain.LotteryDraw;
import com.interview.lottory.domain.LotteryEvent;
import com.interview.lottory.enums.LotteryEventStatus;
import com.interview.lottory.repository.LotteryDrawRepository;
import com.interview.lottory.repository.LotteryEventRepository;
import com.interview.lottory.service.draw.dto.DrawItemBo;
import com.interview.lottory.service.draw.dto.LotteryEventMessageBo;
import com.interview.lottory.service.draw.mapper.DrawEntityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrawEventQueryServiceTest {
    @Mock LotteryEventRepository events;
    @Mock LotteryDrawRepository draws;
    @Mock DrawEntityMapper mapper;
    @InjectMocks DrawEventQueryService service;

    @Test
    void getsEventWithDrawItems() {
        UUID eventId = UUID.randomUUID();
        var event = event(LotteryEventStatus.COMPLETED);
        var draw = new LotteryDraw();
        var message = message(eventId);
        when(events.findByEventIdAndUserId(eventId, "user")).thenReturn(Optional.of(event));
        when(mapper.toMessageBo(event)).thenReturn(message);
        when(draws.findByEventIdOrderByDrawSequence(eventId)).thenReturn(List.of(draw));
        when(mapper.toItemBos(List.of(draw))).thenReturn(List.of(new DrawItemBo(1, null, "LOSE", "No", false)));

        var result = service.getEventByEventIdAndUserId(eventId, "user");

        assertThat(result.status()).isEqualTo(LotteryEventStatus.COMPLETED);
        assertThat(result.results()).hasSize(1);
    }

    @Test
    void rejectsUnknownOrForeignEvent() {
        assertThatThrownBy(() -> service.getEventByEventIdAndUserId(UUID.randomUUID(), "user"))
                .isInstanceOf(InterviewException.class);
    }

    @Test
    void queriesAllUserHistoryWhenCampaignIsNotSpecified() {
        var event = event(LotteryEventStatus.PENDING);
        when(events.findByUserIdOrderByCreatedAtDesc(eq("user"), any(Pageable.class)))
                .thenReturn(List.of(event));
        when(mapper.toMessageBo(event)).thenReturn(message(event.getEventId()));
        when(mapper.toItemBos(any())).thenReturn(List.of());

        assertThat(service.getUserEventHistoryByCampaignId("user", null, 20)).hasSize(1);
        verify(events).findByUserIdOrderByCreatedAtDesc(eq("user"), any(Pageable.class));
    }

    @Test
    void queriesCampaignSpecificHistory() {
        when(events.findByUserIdAndCampaignIdOrderByCreatedAtDesc(eq("user"), eq(1L), any(Pageable.class)))
                .thenReturn(List.of());
        assertThat(service.getUserEventHistoryByCampaignId("user", 1L, 20)).isEmpty();
    }

    private LotteryEvent event(LotteryEventStatus status) {
        var event = new LotteryEvent(); event.setStatus(status); return event;
    }

    private LotteryEventMessageBo message(UUID id) {
        return new LotteryEventMessageBo(id, "request", 1L, "user", "DRAW", 1, "{}", null);
    }

    private static <T> T eq(T value) { return org.mockito.ArgumentMatchers.eq(value); }
}
