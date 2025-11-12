@DataSource[default@com.sonicle.webtop.core]

-- ---------------------------------------------------------
-- Remove unused setting
-- ---------------------------------------------------------
DELETE FROM "core"."settings" WHERE "service_id" = 'com.sonicle.webtop.mail' AND "key" = 'default.grid.today.color';
DELETE FROM "core"."domain_settings" WHERE "service_id" = 'com.sonicle.webtop.mail' AND "key" = 'default.grid.today.color';
DELETE FROM "core"."user_settings" WHERE "service_id" = 'com.sonicle.webtop.mail' AND "key" = 'grid.today.color';
