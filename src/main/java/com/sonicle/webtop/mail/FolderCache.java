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
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.RegexUtils;
import com.sonicle.mail.Mailbox;
import com.sonicle.mail.imap.*;
import com.sonicle.mail.tnef.internet.*;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.*;
import com.sonicle.webtop.mail.ws.RecentMessage;
import com.sonicle.webtop.mail.ws.UnreadChangedMessage;
import com.sun.mail.imap.*;
import java.io.*;
//import com.sonicle.webtop.util.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.mail.*;
import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.event.MessageChangedEvent;
import jakarta.mail.event.MessageChangedListener;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.event.MessageCountListener;
import jakarta.mail.internet.*;
import jakarta.mail.search.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import net.fortuna.ical4j.data.*;
import org.joda.time.LocalDate;
import org.apache.commons.lang3.StringUtils;
import com.sonicle.commons.collection.FifoMap;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.JsonUtils;
import com.sonicle.webtop.core.app.AuditLogManager;
import com.sonicle.webtop.core.app.sdk.AuditReferenceDataEntry;
import com.sonicle.webtop.core.model.ProfileI18n;
import com.sonicle.webtop.core.model.Tag;
import com.sonicle.webtop.mail.bol.model.ImapQuery;
import com.sonicle.webtop.mail.ws.FlagsChangedMessage;
import com.sonicle.webtop.mail.bol.js.JsFlagsChangedMessage;
import com.sonicle.webtop.mail.ws.MessagesDeletedMessage;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import jakarta.mail.event.MailEvent;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTimeZone;


/**
 *
 * @author gbulfon
 */
public class FolderCache {
    
    public static final int SORT_BY_MSGIDX=0;
    public static final int SORT_BY_SENDER=1;
    public static final int SORT_BY_RCPT=2;
    public static final int SORT_BY_SUBJECT=3;
    public static final int SORT_BY_DATE=4;
    public static final int SORT_BY_SIZE=5;
    public static final int SORT_BY_PRIORITY=6;
    public static final int SORT_BY_STATUS=7;
    public static final int SORT_BY_FLAG=8;
    public static final int SORT_BY_SEEN=9;

	private MailManager mailManager=null;
    private boolean externalProvider=false;
    private boolean volatileInstance=false;
    
    private String foldername=null;
    private Folder folder=null;
    //private HashMap<Long, HTMLMailData> dhash=new HashMap<Long, HTMLMailData>();
	//Guarded by synchronized(dhash) at EVERY access: it was mutated under the 'this'
	//monitor (getMailData) but cleared under cacheLock (cleanup) and lock-free
	//(open/close/removeDHash) — different monitors = no exclusion at all.
	private FifoMap<Long, HTMLMailData> dhash=new FifoMap<>(100);
    private final HashMap<String, MessageSearchResult> msrs=new HashMap<>();
    private Message msgs[]=null;
    private volatile boolean modified=false;
    private volatile boolean forceRefresh=true;

    //--- Incremental message-list maintenance ------------------------------------
    //The interactive list re-runs a full server-side SORT/THREAD on every request
    //(client always sends refresh=true). On huge mailboxes (100k+) that SORT costs
    //seconds, and it is re-paid on every delete and every folder-return even when
    //nothing relevant changed. To avoid it we keep the sorted `msgs` array and:
    //  - on a self delete/move-out, SPLICE the removed UIDs out of `msgs` (removal
    //    is order-independent, so it is safe for any sort/group mode);
    //  - on a list request with the SAME plain query and no mailbox change, REUSE
    //    `msgs` instead of re-sorting;
    //  - fall back to a full SORT for additions (new mail / move-in / copy-in),
    //    sort/group/search changes, threaded mode, or any detected drift.
    //The mailbox-change detector is UIDVALIDITY/UIDNEXT/MESSAGES captured at the
    //last full SORT: UIDNEXT only advances on append/copy (catches a back-dated
    //drag-in too), MESSAGES catches net count changes, UIDVALIDITY catches a
    //mailbox reset. Any uncertainty -> full SORT. Toggle: MailServiceSettings
    //MESSAGELIST_INCREMENTAL_ENABLED (default true) restores always-resort when false.
    private long cachedUidValidity=-1;
    private long cachedUidNext=-1;
    private int cachedMessageCount=-1;
    private boolean cachedPlainQuery=false; //was the cached `msgs` built for an unfiltered list?

	//Shelf of additional cached sorted lists, keyed by sort combination, so different
	//consumers of the SHARED cache (web sessions with different sort prefs, REST's
	//fixed date-desc, plain vs search) do not clobber each other's list and re-SORT on
	//every alternation. Only PLAIN (unfiltered) lists are shelved; searches stay
	//transient as before. Bounded LRU (+1 active slot); mutations under cacheLock.
	private static final int MAX_SHELVED_LISTS=2;
	private final LinkedHashMap<String,ShelvedList> shelvedLists=new LinkedHashMap<>(4,0.75f,true);

	private static final class ShelvedList {
		Message[] msgs;
		long uidValidity;
		long uidNext;
		int messageCount;
	}

	private String currentListKey() {
		return sort_by+"|"+ascending+"|"+sort_group+"|"+groupascending+"|"+threaded;
	}

	//Park the ACTIVE cached list on the shelf (LRU-evicting beyond the cap) so a
	//different sort/search can take the active slot without losing it. No-op unless
	//the active list is a plain, drift-checkable one. Must run under cacheLock.
	private void shelveCurrentIfPlain() {
		//threaded lists are NOT shelvable: thread structure lives on the folder
		//(threadRoots) and inside the shared message items (indent/children), and
		//every thread() call - e.g. a threaded SEARCH taking the active slot -
		//rewrites both. Unshelving would restore the array but not that state,
		//resurrecting the search's threading (wrong totals/representatives).
		if (threaded || msgs==null || !cachedPlainQuery || cachedUidValidity<0) return;
		ShelvedList sh=new ShelvedList();
		sh.msgs=msgs;
		sh.uidValidity=cachedUidValidity;
		sh.uidNext=cachedUidNext;
		sh.messageCount=cachedMessageCount;
		shelvedLists.put(currentListKey(), sh);
		while (shelvedLists.size()>MAX_SHELVED_LISTS) {
			Iterator<String> it=shelvedLists.keySet().iterator();
			it.next(); it.remove(); //eldest = least recently used (access-order map)
		}
	}

	//Restore a shelved list matching the CURRENT sort fields into the active slot.
	//Returns true on success; the normal reuse checks (drift etc.) then apply to it.
	//Must run under cacheLock.
	private boolean unshelveCurrent() {
		if (threaded) return false; //see shelveCurrentIfPlain: threaded lists never shelved
		ShelvedList sh=shelvedLists.remove(currentListKey());
		if (sh==null) return false;
		msgs=sh.msgs;
		cachedUidValidity=sh.uidValidity;
		cachedUidNext=sh.uidNext;
		cachedMessageCount=sh.messageCount;
		cachedPlainQuery=true;
		return true;
	}
    private int unread=0;
	//false until 'unread' is first computed from IMAP: lets warm-count readers
	//distinguish "really 0 unread" from "scan has not reached this folder yet"
	private volatile boolean unreadInitialized=false;
    private int recent=0;
    private boolean hasUnreadChildren=false;
    private boolean unreadChanged=false;
    private boolean recentChanged=false;
    private boolean checkUnreads=true;
    private boolean checkRecents=true;
    private boolean isSharedInbox=false;
    private SharedPrincipal sharedInboxPrincipal=null;
    private boolean isInbox=false;
    private boolean isRoot=false;
    private boolean isSent=false;
    private boolean isTrash=false;
    private boolean isSpam=false;
    private boolean isDrafts=false;
    private boolean isArchive=false;
    private boolean isDms=false;
    private boolean isSharedFolder=false;
    private boolean isUnderSharedFolder=false;
    private boolean scanNeverDone=true;
    private boolean scanForcedOff=false;
    private boolean scanForcedOn=false;
    private boolean scanEnabled=false;
	private boolean useArrivalDate=false;
    private String description=null;
    private String wtuser=null;
    //Guarded by synchronized(recentNotified): hit by BOTH the MailFoldersThread sweep
    //and the idle-event queue thread on idle folders (contains+add must be atomic).
    private final ArrayList<String> recentNotified=new ArrayList<>();
    //Per-UID new flag set captured from the IDLE messageChanged listener (fires once per
    //message, BEFORE the event queue coalesces them by foldername|mchange). Drained in
    //sendFlagsChangedMessage so the 'flags' push can carry the exact UIDs+state changed,
    //letting the client patch only the visible grid rows instead of reloading the list.
    private final ConcurrentHashMap<Long,Flags> pendingFlagChanges = new ConcurrentHashMap<>();
    //UIDs expunged by another session/user, captured from the IDLE messagesRemoved
    //listener (fires per message, BEFORE the event queue coalesces them by
    //foldername|mremove). Drained in MessagesRemovedHandler into one 'mdel' push so the
    //client can remove just those visible rows. removedNeedsRefresh is set when a UID
    //could not be resolved (expunged before it was ever fetched) -> push null uids so
    //the client falls back to a full grid refresh.
    private final Set<Long> pendingRemovedUids = Collections.synchronizedSet(new LinkedHashSet<Long>());
    private volatile boolean removedNeedsRefresh=false;

    private int sort_by=0;
    private boolean ascending=true;
    private int sort_group=0;
    private boolean groupascending=true;
	private boolean threaded=false;
    //private MessageComparator comparator;

    private FolderCache parent=null;
    //volatile + copy-on-write (addChild/removeChild replace the list): mutated rarely
    //(tree build, folder destroy) but iterated constantly by the MFT sweep, idle
    //handlers and request threads — in-place mutation under iteration CMEs and can
    //kill the MailFoldersThread. Iterators see an immutable snapshot.
    private volatile ArrayList<FolderCache> children=null;
	private volatile HashMap<String,FolderCache> childrenMap=null;

	//Guarded by synchronized(openThreads): mutated by setThreadOpen (request threads,
	//formerly under 'this') AND rebuilt by _getThreadedMessages (under cacheLock) —
	//different monitors gave no exclusion.
	private final HashMap<Long,Integer> openThreads=new HashMap<>();
	private volatile int totalOpenThreadChildren=0;
    
    private boolean startupLeaf=true;

	private CalendarBuilder calbuilder=new CalendarBuilder();
	
    static final Flags seenFlags=new Flags(Flag.SEEN);
    static final Flags recentFlags=new Flags(Flag.RECENT);
    static final Flags repliedFlags=new Flags(Flag.ANSWERED);
    static final Flags forwardedFlags=new Flags("$Forwarded");
    static final FlagTerm unseenSearchTerm=new FlagTerm(seenFlags,false);
    static final FlagTerm seenSearchTerm=new FlagTerm(seenFlags,true);
    static final FlagTerm recentSearchTerm=new FlagTerm(recentFlags,true);
    static final FlagTerm repliedSearchTerm=new FlagTerm(repliedFlags,true);
    static final FlagTerm forwardedSearchTerm=new FlagTerm(forwardedFlags,true);
	
	private MailAccount account=null;

	//Serializes mutation/read of this folder's cache state (msgs, dhash, sort fields,
	//unread/recent counts) so the interactive message-list path and the periodic
	//MailFoldersThread check can't interleave on the same FolderCache. Reentrant via
	//the object monitor: getMessages -> refresh -> cleanup all re-acquire this lock.
	private final Object cacheLock=new Object();

    //private static final HashMap<String,HashMap<String,Integer>> months=new HashMap<>();

    //ConcurrentHashMap: put/get from web AND REST threads; the purge iterates values()
    //and removes inline, which on a plain HashMap CMEs even single-threaded.
    private final ConcurrentHashMap<String,MessageEntry> providedMessages=new ConcurrentHashMap<>();
    private MessageChangedHandler messageChangedHandler = new MessageChangedHandler();
	private MessagesAddedHandler messagesAddedHandler = new MessagesAddedHandler();
	private MessageCountHandler messageCountHandler = new MessageCountHandler();
	private MessagesRemovedHandler messagesRemovedHandler = new MessagesRemovedHandler();

	private static FetchProfile FP_BS = new FetchProfile();

	//Used by refreshRecentMessagesCount to batch-load what its loop reads:
	//ENVELOPE for from/subject, plus the Message-ID header (ENVELOPE alone does
	//not populate MimeMessage header reads).
	private static FetchProfile FP_RECENT = new FetchProfile();

    static {
		FP_BS.add(FetchProfile.Item.CONTENT_INFO);
		FP_RECENT.add(FetchProfile.Item.ENVELOPE);
		FP_RECENT.add("Message-ID");
    }

	//private static void addHashMonths(String language, String vmonths[]) {
	//	HashMap<String,Integer> hmonths=new HashMap<>();
	//	for (int m=0; m<12;++m) {
	//		hmonths.put(vmonths[m],m+1);
	//	}
	//	months.put(language,hmonths);
	//}

    //for externally provided messages
    class MessageEntry {
        String key;
        String provider;
        String providerid;
        long timestamp;
        Message msg;
        
        MessageEntry(String provider, String providerid, long timestamp, Message msg) {
            this.key=provider+","+providerid;
            this.provider=provider;
            this.providerid=providerid;
            this.timestamp=timestamp;
            this.msg=msg;
        }
    }

    //Special constructor for externally provided messages
    public FolderCache(MailManager mailManager) {
        externalProvider=true;
		account=null;
		this.mailManager=mailManager;
    }

    public FolderCache(MailAccount account, Folder folder, MailManager mailManager) throws MessagingException {
		this(account, folder, mailManager, false);
	}

	//volatileInstance = throwaway cache NOT registered in the account's foldersCache
	//map (e.g. rendering a favorites-tree node whose cache isn't loaded). It must
	//never own machinery: an idle thread started here would be orphaned at teardown
	//(account.cleanup only stops threads of registered caches) and leak per call.
	public FolderCache(MailAccount account, Folder folder, MailManager mailManager, boolean volatileInstance) throws MessagingException {
        this(mailManager);
		this.volatileInstance=volatileInstance;
		this.account=account;
        foldername=folder.getFullName();
		MailUserSettings mailUserSettings = mailManager.getMailUserSettings();
        this.folder=folder;
        String shortfoldername=account.getShortFolderName(foldername);
        isInbox=account.isInboxFolder(foldername);
        isSent=account.isSentFolder(shortfoldername);
        isDrafts=account.isDraftsFolder(shortfoldername);
        isTrash=account.isTrashFolder(shortfoldername);
        isSpam=account.isSpamFolder(shortfoldername);
        isArchive=account.isArchiveFolder(shortfoldername);
        isDms=mailManager.isDmsFolder(account,shortfoldername);
        isSharedFolder=account.isSharedFolder(foldername);
        /*if (isDrafts||isSent||isTrash||isSpam||isArchive) {
            setCheckUnreads(false);
            setCheckRecents(false);
        }*/

        isSharedInbox=false;
		if (account.hasDifferentDefaultFolder() && account.isDefaultFolder(foldername)) {
			
		}
		else if (account.isUnderSharedFolder(foldername)) {
			isUnderSharedFolder=true;
			char sep=account.getFolderSeparator();
            int ix=foldername.indexOf(sep);
            String subname=foldername.substring(ix+1);
			int isep=subname.indexOf(sep);
            if (isep<0) {
                isSharedInbox=true;
                sharedInboxPrincipal=mailManager.getSharedPrincipal(mailManager.getTargetProfileId().getDomainId(),subname);
				//Cyrus has shared/user = inbox
				//Dovecot has shared/user no messages, then INBOX under
				if ((folder.getType()&IMAPFolder.HOLDS_MESSAGES)==0) {
					isSharedInbox=false;
					isSharedFolder=true;
				}
            } else { //look for a possible INBOX under a shared folder
				FolderCache fcparent=account.getFolderCache(folder.getParent().getFullName());
				if (fcparent!=null) {
					String fname=folder.getName();
					if (fcparent.isSharedFolder && fname.equals("INBOX"))
						isSharedInbox=true;
				}
			}
        }
        if (sharedInboxPrincipal==null) description=mailManager.getInternationalFolderName(this);
        else {
			String changedName = mailUserSettings.getSharedFolderName(foldername);
			if(changedName == null)
				 description = sharedInboxPrincipal.getDisplayName();
			else
				description = changedName;
           
            wtuser=sharedInboxPrincipal.getUserId();
        }
        updateScanFlags();
		
		MailUserSettings mus=mailManager.getMailUserSettings();
		useArrivalDate = mus.isUseArrivalDate(foldername);
		
		boolean idle = !volatileInstance && (isInbox
			|| (isSharedInbox && mailManager.getMailServiceSettings().isIdleSharedInboxFolderEnabled())
			|| (account.isFavoriteFolder(foldername) && mailManager.getMailServiceSettings().isIdleFavoriteFolderEnabled()));

		if (idle) startIdle();
		
		//check recents only in important folders (idle mode ones)
		setCheckRecents(idle);
    }
	
