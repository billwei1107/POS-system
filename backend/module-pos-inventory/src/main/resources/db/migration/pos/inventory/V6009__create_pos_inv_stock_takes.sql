-- V6009: 盤點主表 / Stock take / inventory count session
CREATE TABLE pos_inv_stock_takes (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id     UUID        NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',  -- IN_PROGRESS/COMPLETED/CANCELLED
    started_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    created_by   UUID,
    notes        TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);

CREATE INDEX idx_inv_stock_takes_store  ON pos_inv_stock_takes(store_id);
CREATE INDEX idx_inv_stock_takes_status ON pos_inv_stock_takes(status);
