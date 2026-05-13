-- V6003: 調撥申請主表 / Inter-store transfer request
CREATE TABLE pos_inv_transfer_requests (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transfer_no   VARCHAR(30)  NOT NULL UNIQUE,
    from_store_id UUID         NOT NULL,
    to_store_id   UUID         NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'REQUESTED',  -- REQUESTED/APPROVED/IN_TRANSIT/RECEIVED/CANCELLED
    notes         TEXT,
    requested_by  UUID,
    approved_by   UUID,
    shipped_at    TIMESTAMPTZ,
    received_at   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

CREATE INDEX idx_inv_transfer_from ON pos_inv_transfer_requests(from_store_id);
CREATE INDEX idx_inv_transfer_to   ON pos_inv_transfer_requests(to_store_id);
CREATE INDEX idx_inv_transfer_status ON pos_inv_transfer_requests(status);
