--liquibase formatted sql

--changeset interview:001-create-user-schema

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    keycloak_subject VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(150),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE user_role (
    user_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'NORMAL_USER'))
);

COMMENT ON TABLE app_user IS 'Application user account';
COMMENT ON COLUMN app_user.id IS 'User identifier (UUID v7)';
COMMENT ON COLUMN app_user.keycloak_subject IS 'Unique subject identifier from Keycloak';
COMMENT ON COLUMN app_user.username IS 'Unique login username';
COMMENT ON COLUMN app_user.email IS 'Unique email address';
COMMENT ON COLUMN app_user.display_name IS 'User-facing display name';
COMMENT ON COLUMN app_user.enabled IS 'Whether the user account is enabled';
COMMENT ON COLUMN app_user.created_at IS 'Timestamp when the user was created';
COMMENT ON COLUMN app_user.updated_at IS 'Timestamp when the user was last updated';
COMMENT ON COLUMN app_user.version IS 'Optimistic locking version';

COMMENT ON TABLE user_role IS 'Roles assigned to application users';
COMMENT ON COLUMN user_role.user_id IS 'Application user identifier';
COMMENT ON COLUMN user_role.role IS 'Assigned application role';

CREATE INDEX idx_app_user_username_lower ON app_user (LOWER(username));
CREATE INDEX idx_app_user_email_lower ON app_user (LOWER(email));
CREATE INDEX idx_app_user_display_name_lower ON app_user (LOWER(display_name));
