-- V3006: 暫存訂單（掛單）/ Held / parked orders
CREATE TABLE pos_held_orders (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID        NOT NULL,
    terminal_id UUID,
    label       VARCHAR(50),
    payload     TEXT        NOT NULL,
    held_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_pos_held_orders_store_id    ON pos_held_orders(store_id);
CREATE INDEX idx_pos_held_orders_terminal_id ON pos_held_orders(terminal_id);
