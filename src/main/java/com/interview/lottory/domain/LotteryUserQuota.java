package com.interview.lottory.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@IdClass(LotteryUserQuotaId.class)
@Table(name = "lottery_user_quota")
@NoArgsConstructor
public class LotteryUserQuota {
    @Id
    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Id
    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "used_draws", nullable = false)
    private int usedDraws;

    @Version
    @Setter(AccessLevel.NONE)
    private long version;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
