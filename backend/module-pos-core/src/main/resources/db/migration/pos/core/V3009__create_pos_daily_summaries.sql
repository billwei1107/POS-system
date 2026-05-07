-- V3009: 每日銷售彙總（Z Report 用）/ Daily sales summary for Z Report
CREATE TABLE pos_daily_summaries (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID        NOT NULL,
    summary_date    DATE        NOT NULL,
    order_count     INTEGER     NOT NULL DEFAULT 0,
    void_count      INTEGER     NOT NULL DEFAULT 0,
    refund_count    INTEGER     NOT NULL DEFAULT 0,
    gross_sales     DECIMAL(14,2) NOT NULL DEFAULT 0,
    discount_total  DECIMAL(14,2) NOT NULL DEFAULT 0,
    refund_total    DECIMAL(14,2) NOT NULL DEFAULT 0,
    net_sales       DECIMAL(14,2) NOT NULL DEFAULT 0,
    tax_total       DECIMAL(14,2) NOT NULL DEFAULT 0,
    cash_total      DECIMAL(14,2) NOT NULL DEFAULT 0,
    card_total      DECIMAL(14,2) NOT NULL DEFAULT 0,
    other_total     DECIMAL(14,2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP,
    UNIQUE(store_id, summary_date)
);

CREATE INDEX idx_pos_daily_summaries_store_date ON pos_daily_summaries(store_id, summary_date);
