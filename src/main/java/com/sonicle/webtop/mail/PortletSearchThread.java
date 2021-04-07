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

import com.sonicle.commons.MailUtils;
import com.sonicle.mail.imap.SonicleIMAPMessage;
import com.sonicle.webtop.mail.bol.js.JsPortletSearchResult;
import com.sonicle.webtop.mail.bol.model.ImapQuery;
import com.sun.mail.imap.IMAPFolder;
import java.util.ArrayList;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.SearchTerm;

/**
 *
 * @author gbulfon
 */
public class PortletSearchThread extends Thread {

    private static final int MAXRESULTS=500;
	
	private final ArrayList<String> folderIds;
	
	private boolean cancel=false;
    private boolean finished=false;
    private boolean morethanmax=false;
    private Throwable exception=null;
    private int progress=0;
    private FolderCache curfolder;
	private JsPortletSearchResult psr=new JsPortletSearchResult();
	private ImapQuery imapQuery;

    private final Service ms;
	private MailAccount account;
	
    public PortletSearchThread(Service ms, MailAccount account, ArrayList<String> folderIds, ImapQuery imapQuery) throws MessagingException {
        this.ms = ms;
		this.account = account;
		this.folderIds = folderIds;
		this.imapQuery = imapQuery;
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
		int visibleRows=0;
		int maxVisibleRows=20;
        Service.logger.info("START OF PORTLET SEARCH THREAD");
        try {
			int n=0;
			for(String folderId: folderIds) {
                progress++;
				psr.progress=(int)((100.0/folderIds.size()*progress));
				
				FolderCache fc=account.getFolderCache(folderId);
                curfolder=fc;
				psr.curfoldername=fc.getFolderName();
				
				if ((fc.getFolder().getType()&IMAPFolder.HOLDS_MESSAGES)==0) continue;
				try {
					fc.open();
				} catch(MessagingException exc) {
					continue;
				}
                if (cancel) {
                    Service.logger.info("CANCELING PORTLET SEARCH");
                    break;
                }
				
                Service.logger.info("PORTLET SEARCH IN "+fc.getFolderName());
				Message msgs[]=null;
				//some folders (e.g. NS7 Public) may not allow search
				try {
					msgs = fc.getMessages(FolderCache.SORT_BY_DATE, false, true, -1, true, false, imapQuery);
				} catch(MessagingException mexc) {
				}
                
                if (msgs!=null && msgs.length>0) {
					fc.fetch(msgs, ms.getMessageFetchProfile(),0,50);
					int totmsgs=0;
					for(Message xmsg: msgs) {
						SonicleIMAPMessage msg=(SonicleIMAPMessage)xmsg;
						Address afrom=msg.getFrom()[0];
						InternetAddress iafrom=null;
						if (afrom instanceof InternetAddress) {
							iafrom=(InternetAddress) afrom;
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
						/*rcpts=msg.getRecipients(RecipientType.CC);
						if (rcpts!=null
							for(Address acc: rcpts) {
								if (acc instanceof InternetAddress) {
									InternetAddress iacc=(InternetAddress) acc;
									if (!iacc.getAddress().equals(environment.getProfile().getEmailAddress()))
										others.add(iacc);
								}
							}*/


						String msgtext = "";
						if (visibleRows<maxVisibleRows) {
							msgtext=MailUtils.peekText(msg);
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
						psr.addMessage(msg.getUID(), 
								folderId, 
								ms.getInternationalFolderName(account.getFolderCache(folderId)),
								msg.getSubject(), 
								from,
								to,
								msg.getReceivedDate(),
								msgtext);

						++totmsgs;

						++n;
						if (n>=50) break;
					}

					psr.totalRows+=totmsgs;
					psr.visibleRows=visibleRows;
					
                }
				if (n>=50) break;
            }
			
            Service.logger.info("FINISHED PORTLET SEARCH");
        } catch(Exception exc) {
            exception=exc;
            com.sonicle.webtop.mail.Service.logger.error("Exception",exc);
        }
        psr.finished=finished=true;
        Service.logger.info("END OF PORTLET SEARCH THREAD");
    }
	
	public JsPortletSearchResult getPortletSearchResult() {
		return psr;
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

    public int getProgress() {
        return ((progress*100)/folderIds.size());
    }

    public FolderCache getCurrentFolder() {
        return curfolder;
    }

}
