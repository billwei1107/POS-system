-- 菜單排程表 / Menu schedules (breakfast/lunch/dinner menus)
CREATE TABLE pos_prod_menu_schedules (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    category_ids_json TEXT       NOT NULL DEFAULT '[]',
    start_time      TIME        NOT NULL,
    end_time        TIME        NOT NULL,
    days_of_week    VARCHAR(20) NOT NULL DEFAULT '1234567',
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);
