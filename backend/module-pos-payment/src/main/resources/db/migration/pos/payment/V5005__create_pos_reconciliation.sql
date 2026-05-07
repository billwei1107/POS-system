-- V5005: 每日對帳記錄 / Daily reconciliation records
CREATE TABLE pos_reconciliation (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id            UUID            NOT NULL,
    recon_date          DATE            NOT NULL,
    pay_method_id       UUID            NOT NULL,
    method_type         VARCHAR(30)     NOT NULL,
    transaction_count   INT             NOT NULL DEFAULT 0,
    total_amount        DECIMAL(14, 2)  NOT NULL DEFAULT 0,
    refund_count        INT             NOT NULL DEFAULT 0,
    refund_amount       DECIMAL(14, 2)  NOT NULL DEFAULT 0,
    net_amount          DECIMAL(14, 2)  NOT NULL DEFAULT 0,
    gateway_amount      DECIMAL(14, 2),  -- from gateway settlement report
    variance            DECIMAL(14, 2),  -- net_amount - gateway_amount
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING / MATCHED / DISCREPANCY
    reconciled_by       UUID,
    reconciled_at       TIMESTAMPTZ,
    note                TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, recon_date, pay_method_id)
);

CREATE INDEX idx_recon_store_date ON pos_reconciliation (store_id, recon_date DESC);
