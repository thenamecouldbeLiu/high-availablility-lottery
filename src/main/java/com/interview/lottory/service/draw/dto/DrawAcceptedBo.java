package com.interview.lottory.service.draw.dto;

import com.interview.lottory.enums.LotteryEventStatus;
import java.util.UUID;

public record DrawAcceptedBo(UUID eventId, String requestId, LotteryEventStatus status) {
}
