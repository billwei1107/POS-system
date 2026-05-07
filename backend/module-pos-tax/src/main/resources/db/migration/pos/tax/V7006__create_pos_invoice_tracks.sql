-- V7006: POS 發票字軌表 / Invoice character track (字軌) pool
-- 財政部每兩月配發的字軌號碼範圍，用於發票號碼流水號管理
-- 注意: V7003 的 track_id FK 在此表建立後才生效，實際 FK 約束由應用層保證
CREATE TABLE pos_invoice_tracks (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id        UUID        NOT NULL,
    seller_id       VARCHAR(8)  NOT NULL,                  -- 統一編號
    track_prefix    VARCHAR(2)  NOT NULL,                  -- 字軌前兩碼，如 AB
    year_month      VARCHAR(6)  NOT NULL,                  -- 民國年月，如 11401（114年01月）
    period          VARCHAR(10) NOT NULL,                  -- 發票期別，如 11401-11402
    start_no        VARCHAR(8)  NOT NULL,                  -- 起始號 00000001
    end_no          VARCHAR(8)  NOT NULL,                  -- 結束號 00000050
    current_no      VARCHAR(8)  NOT NULL DEFAULT '00000000', -- 已用至號碼（0 表示尚未使用）
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE(store_id, track_prefix, year_month)
);

CREATE INDEX idx_invoice_tracks_store ON pos_invoice_tracks(store_id, is_active);
CREATE INDEX idx_invoice_tracks_active ON pos_invoice_tracks(store_id, is_active, year_month) WHERE is_active = TRUE;

-- 補 V7003 中 track_id 的外鍵約束（因為 tracks 表在 invoices 表之後建立，需用 ALTER）
ALTER TABLE pos_invoices ADD CONSTRAINT fk_invoices_track
    FOREIGN KEY (track_id) REFERENCES pos_invoice_tracks(id);
