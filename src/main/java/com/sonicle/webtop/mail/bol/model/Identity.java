/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
package com.sonicle.webtop.mail.bol.model;

import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.mail.MailAccount;
import com.sonicle.webtop.mail.Mailcard;
import com.sonicle.webtop.mail.bol.OIdentity;

/**
 *
 * @author gabriele.bulfon
 */
public class Identity {
	
	public static final String TYPE_AUTO = "auto";
	public static final String TYPE_USER = "user";

    protected int identityId;
    protected String identityUid=null;
	protected String type = null;
	protected String email;
	protected String displayName;
	protected String mainFolder;
	protected Mailcard mailcard;
	protected UserProfileId originPid;

	protected boolean fax;
	protected boolean forceMailcard;
	protected boolean lockMailcard = false;
	
	protected boolean isMainIdentity=false;
	
	public Identity(OIdentity oi) {
        this(Identity.TYPE_USER, oi.getIdentityId(), oi.getIdentityUid(), oi.getDisplayName(), oi.getEmail(), oi.getMainFolder(), oi.getFax(), false);
	}

	public Identity(int identityId, String identityUid, String displayName, String email, String mainFolder) {
		this(Identity.TYPE_USER, identityId, identityUid, displayName, email, mainFolder);
	}
	
	public Identity(String type, int identityId, String identityUid, String displayName, String email, String mainFolder) {
		this(type,identityId,identityUid,displayName,email,mainFolder,false,false);
	}
	
	public Identity(String type, int identityId, String identityUid, String displayName, String email, String mainFolder, boolean fax, boolean forceMailcard) {
		this.type = type;
		this.identityId = identityId;
		this.identityUid = identityUid;
		this.displayName=displayName;
		this.email=email;
		this.mainFolder=mainFolder;
		this.fax=fax;
		this.forceMailcard=forceMailcard;
	}
    
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isLockMailcard() {
		return lockMailcard;
	}

	public void setLockMailcard(boolean lockMailcard) {
		this.lockMailcard = lockMailcard;
	}
	
	public boolean isMainIdentity() {
		return isMainIdentity;
	}

	public void setIsMainIdentity(boolean isMainIdentity) {
		this.isMainIdentity = isMainIdentity;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMainFolder() {
		return mainFolder;
	}

	public void setMainFolder(String mainFolder) {
		this.mainFolder = mainFolder;
	}

	public Mailcard getMailcard() {
		return mailcard;
	}

	public void setMailcard(Mailcard mailcard) {
		this.mailcard = mailcard;
	}
	
	public UserProfileId getOriginPid() {
		return originPid;
	}

	public void setOriginPid(UserProfileId originPid) {
		this.originPid = originPid;
	}

	public boolean isFax() {
		return fax;
	}

	public void setFax(boolean fax) {
		this.fax = fax;
	}

	public boolean isForceMailcard() {
		return forceMailcard;
	}

	public void setForceMailcard(boolean forceMailcard) {
		this.forceMailcard = forceMailcard;
	}
	
	public boolean isType(String type) {
		return this.type.equals(type);
	}
    
    public int getIdentityId() {
        return identityId;
    }
	
	public void setIdentityId(int id) {
		identityId=id;
	}
	
    public String getIdentityUid() {
        return identityUid;
    }
	
	public void setIdentityUid(String uid) {
		identityUid=uid;
	}
	
	public MailAccount getAccount() {
		return null;
	}
	
/*	public void setAccount(MailAccount account) {
		this.account=account;
	}*/
	
    @Override
	public String toString() {
		return displayName + " <" + email + ">";
	}

}
