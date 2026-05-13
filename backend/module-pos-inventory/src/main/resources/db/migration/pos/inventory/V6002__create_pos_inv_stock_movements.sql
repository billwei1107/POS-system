-- V6002: 庫存異動紀錄表 / Stock movement audit log
CREATE TABLE pos_inv_stock_movements (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id         UUID           NOT NULL,
    item_id          UUID           NOT NULL,
    quantity_change  DECIMAL(12, 3) NOT NULL,
    movement_type    VARCHAR(20)    NOT NULL,  -- SALE/RETURN/TRANSFER_IN/TRANSFER_OUT/ADJUSTMENT/RECEIVING/WASTE
    reference_id     UUID,
    reference_type   VARCHAR(50),
    operated_by      UUID,
    notes            TEXT,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);

CREATE INDEX idx_inv_movements_store ON pos_inv_stock_movements(store_id);
CREATE INDEX idx_inv_movements_item  ON pos_inv_stock_movements(item_id);
CREATE INDEX idx_inv_movements_ref   ON pos_inv_stock_movements(reference_id);
CREATE INDEX idx_inv_movements_type  ON pos_inv_stock_movements(movement_type);
