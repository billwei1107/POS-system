-- V6004: 調撥品項明細 / Transfer request line items
CREATE TABLE pos_inv_transfer_items (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transfer_id   UUID           NOT NULL,
    item_id       UUID           NOT NULL,
    requested_qty DECIMAL(12, 3) NOT NULL DEFAULT 0,
    shipped_qty   DECIMAL(12, 3) NOT NULL DEFAULT 0,
    received_qty  DECIMAL(12, 3) NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

CREATE INDEX idx_inv_transfer_items_transfer ON pos_inv_transfer_items(transfer_id);
CREATE INDEX idx_inv_transfer_items_item     ON pos_inv_transfer_items(item_id);
