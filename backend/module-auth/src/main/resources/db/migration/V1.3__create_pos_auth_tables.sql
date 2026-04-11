-- ============================================================
-- POS 認證擴展資料表 / POS Authentication Extension Tables
-- Sprint 1-1: PIN 碼登入與終端機認證
-- ============================================================

-- PIN 碼表 / PIN codes for POS terminal quick login
CREATE TABLE auth_pin_codes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,
    terminal_type VARCHAR(20) NOT NULL DEFAULT 'POS',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE (user_id, terminal_type)
);

CREATE INDEX idx_auth_pin_codes_user_id ON auth_pin_codes(user_id);
CREATE INDEX idx_auth_pin_codes_active ON auth_pin_codes(user_id, active);

-- 終端機 Token 表 / Terminal device registration tokens
CREATE TABLE auth_terminal_tokens (
    id UUID PRIMARY KEY,
    terminal_id UUID NOT NULL,
    store_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    device_fingerprint VARCHAR(255),
    registered_by UUID,
    registered_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP,
    last_active_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_auth_terminal_tokens_terminal_id ON auth_terminal_tokens(terminal_id);
CREATE INDEX idx_auth_terminal_tokens_store_id ON auth_terminal_tokens(store_id);
