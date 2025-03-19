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

import com.sonicle.commons.AlgoUtils;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.webtop.core.app.PrivateEnvironment;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.commons.LangUtils;
import java.nio.*;
import java.nio.channels.*;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.RegexUtils;
import com.sonicle.commons.ResourceUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.cache.AbstractPassiveExpiringBulkSet;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.http.HttpClientUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.DispositionType;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.JsonUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.MapItemList;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.mail.imap.SonicleIMAPFolder;
import com.sonicle.mail.imap.SonicleIMAPMessage;
import com.sonicle.mail.sieve.SieveAction;
import com.sonicle.mail.sieve.SieveActionMethod;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.Principal;
import com.sonicle.security.auth.directory.LdapNethDirectory;
import com.sonicle.webtop.calendar.model.GetEventScope;
import com.sonicle.webtop.calendar.ICalendarManager;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.contacts.IContactsManager;
import com.sonicle.webtop.contacts.io.ContactInput;
import com.sonicle.webtop.contacts.io.VCardInput;
import com.sonicle.webtop.contacts.model.ContactPictureWithBytes;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.DocEditorManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.app.sdk.BaseDocEditorDocumentHandler;
import com.sonicle.webtop.core.app.servlet.ResourceRequest;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsHiddenFolder;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.model.Recipient;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.*;
import com.sonicle.webtop.core.sdk.interfaces.IServiceUploadStreamListener;
import com.sonicle.webtop.core.app.servlet.ServletHelper;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.mail.bol.ONote;
import com.sonicle.webtop.mail.bol.OScan;
import com.sonicle.webtop.mail.bol.OUserMap;
import com.sonicle.webtop.mail.bol.js.JsAttachment;
import com.sonicle.webtop.mail.bol.js.JsContactData;
import com.sonicle.webtop.mail.bol.js.JsFolder;
import com.sonicle.webtop.mail.bol.js.JsInMailFilters;
import com.sonicle.webtop.mail.bol.js.JsMailAutosave;
import com.sonicle.webtop.mail.bol.js.JsMessage;
import com.sonicle.webtop.mail.bol.js.JsPortletSearchResult;
import com.sonicle.webtop.mail.bol.js.JsPreviewMessage;
import com.sonicle.webtop.mail.bol.js.JsQuickPartModel;
import com.sonicle.webtop.mail.bol.js.JsRecipient;
import com.sonicle.webtop.mail.bol.js.JsSharing;
import com.sonicle.webtop.mail.bol.js.JsSmartSearchTotals;
import com.sonicle.webtop.mail.bol.js.JsSort;
import com.sonicle.webtop.mail.bol.model.Identity;
import com.sonicle.webtop.mail.dal.NoteDAO;
import com.sonicle.webtop.mail.dal.ScanDAO;
import com.sonicle.webtop.mail.dal.UserMapDAO;
import com.sonicle.webtop.mail.model.AutoResponder;
import com.sonicle.webtop.mail.model.ExternalAccount;
import com.sonicle.webtop.mail.model.MailEditFormat;
import com.sonicle.webtop.mail.model.MailFilter;
import com.sonicle.webtop.mail.model.MailFiltersType;
//import com.sonicle.webtop.mail.model.Tag;
import com.sonicle.webtop.mail.ws.AddContactMessage;
import com.sonicle.webtop.vfs.IVfsManager;
import com.sun.mail.imap.*;
import com.sun.mail.util.PropUtil;
import java.io.*;
import java.net.URI;
import java.sql.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.activation.*;
import jakarta.mail.*;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.fortuna.ical4j.model.parameter.PartStat;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.http.client.HttpClient;
import org.apache.commons.vfs2.FileSystemException;
import org.joda.time.DateTimeZone;
import com.sonicle.commons.qbuilders.conditions.Condition;
import com.sonicle.commons.web.ParameterException;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.bean.QueryObj;
import com.sonicle.commons.web.json.extjs.FieldMeta;
import com.sonicle.commons.web.json.extjs.GridMetadata;
import com.sonicle.commons.web.json.extjs.SortMeta;
import com.sonicle.mail.UniqueValue;
import com.sonicle.mail.email.CalendarMethod;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.mail.parser.MimeMessageParser;
import com.sonicle.webtop.contacts.ContactsUtils;
import com.sonicle.webtop.contacts.model.ContactQuery;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.model.Sharing;
import com.sonicle.webtop.core.app.sdk.msg.MessageBoxSM;
import com.sonicle.webtop.core.app.servlet.js.BlobInfoPayload;
import com.sonicle.webtop.core.bol.js.JsAuditMessageInfo;
import com.sonicle.webtop.core.model.RecipientFieldType;
import com.sonicle.webtop.core.model.Tag;
import com.sonicle.webtop.core.util.ICalendarHelper;
import com.sonicle.webtop.mail.bol.js.JsAdvSearchMessage;
import com.sonicle.webtop.mail.bol.js.JsEnvelope;
import com.sonicle.webtop.mail.bol.js.JsListedMessage;
import com.sonicle.webtop.mail.bol.js.JsMessageDetails;
import com.sonicle.webtop.mail.bol.js.JsOperateFolder;
import com.sonicle.webtop.mail.bol.js.JsOperateMessage;
import com.sonicle.webtop.mail.bol.js.JsProActiveSecurity;
import com.sonicle.webtop.mail.bol.js.JsQuickPart;
import com.sonicle.webtop.mail.bol.js.JsRecipient;
import com.sonicle.webtop.mail.bol.model.ImapQuery;
import com.sonicle.webtop.mail.model.FolderShareParameters;
import com.sonicle.webtop.mail.model.SieveRuleList;
import java.text.Normalizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.SearchTerm;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import net.fortuna.ical4j.data.ParserException;
import org.slf4j.Logger;

public class Service extends BaseService {
	
	public final static Logger logger = WT.getLogger(Service.class);
	public static final String META_CONTEXT_SEARCH = "mainsearch";
	public final String[] SPAM_THRESHOLD_HEADERS = new String[] {
		"X-Rspamd-Flag-Threshold",
		"X-Spam-Threshold"
	};
	
	class WebtopFlag {
		String label;
		
		WebtopFlag(String label) {
			this.label=label;
		}
		
	}
	
    public WebtopFlag[] webtopFlags={
        new WebtopFlag("red"),
        new WebtopFlag("blue"),
        new WebtopFlag("yellow"),
        new WebtopFlag("green"),
        new WebtopFlag("orange"),
        new WebtopFlag("purple"),
        new WebtopFlag("black"),
        new WebtopFlag("gray"),
        new WebtopFlag("white"),
        new WebtopFlag("brown"),
        new WebtopFlag("azure"),
        new WebtopFlag("pink"),
        new WebtopFlag("complete")
	};
	
	public String allFlagStrings[];
	
	public static Flags flagsAll = new Flags();
	public static Flags oldFlagsAll = new Flags();
	public static HashMap<String, Flags> flagsHash = new HashMap<String, Flags>();
	public static HashMap<String, Flags> oldFlagsHash = new HashMap<String, Flags>();
	
//	protected List<Tag> atags=new ArrayList<>();
//	protected HashMap<String,Tag> htags=new HashMap<>();
	
	private FetchProfile FP = new FetchProfile();
	private FetchProfile draftsFP=new FetchProfile();
	private FetchProfile pecFP=new FetchProfile();
	
	public static final String HDR_PEC_PROTOCOLLO="X-Protocollo";
	public static final String HDR_PEC_RIFERIMENTO_MESSAGE_ID="X-Riferimento-Message-ID";
	public static final String HDR_PEC_TRASPORTO="X-Trasporto";
	public static final String HDR_PEC_RICEVUTA="X-Ricevuta";
	public static final String HDR_PEC_TIPORICEVUTA="X-Tiporicevuta";
	
	public static final String HDR_PEC_RICEVUTA_VALUE_ACCETTAZIONE="accettazione";
	public static final String HDR_PEC_RICEVUTA_VALUE_NON_ACCETTAZIONE="non-accettazione";
	public static final String HDR_PEC_RICEVUTA_VALUE_AVVENUTA_CONSEGNA="avvenuta-consegna";
	public static final String HDR_PEC_TIPORICEVUTA_VALUE_BREVE="breve";
	public static final String HDR_PEC_TIPORICEVUTA_VALUE_COMPLETA="completa";
	
	private boolean sortfolders=false;
	
	public static final String HEADER_SONICLE_FROM_DRAFTER="Sonicle-from-drafter";	
	
	public static final String HEADER_X_WEBTOP_MSGID="X-WEBTOP-MSGID";
	
	static String startpre = "<PRE>";
	static String endpre = "</PRE>";

	protected static final String MAIN_ACCOUNT_ID="main";
	protected static final String ARCHIVE_ACCOUNT_ID="archive";
	
	private MailManager mailManager;
	private MailAccount mainAccount=null;
	private MailAccount archiveAccount=null;
	private ArrayList<MailAccount> externalAccounts=new ArrayList<MailAccount>();
	private HashMap<String,MailAccount> accounts=new HashMap<>();
	private HashMap<String,ExternalAccount> externalAccountsMap=new HashMap<>();
	
	private PrivateEnvironment environment = null;
	private MailUserProfile mprofile;
	private MailServiceSettings ss = null;
	private MailUserSettings us = null;
	private CoreUserSettings cus = null;
	private int newMessageID = 0;
	private MailFoldersThread mft;
	
	private FolderCache fcProvided = null;
	
	private ArrayList<String> inlineableMimes = new ArrayList<String>();
	
	private HashMap<Long, ArrayList<CloudAttachment>> msgcloudattach = new HashMap<Long, ArrayList<CloudAttachment>>();
	private ArrayList<CloudAttachment> emptyAttachments = new ArrayList<CloudAttachment>();
	
	private AdvancedSearchThread ast = null;
	
	private IVfsManager vfsmanager=null;
	private SmartSearchThread sst;
	private PortletSearchThread pst;
	
	private boolean previewBalanceTags=true;
	
	private boolean refwSanitizeDownlevelRevealedComments=false;
	
	private ProActiveSecurityRules pasRules=new ProActiveSecurityRules();
	private ArrayList<String> pasDangerousExtensions=new ArrayList<>();
	private float pasDefaultSpamThreshold;
	private Pattern pasDomainsWhiteListRegexPattern;
	private final FoldersNamesInByFileFiltersCache cacheFoldersNamesInByFileFilters = new FoldersNamesInByFileFiltersCache(5, TimeUnit.MINUTES);
	
