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
public class Tags extends org.jooq.impl.TableImpl<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord> {

	private static final long serialVersionUID = -1938919885;

	/**
	 * The reference instance of <code>mail.tags</code>
	 */
	public static final com.sonicle.webtop.mail.jooq.tables.Tags TAGS = new com.sonicle.webtop.mail.jooq.tables.Tags();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord> getRecordType() {
		return com.sonicle.webtop.mail.jooq.tables.records.TagsRecord.class;
	}

	/**
	 * The column <code>mail.tags.domain_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord, java.lang.String> DOMAIN_ID = createField("domain_id", org.jooq.impl.SQLDataType.VARCHAR.length(20).nullable(false), this, "");

	/**
	 * The column <code>mail.tags.user_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord, java.lang.String> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.VARCHAR.length(100).nullable(false), this, "");

	/**
	 * The column <code>mail.tags.tag_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord, java.lang.String> TAG_ID = createField("tag_id", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>mail.tags.description</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord, java.lang.String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>mail.tags.color</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord, java.lang.String> COLOR = createField("color", org.jooq.impl.SQLDataType.VARCHAR.length(20), this, "");

	/**
	 * Create a <code>mail.tags</code> table reference
	 */
	public Tags() {
		this("tags", null);
	}

	/**
	 * Create an aliased <code>mail.tags</code> table reference
	 */
	public Tags(java.lang.String alias) {
		this(alias, com.sonicle.webtop.mail.jooq.tables.Tags.TAGS);
	}

	private Tags(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord> aliased) {
		this(alias, aliased, null);
	}

	private Tags(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.sonicle.webtop.mail.jooq.Mail.MAIL, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord> getPrimaryKey() {
		return com.sonicle.webtop.mail.jooq.Keys.TAGS_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.sonicle.webtop.mail.jooq.tables.records.TagsRecord>>asList(com.sonicle.webtop.mail.jooq.Keys.TAGS_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.sonicle.webtop.mail.jooq.tables.Tags as(java.lang.String alias) {
		return new com.sonicle.webtop.mail.jooq.tables.Tags(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.sonicle.webtop.mail.jooq.tables.Tags rename(java.lang.String name) {
		return new com.sonicle.webtop.mail.jooq.tables.Tags(name, null);
	}
}