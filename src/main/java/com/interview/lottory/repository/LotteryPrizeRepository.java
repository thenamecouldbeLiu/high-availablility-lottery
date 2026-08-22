package com.interview.lottory.repository;

import com.interview.lottory.domain.LotteryPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LotteryPrizeRepository extends JpaRepository<LotteryPrize, Long> {
    List<LotteryPrize> findByCampaignIdAndEnabledTrueOrderByDisplayOrderAsc(Long campaignId);
    List<LotteryPrize> findByCampaignIdOrderByDisplayOrderAsc(Long campaignId);
    Optional<LotteryPrize> findByCampaignIdAndPrizeCode(Long campaignId, String prizeCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryPrize p
               set p.remainingStock = p.remainingStock - :quantity,
                   p.version = p.version + 1
             where p.id = :prizeId
               and p.prizeType = com.interview.lottory.domain.PrizeType.PRIZE
               and p.enabled = true
               and p.remainingStock >= :quantity
            """)
    int deductStockIfAvailable(@Param("prizeId") Long prizeId, @Param("quantity") long quantity);
}
