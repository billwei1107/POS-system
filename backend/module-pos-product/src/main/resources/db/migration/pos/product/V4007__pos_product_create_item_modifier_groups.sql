-- 商品-客製化群組關聯表 / Item to modifier group mapping
CREATE TABLE pos_prod_item_modifier_groups (
    id               UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id          UUID      NOT NULL REFERENCES pos_prod_items(id) ON DELETE CASCADE,
    modifier_group_id UUID     NOT NULL REFERENCES pos_prod_modifier_groups(id) ON DELETE CASCADE,
    sort_order       INT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (item_id, modifier_group_id)
);

CREATE INDEX idx_pos_prod_item_mod_groups_item  ON pos_prod_item_modifier_groups(item_id);
CREATE INDEX idx_pos_prod_item_mod_groups_group ON pos_prod_item_modifier_groups(modifier_group_id);
