package com.interview.lottory.domain;

import com.interview.lottory.enums.PrizeType;
import com.interview.lottory.util.IdGeneratorUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lottery_prize")
@NoArgsConstructor
public class LotteryPrize implements Persistable<Long> {
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

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "prize_code", nullable = false, length = 64)
    private String prizeCode;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "prize_type", nullable = false, length = 20)
    private PrizeType prizeType;

    @Column(nullable = false, precision = 8, scale = 7)
    private BigDecimal probability;

    @Column(name = "total_stock", nullable = false)
    private long totalStock;

    @Column(name = "remaining_stock", nullable = false)
    private long remainingStock;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean enabled;

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
