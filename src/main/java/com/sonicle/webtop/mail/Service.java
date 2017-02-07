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

import com.google.gson.Gson;
import com.sonicle.webtop.core.app.PrivateEnvironment;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.commons.LangUtils;
import java.nio.*;
import java.nio.channels.*;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.RegexUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.MapItemList;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.mail.imap.SonicleIMAPFolder;
import com.sonicle.mail.imap.SonicleIMAPMessage;
import com.sonicle.mail.sieve.*;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.Principal;
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.model.InternetRecipient;
import com.sonicle.webtop.core.bol.model.SharePerms;
import com.sonicle.webtop.core.bol.model.SharePermsElements;
import com.sonicle.webtop.core.bol.model.SharePermsFolder;
import com.sonicle.webtop.core.bol.model.SharePermsRoot;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.*;
import com.sonicle.webtop.core.sdk.UserProfile.Data;
import com.sonicle.webtop.core.servlet.ServletHelper;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.mail.bol.ORule;
import com.sonicle.webtop.mail.bol.ONote;
import com.sonicle.webtop.mail.bol.OScan;
import com.sonicle.webtop.mail.bol.OUserMap;
import com.sonicle.webtop.mail.bol.OVacation;
import com.sonicle.webtop.mail.bol.js.JsAttachment;
import com.sonicle.webtop.mail.bol.js.JsFilter;
import com.sonicle.webtop.mail.bol.js.JsMessage;
import com.sonicle.webtop.mail.bol.js.JsQuickPart;
import com.sonicle.webtop.mail.bol.js.JsRecipient;
import com.sonicle.webtop.mail.bol.js.JsRule;
import com.sonicle.webtop.mail.bol.js.JsSharing;
import com.sonicle.webtop.mail.bol.js.JsSort;
import com.sonicle.webtop.mail.bol.model.Identity;
import com.sonicle.webtop.mail.dal.RuleDAO;
import com.sonicle.webtop.mail.dal.NoteDAO;
import com.sonicle.webtop.mail.dal.ScanDAO;
import com.sonicle.webtop.mail.dal.UserMapDAO;
import com.sonicle.webtop.mail.dal.VacationDAO;
// TODO: Fix imported classes
//import com.sonicle.webtop.Mailcard;
//import com.sonicle.webtop.EditingSession;
//import com.sonicle.webtop.bol.OServiceSetting;
//import com.sonicle.webtop.bol.OUser;
//import com.sonicle.webtop.bol.OWorkgroup;
//import com.sonicle.webtop.bol.WebTopSettings;
//import com.sonicle.webtop.bol.js.JsSharingRight;
//import com.sonicle.webtop.contacts.*;
//import com.sonicle.webtop.dal.WebTopDb;
//import com.sonicle.webtop.mail.bol.JsMailcard;
//import com.sonicle.webtop.mail.bol.JsQuickPart;
//import com.sonicle.webtop.mail.dal.InboxMailFiltersDb;
//import com.sonicle.webtop.mail.dal.SentMailFiltersDb;
//import com.sonicle.webtop.profiledata.ProfileDataProviderBase;
//import com.sonicle.webtop.profiledata.ProfilePersonalInfo;
//import com.sonicle.webtop.setting.SettingsManager;
//import com.sonicle.webtop.util.*;
//import com.sonicle.webtop.vfs.VFSService;
import com.sun.mail.imap.*;
import com.sun.mail.util.PropUtil;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.parameter.PartStat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;

public class Service extends BaseService {
	
	boolean imapDebug=false;
	public final static Logger logger = WT.getLogger(Service.class);
	
	class WebtopFlag {
		String label;
		String tbLabel;
		
		WebtopFlag(String label, String tbLabel) {
			this.label=label;
			this.tbLabel=tbLabel;
		}
		
	}
	
    public WebtopFlag[] webtopFlags={
        new WebtopFlag("red","$label1"),
        new WebtopFlag("blue","$label4"),
        new WebtopFlag("yellow",null),
        new WebtopFlag("green","$label3"),
        new WebtopFlag("orange","$label2"),
        new WebtopFlag("purple","$label5"),
        new WebtopFlag("black",null),
        new WebtopFlag("gray",null),
        new WebtopFlag("white",null),
        new WebtopFlag("brown",null),
        new WebtopFlag("azure",null),
        new WebtopFlag("pink",null),
        new WebtopFlag("complete",null)
	};
	
	public String allFlagStrings[];
	
	class ThunderbirdFlag {
		String label;
		int colorindex;
		
		ThunderbirdFlag(String label, int colorindex) {
			this.label=label;
			this.colorindex=colorindex;
		}
	}
	
/*    public ThunderbirdFlag tbFlagStrings[]={
        new ThunderbirdFlag("$label1",0),	//red
        new ThunderbirdFlag("$label2",4),	//orange
        new ThunderbirdFlag("$label3",3),	//green
        new ThunderbirdFlag("$label4",1),	//blue
        new ThunderbirdFlag("$label5",5)	//purple
		
    };*/
	
	public static Flags flagsAll = new Flags();
	public static Flags oldFlagsAll = new Flags();
	public static Flags tbFlagsAll=new Flags();
	public static HashMap<String, Flags> flagsHash = new HashMap<String, Flags>();
	public static HashMap<String, Flags> oldFlagsHash = new HashMap<String, Flags>();
	public static HashMap<String,Flags> tbFlagsHash=new HashMap<String,Flags>();
	private static String sflagNote="mailnote";
	private static String sflagArchived="$Archived";
	public static Flags flagNote=new Flags(sflagNote);
	public static Flags flagArchived=new Flags(sflagArchived);
	public static Flags flagFlagged=new Flags(Flags.Flag.FLAGGED);
	
	private FetchProfile FP = new FetchProfile();
	private FetchProfile draftsFP=new FetchProfile();
	
	private boolean sortfolders=false;
	
	private boolean hasDifferentDefaultFolder=false;
	
	public static final String HEADER_SONICLE_FROM_DRAFTER="Sonicle-from-drafter";	
	
	static String startpre = "<PRE>";
	static String endpre = "</PRE>";
//	private static String webtopTextMessage
//			= "This email has been sent through Sonicle WebMail system [ http://www.sonicle.com ]";
//	private static String webtopHTMLMessage = "This email has been sent through Sonicle WebMail system [ <A HREF='http://www.sonicle.com'>http://www.sonicle.com</A> ]";
//	private static String unwantedTags[] = {"style"};

	private MailManager mailManager;
	private Session session;
	private Store store;
	private String storeProtocol;
	private boolean disconnecting = false;
	private String sharedPrefixes[] = null;
	private char folderSeparator = 0;
	private String folderPrefix = null;
	
	private PrivateEnvironment environment = null;
	private MailUserProfile mprofile;
	private MailServiceSettings ss = null;
	private MailUserSettings us = null;
	private CoreUserSettings cus = null;
	private boolean validated = false;
	private int newMessageID = 0;
	private MailFoldersThread mft;
	private Sieve sieve = null;
	
	private HashMap<String, FolderCache> foldersCache = new HashMap<String, FolderCache>();
	private FolderCache fcRoot = null;
	private FolderCache[] fcShared = null;
	private FolderCache fcProvided = null;
	
	private String skipReplyFolders[] = null;
	private String skipForwardFolders[] = null;
	
	private static ArrayList<String> inlineableMimes = new ArrayList<String>();
	
	private HashMap<Long, ArrayList<Attachment>> msgattach = new HashMap<Long, ArrayList<Attachment>>();
	private HashMap<Long, ArrayList<Attachment>> msgcloudattach = new HashMap<Long, ArrayList<Attachment>>();
	private ArrayList<Attachment> emptyAttachments = new ArrayList<Attachment>();
	
	private AdvancedSearchThread ast = null;
	
	static {
		inlineableMimes.add("image/gif");
		inlineableMimes.add("image/jpeg");
		inlineableMimes.add("image/png");
//      inlineableMimes.add("image/tiff");
//      inlineableMimes.add("text/plain");
//      inlineableMimes.add("message/rfc822");
	}
	