	boolean goidle=true;
	private IdleThread idleThread=null;
	//Step B: idle runs on a DEDICATED connection (own store/session/tracker) from the
	//account's Mailbox, so it no longer occupies a pooled interactive connection and
	//teardown can hard-close it without touching the interactive store. Null = legacy
	//fallback (idle on the pooled folder) when the dedicated open failed.
	private volatile Mailbox.DedicatedFolder dedicatedIdleFolder=null;
	//Idle delivers only CHANGE events, never the initial state, so the periodic
	//MailFoldersThread sweep still polls idle folders too (otherwise their pre-existing
	//unread counts would never appear until the next change). Idle just adds instant push
	//(recent/grid-refresh) on top of that polling.
	class IdleThread extends Thread {
		IdleThread() {
			super("IDLE-"+mailManager.getTargetProfileId()+":"+account.getId()+"-"+foldername);
		}
		@Override
		public void run() {
			//MailService.logger.debug("Starting idle thread");
			long backoff=0;
			while(goidle) {
				try {
					Mailbox.DedicatedFolder dedicated=dedicatedIdleFolder;
					IMAPFolder ifolder;
					if (dedicated!=null) {
						if (dedicated.isClosed()) break;
						dedicated.ensureOpen();
						ifolder=dedicated.getFolder();
					} else {
						ifolder=((IMAPFolder)FolderCache.this.getFolder());
						if (!ifolder.isOpen()) ifolder.open(Folder.READ_WRITE);
					}
					backoff=0;
					//Service.logger.debug("Entering idle mode on {}",foldername);
					ifolder.idle();
					//Service.logger.debug("Exiting idle mode on {}",foldername);
				} catch(Throwable exc) {
					//Idle dropped (connection error, server idle timeout, server restart, or
					//folder closed on shutdown). The OLD code exited the loop here, killing idle
					//permanently so instant push stopped until logout. Instead back off and try
					//to re-establish idle. On shutdown goidle is already false -> exit.
					if (!goidle) break;
					Mailbox.DedicatedFolder dedicated=dedicatedIdleFolder;
					if (dedicated!=null && dedicated.isClosed()) break;
					backoff = (backoff==0) ? 5000 : Math.min(backoff*2, 60000);
					Service.logger.debug("Idle interrupted on {}, retrying in {}ms",foldername,backoff,exc);
					try { Thread.sleep(backoff); } catch(InterruptedException ie) { if (!goidle) break; }
				}
			}
			Service.logger.debug("Exiting idle loop on {}: goidle={}",foldername,goidle);
		}
	}
	
	public boolean hasActiveIdle() {
		IdleThread it=idleThread;
		return it!=null && it.isAlive();
	}

	public void startIdle() {
		//Listeners must attach to the folder instance the idling CONNECTION owns:
		//untagged updates arrive there, not on the pooled interactive folder.
		IMAPFolder target=(IMAPFolder)folder;
		try {
			dedicatedIdleFolder=account.openDedicatedIdleFolder(foldername);
			target=dedicatedIdleFolder.getFolder();
		} catch(Exception exc) {
			Service.logger.warn("Cannot open dedicated idle connection on {}, falling back to pooled-store idle", foldername, exc);
			dedicatedIdleFolder=null;
		}
		target.addMessageChangedListener(
			new MessageChangedListener() {

				@Override
				public void messageChanged(MessageChangedEvent mce) {
					if (mce.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED) {
						//capture uid+new flags here (one event per message) before the queue
						//coalesces; the coalesced handler drains the accumulated set into one push
						try {
							Message cm=mce.getMessage();
							long uid=((SonicleIMAPMessage)cm).getUID();
							if (uid<0) {
								//Some IMAP servers omit UID in the untagged FETCH response that fires
								//FLAGS_CHANGED, so IMAPMessage's cached UID stays at -1. Resolve it
								//explicitly via IMAPFolder.getUID(msg), which issues a UID FETCH.
								Folder mf = cm.getFolder();
								if (mf instanceof IMAPFolder) uid = ((IMAPFolder)mf).getUID(cm);
							}
							if (uid>=0) {
								Flags flags = cm.getFlags();
								pendingFlagChanges.put(uid, flags);
								if (sort_by == SORT_BY_SEEN)
									forceRefresh = true;
							} else {
								Service.logger.warn("FLAGS_CHANGED on {} skipped: uid unresolved even after IMAPFolder.getUID()", foldername);
							}
						} catch(Exception exc) {
							//fall back: handler still sends folder-level signal, but with items=null
							//the mobile push carries no per-UID delta and the client refreshes the grid
							Service.logger.warn("FLAGS_CHANGED on {} could not capture uid/flags: {}", foldername, exc.toString());
						}
						account.queueFolderMailEvent(foldername + "|mchange", mce, messageChangedHandler);
					}
				}

			}
		);
		target.addMessageCountListener(
			new MessageCountListener() {

				@Override
				public void messagesAdded(MessageCountEvent mce) {
					account.queueFolderMailEvent(foldername + "|mcount", mce, messageCountHandler);
					account.queueFolderMailEvent(foldername + "|madd" , mce, messagesAddedHandler);
				}

				@Override
				public void messagesRemoved(MessageCountEvent mce) {
					//capture the expunged UIDs here (one event per batch) before the queue
					//coalesces by foldername|mremove; the coalesced handler drains them into
					//one 'mdel' push. A UID unresolvable on an already-expunged message flags
					//a refresh fallback for the whole push.
					for (Message m : mce.getMessages()) {
						try {
							long uid=((SonicleIMAPMessage)m).getUID();
							if (uid>=0) pendingRemovedUids.add(uid);
							else removedNeedsRefresh=true;
						} catch(Exception exc) { removedNeedsRefresh=true; }
					}
					account.queueFolderMailEvent(foldername + "|mcount", mce, messageCountHandler);
					account.queueFolderMailEvent(foldername + "|mremove", mce, messagesRemovedHandler);
				}
			}
		);
		idleThread=new IdleThread();
		idleThread.start();
	}
	
	protected boolean isSharedToSomeone() throws MessagingException, WTException {
		if (isSharedFolder||isSharedInbox||isUnderSharedFolder) return false;
		
		boolean retval=false;
		for(ACL acl : ((IMAPFolder)folder).getACL()) {
			String aclUserId=acl.getName();
			UserProfileId pid=mailManager.aclUserIdToUserId(aclUserId);
			if (pid==null) continue;
			CoreManager core=WT.getCoreManager();
			String roleUid=core.lookupUserSid(pid);
			if (roleUid==null) { 
				if (!RunContext.isPermitted(true, mailManager.SERVICE_ID, "SHARING_UNKNOWN_ROLES","SHOW")) continue;
			}
			retval=true;
			break;
		}
		return retval;
	}
    
    protected void setStartupLeaf(boolean b) {
        startupLeaf=b;
    }
    
    protected boolean isStartupLeaf() {
        return startupLeaf;
    }

    public void updateScanFlags() {
        String sfname=account.getShortFolderName(foldername);
        if (isInbox || isSharedInbox) {
            setScanForcedOn(true);
            setScanForcedOff(false);
        }
        else if (account.isSpecialFolder(sfname)) {
            setScanForcedOn(true);
			setScanForcedOff(false);
        }
        else {
            setScanForcedOn(mailManager.checkFileRules(foldername));
            setScanForcedOff(false); 
        }
        setScanEnabled(mailManager.checkScanRules(foldername));
    }
	
	public MailAccount getAccount() {
		return account;
	}

    public void setIsRoot(boolean b) {
        this.isRoot=b;
    }

    public String getFolderName() {
        return foldername;
    }
    
    public Folder getFolder() {
        return folder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String s) {
        description=s;
    }

    public String getWebTopUser() {
        return wtuser;
    }

    public void setWebTopUser(String s) {
        wtuser=s;
    }

    public boolean isRoot() {
        return isRoot;
    }
    
    public boolean isInbox() {
        return isInbox;
    }

    public boolean isSent() {
        return isSent;
    }

