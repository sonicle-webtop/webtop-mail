/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.mail.jooq.tables.pojos;

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
public class _Vacation_2eold implements java.io.Serializable {

	private static final long serialVersionUID = 590138052;

	private java.lang.String  domainId;
	private java.lang.String  userId;
	private java.lang.Boolean active;
	private java.lang.String  message;
	private java.lang.String  addresses;

	public _Vacation_2eold() {}

	public _Vacation_2eold(
		java.lang.String  domainId,
		java.lang.String  userId,
		java.lang.Boolean active,
		java.lang.String  message,
		java.lang.String  addresses
	) {
		this.domainId = domainId;
		this.userId = userId;
		this.active = active;
		this.message = message;
		this.addresses = addresses;
	}

	public java.lang.String getDomainId() {
		return this.domainId;
	}

	public void setDomainId(java.lang.String domainId) {
		this.domainId = domainId;
	}

	public java.lang.String getUserId() {
		return this.userId;
	}

	public void setUserId(java.lang.String userId) {
		this.userId = userId;
	}

	public java.lang.Boolean getActive() {
		return this.active;
	}

	public void setActive(java.lang.Boolean active) {
		this.active = active;
	}

	public java.lang.String getMessage() {
		return this.message;
	}

	public void setMessage(java.lang.String message) {
		this.message = message;
	}

	public java.lang.String getAddresses() {
		return this.addresses;
	}

	public void setAddresses(java.lang.String addresses) {
		this.addresses = addresses;
	}
}
