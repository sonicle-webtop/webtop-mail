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
import java.util.ArrayList;

/**
 *
 * @author gbulfon
 */
public class MailFoldersThread extends Thread {
    
    private int threadCountMailFolders=1;
    Service ms;
    boolean abort=false;
    boolean failed=false;
    String failMessage=null;
    
    int sleepInbox=30;
    int sleepCycles=6;
    int sleepOthers=sleepInbox*sleepCycles;
    int sleepCount=0;
    boolean checkAll=false;
    
    public MailFoldersThread(Service ms, PrivateEnvironment env) {
        super();
        this.setName("MailFoldersThread"+(threadCountMailFolders++)+"-"+env.getProfile().getUserId());
        this.ms=ms;
    }

    public void setSleepInbox(int seconds) {
        sleepInbox=seconds;
        sleepOthers=sleepInbox*sleepCycles;
    }

    public void setSleepCycles(int cycles) {
        sleepOthers=sleepInbox*sleepCycles;
    }

    public void setCheckAll(boolean b) {
        checkAll=b;
    }
    
    public void abort() {
        this.abort=true;
        this.interrupt();
    }
    
    @Override
    public void run() {
        abort=false;
        failed=false;
        failMessage=null;
        sleepCount=0;
        try {
            FolderCache fcroot=ms.getRootFolderCache();
            //Service.logger.debug("Entering MFT loop");
            FolderCache fcinbox=null;
			if (ms.hasDifferentDefaultFolder()) fcinbox=fcroot;
			else fcinbox=ms.getFolderCache("INBOX");
			//Check inbox only once, then via idle
			fcinbox.checkFolder();
            while(!abort) {
                //Service.logger.debug("MFT Synchronizing");
                    synchronized(this) {
                        ms.checkStoreConnected();                      if (sleepCount>0) {
                            //Service.logger.debug("MailFolderThread: Checking inbox messages");
							
							//Don't check here inbox, it's in idle mode
                            //fcinbox.checkFolder();
							
                            FolderCache sfc[]=ms.getSharedFoldersCache();
                            if (sfc!=null) {
                                for(FolderCache fc: sfc) {
                                    if (fc!=null) {
                                        ArrayList<FolderCache> children=fc.getChildren();
                                        if (children!=null) {
                                            for(FolderCache inbox: children) inbox.checkFolder();
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            //Service.logger.debug("MailFolderThread: Checking all messages");
                            try {
                                fcroot.checkSubfolders(checkAll);
                            } catch(Exception exc) {
                                Service.logger.error("Exception",exc);
                            }
                        }
                    }
                    //Service.logger.debug("MailFolderThread: Sleeping....");
                    if (sleepCount<=0) sleepCount=sleepOthers;
                    sleep(1000*sleepInbox);
                    sleepCount-=sleepInbox;
            }
        } catch(Throwable exc) {
            Service.logger.error("Exception",exc);
            abort=true;
            failed=true;
            failMessage=exc.getMessage();
        }
        //Service.logger.debug("Exiting MFT");
    }
    
}
