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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.mail.Mailbox;
import com.sonicle.mail.MimeUtils;
import com.sonicle.mail.StoreHostParams;
import com.sonicle.mail.StoreUtils;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.mail.email.EmailMessageBuilder;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.sdk.WTEmailSendException;
import com.sonicle.webtop.core.sdk.BaseBackgroundServiceTask;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.mail.BackgroundService;
import com.sonicle.webtop.mail.MailLocaleKey;
import com.sonicle.webtop.mail.MailServiceSettings;
import com.sonicle.webtop.mail.MailUserSettings;
import com.sonicle.webtop.mail.ManagerUtils;
import com.sonicle.webtop.mail.SimpleMessage;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.HeaderTerm;
import jakarta.mail.search.OrTerm;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
	
	public static final String HEADER_X_SCHEDULED = "X-WT-Scheduled";
	public static final String HEADER_X_SENDAT = "X-WT-Scheduled-SendAt";
	public static final String HEADER_X_NOTIFY_SENDER = "X-WT-Scheduled-Notify-Sender";
	private static final HeaderTerm HT_X_SCHEDULED = new HeaderTerm(HEADER_X_SCHEDULED, "true");
	private static final String HEADER_X_SCHEDULED_LEGACY = "Sonicle-send-scheduled";
	private static final HeaderTerm HT_X_SCHEDULED_LEGACY = new HeaderTerm(HEADER_X_SCHEDULED_LEGACY, "true");
	
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
			
			LOGGER.debug("Processing domain '{}'... ", domainId);
			MailServiceSettings mss = getMailServiceSettings(bs.SERVICE_ID, domainId);
			if (mss.isScheduledEmailsDisabled()) {
				LOGGER.debug("Scheduled emails are disabled for '{}' domain, skipping... ");
				continue;
			}
			mssCache.put(domainId, mss);
			CoreManager coreMgr = WT.getCoreManager(RunContext.buildDomainAdminProfileId(domainId));
			Set<String> userIds = coreMgr.listUserIds(EnabledCond.ENABLED_ONLY);
			List<String> consideredUsers = (LOGGER.isTraceEnabled()) ? new ArrayList(userIds.size()) : null;
			for (String userId : userIds) {
				if (shouldStop()) break; // Speed-up shutdown process!
				usersProfiles.add(new UserProfileId(domainId, userId));
				if (consideredUsers != null) consideredUsers.add(userId);
			}
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Considering users: {}", LangUtils.joinStrings(", ", consideredUsers));
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
			mailbox = new Mailbox(hostParams, ManagerUtils.createMailboxConfig(mus), WT.getMailSessionPropsBuilder(false, true).withDebug().build());
			mailbox.connect();
			
			drafts = StoreUtils.openFolder(mailbox.getSpecialFolder(Mailbox.SpecialFolder.DRAFTS, true), true);
			if (drafts.exists()) {
				checkForScheduledMessagesIntoUserFolder(drafts, userProfile, mus, taskContext);
				
			} else {
				LOGGER.debug("[{}] Out folder ({}) does NOT exist, skipping...", userProfile, drafts.getFullName());
			}
			
		} catch (GeneralSecurityException | MessagingException ex) {
			LOGGER.error("[{}] Unable to connect mailbox or opening INBOX", userProfile, ex);
		} finally {
			StoreUtils.closeQuietly(drafts, true);
			if (mailbox != null) mailbox.disconnect();
		}
	}
	
	private boolean shouldSend(final DateTime now, final DateTime sentAt) {
		if (now == null || sentAt == null) return false;
		return now.compareTo(sentAt) > 0;
	}
	
	private void checkForScheduledMessagesIntoUserFolder(final Folder folder, final UserProfileId userProfile, final MailUserSettings mus, final TaskContext taskContext) throws MessagingException {
		//TODO: cleanup code when compatibility period is over
		Message messages[] = folder.search(new OrTerm(HT_X_SCHEDULED, HT_X_SCHEDULED_LEGACY));
		//Message messages[] = folder.search(HT_XWEBTOP_SCHEDULED); // <-- uncomment when transition is done!
		for (Message message : messages) {
			String messageId = MimeUtils.getFirstHeaderValue(message, MimeUtils.HEADER_MESSAGE_ID);
			if (!StringUtils.isBlank(messageId)) {
				final boolean legacy = StringUtils.equalsIgnoreCase(MimeUtils.getFirstHeaderValue(message, HEADER_X_SCHEDULED_LEGACY), "true");
				if (!legacy) {
					DateTime sendAt = getScheduleHeaderSendAt(message);
					if (shouldSend(taskContext.getExecuteInstant(), sendAt)) {
						boolean notifySender = getScheduleHeaderNotifySender(message);
						try {
							sendMessage(userProfile, mus.getFolderSent(), message, notifySender, taskContext);
							
						} catch (WTEmailSendException ex) {
							LOGGER.error("Unable to send message '{}' for '{}'", messageId, userProfile, ex);
						}
					}
					
				} else {
					String senddate = MimeUtils.getFirstHeaderValue(message, "Sonicle-send-date");
					String sendtime = MimeUtils.getFirstHeaderValue(message, "Sonicle-send-time");
					if (senddate != null && sendtime != null) {
						boolean sendnotify = StringUtils.equalsIgnoreCase(MimeUtils.getFirstHeaderValue(message, "Sonicle-notify-delivery"), "true");
						if (legacy_isTimeToSend(senddate, sendtime)) {
							try {
								legacy_sendMessage(userProfile, mus.getFolderSent(), message, sendnotify, taskContext);
								
							} catch (WTEmailSendException ex) {
								LOGGER.error("Unable to send message '{}' for '{}'", messageId, userProfile, ex);
							}
						}
					}
				}	
			}
		}
	}
	
	private void sendMessage(final UserProfileId sendingProfile, final String sentFolder, final Message scheduledMessage, final boolean notify, final TaskContext taskContext) throws MessagingException, WTEmailSendException {
		MimeMessage mm = new MimeMessage((MimeMessage)scheduledMessage);
		clearScheduleHeaders(mm);
		
		WT.sendEmailMessage(sendingProfile, mm, sentFolder);
		scheduledMessage.setFlag(Flags.Flag.DELETED, true);
		
		try {
			if (notify) sendNotification(sendingProfile, scheduledMessage, taskContext);
		} catch (Exception ex) {
			LOGGER.error("Error sending notification", ex);
		}
	}
	
	private void sendNotification(final UserProfileId sendingProfile, final Message scheduledMessage, final TaskContext taskContext) throws MessagingException, WTEmailSendException {
		DateTime sendAt = JodaTimeUtils.parseDateTimeISO(MimeUtils.getFirstHeaderValue(scheduledMessage, HEADER_X_SENDAT));
		UserProfile.Data ud = WT.getProfileData(sendingProfile);
		
		String firstTo = scheduledMessage.getRecipients(Message.RecipientType.TO)[0].toString();
		String allRecipients = "";
		for (Address address : scheduledMessage.getRecipients(Message.RecipientType.TO)) {
			if (allRecipients.length() > 0) allRecipients += ", ";
			allRecipients += address.toString();
		}
		
		String subject = MessageFormat.format(taskContext.getBackgroundService().lookupResource(ud.getLocale(), MailLocaleKey.SCHEDULED_SENT_SUBJECT), firstTo);
		String html = MessageFormat.format(taskContext.getBackgroundService().lookupResource(ud.getLocale(), MailLocaleKey.SCHEDULED_SENT_HTML), 
			JodaTimeUtils.printYMDHMS(ud.getTimeZone(), sendAt),
			LangUtils.encodeForHTMLContent(allRecipients),
			LangUtils.encodeForHTMLContent(scheduledMessage.getSubject()));
		String toAddress = WT.getProfileData(sendingProfile).getPersonalEmail().toString();
		
		EmailMessage email = EmailMessageBuilder.startingBlank()
			.from(WT.getNoReplyAddress(sendingProfile.getDomainId()))
			.to(toAddress)
			.withSubject(subject)
			.withHTMLText(html)
			.build();
		
		WT.sendEmailMessage(RunContext.getRunProfileId(), email);
	}
	
	
	@Deprecated private void legacy_sendMessage(UserProfileId sendingProfile, String sentFolder, Message message, boolean notify, TaskContext taskContext) throws MessagingException, WTEmailSendException {
		MimeMessage mm = new MimeMessage((MimeMessage)message);
		mm.removeHeader(HEADER_X_SCHEDULED_LEGACY);
		mm.removeHeader("Sonicle-send-date");
		mm.removeHeader("Sonicle-send-time");
		mm.removeHeader("Sonicle-notify-delivery");
		
		WT.sendEmailMessage(sendingProfile, mm, sentFolder);
		message.setFlag(Flags.Flag.DELETED, true);
		
		try {
			if (notify) legacy_notify(sendingProfile, message, taskContext);
		} catch (Exception ex) {
			LOGGER.error("Error sending notification", ex);
		}
	}
	
	@Deprecated private void legacy_notify(UserProfileId sendingProfile, Message message, TaskContext taskContext) throws MessagingException, WTEmailSendException {
		Calendar cal=legacy_parseCalendar(MimeUtils.getFirstHeaderValue(message,"Sonicle-send-date"),MimeUtils.getFirstHeaderValue(message,"Sonicle-send-time"));
		if (cal!=null) {
			String recipients="";
			for(Address ia: message.getRecipients(Message.RecipientType.TO)) {
				if (recipients.length()>0) recipients+=" - ";
				recipients+=ia.toString();
			}
			
			Locale locale = WT.getProfileData(sendingProfile).getLocale();
			String nmto=message.getRecipients(Message.RecipientType.TO)[0].toString();
			String nmsubject=message.getSubject();
			String fmtsubject=taskContext.getBackgroundService().lookupResource(locale, MailLocaleKey.SCHEDULED_SENT_SUBJECT);
			String fmthtml=taskContext.getBackgroundService().lookupResource(locale, MailLocaleKey.SCHEDULED_SENT_HTML);
			String subject=java.text.MessageFormat.format(fmtsubject,nmto);
			String html=java.text.MessageFormat.format(fmthtml,
					DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT,locale).format(cal.getTime()),
					MailUtils.htmlescape(recipients),
					MailUtils.htmlescape(nmsubject));
			String toAddress=WT.getProfileData(sendingProfile).getPersonalEmail().toString();
			
			EmailMessage email = EmailMessageBuilder.startingBlank()
				.from("webtop@"+WT.getPrimaryDomainName(sendingProfile.getDomainId()))
				.to(toAddress)
				.withSubject(subject)
				.withHTMLText(html)
				.build();
			
			WT.sendEmailMessage(RunContext.getRunProfileId(), email);
		}
	}
	
	@Deprecated private boolean legacy_isTimeToSend(String senddate, String sendtime) {
			Calendar cal=legacy_parseCalendar(senddate,sendtime);
			if (cal==null) return false;
			
			Calendar calnow=Calendar.getInstance();
			calnow.setTime(new Date());
			boolean itts=cal.before(calnow);
			return itts;
		}

	@Deprecated private Calendar legacy_parseCalendar(String senddate, String sendtime) {
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
	
	public static void applyScheduleHeaders(final SimpleMessage message, final DateTime sendAt, final boolean notifySender) {
		message.addHeaderLine(HEADER_X_SCHEDULED + ": true");
		message.addHeaderLine(ScheduledSendTask.HEADER_X_SENDAT + ": " + JodaTimeUtils.printISO(sendAt));
		message.addHeaderLine(ScheduledSendTask.HEADER_X_NOTIFY_SENDER + ": " + String.valueOf(notifySender));
	}
	
	public static void clearScheduleHeaders(final MimeMessage message) throws MessagingException {
		message.removeHeader(HEADER_X_SCHEDULED);
		message.removeHeader(HEADER_X_SENDAT);
		message.removeHeader(HEADER_X_NOTIFY_SENDER);
	}
	
	public static boolean hasScheduledSendHeader(final Message message) throws MessagingException {
		return StringUtils.equalsIgnoreCase(MimeUtils.getFirstHeaderValue(message, HEADER_X_SCHEDULED), "true");
	}
	
	public static DateTime getScheduleHeaderSendAt(final Message message) throws MessagingException {
		return JodaTimeUtils.parseDateTimeISO(MimeUtils.getFirstHeaderValue(message, HEADER_X_SENDAT));
	}
	
	public static boolean getScheduleHeaderNotifySender(final Message message) throws MessagingException {
		return StringUtils.equalsIgnoreCase(MimeUtils.getFirstHeaderValue(message, HEADER_X_NOTIFY_SENDER), "true");
	}
}
