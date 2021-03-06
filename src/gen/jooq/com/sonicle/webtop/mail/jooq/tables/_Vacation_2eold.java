/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.mail.jooq.tables;

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
public class _Vacation_2eold extends org.jooq.impl.TableImpl<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord> {

	private static final long serialVersionUID = 1879636728;

	/**
	 * The reference instance of <code>mail._vacation.old</code>
	 */
	public static final com.sonicle.webtop.mail.jooq.tables._Vacation_2eold _VACATION_2eOLD = new com.sonicle.webtop.mail.jooq.tables._Vacation_2eold();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord> getRecordType() {
		return com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord.class;
	}

	/**
	 * The column <code>mail._vacation.old.domain_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord, java.lang.String> DOMAIN_ID = createField("domain_id", org.jooq.impl.SQLDataType.VARCHAR.length(20).nullable(false), this, "");

	/**
	 * The column <code>mail._vacation.old.user_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord, java.lang.String> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.VARCHAR.length(100).nullable(false), this, "");

	/**
	 * The column <code>mail._vacation.old.active</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord, java.lang.Boolean> ACTIVE = createField("active", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>mail._vacation.old.message</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord, java.lang.String> MESSAGE = createField("message", org.jooq.impl.SQLDataType.VARCHAR.length(4000), this, "");

	/**
	 * The column <code>mail._vacation.old.addresses</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord, java.lang.String> ADDRESSES = createField("addresses", org.jooq.impl.SQLDataType.VARCHAR.length(4000).nullable(false), this, "");

	/**
	 * Create a <code>mail._vacation.old</code> table reference
	 */
	public _Vacation_2eold() {
		this("_vacation.old", null);
	}

	/**
	 * Create an aliased <code>mail._vacation.old</code> table reference
	 */
	public _Vacation_2eold(java.lang.String alias) {
		this(alias, com.sonicle.webtop.mail.jooq.tables._Vacation_2eold._VACATION_2eOLD);
	}

	private _Vacation_2eold(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord> aliased) {
		this(alias, aliased, null);
	}

	private _Vacation_2eold(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.sonicle.webtop.mail.jooq.Mail.MAIL, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord> getPrimaryKey() {
		return com.sonicle.webtop.mail.jooq.Keys.VACATION_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records._Vacation_2eoldRecord>>asList(com.sonicle.webtop.mail.jooq.Keys.VACATION_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.sonicle.webtop.mail.jooq.tables._Vacation_2eold as(java.lang.String alias) {
		return new com.sonicle.webtop.mail.jooq.tables._Vacation_2eold(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.sonicle.webtop.mail.jooq.tables._Vacation_2eold rename(java.lang.String name) {
		return new com.sonicle.webtop.mail.jooq.tables._Vacation_2eold(name, null);
	}
}
