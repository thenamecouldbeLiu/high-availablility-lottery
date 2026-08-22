package com.interview.lottory.repository;

import com.interview.lottory.domain.LotteryDraw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LotteryDrawRepository extends JpaRepository<LotteryDraw, Long> {
    List<LotteryDraw> findByEventIdOrderByDrawSequence(UUID eventId);
}
