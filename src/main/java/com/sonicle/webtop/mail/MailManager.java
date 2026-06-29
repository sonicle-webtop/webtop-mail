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
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.RegexUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.rsql.parser.Operator;
import com.sonicle.commons.rsql.parser.RSQLParser;
import com.sonicle.commons.rsql.parser.ast.ComparisonOperator;
import com.sonicle.commons.rsql.parser.ast.Node;
import com.sonicle.commons.rsql.parser.ast.RSQLOperators;
import com.sonicle.commons.time.JavaTimeUtils;
import com.sonicle.mail.Mailbox;
import com.sonicle.mail.StoreHostParams;
import com.sonicle.mail.StoreUtils;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.mail.email.EmailMessageBuilder;
import com.sonicle.mail.email.EmailPopulatingBuilder;
import com.sonicle.mail.email.Recipient;
import com.sonicle.mail.email.Recipients;
import com.sonicle.mail.imap.DateSortTerm;
import com.sonicle.mail.imap.SonicleIMAPFolder;
import com.sonicle.mail.imap.SonicleIMAPMessage;
import com.sonicle.mail.parser.MimeMessageParser;
import com.sonicle.mail.parser.MimeMessageParser.ParsedMimeMessageComponents;
import com.sonicle.security.PasswordUtils;
import com.sonicle.security.Principal;
import com.sonicle.security.auth.directory.LdapNethDirectory;
import com.sonicle.webtop.calendar.ICalendarManager;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.model.EventInstanceId;
import com.sonicle.webtop.contacts.ContactsUtils;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.WT;
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
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.model.ShareOrigin;
import com.sonicle.webtop.core.app.model.Sharing;
import com.sonicle.webtop.core.app.sdk.WTEmailSendException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.util.ICalendarHelper;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.core.util.IdentifierUtils;
import com.sonicle.webtop.mail.MailUserSettings.FavoriteFolder;
import com.sonicle.webtop.mail.MailUserSettings.FavoriteFolders;
import com.sonicle.webtop.mail.bol.OExternalAccount;
import com.sonicle.webtop.mail.bol.ONote;
import com.sonicle.webtop.mail.bol.OTag;
import com.sonicle.webtop.mail.bol.OUserMap;
import com.sonicle.webtop.mail.dal.ExternalAccountDAO;
import com.sonicle.webtop.mail.dal.NoteDAO;
import com.sonicle.webtop.mail.dal.TagDAO;
import com.sonicle.webtop.mail.dal.UserMapDAO;
import com.sonicle.webtop.mail.model.CalendarPartInfo;
import com.sonicle.webtop.mail.model.ExternalAccount;
import com.sonicle.webtop.mail.model.FolderShareParameters;
import com.sonicle.webtop.mail.model.ItipAction;
import com.sonicle.webtop.mail.model.ItipApplyResult;
import com.sonicle.webtop.mail.model.MatchedEventInfo;
import com.sonicle.webtop.mail.model.Tag;
import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.Address;
import jakarta.mail.FetchProfile;
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
import java.util.List;
import java.util.function.Function;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.search.SearchTerm;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Method;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

/**
 *
 * @author gabriele.bulfon
 */
public class MailManager extends BaseManager implements IMailManager {

	public static final Logger logger = WT.getLogger(MailManager.class);
	public static final String IDENTITY_SHARING_CONTEXT = "IDENTITY@FOLDER";
	public static final String IDENTITY_PERMISSION_KEY = "IDENTITY@SHARE_FOLDER";
	public static final String SIEVE_OLD_WEBTOP_SCRIPT = "webtop";
	public static final String SIEVE_WEBTOP_SCRIPT = "webtop5";
	public static final int MAX_EXT_ACCOUNTS = 3; // Update this fixed limit also in UserOptions.js

	private static String START_PRE = "<PRE>";
	private static String END_PRE = "</PRE>";

	private static final String S_FLAG_NOTE="mailnote";
	private static final String S_FLAG_DMS_ARCHIVED="$Archived";
	private static final Flags flagNote=new Flags(S_FLAG_NOTE);
	private static final Flags flagDmsArchived=new Flags(S_FLAG_DMS_ARCHIVED);
	private static final Flags flagFlagged=new Flags(Flags.Flag.FLAGGED);
	
	public static String getFlagNoteString() { return S_FLAG_NOTE; }
	public static String getFlagDmsArchivedString() { return S_FLAG_DMS_ARCHIVED; }
	public static Flags getFlagNote() { return flagNote; }
	public static Flags getFlagDmsArchived() { return flagDmsArchived; }
	public static Flags getFlagFlagged() { return flagFlagged; }
	
	private SieveConfig sieveConfig = null;
	List<Identity> identities=null;
	HashMap<String, Identity> identHash = new HashMap<>();
	HashMap<String, List<Identity>> allPersonalIdentitiesDomains = new HashMap<>();
	Mailbox mailbox = null;
	
	static final private FetchProfile FP = new FetchProfile();
	
	static {
		FP.add(FetchProfile.Item.ENVELOPE);
		FP.add(FetchProfile.Item.FLAGS);
		FP.add(FetchProfile.Item.CONTENT_INFO);
		FP.add(UIDFolder.FetchProfileItem.UID);
		FP.add("Message-ID");
		FP.add("X-Priority");
	}
	
	private static FetchProfile FP_BS = new FetchProfile();

    static {
		FP_BS.add(FetchProfile.Item.CONTENT_INFO);
    }
	
	static class WebtopFlag {
		String label;
		
		WebtopFlag(String label) {
			this.label=label;
		}
		
	}
	
    public static final WebtopFlag[] webtopFlags={
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

	public static String STATUS_READ = "read";
	public static String STATUS_UNREAD = "unead";
	public static String STATUS_REPLIED = "replied";
	public static String STATUS_FORWARDED = "forwarded";
	public static String STATUS_REPLIED_FORWARDED = "repfwd";
	public static String STATUS_NEW = "new";
	public static String STATUS_INVITATION = "invitation";
	
	private boolean attachmentDetectUseBodyStructure = true;
	private boolean messageListIncremental = true;
	private ArrayList<String> inlineableMimes = new ArrayList<String>();
	
	private MailUserSettings mus = null;
	private MailServiceSettings mss = null;

	public MailManager(boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
		
		mss = new MailServiceSettings(SERVICE_ID, targetProfileId.getDomainId());
		mus = new MailUserSettings(targetProfileId, mss);
		
		if (!fastInit) {
			attachmentDetectUseBodyStructure = mss.isAttachmentDetectUseBodyStructure();
			messageListIncremental = mss.isMessageListIncrementalEnabled();
			String mtypes=mss.getInlineableMimeTypes();
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
			
			ArrayList<String> allFlagsArray=new ArrayList<String>();
			//TODO: cleanup code here...make use of new MessageFlags enum!
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
			for(MailManager.WebtopFlag fs: MailManager.webtopFlags) {
				allFlagsArray.add("flag"+fs.label);
			}	  
			allFlagStrings=new String[allFlagsArray.size()];
			allFlagsArray.toArray(allFlagStrings);
		
			try {
				createMailboxObject();
			} catch(GeneralSecurityException exc) {
				logger.error("Error creating Mailbox object", exc);
			}
		}
	}
	
	private void createMailboxObject() throws GeneralSecurityException {
		String user = WT.buildDomainInternetAddress(getTargetProfileId().getDomainId(), getTargetProfileId().getUserId(), null).getAddress();
		final StoreHostParams hostParams = mus.getMailboxHostParams(user, PasswordUtils.asString(WT.lookupSecretStoreValue(getTargetProfileId(), WebTopManager.PSVKEY_PPW)), true);
		mailbox = new Mailbox(hostParams, ManagerUtils.createMailboxConfig(mus), ManagerUtils.createMailboxProperties(hostParams.getProtocol()));
	}
	
	private UserProfile getUserProfile() {
		Principal principal = RunContext.getPrincipal();
		return new UserProfile(WT.getCoreManager(), principal);
	}
	
	private Mailbox getMailbox() throws WTException {
		if (mailbox == null) {
			try {
				createMailboxObject();
			} catch(GeneralSecurityException exc) {
				throw new WTException("Error creating Mailbox object", exc);
			}
		}
		
		try {
			mailbox.ensureConnected();
		} catch(MessagingException exc) {
			throw new WTException("Error while ensuring imap connection", exc);
		}
		return mailbox;
	}
	
	public void cleanup() {
		if (mailbox != null) mailbox.disconnect();
	}
	
	public class Favorite {
		public String id;
		public String name;
		public Folder folder;
	}
	public ArrayList<Favorite> getFavorites() {
		FavoriteFolders ffs = mus.getFavoriteFolders();
		ArrayList<Favorite> favorites = new ArrayList<>();
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			for(FavoriteFolder ff: ffs) {
				Favorite f = new Favorite();
				f.id = ff.folderId;
				f.name = ff.description;
				f.folder = mailbox.getFolder(f.id);
				favorites.add(f);
			}
		} catch(MessagingException|WTException exc) {
			logger.error("Error listing favorites", exc);
		} finally {
			//mailbox.disconnect();
		}
		return favorites;
	}
	
