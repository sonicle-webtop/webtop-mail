@DataSource[default@com.sonicle.webtop.mail]

-- ----------------------------
-- Data move: vacation -> autoresponders
-- ----------------------------

INSERT INTO "mail"."autoresponders"
("domain_id", "user_id", "enabled", "message", "addresses")
SELECT "vacation"."domain_id", "vacation"."user_id", "vacation"."active", "vacation"."message", "vacation"."addresses"
FROM "mail"."vacation";

-- ----------------------------
-- Data move: rules -> in_filters
-- ----------------------------

INSERT INTO "mail"."in_filters"
("domain_id", "user_id", "enabled", "order", "name", "sieve_match", "sieve_rules", "sieve_actions")
SELECT "rules"."domain_id", "rules"."user_id", 
CASE WHEN ("rules"."status" = 'E') THEN TRUE ELSE FALSE END AS "enabled",
1 AS "order",
substring(
CASE
	WHEN ("rules"."action" = 'FILE') THEN 'Sposta '
	WHEN ("rules"."action" = 'FORWARD') THEN 'Inoltra '
	WHEN ("rules"."action" = 'DISCARD') THEN 'Scarta '
	ELSE 'Rifiuta '
END
|| CASE WHEN ("rules"."condition" = 'ALL') THEN 'se si verificano tutte [' ELSE 'se si verifica almeno una [' END
|| CASE WHEN NOT (("rules"."from_value" <> '') IS NOT TRUE) THEN 'Da="' || "rules"."from_value" || '" ' ELSE '' END
|| CASE WHEN NOT (("rules"."to_value" <> '') IS NOT TRUE) THEN 'A="' || "rules"."to_value" || '" ' ELSE '' END
|| CASE WHEN NOT (("rules"."subject_value" <> '') IS NOT TRUE) THEN 'Oggetto="' || "rules"."subject_value" || '" ' ELSE '' END
|| ']'
, 1, 255) AS "name",
CASE WHEN ("rules"."condition" = 'ALL') THEN 'all' ELSE 'any' END AS "sieve_match",
'['
|| array_to_string(ARRAY[
CASE WHEN NOT (("rules"."from_value" <> '') IS NOT TRUE) THEN '{"field":"from","argument":null,"operator":"contains","value":"' || replace("rules"."from_value", '"', chr(92)||'"') || '"}' ELSE NULL END,
CASE WHEN NOT (("rules"."to_value" <> '') IS NOT TRUE) THEN '{"field":"to_cc","argument":null,"operator":"contains","value":"' || replace("rules"."to_value", '"', chr(92)||'"') || '"}' ELSE NULL END,
CASE WHEN NOT (("rules"."subject_value" <> '') IS NOT TRUE) THEN '{"field":"subject","argument":null,"operator":"contains","value":"' || replace("rules"."subject_value", '"', chr(92)||'"') || '"}' ELSE NULL END
], ',')
|| ']' AS "sieve_rules",
'['
|| CASE
	WHEN ("rules"."action" = 'FILE') THEN '{"method":"fileinto","argument":"' || replace("rules"."action_value", '"', chr(92)||'"') || '"}'
	WHEN ("rules"."action" = 'FORWARD') THEN '{"method":"redirect","argument":"' || replace("rules"."action_value", '"', chr(92)||'"') || '"}'
	WHEN ("rules"."action" = 'DISCARD') THEN '{"method":"discard","argument":""}'
	ELSE '{"method":"reject","argument":""}'
END
|| ',{"method":"stop","argument":""}]' AS "sieve_actions"
FROM "mail"."rules"
ORDER BY "rules"."domain_id", "rules"."user_id", "rules"."rule_id";
