-- V8105: 交接班記錄表 / Shift handover records

CREATE TABLE IF NOT EXISTS pos_staff_shift_handovers (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_shift_id     UUID          NOT NULL,
    to_shift_id       UUID,
    store_id          UUID          NOT NULL,
    handover_at       TIMESTAMPTZ   NOT NULL,
    cash_counted      DECIMAL(12,2) NOT NULL DEFAULT 0,
    cash_expected     DECIMAL(12,2) NOT NULL DEFAULT 0,
    cash_variance     DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes             TEXT,
    confirmed_by      UUID,
    confirmed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ
);

CREATE INDEX idx_pos_staff_handover_from_shift ON pos_staff_shift_handovers(from_shift_id);
CREATE INDEX idx_pos_staff_handover_store       ON pos_staff_shift_handovers(store_id);
