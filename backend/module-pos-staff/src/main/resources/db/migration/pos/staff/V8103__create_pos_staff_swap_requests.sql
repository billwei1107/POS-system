-- V8103: 換班申請表 / Shift swap requests

CREATE TABLE IF NOT EXISTS pos_staff_swap_requests (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id     UUID        NOT NULL,
    target_id        UUID        NOT NULL,
    requester_sched  UUID        NOT NULL,
    target_sched     UUID        NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason           TEXT,
    approved_by      UUID,
    approved_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ,
    CONSTRAINT chk_swap_status CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED'))
);

CREATE INDEX idx_pos_staff_swap_requester ON pos_staff_swap_requests(requester_id);
CREATE INDEX idx_pos_staff_swap_target    ON pos_staff_swap_requests(target_id);
