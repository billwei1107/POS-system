-- V6007: 批次效期追蹤 / Batch lot and expiry tracking
CREATE TABLE pos_inv_batch_lots (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id           UUID           NOT NULL,
    item_id            UUID           NOT NULL,
    batch_no           VARCHAR(50)    NOT NULL,
    expiry_date        DATE,
    received_date      DATE,
    quantity_remaining DECIMAL(12, 3) NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at         TIMESTAMPTZ
);

CREATE INDEX idx_inv_batch_store  ON pos_inv_batch_lots(store_id);
CREATE INDEX idx_inv_batch_item   ON pos_inv_batch_lots(item_id);
CREATE INDEX idx_inv_batch_expiry ON pos_inv_batch_lots(expiry_date);
