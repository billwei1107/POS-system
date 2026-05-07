-- V7001: POS 稅率類別表 / Tax class definitions
-- 支援含稅/外加稅/免稅/零稅率，預設含稅 5%
CREATE TABLE pos_tax_classes (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id    UUID        NOT NULL,
    name        VARCHAR(50) NOT NULL,
    tax_type    VARCHAR(20) NOT NULL, -- INCLUSIVE | EXCLUSIVE | EXEMPT | ZERO_RATED
    rate        DECIMAL(6,4) NOT NULL DEFAULT 0.0500, -- 0.05 = 5%
    description VARCHAR(200),
    is_default  BOOLEAN     NOT NULL DEFAULT FALSE,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_tax_classes_store ON pos_tax_classes(store_id) WHERE deleted_at IS NULL;

-- V7002 migration inserts default 5% inclusive tax class
