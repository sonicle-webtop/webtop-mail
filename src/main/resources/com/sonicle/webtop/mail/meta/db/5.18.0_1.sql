@DataSource[default@com.sonicle.webtop.core]

-- ---------------------------------------------------------
-- Convert mail roles_permissions into shares_data
-- ---------------------------------------------------------
truncate table core.shares_data;

insert into core.shares_data (share_id, user_uid, value)
 select distinct 
  "instance"::int,
  role_uid,
  '{ "shareIdentity" : ' ||
	  (( select "action" from core.roles_permissions where 
	    service_id = 'com.sonicle.webtop.mail' and
	    "key" = 'IDENTITY@SHARE_FOLDER' and 
	    "instance" = rp.instance and
	    role_uid = rp.role_uid and
	    action = 'READ' ) is not null)::varchar(5) ||
  ', "forceMailcard" : ' ||
	  (( select "action" from core.roles_permissions where 
	    service_id = 'com.sonicle.webtop.mail' and
	    "key" = 'IDENTITY@SHARE_FOLDER' and 
	    "instance" = rp.instance and
	    role_uid = rp.role_uid and
	    action = 'UPDATE' ) is not null)::varchar(5) ||
  ', "alwaysCc" : ' ||
	  (( select "action" from core.roles_permissions where 
	    service_id = 'com.sonicle.webtop.mail' and
	    "key" = 'IDENTITY@SHARE_FOLDER' and 
	    "instance" = rp.instance and
	    role_uid = rp.role_uid and
	    action = 'DELETE' ) is not null)::varchar(5) ||
  ' }' jsonValue
 from core.roles_permissions rp where 
  service_id = 'com.sonicle.webtop.mail' and
  "key" = 'IDENTITY@SHARE_FOLDER' and 
  "instance" in (select share_id::varchar(255) from core.shares where service_id = 'com.sonicle.webtop.mail' and "key" = 'IDENTITY@FOLDER')

