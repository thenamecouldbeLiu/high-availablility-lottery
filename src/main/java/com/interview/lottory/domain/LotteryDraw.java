package com.interview.lottory.domain;

import com.interview.lottory.util.IdGeneratorUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lottery_draw")
@NoArgsConstructor
public class LotteryDraw implements Persistable<Long> {
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
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;
    @Column(name = "draw_sequence", nullable = false, updatable = false)
    private int drawSequence;
    @Column(name = "campaign_id", nullable = false, updatable = false)
    private Long campaignId;
    @Column(name = "user_id", nullable = false, updatable = false, length = 128)
    private String userId;
    @Column(name = "prize_id", updatable = false)
    private Long prizeId;
    @Column(name = "prize_code", updatable = false, length = 64)
    private String prizeCode;
    @Column(name = "prize_name", nullable = false, updatable = false, length = 128)
    private String prizeName;
    @Column(nullable = false, updatable = false)
    private boolean won;
    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
