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
public class Vacation extends org.jooq.impl.TableImpl<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord> {

	private static final long serialVersionUID = -2007198008;

	/**
	 * The reference instance of <code>mail.vacation</code>
	 */
	public static final com.sonicle.webtop.mail.jooq.tables.Vacation VACATION = new com.sonicle.webtop.mail.jooq.tables.Vacation();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord> getRecordType() {
		return com.sonicle.webtop.mail.jooq.tables.records.VacationRecord.class;
	}

	/**
	 * The column <code>mail.vacation.domain_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord, java.lang.String> DOMAIN_ID = createField("domain_id", org.jooq.impl.SQLDataType.VARCHAR.length(20).nullable(false), this, "");

	/**
	 * The column <code>mail.vacation.user_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord, java.lang.String> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.VARCHAR.length(100).nullable(false), this, "");

	/**
	 * The column <code>mail.vacation.active</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord, java.lang.Boolean> ACTIVE = createField("active", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>mail.vacation.message</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord, java.lang.String> MESSAGE = createField("message", org.jooq.impl.SQLDataType.VARCHAR.length(4000), this, "");

	/**
	 * The column <code>mail.vacation.addresses</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord, java.lang.String> ADDRESSES = createField("addresses", org.jooq.impl.SQLDataType.VARCHAR.length(4000).nullable(false), this, "");

	/**
	 * Create a <code>mail.vacation</code> table reference
	 */
	public Vacation() {
		this("vacation", null);
	}

	/**
	 * Create an aliased <code>mail.vacation</code> table reference
	 */
	public Vacation(java.lang.String alias) {
		this(alias, com.sonicle.webtop.mail.jooq.tables.Vacation.VACATION);
	}

	private Vacation(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord> aliased) {
		this(alias, aliased, null);
	}

	private Vacation(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.sonicle.webtop.mail.jooq.Mail.MAIL, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord> getPrimaryKey() {
		return com.sonicle.webtop.mail.jooq.Keys.VACATION_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.VacationRecord>>asList(com.sonicle.webtop.mail.jooq.Keys.VACATION_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.sonicle.webtop.mail.jooq.tables.Vacation as(java.lang.String alias) {
		return new com.sonicle.webtop.mail.jooq.tables.Vacation(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.sonicle.webtop.mail.jooq.tables.Vacation rename(java.lang.String name) {
		return new com.sonicle.webtop.mail.jooq.tables.Vacation(name, null);
	}
}