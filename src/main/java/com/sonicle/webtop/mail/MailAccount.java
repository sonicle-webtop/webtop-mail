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

import com.sonicle.commons.LangUtils;
import com.sonicle.mail.imap.SonicleIMAPStore;
import com.sonicle.mail.imap.SonicleIMAPFolder;
import com.sonicle.webtop.core.app.PrivateEnvironment;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Quota;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.HeaderTerm;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author gabriele.bulfon
 */
public class MailAccount {
	private String id;
	private Service ms;
	private PrivateEnvironment environment;

	private int port;
	private String mailHost;
	private String mailUsername;
	private String mailPassword;
	private String authorizationId=null;
	private boolean isImpersonated=false;
	private String vmailSecret=null;
	private String replyTo=null;
	private boolean readonly=false;

	private Session session;
	private Store store;
	private String storeProtocol;
	private boolean disconnecting = false;
	private String sharedPrefixes[] = null;
	private char folderSeparator = 0;
	private String folderPrefix = null;
	private String folderSent = null;
	private String folderDrafts = null;
	private String folderTrash = null;
	private String folderSpam = null;
	private String folderArchive = null;
	private boolean validated = false;
	private boolean hasAnnotations=false;
	private boolean hasInboxFolder=false;
	private boolean isDovecot=false;
	private boolean hasDifferentDefaultFolder=false;
	private String defaultFolderName= null;
	private HashMap<String, FolderCache> foldersCache = new HashMap<String, FolderCache>();
	private FolderCache fcRoot = null;
	private FolderCache[] fcShared = null;
	private String skipReplyFolders[] = new String[]{};
	private String skipForwardFolders[] = new String[]{};
	private MailFoldersThread mft=null;
	private Thread cacheLoadThread;

	public MailAccount(String id, Service mailService, PrivateEnvironment environment) {
		this.id=id;
		this.ms=mailService;
		this.environment=environment;
	}

	public String getId() {
		return id;
	}
	
	public MailFoldersThread getFoldersThread() {
		return mft;
	}
	
	public void setFoldersThread(MailFoldersThread mft) {
		this.mft=mft;
	}
	
	public void setFolderPrefix(String folderPrefix) {
		this.folderPrefix=folderPrefix;
	}
	
	public String getFolderPrefix() {
		return folderPrefix;
	}
	
	public void setProtocol(String protocol) {
		this.storeProtocol=protocol;
	}
	
	public String getProtocol() {
		return storeProtocol;
	}
	
	public boolean isDovecot() {
		return isDovecot;
	}
	
	public boolean isReadOnly() {
		return readonly;
	}
	
	public void setReadOnly(boolean b) {
		readonly=b;
	}
	
	public void setHasInboxFolder(boolean b) {
		hasInboxFolder=b;
	}
	
	public boolean hasInboxFolder() {
		return hasInboxFolder;
	}
	
	public void setDifferentDefaultFolder(String defaultFolderName) {
		if (defaultFolderName!=null) {
			this.hasDifferentDefaultFolder=true;
			this.defaultFolderName=defaultFolderName;
		} else {
			this.hasDifferentDefaultFolder=false;
			this.defaultFolderName=null;
		}
	}
	
	public boolean hasDifferentDefaultFolder() {
		return hasDifferentDefaultFolder;
	}
	
	public void setMailSession(Session session) {
		this.session=session;
		try {
			session.setProvider(new Provider(Provider.Type.STORE,"imap","com.sonicle.mail.imap.SonicleIMAPStore","Sonicle","1.0"));
			session.setProvider(new Provider(Provider.Type.STORE,"imaps","com.sonicle.mail.imap.SonicleIMAPSSLStore","Sonicle","1.0"));
		} catch (NoSuchProviderException exc) {
			Service.logger.error("Cannot create mail session", exc);
		}
		
	}
	
	public Session getMailSession() {
		return session;
	}
	
	public void setHost(String host) {
		this.mailHost=host;
	}
	
	public String getHost() {
		return mailHost;
	}
	
