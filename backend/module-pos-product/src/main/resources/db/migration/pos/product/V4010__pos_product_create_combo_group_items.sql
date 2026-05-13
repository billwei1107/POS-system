-- 套餐群組商品關聯表 / Items within a combo group
CREATE TABLE pos_prod_combo_group_items (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    combo_group_id UUID          NOT NULL REFERENCES pos_prod_combo_groups(id) ON DELETE CASCADE,
    item_id        UUID          NOT NULL REFERENCES pos_prod_items(id) ON DELETE CASCADE,
    price_override DECIMAL(12,2),
    sort_order     INT           NOT NULL DEFAULT 0,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    UNIQUE (combo_group_id, item_id)
);

CREATE INDEX idx_pos_prod_combo_group_items_group ON pos_prod_combo_group_items(combo_group_id);
CREATE INDEX idx_pos_prod_combo_group_items_item  ON pos_prod_combo_group_items(item_id);
