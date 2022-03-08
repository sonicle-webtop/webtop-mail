@DataSource[default@com.sonicle.webtop.core]

-- ---------------------------------------------------------
-- Expand notes message_id
-- ---------------------------------------------------------
ALTER TABLE mail.notes ALTER COLUMN message_id TYPE varchar(255)