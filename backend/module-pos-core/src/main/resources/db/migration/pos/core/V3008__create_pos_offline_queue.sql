-- V3008: 離線作業佇列 / Offline operation queue for sync
CREATE TABLE pos_offline_queue (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID        NOT NULL,
    terminal_id     UUID        NOT NULL,
    operation_type  VARCHAR(30) NOT NULL,
    payload         TEXT        NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count     INTEGER     NOT NULL DEFAULT 0,
    last_error      TEXT,
    processed_at    TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_pos_offline_queue_store_terminal ON pos_offline_queue(store_id, terminal_id);
CREATE INDEX idx_pos_offline_queue_status         ON pos_offline_queue(status);
