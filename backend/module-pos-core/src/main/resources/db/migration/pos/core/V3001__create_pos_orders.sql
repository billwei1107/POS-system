-- V3001: POS 訂單主表 / POS orders table
CREATE TABLE pos_orders (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    order_no        VARCHAR(40) NOT NULL UNIQUE,
    store_id        UUID        NOT NULL,
    terminal_id     UUID,
    employee_id     UUID,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    order_type      VARCHAR(20) NOT NULL DEFAULT 'DINE_IN',
    subtotal        DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_total  DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_total       DECIMAL(12,2) NOT NULL DEFAULT 0,
    rounding_adj    DECIMAL(12,2) NOT NULL DEFAULT 0,
    grand_total     DECIMAL(12,2) NOT NULL DEFAULT 0,
    paid_total      DECIMAL(12,2) NOT NULL DEFAULT 0,
    change_given    DECIMAL(12,2) NOT NULL DEFAULT 0,
    member_id       UUID,
    note            TEXT,
    table_no        VARCHAR(20),
    guest_count     INTEGER,
    voided_at       TIMESTAMP,
    voided_by       UUID,
    void_reason     VARCHAR(200),
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_pos_orders_store_id    ON pos_orders(store_id);
CREATE INDEX idx_pos_orders_status      ON pos_orders(status);
CREATE INDEX idx_pos_orders_member_id   ON pos_orders(member_id);
CREATE INDEX idx_pos_orders_created_at  ON pos_orders(created_at);
CREATE INDEX idx_pos_orders_terminal_id ON pos_orders(terminal_id);
