@DataSource[default@com.sonicle.webtop.core]

-- ---------------------------------------------------------
-- Remove old folder audit log entries
-- ---------------------------------------------------------
DELETE FROM "core"."audit_log"
WHERE "context" = 'FOLDER';