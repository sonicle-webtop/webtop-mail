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
import com.sonicle.mail.Mailbox;
import com.sonicle.mail.MimeUtils;
import com.sonicle.mail.StoreHostParams;
import com.sonicle.mail.StoreUtils;
import com.sonicle.mail.email.CalendarMethod;
import com.sonicle.mail.email.ContentTransferEncoding;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.mail.email.EmailMessageBuilder;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.TplHelper;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.sdk.WTEmailSendException;
import com.sonicle.webtop.core.sdk.BaseBackgroundServiceTask;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.mail.BackgroundService;
import com.sonicle.webtop.mail.MailLocaleKey;
import com.sonicle.webtop.mail.MailServiceSettings;
import com.sonicle.webtop.mail.MailUserSettings;
import com.sonicle.webtop.mail.ManagerUtils;
import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.HeaderTerm;
import jakarta.mail.search.OrTerm;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class ScheduledSendTask extends BaseBackgroundServiceTask {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(ScheduledSendTask.class);
	
	private static final HeaderTerm HT_SONICLE_SENDSCHEDULED = new HeaderTerm("Sonicle-send-scheduled", "true");
	//private static final HeaderTerm HT_XWEBTOPSCHEDULED = new HeaderTerm("X-WebTop-Scheduled", "true");
	
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public void executeWork(JobExecutionContext jec, TaskContext context) throws Exception {
		BackgroundService bs = ((BackgroundService)getBackgroundService(jec));
		
		Map<String, MailServiceSettings> mssCache = new HashMap<>();
		LinkedHashSet<UserProfileId> usersProfiles = new LinkedHashSet<>();
		for (String domainId : WT.getCoreManager().listDomainIds(EnabledCond.ENABLED_ONLY)) {
			if (shouldStop()) break; // Speed-up shutdown process!
			
			LOGGER.debug("Processing domain '{}'... ");
			MailServiceSettings mss = getMailServiceSettings(bs.SERVICE_ID, domainId);
			if (mss.isScheduledEmailsDisabled()) {
				LOGGER.debug("Scheduled emails are disabled for '{}' domain, skipping... ");
				continue;
			}
			mssCache.put(domainId, mss);
			CoreManager coreMgr = WT.getCoreManager(RunContext.buildDomainAdminProfileId(domainId));
			for (String userId : coreMgr.listUserIds(EnabledCond.ENABLED_ONLY)) {
				if (shouldStop()) break; // Speed-up shutdown process!
				LOGGER.trace("User '{}' collected");
				usersProfiles.add(new UserProfileId(domainId, userId));
			}
		}
		
		for (UserProfileId userProfile : usersProfiles) {
			if (shouldStop()) return; // Speed-up shutdown process!
			checkScheduledMessagesForUser(userProfile, mssCache.get(userProfile.getDomainId()), context);
		}
	}
	
	private void checkScheduledMessagesForUser(UserProfileId userProfile, MailServiceSettings mss, TaskContext taskContext) {
		MailUserSettings mus = getMailUserSettings(userProfile, mss);
		String user = WT.buildDomainInternetAddress(userProfile.getDomainId(), userProfile.getUserId(), null).getAddress();
		
		Mailbox mailbox = null;
		Folder drafts = null;
		try {
			StoreHostParams hostParams = mus.getMailboxHostParams(user, null, true);
			LOGGER.debug("[{}] Preparing mailbox...", userProfile);
			mailbox = new Mailbox(hostParams, ManagerUtils.createMailboxConfig(mus), WT.getMailSessionPropsBuilder(false, true).build());
			mailbox.connect();
			
			drafts = StoreUtils.openFolder(mailbox.getSpecialFolder(Mailbox.SpecialFolder.DRAFTS, true), true);
			if (drafts.exists()) {
				checkForScheduledMessagesIntoUserFolder(drafts, userProfile, mus, taskContext);
				
			} else {
				LOGGER.debug("[{}] Out folder ({}) does NOT exist, skipping...", userProfile, drafts.getFullName());
			}
			
		} catch (GeneralSecurityException | MessagingException ex) {
			StoreUtils.closeQuietly(drafts, true);
			if (mailbox != null) mailbox.disconnect();
			LOGGER.error("[{}] Unable to connect mailbox or opening INBOX", userProfile, ex);
		}
	}
	
	private void checkForScheduledMessagesIntoUserFolder(Folder folder, UserProfileId userProfile, MailUserSettings mus, TaskContext taskContext) throws MessagingException {
		//TODO: migrate headers to X- prefixed form! (OrTerm)
		Message messages[] = folder.search(HT_SONICLE_SENDSCHEDULED);
		for (Message message : messages) {
			String messageId = MimeUtils.getFirstHeaderValue(message, MimeUtils.HEADER_MESSAGE_ID);
			if (!StringUtils.isBlank(messageId)) {
				String senddate = MimeUtils.getFirstHeaderValue(message, "Sonicle-send-date");
				String sendtime = MimeUtils.getFirstHeaderValue(message, "Sonicle-send-time");
				if (senddate != null && sendtime != null) {
					boolean sendnotify = StringUtils.equalsIgnoreCase(MimeUtils.getFirstHeaderValue(message, "Sonicle-notify-delivery"), "true");
					if (legacy_isTimeToSend(senddate, sendtime)) {
						try {
							sendMessage(userProfile, mus.getFolderSent(), message, sendnotify, taskContext);
						} catch (WTEmailSendException ex) {
							LOGGER.error("Unable to send message '{}' for '{}'", messageId, userProfile, ex);
						}
					}
				}
			}
		}
	}
	
	private void sendMessage(UserProfileId sendingProfile, String sentFolder, Message message, boolean notify, TaskContext taskContext) throws WTEmailSendException {
		WT.sendEmailMessage(sendingProfile, (MimeMessage)message, sentFolder);
		try {
			if (notify) legacy_notify(sendingProfile, message, taskContext);
		} catch (Exception ex) {
			LOGGER.error("Error sending notification", ex);
		}
	}
	
	private void legacy_notify(UserProfileId sendingProfile, Message message, TaskContext taskContext) throws MessagingException, WTEmailSendException {
		Calendar cal=legacy_parseCalendar(MimeUtils.getFirstHeaderValue(message,"Sonicle-send-date"),MimeUtils.getFirstHeaderValue(message,"Sonicle-send-time"));
		if (cal!=null) {
			String recipients="";
			for(Address ia: message.getRecipients(Message.RecipientType.TO)) {
				if (recipients.length()>0) recipients+=" - ";
				recipients+=ia.toString();
			}
			
			Locale locale = WT.getUserData(sendingProfile).getLocale();
			String nmto=message.getRecipients(Message.RecipientType.TO)[0].toString();
			String nmsubject=message.getSubject();
			String fmtsubject=taskContext.getBackgroundService().lookupResource(locale, MailLocaleKey.SCHEDULED_SENT_SUBJECT);
			String fmthtml=taskContext.getBackgroundService().lookupResource(locale, MailLocaleKey.SCHEDULED_SENT_HTML);
			String subject=java.text.MessageFormat.format(fmtsubject,nmto);
			String html=java.text.MessageFormat.format(fmthtml,
					DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT,locale).format(cal.getTime()),
					MailUtils.htmlescape(recipients),
					MailUtils.htmlescape(nmsubject));
			String toAddress=WT.getUserData(sendingProfile).getEmail().toString();
			
			EmailMessage email = EmailMessageBuilder.startingBlank()
				.from("webtop@"+WT.getPrimaryDomainName(sendingProfile.getDomainId()))
				.to(toAddress)
				.withSubject(subject)
				.withHTMLText(html)
				.build();
			
			WT.sendEmailMessage(RunContext.getRunProfileId(), email);
		}
	}
	
	private boolean legacy_isTimeToSend(String senddate, String sendtime) {
			Calendar cal=legacy_parseCalendar(senddate,sendtime);
			if (cal==null) return false;
			
			Calendar calnow=Calendar.getInstance();
			calnow.setTime(new Date());
			boolean itts=cal.before(calnow);
			return itts;
		}

		private Calendar legacy_parseCalendar(String senddate, String sendtime) {
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
	
	private MailServiceSettings getMailServiceSettings(final String serviceId, final String domainId) {
		return new MailServiceSettings(serviceId, domainId);
	}
	
	private MailUserSettings getMailUserSettings(final UserProfileId profileId, final MailServiceSettings mss) {
		return new MailUserSettings(profileId, mss);
	}
}