    public boolean isDrafts() {
        return isDrafts;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public boolean isDms() {
        return isDms;
    }

    public boolean isSpecial() {
        return isSent||isDrafts||isTrash||isSpam||isArchive||isDms;
    }

    public boolean isSharedFolder() {
        return isSharedFolder;
    }
	
	public boolean isUnderSharedFolder() {
		return isUnderSharedFolder;
	}

    public boolean isSharedInbox() {
        return isSharedInbox;
    }

    public SharedPrincipal getSharedInboxPrincipal() {
		SharedPrincipal sp=sharedInboxPrincipal;
		FolderCache fc=this;
		while(sp==null) {
			fc=fc.getParent();
			if (fc==null) break;
			sp=fc.getSharedInboxPrincipal();
		}
        return sp;
    }
	
	public void setUseArrivalDate(boolean b) {
		//the sort field stays nominally "date" so getMessages' sortchanged check won't
		//catch this, but the SORT term flips received<->sent (getSortTerm) and the
		//ordering changes: force a full re-SORT so the next refresh doesn't reuse the
		//stale cached order via the incremental path
		if (useArrivalDate != b) setForceRefresh();
		useArrivalDate = b;
	}
	
	public boolean isUseArrivalDate() {
		return useArrivalDate;
	}
	
    public void setScanForcedOff(boolean b) {
        scanForcedOff=b;
    }

    public boolean isScanForcedOff() {
        return scanForcedOff;
    }
    
    public void setScanForcedOn(boolean b) {
        scanForcedOn=b;
    }

    public boolean isScanForcedOn() {
        return scanForcedOn;
    }

    public void setScanEnabled(boolean b) {
        scanEnabled=b;
		/*if (b) */updateUnreads();
		/*else {
			sendClearUnreadChangedMessage();
			if (parent!=null) parent.updateUnreads();
		}*/
    }

    public boolean isScanEnabled() {
        return scanEnabled;
    }
	
	public boolean isScanForcedOrEnabled() {
		return (!isScanForcedOff() && isScanEnabled())||isScanForcedOn();
	}
	
	public boolean hasChildWithScanForcedOrEnabled() {
		boolean retval=false;

		ArrayList<FolderCache> snapshot=children;
		if (snapshot!=null) {
			//look for a possible direct child with scan enabled
			for (FolderCache child: snapshot) {
				retval=child.isScanForcedOrEnabled();
				if (retval) break;
			}
			if (!retval) {
				//look in subchildren
				for (FolderCache child: snapshot) {
					retval=child.hasChildWithScanForcedOrEnabled();
					if (retval) break;
				}
			}
		}
		
		return retval;
	}	

    public void setCheckUnreads(boolean b) {
        this.checkUnreads=b;
    }
    
    public void setCheckRecents(boolean b) {
        this.checkRecents=b;
    }

    public boolean isCheckUnreads() {
        return this.checkUnreads;
    }
    
    public boolean isCheckRecents() {
        return this.checkRecents;
    }

    public boolean unreadChanged() {
        return unreadChanged;
    }
    
    public boolean recentChanged() {
        return recentChanged;
    }

    public void resetUnreadChanged() {
        unreadChanged=false;
    }
    
    public void resetRecentChanged() {
        recentChanged=false;
    }

    public boolean hasUnreadChildren() {
        return hasUnreadChildren;
    }
    
    protected void setHasUnreadChildren(boolean b) {
        boolean oldhuc=hasUnreadChildren;
        hasUnreadChildren=b;
        if (oldhuc!=b) {
			unreadChanged=true;
			sendUnreadChangedMessage();
		}
    }
    
    public int getUnreadMessagesCount() {
        return unread;
    }

    public boolean isUnreadCountInitialized() {
        return unreadInitialized;
    }
    
    public int getRecentMessagesCount() {
        return recent;
    }
    
    private void purgeProvidedEntries() {
        long maxmillis=System.currentTimeMillis()-(1000*60*5); //five minute max holding
        for(MessageEntry me: providedMessages.values()) {
            if (me.timestamp<=maxmillis) providedMessages.remove(me.key);
        }
    }
    
    public void addProvidedMessage(String provider, String providerid, Message msg) {
        purgeProvidedEntries();
        MessageEntry me=new MessageEntry(provider,providerid,System.currentTimeMillis(),msg);
        providedMessages.put(me.key, me);
    }
    
    public Message getProvidedMessage(String provider, String providerid) {
        Message msg=null;
        MessageEntry me=providedMessages.get(provider+","+providerid);
        if (me!=null) msg=me.msg;
        return msg;
    }    
	
	private void sendUnreadChangedMessage() {
		//NO MORE send ws message only if it's not special or has "scan forced on" active
		//if (/*!isSpecial() || */ isScanForcedOrEnabled())
			mailManager.dispatchMailEvent(account.getId(), foldername, MailEventType.UNREAD,
				new UnreadChangedMessage(account.getId(),foldername, unread, hasUnreadChildren)
			);
	}

	private void sendFlagsChangedMessage() {
		//Drain the per-UID flag changes accumulated since the last push into one message.
		//Each item carries only flag-derived state (no IO, no date/invitation parsing): the
		//client patches the matching visible rows (seen->read/unread transition + flag/note
		//+ tag membership) exactly as its own local toggle does. If the set is empty (e.g.
		//an expunge-driven change), items stays null and the client falls back to a grid
		//refresh.
		ArrayList<JsFlagsChangedMessage.Item> items=null;
		if (mailManager!=null && !pendingFlagChanges.isEmpty()) {
			//Resolve tag definitions once for the whole drain: the map is used to translate
			//IMAP-keyword-encoded tag flags on each Item into user-facing tag IDs. Fall back
			//to an empty map if the CoreManager lookup errors — the client just sees empty
			//tag lists per item, same as if no tags were applied.
			java.util.Map<String, com.sonicle.webtop.core.model.Tag> tagMap;
			try {
				tagMap = WT.getCoreManager().listTags();
			} catch (Exception exc) {
				Service.logger.warn("could not list tags for FLAGS push on {}: {}", foldername, exc.toString());
				tagMap = java.util.Collections.emptyMap();
			}
			items=new ArrayList<>();
			for (Iterator<Map.Entry<Long,Flags>> it=pendingFlagChanges.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Long,Flags> e=it.next();
				it.remove();
				Flags fl=e.getValue();
				if (fl==null) continue;
				items.add(new JsFlagsChangedMessage.Item(
					e.getKey(),
					fl.contains(Flags.Flag.SEEN),
					fl.contains(Flags.Flag.ANSWERED),
					fl.contains("$Forwarded"),
					mailManager.getFlagString(fl),
					mailManager.hasNote(fl),
					mailManager.flagsToTagsIds(fl, tagMap)
				));
			}
			if (items.isEmpty()) items=null;
		}
		mailManager.dispatchMailEvent(account.getId(), foldername, MailEventType.FLAGS,
			new FlagsChangedMessage(account.getId(),foldername,items)
		);
	}

	//Drain the UIDs expunged by another session/user into one 'mdel' push. Splice them
	//from the cached sorted list (when eligible) so a client-side reload does not re-SORT.
	//If any UID could not be resolved, push null uids -> the client does a full refresh.
	private void sendMessagesDeletedMessage() {
		ArrayList<Long> uids=null;
		boolean needsRefresh=removedNeedsRefresh;
		removedNeedsRefresh=false;
		synchronized(pendingRemovedUids) {
			if (!pendingRemovedUids.isEmpty()) {
				uids=new ArrayList<>(pendingRemovedUids);
				pendingRemovedUids.clear();
			}
		}
		if (!needsRefresh && uids!=null && incrementalListEnabled() && !threaded && msgs!=null && cachedPlainQuery) {
			synchronized(cacheLock) { spliceFromCache(uids); }
		}
		//null uids tells the client to fall back to a full grid refresh
		mailManager.dispatchMailEvent(account.getId(), foldername, MailEventType.MDEL,
			new MessagesDeletedMessage(account.getId(), foldername, needsRefresh ? null : uids)
		);
	}

	private void sendClearUnreadChangedMessage() {
		mailManager.dispatchMailEvent(account.getId(), foldername, MailEventType.UNREAD,
			new UnreadChangedMessage(account.getId(),foldername, 0, false)
		);
	}
	
	private void sendRecentMessage(String from, String subject) {
		mailManager.dispatchMailEvent(account.getId(), foldername, MailEventType.RECENT,
			new RecentMessage(account.getId(),foldername, from, subject, account.isFavoriteFolder(foldername))
		);
	}
	
    protected void refreshUnreads() throws MessagingException {
		refreshUnreadMessagesCount();
		updateUnreads();
    }

    protected void refreshUnreadMessagesCount() throws MessagingException {
        if ((folder.getType() & Folder.HOLDS_MESSAGES)>0) {
            int oldunread=unread;
            /*if (folder.isOpen()) {
                Message umsgs[]=folder.search(unseenSearchTerm);
                unread=umsgs.length;
            } else */unread=folder.getUnreadMessageCount();
			unreadInitialized=true;
			//Service.logger.debug("refreshing count on "+foldername+" oldunread="+oldunread+", unread="+unread);
            if (oldunread!=unread) {
				unreadChanged=true;
				sendUnreadChangedMessage();
				if (parent!=null) parent.updateUnreads();
			}
        }
    }

    private java.util.Calendar cal=java.util.Calendar.getInstance();
    protected void refreshRecentMessagesCount() throws MessagingException {
        if (folder.exists() && (folder.getType() & Folder.HOLDS_MESSAGES)>0) {
            int oldrecent=recent;
            boolean wasOpen=folder.isOpen();
            if (!wasOpen) folder.open(Folder.READ_ONLY);
            cal.setTime(new java.util.Date());
            cal.add(java.util.Calendar.HOUR, -24);
            ReceivedDateTerm dterm=new ReceivedDateTerm(ComparisonTerm.GT, cal.getTime());
            FlagTerm sterm=new FlagTerm(seenFlags, false);
            AndTerm term=new AndTerm(new SearchTerm[] {sterm, dterm});
            Message umsgs[]=folder.search(term);
            //Message umsgs[]=folder.search(recentSearchTerm);
            //Batch-prefetch what the loop reads (Message-ID/from/subject) in ONE round trip:
            //without this each getMessageID/getFrom/getSubject below lazily fetches per
            //message, serializing N round trips on the same connection the interactive
            //list uses. ENVELOPE covers from/subject; Message-ID is read as a header.
            if (umsgs.length>0) folder.fetch(umsgs, FP_RECENT);
            recent=0;
            Message recentMsg=null;
            for(Message m: umsgs) {
                IMAPMessage im=(IMAPMessage)m;
                String id=im.getMessageID();
//                if (isInbox) {
//                    ++recent;
//                } else {
                    boolean fresh;
                    synchronized(recentNotified) {
                        fresh=!recentNotified.contains(id);
                        if (fresh) recentNotified.add(id);
                    }
                    if (fresh) {
                        ++recent;
                        recentMsg=m;
                    }
//                }
            }
            //Send ONE recent push per sweep (the latest new message), never one per
            //message, or the client goes crazy when many messages arrive at once.
            //Same rule as the idle-path batch handling in MessagesAddedHandler.
            if (recentMsg!=null) {
                String fromName="";
                Address as[]=recentMsg.getFrom();
                if (as!=null && as.length>0) {
                    InternetAddress ia = (InternetAddress) as[0];
                    fromName = ia.getPersonal();
                    String fromEmail = mailManager.adjustEmail(ia.getAddress());
                    if (fromName == null) {
                        fromName = fromEmail;
                    } else {
                        fromName = fromName+" <"+fromEmail+">";
                    }
                }
                sendRecentMessage(fromName,recentMsg.getSubject());
            }
            if (!wasOpen) folder.close(false);
            //if (!(oldrecent==0 && recent==0)) recentChanged=true;
            if (recent>0) {
				recentChanged=true;
			}
        }
    }

    protected boolean checkSubfolders(boolean all, MailFoldersThread mft) throws MessagingException {
        boolean pHasUnread=false;
        for(FolderCache fcchild: getChildren()) {
			if (mft.isAborted()) break;
            if (fcchild.isScanForcedOff()) continue;
            boolean hasUnread=false;
            if (all || fcchild.scanNeverDone || fcchild.isScanForcedOn() || fcchild.isScanEnabled()) {
                fcchild.scanNeverDone=false;
                hasUnread=fcchild.checkFolder();
            } else {
				hasUnread=fcchild.getUnreadMessagesCount()>0||fcchild.hasUnreadChildren;
			}
            if (fcchild.children!=null) { //volatile snapshot; recursion re-reads safely
                hasUnread|=fcchild.checkSubfolders(all,mft);
            }
            //fcchild.setHasUnreadChildren(hasUnread);
            pHasUnread|=hasUnread;
        }
        return pHasUnread;
    }
    
    protected boolean checkFolder() {
        //Deliberately NOT synchronized on cacheLock. This only refreshes count state
        //(unread/recent/recentNotified) via IMAP STATUS/SEARCH, which is DISJOINT from the
        //message-list cache (msgs/dhash/sort fields) that cacheLock guards. checkFolder is called
        //only by the single MailFoldersThread, and the idle handlers already refresh these same
        //counts lock-free - so this lock never protected anything here. It only stalled the
        //interactive list for the whole STATUS+SEARCH round-trip whenever the sweep happened to
        //hit the folder being viewed (the residual 5-10s list hang).
        try {
            if (checkUnreads || scanForcedOn || scanEnabled) refreshUnreadMessagesCount();
        } catch(MessagingException exc) {
            Service.logger.debug("Exception on folder "+foldername,exc);
        }
        try {
            if (checkRecents /*|| scanForcedOn || scanEnabled*/) refreshRecentMessagesCount();
        } catch(MessagingException exc) {
            Service.logger.debug("Exception on folder "+foldername,exc);
        }
        return (unread>0);
    }

    protected void updateUnreads() {
		updateUnreadChildren();
		FolderCache fcparent=parent;
		while(fcparent!=null && !fcparent.isRoot()) {
			fcparent.updateUnreads();
			fcparent=fcparent.parent;
		}
    }
	
	protected boolean updateUnreadChildren() {
		boolean oldHasUnreadChildren=hasUnreadChildren;
		hasUnreadChildren=false;
		ArrayList<FolderCache> snapshot=children;
		if (snapshot!=null) {
			for(FolderCache child: snapshot) {
				hasUnreadChildren|=(child.unread>0 || child.hasUnreadChildren);
			}
		}
		if (hasUnreadChildren!=oldHasUnreadChildren) {
			sendUnreadChangedMessage();
			return true;
		}
		return false;
	}
    
    public boolean toBeRefreshed() {
        return forceRefresh;
    }
    
    public void setForceRefresh() {
        this.forceRefresh=true;
        //something changed that the incremental logic could not track (untracked
        //additions, arrival-date toggle changing the date-sort meaning, pool
        //eviction): every shelved list is stale by the same token
        synchronized(cacheLock) { shelvedLists.clear(); }
    }
    
    public void refresh(ImapQuery iq) throws MessagingException, IOException {
        boolean dbg=(mailManager!=null && mailManager.isListDebugEnabled());
        long t0=System.nanoTime(), tprev=t0;
        synchronized(cacheLock) {
            //delta here = time spent BLOCKED on cacheLock (contention with another
            //refresh/getMessages on the same folder); a big value means lock contention,
            //not slow IMAP.
            tprev=Service.listMark(dbg,"refresh "+foldername,"acquired cacheLock",t0,tprev);
            cleanup(false);
            if (!threaded)
                msgs=_getMessages("", "",sort_by,ascending,sort_group,groupascending, iq);
            else
                msgs=_getThreadedMessages("", "", iq);
            tprev=Service.listMark(dbg,"refresh "+foldername,(threaded?"_getThreadedMessages (THREAD+fetch)":"_getMessages (SORT)"),t0,tprev);

            if (iq.hasNotePattern()) {

            }

            open();
            //add(msgs);
            modified=false;
            forceRefresh=false;
            captureMailboxState(iq);
        }
    }

    //Snapshot the mailbox identity used to decide later whether `msgs` can be
    //reused without a re-SORT. Called under cacheLock right after a full SORT.
    private void captureMailboxState(ImapQuery iq) {
        try {
            cachedUidValidity=((IMAPFolder)folder).getUIDValidity();
            cachedUidNext=((IMAPFolder)folder).getUIDNext();
            cachedMessageCount=folder.getMessageCount();
        } catch(Exception exc) {
            //unknown -> force a resort next time
            cachedUidValidity=-1; cachedUidNext=-1; cachedMessageCount=-1;
        }
        cachedPlainQuery=isPlainQuery(iq);
    }

    //Returns the toggle cached on MailManager (read once from MailServiceSettings at
    //service init, like attachmentDetectUseBodyStructure). A setting change therefore
    //takes effect on next login. Defaults to legacy (always resort) if unavailable.
    private boolean incrementalListEnabled() {
        try {
            return mailManager!=null && mailManager.isMessageListIncrementalEnabled();
        } catch(Exception exc) {
            return false;
        }
    }

    //An unfiltered list: no search term and no note/attachment filter. Only these
    //are eligible for reuse/splice; any active search always does a full SORT.
    private boolean isPlainQuery(ImapQuery iq) {
        return iq!=null && iq.getSearchTerm()==null
            && !iq.hasNotePattern() && !iq.hasAttachment() && !iq.hasAttachmentName();
    }

    //True if the mailbox changed since the last full SORT (append/copy advanced
    //UIDNEXT, net count differs, or the mailbox was reset). Errs on the safe side:
    //any failure to read the state returns true -> caller does a full SORT.
    private boolean mailboxChangedSinceSort() {
        if (cachedUidValidity<0 || cachedUidNext<0 || cachedMessageCount<0) return true;
        if (!folder.isOpen()) return true; //can't cheaply verify -> resort (also reopens)
        try {
            //getMessageCount() is the live cached EXISTS on an open folder (no round
            //trip, kept current by the IDLE listeners): check it first.
            if (folder.getMessageCount()!=cachedMessageCount) return true;
            //Equal count can still hide an add+expunge that nets to zero; UIDNEXT only
            //ever advances on append/copy, so it catches that (and a back-dated drag-in).
            if (((IMAPFolder)folder).getUIDNext()!=cachedUidNext) return true;
            if (((IMAPFolder)folder).getUIDValidity()!=cachedUidValidity) return true;
            return false;
        } catch(Exception exc) {
            return true;
        }
    }

    //Remove the given UIDs from the cached sorted `msgs` in place. Removal preserves
    //the existing order and grouping, so this is valid for every (non-threaded) sort
    //mode. cachedMessageCount is decremented to stay in sync with the folder so the
    //drift check above does not then falsely fire. Must run under cacheLock.
    private void spliceFromCache(Collection<Long> uids) {
        //Authoritative eligibility re-check (we hold cacheLock): callers pre-check
        //threaded/cachedPlainQuery OUTSIDE the lock as a fast path, but another
        //thread may have switched the active slot to a search/threaded list since —
        //splicing that would silently drop rows from an unrelated view. Marking
        //forceRefresh keeps freshness; stale shelves self-heal via the drift check.
        if (threaded || !cachedPlainQuery) { forceRefresh=true; return; }
        if (msgs==null || uids==null || uids.isEmpty()) return;
        HashSet<Long> rm=new HashSet<>(uids);
        ArrayList<Message> kept=new ArrayList<>(msgs.length);
        for (Message m: msgs) {
            long u=-1;
            try { u=((SonicleIMAPMessage)m).getUID(); } catch(Exception exc) { /* keep if unknown */ }
            if (u<0 || !rm.contains(u)) kept.add(m);
        }
        int removed=msgs.length-kept.size();
        msgs=kept.toArray(new Message[kept.size()]);
        if (cachedMessageCount>=0) cachedMessageCount-=removed;
        //keep the shelved lists consistent too: removal preserves order for every
        //non-threaded sort (only plain non-threaded lists are ever shelved)
        for (ShelvedList sh: shelvedLists.values()) {
            if (sh.msgs==null) continue;
            ArrayList<Message> skept=new ArrayList<>(sh.msgs.length);
            for (Message m: sh.msgs) {
                long u=-1;
                try { u=((SonicleIMAPMessage)m).getUID(); } catch(Exception exc) { /* keep if unknown */ }
                if (u<0 || !rm.contains(u)) skept.add(m);
            }
            int sremoved=sh.msgs.length-skept.size();
            sh.msgs=skept.toArray(new Message[skept.size()]);
            if (sh.messageCount>=0) sh.messageCount-=sremoved;
        }
    }

    //Source-side handling of a move-out: it is a pure removal, so splice the moved
    //UIDs from the cached sorted list when eligible, otherwise force a resort.
    private void spliceMovedOut(long uids[]) {
        if (incrementalListEnabled() && !threaded && msgs!=null && cachedPlainQuery && uids!=null && uids.length>0) {
            ArrayList<Long> ul=new ArrayList<>(uids.length);
            for (long u: uids) ul.add(u);
            synchronized(cacheLock) { spliceFromCache(ul); }
        } else {
            setForceRefresh();
        }
    }
    
/*    public void add(MimeMessage m) throws MessagingException {
        try {
            String id=null;
            //m.getMessageID().trim();
            String xids[];
            xids=m.getHeader("Message-ID");
            if (xids!=null && xids.length>0) {
                id=xids[0];
                list.add(m);
                hash.put(id, m);
                if (!m.isSet(Flags.Flag.SEEN)) {
                    ++unread;
                    updateUnreads();
                }
                modified=true;
            } else {
                Service.logger.debug("Message with no id from "+m.getFrom()[0]);
            }
        } catch(MessageRemovedException exc1) {
        } catch(MessagingException exc2) {
            Service.logger.error("Exception",exc2);
        }
    }*/
    
/*    public void add(Message messages[]) throws MessagingException {
        for(Message m: messages) add((MimeMessage)m);
    }
    
    public void remove(String id) throws MessagingException {
        synchronized(dhash) { dhash.remove(id); }
        Message m=hash.remove(id);
        if (m!=null) {
            list.remove(m);
            if (!m.isSet(Flags.Flag.SEEN)) {
                --unread;
                updateUnreads();
            }
            modified=true;
        }
    }
    
    public void remove(String ids[]) throws MessagingException {
        for(String id: ids) remove(id);
    }*/

    public void removeDHash(long uids[]) {
        synchronized(dhash) {
            for(long uid: uids) {
                dhash.remove(new Long(uid));
             }
        }
     }
	
	public long getUID(Message m) throws MessagingException {
		boolean wasOpen=folder.isOpen();
		if (!wasOpen) folder.open(Folder.READ_ONLY);
		long uid = ((UIDFolder)folder).getUID(m);
		if (!wasOpen) folder.close(false);
		return uid;
	}
    
    public Message getMessage(long uid) throws MessagingException {
        open();
		Message m = ((UIDFolder)folder).getMessageByUID(uid);
		if (m==null) throw new MessagingException(mailManager.lookupResource(MailLocaleKey.ERROR_MESSAGE_NOT_FOUND));
		if (m.isExpunged()) throw new MessagingException(mailManager.lookupResource(MailLocaleKey.ERROR_MESSAGE_EXPUNGED));
		return m;
    }

//    public Set<String> getIds() {
//        return hash.keySet();
//    }
    
    public void fetch(Message fmsgs[], FetchProfile fp) throws MessagingException {
        open();
        ((SonicleIMAPFolder)folder).uid_fetch(fmsgs, fp);
    }

/*    public void fetchThreaded(ThreadMessage fmsgs[], FetchProfile fp, int start, int length) throws MessagingException {
        int n=fmsgs.length;
        if (length>(n-start)) length=n-start;
        Message xmsgs[]=new Message[length];
		for(int i=0;i<length;++i) xmsgs[i]=fmsgs[i].getMessage();
        open();
        ((SonicleIMAPFolder)folder).uid_fetch(xmsgs, fp);
    }*/
	
	public Message[] getMessages(int sort_by, boolean ascending, boolean refresh, int sort_group, boolean groupascending, boolean threaded, ImapQuery iq) throws MessagingException, IOException {
        boolean rebuilt=false;
        boolean sortchanged=false;
        //ArrayList<MimeMessage> xlist=null;
        Message xmsgs[]=null;
        MessageSearchResult msr=null;
        //MessageComparator mcomp=null;
        boolean dbg=(mailManager!=null && mailManager.isListDebugEnabled());
        long t0=System.nanoTime();

        synchronized(cacheLock) {
            //delta here = time BLOCKED acquiring this folder's cacheLock. Big value =>
            //another thread (a concurrent getMessages/refresh on this folder) held it.
            Service.listMark(dbg,"getMessages "+foldername,"acquired cacheLock",t0,t0);
			if (this.sort_by!=sort_by || this.ascending!=ascending || this.sort_group!=sort_group || this.groupascending!=groupascending || this.threaded!=threaded) {
                //a different sort combination takes the active slot: park the current
                //plain list on the shelf instead of losing it (another session/REST may
                //come back to it on its next refresh)
                shelveCurrentIfPlain();
                this.sort_by=sort_by;
                this.ascending=ascending;
                this.sort_group=sort_group;
                this.groupascending=groupascending;
				this.threaded=threaded;
                sortchanged=true;
            }
            boolean plainReq=isPlainQuery(iq);
            //a plain request the active slot can't serve may be served by a shelved
            //list built earlier for this same sort combination (drift still checked below)
            if (plainReq && !forceRefresh && (sortchanged || msgs==null || !cachedPlainQuery)) {
                if (unshelveCurrent()) sortchanged=false;
            }
            //A full SORT is mandatory on first build, a sort/group change, or a pending
            //forceRefresh (set by additions we could not splice). Otherwise, when the
            //client asks to refresh, we may REUSE the cached sorted array instead of
            //re-sorting - but only for an unfiltered list whose mailbox is unchanged
            //since the last sort. Anything else (active search, detected drift) resorts.
            boolean needSort = sortchanged || forceRefresh || msgs==null;
            if (!needSort && refresh) {
                boolean canReuse = incrementalListEnabled() && isPlainQuery(iq) && cachedPlainQuery
                                   && !mailboxChangedSinceSort();
                if (!canReuse) needSort=true;
            }
            if (needSort) {
                //a search is about to overwrite the active slot: park the still-valid
                //plain list first so the post-search plain refresh restores it instead
                //of paying a full re-SORT
                if (!plainReq) shelveCurrentIfPlain();
                refresh(iq);
                rebuilt=true;
            }
//            if (msgs==null || modified) {
//                msgs=new Message[list.size()];
//                list.toArray(msgs);
//                rebuilt=true;
//            }
            xmsgs=msgs;
            //mcomp=this.comparator;
        }

/*        if (rebuilt || sortchanged) {
            mcomp.setSortBy(sort_by);
            if(ascending) mcomp.setAscending();
            else mcomp.setDescending();
            Service.logger.debug("Sorting...");
            java.util.Arrays.sort(xmsgs,mcomp);
            Service.logger.debug("Done.");
        }*/
        modified=false;
        return xmsgs;
    }

/*	
    public ThreadMessage[] getThreadMessages(String pattern, String searchfield, boolean refresh) throws MessagingException {
        boolean rebuilt=false;
        boolean sortchanged=false;
        if (pattern==null) pattern="";
        if (searchfield==null) searchfield="";
        //ArrayList<MimeMessage> xlist=null;
        SonicleIMAPMessage xmsgs[]=null;
        MessageSearchResult msr=null;
        //MessageComparator mcomp=null;
        
        if (pattern.length()>0) {
            String skey=pattern+"."+searchfield+".threaded";
            msr=msrs.get(skey);
            if (msr==null) {
                msr=new MessageSearchResult(pattern,searchfield,0,false,0,false,true);
                msr.refresh();
                msrs.put(skey, msr);
                rebuilt=true;
            } else {
                if (refresh || modified || sortchanged) {
                    msr.refresh();
                    rebuilt=true;
                }
            }
            //xlist=msr.mylist;
            xmsgs=msr.tmsgs;
            //mcomp=msr.comparator;
        } else {
            if (!this.threaded) {
				this.threaded=true;
                sortchanged=true;
            }
            if (refresh || forceRefresh || sortchanged) {
                refresh();
                rebuilt=true;
            }
//            if (msgs==null || modified) {
//                msgs=new Message[list.size()];
//                list.toArray(msgs);
//                rebuilt=true;
//            }
            xmsgs=tmsgs;
            //mcomp=this.comparator;
        }
        
        modified=false;
        return xmsgs;
    }*/
	
    protected void cleanup(boolean endOfSession) {
		IdleThread it=null;
		synchronized(cacheLock) {
			if (endOfSession) {
				goidle=false;
				//hardClose() force-closes the dedicated connection's sockets, unblocking a
				//thread parked in idle() (or aborting an in-flight reconnect); the legacy
				//fallback still relies on folder.close(). interrupt() wakes a thread
				//sitting in the reconnect backoff so it sees goidle=false and exits promptly.
				Mailbox.DedicatedFolder dedicated=dedicatedIdleFolder;
				if (dedicated!=null) {
					try { dedicated.hardClose(); } catch(Exception ignore) {}
				}
				try {  folder.close(false); } catch(Exception exc) {}
				if (idleThread!=null) {
					idleThread.interrupt();
					it=idleThread;
					idleThread=null;
				}
				shelvedLists.clear();
				//this.comparator=null;
			}
			synchronized(dhash) { dhash.clear(); }
//			hash.clear();
//			list.clear();
			//unread=0;
			//recent=0;
			msgs=null;
		}
		//Join OUTSIDE cacheLock (the exiting thread doesn't need it, but event
		//handlers do — don't hold it for up to the timeout). Bounded so a socket
		//stuck in open() can't stall teardown; the account's socketTracker
		//force-close will reap it right after.
		if (it!=null) {
			try { it.join(2000); } catch(InterruptedException exc) { Thread.currentThread().interrupt(); }
			if (it.isAlive()) Service.logger.warn("Idle thread on {} still alive after teardown join", foldername);
		}
    }

    public void close() {
        try { folder.close(true); } catch(Exception exc) {}
        synchronized(dhash) { dhash.clear(); }
    }
    
    public void open() throws MessagingException {
		account.checkStoreConnected();
        if(!folder.isOpen()) {
            if((folder.getType()&Folder.HOLDS_MESSAGES)>0) {
              try {
				  folder.open(Folder.READ_WRITE);
			  } catch(MessagingException exc) {
				  folder.open(Folder.READ_ONLY);
			  }
            } else {
              folder.open(Folder.READ_ONLY);
            }
            synchronized(dhash) { dhash.clear(); }
            mailManager.poolOpened(this);

        }
    }
	
	public int getTreeMessageCacheCount() {
		Message[] m=msgs;
		int n=(m==null)?0:m.length;
		ArrayList<FolderCache> snapshot=children;
		if (snapshot!=null) {
			for(FolderCache fc: snapshot) {
				n+=fc.getTreeMessageCacheCount();
			}
		}
		return n;
	}

    public void save(Message msg) throws MessagingException {
        Message[] saveMsgs=new MimeMessage[1];
        saveMsgs[0]=msg;
		open();
        getFolder().appendMessages(saveMsgs);
        setForceRefresh();
    }
    
    private Message[] getMessages(long uids[], boolean fullthreads) throws MessagingException {
        open();
		Message[] msgs=((UIDFolder)folder).getMessagesByUID(uids);
		if (threaded && fullthreads) {
			ArrayList<Long> auids=new ArrayList<>(uids.length);
			ArrayList<Message> newMsgs=new ArrayList<>();
			for(long uid: uids) auids.add(uid);
			Collections.sort(auids);
			for(Message msg: msgs) {
				SonicleIMAPMessage smsg=(SonicleIMAPMessage)msg;
				if (smsg.getThreadIndent()==0 && smsg.getThreadChildren()>0) {
					Message tmsgs[]=getReferences(smsg.getMessageID());
					for(Message tmsg: tmsgs) {
						SonicleIMAPMessage stmsg=(SonicleIMAPMessage)tmsg;
						if (Collections.binarySearch(auids, stmsg.getUID())<0)
							newMsgs.add(tmsg);
					}
				}
			}
			if (newMsgs.size()>0) {
				Message[] newmsgs=new Message[newMsgs.size()];
				newMsgs.toArray(newmsgs);
				Message[] allmsgs=new Message[msgs.length+newmsgs.length];
				System.arraycopy(msgs, 0, allmsgs, 0, msgs.length);
				System.arraycopy(newmsgs, 0, allmsgs, msgs.length, newmsgs.length);
				msgs=allmsgs;
			}
		}
        return msgs;
    }
	
	protected Message[] getReferences(String msgid) throws MessagingException {
		HeaderTerm ht=new HeaderTerm("References",msgid);
		return folder.search(ht);
	}

    protected Message[] getAllMessages() throws MessagingException {
        open();
        return ((UIDFolder)folder).getMessagesByUID(1,UIDFolder.LASTUID);
    }
	
	private boolean canDelete() throws MessagingException {
		try {
			ACL acls[]=((IMAPFolder)folder).getACL();
			for(ACL acl: acls) {
				if (acl.getRights().contains(Rights.Right.DELETE))
					return true;
			}
		} catch(MessagingException exc) {
			
		}
		return false;
	}
	
	public void appendMessage(Message msg) throws MessagingException {
		Message msgs[]=new Message[1];
		msgs[0]=msg;
		folder.appendMessages(msgs);
	}
	
	private boolean arrayHasNull(Object a[]) {
		for(Object o: a)
			if (o==null) return true;
		return false;
	}
    
    public void moveMessages(long uids[], FolderCache to, boolean fullthreads) throws MessagingException {
		if (canDelete()) {
			Message mmsgs[]=getMessages(uids,fullthreads);
			if (mmsgs==null || arrayHasNull(mmsgs)) throw new MessagingException(mailManager.lookupResource(MailLocaleKey.ERROR_MESSAGE_NOT_FOUND));
			folder.copyMessages(mmsgs, to.folder);
			Boolean moveIsTrash = account.isTrashFolder(to.folder.getFullName());
			
			if (mailManager.isAuditEnabled()) {
				AuditLogManager.Batch auditBatch = mailManager.auditLogGetBatch(MailManager.AuditContext.MAIL, moveIsTrash ? MailManager.AuditAction.TRASH : MailManager.AuditAction.MOVE);
				if (auditBatch != null) {
					for (Message m : mmsgs) {
						String messageId = mailManager.getMessageID(m);
						if (StringUtils.isEmpty(messageId)) continue;

						HashMap<String, String> auditMoveMessage = new HashMap<>();
						auditMoveMessage.put("oldId", folder.getFullName());
						auditMoveMessage.put("newId", to.folder.getFullName());
						
						auditBatch.write(
							messageId,
							JsonResult.gson().toJson(auditMoveMessage)
						);
					}
					auditBatch.flush();
				}
			}
			
			folder.setFlags(mmsgs, new Flags(Flags.Flag.DELETED), true);
			removeDHash(uids);
			folder.expunge();
			spliceMovedOut(uids);     //source: removal -> splice when eligible
			to.setForceRefresh();     //destination: gained messages -> full SORT
			modified=true;
			to.modified=true;
			refreshUnreads();
			to.refreshUnreads();
		}
		else throw new MessagingException(mailManager.lookupResource(MailLocaleKey.PERMISSION_DENIED));
    }

    public void copyMessages(long uids[], FolderCache to, boolean fullthreads) throws MessagingException, IOException {
		
        if (mailManager.hasDmsDocumentArchiving() &&
                mailManager.isDmsSimpleArchiving() &&
                mailManager.getDmsSimpleArchivingMailFolder()!=null &&
                mailManager.getDmsSimpleArchivingMailFolder().equals(to.foldername)) {
				dmsArchiveMessages(uids, to, fullthreads);
        } else {
            Message mmsgs[]=getMessages(uids,fullthreads);
            folder.copyMessages(mmsgs, to.folder);
            to.setForceRefresh();
			to.refreshUnreads();
            to.modified=true;
			
			if (mailManager.isAuditEnabled()) {
				AuditLogManager.Batch auditBatch = mailManager.auditLogGetBatch(MailManager.AuditContext.MAIL, MailManager.AuditAction.COPY);
				if (auditBatch != null) {
					for (Message m : mmsgs) {
						String messageId = mailManager.getMessageID(m);
						if (StringUtils.isEmpty(messageId)) continue;

						HashMap<String, String> auditCopyMessage = new HashMap<>();
						auditCopyMessage.put("oldId", folder.getFullName());
						auditCopyMessage.put("newId", to.folder.getFullName());

						auditBatch.write(
							messageId,
							JsonResult.gson().toJson(auditCopyMessage)
						);
					}
					auditBatch.flush();
				}
			}
        }
    }
	
	private LocalDate getArchivingReferenceDate(Message message) throws MessagingException {
		java.util.Date date = message.getSentDate();
		if (date == null) date = message.getReceivedDate();
		return new LocalDate(date);
	}

    public void archiveMessages(long uids[], String folderarchive, boolean fullthreads) throws MessagingException {
		if (canDelete()) {
			Message mmsgs[]=getMessages(uids,fullthreads);
			MailUserSettings mus=mailManager.getMailUserSettings();
			String sep=""+account.getFolderSeparator();
			String xfolderarchive=folderarchive;
			Message xmmsg[]=new Message[1];
			
			AuditLogManager.Batch auditBatch = mailManager.auditLogGetBatch(MailManager.AuditContext.MAIL, MailManager.AuditAction.ARCHIVE);
			
			for(Message mmsg: mmsgs) {
				folderarchive=xfolderarchive;
				LocalDate ld=getArchivingReferenceDate(mmsg);
				FolderCache fcto=account.checkCreateAndCacheFolder(folderarchive);
				if (mus.getArchiveMode().equals(MailSettings.ARCHIVING_MODE_YEAR)) {
					folderarchive+=sep+ld.getYear();
					fcto=account.checkCreateAndCacheFolder(folderarchive);
				} else if (mus.getArchiveMode().equals(MailSettings.ARCHIVING_MODE_MONTH)) {
					folderarchive+=sep+ld.getYear();
					fcto=account.checkCreateAndCacheFolder(folderarchive);
					folderarchive+=sep+ld.getYear()+"-"+StringUtils.leftPad(ld.getMonthOfYear()+"",2,'0');
					fcto=account.checkCreateAndCacheFolder(folderarchive);
				}
				if (mus.isArchiveKeepFoldersStructure()) {
					String fname=foldername;
					//strip prefix is present
					String prefix=account.getFolderPrefix();
					if (prefix!=null && fname.startsWith(prefix)) {
						fname=fname.substring(prefix.length());
					}
					if (account.isUnderSharedFolder(foldername)) {
						String mainfolder=account.getMainSharedFolder(foldername);
						if (fname.equals(mainfolder)) fname="INBOX";
						else fname=fname.substring(mainfolder.length()+1);
					}
					folderarchive+=sep+fname;
					fcto=account.checkCreateAndCacheFolders(folderarchive);
				}
				xmmsg[0]=mmsg;
				folder.copyMessages(xmmsg, fcto.folder);
				fcto.setForceRefresh();
				fcto.modified=true;
				String messageId = mailManager.getMessageID(mmsg);

				if (auditBatch != null && StringUtils.isNotEmpty(messageId)) {
					HashMap<String, String> auditArchiveMessage = new HashMap<>();
					auditArchiveMessage.put("oldId", folder.getFullName());
					auditArchiveMessage.put("newId", fcto.folder.getFullName());

					auditBatch.write(
						messageId,
						JsonResult.gson().toJson(auditArchiveMessage)
					);
				}
			}
			if (auditBatch != null) auditBatch.flush();
			
			folder.setFlags(mmsgs, new Flags(Flags.Flag.DELETED), true);
			removeDHash(uids);
			folder.expunge();
			spliceMovedOut(uids);     //source: removal -> splice when eligible
			modified=true;
		}
		else throw new MessagingException(mailManager.lookupResource(MailLocaleKey.PERMISSION_DENIED));
    }

    public void dmsArchiveMessages(long uids[], FolderCache to, boolean fullthreads) throws MessagingException, IOException {
        Message mmsgs[]=getMessages(uids,fullthreads);
        MimeMessage newmmsgs[]=getDmsArchivedCopy(mmsgs);
        moveMessages(uids,to,fullthreads);
        folder.appendMessages(newmmsgs);
        refresh(new ImapQuery(false));
    }

    public void markDmsArchivedMessages(long uids[], boolean fullthreads) throws MessagingException, IOException {
        MimeMessage newmmsgs[]=getDmsArchivedCopy(uids,fullthreads);
        try {
			deleteMessages(uids,fullthreads);
			folder.appendMessages(newmmsgs);
		} catch(MessagingException exc) {
			//can't delete, try with flag
			Message msgs[]=getMessages(uids,fullthreads);
			for(Message m: msgs) 
				m.setFlags(MailManager.getFlagDmsArchived(),true);
		}
        
        refresh(new ImapQuery(false));
    }

    public MimeMessage[] getDmsArchivedCopy(long uids[],boolean fullthreads) throws MessagingException {
        Message mmsgs[]=getMessages(uids,fullthreads);
        return getDmsArchivedCopy(mmsgs);
    }

    public MimeMessage[] getDmsArchivedCopy(Message mmsgs[]) throws MessagingException {
        MimeMessage newmmsgs[]=new MimeMessage[mmsgs.length];
        int i=0;
        for(Message m:mmsgs) {
            try {
                MimeMessage mm=new MimeMessage((MimeMessage)m);
                mm.addHeader("X-WT-Archived", "Yes");
                Flags oflags=m.getFlags();
                if (oflags!=null) mm.setFlags(oflags, true);
                newmmsgs[i++]=mm;
            } catch(Exception exc) {
                Service.logger.error("Exception",exc);
                throw new MessagingException(exc.getMessage());
            }
        }
        return newmmsgs;
    }

    public void deleteAllMessages() throws MessagingException {
		_deleteMessages(getAllMessages());
	}
	
    public void deleteMessages(long uids[], boolean fullthreads) throws MessagingException {
		if (canDelete()) {
			Message mmsgs[]=getMessages(uids,fullthreads);
			if (mmsgs!=null && mmsgs.length>0) _deleteMessages(mmsgs);
			removeDHash(uids);
		}
		else throw new MessagingException(mailManager.lookupResource(MailLocaleKey.PERMISSION_DENIED));
    }
	
    public void deleteMessage(long uid) throws MessagingException {
		if (canDelete()) {
			Message msg=getMessage(uid);
                        if (msg!=null) {
                            Message mmsgs[]=new Message[] { msg };
                            _deleteMessages(mmsgs);
                        }
			removeDHash(new long[] {uid});
		}
		else throw new MessagingException(mailManager.lookupResource(MailLocaleKey.PERMISSION_DENIED));
    }
    
	private void _deleteMessages(Message mmsgs[]) throws MessagingException {
        AuditLogManager.Batch auditBatch = mailManager.auditLogGetBatch(MailManager.AuditContext.MAIL, MailManager.AuditAction.DELETE);
		
		ArrayList<Long> delUids=new ArrayList<>();
		for(Message dmsg: mmsgs) {
			if (dmsg!=null) {
				dmsg.setFlag(Flags.Flag.DELETED, true);
				//capture the UID while the message is still valid (before expunge)
				try { delUids.add(((SonicleIMAPMessage)dmsg).getUID()); } catch(Exception exc) {}
				String messageId = mailManager.getMessageID(dmsg);
				if (auditBatch != null && StringUtils.isNotEmpty(messageId)) {
					auditBatch.write(
						messageId,
						null
					);
				}
			}
		}
		if (auditBatch != null) auditBatch.flush();
		
        folder.expunge();
        //A delete is a pure removal: splice the UIDs out of the cached sorted list
        //(order-independent, so valid for any sort/group mode) instead of paying a
        //full re-SORT. Only when the cached list is an unfiltered, non-threaded one;
        //otherwise fall back to forcing a resort.
        if (incrementalListEnabled() && !threaded && msgs!=null && cachedPlainQuery && !delUids.isEmpty()) {
            synchronized(cacheLock) { spliceFromCache(delUids); }
        } else {
            setForceRefresh();
        }
        modified=true;
	refreshUnreads();
    }

    public void flagMessages(long uids[], String flag) throws MessagingException {
//        open();
        Message mmsgs[]=getMessages(uids,false);
        for(Message fmsg: mmsgs) {
			if (flag.equals("special")) {
				boolean wasspecial=fmsg.getFlags().contains(MailManager.getFlagFlagged());
				fmsg.setFlags(MailManager.getFlagFlagged(),!wasspecial);
			}
			else {
				if (!flag.equals("complete")) {
					fmsg.setFlags(Service.flagsAll, false);
					fmsg.setFlags(Service.oldFlagsAll, false);
					//fmsg.setFlags(Service.tbFlagsAll, false);
				}
				fmsg.setFlags(Service.flagsHash.get(flag), true);
				//Flags tbFlags=Service.tbFlagsHash.get(flag);
				//if (tbFlags!=null) fmsg.setFlags(tbFlags, true);
			}
            
        }
    }
  
    public void tagMessages(long uids[], String tagId) throws MessagingException {	
        Message mmsgs[]=getMessages(uids,false);
		MessagingException mexc=null;
		try {
			String flag = TagsHelper.tagIdToFlagString(WT.getCoreManager().getTag(tagId));
			AuditLogManager.Batch auditBatch = mailManager.auditLogGetBatch(MailManager.AuditContext.MAIL, MailManager.AuditAction.TAG);
			
			for(Message fmsg: mmsgs) {
				fmsg.setFlags(new Flags(flag), true);
				String messageId = mailManager.getMessageID(fmsg);
				if (auditBatch != null && StringUtils.isNotEmpty(messageId)) {
					HashMap<String, ArrayList<String>> auditTag = new HashMap<>();
					ArrayList<String> tags = new ArrayList<>();
					tags.add(tagId);
					auditTag.put("set", tags);

					auditBatch.write(
						messageId,
						JsonResult.gson().toJson(auditTag)
					);
				}
			}
			if (auditBatch != null) auditBatch.flush();
			
		} catch(MessagingException exc) {
			mexc=exc;
		} catch(WTException exc) {
			
		}
		
		if (mexc!=null) throw mexc;
		
    }
  
    public void untagMessages(long uids[], String tagId) throws MessagingException {		
        Message mmsgs[] = getMessages(uids,false);
		MessagingException mexc = null;
		try {
			String flag = TagsHelper.tagIdToFlagString(WT.getCoreManager().getTag(tagId));
			AuditLogManager.Batch auditBatch = mailManager.auditLogGetBatch(MailManager.AuditContext.MAIL, MailManager.AuditAction.TAG);
			
			for (Message fmsg: mmsgs) {
				fmsg.setFlags(new Flags(flag), false);
				String messageId = mailManager.getMessageID(fmsg);
				if (auditBatch != null && StringUtils.isNotEmpty(messageId)) {
					HashMap<String, ArrayList<String>> auditTag = new HashMap<>();
					ArrayList<String> tags = new ArrayList<>();
					tags.add(tagId);
					auditTag.put("unset", tags);

					auditBatch.write(
						messageId,
						JsonResult.gson().toJson(auditTag)
					);
				}
			}
			if (auditBatch != null) auditBatch.flush();
			
		} catch(MessagingException exc) {
			mexc=exc;
		} catch(WTException exc) {
			
		}
		
		if (mexc!=null) throw mexc;
    }
  
/*	public void updateMessageTag(long uids[], String oldTagId, String newTagId) throws MessagingException {
		ArrayList<AuditReferenceDataEntry> updated = null;
		if (mailManager.isAuditEnabled()) updated=new ArrayList<>();		
		Message mmsgs[]=getMessages(uids, false);
		MessagingException mexc=null;
		try {
			for(Message fmsg: mmsgs) {
				Flags attachedFlags = fmsg.getFlags();
				Flags oldFlag = new Flags(oldTagId);
				if(attachedFlags.contains(oldFlag)) {
					fmsg.setFlags(oldFlag, false);
					fmsg.setFlags(new Flags(newTagId), true);
					if (mailManager.isAuditEnabled()) updated.add(new AuditMailUpdateTagObj(mailManager.getMessageID(fmsg), oldTagId, newTagId));
				}
			}
		} catch(MessagingException exc) {
			mexc=exc;
		}
		
		if (mailManager.isAuditEnabled())
			mailManager.auditLogWrite(
				MailManager.AuditContext.MAIL,
				MailManager.AuditAction.TAG, 
				updated
			);
		
		if (mexc!=null) throw mexc;
	}*/
	
	public void applyMessagesTags(long uids[], String tagIds[]) throws MessagingException {
        Message mmsgs[]=getMessages(uids,false);
		MessagingException mexc=null;
		Flags allFlags=new Flags();
		Flags newFlags=new Flags();
		try {
			for(Tag tag: WT.getCoreManager().listTags().values()) {
				allFlags.add(TagsHelper.tagIdToFlagString(tag));
			}
			for(String tagId: tagIds) {
				newFlags.add(TagsHelper.tagIdToFlagString(WT.getCoreManager().getTag(tagId)));
			}
		} catch(Exception exc) {
		}
		try {
			AuditLogManager.Batch auditBatch = mailManager.auditLogGetBatch(MailManager.AuditContext.MAIL, MailManager.AuditAction.TAG);
			
			for (Message fmsg : mmsgs) {
				List<String> msgNewFlags = null;
				List<String> msgOldFlags = mailManager.flagsToTagsIds(fmsg.getFlags());

				if (tagIds != null) msgNewFlags = new ArrayList<>(Arrays.asList(tagIds));
				else msgNewFlags=new ArrayList<>();

				fmsg.setFlags(allFlags, false);
				fmsg.setFlags(newFlags, true);

				String messageId = mailManager.getMessageID(fmsg);
				if (auditBatch != null && StringUtils.isNotEmpty(messageId)) {
					HashMap<String, List<String>> auditTag = WT.getCoreManager().compareTags(msgOldFlags, msgNewFlags);

					auditBatch.write(
						messageId,
						JsonResult.gson().toJson(auditTag)
					);
				}
			}
			if (auditBatch != null) auditBatch.flush();
			
		} catch(MessagingException exc) {
			mexc=exc;
		}
		
		if (mexc!=null) throw mexc;
    }
	
    public void clearMessagesFlag(long uids[]) throws MessagingException {
//        open();
        Message mmsgs[]=getMessages(uids,false);
        for(Message fmsg: mmsgs) {
            fmsg.setFlags(Service.flagsAll, false);
            fmsg.setFlags(Service.oldFlagsAll, false);
            //fmsg.setFlags(Service.tbFlagsAll, false);
			//fmsg.setFlags(Service.flagFlagged,false);
        }
    }

    public void setMessagesSeen(long uids[]) throws MessagingException {
        Message mmsgs[]=getMessages(uids,false);
        int changed=setMessagesSeen(mmsgs,true);
        if (changed>0) {
            //unread-=changed;
            //updateUnreads();
			refreshUnreadMessagesCount();
        }
    }
    
    public void setMessagesUnseen(long uids[]) throws MessagingException {
        Message mmsgs[]=getMessages(uids,false);
        int changed=setMessagesSeen(mmsgs,false);
        if (changed>0) {
            //unread+=changed;
            //updateUnreads();
			refreshUnreadMessagesCount();
        }
    }

    public void setMessagesSeen(boolean updateParents) throws MessagingException {
        //try {
        //    open();
        //} catch(Exception exc) {
        //    return;
        //}
        boolean wasOpen=folder.isOpen();
        if (!wasOpen) {
            try { folder.open(Folder.READ_WRITE); } catch(MessagingException exc) { return; }
        }
        if (folder.getUnreadMessageCount()>0) {
            Message umsgs[]=folder.search(unseenSearchTerm);
            folder.setFlags(umsgs, seenFlags, true);
            unread=0;
			unreadInitialized=true;
			if (!updateUnreadChildren()) sendUnreadChangedMessage();
        } else {
			updateUnreadChildren();
		}
		if (updateParents && parent!=null && !parent.isRoot()) parent.updateUnreads();
        if (!wasOpen) folder.close(true);
    }

    public void setMessagesUnseen(boolean updateParents) throws MessagingException {
        try {
            open();
        } catch(Exception exc) {
            return;
        }
        int n=folder.getMessageCount();
        Message umsgs[]=folder.search(seenSearchTerm);
        folder.setFlags(umsgs, seenFlags, false);
        unread=n;
		unreadInitialized=true;
		if (!updateUnreadChildren()) sendUnreadChangedMessage();
        if (updateParents && parent!=null && !parent.isRoot()) parent.updateUnreads();
    }

    private int setMessagesSeen(Message mmsgs[], boolean seen) throws MessagingException {
        int changed=0;
        for(Message fmsg: mmsgs) {
            if (fmsg.isSet(Flags.Flag.SEEN)!=seen) {
                fmsg.setFlag(Flags.Flag.SEEN, seen);
                ++changed;
            }
        }
        return changed;
    }

    public Folder createFolder(String name) throws MessagingException {
        Folder newfolder=null;
        if (!account.hasDifferentDefaultFolder() && isRoot) {
            String prefix=account.getFolderPrefix();
            if (prefix!=null) name=prefix+name;
            newfolder=account.getFolder(name);
        } else {
            newfolder=folder.getFolder(name);
        }
        if (newfolder.create(Folder.HOLDS_MESSAGES)) {
            account.addFoldersCache(this, newfolder);
        }
        else newfolder=null;
        return newfolder;
    }
	
	private EnvelopeSortTerm createDateSortTerm(boolean ascending) {
		if (!mailManager.getMailUserSettings().isUseArrivalDate(foldername)) return new DateSortTerm(!ascending);
		else return new ArrivalSortTerm(!ascending);
	}
	
	private SonicleSortTerm _prepareSortTerm(int sort_by, boolean ascending, int sort_group, boolean groupascending) {
		SonicleSortTerm gsort=null;
      
		switch(sort_group) {
			case SORT_BY_DATE:
				gsort = createDateSortTerm(groupascending);
				break;
			case SORT_BY_FLAG:
				//<SonicleMail>sort=new UserFlagSortTerm(MailService.flagStrings, !ascending);</SonicleMail>
				gsort=new FlagSortTerm(mailManager.allFlagStrings, !groupascending);
				break;
			case SORT_BY_MSGIDX:
				gsort=new MessageIDSortTerm(!groupascending);
				break;
			case SORT_BY_PRIORITY:
				gsort=new PrioritySortTerm(!groupascending);
				break;
			case SORT_BY_RCPT:
				gsort=new ToSortTerm(!groupascending);
				break;
			case SORT_BY_SENDER:
				gsort=new FromSortTerm(!groupascending);
				break;
			case SORT_BY_SIZE:
				gsort=new SizeSortTerm(!groupascending);
				break;
			case SORT_BY_STATUS:
				gsort=new StatusSortTerm(!groupascending);
				break;
			case SORT_BY_SUBJECT:
				gsort=new SubjectSortTerm(!groupascending);
				break;
		}

		SonicleSortTerm sort=null;

		switch(sort_by) {
			case SORT_BY_DATE:
				sort=createDateSortTerm(ascending);
				break;
			case SORT_BY_FLAG:
				//<SonicleMail>sort=new UserFlagSortTerm(MailService.flagStrings, !ascending);</SonicleMail>
				sort=new FlagSortTerm(mailManager.allFlagStrings, !ascending);
				sort.append(createDateSortTerm(false));
				break;
			case SORT_BY_MSGIDX:
				sort=new MessageIDSortTerm(!ascending);
				break;
			case SORT_BY_PRIORITY:
				sort=new PrioritySortTerm(!ascending);
				sort.append(createDateSortTerm(false));
				break;
			case SORT_BY_RCPT:
				sort=new ToSortTerm(!ascending);
				break;
			case SORT_BY_SENDER:
				sort=new FromSortTerm(!ascending);
				break;
			case SORT_BY_SIZE:
				sort=new SizeSortTerm(!ascending);
				break;
			case SORT_BY_STATUS:
				sort=new StatusSortTerm(!ascending);
				sort.append(createDateSortTerm(false));
				break;
			case SORT_BY_SEEN:
				sort=new SeenSortTerm(!ascending);
				sort.append(createDateSortTerm(false));
				break;
			case SORT_BY_SUBJECT:
				sort=new SubjectSortTerm(!ascending);
				break;
		}

		//Service.logger.debug("gsort="+gsort);
		//Service.logger.debug("sort="+sort);

		//Prepend group sorting if present
		if (gsort!=null) {
			if (sort!=null) {
				gsort.append(sort);
			}
			sort=gsort;
		}
		
		return sort;
	}
	
	private Message[] _getMessages(String patterns, String searchfields, int sort_by, boolean ascending, int sort_group, boolean groupascending, ImapQuery iq) throws MessagingException, IOException {

		Message[] xmsgs=null;
		open();

		if((folder.getType()&Folder.HOLDS_MESSAGES)>0) {
			SonicleSortTerm sort=_prepareSortTerm(sort_by,ascending,sort_group,groupascending);
			open();

			//<SonicleMail>xmsgs=((IMAPFolder)folder).sort(sort, term);</SonicleMail>
			try {
				//xmsgs=((SonicleIMAPFolder)folder).uid_sort(sort, term);
				xmsgs=((SonicleIMAPFolder)folder).sort(sort, iq.getSearchTerm());
			} catch(Exception exc) {
				close();
				open();
				//xmsgs=((SonicleIMAPFolder)folder).uid_sort(sort, term);
				xmsgs=((SonicleIMAPFolder)folder).sort(sort, iq.getSearchTerm());
			}
		}
		xmsgs=applyImapQuerySecondaryFilters(xmsgs,iq);
		return xmsgs;
	}

	private Message[] _getThreadedMessages(String patterns, String searchfields, ImapQuery iq) throws MessagingException, IOException {
		Message[] tmsgs=null;
		open();

		if((folder.getType()&Folder.HOLDS_MESSAGES)>0) {

			
			open();
			
			boolean hasrefs=((IMAPStore)folder.getStore()).hasCapability("THREAD=REFS");
			String method=hasrefs?"REFS":"REFERENCES";
			//Light profile (no CONTENT_INFO): threading must materialize every message up front
			//to collapse threads and pick representatives, but that only needs dates/flags/headers.
			//BODYSTRUCTURE is parsed per message by JavaMail and is only needed for the
			//attachment/invitation icons on the visible page, which processListMessages fetches
			//per-page. Loading it for the whole folder here is wasted wire + parse time.
			FetchProfile fp=mailManager.getThreadMessageFetchProfile();
			long tt0=System.currentTimeMillis();
			try {
				tmsgs=((SonicleIMAPFolder)folder).thread(method,iq.getSearchTerm(),fp);
			} catch(Exception exc) {
				Service.logger.debug("**************Retrying thread*********************");
				close();
				open();
				tmsgs=((SonicleIMAPFolder)folder).thread(method,iq.getSearchTerm(),fp);
			}
			long ttel=System.currentTimeMillis()-tt0;
			if (ttel>1000) {
				//breakdown of a slow threaded build: monitor wait = another operation
				//busy on this same folder object (e.g. new-mail scan SEARCH); cmd =
				//server-side THREAD (cold index rebuild); fetch = bulk envelope FETCH
				SonicleIMAPFolder sf=(SonicleIMAPFolder)folder;
				Service.logger.info("[LISTDBG THREAD {}] SLOW {}ms: folder-monitor wait {}ms, cacheLock wait {}ms, THREAD cmd {}ms, bulk fetch ({} msgs) {}ms, parse/build {}ms",
					foldername, ttel,
					ttel-sf.getLastThreadBodyMs(),
					sf.getLastThreadCacheLockWaitMs(),
					sf.getLastThreadCmdMs(),
					(tmsgs!=null?tmsgs.length:-1),
					sf.getLastThreadFetchMs(),
					sf.getLastThreadBodyMs()-sf.getLastThreadCacheLockWaitMs()-sf.getLastThreadCmdMs()-sf.getLastThreadFetchMs());
			}

			//recalculate open threads and total open children
			if (tmsgs!=null) {
				synchronized(openThreads) {
					int total=0;
					HashMap<Long,Integer> newOpenThreads=new HashMap<>();
					for(Message tmsg: tmsgs) {
						long tuid=((SonicleIMAPMessage)tmsg).getUID();
						int tchildren=((SonicleIMAPMessage)tmsg).getThreadChildren();
						if (openThreads.containsKey(tuid)) {
							newOpenThreads.put(tuid, tchildren);
							total+=tchildren;
						}
					}
					openThreads.clear();
					openThreads.putAll(newOpenThreads);
					totalOpenThreadChildren=total;
				}
			}
		}
		tmsgs=applyImapQuerySecondaryFilters(tmsgs,iq);
		return tmsgs;
	}
	
	private Message[] applyImapQuerySecondaryFilters(Message msgs[], ImapQuery iq) throws MessagingException, IOException {
		Message rmsgs[]=msgs;
		
		if (iq.hasAttachment() || iq.hasAttachmentName()) {
			ArrayList<Message> amsgs=new ArrayList<Message>();
			//ensure BODYSTRUCTURE has been loaded
			fetch(msgs, FP_BS);
			for(Message m: msgs) {
				if (mailManager.hasAttachments(m, iq.getAttachmentName())) amsgs.add(m);
			}
			rmsgs=new SonicleIMAPMessage[amsgs.size()];
			amsgs.toArray(rmsgs);
		}
		
		if (iq.hasNotePattern()) {
			try {
				ArrayList<String> msgIds=mailManager.searchNotes(iq.getNotePattern());
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
	
	public Message[] getMessagesByMessageId(String id) throws MessagingException {
		boolean wasOpen=folder.isOpen();
		if (!wasOpen) folder.open(Folder.READ_WRITE);
		Message msgs[]=folder.search(new HeaderTerm("Message-ID", id));
		if (!wasOpen) folder.close(false);
		return msgs;
	}

	public Message getMessageByMessageId(String id) throws MessagingException {
		boolean wasOpen=folder.isOpen();
		if (!wasOpen) folder.open(Folder.READ_WRITE);
		Message msgs[]=folder.search(new HeaderTerm("Message-ID", id));
		if (!wasOpen) folder.close(false);
		return msgs.length > 0 ? msgs[0] : null;
	}
	
  protected Message[] advancedSearchMessages(AdvancedSearchEntry entries[], boolean and, int sort_by, boolean ascending) throws MessagingException {

    Locale locale=mailManager.getLocale();
    Message[] xmsgs=null;

    if((folder.getType()&Folder.HOLDS_MESSAGES)>0) {
      open();
      SearchTerm term=null;
      ArrayList<SearchTerm> terms=new ArrayList<SearchTerm>();
      for(AdvancedSearchEntry entry: entries) {
            String searchfield=entry.getField();
            int method=entry.getMethod();
            String pattern=entry.getValue();
            boolean negate=(method==AdvancedSearchEntry.METHOD_DOESNOTCONTAIN||method==AdvancedSearchEntry.METHOD_ISNOT);
            //Service.logger.debug("ADVSEARCH: pattern="+pattern+" searchfield="+searchfield);
			if (searchfield.equals("any")) {
				SearchTerm anyterms[]=new SearchTerm[6];
				anyterms[0]=new SubjectTerm(pattern);
				anyterms[1]=new RecipientStringTerm(Message.RecipientType.TO, pattern);
				anyterms[2]=new RecipientStringTerm(Message.RecipientType.CC, pattern);
				anyterms[3]=new RecipientStringTerm(Message.RecipientType.BCC, pattern);
				anyterms[4]=new FromStringTerm(pattern);
				anyterms[5]=new BodyTerm(pattern);
				term=new OrTerm(anyterms);
			} else if(searchfield.equals("subject")) {
              term=new SubjectTerm(pattern);
            } else if(searchfield.equals("to")) {
              term=new RecipientStringTerm(Message.RecipientType.TO, pattern);
            } else if(searchfield.equals("cc")) {
              term=new RecipientStringTerm(Message.RecipientType.CC, pattern);
            } else if(searchfield.equals("bcc")) {
              term=new RecipientStringTerm(Message.RecipientType.BCC, pattern);
            } else if(searchfield.equals("from")) {
              term=new FromStringTerm(pattern);
            } else if(searchfield.equals("tocc")) {
              term=new OrTerm(new RecipientStringTerm(Message.RecipientType.TO, pattern),new RecipientStringTerm(Message.RecipientType.CC, pattern));
            } else if(searchfield.equals("alladdr")) {
              SearchTerm all[]=new SearchTerm[4];
              all[0]=new RecipientStringTerm(Message.RecipientType.TO, pattern);
              all[1]=new RecipientStringTerm(Message.RecipientType.CC, pattern);
              all[2]=new RecipientStringTerm(Message.RecipientType.BCC, pattern);
              all[3]=new FromStringTerm(pattern);
              term=new OrTerm(all);
            } else if(searchfield.equals("body")) {
              term=new BodyTerm(pattern);
            } else if (searchfield.equals("flags")) {
              term=new FlagTerm(new Flags(pattern),!negate);
              negate=false;
            } else if (searchfield.equals("tags")) {
			  try {
				term=new FlagTerm(new Flags(
					  TagsHelper.tagIdToFlagString(
						  WT.getCoreManager().getTag(pattern)
					  )
				),!negate);
				negate=false;
			  } catch(Exception exc) {  
			  }
            }else if (searchfield.equals("status")) {
                if (pattern.equals("unread")) {
                    term=unseenSearchTerm;
                } else if (pattern.equals("new")) {
                    term=recentSearchTerm;
                } else if (pattern.equals("replied")) {
                    term=repliedSearchTerm;
                } else if (pattern.equals("forwarded")) {
                    term=forwardedSearchTerm;
                } else if (pattern.equals("read")) {
                    term=seenSearchTerm;
                }
            } else if (searchfield.equals("priority")) {
              HeaderTerm p1=new HeaderTerm("X-Priority", "1");
              HeaderTerm p2=new HeaderTerm("X-Priority", "2");
              term=new OrTerm(p1,p2);
            } else if(searchfield.equals("date")) {
              pattern=pattern.trim();
              int yyyy=Integer.parseInt(pattern.substring(0,4));
              int mm=Integer.parseInt(pattern.substring(4,6));
              int dd=Integer.parseInt(pattern.substring(6,8));
              java.util.Calendar c=java.util.Calendar.getInstance();
              c.set(yyyy, mm-1, dd);
              int comparison=(method==AdvancedSearchEntry.METHOD_UPTO)?DateTerm.LE:
                  (method==AdvancedSearchEntry.METHOD_SINCE)?DateTerm.GE:
                  DateTerm.EQ;
              term=new ReceivedDateTerm(comparison, c.getTime());
            }
            if (term!=null) {
                if (!negate) terms.add(term);
                else terms.add(new NotTerm(term));
            }
      }
      int n=terms.size();
      if (n==1) {
            term=terms.get(0);
      }
      else if (n>1) {
            SearchTerm vterms[]=new SearchTerm[n];
            terms.toArray(vterms);
            if (and) term=new AndTerm(vterms);
            else term=new OrTerm(vterms);
      }

      SonicleSortTerm sort=null;
      switch(sort_by) {
          case SORT_BY_DATE:
              sort=createDateSortTerm(ascending);
              break;
          case SORT_BY_FLAG:
              //<SonicleMail>sort=new UserFlagSortTerm(MailService.flagStrings, !ascending);</SonicleMail>
			  sort=new FlagSortTerm(mailManager.allFlagStrings, !ascending);
              break;
          case SORT_BY_MSGIDX:
              sort=new MessageIDSortTerm(!ascending);
              break;
          case SORT_BY_PRIORITY:
              sort=new PrioritySortTerm(!ascending);
              break;
          case SORT_BY_RCPT:
              sort=new ToSortTerm(!ascending);
              break;
          case SORT_BY_SENDER:
              sort=new FromSortTerm(!ascending);
              break;
          case SORT_BY_SIZE:
              sort=new SizeSortTerm(!ascending);
              break;
          case SORT_BY_STATUS:
              sort=new StatusSortTerm(!ascending);
              break;
          case SORT_BY_SUBJECT:
              sort=new SubjectSortTerm(!ascending);
              break;
      }
      open();
      //<SonicleMail>xmsgs=((IMAPFolder)folder).sort(sort, term);</SonicleMail>
      xmsgs=((SonicleIMAPFolder)folder).sort(sort, term);
    }

    return xmsgs;
  }



  /*private int getMonth(String smonth) {
    if(smonth.length()<3) {
      return-1;
    }
    String language=mailManager.getLocale().getLanguage().toLowerCase();
    HashMap<String,Integer> hash=months.get(language);
    if(hash==null) {
      return -1;
    }
    Integer imonth=(Integer)hash.get(smonth.substring(0, 3).toLowerCase());
    if(imonth==null) {
      return-1;
    }
    return imonth;
  }*/

  private java.util.Date parseDate(String pattern) {
    pattern=pattern.replace('-', '/');
    java.util.Date date=null;
    try {
      date=java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT, mailManager.getLocale()).parse(pattern);
    } catch(Exception exc) {}
    if(date==null) {
      try {
        date=java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, mailManager.getLocale()).parse(pattern);
      } catch(Exception exc) {}
    }
    if(date==null) {
      try {
        date=java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG, mailManager.getLocale()).parse(pattern);
      } catch(Exception exc) {}
    }
    return date;
  }

    void setParent(FolderCache fc) {
        parent=fc;
    }

    public FolderCache getParent() {
        return parent;
    }

    public ArrayList<FolderCache> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return (children!=null && children.size()>0);
    }

