-- V5004: 支付閘道配置 / Payment gateway configurations
CREATE TABLE pos_gateway_configs (
    id              UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id        UUID         NOT NULL,
    gateway_type    VARCHAR(50)  NOT NULL, -- CASH / MOCK_CARD / LINE_PAY / JKOPAY / TAIWAN_PAY
    display_name    VARCHAR(100) NOT NULL,
    merchant_id     VARCHAR(200),
    api_key         VARCHAR(500), -- stored encrypted in production
    api_secret      VARCHAR(500), -- stored encrypted in production
    endpoint_url    VARCHAR(500),
    extra_config    TEXT,        -- JSON for gateway-specific fields
    is_sandbox      BOOLEAN      NOT NULL DEFAULT TRUE,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    UNIQUE (store_id, gateway_type)
);

CREATE INDEX idx_gateway_config_store ON pos_gateway_configs (store_id) WHERE deleted_at IS NULL;
