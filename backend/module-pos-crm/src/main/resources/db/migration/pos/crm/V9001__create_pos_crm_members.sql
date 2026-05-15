-- POS CRM 會員與點數帳 / POS CRM members and loyalty ledger
CREATE TABLE IF NOT EXISTS pos_crm_members (
    id                   UUID PRIMARY KEY,
    member_no            VARCHAR(50) NOT NULL UNIQUE,
    name                 VARCHAR(120) NOT NULL,
    phone                VARCHAR(30) UNIQUE,
    email                VARCHAR(160),
    birthday             DATE,
    card_no              VARCHAR(80) UNIQUE,
    barcode              VARCHAR(120) UNIQUE,
    tier                 VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    discount_percent     DECIMAL(5, 2) NOT NULL DEFAULT 0,
    points_balance       INTEGER NOT NULL DEFAULT 0,
    stored_value_balance DECIMAL(12, 2) NOT NULL DEFAULT 0,
    annual_spend         DECIMAL(12, 2) NOT NULL DEFAULT 0,
    active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at           TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pos_crm_members_query
    ON pos_crm_members (member_no, phone, card_no, barcode);

CREATE INDEX IF NOT EXISTS idx_pos_crm_members_active_tier
    ON pos_crm_members (active, tier);

CREATE TABLE IF NOT EXISTS pos_crm_point_ledgers (
    id             UUID PRIMARY KEY,
    member_id      UUID NOT NULL REFERENCES pos_crm_members(id),
    order_id       UUID,
    points_delta   INTEGER NOT NULL,
    balance_after  INTEGER NOT NULL,
    reason         VARCHAR(40) NOT NULL,
    note           VARCHAR(255),
    occurred_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pos_crm_point_ledgers_member
    ON pos_crm_point_ledgers (member_id, occurred_at DESC);

CREATE UNIQUE INDEX IF NOT EXISTS idx_pos_crm_point_ledgers_order_reason
    ON pos_crm_point_ledgers (order_id, reason)
    WHERE order_id IS NOT NULL;

INSERT INTO pos_crm_members (
    id, member_no, name, phone, tier, discount_percent, points_balance, stored_value_balance, annual_spend
) VALUES
    ('00000000-0000-0000-0000-000000000801', 'M-000801', '林依晨', '0912000801', 'GOLD', 10.00, 1280, 0.00, 12800.00),
    ('00000000-0000-0000-0000-000000000802', 'M-000802', '陳柏宇', '0922000802', 'SILVER', 5.00, 640, 0.00, 6400.00),
    ('00000000-0000-0000-0000-000000000803', 'M-000803', '王小安', '0933000803', 'BRONZE', 0.00, 120, 0.00, 1200.00)
ON CONFLICT (id) DO UPDATE SET
    member_no = EXCLUDED.member_no,
    name = EXCLUDED.name,
    phone = EXCLUDED.phone,
    tier = EXCLUDED.tier,
    discount_percent = EXCLUDED.discount_percent,
    updated_at = NOW();
