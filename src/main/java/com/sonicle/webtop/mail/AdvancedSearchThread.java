/*
* WebTop Groupware is a bundle of WebTop Services developed by Sonicle S.r.l.
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

import java.util.ArrayList;
import javax.mail.*;

/**
 *
 * @author gbulfon
 */
public class AdvancedSearchThread extends Thread {

    private static final int MAXRESULTS=500;

    public static final int FOLDERTYPE_SPECIFIC=1;
    public static final int FOLDERTYPE_ALL=2;
    public static final int FOLDERTYPE_PERSONAL=3;
    public static final int FOLDERTYPE_SHARED=4;

    private Service ms;
	private MailAccount account;
    private int folderType;
    private boolean subfolders;
	private boolean trashspam;
    private boolean and;
    private AdvancedSearchEntry entries[];

    private boolean cancel=false;
    private boolean finished=false;
    private boolean morethanmax=false;
    private Throwable exception=null;
    private int progress=0;
    private FolderCache curfolder;

    ArrayList<FolderCache> folders=new ArrayList<FolderCache>();
    ArrayList<Message> result=new ArrayList<Message>();

    public AdvancedSearchThread(Service ms, MailAccount account, String folder, boolean trashspam, boolean subfolders, boolean and, AdvancedSearchEntry entries[]) throws MessagingException {
        this.ms=ms;
		this.account=account;
        this.folderType=FOLDERTYPE_SPECIFIC;
        this.subfolders=subfolders;
		this.trashspam=trashspam;
        this.and=and;
        this.entries=entries;

        FolderCache fc=account.getFolderCache(folder);
        folders.add(fc);
        if (subfolders) addChildren(fc);
    }

    public AdvancedSearchThread(Service ms, MailAccount account, int folderType, boolean trashspam, boolean subfolders, boolean and, AdvancedSearchEntry entries[]) throws MessagingException {
        this.ms=ms;
		this.account=account;
        this.folderType=folderType;
        this.subfolders=subfolders;
		this.trashspam=trashspam;
        this.and=and;
        this.entries=entries;

        FolderCache fcr=account.getRootFolderCache();
        switch(folderType) {

            case FOLDERTYPE_ALL:
                addChildren(fcr);
                break;

            case FOLDERTYPE_PERSONAL:
                for(FolderCache fc: fcr.getChildren()) {
                    if (!fc.isSharedFolder()) addChildren(fc);
                }
                break;
            case FOLDERTYPE_SHARED:
                FolderCache sfc[]=account.getSharedFoldersCache();
                if (sfc!=null) {
                    for(FolderCache fc: sfc) addChildren(fc);
                }
                break;
        }
    }

    public void cancel() {
        cancel=true;
    }

    @Override
    public void run() {
        cancel=false;
        finished=false;
        morethanmax=false;
        exception=null;
        progress=0;
        Service.logger.debug("START OF ADVANCED SEARCH THREAD");
        try {
            for(FolderCache fc: folders) {
                curfolder=fc;
                progress++;
                if (cancel) {
                    Service.logger.debug("CANCELING ADVANCED SEARCH");
                    break;
                }

                Service.logger.debug("ADVANCED SEARCH IN "+fc.getFolderName());
				Message msgs[]=null;
				//some folders (e.g. NS7 Public) may not allow search
				try {
					msgs=fc.advancedSearchMessages(entries, and, FolderCache.SORT_BY_DATE, false);
				} catch(MessagingException mexc) {
				}
                
                if (msgs!=null && msgs.length>0) {
                    fc.fetch(msgs, ms.getMessageFetchProfile());
                    for(int n=0;n<msgs.length && result.size()<MAXRESULTS;++n) result.add(msgs[n]);
                }
                if (result.size()>=MAXRESULTS) {
                    Service.logger.debug("RESULT REACHED MAX");
                    this.morethanmax=true;
                    break;
                }
            }
            Service.logger.debug("FINISHED ADVANCED SEARCH");
        } catch(Exception exc) {
            exception=exc;
            com.sonicle.webtop.mail.Service.logger.error("Exception",exc);
        }
        finished=true;
        Service.logger.debug("END OF ADVANCED SEARCH THREAD");
    }

    public boolean isRunning() {
        return (isAlive() && !finished);
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isMoreThanMax() {
        return morethanmax;
    }

    public boolean hasErrors() {
        return (exception!=null);
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isCanceled() {
        return cancel;
    }

    public ArrayList<Message> getResult() {
        return result;
    }

    private void addChildren(FolderCache fc) throws MessagingException {
		Folder children[]=fc.getFolder().list();
		if (children!=null) {
			ArrayList<Folder> achildren=ms.sortFolders(account,children);
			for(Folder folder: achildren) {
				FolderCache fcc=account.getFolderCache(folder.getFullName());
				if (fcc!=null) {
					if (!trashspam) {
						//skip trash and spam from advanced search
						if (fcc.isTrash()||fcc.isSpam()) continue;
					}

					folders.add(fcc);
					if (fcc.hasChildren()) addChildren(fcc);				
				}

			}
		}
    }

    public int getProgress() {
        return ((progress*100)/folders.size());
    }

    public FolderCache getCurrentFolder() {
        return curfolder;
    }

}