	public ArrayList<Folder> getAllFolders() {
		ArrayList<Folder> folders = new ArrayList<>();
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			Folder root = mailbox.getRootFolder();
			Folder flist[] = root.list("*");
			for(Folder folder: flist) folders.add(folder);
		} catch(MessagingException|WTException exc) {
			logger.error("Error listing folders", exc);
		} finally {
			//mailbox.disconnect();
		}
		return folders;
	}
	
	public ArrayList<Folder> getRootFolders() {
		ArrayList<Folder> folders = new ArrayList<>();
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			Folder root = mailbox.getRootFolder();
			Folder flist[] = root.list();
			for(Folder folder: flist) folders.add(folder);
		} catch(MessagingException|WTException exc) {
			logger.error("Error listing folders", exc);
		} finally {
			//mailbox.disconnect();
		}
		return folders;
	}
	
	public ArrayList<Folder> getFolders(String id) {
		ArrayList<Folder> folders = new ArrayList<>();
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			Folder parent = mailbox.getFolder(id);
			Folder flist[] = parent.list();
			for(Folder folder: flist) folders.add(folder);
		} catch(MessagingException|WTException exc) {
			logger.error("Error listing folders", exc);
		} finally {
			//mailbox.disconnect();
		}
		return folders;
	}
	
	public Folder getFolder(String id) {
		Folder folder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			folder = mailbox.getFolder(id);
		} catch(MessagingException|WTException exc) {
			logger.error("Error getting folder", exc);
		} finally {
			//mailbox.disconnect();
		}
		return folder;
	}

	public Message[] fetch(Folder folder, Message fmsgs[], FetchProfile fp, int start, int length) throws MessagingException {
        int n=fmsgs.length;
        if (length>(n-start)) length=n-start;
        Message xmsgs[]=new Message[length];
        System.arraycopy(fmsgs, start, xmsgs, 0, length);
        folder.fetch(xmsgs, fp);
		return xmsgs;
    }
	
	private static Node parseRSQL(final String s) {
		java.util.Set<ComparisonOperator> ops = new java.util.HashSet<>();
		ops.addAll(RSQLOperators.defaultOperators());
		ops.addAll(Operator.extendedOperators());
		return new RSQLParser(ops).parse(s);
	}
	
	public void consumeMessages(String folderId, int pageNo, int pageSize, String filterQuery, String orderBy, boolean fullReturnCount, MessagesConsumer mc) {
		Folder folder = null;
		Mailbox mailbox = null;
		try {
			UserProfile.Data pdata = WT.getProfileData(getTargetProfileId());
			SearchTermBuildingVisitor visitor = new SearchTermBuildingVisitor();
			visitor.setTimeZone(pdata.getTimeZone());
			SearchTerm searchTerm = StringUtils.isBlank(filterQuery) ? null : parseRSQL(filterQuery).accept(visitor);
			
			mailbox = getMailbox();
			folder = mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			Message fmsgs[] = ((SonicleIMAPFolder)folder).sort(new DateSortTerm(true), searchTerm);
			if (pageNo<0) {
				((SonicleIMAPFolder)folder).uid_fetch(fmsgs, FP);
			} else {
				int start = pageNo*pageSize;
				/*int end = start + pageSize;
				if (end>fmsgs.length) end = fmsgs.length;
				Message xmsgs[] = new Message[end-start];
				for(int ix=start; ix<end; ++ix) {
					xmsgs[ix-start] = fmsgs[ix];
				}
				((SonicleIMAPFolder)folder).uid_fetch(xmsgs, FP);*/
				
				fmsgs = fetch(folder, fmsgs, FP, start, pageSize);
				
				/*for(Message msg: xmsgs) {
					mc.consume(msg, ((UIDFolder)folder).getUID(msg));
				}*/
			}
			if (visitor.hasAttachment()) fmsgs = applyHasAttachmentFilter(folder, fmsgs, visitor.getAttachmentName());
			if (visitor.hasNotePattern()) fmsgs = applyHasNoteFilter(folder, fmsgs, visitor.getNotePattern());
			for(Message msg: fmsgs) {
				mc.consume(msg, ((UIDFolder)folder).getUID(msg));
			}
		} catch(Exception exc) {
			logger.error("Error listing folders", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}	
	
/*	public MimeMessage getMessage(String folderId, long uid) {
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		MimeMessage msg = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			msg = (MimeMessage) folder.getMessageByUID(uid);
		} catch(Exception exc) {
			logger.error("Error getting message", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
		return msg;
	}*/
	
	public void consumeMessage(String folderId, long uid, boolean setSeen, int index, MessageConsumer mc) {
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(setSeen ? Folder.READ_WRITE : Folder.READ_ONLY);
			MimeMessage mmsg = (MimeMessage) folder.getMessageByUID(uid);
			if (index >= 0) {
				ParsedMimeMessageComponents parsed = getParsedMimeMessageComponents(mmsg);
				mmsg = (MimeMessage)parsed.getAttachmentParts().get(index).getContent();
			}
			MimeMessageParser mmp = new MimeMessageParser().withProcessDisplayParts(false, new MimeMessageParser.DisplayPartEvaluator() {
				
				private boolean icalhtmlview = false;
				
				public void evaluateCalendar(MimeMessageParser.ParsedMimeMessageComponents parsed, Part part, InputStream istream, String charset) throws IOException, MessagingException {
					try {
						ICalendarRequest ir=new ICalendarRequest(istream);
						//mailData.setICalRequest(ir);
						if (!icalhtmlview) {
							UserProfile profile = getUserProfile();
							Locale locale = profile.getLocale();
							String irhtml=ir.getHtmlView(locale, "5.23.0", "default", java.util.ResourceBundle.getBundle("com/sonicle/webtop/mail/locale", locale));
							parsed.appendProcessedHTMLPart(0, irhtml);
							icalhtmlview=true;
						}
						if (parsed.hasICalAttachment) parsed.appendAttachmentPart(part, 0);
					} catch(ParserException exc) {
						parsed.appendAttachmentPart(part, 0);
					}
				}
				
				public void evaluatePlainText(MimeMessageParser.ParsedMimeMessageComponents parsed, Part part, InputStream istream, String charset) throws IOException, MessagingException  {
					StringBuffer xhtml=new StringBuffer();
					xhtml.append("<html><head><meta content='text/html; charset="+charset+"' http-equiv='Content-Type'></head><body><pre>");

					String content;
					try {
						Charset xcharset=Charsets.toCharset(charset);
						content=IOUtils.toString(istream,xcharset);
					} catch(UnsupportedCharsetException | IllegalCharsetNameException exc) {
						content=IOUtils.toString(istream,Charsets.ISO_8859_1);
					}
					//avoid possible XSS
					content = LangUtils.encodeForHTML(content);

					String replacement = "$1";
					String sparams="\"" + replacement + "\"";
					//String onEmailClick = "parent.WT.handleMailAddress(" + sparams + "); return false;";

					//content = content.replaceAll("(" + RegexUtils.MATCH_EMAIL_ADDRESS + ")", "<a target=_blank href=$1 onClick ='"+onEmailClick+"'>$1</a>");
					content = content.replaceAll("(" + RegexUtils.MATCH_URL + ")", "<a target= _blank href=$1>$1</a>");
					content = content.replaceAll("(" + RegexUtils.MATCH_WWW_URL + ")", "$2<a target=_blank href='http://$3'>$3</a>$4");

					xhtml.append(content);	
					xhtml.append("<BR>");
					xhtml.append("</pre><HR></body></html>");
					//TODO : check converted urls above for external links
					parsed.appendProcessedHTMLPart(0, xhtml.toString());
				}
				
				public void evaluateMessage(MimeMessageParser.ParsedMimeMessageComponents parsed, Part part, InputStream istream, String charset) throws IOException, MessagingException  {
					Principal principal = RunContext.getPrincipal();
					UserProfile profile = new UserProfile(WT.getCoreManager(), principal);
					Locale locale = profile.getLocale();
					StringBuffer xhtml = new StringBuffer();
					//msgPart=dispPart;
					Message xmsg = (Message)part.getContent();
					String msgSubject = xmsg.getSubject();
					if (msgSubject == null) msgSubject = "";
					msgSubject = MailUtils.htmlescape(msgSubject);
					Address ad[] = xmsg.getFrom();
					String msgFrom = ad!=null ? getHTMLDecodedAddress(ad[0]) : "";
					java.util.Date dt = xmsg.getSentDate();
					String msgDate = dt!=null ? java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG,java.text.DateFormat.LONG, locale).format(dt) : "";
					ad=xmsg.getRecipients(Message.RecipientType.TO);
					String msgTo=null;
					if (ad!=null) {
					  msgTo="";
					  for(int j=0;j<ad.length;++j) msgTo+=getHTMLDecodedAddress(ad[j])+" ";
					}
					ad=xmsg.getRecipients(Message.RecipientType.CC);
					String msgCc=null;
					if (ad!=null) {
					  msgCc="";
					  for(int j=0;j<ad.length;++j) msgCc+=getHTMLDecodedAddress(ad[j])+" ";
					}

					xhtml.append("<html><head><meta content='text/html; charset=utf-8' http-equiv='Content-Type'></head><body>");
					xhtml.append("<font face='Arial, Helvetica, sans-serif' size=2><BR>");
					xhtml.append("<B>"+lookupResource(locale, MailLocaleKey.MSG_FROMTITLE)+":</B> "+msgFrom+"<BR>");
					if (msgTo!=null) xhtml.append("<B>"+lookupResource(locale, MailLocaleKey.MSG_TOTITLE)+":</B> "+msgTo+"<BR>");
					if (msgCc!=null) xhtml.append("<B>"+lookupResource(locale, MailLocaleKey.MSG_CCTITLE)+":</B> "+msgCc+"<BR>");
					xhtml.append("<B>"+lookupResource(locale, MailLocaleKey.MSG_DATETITLE)+":</B> "+msgDate+"<BR>");
					xhtml.append("<B>"+lookupResource(locale, MailLocaleKey.MSG_SUBJECTTITLE)+":</B> "+msgSubject+"<BR>");
					xhtml.append("</font><br></body></html>");
					parsed.appendProcessedHTMLPart(xhtml.toString());
				}
				
			});

			MimeMessageParser.ParsedMimeMessageComponents parsed = mmp.parse(mmsg, false);
			
			mc.consume(mmsg, uid, parsed);
			
		} catch(Exception exc) {
			logger.error("Error listing folders", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}	
	
	protected String getMessageID(Message m) throws MessagingException {
		String ids[] = m.getHeader("Message-ID");
		if (ids == null) {
			return null;
		}
		return ids[0];
	}
	
	public String getMessageNote(String folderId, long uid) {
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		Connection con = null;
		String mid = null;
		String text = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			MimeMessage mmsg = (MimeMessage) folder.getMessageByUID(uid);
			mid = getMessageID(mmsg);
			con = WT.getConnection(SERVICE_ID);
			ONote onote=NoteDAO.getInstance().selectById(con, getUserProfile().getDomainId(), mid);
			if (onote!=null) {
				text = onote.getText();
			}
			
		} catch(Exception exc) {
			logger.error("Error in getMessageNote", exc);
		} finally {
			DbUtils.closeQuietly(con);
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
		return text;
	}	

	public void setMessageNote(String folderId, long uid, String text) {
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		Connection con = null;
		try {
			UserProfile profile = getUserProfile();
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			MimeMessage mmsg = (MimeMessage) folder.getMessageByUID(uid);
			String mid = getMessageID(mmsg);
			con = WT.getConnection(SERVICE_ID);
			NoteDAO.getInstance().deleteById(con, profile.getDomainId(), mid);
			if (!StringUtils.isBlank(text)) {
				ONote onote = new ONote(profile.getDomainId(), mid, text);
				NoteDAO.getInstance().insert(con, onote);
				mmsg.setFlags(MailManager.getFlagNote(), true);
			} else {
				mmsg.setFlags(MailManager.getFlagNote(), false);
			}
		} catch(Exception exc) {
			logger.error("Error in getMessageNote", exc);
		} finally {
			DbUtils.closeQuietly(con);
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}	

	public String getMessageAttachmentContentType(String folderId, long uid, int index) {
		IMAPFolder folder = null;
		String contentType="binary/octet-stream";
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			MimeMessage mmsg = (MimeMessage) folder.getMessageByUID(uid);
			MimeMessageParser mmp = new MimeMessageParser();
			MimeMessageParser.ParsedMimeMessageComponents parsed = mmp.parse(mmsg, false);
			Part part = parsed.getAttachmentParts().get(index);
			contentType = part.getContentType();
		} catch(Exception exc) {
			logger.error("Error in getMessageAttachmentContentType", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
		return contentType;
	}	
	
	public MimeMessageParser.ParsedMimeMessageComponents getParsedMimeMessageComponents(String folderId, long uid) {
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		MimeMessageParser.ParsedMimeMessageComponents parsed=null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			MimeMessage mmsg = (MimeMessage) folder.getMessageByUID(uid);
			MimeMessageParser mmp = new MimeMessageParser();
			parsed = mmp.parse(mmsg, false);
		} catch(Exception exc) {
			logger.error("Error on getParsedMimeMessageComponents", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
		return parsed;
	}	
	
	public MimeMessageParser.ParsedMimeMessageComponents getParsedMimeMessageComponents(MimeMessage mmsg) {
		MimeMessageParser.ParsedMimeMessageComponents parsed=null;
		try {
			MimeMessageParser mmp = new MimeMessageParser();
			parsed = mmp.parse(mmsg, false);
		} catch(Exception exc) {
			logger.error("Error on getParsedMimeMessageComponents", exc);
		} finally {
			//mailbox.disconnect();
		}
		return parsed;
	}	
	
	public void streamMessageAttachmentData(String folderId, long uid, int index, OutputStream os) {
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			MimeMessage mmsg = (MimeMessage) folder.getMessageByUID(uid);
			MimeMessageParser mmp = new MimeMessageParser();
			MimeMessageParser.ParsedMimeMessageComponents parsed = mmp.parse(mmsg, false);
			Part part = parsed.getAttachmentParts().get(index);
			IOUtils.copy(part.getInputStream(), os);
		} catch(Exception exc) {
			logger.error("Error in streamMessageAttachmentData", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}	
	
	public void streamMessageAttachmentData(MimeMessageParser.ParsedMimeMessageComponents parsed, int index, OutputStream os) {
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			MimeMessage msg = parsed.getOriginalMessage();
			folder = (IMAPFolder) msg.getFolder();
			folder.open(Folder.READ_ONLY);
			Part part = parsed.getAttachmentParts().get(index);
			IOUtils.copy(part.getInputStream(), os);
		} catch(Exception exc) {
			logger.error("Error in streamMessageAttachmentData", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}	
	
	/**
	 * Apply an iTIP action (Accept / Tentative / Decline / Apply / Import / Ignore)
	 * to a {@code text/calendar} (or {@code application/ics}) attachment carried
	 * by the given mail message.
	 *
	 * <p>Server-side single entry point for iTIP handling — replaces the
	 * legacy {@code Service.processCalendarRequest},
	 * {@code Service.processDeclineInvitation} and
	 * {@code Service.processUpdateCalendarReply} bodies. Both the web
	 * action-dispatcher and the REST controllers call into this method so
	 * behavior cannot drift between the two surfaces.
	 *
	 * <p>The caller-supplied {@link ItipAction} crosses with the iTIP METHOD
	 * carried by the ICS to pick the actual server effect. {@code REQUEST}+
	 * {@code ACCEPT}/{@code TENTATIVE} adds or updates the event and emits a
	 * REPLY; {@code REQUEST}+{@code DECLINE} emits a REPLY without persisting;
	 * {@code CANCEL}+{@code APPLY} removes the event; {@code REPLY}+{@code APPLY}
	 * updates the matching attendee's PARTSTAT (organizer flow); {@code IMPORT}
	 * stores a standalone copy without emitting a REPLY.
	 *
	 * <p>When {@code targetCalendarId} is null the calendar is auto-resolved
	 * to the user's default — or to the built-in calendar if the default is
	 * shared. {@code notify=false} suppresses the outgoing REPLY mail.
	 */
	public ItipApplyResult applyICalendar(
			String folderId, long uid, int attachmentIndex,
			ItipAction action, Integer targetCalendarId,
			boolean notify, String comment) throws WTException {
		if (action == ItipAction.IGNORE) {
			return new ItipApplyResult(ItipApplyResult.Outcome.IGNORED, null, null, false);
		}

		IMAPFolder folder = null;
		try {
			Mailbox mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			MimeMessage mmsg = (MimeMessage) folder.getMessageByUID(uid);
			MimeMessageParser.ParsedMimeMessageComponents parsed =
					new MimeMessageParser().parse(mmsg, false);

			net.fortuna.ical4j.model.Calendar iCal = null;
			try {
				ArrayList<Part> aparts = parsed.getAttachmentParts();
				if (aparts != null && attachmentIndex < aparts.size()) {
					Part part = aparts.get(attachmentIndex);
					try (InputStream is = part.getInputStream()) {
						iCal = ICalendarUtils.parse(is);
					}
				}
			} catch (IOException | ParserException ex) {
				throw new WTException("Failed to parse calendar attachment", ex);
			}
			
			//If attachment did not work, let's try with part method
			if (iCal == null) {
				try {
					String calendarContent = parsed.getCalendarContent();
					if (!StringUtils.isBlank(calendarContent)) {
						iCal = ICalendarUtils.parse(calendarContent);
					}
				} catch (IOException | ParserException ex) {
					throw new WTException("Failed to parse calendar part", ex);
				}
			}
			
			if (iCal == null) throw new WTException("No calendar part");

			ICalendarManager cm = (ICalendarManager) WT.getServiceManager(
					"com.sonicle.webtop.calendar", true, getTargetProfileId());

			Integer calendarId = targetCalendarId;
			if (calendarId == null) {
				calendarId = cm.getDefaultCalendarId();
				// Promote to built-in calendar if default is shared — events
				// landing from email belong in personal calendars.
				if (calendarId != null && !cm.listMyCalendarIds().contains(calendarId)) {
					calendarId = cm.getBuiltInCalendarId();
				}
			}

			Method ical4jMethod = iCal.getMethod();
			String method = (ical4jMethod == null) ? "REQUEST" : ical4jMethod.getValue();

			BitFlags<ICalendarManager.HandleICalInviationOption> handleOptions = BitFlags.with(
					ICalendarManager.HandleICalInviationOption.IGNORE_ICAL_CLASSIFICATION,
					ICalendarManager.HandleICalInviationOption.IGNORE_ICAL_TRASPARENCY,
					ICalendarManager.HandleICalInviationOption.IGNORE_ICAL_ALARMS,
					ICalendarManager.HandleICalInviationOption.EVENT_LOOKUP_SCOPE_STRICT);

			switch (action) {
				case IMPORT: {
					Event ev = cm.addEvent(calendarId, iCal);
					String iid = EventInstanceId.buildMaster(ev.getEventId()).toString();
					return new ItipApplyResult(
							ItipApplyResult.Outcome.CREATED, iid, calendarId, false);
				}

				case ACCEPT:
				case TENTATIVE: {
					if (!"REQUEST".equals(method)) {
						throw new WTException(action + " not valid for METHOD:" + method);
					}
					Event ev = cm.handleInvitationFromICal(iCal, calendarId, handleOptions);
					String iid = (ev != null)
							? EventInstanceId.buildMaster(ev.getEventId()).toString()
							: null;
					PartStat ps = (action == ItipAction.ACCEPT)
							? PartStat.ACCEPTED : PartStat.TENTATIVE;
					boolean replySent = notify && sendItipReply(mmsg, iCal, ps);
					return new ItipApplyResult(
							ItipApplyResult.Outcome.CREATED, iid, calendarId, replySent);
				}

				case DECLINE: {
					if (!"REQUEST".equals(method)) {
						throw new WTException("DECLINE not valid for METHOD:" + method);
					}
					boolean replySent = notify && sendItipReply(mmsg, iCal, PartStat.DECLINED);
					return new ItipApplyResult(
							ItipApplyResult.Outcome.RSVP_RECORDED, null, null, replySent);
				}

				case APPLY: {
					if ("CANCEL".equals(method)) {
						cm.handleInvitationFromICal(iCal, calendarId, handleOptions);
						return new ItipApplyResult(
								ItipApplyResult.Outcome.REMOVED, null, calendarId, false);
					}
					if ("REPLY".equals(method)) {
						// handleInvitationFromICal covers METHOD:REPLY internally
						// by routing through doEventAttendeeUpdateResponseByRecipient.
						Event ev = cm.handleInvitationFromICal(iCal, calendarId, handleOptions);
						String iid = (ev != null)
								? EventInstanceId.buildMaster(ev.getEventId()).toString()
								: null;
						return new ItipApplyResult(
								ItipApplyResult.Outcome.RSVP_RECORDED, iid, calendarId, false);
					}
					if ("REQUEST".equals(method)) {
						// APPLY on a REQUEST = "apply the update without changing my PARTSTAT"
						// — matches the legacy "update" action in Service.processCalendarRequest.
						Event ev = cm.handleInvitationFromICal(iCal, calendarId, handleOptions);
						String iid = (ev != null)
								? EventInstanceId.buildMaster(ev.getEventId()).toString()
								: null;
						return new ItipApplyResult(
								ItipApplyResult.Outcome.UPDATED, iid, calendarId, false);
					}
					throw new WTException("APPLY not valid for METHOD:" + method);
				}

				default:
					throw new WTException("Unsupported ItipAction: " + action);
			}
		} catch (MessagingException ex) {
			throw new WTException("IMAP error in applyICalendar", ex);
		} finally {
			StoreUtils.closeQuietly(folder, false);
		}
	}

	/**
	 * Build and send an iTIP REPLY to the event's organizer using the
	 * recipient address found on the inbound message (or the first TO when
	 * multiple). Returns false when no usable TO address can be resolved
	 * (e.g. the message reached the mailbox via BCC), in which case the
	 * caller reports {@code replySent=false} without raising.
	 */
	private boolean sendItipReply(
			MimeMessage mmsg,
			net.fortuna.ical4j.model.Calendar iCal,
			PartStat partStat) throws WTException, MessagingException {
		List<InternetAddress> tos = MimeMessageParser.parseToAddresses(mmsg, true);
		if (tos.isEmpty()) return false;
		UserProfile.Data pdata = WT.getProfileData(getTargetProfileId());
		String prodId = ICalendarUtils.buildProdId(WT.getPlatformName() + " Mail");
		InternetAddress iaOrganizer = ICalendarUtils.getOrganizerAddress(
				ICalendarUtils.getVEvent(iCal));
		EmailMessage email = ICalendarHelper.prepareICalendarReply(
				prodId, iCal, tos.get(0), iaOrganizer, partStat, pdata.getLocale());
		WT.sendEmailMessage(getTargetProfileId(), email, getFolderSent());
		return true;
	}

	/**
	 * Parse every {@code text/calendar} / {@code application/ics} attachment
	 * on the given message and return one {@link CalendarPartInfo} per part.
	 * Used to populate {@code Message.calendarParts} during REST {@code getMessage}
	 * so the client can render the iTIP banner without fetching the ICS bytes.
	 *
	 * <p>Population is best-effort: a part that fails to parse is skipped
	 * (logged at WARN). {@code matchExistingEvent} is null when no event with
	 * the same UID exists in the user's calendars, or when the calendar
	 * service is unreachable. Returns an empty list when the message has no
	 * calendar parts.
	 */
	public List<CalendarPartInfo> readCalendarParts(String folderId, long uid) throws WTException {
		IMAPFolder folder = null;
		try {
			Mailbox mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			MimeMessage mmsg = (MimeMessage) folder.getMessageByUID(uid);
			MimeMessageParser.ParsedMimeMessageComponents parsed =
					new MimeMessageParser().parse(mmsg, false);
			return readCalendarParts(parsed);
		} catch (MessagingException ex) {
			throw new WTException("IMAP error in readCalendarParts", ex);
		} finally {
			StoreUtils.closeQuietly(folder, false);
		}
	}

	/**
	 * Variant of {@link #readCalendarParts(String, long)} that reuses an
	 * already-parsed message. Call this from inside a {@code consumeMessage}
	 * callback to avoid re-opening the folder.
	 */
	public List<CalendarPartInfo> readCalendarParts(
			MimeMessageParser.ParsedMimeMessageComponents parsed) {
		List<CalendarPartInfo> out = new ArrayList<>();
		if (parsed == null || !parsed.hasICalAttachment) return out;

		final String userEmail = WT.getProfileData(getTargetProfileId()).getPersonalEmailAddress();
		ICalendarManager cm;
		try {
			cm = (ICalendarManager) WT.getServiceManager(
					"com.sonicle.webtop.calendar", true, getTargetProfileId());
		} catch (Exception ex) {
			cm = null;
			logger.warn("Calendar service unavailable for readCalendarParts UID lookup", ex);
		}

		// Gmail (and other clients) emit the same VEVENT twice: once as a
		// text/calendar inline part with METHOD=REQUEST, and once as an
		// application/ics attachment. Dedupe by (UID, method, sequence) so the
		// banner renders once per event, not once per MIME part.
		Set<String> seen = new HashSet<>();
		List<Part> attachments = parsed.getAttachmentParts();
		for (int i = 0; i < attachments.size(); i++) {
			Part part = attachments.get(i);
			try {
				if (!isCalendarPart(part)) continue;
			} catch (MessagingException ex) {
				logger.warn("Cannot read MIME type for attachment at index " + i, ex);
				continue;
			}
			try (InputStream is = part.getInputStream()) {
				ICalendarRequest ir = new ICalendarRequest(is);
				String dedupeKey = (ir.getUID() == null ? "" : ir.getUID())
						+ "|" + ir.getMethod()
						+ "|" + ir.getSequence();
				if (!seen.add(dedupeKey)) continue;
				out.add(toCalendarPartInfo(i, ir, userEmail, cm));
			} catch (ParserException | IOException | MessagingException ex) {
				logger.warn("Skipping calendar attachment at index " + i + ": parse failed", ex);
			}
		}
		return out;
	}

	private CalendarPartInfo toCalendarPartInfo(int attachmentIndex, ICalendarRequest ir,
			String userEmail, ICalendarManager cm) {
		String method = ir.getMethod();
		String eventUid = ir.getUID();

		boolean userIsAttendee = false;
		String userPartStat = null;
		if (userEmail != null) {
			int n = ir.getAttendees();
			for (int j = 0; j < n; j++) {
				if (userEmail.equalsIgnoreCase(ir.getAttendeeEmail(j))) {
					userIsAttendee = true;
					userPartStat = toShortPartStat(ir.getAttendeeAnswer(j));
					break;
				}
			}
		}

		String replyCN = null, replyEmail = null, replyStatus = null;
		if ("REPLY".equals(method) && ir.getAttendees() > 0) {
			replyCN = ir.getAttendeeCN(0);
			replyEmail = ir.getAttendeeEmail(0);
			replyStatus = toShortPartStat(ir.getAttendeeAnswer(0));
		}

		MatchedEventInfo match = null;
		if (cm != null && eventUid != null && !eventUid.isEmpty()) {
			try {
				String eventId = cm.findEventId(eventUid);
				if (eventId != null) {
					EventInstanceId iid = EventInstanceId.buildMaster(eventId);
					Integer calId = null;
					String calName = null;
					boolean isOrganizer = false;
					try {
						EventInstance evt = cm.getEventInstance(iid);
						if (evt != null) {
							calId = evt.getCalendarId();
							if (userEmail != null && evt.getOrganizerAddress() != null) {
								isOrganizer = userEmail.equalsIgnoreCase(evt.getOrganizerAddress());
							}
							if (calId != null) {
								try {
									com.sonicle.webtop.calendar.model.Calendar cal = cm.getCalendar(calId);
									calName = (cal != null) ? cal.getName() : null;
								} catch (Exception ignore) { /* leave calName null */ }
							}
						}
					} catch (Exception ignore) { /* event vanished between lookup and fetch */ }
					match = new MatchedEventInfo(iid.toString(), calId, calName, null, isOrganizer);
				}
			} catch (Exception ex) {
				logger.debug("findEventId failed for UID " + eventUid, ex);
			}
		}

		return new CalendarPartInfo(
				attachmentIndex, method, eventUid, ir.getSequence(),
				ir.getSummary(), ir.getLocation(),
				ir.getStartDate(), ir.getEndDate(),
				ir.isAllDay(), ir.getTimezone(),
				ir.getOrganizerCN(), ir.getOrganizerEmail(),
				userIsAttendee, userPartStat,
				match,
				replyCN, replyEmail, replyStatus,
				ir.getComment());
	}

	private static boolean isCalendarPart(Part part) throws MessagingException {
		return part.isMimeType("text/calendar") || part.isMimeType("application/ics");
	}

	/** Short PARTSTAT code used by the wire DTO. Maps ical4j's long form to NA/AC/DE/TE. */
	private static String toShortPartStat(String partStat) {
		if (partStat == null) return null;
		switch (partStat) {
			case "ACCEPTED": return "AC";
			case "DECLINED": return "DE";
			case "TENTATIVE": return "TE";
			case "NEEDS-ACTION": return "NA";
			default: return "NA";
		}
	}

	public void streamMessageCidData(String folderId, long uid, String cidName, OutputStream os) {
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			MimeMessage mmsg = (MimeMessage) folder.getMessageByUID(uid);
			MimeMessageParser mmp = new MimeMessageParser();
			MimeMessageParser.ParsedMimeMessageComponents parsed = mmp.parse(mmsg, false);
			Part part = parsed.getCidParts().get(cidName);
			IOUtils.copy(part.getInputStream(), os);
		} catch(Exception exc) {
			logger.error("Error in streamMessageCidData", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}	
	
	public void streamMessageCidData(MimeMessageParser.ParsedMimeMessageComponents parsed, String cidName, OutputStream os) {
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			MimeMessage msg = parsed.getOriginalMessage();
			folder = (IMAPFolder) msg.getFolder();
			folder.open(Folder.READ_ONLY);
			Part part = parsed.getCidParts().get(cidName);
			IOUtils.copy(part.getInputStream(), os);
		} catch(Exception exc) {
			logger.error("Error in streamMessageCidData", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}	
	
	public interface MessagesConsumer {
		public void consume(Message m, long uid) throws MessagingException, IOException;
	}
	
	public interface MessageConsumer {
		public void consume(Message m, long uid, MimeMessageParser.ParsedMimeMessageComponents parsed) throws MessagingException, IOException;
	}
	
	public String getHTMLDecodedAddress(Address a) {
		String s = getDecodedAddress(a);
		return MailUtils.htmlescape(s);
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
	
	public String getFlagString(Flags flags) {
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
		return cflag;
	}
	
	public String getStatusString(Flags flags, boolean hasInvitation, boolean isToday) {
		String status = STATUS_READ;
		if (hasInvitation) {
			status = STATUS_INVITATION;
		}
		else if (flags != null) {
			if (flags.contains(Flags.Flag.ANSWERED)) {
				if (flags.contains("$Forwarded")) status = STATUS_REPLIED_FORWARDED;
				else status = STATUS_REPLIED;
			} else if (flags.contains("$Forwarded")) {
				status = STATUS_FORWARDED;
			} else if (flags.contains(Flags.Flag.SEEN)) {
				status = STATUS_READ;
			} else if (isToday) {
				status = STATUS_NEW;
			} else {
				status = STATUS_UNREAD;
			}
		}
		return status;
	}
	
	public boolean hasNote(Flags flags) {
		return flags.contains(getFlagNoteString());
	}
	
	public ArrayList<String> flagsToTagsIds(Flags flags, Map<String, com.sonicle.webtop.core.model.Tag> map) {
		ArrayList<String> tags=null;
		if (flags!=null) {
			for(com.sonicle.webtop.core.model.Tag tag: map.values()) {
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
	
	public String getIMAPBackendName() throws WTException {
		CoreManager coreMgr = WT.getCoreManager(true, this.getTargetProfileId());
		final String dirScheme = coreMgr.getAuthDirectoryScheme();
		if (LdapNethDirectory.SCHEME.equals(dirScheme)) {
			return "dovecot";
		} else {
			return "cyrus";
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
				logger.error("Exception", ex);
			}
		}
		return pname;
	}
	
	public String normalizeCidFileName(String filename) {
		if(filename.startsWith("<")) {
		  filename=filename.substring(1);
		}
		if(filename.endsWith(">")) {
		  filename=filename.substring(0, filename.length()-1);
		}
		try {
			filename=MailUtils.decodeQString(filename);
		} catch(Exception exc) {

		}
		return filename;
	}
	
	public String getCidName(Part p) {
		String cidname=null;
		try {
			String cidnames[]=p.getHeader("Content-ID");
			if (cidnames!=null && cidnames.length>0) cidname=normalizeCidFileName(cidnames[0]);
		} catch(MessagingException exc) {
		}
		return cidname;
	}
	
	private Message[] applyHasAttachmentFilter(Folder folder, Message msgs[], String name) throws MessagingException, IOException {
		Message rmsgs[]=msgs;
		
		ArrayList<Message> amsgs=new ArrayList<Message>();
		//ensure BODYSTRUCTURE has been loaded
		((SonicleIMAPFolder)folder).uid_fetch(msgs, FP_BS);
		for(Message m: msgs) {
			if (hasAttachments(m, name)) amsgs.add(m);
		}
		rmsgs=new SonicleIMAPMessage[amsgs.size()];
		amsgs.toArray(rmsgs);
		
		return rmsgs;
	}
		
	private Message[] applyHasNoteFilter(Folder folder, Message msgs[], String notePattern) throws MessagingException, IOException {
		Message rmsgs[]=msgs;
		if (!StringUtils.isEmpty(notePattern)) {
			try {
				ArrayList<String> msgIds=searchNotes(notePattern);
				ArrayList<Message> amsgs=new ArrayList<Message>();
				for(Message m: msgs) {
					String hdrs[]=m.getHeader("Message-ID");
					if (hdrs!=null) {
						if (msgIds.contains(hdrs[0])) amsgs.add(m);
					}
				}
				rmsgs=new SonicleIMAPMessage[amsgs.size()];
				amsgs.toArray(rmsgs);
			} catch(WTException exc) {
				Service.logger.error("Error during note search",exc);
			}
		}
		
		return rmsgs;
	}
	
	
	public boolean isAttachamentDetectUseBodyStructure() {
		return attachmentDetectUseBodyStructure;
	}

	public boolean isMessageListIncrementalEnabled() {
		return messageListIncremental;
	}
	
	public boolean isInlineableMime(String contenttype) {
		return inlineableMimes.contains(contenttype.toLowerCase());
	}
	
	private boolean _isInvitation(Part part) throws MessagingException {
		if (part.isMimeType("text/calendar")) {
			//String ctype=part.getContentType();
			//if (ctype!=null && StringUtils.containsIgnoreCase(ctype, "method=REQUEST"))
				return true;
		}
		return false;
	}
	
	private boolean _isAttachment(Part part) throws MessagingException {
		String disp = part.getDisposition();
		String cid=null;
		//skip cid info in text parts, to avoid wrong detection
		if (!part.isMimeType("text/*")) {
			String hdrs[]=part.getHeader("Content-ID");
			cid=hdrs!=null?hdrs[0]:null;
		}
		if (Part.ATTACHMENT.equalsIgnoreCase(disp)) {
			// Disposition is explicitly set to attachment
			return true;
			
		} else if (cid!=null || Part.INLINE.equalsIgnoreCase(disp)) {
			// Disposition is excplicitly inline, or has a Content-ID
			String ctype = part.getContentType();
			int index = ctype.indexOf(';');
			if (index > 0) {
				ctype = ctype.substring(0, index);
			}
			boolean isInline = isInlineableMime(ctype);
			return !isInline;
		
		} else if (disp == null && !StringUtils.isBlank(part.getFileName())) {
			// Disposition is missing but we have a valid attachment filename
			return true;
		}
		return false;
	}
	
	public boolean hasAttachments(Message m) throws MessagingException, IOException {
		return hasAttachments(m, null);
	}
	
	public boolean hasAttachments(Message m, String lowerCaseNamePattern) throws MessagingException, IOException {
		if (isAttachamentDetectUseBodyStructure() && m instanceof SonicleIMAPMessage)
			return ((SonicleIMAPMessage)m).hasAttachments(lowerCaseNamePattern);
		return _hasAttachments(m, lowerCaseNamePattern);
	}
	
	public boolean hasInvitation(Message m) throws MessagingException, IOException {
		if (isAttachamentDetectUseBodyStructure() && m instanceof SonicleIMAPMessage)
			return ((SonicleIMAPMessage)m).hasInvitation(false);
		return _hasInvitation(m);
	}
	
    private boolean _hasAttachments(Part p, String lowerCaseNamePattern) throws MessagingException, IOException {
        boolean retval=false;
        
		if (_isAttachment(p)) {
			if (lowerCaseNamePattern!=null) {
				String filename = getPartName(p);
				if (filename!=null && filename.toLowerCase().contains(lowerCaseNamePattern))
					retval=true;
			}
			else retval=true;
		}
        else if(p.isMimeType("multipart/*")) {
            Multipart mp=(Multipart)p.getContent();
            int parts=mp.getCount();
            for(int i=0;i<parts;++i) {
                Part bp=mp.getBodyPart(i);
                if (_hasAttachments(bp, lowerCaseNamePattern)) {
                    retval=true;
                    break;
                }
            }
        }
        
        return retval;
    }

    private boolean _hasInvitation(Part p) throws MessagingException, IOException {
        boolean retval=false;
        
		if (_isInvitation(p)) {
			retval=true;
		}
        else if(p.isMimeType("multipart/*")) {
            Multipart mp=(Multipart)p.getContent();
            int parts=mp.getCount();
            for(int i=0;i<parts;++i) {
                Part bp=mp.getBodyPart(i);
                if (_hasInvitation(bp)) {
                    retval=true;
                    break;
                }
            }
        }
        
        return retval;
    }
	
	/*public void sendMessage() {
		SimpleMessage msg = prepareMessage(jsmsg,msgId,save,isFax);
		Identity ifrom = msg.getFrom();
		String from = getUserProfile().getPersonalEmailAddress();
		if (ifrom != null) {
			from = ifrom.getEmail();
		}
		if (ifrom.isAlwaysCc()) {
			msg.addCc(new String[] { ifrom.hasAlwaysCcEmail()?ifrom.getAlwaysCcEmail():ifrom.getEmail() });
		}

		if (ifrom.isMainIdentity()) {
			String alwaysCc = mus.getAlwaysCc();
			if (alwaysCc!=null) {
				msg.addCc(new String[] { alwaysCc });
			}

			String alwaysBcc = mus.getAlwaysBcc();
			if (alwaysBcc!=null) {
				msg.addBcc(new String[] { alwaysBcc });
			}
		}

		SendException sendExc = sendMessage(msg, jsmsg.attachments);
					String foundfolder = null;
		if (sendExc == null || sendExc.messageSent) {
			//if is draft, check for deletion
			if (jsmsg.draftuid>0 && jsmsg.draftfolder!=null && mss.isDefaultFolderDraftsDeleteMsgOnSend()) {
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
//                if (vfs!=null && hashlinks!=null && hashlinks.size()>0) {
//			 for(String hash: hashlinks) {
//			 Service.logger.debug("Adding emails to hash "+hash);
//			 vfs.setAuthEmails(hash, from, emails);
//			 }
//
//			 }
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
	}*/

	public void setSieveConfiguration(String host, int port, String username, String password, String authId) {
		//TODO: portare i parametri (host,port,user,pass) nel manager
		this.sieveConfig = new SieveConfig(host, port, username, password, authId);
	}
	
	@Override
	public String getFolderSent() {
		return mus.getFolderSent();
	}
	
	@Override
	public String getFolderTrash() {
		return mus.getFolderTrash();
	}
	
	public String getFolderSent(Identity ident) {
		String mainFolder = null;
		if (ident != null ) {
			mainFolder=ident.getMainFolder();
		}

		return getFolderSent(mainFolder);
	}
	
	public String getFolderTrash(Identity ident) {
		String mainFolder = null;
		if (ident != null ) {
			mainFolder=ident.getMainFolder();
		}

		return getFolderTrash(mainFolder);
	}

	public String getFolderSent(String mainFolder) {
		UserProfile profile = getUserProfile();
		String sentFolder = getFolderSent();
		if (mainFolder != null && mainFolder.trim().length() > 0) {
			Mailbox mailbox = null;
			try {
				mailbox = getMailbox();
				char sep = mailbox.getFolderSeparator();
				String newSentFolder = mainFolder + sep + getLastFolderName(sentFolder, sep);
				Folder folder = mailbox.getFolder(newSentFolder);
				if (folder.exists()) {
					sentFolder = newSentFolder;
				}
			} catch (Exception exc) {
				logger.error("Error detectomg sent folder on main folder {}/{}", profile.getUserId(), mainFolder, exc);
			} finally {
				//mailbox.disconnect();
			}
		}
		return sentFolder;
	}
	
	public String getFolderTrash(String mainFolder) {
		UserProfile profile = getUserProfile();
		String trashFolder = getFolderTrash();
		if (mainFolder != null && mainFolder.trim().length() > 0) {
			Mailbox mailbox = null;
			try {
				mailbox = getMailbox();
				
				char sep = mailbox.getFolderSeparator();
				String newTrashFolder = mainFolder + sep + getLastFolderName(trashFolder, sep);
				Folder folder = mailbox.getFolder(newTrashFolder);
				if (folder.exists()) {
					trashFolder = newTrashFolder;
				}
			} catch (Exception exc) {
				logger.error("Error detectomg trash folder on main folder {}/{}", profile.getUserId(), mainFolder, exc);
			} finally {
				//mailbox.disconnect();
			}
		}
		return trashFolder;
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
			htmlsb.append(START_PRE + MailUtils.htmlescape(content) + END_PRE);
			isHtml = false;
		} else if (p.isMimeType("message/delivery-status") || p.isMimeType("message/disposition-notification")) {
			InputStream is = (InputStream) p.getContent();
			char cbuf[] = new char[8000];
			byte bbuf[] = new byte[8000];
			int n = 0;
			htmlsb.append(START_PRE);
			while ((n = is.read(bbuf)) >= 0) {
				if (n > 0) {
					for (int i = 0; i < n; ++i) {
						cbuf[i] = (char) bbuf[i];
					}
					textsb.append(cbuf);
					htmlsb.append(MailUtils.htmlescape(new String(cbuf)));
				}
			}
			htmlsb.append(END_PRE);
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

	private void htmlAppendAttachmentNames(StringBuffer sb, ArrayList<String> attnames) {
		if (attnames.size() > 0) {
			sb.append("<br><br>");
			for (String name : attnames) {
				sb.append("&lt;&lt;" + name + "&gt;&gt;<br>");
			}
		}
	}
		
	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	private String getReplyBody(Message msg, String body, int format, boolean isHtml, String fromtitle, String totitle, String cctitle, String datetitle, String subjecttitle, ArrayList<String> attnames) throws MessagingException {
		UserProfile profile = getUserProfile();
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
	
	public EmailMessage getReplyMessage(String folderId, long uid, boolean replyAll, boolean fromSent, boolean richContent, boolean includeOriginal, boolean attachMessageParts) {	
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		EmailMessage emsg = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			Message msg = (MimeMessage) folder.getMessageByUID(uid);
			emsg = getReplyMessage(msg, replyAll, fromSent, richContent, includeOriginal, attachMessageParts);
		} catch(Exception exc) {
			logger.error("Error getting message", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
		return emsg;
	}
	
	public EmailMessage getReplyMessage(Message msg, boolean replyAll, boolean fromSent, boolean richContent, boolean includeOriginal, boolean attachMessageParts) {	
		try {
			UserProfile profile = getUserProfile();
			Message reply = ((SonicleIMAPMessage)msg).reply(replyAll, false, fromSent);

			InternetAddress myEmail = InternetAddressUtils.toInternetAddress(getUserProfile().getPersonalEmailAddress());
			Set<InternetAddress> myAddresses = new LinkedHashSet<>();
			myAddresses.add(myEmail);
			if (mss.getMessageReplyAllStripMyIdentities()) {
				for (Identity ident : listIdentities()) {
					myAddresses.add(InternetAddressUtils.toInternetAddress(ident.getEmail()));
				}
			}
			if (!myAddresses.isEmpty()) Recipients.removeAnyFrom(reply, myAddresses);

			Address[] origTos = msg.getReplyTo();
			Address[] newRcpts = reply.getAllRecipients();
			boolean emptyRcpts = (newRcpts == null || newRcpts.length == 0);
			if (emptyRcpts && (origTos != null)) {
				InternetAddress newTo = null;
				if (origTos.length == 1) {
					newTo = (InternetAddress)origTos[0];
				} else if (origTos.length > 1) {
					if (myEmail.equals((InternetAddress)origTos[0])) {
						newTo = (InternetAddress)origTos[1];
					} else {
						newTo = (InternetAddress)origTos[0];
					}
				}
				if (newTo != null) {
					reply.addRecipient(RecipientType.TO, newTo);
				}
			}
			
			EmailPopulatingBuilder epb = EmailMessageBuilder.startingBlank();
			Address[] rcpts = reply.getRecipients(Message.RecipientType.TO);
			if (rcpts != null)
				for(Address rcpt: rcpts) {
					epb.to(InternetAddressUtils.toInternetAddress(rcpt.toString()));
				}
			rcpts = reply.getRecipients(Message.RecipientType.CC);
			if (rcpts != null)
				for(Address rcpt: rcpts) {
					epb.cc(InternetAddressUtils.toInternetAddress(rcpt.toString()));
				}
			rcpts = reply.getRecipients(Message.RecipientType.BCC);
			if (rcpts != null)
				for(Address rcpt: rcpts) {
					epb.bcc(InternetAddressUtils.toInternetAddress(rcpt.toString()));
				}
			
			epb.withSubject(reply.getSubject());
			
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
				
				Locale locale = profile.getLocale();
				String fromtitle = lookupResource(locale, MailLocaleKey.MSG_FROMTITLE);
				String totitle = lookupResource(locale, MailLocaleKey.MSG_TOTITLE);
				String cctitle = lookupResource(locale, MailLocaleKey.MSG_CCTITLE);
				String datetitle = lookupResource(locale, MailLocaleKey.MSG_DATETITLE);
				String subjecttitle = lookupResource(locale, MailLocaleKey.MSG_SUBJECTTITLE);
				
				if (!richContent) {
					text = getReplyBody(msg, textsb.toString(), SimpleMessage.FORMAT_TEXT, false, fromtitle, totitle, cctitle, datetitle, subjecttitle, attnames);
				} else if (!isHtml) {
					text = getReplyBody(msg, htmlsb.toString(), SimpleMessage.FORMAT_PREFORMATTED, true, fromtitle, totitle, cctitle, datetitle, subjecttitle, attnames);
				} else {
					text = getReplyBody(msg, html, SimpleMessage.FORMAT_HTML, true, fromtitle, totitle, cctitle, datetitle, subjecttitle, attnames);
				}
				String newHtml=MailUtils.removeMSWordShit(text);
				
				if (mss.isReFwSanitizeDownlevelRevealedComments())
					newHtml=MailUtils.sanitizeDownlevelRevealedComments(newHtml);

				epb.withHTMLText(newHtml);
				
				setReferences(epb, (MimeMessage)msg);
				
				//consider cid inline attachments
				MimeMessageParser mmp = new MimeMessageParser();
				MimeMessageParser.ParsedMimeMessageComponents parsed = mmp.parse((MimeMessage)msg, false);
				for (Part part: parsed.getCidParts().values()) {
					try{
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
						boolean inline = false;
						if (part.getDisposition() != null) {
							inline = part.getDisposition().equalsIgnoreCase(Part.INLINE) &&
									isInlineableMime(mime);
						}
						//in reply includes only cid & inline attachments
						if (inline && cid!=null) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							IOUtils.copy(part.getInputStream(), baos);
							epb.withAttachment(baos.toByteArray(), mime, filename, cid);
						}
					} catch (Exception exc) {
						Service.logger.error("Exception",exc);
					}
				}
				
			} else {
				epb.withHTMLText("");
			}
			return epb.build();
		} catch (MessagingException|WTException|IOException e) {
			Service.logger.error("Exception",e);
			return null;
		}
	}
	
	
	public EmailMessage getForwardMessage(String folderId, long uid, boolean richContent, boolean attachMessageParts) {	
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		EmailMessage emsg = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			Message msg = (MimeMessage) folder.getMessageByUID(uid);
			emsg = getForwardMessage(msg, richContent, attachMessageParts);
		} catch(Exception exc) {
			logger.error("Error getting message", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
		return emsg;
	}
	
	public EmailMessage getForwardMessage(Message msg, boolean richContent, boolean attachMessageParts) {
		try {
			UserProfile profile = getUserProfile();
			EmailPopulatingBuilder epb = EmailMessageBuilder.startingBlank();

			StringBuffer htmlsb = new StringBuffer();
			StringBuffer textsb = new StringBuffer();
			String defaultCharset=null;
			if (msg.isMimeType("multipart/*")) defaultCharset=getDefaultCharset((Multipart)msg.getContent());
			boolean isHtml = appendReplyParts(msg, defaultCharset, htmlsb, textsb, null, attachMessageParts);
			String html = "<HTML><BODY>" + htmlsb.toString() + "</BODY></HTML>";

			Locale locale = profile.getLocale();
			String fromtitle = lookupResource(locale, MailLocaleKey.MSG_FROMTITLE);
			String totitle = lookupResource(locale, MailLocaleKey.MSG_TOTITLE);
			String cctitle = lookupResource(locale, MailLocaleKey.MSG_CCTITLE);
			String datetitle = lookupResource(locale, MailLocaleKey.MSG_DATETITLE);
			String subjecttitle = lookupResource(locale, MailLocaleKey.MSG_SUBJECTTITLE);

			String newHtml = "";
			if (!richContent) {
				//use the original unchanged text content
				newHtml = getForwardBody(msg, textsb.toString(), SimpleMessage.FORMAT_TEXT, false, fromtitle, totitle, cctitle, datetitle, subjecttitle);
			} else if (!isHtml) {
				//use html content, which is text content with possible html encoded characters
				newHtml = getForwardBody(msg, htmlsb.toString(), SimpleMessage.FORMAT_PREFORMATTED, true, fromtitle, totitle, cctitle, datetitle, subjecttitle);
			} else {
				newHtml=MailUtils.removeMSWordShit(
					getForwardBody(msg, html, SimpleMessage.FORMAT_HTML, true, fromtitle, totitle, cctitle, datetitle, subjecttitle)
				);
				if (mss.isReFwSanitizeDownlevelRevealedComments())
					newHtml=MailUtils.sanitizeDownlevelRevealedComments(newHtml);

									//take care of possible html shit
			}
			epb.withHTMLText(newHtml);
			
			setReferences(epb, (MimeMessage)msg);

			String subject = msg.getSubject();
			String newSubject = "";
			if (subject == null) {
				subject = "";
			}
			if (!subject.toLowerCase().startsWith("fwd: ")) {
				newSubject = "Fwd: " + subject;
			} else {
				newSubject = msg.getSubject();
			}
			epb.withSubject(newSubject);
			
			//consider cid inline attachments
			MimeMessageParser mmp = new MimeMessageParser();
			MimeMessageParser.ParsedMimeMessageComponents parsed = mmp.parse((MimeMessage)msg, false);
			for (Part part: parsed.getAttachmentParts()) {
				try{
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
					boolean inline = false;
					if (part.getDisposition() != null) {
						inline = part.getDisposition().equalsIgnoreCase(Part.INLINE) &&
								isInlineableMime(mime);
					}
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					IOUtils.copy(part.getInputStream(), baos);
					epb.withAttachment(baos.toByteArray(), mime, filename, cid);
				} catch (Exception exc) {
					Service.logger.error("Exception",exc);
				}
			}


			return epb.build();
		} catch (MessagingException|IOException e) {
			Service.logger.error("Exception",e);
			return null;
		}
	}
	
	private void setReferences(EmailPopulatingBuilder epb, MimeMessage msg) throws MessagingException {
		String msgid = null;
		String vh[] = msg.getHeader("Message-ID");
		if (vh != null) {
			msgid = vh[0];
		}
		if (msgid != null) {
			epb.withForwardedFrom(msgid);
			epb.withInReplyTo(msgid);
		}

		String refs = msg.getHeader("References", " ");
		if (refs == null) {
			// XXX - should only use if it contains a single message identifier
			refs = msg.getHeader("In-Reply-To", " ");
		}
		if (msgid != null) {
			if (refs != null) {
				refs = MimeUtility.unfold(refs) + " " + msgid;
			} else {
				refs = msgid;
			}
		}
		if (refs != null) {
			epb.withReferences(MimeUtility.fold(12, refs));
		}
	}
	
	private String getForwardBody(Message msg, String body, int format, boolean isHtml, String fromtitle, String totitle, String cctitle, String datetitle, String subjecttitle) throws MessagingException {
		UserProfile profile = getUserProfile();
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

	public boolean getMessageSeenState(String folderId, long uid) {	
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		boolean seen = true;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_ONLY);
			Message msg = (MimeMessage) folder.getMessageByUID(uid);
			seen = msg.isSet(Flags.Flag.SEEN);
		} catch(Exception exc) {
			logger.error("Error getting message", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
		return seen;
	}
	
	public void setMessageSeenState(String folderId, long uid, boolean seen) {	
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_WRITE);
			Message msg = (MimeMessage) folder.getMessageByUID(uid);
			msg.setFlag(Flags.Flag.SEEN, seen);
		} catch(Exception exc) {
			logger.error("Error getting message", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}
	
	public void setMessageFlag(String folderId, long uid, String flag) {	
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_WRITE);
			Message msg = (MimeMessage) folder.getMessageByUID(uid);			
			
			if (flag.equals("special")) {
				boolean wasspecial=msg.getFlags().contains(getFlagFlagged());
				msg.setFlags(getFlagFlagged(),!wasspecial);
			}
			else {
				if (!flag.equals("complete")) {
					msg.setFlags(flagsAll, false);
					msg.setFlags(oldFlagsAll, false);
				}
				msg.setFlags(flagsHash.get(flag), true);
			}
			
		} catch(Exception exc) {
			logger.error("Error on setMessageFlag", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}
	
	public void setMessageTags(String folderId, long uid, List<String> tags) {	
		IMAPFolder folder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			folder = (IMAPFolder) mailbox.getFolder(folderId);
			folder.open(Folder.READ_WRITE);
			Message msg = (MimeMessage) folder.getMessageByUID(uid);			

			Flags flags = new Flags();
			for(String tagId: tags) {
				String flag = TagsHelper.tagIdToFlagString(WT.getCoreManager().getTag(tagId));
				flags.add(flag);
			}
			msg.setFlags(flags, true);
			
		} catch(Exception exc) {
			logger.error("Error on setMessageFlag", exc);
		} finally {
			StoreUtils.closeQuietly(folder, false);
			//mailbox.disconnect();
		}
	}
	
	public void trashMessage(String folderId, long uid) {	
		IMAPFolder fromFolder = null;
		IMAPFolder toFolder = null;
		Mailbox mailbox = null;
		try {
			String folderTrashId = getFolderTrash();
			mailbox = getMailbox();
			if (mailbox.isUnderSharedFolder(folderId)) {
				String mainfolder=mailbox.getMainSharedFolder(folderId);
				if (mainfolder!=null) {
					char sep = mailbox.getFolderSeparator();
					folderTrashId = mainfolder + sep + getLastFolderName(folderTrashId, sep);
				}
			}
			fromFolder = (IMAPFolder) mailbox.getFolder(folderId);
			toFolder = (IMAPFolder) mailbox.getFolder(folderTrashId);
			
			fromFolder.open(Folder.READ_WRITE);
			toFolder.open(Folder.READ_WRITE);
			Message msg = (MimeMessage) fromFolder.getMessageByUID(uid);
			Message amsg[] = new Message[] { msg };
			fromFolder.copyMessages(amsg, toFolder);
			fromFolder.setFlags(amsg, new Flags(Flags.Flag.DELETED), true);
			fromFolder.expunge();
		} catch(Exception exc) {
			logger.error("Error trashing messages", exc);
		} finally {
			StoreUtils.closeQuietly(fromFolder, false);
			StoreUtils.closeQuietly(toFolder, false);
			//mailbox.disconnect();
		}
	}

	public void deleteMessage(String folderId, long uid) {	
		IMAPFolder fromFolder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			fromFolder = (IMAPFolder) mailbox.getFolder(folderId);
			
			fromFolder.open(Folder.READ_WRITE);
			Message msg = (MimeMessage) fromFolder.getMessageByUID(uid);
			Message amsg[] = new Message[] { msg };
			fromFolder.setFlags(amsg, new Flags(Flags.Flag.DELETED), true);
			fromFolder.expunge();
		} catch(Exception exc) {
			logger.error("Error deleting messages", exc);
		} finally {
			StoreUtils.closeQuietly(fromFolder, false);
			//mailbox.disconnect();
		}
	}

	public void moveMessage(String fromFolderId, String toFolderId, long uid) {	
		IMAPFolder fromFolder = null;
		IMAPFolder toFolder = null;
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			fromFolder = (IMAPFolder) mailbox.getFolder(fromFolderId);
			toFolder = (IMAPFolder) mailbox.getFolder(toFolderId);
			
			fromFolder.open(Folder.READ_WRITE);
			toFolder.open(Folder.READ_WRITE);
			Message msg = (MimeMessage) fromFolder.getMessageByUID(uid);
			Message amsg[] = new Message[] { msg };
			fromFolder.copyMessages(amsg, toFolder);
			fromFolder.setFlags(amsg, new Flags(Flags.Flag.DELETED), true);
			fromFolder.expunge();
		} catch(Exception exc) {
			logger.error("Error moving messages", exc);
		} finally {
			StoreUtils.closeQuietly(fromFolder, false);
			StoreUtils.closeQuietly(toFolder, false);
			//mailbox.disconnect();
		}
	}

	public String getLastFolderName(String fullname, char separator) {
		String lasttname = fullname;
		if (lasttname.indexOf(separator) >= 0) {
			lasttname = lasttname.substring(lasttname.lastIndexOf(separator) + 1);
		}
		return lasttname;
	}
	
	public void sendMessage(final UserProfileId sendingProfileId, final EmailPopulatingBuilder epb, int identityId) throws WTEmailSendException, WTException {
		List<Identity> identities = listIdentities();
		Identity identity = identities.get(identityId);
		sendMessage(sendingProfileId, epb, identity);
	}
	
	public void sendMessage(final UserProfileId sendingProfileId, final EmailPopulatingBuilder epb, final Identity ifrom) throws WTEmailSendException, WTException {
		if (ifrom.isAlwaysCc()) {
			epb.cc(ifrom.getDisplayName(), ifrom.getEmail());
		}

		if (ifrom.isMainIdentity()) {
			String alwaysCc = mus.getAlwaysCc();
			if (alwaysCc!=null) {
				epb.cc(alwaysCc);
			}

			String alwaysBcc = mus.getAlwaysBcc();
			if (alwaysBcc!=null) {
				epb.bcc(alwaysBcc);
			}
		}
		EmailMessage emsg = epb.build();
		WT.sendEmailMessage(sendingProfileId, emsg, getFolderSent(ifrom));

		CoreManager coreMgr = WT.getCoreManager();
		
		//Save used recipients
		ArrayList<InternetAddress> iaTos = new ArrayList<>();
		ArrayList<InternetAddress> iaCcs = new ArrayList<>();
		ArrayList<InternetAddress> iaBccs = new ArrayList<>();
		for(Recipient rcpt: emsg.getRecipients()) {
			InternetAddress ia = rcpt.asInternetAddress();
			if (!ContactsUtils.isListVirtualRecipient(ia)) coreMgr.autoLearnInternetRecipient(InternetAddressUtils.toFullAddress(ia));
			RecipientType rcptType = rcpt.getType();
			if (rcptType.equals(RecipientType.TO)) iaTos.add(ia);
			else if (rcptType.equals(RecipientType.CC)) iaCcs.add(ia);
			else if (rcptType.equals(RecipientType.BCC)) iaBccs.add(ia);
		}
		//Save subject for suggestions
		String subject = emsg.getSubject();
		if (!StringUtils.isBlank(subject))
			WT.getCoreManager().saveMetaEntry(SERVICE_ID, "subject", subject, subject, true);
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
	
	public ArrayList<String> searchNotes(String pattern) throws WTException {
		ArrayList<String> msgIds=new ArrayList<>();
		Connection con = null;
		try {
			con = WT.getConnection(SERVICE_ID);
			List<ONote> notes=NoteDAO.getInstance().selectByLike(con, getTargetProfileId().getDomainId(), "%"+pattern+"%");
			for(ONote note: notes) msgIds.add(note.getMessageId());
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return msgIds;
	}
	
	public List<Identity> listIdentities() throws WTException {
		if (identities==null)
			identities=buildIdentities();
		
		return identities;
	}
    
	public List<Identity> listAllPersonalIdentities(String domainId) throws WTException {
		List<Identity> allPersonalIdentities=allPersonalIdentitiesDomains.get(domainId);
		if (allPersonalIdentities==null) {
			allPersonalIdentitiesDomains.put(domainId, allPersonalIdentities=buildAllPersonalIdentities(domainId));
		}
		
		return allPersonalIdentities;
	}
	
    public Identity getMainIdentity() {
		if (identities==null) {
            try {
                identities=buildIdentities();
            } catch(WTException exc) {}
        }
        return identities.get(0);
    }
	
	public Identity folderHasIdentity(String folder) {
		return identHash.get(folder);
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
	
	public Identity findIdentity(int id) throws WTException {
                if (identities==null)
                    identities=buildIdentities();
                
		for(Identity ident: identities) {
			if (ident.getIdentityId()==id) 
				return ident;
		}
		return null;
	}
	
	protected List<Identity> buildAllPersonalIdentities(String domainId) throws WTException {
		Connection con=null;
		List<Identity> idents=new ArrayList();
		try {			
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			List<OIdentity> items=idao.selectByDomain(con, domainId);
			for(OIdentity oi: items) {
				Identity ident=new Identity(oi);
				idents.add(ident);
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return idents;
	}
	
	private List<Identity> buildIdentities() throws WTException {
		Connection con=null;
		List<Identity> idents=new ArrayList();
		Mailbox mailbox = null;
		try {
			mailbox = getMailbox();
			UserProfileId pid=getTargetProfileId();
			//first add main identity
			Data udata=WT.getProfileData(pid);
			Identity id=new Identity(0,null,udata.getDisplayName(),udata.getPersonalEmail().getAddress(),null);
			id.setIsMainIdentity(true);
			loadMainIdentityMailcard(id);
			idents.add(id);
			
			//add configured additional identities
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			List<OIdentity> items=idao.selectByDomainUser(con, pid.getDomainId(),pid.getUserId());
			for(OIdentity oi: items) {
				Identity ident=new Identity(oi);
				loadIdentityMailcard(mailbox, ident);
				idents.add(ident);
				identHash.put(ident.getMainFolder(), ident);
			}
			
			//add automatic shared identities
			int autoid=-1;
			CoreManager core=WT.getCoreManager(pid);
			for(ShareOrigin origin: core.listShareOrigins(SERVICE_ID, IDENTITY_SHARING_CONTEXT, Arrays.asList(IDENTITY_PERMISSION_KEY))) {
				UserProfileId opid=origin.getProfileId(); 
				UserProfile.Data opdata=WT.getProfileData(opid);
				Map<String, Sharing.SubjectConfiguration> sconfigurations = core.getShareSubjectConfiguration(SERVICE_ID, IDENTITY_SHARING_CONTEXT, opid, "*", IDENTITY_PERMISSION_KEY, LangUtils.asSet(pid), FolderShareParameters.class);
				if (sconfigurations.isEmpty()) continue;
				Entry<String, Sharing.SubjectConfiguration> entry = sconfigurations.entrySet().iterator().next();
				FolderShareParameters fsp = entry.getValue().getTypedData(FolderShareParameters.class);
				if (fsp!=null && fsp.shareIdentity) {
					id = new Identity(
							Identity.TYPE_AUTO,
							autoid--,
							null,
							opdata.getDisplayName(),
							opdata.getPersonalEmailAddress(),
							null,
							false,
							fsp.forceMailcard,
							fsp.alwaysCc,
							fsp.alwaysCcEmail);
					id.setOriginPid(opid);					
					loadIdentityMailcard(mailbox, id);
					idents.add(id);
				}
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			//if (mailbox!=null) mailbox.disconnect();
			DbUtils.closeQuietly(con);
		}
		return idents;
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
	
	private void loadMainIdentityMailcard(Identity id) {
		Mailcard mc = getMailcard();
		try {
			UserProfile.PersonalInfo upi = WT.getCoreManager().getUserPersonalInfo(getTargetProfileId());
			mc.substitutePlaceholders(upi);			
		} catch(WTException exc) {
			logger.error("cannot load user personal info",exc);
		}
		//mc.html=LangUtils.stripLineBreaks(mc.html);
		id.setMailcard(mc);
	}
	
	private void loadIdentityMailcard(Mailbox mailbox, Identity id) {
		Mailcard mc = getMailcard(id);
		if (mc!=null) {
			if(id.isType(Identity.TYPE_AUTO)) {
				UserProfileId opid=id.getOriginPid();
				// In case of auto identities we need to build real mainfolder
				try {
					String mailUser = getMailUsername(opid);
					String mainfolder = mailbox.getSharedFolderName(mailUser);
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
					mc=getMailcard();
					opid=getTargetProfileId();
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
						UserProfile.PersonalInfo upi = WT.getCoreManager().getUserPersonalInfo(getTargetProfileId());
						mc.substitutePlaceholders(upi);			
				} catch(WTException exc) {
						logger.error("cannot load user personal info",exc);
				}
			}
		}
		//mc.html=LangUtils.stripLineBreaks(mc.html);
		id.setMailcard(mc);
	}
	
	public List<ExternalAccount> listExternalAccounts() throws WTException {
			List<ExternalAccount> externalAccountList =  new ArrayList<>();
			Connection connection = null;
			ExternalAccountDAO dao = ExternalAccountDAO.getInstance();
		try {
			
			connection = WT.getConnection(SERVICE_ID);
			UserProfileId userProfileId = getTargetProfileId();
			List<OExternalAccount> externalAccounts = dao.selectByDomainUser(connection, userProfileId.getDomainId(), userProfileId.getUserId(), MAX_EXT_ACCOUNTS);
			for(OExternalAccount externalAccount: externalAccounts) {
				ExternalAccount account = new ExternalAccount();
				account.setExternalAccountId(externalAccount.getExternalAccountId());
				account.setDisplayName(externalAccount.getDisplayName());
				account.setAccountDescription(externalAccount.getDescription());
				account.setEmail(externalAccount.getEmail());
				account.setProtocol(externalAccount.getProtocol());
				account.setHost(externalAccount.getHost());
				account.setPort(externalAccount.getPort());
				account.setReadOnly(externalAccount.getReadOnly());
				account.setProviderId(externalAccount.getProviderId());
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
			account.setReadOnly(externalAccount.getReadOnly());
			account.setProviderId(externalAccount.getProviderId());
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
			account.setReadOnly(externalAccount.isReadOnly());
			account.setProviderId(externalAccount.getProviderId());
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
		Data udata=WT.getProfileData(pid);
		String domainId=pid.getDomainId();
		String emailAddress=udata.getPersonalEmail().getAddress();
		Mailcard mc = readEmailMailcard(domainId,emailAddress);
		if(mc != null) return mc;
		mc = readUserMailcard(domainId,pid.getUserId());
		if(mc != null) return mc;
		mc = readEmailDomainMailcard(domainId,emailAddress);
		if(mc != null) return mc;
		return readDefaultMailcard(domainId);
    }
	
	public Mailcard getMailcard(UserProfileId pid) {
		Data udata=WT.getProfileData(pid);
		String domainId=pid.getDomainId();
		String emailAddress=udata.getPersonalEmail().getAddress();
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
		final UserProfileId targetPid = ensureProfileDomain(RunContext.AdminScope.SYSADMIN);
		if (RunContext.isWebTopAdmin() || RunContext.isPermitted(true, targetPid, SERVICE_ID, "MAILCARD_SETTINGS", "CHANGE")) {
			writeMailcard(targetPid.getDomainId(), "mailcard_" + targetPid.getUserId(), html);
		} else {
			throw new AuthException("You have insufficient rights to perform the operation [{}]", "MAILCARD_SETTINGS:CHANGE");
		}
	}
	
	public void setEmailMailcard(String email, String html) {
		final UserProfileId targetPid = ensureProfileDomain(RunContext.AdminScope.SYSADMIN);
		if (RunContext.isWebTopAdmin() || RunContext.isPermitted(true, targetPid, SERVICE_ID, "MAILCARD_SETTINGS", "CHANGE")) {
			writeMailcard(targetPid.getDomainId(), "mailcard_" + email, html);
		} else {
			throw new AuthException("You have insufficient rights to perform the operation [{}]", "MAILCARD_SETTINGS:CHANGE");
		}
	}

	public void setEmailDomainMailcard(String email, String html) {
		final UserProfileId targetPid = ensureProfileDomain(RunContext.AdminScope.SYSADMIN);
		if (RunContext.isWebTopAdmin() || RunContext.isPermitted(true, targetPid, SERVICE_ID, "DOMAIN_MAILCARD_SETTINGS", "CHANGE")) {
			if (!StringUtils.contains(email, "@")) return;
			writeMailcard(targetPid.getDomainId(), "mailcard_" + StringUtils.substringAfterLast(email, "@"), html);
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
	
	
	
	
	
	public List<Tag> getOldTags() throws WTException {
		TagDAO tagdao = TagDAO.getInstance();
		List<Tag> tags = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
				List<OTag> items = tagdao.selectByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
				for (OTag item : items) {
					tags.add(createOldTag(item));
				}
				return tags;
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addOldTag(Tag tag) throws WTException {
		TagDAO tagdao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			tagdao.insert(con, createOldTag(getTargetProfileId().getDomainId(), getTargetProfileId().getUserId(),tag));
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void removeOldTag(String tagId) throws WTException {
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
	
	public void updateOldTag(Tag tag,String newTagId) throws WTException {
		TagDAO tagdao = TagDAO.getInstance();
		Connection con = null;
		String oldTagId="";
		
		try {
			con = WT.getConnection(SERVICE_ID);
			oldTagId=tag.getTagId();
			tag.setTagId(newTagId);
			tagdao.update(con,oldTagId, createOldTag(getTargetProfileId().getDomainId(), getTargetProfileId().getUserId(),tag));
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
/*	public void updateFoldersTag(String oldTagId , String newTagId, Collection<FolderCache> folders, SearchTerm searchTerm, boolean hasAttachment){
		
		for(FolderCache fc: folders){
			Message msgs[] = null;
			try {
				msgs = fc.getMessages(FolderCache.SORT_BY_DATE, false, true, -1, true, false, searchTerm, hasAttachment);
				long[] uid = new long[msgs.length];
				for (int i=0;i<msgs.length;i++) {
					uid[i]=fc.getUID(msgs[i]);
				}
				fc.updateMessageTag(uid, oldTagId, newTagId);
			} catch (MessagingException ex) {
				logger.error("Error updating folder tags on "+fc.getFolderName(),ex);
			} catch (IOException ex) {
				logger.error("Error updating folder tags on "+fc.getFolderName(),ex);
			}
		}
	}*/
	
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
	
	protected void addOldBuiltinTags() throws WTException {
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
	
	private Tag createOldTag(OTag otag) {
		if (otag == null) return null;
		Tag tag = new Tag(otag.getTagId(),otag.getDescription(),otag.getColor());
		return tag;
	}	
	
	private OTag createOldTag(String domainId, String userId, Tag tag) {
		if (tag == null) return null;
		OTag otag = new OTag();
		otag.setDomainId(domainId);
		otag.setUserId(userId);
		otag.setTagId(tag.getTagId());
		otag.setDescription(tag.getDescription());
		otag.setColor(tag.getColor());
		return otag;
	}	
	
	protected void convertToCoreTags() throws WTException {
		Connection con = null;
		UserProfileId profileId = getTargetProfileId();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			List<Tag> oldTags=this.getOldTags();
			for(Tag oldTag: oldTags) {
				com.sonicle.webtop.core.model.Tag tag=new com.sonicle.webtop.core.model.Tag();
				tag.setBuiltIn(false);
				tag.setColor(oldTag.getColor());
				tag.setDomainId(profileId.getDomainId());
				tag.setExternalId(oldTag.getTagId());
				tag.setName(oldTag.getDescription());
				tag.setVisibility(com.sonicle.webtop.core.model.Tag.Visibility.PRIVATE);
				WT.getCoreManager().addTag(tag);
			}
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean isAutoResponderActive() throws WTException {
		AutoResponderDAO autdao = AutoResponderDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OAutoResponder oaut = autdao.selectByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			return (oaut != null) ? oaut.getEnabled() : false;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
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
	
	public List<MailFilter> getMailFilters(final MailFiltersType type, final EnabledCond enabled) throws WTException {
		return getMailFilters(type, enabled, null);
	}
	
	public List<MailFilter> getMailFilters(final MailFiltersType type, final EnabledCond enabled, final Short builtIn) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return doMailFiltersGet(con, type, enabled, builtIn);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateMailFilters(final MailFiltersType type, final List<MailFilter> filters) throws WTException {
		Connection con = null;
		
		//TODO: valutare permessi... admin?
		try {
			con = WT.getConnection(SERVICE_ID, false);
			doMailFiltersUpdate(con, type, filters, true);
			DbUtils.commitQuietly(con);
			
			//TODO: update cache
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<MailFilter> doMailFiltersGet(Connection con, MailFiltersType type, EnabledCond enabled) throws WTException {
		return doMailFiltersGet(con, type, enabled, null);
	}
	
	private List<MailFilter> doMailFiltersGet(Connection con, MailFiltersType type, EnabledCond enabled, Short builtIn) throws WTException {
		InFilterDAO infDao = InFilterDAO.getInstance();
		
		if (type.equals(MailFiltersType.INCOMING)) {
			List<OInFilter> ofilters = infDao.selectByProfileBuiltInEnabled(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId(), builtIn, enabled);
			List<MailFilter> items = new ArrayList<>(ofilters.size());
			for (OInFilter ofilter : ofilters) {
				items.add(ManagerUtils.fillMailFilter(new MailFilter(), ofilter));
			}
			return items;
			
		} else {
			throw new WTException("Type '{}' is not supported yet", type.toString());
		}
	}
	
	public void doMailFiltersUpdate(Connection con, final MailFiltersType type, final List<MailFilter> filters, final boolean refreshOrder) throws WTException {
		InFilterDAO infDao = InFilterDAO.getInstance();
		
		if (type.equals(MailFiltersType.INCOMING)) {
			List<MailFilter> oldFilters = doMailFiltersGet(con, type, EnabledCond.ANY_STATE);
			LangUtils.ChangeSet<MailFilter> changes = LangUtils.computeChangeSet(oldFilters, filters);
			for (MailFilter filter : changes.getAdded()) {
				OInFilter oinfilter = ManagerUtils.fillOInFilter(new OInFilter(), filter);
				oinfilter.setInFilterId(infDao.getSequence(con).intValue());
				oinfilter.setDomainId(getTargetProfileId().getDomainId());
				oinfilter.setUserId(getTargetProfileId().getUserId());
				infDao.insert(con, oinfilter);
			}
			for (MailFilter filter : changes.getUpdated()) {
				infDao.update(con, ManagerUtils.fillOInFilter(new OInFilter(), filter));
			}
			for (MailFilter filter : changes.getRemoved()) {
				infDao.delete(con, filter.getFilterId());
			}
			if (refreshOrder) {
				infDao.updateOrderByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			}
			
		} else {
			throw new WTException("Type '{}' is not supported yet", type.toString());
		}
	}
	
	public void initDefaultSieveScript() throws WTException {
		List<com.fluffypeople.managesieve.SieveScript> scripts = listSieveScripts();
		String activeScript = ManagerUtils.findActiveScriptName(scripts);
		if (StringUtils.isBlank(activeScript)) {
			applySieveScript(true);
		}
	}
	
	public List<SieveScript> listSieveScripts() throws WTException {
		ManageSieveClient client = null;
		
		ensureProfileDomain(RunContext.AdminScope.DOMAINADMIN);
		try {
			client = createSieveClient();
			return SieveHelper.listScripts(client);
		} finally {
			SieveHelper.logoutSieveClientQuietly(client);
		}
	}
	
	public String getActiveSieveScriptName() throws WTException {
		ManageSieveClient client = null;
		
		ensureProfileDomain(RunContext.AdminScope.DOMAINADMIN);
		try {
			client = createSieveClient();
			return SieveHelper.getActiveScript(client);
		} finally {
			SieveHelper.logoutSieveClientQuietly(client);
		}
	}
	
	public void activateSieveScript(String name) throws WTException {
		ManageSieveClient client = null;
		
		ensureProfileDomain(RunContext.AdminScope.DOMAINADMIN);
		try {
			client = createSieveClient();
			SieveHelper.activateScript(client, name);
		} finally {
			SieveHelper.logoutSieveClientQuietly(client);
		}
	}
	
	public void applySieveScript(boolean activate) throws WTException {
		UserProfile.Data pdata = WT.getProfileData(getTargetProfileId());
		SieveScriptBuilder ssb = new SieveScriptBuilder();
		MailServiceSettings mss = new MailServiceSettings(SERVICE_ID,getTargetProfileId().getDomainId());
		
		ensureProfileDomain(RunContext.AdminScope.DOMAINADMIN);
		
		if (!mss.isSieveSpamFilterDisabled()) {
			ssb.setSpamFilter(mus.getFolderSpam());
		}
		
		logger.debug("Working on autoresponder...");
		AutoResponder autoResp = getAutoResponder();
		if (autoResp.getEnabled()) {
			String profileEmail = StringUtils.lowerCase(pdata.getProfileEmailAddress());
			String personalEmail = StringUtils.lowerCase(pdata.getPersonalEmailAddress());
			String tokens[] = StringUtils.splitByWholeSeparator(StringUtils.lowerCase(StringUtils.replace(autoResp.getAddresses(), " ", "")), ",");
			Set<String> addresses = Collections.<String>emptySet();
			if (tokens != null) {
				addresses = new HashSet(Arrays.asList(tokens));
			}
			
			if (!profileEmail.equals(personalEmail) && !addresses.contains(personalEmail)) {
				autoResp.setAddresses(LangUtils.joinStrings(",", autoResp.getAddresses(), personalEmail));
			}
			
			ssb.setVacation(autoResp.toSieveVacation(pdata.getPersonalEmail(), pdata.getTimeZone()));
		}
		
		logger.debug("Working on incoming filters...");
		List<MailFilter> filters = getMailFilters(MailFiltersType.INCOMING, EnabledCond.ENABLED_ONLY);
		
		/*
		// Arrange filters in the specified order and fill the builder
		Collections.sort(filters, new Comparator<MailFilter>() {
			@Override
			public int compare(MailFilter o1, MailFilter o2) {
				return Short.compare(o1.getOrder(), o2.getOrder());
			}
		});
		*/
		for (MailFilter filter : filters) {
			if (ManagerUtils.MAILFILTER_SENDERBLACKLIST_BUILTIN.equals(filter.getBuiltIn())) {
				ssb.addPrioritizedFilter(filter.getName(), filter.getSieveMatch(), filter.getSieveRules(), filter.getSieveActions());
			} else {
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
		return SieveHelper.createSieveClient(sieveConfig.getHost(), sieveConfig.getPort(), sieveConfig.getUsername(), sieveConfig.getPassword(), sieveConfig.getAuthId());
	}
	
	private String buildSieveScriptHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("# Generated by WebTop [http://www.sonicle.com]");
		sb.append("\n");
		sb.append("# ").append(SERVICE_ID).append("@").append(WT.getManifest(SERVICE_ID).getVersion().toString());
		sb.append("\n");
		sb.append("# ").append(JavaTimeUtils.printISO(JavaTimeUtils.now()));
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
	
	/*
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
	*/
	
	private static class SieveConfig {
		private String host;
		private int port;
		private String username;
		private char[] password;
		private String authId;
		
		public SieveConfig(String host, int port, String username, String password, String authId) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password.toCharArray();
			this.authId = authId;
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
		
		public String getAuthId() {
			return authId;
		}
	}
	
	protected enum AuditContext {
		MAIL, FOLDER
	}
	
	protected enum AuditAction {
		CREATE, RENAME, DELETE, MOVE, FORWARD, REPLY, VIEW, PRINT, TAG, EMPTY, TRASH, COPY, ARCHIVE
	}
}
