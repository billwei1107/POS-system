-- ========================================
-- 假別類型 / Leave types
-- ========================================
CREATE TABLE leave_types (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) NOT NULL,
    code        VARCHAR(20) NOT NULL UNIQUE,
    paid_type   VARCHAR(20) NOT NULL CHECK (paid_type IN ('PAID','UNPAID','HALF_PAY')),
    require_attachment BOOLEAN NOT NULL DEFAULT FALSE,
    max_days_per_year  NUMERIC(5,1),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);

-- ========================================
-- 假別政策（年資對應額度）/ Leave policies
-- ========================================
CREATE TABLE leave_policies (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    leave_type_id     UUID NOT NULL,
    min_service_years INT  NOT NULL DEFAULT 0,
    max_service_years INT,
    annual_quota      NUMERIC(5,1) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ
);

-- ========================================
-- 員工餘假 / Leave balances per employee per year
-- ========================================
CREATE TABLE leave_balances (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     UUID NOT NULL,
    leave_type_id   UUID NOT NULL,
    year            INT  NOT NULL,
    total_days      NUMERIC(5,1) NOT NULL DEFAULT 0,
    used_days       NUMERIC(5,1) NOT NULL DEFAULT 0,
    remaining_days  NUMERIC(5,1) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,
    UNIQUE (employee_id, leave_type_id, year)
);

-- ========================================
-- 請假申請 / Leave requests
-- ========================================
CREATE TABLE leave_requests (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id          UUID NOT NULL,
    leave_type_id        UUID NOT NULL,
    start_date           DATE NOT NULL,
    end_date             DATE NOT NULL,
    start_half           VARCHAR(20) NOT NULL DEFAULT 'FULL' CHECK (start_half IN ('FULL','MORNING','AFTERNOON')),
    end_half             VARCHAR(20) NOT NULL DEFAULT 'FULL' CHECK (end_half IN ('FULL','MORNING','AFTERNOON')),
    total_hours          NUMERIC(6,2) NOT NULL DEFAULT 0,
    reason               TEXT,
    attachment_path      VARCHAR(500),
    delegate_id          UUID,
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED')),
    workflow_instance_id UUID,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at           TIMESTAMPTZ
);

-- ========================================
-- 請假審批記錄 / Leave approval records
-- ========================================
CREATE TABLE leave_approvals (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id  UUID NOT NULL,
    approver_id UUID NOT NULL,
    action      VARCHAR(20) NOT NULL CHECK (action IN ('APPROVE','REJECT','FORWARD')),
    comment     TEXT,
    operated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status   ON leave_requests(status);
CREATE INDEX idx_leave_balances_employee ON leave_balances(employee_id, year);
CREATE INDEX idx_leave_approvals_request ON leave_approvals(request_id);