	@Override
	public void initialize() {
		
		ArrayList<String> allFlagsArray=new ArrayList<String>();
		for(WebtopFlag fs: webtopFlags) {
			allFlagsArray.add(fs.label);
			String oldfs="flag"+fs.label;
			flagsAll.add(fs.label);
			if (fs.tbLabel!=null) tbFlagsAll.add(fs.tbLabel);
			oldFlagsAll.add(oldfs);
			Flags flags=new Flags();
			flags.add(fs.label);
			flagsHash.put(fs.label, flags);
			flags=new Flags();
			flags.add(oldfs);
			oldFlagsHash.put(fs.label, flags);
			if (fs.tbLabel!=null) {
				Flags tbFlags=new Flags();
				tbFlags.add(fs.tbLabel);
				tbFlagsHash.put(fs.label, tbFlags);
			}
		}
		for(WebtopFlag fs: webtopFlags) {
			if (fs.tbLabel!=null) allFlagsArray.add(fs.tbLabel);
		}	  
		for(WebtopFlag fs: webtopFlags) {
			allFlagsArray.add("flag"+fs.label);
		}	  
		allFlagStrings=new String[allFlagsArray.size()];
		allFlagsArray.toArray(allFlagStrings);

		this.environment = getEnv();
		
		mailManager=(MailManager)WT.getServiceManager(SERVICE_ID);
		cus = new CoreUserSettings(getEnv().getProfileId());

		UserProfile profile = getEnv().getProfile();
		ss = new MailServiceSettings(SERVICE_ID,getEnv().getProfile().getDomainId());
		us = new MailUserSettings(profile.getId(),ss);
		mprofile = new MailUserProfile(environment,this);
		fcProvided = new FolderCache(this, environment);
		
		folderPrefix = mprofile.getFolderPrefix();
		storeProtocol = mprofile.getMailProtocol();
		
		FP.add(FetchProfile.Item.ENVELOPE);
		FP.add(FetchProfile.Item.FLAGS);
		FP.add(FetchProfile.Item.CONTENT_INFO);
		FP.add(UIDFolder.FetchProfileItem.UID);
		FP.add("Message-ID");
		FP.add("X-Priority");
		draftsFP.add(FetchProfile.Item.ENVELOPE);
		draftsFP.add(FetchProfile.Item.FLAGS);
		draftsFP.add(FetchProfile.Item.CONTENT_INFO);
		draftsFP.add(UIDFolder.FetchProfileItem.UID);
		draftsFP.add("Message-ID");
		draftsFP.add("X-Priority");
		draftsFP.add(HEADER_SONICLE_FROM_DRAFTER);
		if (hasDocumentArchiving()) {
			FP.add("X-WT-Archived");
			draftsFP.add("X-WT-Archived");
		}
		
		sortfolders=ss.isSortFolder();
		
		hasDifferentDefaultFolder=us.getDefaultFolder()!=null;
		
		session=WT.getMailSession(profile.getDomainId());
		//session=Session.getDefaultInstance(props, null);
		session.setDebug(imapDebug);

		try {
			session.setProvider(new Provider(Provider.Type.STORE,"imap","com.sonicle.mail.imap.SonicleIMAPStore","Sonicle","1.0"));
			session.setProvider(new Provider(Provider.Type.STORE,"imaps","com.sonicle.mail.imap.SonicleIMAPSSLStore","Sonicle","1.0"));
		} catch (NoSuchProviderException exc) {
			logger.error("Cannot create mail store for {}", profile.getUserId(), exc);
		}
		
		//TODO initialize user for first time use
		//SettingsManager sm = wta.getSettingsManager();
		//boolean initUser = LangUtils.value(sm.getUserSetting(profile, "mail", com.sonicle.webtop.Settings.INITIALIZE), false);
		//if(initUser) initializeUser(profile);

		mft = new MailFoldersThread(this, environment);
		mft.setCheckAll(mprofile.isScanAll());
		mft.setSleepInbox(mprofile.getScanSeconds());
		mft.setSleepCycles(mprofile.getScanCycles());
		try {
			mft.abort();
			checkStoreConnected();

			//prepare special folders if not existant
			if (ss.isAutocreateSpecialFolders()) {
				checkCreateFolder(mprofile.getFolderSent());
				checkCreateFolder(mprofile.getFolderDrafts());
				checkCreateFolder(mprofile.getFolderTrash());
				checkCreateFolder(mprofile.getFolderSpam());
			}
			
			skipReplyFolders = new String[]{
				mprofile.getFolderDrafts(),
				mprofile.getFolderSent(),
				mprofile.getFolderSpam(),
				mprofile.getFolderTrash()
			};
			skipForwardFolders = new String[]{
				mprofile.getFolderSpam(),
				mprofile.getFolderTrash()
			};
			
			loadFoldersCache();
			if (!imapDebug) mft.start();

			//check sieve script
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						UserProfile profile = environment.getProfile();
						Sieve sieve = getSieve();
						
						SieveScript[] scripts=sieve.getScripts();
						boolean rebuildScript=scripts.length==0;
						boolean foundWebtopScript=false;
						if (scripts.length>0) {
							for(SieveScript sscript:scripts) {
								if (sscript.getName().equals("\"webtop\"")) {
									foundWebtopScript=true;
									String script=sieve.getScript("webtop");
									if (script!=null) {
										//script=script.toLowerCase();
										if (!script.contains(SieveScriptGenerator.getVersionComment())) rebuildScript=true;
									}
								}
							}
							if (!foundWebtopScript) rebuildScript=true;
						}
						
						if (rebuildScript) {
							logger.debug("No Sieve scripts for {}. Creating from database.", profile.getUserId());
							Connection con=null;
							try {
								con=getConnection();
								MailRules rules=getMailRules(con,"rules",profile.getUserId(), profile.getDomainId());
								if (rules!=null) sieve.saveScript(rules, true);
							} catch(SQLException exc) {
								logger.error("Error getting connection while trying to save Sieve script", exc);
							} finally {
								DbUtils.closeQuietly(con);
							}
						}						
					} catch (Exception exc) {
						Service.logger.error("Exception",exc);
					}
				}
			});
			t.start();
			
			setSharedSeen(us.isSharedSeen());
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public MailServiceSettings getMailServiceSettings() {
		return this.ss;
	}
	
	public MailUserSettings getMailUserSettings() {
		return this.us;	
	}
	
	public CoreUserSettings getCoreUserSettings() {
		return this.cus;
	}
	
	public boolean hasDifferentDefaultFolder() {
		return hasDifferentDefaultFolder;
	}
	
	
	public Session getMailSession() {
		return this.session;
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
		UserProfile profile = environment.getProfile();
		synchronized (profile) {
			if (!isConnected()) {
				return validateUser();
			}
			return true;
		}
	}
	
	private boolean connect() {
		UserProfile profile = environment.getProfile();
		int port = mprofile.getMailPort();
		String mailHost = mprofile.getMailHost();
		String mailUsername = mprofile.getMailUsername();
		String mailPassword = mprofile.getMailPassword();
		String authorizationId=mailUsername;
		boolean isImpersonated=profile.getPrincipal().isImpersonated();
		try {
			if (store!=null && store.isConnected()) {
				disconnecting = true;
				store.close();
			}
			if (isImpersonated) {
				session.getProperties().setProperty("mail.imap.sasl.authorizationid", authorizationId);
				mailUsername=ss.getAdminUser();
				mailPassword=ss.getAdminPassword();
			}
			
			store=session.getStore(storeProtocol);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
		boolean sucess = true;
		disconnecting = false;
		try {
			
			//warning: debug mode shows credentials
			//Service.logger.debug("  accessing "+storeProtocol+"://"+mailUsername+":"+mailPassword+"@"+mailHost+":"+port);
			Service.logger.info("  accessing "+storeProtocol+"://"+mailUsername+":"+mailPassword+"@"+mailHost+":"+port);
			//Service.logger.info("  accessing "+storeProtocol+"://"+mailUsername+":******@"+mailHost+":"+port);
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
		} catch (MessagingException exc) {
			logger.error("Error connecting to the mail server", exc);
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
			if (store.isConnected()) {
				disconnecting = true;
				store.close();
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

	/**
	 * Logout
	 *
	 */
	public boolean logout() {
		// Disconnect from email server
		cleanup();
		return true;
	}
	
	public FetchProfile getMessageFetchProfile() {
		return FP;
	}

	public MailFoldersThread getMailFoldersThread() {
		return mft;
	}

	public Sieve getSieve() {
		String mailHost = mprofile.getMailHost();
		String mailUsername = mprofile.getMailUsername();
		String mailPassword = mprofile.getMailPassword();
		if (mailHost == null || mailUsername == null || mailPassword == null) {
			return null;
		}
		if (sieve != null) {
			sieve.setHostname(mailHost);
			sieve.setUsername(mailUsername);
			sieve.setPassword(mailPassword);
		} else {
			sieve = new Sieve(mailHost, mailUsername, mailPassword);
		}
		return sieve;
		
	}
	
	public char getFolderSeparator() throws MessagingException {
		return folderSeparator;
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
	
	public FolderCache getRootFolderCache() {
		return fcRoot;
	}
	
	public Set<Entry<String, FolderCache>> getFolderCacheEntries() {
		return foldersCache.entrySet();
	}
	
	public FolderCache getFolderCache(String foldername) throws MessagingException {
		return foldersCache.get(foldername);
	}
	
	public void setFolderPrefix(String prefix) {
		folderPrefix = prefix;
	}
	
	public String getFolderPrefix() {
		return folderPrefix;
	}
	
	public void archiveMessages(FolderCache from, long nuids[], String idcategory, String idsubcategory) throws MessagingException, FileNotFoundException, IOException {
		UserProfile profile = environment.getProfile();
		String archiveto = ss.getArchivePath();
		for (long nuid: nuids) {
			Message msg = from.getMessage(nuid);
			
			String id = getMessageID(msg);
			if (id.startsWith("<")) {
				id = id.substring(1, id.length() - 1);
			}
			id = id.replaceAll("\\\\", "_");
			id = id.replaceAll("/", "_");
			String filename = archiveto + "/" + id + ".eml";
			String txtname = archiveto + "/" + id + ".txt";
			File file = new File(filename);
			File txtfile = new File(txtname);
			//Only if spool file does not exists
			if (!file.exists()) {
				
				String emailfrom = "nomail@nodomain.it";
				Address a[] = msg.getFrom();
				if (a != null && a.length > 0) {
					InternetAddress sender = ((InternetAddress) a[0]);
					emailfrom = sender.getAddress();
				}
				String emailto = "nomail@nodomain.it";
				a = msg.getRecipients(Message.RecipientType.TO);
				if (a != null && a.length > 0) {
					InternetAddress to = ((InternetAddress) a[0]);
					emailto = to.getAddress();
				}
				String subject = msg.getSubject();
				java.util.Date date = msg.getReceivedDate();
				java.util.Calendar cal = java.util.Calendar.getInstance();
				cal.setTime(date);
				int dd = cal.get(java.util.Calendar.DAY_OF_MONTH);
				int mm = cal.get(java.util.Calendar.MONTH) + 1;
				int yyyy = cal.get(java.util.Calendar.YEAR);
				String sdd = dd < 10 ? "0" + dd : "" + dd;
				String smm = mm < 10 ? "0" + mm : "" + mm;
				String syyyy = "" + yyyy;
				
				FileOutputStream fos = new FileOutputStream(file);
				msg.writeTo(fos);
				fos.close();
				
				PrintWriter pw = new PrintWriter(txtfile);
				pw.println(emailfrom);
				pw.println(emailto);
				pw.println(subject);
				pw.println(sdd + "/" + smm + "/" + syyyy);
				pw.println(profile.getUserId());
				pw.println(idcategory);
				pw.println(idsubcategory);
				pw.close();
			}
		}
		from.markArchivedMessages(nuids);
	}
	
	public void moveMessages(FolderCache from, FolderCache to, long uids[]) throws MessagingException {
		from.moveMessages(uids, to);
	}
	
	public void copyMessages(FolderCache from, FolderCache to, long uids[]) throws MessagingException, IOException {
		from.copyMessages(uids, to);
	}
	
	public void deleteMessages(FolderCache from, long uids[]) throws MessagingException {
		from.deleteMessages(uids);
	}
	
	public void flagMessages(FolderCache from, long uids[], String flag) throws MessagingException {
		from.flagMessages(uids, flag);
	}
	
	public void clearMessagesFlag(FolderCache from, long uids[]) throws MessagingException {
		from.clearMessagesFlag(uids);
	}
	
	public void setMessagesSeen(FolderCache from, long uids[]) throws MessagingException {
		from.setMessagesSeen(uids);
	}
	
	public void setMessagesUnseen(FolderCache from, long uids[]) throws MessagingException {
		from.setMessagesUnseen(uids);
	}
	
	public String getShortFolderName(String fullname) {
		String shortname = fullname;
		if (fullname.startsWith(folderPrefix.toLowerCase()) || fullname.startsWith(folderPrefix.toUpperCase())) {
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
			destroyFolderCache(getFolderCache(folder.getFullName()));
		}
		return retval;
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
		if (!isUnderTrashFolder(fullname)) return moveFolder(fullname,mprofile.getFolderTrash());
		else {
			deleteFolder(fullname);
			return null;
		}
	}
	
	public FolderCache moveFolder(String source, String dest) throws MessagingException {
		Folder oldfolder = getFolder(source);
		String oldname = oldfolder.getName();
		Folder newfolder;
		if (dest != null && dest.trim().length() > 0) {
			String newname = dest + getFolderSeparator() + oldname;
			newfolder = getFolder(newname);
		} else {
			String prefix = getFolderPrefix();
			String newname = oldname;
			if (prefix != null) {
				newname = prefix + newname;
			}
			newfolder = getFolder(newname);
		}
		FolderCache fcsrc = getFolderCache(source);
		if (fcsrc != null) {
			destroyFolderCache(fcsrc);
		}
		boolean done = oldfolder.renameTo(newfolder);
		if (done) {
			if (dest != null) {
				FolderCache tfc = getFolderCache(newfolder.getParent().getFullName());
				return addFoldersCache(tfc, newfolder);
			} else {
				return addFoldersCache(fcRoot, newfolder);
			}
		}
		return null;
	}
	
	public String renameFolder(String orig, String newname) throws MessagingException {
		FolderCache fc = getFolderCache(orig);
		FolderCache fcparent = fc.getParent();
		Folder oldfolder = fc.getFolder();
		destroyFolderCache(fc);
		Folder newfolder = fcparent.getFolder().getFolder(newname);
		boolean done = oldfolder.renameTo(newfolder);
		if (!done) {
			throw new MessagingException("Rename failed");
		}
		addFoldersCache(fcparent, newfolder);
		return newfolder.getFullName();
	}
	
	private InternetAddress getInternetAddress(String email) throws UnsupportedEncodingException, AddressException {
	  email=email.trim();
	  if (!email.startsWith("\"")) {
		  int ix=email.indexOf("<");
		  if (ix>=0) {
			  String personal=email.substring(0,ix).trim();
			  String address=email.substring(ix).trim();
			  email="\""+personal+"\" "+address;
		  }
	  }
	  InternetAddress ia[]=InternetAddress.parse(email, false);
	  //build an InternetAddress with UTF8 personal, if present
	  InternetAddress retia=MailUtils.buildInternetAddress(ia[0].getAddress(),ia[0].getPersonal());
	  return retia;
	}
	
	public Exception sendReceipt(String from, String to, String subject, String body) {
		return sendTextMsg(from, from, new String[]{to}, null, null, "Receipt: " + subject, body);
	}
	
	private Exception sendTextMsg(String fromAddr, String rplyAddr, String[] toAddr,
			String[] ccAddr,
			String[] bccAddr, String subject, String body) {
		
		return sendTextMsg(fromAddr,
				rplyAddr, toAddr, ccAddr, bccAddr, subject, body, null);
		
	}
	
	private Exception sendTextMsg(String fromAddr, String rplyAddr, String[] toAddr, String[] ccAddr, String[] bccAddr,
			String subject, String body, List<JsAttachment> attachments) {
		
		SimpleMessage smsg = new SimpleMessage(0);

		//set the TO recipients
		smsg.addTo(toAddr);

		//set the CC recipients
		smsg.addCc(ccAddr);

		//set BCC recipients
		smsg.addBcc(bccAddr);

		//set Reply To address
		if (rplyAddr != null && rplyAddr.length() > 0) {
			smsg.setReplyTo(rplyAddr);

			//set the subject
		}
		smsg.setSubject(subject);

		//set the content
		smsg.setContent(body);
		
		return sendMsg(fromAddr, smsg, attachments);
		
	}
	
	public boolean sendMsg(Message msg) {
		UserProfile profile = environment.getProfile();
		String sentfolder = mprofile.getFolderSent();
		try {
			Transport.send(msg);
			saveSent(msg, sentfolder);
			return true;
			
		} catch (Exception ex) {
			Service.logger.error("Exception",ex);
			return false;
		}
		
	}
	
	public Exception sendMsg(String from, SimpleMessage smsg, List<JsAttachment> attachments) {
		UserProfile profile = environment.getProfile();
		String sentfolder = mprofile.getFolderSent();
		Identity ident = smsg.getFrom();
		if (ident != null ) {
			String mainfolder=ident.getMainFolder();
			if (mainfolder != null && mainfolder.trim().length() > 0) {
				String newsentfolder = mainfolder + folderSeparator + getLastFolderName(sentfolder);
				try {
					Folder folder = getFolder(newsentfolder);
					if (folder.exists()) {
						sentfolder = newsentfolder;
					}
				} catch (MessagingException exc) {
					logger.error("Error on identity {}/{} Sent Folder", profile.getUserId(), ident.getEmail(), exc);
				}
			}
		}
		Exception retexc = null;
		Message msg = null;
		try {
			msg = createMessage(from, smsg, attachments, false);
			Transport.send(msg);
		} catch (Exception ex) {
			Service.logger.error("Exception",ex);
			retexc = ex;
		}

		//if sent succesful, save outgoing message
		if (retexc == null && msg != null) {
			retexc = saveSent(msg, sentfolder);
			
		}
		return retexc;
		
	} //end sendMsg, SimpleMessage version

	public Exception sendMessage(SimpleMessage msg, List<JsAttachment> attachments) {
		UserProfile profile = environment.getProfile();
		String sender = profile.getEmailAddress();
		String name = profile.getDisplayName();
		String replyto = mprofile.getReplyTo();
		
		if (msg.getFrom() != null) {
			Identity from = msg.getFrom();
			sender = from.getEmail();
			name = from.getDisplayName();
			replyto = sender;
		}
		
		msg.setReplyTo(replyto);
		
		if (name != null && name.length() > 0) {
			sender = name + " <" + sender + ">";
			
		}
		Exception retexc = null;
		
		if (attachments.size() == 0 && msg.getAttachments() == null) {
			retexc = sendMsg(sender, msg, null);
		} else {
			
			retexc = sendMsg(sender, msg, attachments);
			
			if (retexc==null) clearAttachments(msg.getId());
		}

		return retexc;
	}
	
	public Message createMessage(String from, SimpleMessage smsg, List<JsAttachment> attachments, boolean tosave) throws Exception {
		MimeMessage msg = null;
		boolean success = true;
		
		String[] to = SimpleMessage.breakAddr(smsg.getTo());
		String[] cc = SimpleMessage.breakAddr(smsg.getCc());
		String[] bcc = SimpleMessage.breakAddr(smsg.getBcc());
		String replyTo = smsg.getReplyTo();
		
		msg = new MimeMessage(session);
		msg.setFrom(getInternetAddress(from));
		InternetAddress ia = null;

		//set the TO recipient
		for (int q = 0; q < to.length; q++) {
//        Service.logger.debug("to["+q+"]="+to[q]);
			to[q] = to[q].replace(',', ' ');
			try {
				ia = getInternetAddress(to[q]);
			} catch (AddressException exc) {
				throw new AddressException(to[q]);
			}
			msg.addRecipient(Message.RecipientType.TO, ia);
		}

		//set the CC recipient
		for (int q = 0; q < cc.length; q++) {
			cc[q] = cc[q].replace(',', ' ');
			try {
				ia = getInternetAddress(cc[q]);
			} catch (AddressException exc) {
				throw new AddressException(cc[q]);
			}
			msg.addRecipient(Message.RecipientType.CC, ia);
		}

		//set BCC recipients
		for (int q = 0; q < bcc.length; q++) {
			bcc[q] = bcc[q].replace(',', ' ');
			try {
				ia = getInternetAddress(bcc[q]);
			} catch (AddressException exc) {
				throw new AddressException(bcc[q]);
			}
			msg.addRecipient(Message.RecipientType.BCC, ia);
		}

		//set reply to addr
		if (replyTo != null && replyTo.length() > 0) {
			Address[] replyaddr = new Address[1];
			replyaddr[0] = getInternetAddress(replyTo);
			msg.setReplyTo(replyaddr);
		}

		//add any header
		String headerLines[] = smsg.getHeaderLines();
		for (int i = 0; i < headerLines.length; ++i) {
			if (!headerLines[i].startsWith("Sonicle-reply-folder")) {
				msg.addHeaderLine(headerLines[i]);
			}
		}

		//add reply/references
		String inreplyto = smsg.getInReplyTo();
		String references[] = smsg.getReferences();
		String replyfolder = smsg.getReplyFolder();
		if (inreplyto != null) {
			msg.setHeader("In-Reply-To", inreplyto);
		}
		if (references != null && references[0] != null) {
			msg.setHeader("References", references[0]);
		}
		if (tosave) {
			if (replyfolder != null) {
				msg.setHeader("Sonicle-reply-folder", replyfolder);
			}
			msg.setHeader("Sonicle-draft", "true");
		}

		//add forward data
		String forwardedfrom = smsg.getForwardedFrom();
		String forwardedfolder = smsg.getForwardedFolder();
		if (forwardedfrom != null) {
			msg.setHeader("Forwarded-From", forwardedfrom);
		}
		if (tosave) {
			if (forwardedfolder != null) {
				msg.setHeader("Sonicle-forwarded-folder", forwardedfolder);
			}
			msg.setHeader("Sonicle-draft", "true");
		}
		//set the subject
		String subject = smsg.getSubject();
		try {
			//subject=MimeUtility.encodeText(smsg.getSubject(), "ISO-8859-1", null);
			subject = MimeUtility.encodeText(smsg.getSubject());
		} catch (Exception exc) {
		}
		msg.setSubject(subject);

		//set priority
		int priority = smsg.getPriority();
		if (priority != 3) {
			msg.setHeader("X-Priority", "" + priority);

			//set receipt
		}
		String receiptTo = from;
		try {
			receiptTo = MimeUtility.encodeText(from, "ISO-8859-1", null);
		} catch (Exception exc) {
		}
		if (smsg.getReceipt()) {
			msg.setHeader("Disposition-Notification-To", from);

			//see if there are any new attachments for the message
		}
		int noAttach;
		int newAttach;
		
		if (attachments == null) {
			newAttach = 0;
		} else {
			newAttach = attachments.size();
		}

		//get the array of the old attachments
		Part[] oldParts = smsg.getAttachments();

		//check if there are old attachments
		if (oldParts == null) {
			noAttach = 0;
		} else { //old attachments exist
			noAttach = oldParts.length;
		}
		
		if ((newAttach > 0) || (noAttach > 0) || !smsg.getMime().equalsIgnoreCase("text/plain")) {
			// create the main Multipart
			MimeMultipart mp = new MimeMultipart("mixed");
			
			String textcontent = smsg.getTextContent();
      //if is text, or no alternative text is available, add the content as one single body part
			//else create a multipart/alternative with both rich and text mime content
			if (textcontent == null || smsg.getMime().equalsIgnoreCase("text/plain")) {
				MimeBodyPart mbp1 = new MimeBodyPart();
				mbp1.setText(smsg.getContent(), "UTF-8");
				mbp1.setHeader("Content-type", smsg.getMime());
				
				mp.addBodyPart(mbp1);
			} else {
				MimeMultipart alternative = new MimeMultipart("alternative");
				//the rich part
				MimeBodyPart mbp2 = new MimeBodyPart();
				mbp2.setText(smsg.getContent(), "UTF-8");
				mbp2.setHeader("Content-type", smsg.getMime());
				//the text part
				MimeBodyPart mbp1 = new MimeBodyPart();

				/*          ByteArrayOutputStream bos=new ByteArrayOutputStream(textcontent.length());
				 com.sun.mail.util.QPEncoderStream qpe=new com.sun.mail.util.QPEncoderStream(bos);
				 for(int i=0;i<textcontent.length();++i) {
				 try {
				 qpe.write(textcontent.charAt(i));
				 } catch(IOException exc) {
				 Service.logger.error("Exception",exc);
				 }
				 }
				 textcontent=new String(bos.toByteArray());*/
				mbp1.setText(textcontent, "UTF-8");
				mbp1.setHeader("Content-type", "text/plain");
//          mbp1.setHeader("Content-transfer-encoding","quoted-printable");

				alternative.addBodyPart(mbp1);
				alternative.addBodyPart(mbp2);
				
				MimeBodyPart altbody = new MimeBodyPart();
				altbody.setContent(alternative);
				
				mp.addBodyPart(altbody);
			}
			
			if (noAttach > 0) { //if there are old attachments
				// create the parts with the attachments
				//MimeBodyPart[] mbps2 = new MimeBodyPart[noAttach];
				//Part[] mbps2 = new Part[noAttach];

        //for(int e = 0;e < noAttach;e++) {
				//  mbps2[e] = (Part)oldParts[e];
				//}//end for e
				//add the old attachment parts
				for (int r = 0; r < noAttach; r++) {
					Object content = null;
					String contentType = null;
					String contentFileName = null;
					if (oldParts[r] instanceof Message) {
//                Service.logger.debug("Attachment is a message");
						Message msgpart = (Message) oldParts[r];
						MimeMessage mm = new MimeMessage(session);
						mm.addFrom(msgpart.getFrom());
						mm.setRecipients(Message.RecipientType.TO, msgpart.getRecipients(Message.RecipientType.TO));
						mm.setRecipients(Message.RecipientType.CC, msgpart.getRecipients(Message.RecipientType.CC));
						mm.setRecipients(Message.RecipientType.BCC, msgpart.getRecipients(Message.RecipientType.BCC));
						mm.setReplyTo(msgpart.getReplyTo());
						mm.setSentDate(msgpart.getSentDate());
						mm.setSubject(msgpart.getSubject());
						mm.setContent(msgpart.getContent(), msgpart.getContentType());
						content = mm;
						contentType = "message/rfc822";
					} else {
//                Service.logger.debug("Attachment is not a message");
						content = oldParts[r].getContent();
						if (!(content instanceof MimeMultipart)) {
							contentType = oldParts[r].getContentType();
							contentFileName = oldParts[r].getFileName();
						}
					}
					MimeBodyPart mbp = new MimeBodyPart();
					if (contentFileName != null) {
						mbp.setFileName(contentFileName);
//              Service.logger.debug("adding attachment mime "+contentType+" filename "+contentFileName);
						contentType += "; name=\"" + contentFileName + "\"";
					}
					if (content instanceof MimeMultipart) mbp.setContent((MimeMultipart)content);
					else mbp.setDataHandler(new DataHandler(content, contentType));
					mp.addBodyPart(mbp);
				}
				
			} //end if, adding old attachments

			if (newAttach > 0) { //if there are new attachments
				// create the parts with the attachments
				MimeBodyPart[] mbps = new MimeBodyPart[newAttach];
				
				for (int e = 0; e < newAttach; e++) {
					mbps[e] = new MimeBodyPart();

					// attach the file to the message
					JsAttachment attach = (JsAttachment) attachments.get(e);
                    UploadedFile upfile=getUploadedFile(attach.uploadId);
					FileDataSource fds = new FileDataSource(upfile.getFile());
					mbps[e].setDataHandler(new DataHandler(fds));
          // filename starts has format:
					// "_" + userid + sessionId + "_" + filename
					//
					if (attach.inline) {
						mbps[e].setDisposition(Part.INLINE);
					}
					String contentFileName = attach.fileName.trim();
					mbps[e].setFileName(contentFileName);
					String contentType = upfile.getMediaType() + "; name=\"" + contentFileName + "\"";
					mbps[e].setHeader("Content-type", contentType);
					if (attach.cid != null && attach.cid.trim().length()>0) {
						mbps[e].setHeader("Content-ID", "<" + attach.cid + ">");
						mbps[e].setHeader("X-Attachment-Id", attach.cid);
						mbps[e].setDisposition(Part.INLINE);
					}
				} //end for e

				//add the new attachment parts
				for (int r = 0; r < newAttach; r++) {
					mp.addBodyPart(mbps[r]);
				}
				
			} //end if, adding new attachments

//
//          msg.addHeaderLine("This is a multi-part message in MIME format.");
			// add the Multipart to the message
			msg.setContent(mp);
			
		} else { //end if newattach
			msg.setText(smsg.getContent());
		} //singlepart message

		msg.setSentDate(new java.util.Date());
		
		return msg;
		
	}
	
	private Exception saveSent(Message msg, String sentfolder) {
		Exception retexc = null;
		try {
			checkCreateFolder(sentfolder);
			Folder outgoing = getFolder(sentfolder);
			msg.setFlag(Flags.Flag.SEEN, true);
			
			Message[] saveMsgs = new MimeMessage[1];
			saveMsgs[0] = msg;
			
			outgoing.appendMessages(saveMsgs);
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
		}
		
		return retexc;
		
	}
	
	public Exception saveMessage(SimpleMessage msg, List<JsAttachment> attachments, FolderCache fc) {
		Exception retexc = null;
		try {
			_saveMessage(msg, attachments, fc);
		} catch (Exception exc) {
			retexc = exc;
		}
		return retexc;
	}
	
	public Exception scheduleMessage(SimpleMessage msg, List<JsAttachment> attachments, FolderCache fc, String senddate, String sendtime, String sendnotify) {
		Exception retexc = null;
		try {
			msg.addHeaderLine("Sonicle-send-scheduled: true");
			msg.addHeaderLine("Sonicle-send-date: " + senddate);
			msg.addHeaderLine("Sonicle-send-time: " + sendtime);
			msg.addHeaderLine("Sonicle-notify-delivery: " + sendnotify);
			Message m = _saveMessage(msg, attachments, fc);
			/*        String mid=m.getHeader("Message-ID")[0];
			 MailServiceGeneral mservgen=(MailServiceGeneral)wta.getServiceGeneralByName("mail");
        
			 UserProfile profile=wts.getEnvironment().getUserProfile();
			 String port=profile.getMailPort()+"";
			 String host=profile.getMailHost();
			 String protocol=profile.getMailProtocol();
			 String username=profile.getMailUsername();
			 String folderprefix=profile.getFolderPrefix();
			 String folderdrafts=profile.getFolderDrafts();
        
			 MailServiceGeneral.MailUser mu=mservgen.createMailUser(profile.getIDDomain(), host, protocol, port, username, folderprefix, folderdrafts);
			 mservgen.scheduleSendTask(mu, mid, senddate, sendtime, sendnotify);*/
		} catch (Exception exc) {
			retexc = exc;
		}
		return retexc;
	}
	
	private Message _saveMessage(SimpleMessage msg, List<JsAttachment> attachments, FolderCache fc) throws Exception {
		UserProfile profile = environment.getProfile();
		String sender = profile.getEmailAddress();
		String name = profile.getDisplayName();
		String replyto = mprofile.getReplyTo();
		
		if (msg.getFrom() != null) {
			Identity from = msg.getFrom();
			sender = from.getEmail();
			name = from.getDisplayName();
			replyto = sender;
		}
		
		msg.setReplyTo(replyto);
		
		if (name != null && name.length() > 0) {
			sender = name + " <" + sender + ">";
			
		}
		/*ArrayList<Attachment> origattachments = getAttachments(msg.getId());
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		for (String attname : attnames) {
			for (Attachment att : origattachments) {
				if (att.getFile().getName().equals(attname)) {
					attachments.add(att);
					//Service.logger.debug("Adding attachment : "+attname+" -> "+att.getName());
					break;
				}
			}
		}*/
		
		Message newmsg = createMessage(sender, msg, attachments, true);
		newmsg.setHeader("Sonicle-draft", "true");
    //FolderCache fc=getFolderCache(profile.getFolderDrafts());
		//newmsg.writeTo(new FileOutputStream("C:/Users/gbulfon/Desktop/TEST.eml"));
		fc.save(newmsg);
		clearAttachments(msg.getId());
		
		return newmsg;
	}
	
	private SimpleMessage getForwardMsg(long id, Message msg, boolean richContent, String fromtitle, String totitle, String cctitle, String datetitle, String subjecttitle, boolean attached) {
		Message forward = new MimeMessage(session);
		if (!attached) {
			try {
				StringBuffer htmlsb = new StringBuffer();
				StringBuffer textsb = new StringBuffer();
				boolean isHtml = appendReplyParts(msg, htmlsb, textsb, null);
    //      Service.logger.debug("isHtml="+isHtml);
				//      Service.logger.debug("richContent="+richContent);
				String html = "<HTML><BODY>" + htmlsb.toString() + "</BODY></HTML>";
				if (!richContent) {
					forward.setText(getForwardBody(msg, textsb.toString(), SimpleMessage.FORMAT_TEXT, false, fromtitle, totitle, cctitle, datetitle, subjecttitle));
				} else if (!isHtml) {
					forward.setText(getForwardBody(msg, textsb.toString(), SimpleMessage.FORMAT_PREFORMATTED, false, fromtitle, totitle, cctitle, datetitle, subjecttitle));
				} else {
					forward.setText(getForwardBody(msg, html, SimpleMessage.FORMAT_HTML, true, fromtitle, totitle, cctitle, datetitle, subjecttitle));
				}
			} catch (Exception exc) {
				Service.logger.error("Exception",exc);
			}
		}
		
		try {
			String msgid = null;
			String vh[] = msg.getHeader("Message-ID");
			if (vh != null) {
				msgid = vh[0];
			}
			if (msgid != null) {
				forward.setHeader("Forwarded-From", msgid);
			}
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
		}
		
		SimpleMessage fwd = new SimpleMessage(id, forward);
		fwd.setTo("");

    // Update appropriate subject
		// Fwd: subject
		try {
			String subject = msg.getSubject();
			if (subject == null) {
				subject = "";
			}
			if (!subject.toLowerCase().startsWith("fwd: ")) {
				fwd.setSubject("Fwd: " + subject);
			} else {
				fwd.setSubject(msg.getSubject());
			}
		} catch (MessagingException e) {
			Service.logger.error("Exception",e);
//      Service.logger.debug("*** SimpleMessage: " +e);
		}
		
		return fwd;
	}
	
	private String getForwardBody(Message msg, String body, int format, boolean isHtml, String fromtitle, String totitle, String cctitle, String datetitle, String subjecttitle) throws MessagingException {
		UserProfile profile = environment.getProfile();
		Locale locale = profile.getLocale();
		String msgSubject = msg.getSubject();
		if (msgSubject == null) {
			msgSubject = "";
		}
		msgSubject = MailUtils.htmlescape(msgSubject);
		Address ad[] = msg.getFrom();
		String msgFrom = "";
		if (ad != null) {
			msgFrom = isHtml?getHTMLDecodedAddress(ad[0]):getDecodedAddress(ad[0]);
		}
		java.util.Date dt = msg.getSentDate();
		String msgDate = "";
		if (dt != null) {
			msgDate = DateFormat.getDateTimeInstance(java.text.DateFormat.LONG, java.text.DateFormat.LONG, locale).format(dt);
		}
		ad = msg.getRecipients(Message.RecipientType.TO);
		String msgTo = null;
		if (ad != null) {
			msgTo = "";
			for (int j = 0; j < ad.length; ++j) {
				msgTo += isHtml?getHTMLDecodedAddress(ad[j]):getDecodedAddress(ad[j]) + " ";
			}
		}
		ad = msg.getRecipients(Message.RecipientType.CC);
		String msgCc = null;
		if (ad != null) {
			msgCc = "";
			for (int j = 0; j < ad.length; ++j) {
				msgCc += isHtml?getHTMLDecodedAddress(ad[j]):getDecodedAddress(ad[j]) + " ";
			}
		}
		
		StringBuffer sb = new StringBuffer();
		String cr = "\n";
		if (format != SimpleMessage.FORMAT_TEXT) {
			cr = "<BR>";
		}
		if (format != SimpleMessage.FORMAT_HTML) {
			if (format == SimpleMessage.FORMAT_PREFORMATTED) {
				sb.append("<TT>");
			}
			sb.append(cr + cr + cr + "----------------------------------------------------------------------------------" + cr + cr);
			sb.append(fromtitle + ": " + msgFrom + cr);
			if (msgTo != null) {
				sb.append(totitle + ": " + msgTo + cr);
			}
			if (msgCc != null) {
				sb.append(cctitle + ": " + msgCc + cr);
			}
			sb.append(datetitle + ": " + msgDate + cr);
			sb.append(subjecttitle + ": " + msgSubject + cr + cr);
			if (format == SimpleMessage.FORMAT_PREFORMATTED) {
				sb.append("</TT>");
			}
		} else {
			sb.append(cr + "<HR>" + cr + cr);
			sb.append("<font face='Arial, Helvetica, sans-serif' size=2>");
			sb.append("<B>" + fromtitle + ":</B> " + msgFrom + "<BR>");
			if (msgTo != null) {
				sb.append("<B>" + totitle + ":</B> " + msgTo + "<BR>");
			}
			if (msgCc != null) {
				sb.append("<B>" + cctitle + ":</B> " + msgCc + "<BR>");
			}
			sb.append("<B>" + datetitle + ":</B> " + msgDate + "<BR>");
			sb.append("<B>" + subjecttitle + ":</B> " + msgSubject + "<BR>");
			sb.append("</font><br>" + cr);
		}

    // Prepend "> " for each line in the body
		//
		if (body != null) {
			if (format == SimpleMessage.FORMAT_HTML) {
//        sb.append("<TABLE border=0 width='100%'><TR><td width=2 bgcolor=#000088></td><td width=2></td><td>");
//        sb.append("<BLOCKQUOTE style='BORDER-LEFT: #000080 2px solid; MARGIN-LEFT: 5px; PADDING-LEFT: 5px'>");
			}
			if (!isHtml) {
				if (format == SimpleMessage.FORMAT_PREFORMATTED) {
//          sb.append("<BLOCKQUOTE style='BORDER-LEFT: #000080 2px solid; MARGIN-LEFT: 5px; PADDING-LEFT: 5px'>");
					sb.append("<tt>");
				}
				StringTokenizer st = new StringTokenizer(body, "\n", true);
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (token.equals("\n")) {
						sb.append(cr);
					} else {
						if (format == SimpleMessage.FORMAT_TEXT) {
							sb.append("> ");
						}
						//sb.append(MailUtils.htmlescape(token));
						sb.append(token);
					}
				}
				if (format == SimpleMessage.FORMAT_PREFORMATTED) {
					sb.append("</tt>");
//          sb.append("</BLOCKQUOTE>");
				}
			} else {
				sb.append(getBodyInnerHtml(body));
			}
			if (format == SimpleMessage.FORMAT_HTML) {
//        sb.append("</td></tr></table>");
//        sb.append("</BLOCKQUOTE>");
			}
		}
		return sb.toString();
	}
	
  private String getBodyInnerHtml(String body) {
	int ix1=StringUtils.indexOfIgnoreCase(body,"<BODY");
	if(ix1>=0) {
	  int ix2=body.indexOf(">", ix1+1);
	  if(ix2<0) {
		ix2=ix1+4;
	  }
	  int ix3=StringUtils.indexOfIgnoreCase(body,"</BODY", ix2+1);
	  if(ix3>0) {
		body=body.substring(ix2+1, ix3);
	  } else {
		body=body.substring(ix2+1);
	  }	
	}
	return body;
  }	

	//Clone of MimeMessage that was private and used by my custom reply
	private Address[] eliminateDuplicates(Vector v, Address[] addrs) {
		if (addrs == null) {
			return null;
		}
		int gone = 0;
		for (int i = 0; i < addrs.length; i++) {
			boolean found = false;
			// search the vector for this address
			for (int j = 0; j < v.size(); j++) {
				if (((InternetAddress) v.elementAt(j)).equals(addrs[i])) {
					// found it; count it and remove it from the input array
					found = true;
					gone++;
					addrs[i] = null;
					break;
				}
			}
			if (!found) {
				v.addElement(addrs[i]);	// add new address to vector
			}
		}
		// if we found any duplicates, squish the array
		if (gone != 0) {
			Address[] a;
	    // new array should be same type as original array
			// XXX - there must be a better way, perhaps reflection?
			if (addrs instanceof InternetAddress[]) {
				a = new InternetAddress[addrs.length - gone];
			} else {
				a = new Address[addrs.length - gone];
			}
			for (int i = 0, j = 0; i < addrs.length; i++) {
				if (addrs[i] != null) {
					a[j++] = addrs[i];
				}
			}
			addrs = a;
		}
		return addrs;
	}

	//CLONE OF ImapMessage.reply() that does not set the ANSWERED Flag
    public Message reply(MimeMessage orig, boolean replyToAll, boolean fromSent) throws MessagingException {
		MimeMessage reply = new MimeMessage(session);
		/*
		 * Have to manipulate the raw Subject header so that we don't lose
		 * any encoding information.  This is safe because "Re:" isn't
		 * internationalized and (generally) isn't encoded.  If the entire
		 * Subject header is encoded, prefixing it with "Re: " still leaves
		 * a valid and correct encoded header.
		 */
		String subject = orig.getHeader("Subject", null);
		if (subject != null) {
			if (!subject.regionMatches(true, 0, "Re: ", 0, 4)) {
				subject = "Re: " + subject;
			}
			reply.setHeader("Subject", subject);
		}
		Address a[] = null;
		if (!fromSent) a=orig.getReplyTo();
		else {
			Address ax[]=orig.getRecipients(RecipientType.TO);
			if (ax!=null) {
				a=new Address[1];
				a[0]=ax[0];
			}
		}
		reply.setRecipients(Message.RecipientType.TO, a);
		if (replyToAll) {
			Vector v = new Vector();
			// add my own address to list
			InternetAddress me = InternetAddress.getLocalAddress(session);
			if (me != null) {
				v.addElement(me);
			}
			// add any alternate names I'm known by
			String alternates = null;
			if (session != null) {
				alternates = session.getProperty("mail.alternates");
			}
			if (alternates != null) {
				eliminateDuplicates(v,
						InternetAddress.parse(alternates, false));
			}
			// should we Cc all other original recipients?
			String replyallccStr = null;
			boolean replyallcc = false;
			if (session != null) {
				replyallcc = PropUtil.getBooleanSessionProperty(session,
						"mail.replyallcc", false);
			}
			// add the recipients from the To field so far
			eliminateDuplicates(v, a);
			a = orig.getRecipients(Message.RecipientType.TO);
			a = eliminateDuplicates(v, a);
			if (a != null && a.length > 0) {
				if (replyallcc) {
					reply.addRecipients(Message.RecipientType.CC, a);
				} else {
					reply.addRecipients(Message.RecipientType.TO, a);
				}
			}
			a = orig.getRecipients(Message.RecipientType.CC);
			a = eliminateDuplicates(v, a);
			if (a != null && a.length > 0) {
				reply.addRecipients(Message.RecipientType.CC, a);
			}
			// don't eliminate duplicate newsgroups
			a = orig.getRecipients(MimeMessage.RecipientType.NEWSGROUPS);
			if (a != null && a.length > 0) {
				reply.setRecipients(MimeMessage.RecipientType.NEWSGROUPS, a);
			}
		}
		
		String msgId = orig.getHeader("Message-Id", null);
		if (msgId != null) {
			reply.setHeader("In-Reply-To", msgId);
		}

		/*
		 * Set the References header as described in RFC 2822:
		 *
		 * The "References:" field will contain the contents of the parent's
		 * "References:" field (if any) followed by the contents of the parent's
		 * "Message-ID:" field (if any).  If the parent message does not contain
		 * a "References:" field but does have an "In-Reply-To:" field
		 * containing a single message identifier, then the "References:" field
		 * will contain the contents of the parent's "In-Reply-To:" field
		 * followed by the contents of the parent's "Message-ID:" field (if
		 * any).  If the parent has none of the "References:", "In-Reply-To:",
		 * or "Message-ID:" fields, then the new message will have no
		 * "References:" field.
		 */
		String refs = orig.getHeader("References", " ");
		if (refs == null) {
			// XXX - should only use if it contains a single message identifier
			refs = orig.getHeader("In-Reply-To", " ");
		}
		if (msgId != null) {
			if (refs != null) {
				refs = MimeUtility.unfold(refs) + " " + msgId;
			} else {
				refs = msgId;
			}
		}
		if (refs != null) {
			reply.setHeader("References", MimeUtility.fold(12, refs));
		}

	//try {
		//    setFlags(answeredFlag, true);
		//} catch (MessagingException mex) {
		//    // ignore it
		//}
		return reply;
	}

	// used above in reply()
	private static final Flags answeredFlag = new Flags(Flags.Flag.ANSWERED);
	
	private SimpleMessage getReplyMsg(int id, Message msg, boolean replyAll, boolean fromSent, boolean richContent, String myemail, boolean includeOriginal, String fromtitle, String totitle, String cctitle, String datetitle, String subjecttitle) {	
		try {
			Message reply=reply((MimeMessage)msg,replyAll,fromSent);
			
			removeDestination(reply, myemail);
			for (Identity ident : mprofile.getIdentities()) {
				removeDestination(reply, ident.getEmail());
			}

      // Setup the message body
			//
			StringBuffer htmlsb = new StringBuffer();
			StringBuffer textsb = new StringBuffer();
			ArrayList<String> attnames = new ArrayList<String>();
			if (includeOriginal) {
				boolean isHtml = appendReplyParts(msg, htmlsb, textsb, attnames);
				String html = "<HTML><BODY>" + htmlsb.toString() + "</BODY></HTML>";
				String text = null;
				if (!richContent) {
					text = getReplyBody(msg, textsb.toString(), SimpleMessage.FORMAT_TEXT, false, fromtitle, totitle, cctitle, datetitle, subjecttitle, attnames);
				} else if (!isHtml) {
					text = getReplyBody(msg, textsb.toString(), SimpleMessage.FORMAT_PREFORMATTED, false, fromtitle, totitle, cctitle, datetitle, subjecttitle, attnames);
				} else {
					text = getReplyBody(msg, html, SimpleMessage.FORMAT_HTML, true, fromtitle, totitle, cctitle, datetitle, subjecttitle, attnames);
				}
				reply.setText(text);
			} else {
				reply.setText("");
			}
			return new SimpleMessage(id, reply);
		} catch (MessagingException e) {
			Service.logger.error("Exception",e);
//      Service.logger.debug("*** SimpleMessage: " + e);
			return null;
		} catch (IOException e) {
			Service.logger.error("Exception",e);
//      Service.logger.debug("*** SimpleMessage: " + e);
			return null;
		}
	}
	
	private String getReplyBody(Message msg, String body, int format, boolean isHtml, String fromtitle, String totitle, String cctitle, String datetitle, String subjecttitle, ArrayList<String> attnames) throws MessagingException {
		UserProfile profile = environment.getProfile();
		Locale locale = profile.getLocale();
		String msgSubject = msg.getSubject();
		if (msgSubject == null) {
			msgSubject = "";
		}
		msgSubject = MailUtils.htmlescape(msgSubject);
		Address ad[] = msg.getFrom();
		String msgFrom = "";
		if (ad != null) {
			msgFrom = isHtml?getHTMLDecodedAddress(ad[0]):getDecodedAddress(ad[0]);
		}
		java.util.Date dt = msg.getSentDate();
		String msgDate = "";
		if (dt != null) {
			msgDate = DateFormat.getDateTimeInstance(java.text.DateFormat.LONG, java.text.DateFormat.LONG, locale).format(dt);
		}
		ad = msg.getRecipients(Message.RecipientType.TO);
		String msgTo = null;
		if (ad != null) {
			msgTo = "";
			for (int j = 0; j < ad.length; ++j) {
				msgTo += isHtml?getHTMLDecodedAddress(ad[j]):getDecodedAddress(ad[j]) + " ";
			}
		}
		ad = msg.getRecipients(Message.RecipientType.CC);
		String msgCc = null;
		if (ad != null) {
			msgCc = "";
			for (int j = 0; j < ad.length; ++j) {
				msgCc += isHtml?getHTMLDecodedAddress(ad[j]):getDecodedAddress(ad[j]) + " ";
			}
		}

		//
		/*String sfrom = "";
		try {
			if (msg.getFrom() != null) {
				InternetAddress ia = (InternetAddress) msg.getFrom()[0];
				String personal = ia.getPersonal();
				String mail = ia.getAddress();
				if (personal == null || personal.equals(mail)) {
					sfrom = mail;
				} else {
					sfrom = personal + " <" + mail + ">";
				}
			}
		} catch (Exception exc) {
			
		}*/
		StringBuffer sb = new StringBuffer();
		String cr = "\n";
		if (format != SimpleMessage.FORMAT_TEXT) {
			cr = "<BR>";
		}
		if (format != SimpleMessage.FORMAT_HTML) {
			if (format == SimpleMessage.FORMAT_PREFORMATTED) {
				sb.append("<TT>");
			}
			sb.append(cr + cr + cr + "----------------------------------------------------------------------------------" + cr + cr);
			sb.append(fromtitle + ": " + msgFrom + cr);
			if (msgTo != null) {
				sb.append(totitle + ": " + msgTo + cr);
			}
			if (msgCc != null) {
				sb.append(cctitle + ": " + msgCc + cr);
			}
			sb.append(datetitle + ": " + msgDate + cr);
			sb.append(subjecttitle + ": " + msgSubject + cr + cr);
			if (format == SimpleMessage.FORMAT_PREFORMATTED) {
				sb.append("</TT>");
			}
		} else {
			sb.append(cr + "<HR>" + cr + cr);
			sb.append("<font face='Arial, Helvetica, sans-serif' size=2>");
			sb.append("<B>" + fromtitle + ":</B> " + msgFrom + "<BR>");
			if (msgTo != null) {
				sb.append("<B>" + totitle + ":</B> " + msgTo + "<BR>");
			}
			if (msgCc != null) {
				sb.append("<B>" + cctitle + ":</B> " + msgCc + "<BR>");
			}
			sb.append("<B>" + datetitle + ":</B> " + msgDate + "<BR>");
			sb.append("<B>" + subjecttitle + ":</B> " + msgSubject + "<BR>");
			sb.append("</font><br>" + cr);
		}

    // Prepend "> " for each line in the body
		//
		if (body != null) {
			if (format == SimpleMessage.FORMAT_HTML) {
//        sb.append("<TABLE border=0 width='100%'><TR><td width=2 bgcolor=#000088></td><td width=2></td><td>");
				sb.append("<BLOCKQUOTE style='BORDER-LEFT: #000080 2px solid; MARGIN-LEFT: 5px; PADDING-LEFT: 5px'>");
			}
			if (!isHtml) {
				if (format == SimpleMessage.FORMAT_PREFORMATTED) {
					sb.append("<BLOCKQUOTE style='BORDER-LEFT: #000080 2px solid; MARGIN-LEFT: 5px; PADDING-LEFT: 5px'>");
					sb.append("<tt>");
				}
				StringTokenizer st = new StringTokenizer(body, "\n", true);
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (token.equals("\n")) {
						sb.append(cr);
					} else {
						if (format == SimpleMessage.FORMAT_TEXT) {
							sb.append("> ");
						}
						//sb.append(MailUtils.htmlescape(token));
						sb.append(token);
					}
				}
				if (format == SimpleMessage.FORMAT_PREFORMATTED) {
					sb.append("</tt>");
					sb.append("</BLOCKQUOTE>");
				}
			} else {
				String ubody = body.toUpperCase();
				while (true) {
					int ix1 = ubody.indexOf("<BODY");
					if (ix1 < 0) {
						break;
					}
					int ix2 = ubody.indexOf(">", ix1 + 1);
					if (ix2 < 0) {
						ix2 = ix1 + 4;
					}
					int ix3 = ubody.indexOf("</BODY", ix2 + 1);
					if (ix3 > 0) {
						body = body.substring(ix2 + 1, ix3);
						ubody = ubody.substring(ix2 + 1, ix3);
					} else {
						body = body.substring(ix2 + 1);
						ubody = ubody.substring(ix2 + 1);
					}
				}
				//body=removeStartEndTag(body,unwantedTags);

				sb.append(body);
			}
			htmlAppendAttachmentNames(sb, attnames);
			if (format == SimpleMessage.FORMAT_HTML) {
//        sb.append("</td></tr></table>");
				sb.append("</BLOCKQUOTE>");
			}
		}
		return sb.toString();
	}
	
	private void removeDestination(Message msg, String email) throws MessagingException {
		Address a[] = null;
		try {
			a = msg.getRecipients(Message.RecipientType.TO);
		} catch (AddressException exc) {
			
		}
		if (a != null) {
			msg.setRecipients(
					Message.RecipientType.TO,
					removeDestination(a, email)
			);
		}
		try {
			a = msg.getRecipients(Message.RecipientType.CC);
		} catch (AddressException exc) {
			
		}
		if (a != null) {
			msg.setRecipients(
					Message.RecipientType.CC,
					removeDestination(a, email)
			);
		}
		try {
			a = msg.getRecipients(Message.RecipientType.BCC);
		} catch (AddressException exc) {
			
		}
		if (a != null) {
			msg.setRecipients(
					Message.RecipientType.BCC,
					removeDestination(a, email)
			);
		}
	}
	
	private Address[] removeDestination(Address a[], String email) throws MessagingException {
		email = email.toLowerCase();
		Vector v = new Vector();
		for (int i = 0; i < a.length; ++i) {
			InternetAddress ia = (InternetAddress) a[i];
			if (!ia.getAddress().toLowerCase().equals(email)) {
				v.addElement(a[i]);
			}
		}
		Address na[] = new Address[v.size()];
		v.copyInto(na);
		return na;
	}
	
	private void textAppendAttachmentNames(StringBuffer sb, ArrayList<String> attnames) {
		if (attnames.size() > 0) {
			sb.append("\n\n");
			for (String name : attnames) {
				sb.append("<<" + name + ">>\n");
			}
		}
	}
	
	private void htmlAppendAttachmentNames(StringBuffer sb, ArrayList<String> attnames) {
		if (attnames.size() > 0) {
			sb.append("<br><br>");
			for (String name : attnames) {
				sb.append("&lt;&lt;" + name + "&gt;&gt;<br>");
			}
		}
	}
	
	private String getTextPlainContentAsString(Part p) throws IOException, MessagingException {
		java.io.InputStream istream = null;
		String charset = MailUtils.getCharsetOrDefault(p.getContentType());
		if (!java.nio.charset.Charset.isSupported(charset)) {
			charset = "ISO-8859-1";
		}
		try {
			if (p instanceof javax.mail.internet.MimeMessage) {
				javax.mail.internet.MimeMessage mm = (javax.mail.internet.MimeMessage) p;
				istream = mm.getInputStream();
			} else if (p instanceof javax.mail.internet.MimeBodyPart) {
				javax.mail.internet.MimeBodyPart mm = (javax.mail.internet.MimeBodyPart) p;
				istream = mm.getInputStream();
			}
		} catch (Exception exc) { //unhandled format, get Raw data
			if (p instanceof javax.mail.internet.MimeMessage) {
				javax.mail.internet.MimeMessage mm = (javax.mail.internet.MimeMessage) p;
				istream = mm.getRawInputStream();
			} else if (p instanceof javax.mail.internet.MimeBodyPart) {
				javax.mail.internet.MimeBodyPart mm = (javax.mail.internet.MimeBodyPart) p;
				istream = mm.getRawInputStream();
			}
		}
		
		if (istream == null) {
			throw new IOException("Unknown message class " + p.getClass().getName());
		}
		
		java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(istream, charset));
		String line = null;
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		br.close();
		return sb.toString();
	}
	
	private boolean appendReplyParts(Part p, StringBuffer htmlsb, StringBuffer textsb, ArrayList<String> attnames) throws MessagingException,
			IOException {
		boolean isHtml = false;
		String disp = p.getDisposition();
		if (disp!=null && disp.equalsIgnoreCase(Part.ATTACHMENT) && !p.isMimeType("message/*")) {
			return false;
		}
		if (p.isMimeType("text/html")) {
			String htmlcontent=(String)p.getContent();
			textsb.append(MailUtils.htmlToText(MailUtils.htmlunescapesource(htmlcontent)));
			htmlsb.append(MailUtils.htmlescapefixsource(getBodyInnerHtml(htmlcontent)));
			isHtml = true;
		} else if (p.isMimeType("text/plain")) {
			String content = getTextPlainContentAsString(p);
			textsb.append(content);
			htmlsb.append(startpre + MailUtils.htmlescape(content) + endpre);
			isHtml = false;
		} else if (p.isMimeType("message/delivery-status") || p.isMimeType("message/disposition-notification")) {
			InputStream is = (InputStream) p.getContent();
			char cbuf[] = new char[8000];
			byte bbuf[] = new byte[8000];
			int n = 0;
			htmlsb.append(startpre);
			while ((n = is.read(bbuf)) >= 0) {
				if (n > 0) {
					for (int i = 0; i < n; ++i) {
						cbuf[i] = (char) bbuf[i];
					}
					textsb.append(cbuf);
					htmlsb.append(MailUtils.htmlescape(new String(cbuf)));
				}
			}
			htmlsb.append(endpre);
			is.close();
			isHtml = false;
		} else if (p.isMimeType("multipart/alternative")) {
			Multipart mp = (Multipart) p.getContent();
			Part bestPart = null;
			for (int i = 0; i < mp.getCount(); ++i) {
				Part part = mp.getBodyPart(i);
				if (part.isMimeType("multipart/*")) {
					isHtml = appendReplyParts(part, htmlsb, textsb, attnames);
					if (isHtml) {
						bestPart = null;
						break;
					}
				} else if (part.isMimeType("text/html")) {
					bestPart = part;
					break;
				} else if (bestPart == null && part.isMimeType("text/plain")) {
					bestPart = part;
				} else if (bestPart == null && part.isMimeType("message/*")) {
					bestPart = part;
				}
			}
			if (bestPart != null) {
				isHtml = appendReplyParts(bestPart, htmlsb, textsb, attnames);
			}
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); ++i) {
				if (appendReplyParts(mp.getBodyPart(i), htmlsb, textsb, attnames)) {
					isHtml = true;
				}
			}
		} else if (p.isMimeType("message/*")) {
			Object content = p.getContent();
			if (appendReplyParts((MimeMessage) content, htmlsb, textsb, attnames)) {
				isHtml = true;
			}
		} else {
			String filename = p.getFileName();
			String id[] = p.getHeader("Content-ID");
			if (id != null || filename != null) {
				if (id != null) {
					filename = id[0];
				}
				if (filename.startsWith("<")) {
					filename = filename.substring(1);
				}
				if (filename.endsWith(">")) {
					filename = filename.substring(0, filename.length() - 1);
				}
			}
			if (filename != null && attnames != null) {
				attnames.add(filename);
			}
		}
		textsb.append('\n');
		textsb.append('\n');
		
		return isHtml;
	}
	
	protected void finalize() {
		cleanup();
	}
	
	public void cleanup() {
		if (mft != null) {
			mft.abort();
		}
		if (ast != null && ast.isRunning()) {
			ast.cancel();
		}
		
		try {
			disconnect();
		} catch (Exception e) {
			Service.logger.error("Exception",e);
		}
		clearAllAttachments();
		if (fcRoot != null) {
			fcRoot.cleanup(true);
		}
		fcRoot = null;
		for (FolderCache fc : foldersCache.values()) {
			fc.cleanup(true);
		}
		foldersCache.clear();
		validated = false;
	}
	
	protected void clearAllAttachments() {
		for (Long id : msgattach.keySet()) {
			clearAttachments(id.longValue());
		}
		msgattach.clear();
	}

	private String getMessageID(Message m) throws MessagingException {
		String ids[] = m.getHeader("Message-ID");
		if (ids == null) {
			return null;
		}
		return ids[0];
	}
	
	private int[] toInts(String values[]) {
		int ret[] = new int[values.length];
		for (int i = 0; i < values.length; ++i) {
			ret[i] = Integer.parseInt(values[i]);
		}
		return ret;
	}

    private long[] toLongs(String values[]) {
        long ret[]=new long[values.length];
        for(int i=0;i<values.length;++i) ret[i]=Long.parseLong(values[i]);
        return ret;
    }
	
	public boolean isSentFolder(String fullname) {
		String lastname = getLastFolderName(fullname);
		String plastname = getLastFolderName(mprofile.getFolderSent());
		if (lastname.equals(plastname)) {
			return true;
		}
		return false;
	}
	
	public boolean isTrashFolder(String fullname) {
		String lastname = getLastFolderName(fullname);
		String plastname = getLastFolderName(mprofile.getFolderTrash());
		if (lastname.equals(plastname)) {
			return true;
		}
		return false;
	}
	
	public boolean isDraftsFolder(String fullname) {
		String lastname = getLastFolderName(fullname);
		String plastname = getLastFolderName(mprofile.getFolderDrafts());
		if (lastname.equals(plastname)) {
			return true;
		}
		return false;
	}
	
	public boolean isSpamFolder(String fullname) {
		String lastname = getLastFolderName(fullname);
		String plastname = getLastFolderName(mprofile.getFolderSpam());
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
				|| isSpamFolder(foldername);
		return retval;
	}
	
	public boolean isInboxFolder(String foldername) {
		if (!hasDifferentDefaultFolder) {
			String lname=getLastFolderName(foldername);
			if (lname.equals("INBOX")) return true;
		} else {
			return isDefaultFolder(foldername);
		}
		return false;
	}
	
	public boolean isDefaultFolder(String foldername) {
		Folder df=null;
		try {
			df=getDefaultFolder();
		} catch(MessagingException exc) {
			logger.error("Error getting default folder",exc);
		}
		if (df!=null) return df.getFullName().equals(foldername);
		return false;
	} 
  
	public boolean isUnderTrashFolder(String foldername) {
		String str=mprofile.getFolderTrash()+folderSeparator;
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
	
	public boolean isUnderSentFolder(String foldername) {	
		   String str=mprofile.getFolderSent()+folderSeparator;
		   return foldername.startsWith(str);
	}  	
	
	public boolean hasDocumentArchiving() {
		return RunContext.isPermitted(SERVICE_ID, "DOCUMENT_ARCHIVING");
	}
	
	public String getSimpleArchivingMailFolder() {
		return us.getSimpleArchivingMailFolder();
	}
	
	public boolean isSimpleArchiving() {
		return us.getArchivingMethod().equals(MailUserSettings.ARCHIVING_METHOD_SIMPLE);
	}

	public boolean isStructuredArchiving() {
		return us.getArchivingMethod().equals(MailUserSettings.ARCHIVING_METHOD_STRUCTURED);
	}
	
	public boolean isWebtopArchiving() {
		return us.getArchivingMethod().equals(MailUserSettings.ARCHIVING_METHOD_WEBTOP);
	}
	
	public boolean isDocMgtFolder(String foldername) {
		CoreManager core = WT.getCoreManager();
		
		UserProfile profile = environment.getProfile();
		if (!hasDocumentArchiving()) {
			return false;
		}
		boolean b = false;
		String df = us.getSimpleArchivingMailFolder();
		if (df != null && df.trim().length() > 0) {
			String lfn = getLastFolderName(foldername);
			String dfn = getLastFolderName(df);
			if (lfn.equals(dfn)) {
				b = true;
			}
		}
		return b;
	}
	
	public String getDecodedAddress(Address a) {
		String ret = "";
		try {
			InternetAddress ia = (InternetAddress) a;
			String personal = ia.getPersonal();
			String email = ia.getAddress();
			if (personal == null || personal.equals(email)) {
				ret = email;
			} else {
				ret = personal + " <" + email + ">";
			}
		} catch (RuntimeException exc) {
			ret = a.toString();
		}
		return ret;
	}
	
	public String getHTMLDecodedAddress(Address a) {
		String s = getDecodedAddress(a);
		return MailUtils.htmlescape(s);
	}
	
	public String getInternationalFolderName(FolderCache fc) {
		Folder folder = fc.getFolder();
		String desc = folder.getName();
		String fullname = folder.getFullName();
		//WebTopApp webtopapp=environment.getWebTopApp();
		Locale locale = environment.getProfile().getLocale();
		if (fc.isInbox()) {
			desc = lookupResource(MailLocaleKey.FOLDERS_INBOX);
		} else if (fc.isSharedFolder()) {
			desc = lookupResource(MailLocaleKey.FOLDERS_SHARED);
		} else if (fc.isDrafts()) {
			desc = lookupResource(MailLocaleKey.FOLDERS_DRAFTS);
		} else if (fc.isTrash()) {
			desc = lookupResource(MailLocaleKey.FOLDERS_TRASH);
		} else if (fc.isSent()) {
			desc = lookupResource(MailLocaleKey.FOLDERS_SENT);
		} else if (fc.isSpam()) {
			desc = lookupResource(MailLocaleKey.FOLDERS_SPAM);
		}
		return desc;
	}
	
	public String getInternationalFolderPath(FolderCache fc) throws MessagingException {
		String intpath = getInternationalFolderName(fc);
		char sep = folderSeparator;
		FolderCache parent = fc.getParent();
		while (parent != null && parent.isRoot()) {
			String intparent = getInternationalFolderName(parent);
			intpath = intparent + sep + intpath;
			parent = parent.getParent();
		}
		return intpath;
	}
	
	protected Folder getDefaultFolder() throws MessagingException {
		String df=us.getDefaultFolder();
		Folder f=null;
		if (df!=null) f=store.getFolder(df);
		else f=store.getDefaultFolder();
		return f;
	}
	
	protected String getInboxFolderFullName() {
		String inboxFolder="INBOX";
		try {
			if (hasDifferentDefaultFolder()) inboxFolder=getDefaultFolder().getFullName();
		} catch(MessagingException exc) {
		}
		return inboxFolder;
	}
  
	protected Folder getFolder(String foldername) throws MessagingException {
		return store.getFolder(foldername);
	}
	
	public void checkCreateFolder(String foldername) throws MessagingException {
		Folder folder = store.getFolder(foldername);
		if (!folder.exists()) {
			folder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);
		}
	}
	
  
  public boolean checkFolder(String foldername) throws MessagingException {
      Folder folder=store.getFolder(foldername);
	  return folder.exists();
  }	
	
	public static int getPriority(Message m) throws MessagingException {
		String xprio = null;
		String h[] = m.getHeader("X-Priority");
		if (h != null && h.length > 0) {
			xprio = h[0];
		}
		int priority = 3;
		if (xprio != null) {
			int ixp = xprio.indexOf(' ');
			if (ixp > 0) {
				xprio = xprio.substring(0, ixp);
			}
			try {
				priority = Integer.parseInt(xprio);
			} catch (RuntimeException exc) {
			}
		}
		return priority;
	}
	
	public boolean isInlineableMime(String contenttype) {
		return inlineableMimes.contains(contenttype.toLowerCase());
	}
	
	public synchronized int getNewMessageID() {
		return ++newMessageID;
	}
	
	public ArrayList<Attachment> getAttachments(long msgid) {
		ArrayList<Attachment> attachments = msgattach.get(new Long(msgid));
		if (attachments != null) {
			return attachments;
		}
		return emptyAttachments;
	}
	
	public ArrayList<Attachment> getCloudAttachments(long msgid) {
		ArrayList<Attachment> attachments = msgcloudattach.get(new Long(msgid));
		if (attachments != null) {
			return attachments;
		}
		return emptyAttachments;
	}
	
	public Attachment getAttachment(long msgid, String tempName) {
		ArrayList<Attachment> attachments = getAttachments(msgid);
		for (Attachment att : attachments) {
			if (att.getFile().getName().equals(tempName)) {
				return att;
			}
		}
		return null;
	}
	
	public Attachment getCloudAttachment(long msgid, String filename) {
		ArrayList<Attachment> attachments = getCloudAttachments(msgid);
		for (Attachment att : attachments) {
			if (att.getName().equals(filename)) {
				return att;
			}
		}
		return null;
	}
	
	public Attachment getAttachmentByCid(long msgid, String cid) {
		ArrayList<Attachment> attachments = getAttachments(msgid);
		for (Attachment att : attachments) {
			if (att.getCid().equals(cid)) {
				return att;
			}
		}
		return null;
	}
	
	public void clearAttachments(long msgid) {
		ArrayList<Attachment> attachments = getAttachments(msgid);
		for (Attachment a : attachments) {
			a.getFile().delete();
		}
		attachments.clear();

	// TODO: remove cloud attachements
/*    attachments=getCloudAttachments(msgid);
		 for(Attachment a: attachments) {
		 try {
		 VFSService vfs=(VFSService)wts.getServiceByName("vfs");
		 vfs.discard(a.getVFSUri());
		 } catch(Exception exc) {
		 Service.logger.error("Exception",exc);
		 }
		 }
		 attachments.clear();*/
	}
	
	public void putAttachments(long msgid, ArrayList<Attachment> attachments) {
		msgattach.put(new Long(msgid), attachments);
	}
	
	public void putCloudAttachments(long msgid, ArrayList<Attachment> attachments) {
		msgcloudattach.put(new Long(msgid), attachments);
	}
	
	public Attachment attachFile(long msgid, File file, String name, String contentType, String cid, boolean inline) {
		Attachment attachment = null;
		ArrayList<Attachment> attachments = getAttachments(msgid);
		if (attachments == null || attachments == emptyAttachments) {
			attachments = new ArrayList<Attachment>();
			putAttachments(msgid, attachments);
		}
		attachment = new Attachment(file, name, contentType, cid, inline);
		attachments.add(attachment);
		return attachment;
	}
	
	public Attachment attachCloud(long msgid, FileObject fileObject, String name, String contentType, String vfsuri) {
		Attachment attachment = null;
		ArrayList<Attachment> attachments = getCloudAttachments(msgid);
		if (attachments == null || attachments == emptyAttachments) {
			attachments = new ArrayList<Attachment>();
			putCloudAttachments(msgid, attachments);
		}
		attachment = new Attachment(fileObject, name, contentType, vfsuri);
		attachments.add(attachment);
		return attachment;
	}
	
	public String replaceCidUrls(String html, HTMLMailData maildata, String preurl) throws MessagingException {
		for (String cidname : maildata.getCidNames()) {
			//Part part=maildata.getCidPart(cidname);
			String surl = preurl + cidname;
			html = StringUtils.replace(html, "cid:" + cidname, surl);
		}
		return html;
	}
	
	public String removeWrongBase(String html) {
		String lhtml = html.toLowerCase();
		int ix = lhtml.indexOf("<base");
		if (ix >= 0) {
			int iy = lhtml.indexOf(">", ix);
			if (iy >= 0) {
				String base = lhtml.substring(ix, iy);
				if (base.indexOf("file:") >= 0) {
					String html1 = html.substring(0, ix);
					String html2 = html.substring(iy + 1);
					html = html1 + html2;
				}
			}
		}
		return html;
	}
	
	private FolderCache createFolderCache(Folder f) throws MessagingException {
		FolderCache fc = new FolderCache(f, this, environment);
		String fname = fc.getFolderName();
		foldersCache.put(fname, fc);
		return fc;
	}
	
	private void loadFoldersCache() throws MessagingException {
        Folder froot=getDefaultFolder();
        fcRoot=createFolderCache(froot);
		fcRoot.setIsRoot(true);
		Folder children[] = fcRoot.getFolder().list();
		final ArrayList<FolderCache> rootParents = new ArrayList<FolderCache>();
		for (Folder child : children) {
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
		
		Thread engine = new Thread(
				new Runnable() {
					public void run() {
						synchronized (mft) {
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
		engine.start();
	}
	
	private void _loadFoldersCache(FolderCache fc) throws MessagingException {
		Folder f = fc.getFolder();
		Folder children[] = f.list();
		for (Folder child : children) {
			String cname=child.getFullName();
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
	
	private synchronized FolderCache addSingleFoldersCache(FolderCache parent, Folder child) throws MessagingException {
		String cname = child.getFullName();
		FolderCache fcChild=foldersCache.get(cname);
		if (fcChild==null) {
			fcChild=createFolderCache(child);
			fcChild.setParent(parent);
			parent.addChild(fcChild);
			fcChild.setStartupLeaf(isLeaf((IMAPFolder)child));
		}
		
		return fcChild;
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
	
	protected FolderCache addFoldersCache(FolderCache parent, Folder child) throws MessagingException {
		//logger.debug("adding {} to {}",child.getName(),parent.getFolderName());
		FolderCache fcChild = addSingleFoldersCache(parent, child);
		boolean leaf = fcChild.isStartupLeaf();
		if (!leaf) {
			_loadFoldersCache(fcChild);
		}
		return fcChild;
	}
	
	private void destroyFolderCache(FolderCache fc) {
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
		fc.cleanup(true);
		try {
			fc.close();
		} catch (Exception exc) {
		}
	}
	
	private ArrayList<FolderCache> opened = new ArrayList<FolderCache>();

	protected void poolOpened(FolderCache fc) {
		if (opened.size() >= 5) {
			FolderCache rfc = opened.remove(0);
			rfc.cleanup(false);
			rfc.close();
			rfc.setForceRefresh();
		}
		opened.add(fc);
	}

	//Client service requests
	public void processGetImapTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String pfoldername = request.getParameter("node");
		Folder folder = null;
		try {
			boolean connected=checkStoreConnected();
			if (!connected) throw new Exception("Mail account authentication error");
			
			out.print("{ data:[");
			boolean isroot=pfoldername.equals("root");
            if (isroot) folder=getDefaultFolder();
			else folder = getFolder(pfoldername);
			
			Folder folders[] = folder.list();
			String fprefix = mprofile.getFolderPrefix();
			boolean level1 = (fprefix != null && fprefix.equals("INBOX."));
			if (isroot && hasDifferentDefaultFolder) {
				Folder fcs[]=new Folder[0];
				//check for other shared folders to be added
				Folder rfolders[]=store.getDefaultFolder().list();
				ArrayList<Folder> afcs=new ArrayList<Folder>();
				for(int i=0;i<sharedPrefixes.length;++i) {
					for(int j=0;j<rfolders.length;++j) {
						if (rfolders[j].getFullName().equals(sharedPrefixes[i]))
							afcs.add(rfolders[j]);
					}
				}
				if (afcs.size()>0) fcs=afcs.toArray(fcs);
				
				Folder xfolders[]=new Folder[1+folders.length+fcs.length];
				xfolders[0]=folder;
				System.arraycopy(folders, 0, xfolders, 1, folders.length);
				if (fcs.length>0) System.arraycopy(fcs,0,xfolders,1+folders.length,fcs.length);
				folders=xfolders;
			}
			outputFolders(folder, folders, level1, out);
			out.println("], message: '' }");
			
		} catch (Exception exc) {
			new JsonResult(exc).printTo(out);
			Service.logger.error("Exception",exc);
		}
	}
	
	Comparator<Folder> ucomp = new Comparator<Folder>() {
		
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
	
	Comparator<Folder> dcomp = new Comparator<Folder>() {
		
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
	
	private void outputFolders(Folder parent, Folder folders[], boolean level1, PrintWriter out) throws Exception {
		ArrayList<Folder> afolders = sortFolders(folders);
		//If Shared Folders, sort on description
		if (isSharedFolder(parent.getFullName())) {
			String ss = mprofile.getSharedSort();
			if (ss.equals("D")) {
				Collections.sort(afolders, dcomp);
			} else if (ss.equals("U")) {
				Collections.sort(afolders, ucomp);
			}
		}
		
		for (Folder f : afolders) {
			String foldername = f.getFullName();
			//in case of moved root, check not to duplicate root elsewhere
			if (hasDifferentDefaultFolder && isSharedFolder(parent.getFullName()) && foldername.equals(getInboxFolderFullName())) continue;
			
			FolderCache mc = getFolderCache(foldername);
			if (mc == null) {
				//continue;
				FolderCache fcparent=getFolderCache(parent.getFullName());
				//Service.logger.debug("==folder not ready, adding now==");
				mc=addSingleFoldersCache(fcparent, f);
			}
			//String shortfoldername=getShortFolderName(foldername);
			IMAPFolder imapf = (IMAPFolder) f;
			String atts[] = imapf.getAttributes();
			boolean leaf = true;
			boolean noinferiors = false;
			if (hasDifferentDefaultFolder && isDefaultFolder(foldername)) {
				
			} else {
				for(String att: atts) {
					if (att.equals("\\HasChildren")) {
						if (!level1 || !foldername.equals("INBOX")) leaf=false;
					}
					else if (att.equals("\\Noinferiors")) noinferiors=true;
				}
				if (noinferiors) leaf=true;
			}
			//boolean leaf=isLeaf((IMAPFolder)f);
			//logger.debug("folder {} isleaf={}, level1={}",f.getFullName(),leaf,level1);
			//if (leaf) {
			//	if (!level1 || !foldername.equals("INBOX")) leaf=false;
			//}
			
			String iconCls = "wtmail-icon-imap-folder-xs";
			int unread = 0;
			boolean hasUnread = false;
			boolean nounread = false;
			if (mc.isSharedFolder()) {
				iconCls = "wtmail-icon-shared-folder-xs";
				nounread = true;
			} else if (mc.isInbox()) {
				iconCls = "wtmail-icon-inbox-folder-xs";
			} else if (mc.isSent()) {
				iconCls = "wtmail-icon-sent-folder-xs";
				nounread = true;
			} else if (mc.isDrafts()) {
				iconCls = "wtmail-icon-drafts-folder-xs";
				nounread = true;
			} else if (mc.isTrash()) {
				iconCls = "wtmail-icon-trash-folder-xs";
				nounread = true;
			} else if (mc.isSpam()) {
				iconCls = "wtmail-icon-spam-folder-xs";
				nounread = true;
			} else if (mc.isDocMgt()) {
				iconCls = "wtmail-icon-docmgt-folder-xs";
			} else if (mc.isSharedInbox()) {
				iconCls = "wtmail-icon-inbox-folder-xs";
			}
			if (!nounread) {
				unread = mc.getUnreadMessagesCount();
				hasUnread = mc.hasUnreadChildren();
			}
			String text = mc.getDescription();
			String group = us.getMessageListGroup(foldername);
			if (group == null) {
				group = "";
			}
			
			String ss = "{id:'" + StringEscapeUtils.escapeEcmaScript(foldername)
					+ "',text:'" + StringEscapeUtils.escapeEcmaScript(text)
					+ "',folder:'" + StringEscapeUtils.escapeEcmaScript(text)
					+ "',leaf:" + leaf
					+ ",iconCls: '" + iconCls
					+ "',unread:" + unread
					+ ",hasUnread:" + hasUnread
					+ ",group: '"+group+"'";

			boolean isSharedToSomeone=false;
			try {
				isSharedToSomeone=mc.isSharedToSomeone();
			} catch(Exception exc) {
				
			}
			if (isSharedToSomeone) ss+=",isSharedToSomeone: true";
			if (mc.isSharedFolder()) ss+=",isSharedRoot: true";
			if (isUnderSharedFolder(foldername)) ss+=",isUnderShared: true";
			if (mc.isInbox()) {
				ss += ",isInbox: true";
			}
			if (mc.isSent()) {
				ss += ",isSent: true";
			}
			if (isUnderSentFolder(mc.getFolderName())) {
				ss += ",isUnderSent: true";
			}
			if (mc.isDrafts()) {
				ss += ",isDrafts: true";
			}
			if (mc.isTrash()) {
				ss += ",isTrash: true";
			}
			if (mc.isSpam()) {
				ss += ",isSpam: true";
			}
			if (mc.isScanForcedOff()) {
				ss += ", scanOff: true";
			} else if (mc.isScanForcedOn()) {
				ss += ", scanOn: true";
			} else if (mc.isScanEnabled()) {
				ss += ", scanEnabled: true";
			}
			ss += "},";
			out.print(ss);
			if (level1 && foldername.equals("INBOX")) {
				outputFolders(f, f.list(), false, out);
			}
		}
	}
	
	private ArrayList<Folder> sortFolders(Folder folders[]) {
		ArrayList<Folder> afolders = new ArrayList<Folder>();
		ArrayList<Folder> sfolders=new ArrayList<Folder>();
		HashMap<String,Folder> mfolders=new HashMap<String,Folder>();
		//add all non special fo the array and map special ones for later insert
		Folder inbox = null;
		Folder sent = null;
		Folder drafts = null;
		Folder trash = null;
		Folder spam = null;
		for (Folder f : folders) {
			String foldername = f.getFullName();
			String shortfoldername = getShortFolderName(foldername);
			if (!mfolders.containsKey(shortfoldername)) {
				mfolders.put(shortfoldername, f);
				if (isInboxFolder(shortfoldername)) inbox=f;
				else if (isSentFolder(shortfoldername)) sent=f;
				else if (isDraftsFolder(shortfoldername)) drafts=f;
				else if (isTrashFolder(shortfoldername)) trash=f;
				else if (isSpamFolder(shortfoldername)) spam=f;
				else if (isSharedFolder(foldername)) sfolders.add(f);
				else afolders.add(f);
			}
		}
		
		if (sortfolders) {
			Collections.sort(afolders,new Comparator<Folder>() {
				@Override
				public int compare(Folder f1, Folder f2) {
					return f1.getFullName().toLowerCase().compareTo(f2.getFullName().toLowerCase());
				}		
			});
			Collections.sort(sfolders,new Comparator<Folder>() {
				@Override
				public int compare(Folder f1, Folder f2) {
					return f1.getFullName().toLowerCase().compareTo(f2.getFullName().toLowerCase());
				}		
			});
		}
		
		//add any mapped special folder in order on top
		if (trash != null) {
			afolders.add(0, trash);
		}
		if (spam != null) {
			afolders.add(0, spam);
		}
		if (sent != null) {
			afolders.add(0, sent);
		}
		if (drafts != null) {
			afolders.add(0, drafts);
		}
		if (inbox != null) {
			afolders.add(0, inbox);
		}
		//add shared folders at the end
		afolders.addAll(sfolders);
		
		return afolders;
	}
	
	public void processMoveMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String fromfolder = request.getParameter("fromfolder");
		String tofolder = request.getParameter("tofolder");
		String allfiltered = request.getParameter("allfiltered");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		String uids[] = null;
		String sout = null;
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(fromfolder);
			FolderCache tomcache = getFolderCache(tofolder);
			
			//check if tofolder is my Spam, and there is spamadm, move there
			if (tofolder.equals(mprofile.getFolderSpam())) {
				String spamadmSpam=ss.getSpamadmSpam();
				if (spamadmSpam!=null) {
					FolderCache fc=getFolderCache(spamadmSpam);
					if (fc!=null) tomcache=fc;
				}
			}
			
			if (allfiltered == null) {
				uids = request.getParameterValues("ids");
				boolean norows=false;
				if (uids.length==1 && uids[0].length()==0) norows=true;
				if (!norows) {
					if (!mf) moveMessages(mcache, tomcache, toLongs(uids));
					else {
						long iuids[]=new long[1];
						for(String uid: uids) {
							int ix=uid.indexOf("|");
							fromfolder=uid.substring(0,ix);
							uid=uid.substring(ix+1);
							mcache = getFolderCache(fromfolder);
							iuids[0]=Long.parseLong(uid);
							moveMessages(mcache, tomcache, iuids);
						}
					}
				}
				long millis = System.currentTimeMillis();
				sout = "{\nresult: true, millis: " + millis + "\n}";
			} else {
                uids=getMessageUIDs(mcache,request);
                moveMessages(mcache, tomcache, toLongs(uids));
				tomcache.refreshUnreads();
				mcache.setForceRefresh();
				long millis = System.currentTimeMillis();
				sout = "{\nresult: true, unread: " + tomcache.getUnreadMessagesCount() + ", millis: " + millis + "\n}";
			}
		} catch(Exception exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processCopyMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String fromfolder = request.getParameter("fromfolder");
		String tofolder = request.getParameter("tofolder");
		String allfiltered = request.getParameter("allfiltered");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		String sout = null;
		String uids[]=null;
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(fromfolder);
			FolderCache tomcache = getFolderCache(tofolder);
			if (allfiltered == null) {
				uids = request.getParameterValues("ids");
				if (!mf) copyMessages(mcache, tomcache, toLongs(uids));
				else {
                    long iuids[]=new long[1];
                    for(String uid: uids) {
                        int ix=uid.indexOf("|");
                        fromfolder=uid.substring(0,ix);
                        uid=uid.substring(ix+1);
						mcache = getFolderCache(fromfolder);
                        iuids[0]=Long.parseLong(uid);
                        copyMessages(mcache, tomcache, iuids);
					}
				}
				long millis = System.currentTimeMillis();
				sout = "{\nresult: true, millis: " + millis + "\n}";
			} else {
                uids=getMessageUIDs(mcache,request);
                copyMessages(mcache, tomcache, toLongs(uids));
				tomcache.refreshUnreads();
				mcache.setForceRefresh();
				long millis = System.currentTimeMillis();
				sout = "{\nresult: true, unread: " + tomcache.getUnreadMessagesCount() + ", millis: " + millis + "\n}";
			}
        } catch(Exception exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processArchiveMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String fromfolder = request.getParameter("fromfolder");
		String tofolder = request.getParameter("tofolder");
		int ix = tofolder.indexOf("|");
		String idcategory = tofolder.substring(0, ix);
		String idsubcategory = tofolder.substring(ix + 1);
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		String uids[]=null;
		String sout = null;
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(fromfolder);
			uids = request.getParameterValues("ids");
			if (!mf) archiveMessages(mcache, toLongs(uids), idcategory, idsubcategory);
			else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = getFolderCache(fromfolder);
                    iuids[0]=Integer.parseInt(uid);
                    archiveMessages(mcache, iuids, idcategory, idsubcategory);
				}
			}
			long millis = System.currentTimeMillis();
			sout = "{\nresult: true, millis: " + millis + "\n}";
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	String[] getMessageUIDs(FolderCache mcache,HttpServletRequest request) throws MessagingException, IOException {
		String psortfield = request.getParameter("sort");
		String psortdir = request.getParameter("dir");
		String pquickfilter=request.getParameter("quickfilter");
		String psearchfield = request.getParameter("searchfield");
		String ppattern = request.getParameter("pattern");
		String pthreaded=null; //request.getParameter("threaded");
		if (pquickfilter!=null && pquickfilter.trim().length()==0) pquickfilter=null;
		if (psearchfield != null && psearchfield.trim().length() == 0) {
			psearchfield = null;
		}
		if (ppattern != null && ppattern.trim().length() == 0) {
			ppattern = null;
		}
		if (psortfield == null) {
			psortfield = "date";
		}
		if (psortdir == null) {
			psortdir = "DESC";
		}
		boolean threaded=(pthreaded!=null && pthreaded.equals("1"));
		int sortby = 0;
		if (psortfield.equals("messageid")) {
			sortby = MessageComparator.SORT_BY_MSGIDX;
		} else if (psortfield.equals("date")) {
			sortby = MessageComparator.SORT_BY_DATE;
		} else if (psortfield.equals("priority")) {
			sortby = MessageComparator.SORT_BY_PRIORITY;
		} else if (psortfield.equals("to")) {
			sortby = MessageComparator.SORT_BY_RCPT;
		} else if (psortfield.equals("from")) {
			sortby = MessageComparator.SORT_BY_SENDER;
		} else if (psortfield.equals("size")) {
			sortby = MessageComparator.SORT_BY_SIZE;
		} else if (psortfield.equals("subject")) {
			sortby = MessageComparator.SORT_BY_SUBJECT;
		} else if (psortfield.equals("status")) {
			sortby = MessageComparator.SORT_BY_STATUS;
		} else if (psortfield.equals("flag")) {
			sortby = MessageComparator.SORT_BY_FLAG;
		}
		boolean ascending = psortdir.equals("ASC");
		
		String group = us.getMessageListGroup(mcache.getFolderName());
		if (group == null) {
			group = "";
		}
		
		int sort_group = 0;
		boolean groupascending = true;
		if (group.equals("messageid")) {
			sort_group = MessageComparator.SORT_BY_MSGIDX;
		} else if (group.equals("gdate")) {
			sort_group = MessageComparator.SORT_BY_DATE;
			groupascending = false;
		} else if (group.equals("priority")) {
			sort_group = MessageComparator.SORT_BY_PRIORITY;
		} else if (group.equals("to")) {
			sort_group = MessageComparator.SORT_BY_RCPT;
		} else if (group.equals("from")) {
			sort_group = MessageComparator.SORT_BY_SENDER;
		} else if (group.equals("size")) {
			sort_group = MessageComparator.SORT_BY_SIZE;
		} else if (group.equals("subject")) {
			sort_group = MessageComparator.SORT_BY_SUBJECT;
		} else if (group.equals("status")) {
			sort_group = MessageComparator.SORT_BY_STATUS;
		} else if (group.equals("flag")) {
			sort_group = MessageComparator.SORT_BY_FLAG;
		}
		
		Message msgs[]=mcache.getMessages(pquickfilter,ppattern,psearchfield,sortby,ascending,false,sort_group,groupascending,threaded);
		ArrayList<String> aids = new ArrayList<String>();
		for (Message m : msgs) {
			aids.add(""+mcache.getUID(m));
			/*            String xids[];
			 try {
			 xids=m.getHeader("Message-ID");
			 } catch(MessagingException exc) {
			 continue;
			 }
			 if (xids!=null && xids.length>0) aids.add(xids[0]);*/
		}
        String uids[]=new String[aids.size()];
        aids.toArray(uids);
        return uids;
	}
	
	public void processDeleteMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		String sout = null;
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(fromfolder);
			if (!mf) {
				deleteMessages(mcache, toLongs(uids));
			} else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    int ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    deleteMessages(mcache, iuids);
				}
			}
			sout = "{\nresult: true\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processFlagMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String flag = request.getParameter("flag");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		String sout = null;
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(fromfolder);
			if (!mf) {
				if (flag.equals("clear")) {
					clearMessagesFlag(mcache, toLongs(uids));
				} else {
					flagMessages(mcache, toLongs(uids), flag);
				}
			} else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    int ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    if (flag.equals("clear")) clearMessagesFlag(mcache, iuids);
                    else flagMessages(mcache, iuids, flag);
				}
			}
			sout = "{\nresult: true\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processSeenMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		String sout = null;
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(fromfolder);
			if (!mf) {
				setMessagesSeen(mcache, toLongs(uids));
			} else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    int ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    setMessagesSeen(mcache, iuids);
				}
			}
			long millis = System.currentTimeMillis();
			sout = "{\nresult: true, millis: " + millis + "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processUnseenMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		String sout = null;
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(fromfolder);
			if (!mf) {
				setMessagesUnseen(mcache, toLongs(uids));
			} else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    int ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    setMessagesUnseen(mcache, iuids);
				}
			}
			long millis = System.currentTimeMillis();
			sout = "{\nresult: true, millis: " + millis + "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processSetScanFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String folder = request.getParameter("folder");
		boolean value = false;
		String svalue = request.getParameter("value");
		if (svalue != null) {
			value = svalue.equals("1");
		}
		boolean recursive = false;
		String srecursive = request.getParameter("recursive");
		if (srecursive != null) {
			recursive = srecursive.equals("1");
		}
		String sout = null;
		Connection con = null;
		try {
			con = getConnection();
			FolderCache fc = getFolderCache(folder);
			setScanFolder(con, fc, value, recursive);
			sout = "{\nresult: true\n}";
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException exc) {
				}
			}
		}
		out.println(sout);
	}
	
	private void setScanFolder(Connection con, FolderCache fc, boolean value, boolean recursive) throws SQLException {
		UserProfile profile = environment.getProfile();
		String iddomain=profile.getDomainId();
		String login = profile.getUserId();
		String folder = fc.getFolderName();
		if (value) {
			OScan oscan = new OScan(iddomain, login, folder);
			ScanDAO.getInstance().insert(con, oscan);
			fc.setScanEnabled(true);
		} else {
			ScanDAO.getInstance().deleteById(con, iddomain, login, folder);
			fc.setScanEnabled(false);
		}
		if (recursive) {
			ArrayList<FolderCache> children = fc.getChildren();
			if (children != null) {
				for (FolderCache child : children) {
					setScanFolder(con, child, value, recursive);
				}
			}
		}
	}
	
	public void processSeenFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String folder = request.getParameter("folder");
		boolean recursive = false;
		String srecursive = request.getParameter("recursive");
		if (srecursive != null) {
			recursive = srecursive.equals("1");
		}
		String sout = null;
		FolderCache mcache = null;
		try {
			checkStoreConnected();
			boolean result = true;
			sout = "{\n";
			mcache = getFolderCache(folder);
			setMessagesSeen(mcache, true, recursive);
			long millis = System.currentTimeMillis();
			sout += "result: " + result + ", millis: " + millis + "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, oldid: '" + folder + "', oldname: '" + (mcache != null ? mcache.getFolder().getName() : "unknown") + "', text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processUnseenFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String folder = request.getParameter("folder");
		boolean recursive = false;
		String srecursive = request.getParameter("recursive");
		if (srecursive != null) {
			recursive = srecursive.equals("1");
		}
		String sout = null;
		FolderCache mcache = null;
		try {
			checkStoreConnected();
			boolean result = true;
			sout = "{\n";
			mcache = getFolderCache(folder);
			setMessagesSeen(mcache, false, recursive);
			sout += "result: " + result + "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, oldid: '" + folder + "', oldname: '" + (mcache != null ? mcache.getFolder().getName() : "unknown") + "', text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	private void setMessagesSeen(FolderCache mcache, boolean seen, boolean recursive) throws MessagingException {
		if (seen) {
			mcache.setMessagesSeen();
		} else {
			mcache.setMessagesUnseen();
		}
		if (recursive) {
			ArrayList<FolderCache> children = mcache.getChildren();
			if (children != null) {
				for (FolderCache fc : children) {
					setMessagesSeen(fc, seen, recursive);
				}
			}
		}
	}
	
	public String getPartName(Part p) throws MessagingException {
		String pname = p.getFileName();
		if (pname == null) {
			String hctypes[] = p.getHeader("Content-Type");
			if (hctypes == null || hctypes.length == 0) {
				return null;
			}
			String hctype = hctypes[0];
			int ix = hctype.indexOf("name=");
			if (ix >= 0) {
				int sx = ix + 5;
				int ex = hctype.indexOf(";", sx);
				if (ex >= 0) {
					pname = hctype.substring(sx, ex);
				} else {
					pname = hctype.substring(sx);
				}
				pname = pname.trim();
				int xx = pname.length() - 1;
				if (pname.charAt(0) == '"' && pname.charAt(xx) == '"') {
					pname = pname.substring(1, xx);
				}
			}
			if (pname == null) {
				return null;
			}
		}
		try {
			pname = MailUtils.decodeQString(pname, "iso-8859-1");
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
		return pname;
	}

    public void processSaveColumnSize(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String name=request.getParameter("name");
		String size=request.getParameter("size");
		us.setColumnSize(name,Integer.parseInt(size));
		out.println("{ result: true }");
	}
	
	public void processSaveColumnsOrder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String orderInfo = ServletUtils.getStringParameter(request, "orderInfo", true);
			ColumnsOrderSetting cos = ColumnsOrderSetting.fromJson(orderInfo);
			if(cos == null) {
				us.clearColumnsOrderSetting();
			} else {
				us.setColumnsOrderSetting(cos);
			}
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error saving columns order.", ex);
			new JsonResult(false).printTo(out);
		}
	}
		
	public void processSaveColumnVisibility(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String folder = ServletUtils.getStringParameter(request, "folder", true);
			String name = ServletUtils.getStringParameter(request, "name", true);
			Boolean visible = ServletUtils.getBooleanParameter(request, "visible", false);

			ColumnVisibilitySetting cvs = us.getColumnVisibilitySetting(folder);
			FolderCache fc = getFolderCache(folder);

			cvs.put(name, visible);
			// Handle default cases...avoid data waste!
			//if(ColumnVisibilitySetting.isDefault(fc.isSent(), name, cvs.get(name))) cvs.remove(name);
			if(ColumnVisibilitySetting.isDefault(isUnderSentFolder(fc.getFolderName()), name, cvs.get(name))) cvs.remove(name);

			if(cvs.isEmpty()) {
				us.clearColumnVisibilitySetting(folder);
			} else {
				us.setColumnVisibilitySetting(folder, cvs);
			}
			new JsonResult().printTo(out);

		} catch(Exception ex) {
			logger.error("Error saving column visibility.", ex);
			new JsonResult(false).printTo(out);
		}
	}
	
	public void processNewFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String folder = request.getParameter("folder");
		String name = request.getParameter("name");
		String sout = null;
		FolderCache mcache = null;
		try {
			checkStoreConnected();
			Folder newfolder = null;
			boolean result = true;
			sout = "{\n";
			name = normalizeName(name);
            if (
					folder==null ||
					(hasDifferentDefaultFolder && folder.trim().length()==0)
				)
				mcache=fcRoot;
            else
				mcache=getFolderCache(folder);
			
			newfolder = mcache.createFolder(name);
			if (newfolder == null) {
				result = false;
			} else {
				if (mcache != fcRoot) {
					sout += "parent: '" + StringEscapeUtils.escapeEcmaScript(mcache.getFolderName()) + "',\n";
				} else {
					sout += "parent: null,\n";
				}
				sout += "name: '" + StringEscapeUtils.escapeEcmaScript(newfolder.getName()) + "',\n";
				sout += "fullname: '" + StringEscapeUtils.escapeEcmaScript(newfolder.getFullName()) + "',\n";
			}
			sout += "result: " + result + "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, oldid: '" + StringEscapeUtils.escapeEcmaScript(folder) + "', oldname: '" + StringEscapeUtils.escapeEcmaScript(mcache != null ? mcache.getFolder().getName() : "unknown") + "', text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processRenameFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String folder = request.getParameter("folder");
		String name = request.getParameter("name");
		String sout = null;
		FolderCache mcache = null;
		try {
			checkStoreConnected();
			boolean result = true;
			sout = "{\n";
			mcache = getFolderCache(folder);
			name = normalizeName(name);
			String newid = renameFolder(folder, name);
			sout += "oldid: '" + StringEscapeUtils.escapeEcmaScript(folder) + "',\n";
			sout += "newid: '" + StringEscapeUtils.escapeEcmaScript(newid) + "',\n";
			sout += "newname: '" + StringEscapeUtils.escapeEcmaScript(name) + "',\n";
			sout += "result: " + result + "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, oldid: '" + StringEscapeUtils.escapeEcmaScript(folder) + "', oldname: '" + StringEscapeUtils.escapeEcmaScript(mcache != null ? mcache.getFolder().getName() : "unknown") + "', text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	private String normalizeName(String name) throws MessagingException {
		String sep = "" + folderSeparator;
		while (name.contains(sep)) {
			name = name.replace(sep, "_");
		}
		return name;
	}
	
	public void processDeleteFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String folder = request.getParameter("folder");
		String sout = null;
		FolderCache mcache = null;
		try {
			checkStoreConnected();
			boolean result = true;
			sout = "{\n";
			mcache = getFolderCache(folder);
			if (isSpecialFolder(folder)) {
				result = false;
			} else {
				result = deleteFolder(folder);
				if (result) {
					sout += "oldid: '" + StringEscapeUtils.escapeEcmaScript(folder) + "',\n";
				}
			}
			sout += "result: " + result + "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, oldid: '" + StringEscapeUtils.escapeEcmaScript(folder) + "', oldname: '" + StringEscapeUtils.escapeEcmaScript(mcache != null ? mcache.getFolder().getName() : "unknown") + "', text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processTrashFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String folder = request.getParameter("folder");
		String sout = null;
		FolderCache mcache = null;
		try {
			checkStoreConnected();
			boolean result = true;
			sout = "{\n";
			mcache = getFolderCache(folder);
			if (isSpecialFolder(folder)) {
				result = false;
			} else {
				FolderCache newfc = trashFolder(folder);
				if (newfc!=null) { 
					sout+="newid: '"+StringEscapeUtils.escapeEcmaScript(newfc.getFolder().getFullName())+"',\n";
					sout+="trashid: '"+StringEscapeUtils.escapeEcmaScript(newfc.getParent().getFolder().getFullName())+"',\n";
				}
				result = true;
			}
			sout += "result: " + result + "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processMoveFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String folder = request.getParameter("folder");
		String to = request.getParameter("to");
		String sout = null;
		FolderCache mcache = null;
		try {
			checkStoreConnected();
			boolean result = true;
			sout = "{\n";
			mcache = getFolderCache(folder);
			if (isSpecialFolder(folder)) {
				result = false;
			} else {
				FolderCache newfc = moveFolder(folder, to);
				Folder newf = newfc.getFolder();
				sout += "oldid: '" + StringEscapeUtils.escapeEcmaScript(folder) + "',\n";
				sout += "newid: '" + StringEscapeUtils.escapeEcmaScript(newf.getFullName()) + "',\n";
				sout += "newname: '" + StringEscapeUtils.escapeEcmaScript(newf.getName()) + "',\n";
				if (to != null) {
					sout += "parent: '" + StringEscapeUtils.escapeEcmaScript(newf.getParent().getFullName()) + "',\n";
				}
				result = true;
			}
			sout += "result: " + result + "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, oldid: '" + StringEscapeUtils.escapeEcmaScript(folder) + "', oldname: '" + StringEscapeUtils.escapeEcmaScript(mcache != null ? mcache.getFolder().getName() : "unknown") + "', text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processEmptyFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String folder = request.getParameter("folder");
		String sout = null;
		FolderCache mcache = null;
		try {
			checkStoreConnected();
			sout = "{\n";
			mcache = getFolderCache(folder);
			emptyFolder(folder);
			sout += "oldid: '" + StringEscapeUtils.escapeEcmaScript(folder) + "',\n";
			sout += "result: true\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, oldid: '" + StringEscapeUtils.escapeEcmaScript(folder) + "', oldname: '" + StringEscapeUtils.escapeEcmaScript(mcache != null ? mcache.getFolder().getName() : "unknown") + "', text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processGetSource(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String foldername = request.getParameter("folder");
		String uid = request.getParameter("id");
		String sheaders = request.getParameter("headers");
		boolean headers = sheaders.equals("true");
		String sout = null;
		try {
			checkStoreConnected();
			StringBuffer sb = new StringBuffer("<pre>");
			FolderCache mcache = getFolderCache(foldername);
			Message msg = mcache.getMessage(Long.parseLong(uid));
			//Folder folder=msg.getFolder();
			for (Enumeration e = msg.getAllHeaders(); e.hasMoreElements();) {
				Header header = (Header) e.nextElement();
				sb.append(MailUtils.htmlescape(header.getName()) + ": " + MailUtils.htmlescape(header.getValue()) + "\n");
			}
			if (!headers) {
				BufferedReader br = new BufferedReader(new InputStreamReader(msg.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(MailUtils.htmlescape(line) + "\n");
				}
			}
			sb.append("</pre>");
			sout = "{\nresult: true, source: '" + StringEscapeUtils.escapeEcmaScript(sb.toString()) + "'\n}";
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		out.println(sout);
	}
	
	public void processSaveMail(HttpServletRequest request, HttpServletResponse response) {
		String foldername = request.getParameter("folder");
		String uid = request.getParameter("id");
		String sout = null;
		try {
			checkStoreConnected();
			StringBuffer sb = new StringBuffer();
			FolderCache mcache = getFolderCache(foldername);
			Message msg=mcache.getMessage(Long.parseLong(uid));
			String subject = msg.getSubject();
			String ctype = "binary/octet-stream";
			response.setContentType(ctype);
			response.setHeader("Content-Disposition", "inline; filename=\"" + subject + ".eml\"");
			OutputStream out = response.getOutputStream();
			msg.writeTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public void processDownloadMails(HttpServletRequest request, HttpServletResponse response) {
		String foldername = request.getParameter("folder");
		String sout = null;
		try {
			checkStoreConnected();
			StringBuffer sb = new StringBuffer();
			FolderCache mcache = getFolderCache(foldername);
			Message msgs[] = mcache.getAllMessages();
			String zipname = "Webtop-mails-" + getInternationalFolderName(mcache);
			response.setContentType("application/x-zip-compressed");
			response.setHeader("Content-Disposition", "inline; filename=\"" + zipname + ".zip\"");
			OutputStream out = response.getOutputStream();
			
			JarOutputStream jos = new java.util.jar.JarOutputStream(out);
			outputJarMailFolder(null, msgs, jos);
			
			int cut=foldername.length()+1;
			cycleMailFolder(mcache.getFolder(), cut, jos);
			
			jos.close();
			
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	private void cycleMailFolder(Folder folder, int cut, JarOutputStream jos) throws Exception {
		for(Folder child: folder.list()) {
			String fullname=child.getFullName();
			String relname=fullname.substring(cut).replace(folder.getSeparator(), '/');
			
			jos.putNextEntry(new JarEntry(relname+"/"));
			jos.closeEntry();
			
			cycleMailFolder(child,cut,jos);
			
			FolderCache mcache=getFolderCache(fullname);
			Message msgs[]=mcache.getAllMessages();
			if (msgs.length>0) outputJarMailFolder(relname, msgs, jos);
		}
	}
	
	private void outputJarMailFolder(String foldername, Message msgs[], JarOutputStream jos) throws Exception {
		int digits=(msgs.length>0?(int)Math.log10(msgs.length)+1:1);
		for(int i=0; i<msgs.length;++i) {
			Message msg=msgs[i];
			String subject=msg.getSubject();
			if (subject!=null) subject=subject.replace('/', '_').replace('\\', '_').replace(':','-');
			else subject="";
			java.util.Date date=msg.getReceivedDate();
			if (date==null) date=new java.util.Date();

			String fname=LangUtils.zerofill(i+1, digits)+" - "+subject+".eml";
			String fullname=null;
			if (foldername!=null && !foldername.isEmpty()) fullname=foldername+"/"+fname;
			else fullname=fname;
			JarEntry je=new JarEntry(fullname);
			je.setTime(date.getTime());
			jos.putNextEntry(je);
			msg.writeTo(jos);
			jos.closeEntry();
		}
		jos.flush();
	}

	public void processGetReplyMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile profile = environment.getProfile();
		//WebTopApp webtopapp=environment.getWebTopApp();
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String preplyall = request.getParameter("replyall");
		boolean replyAll = false;
		if (preplyall != null && preplyall.equals("1")) {
			replyAll = true;
		}
		String sout = null;
		try {
			String format=us.getFormat();
			boolean isHtml=format.equals("html");
			checkStoreConnected();
			FolderCache mcache = getFolderCache(pfoldername);
			Message m=mcache.getMessage(Long.parseLong(puidmessage));
			if (m.isExpunged()) {
				throw new MessagingException("Message " + puidmessage + " expunged");
			}
			SimpleMessage smsg = getReplyMsg(
					getNewMessageID(), m, replyAll, isSentFolder(pfoldername), isHtml,
					profile.getEmailAddress(), mprofile.isIncludeMessageInReply(),
					lookupResource(MailLocaleKey.MSG_FROMTITLE),
					lookupResource(MailLocaleKey.MSG_TOTITLE),
					lookupResource(MailLocaleKey.MSG_CCTITLE),
					lookupResource(MailLocaleKey.MSG_DATETITLE),
					lookupResource(MailLocaleKey.MSG_SUBJECTTITLE)
			);
			sout = "{\n result: true,";
            Identity ident=mprofile.getIdentity(pfoldername);
			String inreplyto = smsg.getInReplyTo();
			String references[] = smsg.getReferences();
			sout += " replyfolder: '" + StringEscapeUtils.escapeEcmaScript(pfoldername) + "',";
			if (inreplyto != null) {
				sout += " inreplyto: '" + StringEscapeUtils.escapeEcmaScript(inreplyto) + "',";
			}
			if (references != null) {
				String refs = "";
				for (String s : references) {
					refs += StringEscapeUtils.escapeEcmaScript(s) + " ";
				}
				sout += " references: '" + refs.trim() + "',";
			}
			String subject = smsg.getSubject();
			sout += " subject: '" + StringEscapeUtils.escapeEcmaScript(subject) + "',\n";
			sout += " recipients: [\n";
			String tos[] = smsg.getTo().split(";");
			boolean first = true;
			for (String to : tos) {
				if (!first) {
					sout += ",\n";
				}
				sout += "   {rtype:'to',email:'" + StringEscapeUtils.escapeEcmaScript(to) + "'}";
				first = false;
			}
			String ccs[] = smsg.getCc().split(";");
			for (String cc : ccs) {
				if (!first) {
					sout += ",\n";
				}
				sout += "   {rtype:'cc',email:'" + StringEscapeUtils.escapeEcmaScript(cc) + "'}";
				first = false;
			}
			sout += "\n ],\n";
            sout += " identityId: "+ident.getIdentityId()+",\n";
			sout += " origuid:"+puidmessage+",\n";
			if (isHtml) {
				String html = smsg.getContent();
				sout += " content:'" + StringEscapeUtils.escapeEcmaScript(html) + "',\n";
			} else {
				String text = smsg.getTextContent();
				sout += " content:'" + StringEscapeUtils.escapeEcmaScript(text) + "',\n";
			}
            sout += " format:'"+format+"'\n";
			sout += "\n}";
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
		if (sout != null) {
			out.println(sout);
		}
	}

	public void processGetForwardMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile profile = environment.getProfile();
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pnewmsgid = request.getParameter("newmsgid");
		String pattached = request.getParameter("attached");
		boolean attached = (pattached != null && pattached.equals("1"));
		long newmsgid = Long.parseLong(pnewmsgid);
		String sout = null;
		try {
			String format=us.getFormat();
			boolean isHtml=format.equals("html");
			checkStoreConnected();
			FolderCache mcache = getFolderCache(pfoldername);
			Message m=mcache.getMessage(Long.parseLong(puidmessage));
			if (m.isExpunged()) {
				throw new MessagingException("Message " + puidmessage + " expunged");
			}
			SimpleMessage smsg = getForwardMsg(
					newmsgid, m, isHtml,
					lookupResource(MailLocaleKey.MSG_FROMTITLE),
					lookupResource(MailLocaleKey.MSG_TOTITLE),
					lookupResource(MailLocaleKey.MSG_CCTITLE),
					lookupResource(MailLocaleKey.MSG_DATETITLE),
					lookupResource(MailLocaleKey.MSG_SUBJECTTITLE),
					attached
			);
			
			sout = "{\n result: true,";
            Identity ident=mprofile.getIdentity(pfoldername);
			String forwardedfrom = smsg.getForwardedFrom();
			sout += " forwardedfolder: '" + StringEscapeUtils.escapeEcmaScript(pfoldername) + "',";
			if (forwardedfrom != null) {
				sout += " forwardedfrom: '" + StringEscapeUtils.escapeEcmaScript(forwardedfrom) + "',";
			}
			String subject = smsg.getSubject();
			sout += " subject: '" + StringEscapeUtils.escapeEcmaScript(subject) + "',\n";
			
			String html=smsg.getContent();
			String text=smsg.getTextContent();
			if (!attached) {
				HTMLMailData maildata = mcache.getMailData((MimeMessage) m);
				boolean first = true;
				sout += " attachments: [\n";
				for (int i = 0; i < maildata.getAttachmentPartCount(); ++i) {
					Part part = maildata.getAttachmentPart(i);
					String filename = getPartName(part);
					if (filename != null) {
						String cids[] = part.getHeader("Content-ID");
						String cid = null;
						//String cid=filename;
						if (cids != null && cids[0] != null) {
							cid = cids[0];
							if (cid.startsWith("<")) cid=cid.substring(1);
							if (cid.endsWith(">")) cid=cid.substring(0,cid.length()-1);
						}
                        String mime=part.getContentType();
						UploadedFile upfile=addAsUploadedFile(pnewmsgid, filename, mime, part.getInputStream());
						//File tempFile = File.createTempFile("strts", null, new File(css.getTempPath()));
						//createFile(part.getInputStream(), tempFile);
						boolean inline = false;
						if (part.getDisposition() != null) {
							inline = part.getDisposition().equalsIgnoreCase(Part.INLINE);
						}
						//maildata.setCidProperties(cid, new CidProperties(cid,inline,upfile));
						//attachFile(newmsgid, tempFile, filename, part.getContentType(), cid, inline);
						//String tempname = tempFile.getName();
						if (!first) {
							sout += ",\n";
						}
						sout += "{ "+
								" uploadId: '" + StringEscapeUtils.escapeEcmaScript(upfile.getUploadId()) + "', "+
								" fileName: '" + StringEscapeUtils.escapeEcmaScript(filename) + "', "+
								" cid: "+(cid==null?null:"'" + StringEscapeUtils.escapeEcmaScript(cid) + "'")+", "+
                                " inline: "+inline+", "+
								" fileSize: "+upfile.getSize()+" "+
								" }";
						first = false;
						//TODO: change this weird matching of cids2urls!
						html = StringUtils.replace(html, "cid:" + cid, "service-request?csrf="+RunContext.getCSRFToken()+"&service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&uploadId=" + upfile.getUploadId() + "&cid="+cid);
					}
				}
				sout += "\n ],\n";
				//String surl = "service-request?service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&newmsgid=" + newmsgid + "&cid=";
				//html = replaceCidUrls(html, maildata, surl);
			} else {
				String filename = m.getSubject() + ".eml";
				UploadedFile upfile=addAsUploadedFile(pnewmsgid, filename, "message/rfc822", ((IMAPMessage)m).getMimeStream());
				//File tempFile = File.createTempFile("strts", null, new File(css.getTempPath()));
				//m.writeTo(new FileOutputStream(tempFile));
				//attachFile(newmsgid, tempFile, filename, "message/rfc822", null, false);
				//String tempname = tempFile.getName();
				sout += " attachments: [\n";
				sout += "{ "+
						" uploadId: '" + StringEscapeUtils.escapeEcmaScript(upfile.getUploadId()) + "', "+
						" fileName: '" + StringEscapeUtils.escapeEcmaScript(filename) + "', "+
                        " cid: null, "+
                        " inline: false, "+
						" fileSize: "+upfile.getSize()+" "+
						" }";
				sout += "\n ],\n";
			}
            sout += " identityId: "+ident.getIdentityId()+",\n";
			sout += " origuid:"+puidmessage+",\n";
			if (isHtml) {
				sout += " content:'" + StringEscapeUtils.escapeEcmaScript(html) + "',\n";
			} else {
				sout += " content:'" + StringEscapeUtils.escapeEcmaScript(text) + "',\n";
			}
            sout += " format:'"+format+"'\n";
			sout += "\n}";
			out.println(sout);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
			out.println(sout);
		}
	}
	
	public void processGetEditMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		//CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, getEnv().getProfile().getDomainId());
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pnewmsgid = request.getParameter("newmsgid");
		long newmsgid = Long.parseLong(pnewmsgid);
		String sout = null;
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(pfoldername);
			Message m = mcache.getMessage(Long.parseLong(puidmessage));
			String vheader[] = m.getHeader("Disposition-Notification-To");
			boolean receipt = false;
			int priority = 3;
			boolean recipients = false;
			if (isDraftsFolder(pfoldername)) {
				if (vheader != null && vheader[0] != null) {
					receipt = true;
				}
				priority = getPriority(m);
				recipients = true;
			}
			
			sout = "{\n result: true,";
			String subject = m.getSubject();
			if (subject == null) {
				subject = "";
			}
			sout += " subject: '" + StringEscapeUtils.escapeEcmaScript(subject) + "',\n";
			
			String inreplyto = null;
			String references[] = null;
			String vs[] = m.getHeader("In-Reply-To");
			if (vs != null && vs[0] != null) {
				inreplyto = vs[0];
			}
			references = m.getHeader("References");
			if (inreplyto != null) {
				vs = m.getHeader("Sonicle-reply-folder");
				String replyfolder = null;
				if (vs != null && vs[0] != null) {
					replyfolder = vs[0];
				}
				if (replyfolder != null) {
					sout += " replyfolder: '" + StringEscapeUtils.escapeEcmaScript(replyfolder) + "',";
				}
				sout += " inreplyto: '" + StringEscapeUtils.escapeEcmaScript(inreplyto) + "',";
			}
			if (references != null) {
				String refs = "";
				for (String s : references) {
					refs += StringEscapeUtils.escapeEcmaScript(s) + " ";
				}
				sout += " references: '" + refs.trim() + "',";
			}
			
			String forwardedfrom = null;
			vs = m.getHeader("Forwarded-From");
			if (vs != null && vs[0] != null) {
				forwardedfrom = vs[0];
			}
			if (forwardedfrom != null) {
				vs = m.getHeader("Sonicle-forwarded-folder");
				String forwardedfolder = null;
				if (vs != null && vs[0] != null) {
					forwardedfolder = vs[0];
				}
				if (forwardedfolder != null) {
					sout += " forwardedfolder: '" + StringEscapeUtils.escapeEcmaScript(forwardedfolder) + "',";
				}
				sout += " forwardedfrom: '" + StringEscapeUtils.escapeEcmaScript(forwardedfrom) + "',";
			}
			
			sout += " receipt: " + receipt + ",\n";
			sout += " priority: " + (priority >= 3 ? false : true) + ",\n";
			
            Identity ident=null;
			Address from[]=m.getFrom();
			if (from!=null && from.length>0) {
				InternetAddress ia=(InternetAddress)from[0];
				String email=ia.getAddress();
				String displayname=ia.getPersonal();
				if (displayname==null) displayname=email;
				//sout+=" from: { email: '"+StringEscapeUtils.escapeEcmaScript(email)+"', displayname: '"+StringEscapeUtils.escapeEcmaScript(displayname)+"' },\n";
                ident=mprofile.getIdentity(displayname, email);
			}
			
			sout += " recipients: [\n";
			if (recipients) {
				Address tos[] = m.getRecipients(RecipientType.TO);
				String srec = "";
				if (tos != null) {
					for (Address to : tos) {
						if (srec.length() > 0) {
							srec += ",\n";
						}
						srec += "   { "
								+ "rtype: 'to', "
								+ "email: '" + StringEscapeUtils.escapeEcmaScript(getDecodedAddress(to)) + "'"
								+ " }";
					}
				}
				Address ccs[] = m.getRecipients(RecipientType.CC);
				if (ccs != null) {
					for (Address cc : ccs) {
						if (srec.length() > 0) {
							srec += ",\n";
						}
						srec += "   { "
								+ "rtype: 'cc', "
								+ "email: '" + StringEscapeUtils.escapeEcmaScript(getDecodedAddress(cc)) + "'"
								+ " }";
					}
				}
				Address bccs[] = m.getRecipients(RecipientType.BCC);
				if (bccs != null) {
					for (Address bcc : bccs) {
						if (srec.length() > 0) {
							srec += ",\n";
						}
						srec += "   { "
								+ "rtype: 'bcc', "
								+ "email: '" + StringEscapeUtils.escapeEcmaScript(getDecodedAddress(bcc)) + "'"
								+ " }";
					}
				}
				
				sout += srec;
			} else {
                sout += "   { "
                        + "rtype: 'to', "
                        + "email: ''"
                        + " }";
                
            }
			sout += " ],\n";
			
			String html = "";
			ArrayList<String> htmlparts = mcache.getHTMLParts((MimeMessage) m, newmsgid, true);
			for (String xhtml : htmlparts) {
				html += xhtml + "<BR><BR>";
			}
            HTMLMailData maildata = mcache.getMailData((MimeMessage) m);
            boolean first = true;
            sout += " attachments: [\n";
            for (int i = 0; i < maildata.getAttachmentPartCount(); ++i) {
                Part part = maildata.getAttachmentPart(i);
                String filename = getPartName(part);
                if (filename != null) {
                    String cids[] = part.getHeader("Content-ID");
                    String cid = null;
                    //String cid=filename;
                    if (cids != null && cids[0] != null) {
                        cid = cids[0];
                        if (cid.startsWith("<")) cid=cid.substring(1);
                        if (cid.endsWith(">")) cid=cid.substring(0,cid.length()-1);
                    }
                    String mime=part.getContentType();
                    UploadedFile upfile=addAsUploadedFile(pnewmsgid, filename, mime, part.getInputStream());
                    //File tempFile = File.createTempFile("strts", null, new File(css.getTempPath()));
                    //createFile(part.getInputStream(), tempFile);
                    boolean inline = false;
                    if (part.getDisposition() != null) {
                        inline = part.getDisposition().equalsIgnoreCase(Part.INLINE);
                    }
                    //maildata.setCidProperties(cid, new CidProperties(cid,inline,upfile));
                    //attachFile(newmsgid, tempFile, filename, part.getContentType(), cid, inline);
                    //String tempname = tempFile.getName();
                    if (!first) {
                        sout += ",\n";
                    }
                    sout += "{ "+
                            " uploadId: '" + StringEscapeUtils.escapeEcmaScript(upfile.getUploadId()) + "', "+
                            " fileName: '" + StringEscapeUtils.escapeEcmaScript(filename) + "', "+
                            " cid: "+(cid==null?null:"'" + StringEscapeUtils.escapeEcmaScript(cid) + "'")+", "+
                            " inline: "+inline+", "+
                            " fileSize: "+upfile.getSize()+" "+
                            " }";
                    first = false;
					//TODO: change this weird matching of cids2urls!
                    html = StringUtils.replace(html, "cid:" + cid, "service-request?csrf="+RunContext.getCSRFToken()+"&service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&uploadId=" + upfile.getUploadId() + "&cid="+cid);
                }
            }
            sout += "\n ],\n";
            
            if (ident!=null) sout += " identityId: "+ident.getIdentityId()+",\n";
			sout += " origuid:"+puidmessage+",\n";
			sout += " content:'" + StringEscapeUtils.escapeEcmaScript(html) + "',\n";
            sout += " mime:'text/html'\n";
			sout += "\n}";
            

			out.println(sout);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			sout = "{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}";
		}
	}

	public void processManageMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String sendAction=ServletUtils.getStringParameter(request, "sendAction", "save");
			
			if (sendAction.equals("save")) {
				processSaveMessage(request, response, out);
			}
			else if (sendAction.equals("send")) {
				processSendMessage(request, response, out);
			}
			else if (sendAction.equals("schedule")) {
				processScheduleMessage(request, response, out);
			} else {
				throw new Exception("Invlid send action operation "+sendAction);
			}
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			Throwable cause = exc.getCause();
			String msg = cause != null ? cause.getMessage() : exc.getMessage();
            JsonResult json=new JsonResult(false, msg);
			json.printTo(out);
		}
	}
	
	public void processSendMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		JsonResult json = null;
		CoreManager coreMgr=WT.getCoreManager();

		// TODO: Cloud integration!!!
/*        VFSService vfs=(VFSService)wts.getServiceByName("vfs");
		 ArrayList<String> hashlinks=null;
		 if (vfs!=null) {
		 //look for links to cloud in the html
		 String html=request.getParameter("content");
		 hashlinks=new ArrayList<String>();
		 int hlx=-1;
		 String puburl=wtd.getSetting("vfs.pub.url");
		 char chars[]=new char[] { '\'', '"'};
		 for(char c: chars) {
		 String pattern="<a href="+c+puburl+"/public/vfs/";
		 Service.logger.debug("Looking for pattern "+pattern);
		 while((hlx=html.indexOf(pattern,hlx+1))>=0) {
		 int xhash1=hlx+pattern.length();
		 int xhash2=html.indexOf(c,xhash1);
		 if (xhash2>xhash1) {
		 String hash=html.substring(xhash1,xhash2);
		 Service.logger.debug("Found hash "+hash);
		 hashlinks.add(hash);
		 }
		 }
		 }
		 }*/
		try {
			//String emails[]=request.getParameterValues("recipients");
            Payload<MapItem, JsMessage> pl=ServletUtils.getPayload(request,JsMessage.class);
            JsMessage jsmsg=pl.data;
            long msgId=ServletUtils.getLongParameter(request, "msgId", true);
            boolean isFax=ServletUtils.getBooleanParameter(request, "isFax", false);
            boolean save=ServletUtils.getBooleanParameter(request, "save", false);
            //TODO: fax
			/*
			if (isFax) {
				int faxmaxtos=ss.getFaxMaxRecipients();
				
				//check for valid fax recipients
				String faxpattern=ss.getFaxPattern();
				String regex="^"+faxpattern.replace("{number}", "(\\d+)").replace("{username}", "(\\w+)")+"$";
				Pattern pattern=Pattern.compile(regex);
				int nemails=0;
				for(String email: emails) {
					if (StringUtils.isEmpty(email)) continue;
					++nemails;
					if (StringUtils.isNumeric(email)) continue;
					Matcher pm=pattern.matcher(email);
					if (!pm.matches()) {
						throw new Exception(lookupResource(MailLocaleKey.FAX_ADDRESS_ERROR));
					}
				}
				if (faxmaxtos>0 && nemails>faxmaxtos) {
					throw new WTException(lookupResource(MailLocaleKey.FAX_MAXADDRESS_ERROR), faxmaxtos);
				}

			}*/
			
			//Save used recipients
			for(JsRecipient rcpt: pl.data.recipients) {
                String email=rcpt.email;
				if (email!=null && email.trim().length()>0) 
					coreMgr.learnInternetRecipient(email);
			}
			//TODO save used subject
			//Save used subject
			/*String subject=request.getParameter("subject");
			if (subject!=null && subject.trim().length()>0) wts.setServiceStoreEntry(getName(), "subject", subject.toUpperCase(),subject);*/
        
			checkStoreConnected();
			//String attachments[] = request.getParameterValues("attachments");
			//if (attachments == null) {
			//	attachments = new String[0];
			//}
			SimpleMessage msg = prepareMessage(jsmsg,msgId,save,isFax);
			Identity ifrom = msg.getFrom();
			String from = environment.getProfile().getEmailAddress();
			if (ifrom != null) {
				from = ifrom.getEmail();
			}
			checkStoreConnected();
			Exception exc = sendMessage(msg, jsmsg.attachments);
			if (exc == null) {
				coreMgr.deleteMyAutosaveData(RunContext.getWebTopClientID(), SERVICE_ID, "newmail", ""+msgId);
				// TODO: Cloud integration!!!
/*                if (vfs!=null && hashlinks!=null && hashlinks.size()>0) {
				 for(String hash: hashlinks) {
				 Service.logger.debug("Adding emails to hash "+hash);
				 vfs.setAuthEmails(hash, from, emails);
				 }

				 }*/
				FolderCache fc = getFolderCache(mprofile.getFolderSent());
				fc.setForceRefresh();
				//check for in-reply-to and set the answered flags
				//String inreplyto = request.getParameter("inreplyto");
				//String replyfolder = request.getParameter("replyfolder");
				//String forwardedfrom = request.getParameter("forwardedfrom");
				//String forwardedfolder = request.getParameter("forwardedfolder");
                //String soriguid=request.getParameter("origuid");
				//long origuid=0;
				//try { origuid=Long.parseLong(soriguid); } catch(RuntimeException rexc) {}
				String foundfolder = null;
				if (jsmsg.forwardedfrom != null && jsmsg.forwardedfrom.trim().length() > 0) {
					try {
						foundfolder = foundfolder=flagForwardedMessage(jsmsg.forwardedfolder,jsmsg.forwardedfrom,jsmsg.origuid);
					} catch (Exception xexc) {
						Service.logger.error("Exception",xexc);
					}
				}
				else if((jsmsg.inreplyto != null && jsmsg.inreplyto.trim().length()>0)||(jsmsg.replyfolder!=null&&jsmsg.replyfolder.trim().length()>0&&jsmsg.origuid>0)) {
					try {
						foundfolder=flagAnsweredMessage(jsmsg.replyfolder,jsmsg.inreplyto,jsmsg.origuid);
					} catch (Exception xexc) {
						Service.logger.error("Exception",xexc);
					}
				}
				
                json=new JsonResult()
                        .set("foundfolder",foundfolder)
                        .set("saved", Boolean.FALSE);
			} else {
				Throwable cause = exc.getCause();
				String msgstr = cause != null ? cause.getMessage() : exc.getMessage();
                json=new JsonResult(false, msgstr);
			}
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			Throwable cause = exc.getCause();
			String msg = cause != null ? cause.getMessage() : exc.getMessage();
            json=new JsonResult(false, msg);
		}
        json.printTo(out);
	}
	
    private String flagAnsweredMessage(String replyfolder, String id, long origuid) throws MessagingException {
		String foundfolder = null;
		if (replyfolder != null) {
			if (_flagAnsweredMessage(replyfolder,origuid)) foundfolder=replyfolder;
		}
		if (foundfolder == null) {
			SonicleIMAPFolder f = (SonicleIMAPFolder) store.getFolder("");
			SonicleIMAPFolder.RecursiveSearchResult rsr = f.recursiveSearchByMessageID(id, skipReplyFolders);
			if (rsr != null) {
				_flagAnsweredMessage(rsr.foldername, rsr.uid);
				foundfolder = rsr.foldername;
			}
		}
		return foundfolder;
	}
	
	private boolean _flagAnsweredMessage(String foldername, long uid) throws MessagingException {
		Message msg = null;
		SonicleIMAPFolder sifolder = (SonicleIMAPFolder) store.getFolder(foldername);
		sifolder.open(Folder.READ_WRITE);
		msg = sifolder.getMessageByUID(uid);
		boolean found = msg != null;
		if (found) {
			msg.setFlags(FolderCache.repliedFlags, true);
		}
		sifolder.close(true);
		return found;
	}
	
    private String flagForwardedMessage(String forwardedfolder, String id, long origuid) throws MessagingException {
		String foundfolder = null;
		if (forwardedfolder != null) {
			if (_flagForwardedMessage(forwardedfolder,origuid)) foundfolder=forwardedfolder;
		}
		if (foundfolder == null) {
			SonicleIMAPFolder f = (SonicleIMAPFolder) store.getFolder("");
			SonicleIMAPFolder.RecursiveSearchResult rsr = f.recursiveSearchByMessageID(id, skipForwardFolders);
			if (rsr != null) {
				_flagForwardedMessage(rsr.foldername, rsr.uid);
				foundfolder = rsr.foldername;
			}
		}
		return foundfolder;
	}
	
	private boolean _flagForwardedMessage(String foldername, long uid) throws MessagingException {
		Message msg = null;
		SonicleIMAPFolder sifolder = (SonicleIMAPFolder) store.getFolder(foldername);
		sifolder.open(Folder.READ_WRITE);
		msg = sifolder.getMessageByUID(uid);
		boolean found = msg != null;
		if (found) {
			msg.setFlags(FolderCache.forwardedFlags, true);
		}
		sifolder.close(true);
		return found;
	}
	
	public void processSaveMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		JsonResult json = null;
		CoreManager coreMgr=WT.getCoreManager();
		try {
			checkStoreConnected();
            Payload<MapItem, JsMessage> pl=ServletUtils.getPayload(request,JsMessage.class);
            JsMessage jsmsg=pl.data;
            long msgId=ServletUtils.getLongParameter(request, "msgId", true);
			//String attachments[] = request.getParameterValues("attachments");
			String savefolder = ServletUtils.getStringParameter(request, "savefolder", false);
            
			//if (attachments == null) {
			//	attachments = new String[0];
			//}
			SimpleMessage msg = prepareMessage(jsmsg,msgId,false,false);
			checkStoreConnected();
			FolderCache fc = null;
			if (savefolder == null) {
				fc = getFolderCache(mprofile.getFolderDrafts());
			} else {
				fc = getFolderCache(savefolder);
			}
			Exception exc = saveMessage(msg, jsmsg.attachments, fc);
			if (exc == null) {
				coreMgr.deleteMyAutosaveData(RunContext.getWebTopClientID(), SERVICE_ID, "newmail", ""+msgId);
				
				fc.setForceRefresh();
                json=new JsonResult()
                        .set("saved", Boolean.TRUE);
			} else {
                json=new JsonResult(false, exc.getMessage());
			}
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
            json=new JsonResult(false, exc.getMessage());
		}
        json.printTo(out);
	}
	
	public void processScheduleMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		JsonResult json = null;
		CoreManager coreMgr=WT.getCoreManager();
		try {
			checkStoreConnected();
            Payload<MapItem, JsMessage> pl=ServletUtils.getPayload(request,JsMessage.class);
            JsMessage jsmsg=pl.data;
            long msgId=ServletUtils.getLongParameter(request, "msgId", true);
			String savefolder = ServletUtils.getStringParameter(request, "savefolder", false);
			String scheddate = ServletUtils.getStringParameter(request, "scheddate", true);
			String schedtime = ServletUtils.getStringParameter(request, "schedtime", true);
			String schednotify = ServletUtils.getStringParameter(request, "schednotify", true);
			
			/*if (attachments == null) {
				attachments = new String[0];
			}*/
			SimpleMessage msg = prepareMessage(jsmsg,msgId,false,false);
			checkStoreConnected();
			FolderCache fc = null;
			if (savefolder == null) {
				fc = getFolderCache(mprofile.getFolderDrafts());
			} else {
				fc = getFolderCache(savefolder);
			}
			Exception exc = scheduleMessage(msg, jsmsg.attachments, fc, scheddate, schedtime, schednotify);
			if (exc == null) {
				coreMgr.deleteMyAutosaveData(RunContext.getWebTopClientID(), SERVICE_ID, "newmail", ""+msgId);
				
				fc.setForceRefresh();
                json=new JsonResult()
                        .set("saved", Boolean.TRUE);
			} else {
                json=new JsonResult(false, exc.getMessage());
			}
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
            json=new JsonResult(false, exc.getMessage());
		}
        json.printTo(out);
	}
	
	public void processDiscardMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		JsonResult json = null;
		CoreManager coreMgr=WT.getCoreManager();
		try {
			long msgId=ServletUtils.getLongParameter(request, "msgId", true);
			clearAttachments(msgId);
			coreMgr.deleteMyAutosaveData(RunContext.getWebTopClientID(), SERVICE_ID, "newmail", ""+msgId);
			json=new JsonResult();
		} catch(Exception exc) {
			Service.logger.error("Exception",exc);
            json=new JsonResult(false, exc.getMessage());
		}
        json.printTo(out);
	}

	private SimpleMessage prepareMessage(JsMessage jsmsg, long msgId, boolean save, boolean isFax) throws Exception {
		PrivateEnvironment env = environment;
		UserProfile profile = env.getProfile();
		//expand multiple addresses
		ArrayList<String> aemails = new ArrayList<>();
		ArrayList<String> artypes = new ArrayList<>();
		for (JsRecipient jsrcpt: jsmsg.recipients) {
            String emails[]=StringUtils.split(jsrcpt.email,';');
			for(String email: emails) {
				aemails.add(email);
				artypes.add(jsrcpt.rtype);
			}
		}
		String emails[] = new String[aemails.size()];
        emails=(String[]) aemails.toArray(emails);
		String rtypes[] = new String[artypes.size()];
        rtypes=(String[]) artypes.toArray(rtypes);
		
		//String replyfolder = request.getParameter("replyfolder");
		//String inreplyto = request.getParameter("inreplyto");
		//String references = request.getParameter("references");
		
		//String forwardedfolder = request.getParameter("forwardedfolder");
		//String forwardedfrom = request.getParameter("forwardedfrom");
		
		//String subject = request.getParameter("subject");
		//String mime = request.getParameter("mime");
		//String sident = request.getParameter("identity");
		//String content = request.getParameter("content");
		//String msgid = request.getParameter("newmsgid");
		//String ssave = request.getParameter("save");
		//boolean save = (ssave != null && ssave.equals("true"));
		//String sreceipt = request.getParameter("receipt");
		//boolean receipt = (sreceipt != null && sreceipt.equals("true"));
		//String spriority = request.getParameter("priority");
		//boolean priority = (spriority != null && spriority.equals("true"));
		
		//boolean isFax = request.getParameter("fax") != null;
		
		String to = null;
		String cc = null;
		String bcc = null;
		for (int i = 0; i < emails.length; ++i) {
			//String email=decoder.decode(ByteBuffer.wrap(emails[i].getBytes())).toString();
			String email = emails[i];
			if (email == null || email.trim().length() == 0) {
				continue;
			}
			//Check for list
			boolean checkemail = true;
			boolean listdone = false;
			if (email.indexOf('@') < 0) {
				if (isFax && StringUtils.isNumeric(email)) {
					String faxpattern = ss.getFaxPattern();
					String faxemail = faxpattern.replace("{number}", email).replace("{username}", profile.getUserId());
					email = faxemail;
				}
			} else {
				//check for list if one email with domain equals one allowed service id
				InternetAddress ia=null;
				try {
					ia=new InternetAddress(email);
				} catch(AddressException exc) {
					
				}
				if (ia!=null) {
					String iamail=ia.getAddress();
					String dom=iamail.substring(iamail.indexOf("@")+1);
					CoreManager core=WT.getCoreManager();
					if (environment.getWebTopSession().isServiceAllowed(dom)) {
						List<InternetRecipient> rcpts=core.expandToInternetRecipients(iamail);
						for (InternetRecipient rcpt: rcpts) {
							String xemail=rcpt.getAddress();
							String xpersonal=rcpt.getPersonal();
							String xrtype=rcpt.getRecipientType().toString();

							if (xpersonal!=null) xemail=xpersonal+" <"+xemail+">";

							try {
							  checkEmail(xemail);
							  InternetAddress.parse(xemail.replace(',', ' '),true);
							} catch(AddressException exc) {
								throw new AddressException(lookupResource(MailLocaleKey.ADDRESS_ERROR)+" : "+xemail);
							}
							if (rtypes[i].equals("to")) {
								if (xrtype.equals("to")) {
								  if (to==null) to=xemail;
								  else to+="; "+xemail;
								} else if (xrtype.equals("cc")) {
								  if (cc==null) cc=xemail;
								  else cc+="; "+xemail;
								} else if (xrtype.equals("bcc")) {
								  if (bcc==null) bcc=xemail;
								  else bcc+="; "+xemail;
								}
							} else if (rtypes[i].equals("cc")) {
							  if (cc==null) cc=xemail;
							  else cc+="; "+xemail;
							} else if (rtypes[i].equals("bcc")) {
							  if (bcc==null) bcc=xemail;
							  else bcc+="; "+xemail;
							}

							listdone=true;
							checkemail=false;
						}
					}
				}
			}
			if (listdone) {
				continue;
			}
			
			if (checkemail) {
				try {
					checkEmail(email);
					//InternetAddress.parse(email.replace(',', ' '), false);
					getInternetAddress(email);
				} catch (AddressException exc) {
					Service.logger.error("Exception",exc);
					throw new AddressException(lookupResource(MailLocaleKey.ADDRESS_ERROR) + " : " + email);
				}
			}
			
			if (rtypes[i].equals("to")) {
				if (to == null) {
					to = email;
				} else {
					to += "; " + email;
				}
			} else if (rtypes[i].equals("cc")) {
				if (cc == null) {
					cc = email;
				} else {
					cc += "; " + email;
				}
			} else if (rtypes[i].equals("bcc")) {
				if (bcc == null) {
					bcc = email;
				} else {
					bcc += "; " + email;
				}
			}
		}
		
		//long id = Long.parseLong(msgid);
		SimpleMessage msg = new SimpleMessage(msgId);
		/*int idx = jsmsg.identity - 1;
		Identity from = null;
		if (idx >= 0) {
			from = mprofile.getIdentity(idx);
		}*/
        Identity from=mprofile.getIdentity(jsmsg.identityId);
		msg.setFrom(from);
		msg.setTo(to);
		msg.setCc(cc);
		msg.setBcc(bcc);
		msg.setSubject(jsmsg.subject);
		
        //TODO: fax
		/*if (isFax) {
			String coverpage = request.getParameter("faxcover");
			if (coverpage != null) {
				if (coverpage.equals("none")) {
					msg.addHeaderLine("X-FAX-AutoCoverPage: No");
				} else {
					msg.addHeaderLine("X-FAX-AutoCoverPage: Yes");
					msg.addHeaderLine("X-FAX-Cover-Template: " + coverpage);
				}
			}
		}*/
		
        //TODO: custom headers keys
		/*String[] headersKeys = request.getParameterValues("headersKeys");
		String[] headersValues = request.getParameterValues("headersValues");
		if (headersKeys != null && headersValues != null && headersKeys.length == headersValues.length) {
			for (int i = 0; i < headersKeys.length; i++) {
				if (!headersKeys[i].equals("")) {
					msg.addHeaderLine(headersKeys[i] + ": " + headersValues[i]);
				}
			}
		}*/
		
		if (jsmsg.inreplyto != null) {
			msg.setInReplyTo(jsmsg.inreplyto);
		}
		if (jsmsg.references != null) {
			msg.setReferences(new String[]{jsmsg.references});
		}
		if (jsmsg.replyfolder != null) {
			msg.setReplyFolder(jsmsg.replyfolder);
		}
		
		if (jsmsg.forwardedfolder != null) {
			msg.setForwardedFolder(jsmsg.forwardedfolder);
		}
		if (jsmsg.forwardedfrom != null) {
			msg.setForwardedFrom(jsmsg.forwardedfrom);
		}
		
		msg.setReceipt(jsmsg.receipt);
		msg.setPriority(jsmsg.priority ? 1 : 3);
		if (jsmsg.format == null || jsmsg.format.equals("plain")) {
			msg.setContent(jsmsg.content);
		} else {
			if (jsmsg.format.equalsIgnoreCase("html")) {
				//TODO: change this weird matching of cids2urls!
				
				//CIDs
                String content=jsmsg.content;
                String pattern1=RegexUtils.escapeRegexSpecialChars("service-request?csrf="+RunContext.getCSRFToken()+"&amp;service="+SERVICE_ID+"&amp;action=PreviewAttachment&amp;nowriter=true&amp;uploadId=");
                String pattern2=RegexUtils.escapeRegexSpecialChars("&amp;cid=");
                content=StringUtils.replacePattern(content, pattern1+".{36}"+pattern2, "cid:");
                pattern1=RegexUtils.escapeRegexSpecialChars("service-request?csrf="+RunContext.getCSRFToken()+"&service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&uploadId=");
                pattern2=RegexUtils.escapeRegexSpecialChars("&cid=");
                content=StringUtils.replacePattern(content, pattern1+".{36}"+pattern2, "cid:");
				
                //URLs
                pattern1=RegexUtils.escapeRegexSpecialChars("service-request?csrf="+RunContext.getCSRFToken()+"&amp;service="+SERVICE_ID+"&amp;action=PreviewAttachment&amp;nowriter=true&amp;uploadId=");
                pattern2=RegexUtils.escapeRegexSpecialChars("&amp;url=");
                content=StringUtils.replacePattern(content, pattern1+".{36}"+pattern2, "");
                pattern1=RegexUtils.escapeRegexSpecialChars("service-request?csrf="+RunContext.getCSRFToken()+"&service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&uploadId=");
                pattern2=RegexUtils.escapeRegexSpecialChars("&url=");
                content=StringUtils.replacePattern(content, pattern1+".{36}"+pattern2, "");
                
				String textcontent = MailUtils.HtmlToText_convert(MailUtils.htmlunescapesource(content));
				String htmlcontent = MailUtils.htmlescapefixsource(content);
				msg.setContent(htmlcontent, textcontent, "text/html");
			} else {
				msg.setContent(jsmsg.content, null, "text/"+jsmsg.format);
			}
			
		}
		return msg;
	}
	
	private void checkEmail(String email) throws AddressException {
		int ix = email.indexOf('@');
		if (ix < 1) {
			throw new AddressException(email);
		}
		int ix2 = email.indexOf('@', ix + 1);
		if (ix2 >= 0) {
			int ixx = email.indexOf('<');
			if (ixx >= 0) {
				if (!(ix < ixx && ix2 > ixx)) {
					throw new AddressException(email);
				}
			} else {
				throw new AddressException(email);
			}
		}
	}
	
	public void processAttachFromMail(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			checkStoreConnected();
			
			String tag = request.getParameter("tag");
			String pfoldername = request.getParameter("folder");
			String puidmessage = request.getParameter("idmessage");
			String pidattach = request.getParameter("idattach");
			
			FolderCache mcache = getFolderCache(pfoldername);
			long uidmessage = Long.parseLong(puidmessage);
			Message m = mcache.getMessage(uidmessage);
			HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
			Part part = mailData.getAttachmentPart(Integer.parseInt(pidattach));
			
			String ctype = part.getContentType();
			int ix = ctype.indexOf(";");
			if (ix > 0) {
				ctype = ctype.substring(0, ix);
			}
			
			String filename = part.getFileName();
			if (filename == null) {
				filename = "";
			}
			try {
				filename = MailUtils.decodeQString(filename);
			} catch (Exception exc) {
			}
			
			ctype = ServletHelper.guessMediaType(filename,ctype);

			File file = WT.createTempFile();
			int filesize=IOUtils.copy(part.getInputStream(), new FileOutputStream(file));
			WebTopSession.UploadedFile uploadedFile = new WebTopSession.UploadedFile(false, this.SERVICE_ID, file.getName(), tag, filename, filesize, ctype);
			environment.getWebTopSession().addUploadedFile(uploadedFile);
			
			MapItem data = new MapItem(); // Empty response data
			data.add("uploadId", uploadedFile.getUploadId());
			data.add("name", uploadedFile.getFilename());
			data.add("size", uploadedFile.getSize());
			new JsonResult(data).printTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false, exc.getMessage()).printTo(out);
		}
	}
	
	public void processAttachFromMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			checkStoreConnected();
			
			String tag = request.getParameter("tag");
			String pfoldername = request.getParameter("folder");
			String suids[] = request.getParameterValues("ids");
			String multifolder = request.getParameter("multifolder");
			boolean mf = multifolder != null && multifolder.equals("true");
			
			String ctype="message/rfc822";
			
			ArrayList<WebTopSession.UploadedFile> ufiles=new ArrayList<>();
			for(String suid: suids) {
				String foldername=pfoldername;
				
				if (mf) {
					int ix=suid.indexOf("|");
					foldername=suid.substring(0,ix);
					suid=suid.substring(ix+1);
				}
				long uid=Long.parseLong(suid);
				FolderCache mcache = getFolderCache(foldername);
				
				Message msg=mcache.getMessage(uid);
				File file = WT.createTempFile();
				FileOutputStream fos=new FileOutputStream(file);
				msg.writeTo(fos);
				fos.close();
				long filesize=file.length();
				String filename=msg.getSubject()+".eml";
			
				WebTopSession.UploadedFile uploadedFile = new WebTopSession.UploadedFile(false, this.SERVICE_ID, file.getName(), tag, filename, filesize, ctype);
				environment.getWebTopSession().addUploadedFile(uploadedFile);
				ufiles.add(uploadedFile);
			}			
			
			MapItemList data=new MapItemList();
			for(WebTopSession.UploadedFile ufile: ufiles) {
				MapItem mi = new MapItem();
				mi.add("uploadId", ufile.getUploadId());
				mi.add("name", ufile.getFilename());
				mi.add("size", ufile.getSize());
				data.add(mi);
			}
			new JsonResult(data).printTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false, exc.getMessage()).printTo(out);
		}
	}
	
	public void processCopyAttachment(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			checkStoreConnected();
			UserProfile profile=environment.getProfile();
			Locale locale=profile.getLocale();
			
			String pfromfolder = request.getParameter("fromfolder");
			String ptofolder = request.getParameter("tofolder");
			String puidmessage = request.getParameter("idmessage");
			String pidattach = request.getParameter("idattach");
			
			FolderCache frommcache = getFolderCache(pfromfolder);
			FolderCache tomcache = getFolderCache(ptofolder);
			long uidmessage = Long.parseLong(puidmessage);
			Message m = frommcache.getMessage(uidmessage);
			
			HTMLMailData mailData = frommcache.getMailData((MimeMessage) m);
			Object content = mailData.getAttachmentPart(Integer.parseInt(pidattach)).getContent();
			if (content instanceof MimeMessage) {
				MimeMessage msg=new MimeMessage((MimeMessage)content);
				msg.setFlag(Flags.Flag.SEEN, true);
				tomcache.appendMessage(msg);
				new JsonResult().printTo(out);
			} else {
				new JsonResult(false, lookupResource(locale, MailLocaleKey.ERROR_ATTACHMENT_TYPE_NOT_SUPPORTED)).printTo(out);
			}
			IMAPNestedMessage impn;
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false, exc.getMessage()).printTo(out);
		}
	}
	
	// TODO: Cloud integration
