-- V7003: POS 電子發票主表 / Electronic invoice (e-invoice) master table
-- 符合台灣財政部電子發票 MIG 3.2 規範
CREATE TABLE pos_invoices (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id        UUID        NOT NULL,
    order_id        UUID        NOT NULL,
    -- 發票號碼 (8碼數字流水號，完整號碼 = track_prefix + invoice_no)
    track_id        UUID        REFERENCES pos_invoice_tracks(id),
    invoice_no      VARCHAR(8),                           -- e.g. 00000001
    full_invoice_no VARCHAR(12),                          -- e.g. AB-00000001
    -- 發票類型
    invoice_type    VARCHAR(20) NOT NULL DEFAULT 'B2C',   -- B2C | B2B
    -- 銷售方資訊
    seller_id       VARCHAR(8)  NOT NULL,                 -- 統一編號 (8碼)
    seller_name     VARCHAR(100) NOT NULL,
    -- 買方資訊 (B2C 可空)
    buyer_id        VARCHAR(8),                           -- 統一編號 (若有)
    buyer_name      VARCHAR(100),
    buyer_email     VARCHAR(200),                         -- 寄送載具用
    -- 載具 (Carrier) 資訊
    carrier_type    VARCHAR(20),                          -- MEMBER | MOBILE | CITIZEN_DIGITAL
    carrier_id1     VARCHAR(64),                          -- 載具隱碼
    carrier_id2     VARCHAR(64),                          -- 載具顯碼
    -- 愛心碼 (Donate)
    donate_code     VARCHAR(7),
    -- 金額 (含稅)
    sales_amount    DECIMAL(12,2) NOT NULL,               -- 稅前
    tax_amount      DECIMAL(12,2) NOT NULL,               -- 稅額
    total_amount    DECIMAL(12,2) NOT NULL,               -- 含稅總額
    -- 狀態
    status          VARCHAR(20)  NOT NULL DEFAULT 'ISSUED', -- ISSUED | VOIDED | ALLOWANCE
    -- 上傳財政部狀態
    upload_status   VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING | SUCCESS | FAILED
    upload_at       TIMESTAMP,
    upload_resp     TEXT,
    -- 時間戳記
    issue_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    void_at         TIMESTAMP,
    void_reason     VARCHAR(200),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_invoices_full_no ON pos_invoices(store_id, full_invoice_no) WHERE full_invoice_no IS NOT NULL;
CREATE INDEX idx_invoices_order ON pos_invoices(order_id);
CREATE INDEX idx_invoices_store_date ON pos_invoices(store_id, issue_at);
CREATE INDEX idx_invoices_upload ON pos_invoices(upload_status) WHERE upload_status = 'PENDING';
