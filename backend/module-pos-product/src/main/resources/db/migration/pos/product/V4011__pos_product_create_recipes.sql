-- 食材配方 (BOM) 表 / Recipe / Bill of Materials
CREATE TABLE pos_prod_recipes (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id             UUID          NOT NULL REFERENCES pos_prod_items(id) ON DELETE CASCADE,
    ingredient_item_id  UUID          NOT NULL REFERENCES pos_prod_items(id) ON DELETE CASCADE,
    quantity_required   DECIMAL(12,4) NOT NULL,
    unit                VARCHAR(10)   NOT NULL DEFAULT 'PCS' CHECK (unit IN ('PCS','KG','LB','ML','L','G')),
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    UNIQUE (item_id, ingredient_item_id)
);

CREATE INDEX idx_pos_prod_recipes_item       ON pos_prod_recipes(item_id);
CREATE INDEX idx_pos_prod_recipes_ingredient ON pos_prod_recipes(ingredient_item_id);
