-- V6005: BOPIS 訂單（線上購買門店取貨）/ Buy Online, Pick Up In Store
CREATE TABLE pos_inv_bopis_orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    online_order_ref    VARCHAR(100) NOT NULL,
    store_id            UUID         NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING/PICKING/READY/PICKED_UP/CANCELLED/EXPIRED
    picker_employee_id  UUID,
    pickup_code         VARCHAR(10),
    ready_at            TIMESTAMPTZ,
    picked_up_at        TIMESTAMPTZ,
    expires_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_inv_bopis_store  ON pos_inv_bopis_orders(store_id);
CREATE INDEX idx_inv_bopis_status ON pos_inv_bopis_orders(status);
CREATE INDEX idx_inv_bopis_code   ON pos_inv_bopis_orders(pickup_code);
