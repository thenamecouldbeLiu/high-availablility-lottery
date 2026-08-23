--liquibase formatted sql

--changeset lottory:001-create-lottery-schema
CREATE TABLE lottery_campaign (
    id BIGINT PRIMARY KEY,
    campaign_code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL,
    max_draws_per_user INTEGER NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_campaign_status CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'ENDED')),
    CONSTRAINT ck_campaign_draw_limit CHECK (max_draws_per_user > 0),
    CONSTRAINT ck_campaign_period CHECK (ends_at > starts_at)
);

CREATE INDEX idx_campaign_status_period ON lottery_campaign (status, starts_at, ends_at);
CREATE UNIQUE INDEX uk_lottery_campaign_code_active
    ON lottery_campaign (campaign_code) WHERE deleted = FALSE;

CREATE TABLE lottery_prize (
    id BIGINT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    prize_code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    prize_type VARCHAR(20) NOT NULL,
    probability NUMERIC(8, 7) NOT NULL,
    total_stock BIGINT NOT NULL DEFAULT 0,
    remaining_stock BIGINT NOT NULL DEFAULT 0,
    display_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_prize_type CHECK (prize_type IN ('PRIZE', 'NO_PRIZE')),
    CONSTRAINT ck_prize_probability CHECK (probability >= 0 AND probability <= 1),
    CONSTRAINT ck_prize_stock CHECK (total_stock >= 0 AND remaining_stock >= 0 AND remaining_stock <= total_stock),
    CONSTRAINT ck_no_prize_stock CHECK (prize_type <> 'NO_PRIZE' OR (total_stock = 0 AND remaining_stock = 0))
);

CREATE INDEX idx_prize_campaign_enabled ON lottery_prize (campaign_id, enabled, display_order);
CREATE UNIQUE INDEX uk_prize_campaign_code_active
    ON lottery_prize (campaign_id, prize_code) WHERE deleted = FALSE;
CREATE UNIQUE INDEX uk_prize_one_no_prize_per_campaign
    ON lottery_prize (campaign_id) WHERE prize_type = 'NO_PRIZE' AND deleted = FALSE;

CREATE TABLE lottery_event (
    event_id UUID PRIMARY KEY,
    request_id VARCHAR(128) NOT NULL,
    campaign_id BIGINT NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    draw_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    payload JSONB NOT NULL,
    result_payload JSONB,
    failure_code VARCHAR(64),
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_lottery_event_request UNIQUE (request_id),
    CONSTRAINT ck_event_draw_count CHECK (draw_count > 0),
    CONSTRAINT ck_event_retry_count CHECK (retry_count >= 0),
    CONSTRAINT ck_event_status CHECK (status IN ('PENDING', 'DISPATCHING', 'PUBLISHED', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_event_outbox_polling ON lottery_event (status, next_retry_at, created_at);
CREATE INDEX idx_event_campaign_user ON lottery_event (campaign_id, user_id, created_at);
CREATE INDEX idx_event_user_created ON lottery_event (user_id, created_at DESC);

CREATE TABLE lottery_user_quota (
    campaign_id BIGINT NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    used_draws INTEGER NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (campaign_id, user_id),
    CONSTRAINT ck_quota_used_draws CHECK (used_draws >= 0)
);

CREATE TABLE lottery_draw (
    id BIGINT PRIMARY KEY,
    event_id UUID NOT NULL,
    draw_sequence INTEGER NOT NULL,
    campaign_id BIGINT NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    prize_id BIGINT,
    prize_code VARCHAR(64),
    prize_name VARCHAR(128) NOT NULL,
    won BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_draw_event_sequence UNIQUE (event_id, draw_sequence),
    CONSTRAINT ck_draw_sequence CHECK (draw_sequence > 0)
);

CREATE INDEX idx_draw_campaign_user ON lottery_draw (campaign_id, user_id, created_at);
CREATE INDEX idx_draw_prize ON lottery_draw (prize_id) WHERE prize_id IS NOT NULL;
