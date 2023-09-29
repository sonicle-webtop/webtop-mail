@DataSource[default@com.sonicle.webtop.mail]

-- ----------------------------
-- Sequence structure for seq_in_filters
-- ----------------------------
CREATE SEQUENCE "mail"."seq_in_filters";

-- ----------------------------
-- Table structure for autoresponders
-- ----------------------------
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
-- Table structure for in_filters
-- ----------------------------
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
-- Indexes structure for table autoresponders
-- ----------------------------
CREATE INDEX "autoresponder_ak1" ON "mail"."autoresponders" USING btree ("domain_id", "user_id", "enabled");

-- ----------------------------
-- Primary Key structure for table autoresponders
-- ----------------------------
ALTER TABLE "mail"."autoresponders" ADD PRIMARY KEY ("domain_id", "user_id");

-- ----------------------------
-- Indexes structure for table in_filters
-- ----------------------------
CREATE INDEX "in_filters_ak1" ON "mail"."in_filters" USING btree ("domain_id", "user_id", "enabled", "order");

-- ----------------------------
-- Primary Key structure for table in_filters
-- ----------------------------
ALTER TABLE "mail"."in_filters" ADD PRIMARY KEY ("in_filter_id");
