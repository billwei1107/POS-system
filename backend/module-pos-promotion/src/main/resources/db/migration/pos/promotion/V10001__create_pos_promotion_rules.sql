-- POS 促銷規則 / POS promotion rules
CREATE TABLE IF NOT EXISTS pos_promo_rules (
    id                    UUID PRIMARY KEY,
    store_id              UUID,
    name                  VARCHAR(120) NOT NULL,
    code                  VARCHAR(50),
    trigger_type          VARCHAR(20) NOT NULL DEFAULT 'AUTO',
    discount_type         VARCHAR(20) NOT NULL DEFAULT 'PERCENT',
    discount_value        DECIMAL(12, 2) NOT NULL,
    minimum_subtotal      DECIMAL(12, 2) NOT NULL DEFAULT 0,
    max_discount_amount   DECIMAL(12, 2),
    starts_at             TIMESTAMP,
    ends_at               TIMESTAMP,
    active                BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at            TIMESTAMP,
    CONSTRAINT ck_pos_promo_trigger_type CHECK (trigger_type IN ('AUTO', 'CODE')),
    CONSTRAINT ck_pos_promo_discount_type CHECK (discount_type IN ('PERCENT', 'AMOUNT')),
    CONSTRAINT ck_pos_promo_discount_value CHECK (discount_value > 0),
    CONSTRAINT ck_pos_promo_minimum_subtotal CHECK (minimum_subtotal >= 0),
    CONSTRAINT ck_pos_promo_max_discount_amount CHECK (max_discount_amount IS NULL OR max_discount_amount >= 0),
    CONSTRAINT ck_pos_promo_code_trigger CHECK (
        (trigger_type = 'CODE' AND code IS NOT NULL AND btrim(code) <> '')
        OR (trigger_type = 'AUTO' AND code IS NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_pos_promo_rules_store_active
    ON pos_promo_rules (store_id, active);

CREATE INDEX IF NOT EXISTS idx_pos_promo_rules_global_active
    ON pos_promo_rules (active)
    WHERE store_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_pos_promo_rules_code_store_active
    ON pos_promo_rules (COALESCE(store_id, '00000000-0000-0000-0000-000000000000'::uuid), UPPER(code))
    WHERE code IS NOT NULL AND deleted_at IS NULL;

INSERT INTO pos_promo_rules (
    id, store_id, name, code, trigger_type, discount_type, discount_value,
    minimum_subtotal, max_discount_amount, active
) VALUES
    (
        '00000000-0000-0000-0000-000000001001',
        NULL,
        '咖啡滿百 9 折',
        NULL,
        'AUTO',
        'PERCENT',
        10.00,
        100.00,
        80.00,
        TRUE
    ),
    (
        '00000000-0000-0000-0000-000000001002',
        NULL,
        '優惠碼 CAFE20',
        'CAFE20',
        'CODE',
        'AMOUNT',
        20.00,
        120.00,
        NULL,
        TRUE
    )
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    trigger_type = EXCLUDED.trigger_type,
    discount_type = EXCLUDED.discount_type,
    discount_value = EXCLUDED.discount_value,
    minimum_subtotal = EXCLUDED.minimum_subtotal,
    max_discount_amount = EXCLUDED.max_discount_amount,
    active = EXCLUDED.active,
    updated_at = NOW();