	@Override
	public void initialize() {
		
		ArrayList<String> allFlagsArray=new ArrayList<String>();
		for(WebtopFlag fs: webtopFlags) {
			allFlagsArray.add(fs.label);
			String oldfs="flag"+fs.label;
			flagsAll.add(fs.label);
			oldFlagsAll.add(oldfs);
			Flags flags=new Flags();
			flags.add(fs.label);
			flagsHash.put(fs.label, flags);
			flags=new Flags();
			flags.add(oldfs);
			oldFlagsHash.put(fs.label, flags);
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
		String mtypes=ss.getInlineableMimeTypes();
		if (StringUtils.isBlank(mtypes)) {
			inlineableMimes.add("image/gif");
			inlineableMimes.add("image/jpeg");
			inlineableMimes.add("image/png");
			inlineableMimes.add("text/plain");
			inlineableMimes.add("text/html");
		} else {
			String vmtypes[]=StringUtils.split(mtypes, ",");
			for(String mtype:vmtypes)
				inlineableMimes.add(mtype.trim());
		}
		
		
		us = new MailUserSettings(profile.getId(),ss);
		mprofile = new MailUserProfile(mailManager,ss,us,profile);
		String mailUsername = mprofile.getMailUsername();
		String mailPassword = mprofile.getMailPassword();
		boolean isImpersonated=profile.getPrincipal().isImpersonated();
		String vmailSecret=ss.getNethTopVmailSecret();
		if (isImpersonated) {
			//use sasl rfc impersonate if no vmailSecret
			if (vmailSecret==null) {
				//TODO: implement sasl rfc authorization id if possible
				//session.getProperties().setProperty("mail.imap.sasl.authorizationid", authorizationId);
				//mailUsername=ss.getAdminUser();
				//mailPassword=ss.getAdminPassword();
				mailManager.setSieveConfiguration(mprofile.getMailHost(), ss.getSievePort(), ss.getAdminUser(), ss.getAdminPassword(), mailUsername);
				
			} else {
				mailUsername+="*vmail";
				mailPassword=vmailSecret;
				mailManager.setSieveConfiguration(mprofile.getMailHost(), ss.getSievePort(), mailUsername, mailPassword, null);
			}
			
		} else {
			mailManager.setSieveConfiguration(mprofile.getMailHost(), ss.getSievePort(), mailUsername, mailPassword, null);
		}
		
		fcProvided = new FolderCache(this, environment);
		
		previewBalanceTags=ss.isPreviewBalanceTags();
		
		mainAccount=createAccount(MAIN_ACCOUNT_ID);
		mainAccount.setFolderPrefix(mprofile.getFolderPrefix());
		mainAccount.setProtocol(mprofile.getMailProtocol());
		
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
		if (hasDmsDocumentArchiving()) {
			FP.add("X-WT-Archived");
			draftsFP.add("X-WT-Archived");
		}
		pecFP.add(FetchProfile.Item.ENVELOPE);
		pecFP.add(FetchProfile.Item.FLAGS);
		pecFP.add(FetchProfile.Item.CONTENT_INFO);
		pecFP.add(UIDFolder.FetchProfileItem.UID);
		pecFP.add("Message-ID");
		pecFP.add("X-Priority");
		pecFP.add(HDR_PEC_PROTOCOLLO);
		pecFP.add(HDR_PEC_RIFERIMENTO_MESSAGE_ID);
		pecFP.add(HDR_PEC_TRASPORTO);
		pecFP.add(HDR_PEC_RICEVUTA);
		pecFP.add(HDR_PEC_TIPORICEVUTA);

		sortfolders=ss.isSortFolder();
		
		mainAccount.setDifferentDefaultFolder(us.getDefaultFolder());
		
		mainAccount.setMailSession(environment.getSession().getMailSession());

		mainAccount.setPort(mprofile.getMailPort());
		mainAccount.setHost(mprofile.getMailHost());
		mainAccount.setUsername(mprofile.getMailUsername());
		mainAccount.setPassword(mprofile.getMailPassword());
		mainAccount.setReplyTo(mprofile.getReplyTo());
		
		if (isImpersonated) {
			if (vmailSecret==null) mainAccount.setSaslRFCImpersonate(mprofile.getMailUsername(), ss.getAdminUser(), ss.getAdminPassword());
			else mainAccount.setNethImpersonate(mprofile.getMailUsername(),vmailSecret);
		}
		mainAccount.setFolderSent(mprofile.getFolderSent());
		mainAccount.setFolderDrafts(mprofile.getFolderDrafts());
		mainAccount.setFolderSpam(mprofile.getFolderSpam());
		mainAccount.setFolderTrash(mprofile.getFolderTrash());
		mainAccount.setFolderArchive(mprofile.getFolderArchive());
		
		//TODO initialize user for first time use
		//SettingsManager sm = wta.getSettingsManager();
		//boolean initUser = LangUtils.value(sm.getUserSetting(profile, "mail", com.sonicle.webtop.Settings.INITIALIZE), false);
		//if(initUser) initializeUser(profile);

		mft = new MailFoldersThread(this, environment, mainAccount);
		mft.setCheckAll(mprofile.isScanAll());
		mft.setSleepInbox(mprofile.getScanSeconds());
		mft.setSleepCycles(mprofile.getScanCycles());
		try {
			//loadTags();
		
			mft.abort();
			mainAccount.checkStoreConnected();
			
			//prepare special folders if not existant
			if (ss.isAutocreateSpecialFolders()) {
				mainAccount.createSpecialFolders();
			}
			
			mainAccount.setSkipReplyFolders(new String[]{
				mainAccount.getFolderDrafts(),
				mainAccount.getFolderSent(),
				mainAccount.getFolderSpam(),
				mainAccount.getFolderTrash(),
				mainAccount.getFolderArchive()
			});
			mainAccount.setSkipForwardFolders(new String[]{
				mainAccount.getFolderSpam(),
				mainAccount.getFolderTrash(),
			});
			
			mainAccount.loadFoldersCache(mft,false);
			//if (!mainAccount.getMailSession().getDebug())
			mft.start();
			
			vfsmanager=(IVfsManager)WT.getServiceManager("com.sonicle.webtop.vfs");
			//cloud uploads goes here
			registerUploadListener("UploadCloudFile", new OnUploadCloudFile());
			
			if (!profile.getPrincipal().getAuthenticationDomain().getDirUri().getScheme().equals(LdapNethDirectory.SCHEME))
				setSharedSeen(mainAccount,us.isSharedSeen());
			
			
			//if external archive, initialize account
			if (ss.isArchivingExternal()) {
				archiveAccount=createAccount(ARCHIVE_ACCOUNT_ID);
				
				//defaults to WebTop External Archive
				archiveAccount.setHasInboxFolder(true); //archive copy creates INBOX folder under user archive
				String defaultFolder=us.getArchiveExternalUserFolder();
				if (defaultFolder==null || defaultFolder.trim().length()==0)
					defaultFolder=profile.getUserId();
				archiveAccount.setDifferentDefaultFolder(defaultFolder);
				
				archiveAccount.setFolderPrefix(ss.getArchivingExternalFolderPrefix());
				archiveAccount.setProtocol(ss.getArchivingExternalProtocol());

				archiveAccount.setMailSession(environment.getSession().getMailSession());

				archiveAccount.setPort(ss.getArchivingExternalPort());
				archiveAccount.setHost(ss.getArchivingExternalHost());
				archiveAccount.setUsername(ss.getArchivingExternalUsername());
				archiveAccount.setPassword(ss.getArchivingExternalPassword());
				
				archiveAccount.setFolderSent(mprofile.getFolderSent());
				archiveAccount.setFolderDrafts(mprofile.getFolderDrafts());
				archiveAccount.setFolderSpam(mprofile.getFolderSpam());
				archiveAccount.setFolderTrash(mprofile.getFolderTrash());
				archiveAccount.setFolderArchive(mprofile.getFolderArchive());				
			}
			
			//add any configured external account
			for(ExternalAccount extacc: mailManager.listExternalAccounts()) {
				String id=extacc.getExternalAccountId().toString();
				externalAccountsMap.put(id,extacc);
				
				MailAccount acct=createAccount(id);
				acct.setFolderPrefix(extacc.getFolderPrefix());
				acct.setProtocol(extacc.getProtocol());

				acct.setMailSession(environment.getSession().getMailSession());

				acct.setPort(extacc.getPort());
				acct.setHost(extacc.getHost());
				acct.setUsername(extacc.getUserName());
				acct.setPassword(extacc.getPassword());
				
				acct.setFolderSent(extacc.getFolderSent());
				acct.setFolderDrafts(extacc.getFolderDrafts());
				acct.setFolderSpam(extacc.getFolderSpam());
				acct.setFolderTrash(extacc.getFolderTrash());
				acct.setFolderArchive(extacc.getFolderArchive());
				acct.setReadOnly(extacc.isReadOnly());
				
				externalAccounts.add(acct);

				MailFoldersThread xmft = new MailFoldersThread(this, environment, acct);
				xmft.setInboxOnly(true);
				
				acct.setFoldersThread(xmft);
				acct.checkStoreConnected();
				acct.loadFoldersCache(xmft,false);
				
				//MFT start postponed to first processGetFavoritesTree
			}

			
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
		refwSanitizeDownlevelRevealedComments = ss.isReFwSanitizeDownlevelRevealedComments();
		
		//PAS
		UserProfileId tpid=mailManager.getTargetProfileId();
		String sid=SERVICE_ID;
		String pkey="PRO_ACTIVE_SECURITY";
		pasRules.setActive(!RunContext.isPermitted(true, tpid, sid, pkey, "DISABLED"));
		pasRules.setLinkDomainCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_LINK_DOMAIN_CHECK"));
		pasRules.setMyDomainCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_MY_DOMAIN_CHECK"));
		pasRules.setFrequentContactCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_FREQUENT_CONTACT_CHECK"));
		pasRules.setAnyContactsCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_ANY_CONTACTS_CHECK"));
		pasRules.setTrustedContactsCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_TRUSTED_CONTACTS_CHECK"));
		pasRules.setFakePatternsCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_FAKE_PATTERNS_CHECK"));
		pasRules.setUnsubscribeDirectivesCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_UNSUBSCRIBE_DIRECTIVES_CHECK"));
		pasRules.setDisplaynameCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_DISPLAYNAME_CHECK"));
		pasRules.setSpamScoreVisualization(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_SPAM_SCORE_VISUALIZATION"));
		pasRules.setLinkClickPrompt(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_LINK_CLICK_PROMPT"));
		pasRules.setZipCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_ZIP_CHECK"));
		//pasRules.setForgedSenderCheck(!RunContext.isPermitted(true, tpid, sid, pkey, "NO_FORGED_SENDER_CHECK,"));
		pasRules.setForgedSenderCheck(false);
		pasRules.setLinkGeolocalization(RunContext.isPermitted(true, tpid, sid, pkey, "DO_LINK_GEOLOCALIZATION"));
		
		pasDefaultSpamThreshold=ss.getPasSpamThreshold();
		String exts[]=ss.getPasDangerousExtensions().split(",");
		for(String ext: exts) pasDangerousExtensions.add(ext.trim().toLowerCase());

		List<String> domainsWhiteList=new ArrayList<String>();
		try {
			URL dwlUrl=ResourceUtils.getResource("com/sonicle/webtop/mail/pas-domains-whitelist.txt");
			if (dwlUrl!=null) {
				BufferedReader dwlIn = new BufferedReader(
					new InputStreamReader(dwlUrl.openStream()));
				String line;
				while ((line = dwlIn.readLine()) != null) {
					domainsWhiteList.add(line.trim().toLowerCase());
				}
				dwlIn.close();
			}
		} catch(Exception exc) {}
		String additionalDomainsWhiteList = ss.getPasAdditionalDomainsWhitelist();
		if (!StringUtils.isEmpty(additionalDomainsWhiteList)) {
			for (String dom: additionalDomainsWhiteList.split(",")) {
				domainsWhiteList.add(dom.trim().toLowerCase());
			}
		}
		String regex=null;
		for (String dom: domainsWhiteList) {
			if (regex==null) regex="";
			else regex+="|";
			regex+=".*"+RegexUtils.escapeRegexSpecialChars(dom)+"$";
		}
		if (regex!=null) pasDomainsWhiteListRegexPattern = Pattern.compile(regex);
	}
	
	private MailAccount createAccount(String id) {
		MailAccount account=new MailAccount(id,this,environment);
		accounts.put(id, account);
		return account;
	}
	
	public MailAccount getMainAccount() {
		return mainAccount;
	}
	
	public MailAccount getArchiveAccount() {
		return archiveAccount;
	}
	
/*	private synchronized void loadTags() throws WTException {		
		atags.clear();
		htags.clear();
		atags=mailManager.getTags();
		for(Tag tag: atags) {
			htags.put(tag.getTagId(), tag);
		}
	}*/
	
	public MailServiceSettings getMailServiceSettings() {
		return this.ss;
	}
	
	public MailUserSettings getMailUserSettings() {
		return this.us;	
	}
	
	public CoreUserSettings getCoreUserSettings() {
		return this.cus;
	}
	
	
	public FetchProfile getMessageFetchProfile() {
		return FP;
	}

	public MailFoldersThread getMailFoldersThread() {
		return mft;
	}
	
	public void dmsArchiveMessages(FolderCache from, long nuids[], String idcategory, String idsubcategory, boolean fullthreads) throws MessagingException, FileNotFoundException, IOException {
		UserProfile profile = environment.getProfile();
		String archiveto = ss.getDmsArchivePath();
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
		from.markDmsArchivedMessages(nuids,fullthreads);
	}
	
	private void deleteAutosavedDraft(MailAccount account, long msgId) {
		int retry=3;
		while(retry>0) {
			try {
				account.deleteByHeaderValue(HEADER_X_WEBTOP_MSGID,""+msgId);
				if (retry<3) logger.debug("Retry deleting automatic draft succeded");
				break;
			} catch(Throwable t) {
				logger.debug("Error deleting automatic draft",t);
			}
			if (--retry >0) {
				logger.debug("Retrying delete of automatic draft after exception");
				try { Thread.sleep(1000); } catch(InterruptedException exc) {}
			}
		}
		if (retry==0) logger.debug("Delete of automatic draft failed");
	}
	
	private MailAccount getAccount(Identity ident) {
		if (ident==null) return mainAccount;
		MailAccount account=ident.getAccount();
		return account!=null?account:mainAccount;
	}
	
	protected InputStream getAttachmentInputStream(String accountId, String foldername, long uidmessage, int idattach) throws MessagingException, IOException {
		MailAccount account=getAccount(accountId);
		FolderCache fc = account.getFolderCache(foldername);
		Message m = fc.getMessage(uidmessage);

		HTMLMailData mailData = fc.getMailData((MimeMessage) m);
		return mailData.getAttachmentPart(idattach).getInputStream();
	}
	
	@Override
	public void processManageAutosave(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			String cntx = ServletUtils.getStringParameter(request, "context", true);
			if ("newmail".equals(cntx) && crud.equals(Crud.UPDATE)) {
				String key = ServletUtils.getStringParameter(request, "key", true);
				Payload<MapItem, JsMailAutosave> pl = ServletUtils.getPayload(request, JsMailAutosave.class, true);
				
				CoreManager core = WT.getCoreManager();
				String cid = getEnv().getClientTrackingID();
				long msgId = Long.parseLong(key);
				core.updateMyAutosaveData(cid, SERVICE_ID, cntx, key, pl.raw);
				
				try {
					Identity id=mailManager.findIdentity(pl.data.identityId);
					MailAccount account=getAccount(id);
					JsMessage jsmsg=new JsMessage();
					jsmsg.content=pl.data.content;
					jsmsg.folder=pl.data.folder;
					jsmsg.format=pl.data.format;
					jsmsg.forwardedfolder=pl.data.forwardedfolder;
					jsmsg.forwardedfrom=pl.data.forwardedfrom;
					jsmsg.from=id.getDisplayName()+" <"+id.getEmail()+">";
					jsmsg.identityId=pl.data.identityId;
					jsmsg.inreplyto=pl.data.inreplyto;
					jsmsg.origuid=pl.data.origuid;
					jsmsg.priority=pl.data.priority;
					jsmsg.receipt=pl.data.receipt;			
					jsmsg.torecipients=new ArrayList<>();
					jsmsg.ccrecipients=new ArrayList<>();
					jsmsg.bccrecipients=new ArrayList<>();
					for(int r=0;r<pl.data.torecipients.size();++r) {
						String email=pl.data.torecipients.get(r).email;
						jsmsg.torecipients.add(new JsRecipient(email));
					}
					for(int r=0;r<pl.data.ccrecipients.size();++r) {
						String email=pl.data.ccrecipients.get(r).email;
						jsmsg.ccrecipients.add(new JsRecipient(email));
					}
					for(int r=0;r<pl.data.bccrecipients.size();++r) {
						String email=pl.data.bccrecipients.get(r).email;
						jsmsg.bccrecipients.add(new JsRecipient(email));
					}
					jsmsg.references=pl.data.references;
					jsmsg.replyfolder=pl.data.replyfolder;
					jsmsg.subject=pl.data.subject;

					SimpleMessage msg = prepareMessage(jsmsg,msgId,true,false,true);
					account.checkStoreConnected();
					FolderCache fc = account.getFolderCache(account.getFolderDrafts());
					
					//find and delete old draft for this msgid
					account.deleteByHeaderValue(HEADER_X_WEBTOP_MSGID,""+msgId);

					msg.addHeaderLine(HEADER_X_WEBTOP_MSGID+": "+msgId);
					Exception exc = saveMessage(msg, null, fc, false);
					
				} catch(Exception exc) {
					logger.debug("Error on autosave in drafts!",exc);
				}
				new JsonResult().printTo(out);
				
			} else {
				super.processManageAutosave(request, response, out);
			}
			
		} catch (Throwable t) {
			logger.error("Error in ManageAutosave", t);
			new JsonResult(t).printTo(out);
		}
	}	
	
	public void moveMessages(FolderCache from, FolderCache to, long uids[], boolean fullthreads) throws MessagingException {
		from.moveMessages(uids, to, fullthreads);
	}
	
	public void copyMessages(FolderCache from, FolderCache to, long uids[], boolean fullthreads) throws MessagingException, IOException {
		from.copyMessages(uids, to, fullthreads);
	}
	
	public void deleteMessages(FolderCache from, long uids[], boolean fullthreads) throws MessagingException {
		from.deleteMessages(uids,fullthreads);
	}
	
	public void archiveMessages(FolderCache from, String folderarchive, long uids[], boolean fullthreads) throws MessagingException {
		from.archiveMessages(uids, folderarchive, fullthreads);
	}
	
	public void flagMessages(FolderCache from, long uids[], String flag) throws MessagingException {
		from.flagMessages(uids, flag);
	}
	
	public void tagMessages(FolderCache from, long uids[], String tagId) throws MessagingException {
		from.tagMessages(uids, tagId);
	}
	
	public void untagMessages(FolderCache from, long uids[], String tagId) throws MessagingException {
		from.untagMessages(uids, tagId);
	}
	
	public void applyMessagesTags(FolderCache from, long uids[], String tagIds[]) throws MessagingException {
		from.applyMessagesTags(uids,tagIds);
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
	
	public void hideFolder(MailAccount account, String foldername) throws MessagingException {
		Folder folder=account.getFolder(foldername);
		try { folder.close(false); } catch(Throwable exc) {}
		account.destroyFolderCache(foldername);
		if (account==mainAccount) us.setFolderHidden(foldername, true);
		else us.setFolderHidden(mainAccount.getId()+"_"+foldername, true);
	}
	
	public boolean isFolderHidden(MailAccount account, String foldername) {
		if (account==mainAccount) return us.isFolderHidden(foldername);
		else return us.isFolderHidden(mainAccount.getId()+"_"+foldername);
	}
	
	private InternetAddress getInternetAddress(String email) throws UnsupportedEncodingException, AddressException {
	  email=email.trim();
	  if (!email.startsWith("\"")) {
		  int ix=email.lastIndexOf("<");
		  if (ix>=0) {
			  String personal=email.substring(0,ix).trim();
			  String address=email.substring(ix).trim();
			  email="\""+personal+"\" "+address;
		  }
	  }
	  InternetAddress ia[]=InternetAddress.parse(email, false);
	  //build an InternetAddress with UTF8 personal, if present
	  InternetAddress retia=InternetAddressUtils.toInternetAddress(ia[0].getAddress(),ia[0].getPersonal());
	  return retia;
	}
	
	public Identity findIdentity(InternetAddress fromAddr) {
		for(Identity ident: mprofile.getIdentities()) {
			if (fromAddr.getAddress().equalsIgnoreCase(ident.getEmail()))
				return ident;
		}
		return null;
	}
	
	public Exception sendReceipt(Identity ident, String from, String to, String subject, String body) {
		return sendTextMsg(ident, from, from, new String[]{to}, null, null, "Receipt: " + subject, body);
	}
	
	private Exception sendTextMsg(Identity ident, String fromAddr, String rplyAddr, String[] toAddr,
			String[] ccAddr,
			String[] bccAddr, String subject, String body) {
		
		return sendTextMsg(ident, fromAddr,
				rplyAddr, toAddr, ccAddr, bccAddr, subject, body, null);
		
	}
	
	private Exception sendTextMsg(Identity ident, String fromAddr, String rplyAddr, String[] toAddr, String[] ccAddr, String[] bccAddr,
			String subject, String body, List<JsAttachment> attachments) {
		
		SimpleMessage smsg = new SimpleMessage(0);

		//set the TO recipients
		smsg.addTo(toAddr);

		//set the CC recipients
		smsg.addCc(ccAddr);

		//set BCC recipients
		smsg.addBcc(bccAddr);
		
		if (ident!=null) smsg.setFrom(ident);

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
	
	public boolean sendMsg(InternetAddress from, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc, String subject, MimeMultipart part) {
		return sendMsg(null,from, to, cc, bcc, subject, part);
	}
	
	public boolean sendMsg(Identity ident, InternetAddress from, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc, String subject, MimeMultipart part) {
		
		try {
			subject = MimeUtility.encodeText(subject);
		} catch (Exception ex) {}
		
		try {
			MailAccount account=getAccount(ident);
			
			MimeMessage message = new MimeMessage(account.getMailSession());
			message.setSubject(subject);
			message.addFrom(new InternetAddress[] {from});

			if (to != null) {
				for(InternetAddress ia: to) message.addRecipient(Message.RecipientType.TO, ia);
			}
			if (cc != null) {
				for(InternetAddress ia: cc) message.addRecipient(Message.RecipientType.CC, ia);
			}
			if (bcc != null) {
				for(InternetAddress ia: bcc) message.addRecipient(Message.RecipientType.BCC, ia);
			}

			message.setContent(part);
			message.setSentDate(new java.util.Date());
			
			return sendMsg(ident,message);
			
		} catch(MessagingException ex) {
			logger.warn("Unable to send message", ex);
			return false;
		}
	}
	
	public boolean sendMsg(Message msg) {
		return sendMsg(null,msg);
	}
	
	public boolean sendMsg(Identity ident, Message msg) {
		if (ident==null) {
			try {
				ident=findIdentity((InternetAddress)(msg.getFrom()[0]));
			} catch(Exception exc) {

			}
		}
		String sentfolder = getSentFolder(ident);
		MailAccount account=getAccount(ident);
		try {
			Transport.send(msg);
			saveSent(account, msg, sentfolder);
			return true;
			
		} catch (Exception ex) {
			Service.logger.error("Exception",ex);
			return false;
		}
		
	}
	
	public String getSentFolder(InternetAddress address) {
		Identity ident = null;
		try {
			ident=findIdentity((InternetAddress)(address));
		} catch(Exception exc) {

		}
		return getSentFolder(ident);
	}
	
	public String getSentFolder(Identity ident) {
		UserProfile profile = environment.getProfile();
		String sentfolder = mainAccount.getFolderSent();
		if (ident != null ) {
			MailAccount account = getAccount(ident);
			String mainfolder=ident.getMainFolder();
			if (mainfolder != null && mainfolder.trim().length() > 0) {
				String newsentfolder = mainfolder + account.getFolderSeparator() + account.getLastFolderName(sentfolder);
				try {
					Folder folder = account.getFolder(newsentfolder);
					if (folder.exists()) {
						sentfolder = newsentfolder;
					}
				} catch (MessagingException exc) {
					logger.error("Error on identity {}/{} Sent Folder", profile.getUserId(), ident.getEmail(), exc);
				}
			}
		}

		return sentfolder;
	}
	
        class SendException extends Exception {
            boolean messageSent=false;
            boolean messageSaved=false;
            Exception exception=null;
			
			SendException(String message) {
				super(message);
			}
            
            void setMessageSent(boolean b) { messageSent=b; }
            void setMessageSaved(boolean b) { messageSaved=b; }
            void setException(Exception exc) { exception=exc; }
        }
        
	public SendException sendMsg(String from, SimpleMessage smsg, List<JsAttachment> attachments) {
		//UserProfile profile = environment.getProfile();
		//String sentfolder = mprofile.getFolderSent();
		Identity ident = smsg.getFrom();
		/*f (ident != null ) {
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
		}*/
		SendException retexc = null;
		Message msg = null;
		try {
			msg = createMessage(from, smsg, attachments, false);
			Transport.send(msg);
		} catch (Exception ex) {
			Service.logger.error("Exception",ex);
			String exmsg = ex.getMessage();
			if (ex.getCause()!=null) exmsg = ex.getCause().getMessage();
			retexc = new SendException(exmsg);
            retexc.setException(ex);			
		}

		retexc = saveSentOrFallbackToMainSent(smsg, msg, ident, retexc);
		
		return retexc;
		
	} //end sendMsg, SimpleMessage version
	
	public SendException saveSentOrFallbackToMainSent(SimpleMessage smsg, Message msg, Identity ident, SendException retexc) {
		String sentfolder=getSentFolder(ident);
		if (retexc == null && msg != null) {
			MailAccount account=getAccount(ident);
			Exception ex = saveSent(account, msg, sentfolder);
			if (ex!=null) {
				String exmsg = ex.getMessage();
				if (ex.getCause()!=null) exmsg = ex.getCause().getMessage();
				retexc = new SendException(exmsg);
				retexc.setMessageSent(true);

				//If shared account retry on main account
				if (!ident.isMainIdentity()) {
					sentfolder=mainAccount.getFolderSent();
					Exception sex = saveSent(mainAccount, msg, sentfolder);
					if (sex==null) {
						retexc.setMessageSaved(true);
					}
				}


				Service.logger.error("Exception",ex);
				retexc.setException(ex);
			}
		}
		if (mailManager.isAuditEnabled() && (retexc == null || retexc.messageSaved) && smsg!=null && msg != null) {
			writeAuditCreateMessage(smsg, msg, sentfolder, null);
		}
		return retexc;
	}

	public SendException sendMessage(SimpleMessage msg, List<JsAttachment> attachments) {
		UserProfile profile = environment.getProfile();
		String sender = profile.getEmailAddress();
		String name = profile.getDisplayName();
		Identity from = msg.getFrom();
		String replyto = getAccount(from).getReplyTo();
		boolean isOtherIdentity=false;
		
		if (from != null) {
			isOtherIdentity=!from.isMainIdentity();
			sender = from.getEmail();
			name = from.getDisplayName();
			if ( replyto==null|| replyto.trim().length()==0) replyto = sender;
		}
		
		if (name != null && name.length() > 0) {
			sender = name + " <" + sender + ">";
			if (!isOtherIdentity && (replyto!=null && replyto.trim().length()>0 && !replyto.trim().endsWith(">"))) replyto=name + " <" + replyto + ">";
		}
		if (!isOtherIdentity && ( replyto!=null && replyto.trim().length()>0)) msg.setReplyTo(replyto);
		
		SendException retexc = null;
		
		if (attachments.size() == 0 && msg.getAttachments() == null) {
			retexc = sendMsg(sender, msg, null);
		} else {
			
			retexc = sendMsg(sender, msg, attachments);
			
			if (retexc==null || retexc.messageSent) clearCloudAttachments(msg.getId());
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
		
		msg = new MimeMessage(mainAccount.getMailSession());
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
			String refs=references[0];
			//trick to remove trash generated by some software inside references
			// that may let the email be refused by some destinations (e.g. 365)
			if (refs.contains("\\\\\\\\")) {
				refs=refs.replaceAll("(\\\\r|\\\\n|\\\\|\\s)", "");
			}
			msg.setHeader("References", refs);
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
		}
		//set receipt
		if (smsg.getReceipt()) {
			InternetAddress iaFrom = InternetAddressUtils.toInternetAddress(from);
			if (iaFrom != null) msg.setHeader("Disposition-Notification-To", iaFrom.toString());
		}
		
		//see if there are any new attachments for the message
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
			MimeMultipart unrelated = null;
			
			String textcontent = smsg.getTextContent();
      //if is text, or no alternative text is available, add the content as one single body part
			//else create a multipart/alternative with both rich and text mime content
			if (textcontent == null || smsg.getMime().equalsIgnoreCase("text/plain")) {
				MimeBodyPart mbp1 = new MimeBodyPart();
				mbp1.setContent(smsg.getContent(), MailUtils.buildPartContentType(smsg.getMime(), "UTF-8"));
				mp.addBodyPart(mbp1);
			} else {
				MimeMultipart alternative = new MimeMultipart("alternative");
				//the rich part
				MimeBodyPart mbp2 = new MimeBodyPart();
				mbp2.setContent(smsg.getContent(), MailUtils.buildPartContentType(smsg.getMime(), "UTF-8"));
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
				mbp1.setContent(textcontent, MailUtils.buildPartContentType("text/plain", "UTF-8"));
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
						MimeMessage mm = new MimeMessage(mainAccount.getMailSession());
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

					// Normalize file name in case it contains combining diacritical marks
					// as done by MacOS browsers when accented letters are present in the file name.
					// This helps imap servers not supporting these UTF codes.
					if (!Normalizer.isNormalized(contentFileName, Normalizer.Form.NFC)) {
						contentFileName=Normalizer.normalize(contentFileName, Normalizer.Form.NFC);
					}

					
					mbps[e].setFileName(contentFileName);
					String contentType = upfile.getMediaType() + "; name=\"" + contentFileName + "\"";
					mbps[e].setHeader("Content-type", contentType);
					
					// No encoding other than "7bit", "8bit", or "binary" is permitted
					// for the body of a "message/rfc822" entity.
					// Others should be base64 to allow binary attach ot txt files
					// without newline conversions
					if (!mbps[e].isMimeType("message/rfc822"))
						mbps[e].setHeader("Content-Transfer-Encoding", "base64");
					
					if (attach.cid != null && attach.cid.trim().length()>0) {
                                            //Simply check for the cid reference through string match
                                            // to avoid parsing html
                                            if (StringUtils.containsIgnoreCase(smsg.getContent(), "cid:"+attach.cid)) {
						mbps[e].setHeader("Content-ID", "<" + attach.cid + ">");
						mbps[e].setHeader("X-Attachment-Id", attach.cid);
						mbps[e].setDisposition(Part.INLINE);
						if (unrelated==null) unrelated=new MimeMultipart("mixed");
                                            }
					}
				} //end for e

				//add the new attachment parts
				if (unrelated==null) {
					for (int r = 0; r < newAttach; r++)
						mp.addBodyPart(mbps[r]);
				} else {
					//mp becomes the related part with text parts and cids
					MimeMultipart related = mp;
					related.setSubType("related");
					//nest the related part into the mixed part
					mp=unrelated;
					MimeBodyPart mbd=new MimeBodyPart();
					mbd.setContent(related);
					mp.addBodyPart(mbd);
					for (int r = 0; r < newAttach; r++) {
						if (mbps[r].getHeader("Content-ID")!=null) {
							related.addBodyPart(mbps[r]);
						} else {
							mp.addBodyPart(mbps[r]);
						}
					}
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
	
	private Exception saveSent(MailAccount account, Message msg, String sentfolder) {
		Exception retexc = null;
		try {
			account.checkCreateFolder(sentfolder);
			Folder outgoing = account.getFolder(sentfolder);
			msg.setFlag(Flags.Flag.SEEN, true);
			
			Message[] saveMsgs = new MimeMessage[1];
			saveMsgs[0] = msg;
			
			outgoing.appendMessages(saveMsgs);
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
                        retexc=exc;
		}
		
		return retexc;
		
	}
	
	public Exception saveMessage(SimpleMessage msg, List<JsAttachment> attachments, FolderCache fc, boolean auditLog) {
		Exception retexc = null;
		Message newmsg = null;
		
		try {
			newmsg = _saveMessage(msg, attachments, fc);
		} catch (Exception exc) {
			retexc = exc;
			logger.error("Error during saveMessage in "+fc.getFolderName(),exc);
		}
		
		if (mailManager.isAuditEnabled() && auditLog && newmsg != null) writeAuditCreateMessage(msg, newmsg, fc.getFolderName(), null);
		
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
			
			if (mailManager.isAuditEnabled() && m != null) writeAuditCreateMessage(msg, m, fc.getFolderName(), true);
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
		Identity from = msg.getFrom();
		String replyto = getAccount(from).getReplyTo();
		
		if (from != null) {
			sender = from.getEmail();
			name = from.getDisplayName();
			if ( replyto==null|| replyto.trim().length()==0) replyto = sender;
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
		clearCloudAttachments(msg.getId());
		
		return newmsg;
	}
        
        private String getDefaultCharset(Multipart mp) throws MessagingException, IOException {
            String defaultCharset=null;
            //cycle parts to get a possible default charset
            for(int i=0;defaultCharset==null && i<mp.getCount();++i) {
                Part bp=mp.getBodyPart(i);
                if (bp.isMimeType("text/*")) {
                    //Use workaround for NethServer installation:
                    defaultCharset=MailUtils.getCharsetOrNull(bp);
                }
                else if (bp.isMimeType("multipart/*")) {
                    defaultCharset=getDefaultCharset((Multipart)bp.getContent());
                }
            }
            return defaultCharset;
        }
	
	private SimpleMessage getForwardMsg(long id, Message msg, boolean richContent, String fromtitle, String totitle, String cctitle, String datetitle, String subjecttitle, boolean attached, boolean isPEC) {
		Message forward = new MimeMessage(mainAccount.getMailSession());
		if (!attached) {
			try {
				StringBuffer htmlsb = new StringBuffer();
				StringBuffer textsb = new StringBuffer();
                                String defaultCharset=null;
                                if (msg.isMimeType("multipart/*")) defaultCharset=getDefaultCharset((Multipart)msg.getContent());
				boolean isHtml = appendReplyParts(msg, defaultCharset, htmlsb, textsb, null,isPEC);
    //      Service.logger.debug("isHtml="+isHtml);
				//      Service.logger.debug("richContent="+richContent);
				String html = "<HTML><BODY>" + htmlsb.toString() + "</BODY></HTML>";
				if (!richContent) {
                                        //use the original unchanged text content
					forward.setText(getForwardBody(msg, textsb.toString(), SimpleMessage.FORMAT_TEXT, false, fromtitle, totitle, cctitle, datetitle, subjecttitle));
				} else if (!isHtml) {
                                        //use html content, which is text content with possible html encoded characters
					forward.setText(getForwardBody(msg, htmlsb.toString(), SimpleMessage.FORMAT_PREFORMATTED, true, fromtitle, totitle, cctitle, datetitle, subjecttitle));
				} else {
					String newHtml=MailUtils.removeMSWordShit(
						getForwardBody(msg, html, SimpleMessage.FORMAT_HTML, true, fromtitle, totitle, cctitle, datetitle, subjecttitle)
					);
					if (refwSanitizeDownlevelRevealedComments)
						newHtml=MailUtils.sanitizeDownlevelRevealedComments(newHtml);
					
                                        //take care of possible html shit
					forward.setText(newHtml);
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
		
		
			/*
			 * Implements both in-reply-to and references as for reply message
			 */
			setInReplyToAndReferences(msgid,(MimeMessage)msg,(MimeMessage)forward);		
		
		
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
				sb.append("<PRE>");
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
				sb.append("</PRE>");
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
					sb.append("<pre>");
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
					sb.append("</pre>");
//          sb.append("</BLOCKQUOTE>");
				}
			} else {
				//sb.append(getBodyInnerHtml(body));
				sb.append(body);
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
    public Message reply(MailAccount account, MimeMessage orig, boolean replyToAll, boolean fromSent) throws MessagingException {
		MimeMessage reply = new MimeMessage(account.getMailSession());
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
			Session session=account.getMailSession();
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
		setInReplyToAndReferences(msgId,orig,reply);
	//try {
		//    setFlags(answeredFlag, true);
		//} catch (MessagingException mex) {
		//    // ignore it
		//}
		return reply;
	}
	
	private void setInReplyToAndReferences(String msgId, MimeMessage orig, MimeMessage dest) throws MessagingException {
		if (msgId != null) {
			dest.setHeader("In-Reply-To", msgId);
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
			dest.setHeader("References", MimeUtility.fold(12, refs));
		}

	}

	// used above in reply()
	private static final Flags answeredFlag = new Flags(Flags.Flag.ANSWERED);
	
	private SimpleMessage getReplyMsg(int id, MailAccount account, Message msg, boolean replyAll, boolean fromSent, boolean richContent, String myemail, boolean includeOriginal, String fromtitle, String totitle, String cctitle, String datetitle, String subjecttitle, boolean attachMessageParts) {	
		try {
			Message reply=reply(account,(MimeMessage)msg,replyAll,fromSent);
			
			removeDestination(reply, myemail);
			if (ss.getMessageReplyAllStripMyIdentities()) {
				for (Identity ident : mprofile.getIdentities()) {
					removeDestination(reply, ident.getEmail());
				}
			}

			
			//if result is no destination, I may be the only destination
			Address[] origTos=msg.getRecipients(RecipientType.TO);
			Address[] newRcpts=reply.getAllRecipients();
			if ((newRcpts==null || newRcpts.length==0) && origTos!=null) {
				InternetAddress ia0=(InternetAddress) origTos[0];
				InternetAddress newTo=null;
				//If I was the only one keep myself as destination
				if (origTos.length==1) {
					newTo=ia0;
				} else if (origTos.length>1) {
					//if first was me, add second, or keep myself
					if (ia0.getAddress().toLowerCase().equals(myemail)) newTo=(InternetAddress) origTos[1];
					else newTo=ia0;
				}
				if (newTo!=null) {
					reply.addRecipient(RecipientType.TO, newTo);
				}
			}
			
			
      // Setup the message body
			//
			StringBuffer htmlsb = new StringBuffer();
			StringBuffer textsb = new StringBuffer();
			ArrayList<String> attnames = new ArrayList<String>();
			if (includeOriginal) {
                                String defaultCharset=null;
                                if (msg.isMimeType("multipart/*")) defaultCharset=getDefaultCharset((Multipart)msg.getContent());
				boolean isHtml = appendReplyParts(msg, defaultCharset, htmlsb, textsb, attnames, attachMessageParts);
				String html = "<HTML><BODY>" + htmlsb.toString() + "</BODY></HTML>";
				String text = null;
				if (!richContent) {
					text = getReplyBody(msg, textsb.toString(), SimpleMessage.FORMAT_TEXT, false, fromtitle, totitle, cctitle, datetitle, subjecttitle, attnames);
				} else if (!isHtml) {
					text = getReplyBody(msg, htmlsb.toString(), SimpleMessage.FORMAT_PREFORMATTED, true, fromtitle, totitle, cctitle, datetitle, subjecttitle, attnames);
				} else {
					text = getReplyBody(msg, html, SimpleMessage.FORMAT_HTML, true, fromtitle, totitle, cctitle, datetitle, subjecttitle, attnames);
				}
				String newHtml=MailUtils.removeMSWordShit(text);
				
				if (refwSanitizeDownlevelRevealedComments)
					newHtml=MailUtils.sanitizeDownlevelRevealedComments(newHtml);

				reply.setText(newHtml);
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
	
	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
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
				sb.append("<PRE>");
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
				sb.append("</PRE>");
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
					//sb.append("<BLOCKQUOTE style='BORDER-LEFT: #000080 2px solid; MARGIN-LEFT: 5px; PADDING-LEFT: 5px'>");
					sb.append("<pre>");
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
					sb.append("</pre>");
					//sb.append("</BLOCKQUOTE>");
				}
			} else {
				/*
				//String ubody = body.toUpperCase();
				while (true) {
					int ix1 = StringUtils.indexOfIgnoreCase(body,"<BODY");
					if (ix1 < 0) {
						break;
					}
					int ix2 = StringUtils.indexOfIgnoreCase(body,">", ix1 + 1);
					if (ix2 < 0) {
						ix2 = ix1 + 4;
					}
					int ix3 = StringUtils.indexOfIgnoreCase(body,"</BODY", ix2 + 1);
					if (ix3 > 0) {
						body = body.substring(ix2 + 1, ix3);
					} else {
						body = body.substring(ix2 + 1);
					}
				}
				//body=removeStartEndTag(body,unwantedTags);
				*/
				
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
	
	private String getTextContentAsString(Part p, String defaultCharset) throws IOException, MessagingException {
                String charset=null;
                if (defaultCharset==null)
                    //Use workaround for NethServer installation:
                    charset=MailUtils.getCharsetOrDefault(p);
                else {
                    //Use workaround for NethServer installation:
                    charset=MailUtils.getCharsetOrNull(p);
                    if (charset==null) charset=defaultCharset;
                }            
            
		java.io.InputStream istream = null;
		if (!java.nio.charset.Charset.isSupported(charset)) {
			charset = "ISO-8859-1";
		}
		try {
			if (p instanceof jakarta.mail.internet.MimeMessage) {
				jakarta.mail.internet.MimeMessage mm = (jakarta.mail.internet.MimeMessage) p;
				istream = mm.getInputStream();
			} else if (p instanceof jakarta.mail.internet.MimeBodyPart) {
				jakarta.mail.internet.MimeBodyPart mm = (jakarta.mail.internet.MimeBodyPart) p;
				istream = mm.getInputStream();
			}
		} catch (Exception exc) { //unhandled format, get Raw data
			if (p instanceof jakarta.mail.internet.MimeMessage) {
				jakarta.mail.internet.MimeMessage mm = (jakarta.mail.internet.MimeMessage) p;
				istream = mm.getRawInputStream();
			} else if (p instanceof jakarta.mail.internet.MimeBodyPart) {
				jakarta.mail.internet.MimeBodyPart mm = (jakarta.mail.internet.MimeBodyPart) p;
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
	
	private boolean appendReplyParts(Part p, String defaultCharset, StringBuffer htmlsb, StringBuffer textsb, ArrayList<String> attnames, boolean attachMessageParts) throws MessagingException,
			IOException {
            
		boolean isHtml = false;
		String disp = p.getDisposition();
		if (disp!=null && disp.equalsIgnoreCase(Part.ATTACHMENT) && !p.isMimeType("multipart/*") && !p.isMimeType("message/*")) {
			if (attnames != null) {
				String id[] = p.getHeader("Content-ID");
				if (id==null || id[0]==null) {
					String filename = p.getFileName();
					if (filename != null) {
						if (filename.startsWith("<")) {
							filename = filename.substring(1);
						}
						if (filename.endsWith(">")) {
							filename = filename.substring(0, filename.length() - 1);
						}
					}
					if (filename != null) {
						attnames.add(filename);
					}
				}
			}
			return false;
		}
		if (p.isMimeType("text/html")) {
			//String htmlcontent=(String)p.getContent();
			String htmlcontent = getTextContentAsString(p,defaultCharset);
			textsb.append(MailUtils.htmlToText(MailUtils.htmlunescapesource(htmlcontent)));
			htmlsb.append(MailUtils.htmlescapefixsource(/*getBodyInnerHtml(*/htmlcontent/*)*/));
			isHtml = true;
		} else if (p.isMimeType("text/plain")) {
			String content = getTextContentAsString(p,defaultCharset);
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
					isHtml = appendReplyParts(part, defaultCharset, htmlsb, textsb, attnames, attachMessageParts);
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
				isHtml = appendReplyParts(bestPart, defaultCharset, htmlsb, textsb, attnames, attachMessageParts);
			}
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); ++i) {
				if (appendReplyParts(mp.getBodyPart(i), defaultCharset, htmlsb, textsb, attnames, attachMessageParts)) {
					isHtml = true;
				}
			}
		} else if (p.isMimeType("message/*") && !attachMessageParts) {
			Object content = p.getContent();
			if (appendReplyParts((MimeMessage) content, defaultCharset, htmlsb, textsb, attnames, attachMessageParts)) {
				isHtml = true;
			}
		} else {
		}
		textsb.append('\n');
		textsb.append('\n');
		
		return isHtml;
	}
	
	public void cleanup() {
		if (mft != null) {
			mft.abort();
			mft=null;
		}
		if (ast != null && ast.isRunning()) {
			ast.cancel();
			ast=null;
		}
		
		for(MailAccount account: accounts.values()) {
			account.cleanup();
		}
		fcProvided=null;
		logger.trace("exiting cleanup");
	}
	
	protected void clearAllCloudAttachments() {
		msgcloudattach.clear();
	}

	protected String getMessageID(Message m) throws MessagingException {
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
	
	public boolean hasDmsDocumentArchiving() {
		return RunContext.isPermitted(true, SERVICE_ID, "DMS_DOCUMENT_ARCHIVING");
	}
	
	public String getDmsSimpleArchivingMailFolder() {
		return us.getSimpleDMSArchivingMailFolder();
	}
	
	public boolean isDmsSimpleArchiving() {
		return us.getDMSMethod().equals(MailSettings.ARCHIVING_DMS_METHOD_SIMPLE);
	}

	public boolean isDmsStructuredArchiving() {
		return us.getDMSMethod().equals(MailSettings.ARCHIVING_DMS_METHOD_STRUCTURED);
	}
	
	public boolean isDmsWebtopArchiving() {
		return us.getDMSMethod().equals(MailSettings.ARCHIVING_DMS_METHOD_WEBTOP);
	}
	
	public boolean isDmsFolder(MailAccount account, String foldername) {
		if (!hasDmsDocumentArchiving()) {
			return false;
		}
		boolean b = false;
		String df = us.getSimpleDMSArchivingMailFolder();
		if (df != null && df.trim().length() > 0) {
			String lfn = account.getLastFolderName(foldername);
			String dfn = account.getLastFolderName(df);
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
		} else if (fc.isArchive()) {
			desc = lookupResource(MailLocaleKey.FOLDERS_ARCHIVE);
		} else if (fc.isSent()) {
			desc = lookupResource(MailLocaleKey.FOLDERS_SENT);
		} else if (fc.isSpam()) {
			desc = lookupResource(MailLocaleKey.FOLDERS_SPAM);
		}
		return desc;
	}
	
	public String getInternationalFolderPath(FolderCache fc) throws MessagingException {
		String intpath = getInternationalFolderName(fc);
		char sep = fc.getAccount().getFolderSeparator();
		FolderCache parent = fc.getParent();
		while (parent != null && parent.isRoot()) {
			String intparent = getInternationalFolderName(parent);
			intpath = intparent + sep + intpath;
			parent = parent.getParent();
		}
		return intpath;
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
	
	public ArrayList<CloudAttachment> getCloudAttachments(long msgid) {
		ArrayList<CloudAttachment> attachments = msgcloudattach.get(new Long(msgid));
		if (attachments != null) {
			return attachments;
		}
		return emptyAttachments;
	}
	
	public CloudAttachment getCloudAttachment(long msgid, String filename) {
		ArrayList<CloudAttachment> attachments = getCloudAttachments(msgid);
		for (CloudAttachment att : attachments) {
			if (att.getName().equals(filename)) {
				return att;
			}
		}
		return null;
	}
	
	public void clearCloudAttachments(long msgid) {
		msgcloudattach.remove(msgid);
	}
	
	public void deleteCloudAttachments(long msgid) {
		ArrayList<CloudAttachment> attachments=getCloudAttachments(msgid);
		for(CloudAttachment a: attachments) {
			try {
				vfsmanager.deleteStoreFile(a.getStoreId(), a.getPath());
			} catch(Exception exc) {
				exc.printStackTrace();
			}
		}
		clearCloudAttachments(msgid);
	}
	
	public void putCloudAttachments(long msgid, ArrayList<CloudAttachment> attachments) {
		msgcloudattach.put(new Long(msgid), attachments);
	}
	
	public CloudAttachment attachCloud(long msgid, int storeId, String path, String name) {
		CloudAttachment attachment = null;
		ArrayList<CloudAttachment> attachments = getCloudAttachments(msgid);
		if (attachments == null || attachments == emptyAttachments) {
			attachments = new ArrayList<CloudAttachment>();
			putCloudAttachments(msgid, attachments);
		}
		attachment = new CloudAttachment(storeId,path,name);
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
	
	private ArrayList<FolderCache> opened = new ArrayList<FolderCache>();

	private static final int FOLDER_CACHE_POOL_SIZE=5; //default 5
	
	protected void poolOpened(FolderCache fc) {
		
		if (opened.size() >= FOLDER_CACHE_POOL_SIZE) {
			FolderCache rfc = opened.remove(0);
			rfc.cleanup(false);
			rfc.close();
			rfc.setForceRefresh();
		}
		opened.add(fc);
	}
	
	private MailAccount getAccount(HttpServletRequest request) {
		String account=request.getParameter("account");
		if (account!=null) return accounts.get(account);
		return mainAccount;
	}
	
	private MailAccount getAccount(String account) {
		return accounts.get(account);
	}

	
	//Client service requests
	public void processGetImapTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		String pfoldername = request.getParameter("node");
		MailAccount account=getAccount(request);
		Folder folder = null;
		try {
			boolean connected=account.checkStoreConnected();
			if (!connected) throw new Exception("Mail account authentication error");
			
			boolean isroot=pfoldername.equals("/");
            if (isroot) folder=account.getDefaultFolder();
			else folder = account.getFolder(pfoldername);
			
			Folder folders[] = folder.list();
			String fprefix = account.getFolderPrefix();
			boolean level1 = (fprefix != null /*&& fprefix.equals("INBOX.")*/ && fprefix.trim().length()>0);
			ArrayList<JsFolder> jsFolders=new ArrayList<>();
			//out.print("{ data:[");
			if (isroot && account.hasDifferentDefaultFolder()) {
				Folder fcs[]=new Folder[0];
				//check for other shared folders to be added
				Folder rfolders[]=account.getRealDefaultFolder().list();
				ArrayList<Folder> afcs=new ArrayList<Folder>();
				for(String sharedPrefix: account.getSharedPrefixes()) {
					for(Folder rfolder: rfolders) {
						if (rfolder.getFullName().equals(sharedPrefix))
							afcs.add(rfolder);
					}
				}
				//don't mind about just the Shared folder with no child (size=1)
				if (afcs.size()>0) fcs=afcs.toArray(fcs);
				
				Folder xfolders[]=new Folder[1+folders.length+fcs.length];
				xfolders[0]=folder;
				System.arraycopy(folders, 0, xfolders, 1, folders.length);
				if (fcs.length>0) System.arraycopy(fcs,0,xfolders,1+folders.length,fcs.length);
				folders=xfolders;
			}
			outputFolders(account, folder, folders, level1, false, jsFolders);
			new JsonResult("data", jsFolders).printTo(out,false);
			//out.println("], message: '' }");
			
		} catch (Exception exc) {
			new JsonResult(exc).printTo(out);
			Service.logger.error("Exception",exc);
		}
	}
	
	private void outputFolders(MailAccount account, Folder parent, Folder folders[], boolean level1, boolean favorites, ArrayList<JsFolder> jsFolders) throws Exception {
		boolean hasPrefix=!StringUtils.isBlank(account.getFolderPrefix());
		String prefixMatch=StringUtils.stripEnd(account.getFolderPrefix(),account.getFolderSeparator()+"");
		ArrayList<Folder> postPrefixList=new ArrayList<Folder>();
		ArrayList<Folder> afolders;
		if (!favorites) afolders=sortFolders(account,folders);
		else {
			afolders=new ArrayList<Folder>();
			for(Folder f: folders) {
				if(f != null) {
					afolders.add(f);
				}
			}
		}
		//If Shared Folders, sort on description
		if (parent!=null && account.isSharedFolder(parent.getFullName())) {
			if (!account.hasDifferentDefaultFolder() || !account.isDefaultFolder(parent.getFullName())) {
				String ss = mprofile.getSharedSort();
				if (ss.equals("D")) {
					Collections.sort(afolders, account.getDescriptionFolderComparator());
				} else if (ss.equals("U")) {
					Collections.sort(afolders, account.getWebTopUserFolderComparator());
				}
			}
		}
		
		//If at level 1, look for the prefix folder in the list
		Folder prefixFolder=null;
		if (level1) {
			for(Folder f : afolders ) {
				if (f.getFullName().equals(prefixMatch)) {
					prefixFolder=f;
					break;
				}
			}
			//remove it and use it later
			if (prefixFolder!=null) afolders.remove(prefixFolder);
		}
		
		//now scan and output folders
		for (Folder f : afolders) {
			String foldername = f.getFullName();
			//in case of moved root, check not to duplicate root elsewhere
			if (account.hasDifferentDefaultFolder()) {
				if (account.isDovecot()) {
					if (account.isDefaultFolder(foldername)) continue;
				} else {
					//skip default folder under shared
					if (account.isDefaultFolder(foldername) && parent!=null && !parent.getFullName().equals(foldername)) continue;
				}
			}
			
			//skip hidden
			if (us.isFolderHidden(foldername)) continue;

			
			FolderCache mc = account.getFolderCache(foldername);
			if (mc == null && parent!=null) {
				//continue;
				//System.out.println("foldername="+foldername+" parentname="+parent.getFullName());
				FolderCache fcparent=account.getFolderCache(parent.getFullName());
				mc=account.addSingleFoldersCache(fcparent, f);
			}
			//String shortfoldername=getShortFolderName(foldername);
			IMAPFolder imapf = (IMAPFolder) f;
			String atts[] = imapf.getAttributes();
			boolean leaf = true;
			boolean noinferiors = false;
			if (account.hasDifferentDefaultFolder() && account.isDefaultFolder(foldername)) {
				
			}
			else if (!favorites) {
				for(String att: atts) {
					if (att.equals("\\HasChildren")) {
						if (!level1 || !foldername.equals(account.getInboxFolderFullName())) leaf=false;
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
			
			if (!favorites && prefixFolder!=null && !foldername.equals("INBOX") && !foldername.startsWith(account.getFolderPrefix())) {
				postPrefixList.add(f);
			}
			else {
/*				
				String iconCls = "wtmail-icon-imapfolder";
				int unread = 0;
				boolean hasUnread = false;
				boolean nounread = false;
				if (mc.isSharedFolder()) {
					iconCls = "wtmail-icon-sharefolder";
					nounread = true;
				} else if (mc.isInbox()) {
					iconCls = "wtmail-icon-inboxfolder";
				} else if (mc.isSent()) {
					iconCls = "wtmail-icon-sentfolder";
					nounread = true;
				} else if (mc.isDrafts()) {
					iconCls = "wtmail-icon-draftsfolder";
					nounread = true;
				} else if (mc.isTrash()) {
					iconCls = "wtmail-icon-trashfolder";
					nounread = true;
				} else if (mc.isArchive()) {
					iconCls = "wtmail-icon-archivefolder";
					nounread = true;
				} else if (mc.isSpam()) {
					iconCls = "wtmail-icon-junkfolder";
					nounread = true;
				} else if (mc.isDms()) {
					iconCls = "wtmail-icon-dmsfolder";
				} else if (mc.isSharedInbox()) {
					iconCls = "wtmail-icon-inboxfolder";
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
						+ "',text:'" + StringEscapeUtils.escapeEcmaScript(description)
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
				if (account.isUnderSharedFolder(foldername)) ss+=",isUnderShared: true";
				if (mc.isInbox()) {
					ss += ",isInbox: true";
				}
				if (mc.isSent()) {
					ss += ",isSent: true";
				}
				if (account.isUnderSentFolder(mc.getFolderName())) {
					ss += ",isUnderSent: true";
				}
				if (mc.isDrafts()) {
					ss += ",isDrafts: true";
				}
				if (mc.isTrash()) {
					ss += ",isTrash: true";
				}
				if (mc.isArchive()) {
					ss += ",isArchive: true";
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

				boolean canRename=true;
				if (mc.isInbox() || mc.isSpecial() || mc.isSharedFolder() || (mc.getParent()!=null && mc.getParent().isSharedFolder())) canRename=false;
				ss += ", canRename: "+canRename;

				ss += ", account: '"+account.getId()+"'";
				ss += "},";

				out.print(ss);
*/
				jsFolders.add(createJsFolder(mc,leaf));
			}
		}
		
		//if we have a prefix folder output remaining folders
		if (!favorites && prefixFolder!=null) {
			for(Folder ff: prefixFolder.list()) postPrefixList.add(ff);					
			ArrayList<Folder> sortedFolders=sortFolders(account,postPrefixList.toArray(new Folder[postPrefixList.size()]));
			outputFolders(account, prefixFolder, sortedFolders.toArray(new Folder[sortedFolders.size()]), false, false, jsFolders);
		}
	}
	
	private JsFolder createJsFolder(FolderCache fc, boolean leaf) {
		String foldername=fc.getFolderName();
		MailAccount account=fc.getAccount();
		String iconCls = "wtmail-icon-imapfolder";
		int unread = 0;
		boolean hasUnread = false;
		boolean nounread = false;
		if (fc.isSharedFolder()) {
			iconCls = "wtmail-icon-sharefolder";
			nounread = true;
		} else if (fc.isInbox()) {
			iconCls = "wtmail-icon-inboxfolder";
		} else if (fc.isSent()) {
			iconCls = "wtmail-icon-sentfolder";
			//nounread = true;
		} else if (fc.isDrafts()) {
			iconCls = "wtmail-icon-draftsfolder";
			//nounread = true;
		} else if (fc.isTrash()) {
			iconCls = "wtmail-icon-trashfolder";
			//nounread = true;
		} else if (fc.isArchive()) {
			iconCls = "wtmail-icon-archivefolder";
			//nounread = true;
		} else if (fc.isSpam()) {
			iconCls = "wtmail-icon-junkfolder";
			//nounread = true;
		} else if (fc.isDms()) {
			iconCls = "wtmail-icon-dmsfolder";
		} else if (fc.isSharedInbox()) {
			iconCls = "wtmail-icon-inboxfolder";
		} 
		if (!nounread) {
			unread = fc.getUnreadMessagesCount();
			hasUnread = fc.hasUnreadChildren();
		}
		String text = fc.getDescription();
		String group = us.getMessageListGroup(foldername);
		if (group == null) {
			group = "";
		}
		
		JsFolder jsFolder=new JsFolder();
		jsFolder.id=foldername;
		jsFolder.leaf=leaf;
		jsFolder.iconCls=iconCls;
		jsFolder.unread=unread;
		jsFolder.hasUnread=hasUnread;
		jsFolder.group=group;

		boolean isSharedToSomeone=false;
		try {
			isSharedToSomeone=fc.isSharedToSomeone();
		} catch(Exception exc) {

		}
		
		IMAPFolder imapf=(IMAPFolder)fc.getFolder();
		try {
			ACL[] acls = imapf.getACL();
			List<ACL> aclList = Arrays.asList(acls);

			boolean canWrite = aclList.stream().map(acl -> acl.getRights()).anyMatch(right -> right.contains(Rights.Right.WRITE));
			jsFolder.isReadOnly=!canWrite;
		} catch(MessagingException messagingException) {
			//System.err.println("Error getting ACLs: "+imapf.getFullName());
			//messagingException.printStackTrace();
			jsFolder.isReadOnly=true;
		}
		if (isSharedToSomeone) jsFolder.isSharedToSomeone=true;
		if (fc.isSharedFolder()) jsFolder.isSharedRoot=true;
		if (account.isUnderSharedFolder(foldername)) {
			jsFolder.isUnderShared=true;
			if(fc.getParent() != null && fc.getParent().isSharedFolder())
				jsFolder.canRename = true;
		}
		
		jsFolder.text=text;
		jsFolder.folder=text;
		
		if (fc.isInbox()) jsFolder.isInbox=true;
		if (fc.isSent()) jsFolder.isSent=true;
		if (account.isUnderSentFolder(fc.getFolderName())) jsFolder.isUnderSent=true;
		if (fc.isDrafts()) jsFolder.isDrafts=true;
		if (fc.isTrash()) jsFolder.isTrash=true;
		if (fc.isArchive()) jsFolder.isArchive=true;
		if (fc.isSpam()) jsFolder.isSpam=true;

		if (fc.isScanForcedOff()) jsFolder.scanOff=true;
		else if (fc.isScanForcedOn()) jsFolder.scanOn=true;
		else if (fc.isScanEnabled()) jsFolder.scanEnabled=true;
		else if(account.isFavoriteFolder(foldername)) {
			jsFolder.scanEnabled = false;
			jsFolder.scanOn = true;
		}

		boolean canRename=true;
		//check both isShared and not underShared because of the different structure on NethServer,
		//so rename is accessible to shared root, to customize name
		if (fc.isInbox() || fc.isSpecial() || (!fc.isUnderSharedFolder() && fc.isSharedFolder())) canRename=false;
		jsFolder.canRename=canRename;
		
		if (fc.isPEC()) jsFolder.isPEC = true;

		jsFolder.account=account.getId();
		 
		return jsFolder;
	}
	
	protected ArrayList<Folder> sortFolders(MailAccount account, Folder folders[]) {
		ArrayList<Folder> afolders = new ArrayList<Folder>();
		ArrayList<Folder> sfolders=new ArrayList<Folder>();
		HashMap<String,Folder> mfolders=new HashMap<String,Folder>();
		
		if (account.isCyrus()) {
			/* Hack for Cyrus bug :
			 *  - when there are two subfolders with same initial name and second one longer
			 *    continuing with space/dash etc (e.g "Test" and "Test 2"), first one is
			 *    listed twice, with first instance always "\HasNoChildren"
			 *  - in this case code is misleaded showing only first instance with no children
			 *    even if second instance actually has children.
			 *
			 *  Detect this situation and get rid of first instance, keeping only last one.
			 */
			HashMap<String, Integer> hackMap=new HashMap<String,Integer>();
			ArrayList<Folder> hackFolders=new ArrayList<>();
			boolean bugfound=false;
			for(Folder f: folders) {
				String name=f.getName();
				Integer ix=hackMap.get(name);
				if (ix==null) {
					ix=hackFolders.size();
					hackFolders.add(f);
					hackMap.put(name, ix);
				} else {
					hackFolders.set(ix, f);
					bugfound=true;
				}
			}
			if (bugfound) folders=hackFolders.toArray(new Folder[] {});
			
		}
		
		//add all non special fo the array and map special ones for later insert
		Folder inbox = null;
		Folder sent = null;
		Folder drafts = null;
		Folder trash = null;
		Folder archive = null;
		Folder spam = null;
		for (Folder f : folders) {
			String foldername = f.getFullName();
			String shortfoldername = account.getShortFolderName(foldername);
			if (!mfolders.containsKey(shortfoldername)) {
				mfolders.put(shortfoldername, f);
				if (account.isInboxFolder(shortfoldername)) inbox=f;
				else if (account.isSentFolder(shortfoldername)) sent=f;
				else if (account.isDraftsFolder(shortfoldername)) drafts=f;
				else if (account.isTrashFolder(shortfoldername)) trash=f;
				else if (account.isSpamFolder(shortfoldername)) spam=f;
				else if (account.isArchiveFolder(shortfoldername)) archive=f;
				else if (account.isSharedFolder(foldername)) sfolders.add(f);
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
		if (archive != null) {
			afolders.add(0, archive);
		}
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
	
	public void processShowArchive(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			boolean connected=archiveAccount.checkStoreConnected();
			if (!connected) throw new Exception("Mail account authentication error");
			if (!archiveAccount.hasFolderCache()) archiveAccount.loadFoldersCache(archiveAccount,false);
			new JsonResult().printTo(out);
		} catch (Exception exc) {
			new JsonResult(exc).printTo(out);
			Service.logger.error("Exception",exc);
		}
	}
	
	public void processHideArchive(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		archiveAccount.disconnect();
		new JsonResult().printTo(out);
	}
	
	public void processGetArchiveTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String pfoldername = request.getParameter("node");
		MailAccount account=archiveAccount; //getAccount(request);
		Folder folder = null;
		try {
			boolean connected=account.checkStoreConnected();
			if (!connected) throw new Exception("Mail account authentication error");
			if (!account.hasFolderCache()) account.loadFoldersCache(account,true);
			
			boolean isroot=pfoldername.equals("/");
            if (isroot) folder=account.getDefaultFolder();
			else folder = account.getFolder(pfoldername);
			
			Folder folders[] = folder.list();
			String fprefix = account.getFolderPrefix();
			boolean level1 = (fprefix != null && fprefix.equals("INBOX."));
			ArrayList<JsFolder> jsFolders=new ArrayList<>();
			//out.print("{ data:[");
			if (isroot && account.hasDifferentDefaultFolder()) {
				Folder fcs[]=new Folder[0];
				//check for other shared folders to be added
				Folder rfolders[]=account.getRealDefaultFolder().list();
				ArrayList<Folder> afcs=new ArrayList<Folder>();
				for(String sharedPrefix: account.getSharedPrefixes()) {
					for(Folder rfolder: rfolders) {
						if (rfolder.getFullName().equals(sharedPrefix))
							afcs.add(rfolder);
					}
				}
				if (afcs.size()>0) fcs=afcs.toArray(fcs);
				
				Folder xfolders[]=new Folder[1+folders.length+fcs.length];
				xfolders[0]=folder;
				System.arraycopy(folders, 0, xfolders, 1, folders.length);
				if (fcs.length>0) System.arraycopy(fcs,0,xfolders,1+folders.length,fcs.length);
				folders=xfolders;
			}
			outputFolders(account, folder, folders, level1, false, jsFolders);
			new JsonResult("data", jsFolders).printTo(out,false);
			//out.println("], message: '' }");
			
			
		} catch (Exception exc) {
			new JsonResult(exc).printTo(out);
			Service.logger.error("Exception",exc);
		}
	}
	
	class FavoriteFolderData {
		FolderCache folderCache;
		String description;
		
		FavoriteFolderData(FolderCache fc, String desc) {
			this.folderCache=fc;
			this.description=desc;
		}
	}
	
	private boolean getFavoritesTreeDone=false;
	
	public void processGetFavoritesTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		//first call runs external checks
		if (!getFavoritesTreeDone) {
			for(MailAccount extacc: externalAccounts) {
				extacc.getFoldersThread().start();
			}
			getFavoritesTreeDone=true;
		}
		
		try {
			//synchronized(mft) {
				ArrayList<JsFolder> jsFolders=new ArrayList<>();
				//out.print("{ data:[");

				MailUserSettings.FavoriteFolders favorites;
				boolean removeOldSetting=false;
				//use new setting if exists
				if (us.hasFavoriteFolders())
					favorites = us.getFavoriteFolders();
				//convert old setting
				else {
					MailUserSettings.Favorites off=us.getFavorites();
					favorites = new MailUserSettings.FavoriteFolders();
					for(String foldername: off) {
						favorites.add(new MailUserSettings.FavoriteFolder(MAIN_ACCOUNT_ID, foldername, foldername));
					}
					removeOldSetting=true;
				}
				MailUserSettings.FavoriteFolders newFavorites = new MailUserSettings.FavoriteFolders();
				ArrayList<FavoriteFolderData> ffds = new ArrayList<>();
				for( int i = 0; i < favorites.size(); ++i) {
					MailUserSettings.FavoriteFolder ff=favorites.get(i);
					MailAccount account=getAccount(ff.accountId);
					if (account!=null) {
						Folder folder=account.getFolder(ff.folderId);
						if (folder.exists()) {
							FolderCache fc=account.getFolderCache(ff.folderId);
							if (fc==null) {
								fc=account.createFolderCache(folder,true);
							}
							newFavorites.add(ff);
							ffds.add(new FavoriteFolderData(fc,ff.description));
						}
					}
				}
				us.setFavoriteFolders(newFavorites);
				//remove old setting if present
				if (removeOldSetting) {
					us.deleteOldFavoritesSetting();
				}

				for(FavoriteFolderData ffd: ffds) {
					JsFolder jsFolder=createJsFolder(ffd.folderCache, true);
					jsFolder.folder=ffd.description;
					jsFolder.hasUnread=false;
					jsFolders.add(jsFolder);
				}
				//outputFolders(mainAccount, mainAccount.getDefaultFolder(), folders, false, true, jsFolders);
				new JsonResult("data", jsFolders).printTo(out,false);
				//out.println("], message: '' }");
			//}
			
		} catch (Exception exc) {
			Service.logger.error("Exception", exc);
			new JsonResult(exc).printTo(out);
		}
	}
	
	public void processAddToFavorites(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String account = request.getParameter("account");
		String folder = request.getParameter("folder");
		String description = request.getParameter("description");
		
		MailUserSettings.FavoriteFolders favorites=us.getFavoriteFolders();
		favorites.add(account,folder,description);
		us.setFavoriteFolders(favorites);
		new JsonResult().printTo(out);
	}	
	
	public void processRemoveFavorite(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String account = request.getParameter("account");
		String folder = request.getParameter("folder");
		MailUserSettings.FavoriteFolders favorites=us.getFavoriteFolders();
		favorites.remove(account,folder);
		us.setFavoriteFolders(favorites);
		new JsonResult().printTo(out);
	}	
	
	
	public void processMoveMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount fromaccount=getAccount(request.getParameter("fromaccount"));
		String fromfolder = request.getParameter("fromfolder");
		MailAccount toaccount=getAccount(request.getParameter("toaccount"));
		String tofolder = request.getParameter("tofolder");
		String allfiltered = request.getParameter("allfiltered");
		String smultifolder = request.getParameter("multifolder");
		boolean multifolder = smultifolder != null && smultifolder.equals("true");
		String sfullthreads = request.getParameter("fullthreads");
		boolean fullthreads = sfullthreads != null && sfullthreads.equals("true");
		String sisdd = request.getParameter("isdd");
		boolean isdd = sisdd != null && sisdd.equals("true");
		String uids[] = null;
		
		boolean archiving = false;
		try {
			fromaccount.checkStoreConnected();
			toaccount.checkStoreConnected();
			FolderCache mcache = fromaccount.getFolderCache(fromfolder);
			FolderCache tomcache = toaccount.getFolderCache(tofolder);
			String foldertrash=toaccount.getFolderTrash();
			String folderspam=toaccount.getFolderSpam();
			String folderarchive=toaccount.getFolderArchive();
			JsOperateMessage message;
			
			//Try to make decisions on destination folders
			// only if it's not a direct drag&drop move
			if (!isdd) {
				//check if tofolder is my Spam, and there is spamadm, move there
				if (toaccount.isSpamFolder(tofolder)) {
					String spamadmSpam=ss.getSpamadmSpam();
					if (spamadmSpam!=null) {
						folderspam=spamadmSpam;
						FolderCache fc=toaccount.getFolderCache(spamadmSpam);
						if (fc!=null) tomcache=fc;
					}
					else if (toaccount.isUnderSharedFolder(fromfolder)) {
						String mainfolder=toaccount.getMainSharedFolder(fromfolder);
						if (mainfolder!=null) {
							folderspam = mainfolder + toaccount.getFolderSeparator() + toaccount.getLastFolderName(folderspam);
							FolderCache fc=toaccount.getFolderCache(folderspam);
							if (fc!=null) tomcache=fc;
						}
					}
					tofolder=folderspam;
				}
				//if trashing, check for shared profile trash
				else if (toaccount.isTrashFolder(tofolder)) {
					if (toaccount.isUnderSharedFolder(fromfolder)) {
						String mainfolder=toaccount.getMainSharedFolder(fromfolder);
						if (mainfolder!=null) {
							foldertrash = mainfolder + toaccount.getFolderSeparator() + toaccount.getLastFolderName(foldertrash);
							FolderCache fc=toaccount.getFolderCache(foldertrash);
							if (fc!=null) tomcache=fc;
						}
					}
					tofolder=foldertrash;
				}
				//if archiving, determine destination folder based on settings and shared profile
				else if (toaccount.isArchiveFolder(tofolder)) {
						if (toaccount.isUnderSharedFolder(fromfolder)) {
							String mainfolder=toaccount.getMainSharedFolder(fromfolder);
						if (mainfolder!=null) {
							folderarchive = mainfolder + toaccount.getFolderSeparator() + toaccount.getLastFolderName(folderarchive);
						}
					}
					tofolder=folderarchive;
					archiving=true;
				}
			}
			
			if (allfiltered == null) {
				uids = request.getParameterValues("ids");
				boolean norows=false;
				if (uids.length==1 && uids[0].length()==0) norows=true;
				if (!norows) {
					if (!multifolder) {
						if (archiving) archiveMessages(mcache, folderarchive, toLongs(uids), fullthreads);
						else moveMessages(mcache, tomcache, toLongs(uids), fullthreads);
					}
					else {
						long iuids[]=new long[1];
						for(String uid: uids) {
							int ix=uid.indexOf("|");
							fromfolder=uid.substring(0,ix);
							uid=uid.substring(ix+1);
							mcache = fromaccount.getFolderCache(fromfolder);
							iuids[0]=Long.parseLong(uid);
							if (archiving) archiveMessages(mcache, folderarchive, iuids,fullthreads);
							else moveMessages(mcache, tomcache, iuids,fullthreads);
						}
					}
				}
				long millis = System.currentTimeMillis();
				message = new JsOperateMessage(millis, tofolder, archiving);
			} else {
                uids=getMessageUIDs(mcache,request);
                if (archiving) archiveMessages(mcache, folderarchive, toLongs(uids),fullthreads);
				else moveMessages(mcache, tomcache, toLongs(uids),fullthreads);
				mcache.setForceRefresh();
				long millis = System.currentTimeMillis();
				message = new JsOperateMessage(tomcache.getUnreadMessagesCount(), millis, tofolder, archiving);
			}
			new JsonResult(message).printTo(out, false);
			tomcache.refreshUnreads();
		} catch(Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processCopyMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount fromaccount=getAccount(request.getParameter("fromaccount"));
		String fromfolder = request.getParameter("fromfolder");
		MailAccount toaccount=getAccount(request.getParameter("toaccount"));
		String tofolder = request.getParameter("tofolder");
		String allfiltered = request.getParameter("allfiltered");
		String smultifolder = request.getParameter("multifolder");
		boolean multifolder = smultifolder != null && smultifolder.equals("true");
		String sfullthreads = request.getParameter("fullthreads");
		boolean fullthreads = sfullthreads != null && sfullthreads.equals("true");
		String uids[] = null;
		
		try {
			fromaccount.checkStoreConnected();
			toaccount.checkStoreConnected();
			FolderCache mcache = fromaccount.getFolderCache(fromfolder);
			FolderCache tomcache = toaccount.getFolderCache(tofolder);
			JsOperateMessage message;
			
			if (allfiltered == null) {
				uids = request.getParameterValues("ids");
				if (!multifolder) copyMessages(mcache, tomcache, toLongs(uids),fullthreads);
				else {
                    long iuids[]=new long[1];
                    for(String uid: uids) {
                        int ix=uid.indexOf("|");
                        fromfolder=uid.substring(0,ix);
                        uid=uid.substring(ix+1);
						mcache = fromaccount.getFolderCache(fromfolder);
                        iuids[0]=Long.parseLong(uid);
                        copyMessages(mcache, tomcache, iuids,fullthreads);
					}
				}
				long millis = System.currentTimeMillis();
				message = new JsOperateMessage(millis);
			} else {
                uids=getMessageUIDs(mcache,request);
                copyMessages(mcache, tomcache, toLongs(uids),fullthreads);
				tomcache.refreshUnreads();
				mcache.setForceRefresh();
				long millis = System.currentTimeMillis();
				message = new JsOperateMessage(tomcache.getUnreadMessagesCount(), millis);
			}
			new JsonResult(message).printTo(out);
        } catch(Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processDmsArchiveMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String fromfolder = request.getParameter("fromfolder");
		String tofolder = request.getParameter("tofolder");
		int ix = tofolder.indexOf("|");
		String idcategory = tofolder.substring(0, ix);
		String idsubcategory = tofolder.substring(ix + 1);
		String smultifolder = request.getParameter("multifolder");
		boolean multifolder = smultifolder != null && smultifolder.equals("true");
		String sfullthreads = request.getParameter("fullthreads");
		boolean fullthreads = sfullthreads != null && sfullthreads.equals("true");
		String uids[];
		
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(fromfolder);
			uids = request.getParameterValues("ids");
			if (!multifolder) dmsArchiveMessages(mcache, toLongs(uids), idcategory, idsubcategory, fullthreads);
			else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = account.getFolderCache(fromfolder);
                    iuids[0]=Integer.parseInt(uid);
                    dmsArchiveMessages(mcache, iuids, idcategory, idsubcategory, fullthreads);
				}
			}
			long millis = System.currentTimeMillis();
			JsOperateMessage message = new JsOperateMessage(millis);
			new JsonResult(message).printTo(out);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	String[] getMessageUIDs(FolderCache mcache,HttpServletRequest request) throws MessagingException, IOException {
		String psortfield = request.getParameter("sort");
		String psortdir = request.getParameter("dir");
		String pthreaded=null; //request.getParameter("threaded");
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
		} else if (psortfield.equals("status")||psortfield.equals("unread")) {
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
		
		QueryObj queryObj = null;
		try {
			queryObj = ServletUtils.getObjectParameter(request, "query", new QueryObj(), QueryObj.class);
		}
		catch(ParameterException parameterException) {
				logger.error("Exception getting query obejct parameter", parameterException);
		}
		ImapQuery iq = new ImapQuery(this.allFlagStrings, queryObj, environment.getProfile().getTimeZone());

		Message msgs[] = mcache.getMessages(sortby, ascending, false, sort_group, groupascending, threaded, iq);
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
	
	public void processRunRefreshUnreads(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailAccount account=getAccount(request);
			ArrayList<String> foldernames=ServletUtils.getStringParameters(request, "folders");
			for(String foldername: foldernames) {
				FolderCache fc=account.getFolderCache(foldername);
				if (fc==null) throw new Exception("Folder "+foldername+" not in cache");
				fc.refreshUnreads();
			}
			new JsonResult().printTo(out);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processDeleteMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String fromfolder = request.getParameter("fromfolder");
		String allfiltered = request.getParameter("allfiltered");
		String smultifolder = request.getParameter("multifolder");
		boolean multifolder = smultifolder != null && smultifolder.equals("true");
		String sfullthreads = request.getParameter("fullthreads");
		boolean fullthreads = sfullthreads != null && sfullthreads.equals("true");
		String uids[];
		
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(fromfolder);
			
			if (allfiltered == null) {
				uids = request.getParameterValues("ids");
				if (!multifolder) {
					deleteMessages(mcache, toLongs(uids),fullthreads);
				} else {
					long iuids[]=new long[1];
					for(String uid: uids) {
						int ix=uid.indexOf("|");
						fromfolder=uid.substring(0,ix);
						uid=uid.substring(ix+1);
						mcache = account.getFolderCache(fromfolder);
						iuids[0]=Long.parseLong(uid);
						deleteMessages(mcache, iuids, fullthreads);
					}
				}
			} else {
                uids=getMessageUIDs(mcache,request);
				deleteMessages(mcache, toLongs(uids),true);
			}
			new JsonResult().printTo(out);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processFlagMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String flag = request.getParameter("flag");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(fromfolder);
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
					mcache = account.getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    if (flag.equals("clear")) clearMessagesFlag(mcache, iuids);
                    else flagMessages(mcache, iuids, flag);
				}
			}
			new JsonResult().printTo(out);
		} catch (MessagingException exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false, lookupResource(MailLocaleKey.PERMISSION_DENIED)).printTo(out);
		}
	}
	
	public void processSeenMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");

		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(fromfolder);
			if (!mf) {
				setMessagesSeen(mcache, toLongs(uids));
			} else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    int ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = account.getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    setMessagesSeen(mcache, iuids);
				}
			}
			long millis = System.currentTimeMillis();
			
			new JsonResult()
					.set("millis", millis)
					.printTo(out);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processUnseenMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(fromfolder);
			if (!mf) {
				setMessagesUnseen(mcache, toLongs(uids));
			} else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    int ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = account.getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    setMessagesUnseen(mcache, iuids);
				}
			}
			long millis = System.currentTimeMillis();
			
			new JsonResult()
					.set("millis", millis)
					.printTo(out);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processManageHiddenFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				ArrayList<JsHiddenFolder> hfolders=new ArrayList<>();
				for(String folderId: us.getHiddenFolders()) {
					hfolders.add(new JsHiddenFolder(folderId,folderId));
				}
				new JsonResult(hfolders).printTo(out);
			}
			else if(crud.equals(Crud.DELETE)) {
				ServletUtils.StringArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, true);
				
				for(String id : ids) {
					us.setFolderHidden(id, false);
				}
				new JsonResult().printTo(out);
			}
		} catch(Exception exc) {
			logger.debug("Cannot restore hidden folders",exc);
			new JsonResult("Cannot restore hidden folders", exc).printTo(out);
		}
	}
	
	public void processSetScanFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
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
		Connection con = null;
		
		try {
			con = getConnection();
			FolderCache fc = account.getFolderCache(folder);
			setScanFolder(con, fc, value, recursive);
			new JsonResult().printTo(out);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException exc) {
				}
			}
		}
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
		MailAccount account=getAccount(request);
		String folder = request.getParameter("folder");
		boolean recursive = false;
		String srecursive = request.getParameter("recursive");
		if (srecursive != null) {
			recursive = srecursive.equals("1");
		}
		FolderCache mcache = null;
		
		try {
			account.checkStoreConnected();
			mcache = account.getFolderCache(folder);
			setMessagesSeen(mcache, true, recursive,true);
			long millis = System.currentTimeMillis();
			JsOperateMessage message = new JsOperateMessage(millis);
			new JsonResult(message).printTo(out);
		} catch (Throwable t) {
			new JsonResult(t)
					.set("oldid", folder)
					.set("oldname", mcache != null ? mcache.getFolder().getName() : "unknown")
					.printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processUnseenFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String folder = request.getParameter("folder");
		boolean recursive = false;
		String srecursive = request.getParameter("recursive");
		if (srecursive != null) {
			recursive = srecursive.equals("1");
		}
		FolderCache mcache = null;
		
		try {
			account.checkStoreConnected();
			mcache = account.getFolderCache(folder);
			setMessagesSeen(mcache, false, recursive,true);
			new JsonResult().printTo(out);
		} catch (Throwable t) {
			new JsonResult(t)
					.set("oldid", folder)
					.set("oldname", mcache != null ? mcache.getFolder().getName() : "unknown")
					.printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	private void setMessagesSeen(FolderCache mcache, boolean seen, boolean recursive, boolean updateParents) throws MessagingException {
		if (recursive) {
			ArrayList<FolderCache> children = mcache.getChildren();
			if (children != null) {
				for (FolderCache fc : children) {
					setMessagesSeen(fc, seen, recursive, false);
				}
			}
		}
		if (seen) {
			mcache.setMessagesSeen(updateParents);
		} else {
			mcache.setMessagesUnseen(updateParents);
		}
	}
	
	public String getPartName(Part p) throws MessagingException {
		String pname = p.getFileName();
		// TODO: Remove code below if already included in JavaMail impl.
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
			logger.warn("Code in getPartName is still used. Please review it!");
			try {
				pname = MimeUtility.decodeText(pname);
			} catch(UnsupportedEncodingException ex) {
				Service.logger.error("Exception", ex);
			}
		}
		return pname;
	}
	
    public void processRequestCloudFile(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
			try {
				String subject=ServletUtils.getStringParameter(request, "subject", true);
				String path="/Uploads";
				int sid=vfsmanager.getMyDocumentsStoreId();
				FileObject fo=vfsmanager.getStoreFile(sid,path);
				if (!fo.exists()) fo.createFolder();
				
				String dirname=PathUtils.sanitizeFolderName(DateTimeUtils.createYmdHmsFormatter(environment.getProfile().getTimeZone()).print(DateTimeUtils.now())+" - "+subject);

				FileObject dir=fo.resolveFile(dirname);
				if (!dir.exists()) dir.createFolder();
				
				JsonResult jsres=new JsonResult();
				jsres.put("storeId", sid);
				jsres.put("filePath", path+"/"+dirname+"/");
				jsres.printTo(out);
			} catch(Exception ex) {
				logger.error("Request cloud file failure", ex);
				new JsonResult(ex).printTo(out);
			}
	}	

    public void processSaveColumnSize(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String name=request.getParameter("name");
		String size=request.getParameter("size");
		us.setColumnSize(name,Integer.parseInt(size));
		new JsonResult().printTo(out);
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
			new JsonResult(ex).printTo(out);
		}
	}
		
	public void processSaveColumnVisibility(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailAccount account=getAccount(request);
			String folder = ServletUtils.getStringParameter(request, "folder", true);
			String name = ServletUtils.getStringParameter(request, "name", true);
			Boolean visible = ServletUtils.getBooleanParameter(request, "visible", false);

			ColumnVisibilitySetting cvs = us.getColumnVisibilitySetting(folder);
			FolderCache fc = account.getFolderCache(folder);

			cvs.put(name, visible);
			// Handle default cases...avoid data waste!
			//if(ColumnVisibilitySetting.isDefault(fc.isSent(), name, cvs.get(name))) cvs.remove(name);
			if(ColumnVisibilitySetting.isDefault(account.isUnderSentFolder(fc.getFolderName()), name, cvs.get(name))) cvs.remove(name);

			if(cvs.isEmpty()) {
				us.clearColumnVisibilitySetting(folder);
			} else {
				us.setColumnVisibilitySetting(folder, cvs);
			}
			new JsonResult().printTo(out);

		} catch(Exception ex) {
			logger.error("Error saving column visibility.", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processNewFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String folder = request.getParameter("folder");
		String name = request.getParameter("name");
		FolderCache mcache = null;
		
		try {
			account.checkStoreConnected();
			Folder newfolder;
			name = account.normalizeName(name);
            if (
					folder==null ||
					(account.hasDifferentDefaultFolder() && folder.trim().length()==0)
				)
				mcache=account.getRootFolderCache();
            else
				mcache=account.getFolderCache(folder);
			
			newfolder = mcache.createFolder(name);
			if (newfolder == null) {
				new JsonResult(false, "Error creating folder").printTo(out);
			} else {
				if (mailManager.isAuditEnabled()) {
					mailManager.auditLogWrite(
						MailManager.AuditContext.FOLDER,
						MailManager.AuditAction.CREATE, 
						newfolder.getFullName(),
						null
					);
				}
				
				String parent = !account.isRoot(mcache) ? mcache.getFolderName() : null;
				JsOperateFolder createdFolder = new JsOperateFolder()
						.setNewIdParent(parent)
						.setName(newfolder.getName())
						.setFullname(newfolder.getFullName());
				new JsonResult(createdFolder).printTo(out, false);
			}
		} catch (Throwable t) {
			new JsonResult(t)
					.set("oldid", folder)
					.set("oldname", mcache != null ? mcache.getFolder().getName() : "unknown")
					.printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processRenameFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String folder = request.getParameter("folder");
		String name = request.getParameter("name");
		FolderCache mcache = null;
		MailUserSettings.FavoriteFolders favorites = us.getFavoriteFolders();
		
		try {
			String newid = folder;
			mcache = account.getFolderCache(folder);
			account.checkStoreConnected();
			
			if(mcache.getParent().isSharedFolder()) { 
				us.setSharedFolderName(folder, name);
				mcache.setDescription(us.getSharedFolderName(folder));
			}
			else {
			
			name = account.normalizeName(name);
			newid = account.renameFolder(folder, name);
			
			if (favorites.contains(account.getId(),folder)) {
				favorites.remove(account.getId(),folder);
				favorites.add(account.getId(),newid,name);
				us.setFavoriteFolders(favorites);
				}
			}
			
			if (mailManager.isAuditEnabled()) {
				mailManager.auditLogWrite(
					MailManager.AuditContext.FOLDER,
					MailManager.AuditAction.RENAME,
					newid,
					JsonUtils.toJson("oldId", folder)
				);
				mailManager.auditLogRebaseReference(MailManager.AuditContext.FOLDER, folder, newid);
			}
			
			JsOperateFolder renamedFolder = new JsOperateFolder()
					.setOldid(folder)
					.setNewid(newid)
					.setNewname(name);
			new JsonResult(renamedFolder).printTo(out, false);
		} catch (Throwable t) {
			new JsonResult(t)
					.set("oldid", folder)
					.set("oldname", mcache != null ? mcache.getFolder().getName() : "unknown")
					.printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processHideFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String folder = request.getParameter("folder");

		try {
			account.checkStoreConnected();
			if (account.isSpecialFolder(folder)) {
				new JsonResult(false, "Cannot hide special folders").printTo(out);
			} else {
				hideFolder(account,folder);
				new JsonResult().printTo(out);
			}
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processDeleteFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String folder = request.getParameter("folder");
		FolderCache mcache = null;
		
		try {
			account.checkStoreConnected();
			mcache = account.getFolderCache(folder);
			if (!account.isUnderFolder(account.getFolderArchive(),folder) && account.isSpecialFolder(folder)) {
				new JsonResult(false, "Cannot delete special folders").printTo(out);
			} else {
				if (account.deleteFolder(folder)) {
					if (mailManager.isAuditEnabled()) {
						mailManager.auditLogWrite(
							MailManager.AuditContext.FOLDER,
							MailManager.AuditAction.DELETE,
							folder,
							null
						);
					}
					
					JsOperateFolder deletedFolder = new JsOperateFolder()
							.setOldid(folder);
					new JsonResult(deletedFolder).printTo(out, false);
				} else {
					new JsonResult(false, "Error deleting folder").printTo(out);
				}
			}
		} catch (Throwable t) {
			new JsonResult(t)
					.set("oldid", folder)
					.set("oldname", mcache != null ? mcache.getFolder().getName() : "unknown")
					.printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processTrashFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String folder = request.getParameter("folder");

		try {
			account.checkStoreConnected();
			
			if (!account.isUnderFolder(account.getFolderArchive(),folder) && account.isSpecialFolder(folder)) {
				new JsonResult(false, "Cannot trash special folder").printTo(out);
			} else {
				FolderCache newfc = account.trashFolder(folder);
				if (newfc!=null) {
					if (mailManager.isAuditEnabled()) {
						HashMap<String, String> trashAudit = new HashMap<>();
						trashAudit.put("oldId", folder);
						trashAudit.put("newId", newfc.getFolderName());
						
						mailManager.auditLogWrite(
							MailManager.AuditContext.FOLDER,
							MailManager.AuditAction.TRASH,
							newfc.getFolderName(),
							JsonResult.gson().toJson(trashAudit)
						);
						mailManager.auditLogRebaseReference(MailManager.AuditContext.FOLDER, folder, newfc.getFolderName());
					}
					
					JsOperateFolder trashedFolder = new JsOperateFolder()
							.setNewid(newfc.getFolder().getFullName())
							.setTrashid(newfc.getParent().getFolder().getFullName());
					new JsonResult(trashedFolder).printTo(out, false);
				}
			}
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processMoveFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String folder = request.getParameter("folder");
		String to = request.getParameter("to");
		FolderCache mcache = null;
		
		try {
			account.checkStoreConnected();
			mcache = account.getFolderCache(folder);
			if (account.isSpecialFolder(folder)) {
				new JsonResult(false, "Cannot move special folders").printTo(out);
			} else {
				FolderCache oldfcparent = account.getFolderCache(folder).getParent();
				FolderCache newfc = account.moveFolder(folder, to);
				Folder newf = newfc.getFolder();
				
				if (mailManager.isAuditEnabled()) {
					mailManager.auditLogWrite(
						MailManager.AuditContext.FOLDER,
						MailManager.AuditAction.MOVE,
						newfc.getFolderName(),
						JsonUtils.toJson("oldId", folder)
					);
					mailManager.auditLogRebaseReference(MailManager.AuditContext.FOLDER, folder, newfc.getFolderName());
				}
				
				JsOperateFolder movedFolder = new JsOperateFolder()
						.setOldid(folder)
						.setNewid(newf.getFullName())
						.setNewname(newf.getName());
				if (to != null) movedFolder.setNewIdParent(newf.getParent().getFullName());
				if (oldfcparent != null) movedFolder.setOldIdParent(oldfcparent.getFolderName());
				new JsonResult(movedFolder).printTo(out, false);
			}
		} catch (Throwable t) {
			new JsonResult(t)
					.set("oldid", folder)
					.set("oldname", mcache != null ? mcache.getFolder().getName() : "unknown")
					.printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processEmptyFolder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String folder = request.getParameter("folder");
		FolderCache mcache = null;
		
		try {
			account.checkStoreConnected();
			mcache = account.getFolderCache(folder);
			account.emptyFolder(folder);
			
			if (mailManager.isAuditEnabled()) {
				mailManager.auditLogWrite(
					MailManager.AuditContext.FOLDER,
					MailManager.AuditAction.EMPTY,
					folder,
					null
				);
			}
			
			JsOperateFolder emptyFolder = new JsOperateFolder().setOldid(folder);
			new JsonResult(emptyFolder).printTo(out, false);
		} catch (Throwable t) {
			new JsonResult(t)
					.set("oldid", folder)
					.set("oldname", mcache != null ? mcache.getFolder().getName() : "unknown")
					.printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processGetSource(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String foldername = request.getParameter("folder");
		String uid = request.getParameter("id");
		String sheaders = request.getParameter("headers");
		boolean headers = sheaders.equals("true");

		try {
			account.checkStoreConnected();
			//StringBuffer sb = new StringBuffer("<pre>");
			StringBuffer sb = new StringBuffer();
			FolderCache mcache = account.getFolderCache(foldername);
			Message msg = mcache.getMessage(Long.parseLong(uid));
			//Folder folder=msg.getFolder();
			for (Enumeration e = msg.getAllHeaders(); e.hasMoreElements();) {
				Header header = (Header) e.nextElement();
				//sb.append(MailUtils.htmlescape(header.getName()) + ": " + MailUtils.htmlescape(header.getValue()) + "\n");
				sb.append(header.getName() + ": " + header.getValue() + "\n");
			}
			if (!headers) {
				BufferedReader br = new BufferedReader(new InputStreamReader(msg.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					//sb.append(MailUtils.htmlescape(line) + "\n");
					sb.append(line + "\n");
				}
			}
			//sb.append("</pre>");
			new JsonResult()
					.set("source", sb.toString())
					.printTo(out);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processSaveMail(HttpServletRequest request, HttpServletResponse response) {
		MailAccount account=getAccount(request);
		String foldername = request.getParameter("folder");
		String uid = request.getParameter("id");
		
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(foldername);
			Message msg=mcache.getMessage(Long.parseLong(uid));
			String subject = msg.getSubject();
			if (StringUtils.isEmpty(subject)) subject="No subject";
			ServletUtils.setFileStreamHeadersForceDownload(response, subject + ".eml");
			OutputStream out = response.getOutputStream();
			msg.writeTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public void processDownloadMails(HttpServletRequest request, HttpServletResponse response) {
		MailAccount account=getAccount(request);
		String foldername = request.getParameter("folder");

		try {
			account.checkStoreConnected();
			StringBuffer sb = new StringBuffer();
			FolderCache mcache = account.getFolderCache(foldername);
			Message msgs[] = mcache.getAllMessages();
			String zipname = "Webtop-mails-" + getInternationalFolderName(mcache);
			response.setContentType("application/x-zip-compressed");
			response.setHeader("Content-Disposition", "inline; filename=\"" + zipname + ".zip\"");
			OutputStream out = response.getOutputStream();
			
			try (ZipOutputStream zos = new ZipOutputStream(out)) {
				outputZipMailFolder(null, msgs, zos);
				
				int cut=foldername.length()+1;
				cycleMailFolder(account, mcache.getFolder(), cut, zos);
			}
			
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	private void cycleMailFolder(MailAccount account, Folder folder, int cut, ZipOutputStream zos) throws Exception {
		for(Folder child: folder.list()) {
			String fullname=child.getFullName();
			String relname=fullname.substring(cut).replace(folder.getSeparator(), '/');
			
			zos.putNextEntry(new ZipEntry(relname+"/"));
			zos.closeEntry();
			
			cycleMailFolder(account,child,cut,zos);
			
			FolderCache mcache=account.getFolderCache(fullname);
			Message msgs[]=mcache.getAllMessages();
			if (msgs.length>0) outputZipMailFolder(relname, msgs, zos);
		}
	}
	
	private void outputZipMailFolder(String foldername, Message msgs[], ZipOutputStream zos) throws Exception {
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
			ZipEntry ze=new ZipEntry(fullname);
			ze.setTime(date.getTime());
			zos.putNextEntry(ze);
			msg.writeTo(zos);
			zos.closeEntry();
		}
		zos.flush();
	}
        
        private boolean isImageFilename(String filename) {
            String ext=PathUtils.getFileExtension(filename).toLowerCase();
            if (ext.equals("jpg")||ext.equals("png")||ext.equals("gif")) return true;
            return false;
        }

	public void processGetReplyMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		UserProfile profile = environment.getProfile();
		//WebTopApp webtopapp=environment.getWebTopApp();
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String preplyall = request.getParameter("replyall");
		boolean replyAll = false;
		if (preplyall != null && preplyall.equals("1")) {
			replyAll = true;
		}
		ArrayList<JsRecipient> torecipients = new ArrayList<>();
		ArrayList<JsRecipient> ccrecipients = new ArrayList<>();
		ArrayList<JsRecipient> bccrecipients = new ArrayList<>();
		ArrayList<JsAttachment> attachments = new ArrayList<>();
		
		try {
			String format=us.getFormat();
			boolean isHtml=format.equals("html");
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
			Message m=mcache.getMessage(Long.parseLong(puidmessage));
			if (m.isExpunged()) {
				throw new MessagingException("Message " + puidmessage + " expunged");
			}
			int newmsgid=getNewMessageID();
                        HTMLMailData maildata = mcache.getMailData((MimeMessage) m);
			SimpleMessage smsg = getReplyMsg(
					getNewMessageID(), account, m, replyAll, account.isSentFolder(pfoldername), isHtml,
					profile.getEmailAddress(), mprofile.isIncludeMessageInReply(),
					lookupResource(MailLocaleKey.MSG_FROMTITLE),
					lookupResource(MailLocaleKey.MSG_TOTITLE),
					lookupResource(MailLocaleKey.MSG_CCTITLE),
					lookupResource(MailLocaleKey.MSG_DATETITLE),
					lookupResource(MailLocaleKey.MSG_SUBJECTTITLE),
                    false
			);
			
			String content;
			String refs = "";
            Identity ident=mprofile.getIdentity(pfoldername);
			String inreplyto = smsg.getInReplyTo();
			String references[] = smsg.getReferences();
			
			if (references != null) {
				for (String s : references) {
					refs += s + " ";
				}
			}
			
			String subject = smsg.getSubject();
			
			String tos[] = smsg.getTo().split(";");
			for (String to : tos) {
				if (!StringUtils.isBlank(to)) torecipients.add(new JsRecipient(to));
			}
			String ccs[] = smsg.getCc().split(";");
			for (String cc : ccs) {
				if (!StringUtils.isBlank(cc)) ccrecipients.add(new JsRecipient(cc));
			}
			
			if (isHtml) {
				String html = smsg.getContent();
				
				//cid inline attachments and relative html href substitution
				for (int i = 0; i < maildata.getAttachmentPartCount(); ++i) {
					try{
						Part part = maildata.getAttachmentPart(i);
						String filename = MailUtils.getPartFilename(part, true);
						String cids[] = part.getHeader("Content-ID");
						String cid = null;
						//String cid=filename;
						if (cids != null && cids[0] != null) {
							cid = cids[0];
							if (cid.startsWith("<")) cid=cid.substring(1);
							if (cid.endsWith(">")) cid=cid.substring(0,cid.length()-1);
						}

						if (filename == null) filename = cid;
						String mime=MailUtils.getMediaTypeFromHeader(part.getContentType());
						UploadedFile upfile=addAsUploadedFile(""+newmsgid, filename, mime, part.getInputStream());
						boolean inline = false;
						if (part.getDisposition() != null) {
							inline = part.getDisposition().equalsIgnoreCase(Part.INLINE) &&
									isInlineableMime(mime);
						}
						//in reply includes only cid & inline attachments
						if (inline || cid!=null) attachments.add(new JsAttachment(upfile.getUploadId(), filename, cid, inline, upfile.getSize(), isFileEditableInDocEditor(filename)));

						//TODO: change this weird matching of cids2urls!
						if (cid!=null) html = StringUtils.replace(html, "cid:" + cid, "service-request?csrf="+getEnv().getCSRFToken()+"&service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&uploadId=" + upfile.getUploadId() + "&cid="+cid);

					} catch (Exception exc) {
						Service.logger.error("Exception",exc);
					}
				}
				content = html;
			} else {
				content = smsg.getTextContent();
			}
			
			JsMessage message = new JsMessage(subject, content, format, ident.getIdentityId(), attachments, torecipients, ccrecipients, null, pfoldername, inreplyto, refs.trim(), Long.parseLong(puidmessage));
			new JsonResult(message).printTo(out, false);
			
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}

	public void processGetForwardMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String[] messagesIds = request.getParameterValues("messageIds");
		String pnewmsgid = request.getParameter("newmsgid");
		String pattached = request.getParameter("attached");
		boolean attached = (pattached != null && pattached.equals("1"));
		if (messagesIds.length > 1) attached = true; // With multiple msgIds we only support "forward" as attachment
		
		long newmsgid = Long.parseLong(pnewmsgid);
		
		try {
			String format=us.getFormat();
			boolean isHtml=format.equals("html");
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
			
			List<Message> messages = new ArrayList<>();
			for(String messageId : messagesIds) {
				Message message = mcache.getMessage(Long.parseLong(messageId)); 
				if (message.isExpunged()) throw new MessagingException("Message expunged");
				messages.add(message);
			}
            HTMLMailData maildata = mcache.getMailData((MimeMessage) messages.get(0));
			
			SimpleMessage smsg = getForwardMsg(
					newmsgid, messages.get(0), isHtml,
					lookupResource(MailLocaleKey.MSG_FROMTITLE),
					lookupResource(MailLocaleKey.MSG_TOTITLE),
					lookupResource(MailLocaleKey.MSG_CCTITLE),
					lookupResource(MailLocaleKey.MSG_DATETITLE),
					lookupResource(MailLocaleKey.MSG_SUBJECTTITLE),
					attached,
                                        maildata.isPEC()
			);
			
			Identity ident=mprofile.getIdentity(pfoldername);
			String forwardedfrom = smsg.getForwardedFrom();
			ArrayList<JsAttachment> attachments = new ArrayList<>();
			
			//add inreplyto and references as with reply
			String inreplyto = smsg.getInReplyTo();
			String references[] = smsg.getReferences();
			String refs = "";
			if (references != null) {
				for (String s : references) {
					refs += StringEscapeUtils.escapeEcmaScript(s) + " ";
				}
			}
			String subject = smsg.getSubject();
			String html=smsg.getContent();
			String text=smsg.getTextContent();
			
			if (!attached) {
				
				for (int i = 0; i < maildata.getAttachmentPartCount(); ++i) {
					try{
						Part part = maildata.getAttachmentPart(i);
                                                int level=maildata.getPartLevel(part);
                                                if (level>0) continue;
                                                
						String filename = MailUtils.getPartFilename(part, true);
						String cids[] = part.getHeader("Content-ID");
						String cid = null;
						//String cid=filename;
						if (cids != null && cids[0] != null) {
							cid = cids[0];
							if (cid.startsWith("<")) cid=cid.substring(1);
							if (cid.endsWith(">")) cid=cid.substring(0,cid.length()-1);
						}

						if (filename == null) filename = cid;
						String mime=MailUtils.getMediaTypeFromHeader(part.getContentType());
						UploadedFile upfile=addAsUploadedFile(pnewmsgid, filename, mime, part.getInputStream());
						boolean inline = false;
						if (part.getDisposition() != null) {
							inline = part.getDisposition().equalsIgnoreCase(Part.INLINE) &&
									isInlineableMime(mime);
						}
						
						attachments.add(new JsAttachment(upfile.getUploadId(), filename, cid, inline, upfile.getSize(), isFileEditableInDocEditor(filename)));
						
						//TODO: change this weird matching of cids2urls!
						html = StringUtils.replace(html, "cid:" + cid, "service-request?csrf="+getEnv().getCSRFToken()+"&service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&uploadId=" + upfile.getUploadId() + "&cid="+cid);
					} catch (Exception exc) {
						Service.logger.error("Exception",exc);
					}
				}
				//String surl = "service-request?service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&newmsgid=" + newmsgid + "&cid=";
				//html = replaceCidUrls(html, maildata, surl);
			} else {
				for(Message message : messages) {
					String filename = message.getSubject() + ".eml";
					UploadedFile upfile = addAsUploadedFile(pnewmsgid, filename, "message/rfc822", ((IMAPMessage)message).getMimeStream());
					attachments.add(new JsAttachment(upfile.getUploadId(), filename, null, false, upfile.getSize(), isFileEditableInDocEditor(filename)));
				}
			}
			String content = isHtml ? html : text;
			
			JsMessage message = new JsMessage(subject, content, format, ident.getIdentityId(), attachments, pfoldername, forwardedfrom, inreplyto, refs.trim(), Long.parseLong(messagesIds[0]));
			new JsonResult(message).printTo(out, false);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	private boolean isPreviewBalanceTags(InternetAddress ia) {
		return previewBalanceTags;
	}
	
	public void processGetEditMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pnewmsgid = request.getParameter("newmsgid");
		long newmsgid = Long.parseLong(pnewmsgid);
		
		try {
			MailEditFormat editFormat = ServletUtils.getEnumParameter(request, "format", null, MailEditFormat.class);
			if (editFormat == null) editFormat = EnumUtils.forSerializedName(us.getFormat(), MailEditFormat.HTML, MailEditFormat.class);
			boolean isPlainEdit = MailEditFormat.PLAIN_TEXT.equals(editFormat);
			
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
			IMAPMessage m = (IMAPMessage)mcache.getMessage(Long.parseLong(puidmessage));
			m.setPeek(us.isManualSeen());
			//boolean wasseen = m.isSet(Flags.Flag.SEEN);
			String vheader[] = m.getHeader("Disposition-Notification-To");
			boolean receipt = false;
			int priority = 3;
			boolean recipients = false;
			boolean toDelete=false;
			ArrayList<JsRecipient> torecipientsList = new ArrayList<>();
			ArrayList<JsRecipient> ccrecipientsList = new ArrayList<>();
			ArrayList<JsRecipient> bccrecipientsList = new ArrayList<>();
			ArrayList<JsAttachment> attachmentsList = new ArrayList<>();
			
			if (account.isDraftsFolder(pfoldername)) {
				if (vheader != null && vheader[0] != null) {
					receipt = true;
				}
				priority = getPriority(m);
				recipients = true;
				
				//if autosaved drafts, delete
				String values[]=m.getHeader(HEADER_X_WEBTOP_MSGID);
				if (values!=null && values.length>0) {
					try {
						long msgId=Long.parseLong(values[0]);
						if (msgId>0) {
							toDelete=true;
						}
					} catch(NumberFormatException exc) {
						
					}
				}
			}
			
			String subject = m.getSubject();
			String inreplyto = null;
			String references[];
			String refs = "";
			String replyfolder = null;
			String forwardedfolder = null;
			
			String vs[] = m.getHeader("In-Reply-To");
			if (vs != null && vs[0] != null) {
				inreplyto = vs[0];
			}
			
			references = m.getHeader("References");
			if (inreplyto != null) {
				vs = m.getHeader("Sonicle-reply-folder");
				if (vs != null && vs[0] != null) {
					replyfolder = vs[0];
				}
			}
			if (references != null) {
				for (String s : references) {
					refs += StringEscapeUtils.escapeEcmaScript(s) + " ";
				}
			}
			
			String forwardedfrom = null;
			vs = m.getHeader("Forwarded-From");
			if (vs != null && vs[0] != null) {
				forwardedfrom = vs[0];
			}
			if (forwardedfrom != null) {
				vs = m.getHeader("Sonicle-forwarded-folder");
				if (vs != null && vs[0] != null) {
					forwardedfolder = vs[0];
				}
			}
			
			Boolean isPriority = priority >= 3 ? false : true;
			
            Identity ident=null;
			Address from[]=m.getFrom();
			InternetAddress iafrom=null;
			if (from!=null && from.length>0) {
				iafrom=(InternetAddress)from[0];
				String email=iafrom.getAddress();
				String displayname=iafrom.getPersonal();
				if (displayname==null) displayname=email;
				//sout+=" from: { email: '"+StringEscapeUtils.escapeEcmaScript(email)+"', displayname: '"+StringEscapeUtils.escapeEcmaScript(displayname)+"' },\n";
                ident=mprofile.getIdentity(displayname, email);
			}
			if (ident==null) ident=mprofile.getIdentity(pfoldername);
			
			if (recipients) {
				Address tos[] = m.getRecipients(RecipientType.TO);
				if (tos != null) {
					for (Address to : tos) {
						torecipientsList.add(new JsRecipient(getDecodedAddress(to)));
					}
				}
				Address ccs[] = m.getRecipients(RecipientType.CC);
				if (ccs != null) {
					for (Address cc : ccs) {
						ccrecipientsList.add(new JsRecipient(getDecodedAddress(cc)));
					}
				}
				Address bccs[] = m.getRecipients(RecipientType.BCC);
				if (bccs != null) {
					for (Address bcc : bccs) {
						bccrecipientsList.add(new JsRecipient(getDecodedAddress(bcc)));
					}
				}
			}
			
			String html = "";
			boolean balanceTags=isPreviewBalanceTags(iafrom);
			ArrayList<FolderCache.HTMLPart> htmlparts = mcache.getHTMLParts((MimeMessage) m, newmsgid, true, balanceTags);
			for (FolderCache.HTMLPart htmlPart : htmlparts) {
				html += htmlPart.html + "<BR><BR>";
			}
            HTMLMailData maildata = mcache.getMailData((MimeMessage) m);
			//if(!wasseen){
			//	if (us.isManualSeen()) {
			//		m.setFlag(Flags.Flag.SEEN, false);
			//	}
			//}
			
            for (int i = 0; i < maildata.getAttachmentPartCount(); ++i) {
                Part part = maildata.getAttachmentPart(i);
                String filename = getPartName(part);
                
                    String cids[] = part.getHeader("Content-ID");
                    String cid = null;
                    //String cid=filename;
                    if (cids != null && cids[0] != null) {
                        cid = cids[0];
                        if (cid.startsWith("<")) cid=cid.substring(1);
                        if (cid.endsWith(">")) cid=cid.substring(0,cid.length()-1);
                    }
					
					if( filename == null ) {
						filename = cid;
					}
                    String mime=part.getContentType();
                    UploadedFile upfile=addAsUploadedFile(pnewmsgid, filename, mime, part.getInputStream());
                    boolean inline = false;
                    if (part.getDisposition() != null) {
                        inline = part.getDisposition().equalsIgnoreCase(Part.INLINE);
                    }
                    
					attachmentsList.add(new JsAttachment(upfile.getUploadId(), filename, cid, inline, upfile.getSize()));
                    
					//TODO: change this weird matching of cids2urls!
                    html = StringUtils.replace(html, "cid:" + cid, "service-request?csrf="+getEnv().getCSRFToken()+"&service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&uploadId=" + upfile.getUploadId() + "&cid="+cid);
             
            }
			
			String content = isPlainEdit ? MailUtils.htmlToText(MailUtils.htmlunescapesource(html)) : html;
			m.setPeek(false);
			
			if (toDelete) {
				m.setFlag(Flags.Flag.DELETED, true);
				m.getFolder().expunge();
			}
			
			JsMessage message = new JsMessage(pfoldername, receipt, isPriority, subject, content, EnumUtils.toSerializedName(editFormat), ident.getIdentityId(), attachmentsList, torecipientsList, ccrecipientsList, bccrecipientsList, replyfolder, inreplyto, refs.trim(), forwardedfolder, forwardedfrom, Long.parseLong(puidmessage), toDelete); 
			new JsonResult(message).printTo(out, false);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
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
		IContactsManager contactsManager = (IContactsManager)WT.getServiceManager("com.sonicle.webtop.contacts", true, environment.getProfileId());

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
			MailAccount account=getAccount(request);
			//String emails[]=request.getParameterValues("recipients");
            Payload<MapItem, JsMessage> pl=ServletUtils.getPayload(request,JsMessage.class);
            JsMessage jsmsg=pl.data;
            long msgId=ServletUtils.getLongParameter(request, "msgId", true);
            boolean isFax=ServletUtils.getBooleanParameter(request, "isFax", false);
            boolean save=ServletUtils.getBooleanParameter(request, "save", false);
			
			if (isFax) {
				int faxmaxtos=getEnv().getCoreServiceSettings().getFaxMaxRecipients();
				
				//check for valid fax recipients
				String faxpattern=getEnv().getCoreServiceSettings().getFaxPattern();
				String regex="^"+faxpattern.replace("{number}", "(\\d+)").replace("{username}", "(\\w+)")+"$";
				Pattern pattern=Pattern.compile(regex);
				int nemails=0;
				
				for(JsRecipient jsrcpt: jsmsg.torecipients) {
					if (StringUtils.isEmpty(jsrcpt.email)) continue;
					++nemails;
					if (StringUtils.isNumeric(jsrcpt.email)) continue;
					boolean matches=false;
					try {
						InternetAddress ia=new InternetAddress(jsrcpt.email);
						jsrcpt.email=ia.getAddress();
						matches=pattern.matcher(jsrcpt.email).matches();
					} catch(Exception exc) {

					}
					if (!matches) {
						throw new Exception(lookupResource(MailLocaleKey.FAX_ADDRESS_ERROR));
					}
				}
				if (faxmaxtos>0 && nemails>faxmaxtos) {
					throw new WTException(lookupResource(MailLocaleKey.FAX_MAXADDRESS_ERROR), faxmaxtos);
				}
			}
			
			account.checkStoreConnected();
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
			if (ifrom.isAlwaysCc()) {
				msg.addCc(new String[] { ifrom.hasAlwaysCcEmail()?ifrom.getAlwaysCcEmail():ifrom.getEmail() });
			}
			
			account.checkStoreConnected();
			SendException sendExc = sendMessage(msg, jsmsg.attachments);
                        String foundfolder = null;
			if (sendExc == null || sendExc.messageSent) {
				//if is draft, check for deletion
				if (jsmsg.draftuid>0 && jsmsg.draftfolder!=null && ss.isDefaultFolderDraftsDeleteMsgOnSend()) {
					FolderCache fc=account.getFolderCache(jsmsg.draftfolder);
					fc.deleteMessages(new long[] { jsmsg.draftuid }, false);
				}
				
				//Save used recipients
				ArrayList<InternetAddress> iaTos = new ArrayList<>();
				ArrayList<InternetAddress> iaCcs = new ArrayList<>();
				ArrayList<InternetAddress> iaBccs = new ArrayList<>();
				for(JsRecipient jsrcpt: pl.data.torecipients) {
					InternetAddress ia = InternetAddressUtils.toInternetAddress(jsrcpt.email);
					if (ia != null) {
						if (!ContactsUtils.isListVirtualRecipient(ia)) coreMgr.autoLearnInternetRecipient(InternetAddressUtils.toFullAddress(ia));
						iaTos.add(ia);
					}
				}
				for(JsRecipient jsrcpt: pl.data.ccrecipients) {
					InternetAddress ia = InternetAddressUtils.toInternetAddress(jsrcpt.email);
					if (ia != null) {
						if (!ContactsUtils.isListVirtualRecipient(ia)) coreMgr.autoLearnInternetRecipient(InternetAddressUtils.toFullAddress(ia));
						iaCcs.add(ia);
					}
				}
				for(JsRecipient jsrcpt: pl.data.bccrecipients) {
					InternetAddress ia = InternetAddressUtils.toInternetAddress(jsrcpt.email);
					if (ia != null) {
						if (!ContactsUtils.isListVirtualRecipient(ia)) coreMgr.autoLearnInternetRecipient(InternetAddressUtils.toFullAddress(ia));
						iaBccs.add(ia);
					}
				}
				
				//Save subject for suggestions
				if (jsmsg.subject!=null && jsmsg.subject.trim().length()>0)
					WT.getCoreManager().addServiceStoreEntry(SERVICE_ID, "subject", jsmsg.subject.toUpperCase(),jsmsg.subject);

				coreMgr.deleteMyAutosaveData(getEnv().getClientTrackingID(), SERVICE_ID, "newmail", ""+msgId);
				
				deleteAutosavedDraft(account,msgId);
				// TODO: Cloud integration!!! Destination emails added to share
/*                if (vfs!=null && hashlinks!=null && hashlinks.size()>0) {
				 for(String hash: hashlinks) {
				 Service.logger.debug("Adding emails to hash "+hash);
				 vfs.setAuthEmails(hash, from, emails);
				 }

				 }*/
				FolderCache fc = account.getFolderCache(account.getFolderSent());
				fc.setForceRefresh();
				//check for in-reply-to and set the answered flags
				//String inreplyto = request.getParameter("inreplyto");
				//String replyfolder = request.getParameter("replyfolder");
				//String forwardedfrom = request.getParameter("forwardedfrom");
				//String forwardedfolder = request.getParameter("forwardedfolder");
                //String soriguid=request.getParameter("origuid");
				//long origuid=0;
				//try { origuid=Long.parseLong(soriguid); } catch(RuntimeException rexc) {}
				
				if (jsmsg.forwardedfrom != null && jsmsg.forwardedfrom.trim().length() > 0) {
					JsAuditMessageInfo jsAudit = iaRecipientsToAuditInfo(iaTos, iaCcs, iaBccs);
					if (StringUtils.isNotEmpty(from)) jsAudit.setFrom(from);
					if (ifrom != null && StringUtils.isNotEmpty(ifrom.getDisplayName())) jsAudit.setFromDN(ifrom.getDisplayName());
					if (StringUtils.isNotEmpty(jsmsg.subject)) jsAudit.setSubject(jsmsg.subject);
					jsAudit.setFolder(jsmsg.folder);
					if (mailManager.isAuditEnabled() && StringUtils.isNotEmpty(jsmsg.forwardedfrom)) {
						mailManager.auditLogWrite(
							MailManager.AuditContext.MAIL,
							MailManager.AuditAction.FORWARD, 
							jsmsg.forwardedfrom, 
							JsonResult.gson(false).toJson(jsAudit)
						);
					}
					
					try {
						foundfolder = foundfolder=flagForwardedMessage(account,jsmsg.forwardedfolder,jsmsg.forwardedfrom,jsmsg.origuid);
					} catch (Exception xexc) {
						Service.logger.error("Exception",xexc);
					}
				}
				else if((jsmsg.inreplyto != null && jsmsg.inreplyto.trim().length()>0)||(jsmsg.replyfolder!=null&&jsmsg.replyfolder.trim().length()>0&&jsmsg.origuid>0)) {
					try {
						String[] toRecipients = SimpleMessage.breakAddr(msg.getTo());
						ArrayList<Integer> cats = new ArrayList<>();
						cats.addAll(contactsManager.listMyCategoryIds());
						cats.addAll(contactsManager.listIncomingCategoryIds());
						
						for (String toRecipient : toRecipients) {
							InternetAddress ia = getInternetAddress(toRecipient);
							if (!StringUtils.isBlank(ia.getAddress())) {
								String email=ia.getAddress();
								Condition<ContactQuery> filterQuery = new ContactQuery().anyEmail().like(email);
								if (!contactsManager.existAnyContact(cats, filterQuery)) {
									boolean found=false;
									//check also internal users profile email
									for (String userId : coreMgr.listUserIds(EnabledCond.ENABLED_ONLY)) {
										UserProfile.Data userData=WT.getProfileData(new UserProfileId(coreMgr.getTargetProfileId().getDomainId(), userId));
										if ((found=StringUtils.equalsIgnoreCase(userData.getPersonalEmailAddress(), email)))
											break;
									}
									if (!found) {
										//check also internal users identities
										List<Identity> idents=mailManager.listAllPersonalIdentities(coreMgr.getTargetProfileId().getDomainId());
										for(Identity ident: idents) {
											if ((found=StringUtils.equalsIgnoreCase(ident.getEmail(), email)))
												break;
										}
									}
									
									if (!found) sendAddContactMessage(ia.getAddress(), ia.getPersonal());
									break;
								}
							}
						}
						
					} catch (Exception xexc) {
						Service.logger.error("Exception",xexc);
					} finally {
						JsAuditMessageInfo jsAudit = iaRecipientsToAuditInfo(iaTos, iaCcs, iaBccs);
						if (StringUtils.isNotEmpty(from)) jsAudit.setFrom(from);
						if (ifrom != null && StringUtils.isNotEmpty(ifrom.getDisplayName())) jsAudit.setFromDN(ifrom.getDisplayName());
						if (StringUtils.isNotEmpty(jsmsg.subject)) jsAudit.setSubject(jsmsg.subject);
						jsAudit.setFolder(jsmsg.folder);
						if (mailManager.isAuditEnabled() && StringUtils.isNotEmpty(jsmsg.inreplyto)) {
							mailManager.auditLogWrite(
								MailManager.AuditContext.MAIL,
								MailManager.AuditAction.REPLY, 
								jsmsg.inreplyto, 
								JsonResult.gson(false).toJson(jsAudit)
							);
						}
						
						try {
							foundfolder=flagAnsweredMessage(account,jsmsg.replyfolder,jsmsg.inreplyto,jsmsg.origuid);
						} catch (Exception xexc) {
							Service.logger.error("Exception",xexc);
						}
					}
				}
			}
			
			if (sendExc!=null) {
				if (!sendExc.messageSent) {
					json=new JsonResult(false,sendExc.getMessage());
				} else {
					if (!sendExc.messageSaved)
						environment.notify(
							new SentMessageNotSavedSM(sendExc.exception)
						);
					else
						environment.notify(
							new SharedSentMessageSavedOnMainSM(sendExc.exception)
						);
				}
			}
			
			if (json==null)
				json=new JsonResult()
						.set("foundfolder",foundfolder)
						.set("saved", Boolean.FALSE);
			
			
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			Throwable cause = exc.getCause();
			String msg = cause != null ? cause.getMessage() : exc.getMessage();
                        json=new JsonResult(false, msg);
		}
                json.printTo(out);
	}
	
	private JsAuditMessageInfo iaRecipientsToAuditInfo(List<InternetAddress> iaTos, List<InternetAddress> iaCcs, List<InternetAddress> iaBccs) {
		ArrayList<String> tos = new ArrayList<>();
		ArrayList<String> tosDN = new ArrayList<>();
		ArrayList<String> ccs = new ArrayList<>();
		ArrayList<String> ccsDN = new ArrayList<>();
		ArrayList<String> bccs = new ArrayList<>();
		ArrayList<String> bccsDN = new ArrayList<>();
		
		JsAuditMessageInfo jsAudit = new JsAuditMessageInfo();
		
		for (InternetAddress iarec : iaTos) {
			tos.add(iarec.getAddress());
			tosDN.add(iarec.getPersonal());
		}
		for (InternetAddress iarec : iaCcs) {
			ccs.add(iarec.getAddress());
			ccsDN.add(iarec.getPersonal());
		}
		for (InternetAddress iarec : iaBccs) {
			bccs.add(iarec.getAddress());
			bccsDN.add(iarec.getPersonal());
		}
		
		if (!tos.isEmpty()) jsAudit.setTos(tos);
		if (!tosDN.isEmpty()) jsAudit.setTosDN(tosDN);
		if (!ccs.isEmpty()) jsAudit.setCcs(ccs);
		if (!ccsDN.isEmpty()) jsAudit.setCcs(ccsDN);
		if (!bccs.isEmpty()) jsAudit.setBccs(bccs);
		if (!bccsDN.isEmpty()) jsAudit.setBccsDN(bccsDN);
		
		return jsAudit;
	}
	
	private void writeAuditCreateMessage(SimpleMessage msg, Message newmsg, String folder, Boolean scheduled) {
		String messageId = null;
		try {
			messageId = getMessageID(newmsg);
		} catch (MessagingException idExc) {
			Service.logger.error("Error getting message ID", idExc);
		}
		
		try {
			Address tos[] = newmsg.getRecipients(RecipientType.TO);
			ArrayList<String> listTos = new ArrayList<>();
			ArrayList<String> listTosDN = new ArrayList<>();
			if (tos != null) {
				for (Address to : tos) {
					InternetAddress ia = (InternetAddress) to;
					listTos.add(ia.getAddress());
					listTosDN.add(ia.getPersonal());
				}
			}

			Address ccs[] = newmsg.getRecipients(RecipientType.CC);
			ArrayList<String> listCcs = new ArrayList<>();
			ArrayList<String> listCcsDN = new ArrayList<>();
			if (ccs != null) {
				for (Address cc : ccs) {
					InternetAddress ia = (InternetAddress) cc;
					listCcs.add(ia.getAddress());
					listCcsDN.add(ia.getPersonal());
				}
			}

			Address bccs[] = newmsg.getRecipients(RecipientType.BCC);
			ArrayList<String> listBccs = new ArrayList<>();
			ArrayList<String> listBccsDN = new ArrayList<>();
			if (bccs != null) {
				for (Address bcc : bccs) {
					InternetAddress ia = (InternetAddress) bcc;
					listBccs.add(ia.getAddress());
					listBccsDN.add(ia.getPersonal());
				}
			}

			JsAuditMessageInfo jsAudit = new JsAuditMessageInfo();
			Identity ident = msg.getFrom();
			if (StringUtils.isNotEmpty(ident.getEmail())) jsAudit.setFrom(ident.getEmail());
			if (StringUtils.isNotEmpty(ident.getDisplayName())) jsAudit.setFromDN(ident.getDisplayName());
			if (!listTos.isEmpty()) jsAudit.setTos(listTos);
			if (!listTosDN.isEmpty()) jsAudit.setTosDN(listTosDN);
			if (!listCcs.isEmpty()) jsAudit.setCcs(listCcs);
			if (!listCcsDN.isEmpty()) jsAudit.setCcsDN(listCcsDN);
			if (!listBccs.isEmpty()) jsAudit.setBccs(listBccs);
			if (!listBccsDN.isEmpty()) jsAudit.setBccsDN(listBccsDN);
			jsAudit.setFolder(folder);
			if (StringUtils.isNotEmpty(msg.getForwardedFrom())) jsAudit.setForwardedFrom(msg.getForwardedFrom());
			if (StringUtils.isNotEmpty(msg.getInReplyTo())) jsAudit.setInReplyTo(msg.getInReplyTo());
			if (StringUtils.isNotEmpty(newmsg.getSubject())) jsAudit.setSubject(newmsg.getSubject());
			if (scheduled != null) jsAudit.setScheduled(scheduled);

			mailManager.auditLogWrite(
				MailManager.AuditContext.MAIL,
				MailManager.AuditAction.CREATE, 
				messageId,
				JsonResult.gson(false).toJson(jsAudit)
			);
		} catch(MessagingException messEx) {
			Service.logger.error("Exception", messEx);
		}
		
	}
	
	private void writeAuditCreateMessage(MimeMessage msg, String folder) {
		String messageId = null;
		try {
			messageId = getMessageID(msg);
		} catch (MessagingException idExc) {
			Service.logger.error("Error getting message ID", idExc);
		}
		
		try {
			Address tos[] = msg.getRecipients(RecipientType.TO);
			ArrayList<String> listTos = new ArrayList<>();
			ArrayList<String> listTosDN = new ArrayList<>();
			if (tos != null) {
				for (Address to : tos) {
					InternetAddress ia = (InternetAddress) to;
					listTos.add(ia.getAddress());
					listTosDN.add(ia.getPersonal());
				}
			}

			Address ccs[] = msg.getRecipients(RecipientType.CC);
			ArrayList<String> listCcs = new ArrayList<>();
			ArrayList<String> listCcsDN = new ArrayList<>();
			if (ccs != null) {
				for (Address cc : ccs) {
					InternetAddress ia = (InternetAddress) cc;
					listCcs.add(ia.getAddress());
					listCcsDN.add(ia.getPersonal());
				}
			}

			Address bccs[] = msg.getRecipients(RecipientType.BCC);
			ArrayList<String> listBccs = new ArrayList<>();
			ArrayList<String> listBccsDN = new ArrayList<>();
			if (bccs != null) {
				for (Address bcc : bccs) {
					InternetAddress ia = (InternetAddress) bcc;
					listBccs.add(ia.getAddress());
					listBccsDN.add(ia.getPersonal());
				}
			}

			JsAuditMessageInfo jsAudit = new JsAuditMessageInfo();
			Address from = msg.getFrom()[0];
			InternetAddress iaFrom = null;
			if (from instanceof InternetAddress) iaFrom = (InternetAddress)from;
			if (iaFrom != null) {
				if (StringUtils.isNotEmpty(iaFrom.getAddress())) jsAudit.setFrom(iaFrom.getAddress());
				if (StringUtils.isNotEmpty(iaFrom.getPersonal())) jsAudit.setFromDN(iaFrom.getPersonal());
			}
			if (!listTos.isEmpty()) jsAudit.setTos(listTos);
			if (!listTosDN.isEmpty()) jsAudit.setTosDN(listTosDN);
			if (!listCcs.isEmpty()) jsAudit.setCcs(listCcs);
			if (!listCcsDN.isEmpty()) jsAudit.setCcsDN(listCcsDN);
			if (!listBccs.isEmpty()) jsAudit.setBccs(listBccs);
			if (!listBccsDN.isEmpty()) jsAudit.setBccsDN(listBccsDN);
			jsAudit.setFolder(folder);
			if (StringUtils.isNotEmpty(msg.getSubject())) jsAudit.setSubject(msg.getSubject());
			
			mailManager.auditLogWrite(
				MailManager.AuditContext.MAIL,
				MailManager.AuditAction.CREATE, 
				messageId,
				JsonResult.gson(false).toJson(jsAudit)
			);
		} catch(MessagingException messEx) {
			Service.logger.error("Exception", messEx);
		}
		
	}
	
	private void sendAddContactMessage(String email, String personal) {
		this.environment.notify(new AddContactMessage(email, personal)
		);
	}
	
    private String flagAnsweredMessage(MailAccount account, String replyfolder, String id, long origuid) throws MessagingException {
		String foundfolder = null;
		if (replyfolder != null) {
			if (_flagAnsweredMessage(account,replyfolder,origuid)) foundfolder=replyfolder;
		}
		if (foundfolder == null) {
			SonicleIMAPFolder.RecursiveSearchResult rsr = account.recursiveSearchByMessageID("",id);
			if (rsr != null) {
				_flagAnsweredMessage(account,rsr.foldername, rsr.uid);
				foundfolder = rsr.foldername;
			}
		}
		return foundfolder;
	}
	
	private boolean _flagAnsweredMessage(MailAccount account, String foldername, long uid) throws MessagingException {
		Message msg = null;
		SonicleIMAPFolder sifolder = (SonicleIMAPFolder) account.getFolder(foldername);
		sifolder.open(Folder.READ_WRITE);
		msg = sifolder.getMessageByUID(uid);
		boolean found = msg != null;
		if (found) {
			msg.setFlags(FolderCache.repliedFlags, true);
		}
		sifolder.close(true);
		return found;
	}
	
    private String flagForwardedMessage(MailAccount account, String forwardedfolder, String id, long origuid) throws MessagingException {
		String foundfolder = null;
		if (forwardedfolder != null) {
			if (_flagForwardedMessage(account,forwardedfolder,origuid)) foundfolder=forwardedfolder;
		}
		if (foundfolder == null) {
			SonicleIMAPFolder.RecursiveSearchResult rsr = account.recursiveSearchByMessageID("",id);
			if (rsr != null) {
				_flagForwardedMessage(account,rsr.foldername, rsr.uid);
				foundfolder = rsr.foldername;
			}
		}
		return foundfolder;
	}
	
	private boolean _flagForwardedMessage(MailAccount account, String foldername, long uid) throws MessagingException {
		Message msg = null;
		SonicleIMAPFolder sifolder = (SonicleIMAPFolder) account.getFolder(foldername);
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
		MailAccount account=getAccount(request);
		JsonResult json = null;
		CoreManager coreMgr=WT.getCoreManager();
		try {
			account.checkStoreConnected();
            Payload<MapItem, JsMessage> pl=ServletUtils.getPayload(request,JsMessage.class);
            JsMessage jsmsg=pl.data;
            long msgId=ServletUtils.getLongParameter(request, "msgId", true);
			//String attachments[] = request.getParameterValues("attachments");
			String savefolder = ServletUtils.getStringParameter(request, "savefolder", false);
			//if (attachments == null) {
			//	attachments = new String[0];
			//}
			SimpleMessage msg = prepareMessage(jsmsg,msgId,true,false);
			account.checkStoreConnected();
			FolderCache fc = null;
			if (savefolder == null) {
				fc = determineDraftFolder(account,msg);
			} else {
				fc = account.getFolderCache(savefolder);
			}
			Exception exc = saveMessage(msg, jsmsg.attachments, fc, true);
			if (exc == null) {
				coreMgr.deleteMyAutosaveData(getEnv().getClientTrackingID(), SERVICE_ID, "newmail", ""+msgId);
				
				deleteAutosavedDraft(account,msgId);
				
				if (pl.data.origuid>0 && pl.data.folder!=null && fc.getFolder().getFullName().equals(pl.data.folder)) {
					fc.deleteMessage(pl.data.origuid);
				}
				
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
	
	private FolderCache determineDraftFolder(MailAccount account, SimpleMessage msg) throws MessagingException {
		String draftsfolder=account.getFolderDrafts();
		Identity ident = msg.getFrom();
		if (ident != null ) {
			String mainfolder=ident.getMainFolder();
			if (mainfolder != null && mainfolder.trim().length() > 0) {
				String newdraftsfolder = mainfolder + account.getFolderSeparator() + account.getLastFolderName(draftsfolder);
				try {
					Folder folder = account.getFolder(newdraftsfolder);
					if (folder.exists()) {
						draftsfolder = newdraftsfolder;
					}
				} catch (MessagingException exc) {
					logger.error("Error on identity {}/{} Drafts Folder", environment.getProfile().getUserId(), ident.getEmail(), exc);
				}
			}
		}
		FolderCache fc = account.getFolderCache(draftsfolder);
		return fc;
	}
	
	public void processScheduleMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		JsonResult json = null;
		CoreManager coreMgr=WT.getCoreManager();
		try {
			MailAccount account=getAccount(request);
			account.checkStoreConnected();
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
			account.checkStoreConnected();
			FolderCache fc = null;
			if (savefolder == null) {
				fc = determineDraftFolder(account,msg);
			} else {
				fc = account.getFolderCache(savefolder);
			}
			Exception exc = scheduleMessage(msg, jsmsg.attachments, fc, scheddate, schedtime, schednotify);
			if (exc == null) {
				coreMgr.deleteMyAutosaveData(getEnv().getClientTrackingID(), SERVICE_ID, "newmail", ""+msgId);
				
				deleteAutosavedDraft(account,msgId);
				
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
		JsonResult json;
		CoreManager coreMgr=WT.getCoreManager();
		try {
			MailAccount account=getAccount(request);
			long msgId=ServletUtils.getLongParameter(request, "msgId", true);
			deleteCloudAttachments(msgId);
			coreMgr.deleteMyAutosaveData(getEnv().getClientTrackingID(), SERVICE_ID, "newmail", ""+msgId);
			deleteAutosavedDraft(account,msgId);
			json=new JsonResult();
		} catch(Exception exc) {
			Service.logger.error("Exception",exc);
            json=new JsonResult(false, exc.getMessage());
		}
        json.printTo(out);
	}

	private SimpleMessage prepareMessage(JsMessage jsmsg, long msgId, boolean save, boolean isFax) throws Exception {
		return prepareMessage(jsmsg,msgId,save,isFax,false);
	}
	
	private SimpleMessage prepareMessage(JsMessage jsmsg, long msgId, boolean save, boolean isFax, boolean skipInvalidEmails) throws Exception {
		PrivateEnvironment env = environment;
		UserProfile profile = env.getProfile();
		//expand multiple addresses
		ArrayList<String> aemails = new ArrayList<>();
		ArrayList<String> artypes = new ArrayList<>();
		if (jsmsg.torecipients!=null)
			for (JsRecipient jsrcpt: jsmsg.torecipients) {
				String emails[]=StringUtils.split(jsrcpt.email,';');
				for(String email: emails) {
					aemails.add(email);
					artypes.add("to");
				}
			}
		if (jsmsg.ccrecipients!=null)
			for (JsRecipient jsrcpt: jsmsg.ccrecipients) {
				String emails[]=StringUtils.split(jsrcpt.email,';');
				for(String email: emails) {
					aemails.add(email);
					artypes.add("cc");
				}
			}
		if (jsmsg.bccrecipients!=null)
			for (JsRecipient jsrcpt: jsmsg.bccrecipients) {
				String emails[]=StringUtils.split(jsrcpt.email,';');
				for(String email: emails) {
					aemails.add(email);
					artypes.add("bcc");
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
		
		ArrayList<String> to = new ArrayList<>();
		ArrayList<String> cc = new ArrayList<>();
		ArrayList<String> bcc = new ArrayList<>();
		CoreManager core=WT.getCoreManager();
		for (int i = 0; i < emails.length; ++i) {
			InternetAddress iaEmail = InternetAddressUtils.toInternetAddress(emails[i]);
			if (iaEmail == null) {
				if (skipInvalidEmails) continue;
				throw new AddressException(lookupResource(MailLocaleKey.ADDRESS_ERROR) + " : " + emails[i]);
			}
			
			boolean skipEmail = false;
			if (!StringUtils.contains(iaEmail.getAddress(), "@")) {
				if (isFax && StringUtils.isNumeric(iaEmail.getAddress())) {
					String faxpattern = getEnv().getCoreServiceSettings().getFaxPattern();
					String newAddress = faxpattern.replace("{number}", iaEmail.getAddress()).replace("{username}", profile.getUserId());
					iaEmail = InternetAddressUtils.toInternetAddress(newAddress, null);
					
				} else {
					throw new AddressException(lookupResource(MailLocaleKey.ADDRESS_ERROR) + " : " + InternetAddressUtils.toFullAddress(iaEmail));
				}
				
			} else {
				String dom = StringUtils.substringAfterLast(iaEmail.getAddress(), "@");
				if (environment.getSession().isServiceAllowed(dom)) {
					List<Recipient> rcpts = core.expandVirtualProviderRecipient(iaEmail.getAddress());
					if (rcpts.isEmpty() && ContactsUtils.isListVirtualRecipient(iaEmail)) {
						throw new MessagingException("List '" + iaEmail.getPersonal() + "' doesn't exist or is empty");
					}
					
					for (Recipient rcpt: rcpts) {
						InternetAddress iaRcpt = InternetAddressUtils.toInternetAddress(rcpt.getAddress(), rcpt.getPersonal());
						if (iaRcpt == null) {
							if (skipInvalidEmails) continue;
							throw new AddressException(lookupResource(MailLocaleKey.ADDRESS_ERROR) + " : " + rcpt.getAddress());
						}
						
						if ("to".equals(rtypes[i])) {
							if (Recipient.Type.TO.equals(rcpt.getType())) {
								to.add(toSimpleMessageRecipient(iaRcpt));
							} else if (Recipient.Type.CC.equals(rcpt.getType())) {
								cc.add(toSimpleMessageRecipient(iaRcpt));
							} else if (Recipient.Type.BCC.equals(rcpt.getType())) {
								bcc.add(toSimpleMessageRecipient(iaRcpt));
							}
						} else if ("cc".equals(rtypes[i])) {
							cc.add(toSimpleMessageRecipient(iaRcpt));
						} else if ("bcc".equals(rtypes[i])) {
							bcc.add(toSimpleMessageRecipient(iaRcpt));
						}
						skipEmail = true;
					}
				}
			}
			
			if (!skipEmail) {
				if ("to".equals(rtypes[i])) {
					to.add(toSimpleMessageRecipient(iaEmail));
				} else if ("cc".equals(rtypes[i])) {
					cc.add(toSimpleMessageRecipient(iaEmail));
				} else if ("bcc".equals(rtypes[i])) {
					bcc.add(toSimpleMessageRecipient(iaEmail));
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
		msg.setTo(StringUtils.join(to, "; "));
		msg.setCc(StringUtils.join(cc, "; "));
		msg.setBcc(StringUtils.join(bcc, "; "));
		msg.setSubject(jsmsg.subject);
		
        //TODO: fax coverpage - dismissed
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
                String pattern1=RegexUtils.escapeRegexSpecialChars("service-request?csrf="+getEnv().getCSRFToken()+"&amp;service="+SERVICE_ID+"&amp;action=PreviewAttachment&amp;nowriter=true&amp;uploadId=");
                String pattern2=RegexUtils.escapeRegexSpecialChars("&amp;cid=");
                content=StringUtils.replacePattern(content, pattern1+".{39}"+pattern2, "cid:");
                pattern1=RegexUtils.escapeRegexSpecialChars("service-request?csrf="+getEnv().getCSRFToken()+"&service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&uploadId=");
                pattern2=RegexUtils.escapeRegexSpecialChars("&cid=");
                content=StringUtils.replacePattern(content, pattern1+".{39}"+pattern2, "cid:");
				
                //URLs
                pattern1=RegexUtils.escapeRegexSpecialChars("service-request?csrf="+getEnv().getCSRFToken()+"&amp;service="+SERVICE_ID+"&amp;action=PreviewAttachment&amp;nowriter=true&amp;uploadId=");
                pattern2=RegexUtils.escapeRegexSpecialChars("&amp;url=");
                content=StringUtils.replacePattern(content, pattern1+".{39}"+pattern2, "");
                pattern1=RegexUtils.escapeRegexSpecialChars("service-request?csrf="+getEnv().getCSRFToken()+"&service="+SERVICE_ID+"&action=PreviewAttachment&nowriter=true&uploadId=");
                pattern2=RegexUtils.escapeRegexSpecialChars("&url=");
                content=StringUtils.replacePattern(content, pattern1+".{39}"+pattern2, "");
				
				//My resources as cids?
				if (!save && ss.isPublicResourceLinksAsInlineAttachments()) {
					// Replaces every src URL pointing a public image with a cid, tranforming URL to a direct inline attachments
					String uploadTag = ""+msgId;
					String resourcesBaseUrl = URIUtils.concat(getEnv().getCoreServiceSettings().getPublicBaseUrl(), ResourceRequest.URL);
					Pattern pattSrc = Pattern.compile("(?:\"|\')(" + RegexUtils.escapeRegexSpecialChars(resourcesBaseUrl) + "(.+?))(?:\"|\')");
					Matcher maSrc = pattSrc.matcher(content);
					
					ArrayList<JsAttachment> rescids = new ArrayList<>();
					StringBuffer sb = new StringBuffer(content.length());
					while (maSrc.find()) {
						String imageUrl = maSrc.group(1);
						URI uri = new URI(imageUrl);
						
						logger.debug("Downloading resource as uploaded file from [{}]", imageUrl);
						HttpClient httpCli = null;
						try {
							httpCli = HttpClientUtils.createBasicHttpClient(HttpClientUtils.configureSSLAcceptAll(), uri);
							String filename = PathUtils.getFileName(uri.getPath());
							UploadedFile upf = addAsUploadedFile(uploadTag, filename, ServletHelper.guessMediaType(filename), HttpClientUtils.getContent(httpCli, uri));
							rescids.add(new JsAttachment(upf.getUploadId(), filename, upf.getUploadId(), true, upf.getSize()));
							maSrc.appendReplacement(sb, "\"cid:" + upf.getUploadId() + "\"");
							
						} catch(IOException ex) {
							throw new WTException(ex, "Unable to download resource file [{}]", uri);
						} finally {
							HttpClientUtils.closeQuietly(httpCli);
						}
					}
					maSrc.appendTail(sb);
					content = sb.toString();

					//add new resource cids as attachments
					if (rescids.size()>0) {
						if (jsmsg.attachments==null) jsmsg.attachments=new ArrayList<>();
						jsmsg.attachments.addAll(rescids);
					}
				}
				
				
				String textcontent = StringEscapeUtils.unescapeHtml4(MailUtils.HtmlToText_convert(content));
				String htmlcontent = MailUtils.htmlescapefixsource(content).trim();
				if (htmlcontent.length()<6 || !htmlcontent.substring(0,6).toLowerCase().equals("<html>")) {
					htmlcontent="<html><header></header><body>"+htmlcontent+"</body></html>";
				}
				
				if (jsmsg.attachments!=null) {
					//remove unreferenced cids attachments
					//cycle through a copy of the list to delete elements from the original list
					ArrayList<JsAttachment> atts = new ArrayList<>();
					atts.addAll(jsmsg.attachments);
					for(JsAttachment jsa: atts) {
						UploadedFile upfile = getUploadedFile(jsa.uploadId);
						String ctype = upfile.getMediaType();
						boolean inline = jsa.inline && isInlineableMime(ctype);
						if (inline && !StringUtils.isEmpty(jsa.cid) && htmlcontent.indexOf("src=\"cid:"+jsa.cid)<0)
							jsmsg.attachments.remove(jsa);
					}
				}
				
				msg.setContent(htmlcontent, textcontent, "text/html");
			} else {
				msg.setContent(jsmsg.content, null, "text/"+jsmsg.format);
			}
			
		}
		return msg;
	}
	
	private String toSimpleMessageRecipient(InternetAddress ia) {
		return StringUtils.replace(InternetAddressUtils.toFullAddress(ia), ";", "");
	}
	
	public void processAttachFromMail(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailAccount account=getAccount(request);
			account.checkStoreConnected();
			
			String tag = request.getParameter("tag");
			String pfoldername = request.getParameter("folder");
			String puidmessage = request.getParameter("idmessage");
			String pidattach = request.getParameter("idattach");
			
			FolderCache mcache = account.getFolderCache(pfoldername);
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
			environment.getSession().addUploadedFile(uploadedFile);
			
			MapItem data = new MapItem(); // Empty response data
			data.add("uploadId", uploadedFile.getUploadId());
			data.add("name", uploadedFile.getFilename());
			data.add("size", uploadedFile.getSize());
			data.add("editable", isFileEditableInDocEditor(filename));
			new JsonResult(data).printTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false, exc.getMessage()).printTo(out);
		}
	}
	
	public void processAttachFromMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailAccount account=getAccount(request);
			account.checkStoreConnected();
			
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
				FolderCache mcache = account.getFolderCache(foldername);
				
				Message msg=mcache.getMessage(uid);
				File file = WT.createTempFile();
				FileOutputStream fos=new FileOutputStream(file);
				msg.writeTo(fos);
				fos.close();
				long filesize=file.length();
				String filename=msg.getSubject()+".eml";
			
				WebTopSession.UploadedFile uploadedFile = new WebTopSession.UploadedFile(false, this.SERVICE_ID, file.getName(), tag, filename, filesize, ctype);
				environment.getSession().addUploadedFile(uploadedFile);
				ufiles.add(uploadedFile);
			}			
			
			MapItemList data=new MapItemList();
			for(WebTopSession.UploadedFile ufile: ufiles) {
				MapItem mi = new MapItem();
				mi.add("uploadId", ufile.getUploadId());
				mi.add("name", ufile.getFilename());
				mi.add("size", ufile.getSize());
				mi.add("editable", isFileEditableInDocEditor(ufile.getFilename()));
				data.add(mi);
			}
			new JsonResult(data).printTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false, exc.getMessage()).printTo(out);
		}
	}
	
	public void processAttachFromCloud(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			int storeId = ServletUtils.getIntParameter(request, "storeId", true);
			String path = ServletUtils.getStringParameter(request, "path", true);
			String tag = ServletUtils.getStringParameter(request, "tag", true);
			
			WebTopSession.UploadedFile uploadedFile = null;
			FileObject fo = vfsmanager.getStoreFile(storeId, path);
			if (fo == null) throw new WTException("Unable to get file [{}, {}]", storeId, path);
			InputStream is = null;
			try {
				is = fo.getContent().getInputStream();
				String name = fo.getName().getBaseName();
				String mediaType = ServletHelper.guessMediaType(name, true);
				uploadedFile = addAsUploadedFile(tag, name, mediaType, is);
			} finally {
				IOUtils.closeQuietly(is);
			}
			if (uploadedFile == null) throw new WTException("Unable to prepare uploaded file");
			
			MapItem data = new MapItem();
			data.add("uploadId", uploadedFile.getUploadId());
			data.add("name", uploadedFile.getFilename());
			data.add("size", uploadedFile.getSize());
			new JsonResult(data).printTo(out);
			
		} catch (FileSystemException | WTException ex) {
			Service.logger.error("Exception",ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		} catch (Exception ex) {
			Service.logger.error("Exception",ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public void processSaveFileToCloud(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailAccount account=getAccount(request);
			String path = ServletUtils.getStringParameter(request, "path", true);
			int storeId = ServletUtils.getIntParameter(request, "storeId", true);
			String folder = ServletUtils.getStringParameter(request, "folder", true);
			int idAttach = ServletUtils.getIntParameter(request, "idAttach", true);
			int idMessage = ServletUtils.getIntParameter(request, "idMessage", true);
			
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(folder);
			Message m = mcache.getMessage(idMessage);
			HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
			Part part = mailData.getAttachmentPart(idAttach);
			String fileName = part.getFileName();
			InputStream is = part.getInputStream();
			
			vfsmanager.addStoreFileFromStream(storeId, path, fileName, is);
			
			MapItem data = new MapItem();
			data.add("success", true);
			new JsonResult(data).printTo(out);
		}
		catch(Exception ex) {
			Service.logger.error("Exception", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
		
	}

	public void processCopyAttachment(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailAccount fromaccount=getAccount(request.getParameter("fromaccount"));
			MailAccount toaccount=getAccount(request.getParameter("toaccount"));
			fromaccount.checkStoreConnected();
			toaccount.checkStoreConnected();
			UserProfile profile=environment.getProfile();
			Locale locale=profile.getLocale();
			
			String pfromfolder = request.getParameter("fromfolder");
			String ptofolder = request.getParameter("tofolder");
			String puidmessage = request.getParameter("idmessage");
			String pidattach = request.getParameter("idattach");
			
			FolderCache frommcache = fromaccount.getFolderCache(pfromfolder);
			FolderCache tomcache = toaccount.getFolderCache(ptofolder);
			long uidmessage = Long.parseLong(puidmessage);
			Message m = frommcache.getMessage(uidmessage);
			
			HTMLMailData mailData = frommcache.getMailData((MimeMessage) m);
			Object content = mailData.getAttachmentPart(Integer.parseInt(pidattach)).getContent();
			
			// We can copy attachments only if the content is an eml. If it is
			// explicit simply treat it as message, otherwise try to decode the
			// stream as a mime message. If an error is thrown during parse, it 
			// means that the stream is reconducible to a valid mime-message.
			MimeMessage	msgContent = null;
			if (content instanceof MimeMessage) {
				msgContent = new MimeMessage((MimeMessage)content);
			} else if(content instanceof IMAPInputStream) {
				try {
					msgContent = new MimeMessage(fromaccount.getMailSession(), (IMAPInputStream)content);
				} catch(MessagingException ex1) {
					logger.debug("Stream cannot be interpreted as MimeMessage", ex1);
				}
			}
			
			if (msgContent != null) {
				msgContent.setFlag(Flags.Flag.SEEN, true);
				tomcache.appendMessage(msgContent);
				if (mailManager.isAuditEnabled()) writeAuditCreateMessage(msgContent, ptofolder);
				new JsonResult().printTo(out);
			} else {
				new JsonResult(false, lookupResource(locale, MailLocaleKey.ERROR_ATTACHMENT_TYPE_NOT_SUPPORTED)).printTo(out);
			}
			
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false, exc.getMessage()).printTo(out);
		}
	}
	
	public void processSendReceipt(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		boolean send = ServletUtils.getBooleanParameter(request, "send", true);
		//if not send, it will not send but save info not to send anymore
		
		String messageid = request.getParameter("messageid");
		String subject = request.getParameter("subject");
		//String from = request.getParameter("from");
		int identityId = Integer.parseInt(request.getParameter("identityId"));
		String to = request.getParameter("to");
		//String folder = request.getParameter("folder");
		//String sout = "";
		Identity ident = mprofile.getIdentity(identityId);
		String from = ident.getDisplayName()+" <"+ident.getEmail()+">";
		
		String bodyUserLang = request.getParameter("bodyuserlang");
		String body = "";
		
		if (!bodyUserLang.equals("${receipt.message}")) {
			body = bodyUserLang + ".\n\n";
		}
		
		if (!cus.getLanguageTag().equals("en_EN")) {
			body += "Your message sent to " + from + " with subject [" + subject + "] has been read.\n\n";
		}
		
		try {
			account.checkStoreConnected();
			Exception exc = null;
			if (send) exc = sendReceipt(ident, from, to, subject, body);
			if (exc == null) {
				CoreManager coreMgr=WT.getCoreManager();
				coreMgr.addServiceStoreEntry(SERVICE_ID, "receipt", messageid, "sent");
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

	public void processMarkEmailAsTrusted(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String email = request.getParameter("email");
		if (!StringUtils.isEmpty(email)) {
			CoreManager coreMgr=WT.getCoreManager();
			coreMgr.autoLearnInternetRecipient(email);
		}
		new JsonResult().printTo(out);
	}
	
	public void processPortletMail(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsPreviewMessage> items = new ArrayList<>();
		
		try {
			MailAccount account=getAccount(request);
			String query = ServletUtils.getStringParameter(request, "query", null);
			int visibleRows=0;
			int maxVisibleRows=20;
			
			if (query == null) {
				String folderId = account.getInboxFolderFullName();
				FolderCache fc=account.getFolderCache(folderId);
				ImapQuery iq = new ImapQuery(new FlagTerm(new Flags(Flags.Flag.SEEN), false),false);
				Message msgs[]=fc.getMessages(FolderCache.SORT_BY_DATE,false,true,-1,true,false, iq);
				if (msgs!=null) fc.fetch(msgs, getMessageFetchProfile(),0,50);
				else msgs=new Message[0];
				
				int n=0;
				for (Message msg: msgs) {
					SonicleIMAPMessage simsg=(SonicleIMAPMessage)msg;
					
					InternetAddress iafrom=null;
					Address vfrom[]=msg.getFrom();
					if (vfrom!=null && vfrom.length>0) {
						Address afrom=vfrom[0];
						if (afrom instanceof InternetAddress) {
							iafrom=(InternetAddress) afrom;
						}
					}

					Address[] rcpts=msg.getRecipients(Message.RecipientType.TO);
					ArrayList<InternetAddress> tos=new ArrayList<>();
					if (rcpts!=null)
						for(Address ato: rcpts) {
							if (ato instanceof InternetAddress) {
								InternetAddress iato=(InternetAddress) ato;
								tos.add(iato);
							}
						}
						
					String msgtext = "";
					if (visibleRows<maxVisibleRows) {
						msgtext=MailUtils.peekText(simsg);
						if (msgtext==null) msgtext="";
						else {
							msgtext=msgtext.trim();
							if (msgtext.length()>100) msgtext=msgtext.substring(0,100);
						}
						++visibleRows;
					}
								
					String from=iafrom!=null?(iafrom.getPersonal()!=null?iafrom.getPersonal():iafrom.getAddress()):"";
					String to="";
					if (tos.size()>0) {
						boolean first=true;
						for(InternetAddress iato: tos) {
							if (!first) to+="; ";
							to+=(iato.getPersonal()!=null?iato.getPersonal():iato.getAddress());
							first=false;
						}
					}
					
					JsPreviewMessage jsmsg=new JsPreviewMessage(
						simsg.getUID(), 
						folderId,
						getInternationalFolderName(account.getFolderCache(folderId)),
						simsg.getSubject(), 
						from,
						to,
						msg.getReceivedDate(),
						msgtext
					);
					items.add(jsmsg);
					
					++n;
					if (n>=50) break;
				}
			} else {
			}
			
			new JsonResult(items).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in PortletMail", ex);
			new JsonResult(false, "Error").printTo(out);	
		}
	}
	
	public void processUploadBlobInfo(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String tag = ServletUtils.getStringParameter(request, "tag", true);
			Payload<MapItem, BlobInfoPayload> payload = ServletUtils.getPayload(request, BlobInfoPayload.class);
			
			UploadedFile upfile = addAsUploadedFile(tag, payload.data);
			new JsonResult(upfile.getUploadId()).printTo(out);
			
		} catch(Throwable t) {
			logger.error("Error in UploadBlobInfo", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processManageQuickParts(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				List<JsQuickPart> items = JsQuickPart.toSortedList(us.getMessageQuickParts());
				new JsonResult(items, items.size()).printTo(out);

			} else if (crud.equals(Crud.CREATE)) {
				PayloadAsList<JsQuickPart.List> pl = ServletUtils.getPayloadAsList(request, JsQuickPart.List.class);
				List<JsQuickPart> items = new ArrayList<>();
				for (JsQuickPart jsqp : pl.data) {
					us.setMessageQuickPart(jsqp.name, jsqp.html);
					items.add(jsqp);
				}
				new JsonResult(items, items.size()).printTo(out);

			} else if (crud.equals(Crud.UPDATE)) {
				PayloadAsList<JsQuickPart.List> pl = ServletUtils.getPayloadAsList(request, JsQuickPart.List.class);
				for (JsQuickPart jsqp : pl.data) {
					us.setMessageQuickPart(jsqp.name, jsqp.html);
				}
				new JsonResult().printTo(out);

			} else if (crud.equals(Crud.DELETE)) {
				PayloadAsList<JsQuickPart.List> pl = ServletUtils.getPayloadAsList(request, JsQuickPart.List.class);
				for (JsQuickPart jsqp : pl.data) {
					us.deleteMessageQuickPart(jsqp.name);
				}
				new JsonResult().printTo(out);
			}

		} catch(Throwable t) {
			logger.error("Error in ManageQuickParts", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processTagMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String tagId = request.getParameter("tagId");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(fromfolder);
			if (!mf) {
				tagMessages(mcache, toLongs(uids), tagId);
			} else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    int ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = account.getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    tagMessages(mcache, iuids, tagId);
				}
			}
			new JsonResult().printTo(out);
		} catch (MessagingException exc) {
		   logger.error("Error managing tags", exc);
		   new JsonResult(false, lookupResource(MailLocaleKey.PERMISSION_DENIED)).printTo(out);
		}
	}	
	
	public void processUntagMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String tagId = request.getParameter("tagId");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(fromfolder);
			if (!mf) {
				untagMessages(mcache, toLongs(uids), tagId);
			} else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    int ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = account.getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    untagMessages(mcache, iuids, tagId);
				}
			}
			new JsonResult().printTo(out);
		} catch (MessagingException exc) {
		   logger.error("Error managing tags", exc);
		   new JsonResult(false, "Error managing tags").printTo(out);
		}
	}	
	
	public void processApplyMessagesTags(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String fromfolder = request.getParameter("fromfolder");
		String uids[] = request.getParameterValues("ids");
		String tagIds[] = request.getParameterValues("tagIds");
		String multifolder = request.getParameter("multifolder");
		boolean mf = multifolder != null && multifolder.equals("true");
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(fromfolder);
			if (!mf) {
				applyMessagesTags(mcache, toLongs(uids), tagIds);
			} else {
                long iuids[]=new long[1];
                for(String uid: uids) {
                    int ix=uid.indexOf("|");
                    fromfolder=uid.substring(0,ix);
                    uid=uid.substring(ix+1);
					mcache = account.getFolderCache(fromfolder);
                    iuids[0]=Long.parseLong(uid);
                    applyMessagesTags(mcache, iuids, tagIds);
				}
			}
			new JsonResult().printTo(out);
		} catch (MessagingException exc) {
		   logger.error("Error managing tags", exc);
		   new JsonResult(false, "Error managing tags").printTo(out);
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
	
	private String getSharedFolderName(MailAccount account, String mailUser, String folder) throws MessagingException {
		FolderCache folderCache = null;
		String sharedFolderName = null;
		String folderName = null;
		
		// Clear mailUser removing any domain info (ldap auth contains 
		// domain suffix), we don't want it!
		String user = StringUtils.split(mailUser, "@")[0];
		// INBOX is a fake name, it's equals to user's direct folder
		boolean isInbox=folder.equals("INBOX");
		
		FolderCache[] sharedCache = account.getSharedFoldersCache();
		for(FolderCache sharedFolder : sharedCache) {
			sharedFolderName = sharedFolder.getFolderName();
			folderCache = account.getFolderCache(sharedFolderName);
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


	public void processUploadToFolder (HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try{
			MailAccount account=getAccount(request);
			String currentFolder=request.getParameter("folder");
			String uploadId=request.getParameter("uploadId");
			
			UploadedFile upfile=getUploadedFile(uploadId);
			InputStream in = new FileInputStream(upfile.getFile());
			MimeMessage msgContent = new MimeMessage(account.getMailSession(), in);
			FolderCache tomcache = account.getFolderCache(currentFolder);
			msgContent.setFlag(Flags.Flag.SEEN, true);
			tomcache.appendMessage(msgContent);
			
			if (mailManager.isAuditEnabled()) writeAuditCreateMessage(msgContent, currentFolder);
			
			new JsonResult().printTo(out);
		} catch(Exception exc) {
			logger.debug("Cannot upload to folder",exc);
			new JsonResult("Cannot upload to folder", exc).printTo(out);
		}
	}	

	
	class MessagesInfo {
		Message messages[];
		long millis;
		private MessageListThread mlt;
		
		MessagesInfo(Message msgs[], MessageListThread mlt) {
			this.messages=msgs;
			this.millis=mlt.millis;
			this.mlt=mlt;
		}
		
		public boolean isPEC() {
			return mlt.isPec;
		}
		
/*		public boolean checkSkipPEC(Message xm) throws MessagingException {
			return mlt.checkSkipPEC(xm);
		}*/
	}
	
	private MessagesInfo listMessages(FolderCache mcache, String key, boolean refresh, SortGroupInfo sgi, long timestamp, ImapQuery iq) throws MessagingException {
		MessageListThread mlt = null;
		synchronized(mlThreads) {
			mlt = mlThreads.get(key);
			if (mlt == null || (mlt.lastRequest!=timestamp && refresh)) {
				//if (mlt!=null)
				//	System.out.println(page+": same time stamp ="+(mlt.lastRequest!=timestamp)+" - refresh = "+refresh);
				//else
				//	System.out.println(page+": mlt not found");
				mlt = new MessageListThread(mcache, sgi.sortby, sgi.sortascending, refresh, sgi.sortgroup, sgi.groupascending, sgi.threaded, iq);
				mlt.lastRequest = timestamp;
				mlThreads.put(key, mlt);
			}
			//else System.out.println(page+": reusing list thread");

			//remove old requests
			ArrayList<String> rkeys=null;
			for(String xkey: mlThreads.keySet()) {
				MessageListThread xmlt = mlThreads.get(xkey);
				//remove if older than one minute
				long age=timestamp - xmlt.lastRequest;
				if (xmlt!=null && age>60000) {
					if (rkeys==null) rkeys=new ArrayList<>();
					rkeys.add(xkey);
				}
			}
			if (rkeys!=null) {
				for(String xkey: rkeys) {
					//MessageListThread xmlt = mlThreads.get(xkey);
					//long age=timestamp - xmlt.lastRequest;
					//System.out.println("removing ["+xkey+"] - age: "+age);
					mlThreads.remove(xkey);
				}
			}

			//System.out.println("mlThreads size is now "+mlThreads.size());
		}

		Message xmsgs[]=null;
		synchronized (mlt.lock) {
			if (!mlt.started) {
				//System.out.println(page+": starting list thread");
				Thread t = new Thread(mlt);
				t.start();
			}
			if (!mlt.finished) {
				//System.out.println(page+": waiting list thread to finish");
				try {
					mlt.lock.wait();
				} catch (InterruptedException exc) {
					Service.logger.error("Exception",exc);
				}
				//mlThreads.remove(key);
			}
			//TODO: see if we can check first request from buffered store
			//if (mlt.lastRequest==timestamp) {
				//System.out.println(page+": got list thread result");
				xmsgs=mlt.msgs;
			//}
		}

		return new MessagesInfo(xmsgs,mlt);
	}
	
	class SortGroupInfo {
		int sortby;
		boolean sortascending;
		int sortgroup;
		boolean groupascending;
		boolean threaded;
		
		SortGroupInfo(int sortby, boolean sortascending, int sortgroup, boolean groupascending, boolean threaded) {
			this.sortby=sortby;
			this.sortascending=sortascending;
			this.sortgroup=sortgroup;
			this.groupascending=groupascending;
			this.threaded=threaded;
		}
	}
	
	private SortGroupInfo getSortGroupInfo(String psortfield, String psortdir, String group) {
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
		} else if (psortfield.equals("unread")) {
			sortby = MessageComparator.SORT_BY_SEEN;
		} else if (psortfield.equals("flag")) {
			sortby = MessageComparator.SORT_BY_FLAG;
		}
		
		int sortgroup = MessageComparator.SORT_BY_NONE;
		boolean groupascending = true;
		if (group.equals("messageid")) {
			sortgroup = MessageComparator.SORT_BY_MSGIDX;
		} else if (group.equals("gdate")) {
			sortgroup = MessageComparator.SORT_BY_DATE;
			groupascending = false;
		} else if (group.equals("priority")) {
			sortgroup = MessageComparator.SORT_BY_PRIORITY;
		} else if (group.equals("to")) {
			sortgroup = MessageComparator.SORT_BY_RCPT;
		} else if (group.equals("from")) {
			sortgroup = MessageComparator.SORT_BY_SENDER;
		} else if (group.equals("size")) {
			sortgroup = MessageComparator.SORT_BY_SIZE;
		} else if (group.equals("subject")) {
			sortgroup = MessageComparator.SORT_BY_SUBJECT;
		} else if (group.equals("status")) {
			sortgroup = MessageComparator.SORT_BY_STATUS;
		} else if (group.equals("flag")) {
			sortgroup = MessageComparator.SORT_BY_FLAG;
		}
		
		boolean threaded=group.equals("threadId");
		
		if (threaded && (!psortfield.equals("date")||!psortdir.equals("DESC"))) threaded=false;
		
		return new SortGroupInfo(sortby,psortdir.equals("ASC"),sortgroup,groupascending,threaded);
	}
	
	public void processListMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile profile = environment.getProfile();
		Locale locale = profile.getLocale();
		java.util.Calendar cal = java.util.Calendar.getInstance(locale);
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		//String psortfield = request.getParameter("sort");
		//String psortdir = request.getParameter("dir");
		String pstart = request.getParameter("start");
		String plimit = request.getParameter("limit");
		String ppage = request.getParameter("page");
		String prefresh = request.getParameter("refresh");
		String ptimestamp = request.getParameter("timestamp");
		String pthreadaction=request.getParameter("threadaction");
		String pthreadactionuid=request.getParameter("threadactionuid");
		
		boolean showMessagePreviewOnRow=us.getGridShowMessagePreview();
		
		try {
			String queryText = ServletUtils.getStringParameter(request, "queryText", null);
			if (!StringUtils.isBlank(queryText)) {
				CoreManager core = WT.getCoreManager();
				core.saveMetaEntry(SERVICE_ID, META_CONTEXT_SEARCH, queryText, queryText, false);
			}
		} catch(WTException exc) {
			logger.error("Exception getting queryText parameter", exc);
		}
		
		QueryObj queryObj = null;
		try {
			queryObj = ServletUtils.getObjectParameter(request, "query", new QueryObj(), QueryObj.class);
		}
		catch(ParameterException parameterException) {
				logger.error("Exception getting query obejct parameter", parameterException);
		}
		
		boolean refresh = (prefresh != null && prefresh.equals("true"));
		//boolean threaded=(pthreaded!=null && pthreaded.equals("1"));
		
		//String threadedSetting="list-threaded-"+pfoldername;
		//if (pthreaded==null || pthreaded.equals("2")) {
		//	threaded=us.isMessageListThreaded(pfoldername);
		//} else {
		//	us.setMessageListThreaded(pfoldername, threaded);
		//}
		//System.out.println("timestamp="+ptimestamp);
		long timestamp=Long.parseLong(ptimestamp);
		
		if (account.isSpecialFolder(pfoldername) || account.isSharedFolder(pfoldername)) {
			logger.debug("folder is special or shared, refresh forced");
			refresh=true;
		}

		String group = us.getMessageListGroup(pfoldername);
		if (group == null) {
			group = "";
		}
		

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
		
		SortGroupInfo sgi=getSortGroupInfo(psortfield,psortdir,group);
		
		//Save search requests
		
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
		
		Folder folder = null;
		boolean connected=false;
		try {
			connected=account.checkStoreConnected();
			if (!connected) throw new Exception("Mail account authentication error");

			Map<String, Tag> tagsMap=WT.getCoreManager().listTags();
			int funread = 0;
			if (pfoldername == null) {
				folder = account.getDefaultFolder();
			} else {
				folder = account.getFolder(pfoldername);
			}
			boolean issent = account.isSentFolder(folder.getFullName());
			boolean isdrafts = account.isDraftsFolder(folder.getFullName());
			boolean isundershared=account.isUnderSharedFolder(pfoldername);
			if (!issent) {
				String names[] = folder.getFullName().split("\\" + account.getFolderSeparator());
				for (String pname : names) {
					if (account.isSentFolder(pname)) {
						issent = true;
						break;
					}
				}
			}
			
			String key = folder.getFullName();
			JsonResult jsRes;
			ArrayList<JsListedMessage> items = new ArrayList<>();
			
			if(!pfoldername.equals("/")) {
				
				FolderCache mcache = account.getFolderCache(key);
				if (mcache.toBeRefreshed()) refresh=true;
				//Message msgs[]=mcache.getMessages(ppattern,psearchfield,sortby,ascending,refresh);
				if (psortfield !=null && psortdir != null) {
					key += "|" + psortdir + "|" + psortfield;
				}

				ImapQuery iq = new ImapQuery(this.allFlagStrings, queryObj, profile.getTimeZone());

				if(queryObj != null) refresh = true; 
				MessagesInfo messagesInfo = listMessages(mcache, key, refresh, sgi, timestamp, iq);
				Message xmsgs[] = messagesInfo.messages;

				if (pthreadaction!=null && pthreadaction.trim().length()>0) {
					long actuid=Long.parseLong(pthreadactionuid);
					mcache.setThreadOpen(actuid, pthreadaction.equals("open"));
				}

				//if threaded, look for the start considering roots and opened children
				if (xmsgs!=null && sgi.threaded && page>1) {
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
				if (xmsgs!=null && max>xmsgs.length) max=xmsgs.length;
				ArrayList<Long> autoeditList=new ArrayList<Long>();

				if (xmsgs!=null) {
					int total=0;
					int expunged=0;

					//calculate expunged
					//for(Message xmsg: xmsgs) {
					//	if (xmsg.isExpunged()) ++expunged;
					//}

					/*               if (ppattern==null && !isSpecialFolder(mcache.getFolderName())) {
					 //mcache.fetch(msgs,FolderCache.flagsFP,0,start);
					 for(int i=0;i<start;++i) {
					 try {
					 if (!msgs[i].isSet(Flags.Flag.SEEN)) funread++;
					 } catch(Exception exc) {

					 }
					 }
					 }*/
					total=sgi.threaded?mcache.getThreadedCount():xmsgs.length;
					if (start<max) {

						Folder fsent=account.getFolder(account.getFolderSent());
						boolean openedsent=false;
						//Fetch others for these messages
						mcache.fetch(xmsgs,(isdrafts?draftsFP:messagesInfo.isPEC()?pecFP:FP), start, max);
						long tId=0;
						boolean tIsOpen = false;
						boolean tChildren = false;
						int tUnseenChildren = 0;
						SonicleIMAPMessage threadRootMsg = null;
						SonicleIMAPMessage mostRecentUnseenMsg = null;
						SonicleIMAPMessage mostRecentMsg = null;

						for (int i = 0, ni = 0; i < limit; ++ni, ++i) {
							int ix = start + i;
							int nx = start + ni;
							if (nx >= xmsgs.length) break;
							if (ix >= max) break;

							SonicleIMAPMessage xm = (SonicleIMAPMessage)xmsgs[nx];
							if (xm.isExpunged()) {
								--i;
								continue;
							}

							long nuid = mcache.getUID(xm);

							int tIndent = 0;

							if (sgi.threaded) {
								tIndent = xm.getThreadIndent();

								if (tIndent == 0) {
									// This is a thread root
									tId = nuid;
									tIsOpen = mcache.isThreadOpen(tId);
									tChildren = tIsOpen;
									tUnseenChildren = 0;
									threadRootMsg = xm;
									mostRecentUnseenMsg = null;
									mostRecentMsg = xm; // Start with root as most recent

									// if closed thread, count unseen children
									if (!tIsOpen) {
										int cnx = nx + 1;
										boolean skipThis = false;
										while(cnx < xmsgs.length) {
											SonicleIMAPMessage cxm = (SonicleIMAPMessage)xmsgs[cnx];
											if (cxm.isExpunged()) {
												cnx++;
												continue;
											}
											//if this is root and next is child skip this node once done
											if (cxm.getThreadIndent() > 0) skipThis = true;
											while(cxm.getThreadIndent() > 0) {
												tChildren = true;
												if (!cxm.isExpunged() && !cxm.isSet(Flags.Flag.SEEN)) ++tUnseenChildren;
												++cnx;
												if (cnx >= xmsgs.length) break;
												cxm = (SonicleIMAPMessage)xmsgs[cnx];
											}
											break;
										}

										if (skipThis) {
											--i;
											continue;
										}
									}
								} else {
									// This is a child message in a thread
									if (!tIsOpen) {
										// If the thread is closed, check if this message should replace the root

										// Update most recent message in thread
										if (mostRecentMsg == null || 
											xm.getSentDate().after(mostRecentMsg.getSentDate()) ||
											(xm.getSentDate().equals(mostRecentMsg.getSentDate()) && nuid > mcache.getUID(mostRecentMsg))) {
											mostRecentMsg = xm;
										}

										// Update most recent unseen message in thread
										if (!xm.isSet(Flags.Flag.SEEN)) {
											if (mostRecentUnseenMsg == null ||
												xm.getSentDate().after(mostRecentUnseenMsg.getSentDate()) ||
												(xm.getSentDate().equals(mostRecentUnseenMsg.getSentDate()) && nuid > mcache.getUID(mostRecentUnseenMsg))) {
												mostRecentUnseenMsg = xm;
											}
										}

										// If we're at the end of this thread or the next message is another thread root
										boolean isLastMessageInThread = (nx+1 >= xmsgs.length) || 
																	  ((nx+1 < xmsgs.length) && 
																	   ((SonicleIMAPMessage)xmsgs[nx+1]).getThreadIndent() == 0);

										if (isLastMessageInThread) {
											// We've seen all messages in this thread, decide which one to show
											SonicleIMAPMessage messageToShow = (mostRecentUnseenMsg != null) ? 
																			  mostRecentUnseenMsg : mostRecentMsg;

											// Skip this message unless it's the one we want to show
											//if (xm != messageToShow) {
											//	--i;
											//	continue;
											//}

											// This is the message we want to show
											// Set threadRootMsg flag to indicate this is representing a thread
											xm = messageToShow;
											nuid = mcache.getUID(xm);
											//don't count myself if I'm unseen
											if (!xm.isSet(Flags.Flag.SEEN)) --tUnseenChildren;

											// We'll need to count all children in the thread for display purposes
											// This is already handled in existing code
										} else {
											// Not the last message in thread and thread is closed, skip for now
											--i;
											continue;
										}
									}
								}

							}
							Flags flags=xm.getFlags();

							//Date
							java.util.Date d=xm.getSentDate();
							java.util.Date rd = xm.getResentDate();
							if (rd != null) d = rd;
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
							String fromemail="";
							Address ia[]=xm.getFrom();
							if (ia!=null) {
								InternetAddress iafrom=(InternetAddress)ia[0];
								from=iafrom.getPersonal();
								if (from==null) from=iafrom.getAddress();
								fromemail=iafrom.getAddress();
							}
							
							//To
							String to="";
							String toemail="";
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
								toemail=iato.getAddress();
							}

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

							boolean hasAttachments=false; //mcache.hasAttachments(xm, null);
							boolean hasInvitation=false; //mcache.hasInvitation(xm);

							//Unread
							boolean unread=!xm.isSet(Flags.Flag.SEEN);
							if (queryObj != null && unread) ++funread;
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
								java.util.Date gd=sgi.threaded?xm.getMostRecentThreadDate():d;

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
							if (hasInvitation) {
								status="invitation";
							}
							else if (flags!=null) {
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
								//String tbflagstring=webtopFlag.tbLabel;
								if (!flagstring.equals("complete")) {
									String oldflagstring="flag"+flagstring;
									if (flags.contains(flagstring)
											||flags.contains(oldflagstring)
											/*|| (tbflagstring!=null && flags.contains(tbflagstring))*/
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

							boolean hasNote=flags.contains(MailManager.getFlagNoteString());
							ArrayList<String> svtags = flagsToTagsIds(flags,tagsMap);
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
									if (scal!=null) {
										syyyy=scal.get(java.util.Calendar.YEAR);
										smm=scal.get(java.util.Calendar.MONTH);
										sdd=scal.get(java.util.Calendar.DAY_OF_MONTH);
										shhh=scal.get(java.util.Calendar.HOUR_OF_DAY);
										smmm=scal.get(java.util.Calendar.MINUTE);
										ssss=scal.get(java.util.Calendar.SECOND);
										issched=true;
										status="scheduled";
									}
								} 

								h=getSingleHeaderValue(xm,HEADER_SONICLE_FROM_DRAFTER);
								if (h!=null && h.equals("true")) {
									autoedit=true;
								}
							}

							String xmfoldername=xm.getFolder().getFullName();

							//idmessage=idmessage.replaceAll("\\\\", "\\\\");
							//idmessage=Utils.jsEscape(idmessage);
							boolean archived=false;
							if (hasDmsDocumentArchiving()) {
								archived=xm.getHeader("X-WT-Archived")!=null;
								if (!archived) {
									archived=flags.contains(MailManager.getFlagDmsArchivedString());
								}
							}

							String msgtext=null;
							if (showMessagePreviewOnRow && isToday && unread) {
								try {
									msgtext=MailUtils.peekText(xm);
									if (msgtext!=null) {
										msgtext=msgtext.trim();
										if (msgtext.length()>100) msgtext=msgtext.substring(0,100);
									}
								} catch(MessagingException | IOException ex1) {
									msgtext = ex1.getMessage();
								}
							}

							String pecstatus=null;
							if (messagesInfo.isPEC()) {
								String hdrs[]=xm.getHeader(HDR_PEC_TRASPORTO);
								if (hdrs!=null && hdrs.length>0 && (hdrs[0].equals("errore")||hdrs[0].equals("posta-certificata")))
									pecstatus=hdrs[0];
								else {
									hdrs=xm.getHeader(HDR_PEC_RICEVUTA);
									if (hdrs!=null && hdrs.length>0)
										pecstatus=hdrs[0];
								}
							}
							
							String schedDate = issched ? formatCalendarDate(syyyy, smm, sdd, shhh, smmm, ssss) : null;
							Boolean threadOpen = false;
							Boolean threadHasChildren = false;
							Integer threadUnseenChildren = null;
							Boolean fmtd = false;
							String fromFolder = null;

							if (sgi.threaded) {
								threadOpen = mcache.isThreadOpen(tId);
								if (tIndent == 0) {
									threadHasChildren = tChildren;
									threadUnseenChildren = tUnseenChildren;
								} else {
									//if closed thread force indent to 0
									//so that any child selected will be rendered as 0 indentation
									if (!threadOpen) {
										tIndent = 0;
										threadHasChildren = tChildren;
										threadUnseenChildren = tUnseenChildren;
									}
								}
								if (xm.hasThreads() && !xm.isMostRecentInThread()) fmtd = true;
								if (!xmfoldername.equals(folder.getFullName())) fromFolder = xmfoldername;
							}

							items.add(new JsListedMessage(nuid, priority, status, to, toemail, from, fromemail, StringEscapeUtils.escapeHtml4(subject), msgtext, tId, tIndent, formatCalendarDate(yyyy, mm, dd, hhh, mmm, sss), gdate, sdate, xdate, unread, msgsize, svtags, pecstatus, cflag, hasNote, archived, isToday, hasAttachments, schedDate, threadOpen, threadHasChildren, threadUnseenChildren, fmtd, fromFolder));

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

					long qlimit=-1;
					long qusage=-1;
					try {
						Quota quotas[]=account.getQuota("INBOX");
						if (quotas!=null)
							for(Quota q: quotas) {
								if ((q.quotaRoot.equals("INBOX") || q.quotaRoot.equals("Quota")) && q.resources!=null) {
									for(Quota.Resource r: q.resources) {
										if (r.name.equals("STORAGE")) {
											qlimit=r.limit;
											qusage=r.usage;
										}
									}
								}
							}
					} catch(MessagingException exc) {
						logger.debug("Error on QUOTA",exc);
					}

					jsRes = new JsonResult("messages", items)
							.setTotal(total - expunged)
							.set("realTotal", xmsgs.length - expunged)
							.set("expunged", expunged)
							.set("isPEC", messagesInfo.isPEC());

					if (qlimit >= 0 && qusage >= 0) {
						jsRes.set("quotaLimit", qlimit)
								.set("quotaUsage", qusage);
					}
				} else {
					jsRes = new JsonResult("messages", items)
							.setTotal(0)
							.set("realTotal", 0)
							.set("expunged", 0);
				}
				
				GridMetadata gridMeta = new GridMetadata();
				SortMeta sortMeta = new SortMeta(psortfield, psortdir);
				String groupField = sgi.threaded ? "threadId" : group;
				List<FieldMeta> fields = Arrays.asList(
						new FieldMeta("idmessage"),
						new FieldMeta("priority").setType("int"),
						new FieldMeta("status"),
						new FieldMeta("from"),
						new FieldMeta("to"),
						new FieldMeta("subject"),
						new FieldMeta("date").setType("date"),
						new FieldMeta("gdate"),
						new FieldMeta("unread").setType("boolean"),
						new FieldMeta("flag"),
						new FieldMeta("note"),
						new FieldMeta("istoday").setType("boolean"),
						new FieldMeta("arch").setType("boolean"),
						new FieldMeta("atts").setType("boolean"),
						new FieldMeta("scheddate").setType("date"),
						new FieldMeta("fmtd").setType("boolean"),
						new FieldMeta("fromfolder")
				);
				
				gridMeta.setRoot("messages")
						.setSortInfo(sortMeta)
						.setFields(fields)
						.set("total", "total")
						.set("idProperty", "idmessage")
						.set("threaded", sgi.threaded)
						.set("groupField", groupField);

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

				jsRes.set("threaded", sgi.threaded)
						.set("unread", funread)
						.set("issent", issent)
						.set("millis", messagesInfo.millis)
						.setMetaData(gridMeta);
			} else {
				jsRes = new JsonResult("messages", items)
						.setTotal(0)
						.setStart(0)
						.setLimit(0)
						.set("unread", 0)
						.set("issent", false);
			}
			jsRes.printTo(out, false);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processGetMessagePage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puid = request.getParameter("uid");
		String prowsperpage = request.getParameter("rowsperpage");
		//String psearchfield = request.getParameter("searchfield");
		//String ppattern = request.getParameter("pattern");
		//String pquickfilter=request.getParameter("quickfilter");
		//String prefresh = request.getParameter("refresh");
		//String ptimestamp = request.getParameter("timestamp");
		//if (psearchfield != null && psearchfield.trim().length() == 0) {
		//	psearchfield = null;
		//}
		//if (ppattern != null && ppattern.trim().length() == 0) {
		//	ppattern = null;
		//}
		//if (pquickfilter!=null && pquickfilter.trim().length()==0) pquickfilter=null;
		//boolean refresh = (prefresh != null && prefresh.equals("true"));
		//long timestamp=Long.parseLong(ptimestamp);
		boolean refresh=true;
		long uid=Long.parseLong(puid);		
		long rowsperpage=Long.parseLong(prowsperpage);		
		
		String group = us.getMessageListGroup(pfoldername);
		if (group == null) {
			group = "";
		}

		String psortfield = "date";
		String psortdir = "DESC";
		try {
			boolean nogroup=group.equals("");
			if (nogroup) {
				String s = us.getMessageListSort(pfoldername);
				int ix = s.indexOf("|");
				psortfield = s.substring(0, ix);
				psortdir = s.substring(ix + 1);
			} else {
				psortfield = "date";
				psortdir = "DESC";
			}
		} catch(Exception exc) {
			logger.error("Exception",exc);
		}
		
		SortGroupInfo sgi=getSortGroupInfo(psortfield,psortdir,group);
		
		Folder folder = null;
		boolean connected=false;
		try {
			connected=account.checkStoreConnected();
			if (!connected) throw new Exception("Mail account authentication error");

				
			if (pfoldername == null) {
				folder = account.getDefaultFolder();
			} else {
				folder = account.getFolder(pfoldername);
			}
			
			String key = folder.getFullName();
			FolderCache mcache = account.getFolderCache(key);
			if (mcache.toBeRefreshed()) refresh=true;
//			if (ppattern != null && psearchfield != null) {
//				key += "|" + ppattern + "|" + psearchfield;
//			}
			if (psortfield !=null && psortdir != null) {
				key += "|" + psortdir + "|" + psortfield;
			}
//			if (pquickfilter !=null) {
//				key +="|" + pquickfilter;
//			}
			
			MessagesInfo messagesInfo = listMessages(mcache, key, refresh, sgi, 0, new ImapQuery(false));
            Message xmsgs[]=messagesInfo.messages;
			if (xmsgs!=null) {
				boolean found=false;
				int start=0;
				int startx=0;
				long tId=0;
				for (int i = 0, ni = 0; i < xmsgs.length; ++ni, ++i) {
					int ix = start + i;
					int nx = start + ni;
					if (nx>=xmsgs.length) break;

					SonicleIMAPMessage xm=(SonicleIMAPMessage)xmsgs[nx];
					if (xm.isExpunged()) {
						--i;
						continue;
					}

					long nuid=mcache.getUID(xm);
					int tIndent=xm.getThreadIndent();
					
					if (nuid==uid) {
						found=true;
						if (tIndent==0) tId=0;
						//else tId contains the last thread root id
						break;
					}
					
					if (tIndent==0) tId=nuid;
					else if (sgi.threaded) {
						if (!mcache.isThreadOpen(tId)) {
							--i;
							continue;
						}
					}
					
					++startx;
				}
				if (found) {
					JsonResult jsr=new JsonResult();
					jsr.set("page", ((int)(startx/rowsperpage)+1));
					jsr.set("row", startx);
					jsr.set("account", account.getId());
					if (tId>0) jsr.set("threadid", tId);
					jsr.printTo(out);
				}
				else new JsonResult(false,"Message not found");
			}
			else new JsonResult(false,"No messages");
		} catch(Exception exc) {
			new JsonResult(exc).printTo(out);
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
		if (sdp.length<3 || sdt.length<2) return null;
		
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
	
	class MessageListThread implements Runnable {
		
		FolderCache fc;
		String pattern;
		String searchfield;
		int sortby;
		boolean ascending;
		int sort_group;
		boolean groupascending;
		boolean refresh;
		long millis;
		ImapQuery imapQuery;
		
		boolean isPec=false;
		HashMap<String,Message> hpecsent=null;
		
		boolean threaded=false;
		
		Message msgs[] = null;
		boolean started = false;
		boolean finished = false;
		final Object lock = new Object();
		long lastRequest = 0;
		
		MessageListThread(FolderCache fc, int sortby, boolean ascending, boolean refresh, int sort_group, boolean groupascending, boolean threaded, ImapQuery imapQuery) {
			this.fc = fc;
			this.sortby = sortby;
			this.ascending = ascending;
			this.refresh = refresh;
			this.sort_group = sort_group;
			this.groupascending = groupascending;
			this.threaded=threaded;
			this.imapQuery = imapQuery;
		}
		
		public void run() {
			started = true;
			finished = false;
			synchronized (lock) {
				try {
					this.millis = System.currentTimeMillis();
					msgs = fc.getMessages(sortby, ascending, refresh, sort_group, groupascending, threaded, imapQuery);
					
					/*
					UserProfileId profileId=getEnv().getProfileId();
					String domainId=profileId.getDomainId();
					if (fc.isUnderSharedFolder()) {
						SharedPrincipal sp=fc.getSharedInboxPrincipal();
						if (sp!=null) profileId=new UserProfileId(domainId, sp.getUserId());
						else profileId=null;
					}
					if (profileId!=null)
						isPec=RunContext.hasRole(profileId, WT.getGroupUidForPecAccounts(profileId.getDomainId()));
					*/
					isPec=fc.isPEC();

/*					if (isPec) {
						Message pmsgs[]=fc.searchMessagesByXHeader(HDR_PEC_RICEVUTA,HDR_PEC_RICEVUTA_VALUE_AVVENUTA_CONSEGNA);
						fc.fetch(pmsgs, pecFP);
						hpecsent=new HashMap<>();
						for(Message pmsg: pmsgs) {
							String hdrs[]=pmsg.getHeader(HDR_PEC_RIFERIMENTO_MESSAGE_ID);
							if (hdrs!=null && hdrs.length>0)
								hpecsent.put(hdrs[0], pmsg);
						}
						System.out.println("loaded PEC hash");
					}*/
				} catch (Exception exc) {
					Service.logger.error("Exception",exc);
				} finally {
					finished = true;
					lock.notifyAll();
				}
			}
		}
		
/*		public boolean checkSkipPEC(Message xm) throws MessagingException {
			if (isPec) {
				String hdrs[]=xm.getHeader(HDR_PEC_RICEVUTA);
				if (hdrs!=null && hdrs.length>0 && hdrs[0].equals(HDR_PEC_RICEVUTA_VALUE_ACCETTAZIONE)) {							
					hdrs=xm.getHeader(HDR_PEC_RIFERIMENTO_MESSAGE_ID);
					if (hdrs!=null && hdrs.length>0) {
						return hpecsent.containsKey(hdrs[0]);
					}
				}
			}
			return false;
		}*/
		
	}
	
	private ArrayList<String> flagsToTagsIds(Flags flags, Map<String, Tag> map) {
		ArrayList<String> tags=null;
		if (flags!=null) {
			for(Tag tag: map.values()) {
				if (flags.contains(TagsHelper.tagIdToFlagString(tag))) {
					if (tags==null) tags=new ArrayList<>();
					tags.add(tag.getTagId());
				}
			}
		}
		if (tags==null) tags=new ArrayList<>();
		return tags;
	}
	
	protected ArrayList<String> flagsToTagsIds(Flags flags) {
		ArrayList<String> tags=null;
		try {
			tags=flagsToTagsIds(flags,WT.getCoreManager().listTags());
		} catch(WTException exc) {
			logger.error("Error converting flags to tags",exc);
		}
		if (tags==null) tags=new ArrayList<>();
		return tags;
	}
	
	/*private String getJSTagsArray(Flags flags) {
		ArrayList<Tag> tags=null;
		String svtags=null;
		if (flags!=null) {
			for(Tag tag: atags) {
				if (flags.contains(tag.getTagId())) {
					if (tags==null) tags=new ArrayList<>();
					tags.add(tag);
				}
			}
			if (tags!=null) {
				for(Tag tag: tags) {
					if (svtags==null) svtags="[ ";
					else svtags+=",";
					svtags+="'"+StringEscapeUtils.escapeEcmaScript(tag.getTagId())+"'";
				}
				if (svtags!=null) svtags+=" ]";
			}
		}
		return svtags;
	}*/
	
	DateFormat df = null;
	
	public void processGetMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pidattach = request.getParameter("idattach");
		String providername = request.getParameter("provider");
		String providerid = request.getParameter("providerid");
		String nopec = request.getParameter("nopec");
		int idattach = 0;
		boolean setSeen = ServletUtils.getBooleanParameter(request, "setseen", true);
		
		if (df == null) {
			df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, environment.getProfile().getLocale());
		}
		try {
			FolderCache mcache = null;
			Message m = null;
                        Message morig = null;
			SonicleIMAPMessage im=null;
			int recs = 0;
			long msguid=-1;
			String vheader[] = null;
			boolean wasseen = false;
			boolean isPECView=false;
			ArrayList<JsMessageDetails> items = new ArrayList<>();
			
			if (providername == null) {
				account.checkStoreConnected();
				mcache = account.getFolderCache(pfoldername);
                                msguid=Long.parseLong(puidmessage);
                                //keeep morig copy in case pec changes m into its internal part
                                morig=m=mcache.getMessage(msguid);
				im=(SonicleIMAPMessage)m;
				im.setPeek(us.isManualSeen());
                                if (m.isExpunged()) throw new MessagingException("Message "+puidmessage+" expunged");
				vheader = m.getHeader("Disposition-Notification-To");
				wasseen = m.isSet(Flags.Flag.SEEN);
				if (pidattach != null) {
					
					HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
					Part part = mailData.getAttachmentPart(Integer.parseInt(pidattach));
					m = (Message) part.getContent();
					idattach = Integer.parseInt(pidattach) + 1;
				}
				else if (nopec==null && mcache.isPEC()) {
					String hdrs[]=m.getHeader(HDR_PEC_TRASPORTO);
					if (hdrs!=null && hdrs.length>0 && hdrs[0].equals("posta-certificata")) {
						HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
						int parts=mailData.getAttachmentPartCount();
						for(int i=0; i<parts; ++i) {
							Part p=mailData.getAttachmentPart(i);
							if(p.isMimeType("message/rfc822")) {
								m=(Message)p.getContent();
								idattach=i+1;
								isPECView=true;
								break;
							}
						}
					}
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
				subject = MimeUtility.decodeText(subject);
			}
			
			//check for List-Unsubscribe, http or mailto only, http has precedence
			String listUnsubscribe=null;
			String hdrs[] = m.getHeader("List-Unsubscribe");
			if (hdrs!=null && hdrs.length>0) {
				String hdr = MimeUtility.decodeText(hdrs[0]);
				String links[] = StringUtils.split(hdr, ",");
				for(String link: links) {
					link = StringUtils.strip(link);
					link = StringUtils.strip(link, "<>");
					//first http link found will be ok and break cycle
					if (StringUtils.startsWithIgnoreCase(link, "http")) {
						listUnsubscribe=link;
						break;
					}
					//mailto is ok, but continue to check for an http link
					else if (StringUtils.startsWithIgnoreCase(link, "mailto")) {
						listUnsubscribe=link;
					}
				}
			}
			if (listUnsubscribe!=null) items.add(new JsMessageDetails("listUnsubscribe", listUnsubscribe));
			
			java.util.Date d = m.getSentDate();
			if (d == null) {
				d = m.getReceivedDate();
			}
			if (d == null) {
				d = new java.util.Date(0);
			}
			String date = df.format(d).replaceAll("\\.", ":");

			String throughDate = null;
			java.util.Date rd = im.getResentDate();
			if (rd != null) {
				d = rd;
				throughDate = df.format(d).replaceAll("\\.", ":");
			}

			String fromName = "";
			String fromEmail = "";
			Address as[] = m.getFrom();
			InternetAddress iafrom=null;
			if (as != null && as.length > 0) {
				iafrom = (InternetAddress) as[0];
				fromName = iafrom.getPersonal();
				fromEmail = adjustEmail(iafrom.getAddress());
				if (fromName == null) {
					fromName = fromEmail;
				}
			}
			items.add(new JsMessageDetails("from", fromName, fromEmail));
			recs += 2;
			
			String throughName=null;
			String throughEmail=null;
			String rfh[]=m.getHeader("Resent-From");
			InternetAddress iathrough=null;
			if (rfh!=null && rfh.length>0) {
				iathrough = InternetAddressUtils.toInternetAddress(rfh[0]);
				throughName = iathrough.getPersonal();
				throughEmail = adjustEmail(iathrough.getAddress());
				if (throughName == null) {
					throughName = throughEmail;
				}
			}
			items.add(new JsMessageDetails("through", throughName, throughEmail, 0, throughDate, false));
			
			Address tos[] = m.getRecipients(RecipientType.TO);
			ArrayList<String> listTos = new ArrayList<>();
			ArrayList<String> listTosDN = new ArrayList<>();
			if (tos != null) {
				for (Address to : tos) {
					InternetAddress ia = (InternetAddress) to;
					String toName = ia.getPersonal();
					String toEmail = adjustEmail(ia.getAddress());
					listTos.add(toEmail);
					listTosDN.add(toName);
					
					if (toName == null) {
						toName = toEmail;
					}
					
					items.add(new JsMessageDetails("to", toName, toEmail));
					++recs;
				}
			}
			Address ccs[] = m.getRecipients(RecipientType.CC);
			ArrayList<String> listCcs = new ArrayList<>();
			ArrayList<String> listCcsDN = new ArrayList<>();
			if (ccs != null) {
				for (Address cc : ccs) {
					InternetAddress ia = (InternetAddress) cc;
					String ccName = ia.getPersonal();
					String ccEmail = adjustEmail(ia.getAddress());
					listCcs.add(ccEmail);
					listCcsDN.add(ccName);
					
					if (ccName == null) {
						ccName = ccEmail;
					}
					
					items.add(new JsMessageDetails("cc", ccName, ccEmail));
					++recs;
				}
			}
            Address bccs[]=m.getRecipients(RecipientType.BCC);
			ArrayList<String> listBccs = new ArrayList<>();
			ArrayList<String> listBccsDN = new ArrayList<>();
            if (bccs!=null)
                for(Address bcc: bccs) {
                    InternetAddress ia=(InternetAddress)bcc;
                    String bccName=ia.getPersonal();
                    String bccEmail=adjustEmail(ia.getAddress());
                    listBccs.add(bccEmail);
					listBccsDN.add(bccName);
					
					if (bccName==null) {
						bccName=bccEmail;
					}
					
                    items.add(new JsMessageDetails("bcc", bccName, bccEmail));
					++recs;
                }
			ArrayList<FolderCache.HTMLPart> htmlparts = null;
			boolean balanceTags=isPreviewBalanceTags(iafrom);
			if (providername == null) {
				htmlparts = mcache.getHTMLParts((MimeMessage) m, msguid, false, balanceTags);
			} else {
				htmlparts = mcache.getHTMLParts((MimeMessage) m, providername, providerid, balanceTags);
			}
			
			HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
			ICalendarRequest ir=mailData.getICalRequest();
			if (ir!=null) {
			    if (htmlparts.size() > 0) {
					FolderCache.HTMLPart htmlPart=htmlparts.get(0);
					items.add(new JsMessageDetails("html", htmlPart.html));
				}
			} else {
				for (FolderCache.HTMLPart htmlPart : htmlparts) {
					//sout += "{iddata:'html',value1:'" + OldUtils.jsEscape(html) + "',value2:'',value3:0},\n";
					items.add(new JsMessageDetails("html", htmlPart.html));
					++recs;
			    }
			}
						
			/*if (!wasseen) {
				//if (us.isManualSeen()) {
				if (!setSeen) {
					m.setFlag(Flags.Flag.SEEN, false);
				} else {
					//if no html part, flag seen is not set
					if (htmlparts.size()==0) m.setFlag(Flags.Flag.SEEN, true);
				}
			}*/
			
			//Catch exception during setFlag, which may fail when viewing
			// internal parts (eg. italian PEC)
			try {
				if (!us.isManualSeen()) {
					if (htmlparts.size()==0) m.setFlag(Flags.Flag.SEEN, true);
				}
				else {
					if (setSeen) {
						//uses morig so that it's ok also if it's a pec view
						morig.setFlag(Flags.Flag.SEEN, true);
					}
				}
			} catch(MethodNotSupportedException exc) {
				logger.error("Cannot set Flags as SEEN",exc);
			}
			
			//audit log only when changing state
			if (!wasseen && mailManager.isAuditEnabled() && StringUtils.isNotEmpty(messageid)) {
				JsAuditMessageInfo jsAudit = new JsAuditMessageInfo();
				if (StringUtils.isNotEmpty(fromEmail)) jsAudit.setFrom(fromEmail);
				if (StringUtils.isNotEmpty(fromName)) jsAudit.setFromDN(fromName);
				if (!listTos.isEmpty()) jsAudit.setTos(listTos);
				if (!listTosDN.isEmpty()) jsAudit.setTosDN(listTosDN);
				if (!listCcs.isEmpty()) jsAudit.setCcs(listCcs);
				if (!listCcsDN.isEmpty()) jsAudit.setCcsDN(listCcsDN);
				if (!listBccs.isEmpty()) jsAudit.setBccs(listBccs);
				if (!listBccsDN.isEmpty()) jsAudit.setBccsDN(listBccsDN);
				if (StringUtils.isNotEmpty(subject)) jsAudit.setSubject(subject);
				jsAudit.setFolder(morig.getFolder().getFullName());
				
				mailManager.auditLogWrite(
					MailManager.AuditContext.MAIL,
					MailManager.AuditAction.VIEW, 
					messageid, 
					JsonResult.gson(false).toJson(jsAudit)
				);
			}
			
			ArrayList<String> attnames=null;
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
					imgname = "resources/" + getManifest().getId() + "/laf/" + cus.getUILookAndFeel() + "/ical_16.png";
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
				} else {
					if (isCalendar && !StringUtils.endsWithIgnoreCase(pname, ".ics")) pname+=".ics";
				}
				int size = p.getSize();
				int lines = (size / 76);
				int rsize = size - (lines * 2);//(p.getSize()/4)*3;
				String iddata = ctype.equalsIgnoreCase("message/rfc822") ? "eml" : (inline ? "inlineattach" : "attach");
				boolean editable=isFileEditableInDocEditor(pname);
				
				if (attnames==null) attnames=new ArrayList<String>();
				attnames.add(pname);
				items.add(new JsMessageDetails(iddata, Integer.toString(i + idattach), pname, rsize, imgname, editable));
			}
			if (!mcache.isDrafts() && !mcache.isSent() && !mcache.isSpam() && !mcache.isTrash() && !mcache.isArchive()) {
				if (vheader != null && vheader[0] != null && !wasseen) {
					if (WT.getCoreManager().getServiceStoreEntry(SERVICE_ID, "receipt", messageid)==null) {
						items.add(new JsMessageDetails("receipt", us.getReadReceiptConfirmation(), vheader[0]));
					}
				}
			}
			
			String h = getSingleHeaderValue(m, "Sonicle-send-scheduled");
			if (h != null && h.equals("true")) {
				java.util.Calendar scal = parseScheduleHeader(getSingleHeaderValue(m, "Sonicle-send-date"), getSingleHeaderValue(m, "Sonicle-send-time"));
				if (scal!=null) {
					java.util.Date sd = scal.getTime();
					String sdate = df.format(sd).replaceAll("\\.", ":");
					items.add(new JsMessageDetails("scheddate", sdate));
				}
			}			
			
			if (ir!=null) {
				
				/*
				ICalendarManager calMgr = (ICalendarManager)WT.getServiceManager("com.sonicle.webtop.calendar",environment.getProfileId());
				if (calMgr != null) {
					if (ir.getMethod().equals("REPLY")) {
						calMgr.updateEventFromICalReply(ir.getCalendar());
						//TODO: gestire lato client una notifica di avvenuto aggiornamento
					} else {
						Event evt = calMgr..getEvent(GetEventScope.PERSONAL_AND_INCOMING, false, ir.getUID())
						if (evt != null) {
							UserProfileId pid = getEnv().getProfileId();
							UserProfile.Data ud = WT.getUserData(pid);
							boolean iAmOrganizer = StringUtils.equalsIgnoreCase(evt.getOrganizerAddress(), ud.getEmailAddress());
							boolean iAmOwner = pid.equals(calMgr.getCalendarOwner(evt.getCalendarId()));
							
							if (!iAmOrganizer && !iAmOwner) {
								//TODO: gestire lato client l'aggiornamento: Accetta/Rifiuta, Aggiorna e20 dopo update/request
							}
						}
					}
				}
				*/
				
				ICalendarManager cm=(ICalendarManager)WT.getServiceManager("com.sonicle.webtop.calendar", true, environment.getProfileId());
				if (cm!=null) {
					int eid=-1;
					//Event ev=cm.getEventByScope(EventScope.PERSONAL_AND_INCOMING, ir.getUID());
					Event ev = null;
					if (ir.getMethod().equals("REPLY")) {
						// Previous impl. forced (forceOriginal == true)
						ev = cm.getEvent(GetEventScope.PERSONAL_AND_INCOMING, ir.getUID());
					} else {
						ev = cm.getEvent(GetEventScope.PERSONAL_AND_INCOMING, ir.getUID());
					}
					
					UserProfileId pid = getEnv().getProfileId();
					UserProfile.Data ud = WT.getUserData(pid);
					
					if (ev != null) {
						InternetAddress organizer = InternetAddressUtils.toInternetAddress(ev.getOrganizer());
						boolean iAmOwner = pid.equals(cm.getCalendarOwner(ev.getCalendarId()));
						boolean iAmOrganizer = (organizer != null) && StringUtils.equalsIgnoreCase(organizer.getAddress(), ud.getEmailAddress());
						
						//TODO: in reply controllo se mail combacia con quella dell'attendee che risponde...
						//TODO: rimuovere controllo su data? dovrebbe sempre aggiornare?
						
						if (iAmOwner || iAmOrganizer) {
							eid = 0;
							//TODO: troviamo un modo per capire se la risposta si riverisce all'ultima versione dell'evento? Nuovo campo timestamp?
							/*
							DateTime dtEvt = ev.getRevisionTimestamp().withMillisOfSecond(0).withZone(DateTimeZone.UTC);
							DateTime dtICal = ICal4jUtils.fromICal4jDate(ir.getLastModified(), ICal4jUtils.getTimeZone(DateTimeZone.UTC));
							if (dtICal.isAfter(dtEvt)) {
								eid = 0;
							} else {
								eid = ev.getEventId();
							}
							*/
						}
					}
					items.add(new JsMessageDetails("ical", ir.getMethod(), ir.getUID(), eid));
				}
			}
			
			items.add(new JsMessageDetails("date", date));
			items.add(new JsMessageDetails("subject", subject));
			items.add(new JsMessageDetails("messageid", messageid));
			
			if (providername == null) {
				mcache.refreshUnreads();
			}
			long millis = System.currentTimeMillis();
			ArrayList<String> svtags = flagsToTagsIds(m.getFlags());
			JsProActiveSecurity jsPas=null;
			if (!mcache.isPEC()) jsPas=setupProActiveSecurity(fromName, fromEmail, htmlparts, attnames, m);
			
			JsonResult ret=new JsonResult("message", items)
					.set("tags", svtags)
					.set("pec", isPECView)
					.setTotal(recs)
					.set("millis", millis);
			if (jsPas!=null) ret.set("pas", jsPas);
			
			ret.printTo(out, false);
			
			if (im!=null) im.setPeek(false);
			
//            if (!wasopen) folder.close(false);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processGetMessageEnvelope(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		Boolean stripmyself = ServletUtils.getBooleanParameter(request, "stripmyself", false);
		
		if (df == null) {
			df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, environment.getProfile().getLocale());
		}
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
			long msguid=Long.parseLong(puidmessage);
			Message m=mcache.getMessage(msguid);
			if (m.isExpunged()) throw new MessagingException("Message "+puidmessage+" expunged");
			
			String messageid=getMessageID(m);
			String subject = m.getSubject();
			if (subject == null) {
				subject = "";
			} else {
				subject = MimeUtility.decodeText(subject);
			}
			
			java.util.Date d = m.getSentDate();
			if (d == null) {
				d = m.getReceivedDate();
			}
			if (d == null) {
				d = new java.util.Date(0);
			}
			String date = df.format(d).replaceAll("\\.", ":");

			Address as[] = m.getFrom();
			String from=null;
			if (as != null && as.length > 0) {
				from = InternetAddressUtils.toFullAddress((InternetAddress) as[0]);
			}
			
			Address atos[] = m.getRecipients(RecipientType.TO);
			String tos[] = null;
			if (atos != null) {
				if (stripmyself) atos = removeDestination(atos, environment.getProfile().getEmailAddress());
				tos = new String[atos.length];
				for (int i=0; i<atos.length; ++i) {
					tos[i] = InternetAddressUtils.toFullAddress((InternetAddress)atos[i]);
				}
			}
			
			
			Address accs[] = m.getRecipients(RecipientType.CC);
			String ccs[] = null;
			if (accs != null) {
				if (stripmyself) accs = removeDestination(accs, environment.getProfile().getEmailAddress());
				ccs = new String[accs.length];
				for (int i=0; i<accs.length; ++i) {
					ccs[i] = InternetAddressUtils.toFullAddress((InternetAddress)accs[i]);
				}
			}
			
			Address abccs[] = m.getRecipients(RecipientType.BCC);
			String bccs[] = null;
			if (abccs != null) {
				if (stripmyself) abccs = removeDestination(abccs, environment.getProfile().getEmailAddress());
				bccs = new String[abccs.length];
				for (int i=0; i<abccs.length; ++i) {
					bccs[i] = InternetAddressUtils.toFullAddress((InternetAddress)abccs[i]);
				}
			}

			JsEnvelope jse = new JsEnvelope(messageid, date, subject, from, tos, ccs, bccs);
			
			JsonResult ret=new JsonResult("envelope", jse);
			
			ret.printTo(out, false);
			
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	private JsProActiveSecurity setupProActiveSecurity(String fromDisplayname, String fromAddress, ArrayList<FolderCache.HTMLPart> htmlparts, ArrayList<String> attnames, Message m) throws WTException {
		JsProActiveSecurity jsPas=null;
			
		if (pasRules.isActive()) {
			jsPas=new JsProActiveSecurity();
			String internetDomain=WT.getPrimaryDomainName(environment.getProfileId().getDomainId()).toLowerCase();
			String senderDomain=null;
			
			boolean isSpam=false;
			boolean isAlmostSpam=false;
			boolean isNewsletter=false;
			boolean senderTrusted=false;
			boolean hasForgedSender=false;
			
			try {
				float score=0;
				//check rspamd first
				String hdrs[]=m.getHeader("X-Spamd-Result");
				float threshold=pasDefaultSpamThreshold;
				//check for threshold headers
				for(String spamThresholdHeader: SPAM_THRESHOLD_HEADERS) {
					String thhdrs[]=m.getHeader(spamThresholdHeader);
					if (thhdrs!=null && thhdrs.length>0 && thhdrs[0]!=null) {
						try {
							threshold=Float.parseFloat(thhdrs[0]);
						} catch(NumberFormatException nexc) {
						}
					}
				}
				float threshold1=threshold/2;
				if (hdrs!=null && hdrs.length>0) {
					String hdr=hdrs[0];
					int ix1=hdr.indexOf("[");
					int ix2=hdr.indexOf("/");
					if (ix1>=0 && ix2>ix1) {
						String v=hdr.substring(ix1+1,ix2);
						score=Float.parseFloat(v);
						isSpam=score>=threshold;
						isAlmostSpam=!isSpam && score>=threshold1;
					}
					if (pasRules.hasForgedSenderCheck()) {
						//check forged sender only if spam score is yellow
						if (isAlmostSpam)
							hasForgedSender=hdr.contains("FORGED_SENDER");
					}
				} 
				//check spamassassin second
				else {
					hdrs=m.getHeader("X-Spam-Status");
					if (hdrs!=null && hdrs.length>0) {
						String tokens[]=StringUtils.split(hdrs[0]);
						boolean scoreDone=false;
						boolean thresholdDone=false;
						for(String token: tokens) {
							if (!scoreDone && StringUtils.startsWithIgnoreCase(token, "score=")) {
								try {
									score=Float.parseFloat(token.substring(6));
									scoreDone=true;
								} catch(Throwable t) {
									
								}
							}
							else if (!thresholdDone && StringUtils.startsWithIgnoreCase(token, "required=")) {
								try {
									threshold=Float.parseFloat(token.substring(9));
									thresholdDone=true;
								} catch(Throwable t) {
									
								}
							}
							if (scoreDone && thresholdDone) break;
						}
						if (scoreDone && !thresholdDone) threshold=pasDefaultSpamThreshold;
						threshold1=threshold/2;
						isSpam=score>=threshold;
						isAlmostSpam=!isSpam && score>=threshold1;
					}
				}
				if (pasRules.hasSpamScoreVisualization()) jsPas.setIsSpam(isSpam, score, threshold);
				jsPas.setHasForgedSender(hasForgedSender);
			} catch(MessagingException exc) {
				if (pasRules.hasSpamScoreVisualization()) jsPas.setIsSpam(false, 0f, 0f);
			}
			
			//check for newsletter only if spam is green and no forged sender
			if (!isSpam && !isAlmostSpam && !hasForgedSender) {
				//check for newsletter
				if (pasRules.hasUnsubscribeDirectivesCheck()) {
					try {
						String hdrs[]=m.getHeader("List-Unsubscribe");
						isNewsletter=(hdrs!=null && hdrs.length>0);
						jsPas.setIsNewsletter(isNewsletter);
						senderTrusted=isNewsletter;
					} catch(MessagingException exc) {
					}
				}
			}

			
			//If not spam and not a forged sender and not a valid newsletter, goes on with sender checks
			if (!isSpam && !hasForgedSender && !isNewsletter) {
				
				int ix=fromAddress.indexOf("@");
				if (ix>=0 && fromAddress.length()>(ix+1)) senderDomain=fromAddress.substring(ix+1).toLowerCase();

				//check sender against my domain
				if (!senderTrusted && pasRules.hasMyDomainCheck()) {
					jsPas.setIsSenderMyDomain(
							senderTrusted=senderDomain.equals(internetDomain)
					);
				}

				//check against frequent contacts
				if (!senderTrusted && pasRules.hasFrequentContactCheck()) {
					final ArrayList<String> ids = new ArrayList<>();
					ids.add(CoreManager.RECIPIENT_PROVIDER_AUTO_SOURCE_ID);
					CoreManager core = WT.getCoreManager();
					jsPas.setIsSenderFrequent(
						senderTrusted=core.listProviderRecipients(RecipientFieldType.EMAIL, ids, fromAddress, 1).size()>0
					);
				}

				//check against any contacts
				if (!senderTrusted && pasRules.hasAnyContactsCheck()) {
					final ArrayList<String> ids = new ArrayList<>();
					CoreManager core = WT.getCoreManager();
					ids.addAll(core.listRecipientProviderSourceIds());
					List<Recipient> rcpnts=core.listProviderRecipients(RecipientFieldType.EMAIL, ids, fromAddress, Integer.MAX_VALUE);
					if (rcpnts.size()>0) {
						//prepare sender dn tokens upper case
						ArrayList<String> fromTokens=new ArrayList<>();
						for(String dnToken: StringUtils.split(fromDisplayname))
							fromTokens.add(dnToken.toUpperCase());

						for(Recipient rcpnt: rcpnts) {
							boolean addressEqual=rcpnt.getAddress().equalsIgnoreCase(fromAddress);
							jsPas.setIsSenderAnyContact(addressEqual);
							if (addressEqual) {
								//check for consinstency with contact Dn
								// looking for words in contact Dn
								// and check if they're all present in email Dn
								if (pasRules.hasDisplaynameCheck()) {
									String rcpntDnTokens[]=StringUtils.split(rcpnt.getPersonal());
									boolean dnOk=true;
									for(String token: rcpntDnTokens) {
										if (!fromTokens.contains(token.toUpperCase())) {
											dnOk=false;
											break;
										}
									}
									jsPas.setIsSenderDisplaynameConsistentWithContact(
										senderTrusted=dnOk
									);
								} else {
									senderTrusted=true;
								}
								if (senderTrusted) break;
							}
						}
					}
					else jsPas.setIsSenderAnyContact(false);
				}

			}
			
			//check against fake sender patterns
			//force untrusted in this case
			if (senderTrusted && pasRules.hasFakePatternsCheck()) {
				//check if displayname is an email address
				if (InternetAddressUtils.isAddressValid(fromDisplayname)) {
					//check if the email in displayname is same as the real email
					if (!fromDisplayname.equalsIgnoreCase(fromAddress)) {
						senderTrusted=false;
						jsPas.setIsSenderFakePattern(true);
					}
				}
			}

			jsPas.setIsSenderTrusted(senderTrusted);
			
			//if sender is not trusted
			//go with external links and attachment check
			if (!senderTrusted) {
				for(FolderCache.HTMLPart htmlPart: htmlparts) {
					ArrayList<String> hosts=new ArrayList<>();
					//get unique hosts
					for(String href: htmlPart.hrefs) {
						String host=null;
						try {
							host=URI.create(href.trim()).getHost();
						} catch(IllegalArgumentException exc) {
							//if href is invalid, use it entirely
							//so it will be shown as dangerous
							host=href;
						}
						if (host!=null && !hosts.contains(host)) hosts.add(host.toLowerCase());
					}
					//check external links
					for(String host: hosts) {
						//host link verifications
						boolean hostTrusted=false;

						//check host against my internet domain
						if (host.endsWith(internetDomain)) hostTrusted=true;

						//check host against sender domain
						if (senderDomain!=null && host.endsWith(senderDomain)) hostTrusted=true;
						
						//check against white list
						if (!hostTrusted && pasDomainsWhiteListRegexPattern!=null) {
							hostTrusted = pasDomainsWhiteListRegexPattern.matcher(host).matches();
						}

						if (!hostTrusted) jsPas.addExternalLinkHost(host);
					}
				}
				
				if (attnames!=null) {
					
					//check dangerous attachments
					for(String attname: attnames) {
						String ext=FilenameUtils.getExtension(attname).toLowerCase();
						if (pasDangerousExtensions.contains(ext))
							jsPas.addDangerousExtension(ext);
					}
					
					//check for zip attachments
					if (pasRules.hasZipCheck()) {
						for(String attname: attnames) {
							String ext=FilenameUtils.getExtension(attname).toLowerCase();
							if (ext.equals("zip")) {
								jsPas.setHasZipAttachment(true);
								break;
							}
						}
					}
				}
				
			}
		}
		return jsPas;
	}
	
	public void processGetContactFromVCard(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String folder = ServletUtils.getStringParameter(request, "folder", true);
			int messageId = ServletUtils.getIntParameter(request, "messageId", true);
			int attachId = ServletUtils.getIntParameter(request, "attachId", true);
			String uploadTag = ServletUtils.getStringParameter(request, "uploadTag", true);
			
			MailAccount account = getAccount(request);
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(folder);
			Message m = mcache.getMessage(messageId);
			HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
			Part part = mailData.getAttachmentPart(attachId);
			String filename = MailUtils.getPartFilename(part);
			
			InputStream is = part.getInputStream();
			try {
				List<ContactInput> results = new VCardInput().parseVCard(is);
				ContactInput ci = results.get(0);
				
				JsContactData js = new JsContactData(ci.contact, ci.contactCompany);

				if (ci.contactPicture != null) {
					ContactPictureWithBytes picture = (ContactPictureWithBytes)ci.contactPicture;
					WebTopSession.UploadedFile upl = null;
					ByteArrayInputStream bais = null;
					try {
						bais = new ByteArrayInputStream(picture.getBytes());
						upl = addAsUploadedFile("com.sonicle.webtop.contacts", uploadTag, StringUtils.defaultIfBlank(filename, "idAttach"), "text/vcard", bais);
					} finally {
						IOUtils.closeQuietly(bais);
					}
					if (upl != null) js.picture = upl.getUploadId();
				}
				new JsonResult(js).printTo(out);
				
			} finally {
				IOUtils.closeQuietly(is);
			}
			
		} catch (Exception ex) {
			Service.logger.error("Exception",ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processGetMessageId(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		boolean result = false;
		String value = "";
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
            long msguid=Long.parseLong(puidmessage);
            value=getMessageID(mcache.getMessage(msguid));
			result = true;
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			value = exc.getMessage();
		}
		new JsonResult(result, value).printTo(out);
	}

	public void processMessagePrinted(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
            long msguid=Long.parseLong(puidmessage);
			String messageId = getMessageID(mcache.getMessage(msguid));
			if (mailManager.isAuditEnabled() && StringUtils.isNotEmpty(messageId)) {
				mailManager.auditLogWrite(
					MailManager.AuditContext.MAIL,
					MailManager.AuditAction.PRINT, 
					messageId, 
					null
				);
			}

		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
		new JsonResult().printTo(out);
	}
	
	public void processGetMessageNote(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		UserProfile profile = environment.getProfile();
		Connection con = null;
		boolean result = false;
		String text = "";
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
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
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String text = request.getParameter("text").trim();
		UserProfile profile = environment.getProfile();
		Connection con = null;
		boolean result = false;
		String message = "";
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
            long msguid=Long.parseLong(puidmessage);
            Message msg=mcache.getMessage(msguid);
			String id = getMessageID(msg);
			con = getConnection();
			NoteDAO.getInstance().deleteById(con, profile.getDomainId(), id);
			if (text.length() > 0) {
				ONote onote = new ONote(profile.getDomainId(), id, text);
				NoteDAO.getInstance().insert(con, onote);
				msg.setFlags(MailManager.getFlagNote(), true);
			} else {
				msg.setFlags(MailManager.getFlagNote(), false);
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
		MailAccount account=getAccount(request);
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
			account.checkStoreConnected();
			FolderCache mcache = null;
			Message m = null;
			if (providername == null) {
				mcache = account.getFolderCache(pfoldername);
                long newmsguid=Long.parseLong(puidmessage);
                m=mcache.getMessage(newmsguid);
			} else {
				mcache = fcProvided;
				m = mcache.getProvidedMessage(providername, providerid);
			}
			IMAPMessage im=(IMAPMessage)m;
			im.setPeek(us.isManualSeen());
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

			//boolean wasseen = m.isSet(Flags.Flag.SEEN);
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
 	            name=name.trim();
				if (psaveas==null) {
					int ix=name.lastIndexOf(".");
					if (ix>0) {
						//String ext=name.substring(ix+1);
						String xctype=ServletHelper.guessMediaType(name);
						if (xctype!=null) ctype=xctype;
					}
				}
				ServletUtils.setFileStreamHeaders(response, ctype, DispositionType.INLINE, name);
				if (providername==null) {
					Folder folder=mailData.getFolder();
					if (!folder.isOpen()) folder.open(Folder.READ_ONLY);
				}				
				InputStream is = part.getInputStream();
				OutputStream out = response.getOutputStream();
				fastStreamCopy(is, out);
				is.close();
				out.close();
				//if(!wasseen){
				//   if (us.isManualSeen()) {
				//	m.setFlag(Flags.Flag.SEEN, false);
				//   }
				//}
			}	
			im.setPeek(false);
			
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public void processGetAttachments(HttpServletRequest request, HttpServletResponse response) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pids[] = request.getParameterValues("ids");
		String providername = request.getParameter("provider");
		String providerid = request.getParameter("providerid");
		
		try {
			account.checkStoreConnected();
			FolderCache mcache = null;
			Message m = null;
			if (providername == null) {
				mcache = account.getFolderCache(pfoldername);
                long newmsguid=Long.parseLong(puidmessage);
                m=mcache.getMessage(newmsguid);
			} else {
				mcache = fcProvided;
				m = mcache.getProvidedMessage(providername, providerid);
			}
			IMAPMessage im=(IMAPMessage)m;
			im.setPeek(us.isManualSeen());
			HTMLMailData mailData = mcache.getMailData((MimeMessage) m);
			String name = m.getSubject();
			if (StringUtils.isEmpty(name)) {
				name = "attachments";
			}
			try {
				name = MailUtils.decodeQString(name);
			} catch (Exception exc) {
			}
			name += ".zip";
			//prepare hashmap to hold already used pnames
			HashMap<String,String> pnames=new HashMap<String,String>();
			ServletUtils.setFileStreamHeaders(response, "application/x-zip-compressed", DispositionType.INLINE, name);
			ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
			byte[] b = new byte[64 * 1024];
			for (String pid : pids) {
				Part part = mailData.getAttachmentPart(Integer.parseInt(pid));
				String pname = part.getFileName();
				if (pname == null) {
					pname = "unknown";
				}
				/*
				try {
					pname = MailUtils.decodeQString(pname, "iso-8859-1");
				} catch (Exception exc) {
				}
				*/
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
								
				ZipEntry ze = new ZipEntry(rpname);
				zos.putNextEntry(ze);
				if (providername == null) {
					Folder folder = mailData.getFolder();
					if (!folder.isOpen()) {
						folder.open(Folder.READ_ONLY);
					}
				}
				InputStream is = part.getInputStream();
				int len = 0;
				while ((len = is.read(b)) != -1) {
					zos.write(b, 0, len);
				}
				is.close();
				
				//remember used pname
				pnames.put(rpname, rpname);
			}
			zos.closeEntry();
			zos.flush();
			zos.close();
			
			im.setPeek(false);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
		}
	}
	
	public void processPreviewAttachment(HttpServletRequest request, HttpServletResponse response) {
		try {
			MailAccount account=getAccount(request);
			account.checkStoreConnected();
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
				String ctype = ServletHelper.guessMediaType(upl.getFilename(),upl.getMediaType());
				ServletUtils.setFileStreamHeaders(response, ctype,DispositionType.INLINE,upl.getFilename());
				
				InputStream is = new FileInputStream(upl.getFile());
				OutputStream oout = response.getOutputStream();
				fastStreamCopy(is, oout);
			} else {
				Service.logger.debug("uploadId was not valid!");
			}
		} catch (Exception exc) {
			logger.error("Error in PreviewAttachment", exc);
		}
	}
	
	public void processDocPreviewAttachment(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailAccount account=getAccount(request);
			account.checkStoreConnected();
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
				
				String fileHash=AlgoUtils.md5Hex(new CompositeId("previewattach",uploadId).toString());
				AttachmentViewerDocumentHandler docHandler = new AttachmentViewerDocumentHandler(false, getEnv().getProfileId(), fileHash, upl.getFile());
				DocEditorManager.EditingResult result = getWts().docEditorPrepareEditing(docHandler, upl.getFilename(), upl.getFile().lastModified());
				
				new JsonResult(result).printTo(out);
				
				
			} else {
				Service.logger.debug("uploadId was not valid!");
			}
		} catch (Exception exc) {
			logger.error("Error in PreviewAttachment", exc);
		}
	}
	
	//view through onlyoffice doc viewer
	public void processViewAttachment(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pidattach = request.getParameter("idattach");
		String providername = request.getParameter("provider");
		String providerid = request.getParameter("providerid");
		String pcid = request.getParameter("cid");
		String purl = request.getParameter("url");
		String punknown = request.getParameter("unknown");
		
		try {
			account.checkStoreConnected();
			FolderCache mcache = null;
			Message m = null;
			if (providername == null) {
				mcache = account.getFolderCache(pfoldername);
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
				String name=part.getFileName();
				if (name==null) name="";
				try {
					name=MailUtils.decodeQString(name);
				} catch(Exception exc) {
				}
 	            name=name.trim();
				if (providername==null) {
					Folder folder=mailData.getFolder();
					if (!folder.isOpen()) folder.open(Folder.READ_ONLY);
				}				
				
				String fileHash=AlgoUtils.md5Hex(new CompositeId(pfoldername,puidmessage,pidattach).toString());
				long lastModified=m.getReceivedDate().getTime();
				AttachmentViewerDocumentHandler docHandler = new AttachmentViewerDocumentHandler(false, getEnv().getProfileId(), fileHash, part, lastModified);
				DocEditorManager.EditingResult result = getWts().docEditorPrepareEditing(docHandler, name, lastModified);
				
				new JsonResult(result).printTo(out);
				
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
		MailAccount account=getAccount(request);
		String pcalaction = request.getParameter("calaction");
		String pfoldername = request.getParameter("folder");
		String puidmessage = request.getParameter("idmessage");
		String pidattach = request.getParameter("idattach");
		UserProfile.Data pdata = WT.getUserData(environment.getProfileId());
		
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
            long newmsguid = Long.parseLong(puidmessage);
            Message m = mcache.getMessage(newmsguid);
            HTMLMailData mailData = mcache.getMailData((MimeMessage)m);
			
			//CalendarMethod calendarMethod = mailData.getParsedMimeMessageComponents().getCalendarMethod();
			ICalendarManager cm = (ICalendarManager)WT.getServiceManager("com.sonicle.webtop.calendar", true, environment.getProfileId());
			
			// Parse Calendar content
			net.fortuna.ical4j.model.Calendar iCal = null;
			try {
				Part part = mailData.getAttachmentPart(Integer.parseInt(pidattach));
				if (part != null) {
					try (InputStream is = part.getInputStream()) {
						iCal = ICalendarUtils.parse(is);
					}
				}
			} catch (IOException | ParserException ex) {
				throw new WTException(ex);
			}
			
			// For actions evaluation related to invitations, the above impl. bases
			// its logic on the presence of an attachment equal to the invitation part 
			// in message structure: if the attachment is not present, accept/cancel/update
			// operations will not work! Fortunately adding the attachment is a common practice in invitation requests.
			// Vice-versa, for imports, the attachment part will be enought; the action is done a specific attachment.
			// Maybe in future we should separate these paths in two handling methods.
			/*
			try {
				String calendarContent = mailData.getParsedMimeMessageComponents().getCalendarContent();
				if (!StringUtils.isBlank(calendarContent)) {
					iCal = ICalendarUtils.parse(mailData.getParsedMimeMessageComponents().getCalendarContent());
				}
			} catch (IOException | ParserException ex) {}
			*/
			///////////////////////////////////////////////////////////////

			Integer calendarId = cm.getDefaultCalendarId();
			// Overrides default calendar if shared: this kind of events needs to
			// be saved into personal calendars.
			if (!cm.listMyCalendarIds().contains(calendarId)) {
				calendarId = cm.getBuiltInCalendarId();
			}

			if (pcalaction.equals("accept")) {
				Event ev = cm.addEventFromICal(calendarId, iCal);
				String ekey = cm.getEventInstanceKey(ev.getEventId());

				final List<InternetAddress> tos = MimeMessageParser.parseToAddresses((MimeMessage)m, true);
				// in case mail was sent as ccn we don't have any recipient, so we don't send any reply
				if (!tos.isEmpty()) {
					final String prodId = ICalendarUtils.buildProdId(WT.getPlatformName() + " Mail");
					final InternetAddress iaOrganizer = ICalendarUtils.getOrganizerAddress(ICalendarUtils.getVEvent(iCal));
					final EmailMessage email = ICalendarHelper.prepareICalendarReply(prodId, iCal, tos.get(0), iaOrganizer, PartStat.ACCEPTED, pdata.getLocale());
					final String sentFolder = getSentFolder(tos.get(0));
					WT.sendEmailMessage(environment.getProfileId(), email, sentFolder);
				}
				new JsonResult(ekey).printTo(out);

			} else if (pcalaction.equals("import")) {
				Event ev = cm.addEventFromICal(calendarId, iCal);
				String ekey = cm.getEventInstanceKey(ev.getEventId());
				new JsonResult(ekey).printTo(out);

			} else if (pcalaction.equals("cancel") || pcalaction.equals("update")) {
				cm.updateEventFromICal(iCal);
				new JsonResult().printTo(out);

			} else {
				throw new Exception("Unsupported calendar request action : " + pcalaction);
			}
			
			/*
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
			long newmsguid = Long.parseLong(puidmessage);
			Message m = mcache.getMessage(newmsguid);
			HTMLMailData mailData = mcache.getMailData((MimeMessage)m);
			Part part = mailData.getAttachmentPart(Integer.parseInt(pidattach));

			ICalendarRequest ir = new ICalendarRequest(part.getInputStream());
			ICalendarManager cm = (ICalendarManager)WT.getServiceManager("com.sonicle.webtop.calendar", true, environment.getProfileId());
			
			Integer calendarId = cm.getDefaultCalendarId();
			// Overrides default calendar if shared: this kind of events needs to
			// be saved into personal calendars.
			if (!cm.listMyCalendarIds().contains(calendarId)) {
				calendarId = cm.getBuiltInCalendarId();
			}
			
			if (pcalaction.equals("accept")) {
				Event ev = cm.addEventFromICal(calendarId, ir.getCalendar());
				String ekey = cm.getEventInstanceKey(ev.getEventId());
				// in case mail was sent as ccn we don't have any recipient
				// so we don't send any reply
				if (m.getRecipients(RecipientType.TO)!=null)
					sendICalendarReply(account, ir, ((InternetAddress)m.getRecipients(RecipientType.TO)[0]), PartStat.ACCEPTED);
				new JsonResult(ekey).printTo(out);
				
			} else if (pcalaction.equals("import")) {
				Event ev = cm.addEventFromICal(calendarId, ir.getCalendar());
				String ekey = cm.getEventInstanceKey(ev.getEventId());
				new JsonResult(ekey).printTo(out);
				
			} else if (pcalaction.equals("cancel") || pcalaction.equals("update")) {
				cm.updateEventFromICal(ir.getCalendar());
				new JsonResult().printTo(out);
				
			} else {
				throw new Exception("Unsupported calendar request action : " + pcalaction);
			}
			*/
		} catch (Exception exc) {
			new JsonResult(exc).printTo(out);
			logger.error("Error sending " + pcalaction, exc);
		}
	}
	
    public void processDeclineInvitation(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
        String pfoldername=request.getParameter("folder");
        String puidmessage=request.getParameter("idmessage");
        String pidattach=request.getParameter("idattach");
		UserProfile.Data pdata = WT.getUserData(environment.getProfileId());
		
        try {
            account.checkStoreConnected();
            FolderCache mcache = account.getFolderCache(pfoldername);
            long newmsguid = Long.parseLong(puidmessage);
            Message m = mcache.getMessage(newmsguid);
            HTMLMailData mailData = mcache.getMailData((MimeMessage)m);
			
			CalendarMethod calendarMethod = mailData.getParsedMimeMessageComponents().getCalendarMethod();
			if (CalendarMethod.REQUEST.equals(calendarMethod)) {
				// Parse Calendar content
				final net.fortuna.ical4j.model.Calendar iCal;
				try {
					iCal = ICalendarUtils.parse(mailData.getParsedMimeMessageComponents().getCalendarContent());
				} catch (IOException | ParserException ex) {
					throw new WTException(ex);
				}
				
				final String prodId = ICalendarUtils.buildProdId(WT.getPlatformName() + " Mail");
				final List<InternetAddress> tos = MimeMessageParser.parseToAddresses((MimeMessage)m, true);
				final InternetAddress iaOrganizer = ICalendarUtils.getOrganizerAddress(ICalendarUtils.getVEvent(iCal));
				final EmailMessage email = ICalendarHelper.prepareICalendarReply(prodId, iCal, tos.get(0), iaOrganizer, PartStat.DECLINED, pdata.getLocale());
				final String sentFolder = getSentFolder(tos.get(0));
				WT.sendEmailMessage(environment.getProfileId(), email, sentFolder);
				
			} else {
				throw new WTException("Cannod decline a NON request");
			}
			
            //Part part=mailData.getAttachmentPart(Integer.parseInt(pidattach));
			//ICalendarRequest ir=new ICalendarRequest(part.getInputStream());
			//sendICalendarReply(account, ir, ((InternetAddress)m.getRecipients(RecipientType.TO)[0]), PartStat.DECLINED);
			new JsonResult().printTo(out);
        } catch(Exception exc) {
            new JsonResult(false,exc.getMessage()).printTo(out);
			logger.error("Error sending decline", exc);
        }        
        
	}
	
/*	private void sendICalendarReply(InternetAddress forAddress, PartStat response, net.fortuna.ical4j.model.Calendar cal, String summary, String organizerAddress) throws Exception {
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

			jakarta.mail.internet.MimeBodyPart part2 = new jakarta.mail.internet.MimeBodyPart();
			part2.setContent(icalContent, MailUtils.buildPartContentType(icalContentType, "UTF-8"));
			part2.setHeader("Content-Transfer-Encoding", "8BIT");
			//part2.setFileName("webtop-reply.ics");
			//jakarta.mail.internet.MimeBodyPart part1 = new jakarta.mail.internet.MimeBodyPart();
			//part1.setText(content, "UTF8", "application/ics");
			//part1.setHeader("Content-type", "application/ics");
			//part1.setFileName("webtop-reply.ics");

			MimeMultipart mp = new MimeMultipart("mixed");
			mp.addBodyPart(part2);
			MimeBodyPart mbp=new MimeBodyPart();
			mbp.setHeader("Content-type", "multipart/mixed");
			mbp.setContent(mp);

			smsg.setAttachments(new jakarta.mail.Part[]{mbp});

			Exception exc=sendMsg(profile.getFullEmailAddress(), smsg, null);
			if (exc!=null) throw exc;
		}
		
	}*/
	
	/*
	private void sendICalendarReply(MailAccount account, ICalendarRequest request, InternetAddress forAddress, PartStat response) throws Exception {
		InternetAddress organizerAddress = InternetAddressUtils.toInternetAddress(request.getOrganizerAddress());
		sendICalendarReply(account, request.getCalendar(), organizerAddress, forAddress, response, request.getSummary());
	}
	
	private void sendICalendarReply(MailAccount account, net.fortuna.ical4j.model.Calendar ical, InternetAddress organizerAddress, InternetAddress forAddress, PartStat response, String eventSummary) throws Exception {
		String prodId = ICalendarUtils.buildProdId(WT.getPlatformName() + " Mail");
		net.fortuna.ical4j.model.Calendar icalReply = ICalendarUtils.buildInvitationReply(ical, prodId, forAddress, response);
		
		// Creates base message parts
		net.fortuna.ical4j.model.property.Method icalMethod = net.fortuna.ical4j.model.property.Method.REPLY;
		String icalText = ICalendarUtils.calendarToString(icalReply);
		MimeBodyPart calPart = ICalendarUtils.createInvitationCalendarPart(icalMethod, icalText);
		String filename = ICalendarUtils.buildICalendarAttachmentFilename(WT.getPlatformName());
		MimeBodyPart attPart = ICalendarUtils.createInvitationAttachmentPart(icalText, filename);
		
		MimeMultipart mmp = ICalendarUtils.createInvitationPart(null, calPart, attPart);
		
		UserProfile.Data ud = WT.getUserData(getEnv().getProfileId());
		InternetAddress from = InternetAddressUtils.toInternetAddress(ud.getFullEmailAddress());
		Message message = createMessage(account, from, TplHelper.buildEventInvitationReplyEmailSubject(ud.getLocale(), response, eventSummary));
		message.addRecipient(RecipientType.TO, organizerAddress);
		message.setContent(mmp);
		
		sendMsg(message);
	}
	*/
	
	private MimeMessage createMessage(MailAccount account, InternetAddress from, String subject) throws MessagingException {
		try {
			subject = MimeUtility.encodeText(subject);
		} catch (Exception ex) {}
		
		MimeMessage message = new MimeMessage(account.getMailSession());
		message.setSubject(subject);
		message.addFrom(new InternetAddress[] {from});
		message.setSentDate(new java.util.Date());
		return message;
	}	
	
    public void processUpdateCalendarReply(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account=getAccount(request);
        String pfoldername=request.getParameter("folder");
        String puidmessage=request.getParameter("idmessage");
        String pidattach=request.getParameter("idattach");
        //UserProfile profile=environment.getUserProfile();
        try {
            account.checkStoreConnected();
            FolderCache mcache=account.getFolderCache(pfoldername);
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
	
	boolean checkFileRules(String foldername) {
		return cacheFoldersNamesInByFileFilters.contains(foldername);
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
			
			MailAccount account=getAccount(request);
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
				ast = new AdvancedSearchThread(this, account, folder, trashspam, subfolders, and, entries);
			} else {
				int folderType = folder.equals("personal") ? AdvancedSearchThread.FOLDERTYPE_PERSONAL
						: folder.equals("shared") ? AdvancedSearchThread.FOLDERTYPE_SHARED
								: AdvancedSearchThread.FOLDERTYPE_ALL;
				ast = new AdvancedSearchThread(this, account, folderType, trashspam, subfolders, and, entries);
			}
			ast.start();
			new JsonResult().printTo(out);
		} catch (Throwable t) {
			new JsonResult(t).printTo(out);
			Service.logger.error("Exception", t);
		}
	}
	
	public void processPollAdvancedSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailAccount account=getAccount(request);
			String sstart = request.getParameter("start");
			int start = 0;
			ArrayList<JsAdvSearchMessage> items = new ArrayList<>();
			
			if (sstart != null) {
				start = Integer.parseInt(sstart);
			}
			
			if (ast != null) {
				UserProfile profile = environment.getProfile();
				Locale locale = profile.getLocale();
				java.util.Calendar cal = java.util.Calendar.getInstance(locale);
				ArrayList<Message> msgs = ast.getResult();
				int totalrows = msgs.size();
				int newrows = totalrows - start;
				
				for (int i = start; i < msgs.size(); ++i) {
					Message xm = msgs.get(i);
					if (xm.isExpunged()) {
						continue;
					}
					IMAPFolder xmfolder=(IMAPFolder)xm.getFolder();
					boolean wasopen=xmfolder.isOpen();
					if (!wasopen) xmfolder.open(Folder.READ_ONLY);
                    long nuid=xmfolder.getUID(xm);
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
					FolderCache fc = account.getFolderCache(xfolder);
					String folder = xfolder;
					String foldername = StringEscapeUtils.escapeHtml4(getInternationalFolderName(fc));
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
					from = (from == null ? "" : StringEscapeUtils.escapeHtml4(from));
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
					to = (to == null ? "" : StringEscapeUtils.escapeHtml4(to));
					//Subject
					String subject = m.getSubject();
					if (subject != null) {
						try {
							subject = MailUtils.decodeQString(subject);
						} catch (Exception exc) {
							
						}
					}
					subject = (subject == null ? "" : StringEscapeUtils.escapeHtml4(subject));
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
						//String tbflagstring=webtopFlag.tbLabel;
						if (!flagstring.equals("complete")) {
							String oldflagstring="flag"+flagstring;
							if (flags.contains(flagstring)
									||flags.contains(oldflagstring)
									/*|| (tbflagstring!=null && flags.contains(tbflagstring))*/
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
					boolean archived = false;
					if (hasDmsDocumentArchiving()) {
						archived=m.getHeader("X-WT-Archived")!=null;
						if (!archived) {
							archived=flags.contains(MailManager.getFlagDmsArchivedString());
						}
					}
					
					boolean hasNote=flags.contains(MailManager.getFlagNoteString());

					boolean hasAttachments=fc.hasAttachments(xm, null);
					
					String fullfolderdesc=fc.getDescription();
					FolderCache xfc=fc;
					char sep=xfc.getFolder().getSeparator();
					while(xfc.getParent()!=null) {
						xfc=xfc.getParent();
						String desc=xfc.getDescription();
						if (!StringUtils.isEmpty(desc)) fullfolderdesc=desc+sep+fullfolderdesc;
					}
					
					items.add(new JsAdvSearchMessage(folder, foldername, fullfolderdesc, folder + "|" + nuid, nuid, priority, status, to, from, subject, formatCalendarDate(yyyy, mm, dd, hhh, mmm, sss), unread, msgsize, cflag, archived, isToday, hasNote, hasAttachments));
					
					if (!wasopen) xmfolder.close(false);
				}								
				new JsonResult("messages", items)
						.setTotal(totalrows)
						.setStart(start)
						.setLimit(newrows)
						.set("progress", ast.getProgress())
						.set("curfoldername", ast.getCurrentFolderInternationalName())
						.set("max", ast.isMoreThanMax())
						.set("finished", (ast.isFinished() || ast.isCanceled() || !ast.isRunning()))
						.printTo(out);
			} else {
				new JsonResult("messages", items)
						.setTotal(0)
						.setStart(0)
						.setLimit(0)
						.printTo(out, false);
			}
		} catch (Throwable t) {
			Service.logger.error("Exception", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processCancelAdvancedSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		if (ast != null && ast.isRunning()) {
			ast.cancel();
		}
		new JsonResult().printTo(out);
	}

	public void processRunSmartSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			if (sst!=null && sst.isRunning())
				sst.cancel();
			
			UserProfile profile = environment.getProfile();
			MailAccount account=getAccount(request);
			String pattern=ServletUtils.getStringParameter(request, "pattern", true);
			String folder=ServletUtils.getStringParameter(request, "folder", false);
			boolean trashspam=ServletUtils.getBooleanParameter(request, "trashspam", false);
			boolean fromme=ServletUtils.getBooleanParameter(request, "fromme", false);
			boolean tome=ServletUtils.getBooleanParameter(request, "tome", false);
			boolean attachments=ServletUtils.getBooleanParameter(request, "attachments", false);
			int year=ServletUtils.getIntParameter(request, "year", 0);
			int month=ServletUtils.getIntParameter(request, "month", 0);
			int day=ServletUtils.getIntParameter(request, "day", 0);
			ArrayList<String> ispersonfilters=ServletUtils.getStringParameters(request, "ispersonfilters");
			ArrayList<String> isnotpersonfilters=ServletUtils.getStringParameters(request, "isnotpersonfilters");
			ArrayList<String> isfolderfilters=ServletUtils.getStringParameters(request, "isfolderfilters");
			ArrayList<String> isnotfolderfilters=ServletUtils.getStringParameters(request, "isnotfolderfilters");
			Set<String> _folderIds=account.getFolderCacheKeys();
			
			ArrayList<SearchTerm> terms = new ArrayList<>();
			SearchTerm searchTerm = null;
				
			ArrayList<String> folderIds=new ArrayList<>();
			String firstFolders[]={};
			if (folder==null || folder.trim().length()==0) {
				firstFolders=new String[] {account.getInboxFolderFullName(), account.getFolderSent()};
				for(String folderId: firstFolders) folderIds.add(folderId);
			}
			for(String folderId: _folderIds) {
				
				//if folder selected, look only under that folder
				if (folder!=null && folder.trim().length()>0) {
					if (!folder.equals(folderId) && !account.isUnderFolder(folder, folderId))
						continue;
				} else {
					//else skip shared
					if (account.isUnderSharedFolder(folderId)) continue;
				}
				
				//skip trash & spam unless selected
				if (!trashspam && (account.isTrashFolder(folderId)||account.isSpamFolder(folderId))) continue;
				
				boolean skip=false;
				for(String skipfolder: firstFolders) {
					if (skipfolder.equals(folderId)) {
						skip=true;
						break;
					}
				}
				if (!skip) folderIds.add(folderId);
			}
			
			terms.add(new OrTerm(ImapQuery.toAnySearchTerm(pattern)));
			int n = terms.size();
			
			if(n == 1)
				searchTerm = terms.get(0);
			else if (n>1) {
				SearchTerm vterms[] = new SearchTerm[n];
					terms.toArray(vterms);
					searchTerm = new AndTerm(vterms);
			}
			
			sst = new SmartSearchThread(this ,account, folderIds, fromme, tome, attachments,
				ispersonfilters, isnotpersonfilters, isfolderfilters, isnotfolderfilters,
				year, month, day, new ImapQuery(searchTerm, false));
			sst.start();
			new JsonResult().printTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false,exc.getMessage()).printTo(out);
		}
	}
	
	public void processPollSmartSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			if (sst!=null) {
				JsSmartSearchTotals jssst=sst.getSmartSearchTotals();
				synchronized(jssst.lock) {
					JsonResult jsr=new JsonResult(jssst);
					jsr.printTo(out);
				}
			}
			else new JsonResult(false,"Smart search is not available").printTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false,exc.getMessage()).printTo(out);
		}
	}
	
	public void processCancelSmartSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		if (sst != null && sst.isRunning()) {
			sst.cancel();
		}
		new JsonResult().printTo(out);
	}

	public void processPortletRunSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			UserProfile profile = environment.getProfile();
			boolean trash=false;
			boolean spam=false;
			ArrayList<SearchTerm> terms = new ArrayList<SearchTerm>();
			SearchTerm searchTerm = null;	
			
			if (pst!=null && pst.isRunning())
				pst.cancel();
			
			MailAccount account=getAccount(request);
			
			String pattern = ServletUtils.getStringParameter(request, "pattern", true);
			
			Set<String> _folderIds=account.getFolderCacheKeys();
			
			//sort folders, placing first interesting ones
			ArrayList<String> folderIds=new ArrayList<>();
			Collections.sort(folderIds);
			String firstFolders[]={account.getInboxFolderFullName(), account.getFolderSent()};
			for(String folderId: firstFolders) folderIds.add(folderId);
			for(String folderId: _folderIds) {
				
				if (account.isUnderSharedFolder(folderId)) continue;
				//skip trash & spam unless selected
				if (!trash && account.isTrashFolder(folderId)) continue;
				if (!spam && account.isSpamFolder(folderId)) continue;
				
				folderIds.add(folderId);
			}
			
			terms.add(new OrTerm(ImapQuery.toAnySearchTerm(pattern)));
			int n = terms.size();
			
			if(n == 1)
				searchTerm = terms.get(0);
			else if (n>1) {
				SearchTerm vterms[] = new SearchTerm[n];
					terms.toArray(vterms);
					searchTerm = new AndTerm(vterms);
			}
			pst = new PortletSearchThread(this, account, folderIds, new ImapQuery(searchTerm, false));
			pst.start();
			new JsonResult().printTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false,exc.getMessage()).printTo(out);
		}
	}
	
	public void processPortletPollSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			if (pst!=null) {
				JsPortletSearchResult jspsr=pst.getPortletSearchResult();
				synchronized(jspsr.lock) {
					JsonResult jsr=new JsonResult(jspsr);
					jsr.printTo(out);
				}
			}
			else new JsonResult(false,"Portlet search is not available").printTo(out);
		} catch (Exception exc) {
			Service.logger.error("Exception",exc);
			new JsonResult(false,exc.getMessage()).printTo(out);
		}
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
	
	public boolean isSharedSeen(MailAccount account) throws MessagingException {
		if (!account.hasAnnotations()) return false;
		
		SonicleIMAPFolder xfolder = (SonicleIMAPFolder) account.getFolder("INBOX");
		String annot = xfolder.getAnnotation("/vendor/cmu/cyrus-imapd/sharedseen", true);
		return annot.equals("true");
	}
	
	public void setSharedSeen(MailAccount account, boolean b) throws MessagingException {
		if (!account.hasAnnotations()) return;
		SonicleIMAPFolder xfolder = (SonicleIMAPFolder) account.getFolder("INBOX");
		xfolder.setAnnotation("/vendor/cmu/cyrus-imapd/sharedseen", true, b ? "true" : "false");
	}

	protected boolean schemeWantsUserWithDomain(AuthenticationDomain ad) {
		//String scheme=ad.getDirUri().getScheme();
		////return scheme.equals("ldapneth")?false:scheme.equals("ad")?true:scheme.startsWith("ldap");
		//return scheme.equals("ad")||scheme.startsWith("ldap");
		return MailSettings.ACLDomainSuffixPolicy.APPEND.equals(ss.getACLDomainSuffixPolicy(ad.getDirUri().getScheme()));
	}
	
	UserProfileId  aclUserIdToUserId(String aclUserId) {
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
					if (!RunContext.isPermitted(true, SERVICE_ID, "SHARING_UNKNOWN_ROLES","SHOW")) userId=null;
				}
			} else {
				if (!RunContext.isPermitted(true, SERVICE_ID, "SHARING_UNKNOWN_ROLES","SHOW")) userId=null;
			}
		}
		if (principal.getUserId().equals(userId)) userId=null;
		
		UserProfileId pid=null;
		if (userId!=null) pid=new UserProfileId(environment.getProfile().getDomainId(),userId);
		return pid;
	}

	public void processManageSharing(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con=null;
		try {
			con=getConnection();
			MailAccount account=getAccount(request);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			String id=null;
			Payload<MapItem, JsSharing> pl=null;
			
			if(crud.equals(Crud.READ)) {
				id = ServletUtils.getStringParameter(request, "id", true);
			} else if(crud.equals(Crud.UPDATE)) {
				pl = ServletUtils.getPayload(request, JsSharing.class);
				id=pl.data.id;
			}
			
			CoreManager core = WT.getCoreManager();
			FolderCache fc = null;
			String description = null;
			String method = "this";
			ArrayList<JsSharing.SharingRights> rights = new ArrayList<>();

			if(id.equals("/")) {
				id="INBOX";
				method = "all";
			}
			
			fc = account.getFolderCache(id);
			SonicleIMAPFolder folder=(SonicleIMAPFolder)fc.getFolder();
			description = folder.getName();
			Map<String, Sharing.SubjectConfiguration> sconfigurations=core.getShareSubjectConfiguration(SERVICE_ID, MailManager.IDENTITY_SHARING_CONTEXT, environment.getProfileId(), "*", MailManager.IDENTITY_PERMISSION_KEY, FolderShareParameters.class);
			for(ACL acl : folder.getACL()) {
				String aclUserId=acl.getName();
				UserProfileId pid=aclUserIdToUserId(aclUserId);
				if (pid==null) continue;
				String roleUid=null;
				try {
					roleUid=core.lookupUserSid(pid);
				} catch(WTException exc) {
				}
				String roleDescription=null;
				FolderShareParameters fsp=null;
				if (roleUid!=null) {
					Sharing.SubjectConfiguration sconfiguration=sconfigurations.get(roleUid);
					if (sconfiguration!=null) fsp=sconfiguration.getTypedData(FolderShareParameters.class);
				}
				boolean shareIdentity=false;
				boolean forceMailcard=false;
				boolean alwaysCc=false;
				String alwaysCcEmail=null;
				if (roleUid==null) { 
					if (!RunContext.isPermitted(true, SERVICE_ID, "SHARING_UNKNOWN_ROLES","SHOW")) continue;
					roleUid=aclUserId; 
					roleDescription=roleUid; 
				} else {
					if (fsp!=null) {
						shareIdentity=fsp.shareIdentity;
						forceMailcard=fsp.forceMailcard;
						alwaysCc=fsp.alwaysCc;
						alwaysCcEmail=fsp.alwaysCcEmail;
					}
					String dn;
					UserProfile.Data pdata=WT.getProfileData(pid);
					if (pdata!=null) dn=pdata.getDisplayName();
					else dn="no description available";
					roleDescription=pid.getUserId()+" ["+dn+"]";
				}

				Rights ar = acl.getRights();
				rights.add(new JsSharing.SharingRights(
						id, 
						roleUid,
						roleDescription,
						aclUserId,
						shareIdentity,
						forceMailcard,
						alwaysCc,
						alwaysCcEmail,
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
			JsSharing sharing=new JsSharing(id, description, method, rights);
			
			if(crud.equals(Crud.READ)) {
				new JsonResult(sharing).printTo(out);			
			} else if(crud.equals(Crud.UPDATE)) {
				for(JsSharing.SharingRights sr: pl.data.rights) {
					//try to fill in the imapId where empty
					if (StringUtils.isEmpty(sr.imapId)) {
						UserProfileId pid=core.userUidToProfileId(sr.roleUid);
						String imapId=null;
						//look for any custom mail user
						OUserMap userMap=UserMapDAO.getInstance().selectById(con, pid.getDomainId(), pid.getUserId());
						if (userMap!=null && !StringUtils.isEmpty(userMap.getMailUser())) {
							if (userMap.getMailHost().equals(account.getHost()) && 
									userMap.getMailPort().equals(account.getPort()) && 
									userMap.getMailProtocol().equals(account.getProtocol())) {
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
				
				//
				//Prepare new sharing structure
				//
				
				//first delete all removed roles
				for(JsSharing.SharingRights sr: sharing.rights) {
					String imapId=ss.isImapAclLowercase()?sr.imapId.toLowerCase(): sr.imapId ;
					if (!pl.data.hasImapId(sr.imapId)) {
						logger.debug("Folder ["+foldername+"] - remove acl for "+imapId+" recursive="+recursive);
						account.removeFolderSharing(foldername,imapId,recursive);
						//updateIdentitySharingRights(newwtrights,sr.roleUid,false,false,false);
					}
				}
				
				//now apply new acls
				Set<Sharing.SubjectConfiguration> setconfigurations=new LinkedHashSet<>();
				for(JsSharing.SharingRights sr: pl.data.rights) {
					String imapId=ss.isImapAclLowercase()?sr.imapId.toLowerCase(): sr.imapId ;
					if (!StringUtils.isEmpty(sr.imapId)) {
						String strrights=sr.toString();
						logger.debug("Folder ["+foldername+"] - add acl "+strrights+" for "+imapId+" recursive="+recursive);
						account.setFolderSharing(foldername, imapId, strrights, recursive);
						//updateIdentitySharingRights(newwtrights,sr.roleUid,sr.shareIdentity,sr.forceMailcard,sr.alwaysCc);

						FolderShareParameters fsp=new FolderShareParameters();
						fsp.shareIdentity=sr.shareIdentity;
						fsp.forceMailcard=sr.forceMailcard;
						fsp.alwaysCc=sr.alwaysCc;
						fsp.alwaysCcEmail=sr.alwaysCcEmail;

						setconfigurations.add(new Sharing.SubjectConfiguration(sr.roleUid, LangUtils.asSet("READ"), fsp));
					}
				}
				
				core.updateShareConfigurations(SERVICE_ID, MailManager.IDENTITY_SHARING_CONTEXT, environment.getProfileId(), "*", MailManager.IDENTITY_PERMISSION_KEY, setconfigurations, FolderShareParameters.class);
/*				for(JsSharing.SharingRights sr: pl.data.rights) {
					FolderShareParameters fsp=new FolderShareParameters();
					fsp.shareIdentity=sr.shareIdentity;
					fsp.forceMailcard=sr.forceMailcard;
					fsp.alwaysCc=sr.alwaysCc;
					fsp.alwaysCcEmail=sr.alwaysCcEmail;
					core.updateShareData(
							core.lookupUserProfileIdBySid(sr.roleUid), 
							SERVICE_ID, 
							MailManager.IDENTITY_SHARING_CONTEXT, 
							environment.getProfileId(), 
							"*", 
							fsp, 
							FolderShareParameters.class, 
							false);
				}*/
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ManageSharing", ex);
			new JsonResult(false, "Error").printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
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
				//remove @domain if present
				mailUserId=StringUtils.substringBefore(mailUserId, "@");
				logger.debug("mapping not found, looking for a webtop user with id = {}",mailUserId);
				//try looking for a webtop user with userId=mailUserId
				ouser=UserDAO.getInstance().selectByDomainUser(con, domainId, mailUserId);
			}
			
			String desc=null;
			if (ouser!=null) {
				desc=LangUtils.value(ouser.getDisplayName(),"");
			} else {
				String email=mailUserId;
				if (email.indexOf("@")<0) email+="@"+WT.getPrimaryDomainName(domainId);
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
			
			if (mainAccount.hasDifferentDefaultFolder()) co.put("inboxFolder",mainAccount.getInboxFolderFullName());
			co.put("folderDrafts", us.getFolderDrafts());
			co.put("folderSent", us.getFolderSent());
			co.put("folderSpam", us.getFolderSpam());
			co.put("folderTrash", us.getFolderTrash());
			co.put("folderArchive", us.getFolderArchive());
			co.put("folderSeparator", mainAccount.getFolderSeparator());
			co.put("autoAddContact", us.isAutoAddContact());
			co.put("newMessageMaxRecipients", Math.max(0, ss.getNewMessageMaxRecipients()));
			co.put("attachmentMaxFileSize", ss.getAttachmentMaxFileSize(true));
			co.put("format", us.getFormat());
			co.put("fontName", us.getFontName());
			co.put("fontSize", us.getFontSize());
			co.put("fontColor", us.getFontColor());
			co.put("receipt", us.isReceipt());
			co.put("priority", us.isPriority());
			co.put("viewMode", EnumUtils.toSerializedName(us.getViewMode()));
			co.put("noMailcardOnReplyForward", us.isNoMailcardOnReplyForward());
			co.put("pageRows", us.getPageRows());
			co.put("schedDisabled",ss.isScheduledEmailsDisabled());
			co.put("identities", jsidents);
			co.put("gridAlwaysShowTime", us.getGridAlwaysShowTime());
			co.put("manualSeen",us.isManualSeen());
			co.put("seenOnOpen",us.isSeenOnOpen());
			co.put("messageViewRegion",us.getMessageViewRegion());
			co.put("messageViewWidth",us.getMessageViewWidth());
			co.put("messageViewHeight",us.getMessageViewHeight());
			co.put("messageViewCollapsed",us.getMessageViewCollapsed());
			co.put("messageViewMaxTos",ss.getMessageViewMaxTos());
			co.put("messageViewMaxCcs",ss.getMessageViewMaxCcs());
			co.put("messageEditSubject", ss.isMessageEditSubject());
			co.put("columnSizes",JsonResult.gson().toJson(us.getColumnSizes()));
			co.put("autoResponderActive", mailManager.isAutoResponderActive());
			co.put("showUpcomingEvents", us.getShowUpcomingEvents());
			co.put("showUpcomingTasks", us.getShowUpcomingTasks());
			co.put("isArchivingExternal", ss.isArchivingExternal());
			co.put("todayRowColor", us.getTodayRowColor());
			co.put("favoriteNotifications", us.isFavoriteNotifications());
			co.put("invitationTrashAfterAction", ss.isInvitationTrashAfterAction());
			co.put("invitationAskEditAfterAction", ss.isInvitationAskEditAfterAction());
			
			if (RunContext.isPermitted(true, SERVICE_ID, "FAX", "ACCESS")) {
				co.put("faxSubject", getEnv().getCoreServiceSettings().getFaxSubject());
			}
			
			List<MailSettings.ExternalProvider> providers=ss.getExternalProviders(getEnv().getCoreUserSettings().getUILookAndFeel());
			HashMap<String,String> providerIcons=new HashMap<>();
			for(MailSettings.ExternalProvider provider: providers) providerIcons.put(provider.id, provider.iconUrl);
			
			String extids="";
			for(MailAccount acct: externalAccounts) {
				String id=acct.getId();
				if (extids.length()>0) extids+=",";
				extids+=id;
				
				co.put("externalAccountDescription."+id,externalAccountsMap.get(id).getAccountDescription());
				co.put("externalAccountDrafts."+id,externalAccountsMap.get(id).getFolderDrafts());
				co.put("externalAccountSent."+id,externalAccountsMap.get(id).getFolderSent());
				co.put("externalAccountTrash."+id,externalAccountsMap.get(id).getFolderTrash());
				co.put("externalAccountSpam."+id,externalAccountsMap.get(id).getFolderSpam());
				co.put("externalAccountReadOnly."+id,externalAccountsMap.get(id).isReadOnly());
				co.put("externalAccountIcon."+id,providerIcons.get(externalAccountsMap.get(id).getProviderId()));
			}
			co.put("externalAccounts",extids);
			
			if (ss.isToolbarCompact()) co.put("toolbarCompact",true);
			
			
			co.put("hasAudit",mailManager.isAuditEnabled()&&(RunContext.isImpersonated()||RunContext.isPermitted(true, CoreManifest.ID, "AUDIT")));
			
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
				UserProfileId opid=id.getOriginPid();
				// In case of auto identities we need to build real mainfolder
				try {
					String mailUser = getMailUsername(opid);
					String mainfolder = getSharedFolderName(getAccount(id),mailUser, "INBOX" /*id.getMainFolder()*/);
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
				
				if (!id.isForceMailcard()) {
					mc=mailManager.getMailcard();
					opid=getEnv().getProfileId();
				}

				try {
					UserProfile.PersonalInfo upi = WT.getCoreManager().getUserPersonalInfo(opid);
					mc.substitutePlaceholders(upi);
				} catch (Exception ex) {
					logger.error("Unable to get auto identity personal info [{}]", id.getEmail(), ex);
				}
			}
                        else if(id.isType(Identity.TYPE_USER)) {
                            try {
                                    UserProfile.PersonalInfo upi = WT.getCoreManager().getUserPersonalInfo(mailManager.getTargetProfileId());
                                    mc.substitutePlaceholders(upi);			
                            } catch(WTException exc) {
                                    logger.error("cannot load user personal info",exc);
                            }
                        }
		}
		//mc.html=LangUtils.stripLineBreaks(mc.html);
		id.setMailcard(mc);
	}
	
	private String getMailUsername(UserProfileId userProfileId) {
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
		return username+"@"+WT.getPrimaryDomainName(userProfileId.getDomainId());
	}
	
	public void processLookupSieveScripts(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			items.add(new JsSimple(MailManager.SIEVE_WEBTOP_SCRIPT, MailManager.SIEVE_WEBTOP_SCRIPT));
			
			try {
				List<com.fluffypeople.managesieve.SieveScript> scripts = mailManager.listSieveScripts();
				for(com.fluffypeople.managesieve.SieveScript script : scripts) {
					// Skip new webtop script name, we want it as first element
					if (!StringUtils.equals(script.getName(), MailManager.SIEVE_WEBTOP_SCRIPT)) {
						items.add(new JsSimple(script.getName(), script.getName()));
					}
				}
			} catch(WTException ex1) {
				logger.warn("Unable to read scripts", ex1);
			}
				
			new JsonResult(items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in LookupSieveScripts", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public void processManageMailFilters(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			DateTimeZone profileTz = getEnv().getProfile().getTimeZone();
			
			if (crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				int scriptCount = -1;
				String activeScript = null;
				try {
					List<com.fluffypeople.managesieve.SieveScript> scripts = mailManager.listSieveScripts();
					scriptCount = scripts.size();
					activeScript = ManagerUtils.findActiveScriptName(scripts);
				} catch(WTException ex1) {
					logger.warn("Error reading active script", ex1);
				}
				if (StringUtils.isBlank(activeScript)) activeScript = MailManager.SIEVE_WEBTOP_SCRIPT;
				
				AutoResponder autoResp = mailManager.getAutoResponder();
				MailFiltersType type = EnumUtils.forSerializedName(id, MailFiltersType.class);
				List<MailFilter> filters = mailManager.getMailFilters(type, EnabledCond.ANY_STATE);
				
				JsInMailFilters js = new JsInMailFilters(scriptCount, activeScript, autoResp, filters, profileTz);
				
				new JsonResult(js).printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsInMailFilters> pl = ServletUtils.getPayload(request, JsInMailFilters.class);
				
				if (EnumUtils.equals(pl.data.id, MailFiltersType.INCOMING)) {
					mailManager.updateAutoResponder(pl.data.createAutoResponderForUpdate(profileTz));
					mailManager.updateMailFilters(pl.data.id, pl.data.createMailFiltersForUpdate());
					
					boolean isWTScript = StringUtils.equals(pl.data.activeScript, MailManager.SIEVE_WEBTOP_SCRIPT);
					if (!isWTScript && !StringUtils.isBlank(pl.data.activeScript)) {
						try {
							mailManager.activateSieveScript(pl.data.activeScript);
						} catch(WTException ex1) {
							logger.warn("Error activating chosen script", ex1);
						}
					}
					// Always generate a WT script but activate it 
					// automatically only if has been selected our script
					mailManager.applySieveScript(isWTScript);
				}	
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageFilters", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public void processBlockSenderAddress(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String address = ServletUtils.getStringParameter(request, "address", true);
			String spamFolder = us.getFolderSpam();
			
			List<MailFilter> filters = mailManager.getMailFilters(MailFiltersType.INCOMING, EnabledCond.ANY_STATE);
			boolean blockFilterFound = false;
			for (MailFilter filter : filters) {
				if (ManagerUtils.MAILFILTER_SENDERBLACKLIST_BUILTIN.equals(filter.getBuiltIn())) {
					blockFilterFound = true;
					ManagerUtils.fillSenderBlacklistMailFilterWithDefaults(filter, spamFolder);
					filter.getSieveRules().add(SieveRuleList.newRuleMatchFrom(address));
				}
			}
			if (blockFilterFound == false) {
				MailFilter filter = new MailFilter();
				ManagerUtils.fillSenderBlacklistMailFilterWithDefaults(filter, spamFolder);
				filter.getSieveRules().add(SieveRuleList.newRuleMatchFrom(address));
				filters.add(filter);
			}
			mailManager.updateMailFilters(MailFiltersType.INCOMING, filters);
			boolean isWTScript = StringUtils.equals(mailManager.getActiveSieveScriptName(), MailManager.SIEVE_WEBTOP_SCRIPT);
			mailManager.applySieveScript(isWTScript);
			
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in BlockSender", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processEditEmailSubject(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String newSubject = ServletUtils.getStringParameter(request, "subject", true);
			int messageId = ServletUtils.getIntParameter(request, "messageId", true);
			String currentFolder = ServletUtils.getStringParameter(request, "currentFolder", true);
			
			MailAccount account=getAccount(request);
			FolderCache folderCache = account.getFolderCache(currentFolder);
			Message currentMessage = folderCache.getMessage(messageId);
			String oldSubject = currentMessage.getSubject();
			
			MimeMessage newMessage = new MimeMessage((MimeMessage)currentMessage);
			newMessage.setSubject(newSubject);
			folderCache.appendMessage(newMessage);
			folderCache.deleteMessages(new long[]{messageId}, true);
			String srvMessageId = getMessageID(newMessage);
			
			if (mailManager.isAuditEnabled() && StringUtils.isNotEmpty(srvMessageId)) {
				HashMap<String, String> subjectMap = new HashMap<>();
				subjectMap.put("old", oldSubject);
				subjectMap.put("new", newSubject);
				
				mailManager.auditLogWrite(
					MailManager.AuditContext.MAIL,
					MailManager.AuditAction.RENAME,
					srvMessageId,
					JsonResult.gson().toJson(subjectMap)
				);
			}
			
			new JsonResult(true).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in EditEmailSubject", ex);
			new JsonResult(ex).printTo(out);
		}
	}

	public void processForwardRedirect(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Identity ident = mailManager.getMainIdentity();
		MailAccount account=getAccount(ident);
		String pfoldername = request.getParameter("folder");
		String messageId = request.getParameter("messageId");
		String to = request.getParameter("to");
		try {
			account.checkStoreConnected();
			FolderCache mcache = account.getFolderCache(pfoldername);
			MimeMessage src = (MimeMessage) mcache.getMessage(Long.parseLong(messageId)); 
			if (src.isExpunged()) throw new MessagingException("Message expunged");
			
			MimeMessage dst = new MimeMessage(src);
			
			dst.addHeader("Resent-From", ident.toString());
			dst.addHeader("Resent-To", to);
			dst.addHeader("Resent-Date", new MailDateFormat().format(new java.util.Date()));
			
			dst.setHeader("Message-ID", "<"+UniqueValue.getUniqueMessageIDValue(account.getMailSession())+">");
			
			SendException retexc = null;
			try {
				Transport.send(dst, new InternetAddress[] { new InternetAddress(to) });
			} catch (Exception ex) {
				Service.logger.error("Exception",ex);
				String exmsg = ex.getMessage();
				if (ex.getCause()!=null) exmsg = ex.getCause().getMessage();
				retexc = new SendException(exmsg);
				retexc.setException(ex);
			}

			retexc = saveSentOrFallbackToMainSent(null, dst, ident, retexc);
		} catch(Exception ex) {
			logger.error("Error in ForwardRedirect", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processPECChangePassword(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		MailAccount account = getAccount(request);
		String pfoldername = request.getParameter("foldername");
		String ppassword = request.getParameter("password");
		
		try {
			String webtopProfileId = getEnv().getProfile().getId().toString();
			if (!StringUtils.isBlank(pfoldername)) {
				FolderCache mcache = account.getFolderCache(pfoldername);
				webtopProfileId = mcache.getWebTopUser()+"@"+getEnv().getProfile().getDomainId();
			}
			CoreManager core = WT.getCoreManager();
			core.updatePecBridgeRelayPassword(webtopProfileId, ppassword);
			core.updatePecBridgeFetcherPassword(webtopProfileId, ppassword);
			//notify PECBridge about the password change
			try {
				MimeMessage msg = new MimeMessage(account.getMailSession());
				msg.setFrom(new InternetAddress("pecbridge-manager"));
				msg.setRecipient(RecipientType.TO, new InternetAddress("password-changed."+webtopProfileId));
				msg.setContent("", "text/plain");
				Transport.send(msg);
			} catch(MessagingException exc) {
				logger.error("Error sending reload command to PEC Bridge",exc);
			}
			new JsonResult(true).printTo(out);
		} catch(Exception ex) {
			logger.error("Error in PECChangePassword", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	private class OnUploadCloudFile implements IServiceUploadStreamListener {
		@Override
		public void onUpload(String context, HttpServletRequest request, HashMap<String, String> multipartParams, WebTopSession.UploadedFile file, InputStream is, MapItem responseData) throws UploadException {
			
			try {
				long msgid=Long.parseLong(file.getTag());
				String path="/Emails";
				int sid=vfsmanager.getMyDocumentsStoreId();
				FileObject fo=vfsmanager.getStoreFile(sid,path);
				if (!fo.exists()) fo.createFolder();
				String filename=file.getFilename();
				String newFullName = vfsmanager.addStoreFileFromStream(sid, path, filename, is, false);
				
				filename=FilenameUtils.getName(newFullName);
				responseData.add("storeId", sid);
				responseData.add("filePath", newFullName);
				attachCloud(msgid,sid,newFullName,filename);
			} catch(UploadException ex) {
				logger.trace("Upload failure", ex);
				throw ex;
			} catch(Throwable t) {
				logger.error("Upload failure", t);
				throw new UploadException("Upload failure");
			}
		}
	}
	
	private static class AttachmentViewerDocumentHandler extends BaseDocEditorDocumentHandler {
		private final File file;
		private final Part part;
		private final long lastModified;
		
		public AttachmentViewerDocumentHandler(boolean writeCapability, UserProfileId targetProfileId, String documentUniqueId, Part part, long lastModified) {
			super(writeCapability, targetProfileId, documentUniqueId);
			this.part = part;
			this.lastModified = lastModified;
			this.file=null;
		}
		
		public AttachmentViewerDocumentHandler(boolean writeCapability, UserProfileId targetProfileId, String documentUniqueId, File file) {
			super(writeCapability, targetProfileId, documentUniqueId);
			this.file=file;
			this.lastModified = file.lastModified();
			this.part = null;
		}
		
		@Override
		public long getLastModifiedTime() throws IOException {
			return lastModified;
		}
		
		@Override
		public InputStream readDocument() throws IOException {
			try {
				if (part!=null) return part.getInputStream();
				if (file!=null) return new FileInputStream(file);
				throw new IOException("Unable to find part or file");
			} catch(MessagingException ex) {
				throw new IOException("Unable to read document", ex);
			}
		}
		
		@Override
		public void writeDocument(InputStream is) throws IOException {
			throw new IOException("Write not supported");
		}
		
	}
	
	protected MailManager getManager() {
		return mailManager;
	}
	
	private WebTopSession getWts() {
		return getEnv().getWebTopSession();
	}
	
	private String formatCalendarDate(int year, int month, int day, int hours, int minutes, int seconds) {
		return year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day) + " " + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
	}
	
	private class FoldersNamesInByFileFiltersCache extends AbstractPassiveExpiringBulkSet<String> {
		
		public FoldersNamesInByFileFiltersCache(final long timeToLive, final TimeUnit timeUnit) {
			super(timeToLive, timeUnit);
		}
		
		@Override
		protected Set<String> internalGetSet() {
			try {
				Set<String> folders = new HashSet<>();
				List<MailFilter> filters = mailManager.getMailFilters(MailFiltersType.INCOMING, EnabledCond.ENABLED_ONLY);
				for (MailFilter filter : filters) {
					for (SieveAction action : filter.getSieveActions()) {
						if (action.getMethod() == SieveActionMethod.FILE_INTO) {
							folders.add(action.getArgument());
						}
					}
				}
				return folders;
				
			} catch(Exception ex) {
				logger.error("[FoldersNamesInByFileFiltersCache] Unable to build cache", ex);
				throw new UnsupportedOperationException();
			}
		}
	}
        
    class SentMessageNotSavedSM extends MessageBoxSM {

            public SentMessageNotSavedSM(Exception exc) {
                    super(MessageBoxSM.MsgType.WARN,
                            lookupResource(MailLocaleKey.SENT_MESSAGE_NOT_SAVED_MESSAGE)+":\n\n"+exc.getMessage()
                    );
            }
    }
        
    class SharedSentMessageSavedOnMainSM extends MessageBoxSM {

            public SharedSentMessageSavedOnMainSM(Exception exc) {
                    super(MessageBoxSM.MsgType.WARN,
                            lookupResource(MailLocaleKey.SHARED_SENT_MESSAGE_SAVED_ON_MAIN_MESSAGE)+":\n\n"+exc.getMessage()
                    );
            }
    }
}
