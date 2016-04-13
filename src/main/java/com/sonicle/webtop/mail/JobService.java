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

import com.sonicle.commons.MailUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseJobServiceTask;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.mail.bol.OUserMap;
import com.sonicle.webtop.mail.dal.UserMapDAO;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.search.HeaderTerm;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;

/**
 *
 * @author gabriele.bulfon
 */
public class JobService extends BaseJobService {
	private static final Logger logger = WT.getLogger(com.sonicle.webtop.core.JobService.class);
	
	@Override
	public void initialize() throws Exception {
	}

	@Override
	public void cleanup() throws Exception {
	}
	
	@Override
	public List<TaskDefinition> returnTasks() {
		ArrayList<TaskDefinition> jobs = new ArrayList<>();
		
		// Reminder job
		Trigger remTrigger = TriggerBuilder.newTrigger()
				.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(2))
				.build();
		jobs.add(new TaskDefinition(ScheduledSendJob.class, remTrigger));
		
		return jobs;
	}
	
	class ScheduledSendJob extends BaseJobServiceTask {
		private JobService jobService = null;
		private CoreManager cm=null;
		
		private java.util.Properties props=System.getProperties();
		private javax.mail.Session session=null;
		private HashMap<String,MailServiceSettings> hmss=new HashMap();
		private HashMap<String,HashMap<String,MailUserSettings>> hhmus=new HashMap();

		private boolean findSendTasksExceptionTraced=false;

		HeaderTerm hterm=new HeaderTerm("Sonicle-send-scheduled","true");
		
		public ScheduledSendJob() {
			cm=WT.getCoreManager(getRunContext());
			props.setProperty("mail.imaps.ssl.trust", "*");
			session=javax.mail.Session.getDefaultInstance(props);
		}
	
		@Override
		public void setJobService(BaseJobService jobService) {
			// This method is automatically called by scheduler engine
			// while instantiating this task.
			this.jobService = (JobService)jobService;
		}
		
		@Override
		public void executeWork() {
			Connection con=null;
			try {
				con=getConnection();
				List<ODomain> domains=cm.listDomains(true);
				for(ODomain domain: domains) {
					String domainId=domain.getDomainId();
					MailServiceSettings mss = getMailServiceSettings(domainId);
					String vmailSecret=mss.getNethTopVmailSecret();
					List<OUserMap> musers=UserMapDAO.getInstance().selectByDomainId(con, domainId);
					for(OUserMap muser: musers) {
						Store store=null;
						try {
							String userId=muser.getUserId();
							UserProfile.Id pid=new UserProfile.Id(domainId,userId);
							if (vmailSecret==null) {
								String adminUser=mss.getAdminUser();
								String adminPassword=mss.getAdminPassword();
								store=connectAdminStore(muser, adminUser, adminPassword);
							} else {
								store=connectVmailStore(muser, vmailSecret);
							}
							MailUserSettings mus=getMailUserSettings(pid, mss);
							Folder outgoings[]=getOutgoingFolders(store, domain, muser, vmailSecret, mus);
							Folder folderSent=getSentFolder(store, muser, domain, vmailSecret, mus);
							sendScheduledMails(muser,outgoings,folderSent,WT.getUserData(pid).getLocale());
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
			} catch(Exception exc) {
				logger.error("Error during executeWork",exc);
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
		
		public void sendScheduledMails(OUserMap mu, Folder outgoings[], Folder folderSent, Locale locale) {
			for(Folder targetfolder: outgoings) {
				try {
					targetfolder.open(Folder.READ_WRITE);
					Message msgs[]=targetfolder.search(hterm);
					for(Message m: msgs) {
						String mids[]=m.getHeader("Message-ID");
						if (mids!=null) {
							String mid=mids[0];
							if (mid!=null && mid.length()>0) {
								String senddate=getSingleHeaderValue(m,"Sonicle-send-date");
								String sendtime=getSingleHeaderValue(m,"Sonicle-send-time");
								boolean sendnotify=getSingleHeaderValue(m,"Sonicle-notify-delivery").equals("true");
								if (isTimeToSend(senddate,sendtime)) {
									sendScheduledMessage(mu, folderSent, locale, m, sendnotify);
								}
								//scheduleSendTask(mu, mid, senddate, sendtime, sendnotify);
							}
						}
					}
					//targetfolder.close(true);
				} catch(Exception exc2) {
					//MailService.logger.error("");
				} finally {
					try  { targetfolder.close(true); } catch(Exception exc3) {}
				}
			}
		}
		
		private Folder[] getOutgoingFolders(Store store, ODomain domain, OUserMap mu, String vmailSecret, MailUserSettings mus) throws MessagingException {
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
					String fname=mu.getMailUser()+sep+foldername;
					if (domain.getAuthUri().startsWith("ldapWebTop:")) fname+="@"+domain.getDomainName();
					outgoings[i]=un.getFolder(fname);
				}
			} else {
				outgoings=new Folder[1];
				outgoings[0]=store.getFolder(foldername);
			}
			return outgoings;
		}
		
		private Folder getSentFolder(Store store, OUserMap mu, ODomain domain, String vmailSecret, MailUserSettings mus) throws MessagingException {
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
					String fname=mu.getMailUser()+sep+foldername;
					if (domain.getAuthUri().startsWith("ldapWebTop:")) fname+="@"+domain.getDomainName();
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
			Calendar calnow=Calendar.getInstance();
			calnow.setTime(new Date());
			boolean itts=cal.before(calnow);
			return itts;
		}

		private Calendar parseCalendar(String senddate, String sendtime) {
			String sdp[]=senddate.split("/");
			String sdt[]=sendtime.split(":");
			String sschedday=sdp[0];
			String sschedmonth=sdp[1];
			String sschedyear=sdp[2];
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
		
		private void sendScheduledMessage(OUserMap mu, Folder sentFolder, Locale locale, Message m, boolean notify) throws MessagingException {
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

				if (notify) {
					Calendar cal=parseCalendar(getSingleHeaderValue(m,"Sonicle-send-date"),getSingleHeaderValue(m,"Sonicle-send-time"));
					String recipients="";
					for(Address ia: nm.getRecipients(Message.RecipientType.TO)) {
						if (recipients.length()>0) recipients+=" - ";
						recipients+=ia.toString();
					}
					String nmto=nm.getRecipients(Message.RecipientType.TO)[0].toString();
					String nmsubject=nm.getSubject();
					String fmtsubject=lookupResource(locale, MailLocaleKey.SCHEDULED_SENT_SUBJECT);
					String fmthtml=lookupResource(locale, MailLocaleKey.SCHEDULED_SENT_HTML);
					String subject=java.text.MessageFormat.format(fmtsubject,nmto);
					String html=java.text.MessageFormat.format(fmthtml,
							DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT,locale).format(cal.getTime()),
							MailUtils.htmlescape(recipients),
							MailUtils.htmlescape(nmsubject));
					//wta.sendHtmlMessage("webtop@"+mu.domain, mu.email, subject, html);
				}
			} catch(Throwable t) {
				logger.error("Exception",t);
			}
		}
		
		private Store connectAdminStore(OUserMap mu, String adminUser, String adminPassword) throws MessagingException {
			Store store=session.getStore(mu.getMailProtocol());
			store.connect(mu.getMailHost(),mu.getMailPort(),adminUser,adminPassword);
			return store;
		}
    
		private Store connectVmailStore(OUserMap mu, String vmailSecret) throws MessagingException {
			Store store=session.getStore(mu.getMailProtocol());
			store.connect(mu.getMailHost(),mu.getMailPort(),mu.getMailUser()+"*vmail",vmailSecret);
			return store;
		}
		
		/*
		* Implement mail service settings object cache
		*/
		private MailServiceSettings getMailServiceSettings(String domainId) {
			MailServiceSettings mss=hmss.get(domainId);
			if (mss==null) {
				mss=new MailServiceSettings(SERVICE_ID,domainId);
				hmss.put(domainId, mss);
			}
			return mss;
		}
		
		/*
		* Implement mail user settings object cache
		*/
		private MailUserSettings getMailUserSettings(UserProfile.Id pid, MailServiceSettings mss) {
			String domainId=pid.getDomainId();
			String userId=pid.getUserId();
			HashMap<String,MailUserSettings> hmus=hhmus.get(domainId);
			if (hmus==null) {
				hmus=new HashMap();
				hhmus.put(domainId, hmus);
			}
			
			MailUserSettings mus=hmus.get(userId);
			if (mus==null) {
				mus=new MailUserSettings(pid,mss);
				hmus.put(userId, mus);
			}
			return mus;
		}
		
	}
}
