-- V5006: 外幣匯率設定 / Foreign currency exchange rates
CREATE TABLE pos_foreign_currencies (
    id              UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id        UUID            NOT NULL,
    currency_code   VARCHAR(3)      NOT NULL, -- ISO 4217: USD, JPY, CNY, HKD, EUR
    currency_name   VARCHAR(50)     NOT NULL,
    buy_rate        DECIMAL(12, 6)  NOT NULL, -- store buys foreign currency at this rate
    sell_rate       DECIMAL(12, 6)  NOT NULL, -- store sells foreign currency at this rate
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    effective_date  DATE            NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    UNIQUE (store_id, currency_code)
);

CREATE INDEX idx_foreign_currency_store ON pos_foreign_currencies (store_id) WHERE deleted_at IS NULL;
