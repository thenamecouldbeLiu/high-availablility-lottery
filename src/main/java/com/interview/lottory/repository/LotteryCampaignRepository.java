package com.interview.lottory.repository;

import com.interview.lottory.domain.LotteryCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LotteryCampaignRepository extends JpaRepository<LotteryCampaign, Long> {
    Optional<LotteryCampaign> findByIdAndDeletedFalse(Long id);
    Optional<LotteryCampaign> findByCampaignCodeAndDeletedFalse(String campaignCode);
    boolean existsByCampaignCodeAndDeletedFalse(String campaignCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LotteryCampaign c
               set c.deleted = true,
                   c.deletedAt = CURRENT_TIMESTAMP,
                   c.status = com.interview.lottory.enums.CampaignStatus.ENDED,
                   c.version = c.version + 1
             where c.id = :campaignId
               and c.deleted = false
            """)
    int softDeleteById(@Param("campaignId") Long campaignId);
}
