-- 商品主表 / Product items
CREATE TABLE pos_prod_items (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    sku             VARCHAR(100)    NOT NULL UNIQUE,
    name            VARCHAR(200)    NOT NULL,
    description     TEXT,
    category_id     UUID            REFERENCES pos_prod_categories(id) ON DELETE SET NULL,
    base_price      DECIMAL(12,2)   NOT NULL DEFAULT 0,
    cost_price      DECIMAL(12,2),
    tax_class_id    UUID,
    unit            VARCHAR(10)     NOT NULL DEFAULT 'PCS' CHECK (unit IN ('PCS','KG','LB','ML','L')),
    barcode_primary VARCHAR(100),
    image_url       VARCHAR(500),
    track_inventory BOOLEAN         NOT NULL DEFAULT FALSE,
    sellable        BOOLEAN         NOT NULL DEFAULT TRUE,
    weight_based    BOOLEAN         NOT NULL DEFAULT FALSE,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_pos_prod_items_sku        ON pos_prod_items(sku);
CREATE INDEX idx_pos_prod_items_category   ON pos_prod_items(category_id);
CREATE INDEX idx_pos_prod_items_barcode    ON pos_prod_items(barcode_primary) WHERE barcode_primary IS NOT NULL;
CREATE INDEX idx_pos_prod_items_active     ON pos_prod_items(active) WHERE deleted_at IS NULL;