    //copy-on-write, mutators serialized on 'this': concurrent iterators (MFT sweep,
    //idle handlers) keep reading their immutable snapshot
    synchronized void addChild(FolderCache fc) {
        ArrayList<FolderCache> cur = (children==null) ? new ArrayList<FolderCache>() : new ArrayList<>(children);
        cur.add(fc);
        HashMap<String,FolderCache> map = (childrenMap==null) ? new HashMap<String,FolderCache>() : new HashMap<>(childrenMap);
        map.put(fc.foldername, fc);
        childrenMap=map;
        children=cur;
    }

    synchronized void removeChild(FolderCache fc) {
        if (children!=null) {
            ArrayList<FolderCache> cur = new ArrayList<>(children);
            cur.remove(fc);
            children = cur.isEmpty() ? null : cur;
        }
        if (childrenMap!=null) {
            HashMap<String,FolderCache> map = new HashMap<>(childrenMap);
            map.remove(fc.foldername);
            childrenMap=map;
        }
    }

	public boolean hasChild(String name) {
		HashMap<String,FolderCache> map=childrenMap;
		return map!=null && map.containsKey(name);
	}

    public HTMLMailData getMailData(MimeMessage m) throws MessagingException, IOException {
        HTMLMailData mailData=null;
        synchronized(this) {
			long muid=-1;
			if (m instanceof SonicleIMAPMessage) {
				muid=((SonicleIMAPMessage)m).getUID();
				synchronized(dhash) { mailData=dhash.get(muid); }
				if (mailData!=null && mailData.getMessage()!=m) {
					Service.logger.debug("found wrong cached message, refreshing");
					mailData=null;
				}
			}
            if (mailData==null) {
                mailData=prepareHTMLMailData(m);
                if (muid>0) synchronized(dhash) { dhash.put(muid, mailData); }
            }
        }
        return mailData;
    }
	
