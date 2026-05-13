-- 價格規則表 / Price rules (member price, happy hour, bulk discount)
CREATE TABLE pos_prod_price_rules (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id        UUID          NOT NULL REFERENCES pos_prod_items(id) ON DELETE CASCADE,
    store_id       UUID,
    price_type     VARCHAR(20)   NOT NULL CHECK (price_type IN ('BASE','MEMBER','HAPPY_HOUR','BULK','STAFF')),
    price          DECIMAL(12,2) NOT NULL,
    min_qty        INT           NOT NULL DEFAULT 1,
    effective_from TIMESTAMP,
    effective_to   TIMESTAMP,
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMP
);

CREATE INDEX idx_pos_prod_price_rules_item  ON pos_prod_price_rules(item_id);
CREATE INDEX idx_pos_prod_price_rules_store ON pos_prod_price_rules(store_id) WHERE store_id IS NOT NULL;
CREATE INDEX idx_pos_prod_price_rules_type  ON pos_prod_price_rules(price_type, active);
