-- V3007: 禮品卡主檔與交易記錄 / Gift cards and transactions
CREATE TABLE pos_gift_cards (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    card_no         VARCHAR(30) NOT NULL UNIQUE,
    balance         DECIMAL(12,2) NOT NULL DEFAULT 0,
    initial_value   DECIMAL(12,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at      TIMESTAMP,
    issued_by       UUID,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE TABLE pos_gift_card_transactions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id         UUID        NOT NULL,
    order_id        UUID,
    txn_type        VARCHAR(20) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    balance_after   DECIMAL(12,2) NOT NULL,
    note            VARCHAR(200),
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_pos_gift_cards_card_no       ON pos_gift_cards(card_no);
CREATE INDEX idx_pos_gift_card_txn_card_id    ON pos_gift_card_transactions(card_id);
CREATE INDEX idx_pos_gift_card_txn_order_id   ON pos_gift_card_transactions(order_id);
