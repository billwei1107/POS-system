-- POS 快捷按鈕表 / POS quick-access buttons on terminal
CREATE TABLE pos_prod_quick_buttons (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID        NOT NULL,
    terminal_id UUID,
    position    INT         NOT NULL,
    item_id     UUID        REFERENCES pos_prod_items(id) ON DELETE SET NULL,
    label       VARCHAR(50) NOT NULL,
    color       VARCHAR(20),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, terminal_id, position)
);

CREATE INDEX idx_pos_prod_quick_buttons_store    ON pos_prod_quick_buttons(store_id);
CREATE INDEX idx_pos_prod_quick_buttons_terminal ON pos_prod_quick_buttons(terminal_id) WHERE terminal_id IS NOT NULL;