/*    public void processUploadCloudAttachments(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
	 doUploadCloudAttachments(request, response, out);
	 }

	 private void doUploadCloudAttachments(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
	 try {
	 checkStoreConnected();
	 VFSService vfs=(VFSService)wts.getServiceByName("vfs");
	 FileObject rfo=vfs.getEmailsCloudFolder();
	 MultipartIterator iterator=new MultipartIterator(request,64*1024,vfs.getMaxInternalUploadSize(),rfo);
	 MultipartElement element=null; 
	 ArrayList<MultipartElement> elements=new ArrayList<MultipartElement>();
	 int msgid=0;
	 while((element=iterator.getNextElement())!=null) {
	 if (element.isFileObject() && element.getFileName()!=null && element.getFileName().length()>0) {
	 elements.add(element);
	 } else {
	 String name=element.getName();
	 if (name.equals("newmsgid")) {
	 msgid=Integer.parseInt(element.getValue());
	 }
	 }
	 }
	 if (msgid==0) {
	 String smsgid=request.getParameter("newmsgid");
	 if (smsgid!=null) msgid=Integer.parseInt(smsgid);
	 }
	 String sout="{ success: true, data: [\n";
	 boolean first=true;
	 for(MultipartElement el: elements) {
	 String filename=el.getFileName();
	 FileObject fileObject=el.getFileObject();
	 String vfsuri="0,"+vfs.getEmailsCloudFolderName()+"/"+filename;
                
	 String ctype="binary/octet-stream";
	 int ix=filename.lastIndexOf(".");
	 if (ix>0) {
	 String extension=filename.substring(ix+1);
	 String xctype=wta.getContentType(extension);
	 if (xctype!=null) ctype=xctype;
	 }

	 attachCloud(msgid,fileObject,filename,ctype,vfsuri);

	 if (!first) sout+=",\n";
	 sout+="{ name: '"+OldUtils.jsEscape(filename)+"', vfsuri: '"+OldUtils.jsEscape(vfsuri)+"' }";
                
	 first=false;
	 }
	 sout+="], result: null, id: 'id', jsonrpc: '2.0' }";
	 out.println(sout);
	 } catch(Exception exc) {
	 Service.logger.error("Exception",exc);
	 }
	 }

	 public void processRequestCloudFile(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
	 String subject=request.getParameter("subject");
	 try {
	 checkStoreConnected();
	 VFSService vfs=(VFSService)wts.getServiceByName("vfs");
	 FileObject rfo=vfs.getUploadsCloudFolder();
	 java.util.Date now=new java.util.Date();
	 java.util.Calendar cal=java.util.Calendar.getInstance();
	 cal.setTime(now);
	 String dirname=cal.get(java.util.Calendar.YEAR)+"-"+(cal.get(java.util.Calendar.MONTH)+1)+"-"+cal.get(java.util.Calendar.DAY_OF_MONTH)+" "+cal.get(java.util.Calendar.HOUR)+":"+cal.get(java.util.Calendar.MINUTE)+":"+cal.get(java.util.alendar.SECOND)+" - "+subject;
	 FileObject nfo=rfo.resolveFile(dirname);
	 nfo.createFolder();
	 String vfsuri="0,"+vfs.getUploadsCloudFolderName()+"/"+dirname;
	 String sout="{ success: true, vfsuri: '"+OldUtils.jsEscape(vfsuri)+"', name: '"+OldUtils.jsEscape(dirname)+"' }";
	 out.println(sout);
	 } catch(Exception exc) {
	 Service.logger.error("Exception",exc);
	 String sout="{ success: false, message: '"+exc.getMessage()+"' }";
	 out.println(sout);
	 }
	 }*/
	public void processSendReceipt(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile profile = environment.getProfile();
		String subject = request.getParameter("subject");
		String from = request.getParameter("from");
		String to = request.getParameter("to");
		String folder = request.getParameter("folder");
		String sout = "";
		Identity ident = mprofile.getIdentity(folder);
		if (ident != null) {
			from = ident.toString();
		}
		String body = "Il messaggio inviato a " + from + " con soggetto [" + subject + "] è stato letto.\n\n"
				+ "Your message sent to " + from + " with subject [" + subject + "] has been read.\n\n";
		try {
			checkStoreConnected();
			Exception exc = sendReceipt(from, to, subject, body);
			if (exc == null) {
				new JsonResult().printTo(out);
			} else {
				Service.logger.error("Exception",exc);
				new JsonResult(exc).printTo(out);
			}
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(exc).printTo(out);
		}
	}

	public void processManageQuickParts(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String crud = null;
			HashMap<String,String> items;
		try {
			crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				items = us.getMessageQuickParts();
				new JsonResult(JsQuickPart.asList(items)).printTo(out);

			} else if (crud.equals(Crud.CREATE)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				String html = ServletUtils.getStringParameter(request, "html", true);
				us.setMessageQuickPart(id, html);

				items = us.getMessageQuickParts();
				new JsonResult(JsQuickPart.asList(items)).printTo(out);

			} else if (crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsQuickPart> pl = ServletUtils.getPayload(request, JsQuickPart.class);
				us.deleteMessageQuickPart(pl.data.id);

				new JsonResult().printTo(out);
			}
		} catch (Exception ex) {
		   logger.error("Error managing quickparts", ex);
		   new JsonResult(false, "Error managing quickparts").printTo(out);
		}
	}
	
