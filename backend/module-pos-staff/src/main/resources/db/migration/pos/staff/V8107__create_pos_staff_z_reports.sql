-- V8107: Z Report 表 / Z Report (end-of-day close, with tamper-proof hash)
-- Z Report 重置寄存器並產生防篡改 Hash（SHA-256 over report content）

CREATE TABLE IF NOT EXISTS pos_staff_z_reports (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id             UUID          NOT NULL,
    report_date          DATE          NOT NULL,
    report_no            VARCHAR(30)   NOT NULL UNIQUE,
    generated_at         TIMESTAMPTZ   NOT NULL,
    period_start         TIMESTAMPTZ   NOT NULL,
    period_end           TIMESTAMPTZ   NOT NULL,
    total_sales          DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_refunds        DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_discounts      DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_tax            DECIMAL(14,2) NOT NULL DEFAULT 0,
    net_sales            DECIMAL(14,2) NOT NULL DEFAULT 0,
    gross_sales          DECIMAL(14,2) NOT NULL DEFAULT 0,
    cash_sales           DECIMAL(14,2) NOT NULL DEFAULT 0,
    card_sales           DECIMAL(14,2) NOT NULL DEFAULT 0,
    other_sales          DECIMAL(14,2) NOT NULL DEFAULT 0,
    cash_in_drawer       DECIMAL(12,2) NOT NULL DEFAULT 0,
    expected_cash        DECIMAL(12,2) NOT NULL DEFAULT 0,
    cash_variance        DECIMAL(12,2) NOT NULL DEFAULT 0,
    transaction_count    INTEGER       NOT NULL DEFAULT 0,
    refund_count         INTEGER       NOT NULL DEFAULT 0,
    void_count           INTEGER       NOT NULL DEFAULT 0,
    shift_count          INTEGER       NOT NULL DEFAULT 0,
    report_data          JSONB,
    content_hash         VARCHAR(64)   NOT NULL,
    generated_by         UUID          NOT NULL,
    reset_at             TIMESTAMPTZ,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at           TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_pos_z_reports_store_date  ON pos_staff_z_reports(store_id, report_date) WHERE deleted_at IS NULL;
CREATE INDEX        idx_pos_z_reports_gen_at       ON pos_staff_z_reports(generated_at);
