-- 套餐選項群組表 / Combo selection groups
CREATE TABLE pos_prod_combo_groups (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    combo_id    UUID        NOT NULL REFERENCES pos_prod_combos(id) ON DELETE CASCADE,
    group_name  VARCHAR(100) NOT NULL,
    min_select  INT         NOT NULL DEFAULT 1,
    max_select  INT         NOT NULL DEFAULT 1,
    sort_order  INT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pos_prod_combo_groups_combo ON pos_prod_combo_groups(combo_id);
