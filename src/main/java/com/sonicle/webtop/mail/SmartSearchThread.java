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
import com.sonicle.webtop.mail.bol.js.JsSmartSearchTotals;
import com.sun.mail.imap.IMAPFolder;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.SearchTerm;

/**
 *
 * @author gbulfon
 */
public class SmartSearchThread extends Thread {

    private static final int MAXRESULTS=500;
	
	private final ArrayList<String> folderIds;
	private final boolean fromme;
	private final boolean tome;
	private final boolean attachments;
	private final ArrayList<String> ispersonfilters;
	private final ArrayList<String> isnotpersonfilters;
	private final ArrayList<String> isfolderfilters;
	private final ArrayList<String> isnotfolderfilters;
	private final int year;
	private final int month;
	private final int day;
	private SearchTerm searchTerm;
	
	private boolean cancel=false;
    private boolean finished=false;
    private boolean morethanmax=false;
    private Throwable exception=null;
    private int progress=0;
    private FolderCache curfolder;
	private JsSmartSearchTotals sst=new JsSmartSearchTotals();

    private final Service ms;
	private MailAccount account;
	
    public SmartSearchThread(Service ms, MailAccount account, ArrayList<String> folderIds, 
			boolean fromme, boolean tome, boolean attachments,
			ArrayList<String> ispersonfilters, ArrayList<String> isnotpersonfilters,
			ArrayList<String> isfolderfilters, ArrayList<String> isnotfolderfilters,
			int year, int month, int day, SearchTerm searchTerm) throws MessagingException {
        this.ms=ms;
		this.account=account;
		this.searchTerm = searchTerm;
		this.folderIds=folderIds;
		this.fromme=fromme;
		this.tome=tome;
		this.attachments=attachments;
		this.ispersonfilters=ispersonfilters;
		this.isnotpersonfilters=isnotpersonfilters;
		this.isfolderfilters=isfolderfilters;
		this.isnotfolderfilters=isnotfolderfilters;
		this.year=year;
		this.month=month;
		this.day=day;
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
        Service.logger.info("START OF SMART SEARCH THREAD");
        try {
			for(String folderId: folderIds) {
                progress++;
				sst.progress=(int)((100.0/folderIds.size()*progress));
				
				boolean skipfolder=false;
				if (isfolderfilters.size()>0 && !isfolderfilters.contains(folderId)) skipfolder=true;
				if (isnotfolderfilters.contains(folderId)) skipfolder=true;
				
				FolderCache fc=account.getFolderCache(folderId);
                curfolder=fc;
				sst.curfoldername=fc.getFolderName();
				
				if ((fc.getFolder().getType()&IMAPFolder.HOLDS_MESSAGES)==0) continue;
				try {
					fc.open();
				} catch(MessagingException exc) {
					continue;
				}
                if (cancel) {
                    Service.logger.info("CANCELING SMART SEARCH");
                    break;
                }
				
                Service.logger.info("SMART SEARCH IN "+fc.getFolderName());
				Message msgs[]=null;
				//some folders (e.g. NS7 Public) may not allow search
				try {
					msgs = fc.getMessages(FolderCache.SORT_BY_DATE, false, true, -1, false, false, searchTerm);
				} catch(MessagingException mexc) {
				}
                
                if (msgs!=null && msgs.length>0) {
					fc.fetch(msgs, ms.getMessageFetchProfile());
					int totmsgs=0;
					int foldermsgs=0;
					for(Message xmsg: msgs) {
						SonicleIMAPMessage msg=(SonicleIMAPMessage)xmsg;
						Address afrom=msg.getFrom()[0];
						InternetAddress iafrom=null;
						boolean isfromme=false;
						if (afrom instanceof InternetAddress) {
							iafrom=(InternetAddress) afrom;
							if (iafrom.getAddress().equals(ms.getEnv().getProfile().getEmailAddress())) isfromme=true;
						}

						Address[] rcpts=msg.getRecipients(Message.RecipientType.TO);
						ArrayList<InternetAddress> others=new ArrayList<>();
						ArrayList<InternetAddress> tos=new ArrayList<>();
						boolean istome=false;
						if (rcpts!=null)
							for(Address ato: rcpts) {
								if (ato instanceof InternetAddress) {
									InternetAddress iato=(InternetAddress) ato;
									tos.add(iato);
									if (iato.getAddress().equals(ms.getEnv().getProfile().getEmailAddress())) istome=true;
									else others.add(iato);
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

						boolean hasattachments=fc.hasAttachements(msg);

						if ((!fromme || (fromme && isfromme)) && 
								(!tome || (tome && istome)) &&
								(!attachments || (attachments && hasattachments))) {

							if (isfromme) sst.viewFromMe++;
							else {
								String email=iafrom.getAddress();
								JsSmartSearchTotals.Person p=sst.addPerson(iafrom.getPersonal(), email);
								if (ispersonfilters.contains(email)) p.include();
								else if (isnotpersonfilters.contains(email)) p.exclude();
							}

							if (istome) sst.viewToMe++;
							for(InternetAddress iato: others) {
								String email=iato.getAddress();
								String name=iato.getPersonal();
								JsSmartSearchTotals.Person p=sst.addPerson(name, email);
								if (ispersonfilters.contains(email)) p.include();
								else if (isnotpersonfilters.contains(email)) p.exclude();
							}

							if (hasattachments) sst.viewAttachments++;

							foldermsgs++;

							if (!skipfolder) {
								//skip against person filters
								boolean skip=false;
								if (ispersonfilters.size()>0) {
									skip=true;
									if (iafrom!=null && ispersonfilters.contains(iafrom.getAddress())) skip=false;
									else {
										for(InternetAddress ia: tos) {
											if (ispersonfilters.contains(ia.getAddress())) {
												skip=false;
												break;
											}
										}
									}
								}
								if (skip) continue;

								if (iafrom!=null && isnotpersonfilters.contains(iafrom.getAddress())) skip=true;
								else {
									for(InternetAddress ia: tos) {
										if (isnotpersonfilters.contains(ia.getAddress())) {
											skip=true;
											break;
										}
									}
								}
								if (skip) continue;


								sst.addDate(msg.getReceivedDate());

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
								sst.addMessage(msg.getUID(), 
										folderId, 
										ms.getInternationalFolderName(account.getFolderCache(folderId)),
										msg.getSubject(), 
										from,
										to,
										msg.getReceivedDate(),
										msgtext);

								++totmsgs;

							}
						}

					}
					if (foldermsgs>0) {
						JsSmartSearchTotals.Folder f=sst.addFolder(folderId, fc.getDescription(),foldermsgs);
						if (isfolderfilters.contains(folderId)) f.include();
						else if (isnotfolderfilters.contains(folderId)) f.exclude();
					}

					sst.totalRows+=totmsgs;
					sst.visibleRows=visibleRows;
                }
            }
			sst.sortByTotal();
			sst.sortYears();
			sst.year=year;
			sst.month=month;
			sst.day=day;
			
            Service.logger.info("FINISHED SMART SEARCH");
        } catch(Exception exc) {
            exception=exc;
            com.sonicle.webtop.mail.Service.logger.error("Exception",exc);
        }
        sst.finished=finished=true;
        Service.logger.info("END OF SMART SEARCH THREAD");
    }
	
	public JsSmartSearchTotals getSmartSearchTotals() {
		return sst;
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
