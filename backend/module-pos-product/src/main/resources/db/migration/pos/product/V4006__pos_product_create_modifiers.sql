-- 客製化選項表 / Modifier options (e.g. "No Sugar", "Less Sugar")
CREATE TABLE pos_prod_modifiers (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id         UUID          NOT NULL REFERENCES pos_prod_modifier_groups(id) ON DELETE CASCADE,
    name             VARCHAR(100)  NOT NULL,
    price_adjustment DECIMAL(12,2) NOT NULL DEFAULT 0,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order       INT           NOT NULL DEFAULT 0,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMP
);

CREATE INDEX idx_pos_prod_modifiers_group ON pos_prod_modifiers(group_id);
