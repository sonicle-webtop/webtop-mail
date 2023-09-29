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
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.CredentialAlgorithm;
import com.sonicle.security.Principal;
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.AbstractLdapDirectory;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.PrivateEnvironment;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.util.Encryption;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.mail.bol.OIdentity;
import com.sonicle.webtop.mail.bol.OUserMap;
import com.sonicle.webtop.mail.bol.model.Identity;
import com.sonicle.webtop.mail.dal.IdentityDAO;
import com.sonicle.webtop.mail.dal.UserMapDAO;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gbulfon
 */
public class MailUserProfile {
    
	public final static Logger logger = (Logger) LoggerFactory.getLogger(MailUserProfile.class);
	
    //private PrivateEnvironment env;
    private String folderPrefix;
    private boolean scanAll;
    private int scanSeconds;
    private int scanCycles;
    private String folderSent;
    private String folderDrafts;
    private String folderTrash;
    private String folderSpam;
    private String folderArchive;
    private String mailProtocol;
	private String mailHost;
    private int mailPort;
	private String mailUsername;
	private String mailPassword;
	private String replyTo;
	private List<Identity> identities;
	private String sharedSort;
	private boolean includeMessageInReply;
	private int numMessageList;
    private MailManager mman;
	private MailServiceSettings mss;

