-- V8101: 班次主表 / Staff shift records
-- 記錄每位員工的班次開始/結束、交接班狀態與累計銷售統計

CREATE TABLE IF NOT EXISTS pos_staff_shifts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID         NOT NULL,
    employee_id     UUID         NOT NULL,
    terminal_id     UUID,
    shift_no        VARCHAR(30)  NOT NULL UNIQUE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    opened_at       TIMESTAMPTZ  NOT NULL,
    closed_at       TIMESTAMPTZ,
    opening_cash    DECIMAL(12,2) NOT NULL DEFAULT 0,
    closing_cash    DECIMAL(12,2),
    expected_cash   DECIMAL(12,2),
    cash_variance   DECIMAL(12,2),
    total_sales     DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_refunds   DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_discounts DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_tax       DECIMAL(14,2) NOT NULL DEFAULT 0,
    transaction_count INTEGER     NOT NULL DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT chk_shift_status CHECK (status IN ('OPEN','CLOSED','BLIND_CLOSED'))
);

CREATE INDEX idx_pos_staff_shifts_store_id       ON pos_staff_shifts(store_id);
CREATE INDEX idx_pos_staff_shifts_employee_id    ON pos_staff_shifts(employee_id);
CREATE INDEX idx_pos_staff_shifts_opened_at      ON pos_staff_shifts(opened_at);
CREATE INDEX idx_pos_staff_shifts_status         ON pos_staff_shifts(status);
