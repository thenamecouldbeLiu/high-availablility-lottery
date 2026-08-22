package com.interview.yuzhi.repository;

import com.interview.yuzhi.domain.LotteryEvent;
import com.interview.yuzhi.domain.LotteryEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotteryEventRepository extends JpaRepository<LotteryEvent, UUID> {
    Optional<LotteryEvent> findByRequestId(String requestId);
    boolean existsByRequestId(String requestId);

    @Query("""
            select e from LotteryEvent e
             where e.status = :status
               and (e.nextRetryAt is null or e.nextRetryAt <= :now)
             order by e.createdAt asc
            """)
    List<LotteryEvent> findReadyToPublish(
            @Param("status") LotteryEventStatus status,
            @Param("now") Instant now,
            Pageable pageable);
}
