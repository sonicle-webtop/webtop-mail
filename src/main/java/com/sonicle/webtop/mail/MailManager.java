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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.UserPersonalInfo;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfile.Data;
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
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

/**
 *
 * @author gabriele.bulfon
 */
public class MailManager extends BaseManager {

	public static final Logger logger = WT.getLogger(MailManager.class);
	
	List<Identity> identities=null;
	
	public MailManager() {
		this(RunContext.getProfileId());
	}
	
	public MailManager(UserProfile.Id targetProfileId) {
		super(targetProfileId);
	}
	
	public Identity createIdentity(OIdentity oi) throws WTException {
		try {
			Identity i=new Identity();
			BeanUtils.copyProperties(i,oi);
			return i;
		} catch(Exception ex) {
			throw new WTException(ex,"Error creating identity");
		}
	}
	
	public List<Identity> listIdentities(boolean includeMainEmail) throws WTException {
		if (identities==null)
			identities=buildIdentities();
		
		List<Identity> idents=identities;
					
		if (includeMainEmail) {
			idents=new ArrayList();
			idents.addAll(identities);
			UserProfile.Id pid=getTargetProfileId();
			Data udata=WT.getUserData(pid);
			Identity id=new Identity(udata.getEmail().getAddress(),udata.getDisplayName(),null);
			idents.add(id);
		}
	
		return idents;
	}
	
	private List<Identity> buildIdentities() throws WTException {
		Connection con=null;
		List<Identity> idents=new ArrayList();
		try {
			UserProfile.Id pid=getTargetProfileId();
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			List<OIdentity> items=idao.selectById(con, pid.getDomainId(),pid.getUserId());
			for(OIdentity oi: items) {
				Identity id=createIdentity(oi);
				idents.add(id);
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return idents;
	}
	
	public Mailcard getMailcard() {
		UserProfile.Id pid=getTargetProfileId();
		Data udata=WT.getUserData(pid);
		String email=udata.getEmail().getAddress();
		Mailcard mc = readEmailMailcard(email);
		if(mc != null) return mc;
		mc = readUserMailcard(pid.getUserId());
		if(mc != null) return mc;
		mc = readEmailDomainMailcard(email);
		if(mc != null) return mc;
		return readDefaultMailcard();
    }
	
	public Mailcard getMailcard(Identity identity) {
		Mailcard mc = readEmailMailcard(identity.getEmail());
		if(mc != null) return mc;
		mc = readUserMailcard(identity.getFromUserProfileId().getUserId());
		if(mc != null) return mc;
		mc = readEmailDomainMailcard(identity.getEmail());
		if(mc != null) return mc;
		return readDefaultMailcard();
    }
	
//	public Mailcard getMailcard() {
//		return readDefaultMailcard();
//    }
	
	public Mailcard getMailcard(String emailAddress) {
		Mailcard mc = readEmailMailcard(emailAddress);
		if (mc != null) {
			return mc;
		}
		mc = readEmailDomainMailcard(emailAddress);
		if (mc != null) {
			return mc;
		}
		return readDefaultMailcard();
	}
	
	public Mailcard getEmailDomainMailcard(UserProfile profile) {
		Mailcard mc = readEmailDomainMailcard(profile.getEmailAddress());
		if (mc != null) {
			return mc;
		}
		return getMailcard();
	}

	private Mailcard readEmailMailcard(String email) {
		String mailcard = readMailcard("mailcard_" + email);
		if (mailcard != null) {
			return new Mailcard(Mailcard.TYPE_EMAIL, mailcard);
		}
		return null;
	}

	private Mailcard readEmailDomainMailcard(String email) {
		int index = email.indexOf("@");
		if (index < 0) {
			return null;
		}
		String mailcard = readMailcard("mailcard_" + email.substring(index + 1));
		if (mailcard != null) {
			return new Mailcard(Mailcard.TYPE_EMAIL_DOMAIN, mailcard);
		}
		return null;
	}

	private Mailcard readUserMailcard(String user) {
		String mailcard = readMailcard("mailcard_" + user);
		if (mailcard != null) {
			return new Mailcard(Mailcard.TYPE_USER, mailcard);
		}
		return null;
	}

	private Mailcard readDefaultMailcard() {
		String mailcard = readMailcard("mailcard");
		if (mailcard != null) {
			return new Mailcard(Mailcard.TYPE_DEFAULT, mailcard);
		}
		return new Mailcard();
	}

	public void setEmailMailcard(String email, String html) {
		writeMailcard("mailcard_" + email, html);
	}

	public void setEmailDomainMailcard(String email, String html) {
		int index = email.indexOf("@");
		if (index < 0) {
			return;
		}
		writeMailcard("mailcard_" + email.substring(index + 1), html);
	}

	public void setUserMailcard(String user, String html) {
		writeMailcard("mailcard_" + user, html);
	}

	private void writeMailcard(String filename, String html) {
		String pathname = MessageFormat.format("{0}/{1}.html", getModelPath(), filename);

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

	private String readMailcard(String filename) {
		String pathname = MessageFormat.format("{0}/{1}.html", getModelPath(), filename);

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
	
	public String getModelPath() {
		return "c:/temp/models";
	}

/*
	private void loadMailcards(List<Identity> identities) {
		boolean first=true;
		for(Identity identity: identities) {
			Mailcard mc;
			if (first) {
				mc = getMailcard();
				try {
					UserPersonalInfo upi=WT.getCoreManager().getUserPersonalInfo(getTargetProfileId());
					mc.substitutePlaceholders(upi);			
				} catch(WTException exc) {
					logger.error("cannot load user personal info",exc);
				}
			} else {
				mc = getMailcard(identity);
				if (mc!=null) {
					if(identity.isType(Identity.TYPE_AUTO)) {
						// In case of auto identities we need to build real mainfolder
						try {
							String mailUser = wtd.getMailUsername(i.fromLogin);
							String mainfolder = getSharedFolderName(mailUser, i.mainfolder);
							i.mainfolder = mainfolder;
							if(mainfolder == null) throw new Exception(MessageFormat.format("Shared folderName is null [{0}, {1}]", mailUser, i.mainfolder));

						} catch (Exception ex) {
							logger.error("Unable to get auto identity foldername [{}]", i.email, ex);
						}

						// Avoids default mailcard display for automatic identities
						if(mc.source.equals(Mailcard.TYPE_DEFAULT) && !StringUtils.isEmpty(i.mainfolder)) {
							mc = wtd.getMailcard(profile);
						}
					}

					mc.substitutePlaceholders(getPersonalInfo(i));
					sout+=" mailcardsource: '"+mc.source+"',";
					// Clears any new-line chars in order to avoid html substistution
					// problems at client-side
					mc.html = LangUtils.stripLineBreaks(mc.html);
					sout+=" mailcard: '"+Utils.jsEscape(mc.html)+"',";
				}
				
			}
			
			if (mc!=null) identity.setMailcard(mc);
			
			first=false;
		}		
	}
*/	
}
