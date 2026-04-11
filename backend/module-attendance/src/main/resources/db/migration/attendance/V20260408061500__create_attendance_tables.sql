-- =============================================
-- 打卡考勤模組資料表 / Attendance Module Tables
-- =============================================

-- 班表定義 / Shift Schedule Definition
CREATE TABLE att_shift_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'FIXED',  -- FIXED / FLEXIBLE / ROTATING
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    grace_minutes INTEGER NOT NULL DEFAULT 5,
    break_start TIME,
    break_end TIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 班表指派 / Shift Assignment
CREATE TABLE att_shift_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id VARCHAR(36) NOT NULL,
    shift_schedule_id UUID NOT NULL REFERENCES att_shift_schedules(id),
    effective_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_att_shift_assign_emp ON att_shift_assignments(employee_id);

-- 打卡記錄 / Attendance Record
CREATE TABLE att_attendance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id VARCHAR(36) NOT NULL,
    date DATE NOT NULL,
    clock_in_time TIMESTAMP,
    clock_out_time TIMESTAMP,
    clock_in_location JSONB,
    clock_in_method VARCHAR(10) DEFAULT 'GPS',   -- GPS / WIFI / QR
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL', -- NORMAL / LATE / EARLY_LEAVE / ABSENT
    overtime_minutes INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_att_records_emp ON att_attendance_records(employee_id);
CREATE INDEX idx_att_records_date ON att_attendance_records(date);
CREATE UNIQUE INDEX uk_att_records_emp_date ON att_attendance_records(employee_id, date);

-- 補卡申請 / Attendance Correction
CREATE TABLE att_attendance_corrections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID REFERENCES att_attendance_records(id),
    employee_id VARCHAR(36) NOT NULL,
    reason TEXT NOT NULL,
    original_time TIMESTAMP,
    corrected_time TIMESTAMP NOT NULL,
    correction_type VARCHAR(20) NOT NULL DEFAULT 'CLOCK_IN', -- CLOCK_IN / CLOCK_OUT
    workflow_instance_id VARCHAR(36),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING / APPROVED / REJECTED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 假日設定 / Holiday
CREATE TABLE att_holidays (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'NATIONAL', -- NATIONAL / COMPANY
    year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE UNIQUE INDEX uk_att_holidays_date ON att_holidays(date);

-- 地理圍欄 / Geofence
CREATE TABLE att_geofences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    radius_meters INTEGER NOT NULL DEFAULT 200,
    wifi_bssids JSONB,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
