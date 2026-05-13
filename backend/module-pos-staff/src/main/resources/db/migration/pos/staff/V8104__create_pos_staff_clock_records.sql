-- V8104: 打卡記錄表 / Clock in/out records

CREATE TABLE IF NOT EXISTS pos_staff_clock_records (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shift_id     UUID        NOT NULL,
    employee_id  UUID        NOT NULL,
    store_id     UUID        NOT NULL,
    clock_type   VARCHAR(10) NOT NULL,
    clocked_at   TIMESTAMPTZ NOT NULL,
    terminal_id  UUID,
    location     VARCHAR(200),
    notes        TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ,
    CONSTRAINT chk_clock_type CHECK (clock_type IN ('IN','OUT','BREAK_START','BREAK_END'))
);

CREATE INDEX idx_pos_staff_clock_shift      ON pos_staff_clock_records(shift_id);
CREATE INDEX idx_pos_staff_clock_employee   ON pos_staff_clock_records(employee_id);
CREATE INDEX idx_pos_staff_clock_clocked_at ON pos_staff_clock_records(clocked_at);
