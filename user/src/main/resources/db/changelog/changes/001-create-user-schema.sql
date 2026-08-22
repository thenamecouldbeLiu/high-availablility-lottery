--liquibase formatted sql

--changeset interview:001-create-user-schema

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    keycloak_subject VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(150),
    user_role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_app_user_role CHECK (user_role IN ('ADMIN', 'NORMAL_USER'))
);

CREATE INDEX idx_app_user_username_lower ON app_user (LOWER(username));
CREATE INDEX idx_app_user_email_lower ON app_user (LOWER(email));
CREATE INDEX idx_app_user_display_name_lower ON app_user (LOWER(display_name));
