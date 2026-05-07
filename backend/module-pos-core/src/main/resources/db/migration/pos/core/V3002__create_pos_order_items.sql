-- V3002: 訂單明細 / Order line items
CREATE TABLE pos_order_items (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID        NOT NULL,
    item_id             UUID        NOT NULL,
    variant_id          UUID,
    item_name_snapshot  VARCHAR(200) NOT NULL,
    sku_snapshot        VARCHAR(100),
    unit_price          DECIMAL(12,2) NOT NULL,
    quantity            DECIMAL(12,3) NOT NULL DEFAULT 1,
    discount_amount     DECIMAL(12,2) NOT NULL DEFAULT 0,
    line_total          DECIMAL(12,2) NOT NULL,
    note                VARCHAR(200),
    sort_order          INTEGER     NOT NULL DEFAULT 0,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_pos_order_items_order_id ON pos_order_items(order_id);
CREATE INDEX idx_pos_order_items_item_id  ON pos_order_items(item_id);
