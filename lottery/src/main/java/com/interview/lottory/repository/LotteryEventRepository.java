package com.interview.lottory.repository;

import com.interview.lottory.domain.LotteryEvent;
import com.interview.lottory.enums.LotteryEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotteryEventRepository extends JpaRepository<LotteryEvent, UUID> {
    Optional<LotteryEvent> findByRequestId(String requestId);
    boolean existsByRequestId(String requestId);
    Optional<LotteryEvent> findByEventIdAndUserId(UUID eventId, String userId);
    List<LotteryEvent> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<LotteryEvent> findByUserIdAndCampaignIdOrderByCreatedAtDesc(
            String userId, Long campaignId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryEvent e
               set e.status = com.interview.lottory.enums.LotteryEventStatus.PROCESSING,
                   e.version = e.version + 1
             where e.eventId = :eventId
               and e.status in (
                   com.interview.lottory.enums.LotteryEventStatus.DISPATCHING,
                   com.interview.lottory.enums.LotteryEventStatus.PUBLISHED)
            """)
    int claimForProcessing(@Param("eventId") UUID eventId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryEvent e
               set e.status = com.interview.lottory.enums.LotteryEventStatus.DISPATCHING,
                   e.version = e.version + 1
             where e.eventId = :eventId
               and e.status = com.interview.lottory.enums.LotteryEventStatus.PENDING
            """)
    int claimForPublishing(@Param("eventId") UUID eventId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryEvent e
               set e.status = com.interview.lottory.enums.LotteryEventStatus.PUBLISHED,
                   e.publishedAt = :publishedAt,
                   e.version = e.version + 1
             where e.eventId = :eventId
               and e.status = com.interview.lottory.enums.LotteryEventStatus.DISPATCHING
            """)
    int markPublishedIfDispatching(@Param("eventId") UUID eventId,
                                   @Param("publishedAt") Instant publishedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryEvent e
               set e.status = com.interview.lottory.enums.LotteryEventStatus.PENDING,
                   e.retryCount = e.retryCount + 1,
                   e.nextRetryAt = :nextRetryAt,
                   e.version = e.version + 1
             where e.eventId = :eventId
               and e.status = com.interview.lottory.enums.LotteryEventStatus.DISPATCHING
            """)
    int reschedulePublishing(@Param("eventId") UUID eventId,
                             @Param("nextRetryAt") Instant nextRetryAt);

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
