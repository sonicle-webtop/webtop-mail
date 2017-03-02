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
package com.sonicle.webtop.mail;

import com.sonicle.commons.PathUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.model.IncomingShareRoot;
import com.sonicle.webtop.core.model.SharePermsFolder;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.UserProfile.Data;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.mail.bol.OIdentity;
import com.sonicle.webtop.mail.bol.model.Identity;
import com.sonicle.webtop.mail.dal.IdentityDAO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

/**
 *
 * @author gabriele.bulfon
 */
public class MailManager extends BaseManager {

	public static final Logger logger = WT.getLogger(MailManager.class);
	public static final String IDENTITY_SHARING_GROUPNAME = "IDENTITY";
	public static final String IDENTITY_SHARING_ID = "0";
	
	
	List<Identity> identities=null;
	
	public MailManager(boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
	}
	
	public List<Identity> listIdentities() throws WTException {
		if (identities==null)
			identities=buildIdentities();
		
		return identities;
	}
    
    public Identity getMainIdentity() {
		if (identities==null) {
            try {
                identities=buildIdentities();
            } catch(WTException exc) {}
        }
        return identities.get(0);
    }
	
	public void addIdentity(Identity ident) throws WTException {
		Connection con=null;
		try {
			UserProfileId pid=getTargetProfileId();
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			OIdentity oident=new OIdentity();
			oident.setDisplayName(ident.getDisplayName());
			oident.setDomainId(pid.getDomainId());
			oident.setEmail(ident.getEmail());
			oident.setFax(ident.isFax());
			oident.setMainFolder(ident.getMainFolder());
			oident.setUserId(pid.getUserId());
			idao.insert(con, oident);
			identities.add(ident);
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteIdentity(Identity ident) throws WTException {
		Connection con=null;
		try {
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			idao.deleteById(con, ident.getIdentityId());
			Identity dident=findIdentity(ident.getIdentityId());
			identities.remove(dident);
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateIdentity(int identityId, Identity ident) throws WTException {
		Connection con=null;
		try {
			UserProfileId pid=getTargetProfileId();
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			OIdentity oident=new OIdentity();
			oident.setDisplayName(ident.getDisplayName());
			oident.setDomainId(pid.getDomainId());
			oident.setEmail(ident.getEmail());
			oident.setFax(ident.isFax());
			oident.setMainFolder(ident.getMainFolder());
			oident.setUserId(pid.getUserId());
			idao.update(con, identityId, oident);
			Identity uident=findIdentity(ident.getIdentityId());
			uident.setDisplayName(ident.getDisplayName());
			uident.setEmail(ident.getEmail());
			uident.setFax(ident.isFax());
			uident.setMainFolder(ident.getMainFolder());
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Identity findIdentity(int id) {
		for(Identity ident: identities) {
			if (ident.getIdentityId()==id) 
				return ident;
		}
		return null;
	}
	
	private List<Identity> buildIdentities() throws WTException {
		Connection con=null;
		List<Identity> idents=new ArrayList();
		try {
			UserProfileId pid=getTargetProfileId();
			//first add main identity
			Data udata=WT.getUserData(pid);
			Identity id=new Identity(0,udata.getDisplayName(),udata.getEmail().getAddress(),null);
			id.setIsMainIdentity(true);
			idents.add(id);
			
			//add configured additional identities
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			List<OIdentity> items=idao.selectByDomainUser(con, pid.getDomainId(),pid.getUserId());
			for(OIdentity oi: items) {
				idents.add(new Identity(oi));
			}
			
			//add automatic shared identities
			CoreManager core=WT.getCoreManager(getTargetProfileId());
			for(IncomingShareRoot share: core.listIncomingShareRoots(SERVICE_ID, IDENTITY_SHARING_GROUPNAME)) {
				UserProfileId opid=share.getOriginPid();
				udata=WT.getUserData(opid);
				List<OShare> folders=core.listIncomingShareFolders(share.getShareId(), IDENTITY_SHARING_GROUPNAME);
				if (folders!=null && folders.size()>0) {
					OShare folder=folders.get(0);
					SharePermsFolder spf=core.getShareFolderPermissions(folder.getShareId().toString());
					boolean shareIdentity=spf.implies("READ");
					boolean forceMailcard=spf.implies("UPDATE");
					if (shareIdentity) {
						id = new Identity(Identity.TYPE_AUTO,-1,udata.getDisplayName(),udata.getEmail().getAddress(),null,false,forceMailcard);
						id.setOriginPid(opid);
						idents.add(id);
					}
				}
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return idents;
	}
	
	public Mailcard getMailcard() {
		UserProfileId pid=getTargetProfileId();
		Data udata=WT.getUserData(pid);
		String domainId=pid.getDomainId();
		String emailAddress=udata.getEmail().getAddress();
		Mailcard mc = readEmailMailcard(domainId,emailAddress);
		if(mc != null) return mc;
		mc = readUserMailcard(domainId,pid.getUserId());
		if(mc != null) return mc;
		mc = readEmailDomainMailcard(domainId,emailAddress);
		if(mc != null) return mc;
		return readDefaultMailcard(domainId);
    }
	
	public Mailcard getMailcard(UserProfileId pid) {
		Data udata=WT.getUserData(pid);
		String domainId=pid.getDomainId();
		String emailAddress=udata.getEmail().getAddress();
		Mailcard mc = readEmailMailcard(domainId,emailAddress);
		if(mc != null) return mc;
		mc = readUserMailcard(domainId,pid.getUserId());
		if(mc != null) return mc;
		mc = readEmailDomainMailcard(domainId,emailAddress);
		if(mc != null) return mc;
		return readDefaultMailcard(domainId);
    }
	
	public Mailcard getMailcard(Identity identity) {
        UserProfileId pid=getTargetProfileId();
		Mailcard mc = readEmailMailcard(pid.getDomainId(),identity.getEmail());
		if(mc != null) return mc;
		UserProfileId fpid=identity.getOriginPid();
		if (fpid!=null) mc = readUserMailcard(fpid.getDomainId(),fpid.getUserId());
		if(mc != null) return mc;
		mc = readEmailDomainMailcard(pid.getDomainId(),identity.getEmail());
		if(mc != null) return mc;
		return readDefaultMailcard(pid.getDomainId());
    }
	
//	public Mailcard getMailcard() {
//		return readDefaultMailcard();
//    }
	
	public Mailcard getMailcard(String domainId, String emailAddress) {
		Mailcard mc = readEmailMailcard(domainId, emailAddress);
		if (mc != null) {
			return mc;
		}
		mc = readEmailDomainMailcard(domainId, emailAddress);
		if (mc != null) {
			return mc;
		}
		return readDefaultMailcard(domainId);
	}
	
	public Mailcard getEmailDomainMailcard(String domainId, String emailAddress) {
		Mailcard mc = readEmailDomainMailcard(domainId, emailAddress);
		if (mc != null) {
			return mc;
		}
		return getMailcard();
	}

	private Mailcard readEmailMailcard(String domainId, String email) {
		String mailcard = readMailcard(domainId, "mailcard_" + email);
		if (mailcard != null) {
			return new Mailcard(Mailcard.TYPE_EMAIL, mailcard);
		}
		return null;
	}

	private Mailcard readEmailDomainMailcard(String domainId, String email) {
		int index = email.indexOf("@");
		if (index < 0) {
			return null;
		}
		String mailcard = readMailcard(domainId, "mailcard_" + email.substring(index + 1));
		if (mailcard != null) {
			return new Mailcard(Mailcard.TYPE_EMAIL_DOMAIN, mailcard);
		}
		return null;
	}

	private Mailcard readUserMailcard(String domainId, String user) {
		String mailcard = readMailcard(domainId, "mailcard_" + user);
		if (mailcard != null) {
			return new Mailcard(Mailcard.TYPE_USER, mailcard);
		}
		return null;
	}

	private Mailcard readDefaultMailcard(String domainId) {
		String mailcard = readMailcard(domainId, "mailcard");
		if (mailcard != null) {
			return new Mailcard(Mailcard.TYPE_DEFAULT, mailcard);
		}
		return new Mailcard();
	}
	
	public void setEmailMailcard(Identity ident, String html) {
		setEmailMailcard(getTargetProfileId().getDomainId(),ident.getEmail(),html);
	}

	public void setEmailMailcard(String domainId, String email, String html) {
		writeMailcard(domainId, "mailcard_" + email, html);
	}

	public void setEmailDomainMailcard(String domainId, String email, String html) {
		int index = email.indexOf("@");
		if (index < 0) {
			return;
		}
		writeMailcard(domainId, "mailcard_" + email.substring(index + 1), html);
	}

	public void setUserMailcard(String domainId, String user, String html) {
		writeMailcard(domainId, "mailcard_" + user, html);
	}

	private void writeMailcard(String domainId, String filename, String html) {
		String pathname = MessageFormat.format("{0}/{1}.html", getModelPath(domainId), filename);

		try {
			File file = new File(pathname);
			if (html != null) {
				FileUtils.write(file, html, "ISO-8859-15");
			} else {
				FileUtils.forceDelete(file);
			}

		} catch (FileNotFoundException ex) {
			logger.trace("Cleaning not necessary. Mailcard file not found. [{}]", pathname, ex);
		} catch (IOException ex) {
			logger.error("Unable to write/delete mailcard file. [{}]", pathname, ex);
		}
	}

	private String readMailcard(String domainId, String filename) {
		String pathname = MessageFormat.format("{0}/{1}.html", getModelPath(domainId), filename);

		try {
			File file = new File(pathname);
			return FileUtils.readFileToString(file, "ISO-8859-15");

		} catch (FileNotFoundException ex) {
			logger.trace("Mailcard file not found. [{}]", pathname);
			return null;
		} catch (IOException ex) {
			logger.error("Unable to read mailcard file. [{}]", pathname, ex);
		}
		return null;
	}
	
	public String getModelPath(String domainId) {
		String path=PathUtils.concatPathParts(WT.getServiceHomePath(domainId, SERVICE_ID),"models");
		return path;
	}

}
