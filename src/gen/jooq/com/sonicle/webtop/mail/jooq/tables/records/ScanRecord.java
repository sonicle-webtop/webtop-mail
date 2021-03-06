/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.mail.jooq.tables.records;

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
public class ScanRecord extends org.jooq.impl.UpdatableRecordImpl<com.sonicle.webtop.mail.jooq.tables.records.ScanRecord> implements org.jooq.Record3<java.lang.String, java.lang.String, java.lang.String> {

	private static final long serialVersionUID = -825655246;

	/**
	 * Setter for <code>mail.scan.domain_id</code>.
	 */
	public void setDomainId(java.lang.String value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>mail.scan.domain_id</code>.
	 */
	public java.lang.String getDomainId() {
		return (java.lang.String) getValue(0);
	}

	/**
	 * Setter for <code>mail.scan.user_id</code>.
	 */
	public void setUserId(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>mail.scan.user_id</code>.
	 */
	public java.lang.String getUserId() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>mail.scan.foldername</code>.
	 */
	public void setFoldername(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>mail.scan.foldername</code>.
	 */
	public java.lang.String getFoldername() {
		return (java.lang.String) getValue(2);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record3<java.lang.String, java.lang.String, java.lang.String> key() {
		return (org.jooq.Record3) super.key();
	}

	// -------------------------------------------------------------------------
	// Record3 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.String, java.lang.String, java.lang.String> fieldsRow() {
		return (org.jooq.Row3) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.String, java.lang.String, java.lang.String> valuesRow() {
		return (org.jooq.Row3) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field1() {
		return com.sonicle.webtop.mail.jooq.tables.Scan.SCAN.DOMAIN_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return com.sonicle.webtop.mail.jooq.tables.Scan.SCAN.USER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return com.sonicle.webtop.mail.jooq.tables.Scan.SCAN.FOLDERNAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value1() {
		return getDomainId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value2() {
		return getUserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getFoldername();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScanRecord value1(java.lang.String value) {
		setDomainId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScanRecord value2(java.lang.String value) {
		setUserId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScanRecord value3(java.lang.String value) {
		setFoldername(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScanRecord values(java.lang.String value1, java.lang.String value2, java.lang.String value3) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached ScanRecord
	 */
	public ScanRecord() {
		super(com.sonicle.webtop.mail.jooq.tables.Scan.SCAN);
	}

	/**
	 * Create a detached, initialised ScanRecord
	 */
	public ScanRecord(java.lang.String domainId, java.lang.String userId, java.lang.String foldername) {
		super(com.sonicle.webtop.mail.jooq.tables.Scan.SCAN);

		setValue(0, domainId);
		setValue(1, userId);
		setValue(2, foldername);
	}
}
