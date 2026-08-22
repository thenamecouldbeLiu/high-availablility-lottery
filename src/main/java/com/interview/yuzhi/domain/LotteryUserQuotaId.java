package com.interview.yuzhi.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LotteryUserQuotaId implements Serializable {
    private Long campaignId;
    private String userId;
}
