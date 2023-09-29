@DataSource[default@com.sonicle.webtop.mail]

CREATE SCHEMA "mail";

-- ----------------------------
-- Sequence structure for seq_identities
-- ----------------------------
DROP SEQUENCE IF EXISTS "mail"."seq_identities";
CREATE SEQUENCE "mail"."seq_identities";

-- ----------------------------
-- Sequence structure for seq_in_filters
-- ----------------------------
DROP SEQUENCE IF EXISTS "mail"."seq_in_filters";
CREATE SEQUENCE "mail"."seq_in_filters";

-- ----------------------------
-- Table structure for autoresponders
-- ----------------------------
DROP TABLE IF EXISTS "mail"."autoresponders";
CREATE TABLE "mail"."autoresponders" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"enabled" bool DEFAULT false NOT NULL,
"subject" varchar(255),
"message" varchar(4000),
"addresses" varchar(4000),
"days_interval" int2 DEFAULT 7 NOT NULL,
"start_date" timestamptz(6),
"end_date" timestamptz(6),
"skip_mailing_lists" bool DEFAULT false NOT NULL
);

-- ----------------------------
-- Table structure for identities
-- ----------------------------
DROP TABLE IF EXISTS "mail"."identities";
CREATE TABLE "mail"."identities" (
"identity_id" int4 DEFAULT nextval('"mail".seq_identities'::regclass) NOT NULL,
"domain_id" varchar(20) DEFAULT ''::character varying NOT NULL,
"user_id" varchar(100) NOT NULL,
"email" varchar(100) NOT NULL,
"display_name" varchar(100) NOT NULL,
"main_folder" varchar(100),
"fax" bool DEFAULT false,
"identity_uid" varchar(36)
);

-- ----------------------------
-- Table structure for in_filters
-- ----------------------------
DROP TABLE IF EXISTS "mail"."in_filters";
CREATE TABLE "mail"."in_filters" (
"in_filter_id" int4 DEFAULT nextval('"mail".seq_in_filters'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"enabled" bool DEFAULT false NOT NULL,
"order" int2 NOT NULL,
"name" varchar(255) NOT NULL,
"sieve_match" varchar(10) NOT NULL,
"sieve_rules" text NOT NULL,
"sieve_actions" text NOT NULL
);

-- ----------------------------
-- Table structure for notes
-- ----------------------------
DROP TABLE IF EXISTS "mail"."notes";
CREATE TABLE "mail"."notes" (
"domain_id" varchar(20) NOT NULL,
"message_id" varchar(128) NOT NULL,
"text" text
);

-- ----------------------------
-- Table structure for scan
-- ----------------------------
DROP TABLE IF EXISTS "mail"."scan";
CREATE TABLE "mail"."scan" (
"domain_id" varchar(20) DEFAULT ''::character varying NOT NULL,
"user_id" varchar(100) NOT NULL,
"foldername" varchar(255) NOT NULL
);

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS "mail"."tags";
CREATE TABLE "mail"."tags" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"tag_id" varchar(255) NOT NULL,
"description" varchar(255),
"color" varchar(20)
);

-- ----------------------------
-- Table structure for users_map
-- ----------------------------
DROP TABLE IF EXISTS "mail"."users_map";
CREATE TABLE "mail"."users_map" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"mail_user" varchar(100) NOT NULL,
"mail_password" varchar(128),
"mail_host" varchar(100),
"mail_port" int4,
"mail_protocol" varchar(10)
);

-- ----------------------------
-- Alter Sequences Owned By 
-- ----------------------------

-- ----------------------------
-- Indexes structure for table autoresponders
-- ----------------------------
CREATE INDEX "autoresponder_ak1" ON "mail"."autoresponders" USING btree ("domain_id", "user_id", "enabled");

-- ----------------------------
-- Primary Key structure for table autoresponders
-- ----------------------------
ALTER TABLE "mail"."autoresponders" ADD PRIMARY KEY ("domain_id", "user_id");

-- ----------------------------
-- Indexes structure for table identities
-- ----------------------------
CREATE INDEX "identities_ak1" ON "mail"."identities" USING btree ("user_id", "display_name");

-- ----------------------------
-- Primary Key structure for table identities
-- ----------------------------
ALTER TABLE "mail"."identities" ADD PRIMARY KEY ("identity_id");

-- ----------------------------
-- Indexes structure for table in_filters
-- ----------------------------
CREATE INDEX "in_filters_ak1" ON "mail"."in_filters" USING btree ("domain_id", "user_id", "enabled", "order");

-- ----------------------------
-- Primary Key structure for table in_filters
-- ----------------------------
ALTER TABLE "mail"."in_filters" ADD PRIMARY KEY ("in_filter_id");

-- ----------------------------
-- Primary Key structure for table notes
-- ----------------------------
ALTER TABLE "mail"."notes" ADD PRIMARY KEY ("domain_id", "message_id");

-- ----------------------------
-- Primary Key structure for table scan
-- ----------------------------
ALTER TABLE "mail"."scan" ADD PRIMARY KEY ("domain_id", "user_id", "foldername");

-- ----------------------------
-- Primary Key structure for table tags
-- ----------------------------
ALTER TABLE "mail"."tags" ADD PRIMARY KEY ("domain_id", "user_id", "tag_id");

-- ----------------------------
-- Primary Key structure for table users_map
-- ----------------------------
ALTER TABLE "mail"."users_map" ADD PRIMARY KEY ("domain_id", "user_id");

-- ----------------------------
-- Align service version
-- ----------------------------
@DataSource[default@com.sonicle.webtop.core]
DELETE FROM "core"."settings" WHERE ("settings"."service_id" = 'com.sonicle.webtop.mail') AND ("settings"."key" = 'manifest.version');
INSERT INTO "core"."settings" ("service_id", "key", "value") VALUES ('com.sonicle.webtop.mail', 'manifest.version', '5.0.14');
