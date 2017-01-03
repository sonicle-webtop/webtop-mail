
CREATE SCHEMA "mail";

-- ----------------------------
-- Table structure for rules
-- ----------------------------
DROP TABLE IF EXISTS "mail"."rules";
CREATE TABLE "mail"."rules" (
"domain_id" varchar(20) DEFAULT ''::character varying NOT NULL,
"user_id" varchar(100) NOT NULL,
"rule_id" numeric(10) NOT NULL,
"status" varchar(1) NOT NULL,
"continue" varchar(1) NOT NULL,
"keep_copy" varchar(1) NOT NULL,
"condition" varchar(3) NOT NULL,
"from_value" varchar(4000),
"to_value" varchar(4000),
"subject_value" varchar(4000),
"size_match" varchar(1),
"size_value" numeric(38),
"field_name" varchar(100),
"field_value" varchar(4000),
"action" varchar(10) NOT NULL,
"action_value" varchar(4000)
)
WITH (OIDS=TRUE)

;

-- ----------------------------
-- Table structure for identities
-- ----------------------------
DROP TABLE IF EXISTS "mail"."identities";
CREATE TABLE "mail"."identities" (
"user_id" varchar(100) NOT NULL,
"email" varchar(100) NOT NULL,
"display_name" varchar(100) NOT NULL,
"main_folder" varchar(100),
"mailcard_user_id" varchar(15),
"domain_id" varchar(20) DEFAULT ''::character varying NOT NULL,
"fax" bool DEFAULT false,
"use_my_personal_infos" bool DEFAULT false
)
WITH (OIDS=TRUE)

;

-- ----------------------------
-- Table structure for notes
-- ----------------------------
DROP TABLE IF EXISTS "mail"."notes";
CREATE TABLE "mail"."notes" (
"domain_id" varchar(20) NOT NULL,
"message_id" varchar(128) NOT NULL,
"text" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for scan
-- ----------------------------
DROP TABLE IF EXISTS "mail"."scan";
CREATE TABLE "mail"."scan" (
"domain_id" varchar(20) DEFAULT ''::character varying NOT NULL,
"user_id" varchar(100) NOT NULL,
"foldername" varchar(255) NOT NULL
)
WITH (OIDS=FALSE)

;

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
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Alter Sequences Owned By 
-- ----------------------------

-- ----------------------------
-- Primary Key structure for table rules
-- ----------------------------
ALTER TABLE "mail"."rules" ADD PRIMARY KEY ("domain_id", "user_id", "filter_id");

-- ----------------------------
-- Indexes structure for table identities
-- ----------------------------
CREATE INDEX "identities_index" ON "mail"."identities" USING btree ("user_id", "display_name");

-- ----------------------------
-- Primary Key structure for table identities
-- ----------------------------
ALTER TABLE "mail"."identities" ADD PRIMARY KEY ("domain_id", "user_id", "email", "display_name");

-- ----------------------------
-- Primary Key structure for table notes
-- ----------------------------
ALTER TABLE "mail"."notes" ADD PRIMARY KEY ("domain_id", "message_id");

-- ----------------------------
-- Primary Key structure for table scan
-- ----------------------------
ALTER TABLE "mail"."scan" ADD PRIMARY KEY ("domain_id", "user_id", "foldername");

-- ----------------------------
-- Primary Key structure for table users_map
-- ----------------------------
ALTER TABLE "mail"."users_map" ADD PRIMARY KEY ("domain_id", "user_id");
