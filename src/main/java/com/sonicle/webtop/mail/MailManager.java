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

import com.sonicle.mail.sieve.SieveScriptBuilder;
import com.fluffypeople.managesieve.ManageSieveClient;
import com.fluffypeople.managesieve.SieveScript;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.LangUtils.CollectionChangeSet;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
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
import com.sonicle.webtop.mail.bol.OAutoResponder;
import com.sonicle.webtop.mail.bol.OIdentity;
import com.sonicle.webtop.mail.bol.OInFilter;
import com.sonicle.webtop.mail.bol.model.Identity;
import com.sonicle.webtop.mail.dal.AutoResponderDAO;
import com.sonicle.webtop.mail.dal.IdentityDAO;
import com.sonicle.webtop.mail.dal.InFilterDAO;
import com.sonicle.webtop.mail.model.AutoResponder;
import com.sonicle.webtop.mail.model.MailFilter;
import com.sonicle.webtop.mail.model.MailFiltersType;
import com.sonicle.mail.sieve.SieveMatch;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.util.IdentifierUtils;
import com.sonicle.webtop.mail.bol.OExternalAccount;
import com.sonicle.webtop.mail.bol.OTag;
import com.sonicle.webtop.mail.dal.ExternalAccountDAO;
import com.sonicle.webtop.mail.dal.TagDAO;
import com.sonicle.webtop.mail.model.ExternalAccount;
import com.sonicle.webtop.mail.model.SieveActionList;
import com.sonicle.webtop.mail.model.SieveRuleList;
import com.sonicle.webtop.mail.model.Tag;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

/**
 *
 * @author gabriele.bulfon
 */
public class MailManager extends BaseManager implements IMailManager {

	public static final Logger logger = WT.getLogger(MailManager.class);
	public static final String IDENTITY_SHARING_GROUPNAME = "IDENTITY";
	public static final String IDENTITY_SHARING_ID = "0";
	public static final String SIEVE_OLD_WEBTOP_SCRIPT = "webtop";
	public static final String SIEVE_WEBTOP_SCRIPT = "webtop5";
	
	private SieveConfig sieveConfig = null;
	List<Identity> identities=null;
	
	public MailManager(boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
	}
	
	public void setSieveConfiguration(String host, int port, String username, String password) {
		//TODO: portare i parametri (host,port,user,pass) nel manager
		this.sieveConfig = new SieveConfig(host, port, username, password);
	}
	
	@Override
	public boolean sendMessage(InternetAddress from, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc, String subject, MimeMultipart part) throws WTException {
		com.sonicle.webtop.mail.Service mail = findMailService();
		return mail.sendMsg(from, to, cc, bcc, subject, part);
	}
	
	public com.sonicle.webtop.mail.Service findMailService() throws WTException {
		WebTopSession wts = SessionContext.getCurrent();
		if (wts == null) throw new WTException("Unable to get session");
		com.sonicle.webtop.mail.Service mail = (com.sonicle.webtop.mail.Service)wts.getPrivateServiceById(SERVICE_ID);
		if (mail == null) throw new WTException("Unable to get service");
		return mail;
	}
	
