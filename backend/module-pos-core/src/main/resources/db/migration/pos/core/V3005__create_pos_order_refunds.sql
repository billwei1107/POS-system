-- V3005: 退款記錄 / Order refund records
CREATE TABLE pos_order_refunds (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID        NOT NULL,
    refund_no       VARCHAR(40) NOT NULL UNIQUE,
    refund_amount   DECIMAL(12,2) NOT NULL,
    refund_method   VARCHAR(30) NOT NULL,
    reason          VARCHAR(200),
    approved_by     UUID,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_at    TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_pos_order_refunds_order_id ON pos_order_refunds(order_id);
CREATE INDEX idx_pos_order_refunds_status   ON pos_order_refunds(status);
