-- 商品多條碼表 / Product multiple barcodes
CREATE TABLE pos_prod_item_barcodes (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id      UUID        NOT NULL REFERENCES pos_prod_items(id) ON DELETE CASCADE,
    barcode      VARCHAR(100) NOT NULL,
    barcode_type VARCHAR(20) NOT NULL DEFAULT 'EAN13' CHECK (barcode_type IN ('EAN13','CODE128','QR','UPC','CODE39')),
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMP,
    UNIQUE (item_id, barcode)
);

CREATE INDEX idx_pos_prod_item_barcodes_item    ON pos_prod_item_barcodes(item_id);
CREATE INDEX idx_pos_prod_item_barcodes_barcode ON pos_prod_item_barcodes(barcode);
