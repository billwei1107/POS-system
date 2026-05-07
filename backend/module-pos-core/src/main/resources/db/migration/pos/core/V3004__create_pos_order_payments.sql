-- V3004: 訂單付款記錄（支援分筆付款）/ Order payment records
CREATE TABLE pos_order_payments (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID        NOT NULL,
    pay_method      VARCHAR(30) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    tendered        DECIMAL(12,2),
    change_given    DECIMAL(12,2) NOT NULL DEFAULT 0,
    reference_no    VARCHAR(100),
    gateway_resp    TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    processed_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_pos_order_payments_order_id ON pos_order_payments(order_id);
