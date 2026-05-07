-- V7002: POS 稅務規則表 / Tax rules mapping items to tax classes
-- 支援商品類別覆蓋稅率
CREATE TABLE pos_tax_rules (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id        UUID        NOT NULL,
    tax_class_id    UUID        NOT NULL REFERENCES pos_tax_classes(id),
    rule_name       VARCHAR(100) NOT NULL,
    -- 優先套用順序：product_id > category_id > default
    product_id      UUID,
    category_id     UUID,
    priority        INT         NOT NULL DEFAULT 0,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tax_rules_store ON pos_tax_rules(store_id, is_active);
CREATE INDEX idx_tax_rules_product ON pos_tax_rules(product_id) WHERE product_id IS NOT NULL;
CREATE INDEX idx_tax_rules_category ON pos_tax_rules(category_id) WHERE category_id IS NOT NULL;
