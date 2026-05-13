-- V5001: POS 支付方式主表 / POS payment methods
CREATE TABLE pos_pay_methods (
    id              UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id        UUID         NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    method_type     VARCHAR(30)  NOT NULL, -- CASH / CARD / QR_CODE / GIFT_CARD / MIXED
    gateway_id      UUID,                  -- FK to pos_gateway_configs (nullable for CASH)
    is_change_back  BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order      INT          NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    UNIQUE (store_id, code)
);

CREATE INDEX idx_pay_methods_store ON pos_pay_methods (store_id) WHERE deleted_at IS NULL;
