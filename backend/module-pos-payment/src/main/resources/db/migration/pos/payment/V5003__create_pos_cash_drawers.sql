-- V5003: POS 現金抽屜 / POS cash drawers
CREATE TABLE pos_cash_drawers (
    id              UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id        UUID            NOT NULL,
    terminal_id     UUID            NOT NULL,
    opening_amount  DECIMAL(12, 2)  NOT NULL DEFAULT 0,
    closing_amount  DECIMAL(12, 2),
    expected_amount DECIMAL(12, 2),
    variance        DECIMAL(12, 2),
    opened_by       UUID            NOT NULL,
    closed_by       UUID,
    opened_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMPTZ,
    status          VARCHAR(20)     NOT NULL DEFAULT 'OPEN', -- OPEN / CLOSED
    note            TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE pos_cash_drawer_events (
    id              UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    drawer_id       UUID            NOT NULL REFERENCES pos_cash_drawers(id),
    event_type      VARCHAR(30)     NOT NULL, -- OPEN / CLOSE / SALE / REFUND / PAY_IN / PAY_OUT / NO_SALE
    amount          DECIMAL(12, 2)  NOT NULL DEFAULT 0,
    order_id        UUID,
    employee_id     UUID            NOT NULL,
    note            TEXT,
    occurred_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cash_drawer_store     ON pos_cash_drawers (store_id, terminal_id);
CREATE INDEX idx_cash_drawer_events_id ON pos_cash_drawer_events (drawer_id, occurred_at DESC);
