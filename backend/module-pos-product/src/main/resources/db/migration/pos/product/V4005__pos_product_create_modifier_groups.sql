-- 客製化群組表 / Modifier groups (e.g. "Sugar Level", "Ice Level")
CREATE TABLE pos_prod_modifier_groups (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    min_select  INT         NOT NULL DEFAULT 0,
    max_select  INT         NOT NULL DEFAULT 1,
    required    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);
