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
package com.sonicle.webtop.mail.bg;

import com.sonicle.commons.MailUtils;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.sdk.BaseBackgroundServiceTask;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.mail.BackgroundService;
import com.sonicle.webtop.mail.MailLocaleKey;
import com.sonicle.webtop.mail.MailServiceSettings;
import com.sonicle.webtop.mail.MailUserSettings;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.HeaderTerm;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class LegacyScheduledSendTask extends BaseBackgroundServiceTask {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(LegacyScheduledSendTask.class);
	
	private BackgroundService bs = null;
	private HashMap<String,MailServiceSettings> hmss=new HashMap();
	private HashMap<UserProfileId,MailUserSettings> hmus=new HashMap();
	private boolean findSendTasksExceptionTraced=false;
	private HeaderTerm hterm=new HeaderTerm("Sonicle-send-scheduled","true");
	
	@Override
	public Logger getLogger() {
		return LOGGER;
	}
	
	@Override
	public void executeWork(JobExecutionContext jec, TaskContext context) throws Exception {
		try {
			bs = ((BackgroundService)getBackgroundService(jec));
			
			for(Domain domain : WT.getCoreManager().listDomains(EnabledCond.ENABLED_ONLY).values()) {
				if (shouldStop()) break; // Speed-up shutdown process!
				String domainId=domain.getDomainId();
				UserProfileId adminPid=new UserProfileId(domainId,"admin");
				CoreManager domainCm=WT.getCoreManager(adminPid);
				Session session=WT.getGlobalMailSession(domainId);
				MailServiceSettings mss = getMailServiceSettings(domainId);
				String defaultHost=mss.getDefaultHost();
				LOGGER.debug(" default host for domain "+domainId+" is "+defaultHost);
				if (mss.isScheduledEmailsDisabled()) {
					LOGGER.debug(" skipping domain "+domainId+" : scheduled-emails disabled");
					continue;
				}
				String vmailSecret=mss.getNethTopVmailSecret();
				//List<OUserMap> musers=UserMapDAO.getInstance().selectByDomainId(con, domainId);
				for (String userId : domainCm.listUserIds(EnabledCond.ENABLED_ONLY)) {
					if (shouldStop()) break; // Speed-up shutdown process!
					Store store=null;
					try {
						UserProfileId pid=new UserProfileId(domainId,userId);
						MailUserSettings mus=getMailUserSettings(pid, mss);
						String host=mus.getHost();
						if (!defaultHost.equals(host)) {
							LOGGER.debug("  skipping host "+host+" for user "+userId+"@"+domainId);
							continue;
						}

						int port=mus.getPort();
						String protocol=mus.getProtocol();
						if (vmailSecret==null) {
							String adminUser=mss.getAdminUser();
							String adminPassword=mss.getAdminPassword();
							store=connectAdminStore(session, host, port, protocol, adminUser, adminPassword);
						} else {
							store=connectVmailStore(session, host, port, protocol, pid, vmailSecret);
						}
						Folder outgoings[]=getOutgoingFolders(store, domain, pid, vmailSecret, mus);
						Folder folderSent=getSentFolder(store, pid, domain, vmailSecret, mus);
						if (shouldStop()) break; // Speed-up shutdown process!
						sendScheduledMails(session,pid,domain,outgoings,folderSent,WT.getUserData(pid).getLocale());
						store.close();
						store=null;
					} catch(Exception exc) {
						if (!findSendTasksExceptionTraced) {
							exc.printStackTrace();
							findSendTasksExceptionTraced=true;
						}
					} finally {
						if (store!=null) try { store.close(); } catch(Exception exc) {}
					}
				}
			}
			
		} finally {
			bs = null;
			hmss.clear();
			hmus.clear();
		}	
	}
	
	public void sendScheduledMails(Session session, UserProfileId pid, Domain domain, Folder outgoings[], Folder folderSent, Locale locale) {
		for(Folder targetfolder: outgoings) {
			if (shouldStop()) break; // Speed-up shutdown process!
			try {
				targetfolder.open(Folder.READ_WRITE);
				Message msgs[]=targetfolder.search(hterm);
				for(Message m: msgs) {
					if (shouldStop()) break; // Speed-up shutdown process!
					String mids[]=m.getHeader("Message-ID");
					if (mids!=null) {
						String mid=mids[0];
						if (mid!=null && mid.length()>0) {
							try {
								String senddate=getSingleHeaderValue(m,"Sonicle-send-date");
								String sendtime=getSingleHeaderValue(m,"Sonicle-send-time");
								//hack for possible bug in Cyrus 3.4 returning more than the searched items
								if (senddate!=null && sendtime!=null) {
									String hsendnotify=getSingleHeaderValue(m,"Sonicle-notify-delivery");
									boolean sendnotify=hsendnotify!=null && hsendnotify.equals("true");
									if (isTimeToSend(senddate,sendtime)) {
										sendScheduledMessage(session, pid, domain, folderSent, locale, m, sendnotify);
									}
								}
							} catch(Exception exc) {
								LOGGER.debug("Error during sendScheduledMails on user "+pid.getUserId()+ " message-id "+mid, exc);

							}
						}
					}
				}
				//targetfolder.close(true);
			} catch(Exception exc2) {
				LOGGER.debug("Error during sendScheduledMails on user "+pid.getUserId(), exc2);
			} finally {
				try  { targetfolder.close(true); } catch(Exception exc3) {}
			}
		}
	}

	private Folder[] getOutgoingFolders(Store store, Domain domain, UserProfileId pid, String vmailSecret, MailUserSettings mus) throws MessagingException {
		String foldername=mus.getFolderDrafts();
		String folderprefix=mus.getFolderPrefix();
		if (folderprefix!=null && folderprefix.length()>0 && foldername.startsWith(folderprefix)) {
			foldername=foldername.substring(folderprefix.length());
		}
		Folder outgoings[]=null;

		if (vmailSecret==null) {
			Folder uns[]=store.getUserNamespaces("");
			outgoings=new Folder[uns.length];
			for(int i=0;i<uns.length;++i) {
				Folder un=uns[i];
				char sep=un.getSeparator();
				String fname=pid.getUserId()+sep+foldername;
				if ("ldapWebTop".equalsIgnoreCase(domain.getDirScheme())) fname+="@"+domain.getInternetName();
				outgoings[i]=un.getFolder(fname);
			}
		} else {
			outgoings=new Folder[1];
			outgoings[0]=store.getFolder(foldername);
		}
		return outgoings;
	}

	private Folder getSentFolder(Store store, UserProfileId pid, Domain domain, String vmailSecret, MailUserSettings mus) throws MessagingException {
		String foldername=mus.getFolderSent();
		String folderprefix=mus.getFolderPrefix();
		if (folderprefix!=null && folderprefix.length()>0 && foldername.startsWith(folderprefix)) {
			foldername=foldername.substring(folderprefix.length());
		}
		Folder sent=null;
		if (vmailSecret==null) {
			Folder uns[]=store.getUserNamespaces("");
			sent=null;
			if (uns.length>0) {
				Folder un=uns[0];
				char sep=un.getSeparator();
				String fname=pid.getUserId()+sep+foldername;
				if ("ldapWebTop".equalsIgnoreCase(domain.getDirScheme())) fname+="@"+domain.getInternetName();
				sent=un.getFolder(fname);
			}
		} else {
			sent=store.getFolder(foldername);
		}
		return sent;
	}

	private String getSingleHeaderValue(Message m, String headerName) throws MessagingException {
		String s[]=m.getHeader(headerName);
		String sv=null;
		if (s!=null && s.length>0) sv=s[0];
		return sv;
	}

	private boolean isTimeToSend(String senddate, String sendtime) {
		Calendar cal=parseCalendar(senddate,sendtime);
		if (cal==null) return false;

		Calendar calnow=Calendar.getInstance();
		calnow.setTime(new Date());
		boolean itts=cal.before(calnow);
		return itts;
	}

	private Calendar parseCalendar(String senddate, String sendtime) {
		String sdp[]=senddate.split("/");
		String sdt[]=sendtime.split(":");
		if (sdp.length<3 || sdt.length<2) return null;

		String sschedday = sdp[0];
		String sschedmonth = sdp[1];
		String sschedyear = sdp[2];
		String sschedhour=sdt[0];
		String sschedmins=sdt[1];
		int schedday=Integer.parseInt(sschedday);
		int schedmonth=Integer.parseInt(sschedmonth);
		int schedyear=Integer.parseInt(sschedyear);
		int schedhour=Integer.parseInt(sschedhour);
		int schedmins=Integer.parseInt(sschedmins);
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.YEAR, schedyear);
		cal.set(Calendar.MONTH, schedmonth-1);
		cal.set(Calendar.DATE, schedday);
		cal.set(Calendar.HOUR_OF_DAY, schedhour);
		cal.set(Calendar.MINUTE, schedmins);
		return cal;
	}

	private void sendScheduledMessage(Session session, UserProfileId pid, Domain domain, Folder sentFolder, Locale locale, Message m, boolean notify) throws MessagingException {
		try {
			MimeMessage nm=new MimeMessage((MimeMessage)m);
			nm.removeHeader("Sonicle-send-scheduled");
			nm.removeHeader("Sonicle-send-date");
			nm.removeHeader("Sonicle-send-time");
			nm.removeHeader("Sonicle-notify-delivery");
			nm.setSentDate(new Date());
			Transport.send(nm);
			sentFolder.open(Folder.READ_WRITE);
			nm.setFlag(Flags.Flag.SEEN, true);
			Message msgs[]=new Message[1];
			msgs[0]=nm;
			sentFolder.appendMessages(msgs);
			//folder.copyMessages(msgs, sentFolder);
			sentFolder.close(true);
			m.setFlag(Flags.Flag.DELETED, true);

			Calendar cal=parseCalendar(getSingleHeaderValue(m,"Sonicle-send-date"),getSingleHeaderValue(m,"Sonicle-send-time"));
			if (notify && cal!=null) {
				String recipients="";
				for(Address ia: nm.getRecipients(Message.RecipientType.TO)) {
					if (recipients.length()>0) recipients+=" - ";
					recipients+=ia.toString();
				}
				String nmto=nm.getRecipients(Message.RecipientType.TO)[0].toString();
				String nmsubject=nm.getSubject();
				String fmtsubject=bs.lookupResource(locale, MailLocaleKey.SCHEDULED_SENT_SUBJECT);
				String fmthtml=bs.lookupResource(locale, MailLocaleKey.SCHEDULED_SENT_HTML);
				String subject=java.text.MessageFormat.format(fmtsubject,nmto);
				String html=java.text.MessageFormat.format(fmthtml,
						DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT,locale).format(cal.getTime()),
						MailUtils.htmlescape(recipients),
						MailUtils.htmlescape(nmsubject));
				String email=WT.getUserData(pid).getEmail().toString();
				WT.sendEmail(session, true, "webtop@"+domain.getInternetName(), email, subject, html);
			}
		} catch(Throwable t) {
			LOGGER.error("Exception",t);
		}
	}

	private Store connectAdminStore(Session session, String host, int port, String protocol, String adminUser, String adminPassword) throws MessagingException {
		Store store=session.getStore(protocol);
		store.connect(host,port,adminUser,adminPassword);
		return store;
	}

	private Store connectVmailStore(Session session, String host, int port, String protocol, UserProfileId pid, String vmailSecret) throws MessagingException {
		Store store=session.getStore(protocol);
		store.connect(host,port,pid.getUserId()+"*vmail",vmailSecret);
		return store;
	}

	/*
	* Implement mail service settings object cache
	*/
	private MailServiceSettings getMailServiceSettings(String domainId) {
		MailServiceSettings mss=hmss.get(domainId);
		if (mss==null) {
			mss=new MailServiceSettings(bs.SERVICE_ID,domainId);
			hmss.put(domainId, mss);
		}
		return mss;
	}

	/*
	* Implement mail user settings object cache
	*/
	private MailUserSettings getMailUserSettings(UserProfileId pid, MailServiceSettings mss) {
		MailUserSettings mus=hmus.get(pid);
		if (mus==null) {
			mus=new MailUserSettings(pid,mss);
			hmus.put(pid, mus);
		}
		return mus;
	}
}
