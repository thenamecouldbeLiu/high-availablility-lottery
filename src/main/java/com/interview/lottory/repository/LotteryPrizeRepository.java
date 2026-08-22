package com.interview.lottory.repository;

import com.interview.lottory.domain.LotteryPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LotteryPrizeRepository extends JpaRepository<LotteryPrize, Long> {
    List<LotteryPrize> findByCampaignIdAndEnabledTrueAndDeletedFalseOrderByDisplayOrderAsc(Long campaignId);
    List<LotteryPrize> findByCampaignIdAndDeletedFalseOrderByDisplayOrderAsc(Long campaignId);
    Optional<LotteryPrize> findByIdAndCampaignIdAndDeletedFalse(Long id, Long campaignId);
    Optional<LotteryPrize> findByCampaignIdAndPrizeCodeAndDeletedFalse(Long campaignId, String prizeCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryPrize p
               set p.remainingStock = p.remainingStock - :quantity,
                   p.version = p.version + 1
             where p.id = :prizeId
               and p.prizeType = com.interview.lottory.enums.PrizeType.PRIZE
               and p.enabled = true
               and p.deleted = false
               and p.remainingStock >= :quantity
            """)
    int deductStockIfAvailable(@Param("prizeId") Long prizeId, @Param("quantity") long quantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryPrize p
               set p.deleted = true,
                   p.deletedAt = CURRENT_TIMESTAMP,
                   p.enabled = false,
                   p.version = p.version + 1
             where p.id = :prizeId
               and p.campaignId = :campaignId
               and p.deleted = false
            """)
    int softDeleteByIdAndCampaignId(@Param("prizeId") Long prizeId,
                                    @Param("campaignId") Long campaignId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryPrize p
               set p.deleted = true,
                   p.deletedAt = CURRENT_TIMESTAMP,
                   p.enabled = false,
                   p.version = p.version + 1
             where p.campaignId = :campaignId
               and p.deleted = false
            """)
    int softDeleteAllByCampaignId(@Param("campaignId") Long campaignId);
}
