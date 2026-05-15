CREATE TABLE auth_audit_logs (
    id UUID PRIMARY KEY,
    module VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    method_name VARCHAR(255) NOT NULL,
    user_id UUID,
    role_code VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(500),
    duration_ms BIGINT NOT NULL DEFAULT 0,
    occurred_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_auth_audit_logs_module_action ON auth_audit_logs(module, action);
CREATE INDEX idx_auth_audit_logs_user_time ON auth_audit_logs(user_id, occurred_at DESC);
CREATE INDEX idx_auth_audit_logs_status_time ON auth_audit_logs(status, occurred_at DESC);
