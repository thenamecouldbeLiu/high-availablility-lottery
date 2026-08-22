package com.interview.lottory.domain;

import com.interview.lottory.enums.CampaignStatus;
import com.interview.lottory.util.IdGeneratorUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lottery_campaign")
@NoArgsConstructor
public class LotteryCampaign implements Persistable<Long> {
    @Id
    private Long id = IdGeneratorUtil.nextSnowflakeId();

    @Transient
    private boolean newEntity = true;

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        newEntity = false;
    }

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = IdGeneratorUtil.nextSnowflakeId();
        }
    }

    @Column(name = "campaign_code", nullable = false, length = 64, unique = true)
    private String campaignCode;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status;

    @Column(name = "max_draws_per_user", nullable = false)
    private int maxDrawsPerUser;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Version
    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    private long version;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;
}