	public void setThreadOpen(long uid, boolean open) throws MessagingException {
		int nchildren=((SonicleIMAPMessage)getMessage(uid)).getThreadChildren();
		synchronized(openThreads) {
			if (open) {
				if (!openThreads.containsKey(uid)) {
					totalOpenThreadChildren+=nchildren;
					openThreads.put(uid, nchildren);
				}
			}
			else {
				if (openThreads.containsKey(uid)) {
					totalOpenThreadChildren-=nchildren;
					openThreads.remove(uid);
				}
			}
		}
	}

	public boolean isThreadOpen(long uid) {
		synchronized(openThreads) {
			return openThreads.get(uid)!=null;
		}
	}
	
	public int getThreadedCount() {
		int threadRoots=((SonicleIMAPFolder)folder).getThreadRoots();
		return threadRoots+totalOpenThreadChildren;
	}
	
	class HTMLPart {
		
		String html;
		ArrayList<String> hrefs;
		
		HTMLPart(String html) {
			this(html, new ArrayList<String>());
		}
		HTMLPart(String html, ArrayList<String> hrefs) {
			this.html=html;
			this.hrefs=hrefs;
		}
	}
    
    public ArrayList<HTMLPart> getHTMLParts(MimeMessage m, long msguid, boolean forEdit, boolean balanceTags, boolean removeHeadStyle) throws MessagingException, IOException {
        return getHTMLParts(m, msguid, null, null, forEdit, balanceTags, removeHeadStyle);
    }
    
