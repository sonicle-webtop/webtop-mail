@DataSource[default@com.sonicle.webtop.mail]

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
-- Primary Key structure for table tags
-- ----------------------------
ALTER TABLE "mail"."tags" ADD PRIMARY KEY ("domain_id", "user_id", "tag_id");
