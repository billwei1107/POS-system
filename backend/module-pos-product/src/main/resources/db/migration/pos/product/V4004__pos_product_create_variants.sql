-- 商品變體表 / Product variants (e.g. size: S/M/L, color: red/blue)
CREATE TABLE pos_prod_variants (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id        UUID          NOT NULL REFERENCES pos_prod_items(id) ON DELETE CASCADE,
    variant_name   VARCHAR(100)  NOT NULL,
    sku            VARCHAR(100)  NOT NULL UNIQUE,
    barcode        VARCHAR(100),
    price_override DECIMAL(12,2),
    cost_override  DECIMAL(12,2),
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMP
);

CREATE INDEX idx_pos_prod_variants_item ON pos_prod_variants(item_id);
CREATE INDEX idx_pos_prod_variants_sku  ON pos_prod_variants(sku);
