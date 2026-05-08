-- V6010: 盤點明細表 / Stock take line items with system vs counted quantities
CREATE TABLE pos_inv_stock_take_items (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stock_take_id UUID           NOT NULL,
    item_id       UUID           NOT NULL,
    system_qty    DECIMAL(12, 3) NOT NULL DEFAULT 0,
    counted_qty   DECIMAL(12, 3),
    difference    DECIMAL(12, 3),
    notes         TEXT,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

CREATE INDEX idx_inv_take_items_take ON pos_inv_stock_take_items(stock_take_id);
CREATE INDEX idx_inv_take_items_item ON pos_inv_stock_take_items(item_id);
