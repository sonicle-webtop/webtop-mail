/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.mail.jooq;

/**
 * Convenience access to all sequences in mail
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Sequences {

	/**
	 * The sequence <code>mail.seq_identities</code>
	 */
	public static final org.jooq.Sequence<java.lang.Long> SEQ_IDENTITIES = new org.jooq.impl.SequenceImpl<java.lang.Long>("seq_identities", com.sonicle.webtop.mail.jooq.Mail.MAIL, org.jooq.impl.SQLDataType.BIGINT.nullable(false));
}
