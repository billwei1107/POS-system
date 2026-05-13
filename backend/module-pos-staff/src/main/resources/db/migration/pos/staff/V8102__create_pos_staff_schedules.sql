-- V8102: 排班計劃表 / Staff schedule plans
-- 記錄每日排班計劃（計劃上班時段、實際班次關聯）

CREATE TABLE IF NOT EXISTS pos_staff_schedules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID        NOT NULL,
    employee_id     UUID        NOT NULL,
    work_date       DATE        NOT NULL,
    planned_start   TIME        NOT NULL,
    planned_end     TIME        NOT NULL,
    actual_shift_id UUID,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT chk_schedule_status CHECK (status IN ('SCHEDULED','CONFIRMED','ABSENT','SWAPPED'))
);

CREATE INDEX idx_pos_staff_schedules_store_employee ON pos_staff_schedules(store_id, employee_id);
CREATE INDEX idx_pos_staff_schedules_work_date      ON pos_staff_schedules(work_date);
