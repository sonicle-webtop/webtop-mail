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
public class Scan extends org.jooq.impl.TableImpl<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord> {

	private static final long serialVersionUID = -203760398;

	/**
	 * The reference instance of <code>mail.scan</code>
	 */
	public static final com.sonicle.webtop.mail.jooq.tables.Scan SCAN = new com.sonicle.webtop.mail.jooq.tables.Scan();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord> getRecordType() {
		return com.sonicle.webtop.mail.jooq.tables.records.ScanRecord.class;
	}

	/**
	 * The column <code>mail.scan.domain_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord, java.lang.String> DOMAIN_ID = createField("domain_id", org.jooq.impl.SQLDataType.VARCHAR.length(20).nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>mail.scan.user_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord, java.lang.String> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.VARCHAR.length(100).nullable(false), this, "");

	/**
	 * The column <code>mail.scan.foldername</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord, java.lang.String> FOLDERNAME = createField("foldername", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * Create a <code>mail.scan</code> table reference
	 */
	public Scan() {
		this("scan", null);
	}

	/**
	 * Create an aliased <code>mail.scan</code> table reference
	 */
	public Scan(java.lang.String alias) {
		this(alias, com.sonicle.webtop.mail.jooq.tables.Scan.SCAN);
	}

	private Scan(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord> aliased) {
		this(alias, aliased, null);
	}

	private Scan(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.sonicle.webtop.mail.jooq.Mail.MAIL, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord> getPrimaryKey() {
		return com.sonicle.webtop.mail.jooq.Keys.MAILSCAN_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord>>asList(com.sonicle.webtop.mail.jooq.Keys.MAILSCAN_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.sonicle.webtop.mail.jooq.tables.Scan as(java.lang.String alias) {
		return new com.sonicle.webtop.mail.jooq.tables.Scan(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.sonicle.webtop.mail.jooq.tables.Scan rename(java.lang.String name) {
		return new com.sonicle.webtop.mail.jooq.tables.Scan(name, null);
	}
}