    public ArrayList<HTMLPart> getHTMLParts(MimeMessage m, String provider, String providerid, boolean balanceTags, boolean removeHeadStyle) throws MessagingException, IOException {
        return getHTMLParts(m, -1, provider, providerid, false, balanceTags, removeHeadStyle);
    }
    
    private ArrayList<HTMLPart> getHTMLParts(MimeMessage m, long msguid, String provider, String providerid, boolean forEdit, boolean balanceTags, boolean removeHeadStyle) throws MessagingException, IOException {
      ArrayList<HTMLPart> htmlparts=new ArrayList<>();
      //WebTopApp webtopapp=environment.getWebTopApp();
      //Session wts=environment.get();
      HTMLMailData mailData=getMailData(m);
      int objid=0;
      Part msgPart=null;
      String msgSubject;
      String msgFrom;
      String msgDate;
      String msgTo;
      String msgCc;
	  ProfileI18n i18nInfo = mailManager.getI18nInfo();
	  boolean icalhtmlview=false;
          
      //first cycle parts to get a possible default charset
      String defaultCharset=null;
      for(int i=0;defaultCharset==null && i<mailData.getDisplayPartCount();++i) {
        Part dispPart=mailData.getDisplayPart(i);
        
        //Use workaround for NethServer installation:
        defaultCharset=MailUtils.getCharsetOrNull(dispPart);
      }
      
      for(int i=0;i<mailData.getDisplayPartCount();++i) {
        Part dispPart=mailData.getDisplayPart(i);
        java.io.InputStream istream=null;
        String charset=null;
        if (defaultCharset==null)
            //Use workaround for NethServer installation:
            charset=MailUtils.getCharsetOrDefault(dispPart);
        else {
            //Use workaround for NethServer installation:
            charset=MailUtils.getCharsetOrNull(dispPart);
            if (charset==null) charset=defaultCharset;
        }
//        boolean ischarset=false;
//        try { ischarset=java.nio.charset.Charset.isSupported(charset); } catch(Exception exc) {}
//        if (!ischarset) charset="UTF-8";
		if (dispPart.isMimeType("text/plain")||dispPart.isMimeType("text/html")||dispPart.isMimeType("message/delivery-status")||dispPart.isMimeType("message/disposition-notification")||dispPart.isMimeType("text/calendar")||dispPart.isMimeType("application/ics")) {
            try {
              if (dispPart instanceof jakarta.mail.internet.MimeMessage) {
                jakarta.mail.internet.MimeMessage mm=(jakarta.mail.internet.MimeMessage)dispPart;
                istream=mm.getInputStream();
              } else if (dispPart instanceof jakarta.mail.internet.MimeBodyPart) {
                jakarta.mail.internet.MimeBodyPart mm=(jakarta.mail.internet.MimeBodyPart)dispPart;
                istream=mm.getInputStream();
              }
            } catch(Exception exc) { //unhandled format, get Raw data
              if (dispPart instanceof jakarta.mail.internet.MimeMessage) {
                jakarta.mail.internet.MimeMessage mm=(jakarta.mail.internet.MimeMessage)dispPart;
                istream=mm.getRawInputStream();
              } else if (dispPart instanceof jakarta.mail.internet.MimeBodyPart) {
                jakarta.mail.internet.MimeBodyPart mm=(jakarta.mail.internet.MimeBodyPart)dispPart;
                istream=mm.getRawInputStream();
              }
            }


            if (istream==null) throw new IOException("Unknown message class "+dispPart.getClass().getName());


            StringBuffer xhtml=new StringBuffer();
            if (dispPart.isMimeType("text/html")) {
                Object tlock=new Object();
                String uri=mailManager.getCurrentRefererUri();
                HTMLMailParserThread parserThread=null;
                if (provider==null) parserThread=new HTMLMailParserThread(tlock, istream, charset, uri, msguid, forEdit, balanceTags, removeHeadStyle);
                else parserThread=new HTMLMailParserThread(tlock, istream, charset, uri, provider, providerid, balanceTags, removeHeadStyle);
                try {
                    java.io.BufferedReader breader=startHTMLMailParser(parserThread,mailData,false);
                    char chars[]=new char[8192];
                    int n=0;
                    while((n=breader.read(chars))>=0) {
                     if (n>0) xhtml.append(chars,0,n);
                    }
                } catch(Exception exc) {
                    Service.logger.error("Exception",exc);
                    parserThread.notifyParserEndOfRead();
                    //            return exc.getMessage();
                }
                parserThread.notifyParserEndOfRead();

                htmlparts.add(new HTMLPart(xhtml.toString(),parserThread.getHrefs()));
                //String key="htmlpart"+objid;
                //controller.putTempData(key,html);
			} else if (dispPart.isMimeType("text/calendar")||dispPart.isMimeType("application/ics")) {
				if (dispPart.getContentType().contains("method=")) {
					try {
						ICalendarRequest ir = new ICalendarRequest(istream);
						mailData.setICalendarRequest(ir);
						if (!icalhtmlview) {
							String laf = mailManager.getCoreUserSettings().getUILookAndFeel();
							String theme = mailManager.getCoreUserSettings().getUITheme();
							String pbody = ir.generatePreviewBody(i18nInfo.getLocale(), i18nInfo.getTimezone());
							String phtml = ICalendarRequest.htmlWrap(pbody, charset, mailManager.getManifest(), theme, laf);
							htmlparts.add(0, new HTMLPart(phtml));
							icalhtmlview = true;
						}
						if (!mailData.hasICalAttachment()) mailData.addAttachmentPart(dispPart,0);
						
					} catch (Exception ex) {
						mailData.addAttachmentPart(dispPart,0);
						//MailService.logger.error("Error parsing calendar part",exc);
					}
					
				} else {
					mailData.addAttachmentPart(dispPart,0);
				}
            } else {
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
				String onEmailClick = "parent.WT.handleMailAddress(" + sparams + "); return false;";
				
				content = content.replaceAll("(" + RegexUtils.MATCH_EMAIL_ADDRESS + ")", "<a target=_blank href=$1 onClick ='"+onEmailClick+"'>$1</a>");
				content = content.replaceAll("(" + RegexUtils.MATCH_URL + ")", "<a target= _blank href=$1>$1</a>");
				content = content.replaceAll("(" + RegexUtils.MATCH_WWW_URL + ")", "$2<a target=_blank href='http://$3'>$3</a>$4");

				xhtml.append(content);	
                xhtml.append("<BR>");
                xhtml.append("</pre><HR></body></html>");
				//TODO : check converted urls above for external links
				htmlparts.add(new HTMLPart(xhtml.toString()));
            }
        } else if (dispPart.isMimeType("message/*")) {
          StringBuffer xhtml=new StringBuffer();
          msgPart=dispPart;
          Message xmsg=(Message)dispPart.getContent();
          msgSubject=xmsg.getSubject();
          if (msgSubject==null) msgSubject="";
          msgSubject=MailUtils.htmlescape(msgSubject);
          Address ad[]=xmsg.getFrom();
          if (ad!=null) msgFrom=mailManager.getHTMLDecodedAddress(ad[0]);
          else msgFrom="";
          java.util.Date dt=xmsg.getSentDate();
          if (dt!=null) msgDate=java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG,java.text.DateFormat.LONG, i18nInfo.getLocale()).format(dt);
          else msgDate="";
          ad=xmsg.getRecipients(Message.RecipientType.TO);
          msgTo=null;
          if (ad!=null) {
            msgTo="";
            for(int j=0;j<ad.length;++j) msgTo+=mailManager.getHTMLDecodedAddress(ad[j])+" ";
          }
          ad=xmsg.getRecipients(Message.RecipientType.CC);
          msgCc=null;
          if (ad!=null) {
            msgCc="";
            for(int j=0;j<ad.length;++j) msgCc+=mailManager.getHTMLDecodedAddress(ad[j])+" ";
          }

          xhtml.append("<html><head><meta content='text/html; charset=utf-8' http-equiv='Content-Type'></head><body>");
          xhtml.append("<font face='Arial, Helvetica, sans-serif' size=2><BR>");
          xhtml.append("<B>"+mailManager.lookupResource(MailLocaleKey.MSG_FROMTITLE)+":</B> "+msgFrom+"<BR>");
          if (msgTo!=null) xhtml.append("<B>"+mailManager.lookupResource(MailLocaleKey.MSG_TOTITLE)+":</B> "+msgTo+"<BR>");
          if (msgCc!=null) xhtml.append("<B>"+mailManager.lookupResource(MailLocaleKey.MSG_CCTITLE)+":</B> "+msgCc+"<BR>");
          xhtml.append("<B>"+mailManager.lookupResource(MailLocaleKey.MSG_DATETITLE)+":</B> "+msgDate+"<BR>");
          xhtml.append("<B>"+mailManager.lookupResource(MailLocaleKey.MSG_SUBJECTTITLE)+":</B> "+msgSubject+"<BR>");
          xhtml.append("</font><br></body></html>");
          htmlparts.add(new HTMLPart(xhtml.toString()));
        }

      }
      return htmlparts;
  }
	
	public synchronized HTMLMailData prepareHTMLMailData(MimeMessage msg) throws MessagingException, IOException {
		return new HTMLMailData(msg,this);
	}

