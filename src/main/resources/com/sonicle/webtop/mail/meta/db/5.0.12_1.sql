@DataSource[default@com.sonicle.webtop.core]

DELETE FROM "core"."user_settings" WHERE "service_id" = 'com.sonicle.webtop.mail' AND "key" = 'reply.to';
