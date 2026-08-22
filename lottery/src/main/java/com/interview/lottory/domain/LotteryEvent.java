package com.interview.lottory.domain;

import com.interview.lottory.enums.LotteryEventStatus;
import com.interview.lottory.util.IdGeneratorUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lottery_event")
@NoArgsConstructor
public class LotteryEvent implements Persistable<UUID> {
    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId = IdGeneratorUtil.nextUuid();

    @Transient
    private boolean newEntity = true;

    @Override
    public UUID getId() {
        return eventId;
    }

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
        if (eventId == null) {
            eventId = IdGeneratorUtil.nextUuid();
        }
    }

    @Column(name = "request_id", nullable = false, updatable = false, length = 128, unique = true)
    private String requestId;

    @Column(name = "campaign_id", nullable = false, updatable = false)
    private Long campaignId;

    @Column(name = "user_id", nullable = false, updatable = false, length = 128)
    private String userId;

    @Column(name = "event_type", nullable = false, updatable = false, length = 64)
    private String eventType;

    @Column(name = "draw_count", nullable = false, updatable = false)
    private int drawCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LotteryEventStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_payload", columnDefinition = "jsonb")
    private String resultPayload;

    @Column(name = "failure_code", length = 64)
    private String failureCode;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

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
