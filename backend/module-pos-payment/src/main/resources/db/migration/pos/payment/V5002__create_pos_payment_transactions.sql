-- V5002: POS 支付交易記錄 / POS payment transactions
CREATE TABLE pos_payment_transactions (
    id              UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id        UUID            NOT NULL,
    store_id        UUID            NOT NULL,
    pay_method_id   UUID            NOT NULL,
    method_type     VARCHAR(30)     NOT NULL,
    amount          DECIMAL(12, 2)  NOT NULL,
    tendered        DECIMAL(12, 2),
    change_given    DECIMAL(12, 2)  NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'SUCCESS', -- SUCCESS / FAILED / VOIDED / REFUNDED
    gateway_ref     VARCHAR(200),
    gateway_resp    TEXT,
    error_code      VARCHAR(50),
    error_msg       TEXT,
    processed_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    voided_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_txn_order   ON pos_payment_transactions (order_id);
CREATE INDEX idx_payment_txn_store   ON pos_payment_transactions (store_id, processed_at DESC);
CREATE INDEX idx_payment_txn_method  ON pos_payment_transactions (pay_method_id);
