/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.mail.jooq;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Mail extends org.jooq.impl.SchemaImpl {

	private static final long serialVersionUID = -1289384962;

	/**
	 * The reference instance of <code>mail</code>
	 */
	public static final Mail MAIL = new Mail();

	/**
	 * No further instances allowed
	 */
	private Mail() {
		super("mail");
	}

	@Override
	public final java.util.List<org.jooq.Sequence<?>> getSequences() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getSequences0());
		return result;
	}

	private final java.util.List<org.jooq.Sequence<?>> getSequences0() {
		return java.util.Arrays.<org.jooq.Sequence<?>>asList(
			com.sonicle.webtop.mail.jooq.Sequences.SEQ_EXTERNAL_ACCOUNTS,
			com.sonicle.webtop.mail.jooq.Sequences.SEQ_IDENTITIES,
			com.sonicle.webtop.mail.jooq.Sequences.SEQ_IN_FILTERS);
	}

	@Override
	public final java.util.List<org.jooq.Table<?>> getTables() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final java.util.List<org.jooq.Table<?>> getTables0() {
		return java.util.Arrays.<org.jooq.Table<?>>asList(
			com.sonicle.webtop.mail.jooq.tables._Rules_2eold._RULES_2eOLD,
			com.sonicle.webtop.mail.jooq.tables._Vacation_2eold._VACATION_2eOLD,
			com.sonicle.webtop.mail.jooq.tables.Autoresponders.AUTORESPONDERS,
			com.sonicle.webtop.mail.jooq.tables.ExternalAccounts.EXTERNAL_ACCOUNTS,
			com.sonicle.webtop.mail.jooq.tables.Identities.IDENTITIES,
			com.sonicle.webtop.mail.jooq.tables.IdentitiesOld.IDENTITIES_OLD,
			com.sonicle.webtop.mail.jooq.tables.InFilters.IN_FILTERS,
			com.sonicle.webtop.mail.jooq.tables.Notes.NOTES,
			com.sonicle.webtop.mail.jooq.tables.RulesCopy.RULES_COPY,
			com.sonicle.webtop.mail.jooq.tables.Scan.SCAN,
			com.sonicle.webtop.mail.jooq.tables.Tags.TAGS,
			com.sonicle.webtop.mail.jooq.tables.UsersMap.USERS_MAP);
	}
}
