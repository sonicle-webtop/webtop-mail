@DataSource[default@com.sonicle.webtop.mail]

-- ----------------------------
-- Deprecate rules table
-- ----------------------------
@IgnoreErrors
ALTER TABLE "mail"."rules" RENAME TO "_rules.old";

-- ----------------------------
-- Deprecate vacation table
-- ----------------------------
@IgnoreErrors
ALTER TABLE "mail"."vacation" RENAME TO "_vacation.old";

-- ----------------------------
-- Support new identity management
-- ----------------------------
ALTER TABLE "mail"."identities" ADD COLUMN "identity_uid" varchar(36);

-- ----------------------------
-- Support new archiving feature
-- ----------------------------
@DataSource[default@com.sonicle.webtop.core]
INSERT INTO "core"."settings" ("service_id", "key", "value") VALUES ('com.sonicle.webtop.mail', 'default.folder.archive', 'Archive');
