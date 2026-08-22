package com.interview.yuzhi.repository;

import com.interview.yuzhi.domain.LotteryCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LotteryCampaignRepository extends JpaRepository<LotteryCampaign, Long> {
    Optional<LotteryCampaign> findByCampaignCode(String campaignCode);
    boolean existsByCampaignCode(String campaignCode);
}
