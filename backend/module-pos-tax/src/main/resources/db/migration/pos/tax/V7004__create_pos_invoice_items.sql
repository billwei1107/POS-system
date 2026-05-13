-- V7004: POS 發票明細表 / Invoice line items
CREATE TABLE pos_invoice_items (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    invoice_id      UUID        NOT NULL REFERENCES pos_invoices(id),
    sequence_no     INT         NOT NULL,                  -- 品項序號 (1-based)
    product_id      UUID,
    description     VARCHAR(256) NOT NULL,                 -- 品名
    quantity        DECIMAL(10,3) NOT NULL,
    unit            VARCHAR(20)  NOT NULL DEFAULT '個',
    unit_price      DECIMAL(12,4) NOT NULL,                -- 單價（未稅）
    amount          DECIMAL(12,2) NOT NULL,                -- 小計（未稅）
    tax_type        VARCHAR(20)  NOT NULL DEFAULT 'INCLUSIVE',
    tax_rate        DECIMAL(6,4) NOT NULL DEFAULT 0.0500,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoice_items_invoice ON pos_invoice_items(invoice_id);
