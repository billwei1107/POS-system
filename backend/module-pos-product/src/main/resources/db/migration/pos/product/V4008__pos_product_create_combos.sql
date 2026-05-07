-- 套餐定義表 / Combo meal definitions
CREATE TABLE pos_prod_combos (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200)  NOT NULL,
    combo_price DECIMAL(12,2) NOT NULL,
    description TEXT,
    image_url   VARCHAR(500),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);