	public void setPort(int port) {
		this.port=port;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setUsername(String username) {
		this.mailUsername=username;
	}
	
	public String getUsername() {
		return mailUsername;
	}
	
	public void setPassword(String password) {
		this.mailPassword=password;
	}
	
	public String getPassword() {
		return mailPassword;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getReplyTo() {
		return replyTo;
	}
	
	
	public void setSaslRFCImpersonate(String authorizationId, String adminUser, String adminPassword) {
		isImpersonated=true;
		session.getProperties().setProperty("mail.imap.sasl.authorizationid", authorizationId);
		mailUsername=adminUser;
		mailPassword=adminPassword;
	}

	public void setNethImpersonate(String username, String vmailSecret) {
		isImpersonated=true;
		this.mailUsername=username+"*vmail";
		this.mailPassword=vmailSecret;
		this.vmailSecret=vmailSecret;
	}
	
	public boolean isImpersonated() {
		return isImpersonated;
	}

	public void setFolderSent(String folderSent) {
		this.folderSent = folderSent;
	}

	public String getFolderSent() {
		return hasDifferentDefaultFolder?defaultFolderName+folderSeparator+folderSent:folderSent;
	}

	public void setFolderDrafts(String folderDrafts) {
		this.folderDrafts = folderDrafts;
	}

	public String getFolderDrafts() {
		return hasDifferentDefaultFolder?defaultFolderName+folderSeparator+folderDrafts:folderDrafts;
	}

	public void setFolderTrash(String folderTrash) {
		this.folderTrash = folderTrash;
	}

	public String getFolderTrash() {
		return hasDifferentDefaultFolder?defaultFolderName+folderSeparator+folderTrash:folderTrash;
	}

	public void setFolderSpam(String folderSpam) {
		this.folderSpam = folderSpam;
	}

	public String getFolderSpam() {
		return hasDifferentDefaultFolder?defaultFolderName+folderSeparator+folderSpam:folderSpam;
	}

	public void setFolderArchive(String folderArchive) {
		this.folderArchive = folderArchive;
	}

	public String getFolderArchive() {
		return hasDifferentDefaultFolder?defaultFolderName+folderSeparator+folderArchive:folderArchive;
	}

	public void setFolderSeparator(char folderSeparator) {
		this.folderSeparator = folderSeparator;
	}

	public char getFolderSeparator() {
		return folderSeparator;
	}

	public void setHasAnnotations(boolean b) {
		this.hasAnnotations = b;
	}

	public boolean hasAnnotations() {
		return hasAnnotations;
	}

	public void setSkipReplyFolders(String[] skipReplyFolders) {
		this.skipReplyFolders = skipReplyFolders;
	}

	public void setSkipForwardFolders(String[] skipForwardFolders) {
		this.skipForwardFolders = skipForwardFolders;
	}

	public String[] getSharedPrefixes() {
		return sharedPrefixes;
	}

	protected Folder getDefaultFolder() throws MessagingException {
		if (hasDifferentDefaultFolder) return store.getFolder(defaultFolderName);
		else return store.getDefaultFolder();
	}
	
	protected Folder getRealDefaultFolder() throws MessagingException {
		return store.getDefaultFolder();
	}

	public boolean isValid() throws MessagingException {
		if (!validated) {
			if (environment == null) {
				return false;
			}
			validateUser();
		}
		return validated;
	}
	
	public boolean checkStoreConnected() throws MessagingException {
		if (environment == null) {
			return false;
		}
		synchronized (this) {
			if (!isConnected()) {
				return validateUser();
			}
			return true;
		}
	}
	
	private boolean connect() {
		try {
			if (store!=null && store.isConnected()) {
				disconnecting = true;
				store.close();
			}
			
			store=session.getStore(storeProtocol);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
		boolean sucess = true;
		disconnecting = false;
		try {
			
			//warning: trace mode shows credentials
			Service.logger.trace("  accessing "+storeProtocol+"://"+mailUsername+":"+mailPassword+"@"+mailHost+":"+port);
			if (isImpersonated)
				Service.logger.info(" impersonating "+authorizationId);
			
			if (port > 0) {
				store.connect(mailHost, port, mailUsername, mailPassword);
			} else {
				store.connect(mailHost, mailUsername, mailPassword);
			}
			folderSeparator = getDefaultFolder().getSeparator();
			Folder un[] = store.getUserNamespaces("");
			sharedPrefixes = new String[un.length];
			int ix = 0;
			for (Folder sp : un) {
				String s = sp.getFullName();
				//if (s.endsWith(""+folderSeparator)) s=s.substring(0,s.length()-1);
				sharedPrefixes[ix] = s;
				++ix;
			}
			hasAnnotations=((IMAPStore)store).hasCapability("ANNOTATEMORE");
			if (((IMAPStore)store).hasCapability("ID")) {
				Map<String,String> map=((IMAPStore)store).id(null);
				isDovecot=(map!=null && map.containsKey("name") && map.get("name").equalsIgnoreCase("dovecot"));
				//leave hasInboxFolder as it was set in case it's not Dovecot
				if (isDovecot) hasInboxFolder=true;
			}
					
		} catch (MessagingException exc) {
			Service.logger.error("Error connecting to the mail server "+mailHost, exc);
			sucess = false;
		}
		
		return sucess;
		
	}
	
	private boolean isConnected() {
		if (store == null) {
			return false;
		}
		return store.isConnected();
	}
	
	public boolean disconnect() {
		
		try {
			if (store!=null) {
				disconnecting = true;
				((SonicleIMAPStore)store).forceDisconnect();
			}
			
		} catch (MessagingException ex) {
			Service.logger.error("Exception",ex);
		}
		
		return true;
		
	}

	/**
	 * Validate the user login and password through the email server
	 *
	 */
	private boolean validateUser() throws MessagingException {
		validated = connect();
		return validated;
	}
	
	public void createSpecialFolders() throws MessagingException {
		checkCreateFolder(getFolderSent());
		checkCreateFolder(getFolderDrafts());
		checkCreateFolder(getFolderTrash());
		checkCreateFolder(getFolderSpam());
		checkCreateFolder(getFolderArchive());
	}

	
	protected FolderCache createFolderCache(Folder f) throws MessagingException {
		return createFolderCache(f,false);
	}
	
	protected FolderCache createFolderCache(Folder f, boolean volatileInstance) throws MessagingException {
		FolderCache fc;
		synchronized(this) {
			fc=foldersCache.get(f.getFullName());
			if (fc==null) {
				fc = new FolderCache(this, f, ms, environment);
				String fname = fc.getFolderName();
				if (!volatileInstance) foldersCache.put(fname, fc);
			}
		}
		return fc;
	}
	
	protected FolderCache addFoldersCache(FolderCache parent, Folder child) throws MessagingException {
		//Service.logger.trace("adding {} to {}",child.getName(),parent.getFolderName());
		FolderCache fcChild = addSingleFoldersCache(parent, child);
		boolean leaf = fcChild.isStartupLeaf();
		if (!leaf) {
			_loadFoldersCache(fcChild);
		}
		return fcChild;
	}
	
	public void destroyFolderCache(String foldername) throws MessagingException {
		destroyFolderCache(getFolderCache(foldername));
	}
	
	public void destroyFolderCache(FolderCache fc) {
		ArrayList<FolderCache> fcc = fc.getChildren();
		if (fcc != null) {
			FolderCache afc[] = null;
			int len = fcc.size();
			if (len > 0) {
				afc = new FolderCache[len];
				fc.getChildren().toArray(afc);
				for (FolderCache child : afc) {
					destroyFolderCache(child);
				}
			}
		}
		FolderCache fcp = fc.getParent();
		if (fcp != null) {
			fcp.removeChild(fc);
		}
		foldersCache.remove(fc.getFolderName());
		fc.cleanup(true);
		try {
			fc.close();
		} catch (Exception exc) {
		}
	}
	
	public boolean hasFolderCache() {
		return fcRoot!=null;
	}
	
	public void loadFoldersCache(final Object lock, boolean waitLoad) throws MessagingException {
        Folder froot=getDefaultFolder();
        fcRoot=createFolderCache(froot);
		fcRoot.setIsRoot(true);
		Folder children[] = fcRoot.getFolder().list();
		final ArrayList<FolderCache> rootParents = new ArrayList<FolderCache>();
		for (Folder child : children) {
			if (ms.isFolderHidden(this,child.getFullName())) continue;
			if (!fcRoot.hasChild(child.getName())) {
				FolderCache fcc=addSingleFoldersCache(fcRoot,child);
				if (!fcc.isStartupLeaf()) rootParents.add(fcc);
			}
		}
		
		if (hasDifferentDefaultFolder) {
			//check for other shared folders to be added
			Folder rfolders[]=store.getDefaultFolder().list();
			for(int i=0;i<sharedPrefixes.length;++i) {
				for(int j=0;j<rfolders.length;++j) {
					if (rfolders[j].getFullName().equals(sharedPrefixes[i])) {
						FolderCache fcc=addSingleFoldersCache(fcRoot,rfolders[j]);
						rootParents.add(fcc);
					}
				}
			}
		}
		
		cacheLoadThread = new Thread(
				new Runnable() {
					public void run() {
						synchronized (lock) {
							try {
								for (FolderCache fc : rootParents) {
									_loadFoldersCache(fc);
								}
							} catch (MessagingException exc) {
								Service.logger.error("Exception",exc);
							}
						}
					}
				}
		);
		cacheLoadThread.start();
		try {
			if (waitLoad) cacheLoadThread.join();
		} catch(InterruptedException exc) {
			Service.logger.error("Error waiting folder cache load",exc);
		}
	}
	
	private void _loadFoldersCache(FolderCache fc) throws MessagingException {
		Folder f = fc.getFolder();
		Folder children[] = f.list();
		for (Folder child : children) {
			String cname=child.getFullName();
			if (ms.isFolderHidden(this,cname)) continue;
			if (hasDifferentDefaultFolder && cname.equals(fcRoot.getFolderName())) continue;
			FolderCache fcc = addFoldersCache(fc, child);
		}
		//If shared folders, check for same descriptions and in case add login
		if (isSharedFolder(f.getFullName()) && fc.hasChildren()) {
			HashMap<String, ArrayList<FolderCache>> hm = new HashMap<String, ArrayList<FolderCache>>();
			//map descriptions to list of folders
			for (FolderCache child : fc.getChildren()) {
				String desc = child.getDescription();
				ArrayList<FolderCache> al = hm.get(desc);
				if (al == null) {
					al = new ArrayList<FolderCache>();
					al.add(child);
					hm.put(desc, al);
				} else {
					al.add(child);
				}
			}
			//for folders with list>1 change description to all elements
			for (ArrayList<FolderCache> al : hm.values()) {
				if (al.size() > 1) {
					for (FolderCache fcc : al) {
						SharedPrincipal sip = fcc.getSharedInboxPrincipal();
						String user = sip.getUserId();
						fcc.setWebTopUser(user);
						fcc.setDescription(fcc.getDescription() + " [" + user + "]");
					}
				}
			}
		}
	}
	
	protected synchronized FolderCache addSingleFoldersCache(FolderCache parent, Folder child) throws MessagingException {
		String cname = child.getFullName();
		FolderCache fcChild=foldersCache.get(cname);
		if (fcChild==null) {
			fcChild=createFolderCache(child);
			if (parent!=null) {
				fcChild.setParent(parent);
				parent.addChild(fcChild);
			}
			fcChild.setStartupLeaf(isLeaf((IMAPFolder)child));
		}
		
		return fcChild;
	}
	
	
	public FolderCache[] getSharedFoldersCache() throws MessagingException {
		if (fcShared == null) {
			if (sharedPrefixes != null) {
				String sf[] = sharedPrefixes;
				fcShared = new FolderCache[sf.length];
				for (int i = 0; i < sf.length; ++i) {
					fcShared[i] = getFolderCache(sf[i]);
				}
			}
		}
		return fcShared;
	}
	
	public ArrayList<FolderCache> getFavoritesFoldersCache() throws MessagingException {
		 ArrayList<FolderCache> caches = new ArrayList<>();
		MailUserSettings mailUserSettings = ms.getMailUserSettings();
		MailUserSettings.FavoriteFolders favorites = mailUserSettings.getFavoriteFolders();
		
		for(int i =0; i < favorites.size(); i++) {
			MailUserSettings.FavoriteFolder favoriteFolder = favorites.get(i);
			Folder folder = getFolder(favoriteFolder.folderId);
			
			if (folder.exists()) {
				FolderCache folderCache = getFolderCache(folder.getFullName());
					caches.add(folderCache);
				}
		}
	
		return  caches;
	}
	
	public boolean isFavoriteFolder(String folderName) {
			MailUserSettings mailUserSettings = ms.getMailUserSettings();
			MailUserSettings.FavoriteFolders favorites = mailUserSettings.getFavoriteFolders();
			boolean contains = favorites.contains(this.id, folderName);
			return contains;
	}
	
	public FolderCache getRootFolderCache() {
		return fcRoot;
	}
	
	public boolean isRoot(FolderCache fc) {
		return fcRoot!=null && fc==fcRoot;
	}
	
	public Set<Map.Entry<String, FolderCache>> getFolderCacheEntries() {
		return foldersCache.entrySet();
	}
	
	public Collection<FolderCache> getFolderCacheValues() {
		return foldersCache.values();
	}
	
	public Set<String> getFolderCacheKeys() {
		return foldersCache.keySet();
	}
	
	public Quota[] getQuota(String foldername) throws MessagingException {
		return ((IMAPStore)store).getQuota(foldername);
	}
	
	public FolderCache getFolderCache(String foldername) throws MessagingException {
		return foldersCache.get(foldername);
	}
	
	public String getShortFolderName(String fullname) {
		String shortname = fullname;
		if (StringUtils.startsWithIgnoreCase(fullname, folderPrefix)) {
			shortname = fullname.substring(folderPrefix.length());
		}
		return shortname;
	}
	
	public String getLastFolderName(String fullname) {
		String lasttname = fullname;
		if (lasttname.indexOf(folderSeparator) >= 0) {
			lasttname = lasttname.substring(lasttname.lastIndexOf(folderSeparator) + 1);
		}
		return lasttname;
	}
	
	protected String getInboxFolderFullName() {
		String inboxFolder="INBOX";
		try {
			if (hasDifferentDefaultFolder) {
				inboxFolder=getDefaultFolder().getFullName();
				if (hasInboxFolder) inboxFolder+=folderSeparator+"INBOX";
			}
		} catch(MessagingException exc) {
		}
		return inboxFolder;
	}
  

	protected Folder getFolder(String foldername) throws MessagingException {
		return store.getFolder(foldername);
	}
	
	public Folder checkCreateFolder(String foldername) throws MessagingException {
		Folder folder = store.getFolder(foldername);
		if (!folder.exists()) {
			folder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);
		}
		return folder;
	}
	
	public FolderCache checkCreateAndCacheFolder(String fullname) throws MessagingException {
		Folder folder=checkCreateFolder(fullname);
		FolderCache fc=getFolderCache(fullname);
		if (fc==null) {
			FolderCache parent=fcRoot;
			int ix=fullname.lastIndexOf(folderSeparator);
			if (ix>0) parent=getFolderCache(fullname.substring(0,ix));
			fc=addFoldersCache(parent, folder);
		}
		return fc;
	}
	
	public FolderCache checkCreateAndCacheFolders(String fullname) throws MessagingException {
		int x=fullname.indexOf(folderSeparator);
		while(x>0) {
			String fname=fullname.substring(0,x);
			checkCreateAndCacheFolder(fname);
			x=fullname.indexOf(folderSeparator,x+1);
		}
		return checkCreateAndCacheFolder(fullname);
	}
  
	public boolean checkFolder(String foldername) throws MessagingException {
		Folder folder=store.getFolder(foldername);
		return folder.exists();
	}	

	public boolean deleteFolder(String fullname) throws MessagingException {
		return deleteFolder(getFolder(fullname));
	}
	
	public boolean deleteFolder(Folder folder) throws MessagingException {
		for (Folder subfolder : folder.list()) {
			deleteFolder(subfolder);
		}
		try { folder.close(false); } catch(Throwable exc) {}
		boolean retval = folder.delete(true);
		if (retval) {
			destroyFolderCache(folder.getFullName());
		}
		return retval;
	}
	
	private boolean isLeaf(IMAPFolder folder) throws MessagingException {
		String atts[] = folder.getAttributes();
		boolean leaf = true;
		boolean noinferiors = false;
		for (String att : atts) {
			if (att.equals("\\HasChildren")) {
				leaf = false;
			} else if (att.equals("\\Noinferiors")) {
				noinferiors = true;
			}
		}
		if (noinferiors) {
			leaf = true;
		}
		return leaf;
	}
	
	public boolean isSentFolder(String fullname) {
		String lastname = getLastFolderName(fullname);
		String plastname = getLastFolderName(folderSent);
		if (lastname.equals(plastname)) {
			return true;
		}
		return false;
	}
	
	public boolean isTrashFolder(String fullname) {
		String lastname = getLastFolderName(fullname);
		String plastname = getLastFolderName(folderTrash);
		if (lastname.equals(plastname)) {
			return true;
		}
		return false;
	}
	
	public boolean isDraftsFolder(String fullname) {
		String lastname = getLastFolderName(fullname);
		String plastname = getLastFolderName(folderDrafts);
		if (lastname.equals(plastname)) {
			return true;
		}
		return false;
	}
	
	public boolean isArchiveFolder(String fullname) {
		if (folderArchive!=null) {
			String lastname = getLastFolderName(fullname);
			String plastname = getLastFolderName(folderArchive);
			if (lastname.equals(plastname)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isSpamFolder(String fullname) {
		String lastname = getLastFolderName(fullname);
		String plastname = getLastFolderName(folderSpam);
		if (lastname.equals(plastname)) {
			return true;
		}
		return false;
	}
	
	public boolean isSpecialFolder(String foldername) {
		boolean retval = isTrashFolder(foldername)
				|| isDraftsFolder(foldername)
				|| isInboxFolder(foldername)
				|| isSentFolder(foldername)
				|| isArchiveFolder(foldername)
				|| isSpamFolder(foldername);
		return retval;
	}
	
	public boolean isInboxFolder(String foldername) {
		if (!hasDifferentDefaultFolder) {
			//if is root inbox
			if (foldername.equals("INBOX") || 
					//or a shared inbox
					(isUnderSharedFolder(foldername) && LangUtils.charOccurrences(folderSeparator, foldername)==2 && getLastFolderName(foldername).equals("INBOX"))
			) return true;
		} else {
			if (hasInboxFolder)
				return foldername.endsWith(folderSeparator+"INBOX");
			
			return (isDefaultFolder(foldername) ||
					(isUnderSharedFolder(foldername) && LangUtils.charOccurrences(folderSeparator, foldername)==2 && getLastFolderName(foldername).equals("INBOX")));
		}
		return false;
	}
	
	public boolean isDefaultFolder(String foldername) {
		Folder df=null;
		try {
			df=getDefaultFolder();
		} catch(MessagingException exc) {
			Service.logger.error("Error getting default folder",exc);
		}
		if (df!=null) return df.getFullName().equals(foldername);
		return false;
	} 
  
	public boolean isUnderTrashFolder(String foldername) {
		String str=folderTrash+folderSeparator;
		return foldername.startsWith(str);
	}
	
	public boolean isUnderSharedFolder(String foldername) {
		boolean b = false;
		String str = null;
		for (String fn : sharedPrefixes) {
			str = fn + folderSeparator;
			if (foldername.startsWith(str)) {
				b = true;
				break;
			}
		}
		return b;
	}
	
	public boolean isUnderFolder(String parentname, String foldername) {
		if (!parentname.endsWith(""+folderSeparator)) parentname = parentname + folderSeparator;
		return (foldername.startsWith(parentname));
	}
	
	public boolean isSharedFolder(String foldername) {
		boolean b = false;
		for (String fn : sharedPrefixes) {
			if (foldername.equals(fn)) {
				b = true;
				break;
			}
		}
		return b;
	}
	
	public boolean isSharedFolderContainer(IMAPFolder folder) {
		// If passed folder matches a shared prefix, we have taken shared 
		// folders' container folder 
		for(String prefix : sharedPrefixes) {
			if(folder.getFullName().equals(prefix)) return true;
		}
		return false;
	}
	
	public boolean isUnderSentFolder(String foldername) {	
		   String str=getFolderSent()+folderSeparator;
		   return foldername.startsWith(str);
	}  	
	
	public String getMainSharedFolder(String foldername) {
		if (!foldername.endsWith(""+folderSeparator)) foldername+=folderSeparator;
		for (String fn : sharedPrefixes) {
			String str = fn + folderSeparator;
			if (foldername.startsWith(str)) {
				int ix=foldername.indexOf(folderSeparator,str.length());
				if (ix>=0) return foldername.substring(0,ix);
			}
		}
		return null;
	}
	
	
	public void deleteByHeaderValue(String header, String value) throws MessagingException {
		FolderCache fc=getFolderCache(getFolderDrafts());
		fc.open();
		Folder folder=fc.getFolder();
		Message[] oldmsgs=folder.search(new HeaderTerm(header,value));
		if (oldmsgs!=null && oldmsgs.length>0) {
			for(Message m: oldmsgs) m.setFlag(Flags.Flag.DELETED, true);
			folder.expunge();
		}
	}
	
	public boolean emptyFolder(String fullname) throws MessagingException {
		FolderCache fc = getFolderCache(fullname);
		for (Folder child: fc.getFolder().list()) {
			deleteFolder(child);
		}
		fc.deleteAllMessages();
	  
		return true;
	}
	
	public FolderCache trashFolder(String fullname) throws MessagingException {
		if (!isUnderTrashFolder(fullname)) {
			String foldertrash=getFolderTrash();
			if (isUnderSharedFolder(fullname)) {
				String mainfolder=getMainSharedFolder(fullname);
				if (mainfolder!=null) {
					foldertrash = mainfolder + folderSeparator + getLastFolderName(foldertrash);
				}
			}
			return moveFolder(fullname,foldertrash);
		}
		else {
			deleteFolder(fullname);
			return null;
		}
	}
	
	public FolderCache moveFolder(String source, String dest) throws MessagingException {
		Folder oldfolder = getFolder(source);
		String oldname = oldfolder.getName();
		//System.out.println("moveFolder "+source+" -> "+dest);
		Folder newfolder;
		if (dest != null && dest.trim().length() > 0) {
			String newname = dest + folderSeparator + oldname;
			newfolder = getFolder(newname);
		} else {
			if (hasDifferentDefaultFolder) {
				String prefix=getDefaultFolder().getFullName();
				String newname=oldname;
				if (prefix!=null) newname=prefix+folderSeparator+newname;
				newfolder=getFolder(newname);
			} else {
				String newname = oldname;
				if (folderPrefix != null) {
					newname = folderPrefix + newname;
				}
				newfolder = getFolder(newname);
			}
		}
		FolderCache fcsrc = getFolderCache(source);
		boolean done = oldfolder.renameTo(newfolder);
		if (!done) {
			throw new MessagingException("Permission denied");
		} else {
			if (fcsrc != null) destroyFolderCache(fcsrc);
			if (dest != null) {
				FolderCache tfc = getFolderCache(newfolder.getParent().getFullName());
				return addFoldersCache(tfc, newfolder);
			} else {
				return addFoldersCache(fcRoot, newfolder);
			}
		}
	}
	
	public String renameFolder(String orig, String newname) throws MessagingException {
		FolderCache fc = getFolderCache(orig);
		if (fc.getFolder().getName().equals(newname)) return orig;
		
		FolderCache fcparent = fc.getParent();
		Folder oldfolder = fc.getFolder();
		//we need to close the source folder or exception will not be thrown in case of error
		fc.close();
		Folder newfolder = fcparent.getFolder().getFolder(newname);
		boolean done = oldfolder.renameTo(newfolder);
		if (!done) {
			throw new MessagingException("Rename failed");
		}
		//destroy folder cache only if rename was done
		destroyFolderCache(fc);
		
		//trick for Dovecot on NethServer: under shared folders, create and destroy a fake folder
		//or rename will not work correctly
		if (isUnderSharedFolder(newfolder.getFullName())) {
			Map<String,String> map=((IMAPStore)store).id(null);
			if (map!=null && map.containsKey("name") && map.get("name").equalsIgnoreCase("dovecot")) {
				String trickName="_________"+System.currentTimeMillis();
				Folder trickFolder=fcparent.getFolder().getFolder(trickName);
				try {
					trickFolder.create(Folder.READ_ONLY);
					trickFolder.delete(true);
				} catch(MessagingException exc) {

				}
			}
		}
		
		addFoldersCache(fcparent, newfolder);
		return newfolder.getFullName();
	}
	
	public void cleanup() {
		Service.logger.trace("clean up account "+id);
		if (fcRoot != null) {
			fcRoot.cleanup(true);
		}
		fcRoot = null;
		for (FolderCache fc : foldersCache.values()) {
			fc.cleanup(true);
		}
		foldersCache.clear();
		foldersCache=null;
		try {
			Service.logger.trace("-disconnecting imap");
			disconnect();
			Service.logger.trace("-done");
		} catch (Exception e) {
			Service.logger.error("Exception",e);
		}
		this.ms=null;
		validated = false;
	}
	
	public String normalizeName(String name) throws MessagingException {
		String sep = "" + folderSeparator;
		while (name.contains(sep)) {
			name = name.replace(sep, "_");
		}
		return name;
	}
	
	public SonicleIMAPFolder.RecursiveSearchResult recursiveSearchByMessageID(String foldername, String id) throws MessagingException {
		SonicleIMAPFolder f = (SonicleIMAPFolder)getFolder(foldername);
		return f.recursiveSearchByMessageID(id, skipReplyFolders);
	}
	
	public void removeFolderSharing(String foldername, String acluser, boolean recursive) throws MessagingException {
		Folder folders[]=null;
		if (StringUtils.isEmpty(foldername)) {
		   folders=getDefaultFolder().list();
		} else {
			folders=new IMAPFolder[1];
			folders[0]=(IMAPFolder)getFolderCache(foldername).getFolder();
		}
		for(Folder folder: folders)
			_removeFolderSharing((IMAPFolder)folder, acluser, recursive);
	}
	
	private void _removeFolderSharing(IMAPFolder folder, String acluser, boolean recursive) throws MessagingException {
		if(isSharedFolderContainer(folder)) return; // Skip shared folder container
		
		if (recursive && !isLeaf(folder)) {
			Folder children[]=folder.list();
			for(Folder child: children) {
				_removeFolderSharing((IMAPFolder)child, acluser, true);
			}
		}
		try {
			folder.removeACL(acluser);
		} catch(MessagingException exc) {
			Service.logger.error("Error removing acl on folder "+folder.getFullName(),exc);
		}
		
	}
	
	public void setFolderSharing(String foldername, String acluser, String rights, boolean recursive) throws MessagingException {
		Folder folders[]=null;
		if (StringUtils.isEmpty(foldername)) {
		   folders=getDefaultFolder().list();
		} else {
			folders=new IMAPFolder[1];
			folders[0]=(IMAPFolder)getFolderCache(foldername).getFolder();
		}
		for(Folder folder: folders)
			_setFolderSharing((IMAPFolder)folder, acluser, rights, recursive);
	}
	
	private void _setFolderSharing(IMAPFolder folder, String acluser, String rights, boolean recursive) throws MessagingException {
		if(isSharedFolderContainer(folder)) return; // Skip shared folder container
		
		if (recursive && !isLeaf(folder)) {
			Folder children[]=folder.list();
			for(Folder child: children) {
				_setFolderSharing((IMAPFolder)child, acluser, rights, true);
			}
		}
		try {
			folder.removeACL(acluser);
			folder.addACL(new ACL(acluser,new Rights(rights)));
		} catch(MessagingException exc) {
			Service.logger.error("Error setting acl on folder "+folder.getFullName(),exc);
		}
	}
	
	private Comparator<Folder> wtufComp=new Comparator<Folder>() {

		public int compare(Folder f1, Folder f2) {
			int ret = 0;
			try {
				FolderCache fc1 = getFolderCache(f1.getFullName());
				FolderCache fc2 = getFolderCache(f2.getFullName());
				String u1 = fc1.getWebTopUser();
				String u2 = fc2.getWebTopUser();
				ret = u1.compareTo(u2);
			} catch (MessagingException exc) {
				Service.logger.error("Exception",exc);
			}
			return ret;
		}
	};
	
	public Comparator<Folder> getWebTopUserFolderComparator() {
		return wtufComp;
	}
	
	private Comparator<Folder> fdComp=new Comparator<Folder>() {
		
		public int compare(Folder f1, Folder f2) {
			int ret = 0;
			try {
				FolderCache fc1 = getFolderCache(f1.getFullName());
				FolderCache fc2 = getFolderCache(f2.getFullName());
				String desc1 = fc1.getDescription();
				String desc2 = fc2.getDescription();
				ret = desc1.compareTo(desc2);
			} catch (MessagingException exc) {
				Service.logger.error("Exception",exc);
			}
			return ret;
		}
	};
			
	public Comparator<Folder> getDescriptionFolderComparator() {
		return fdComp;
	}
}
	
