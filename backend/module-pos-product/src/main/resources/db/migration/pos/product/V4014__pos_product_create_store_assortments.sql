-- 門店商品分配表 / Store assortments (which items are available at which store)
CREATE TABLE pos_prod_store_assortments (
    id            UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id       UUID      NOT NULL REFERENCES pos_prod_items(id) ON DELETE CASCADE,
    store_id      UUID      NOT NULL,
    available     BOOLEAN   NOT NULL DEFAULT TRUE,
    display_order INT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (item_id, store_id)
);

CREATE INDEX idx_pos_prod_store_assortments_item  ON pos_prod_store_assortments(item_id);
CREATE INDEX idx_pos_prod_store_assortments_store ON pos_prod_store_assortments(store_id);
