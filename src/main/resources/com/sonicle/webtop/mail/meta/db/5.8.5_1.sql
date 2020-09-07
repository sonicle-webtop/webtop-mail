@DataSource[default@com.sonicle.webtop.core]

-- ---------------------------------------------------------
-- Remove unwanted autosaved recipients
-- ---------------------------------------------------------
DELETE FROM "core"."servicestore_entries"
WHERE "service_id" = 'com.sonicle.webtop.core' AND "context" = 'recipients' AND "key" LIKE '%@COM.SONICLE.WEBTOP.CONTACTS%' AND "key" LIKE '%LIST-%';