	public InputStream getAttachmentInputStream(String accountId, String foldername, long uidmessage, int idattach) throws WTException {
		try {
			Service s=findMailService();
			return s.getAttachmentInputStream(accountId, foldername, uidmessage, idattach);
		} catch(Exception exc) {
			throw new WTException(exc);
		}
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
	
	public Identity addIdentity(Identity ident) throws WTException {
		Connection con=null;
		Identity newident=null;
		try {
			UserProfileId pid=getTargetProfileId();
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			OIdentity oident=new OIdentity();
			oident.setIdentityId(idao.getSequence(con).intValue());
			oident.setIdentityUid(IdentifierUtils.getUUIDTimeBased());
			oident.setDisplayName(ident.getDisplayName());
			oident.setDomainId(pid.getDomainId());
			oident.setEmail(ident.getEmail());
			oident.setFax(ident.isFax());
			oident.setMainFolder(ident.getMainFolder());
			oident.setUserId(pid.getUserId());
			idao.insert(con, oident);
			if (identities!=null) {
				newident=new Identity(oident);
				identities.add(newident);
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return newident;
	}
	
	public void deleteIdentity(Identity ident) throws WTException {
		Connection con=null;
		try {
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			idao.deleteById(con, ident.getIdentityId());
			if (identities!=null) {
				Identity dident=findIdentity(ident.getIdentityId());
				identities.remove(dident);
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Identity updateIdentity(int identityId, Identity ident) throws WTException {
		Connection con=null;
		Identity uident=null;
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
			if (ident.getIdentityUid()==null) ident.setIdentityUid(IdentifierUtils.getUUIDTimeBased());
			oident.setIdentityUid(ident.getIdentityUid());
			idao.update(con, identityId, oident);
			if (identities!=null) {
				uident=findIdentity(ident.getIdentityId());
				uident.setIdentityUid(ident.getIdentityUid());
				uident.setDisplayName(ident.getDisplayName());
				uident.setEmail(ident.getEmail());
				uident.setFax(ident.isFax());
				uident.setMainFolder(ident.getMainFolder());
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return uident;
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
			Identity id=new Identity(0,null,udata.getDisplayName(),udata.getEmail().getAddress(),null);
			id.setIsMainIdentity(true);
			idents.add(id);
			
			//add configured additional identities
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			List<OIdentity> items=idao.selectByDomainUser(con, pid.getDomainId(),pid.getUserId());
			for(OIdentity oi: items) {
				Identity ident=new Identity(oi);
				idents.add(ident);
			}
			
			//add automatic shared identities
			int autoid=-1;
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
						id = new Identity(Identity.TYPE_AUTO,autoid--,null,udata.getDisplayName(),udata.getEmail().getAddress(),null,false,forceMailcard);
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
	
	public List<ExternalAccount> listExternalAccounts() throws WTException {
			List<ExternalAccount> externalAccountList =  new ArrayList<>();
			Connection connection = null;
			ExternalAccountDAO dao = ExternalAccountDAO.getInstance();
		try {
			
			connection = WT.getConnection(SERVICE_ID);
			UserProfileId userProfileId = getTargetProfileId();
			List<OExternalAccount> externalAccounts = dao.selectByDomainUser(connection, userProfileId.getDomainId(), userProfileId.getUserId());
			for(OExternalAccount externalAccount: externalAccounts) {
				ExternalAccount account = new ExternalAccount();
				account.setExternalAccountId(externalAccount.getExternalAccountId());
				account.setDisplayName(externalAccount.getDisplayName());
				account.setAccountDescription(externalAccount.getDescription());
				account.setEmail(externalAccount.getEmail());
				account.setProtocol(externalAccount.getProtocol());
				account.setHost(externalAccount.getHost());
				account.setPort(externalAccount.getPort());
				account.setReadonlyProvider(externalAccount.getReadonlyProvider());
				account.setUserName(externalAccount.getUsername());
				account.setPassword(externalAccount.getPassword());
				account.setFolderPrefix(externalAccount.getFolderPrefix());
				account.setFolderSent(externalAccount.getFolderSent());
				account.setFolderDrafts(externalAccount.getFolderDrafts());
				account.setFolderTrash(externalAccount.getFolderTrash());
				account.setFolderSpam(externalAccount.getFolderSpam());
				account.setFolderArchive(externalAccount.getFolderArchive());
				externalAccountList.add(account);
			}
			//externalAccountList = externalAccounts.stream().map(mapToExternalAccount()).collect(Collectors.toList());
		} catch (SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(connection);
		}
		return externalAccountList;
	}
	
	public ExternalAccount getExternalAccount(int externalAccountId) throws WTException {
		ExternalAccount externalAccount = null;
		Connection connection = null;
		ExternalAccountDAO dao = ExternalAccountDAO.getInstance();
		
		try {
			connection = WT.getConnection(SERVICE_ID);
			externalAccount = mapToExternalAccount().apply(dao.selectById(connection, externalAccountId));
			
		} catch (SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(connection);
		}
		return externalAccount;
	}
	
	public void addExternalAccount(ExternalAccount account) throws WTException {
		Connection connection = null;
		ExternalAccountDAO dao = ExternalAccountDAO.getInstance();
		
		try {
			OExternalAccount externalAccount = mapToOExternalAccount().apply(account);
			connection = WT.getConnection(SERVICE_ID);
			externalAccount.setExternalAccountId(dao.getSequence(connection).intValue());
			
			UserProfileId userProfileId = getTargetProfileId();
			externalAccount.setDomainId(userProfileId.getDomainId());
			externalAccount.setUserId(userProfileId.getUserId());
			dao.insert(connection, externalAccount);
			
		} catch (SQLException | DAOException ex) {
			StackTraceElement[] els = ex.getStackTrace();
			for(StackTraceElement st : els)
				System.out.println(st.toString());
			System.out.println(ex.getCause());
			System.out.println(ex.getMessage());
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(connection);
		}
	}
	
	public void removeExternalAccount(int externalAccountId) throws WTException {
		Connection connection = null;
		ExternalAccountDAO dao = ExternalAccountDAO.getInstance();
		
		try {
			connection = WT.getConnection(SERVICE_ID);
			dao.deleteById(connection, externalAccountId);
			
		} catch (SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(connection);
		}
	}
  	
	public void updateExternalAccount(ExternalAccount account) throws WTException {
		Connection connection = null;
		ExternalAccountDAO dao = ExternalAccountDAO.getInstance();
		
		try {
			OExternalAccount externalAccount = mapToOExternalAccount().apply(account);
			connection = WT.getConnection(SERVICE_ID);
			dao.update(connection, externalAccount);
			
		} catch (SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(connection);
		}
	}
	
	private Function<OExternalAccount, ExternalAccount> mapToExternalAccount() {
		return externalAccount -> {
			ExternalAccount account = new ExternalAccount();
			account.setExternalAccountId(externalAccount.getExternalAccountId());
			account.setDisplayName(externalAccount.getDisplayName());
			account.setAccountDescription(externalAccount.getDescription());
			account.setEmail(externalAccount.getEmail());
			account.setProtocol(externalAccount.getProtocol());
			account.setHost(externalAccount.getHost());
			account.setPort(externalAccount.getPort());
			account.setReadonlyProvider(externalAccount.getReadonlyProvider());
			account.setUserName(externalAccount.getUsername());
			account.setPassword(externalAccount.getPassword());
			account.setFolderPrefix(externalAccount.getFolderPrefix());
			account.setFolderSent(externalAccount.getFolderSent());
			account.setFolderDrafts(externalAccount.getFolderDrafts());
			account.setFolderTrash(externalAccount.getFolderTrash());
			account.setFolderSpam(externalAccount.getFolderSpam());
			account.setFolderArchive(externalAccount.getFolderArchive());
			return account;
		};	
	}
	
	private Function<ExternalAccount, OExternalAccount> mapToOExternalAccount() {
		return externalAccount -> {
			OExternalAccount account = new OExternalAccount();
			account.setExternalAccountId(externalAccount.getExternalAccountId());
			account.setDisplayName(externalAccount.getDisplayName());
			account.setDescription(externalAccount.getAccountDescription());
			account.setEmail(externalAccount.getEmail());
			account.setProtocol(externalAccount.getProtocol());
			account.setHost(externalAccount.getHost());
			account.setPort(externalAccount.getPort());
			account.setReadonlyProvider(externalAccount.isReadonlyProvider());
			account.setUsername(externalAccount.getUserName());
			account.setPassword(externalAccount.getPassword());
			account.setFolderPrefix(externalAccount.getFolderPrefix());
			account.setFolderSent(externalAccount.getFolderSent());
			account.setFolderDrafts(externalAccount.getFolderDrafts());
			account.setFolderTrash(externalAccount.getFolderTrash());
			account.setFolderSpam(externalAccount.getFolderSpam());
			account.setFolderArchive(externalAccount.getFolderArchive());
			return account;
		};	
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
		Mailcard mc = null;
		if (identity.getIdentityUid()!=null) mc=readIdentityMailcard(pid.getDomainId(),identity);
		if (mc != null) return mc;
		mc = readEmailMailcard(pid.getDomainId(),identity.getEmail());
		if (mc != null) return mc;
		UserProfileId fpid=identity.getOriginPid();
		if (fpid!=null) mc = readUserMailcard(fpid.getDomainId(),fpid.getUserId());
		if (mc != null) return mc;
		mc = readEmailDomainMailcard(pid.getDomainId(),identity.getEmail());
		if (mc != null) return mc;
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
	
	private Mailcard readIdentityMailcard(String domainId, Identity identity) {
		String mailcard = readMailcard(domainId, "mailcard_" + identity.getEmail()+"_"+identity.getIdentityUid());
		if (mailcard != null) {
			return new Mailcard(Mailcard.TYPE_EMAIL, mailcard);
		}
		return null;
	}

	public void setIdentityMailcard(Identity ident, String html) {
		String domainId=getTargetProfileId().getDomainId();
		if (ident.getIdentityUid()==null) setEmailMailcard(ident.getEmail(),html);
		else writeMailcard(domainId, "mailcard_" + ident.getEmail() + "_"+ident.getIdentityUid(), html);
	}
	
	public void setUserMailcard(String html) {
		ensureProfileDomain(true);
		if (RunContext.isWebTopAdmin() || RunContext.isPermitted(true, getTargetProfileId(), SERVICE_ID, "MAILCARD_SETTINGS", "CHANGE")) {
			writeMailcard(getTargetProfileId().getDomainId(), "mailcard_" + getTargetProfileId().getUserId(), html);
		} else {
			throw new AuthException("You have insufficient rights to perform the operation [{}]", "MAILCARD_SETTINGS:CHANGE");
		}
	}
	
	public void setEmailMailcard(String email, String html) {
		ensureProfileDomain(true);
		if (RunContext.isWebTopAdmin() || RunContext.isPermitted(true, getTargetProfileId(), SERVICE_ID, "MAILCARD_SETTINGS", "CHANGE")) {
			writeMailcard(getTargetProfileId().getDomainId(), "mailcard_" + email, html);
		} else {
			throw new AuthException("You have insufficient rights to perform the operation [{}]", "MAILCARD_SETTINGS:CHANGE");
		}
	}

	public void setEmailDomainMailcard(String email, String html) {
		ensureProfileDomain(true);
		if (RunContext.isWebTopAdmin() || RunContext.isPermitted(true, getTargetProfileId(), SERVICE_ID, "DOMAIN_MAILCARD_SETTINGS", "CHANGE")) {
			if (!StringUtils.contains(email, "@")) return;
			writeMailcard(getTargetProfileId().getDomainId(), "mailcard_" + StringUtils.substringAfterLast(email, "@"), html);
		} else {
			throw new AuthException("You have insufficient rights to perform the operation [{}]", "DOMAIN_MAILCARD_SETTINGS:CHANGE");
		}
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
	
	
	
	
	
	public List<Tag> getTags() throws WTException {
		TagDAO tagdao = TagDAO.getInstance();
		List<Tag> tags = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
				List<OTag> items = tagdao.selectByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
				for (OTag item : items) {
					tags.add(createTag(item));
				}
				return tags;
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addTag(Tag tag) throws WTException {
		TagDAO tagdao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			tagdao.insert(con, createTag(getTargetProfileId().getDomainId(), getTargetProfileId().getUserId(),tag));
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void removeTag(String tagId) throws WTException {
		TagDAO tagdao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			tagdao.deleteById(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId(),tagId);
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateTag(Tag tag,String newTagId) throws WTException {
		TagDAO tagdao = TagDAO.getInstance();
		Connection con = null;
		String oldTagId="";
		
		try {
			con = WT.getConnection(SERVICE_ID);
			oldTagId=tag.getTagId();
			tag.setTagId(newTagId);
			tagdao.update(con,oldTagId, createTag(getTargetProfileId().getDomainId(), getTargetProfileId().getUserId(),tag));
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateFoldersTag(String oldTagId , String newTagId, Collection<FolderCache> folders){
		
		for(FolderCache fc: folders){
			Message msgs[] = null;
			try {
				msgs = fc.getMessages(null,oldTagId,"flag",FolderCache.SORT_BY_DATE,false,true,-1,true,false);
				long[] uid = new long[msgs.length];
				for (int i=0;i<msgs.length;i++) {
					uid[i]=fc.getUID(msgs[i]);
				}
				fc.untagMessages(uid, oldTagId);
				fc.tagMessages(uid, newTagId);
			} catch (MessagingException ex) {
				logger.error("Error updating folder tags on "+fc.getFolderName(),ex);
			} catch (IOException ex) {
				logger.error("Error updating folder tags on "+fc.getFolderName(),ex);
			}
		}
	}
	
	public String sanitazeTagId(String tagId){
		 tagId=tagId.replace(" ","_");
		 tagId=tagId.replace("%","_");
		 tagId=tagId.replace("*","_");
		 tagId=tagId.replace("\\","_");
		 tagId=tagId.replace("]","_");
	     tagId=tagId.replace("[","_");			 
		 for(char c=1;c<32;++c) tagId=tagId.replace(String.valueOf(c),"_");	
		 return tagId;		
	}
	
	private static Tag builtinTags[] = {
		new Tag("$label1","$Label1","#ff003a"),
		new Tag("$label2","$Label2","#ff9900"),
		new Tag("$label3","$Label3","#009900"),
		new Tag("$label4","$Label4","#3333ff"),
		new Tag("$label5","$Label5","#993399")
	};
	
	protected void addBuiltinTags() throws WTException {
		TagDAO tagdao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			for(Tag tag: builtinTags) {
				try {
					tagdao.insert(con, new OTag(
							getTargetProfileId().getDomainId(), 
							getTargetProfileId().getUserId(), 
							tag.getTagId(),
							lookupResource(getLocale(), MailLocaleKey.TAGS_LABEL(tag.getTagId())),
							tag.getColor()
					));
				} catch(DataAccessException exc) {
					
				}
			}
		} catch(SQLException ex) {
			throw new WTException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private Tag createTag(OTag otag) {
		if (otag == null) return null;
		Tag tag = new Tag(otag.getTagId(),otag.getDescription(),otag.getColor());
		return tag;
	}	
	
	private OTag createTag(String domainId, String userId, Tag tag) {
		if (tag == null) return null;
		OTag otag = new OTag();
		otag.setDomainId(domainId);
		otag.setUserId(userId);
		otag.setTagId(tag.getTagId());
		otag.setDescription(tag.getDescription());
		otag.setColor(tag.getColor());
		return otag;
	}	
	
	
	public boolean isAutoResponderActive() throws WTException {
		AutoResponderDAO autdao = AutoResponderDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OAutoResponder oaut = autdao.selectByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			return (oaut != null) ? oaut.getEnabled() : false;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public AutoResponder getAutoResponder() throws WTException {
		AutoResponderDAO autdao = AutoResponderDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OAutoResponder oaut = autdao.selectByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if (oaut == null) {
				return new AutoResponder();
			} else {
				return createAutoResponder(oaut);
			}
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateAutoResponder(AutoResponder autoResponder) throws WTException {
		AutoResponderDAO autdao = AutoResponderDAO.getInstance();
		Connection con = null;
		
		//TODO: valutare permessi... admin?
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OAutoResponder oaut = createOAutoResponder(autoResponder);
			oaut.setDomainId(getTargetProfileId().getDomainId());
			oaut.setUserId(getTargetProfileId().getUserId());
			boolean exist = autdao.existByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if (exist) {
				autdao.update(con, oaut);
			} else {
				autdao.insert(con, oaut);
			}
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<MailFilter> getMailFilters(MailFiltersType type) throws WTException {
		return getMailFilters(type, false);
	}
	
	public List<MailFilter> getMailFilters(MailFiltersType type, boolean enabledOnly) throws WTException {
		InFilterDAO indao = InFilterDAO.getInstance();
		List<MailFilter> filters = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			if (type.equals(MailFiltersType.INCOMING)) {
				List<OInFilter> items = indao.selectByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
				if (enabledOnly) {
					items = indao.selectEnabledByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
				} else {
					items = indao.selectByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
				}
				for (OInFilter item : items) {
					filters.add(createMailFilter(item));
				}
				return filters;
			} else {
				throw new WTException("Type not supported yet [{0}]", type.toString());
			}
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateMailFilters(MailFiltersType type, List<MailFilter> filters) throws WTException {
		InFilterDAO indao = InFilterDAO.getInstance();
		Connection con = null;
		
		//TODO: valutare permessi... admin?
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			if (type.equals(MailFiltersType.INCOMING)) {
				List<MailFilter> origFilters = getMailFilters(type);
				CollectionChangeSet<MailFilter> changeSet = LangUtils.getCollectionChanges(origFilters, filters);
				
				OInFilter ofil = null;
				for(MailFilter filter : changeSet.inserted) {
					ofil = createOInFilter(filter);
					ofil.setInFilterId(indao.getSequence(con).intValue());
					ofil.setDomainId(getTargetProfileId().getDomainId());
					ofil.setUserId(getTargetProfileId().getUserId());
					indao.insert(con, ofil);
				}
				for(MailFilter filter : changeSet.updated) {
					ofil = createOInFilter(filter);
					indao.update(con, ofil);
				}
				for(MailFilter filter : changeSet.deleted) {
					indao.delete(con, filter.getFilterId());
				}
				
				DbUtils.commitQuietly(con);
				
			} else {
				throw new WTException("Type not supported yet [{0}]", type.toString());
			}
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SieveScript> listSieveScripts() throws WTException {
		ManageSieveClient client = null;
		
		ensureUserDomain();
		try {
			client = createSieveClient();
			return SieveHelper.listScripts(client);
		} finally {
			SieveHelper.logoutSieveClientQuietly(client);
		}
	}
	
	public String getActiveSieveScriptName() throws WTException {
		ManageSieveClient client = null;
		
		ensureUserDomain();
		try {
			client = createSieveClient();
			return SieveHelper.getActiveScript(client);
		} finally {
			SieveHelper.logoutSieveClientQuietly(client);
		}
	}
	
	public void activateSieveScript(String name) throws WTException {
		ManageSieveClient client = null;
		
		ensureUser();
		try {
			client = createSieveClient();
			SieveHelper.activateScript(client, name);
		} finally {
			SieveHelper.logoutSieveClientQuietly(client);
		}
	}
	
	public void applySieveScript(boolean activate) throws WTException {
		UserProfile.Data ud = WT.getUserData(getTargetProfileId());
		SieveScriptBuilder ssb = new SieveScriptBuilder();
		MailServiceSettings mss = new MailServiceSettings(SERVICE_ID,getTargetProfileId().getDomainId());
		MailUserSettings mus = new MailUserSettings(getTargetProfileId(),mss);
		if (!mss.isSieveSpamFilterDisabled())
			ssb.setSpamFilter(mus.getFolderSpam());
		
		ensureUser();
		logger.debug("Working on autoresponder...");
		AutoResponder autoResp = getAutoResponder();
		if (autoResp.getEnabled()) {
			ssb.setVacation(autoResp.toSieveVacation(ud.getPersonalEmail(), ud.getTimeZone()));
		}
		
		logger.debug("Working on incoming filters...");
		List<MailFilter> filters = getMailFilters(MailFiltersType.INCOMING);
		
		// Arrange filters in the specified order and fill the builder
		Collections.sort(filters, new Comparator<MailFilter>() {
			@Override
			public int compare(MailFilter o1, MailFilter o2) {
				return Short.compare(o1.getOrder(), o2.getOrder());
			}
		});
		for(MailFilter filter : filters) {
			if (filter.getEnabled()) {
				ssb.addFilter(filter.getName(), filter.getSieveMatch(), filter.getSieveRules(), filter.getSieveActions());
			}
		}
		
		String script = buildSieveScriptHeader() + ssb.build();
		
		ManageSieveClient client = null;
		try {
			if (sieveConfig == null) throw new WTException("SieveConfiguration not defined. Please call setSieveConfiguration(...) before call this method!");
			client = createSieveClient();
			SieveHelper.putScript(client, SIEVE_WEBTOP_SCRIPT, script);
			if (activate) {
				SieveHelper.activateScript(client, SIEVE_WEBTOP_SCRIPT);
			}
		} finally {
			SieveHelper.logoutSieveClientQuietly(client);
		}
	}
	
	private ManageSieveClient createSieveClient() throws WTException {
		if (sieveConfig == null) throw new WTException("SieveConfiguration not defined. Please call setSieveConfiguration(...) before using Sieve!");
		return SieveHelper.createSieveClient(sieveConfig.getHost(), sieveConfig.getPort(), sieveConfig.getUsername(), sieveConfig.getPassword());
	}
	
	private String buildSieveScriptHeader() {
		DateTimeFormatter fmt = DateTimeUtils.createYmdHmsFormatter();
		StringBuilder sb = new StringBuilder();
		sb.append("# Generated by WebTop [http://www.sonicle.com]");
		sb.append("\n");
		sb.append("# ").append(SERVICE_ID).append("@").append(WT.getManifest(SERVICE_ID).getVersion().toString());
		sb.append("\n");
		sb.append("# ").append(fmt.print(DateTimeUtils.now()));
		sb.append("\n");
		sb.append("\n");
		return sb.toString();
	}
	
	private AutoResponder createAutoResponder(OAutoResponder oaut) {
		if (oaut == null) return null;
		AutoResponder aut = new AutoResponder();
		aut.setEnabled(oaut.getEnabled());
		aut.setSubject(oaut.getSubject());
		aut.setMessage(oaut.getMessage());
		aut.setAddresses(oaut.getAddresses());
		aut.setDaysInterval(oaut.getDaysInterval());
		aut.setActivationStartDate(oaut.getStartDate());
		aut.setActivationEndDate(oaut.getEndDate());
		aut.setSkipMailingLists(oaut.getSkipMailingLists());
		return aut;
	}
	
	private OAutoResponder createOAutoResponder(AutoResponder aut) {
		if (aut == null) return null;
		OAutoResponder oaut = new OAutoResponder();
		oaut.setEnabled(aut.getEnabled());
		oaut.setSubject(aut.getSubject());
		oaut.setMessage(aut.getMessage());
		oaut.setAddresses(aut.getAddresses());
		oaut.setDaysInterval(aut.getDaysInterval());
		oaut.setStartDate(aut.getActivationStartDate());
		oaut.setEndDate(aut.getActivationEndDate());
		oaut.setSkipMailingLists(aut.getSkipMailingLists());
		return oaut;
	}
	
	private OInFilter createOInFilter(MailFilter fil) {
		if (fil == null) return null;
		OInFilter ofil = new OInFilter();
		ofil.setInFilterId(fil.getFilterId());
		ofil.setEnabled(fil.getEnabled());
		ofil.setOrder(fil.getOrder());
		ofil.setName(fil.getName());
		ofil.setSieveMatch(EnumUtils.toSerializedName(fil.getSieveMatch()));
		ofil.setSieveRules(LangUtils.serialize(fil.getSieveRules(), SieveRuleList.class));
		ofil.setSieveActions(LangUtils.serialize(fil.getSieveActions(), SieveActionList.class));
		return ofil;
	}
	
	private MailFilter createMailFilter(OInFilter ofil) {
		if (ofil == null) return null;
		MailFilter fil = new MailFilter();
		fil.setFilterId(ofil.getInFilterId());
		fil.setEnabled(ofil.getEnabled());
		fil.setOrder(ofil.getOrder());
		fil.setName(ofil.getName());
		fil.setSieveMatch(EnumUtils.forSerializedName(ofil.getSieveMatch(), SieveMatch.class));
		SieveRuleList rules = LangUtils.deserialize(ofil.getSieveRules(), null, SieveRuleList.class);
		if (rules != null) fil.getSieveRules().addAll(rules);
		SieveActionList acts = LangUtils.deserialize(ofil.getSieveActions(), null, SieveActionList.class);
		if (acts != null) fil.getSieveActions().addAll(acts);
		return fil;
	}
	
	private static class SieveConfig {
		private String host;
		private int port;
		private String username;
		private char[] password;
		
		public SieveConfig(String host, int port, String username, String password) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password.toCharArray();
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return new String(password);
		}
	}
}