/*	public void processListPublicImages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		SettingsManager sm = wta.getSettingsManager();
		ArrayList<JsonHashMap> items = null;
		JsonHashMap item = null;
		
		try {
			items = new ArrayList<JsonHashMap>();
			String publicArea = sm.getSetting("webtop.public");
			File pubimgDir = new File(publicArea + "/main/images/");
			if(pubimgDir.isDirectory() && pubimgDir.exists()) {
				File[] files = pubimgDir.listFiles();
				for(File file : files) {
					String url = "webtop/public/images/"+file.getName();
					item = new JsonHashMap(url, file.getName());
					items.add(item);
				}
			}
			new JsonResult(items).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error ListPublicImages", ex);
			new JsonResult(false, "Error ListPublicImages").printTo(out);
		}
	}	*/
	
	private String getSharedFolderName(String mailUser, String folder) throws MessagingException {
		FolderCache folderCache = null;
		String sharedFolderName = null;
		String folderName = null;
		
		// Clear mailUser removing any domain info (ldap auth contains 
		// domain suffix), we don't want it!
		String user = StringUtils.split(mailUser, "@")[0];
		// INBOX is a fake name, it's equals to user's direct folder
		boolean isInbox=folder.equals("INBOX");
		
		FolderCache[] sharedCache = getSharedFoldersCache();
		for(FolderCache sharedFolder : sharedCache) {
			sharedFolderName = sharedFolder.getFolderName();
			folderCache = getFolderCache(sharedFolderName);
			for(Folder fo : folderCache.getFolder().list()) {
				folderName = fo.getFullName(); 
				char sep=fo.getSeparator();
				//if is a shared mailbox, and it contains an @, match it with mail user (NS7)
				//or just user instead (XStream and NS6)
				String name = isInbox? (fo.getName().indexOf('@')>0?mailUser:user): folder;
				if(folderName.equals(sharedFolderName + sep + name)) return folderName;
			}
		}
		return null;
	}
	
	HashMap<String, MessageListThread> mlThreads = new HashMap<String, MessageListThread>();

	public void processGroupChanged(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String group=request.getParameter("group");
		String folder=request.getParameter("folder");
		us.setMessageListGroup(folder, group);
		if (!group.equals("")) us.setMessageListSort(folder, "date|DESC");
		new JsonResult(true).printTo(out);
	}
	
	
    public void processSavePageRows(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String spagerows=request.getParameter("pagerows");
		int pagerows=Integer.parseInt(spagerows);
		us.setPageRows(pagerows);
		mprofile.setNumMsgList(pagerows);
		new JsonResult(true,"").printTo(out);
	}	
 	
	public void processListMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager core = WT.getCoreManager();
		UserProfile profile = environment.getProfile();
		Locale locale = profile.getLocale();
		java.util.Calendar cal = java.util.Calendar.getInstance(locale);
		String pfoldername = request.getParameter("folder");
		//String psortfield = request.getParameter("sort");
		//String psortdir = request.getParameter("dir");
		String pstart = request.getParameter("start");
		String plimit = request.getParameter("limit");
		String ppage = request.getParameter("page");
		String psearchfield = request.getParameter("searchfield");
		String ppattern = request.getParameter("pattern");
		String pquickfilter=request.getParameter("quickfilter");
		String prefresh = request.getParameter("refresh");
        String pthreaded=request.getParameter("threaded");
		String pthreadaction=request.getParameter("threadaction");
		String pthreadactionuid=request.getParameter("threadactionuid");
		if (psearchfield != null && psearchfield.trim().length() == 0) {
			psearchfield = null;
		}
		if (ppattern != null && ppattern.trim().length() == 0) {
			ppattern = null;
		}
		if (pquickfilter!=null && pquickfilter.trim().length()==0) pquickfilter=null;
		boolean refresh = (prefresh != null && prefresh.equals("true"));
		//boolean threaded=(pthreaded!=null && pthreaded.equals("1"));
		
		//String threadedSetting="list-threaded-"+pfoldername;
		//if (pthreaded==null || pthreaded.equals("2")) {
		//	threaded=us.isMessageListThreaded(pfoldername);
		//} else {
		//	us.setMessageListThreaded(pfoldername, threaded);
		//}
		
		if (isSpecialFolder(pfoldername) || isSharedFolder(pfoldername)) {
			logger.debug("folder is special or shared, refresh forced");
			refresh=true;
		}

		String group = us.getMessageListGroup(pfoldername);
		if (group == null) {
			group = "";
		}
		boolean threaded=group.equals("threadId");

		String psortfield = "date";
		String psortdir = "DESC";
		try {
			boolean nogroup=group.equals("");
			JsSort.List sortList=ServletUtils.getObjectParameter(request,"sort",null,JsSort.List.class);
			if (sortList==null) {
				if (nogroup) {
					String s = us.getMessageListSort(pfoldername);
					int ix = s.indexOf("|");
					psortfield = s.substring(0, ix);
					psortdir = s.substring(ix + 1);
				} else {
					psortfield = "date";
					psortdir = "DESC";
				}
			} else {
				JsSort jsSort=sortList.get(0);
				psortfield=jsSort.property;
				psortdir=jsSort.direction;
				if (!nogroup&&!psortfield.equals("date")) {
					group = "";
				}
				us.setMessageListGroup(pfoldername, group);
				us.setMessageListSort(pfoldername, psortfield, psortdir);
			}
		} catch(Exception exc) {
			logger.error("Exception",exc);
		}
		
		if (threaded && (!psortfield.equals("date")||!psortdir.equals("DESC"))) threaded=false;
		
		int sortby = MessageComparator.SORT_BY_NONE;
		if (psortfield.equals("messageid")) {
			sortby = MessageComparator.SORT_BY_MSGIDX;
		} else if (psortfield.equals("date")) {
			sortby = MessageComparator.SORT_BY_DATE;
		} else if (psortfield.equals("priority")) {
			sortby = MessageComparator.SORT_BY_PRIORITY;
		} else if (psortfield.equals("to")) {
			sortby = MessageComparator.SORT_BY_RCPT;
		} else if (psortfield.equals("from")) {
			sortby = MessageComparator.SORT_BY_SENDER;
		} else if (psortfield.equals("size")) {
			sortby = MessageComparator.SORT_BY_SIZE;
		} else if (psortfield.equals("subject")) {
			sortby = MessageComparator.SORT_BY_SUBJECT;
		} else if (psortfield.equals("status")) {
			sortby = MessageComparator.SORT_BY_STATUS;
		} else if (psortfield.equals("flag")) {
			sortby = MessageComparator.SORT_BY_FLAG;
		}
		boolean ascending = psortdir.equals("ASC");
		
		int sort_group = MessageComparator.SORT_BY_NONE;
		boolean groupascending = true;
		if (group.equals("messageid")) {
			sort_group = MessageComparator.SORT_BY_MSGIDX;
		} else if (group.equals("gdate")) {
			sort_group = MessageComparator.SORT_BY_DATE;
			groupascending = false;
		} else if (group.equals("priority")) {
			sort_group = MessageComparator.SORT_BY_PRIORITY;
		} else if (group.equals("to")) {
			sort_group = MessageComparator.SORT_BY_RCPT;
		} else if (group.equals("from")) {
			sort_group = MessageComparator.SORT_BY_SENDER;
		} else if (group.equals("size")) {
			sort_group = MessageComparator.SORT_BY_SIZE;
		} else if (group.equals("subject")) {
			sort_group = MessageComparator.SORT_BY_SUBJECT;
		} else if (group.equals("status")) {
			sort_group = MessageComparator.SORT_BY_STATUS;
		} else if (group.equals("flag")) {
			sort_group = MessageComparator.SORT_BY_FLAG;
		}

		//Save search requests
		if (psearchfield != null && psearchfield.trim().length() > 0 && ppattern != null && ppattern.trim().length() > 0) {
			// TODO: save filter!!!!
			//wts.setServiceStoreEntry(getName(), "filter"+psearchfield, ppattern.toUpperCase(),ppattern);
		}
		
		//Check for multiple filters
		try {
			JsFilter.List filterList=ServletUtils.getObjectParameter(request,"filter",JsFilter.List.class,false);
			if (filterList!=null) {
				psearchfield="";
				ppattern="";
				boolean first=true;
				for(JsFilter filter: filterList) {
					if (!first) {
						psearchfield+="|";
						ppattern+="|";
					}
					if (filter.property.equals("unread")) {
						psearchfield+="status";
						ppattern+=filter.value.equals("true")?"unread":"read";
					} else {
						psearchfield+=filter.property;
						ppattern+=filter.value;
					}
					first=false;
				}
			}
		} catch(Exception exc) {
			logger.error("Excetpion",exc);
		}
		
		int start=Integer.parseInt(pstart);
		int limit=Integer.parseInt(plimit);
		int page=0;
		if (ppage!=null) {
			page=Integer.parseInt(ppage);
			start=(page-1)*limit;
		}
		/*int start = 0;
		int limit = mprofile.getNumMsgList();
		if (ppage==null) {
			if (pstart != null) {
				start = Integer.parseInt(pstart);
			}	
			if (plimit != null) {
				limit = Integer.parseInt(plimit);
			}
		} else {
			int page=Integer.parseInt(ppage);
			int nxpage=mprofile.getNumMsgList();
			start=(page-1)*nxpage;
			limit=nxpage;
		}*/
		
		String sout = "{\n";
		Folder folder = null;
		boolean connected=false;
		try {
			connected=checkStoreConnected();
			if (!connected) throw new Exception("Mail account authentication error");

				
			int funread = 0;
			if (pfoldername == null) {
				folder = getDefaultFolder();
			} else {
				folder = getFolder(pfoldername);
			}
			boolean issent = isSentFolder(folder.getFullName());
			boolean isundersent=isUnderSentFolder(folder.getFullName());
			boolean isdrafts = isDraftsFolder(folder.getFullName());
			boolean isundershared=isUnderSharedFolder(pfoldername);
			if (!issent) {
				String names[] = folder.getFullName().split("\\" + getFolderSeparator());
				for (String pname : names) {
					if (isSentFolder(pname)) {
						issent = true;
						break;
					}
				}
			}
			
			String ctn = Thread.currentThread().getName();
			String key = folder.getFullName();
			FolderCache mcache = getFolderCache(key);
			//Message msgs[]=mcache.getMessages(ppattern,psearchfield,sortby,ascending,refresh);
			if (ppattern != null && psearchfield != null) {
				key += "|" + ppattern + "|" + psearchfield;
			}
			MessageListThread mlt = mlThreads.get(key);
			if (mlt == null) {
				mlThreads.put(key, mlt = new MessageListThread(mcache,pquickfilter,ppattern,psearchfield,sortby,ascending,refresh,sort_group,groupascending,threaded));
			}
			String tname = Thread.currentThread().getName();			
			long millis = System.currentTimeMillis();
            Message xmsgs[]=null;
			synchronized (mlt.lock) {
				mlt.lastRequest = millis;
				if (!mlt.started) {
					Thread t = new Thread(mlt);
					t.start();
				}
				if (!mlt.finished) {
					try {
						mlt.lock.wait();
					} catch (InterruptedException exc) {
						Service.logger.error("Exception",exc);
					}
					mlThreads.remove(key);
				}
				if (mlt.lastRequest==millis) {
					xmsgs=mlt.msgs;
				}
			}
			
			//if threaded, look for the start considering roots and opened children
			if (xmsgs!=null && threaded && page>1) {
				int i=0,ni=0,np=1;
				long tId=0;
				while(np < page && ni < xmsgs.length ) {
					SonicleIMAPMessage xm=(SonicleIMAPMessage)xmsgs[ni];
					++ni;
					if (xm.isExpunged())
						continue;
					
					long nuid=mcache.getUID(xm);

					int tIndent=xm.getThreadIndent();
					if (tIndent==0) tId=nuid;
					else {
						if (!mcache.isThreadOpen(tId))
							continue;
					}
					
					++i;
					if ((i%limit)==0) ++np;
				}
				if (np==page) {
					start=ni;
					//System.out.println("page "+np+" start is "+start);
				}
			}

			int max = start + limit;
			if (max>xmsgs.length) max=xmsgs.length;
			ArrayList<Long> autoeditList=new ArrayList<Long>();
			if (pthreadaction!=null && pthreadaction.trim().length()>0) {
				long actuid=Long.parseLong(pthreadactionuid);
				mcache.setThreadOpen(actuid, pthreadaction.equals("open"));
			}
            if (xmsgs!=null) {
                int total=0;
				
				sout+="messages: [\n";

				/*               if (ppattern==null && !isSpecialFolder(mcache.getFolderName())) {
				 //mcache.fetch(msgs,FolderCache.flagsFP,0,start);
				 for(int i=0;i<start;++i) {
				 try {
				 if (!msgs[i].isSet(Flags.Flag.SEEN)) funread++;
				 } catch(Exception exc) {

				 }
				 }
				 }*/
				total=threaded?mcache.getThreadedCount():xmsgs.length;
				if (start<max) {
					
					Folder fsent=getFolder(mprofile.getFolderSent());
					boolean openedsent=false;
					//Fetch others for these messages
					mcache.fetch(xmsgs,(isdrafts?draftsFP:FP), start, max);
					long tId=0;
					for (int i = 0, ni = 0; i < limit; ++ni, ++i) {
						int ix = start + i;
						int nx = start + ni;
						if (nx>=xmsgs.length) break;
						if (ix >= max) break;

						SonicleIMAPMessage xm=(SonicleIMAPMessage)xmsgs[nx];
						if (xm.isExpunged()) {
							--i;
							continue;
						}
						/*String ids[]=null;
						 try {
						 ids=xm.getHeader("Message-ID");
						 } catch(MessagingException exc) {
						 --i;
						 continue;
						 }
						 if (ids==null || ids.length==0) { --i; continue; }
						 String idmessage=ids[0];*/

						long nuid=mcache.getUID(xm);

						int tIndent=xm.getThreadIndent();
						if (tIndent==0) tId=nuid;
						else if (threaded) {
							if (!mcache.isThreadOpen(tId)) {
								--i;
								continue;
							}
						}
						boolean tChildren=false;
						int tUnseenChildren=0;
						if (threaded) {
							int cnx=nx+1;
							while(cnx<xmsgs.length) {
								SonicleIMAPMessage cxm=(SonicleIMAPMessage)xmsgs[cnx];
								if (cxm.isExpunged()) {
									cnx++;
									continue;
								}
								while(cxm.getThreadIndent()>0) {
									tChildren=true;
									if (!cxm.isExpunged() && !cxm.isSet(Flags.Flag.SEEN)) ++tUnseenChildren;
									++cnx;
									if (cnx>=xmsgs.length) break;
									cxm=(SonicleIMAPMessage)xmsgs[cnx];
								}
								break;
							}
						}



						Flags flags=xm.getFlags();

						//Date
						java.util.Date d=xm.getSentDate();
						if (d==null) d=xm.getReceivedDate();
						if (d==null) d=new java.util.Date(0);
						cal.setTime(d);
						int yyyy=cal.get(java.util.Calendar.YEAR);
						int mm=cal.get(java.util.Calendar.MONTH);
						int dd=cal.get(java.util.Calendar.DAY_OF_MONTH);
						int hhh=cal.get(java.util.Calendar.HOUR_OF_DAY);
						int mmm=cal.get(java.util.Calendar.MINUTE);
						int sss=cal.get(java.util.Calendar.SECOND);
						//From
						String from="";
						Address ia[]=xm.getFrom();
						if (ia!=null) {
							InternetAddress iafrom=(InternetAddress)ia[0];
							from=iafrom.getPersonal();
							if (from==null) from=iafrom.getAddress();
						}
						if (from==null) from="";

						//To
						String to="";
						ia=xm.getRecipients(Message.RecipientType.TO);
						//if not sent and not shared, show me first if in TO
						if (ia!=null) {
							InternetAddress iato=(InternetAddress)ia[0];
							if (!issent && !isundershared) {
								for(Address ax: ia) {
									InternetAddress iax=(InternetAddress)ax;
									if (iax.getAddress().equals(profile.getEmailAddress())) {
										iato=iax;
										break;
									}
								}
							}
							to=iato.getPersonal();
							if (to==null) to=iato.getAddress();
						}
						to=(to==null?"":StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(to)));
						//Subject
						String subject=xm.getSubject();
						if (subject!=null) {
							try {
								subject=MailUtils.decodeQString(subject);
							} catch(Exception exc) {

							}
						}
						else subject="";

	/*						if (threaded) {
							if (tIndent>0) {
								StringBuffer sb=new StringBuffer();
								for(int w=0;w<tIndent;++w) sb.append("&nbsp;");
								subject=sb+subject;
							}
						}*/

						boolean hasAttachments=hasAttachements(xm);

						from=StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(from));
						subject=StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(subject));

						//Unread
						boolean unread=!xm.isSet(Flags.Flag.SEEN);
						if (ppattern==null && unread) ++funread;
						//Priority
						int priority=getPriority(xm);
						//Status
						java.util.Date today=new java.util.Date();
						java.util.Calendar cal1=java.util.Calendar.getInstance(locale);
						java.util.Calendar cal2=java.util.Calendar.getInstance(locale);
						boolean isToday=false;
						String gdate="";
						String sdate = "";
						String xdate = "";
						if (d!=null) {
							java.util.Date gd=threaded?xm.getMostRecentThreadDate():d;

							cal1.setTime(today);
							cal2.setTime(gd);

							gdate=DateFormat.getDateInstance(DateFormat.MEDIUM,locale).format(gd);
							sdate=cal2.get(java.util.Calendar.YEAR)+"/"+String.format("%02d",(cal2.get(java.util.Calendar.MONTH)+1))+"/"+String.format("%02d",cal2.get(java.util.Calendar.DATE));
							//boolean isGdate=group.equals("gdate");
							if (cal1.get(java.util.Calendar.MONTH)==cal2.get(java.util.Calendar.MONTH) && cal1.get(java.util.Calendar.YEAR)==cal2.get(java.util.Calendar.YEAR)) {
								int dx=cal1.get(java.util.Calendar.DAY_OF_MONTH)-cal2.get(java.util.Calendar.DAY_OF_MONTH);
								if (dx==0) {
									isToday=true;
									//if (isGdate) {
									//	gdate=WT.lookupCoreResource(locale, CoreLocaleKey.WORD_DATE_TODAY)+"  "+gdate;
									//}
									xdate = WT.lookupCoreResource(locale, CoreLocaleKey.WORD_DATE_TODAY);
								} else if (dx == 1 /*&& isGdate*/) {
									xdate = WT.lookupCoreResource(locale, CoreLocaleKey.WORD_DATE_YESTERDAY);
								}							}
						}

						String status="read";
						if (flags!=null) {
							if (flags.contains(Flags.Flag.ANSWERED)) {
								if (flags.contains("$Forwarded")) status = "repfwd";
								else status = "replied";
							} else if (flags.contains("$Forwarded")) {
								status="forwarded";
							} else if (flags.contains(Flags.Flag.SEEN)) {
								status="read";
							} else if (isToday) {
								status="new";
							} else {
								status="unread";
							}
		//                    if (flags.contains(Flags.Flag.USER)) flagImage=webtopapp.getUri()+"/images/themes/"+profile.getTheme()+"/mail/flag.gif";
						}
						//Size
						int msgsize=0;
						msgsize=(xm.getSize()*3)/4;// /1024 + 1;
						//User flags
						String cflag="";
						for (WebtopFlag webtopFlag: webtopFlags) {
							String flagstring=webtopFlag.label;
							String tbflagstring=webtopFlag.tbLabel;
							if (!flagstring.equals("complete")) {
								String oldflagstring="flag"+flagstring;
								if (flags.contains(flagstring)
										||flags.contains(oldflagstring)
										|| (tbflagstring!=null && flags.contains(tbflagstring))
								) {
									cflag=flagstring;
								}
							}
						}
						boolean flagComplete=flags.contains("complete");
						if (flagComplete) {
							if (cflag.length()>0) cflag+="-complete";
							else cflag="complete";
						}

						if (cflag.length()==0 && flags.contains(Flags.Flag.FLAGGED)) cflag="special";

						boolean hasNote=flags.contains(sflagNote);

						boolean autoedit=false;

						boolean issched=false;
						int syyyy=0;
						int smm=0;
						int sdd=0;
						int shhh=0;
						int smmm=0;
						int ssss=0;
						if (isdrafts) {
							String h=getSingleHeaderValue(xm,"Sonicle-send-scheduled");
							if (h!=null && h.equals("true")) {
								java.util.Calendar scal=parseScheduleHeader(getSingleHeaderValue(xm,"Sonicle-send-date"),getSingleHeaderValue(xm,"Sonicle-send-time"));
								syyyy=scal.get(java.util.Calendar.YEAR);
								smm=scal.get(java.util.Calendar.MONTH);
								sdd=scal.get(java.util.Calendar.DAY_OF_MONTH);
								shhh=scal.get(java.util.Calendar.HOUR_OF_DAY);
								smmm=scal.get(java.util.Calendar.MINUTE);
								ssss=scal.get(java.util.Calendar.SECOND);
								issched=true;
								status="scheduled";
							} 

							h=getSingleHeaderValue(xm,HEADER_SONICLE_FROM_DRAFTER);
							if (h!=null && h.equals("true")) {
								autoedit=true;
							}
						}

						String xmfoldername=xm.getFolder().getFullName();

						//idmessage=idmessage.replaceAll("\\\\", "\\\\");
						//idmessage=Utils.jsEscape(idmessage);
						if (i>0) sout+=",\n";
						boolean archived=false;
						if (hasDocumentArchiving()) {
							archived=xm.getHeader("X-WT-Archived")!=null;
							if (!archived) {
								archived=flags.contains(sflagArchived);
							}
						}
						sout += "{idmessage:'" + nuid + "',"
							+ "priority:" + priority + ","
							+ "status:'" + status + "',"
							+ "to:'" + to + "',"
							+ "from:'" + from + "',"
							+ "subject:'" + subject + "',"
							+ (threaded?"threadId: "+tId+",":"")
							+ (threaded?"threadIndent:"+tIndent+",":"")
							+ "date: new Date(" + yyyy + "," + mm + "," + dd + "," + hhh + "," + mmm + "," + sss + "),"
							+ "gdate: '" + gdate + "',"
							+ "sdate: '" + sdate + "',"
							+ "xdate: '" + xdate + "',"
							+ "unread: " + unread + ","
							+ "size:" + msgsize + ","
							+ "flag:'" + cflag + "'"
							+ (hasNote ? ",note:true" : "")
							+ (archived ? ",arch:true" : "")
							+ (isToday ? ",istoday:true" : "")
							+ (hasAttachments ? ",atts:true" : "")
							+ (issched ? ",scheddate: new Date(" + syyyy + "," + smm + "," + sdd + "," + shhh + "," + smmm + "," + ssss + ")" : "")
							+ (threaded&&tIndent==0 ? ",threadOpen: "+mcache.isThreadOpen(nuid) : "")
							+ (threaded&&tIndent==0 ? ",threadHasChildren: "+tChildren : "")
							+ (threaded&&tIndent==0 ? ",threadUnseenChildren: "+tUnseenChildren : "")
							+ (threaded&&xm.hasThreads()&&!xm.isMostRecentInThread()?",fmtd: true":"")
							+ (threaded&&!xmfoldername.equals(folder.getFullName())?",fromfolder: '"+StringEscapeUtils.escapeEcmaScript(xmfoldername)+"'":"")
							+ "}";

						if (autoedit) {
							autoeditList.add(nuid);
						}

						//                sout+="{messageid:'"+m.getMessageID()+"',from:'"+from+"',subject:'"+subject+"',date: new Date("+yyyy+","+mm+","+dd+"),unread: "+unread+"},\n";
					}
					
					if (openedsent) fsent.close(false);
				}
				/*                if (ppattern==null && !isSpecialFolder(mcache.getFolderName())) {
				 //if (max<msgs.length) mcache.fetch(msgs,FolderCache.flagsFP,max,msgs.length);
				 for(int i=max;i<msgs.length;++i) {
				 try {
				 if (!msgs[i].isSet(Flags.Flag.SEEN)) funread++;
				 } catch(Exception exc) {

				 }
				 }
				 } else {
				 funread=mcache.getUnreadMessagesCount();
				 }*/
				if (mcache.isScanForcedOrEnabled()) {
					//Send message only if first page
					if (start==0) mcache.refreshUnreads();
					funread=mcache.getUnreadMessagesCount();
				}
				else funread=0;
				
				sout += "\n],\n";
				sout += "total: "+total+",\n";
				sout += "metaData: {\n"
						+ "  root: 'messages', total: 'total', idProperty: 'idmessage',\n"
						+ "  fields: ['idmessage','priority','status','to','from','subject','date','gdate','unread','size','flag','note','arch','istoday','atts','scheddate','fmtd','fromfolder'],\n"
						+ "  sortInfo: { field: '" + psortfield + "', direction: '" + psortdir + "' },\n"
                        + "  threaded: "+threaded+",\n"
						+ "  groupField: '" + (threaded?"threadId":group) + "',\n";
				
