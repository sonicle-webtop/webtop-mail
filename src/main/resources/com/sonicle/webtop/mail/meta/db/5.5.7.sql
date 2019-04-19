@DataSource[default@com.sonicle.webtop.mail]

-- ----------------------------
-- Table structure for external_accounts
-- ----------------------------
DROP TABLE IF EXISTS "mail"."external_accounts";
CREATE TABLE "mail"."external_accounts" (
"external_account_id" SERIAL PRIMARY KEY,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"display_name" varchar(255),
"description" varchar(255),
"email" varchar(255),
"protocol" varchar(255),
"host" varchar(255),
"port" integer,
"username" varchar(255),
"password" varchar(255),
"folder_prefix" varchar(255),
"folder_sent" varchar(255),
"folder_drafts" varchar(255),
"folder_trash" varchar(255),
"folder_spam" varchar(255),
"folder_archive" varchar(255)
)
WITH (OIDS=FALSE)

;

-- ---------------------------------------------------------
-- Add readonly_provider column in table external_accounts
-- ---------------------------------------------------------

ALTER TABLE mail.external_accounts
ADD COLUMN readonly_provider BOOLEAN;