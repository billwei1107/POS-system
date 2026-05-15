-- V3010: 訂單折扣來源欄位 / Order discount source columns
ALTER TABLE pos_orders
    ADD COLUMN discount_source VARCHAR(20),
    ADD COLUMN promotion_rule_id UUID,
    ADD COLUMN promotion_code VARCHAR(50),
    ADD COLUMN discount_label VARCHAR(120);

CREATE INDEX idx_pos_orders_discount_source ON pos_orders(discount_source);
CREATE INDEX idx_pos_orders_promotion_rule_id ON pos_orders(promotion_rule_id);