/*				ColumnVisibilitySetting cvs = us.getColumnVisibilitySetting(pfoldername);
				ColumnsOrderSetting cos = us.getColumnsOrderSetting();
				// Apply grid defaults
				//ColumnVisibilitySetting.applyDefaults(mcache.isSent(), cvs);
				ColumnVisibilitySetting.applyDefaults(issent||isundersent, cvs);

				if (autoeditList.size()>0) {
					sout+="autoedit: [";
					for(long muid: autoeditList) {
						sout+=muid+",";
					}
					if(StringUtils.right(sout, 1).equals(",")) sout = StringUtils.left(sout, sout.length()-1);
					sout+="],\n";
				}
				
				// Fills columnsInfo object for client rendering
				sout += "colsInfo2: [";
				for (String dataIndex : cvs.keySet()) {
					sout += "{dataIndex:'" + dataIndex + "',hidden:" + String.valueOf(!cvs.get(dataIndex)) + ",index:"+cos.indexOf(dataIndex)+"},";
				}
				if (StringUtils.right(sout, 1).equals(",")) {
					sout = StringUtils.left(sout, sout.length() - 1);
				}
				sout += "]\n";*/
				
				sout += "},\n";
				sout += "threaded: "+(threaded?"1":"0")+",\n";
				sout += "unread: " + funread + ", issent: " + issent + ", millis: " + mlt.millis + " }\n";
				
			} else {
				sout += "total:0,\nstart:0,\nlimit:0,\nmessages: [\n";
				sout += "\n], unread: 0, issent: false }\n";
			}
			out.println(sout);
		} catch (Exception exc) {
			new JsonResult(exc).printTo(out);
			Service.logger.error("Exception",exc);
		}
	}
	
	private String getSingleHeaderValue(Message m, String headerName) throws MessagingException {
		String s[] = m.getHeader(headerName);
		String sv = null;
		if (s != null && s.length > 0) {
			sv = s[0];
		}
		return sv;
	}
	
	private java.util.Calendar parseScheduleHeader(String senddate, String sendtime) {
		String sdp[] = senddate.split("/");
		String sdt[] = sendtime.split(":");
		String sschedday = sdp[0];
		String sschedmonth = sdp[1];
		String sschedyear = sdp[2];
		String sschedhour = sdt[0];
		String sschedmins = sdt[1];
		int schedday = Integer.parseInt(sschedday);
		int schedmonth = Integer.parseInt(sschedmonth);
		int schedyear = Integer.parseInt(sschedyear);
		int schedhour = Integer.parseInt(sschedhour);
		int schedmins = Integer.parseInt(sschedmins);
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.set(java.util.Calendar.YEAR, schedyear);
		cal.set(java.util.Calendar.MONTH, schedmonth - 1);
		cal.set(java.util.Calendar.DATE, schedday);
		cal.set(java.util.Calendar.HOUR_OF_DAY, schedhour);
		cal.set(java.util.Calendar.MINUTE, schedmins);
		return cal;
	}
	
	private boolean isAttachment(Part part) throws MessagingException {
		return Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
				|| Part.INLINE.equalsIgnoreCase(part.getDisposition())
				|| (part.getDisposition() == null && part.getFileName() != null);
	}
	
	private boolean hasAttachements(Part p) throws MessagingException, IOException {
		boolean retval = false;

		//String disp=p.getDisposition();
		if (isAttachment(p)) {
			retval = true;
		} //if (disp!=null && disp.equalsIgnoreCase(Part.ATTACHMENT)) retval=true;
		else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			int parts = mp.getCount();
			for (int i = 0; i < parts; ++i) {
				Part bp = mp.getBodyPart(i);
				if (hasAttachements(bp)) {
					retval = true;
					break;
				}
			}
		}
		
		return retval;
	}
	
	class MessageListThread implements Runnable {
		
		FolderCache fc;
		String quickfilter;
		String pattern;
		String searchfield;
		int sortby;
		boolean ascending;
		int sort_group;
		boolean groupascending;
		boolean refresh;
		long millis;
		
		boolean threaded=false;
		
		Message msgs[] = null;
		boolean started = false;
		boolean finished = false;
		final Object lock = new Object();
		long lastRequest = 0;
		
		MessageListThread(FolderCache fc, String quickfilter, String pattern, String searchfield, int sortby, boolean ascending, boolean refresh, int sort_group, boolean groupascending, boolean threaded) {
			this.fc = fc;
			this.quickfilter=quickfilter;
			this.pattern = pattern;
			this.searchfield = searchfield;
			this.sortby = sortby;
			this.ascending = ascending;
			this.refresh = refresh;
			this.sort_group = sort_group;
			this.groupascending = groupascending;
			this.threaded=threaded;
		}
		
		public void run() {
			started = true;
			finished = false;
			synchronized (lock) {
				try {
					this.millis = System.currentTimeMillis();
					msgs=fc.getMessages(quickfilter,pattern,searchfield,sortby,ascending,refresh, sort_group, groupascending,threaded);
				} catch (Exception exc) {
					Service.logger.error("Exception",exc);
				}
				lock.notifyAll();
			}
			finished = true;
		}
	}
	
	DateFormat df = null;
	
	public void processGetMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pidattach = request.getParameter("idattach");
		String providername = request.getParameter("provider");
		String providerid = request.getParameter("providerid");
		int idattach = 0;
		boolean isEditor = request.getParameter("editor") != null;
		if (df == null) {
			df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, environment.getProfile().getLocale());
		}
		try {
			FolderCache mcache = null;
			Message m = null;
			int recs = 0;
			long msguid=-1;
			String vheader[] = null;
			boolean wasseen = false;
			String sout = "{\nmessage: [\n";
			if (providername == null) {
				checkStoreConnected();
				mcache = getFolderCache(pfoldername);
                msguid=Long.parseLong(puidmessage);
                m=mcache.getMessage(msguid);
                if (m.isExpunged()) throw new MessagingException("Message "+puidmessage+" expunged");
				vheader = m.getHeader("Disposition-Notification-To");
				wasseen = m.isSet(Flags.Flag.SEEN);
				
				if (pidattach != null) {
					
					HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
					Part part = mailData.getAttachmentPart(Integer.parseInt(pidattach));
					m = (Message) part.getContent();
					idattach = Integer.parseInt(pidattach) + 1;
				}
			} else {
				// TODO: provider get message!!!!
/*                WebTopService provider=wts.getServiceByName(providername);
				 MessageContentProvider mcp=provider.getMessageContentProvider(providerid);
				 m=new MimeMessage(session,mcp.getSource());
				 mcache=fcProvided;
				 mcache.addProvidedMessage(providername, providerid, m);*/
			}
			String messageid=getMessageID(m);
			String subject = m.getSubject();
			if (subject == null) {
				subject = "";
			} else {
				try {
					subject = MailUtils.decodeQString(subject);
				} catch (Exception exc) {
					
				}
			}
			java.util.Date d = m.getSentDate();
			if (d == null) {
				d = m.getReceivedDate();
			}
			if (d == null) {
				d = new java.util.Date(0);
			}
			String date = df.format(d).replaceAll("\\.", ":");
			String fromName = "";
			String fromEmail = "";
			Address as[] = m.getFrom();
			if (as != null && as.length > 0) {
				InternetAddress ia = (InternetAddress) as[0];
				fromName = ia.getPersonal();
				fromEmail = adjustEmail(ia.getAddress());
				if (fromName == null) {
					fromName = fromEmail;
				}
			}
			sout += "{iddata:'from',value1:'" + StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(fromName)) + "',value2:'" + StringEscapeUtils.escapeEcmaScript(fromEmail) + "',value3:0},\n";
			recs += 2;
			Address tos[] = m.getRecipients(RecipientType.TO);
			if (tos != null) {
				for (Address to : tos) {
					InternetAddress ia = (InternetAddress) to;
					String toName = ia.getPersonal();
					String toEmail = adjustEmail(ia.getAddress());
					if (toName == null) {
						toName = toEmail;
					}
					sout += "{iddata:'to',value1:'" + StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(toName)) + "',value2:'" + StringEscapeUtils.escapeEcmaScript(toEmail) + "',value3:0},\n";
					++recs;
				}
			}
			Address ccs[] = m.getRecipients(RecipientType.CC);
			if (ccs != null) {
				for (Address cc : ccs) {
					InternetAddress ia = (InternetAddress) cc;
					String ccName = ia.getPersonal();
					String ccEmail = adjustEmail(ia.getAddress());
					if (ccName == null) {
						ccName = ccEmail;
					}
					sout += "{iddata:'cc',value1:'" + StringEscapeUtils.escapeEcmaScript(ccName) + "',value2:'" + StringEscapeUtils.escapeEcmaScript(ccEmail) + "',value3:0},\n";
					++recs;
				}
			}
            Address bccs[]=m.getRecipients(RecipientType.BCC);
            if (bccs!=null)
                for(Address bcc: bccs) {
                    InternetAddress ia=(InternetAddress)bcc;
                    String bccName=ia.getPersonal();
                    String bccEmail=adjustEmail(ia.getAddress());
                    if (bccName==null) {
						bccName=bccEmail;
					}
                    sout+="{iddata:'bcc',value1:'"+StringEscapeUtils.escapeEcmaScript(bccName)+"',value2:'"+StringEscapeUtils.escapeEcmaScript(bccEmail)+"',value3:0},\n";
                    ++recs;
                }
			ArrayList<String> htmlparts = null;
			if (providername == null) {
				htmlparts = mcache.getHTMLParts((MimeMessage) m, msguid, false);
			} else {
				htmlparts = mcache.getHTMLParts((MimeMessage) m, providername, providerid);
			}
			for (String html : htmlparts) {
				//sout += "{iddata:'html',value1:'" + OldUtils.jsEscape(html) + "',value2:'',value3:0},\n";
				sout += "{iddata:'html',value1:'" + StringEscapeUtils.escapeEcmaScript(html) + "',value2:'',value3:0},\n";
				++recs;
			}
			
			if (!wasseen) {
				if (us.isManualSeen()) {
					m.setFlag(Flags.Flag.SEEN, false);
				} else {
					//if no html part, flag seen is not set
					if (htmlparts.size()==0) m.setFlag(Flags.Flag.SEEN, true);
				}
			}
			
			HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
			int acount = mailData.getAttachmentPartCount();
			for (int i = 0; i < acount; ++i) {
				Part p = mailData.getAttachmentPart(i);
				String ctype = p.getContentType();
				Service.logger.debug("attachment " + i + " is " + ctype);
				int ix = ctype.indexOf(';');
				if (ix > 0) {
					ctype = ctype.substring(0, ix);
				}
				String cidnames[]=p.getHeader("Content-ID");
				String cidname=null;
				if (cidnames!=null && cidnames.length>0) cidname=mcache.normalizeCidFileName(cidnames[0]);
				boolean isInlineable = isInlineableMime(ctype);
				boolean inline=((p.getHeader("Content-Location")!=null)||(cidname!=null))&&isInlineable;
				if (inline && cidname!=null) inline=mailData.isReferencedCid(cidname);
				if (p.getDisposition() != null && p.getDisposition().equalsIgnoreCase(Part.INLINE) && inline) {
					continue;
				}
				
				String imgname = null;
				boolean isCalendar=ctype.equalsIgnoreCase("text/calendar")||ctype.equalsIgnoreCase("text/icalendar");
				if (isCalendar) {
					imgname = "resources/" + getManifest().getId() + "/laf/" + cus.getLookAndFeel() + "/ical_16.png";
				}
				
				String pname = getPartName(p);
				try { pname=MailUtils.decodeQString(pname); } catch(Exception exc) {}
				if (pname == null) {
					ix = ctype.indexOf("/");
					String fname = ctype;
					if (ix > 0) {
						fname = ctype.substring(ix + 1);
					}
					//String ext = WT.getMediaTypeExtension(ctype);
					//if (ext == null) {
						pname = fname;
					//} else {
					//	pname = fname + "." + ext;
					//}
					if (isCalendar) pname+=".ics";
				}
				int size = p.getSize();
				int lines = (size / 76);
				int rsize = size - (lines * 2);//(p.getSize()/4)*3;
				String iddata = ctype.equalsIgnoreCase("message/rfc822") ? "eml" : (inline ? "inlineattach" : "attach");
				
				sout += "{iddata:'" + iddata + "',value1:'" + (i + idattach) + "',value2:'" + StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(pname)) + "',value3:" + rsize + ",value4:" + (imgname == null ? "null" : "'" + StringEscapeUtils.escapeEcmaScript(imgname) + "'") + "},\n";
			}
			if (!mcache.isDrafts() && !mcache.isSent() && !mcache.isSpam() && !mcache.isTrash()) {
				if (vheader != null && vheader[0] != null && !wasseen) {
					sout += "{iddata:'receipt',value1:'" + StringEscapeUtils.escapeEcmaScript(vheader[0]) + "',value2:'',value3:0},\n";
				}
			}
			
			String h = getSingleHeaderValue(m, "Sonicle-send-scheduled");
			if (h != null && h.equals("true")) {
				java.util.Calendar scal = parseScheduleHeader(getSingleHeaderValue(m, "Sonicle-send-date"), getSingleHeaderValue(m, "Sonicle-send-time"));
				java.util.Date sd = scal.getTime();
				String sdate = df.format(sd).replaceAll("\\.", ":");
				sout += "{iddata:'scheddate',value1:'" + StringEscapeUtils.escapeEcmaScript(sdate) + "',value2:'',value3:0},\n";
			}			
			
			ICalendarRequest ir=mailData.getICalRequest();
			if (ir!=null) {
				CalendarManager cm=(CalendarManager)WT.getServiceManager("com.sonicle.webtop.calendar",environment.getProfileId());
				if (cm!=null) {
					Event ev=cm.getEvent(ir.getUID());
					int eid=-1;
					if (ev!=null) {
						DateTime rts=ev.getRevisionTimestamp();
						if (ir.getLastModified().after(rts.toDate())) eid=0;
						else eid=ev.getEventId();
					}
					sout+="{iddata:'ical',value1:'"+ir.getMethod()+"',value2:'"+ir.getUID()+"',value3:'"+eid+"'},\n";
				}
			}
			
			sout += "{iddata:'date',value1:'" + StringEscapeUtils.escapeEcmaScript(date) + "',value2:'',value3:0},\n";
			sout += "{iddata:'subject',value1:'" + StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(subject)) + "',value2:'',value3:0},\n";
			sout += "{iddata:'messageid',value1:'"+StringEscapeUtils.escapeEcmaScript(messageid)+"',value2:'',value3:0}\n";
			
			if (providername == null && !mcache.isSpecial()) {
				mcache.refreshUnreads();
			}
			long millis = System.currentTimeMillis();
			sout += "\n],\ntotal:" + recs + ",\nmillis:" + millis + "\n}\n";
			out.println(sout);
