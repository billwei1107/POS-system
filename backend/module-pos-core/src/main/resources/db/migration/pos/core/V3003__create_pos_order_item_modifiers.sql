-- V3003: 訂單明細的客製化選項快照 / Modifier snapshots per order item
CREATE TABLE pos_order_item_modifiers (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id           UUID        NOT NULL,
    modifier_group_id       UUID        NOT NULL,
    modifier_id             UUID        NOT NULL,
    modifier_name_snapshot  VARCHAR(100) NOT NULL,
    price_adjustment        DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_at              TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at              TIMESTAMP
);

CREATE INDEX idx_pos_oi_modifiers_order_item_id ON pos_order_item_modifiers(order_item_id);
