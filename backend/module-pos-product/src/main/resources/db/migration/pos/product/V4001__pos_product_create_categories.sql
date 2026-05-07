-- 商品分類表 / Product categories
CREATE TABLE pos_prod_categories (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    parent_id   UUID        REFERENCES pos_prod_categories(id) ON DELETE SET NULL,
    sort_order  INT         NOT NULL DEFAULT 0,
    image_url   VARCHAR(500),
    display_color VARCHAR(20),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_pos_prod_categories_parent ON pos_prod_categories(parent_id);
CREATE INDEX idx_pos_prod_categories_active ON pos_prod_categories(active) WHERE deleted_at IS NULL;