//            if (!wasopen) folder.close(false);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public void processGetMessageNote(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		UserProfile profile = environment.getProfile();
		Connection con = null;
		boolean result = false;
		String text = "";
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(pfoldername);
            long msguid=Long.parseLong(puidmessage);
            String id=getMessageID(mcache.getMessage(msguid));
			con = getConnection();
			ONote onote=NoteDAO.getInstance().selectById(con, profile.getDomainId(), id);
			if (onote!=null) {
				text = onote.getText();
			}
			result = true;
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			text = exc.getMessage();
		} finally {
			DbUtils.closeQuietly(con);
		}
		new JsonResult(result, text).printTo(out);
	}
	
	public void processSaveMessageNote(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String text = request.getParameter("text").trim();
		UserProfile profile = environment.getProfile();
		Connection con = null;
		boolean result = false;
		String message = "";
		try {
			checkStoreConnected();
			FolderCache mcache = getFolderCache(pfoldername);
            long msguid=Long.parseLong(puidmessage);
            Message msg=mcache.getMessage(msguid);
			String id = getMessageID(msg);
			con = getConnection();
			NoteDAO.getInstance().deleteById(con, profile.getDomainId(), id);
			if (text.length() > 0) {
				ONote onote = new ONote(profile.getDomainId(), id, text);
				NoteDAO.getInstance().insert(con, onote);
				msg.setFlags(flagNote, true);
			} else {
				msg.setFlags(flagNote, false);
			}
			result = true;
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			message = exc.getMessage();
		} finally {
			DbUtils.closeQuietly(con);
		}
		new JsonResult(result, message).printTo(out);
	}

	protected String adjustEmail(String email) {
		if (email != null) {
			email = email.trim();
			if (email.startsWith("'")) {
				email = email.substring(1);
			}
			if (email.endsWith("'")) {
				email = email.substring(0, email.length() - 1);
			}
			email = email.trim();
		}
		return email;
	}
	
	class ContactElement {

		String email;
		String source;
		
		ContactElement(String email, String source) {
			this.email = email;
			this.source = source;
		}
		
		public boolean equals(Object o) {
			ContactElement contact = (ContactElement) o;
			return email.equals(contact.email);
		}
	}

	public void processGetAttachment(HttpServletRequest request, HttpServletResponse response) {
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pidattach = request.getParameter("idattach");
		String providername = request.getParameter("provider");
		String providerid = request.getParameter("providerid");
		String pcid = request.getParameter("cid");
		String purl = request.getParameter("url");
		String punknown = request.getParameter("unknown");
		String psaveas = request.getParameter("saveas");
		
		try {
			checkStoreConnected();
			FolderCache mcache = null;
			Message m = null;
			if (providername == null) {
				mcache = getFolderCache(pfoldername);
                long newmsguid=Long.parseLong(puidmessage);
                m=mcache.getMessage(newmsguid);
			} else {
				mcache = fcProvided;
				m = mcache.getProvidedMessage(providername, providerid);
			}
			HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
			Part part = null;
			if (pcid != null) {
				part = mailData.getCidPart(pcid);
			} else if (purl != null) {
				part = mailData.getUrlPart(purl);
			} else if (pidattach != null) {
				part = mailData.getAttachmentPart(Integer.parseInt(pidattach));
			} else if (punknown != null) {
				part = mailData.getUnknownPart(Integer.parseInt(punknown));
			}

			if (part!=null) {
				String ctype="binary/octet-stream";
				if (psaveas==null) {
					ctype=part.getContentType();
					int ix=ctype.indexOf(";");
					if (ix>0) ctype=ctype.substring(0,ix);
				}
				String name=part.getFileName();
				if (name==null) name="";
				try {
					name=MailUtils.decodeQString(name);
				} catch(Exception exc) {
				}
 
				if (psaveas==null) {
					int ix=name.lastIndexOf(".");
					if (ix>0) {
						//String ext=name.substring(ix+1);
						String xctype=ServletHelper.guessMediaType(name);
						if (xctype!=null) ctype=xctype;
					}
				}
				response.setContentType(ctype);
				response.setHeader("Content-Disposition", "inline; filename=\"" + name + "\"");
				if (providername==null) {
					Folder folder=mailData.getFolder();
					if (!folder.isOpen()) folder.open(Folder.READ_ONLY);
				}
				InputStream is = part.getInputStream();
				OutputStream out = response.getOutputStream();
				fastStreamCopy(is, out);
			}			
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public void processGetAttachments(HttpServletRequest request, HttpServletResponse response) {
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pids[] = request.getParameterValues("ids");
		String providername = request.getParameter("provider");
		String providerid = request.getParameter("providerid");
		
		try {
			checkStoreConnected();
			FolderCache mcache = null;
			Message m = null;
			if (providername == null) {
				mcache = getFolderCache(pfoldername);
                long newmsguid=Long.parseLong(puidmessage);
                m=mcache.getMessage(newmsguid);
			} else {
				mcache = fcProvided;
				m = mcache.getProvidedMessage(providername, providerid);
			}
			HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
			String name = m.getSubject();
			if (name == null) {
				name = "attachments";
			}
			try {
				name = MailUtils.decodeQString(name);
			} catch (Exception exc) {
			}
			name += ".zip";
			//prepare hashmap to hold already used pnames
			HashMap<String,String> pnames=new HashMap<String,String>();
			response.setContentType("application/x-zip-compressed");
			response.setHeader("Content-Disposition", "inline; filename=\"" + name + "\"");
			JarOutputStream jos = new java.util.jar.JarOutputStream(response.getOutputStream());
			byte[] b = new byte[64 * 1024];
			for (String pid : pids) {
				Part part = mailData.getAttachmentPart(Integer.parseInt(pid));
				String pname = part.getFileName();
				if (pname == null) {
					pname = "unknown";
				}
				try {
					pname = MailUtils.decodeQString(pname, "iso-8859-1");
				} catch (Exception exc) {
				}
				//keep name and extension
				String bpname=pname;
				String extpname=null;
				int ix=pname.lastIndexOf(".");
				if (ix>0) {
					bpname=pname.substring(0,ix);
					extpname=pname.substring(ix+1);
				}
				//check for existing pname and find an unused name
				int xid=0;
				String rpname=pname;
				while(pnames.containsKey(rpname)) {
					rpname=bpname+" ("+(++xid)+")";
					if (extpname!=null) rpname+="."+extpname;
				}
								
				JarEntry je = new JarEntry(rpname);
				jos.putNextEntry(je);
				if (providername == null) {
					Folder folder = mailData.getFolder();
					if (!folder.isOpen()) {
						folder.open(Folder.READ_ONLY);
					}
				}
				InputStream is = part.getInputStream();
				int len = 0;
				while ((len = is.read(b)) != -1) {
					jos.write(b, 0, len);
				}
				is.close();
				
				//remember used pname
				pnames.put(rpname, rpname);
			}
			jos.closeEntry();
			jos.flush();
			jos.close();
			
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public void processPreviewAttachment(HttpServletRequest request, HttpServletResponse response) {
		try {
			checkStoreConnected();
			String uploadId = request.getParameter("uploadId");
			/*String cid = request.getParameter("cid");
			Attachment att = null;
			if (tempname != null) {
				att = getAttachment(msgid, tempname);
			} else if (cid != null) {
				att = getAttachmentByCid(msgid, cid);
			}*/
			if (uploadId != null && hasUploadedFile(uploadId)) {
				WebTopSession.UploadedFile upl = getUploadedFile(uploadId);
				String ctype = upl.getMediaType();
				response.setContentType(ctype+"; name=\"" + upl.getFilename() + "\"");
				response.setHeader("Content-Disposition", "filename=\"" + upl.getFilename() + "\"");
				
				InputStream is = new FileInputStream(upl.getFile());
				OutputStream oout = response.getOutputStream();
				fastStreamCopy(is, oout);
			} else {
				Service.logger.debug("uploadId was not valid!");
			}
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public java.util.Calendar convertTimeZone(String year, String month, String day, String hour, String min, String timezonefrom, String timezoneto) {
		java.util.TimeZone timeZone1 = java.util.TimeZone.getTimeZone(timezonefrom);
		java.util.TimeZone timeZone2 = java.util.TimeZone.getTimeZone(timezoneto);
		java.util.Calendar calendar = new GregorianCalendar();
		calendar.setTimeZone(timeZone1);
		calendar.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(min));
		java.util.Calendar calendarout = new GregorianCalendar();
		calendarout.setTimeZone(timeZone2);
		calendarout.setTimeInMillis(calendar.getTimeInMillis());
		return calendarout;
	}
	
    public void processCalendarRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String pcalaction=request.getParameter("calaction");
        String pfoldername=request.getParameter("folder");
        String puidmessage=request.getParameter("idmessage");
        String pidattach=request.getParameter("idattach");
        try {
            checkStoreConnected();
			FolderCache mcache=getFolderCache(pfoldername);
			long newmsguid=Long.parseLong(puidmessage);
			Message m=mcache.getMessage(newmsguid);
			HTMLMailData mailData=mcache.getMailData((MimeMessage)m);
			Part part=mailData.getAttachmentPart(Integer.parseInt(pidattach));

			ICalendarRequest ir=new ICalendarRequest(part.getInputStream());
			CalendarManager cm=(CalendarManager)WT.getServiceManager("com.sonicle.webtop.calendar",environment.getProfileId());
			if (pcalaction.equals("accept")) {
				Event ev=cm.addEventFromICal(cm.getBuiltInCalendar().getCalendarId(), ir.getCalendar());
				String ekey=cm.getEventInstanceKey(ev.getEventId());

				sendICalendarReply(((InternetAddress)m.getRecipients(RecipientType.TO)[0]).toString(), PartStat.ACCEPTED, ir.getCalendar(),ir.getSummary(),ir.getOrganizerAddress());
				new JsonResult(ekey).printTo(out);
			} else if (pcalaction.equals("cancel")||pcalaction.equals("update")) {
				cm.updateEventFromICal(ir.getCalendar());
				new JsonResult().printTo(out);
			} else {
				throw new Exception("Unsupported calendar request action : "+pcalaction);
			}
        } catch(Exception exc) {
            new JsonResult(exc).printTo(out);
			logger.error("Error sending "+pcalaction, exc);
        }        
        
	}
	
    public void processDeclineInvitation(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String pfoldername=request.getParameter("folder");
        String puidmessage=request.getParameter("idmessage");
        String pidattach=request.getParameter("idattach");
        try {
            checkStoreConnected();
            FolderCache mcache=getFolderCache(pfoldername);
            long newmsguid=Long.parseLong(puidmessage);
            Message m=mcache.getMessage(newmsguid);
            HTMLMailData mailData=mcache.getMailData((MimeMessage)m);
            Part part=mailData.getAttachmentPart(Integer.parseInt(pidattach));
            
			ICalendarRequest ir=new ICalendarRequest(part.getInputStream());
			sendICalendarReply(((InternetAddress)m.getRecipients(RecipientType.TO)[0]).getAddress(), PartStat.DECLINED, ir.getCalendar(),ir.getSummary(),ir.getOrganizerAddress());
			new JsonResult().printTo(out);
        } catch(Exception exc) {
            new JsonResult(false,exc.getMessage()).printTo(out);
			logger.error("Error sending decline", exc);
        }        
        
	}
	
	private void sendICalendarReply(String forAddress, PartStat response, net.fortuna.ical4j.model.Calendar cal, String summary, String organizerAddress) throws Exception {
		UserProfile profile=environment.getProfile();
		Locale locale=profile.getLocale();
		String action_string=response.equals(PartStat.ACCEPTED)?
				lookupResource(locale, MailLocaleKey.ICAL_REPLY_ACCEPTED):
				lookupResource(locale, MailLocaleKey.ICAL_REPLY_DECLINED);
		
		net.fortuna.ical4j.model.Calendar reply=ICalendarUtils.buildInvitationReply(cal, ICalendarUtils.buildProdId(WT.getPlatformName()+" Mail"),forAddress, response);
		
		//If forAddress is not on any of the intended iCal attendee, don't send a reply (e.g. forwarded ics)
		if (reply!=null) {
			//String icalContent=reply.toString();
			String icalContent=ICalendarUtils.calendarToString(reply);

			String icalContentType="text/calendar; charset=UTF-8; method=REPLY";
			SimpleMessage smsg = new SimpleMessage(999999);

			String subject = action_string + " " + summary;
			smsg.setSubject(subject);
			InternetAddress to[]=new InternetAddress[1];
			to[0]=new InternetAddress(organizerAddress);
			smsg.setTo(to);

			//smsg.setContent(icalContent,"this is a meeting invitation",icalContentType);
			smsg.setContent("");

			javax.mail.internet.MimeBodyPart part2 = new javax.mail.internet.MimeBodyPart();
			part2.setText(icalContent, "UTF-8");
			part2.setHeader("Content-type", icalContentType);
			part2.setHeader("Content-Transfer-Encoding", "8BIT");
			//part2.setFileName("webtop-reply.ics");
			/*javax.mail.internet.MimeBodyPart part1 = new javax.mail.internet.MimeBodyPart();
			part1.setText(content, "UTF8", "application/ics");
			part1.setHeader("Content-type", "application/ics");
			part1.setFileName("webtop-reply.ics");*/

			MimeMultipart mp = new MimeMultipart("mixed");
			mp.addBodyPart(part2);
			MimeBodyPart mbp=new MimeBodyPart();
			mbp.setHeader("Content-type", "multipart/mixed");
			mbp.setContent(mp);

			smsg.setAttachments(new javax.mail.Part[]{mbp});

			Exception exc=sendMsg(profile.getFullEmailAddress(), smsg, null);
			if (exc!=null) throw exc;
		}
		
	}
	
    public void processUpdateCalendarReply(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String pfoldername=request.getParameter("folder");
        String puidmessage=request.getParameter("idmessage");
        String pidattach=request.getParameter("idattach");
        //UserProfile profile=environment.getUserProfile();
        try {
            checkStoreConnected();
            FolderCache mcache=getFolderCache(pfoldername);
            long newmsguid=Long.parseLong(puidmessage);
            Message m=mcache.getMessage(newmsguid);
            HTMLMailData mailData=mcache.getMailData((MimeMessage)m);
            Part part=mailData.getAttachmentPart(Integer.parseInt(pidattach));
            
            ICalendarRequest ir=new ICalendarRequest(part.getInputStream());
			String event_id=null;
			//TODO: String event_id=((CalendarService)wts.getServiceByName("calendar")).updateFromReply(ir);
			if (event_id!=null) {
				new JsonResult(event_id).printTo(out);
			} else {
				throw new Exception("Event not found");
			}
        } catch(Exception exc) {
            //sout="{\nresult: false, text:'"+Utils.jsEscape(exc.getMessage())+"'\n}";
			new JsonResult(false, exc.getMessage()).printTo(out);
			logger.error("Error getting calendar events", exc);
        }
	}
	
	
    public void processListRules(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        UserProfile profile=environment.getProfile();
        Locale locale=profile.getLocale();
        Connection con=null;
        String context=request.getParameter("context");
        try {
			String table = "none";
			if (context.equals("INBOX")) table = "mail.rules";
			else if (context.equals("SENT")) table = "mail.sentrules";
			
            con=getConnection();
            com.sonicle.webtop.mail.SieveJSonGenerator jsg=new com.sonicle.webtop.mail.SieveJSonGenerator(context,this,locale,folderPrefix);
			MailRules rules=getMailRules(con, table, profile.getUserId(), profile.getDomainId());
            StringBuffer sb=jsg.generate(rules);
            String sout=sb.toString();
            out.println(sout);
        } catch(Exception exc) {
            logger.error("Exception",exc);
        } finally {
            if (con!=null) try { con.close(); } catch(Exception e) {}
        }
    }
	
	public void processSaveRules(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        UserProfile profile=environment.getProfile();
        Connection con=null;
		
        try {
            con=WT.getConnection(this.SERVICE_ID,false);
			
			JsRule.List jsrules=JsonResult.GSON.fromJson(ServletUtils.getStringParameter(request,"rules",true), JsRule.List.class);
			OVacation vitem=new OVacation();
			vitem.setDomainId(profile.getDomainId());
			vitem.setUserId(profile.getUserId());
			vitem.setActive(ServletUtils.getBooleanParameter(request, "vactive", false));
			vitem.setMessage(ServletUtils.getStringParameter(request, "vmessage", ""));
			vitem.setAddresses(ServletUtils.getStringParameter(request, "vaddresses", profile.getEmailAddress()));
			
			RuleDAO.getInstance().deleteByUserId(con, profile.getDomainId(), profile.getUserId());
			long rule_id=0;
			for(JsRule jsrule: jsrules) {
				ORule orule=new ORule();
				orule.setDomainId(profile.getDomainId());
				orule.setUserId(profile.getUserId());
				orule.setRuleId(++rule_id);
				orule.setAction(jsrule.action);
				orule.setActionValue(jsrule.value);
				orule.setCondition(jsrule.condition);
				orule.setFromValue(jsrule.from);
				orule.setStatus(jsrule.active?"E":"D");
				orule.setSubjectValue(jsrule.subject);
				orule.setToValue(jsrule.to);
				orule.setContinue("N");
				orule.setKeepCopy("N");
				RuleDAO.getInstance().insert(con, orule);
			}

			VacationDAO.getInstance().deleteByUserId(con, profile.getDomainId(), profile.getUserId());
			VacationDAO.getInstance().insert(con, vitem);
			
			DbUtils.commitQuietly(con);
			
            MailRules rules=getMailRules(con,"rules",profile.getUserId(),profile.getDomainId());
			if (rules!=null) sieve.saveScript(rules, true);

			//TODO: update scan folders
/*            String scf="";
            for(String foldername: foldersCache.keySet()) {
                FolderCache fc=foldersCache.get(foldername);
                boolean psfon=fc.isScanForcedOn();
                fc.updateScanFlags();
                boolean nsfon=fc.isScanForcedOn();
                if (psfon!=nsfon) {
                    if (scf.length()>0) scf+=",";
                    scf+="{ idfolder: '"+Utils.jsEscape(foldername)+"', sfon: "+nsfon+" }";
                }
            }
			*/

            new JsonResult().printTo(out);
        } catch(Exception exc) {
			DbUtils.rollbackQuietly(con);
            new JsonResult(exc).printTo(out);
            logger.error("Exception",exc);
        } finally {
			DbUtils.closeQuietly(con);
        }
	}
	
/*    public void processApplyFilters(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        UserProfile profile=environment.getUserProfile();
        String login=profile.getUser();
		String iddomain=wtd.getDataIDDomain();
        String vactive=request.getParameter("vactive");
        String vmessage=request.getParameter("vmessage");
        String vaddresses=request.getParameter("vaddresses");
        Connection con=null;
        Statement stmt=null;
		
        String sout;
        try {
			String esid = ServletUtils.getStringParameter(request, "esid", true);
			EditingSession es = wts.getEditing(EditingSession.Scope.SESSION, esid);
			
			con=wtd.getConnection();
			
			try {
				con.setAutoCommit(false);
				InboxMailFiltersDb.deleteByDomainUser(con, profile.getIDDomain(), profile.getUser());
				SentMailFiltersDb.deleteByDomainUser(con, profile.getIDDomain(), profile.getUser());
				es.copyBackTempTable(con, "mailfilters");
				es.copyBackTempTable(con, "mailsentfilters");
				con.commit();
			} catch(Exception ex1) {
				con.rollback();
				throw ex1;
			}
			
            con.setAutoCommit(true);
            stmt=con.createStatement();
            stmt.executeUpdate("delete from vacation where iddomain='"+iddomain+"' and login='"+profile.getUser()+"'");

            String sql="insert into vacation (iddomain,login,active,message,addresses) values ("+
             "'"+wtd.getLocalIDDomain()+"',"+
             "'"+login+"',"+
             "'"+vactive+"',"+
             "'"+Utils.getSQLString(vmessage)+"',"+
             "'"+Utils.getSQLString(vaddresses)+"'"+
             ")";
            stmt.executeUpdate(sql);
            con.commit();
            MailFilters filters=getMailFilters(con,"mailfilters",login,iddomain);
			if (filters!=null) sieve.saveScript(filters, true);

            String scf="";
            for(String foldername: foldersCache.keySet()) {
                FolderCache fc=foldersCache.get(foldername);
                boolean psfon=fc.isScanForcedOn();
                fc.updateScanFlags();
                boolean nsfon=fc.isScanForcedOn();
                if (psfon!=nsfon) {
                    if (scf.length()>0) scf+=",";
                    scf+="{ idfolder: '"+Utils.jsEscape(foldername)+"', sfon: "+nsfon+" }";
                }
            }

            sout="{\nresult: true, scf:["+scf+"]\n}";
        } catch(Exception exc) {
            logger.error("Exception",exc);
            sout="{\nresult: false, text:'"+Utils.jsEscape(exc.getMessage().replaceAll("\n","<BR>"))+"'\n}";
        } finally {
            if (stmt!=null) try { stmt.close(); } catch(Exception e) {}
            if (con!=null) try { con.close(); } catch(Exception e) {}
        }
        out.println(sout);
    }
*/
	
	boolean checkFileRules(String foldername) {
		boolean b = false;
		Connection con = null;
		try {
			UserProfile profile = environment.getProfile();
			con = getConnection();
			b = RuleDAO.getInstance().hasFileIntoFolder(con, profile.getDomainId(), profile.getUserId(), foldername);
		} catch (SQLException exc) {
			logger.error("Error checking File rules on folder {}", foldername, exc);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return b;
	}
	
	boolean checkScanRules(String foldername) {
		boolean b = false;
		Connection con = null;
		try {
			UserProfile profile = environment.getProfile();
			con = getConnection();
			b = ScanDAO.getInstance().isScanFolder(con, profile.getDomainId(), profile.getUserId(), foldername);
		} catch (SQLException exc) {
			logger.error("Error checking Scan rules on folder {}", foldername, exc);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return b;
	}
	
	public void processRunAdvancedSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			if (ast != null && ast.isRunning()) {
				throw new Exception("Advanced search is still running!");
			}
			
			String folder = request.getParameter("folder");
            String strashspam=request.getParameter("trashspam");
			String ssubfolders = request.getParameter("subfolders");
			String sandor = request.getParameter("andor");
			String sentries[] = request.getParameterValues("entries");
			
			boolean subfolders = ssubfolders.equals("true");
			boolean trashspam=strashspam.equals("true");
			boolean and = sandor.equals("and");
			
			AdvancedSearchEntry entries[] = new AdvancedSearchEntry[sentries.length];
			for (int i = 0; i < sentries.length; ++i) {
				entries[i] = new AdvancedSearchEntry(sentries[i]);
			}
			
			if (folder.startsWith("folder:")) {
				folder = folder.substring(7);
				ast = new AdvancedSearchThread(this, folder, trashspam, subfolders, and, entries);
			} else {
				int folderType = folder.equals("personal") ? AdvancedSearchThread.FOLDERTYPE_PERSONAL
						: folder.equals("shared") ? AdvancedSearchThread.FOLDERTYPE_SHARED
								: AdvancedSearchThread.FOLDERTYPE_ALL;
				ast = new AdvancedSearchThread(this, folderType, trashspam, subfolders, and, entries);
			}
			ast.start();
			out.println("{\nresult: true\n}");
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			out.println("{\nresult: false, text:'" + StringEscapeUtils.escapeEcmaScript(exc.getMessage()) + "'\n}");
		}
	}
	
	public void processPollAdvancedSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager core = WT.getCoreManager();
		
		try {
			String sstart = request.getParameter("start");
			int start = 0;
			if (sstart != null) {
				start = Integer.parseInt(sstart);
			}
			String sout = "{\n";
			if (ast != null) {
				UserProfile profile = environment.getProfile();
				Locale locale = profile.getLocale();
				java.util.Calendar cal = java.util.Calendar.getInstance(locale);
				ArrayList<Message> msgs = ast.getResult();
				int totalrows = msgs.size();
				int newrows = totalrows - start;
				sout += "total:" + totalrows + ",\nstart:" + start + ",\nlimit:" + newrows + ",\nmessages: [\n";
				boolean first = true;
				for (int i = start; i < msgs.size(); ++i) {
					Message xm = msgs.get(i);
					if (xm.isExpunged()) {
						continue;
					}
					IMAPFolder xmfolder=(IMAPFolder)xm.getFolder();
					boolean wasopen=xmfolder.isOpen();
					if (!wasopen) xmfolder.open(Folder.READ_ONLY);
                    long nuid=xmfolder.getUID(xm);
					if (!wasopen) xmfolder.close(false);
					IMAPMessage m = (IMAPMessage) xm;
					//Date
					java.util.Date d = m.getSentDate();
					if (d == null) {
						d = m.getReceivedDate();
					}
					if (d == null) {
						d = new java.util.Date(0);
					}
					cal.setTime(d);
					int yyyy = cal.get(java.util.Calendar.YEAR);
					int mm = cal.get(java.util.Calendar.MONTH);
					int dd = cal.get(java.util.Calendar.DAY_OF_MONTH);
					int hhh = cal.get(java.util.Calendar.HOUR_OF_DAY);
					int mmm = cal.get(java.util.Calendar.MINUTE);
					int sss = cal.get(java.util.Calendar.SECOND);
					String xfolder = xm.getFolder().getFullName();
					FolderCache fc = getFolderCache(xfolder);
					String folder = StringEscapeUtils.escapeEcmaScript(xfolder);
					String foldername = StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(getInternationalFolderName(fc)));
					//From
					String from = "";
					Address ia[] = m.getFrom();
					if (ia != null) {
						InternetAddress iafrom = (InternetAddress) ia[0];
						from = iafrom.getPersonal();
						if (from == null) {
							from = iafrom.getAddress();
						}
					}
					from = (from == null ? "" : StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(from)));
					//To
					String to = "";
					ia = m.getRecipients(Message.RecipientType.TO);
					if (ia != null) {
						InternetAddress iato = (InternetAddress) ia[0];
						to = iato.getPersonal();
						if (to == null) {
							to = iato.getAddress();
						}
					}
					to = (to == null ? "" : StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(to)));
					//Subject
					String subject = m.getSubject();
					if (subject != null) {
						try {
							subject = MailUtils.decodeQString(subject);
						} catch (Exception exc) {
							
						}
					}
					subject = (subject == null ? "" : StringEscapeUtils.escapeEcmaScript(MailUtils.htmlescape(subject)));
					//Unread
					boolean unread = !m.isSet(Flags.Flag.SEEN);
                    //if (ppattern==null && unread) ++funread;
					//Priority
					int priority = getPriority(m);
					//Status
					java.util.Date today = new java.util.Date();
					java.util.Calendar cal1 = java.util.Calendar.getInstance(locale);
					java.util.Calendar cal2 = java.util.Calendar.getInstance(locale);
					boolean isToday = false;
					if (d != null) {
						cal1.setTime(today);
						cal2.setTime(d);
						if (cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH)
								&& cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH)
								&& cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)) {
							isToday = true;
						}
					}
					
					Flags flags = m.getFlags();
					String status = "read";
					if (flags != null) {
						if (flags.contains(Flags.Flag.ANSWERED)) {
							if (flags.contains("$Forwarded")) {
								status = "repfwd";
							} else {
								status = "replied";
							}
						} else if (flags.contains("$Forwarded")) {
							status = "forwarded";
						} else if (flags.contains(Flags.Flag.SEEN)) {
							status = "read";
						} else if (isToday) {
							status = "new";
						} else {
							status = "unread";
						}
						//                    if (flags.contains(Flags.Flag.USER)) flagImage=webtopapp.getUri()+"/images/themes/"+profile.getTheme()+"/mail/flag.gif";
					}
					//Size
					int msgsize = 0;
					msgsize = (m.getSize() * 3) / 4;// /1024 + 1;
					//User flags
					String cflag="";
					for (WebtopFlag webtopFlag: webtopFlags) {
						String flagstring=webtopFlag.label;
						String tbflagstring=webtopFlag.tbLabel;
						if (!flagstring.equals("complete")) {
							String oldflagstring="flag"+flagstring;
							if (flags.contains(flagstring)
									||flags.contains(oldflagstring)
									|| (tbflagstring!=null && flags.contains(tbflagstring))
							) {
								cflag=flagstring;
							}
						}
					}
					boolean flagComplete = flags.contains("complete");
					if (flagComplete) {
						cflag += "-complete";
					}

                    //idmessage=idmessage.replaceAll("\\\\", "\\\\");
					//idmessage=OldUtils.jsEscape(idmessage);
					if (!first) {
						sout += ",\n";
					}
					boolean archived = false;
					if (hasDocumentArchiving()) {
						archived=m.getHeader("X-WT-Archived")!=null;
						if (!archived) {
							archived=flags.contains(sflagArchived);
						}
					}
					
					boolean hasNote=flags.contains(sflagNote);

					sout += "{folder:'" + folder + "', folderdesc:'" + foldername + "',idmandfolder:'" + folder + "|" + nuid + "',idmessage:'" + nuid + "',priority:" + priority + ",status:'" + status + "',to:'" + to + "',from:'" + from + "',subject:'" + subject + "',date: new Date(" + yyyy + "," + mm + "," + dd + "," + hhh + "," + mmm + "," + sss + "),unread: " + unread + ",size:" + msgsize + ",flag:'" + cflag + "'" + (archived ? ",arch:true" : "") + (isToday ? ",istoday:true" : "") + (hasNote ? ",note:true" : "")+"}";
					first = false;
				}
				sout += "\n]\n, progress: " + ast.getProgress() + ", curfoldername: '" + StringEscapeUtils.escapeEcmaScript(getInternationalFolderName(ast.getCurrentFolder())) + "', "
						+ "max: " + ast.isMoreThanMax() + ", finished: " + (ast.isFinished() || ast.isCanceled() || !ast.isRunning()) + " }\n";
			} else {
				sout += "total:0,\nstart:0,\nlimit:0,\nmessages: [\n";
				sout += "\n] }\n";
			}
			out.println(sout);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public void processCancelAdvancedSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		if (ast != null && ast.isRunning()) {
			ast.cancel();
		}
		out.println("{\nresult: true\n}");
	}

	public void processSetMessageView(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String region = ServletUtils.getStringParameter(request, "region", true);
			Integer width = ServletUtils.getIntParameter(request, "width", true);
			Integer height = ServletUtils.getIntParameter(request, "height", true);
			Boolean collapsed = ServletUtils.getBooleanParameter(request, "collapsed", false);
			
			us.setMessageViewRegion(region);
			us.setMessageViewWidth(width);
			us.setMessageViewHeight(height);
			us.setMessageViewCollapsed(collapsed);
			
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action SetToolComponentWidth", ex);
			new JsonResult(false, "Unable to save with").printTo(out);
		}
	}	
	
	public MailRules getMailRules(Connection con, String filtersTable, String login, String iddomain) throws Exception {
		MailRules rfilters = null;
		
		try {
			List<ORule> orules = RuleDAO.getInstance().selectByUserId(con, iddomain, login);
			
			MailRules filters = new MailRules();

			for (ORule or: orules) {
				long idfilter = or.getRuleId();
				String status = or.getStatus();
				boolean enabled = status.equals("E");
				String action = or.getAction();
				String actionvalue = or.getActionValue();
				String operator = "or";
				if (or.getCondition().equals("ALL")) {
					operator = "and";
				}
				
				MailRuleConditions mfcs = new MailRuleConditions((int)idfilter, enabled, action, actionvalue, operator);
				
				String fromvalue = or.getFromValue();
				if (fromvalue != null && fromvalue.trim().length() > 0) {
					MailRuleCondition mfc = new MailRuleCondition();
					mfc.setComparison(MailRuleCondition.CONTAINS);
					mfc.setField("from");
					mfc.setValues(fromvalue, true);
					mfcs.add(mfc);
				}
				String tovalue = or.getToValue();
				if (tovalue != null && tovalue.trim().length() > 0) {
					MailRuleCondition mfc = new MailRuleCondition();
					mfc.setComparison(MailRuleCondition.CONTAINS);
					mfc.setField("to");
					mfc.setValues(tovalue, true);
					mfcs.add(mfc);
				}
				String subjectvalue = or.getSubjectValue();
				if (subjectvalue != null && subjectvalue.trim().length() > 0) {
					MailRuleCondition mfc = new MailRuleCondition();
					mfc.setComparison(MailRuleCondition.CONTAINS);
					mfc.setField("subject");
					mfc.setValues(subjectvalue, false);
					mfcs.add(mfc);
				}
				String sizevalue = null;
				if (or.getSizeValue()!=null) sizevalue=or.getSizeValue().toString();
				String size = or.getSizeMatch();
				if (size != null && size.trim().length() > 0 && sizevalue != null && sizevalue.trim().length() > 0) {
					MailRuleCondition mfc = new MailRuleCondition();
					if (size.equals("+")) {
						mfc.setComparison(MailRuleCondition.GREATERTHAN);
					} else {
						mfc.setComparison(MailRuleCondition.LESSTHAN);
					}
					mfc.setField("size");
					mfc.setValues(sizevalue, true);
					mfcs.add(mfc);
				}
				String fieldvalue = or.getFieldValue();
				String fieldname = or.getFieldName();
				if (fieldname != null && fieldname.trim().length() > 0 && fieldvalue != null && fieldvalue.trim().length() > 0) {
					MailRuleCondition mfc = new MailRuleCondition();
					mfc.setComparison(MailRuleCondition.CONTAINS);
					mfc.setField(fieldname);
					mfc.setValues(fieldvalue, true);
					mfcs.add(mfc);
				}
				
				filters.add(mfcs);
			}
			
			OVacation ovacation = VacationDAO.getInstance().selectByUserId(con, iddomain, login);
			
			if (ovacation!=null) {
				if (ovacation.getActive()) {
					filters.setVacation(ovacation.getMessage(), ovacation.getAddresses());
				}
			}
			
			rfilters = filters;
			
		} catch (DAOException exc) {
			logger.error("Error saving Sieve script", exc);
		} finally {
		}
		
		return rfilters;
	}
	
	public boolean isSharedSeen() throws MessagingException {
		SonicleIMAPFolder xfolder = (SonicleIMAPFolder) store.getFolder("INBOX");
		String annot = xfolder.getAnnotation("/vendor/cmu/cyrus-imapd/sharedseen", true);
		return annot.equals("true");
	}
	
	public void setSharedSeen(boolean b) throws MessagingException {
		SonicleIMAPFolder xfolder = (SonicleIMAPFolder) store.getFolder("INBOX");
		xfolder.setAnnotation("/vendor/cmu/cyrus-imapd/sharedseen", true, b ? "true" : "false");
	}

	private boolean isSharedFolderContainer(IMAPFolder folder) {
		// If passed folder matches a shared prefix, we have taken shared 
		// folders' container folder 
		for(String prefix : sharedPrefixes) {
			if(folder.getFullName().equals(prefix)) return true;
		}
		return false;
	}
	
	protected boolean schemeWantsUserWithDomain(AuthenticationDomain ad) {
		String scheme=ad.getDirUri().getScheme();
		//return scheme.equals("ldapneth")?false:scheme.equals("ad")?true:scheme.startsWith("ldap");
		return scheme.equals("ad")||scheme.startsWith("ldap");
	}
	
	UserProfile.Id  aclUserIdToUserId(String aclUserId) {
		String userId=aclUserId;
		//imap user includes domain only if ldap or AD, not including nethserver 6
		//strip domain if needed
		Principal principal=(Principal)RunContext.getSubject().getPrincipal();
		AuthenticationDomain ad=principal.getAuthenticationDomain();
		if (schemeWantsUserWithDomain(ad)) {
			int ix=aclUserId.indexOf("@");
			if (ix>0) {
				String domain=aclUserId.substring(ix+1).toLowerCase();
				if (ad.getInternetName().toLowerCase().equals(domain)) userId=aclUserId.substring(0,ix);
				else {
					//skip if non domain users not permitted
					if (!RunContext.isPermitted(SERVICE_ID, "SHARING_UNKNOWN_ROLES","SHOW")) userId=null;
				}
			} else {
				if (!RunContext.isPermitted(SERVICE_ID, "SHARING_UNKNOWN_ROLES","SHOW")) userId=null;
			}
		}
		if (principal.getUserId().equals(userId)) userId=null;
		
		UserProfile.Id pid=null;
		if (userId!=null) pid=new UserProfile.Id(environment.getProfile().getDomainId(),userId);
		return pid;
	}

	public void processManageSharing(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			String id=null;
			Payload<MapItem, JsSharing> pl=null;
			
			if(crud.equals(Crud.READ)) {
				id = ServletUtils.getStringParameter(request, "id", true);
			} else if(crud.equals(Crud.UPDATE)) {
				pl = ServletUtils.getPayload(request, JsSharing.class);
				id=pl.data.id;
			}
			
			FolderCache fc = getFolderCache(id);
			SonicleIMAPFolder folder=(SonicleIMAPFolder)fc.getFolder();
			CoreManager core=WT.getCoreManager();
			Sharing wtsharing=core.getSharing(SERVICE_ID, MailManager.IDENTITY_SHARING_GROUPNAME, MailManager.IDENTITY_SHARING_ID);
			String description=folder.getName();
			ArrayList<JsSharing.SharingRights> rights = new ArrayList<>();
			for(ACL acl : folder.getACL()) {
				String aclUserId=acl.getName();
				UserProfile.Id pid=aclUserIdToUserId(aclUserId);
				if (pid==null) continue;
				String roleUid=core.getUserUid(pid);
				String roleDescription=null;
				boolean shareIdentity=false;
				boolean forceMailcard=false;
				if (roleUid==null) { 
					if (!RunContext.isPermitted(SERVICE_ID, "SHARING_UNKNOWN_ROLES","SHOW")) continue;
					roleUid=aclUserId; 
					roleDescription=roleUid; 
				} else {
					Sharing.RoleRights wtrr=wtsharing.getRoleRights(roleUid);
					if (wtrr!=null) {
						shareIdentity=wtrr.folderRead;
						forceMailcard=wtrr.folderUpdate;
					}
					String dn;
					try {
						OUser ouser=core.getUser(pid);
						dn=ouser.getDisplayName();
					} catch(WTException exc) {
						dn="no description available";
					}
					roleDescription=pid.getUserId()+" ["+dn+"]";
				}

				Rights ar=acl.getRights();
				rights.add(new JsSharing.SharingRights(
						id, 
						roleUid,
						roleDescription,
						aclUserId,
						shareIdentity,
						forceMailcard,
						ar.contains(Rights.Right.getInstance('l')),
						ar.contains(Rights.Right.getInstance('r')),
						ar.contains(Rights.Right.getInstance('s')),
						ar.contains(Rights.Right.getInstance('w')),
						ar.contains(Rights.Right.getInstance('i')),
						ar.contains(Rights.Right.getInstance('p')),
						ar.contains(Rights.Right.getInstance('k')),
						ar.contains(Rights.Right.getInstance('a')),
						ar.contains(Rights.Right.getInstance('x')),
						ar.contains(Rights.Right.getInstance('t')),
						ar.contains(Rights.Right.getInstance('n')),
						ar.contains(Rights.Right.getInstance('e'))
				));
			}
			JsSharing sharing=new JsSharing(id,description,"this",rights);
			
			if(crud.equals(Crud.READ)) {
				new JsonResult(sharing).printTo(out);			
			} else if(crud.equals(Crud.UPDATE)) {
				for(JsSharing.SharingRights sr: pl.data.rights) {
					//try to fill in the imapId where empty
					if (StringUtils.isEmpty(sr.imapId)) {
						UserProfile.Id pid=core.userUidToProfileId(sr.roleUid);
						String imapId=null;
						//look for any custom mail user
						OUserMap userMap=UserMapDAO.getInstance().selectById(getConnection(), pid.getDomainId(), pid.getUserId());
						if (userMap!=null && !StringUtils.isEmpty(userMap.getMailUser())) {
							if (userMap.getMailHost().equals(mprofile.getMailHost()) && 
									userMap.getMailPort().equals(mprofile.getMailPort()) && 
									userMap.getMailProtocol().equals(mprofile.getMailProtocol())) {
								imapId=userMap.getMailUser();
							}
						} else {
							imapId=pid.getUserId();
							Principal principal=(Principal)RunContext.getSubject().getPrincipal();
							AuthenticationDomain ad=principal.getAuthenticationDomain();
							if (schemeWantsUserWithDomain(ad)) {
								imapId+="@"+ad.getInternetName();
							}							
						}
						sr.imapId=imapId;
					}
				}
				
				String foldername=pl.data.method.equals("all")?"":id;
				boolean recursive=pl.data.method.equals("all")||pl.data.method.equals("branch");
				
				//first delete all removed roles
				for(JsSharing.SharingRights sr: sharing.rights) {
					if (!pl.data.hasImapId(sr.imapId)) {
						System.out.println("Folder ["+foldername+"] - remove acl for "+sr.imapId+" recursive="+recursive);
						removeFolderSharing(foldername,sr.imapId,recursive);
						updateIdentitySharing(sr.roleUid,false,false);
					}
				}
				//now apply new acls
				for(JsSharing.SharingRights sr: pl.data.rights) {
					if (!StringUtils.isEmpty(sr.imapId)) {
						String srights=sr.toString();
						System.out.println("Folder ["+foldername+"] - add acl "+srights+" for "+sr.imapId+" recursive="+recursive);
						setFolderSharing(foldername, sr.imapId, srights, recursive);
						updateIdentitySharing(sr.roleUid,sr.shareIdentity,sr.forceMailcard);
					}
				}
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ManageSharing", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	private void updateIdentitySharing(String roleUid, boolean shareIdentity, boolean forceMailcard) throws WTException {
		//update sharing settings
		Sharing wtsharing=new Sharing();
		wtsharing.setId(MailManager.IDENTITY_SHARING_ID);
		ArrayList<Sharing.RoleRights> wtrights=new ArrayList<>();
		SharePermsFolder spf=new SharePermsFolder();
		if (shareIdentity||forceMailcard) {
			if (shareIdentity) spf.add(SharePermsFolder.READ);
			if (forceMailcard) spf.add(SharePermsFolder.UPDATE);
			wtrights.add(new Sharing.RoleRights(roleUid,null,spf,new SharePermsElements()));
		}
		wtsharing.setRights(wtrights);
		WT.getCoreManager().updateSharing(SERVICE_ID, MailManager.IDENTITY_SHARING_GROUPNAME, wtsharing);
	}
	
	private void removeFolderSharing(String foldername, String acluser, boolean recursive) throws MessagingException {
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
			logger.error("Error removing acl on folder "+folder.getFullName(),exc);
		}
		
	}
	
	private void setFolderSharing(String foldername, String acluser, String rights, boolean recursive) throws MessagingException {
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
			logger.error("Error setting acl on folder "+folder.getFullName(),exc);
		}
	}
	// TODO: set folder sharing!
	
