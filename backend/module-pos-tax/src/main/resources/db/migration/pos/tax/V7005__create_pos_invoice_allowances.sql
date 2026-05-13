-- V7005: POS 發票折讓單 / Invoice allowance (credit note)
-- 退款時對應發票折讓，若全額退款則作廢原發票
CREATE TABLE pos_invoice_allowances (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id        UUID        NOT NULL,
    invoice_id      UUID        NOT NULL REFERENCES pos_invoices(id),
    refund_id       UUID        NOT NULL,
    allowance_no    VARCHAR(20),                           -- 折讓編號（財政部給）
    sales_amount    DECIMAL(12,2) NOT NULL,
    tax_amount      DECIMAL(12,2) NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ISSUED', -- ISSUED | FAILED
    upload_status   VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    upload_at       TIMESTAMP,
    upload_resp     TEXT,
    issue_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_allowances_invoice ON pos_invoice_allowances(invoice_id);
CREATE INDEX idx_allowances_refund ON pos_invoice_allowances(refund_id);
