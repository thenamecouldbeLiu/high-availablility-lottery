package com.interview.lottory.controller.draw.dto;

import com.interview.lottory.enums.LotteryEventStatus;
import java.util.UUID;

public record DrawAcceptedVo(UUID eventId, String requestId, LotteryEventStatus status) {
}