/*	public void setFolderSharing(String targetUser, String resource, String params) throws MessagingException {
	 IMAPFolder imapf = null;
	 String acluser = wtd.getMailUsername(targetUser);
	 if(acluser == null) return;
		
	 FolderCache fc = null;
	 if(!resource.equals("INBOX")) {
		fc = getFolderCache(resource);
		if(fc == null) return;
	 }
		
	 ACL acl = new ACL(acluser, new Rights(params));
	 if(fc != null) {
		imapf = (IMAPFolder)fc.getFolder();
		_setFolderSharing(imapf, acl);
	 } else {
		Folder folder = getDefaultFolder();
		Folder children[] = folder.list();
		for(Folder child: children) {
			imapf = (IMAPFolder)child;
			if(isSharedFolderContainer(imapf)) continue; // Skip shared folder container
			_setFolderSharing(imapf, acl);
		}
	 }
		
	}
	
	private void _setFolderSharing(IMAPFolder folder, ACL acl) throws MessagingException {
	 if (!isLeaf(folder)) {
	 Folder children[]=folder.list();
	 for(Folder child: children)
	 _setFolderSharing((IMAPFolder)child, acl);
	 }
	 //logger.debug("set sharing on {} to {}",folder.getFullName(),acl.getName());
	 folder.removeACL(acl.getName());
	 folder.addACL(acl);			
	 }
	
	 public void removeFolderSharing(String targetUser, String resource) throws MessagingException {
	 logger.debug("removeFolderSharing({},{})",targetUser,resource);
	 IMAPFolder imapf = null;
	 String acluser = wtd.getMailUsername(targetUser);
	 if (acluser == null) return;
		
	 FolderCache fc = null;
	 if(!resource.equals("INBOX")) {
		fc = getFolderCache(resource);
		if(fc == null) return;
	 }
		
	 if(fc != null) {
		imapf = (IMAPFolder)fc.getFolder();
		_removeFolderSharing(imapf, acluser);
	 } else {
		Folder folder = getDefaultFolder();
		Folder children[] = folder.list();
		for(Folder child: children) {
			imapf = (IMAPFolder)child;
			if(isSharedFolderContainer(imapf)) continue; // Skip shared folder container
			_removeFolderSharing(imapf, acluser);
		}
	 }
				
	}
	

	*/
	public SharedPrincipal getSharedPrincipal(String domainId, String mailUserId) {
		SharedPrincipal p = null;
		Connection con = null;
		try {
			con = getConnection();
			logger.debug("looking for shared folder map on {}@{}",mailUserId,domainId);
			OUserMap omap=UserMapDAO.getInstance().selectFirstByMailUser(con, domainId, mailUserId);
			OUser ouser;
			if (omap!=null) {
				logger.debug("found mapping : {}",omap.getUserId());
				//get mapped webtop user
				ouser=UserDAO.getInstance().selectByDomainUser(con, domainId, omap.getUserId());
			} else {
				logger.debug("mapping not found, looking for a webtop user with id = {}",mailUserId);
				//try looking for a webtop user with userId=mailUserId
				ouser=UserDAO.getInstance().selectByDomainUser(con, domainId, mailUserId);
			}
			
			String desc=null;
			if (ouser!=null) {
				desc=LangUtils.value(ouser.getDisplayName(),"");
			} else {
				String email=mailUserId;
				if (email.indexOf("@")<0) email+="@"+WT.getDomainInternetName(domainId);
				UserProfile.Data udata=WT.guessUserData(email);
				if (udata!=null) desc=LangUtils.value(udata.getDisplayName(),"");
			}
			
			if (desc!=null) {
				logger.debug("webtop user found, desc={}",desc);
				p = new SharedPrincipal(mailUserId, desc.trim());
			} else {
				logger.debug("webtop user not found, creating unmapped principal");
				p = new SharedPrincipal(mailUserId, mailUserId);
			}
			
		} catch (SQLException exc) {
			logger.error("Error finding principal for {}@{}", mailUserId, domainId, exc);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return p;
	}
	
	private void createFile(InputStream in, File outfile) throws IOException {
		FileOutputStream out = new FileOutputStream(outfile);
		byte buffer[] = new byte[8192];
		int n = 0;
		while ((n = in.read(buffer)) >= 0) {
			if (n > 0) {
				out.write(buffer, 0, n);
			}
		}
		out.close();
	}
	
	private void fastStreamCopy(final InputStream src, final OutputStream dest) throws IOException {
		final ReadableByteChannel in = Channels.newChannel(src);
		final WritableByteChannel out = Channels.newChannel(dest);		
		fastChannelCopy(in, out);
		in.close();
		out.close();
	}
	
	public static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
		while (src.read(buffer) != -1) {
			// prepare the buffer to be drained
			buffer.flip();
			// write to the channel, may block
			dest.write(buffer);
	      // If partial transfer, shift remainder down
			// If buffer is empty, same as doing clear()
			buffer.compact();
		}
		// EOF will leave buffer in fill state
		buffer.flip();
		// make sure the buffer is fully drained.
		while (buffer.hasRemaining()) {
			dest.write(buffer);
		}
	}
	
	
	@Override
	public ServiceVars returnServiceVars() {
		ServiceVars co = new ServiceVars();
		Connection con=null;
		try {
			con=getConnection();
			List<Identity> identities=mailManager.listIdentities();
			
			List<Identity> jsidents=new ArrayList();
			jsidents.addAll(identities);
			for(Identity jsid: jsidents) {	
				//if (jsid.isType(Identity.TYPE_AUTO)) {
				//	jsid.setMainFolder(getSharedFolderName(jsid.getOriginPid().getUserId(), "INBOX"));
				//}
				if (jsid.isMainIdentity()) loadMainIdentityMailcard(jsid);
				else loadIdentityMailcard(jsid);
			}
			
			co.put("folderDrafts", us.getFolderDrafts());
			co.put("folderSent", us.getFolderSent());
			co.put("folderSpam", us.getFolderSpam());
			co.put("folderTrash", us.getFolderTrash());
			co.put("format", us.getFormat());
			co.put("fontName", us.getFontName());
			co.put("fontSize", us.getFontSize());
			co.put("receipt", us.isReceipt());
			co.put("priority", us.isPriority());
			co.put("pageRows", us.getPageRows());
			co.put("schedDisabled",ss.isScheduledEmailsDisabled());
			co.put("identities", jsidents);
			co.put("manualSeen",us.isManualSeen());
			co.put("messageViewRegion",us.getMessageViewRegion());
			co.put("messageViewWidth",us.getMessageViewWidth());
			co.put("messageViewHeight",us.getMessageViewHeight());
			co.put("messageViewCollapsed",us.getMessageViewCollapsed());
			co.put("messageViewMaxTos",ss.getMessageViewMaxTos());
			co.put("messageViewMaxCcs",ss.getMessageViewMaxCcs());
			co.put("columnSizes",JsonResult.gson.toJson(us.getColumnSizes()));
			
			UserProfile profile = environment.getProfile();
			OVacation vacation=VacationDAO.getInstance().selectByUserId(con, profile.getDomainId(), profile.getUserId());
			boolean vactive=false;
			String vmessage="";
			String vaddresses=profile.getEmailAddress();
			if (vacation!=null) {
				vactive=vacation.getActive();
				vmessage=vacation.getMessage();
				vaddresses=vacation.getAddresses();
			}
			co.put("vacationActive",vactive);
			co.put("vacationMessage",vmessage);
			co.put("vacationAddresses",vaddresses);
			
		} catch(Exception ex) {
			logger.error("Error getting client options", ex);	
		} finally {
			DbUtils.closeQuietly(con);
		}
		return co;
	}
	
	private void loadMainIdentityMailcard(Identity id) {
		Mailcard mc = mailManager.getMailcard();
		try {
			UserProfile.PersonalInfo upi = WT.getCoreManager().getUserPersonalInfo(mailManager.getTargetProfileId());
			mc.substitutePlaceholders(upi);			
		} catch(WTException exc) {
			logger.error("cannot load user personal info",exc);
		}
		//mc.html=LangUtils.stripLineBreaks(mc.html);
		id.setMailcard(mc);
	}
	
	private void loadIdentityMailcard(Identity id) {
		Mailcard mc = mailManager.getMailcard(id);
		if (mc!=null) {
			if(id.isType(Identity.TYPE_AUTO)) {
				UserProfile.Id opid=id.getOriginPid();
				// In case of auto identities we need to build real mainfolder
				try {
					String mailUser = getMailUsername(opid);
					String mainfolder = getSharedFolderName(mailUser, "INBOX" /*id.getMainFolder()*/);
					id.setMainFolder(mainfolder);
					if(mainfolder == null) throw new Exception(MessageFormat.format("Shared folderName is null [{0}, {1}]", mailUser, id.getMainFolder()));
				} catch (Exception ex) {
					logger.error("Unable to get auto identity foldername [{}]", id.getEmail(), ex);
				}

				/*
				// Avoids default mailcard display for automatic identities
				if(mc.source.equals(Mailcard.TYPE_DEFAULT) && !StringUtils.isEmpty(id.getMainFolder())) {
					mc = getMailcard();
				}*/
				
				if (!id.isForceMailcard()) mc=mailManager.getMailcard();

				try {
					UserProfile.PersonalInfo upi = WT.getCoreManager().getUserPersonalInfo(opid);
					mc.substitutePlaceholders(upi);
				} catch (Exception ex) {
					logger.error("Unable to get auto identity personal info [{}]", id.getEmail(), ex);
				}
			}
		}
		//mc.html=LangUtils.stripLineBreaks(mc.html);
		id.setMailcard(mc);
	}
	
	private String getMailUsername(UserProfile.Id userProfileId) {
		Connection con=null;
		String username=null;
		try {
			con=WT.getConnection(SERVICE_ID);
			OUserMap omap=UserMapDAO.getInstance().selectById(con, userProfileId.getDomainId(), userProfileId.getUserId());
			if (omap!=null) username=omap.getMailUser();
			if (username==null || username.isEmpty()) username=userProfileId.getUserId();
		} catch(Exception exc) {
			logger.error("Error mapping mail user",exc);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return username+"@"+WT.getDomainInternetName(userProfileId.getDomainId());
	}	
	
}