    public MailUserProfile(MailManager mman, MailServiceSettings mss, MailUserSettings mus, String dirScheme) {
		this.mman=mman;
		this.mss = mss;
		UserProfileId profile = mman.getTargetProfileId();
		Connection con = null;
		
		try {
			Principal principal = RunContext.getPrincipal();
			con = WT.getConnection(mman.SERVICE_ID);
			
			//MailUserSettings mus=ms.getMailUserSettings();
			mailProtocol=mus.getProtocol();
			mailHost=mus.getHost();
			mailPort=mus.getPort();
			mailUsername=mus.getUsername();
			mailPassword=mus.getPassword();
			
			//Use AD properties if it can provide them
			//AuthenticationDomain ad=principal.getAuthenticationDomain();
/*			if (ad!=null && ad.getProperty("mail.protocol",null)!=null) {
				mailProtocol=ad.getProperty("mail.protocol",null);
				mailHost=ad.getProperty("mail.host",null);
				mailPort=ad.getProperty("mail.port",0);
				mailUsername=ad.getProperty("mail.username",null);
				mailPassword=ad.getProperty("mail.password",null);
			} else {*/
				OUserMap omap=UserMapDAO.getInstance().selectById(con, profile.getDomainId(), profile.getUserId());
				if (omap!=null) {
					mailProtocol=omap.getMailProtocol();
					mailHost=omap.getMailHost();
					mailPort=omap.getMailPort();
					mailUsername=omap.getMailUser();
					mailPassword=omap.getMailPassword();
				}
			//}
			

			//If LDAP overwrite any null value with specific LDAP default values
			com.sonicle.security.auth.directory.AbstractDirectory dir=DirectoryManager.getManager().getDirectory(dirScheme);
			if (dir!=null && dir instanceof com.sonicle.security.auth.directory.AbstractLdapDirectory) {
				//MailServiceSettings mss=ms.getMailServiceSettings();
				if (mailHost==null) mailHost=mss.getDefaultHost();
				if (mailProtocol==null) mailProtocol=mss.getDefaultProtocol();
				if (mailPort==0) mailPort=mss.getDefaultPort();
				if (mailUsername==null||mailUsername.trim().length()==0) mailUsername=profile.getUserId();
				//TODO: which domain-name is needed here?
				if (mailUsername.indexOf('@')<0 && schemeWantsUserWithDomain(dirScheme)) mailUsername+="@"+WT.getPrimaryDomainName(profile.getDomainId());
				if (principal!=null && (mailPassword==null||mailPassword.trim().length()==0)) mailPassword=new String(principal.getPassword());
			}
			
			//If still something is invalid, provides defaults
			if (mailHost==null) mailHost="localhost";
			if (mailProtocol==null) mailProtocol="imap";
			if (mailPort==0) {
				switch(mailProtocol) {
					case "imap":
						mailPort=143;
						break;
						
					case "imaps":
						mailPort=993;
						break;
				}
			}
			
			//TODO: encrypted mail password
			//CredentialAlgorithm encpasswordType=principal.getCredentialAlgorithm();
			//String encpassword=principal.getCredential();
			//if (encpasswordType==null) encpassword=new String(principal.getPassword());

			if (mailUsername==null||mailUsername.trim().length()==0 && principal!=null) mailUsername=profile.getUserId();
			if (principal!=null && (mailPassword==null||mailPassword.trim().length()==0)) mailPassword=new String(principal.getPassword());
			//else {
			//	if (encpasswordType!=null && !encpasswordType.equals(CredentialAlgorithm.PLAIN))
			//		mailPassword=Encryption.decipher(mailPassword,encpassword);
			//}
			
			folderPrefix=mus.getFolderPrefix();
			scanAll=mus.isScanAll();
			scanSeconds=mus.getScanSeconds();
			scanCycles=mus.getScanCycles();
			folderSent=mus.getFolderSent();
			folderDrafts=mus.getFolderDrafts();
			folderTrash=mus.getFolderTrash();
			folderArchive=mus.getFolderArchive();
			folderSpam=mus.getFolderSpam();
			replyTo=mus.getReplyTo();
			sharedSort=mus.getSharedSort();
			includeMessageInReply=mus.isIncludeMessageInReply();
			numMessageList=mus.getPageRows();
			
			//List<OIdentity> oids=IdentityDAO.getInstance().selectByDomainUser(con, profile.getDomainId(), profile.getUserId());
			//identities=new Identity[oids.size()];
			//int i=0;
			//for(OIdentity oid: oids) {
            //    Identity ident=new Identity(oid.getIdentityId(),oid.getDisplayName(),oid.getEmail(),oid.getMainFolder());
			//	identities[i++]=ident;
			//}
			identities=mman.listIdentities();
			
			
		} catch(Exception ex) {
			logger.error("Error mapping mail user", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	
    public MailUserProfile(MailManager mman, MailServiceSettings mss, MailUserSettings mus, UserProfile profile) {
        this.mss = mss;
		//this.env=env;
		//UserProfile profile=env.getProfile();
        
		Connection con=null;
		try {
			con = WT.getConnection(mman.SERVICE_ID);
            //mman=(MailManager)WT.getServiceManager(ms.SERVICE_ID);
			this.mman=mman;
			
			Principal principal=profile.getPrincipal();
			
			//MailUserSettings mus=ms.getMailUserSettings();
			mailProtocol=mus.getProtocol();
			mailHost=mus.getHost();
			mailPort=mus.getPort();
			mailUsername=mus.getUsername();
			mailPassword=mus.getPassword();
			
			//Use AD properties if it can provide them
			AuthenticationDomain ad=principal.getAuthenticationDomain();
/*			if (ad!=null && ad.getProperty("mail.protocol",null)!=null) {
				mailProtocol=ad.getProperty("mail.protocol",null);
				mailHost=ad.getProperty("mail.host",null);
				mailPort=ad.getProperty("mail.port",0);
				mailUsername=ad.getProperty("mail.username",null);
				mailPassword=ad.getProperty("mail.password",null);
			} else {*/
				OUserMap omap=UserMapDAO.getInstance().selectById(con, profile.getDomainId(), profile.getUserId());
				if (omap!=null) {
					mailProtocol=omap.getMailProtocol();
					mailHost=omap.getMailHost();
					mailPort=omap.getMailPort();
					mailUsername=omap.getMailUser();
					mailPassword=omap.getMailPassword();
				}
			//}
			

			//If LDAP overwrite any null value with specific LDAP default values
			com.sonicle.security.auth.directory.AbstractDirectory dir=DirectoryManager.getManager().getDirectory(ad.getDirUri().getScheme());
			if (ad!=null && dir!=null && dir instanceof com.sonicle.security.auth.directory.AbstractLdapDirectory) {
				//MailServiceSettings mss=ms.getMailServiceSettings();
				if (mailHost==null) mailHost=mss.getDefaultHost();
				if (mailProtocol==null) mailProtocol=mss.getDefaultProtocol();
				if (mailPort==0) mailPort=mss.getDefaultPort();
				if (mailUsername==null||mailUsername.trim().length()==0) mailUsername=principal.getUserId();
				if (!mss.isAuthUserStripDomain() && mailUsername.indexOf('@')<0 && schemeWantsUserWithDomain(ad)) mailUsername+="@"+ad.getInternetName();
				if (mailPassword==null||mailPassword.trim().length()==0) mailPassword=new String(principal.getPassword());
			}
			
			//If still something is invalid, provides defaults
			if (mailHost==null) mailHost="localhost";
			if (mailProtocol==null) mailProtocol="imap";
			if (mailPort==0) {
				switch(mailProtocol) {
					case "imap":
						mailPort=143;
						break;
						
					case "imaps":
						mailPort=993;
						break;
				}
			}
			
			//TODO: encrypted mail password
			//CredentialAlgorithm encpasswordType=principal.getCredentialAlgorithm();
			//String encpassword=principal.getCredential();
			//if (encpasswordType==null) encpassword=new String(principal.getPassword());

			if (mailUsername==null||mailUsername.trim().length()==0) mailUsername=principal.getUserId();
			if (mailPassword==null||mailPassword.trim().length()==0) mailPassword=new String(principal.getPassword());
			//else {
			//	if (encpasswordType!=null && !encpasswordType.equals(CredentialAlgorithm.PLAIN))
			//		mailPassword=Encryption.decipher(mailPassword,encpassword);
			//}
			
			folderPrefix=mus.getFolderPrefix();
			scanAll=mus.isScanAll();
			scanSeconds=mus.getScanSeconds();
			scanCycles=mus.getScanCycles();
			folderSent=mus.getFolderSent();
			folderDrafts=mus.getFolderDrafts();
			folderTrash=mus.getFolderTrash();
			folderArchive=mus.getFolderArchive();
			folderSpam=mus.getFolderSpam();
			replyTo=mus.getReplyTo();
			sharedSort=mus.getSharedSort();
			includeMessageInReply=mus.isIncludeMessageInReply();
			numMessageList=mus.getPageRows();
			
			//List<OIdentity> oids=IdentityDAO.getInstance().selectByDomainUser(con, profile.getDomainId(), profile.getUserId());
			//identities=new Identity[oids.size()];
			//int i=0;
			//for(OIdentity oid: oids) {
            //    Identity ident=new Identity(oid.getIdentityId(),oid.getDisplayName(),oid.getEmail(),oid.getMainFolder());
			//	identities[i++]=ident;
			//}
			identities=mman.listIdentities();
			
		} catch(Exception exc) {
			logger.error("Error mapping mail user",exc);
		} finally {
			DbUtils.closeQuietly(con);
		}
    }
	
	private boolean schemeWantsUserWithDomain(String directoryScheme) {
		//return "ad".equals(directoryScheme) || directoryScheme.startsWith("ldap");
		return MailSettings.ACLDomainSuffixPolicy.APPEND.equals(mss.getACLDomainSuffixPolicy(directoryScheme));
	}
    
	private boolean schemeWantsUserWithDomain(AuthenticationDomain ad) {
		//String scheme=ad.getDirUri().getScheme();
		//return scheme.equals("ad")||scheme.startsWith("ldap");
		return MailSettings.ACLDomainSuffixPolicy.APPEND.equals(mss.getACLDomainSuffixPolicy(ad.getDirUri().getScheme()));
	}
	
    public String getFolderPrefix() {
        return folderPrefix;
    }
    
    public boolean isScanAll() {
        return scanAll;
    }
    
    public int getScanSeconds() {
        return scanSeconds;
    }
    
    public int getScanCycles() {
        return scanCycles;
    }
    
    public String getFolderSent() {
        return folderSent;
    }
	
	public void setFolderSent(String name) {
		folderSent=name;
	}

    public String getFolderDrafts() {
        return folderDrafts;
    }
    
	public void setFolderDrafts(String name) {
		folderDrafts=name;
	}

    public String getFolderTrash() {
        return folderTrash;
    }
    
	public void setFolderTrash(String name) {
		folderTrash=name;
	}

    public String getFolderSpam() {
        return folderSpam;
    }
	
	public void setFolderSpam(String name) {
		folderSpam=name;
	}

    public String getFolderArchive() {
        return folderArchive;
    }
    
	public void setFolderArchive(String name) {
		folderArchive=name;
	}

    public String getMailProtocol() {
        return mailProtocol;
    }
    
    public String getMailHost() {
        return mailHost;
    }
	
    public int getMailPort() {
        return mailPort;
    }
    
    public String getMailUsername() {
        return mailUsername;
    }
	
    public String getMailPassword() {
        return mailPassword;
    }
	
	public String getReplyTo() {
		return replyTo;
	}
	
	public Identity[] getIdentities() {
		return identities.toArray(new Identity[0]);
	}
	
	public Identity getIdentityAt(int index) {
		return identities.get(index);
	}
	
	public Identity getIdentity(String displayName, String email) {
		for(Identity ident: identities) {
			if (ident.getDisplayName().equals(displayName) && ident.getEmail().equals(email))
				return ident;
		}
		return null;
	}
    
	public Identity getIdentity(int identityId) {
		for(Identity ident: identities) {
			if (ident.getIdentityId()==identityId)
				return ident;
		}
		return null;
	}
	
	public Identity getIdentity(String foldername) {
		for(Identity ident: identities) {
			String mainFolder=ident.getMainFolder();
			if (mainFolder!=null && mainFolder.length()>0 && mainFolder.equals(foldername)) {
				return ident;
		}
	  }
	  return mman.getMainIdentity();
	}
		
	public String getSharedSort() {
		return sharedSort;
	}
	
	public boolean isIncludeMessageInReply() {
		return includeMessageInReply;
	}
	
	public int getNumMsgList() {
		return numMessageList;
	}
	
	public void setNumMsgList(int n) {
		numMessageList=n;
	}
	
}
