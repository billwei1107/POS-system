-- POS CRM 點數流水來源索引 / POS CRM point ledger references
ALTER TABLE pos_crm_point_ledgers
    ADD COLUMN IF NOT EXISTS reference_id UUID,
    ADD COLUMN IF NOT EXISTS reference_type VARCHAR(40);

UPDATE pos_crm_point_ledgers
SET reference_id = order_id,
    reference_type = 'ORDER'
WHERE reference_id IS NULL
  AND order_id IS NOT NULL
  AND reason = 'ORDER_EARN';

DROP INDEX IF EXISTS idx_pos_crm_point_ledgers_order_reason;

CREATE UNIQUE INDEX IF NOT EXISTS idx_pos_crm_point_ledgers_reference_reason
    ON pos_crm_point_ledgers (reference_id, reason)
    WHERE reference_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_pos_crm_point_ledgers_order_reason
    ON pos_crm_point_ledgers (order_id, reason);
