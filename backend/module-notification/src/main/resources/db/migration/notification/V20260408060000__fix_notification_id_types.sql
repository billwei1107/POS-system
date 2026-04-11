-- 修正通知模塊所有資料表的 id 欄位型別 (VARCHAR → UUID)
-- Hibernate 6 + PostgreSQL 原生要求 UUID 型態

ALTER TABLE sys_notification_templates
    ALTER COLUMN id TYPE UUID USING id::uuid;

ALTER TABLE sys_notifications
    ALTER COLUMN id TYPE UUID USING id::uuid;

ALTER TABLE sys_notification_preferences
    ALTER COLUMN id TYPE UUID USING id::uuid;
