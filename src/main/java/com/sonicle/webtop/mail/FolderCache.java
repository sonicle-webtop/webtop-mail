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

import com.sonicle.webtop.core.app.PrivateEnvironment;
import com.sonicle.commons.MailUtils;
import com.sonicle.mail.imap.*;
import com.sonicle.mail.tnef.internet.*;
import com.sonicle.webtop.core.sdk.*;
import com.sonicle.webtop.mail.ws.UnreadChangedMessage;
import com.sun.mail.imap.*;
import java.io.*;
//import com.sonicle.webtop.util.*;
import java.util.*;
import javax.mail.*;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.*;
import javax.mail.search.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import net.fortuna.ical4j.data.*;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.*;
import net.fortuna.ical4j.model.property.*;


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

    private PrivateEnvironment environment=null;
    //private WebTopDomain wtd=null;
    private Service ms=null;
    private boolean externalProvider=false;
    
    private String foldername=null;
    private Folder folder=null;
    private HashMap<Long, HTMLMailData> dhash=new HashMap<Long, HTMLMailData>();
    private final HashMap<String, MessageSearchResult> msrs=new HashMap<>();
    private Message msgs[]=null;
    private boolean modified=false;
    private boolean forceRefresh=true;
    private int unread=0;
    private int recent=0;
    private boolean hasUnreadChildren=false;
    private boolean unreadChanged=false;
    private boolean recentChanged=false;
    private boolean checkUnreads=true;
    private boolean checkRecents=true;
    private boolean isSharedInbox=false;
    private com.sonicle.security.Principal sharedInboxPrincipal=null;
    private boolean isInbox=false;
    private boolean isRoot=false;
    private boolean isSent=false;
    private boolean isTrash=false;
    private boolean isSpam=false;
    private boolean isDrafts=false;
    private boolean isDocMgt=false;
    private boolean isSharedFolder=false;
    private boolean scanNeverDone=true;
    private boolean scanForcedOff=false;
    private boolean scanForcedOn=false;
    private boolean scanEnabled=false;
    private String description=null;
    private String wtuser=null;
    private ArrayList<String> recentNotified=new ArrayList<>();
    
    private int sort_by=0;
    private boolean ascending=true;
    private int sort_group=0;
    private boolean groupascending=true;
	private boolean threaded=false;
    private MessageComparator comparator;
    private UserProfile profile;
    
    private FolderCache parent=null;
    private ArrayList<FolderCache> children=null;
	private HashMap<String,FolderCache> childrenMap=null;
	
	private HashMap<Long,Integer> openThreads=new HashMap<>();
	private int totalOpenThreadChildren=0;
    
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
    
    private static final HashMap<String,HashMap<String,Integer>> months=new HashMap<>();

    private HashMap<String,MessageEntry> providedMessages=new HashMap<>();
    


    static {
		addHashMonths("en",new String[]{"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"});
		addHashMonths("it",new String[]{"gen", "feb", "mar", "apr", "mag", "giu", "lug", "ago", "set", "ott", "nov", "dic"});
    }

	private static void addHashMonths(String language, String vmonths[]) {
		HashMap<String,Integer> hmonths=new HashMap<>();
		for (int m=0; m<12;++m) {
			hmonths.put(vmonths[m],m+1);
		}
		months.put(language,hmonths);
	}

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
    public FolderCache(Service ms, PrivateEnvironment env) {
        this.ms=ms;
		comparator=new MessageComparator(ms);
        externalProvider=true;
        environment=env;
//        wtd=environment.getWebTopDomain();
        profile=env.getProfile();
    }
    
    public FolderCache(Folder folder, Service ms, PrivateEnvironment env) throws MessagingException {
        this(ms,env);
        foldername=folder.getFullName();
        this.folder=folder;
        String shortfoldername=ms.getShortFolderName(foldername);
        isInbox=ms.isInboxFolder(foldername);
        isSent=ms.isSentFolder(shortfoldername);
        isDrafts=ms.isDraftsFolder(shortfoldername);
        isTrash=ms.isTrashFolder(shortfoldername);
        isSpam=ms.isSpamFolder(shortfoldername);
        isDocMgt=ms.isDocMgtFolder(shortfoldername);
        isSharedFolder=ms.isSharedFolder(foldername);
        if (isDrafts||isSent||isTrash||isSpam) {
            setCheckUnreads(false);
            setCheckRecents(false);
        }

        isSharedInbox=false;
		if (ms.hasDifferentDefaultFolder() && ms.isDefaultFolder(foldername)) {
			
		}
		else if (ms.isUnderSharedFolder(foldername)) {
			char sep=ms.getFolderSeparator();
            int ix=foldername.indexOf(sep);
            String subname=foldername.substring(ix+1);
			int isep=subname.indexOf(sep);
            if (isep<0) {
                isSharedInbox=true;
                sharedInboxPrincipal=ms.getPrincipal(environment.getProfile().getDomainId(),subname);
				//Cyrus has shared/user = inbox
				//Dovecot has shared/user no messages, then INBOX under
				if ((folder.getType()&IMAPFolder.HOLDS_MESSAGES)==0) {
					isSharedInbox=false;
					isSharedFolder=true;
				}
            } else { //look for a possible INBOX under a shared folder
				FolderCache fcparent=ms.getFolderCache(folder.getParent().getFullName());
				String fname=folder.getName();
				if (fcparent.isSharedFolder && fname.equals("INBOX"))
					isSharedInbox=true;
			}
        }
        if (sharedInboxPrincipal==null) description=ms.getInternationalFolderName(this);
        else {
            description=sharedInboxPrincipal.getDescription();
            wtuser=sharedInboxPrincipal.getSubjectId();
        }
        updateScanFlags();
		if (isInbox) startIdle();
    }
	
	boolean goidle=true;
	class IdleThread extends Thread {
		@Override
		public void run() {
			//MailService.logger.debug("Starting idle thread");
			try {
				while(goidle) {
					IMAPFolder folder=((IMAPFolder)FolderCache.this.getFolder());
					if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
					//Service.logger.debug("Entering idle mode on {}",foldername);
					folder.idle();
					//Service.logger.debug("Exiting idle mode on {}",foldername);
				}
				Service.logger.debug("Exiting idle loop: goidle is {}",goidle);
			} catch(MessagingException exc) {
				Service.logger.debug("Error during idle",exc);
			}
		}
	}
	
	public void startIdle() {
		folder.addMessageChangedListener(
				new MessageChangedListener() {

					@Override
					public void messageChanged(MessageChangedEvent mce) {
						try {
							//Service.logger.debug("MessageChanged: {},{},{}",mce.getMessage().getFolder().getFullName(),mce.getMessage().getSubject(),mce.getMessageChangeType());
							refreshUnreads();
						} catch(MessagingException exc) {
						}
					}
					
				}
		);
		folder.addMessageCountListener(
				new MessageCountListener() {

					@Override
					public void messagesAdded(MessageCountEvent mce) {
						try {
							//Service.logger.debug("MessageAdded: {}",mce.getType());
							refreshUnreads();
						} catch(MessagingException exc) {
						}
					}

					@Override
					public void messagesRemoved(MessageCountEvent mce) {
						try {
							//Service.logger.debug("MessageRemoved: {}",mce.getType());
							refreshUnreads();
						} catch(MessagingException exc) {
						}
					}
					
				}
		);
		IdleThread ithread=new IdleThread();
		ithread.start();
	}
    
    protected void setStartupLeaf(boolean b) {
        startupLeaf=b;
    }
    
    protected boolean isStartupLeaf() {
        return startupLeaf;
    }

    public void updateScanFlags() {
        String sfname=ms.getShortFolderName(foldername);
        if (isInbox || isSharedInbox) {
            setScanForcedOn(true);
            setScanForcedOff(false);
        }
        else if (ms.isSpecialFolder(sfname)) {
            setScanForcedOn(false);
            //setScanForcedOff(true);
			setScanForcedOff(false);
        }
        else {
            setScanForcedOn(ms.checkFileRules(foldername));
            setScanForcedOff(false); 
        }
        setScanEnabled(ms.checkScanRules(foldername));
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

    public boolean isDocMgt() {
        return isDocMgt;
    }

    public boolean isSpecial() {
        return isSent||isDrafts||isTrash||isSpam||isDocMgt;
    }

    public boolean isSharedFolder() {
        return isSharedFolder;
    }

    public boolean isSharedInbox() {
        return isSharedInbox;
    }

    public com.sonicle.security.Principal getSharedInboxPrincipal() {
        return sharedInboxPrincipal;
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
    }

    public boolean isScanEnabled() {
        return scanEnabled;
    }
	
	public boolean isScanForcedOrEnabled() {
		return (!isScanForcedOff() && isScanEnabled())||isScanForcedOn();
	}
	
	public boolean hasChildWithScanForcedOrEnabled() {
		boolean retval=false;
		
		if (children!=null) {
			//look for a possible direct child with scan enabled
			for (FolderCache child: children) {
				retval=child.isScanForcedOrEnabled();
				if (retval) break;
			}
			if (!retval) {
				//look in subchildren
				for (FolderCache child: children) {
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
		//Service.logger.debug("sending unread changed on "+foldername);
		this.environment.notify(
				new UnreadChangedMessage(foldername, unread, hasUnreadChildren)
		);
	}

    protected void refreshUnreads() throws MessagingException {
        refreshUnreadMessagesCount();
        updateUnreads();
    }

    protected void refreshUnreadMessagesCount() throws MessagingException {
        if ((folder.getType() & Folder.HOLDS_MESSAGES)>0) {
            int oldunread=unread;
            if (folder.isOpen()) {
                Message umsgs[]=folder.search(unseenSearchTerm);
                unread=umsgs.length;
            } else unread=folder.getUnreadMessageCount();
			//Service.logger.debug("refreshing count on "+foldername+" oldunread="+oldunread+", unread="+unread);
            if (oldunread!=unread) {
				unreadChanged=true;
				sendUnreadChangedMessage();
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
            recent=0;
            for(Message m: umsgs) {
                IMAPMessage im=(IMAPMessage)m;
                String id=im.getMessageID();
//                if (isInbox) {
//                    ++recent;
//                } else {
                    if (!recentNotified.contains(id)) {
                        ++recent;
                        recentNotified.add(id);
                    }
//                }
            }
            if (!wasOpen) folder.close(false);
            //if (!(oldrecent==0 && recent==0)) recentChanged=true;
            if (recent>0) recentChanged=true;
        }
    }

    protected boolean checkSubfolders(boolean all) throws MessagingException {
        boolean pHasUnread=false;
		//Service.logger.debug("working on "+getFolderName());
        for(FolderCache fcchild: getChildren()) {
            if (fcchild.isScanForcedOff()) continue;
            boolean hasUnread=false;
            if (all || fcchild.scanNeverDone || fcchild.isScanForcedOn() || fcchild.isScanEnabled()) {
                fcchild.scanNeverDone=false;
                hasUnread=fcchild.checkFolder();
            }
            if (fcchild.children!=null) {
                hasUnread|=fcchild.checkSubfolders(all);
            }
            fcchild.setHasUnreadChildren(hasUnread);
            pHasUnread|=hasUnread;

        }
        return pHasUnread;
    }
    
    protected boolean checkFolder() {
        try {
            if (checkUnreads || scanForcedOn || scanEnabled) refreshUnreadMessagesCount();
        } catch(MessagingException exc) {
            Service.logger.debug("REFRESH COUNT ERROR ON FOLDER: "+this.foldername+" ("+exc.getMessage()+")");
            Service.logger.error("Exception on folder "+foldername,exc);
        }
        try {
            if (checkRecents || scanForcedOn || scanEnabled) refreshRecentMessagesCount();
        } catch(MessagingException exc) {
            Service.logger.debug("REFRESH RECENT ERROR ON FOLDER: "+this.foldername+" ("+exc.getMessage()+")");
            Service.logger.error("Exception on folder "+foldername,exc);
        }
        return (unread>0);
    }

    private void updateUnreads() {
		boolean oldhuc=hasUnreadChildren;
        if (unread>0) hasUnreadChildren=true;
        else {
            hasUnreadChildren=false;
            if (children!=null) {
                for(FolderCache child: children) {
                    hasUnreadChildren|=(child.unread>0 || child.hasUnreadChildren);
                }
            }
        }
		//if (oldhuc!=hasUnreadChildren)
			sendUnreadChangedMessage();
        if (hasUnreadChildren) {
            FolderCache fcparent=parent;
            while(fcparent!=null) {
                if (fcparent.parent!=null) {
                    fcparent.updateUnreads();
                }
                fcparent=fcparent.parent;
            }
        }
    }
    
    public boolean toBeRefreshed() {
        return forceRefresh;
    }
    
    public void setForceRefresh() {
        this.forceRefresh=true;
    }
    
    public void refresh() throws MessagingException, IOException {
        cleanup(false);
		if (!threaded)
			msgs=_getMessages("", "", "",sort_by,ascending,sort_group,groupascending);
		else
			msgs=_getThreadedMessages("", "", "");
        open();
        //add(msgs);
        modified=false;
        forceRefresh=false;
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
        dhash.remove(id);
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
        for(long uid: uids) {
            dhash.remove(new Long(uid));
         }
     }
	
	public long getUID(Message m) throws MessagingException {
		return ((UIDFolder)folder).getUID(m);
	}
    
    public Message getMessage(long uid) throws MessagingException {
        open();
		return ((UIDFolder)folder).getMessageByUID(uid);
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
	
    public void fetch(Message fmsgs[], FetchProfile fp, int start, int length) throws MessagingException {
        int n=fmsgs.length;
        if (length>(n-start)) length=n-start;
        Message xmsgs[]=new Message[length];
        System.arraycopy(fmsgs, start, xmsgs, 0, length);
        open();
        ((SonicleIMAPFolder)folder).uid_fetch(xmsgs, fp);
    }
    
	public Message[] getMessages(String quickfilter, String pattern, String searchfield, int sort_by, boolean ascending, boolean refresh, int sort_group, boolean groupascending, boolean threaded) throws MessagingException, IOException {
        boolean rebuilt=false;
        boolean sortchanged=false;
        if (pattern==null) pattern="";
        if (searchfield==null) searchfield="";
		if (quickfilter==null) quickfilter="";
        //ArrayList<MimeMessage> xlist=null;
        Message xmsgs[]=null;
        MessageSearchResult msr=null;
        //MessageComparator mcomp=null;
        
        if (!quickfilter.equals("any") || pattern.length()>0) {
            String skey=quickfilter+"."+pattern+"."+searchfield+"."+threaded;
            msr=msrs.get(skey);
            if (msr==null) {
				msr=new MessageSearchResult(quickfilter, pattern,searchfield,sort_by,ascending,sort_group,groupascending,threaded);
                msr.refresh();
                msrs.put(skey, msr);
                rebuilt=true;
            } else {
                if (msr.quickfilter!=quickfilter || msr.sort_by!=sort_by || msr.ascending!=ascending || msr.sort_group!=sort_group || msr.groupascending!=groupascending || msr.threaded!=threaded) {
					msr.quickfilter=quickfilter;
                    msr.sort_by=sort_by;
                    msr.ascending=ascending;
                    msr.sort_group=sort_group;
                    msr.groupascending=groupascending;
					msr.threaded=threaded;
                    sortchanged=true;
                }
                if (refresh || modified || sortchanged) {
                    msr.refresh();
                    rebuilt=true;
                }
            }
            //xlist=msr.mylist;
            xmsgs=msr.msgs;
            //mcomp=msr.comparator;
        } else {
			if (this.sort_by!=sort_by || this.ascending!=ascending || this.sort_group!=sort_group || this.groupascending!=groupascending || this.threaded!=threaded) {
                this.sort_by=sort_by;
                this.ascending=ascending;
                this.sort_group=sort_group;
                this.groupascending=groupascending;
				this.threaded=threaded;
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
	
    protected void cleanup(boolean stopIdleThread) {
		if (stopIdleThread) goidle=false;
        dhash.clear();
//        hash.clear();
//        list.clear();
        unread=0;
        recent=0;
        msgs=null;
    }

    public void close() {
        try { folder.close(true); } catch(Exception exc) {}
        dhash.clear();
    }
    
    public void open() throws MessagingException {
        if(!folder.isOpen()) {
            if((folder.getType()&Folder.HOLDS_MESSAGES)>0) {
              folder.open(Folder.READ_WRITE);
            } else {
              folder.open(Folder.READ_ONLY);
            }
            dhash.clear();
            ms.poolOpened(this);

        }
    }

    public void save(Message msg) throws MessagingException {
        Message[] saveMsgs=new MimeMessage[1];
        saveMsgs[0]=msg;

        getFolder().appendMessages(saveMsgs);
        setForceRefresh();
    }
    
    private Message[] getMessages(long uids[]) throws MessagingException {
        open();
        return ((UIDFolder)folder).getMessagesByUID(uids);
    }

    protected Message[] getAllMessages() throws MessagingException {
        open();
        return ((UIDFolder)folder).getMessagesByUID(1,UIDFolder.LASTUID);
    }
    
    public void moveMessages(long uids[], FolderCache to) throws MessagingException {
        Message mmsgs[]=getMessages(uids);
        folder.copyMessages(mmsgs, to.folder);
        folder.setFlags(mmsgs, new Flags(Flags.Flag.DELETED), true);
        removeDHash(uids);
        folder.expunge();
        setForceRefresh();
        to.setForceRefresh();
        modified=true;
        to.modified=true;
    }

    public void copyMessages(long uids[], FolderCache to) throws MessagingException, IOException {
		
        if (ms.hasDocumentArchiving() &&
                ms.isSimpleArchiving() &&
                ms.getSimpleArchivingMailFolder()!=null &&
                ms.getSimpleArchivingMailFolder().equals(to.foldername)) {
            archiveMessages(uids, to);
        } else {
            Message mmsgs[]=getMessages(uids);
            folder.copyMessages(mmsgs, to.folder);
            to.setForceRefresh();
            to.modified=true;
        }
    }

    public void archiveMessages(long uids[], FolderCache to) throws MessagingException, IOException {
        Message mmsgs[]=getMessages(uids);
        MimeMessage newmmsgs[]=getArchivedCopy(mmsgs);
        moveMessages(uids,to);
        folder.appendMessages(newmmsgs);
        refresh();
    }

    public void markArchivedMessages(long uids[]) throws MessagingException, IOException {
        MimeMessage newmmsgs[]=getArchivedCopy(uids);
        try {
			deleteMessages(uids);
			folder.appendMessages(newmmsgs);
		} catch(MessagingException exc) {
			//can't delete, try with flag
			Message msgs[]=getMessages(uids);
			for(Message m: msgs) 
				m.setFlags(Service.flagArchived,true);
		}
        
        refresh();
    }

    public MimeMessage[] getArchivedCopy(long uids[]) throws MessagingException {
        Message mmsgs[]=getMessages(uids);
        return getArchivedCopy(mmsgs);
    }

    public MimeMessage[] getArchivedCopy(Message mmsgs[]) throws MessagingException {
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
	
    public void deleteMessages(long uids[]) throws MessagingException {
        Message mmsgs[]=getMessages(uids);
		_deleteMessages(mmsgs);
        removeDHash(uids);
    }
	
	private void _deleteMessages(Message mmsgs[]) throws MessagingException {
        for(Message dmsg: mmsgs) {
          dmsg.setFlag(Flags.Flag.DELETED, true);
        }
        folder.expunge();
        setForceRefresh();
        modified=true;
    }

    public void flagMessages(long uids[], String flag) throws MessagingException {
//        open();
        Message mmsgs[]=getMessages(uids);
        for(Message fmsg: mmsgs) {
			if (flag.equals("special")) {
				boolean wasspecial=fmsg.getFlags().contains(Service.flagFlagged);
				fmsg.setFlags(Service.flagFlagged,!wasspecial);
			}
			else {
				if (!flag.equals("complete")) {
					fmsg.setFlags(Service.flagsAll, false);
					fmsg.setFlags(Service.oldFlagsAll, false);
					fmsg.setFlags(Service.tbFlagsAll, false);
				}
				fmsg.setFlags(Service.flagsHash.get(flag), true);
				Flags tbFlags=Service.tbFlagsHash.get(flag);
				if (tbFlags!=null) fmsg.setFlags(tbFlags, true);
			}
            
        }
    }
  
    public void clearMessagesFlag(long uids[]) throws MessagingException {
//        open();
        Message mmsgs[]=getMessages(uids);
        for(Message fmsg: mmsgs) {
            fmsg.setFlags(Service.flagsAll, false);
            fmsg.setFlags(Service.oldFlagsAll, false);
            fmsg.setFlags(Service.tbFlagsAll, false);
			//fmsg.setFlags(Service.flagFlagged,false);
        }
    }

    public void setMessagesSeen(long uids[]) throws MessagingException {
        Message mmsgs[]=getMessages(uids);
        int changed=setMessagesSeen(mmsgs,true);
        if (changed>0) {
            unread-=changed;
            updateUnreads();
        }
    }
    
    public void setMessagesUnseen(long uids[]) throws MessagingException {
        Message mmsgs[]=getMessages(uids);
        int changed=setMessagesSeen(mmsgs,false);
        if (changed>0) {
            unread+=changed;
            updateUnreads();
        }
    }

    public void setMessagesSeen() throws MessagingException {
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
            updateUnreads();
        }
        if (!wasOpen) folder.close(true);
    }

    public void setMessagesUnseen() throws MessagingException {
        try {
            open();
        } catch(Exception exc) {
            return;
        }
        int n=folder.getMessageCount();
        Message umsgs[]=folder.search(seenSearchTerm);
        folder.setFlags(umsgs, seenFlags, false);
        unread=n;
        updateUnreads();
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
        if (!ms.hasDifferentDefaultFolder() && isRoot) {
            String prefix=ms.getFolderPrefix();
            if (prefix!=null) name=prefix+name;
            newfolder=ms.getFolder(name);
        } else {
            newfolder=folder.getFolder(name);
        }
        if (newfolder.create(Folder.HOLDS_MESSAGES)) {
            ms.addFoldersCache(this, newfolder);
        }
        else newfolder=null;
        return newfolder;
    }
	
	private SearchTerm _prepareSearchTerm(String quickfilter, String patterns, String searchfields) {
		Locale locale=profile.getLocale();
		SearchTerm term=null;
		ArrayList<SearchTerm> terms=new ArrayList<SearchTerm>();
		if(patterns!=null && patterns.trim().length()>0) {
		  int ixp=0;
		  int ixs=0;
		  while(ixp>=0 && ixs>=0) {
			  int ixp2=patterns.indexOf('|',ixp);
			  int ixs2=searchfields.indexOf('|',ixs);
			  String pattern;
			  String searchfield;
			  if (ixp2>=0 && ixs2>=0) {
				  pattern=patterns.substring(ixp,ixp2);
				  searchfield=searchfields.substring(ixs,ixs2);
			  } else {
				  pattern=patterns.substring(ixp);
				  searchfield=searchfields.substring(ixs);
			  }
			  ixp=ixp2;
			  ixs=ixs2;
			  if (ixp>=0 && ixs>=0) { ++ixp; ++ixs; }
			  if (pattern.trim().length()==0) continue;
			  if (searchfield.equals("any")) {
				SearchTerm anyterms[]=new SearchTerm[6];
				anyterms[0]=new SubjectTerm(pattern);
				anyterms[1]=new RecipientStringTerm(Message.RecipientType.TO, pattern);
				anyterms[2]=new RecipientStringTerm(Message.RecipientType.CC, pattern);
				anyterms[3]=new RecipientStringTerm(Message.RecipientType.BCC, pattern);
				anyterms[4]=new FromStringTerm(pattern);
				anyterms[5]=new BodyTerm(pattern);
				terms.add(new OrTerm(anyterms));
			  } else if(searchfield.equals("subject")) {
				terms.add(new SubjectTerm(pattern));
			  } else if(searchfield.equals("to")) {
				terms.add(new RecipientStringTerm(Message.RecipientType.TO, pattern));
			  } else if(searchfield.equals("cc")) {
				terms.add(new RecipientStringTerm(Message.RecipientType.CC, pattern));
			  } else if(searchfield.equals("bcc")) {
				terms.add(new RecipientStringTerm(Message.RecipientType.BCC, pattern));
			  } else if(searchfield.equals("from")) {
				terms.add(new FromStringTerm(pattern));
			  } else if(searchfield.equals("body")) {
				terms.add(new BodyTerm(pattern));
			  } else if (searchfield.equals("flag")) {
				  terms.add(new FlagTerm(new Flags(pattern),true));
			  } else if (searchfield.equals("status")) {
				  if (pattern.equals("unread")) {
					  terms.add(unseenSearchTerm);
				  } else if (pattern.equals("new")) {
					  terms.add(recentSearchTerm);
				  } else if (pattern.equals("replied")) {
					  terms.add(repliedSearchTerm);
				  } else if (pattern.equals("forwarded")) {
					  terms.add(forwardedSearchTerm);
				  } else if (pattern.equals("read")) {
					  terms.add(seenSearchTerm);
				  }
			  } else if (searchfield.equals("priority")) {
				HeaderTerm p1=new HeaderTerm("X-Priority", "1");
				HeaderTerm p2=new HeaderTerm("X-Priority", "2");
				terms.add(new OrTerm(p1,p2));
			  } else if(searchfield.equals("date")||searchfield.equals("recvdate")||searchfield.equals("sentdate")) {
				pattern=pattern.trim();
				if (searchfield.equals("date")) {
					if (isSent) searchfield="sentdate";
					else searchfield="recvdate";
				}
				if(pattern.length()>0) {
				  int ix=pattern.indexOf(" - ");
				  if(ix<0) { //No range
					java.util.Date date=parseDate(pattern);
					if(date!=null) {
					  if(searchfield.equals("recvdate")) {
						terms.add(new ReceivedDateTerm(DateTerm.EQ, date));
					  } else {
						terms.add(new SentDateTerm(DateTerm.EQ, date));
					  }
					} else { //Check for "month year"
					  ix=pattern.indexOf(" ");
					  int month=-1;
					  int year=-1;
					  if(ix>0) {
						String smonth=pattern.substring(0, ix).toLowerCase();
						month=getMonth(smonth);
						year=Integer.parseInt(pattern.substring(ix+1));
					  } else {
						month=getMonth(pattern);
						if(month==-1) {
						  try {
							year=Integer.parseInt(pattern);
						  } catch(RuntimeException exc) {
							Service.logger.error("Exception",exc);
						  }
						}
					  }
					  if(year==-1) {
						java.util.Calendar c=java.util.Calendar.getInstance();
						c.setTime(new java.util.Date());
						year=c.get(java.util.Calendar.YEAR);
					  }

					  java.util.Calendar c1=java.util.Calendar.getInstance(locale);
					  java.util.Calendar c2=java.util.Calendar.getInstance(locale);
					  if(month==-1) {
						c1.set(year, 0, 1, 0, 0, 0);
						c2.set(year, 11, 31, 23, 59, 59);
					  } else {
						c1.set(year, month-1, 1, 0, 0, 0);
						c2.set(year, month-1, 1, 23, 59, 59);
						int lastday=c2.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
						c2.set(java.util.Calendar.DAY_OF_MONTH, lastday);
					  }
					  java.util.Date date1=c1.getTime();
					  java.util.Date date2=c2.getTime();
					  DateTerm dt1=null;
					  DateTerm dt2=null;
					  if(searchfield.equals("recvdate")) {
						dt1=new ReceivedDateTerm(DateTerm.GE, date1);
					  } else {
						dt1=new SentDateTerm(DateTerm.GE, date1);
					  }
					  if(searchfield.equals("recvdate")) {
						dt2=new ReceivedDateTerm(DateTerm.LE, date2);
					  } else {
						dt2=new SentDateTerm(DateTerm.LE, date2);
					  }
					  terms.add(new AndTerm(dt1, dt2));
					}
				  } else { //range
					String p1=pattern.substring(0, ix).trim();
					String p2=pattern.substring(ix+3).trim();
					java.util.Date date1=parseDate(p1);
					java.util.Date date2=parseDate(p2);
					if(date1!=null&&date2!=null) {
					  if(date1.after(date2)) { //Swap if wrong
						java.util.Date xdate=date1;
						date1=date2;
						date2=xdate;
					  }
					  DateTerm dt1=null;
					  DateTerm dt2=null;
					  if(searchfield.equals("recvdate")) {
						dt1=new ReceivedDateTerm(DateTerm.GE, date1);
					  } else {
						dt1=new SentDateTerm(DateTerm.GE, date1);
					  }
					  if(searchfield.equals("recvdate")) {
						dt2=new ReceivedDateTerm(DateTerm.LE, date2);
					  } else {
						dt2=new SentDateTerm(DateTerm.LE, date2);
					  }
					  terms.add(new AndTerm(dt1, dt2));
					}

				  }
				}
			  }
		  }
		}
		if (quickfilter!=null) {
			if (quickfilter.equals("unread")) {
			  terms.add(new FlagTerm(new Flags(Flag.SEEN),false));
			} 
			else if (quickfilter.equals("flagged")) {
				FlagTerm fts[]=new FlagTerm[ms.allFlagStrings.length+1];
				fts[0]=new FlagTerm(new Flags(Flag.FLAGGED),true);
				for(int i=0;i<ms.allFlagStrings.length;++i)
					fts[i+1]=new FlagTerm(new Flags(ms.allFlagStrings[i]),true);
				terms.add(new OrTerm(fts));
			}
			else if (quickfilter.equals("unanswered")) {
			  terms.add(new FlagTerm(new Flags(Flag.ANSWERED),false));
			}
			else if (quickfilter.equals("priority")) {
			  HeaderTerm p1=new HeaderTerm("X-Priority", "1");
			  HeaderTerm p2=new HeaderTerm("X-Priority", "2");
			  terms.add(new OrTerm(p1,p2));
			}
		}

		int n=terms.size();
		if (n==1) {
			term=terms.get(0);
		}
		else if (n>1) {
			SearchTerm vterms[]=new SearchTerm[n];
			terms.toArray(vterms);
			term=new AndTerm(vterms);
		}
		return term;
	}
	
	private SonicleSortTerm _prepareSortTerm(int sort_by, boolean ascending, int sort_group, boolean groupascending) {
		SonicleSortTerm gsort=null;
      
		switch(sort_group) {
			case SORT_BY_DATE:
				gsort=new DateSortTerm(!groupascending);
				break;
			case SORT_BY_FLAG:
				//<SonicleMail>sort=new UserFlagSortTerm(MailService.flagStrings, !ascending);</SonicleMail>
				gsort=new FlagSortTerm(ms.allFlagStrings, !groupascending);
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
				sort=new DateSortTerm(!ascending);
				break;
			case SORT_BY_FLAG:
				//<SonicleMail>sort=new UserFlagSortTerm(MailService.flagStrings, !ascending);</SonicleMail>
				sort=new FlagSortTerm(ms.allFlagStrings, !ascending);
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
	
	private Message[] _getMessages(String quickfilter, String patterns, String searchfields, int sort_by, boolean ascending, int sort_group, boolean groupascending) throws MessagingException, IOException {

		Message[] xmsgs=null;
		open();

		if((folder.getType()&Folder.HOLDS_MESSAGES)>0) {
			SearchTerm term=_prepareSearchTerm(quickfilter, patterns,searchfields);
			SonicleSortTerm sort=_prepareSortTerm(sort_by,ascending,sort_group,groupascending);
			open();

			//<SonicleMail>xmsgs=((IMAPFolder)folder).sort(sort, term);</SonicleMail>
			try {
				//xmsgs=((SonicleIMAPFolder)folder).uid_sort(sort, term);
				xmsgs=((SonicleIMAPFolder)folder).sort(sort, term);
			} catch(Exception exc) {
				close();
				open();
				//xmsgs=((SonicleIMAPFolder)folder).uid_sort(sort, term);
				xmsgs=((SonicleIMAPFolder)folder).sort(sort, term);
			}
		}
		if (quickfilter!=null && quickfilter.equals("attachment")) {
			ArrayList<Message> amsgs=new ArrayList<Message>();
			for(Message m: xmsgs) {
				if (hasAttachements(m)) amsgs.add(m);
			}
			xmsgs=new Message[amsgs.size()];
			amsgs.toArray(xmsgs);
		}

		return xmsgs;
	}
	
	private SonicleIMAPMessage[] _getThreadedMessages(String quickfilter, String patterns, String searchfields) throws MessagingException {
		SonicleIMAPMessage[] tmsgs=null;
		open();

		if((folder.getType()&Folder.HOLDS_MESSAGES)>0) {

			SearchTerm term=_prepareSearchTerm(quickfilter, patterns,searchfields);
			
			open();
			
			boolean hasrefs=((IMAPStore)folder.getStore()).hasCapability("THREAD=REFS");
			String method=hasrefs?"REFS":"REFERENCES";
			FetchProfile fp=ms.getMessageFetchProfile();
			try {
				tmsgs=((SonicleIMAPFolder)folder).thread(method,term,fp);
			} catch(Exception exc) {
				Service.logger.debug("**************Retrying thread*********************");
				close();
				open();
				tmsgs=((SonicleIMAPFolder)folder).thread(method,term,fp);
			}
		}
		
		return tmsgs;
	}

	private boolean isAttachment(Part part) throws MessagingException {
		return Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) 
				|| Part.INLINE.equalsIgnoreCase(part.getDisposition()) 
				|| (part.getDisposition() == null && part.getFileName() != null);
	}
    
    private boolean hasAttachements(Part p) throws MessagingException, IOException {
        boolean retval=false;
        
        //String disp=p.getDisposition();
		if (isAttachment(p)) retval=true;
		//if (disp!=null && disp.equalsIgnoreCase(Part.ATTACHMENT)) retval=true;
        else if(p.isMimeType("multipart/*")) {
            Multipart mp=(Multipart)p.getContent();
            int parts=mp.getCount();
            for(int i=0;i<parts;++i) {
                Part bp=mp.getBodyPart(i);
                if (hasAttachements(bp)) {
                    retval=true;
                    break;
                }
            }
        }
        
        return retval;
    }

	public Message[] getMessagesByMessageId(String id) throws MessagingException {
		boolean wasOpen=folder.isOpen();
		if (!wasOpen) folder.open(Folder.READ_WRITE);
		Message msgs[]=folder.search(new HeaderTerm("Message-ID", id));
		if (!wasOpen) folder.close(false);
		return msgs;
	}

  protected Message[] advancedSearchMessages(AdvancedSearchEntry entries[], boolean and, int sort_by, boolean ascending) throws MessagingException {

    Locale locale=profile.getLocale();
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
            if(searchfield.equals("subject")) {
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
            } else if (searchfield.equals("status")) {
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
              sort=new DateSortTerm(!ascending);
              break;
          case SORT_BY_FLAG:
              //<SonicleMail>sort=new UserFlagSortTerm(MailService.flagStrings, !ascending);</SonicleMail>
			  sort=new FlagSortTerm(ms.allFlagStrings, !ascending);
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



  private int getMonth(String smonth) {
    if(smonth.length()<3) {
      return-1;
    }
    String language=profile.getLocale().getLanguage().toLowerCase();
    HashMap<String,Integer> hash=months.get(language);
    if(hash==null) {
      return -1;
    }
    Integer imonth=(Integer)hash.get(smonth.substring(0, 3).toLowerCase());
    if(imonth==null) {
      return-1;
    }
    return imonth;
  }

  private java.util.Date parseDate(String pattern) {
    pattern=pattern.replace('-', '/');
    java.util.Date date=null;
    try {
      date=java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT, profile.getLocale()).parse(pattern);
    } catch(Exception exc) {}
    if(date==null) {
      try {
        date=java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, profile.getLocale()).parse(pattern);
      } catch(Exception exc) {}
    }
    if(date==null) {
      try {
        date=java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG, profile.getLocale()).parse(pattern);
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

    void addChild(FolderCache fc) {
        if (children==null) children=new ArrayList<>();
		if (childrenMap==null) childrenMap=new HashMap<String,FolderCache>();
        children.add(fc);
		childrenMap.put(fc.foldername, fc);
    }

    void removeChild(FolderCache fc) {
        if (children!=null) children.remove(fc);
		if (childrenMap!=null) childrenMap.remove(fc.foldername);
        if (children!=null && children.size()==0) children=null;
    }
    
	public boolean hasChild(String name) {
		if (childrenMap==null) return false;
		return childrenMap.containsKey(name);
	}

    public HTMLMailData getMailData(MimeMessage m) throws MessagingException, IOException {
        HTMLMailData mailData=null;
        synchronized(this) {
			long muid=-1;
			if (m instanceof SonicleIMAPMessage) {
				muid=((SonicleIMAPMessage)m).getUID();
				mailData=dhash.get(muid);
				if (mailData!=null && mailData.getMessage()!=m) {
					Service.logger.debug("found wrong cached message, refreshing");
					mailData=null;
				}
			}
            if (mailData==null) {
                mailData=prepareHTMLMailData(m);
                if (muid>0) dhash.put(muid, mailData);
            }
        }
        return mailData;
    }
	
	public synchronized void setThreadOpen(long uid, boolean open) throws MessagingException {
		int children=((SonicleIMAPMessage)getMessage(uid)).getThreadChildren();
		if (open) {
			if (!openThreads.containsKey(uid)) {
				totalOpenThreadChildren+=children;
				openThreads.put(uid, children);
			}
		}
		else {
			if (openThreads.containsKey(uid)) {
				totalOpenThreadChildren-=children;
				openThreads.remove(uid);
			}
		}
	}
	
	public boolean isThreadOpen(long uid) {
		Integer children=openThreads.get(uid);
		return children!=null;
	}
	
	public int getThreadedCount() {
		int threadRoots=((SonicleIMAPFolder)folder).getThreadRoots();
		return threadRoots+totalOpenThreadChildren;
	}
    
    public ArrayList<String> getHTMLParts(MimeMessage m, long msguid, boolean forEdit) throws MessagingException, IOException {
        return getHTMLParts(m, msguid, null, null, forEdit);
    }
    
    public ArrayList<String> getHTMLParts(MimeMessage m, String provider, String providerid) throws MessagingException, IOException {
        return getHTMLParts(m, -1, provider, providerid, false);
    }
    
    private ArrayList<String> getHTMLParts(MimeMessage m, long msguid, String provider, String providerid, boolean forEdit) throws MessagingException, IOException {
      ArrayList<String> htmlparts=new ArrayList<>();
      //WebTopApp webtopapp=environment.getWebTopApp();
      //Session wts=environment.get();
      UserProfile profile=environment.getProfile();
      HTMLMailData mailData=getMailData(m);
      int objid=0;
      Part msgPart=null;
      String msgSubject;
      String msgFrom;
      String msgDate;
      String msgTo;
      String msgCc;
      Locale locale=profile.getLocale();
	  boolean icalhtmlview=false;
      for(int i=0;i<mailData.getDisplayPartCount();++i) {
        Part dispPart=mailData.getDisplayPart(i);
        java.io.InputStream istream=null;
        String charset=MailUtils.getCharsetOrDefault(dispPart.getContentType());
//        boolean ischarset=false;
//        try { ischarset=java.nio.charset.Charset.isSupported(charset); } catch(Exception exc) {}
//        if (!ischarset) charset="UTF-8";
		if (dispPart.isMimeType("text/plain")||dispPart.isMimeType("text/html")||dispPart.isMimeType("message/delivery-status")||dispPart.isMimeType("message/disposition-notification")||dispPart.isMimeType("text/calendar")||dispPart.isMimeType("application/ics")) {
            try {
              if (dispPart instanceof javax.mail.internet.MimeMessage) {
                javax.mail.internet.MimeMessage mm=(javax.mail.internet.MimeMessage)dispPart;
                istream=mm.getInputStream();
              } else if (dispPart instanceof javax.mail.internet.MimeBodyPart) {
                javax.mail.internet.MimeBodyPart mm=(javax.mail.internet.MimeBodyPart)dispPart;
                istream=mm.getInputStream();
              }
            } catch(Exception exc) { //unhandled format, get Raw data
              if (dispPart instanceof javax.mail.internet.MimeMessage) {
                javax.mail.internet.MimeMessage mm=(javax.mail.internet.MimeMessage)dispPart;
                istream=mm.getRawInputStream();
              } else if (dispPart instanceof javax.mail.internet.MimeBodyPart) {
                javax.mail.internet.MimeBodyPart mm=(javax.mail.internet.MimeBodyPart)dispPart;
                istream=mm.getRawInputStream();
              }
            }


            if (istream==null) throw new IOException("Unknown message class "+dispPart.getClass().getName());


            StringBuffer xhtml=new StringBuffer();
            if (dispPart.isMimeType("text/html")) {
                Object tlock=new Object();
                String uri=environment.getSessionRefererUri();
                HTMLMailParserThread parserThread=null;
                if (provider==null) parserThread=new HTMLMailParserThread(tlock, istream, charset, uri, msguid, forEdit);
                else parserThread=new HTMLMailParserThread(tlock, istream, charset, uri, provider, providerid);
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

                htmlparts.add(xhtml.toString());
                //String key="htmlpart"+objid;
                //controller.putTempData(key,html);
			} else if (dispPart.isMimeType("text/calendar")||dispPart.isMimeType("application/ics")) {
				if (dispPart.getContentType().contains("method=")) {
					try {
						String laf="default"; //TODO: get it from user setting
						ICalendarRequest ir=new ICalendarRequest(istream);
						mailData.setICalRequest(ir);
						if (!icalhtmlview) {
							String irhtml=ir.getHtmlView(locale,laf,java.util.ResourceBundle.getBundle("com/sonicle/webtop/mail/locale", locale));
							htmlparts.add(0,irhtml);
							icalhtmlview=true;
						}
						if (!mailData.hasICalAttachment()) mailData.addAttachmentPart(dispPart);
					} catch(ParserException exc) {
						mailData.addAttachmentPart(dispPart);
						//MailService.logger.error("Error parsing calendar part",exc);
					}
				} else {
					mailData.addAttachmentPart(dispPart);
				}
            } else {
                xhtml.append("<html><head><meta content='text/html; charset=utf-8' http-equiv='Content-Type'></head><body><tt>");
                String line=null;
                java.io.BufferedReader br=new java.io.BufferedReader(new java.io.InputStreamReader(istream,charset));
                while((line=br.readLine())!=null) {
                    while(true) {
                      String token=null;
                      boolean ismail=false;
                      int x=line.indexOf((token="http://"));
					  if (x==-1) x=line.indexOf((token="https://"));
                      if (x==-1) x=line.indexOf((token="ftp://"));
                      if (x==-1) {
                          x=line.indexOf((token="www."));
                          if (x==(line.length()-1) || Character.isSpaceChar(line.charAt(x+1))) {
                            x=-1;
                            token=null;
                          }
                      }
                      if (x==-1) {
                        x=line.indexOf((token="@"));
                        int atx=x;
                        --x;
                        while(x>=0) {
                          char ch=line.charAt(x);
                          if (Character.isLetterOrDigit(ch) || ch=='.' || ch=='-' || ch=='_') {
                            --x;
                            if (x<0) {
                              x=0;
                              break;
                            }
                          } else {
                            ++x;
                            break;
                          }
                        }
                        if (atx==x) x=-1; //nothing before @
                        if (x>=0) ismail=true;
                      }
                      if (token!=null && x>=0) {
                        xhtml.append(MailUtils.htmlescape(line.substring(0,x)));
                        int y=0;
                        if (ismail) {
                          int ats=0;
                          for(int c=x+1;c<line.length();++c) {
                            char ch=line.charAt(c);
                            if (ats==0 && ch=='@') ++ats;
                            else if (Character.isSpaceChar(ch) || (ch!='.' && ch!='-' && !Character.isLetterOrDigit(ch))) {
                              y=c;
                              break;
                            }
                          }
                        } else {
                          for(int c=x+token.length();c<line.length();++c) {
                            char ch=line.charAt(c);
                            if (Character.isSpaceChar(ch) || (ch!='.' && ch!='-' && ch!='_' && ch!=':' && ch!='/' && ch!='?' && ch!='=' && ch!='@' && ch!='+' && ch!='&' && ch!='%' && !Character.isLetterOrDigit(ch))) {
                              y=c;
                              break;
                            }
                          }
                        }
                        if (y>0) {
                          token=line.substring(x,y);
                          line=line.substring(y);
                        } else {
                          token=line.substring(x);
                          line=null;
                        }
                        String href=token;
                        String onclick="";
                    //                if (ismail) {
                    //                  href="#";
                    //                  onclick="handleMailClick(\""+token+"\"); return false;";
                    //                }
                        if (href.startsWith("www.")) href="http://"+token;
                        xhtml.append("<A TARGET=_new HREF=\""+href+"\" onClick='"+onclick+"'>"+MailUtils.htmlescape(token)+"</A>");
                        if (line==null) break;
                      } else {
                        xhtml.append(MailUtils.htmlescape(line));
                        break;
                      }
                    }
                    xhtml.append("<BR>");
                }
                xhtml.append("</tt><HR></body></html>");
                htmlparts.add(xhtml.toString());
            }
        } else if (dispPart.isMimeType("message/*")) {
          StringBuffer xhtml=new StringBuffer();
          msgPart=dispPart;
          Message xmsg=(Message)dispPart.getContent();
          msgSubject=xmsg.getSubject();
          if (msgSubject==null) msgSubject="";
          msgSubject=MailUtils.htmlescape(msgSubject);
          Address ad[]=xmsg.getFrom();
          if (ad!=null) msgFrom=ms.getHTMLDecodedAddress(ad[0]);
          else msgFrom="";
          java.util.Date dt=xmsg.getSentDate();
          if (dt!=null) msgDate=java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG,java.text.DateFormat.LONG, locale).format(dt);
          else msgDate="";
          ad=xmsg.getRecipients(Message.RecipientType.TO);
          msgTo=null;
          if (ad!=null) {
            msgTo="";
            for(int j=0;j<ad.length;++j) msgTo+=ms.getHTMLDecodedAddress(ad[j])+" ";
          }
          ad=xmsg.getRecipients(Message.RecipientType.CC);
          msgCc=null;
          if (ad!=null) {
            msgCc="";
            for(int j=0;j<ad.length;++j) msgCc+=ms.getHTMLDecodedAddress(ad[j])+" ";
          }

          xhtml.append("<html><head><meta content='text/html; charset=utf-8' http-equiv='Content-Type'></head><body>");
          xhtml.append("<font face='Arial, Helvetica, sans-serif' size=2><BR>");
          xhtml.append("<B>"+ms.lookupResource(MailLocaleKey.MSG_FROMTITLE)+":</B> "+msgFrom+"<BR>");
          if (msgTo!=null) xhtml.append("<B>"+ms.lookupResource(MailLocaleKey.MSG_TOTITLE)+":</B> "+msgTo+"<BR>");
          if (msgCc!=null) xhtml.append("<B>"+ms.lookupResource(MailLocaleKey.MSG_CCTITLE)+":</B> "+msgCc+"<BR>");
          xhtml.append("<B>"+ms.lookupResource(MailLocaleKey.MSG_DATETITLE)+":</B> "+msgDate+"<BR>");
          xhtml.append("<B>"+ms.lookupResource(MailLocaleKey.MSG_SUBJECTTITLE)+":</B> "+msgSubject+"<BR>");
          xhtml.append("</font><br></body></html>");
          htmlparts.add(xhtml.toString());
        }

      }
      return htmlparts;
  }

  public synchronized HTMLMailData prepareHTMLMailData(MimeMessage msg) throws MessagingException, IOException {
    HTMLMailData mailData=new HTMLMailData(msg,folder);

    prepareHTMLMailData(msg, mailData);
	if (mailData.getDisplayPartCount()==0 && mailData.getAttachmentPartCount()>0) {
		Part part=mailData.getAttachmentPart(0);
		if (part.isMimeType("text/plain")||part.isMimeType("text/html")||part.isMimeType("message/delivery-status"))
			mailData.addDisplayPart(part);
	}
    return mailData;

  }
  
  public void prepareHTMLMailData(Part msg, HTMLMailData mailData) throws MessagingException, IOException {
    if(msg.isMimeType("text/plain")||msg.isMimeType("text/html")||msg.isMimeType("message/delivery-status")||msg.isMimeType("message/disposition-notification")) {
      if (msg.getDisposition()==null || msg.getDisposition().equalsIgnoreCase(Part.INLINE))
        mailData.addDisplayPart(msg);
      else mailData.addAttachmentPart(msg);
    } else if(msg.isMimeType("text/calendar")||msg.isMimeType("application/ics")) {
		mailData.addDisplayPart(msg);
    } else if(msg.isMimeType("message/rfc822")) {
      mailData.addDisplayPart(msg);
      prepareHTMLMailData((Message)msg.getContent(), mailData);
    } else if (msg.isMimeType("application/ms-tnef")) {
      try {
        TnefMultipartDataSource tnefDS = new TnefMultipartDataSource((MimePart)msg);
        TnefMultipart tnefmp=new TnefMultipart(tnefDS);
        int tnefparts=tnefmp.getCount();
        Part tnefp=null;
        for(int j=0; j<tnefparts; ++j) {
          tnefp=tnefmp.getBodyPart(j);
          prepareHTMLMailData(tnefp,mailData);
        }
        //  Part tnefp=TNEFMime.convert(this.ms.getMailSession(), msg);
        //  if (tnefp instanceof Multipart) {
        //      Multipart tnefmp=(Multipart)tnefp;
        //      int tnefparts=tnefmp.getCount();
        //      for(int j=0; j<tnefparts; ++j) {
        //        tnefp=tnefmp.getBodyPart(j);
        //        prepareHTMLMailData(tnefp,mailData);
        //      }
        //  }
        //  else prepareHTMLMailData(tnefp,mailData);
      } catch(Exception exc) {
        Service.logger.error("Exception",exc);
        mailData.addUnknownPart(msg);
        mailData.addAttachmentPart(msg);
      }
      
    } else if(msg.isMimeType("multipart/alternative")) {
      Part ap=getAlternativePart((Multipart)msg.getContent(),mailData);
      if(ap!=null) {
        mailData.addDisplayPart(ap);
      }
    } else if(msg.isMimeType("multipart/*")) {
      // Display the text content of the multipart message
      Multipart mp=(Multipart)msg.getContent();
      int parts=mp.getCount();
      Part p=null;
      for(int i=0; i<parts; ++i) {
        p=mp.getBodyPart(i);
        String ctype=p.getContentType();
        if(p.isMimeType("multipart/alternative")) {
          Part ap=getAlternativePart((Multipart)p.getContent(),mailData);
          if(ap!=null) {
			if (ap.isMimeType("text/calendar") || ap.isMimeType("application/ics")|| ap.getDisposition()==null || ap.getDisposition().equalsIgnoreCase(Part.INLINE))
                mailData.addDisplayPart(ap);
            else mailData.addAttachmentPart(ap);
          }
        } else if(p.isMimeType("multipart/*")) {
          prepareHTMLMailData(p, mailData);
        } else if(p.isMimeType("text/html")) {
          if (p.getDisposition()==null || p.getDisposition().equalsIgnoreCase(Part.INLINE))
            mailData.addDisplayPart(p);
          else mailData.addAttachmentPart(p);
        } else if(p.isMimeType("text/plain")) {
          if (p.getDisposition()==null || p.getDisposition().equalsIgnoreCase(Part.INLINE))
            mailData.addDisplayPart(p);
          else mailData.addAttachmentPart(p);
		} else if(p.isMimeType("text/calendar")||p.isMimeType("application/ics")) {
			mailData.addDisplayPart(p);
        } else if(p.isMimeType("message/delivery-status")||p.isMimeType("message/disposition-notification")) {
          if (p.getDisposition()==null || p.getDisposition().equalsIgnoreCase(Part.INLINE)) 
              mailData.addDisplayPart(p);
          else mailData.addAttachmentPart(p);
        } else if(p.isMimeType("message/rfc822")) {
          if (p.getDisposition()==null || p.getDisposition().equalsIgnoreCase(Part.INLINE))
              mailData.addDisplayPart(p);
          else mailData.addAttachmentPart(p);
          prepareHTMLMailData((Message)p.getContent(), mailData);
        } else if (p.isMimeType("application/ms-tnef")) {
          try {
            TnefMultipartDataSource tnefDS = new TnefMultipartDataSource((MimePart)p);
            TnefMultipart tnefmp=new TnefMultipart(tnefDS);
            int tnefparts=tnefmp.getCount();
            Part tnefp=null;
            for(int j=0; j<tnefparts; ++j) {
              tnefp=tnefmp.getBodyPart(j);
              prepareHTMLMailData(tnefp,mailData);
            }

            //  Part tnefp=TNEFMime.convert(this.ms.getMailSession(), p);
            //  if (tnefp instanceof Multipart) {
            //      Multipart tnefmp=(Multipart)tnefp;
            //      int tnefparts=tnefmp.getCount();
            //      for(int j=0; j<tnefparts; ++j) {
            //        tnefp=tnefmp.getBodyPart(j);
            //        prepareHTMLMailData(tnefp,mailData);
            //      }
            //  }
            //  else prepareHTMLMailData(tnefp,mailData);
          } catch(Exception exc) {
            mailData.addUnknownPart(p);
            mailData.addAttachmentPart(p);
            Service.logger.error("Exception",exc);
          }
        } else {
          mailData.addUnknownPart(p);
          mailData.addAttachmentPart(p);
          //Look for a possible Cid
          String filename=p.getFileName();
          String id[]=p.getHeader("Content-ID");
          if(id!=null||filename!=null) {
            if(id!=null) {
              filename=id[0];
            }
			filename=normalizeCidFileName(filename);
            mailData.addCidPart(filename, p);
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
            mailData.addUrlPart(url, p);
          }
        }
      }
    } else {
      mailData.addUnknownPart(msg);
      mailData.addAttachmentPart(msg);
    }
  }

  protected String normalizeCidFileName(String filename) {
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
  class PrepareStatus {
    boolean htmlfound=false;
    boolean textfound=false;
    Part bestPart=null;
  }

  private Part getAlternativePart(Multipart amp, HTMLMailData mailData) throws MessagingException, IOException {
    PrepareStatus status=new PrepareStatus();
    Part dispPart=null;
    for(int x=0; x<amp.getCount(); ++x) {
      Part ap=amp.getBodyPart(x);
      if(ap.isMimeType("multipart/*")) {
        prepareHTMLMailData(ap, mailData);
      } else if(ap.isMimeType("text/html")) {
        dispPart=ap;
        status.htmlfound=true;
      } else if(ap.isMimeType("text/plain")) {
        if(!status.htmlfound) {
          dispPart=ap;
          status.textfound=true;
        }
      } else if(ap.isMimeType("text/calendar")) {
          dispPart=ap;
          //mailData.addUnknownPart(ap);
          mailData.addAttachmentPart(ap);
		  break;
      }
    }
    return dispPart;
  }


  class HTMLMailParserThread implements Runnable {

    InputStream istream=null;
    String charset=null;
    Object threadLock=null;
    SaxHTMLMailParser saxHTMLMailParser=null;
    String appUrl=null;
    
    HTMLMailParserThread(Object tlock,InputStream istream, String charset, String appUrl, long msguid, boolean forEdit) {
        this.threadLock=tlock;
        this.istream=istream;
        this.charset=charset;
        this.appUrl=appUrl;
        this.saxHTMLMailParser=new SaxHTMLMailParser(environment.getSecurityToken(),forEdit,msguid);
    }
    
    HTMLMailParserThread(Object tlock,InputStream istream, String charset, String appUrl, String provider, String providerid) {
        this.threadLock=tlock;
        this.istream=istream;
        this.charset=charset;
        this.appUrl=appUrl;
        this.saxHTMLMailParser=new SaxHTMLMailParser(environment.getSecurityToken(),provider,providerid);
    }
    
    public void initialize(HTMLMailData mailData, boolean justBody) throws SAXException {
        saxHTMLMailParser.setApplicationURL(appUrl);
        saxHTMLMailParser.initialize(mailData, justBody);        
    }

    public void run() {
      try {
        FolderCache.this.doHTMLMailParse(saxHTMLMailParser,istream,charset);
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

  }

  private void doHTMLMailParse(SaxHTMLMailParser saxHTMLMailParser, InputStream istream, String charset) throws SAXException, IOException {
    HTMLInputStream hstream=new HTMLInputStream(istream);
    XMLReader xmlparser=XMLReaderFactory.createXMLReader("org.cyberneko.html.parsers.SAXParser");
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
  
  class MessageSearchResult {
	  String quickfilter;
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
      MessageComparator comparator=new MessageComparator(FolderCache.this.ms);
      
	  MessageSearchResult(String quickfilter, String pattern, String searchfield, int sort_by, boolean ascending, int sort_group, boolean groupascending, boolean threaded) {
		  this.quickfilter=quickfilter;
          this.sort_by=sort_by;
          this.ascending=ascending;
          this.pattern=pattern;
          this.searchfield=searchfield;
          this.sort_group=sort_group;
          this.groupascending=groupascending;
		  this.threaded=threaded;
      }
      
      void refresh() throws MessagingException, IOException {
          this.cleanup();
		  if (!threaded)
			this.msgs=_getMessages(quickfilter, pattern, searchfield, sort_by, ascending,sort_group,groupascending);
		  else
			this.msgs=_getThreadedMessages(quickfilter, pattern, searchfield);
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

}
