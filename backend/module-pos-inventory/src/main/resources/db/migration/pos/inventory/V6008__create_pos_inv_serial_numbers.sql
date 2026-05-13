-- V6008: 序號追蹤 / Serial number tracking
CREATE TABLE pos_inv_serial_numbers (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id   UUID        NOT NULL,
    item_id    UUID        NOT NULL,
    serial_no  VARCHAR(100) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'IN_STOCK',  -- IN_STOCK/SOLD/RETURNED/DEFECTIVE
    order_id   UUID,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_inv_serial_no UNIQUE (item_id, serial_no)
);

CREATE INDEX idx_inv_serial_store  ON pos_inv_serial_numbers(store_id);
CREATE INDEX idx_inv_serial_item   ON pos_inv_serial_numbers(item_id);
CREATE INDEX idx_inv_serial_status ON pos_inv_serial_numbers(status);
