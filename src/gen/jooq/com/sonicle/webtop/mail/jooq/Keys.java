/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.mail.jooq;

/**
 * A class modelling foreign key relationships between tables of the <code>mail</code> 
 * schema
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

	// -------------------------------------------------------------------------
	// IDENTITY definitions
	// -------------------------------------------------------------------------

	public static final org.jooq.Identity<com.sonicle.webtop.mail.jooq.tables.records.IdentitiesRecord, java.lang.Integer> IDENTITY_IDENTITIES = Identities0.IDENTITY_IDENTITIES;
	public static final org.jooq.Identity<com.sonicle.webtop.mail.jooq.tables.records.InFiltersRecord, java.lang.Integer> IDENTITY_IN_FILTERS = Identities0.IDENTITY_IN_FILTERS;

	// -------------------------------------------------------------------------
	// UNIQUE and PRIMARY KEY definitions
	// -------------------------------------------------------------------------

	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records._Rules_2eoldRecord> FILTERS_PKEY = UniqueKeys0.FILTERS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord> VACATION_PKEY = UniqueKeys0.VACATION_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.AutorespondersRecord> AUTORESPONDER_PKEY = UniqueKeys0.AUTORESPONDER_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.ExternalAccountsRecord> EXTERNAL_ACCOUNTS_PKEY = UniqueKeys0.EXTERNAL_ACCOUNTS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.IdentitiesRecord> IDENTITIES_PKEY1 = UniqueKeys0.IDENTITIES_PKEY1;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.IdentitiesOldRecord> IDENTITIES_PKEY = UniqueKeys0.IDENTITIES_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.InFiltersRecord> IN_FILTERS_PKEY = UniqueKeys0.IN_FILTERS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.NotesRecord> NOTES_PKEY = UniqueKeys0.NOTES_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.RulesCopyRecord> RULES_COPY_PKEY = UniqueKeys0.RULES_COPY_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord> MAILSCAN_PKEY = UniqueKeys0.MAILSCAN_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord> TAGS_PKEY = UniqueKeys0.TAGS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.UsersMapRecord> USERS_MAP_PKEY = UniqueKeys0.USERS_MAP_PKEY;

	// -------------------------------------------------------------------------
	// FOREIGN KEY definitions
	// -------------------------------------------------------------------------


	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class Identities0 extends org.jooq.impl.AbstractKeys {
		public static org.jooq.Identity<com.sonicle.webtop.mail.jooq.tables.records.IdentitiesRecord, java.lang.Integer> IDENTITY_IDENTITIES = createIdentity(com.sonicle.webtop.mail.jooq.tables.Identities.IDENTITIES, com.sonicle.webtop.mail.jooq.tables.Identities.IDENTITIES.IDENTITY_ID);
		public static org.jooq.Identity<com.sonicle.webtop.mail.jooq.tables.records.InFiltersRecord, java.lang.Integer> IDENTITY_IN_FILTERS = createIdentity(com.sonicle.webtop.mail.jooq.tables.InFilters.IN_FILTERS, com.sonicle.webtop.mail.jooq.tables.InFilters.IN_FILTERS.IN_FILTER_ID);
	}

	private static class UniqueKeys0 extends org.jooq.impl.AbstractKeys {
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records._Rules_2eoldRecord> FILTERS_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables._Rules_2eold._RULES_2eOLD, com.sonicle.webtop.mail.jooq.tables._Rules_2eold._RULES_2eOLD.DOMAIN_ID, com.sonicle.webtop.mail.jooq.tables._Rules_2eold._RULES_2eOLD.USER_ID, com.sonicle.webtop.mail.jooq.tables._Rules_2eold._RULES_2eOLD.RULE_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord> VACATION_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables._Vacation_2eold._VACATION_2eOLD, com.sonicle.webtop.mail.jooq.tables._Vacation_2eold._VACATION_2eOLD.DOMAIN_ID, com.sonicle.webtop.mail.jooq.tables._Vacation_2eold._VACATION_2eOLD.USER_ID, com.sonicle.webtop.mail.jooq.tables._Vacation_2eold._VACATION_2eOLD.ACTIVE);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.AutorespondersRecord> AUTORESPONDER_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.Autoresponders.AUTORESPONDERS, com.sonicle.webtop.mail.jooq.tables.Autoresponders.AUTORESPONDERS.DOMAIN_ID, com.sonicle.webtop.mail.jooq.tables.Autoresponders.AUTORESPONDERS.USER_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.ExternalAccountsRecord> EXTERNAL_ACCOUNTS_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.ExternalAccounts.EXTERNAL_ACCOUNTS, com.sonicle.webtop.mail.jooq.tables.ExternalAccounts.EXTERNAL_ACCOUNTS.EXTERNAL_ACCOUNT_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.IdentitiesRecord> IDENTITIES_PKEY1 = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.Identities.IDENTITIES, com.sonicle.webtop.mail.jooq.tables.Identities.IDENTITIES.IDENTITY_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.IdentitiesOldRecord> IDENTITIES_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.IdentitiesOld.IDENTITIES_OLD, com.sonicle.webtop.mail.jooq.tables.IdentitiesOld.IDENTITIES_OLD.DOMAIN_ID, com.sonicle.webtop.mail.jooq.tables.IdentitiesOld.IDENTITIES_OLD.USER_ID, com.sonicle.webtop.mail.jooq.tables.IdentitiesOld.IDENTITIES_OLD.EMAIL, com.sonicle.webtop.mail.jooq.tables.IdentitiesOld.IDENTITIES_OLD.DISPLAY_NAME);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.InFiltersRecord> IN_FILTERS_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.InFilters.IN_FILTERS, com.sonicle.webtop.mail.jooq.tables.InFilters.IN_FILTERS.IN_FILTER_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.NotesRecord> NOTES_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.Notes.NOTES, com.sonicle.webtop.mail.jooq.tables.Notes.NOTES.DOMAIN_ID, com.sonicle.webtop.mail.jooq.tables.Notes.NOTES.MESSAGE_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.RulesCopyRecord> RULES_COPY_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.RulesCopy.RULES_COPY, com.sonicle.webtop.mail.jooq.tables.RulesCopy.RULES_COPY.DOMAIN_ID, com.sonicle.webtop.mail.jooq.tables.RulesCopy.RULES_COPY.USER_ID, com.sonicle.webtop.mail.jooq.tables.RulesCopy.RULES_COPY.RULE_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord> MAILSCAN_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.Scan.SCAN, com.sonicle.webtop.mail.jooq.tables.Scan.SCAN.DOMAIN_ID, com.sonicle.webtop.mail.jooq.tables.Scan.SCAN.USER_ID, com.sonicle.webtop.mail.jooq.tables.Scan.SCAN.FOLDERNAME);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord> TAGS_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.Tags.TAGS, com.sonicle.webtop.mail.jooq.tables.Tags.TAGS.DOMAIN_ID, com.sonicle.webtop.mail.jooq.tables.Tags.TAGS.USER_ID, com.sonicle.webtop.mail.jooq.tables.Tags.TAGS.TAG_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.UsersMapRecord> USERS_MAP_PKEY = createUniqueKey(com.sonicle.webtop.mail.jooq.tables.UsersMap.USERS_MAP, com.sonicle.webtop.mail.jooq.tables.UsersMap.USERS_MAP.DOMAIN_ID, com.sonicle.webtop.mail.jooq.tables.UsersMap.USERS_MAP.USER_ID);
	}
}
