@DataSource[default@com.sonicle.webtop.core]

-- ---------------------------------------------------------
-- Add new "built_in" column in filters 
-- ---------------------------------------------------------
ALTER TABLE "mail"."in_filters" ADD COLUMN "built_in" int2 NOT NULL DEFAULT 0;

DROP INDEX "mail"."in_filters_ak1";
CREATE INDEX "in_filters_ak1" ON "mail"."in_filters" ("domain_id", "user_id", "built_in", "enabled", "order");
CREATE INDEX "in_filters_ak2" ON "mail"."in_filters" ("domain_id", "user_id", "enabled", "order");
CREATE UNIQUE INDEX "in_filters_uk1" ON "mail"."in_filters" ("domain_id", "user_id", "built_in") WHERE "built_in" != 0;
