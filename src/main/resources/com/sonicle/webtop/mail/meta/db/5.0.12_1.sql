@DataSource[default@com.sonicle.webtop.core]

delete from core.user_settings where service_id='com.sonicle.webtop.mail' and "key"='reply.to';
