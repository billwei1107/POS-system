-- V6001: 門店庫存表 / Store-level stock table
CREATE TABLE pos_inv_store_stock (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id          UUID        NOT NULL,
    item_id           UUID        NOT NULL,
    quantity          DECIMAL(12, 3) NOT NULL DEFAULT 0,
    reserved_quantity DECIMAL(12, 3) NOT NULL DEFAULT 0,
    reorder_point     DECIMAL(12, 3) NOT NULL DEFAULT 0,
    reorder_quantity  DECIMAL(12, 3) NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ,
    CONSTRAINT uq_inv_store_item UNIQUE (store_id, item_id)
);

CREATE INDEX idx_inv_store_stock_store ON pos_inv_store_stock(store_id);
CREATE INDEX idx_inv_store_stock_item  ON pos_inv_store_stock(item_id);
