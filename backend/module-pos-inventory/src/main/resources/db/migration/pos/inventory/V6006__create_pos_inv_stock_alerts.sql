-- V6006: 庫存警示表 / Stock alert notifications
CREATE TABLE pos_inv_stock_alerts (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id         UUID           NOT NULL,
    item_id          UUID           NOT NULL,
    alert_type       VARCHAR(20)    NOT NULL,  -- LOW_STOCK/OUT_OF_STOCK/EXPIRING
    current_qty      DECIMAL(12, 3) NOT NULL,
    threshold_qty    DECIMAL(12, 3) NOT NULL,
    acknowledged     BOOLEAN        NOT NULL DEFAULT FALSE,
    acknowledged_by  UUID,
    acknowledged_at  TIMESTAMPTZ,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);

CREATE INDEX idx_inv_alerts_store        ON pos_inv_stock_alerts(store_id);
CREATE INDEX idx_inv_alerts_item         ON pos_inv_stock_alerts(item_id);
CREATE INDEX idx_inv_alerts_acknowledged ON pos_inv_stock_alerts(acknowledged);
