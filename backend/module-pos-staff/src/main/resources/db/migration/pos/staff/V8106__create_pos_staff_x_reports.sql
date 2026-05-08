-- V8106: X Report 表 / X Report (mid-shift snapshot, no reset)
-- X Report 不重置寄存器，僅快照當前班次累計數據

CREATE TABLE IF NOT EXISTS pos_staff_x_reports (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shift_id             UUID          NOT NULL,
    store_id             UUID          NOT NULL,
    employee_id          UUID          NOT NULL,
    generated_at         TIMESTAMPTZ   NOT NULL,
    period_start         TIMESTAMPTZ   NOT NULL,
    period_end           TIMESTAMPTZ   NOT NULL,
    total_sales          DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_refunds        DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_discounts      DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_tax            DECIMAL(14,2) NOT NULL DEFAULT 0,
    net_sales            DECIMAL(14,2) NOT NULL DEFAULT 0,
    cash_sales           DECIMAL(14,2) NOT NULL DEFAULT 0,
    card_sales           DECIMAL(14,2) NOT NULL DEFAULT 0,
    other_sales          DECIMAL(14,2) NOT NULL DEFAULT 0,
    transaction_count    INTEGER       NOT NULL DEFAULT 0,
    refund_count         INTEGER       NOT NULL DEFAULT 0,
    void_count           INTEGER       NOT NULL DEFAULT 0,
    report_data          JSONB,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at           TIMESTAMPTZ
);

CREATE INDEX idx_pos_x_reports_shift   ON pos_staff_x_reports(shift_id);
CREATE INDEX idx_pos_x_reports_store   ON pos_staff_x_reports(store_id);
CREATE INDEX idx_pos_x_reports_gen_at  ON pos_staff_x_reports(generated_at);
