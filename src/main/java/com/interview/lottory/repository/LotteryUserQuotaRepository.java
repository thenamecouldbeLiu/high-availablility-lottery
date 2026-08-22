package com.interview.lottory.repository;

import com.interview.lottory.domain.LotteryUserQuota;
import com.interview.lottory.domain.LotteryUserQuotaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LotteryUserQuotaRepository extends JpaRepository<LotteryUserQuota, LotteryUserQuotaId> {
    @Modifying
    @Query(value = """
            INSERT INTO lottery_user_quota (campaign_id, user_id, used_draws)
            VALUES (:campaignId, :userId, 0)
            ON CONFLICT (campaign_id, user_id) DO NOTHING
            """, nativeQuery = true)
    int createIfAbsent(@Param("campaignId") Long campaignId, @Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryUserQuota q
               set q.usedDraws = q.usedDraws + :drawCount,
                   q.version = q.version + 1
             where q.campaignId = :campaignId
               and q.userId = :userId
               and q.usedDraws + :drawCount <= :maxDraws
            """)
    int consumeIfAvailable(@Param("campaignId") Long campaignId,
                           @Param("userId") String userId,
                           @Param("drawCount") int drawCount,
                           @Param("maxDraws") int maxDraws);
}