/*
  public synchronized HTMLMailData prepareHTMLMailData(MimeMessage msg) throws MessagingException, IOException {
    HTMLMailData mailData=new HTMLMailData(msg,this);

    prepareHTMLMailData(msg, mailData,0);
	if (mailData.getDisplayPartCount()==0 && mailData.getAttachmentPartCount()>0) {
		Part part=mailData.getAttachmentPart(0);
		if (part.isMimeType("text/plain")||part.isMimeType("text/html")||part.isMimeType("message/delivery-status"))
			mailData.addDisplayPart(part,0);
	}
    return mailData;

  }
  
  public void prepareHTMLMailData(Part msg, HTMLMailData mailData, int level) throws MessagingException, IOException {
    if(msg.isMimeType("text/plain")||msg.isMimeType("text/html")||msg.isMimeType("message/delivery-status")||msg.isMimeType("message/disposition-notification")) {
      if (msg.getDisposition()==null || msg.getDisposition().equalsIgnoreCase(Part.INLINE))
        mailData.addDisplayPart(msg,level);
      else mailData.addAttachmentPart(msg,level);
    } else if(msg.isMimeType("text/calendar")||msg.isMimeType("application/ics")) {
		mailData.addDisplayPart(msg,level);
    } else if(msg.isMimeType("message/rfc822")) {
      mailData.addDisplayPart(msg,level);
      prepareHTMLMailData((Message)msg.getContent(), mailData,level+1);
    } else if (msg.isMimeType("application/ms-tnef")) {
      try {
        TnefMultipartDataSource tnefDS = new TnefMultipartDataSource((MimePart)msg);
        TnefMultipart tnefmp=new TnefMultipart(tnefDS);
        int tnefparts=tnefmp.getCount();
        Part tnefp=null;
        for(int j=0; j<tnefparts; ++j) {
          tnefp=tnefmp.getBodyPart(j);
          prepareHTMLMailData(tnefp,mailData,level);
        }
		
		
        //Part tnefp=net.freeutils.tnef.mime.TNEFMime.convert(this.ms.getMailSession(), (Part)msg, false);
        //if (tnefp instanceof Multipart) {
        //    Multipart tnefmp=(Multipart)tnefp;
        //    int tnefparts=tnefmp.getCount();
        //    for(int j=0; j<tnefparts; ++j) {
        //      tnefp=tnefmp.getBodyPart(j);
        //      prepareHTMLMailData(tnefp,mailData);
        //    }
        //}
        //else prepareHTMLMailData(tnefp,mailData);
      } catch(Exception exc) {
        Service.logger.error("Exception",exc);
        mailData.addUnknownPart(msg,level);
        mailData.addAttachmentPart(msg,level);
      }
      
    } else if (msg.isMimeType("application/pkcs7-signature")||msg.isMimeType("application/x-pkcs7-signature")) {
        mailData.addUnknownPart(msg,level);
        mailData.addAttachmentPart(msg,level);
    } else if(msg.isMimeType("multipart/alternative")) {
      Part ap=getAlternativePart((Multipart)msg.getContent(),mailData,level);
      if(ap!=null) {
        mailData.addDisplayPart(ap,level);
      }
    } else if(msg.isMimeType("multipart/*")) {
      // Display the text content of the multipart message
      Multipart mp=(Multipart)msg.getContent();
      int parts=mp.getCount();
      Part p=null;
      for(int i=0; i<parts; ++i) {
        p=mp.getBodyPart(i);
        if(p.isMimeType("multipart/alternative")) {
          Part ap=getAlternativePart((Multipart)p.getContent(),mailData,level);
          if(ap!=null) {
			if (ap.isMimeType("text/calendar") || ap.isMimeType("application/ics")|| ap.getDisposition()==null || ap.getDisposition().equalsIgnoreCase(Part.INLINE))
                mailData.addDisplayPart(ap,level);
            else mailData.addAttachmentPart(ap,level);
          }
        } else if(p.isMimeType("multipart/*")) {
          prepareHTMLMailData(p, mailData,level);
        } else if(p.isMimeType("text/html")) {
          if (p.getDisposition()==null || p.getDisposition().equalsIgnoreCase(Part.INLINE))
            //if (!mailData.isPEC())
			  mailData.addDisplayPart(p,level);
          else mailData.addAttachmentPart(p,level);
        } else if(p.isMimeType("text/plain")) {
          if (p.getDisposition()==null || p.getDisposition().equalsIgnoreCase(Part.INLINE))
            mailData.addDisplayPart(p,level);
          else mailData.addAttachmentPart(p,level);
		} else if(p.isMimeType("text/calendar")||p.isMimeType("application/ics")) {
			mailData.addDisplayPart(p,level);
        } else if(p.isMimeType("message/delivery-status")||p.isMimeType("message/disposition-notification")) {
          if (p.getDisposition()==null || p.getDisposition().equalsIgnoreCase(Part.INLINE)) 
              mailData.addDisplayPart(p,level);
          else mailData.addAttachmentPart(p,level);
        } else if(p.isMimeType("message/rfc822")) {
            int newlevel=level;
          if (!mailData.isPEC() && (p.getDisposition()==null || p.getDisposition().equalsIgnoreCase(Part.INLINE)))
              mailData.addDisplayPart(p,level);
          else {
              mailData.addAttachmentPart(p,level);
              ++newlevel;
          }
          prepareHTMLMailData((Message)p.getContent(), mailData,newlevel);
        } else if (p.isMimeType("application/ms-tnef")) {
          try {
            TnefMultipartDataSource tnefDS = new TnefMultipartDataSource((MimePart)p);
            TnefMultipart tnefmp=new TnefMultipart(tnefDS);
            int tnefparts=tnefmp.getCount();
            Part tnefp=null;
            for(int j=0; j<tnefparts; ++j) {
              tnefp=tnefmp.getBodyPart(j);
              prepareHTMLMailData(tnefp,mailData,level);
            }

            //Part tnefp=net.freeutils.tnef.mime.TNEFMime.convert(this.ms.getMailSession(), (Part) p, false);
            //if (tnefp instanceof Multipart) {
            //    Multipart tnefmp=(Multipart)tnefp;
            //    int tnefparts=tnefmp.getCount();
            //    for(int j=0; j<tnefparts; ++j) {
            //      tnefp=tnefmp.getBodyPart(j);
            //      prepareHTMLMailData(tnefp,mailData);
            //    }
            //}
            else prepareHTMLMailData(tnefp,mailData);
          } catch(Exception exc) {
            mailData.addUnknownPart(p,level);
            mailData.addAttachmentPart(p,level);
            Service.logger.error("Exception",exc);
          }
        } else if (p.isMimeType("application/pkcs7-signature")||p.isMimeType("application/x-pkcs7-signature")) {
		mailData.addUnknownPart(p,level);
		mailData.addAttachmentPart(p,level);
        } else {
          mailData.addUnknownPart(p,level);
          mailData.addAttachmentPart(p,level);
	  evaluateCidPart(p, mailData, level);
        }
      }
    } else {
      mailData.addUnknownPart(msg,level);
      mailData.addAttachmentPart(msg,level);
    }
  }
  
  protected void evaluateCidPart(Part p, HTMLMailData mailData, int level) throws MessagingException, IOException {
          //Look for a possible Cid
          String filename=p.getFileName();
          String id[]=p.getHeader("Content-ID");
          if(id!=null||filename!=null) {
            if(id!=null) {
              filename=id[0];
            }
			filename=normalizeCidFileName(filename);
            mailData.addCidPart(filename, p, level);
          }
          //Look for a possible Url copy
          String location[]=p.getHeader("Content-Location");
          if(location!=null) {
            String url="";
            java.io.BufferedReader br=new java.io.BufferedReader(new java.io.StringReader(location[0]));
            String line=null;
            while((line=br.readLine())!=null) {
              url+=line.trim();
            }
            mailData.addUrlPart(url, p,level);
          }
  }
  */

  /*
  class PrepareStatus {
    boolean htmlfound=false;
    boolean textfound=false;
    Part bestPart=null;
  }

  private Part getAlternativePart(Multipart amp, HTMLMailData mailData, int level) throws MessagingException, IOException {
    PrepareStatus status=new PrepareStatus();
    Part dispPart=null;
    for(int x=0; x<amp.getCount(); ++x) {
      Part ap=amp.getBodyPart(x);
      if(ap.isMimeType("multipart/*")) {
	if (ap.isMimeType("multipart/related")) {
		dispPart=getAlternativePart((Multipart)ap.getContent(), mailData, level);
		if (dispPart.isMimeType("text/html")) status.htmlfound=true;
		else if (dispPart.isMimeType("text/plain")) status.textfound=true;
	}
	else prepareHTMLMailData(ap, mailData, level);
      } else if(ap.isMimeType("text/html")) {
        dispPart=ap;
        status.htmlfound=true;
      } else if(ap.isMimeType("text/plain")) {
        if(!status.htmlfound) {
          dispPart=ap;
          status.textfound=true;
        }
      } else if(ap.isMimeType("text/calendar")) {
		mailData.addAttachmentPart(ap,level);
		mailData.addDisplayPart(ap,level);
        if(!status.htmlfound) {
          dispPart=ap;
          //mailData.addUnknownPart(ap);
		  //break;
		}
      } else {
          mailData.addUnknownPart(ap,level);
	  mailData.addAttachmentPart(ap,level);
	  evaluateCidPart(ap, mailData, level);
      }
    }
    return dispPart;
  }
  */


  class HTMLMailParserThread implements Runnable {

    InputStream istream=null;
    String charset=null;
    Object threadLock=null;
    SaxHTMLMailParser saxHTMLMailParser=null;
    String appUrl=null;
    boolean balanceTags=true;
	boolean removeHeadStyle=false;
    
    HTMLMailParserThread(Object tlock,InputStream istream, String charset, String appUrl, long msguid, boolean forEdit, boolean balanceTags, boolean removeHeadStyle) {
        this.threadLock=tlock;
        this.istream=istream;
        this.charset=charset;
        this.appUrl=appUrl;
		this.balanceTags=balanceTags;
		this.removeHeadStyle=removeHeadStyle;
        this.saxHTMLMailParser=new SaxHTMLMailParser(mailManager.getCurrentSecurityToken(),forEdit,msguid);
    }
    
    HTMLMailParserThread(Object tlock,InputStream istream, String charset, String appUrl, String provider, String providerid, boolean balanceTags, boolean removeHeadStyle) {
        this.threadLock=tlock;
        this.istream=istream;
        this.charset=charset;
        this.appUrl=appUrl;
		this.balanceTags=balanceTags;
		this.removeHeadStyle=removeHeadStyle;
        this.saxHTMLMailParser=new SaxHTMLMailParser(mailManager.getCurrentSecurityToken(),provider,providerid);
    }
    
    public void initialize(HTMLMailData mailData, boolean justBody) throws SAXException {
        saxHTMLMailParser.setApplicationURL(appUrl);
        saxHTMLMailParser.initialize(mailData, justBody,removeHeadStyle);        
    }

    public void run() {
      try {
        FolderCache.this.doHTMLMailParse(saxHTMLMailParser,istream,charset,balanceTags);
        synchronized(threadLock) {
          threadLock.wait(60000); //give up after one minute
        }
      } catch(Exception exc) {
        Service.logger.error("Exception",exc);
      }
    }
    
    public BufferedReader getParsedHTML() {
        return saxHTMLMailParser.getParsedHTML();        
    }
    
    private void notifyParserEndOfRead() {
        synchronized(threadLock) {
          threadLock.notifyAll();
        }
        saxHTMLMailParser.release();
    }
    
    public ArrayList<String> getHrefs() {
		return saxHTMLMailParser.getHrefs();
    }

  }

  private void doHTMLMailParse(SaxHTMLMailParser saxHTMLMailParser, InputStream istream, String charset, boolean balanceTags) throws SAXException, IOException {
    HTMLInputStream hstream=new HTMLInputStream(istream);
    XMLReader xmlparser=XMLReaderFactory.createXMLReader("org.cyberneko.html.parsers.SAXParser");
	//XMLReader xmlparser=XMLReaderFactory.createXMLReader("net.sourceforge.htmlunit.cyberneko.parsers.SAXParser");
	xmlparser.setProperty("http://xml.org/sax/properties/lexical-handler", saxHTMLMailParser);
	xmlparser.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
	//xmlparser.setFeature("http://cyberneko.org/html/features/balance-tags", balanceTags);
    xmlparser.setContentHandler(saxHTMLMailParser);
    xmlparser.setErrorHandler(saxHTMLMailParser);
    while(!hstream.isRealEof()) {
      hstream.newDocument();
	  InputStreamReader isr=null;
	  try {
		  isr=charset!=null?new InputStreamReader(hstream,charset):new InputStreamReader(hstream);
	  } catch(java.io.UnsupportedEncodingException exc) {
		  isr=new InputStreamReader(hstream);	
	  }
      xmlparser.parse(new InputSource(isr));
    }
    saxHTMLMailParser.endOfFile();
  }

  private BufferedReader startHTMLMailParser(HTMLMailParserThread parserThread, HTMLMailData mailData, boolean justBody) throws SAXException {
    Thread engine=new Thread(parserThread);
    parserThread.initialize(mailData, justBody);
    engine.start();
    return parserThread.getParsedHTML();
  }
  
	public boolean isPEC() {
		boolean isPec=false;
		try {
			UserProfileId profileId=mailManager.getTargetProfileId();
			String domainId=profileId.getDomainId();
			if (isUnderSharedFolder()) {
				SharedPrincipal sp=getSharedInboxPrincipal();
				if (sp!=null) profileId=new UserProfileId(domainId, sp.getUserId());
				else profileId=null;
			}
			if (profileId!=null)
				isPec=RunContext.hasRole(profileId, WT.getGroupSidOfPecAccounts(profileId.getDomainId()));
			
		} catch(Throwable t) {
			
		}

		return isPec;
	}

  class MessageSearchResult {
      String pattern;
      String searchfield;
//      ArrayList<MimeMessage> mylist=new ArrayList<MimeMessage>();
      Message msgs[]=null;
	  //SonicleIMAPMessage tmsgs[]=null;
      int sort_by=0;
      boolean ascending=true;
      int sort_group=0;
      boolean groupascending=true;
	  boolean threaded=false;
      //MessageComparator comparator=new MessageComparator(FolderCache.this.ms);
	  ImapQuery imapQuery;
      
	  MessageSearchResult(int sort_by, boolean ascending, int sort_group, boolean groupascending, boolean threaded, ImapQuery imapQuery) {
          this.sort_by=sort_by;
          this.ascending=ascending;
          this.sort_group=sort_group;
          this.groupascending=groupascending;
		  this.threaded=threaded;
		  this.imapQuery = imapQuery;
      }
      
      void refresh() throws MessagingException, IOException {
          this.cleanup();
		  if (!threaded)
			this.msgs=_getMessages(pattern, searchfield, sort_by, ascending,sort_group,groupascending, imapQuery);
		  else
			this.msgs=_getThreadedMessages(pattern, searchfield, imapQuery);
//          for(Message m: msgs) {
//              String mid=m.getHeader("Message-ID")[0];
//              if (hash.containsKey(mid)) mylist.add(hash.get(mid));
//              else {
//                  //add((MimeMessage)m);
//                  mylist.add((MimeMessage)m);
//              }
//          }
      }
	  
      protected void cleanup() {
          //this.mylist.clear();
          this.msgs=null;
      }

  }
	
	private class MessageChangedHandler implements IdleMailEventHandler {

		@Override
		public void handle(MailEvent event) {
			try {
				refreshUnreads();
				sendFlagsChangedMessage();
			} catch(MessagingException ex) { /* Do nothing... */ }
		}
	}
	
	private class MessagesAddedHandler implements IdleMailEventHandler {
		@Override
		public void handle(MailEvent event) {
			try {
				MessageCountEvent mce = (MessageCountEvent)event;
				//Scan the WHOLE batch but send only ONE 'recent' push for it. Scanning all
				//messages (instead of only the last, as the old code did) means we still notify
				//when the last message isn't RECENT or was already seen - and we mark every new
				//RECENT message as notified. But a single push is enough: it triggers one client
				//grid-refresh + one desktop notification, so a burst of N new messages (e.g. a
				//1000-message initial sync) must NOT become N pushes or the client floods.
				//The latest new message is used for the displayed from/subject.
				Message recentMsg=null;
				for (Message m : mce.getMessages()) {
					String id=((IMAPMessage)m).getMessageID();
					//Do NOT gate on \Recent: with the folder open on several connections at
					//once (dedicated idle + interactive pool + raw/scan) RFC 3501 grants the
					//flag to ONE undefined session - when another one wins it, this handler
					//would silently drop the push and the grid only caught up on the next MFT
					//sweep. messagesAdded on the idling connection already means newly
					//arrived; skip only already-read additions (e.g. drag-in of read mail),
					//the recentNotified dedup (shared with the sweep path) does the rest.
					if (!m.getFlags().contains(Flag.SEEN)) {
						boolean fresh;
						synchronized(recentNotified) {
							fresh=!recentNotified.contains(id);
							if (fresh) recentNotified.add(id);
						}
						if (fresh) recentMsg=m;
					}
				}
				if (recentMsg!=null) {
					String fromName="";
					Address as[]=recentMsg.getFrom();
					if (as!=null && as.length>0) {
						InternetAddress ia=(InternetAddress)as[0];
						fromName = ia.getPersonal();
						String fromEmail = mailManager.adjustEmail(ia.getAddress());
						if (fromName == null) {
							fromName = fromEmail;
						} else {
							fromName = fromName+" <"+fromEmail+">";
						}
					}
					sendRecentMessage(fromName,recentMsg.getSubject());
				}

			} catch(MessagingException ex) { /* Do nothing... */ }
		}
	}
	
	private class MessageCountHandler implements IdleMailEventHandler {
		@Override
		public void handle(MailEvent event) {
			try {
				refreshUnreads();

			} catch(MessagingException ex) { /* Do nothing... */ }
		}
	}

	private class MessagesRemovedHandler implements IdleMailEventHandler {
		@Override
		public void handle(MailEvent event) {
			//external expunge (e.g. another user on a shared folder): push the removed UIDs
			//so the client drops just those rows, mirroring a local delete
			sendMessagesDeletedMessage();
		}
	}
}
