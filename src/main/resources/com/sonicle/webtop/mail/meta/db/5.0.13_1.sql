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
