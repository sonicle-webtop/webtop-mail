/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation of the addition of the following permission
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
 * along of this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance of Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
package com.sonicle.webtop.mail.bg;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.LangUtils.ChangeSet;
import com.sonicle.commons.LangUtils.CollectionChangeSet;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.web.json.CId;
import com.sonicle.mail.Mailbox;
import com.sonicle.mail.MailboxConfig;
import com.sonicle.mail.StoreHostParams;
import com.sonicle.mail.StoreProtocol;
import com.sonicle.mail.StoreUtils;
import com.sonicle.mail.email.CalendarMethod;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.mail.parser.MimeMessageParseException;
import com.sonicle.mail.parser.MimeMessageParser;
import com.sonicle.webtop.calendar.ICalendarManager;
import com.sonicle.webtop.calendar.ICalendarManager.HandleICalInviationOption;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.WTEmailSendException;
import com.sonicle.webtop.core.app.sdk.WTConstraintException;
import com.sonicle.webtop.core.app.sdk.WTParseException;
import com.sonicle.webtop.core.app.model.FolderShare;
import com.sonicle.webtop.core.app.model.FolderSharing;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceGetOption;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.msg.ResourceErrorSM;
import com.sonicle.webtop.core.msg.ResourceReservationReplySM;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.ICalendarHelper;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.mail.BackgroundService;
import com.sonicle.webtop.mail.MailServiceSettings;
import com.sonicle.webtop.mail.MailUserSettings;
import com.sonicle.webtop.mail.ManagerUtils;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IdleManager;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.ConnectionListener;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.event.MessageCountListener;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class ResourcesAutoresponderManager {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(ResourcesAutoresponderManager.class);
	private static final String CALENDAR_SERVICE_ID = "com.sonicle.webtop.calendar";
	private final BackgroundService service;
	private final ExecutorService executorService;
	private final IdleManager idleManager;
	private final ConnectionListener inboxesConnectionListener;
	private final MessageCountListener inboxesMessageCountListener;
	private final HashMap<UserProfileId, Mailbox> monitoredMailboxes = new HashMap<>();
	private final BidiMap<UserProfileId, Folder> monitoredFolders = new DualHashBidiMap<>();
	private final KeyedReentrantLocks<UserProfileId> locks = new KeyedReentrantLocks<>();
	private final LoadingCache<String, Boolean> resourceAllowedProfilesCache = Caffeine.newBuilder()
		.expireAfterWrite(1, TimeUnit.MINUTES)
		.build(new ResourceAllowedProfilesCacheLoader());
	
	public ResourcesAutoresponderManager(BackgroundService service) throws IOException {
		this.service = service;
		this.executorService = Executors.newCachedThreadPool();
		this.idleManager = new IdleManager(createIdleManagerSession(), executorService);
		this.inboxesConnectionListener = new InboxConnectionListener(this);
		this.inboxesMessageCountListener = new InboxMessageCountListener(this);
	}
	
	public void cleanup() {
		idleManager.stop();
		executorService.shutdown();
		resourceAllowedProfilesCache.cleanUp();
	}
	
	public void updateMonitoredResources(Set<UserProfileId> resourceProfiles) {
		ChangeSet<UserProfileId> changes  = LangUtils.computeChangeSet(monitoredMailboxes.keySet(), resourceProfiles);
		
		// Clear removed resources
		for (UserProfileId resourceProfile : changes.getRemoved()) {
			try {
				locks.lock(resourceProfile);
				if (changes.getRemoved().contains(resourceProfile)) {
					LOGGER.debug("Removing '{}' from monitored resources [removed]", resourceProfile);
					doRemoveMonitoredResource(resourceProfile);
				}
			} finally {
				locks.unlock(resourceProfile);
			}
		}
		
		// Clear resources with dead connections
		Set<UserProfileId> deads = new LinkedHashSet<>();
		for (UserProfileId resourceProfile : monitoredMailboxes.keySet()) {
			try {
				locks.lock(resourceProfile);
				final Mailbox mailbox = monitoredMailboxes.get(resourceProfile);
				if (mailbox != null && !mailbox.isConnected()) deads.add(resourceProfile);
			} finally {
				locks.unlock(resourceProfile);
			}
		}
		for (UserProfileId resourceProfile : deads) {
			try {
				locks.lock(resourceProfile);
				LOGGER.debug("Removing '{}' from monitored resources [not connected]", resourceProfile);
				doRemoveMonitoredResource(resourceProfile);
			} finally {
				locks.unlock(resourceProfile);
			}
		}
		
		// Adds new resources (if not present yet)
		for (UserProfileId resourceProfile : resourceProfiles) {
			try {
				locks.lock(resourceProfile);
				if (!monitoredMailboxes.containsKey(resourceProfile)) {
					LOGGER.debug("Adding '{}' as monitored resource", resourceProfile);
					doAddMonitoredResource(resourceProfile);
				}
			} finally {
				locks.unlock(resourceProfile);
			}
		}
	}
	
	private void onInboxFolderConnectionClosed(final Folder eventedFolder) {
		
		// Remove resource from monitored list: it will be added 
		// again on next `updateMonitoredResources()` call.
		// This allow to recover from start without losing any potential 
		// message arrived during blackout (time with no idle) period.
		UserProfileId resourceProfile = monitoredFolders.inverseBidiMap().get(eventedFolder);
		if (resourceProfile == null) {
			LOGGER.warn("Monitored folder '{}' was closed, probably due to a connection problem!", eventedFolder.getFullName());
			LOGGER.error("Unable to retrieve resource associated to folder '{}'", eventedFolder.getFullName());
		} else {
			LOGGER.warn("Monitored folder '{}' of '{}' was closed, probably due to a connection problem!", eventedFolder.getFullName(), resourceProfile);
			try {
				LOGGER.debug("Removing '{}' from monitored resources", resourceProfile);
				locks.lock(resourceProfile);
				doRemoveMonitoredResource(resourceProfile);

			} finally {
				locks.unlock(resourceProfile);
			}
		}
	}
	
	private void onInboxFolderMessagesAdded(final Folder eventedFolder, final Message[] messages) {
		UserProfileId resourceProfile = monitoredFolders.inverseBidiMap().get(eventedFolder);
		if (resourceProfile == null) {
			LOGGER.error("Unable to retrieve resource associated to folder '{}'", eventedFolder.getFullName());
		} else {
			boolean die = false;
			try {
				locks.lock(resourceProfile);
				final Mailbox mailbox = monitoredMailboxes.get(resourceProfile);
				if (mailbox != null) {
					handleMessages(resourceProfile, mailbox, eventedFolder, messages);
				} else {
					die = true;
					LOGGER.error("[{}] Unable to retrieve mailbox from cache. Idle will not be restored.", resourceProfile);
					LOGGER.error("[{}] {} message/s ignored", resourceProfile, messages.length);
				}

			} catch (MessagingException ex) {
				LOGGER.error("[{}] Error handling messages", resourceProfile, ex);
			} finally {
				locks.unlock(resourceProfile);
			}

			if (!die) {
				// Keep watching for new messages
				try {
					idleManager.watch(eventedFolder);
					LOGGER.debug("[{}] Started watching '{}' folder", resourceProfile, eventedFolder.getFullName());
				} catch (MessagingException ex) {
					LOGGER.error("[{}] Error continue watching folder '{}'", resourceProfile, eventedFolder.getFullName(), ex);
				}
			}
		}
	}
	
	private void doRemoveMonitoredResource(final UserProfileId resourceProfile) {
		final Mailbox mailbox = monitoredMailboxes.remove(resourceProfile);
		final Folder folder = monitoredFolders.remove(resourceProfile);
		
		try {
			if (folder != null) {
				LOGGER.debug("[{}] Detaching '{}' folder listeners...", resourceProfile, folder.getFullName());
				// Make sure to remove connection listeners before actually closing folder...
				folder.removeMessageCountListener(inboxesMessageCountListener);
				folder.removeConnectionListener(inboxesConnectionListener);
				StoreUtils.close(folder, true);
			}
		} catch (MessagingException ex) {
			LOGGER.error("[{}] Error closing folder", resourceProfile, ex);
		}
		if (mailbox != null) mailbox.disconnect();
	}
	
	private void doAddMonitoredResource(final UserProfileId resourceProfile) {
		MailUserSettings mus = getMailUserSettings(resourceProfile);
		String user = WT.buildDomainInternetAddress(resourceProfile.getDomainId(), resourceProfile.getUserId(), null).getAddress();
		
		boolean cached = false;
		Mailbox mailbox = null;
		Folder inbox = null;
		try {
			StoreHostParams hostParams = mus.getMailboxHostParams(user, null, true);
			LOGGER.debug("[{}] Preparing mailbox...", resourceProfile);
			mailbox = new Mailbox(hostParams, createMailboxConfig(mus), createMailboxProperties(hostParams.getProtocol()));
			mailbox.connect();
			inbox = StoreUtils.openFolder(mailbox.getInboxFolder(), true);
			
			if (inbox != null) {
				LOGGER.debug("[{}] Attaching '{}' folder listeners...", resourceProfile, inbox.getFullName());
				inbox.addConnectionListener(inboxesConnectionListener);
				inbox.addMessageCountListener(inboxesMessageCountListener);
				monitoredMailboxes.put(resourceProfile, mailbox);
				monitoredFolders.put(resourceProfile, inbox);
				cached = true;
			}

		} catch (GeneralSecurityException | MessagingException ex) {
			StoreUtils.closeQuietly(inbox, true);
			if (mailbox != null) mailbox.disconnect();
			LOGGER.error("[{}] Unable to connect mailbox or opening INBOX", resourceProfile, ex);
		}
		if (cached) {
			LOGGER.debug("[{}] Getting '{}' folder (initial) messages...", resourceProfile, inbox.getFullName());
			try {
				final Message[] msgs = inbox.getMessages();
				if (msgs.length > 0) {
					handleMessages(resourceProfile, mailbox, inbox, msgs);
				} else {
					LOGGER.debug("[{}] NO messages found in '{}' folder", resourceProfile, inbox.getFullName());
				}
			} catch (MessagingException ex) {
				LOGGER.error("[{}] Error handling messages", resourceProfile, ex);
			}
			try {
				idleManager.watch(inbox); // Start folder monitoring...
				LOGGER.debug("[{}] Started watching '{}' folder", resourceProfile, inbox.getFullName());
			} catch (MessagingException ex) {
				LOGGER.error("[{}] Error start watching folder '{}'", resourceProfile, inbox.getFullName(), ex);
			}
		}
	}
	
	private void handleMessages(final UserProfileId resourceProfile, final Mailbox mailbox, final Folder inbox, final Message[] messages) throws MessagingException {
		LOGGER.debug("[{}] Processing {} message/s...", resourceProfile, messages.length);
		//UIDFolder uidFolder = ((UIDFolder)inbox);
		// add id to hashmap to avoid multi processing
		// skip messages not containing invitation requests
		
		Folder outFolder = mailbox.getFolder("Outbox", true);
		Folder archiveFolder = mailbox.getSpecialFolder(Mailbox.SpecialFolder.ARCHIVE, true);
		Folder trashFolder = mailbox.getSpecialFolder(Mailbox.SpecialFolder.TRASH, true);
		boolean isDovecot = mailbox.isDovecot();
		
		try {
			//inbox.fetch(messages, StoreUtils.FETCH_PROFILE_UID);
			int i = 0;
			for (Message message : messages) {
				i++;
				final IMAPMessage imessage = ((IMAPMessage)message);
				final String messageId = MimeMessageParser.parseMessageId(imessage, true);
				IMAPMessage omessage = null;
				LOGGER.trace("[{}] Working on message '{}' [#{}]", resourceProfile, messageId, i);
				//long uid = uidFolder.getUID(message);
				
				if (StringUtils.isBlank(messageId)) {
					LOGGER.trace("[{}][{}] Message-ID is missing! Discarding message...", resourceProfile, messageId);
					LOGGER.trace("[{}][{}] Moving message to '{}' folder...", resourceProfile, messageId, trashFolder.getFullName());
					StoreUtils.openFolder(trashFolder, true);
					StoreUtils.moveMessage(imessage, inbox, trashFolder, true);
					continue;
					
				} else {
					// Move message to out folder
					LOGGER.trace("[{}][{}] Moving message to '{}' folder...", resourceProfile, messageId, outFolder.getFullName());
					StoreUtils.openFolder(outFolder, true);
					StoreUtils.moveMessage(imessage, inbox, outFolder, true);
					if (isDovecot) {
						// WARNING: it seems that when remote server is Dovecot, 
						// recently moved messages are hidden to searches unless 
						// you force server to actualize recents issuing a command 
						// before the actual search: here we inject a NOOP before 
						// search command!
						LOGGER.trace("[{}][{}] IMAP server is Dovecot: injecting a NOOP command...", resourceProfile, messageId);
						StoreUtils.issueNoop(outFolder);
					}
					omessage = (IMAPMessage)StoreUtils.getMessageByMessageID(outFolder, messageId);
					if (omessage == null) {
						LOGGER.error("[{}][{}] Unable to retrieve moved message from '{}' folder. Message lost.", resourceProfile, messageId, outFolder.getFullName());
						continue;
					}
				}
				
				// Analyze message parsing its structure
				final MimeMessageParser.ParsedMimeMessageComponents parsed;
				try {
					parsed = MimeMessageParser.parseMimeMessage(omessage, false);
				} catch (MimeMessageParseException ex) {
					LOGGER.trace("[{}][{}] Unable to parse structure, message ignored.", resourceProfile, messageId, ex);
					continue;
				}
				
				// Handle message...
				MessageOperation op = MessageOperation.TRASH;
				CalendarMethod method = parsed.getCalendarMethod();
				if (method != null) op = handleCalendarMessage(resourceProfile, method, parsed.getCalendarContent(), (MimeMessage)omessage);
				
				Folder finalFolder = null;
				if (MessageOperation.ARCHIVE.equals(op)) {
					finalFolder = archiveFolder;
				} else if (MessageOperation.TRASH.equals(op)) {
					finalFolder = trashFolder;
				}
					
				if (finalFolder != null) {
					try {
						LOGGER.trace("[{}][{}] Moving message to '{}' folder...", resourceProfile, messageId, finalFolder.getFullName());
						StoreUtils.openFolder(finalFolder, true);
						StoreUtils.moveMessage(omessage, outFolder, finalFolder, true);
						LOGGER.trace("[{}][{}] Message processed successfully", resourceProfile, messageId);
					} catch (MessagingException ex) {
						LOGGER.trace("[{}][{}] Error moving message to '{}' folder", resourceProfile, messageId, finalFolder, ex);
					}
				} else {
					LOGGER.error("[{}][{}] Message processed with errors: evaluate it manually! See '{}' folder.", resourceProfile, messageId, outFolder.getFullName());
				}
			}
			
		} finally {
			StoreUtils.closeQuietly(outFolder, false);
			StoreUtils.closeQuietly(archiveFolder, false);
			StoreUtils.closeQuietly(trashFolder, false);
		}
	}
	
	private Properties createMailboxProperties(StoreProtocol protocol) {
		return WT.getMailSessionPropsBuilder(false, true)
			.withEnableIMAPEvents()
			.withProperty(protocol, "usesocketchannels", "true")
			.build();
	}
	
	private MailboxConfig createMailboxConfig(MailUserSettings mus) {
		return new MailboxConfig.Builder()
			.withUserFoldersPrefix(mus.getFolderPrefix())
			.withSentFolderName(mus.getFolderSent())
			.withDraftsFolderName(mus.getFolderDrafts())
			.withTrashFolderName(mus.getFolderTrash())
			.withSpamFolderName(mus.getFolderSpam())
			.withArchiveFolderName(mus.getFolderArchive())
			.build();
	}
	
	private enum MessageOperation {
		ARCHIVE, TRASH, NOOP;
	}
	
	private MessageOperation handleCalendarMessage(final UserProfileId resourceProfileId, final CalendarMethod calendarMethod, final String calendarContent, final MimeMessage message) {
		final String messageId = MimeMessageParser.parseMessageId(message, true);
		
		// Find resource personal address
		final InternetAddress resourceIa = getProfilePersonalEmail(resourceProfileId);
		if (resourceIa == null) { // Exit quickly without any other computation...
			LOGGER.trace("[{}] Resource does NOT have valid personal address", resourceProfileId);
			return MessageOperation.TRASH;
		}
		
		// Extracts Message's from-address and check if it is known
		final InternetAddress from = MimeMessageParser.parseFrom(message, true);
		if (from == null) {
			LOGGER.trace("[{}][{}] Message's FROM is NOT valid", resourceProfileId, messageId);
			return MessageOperation.TRASH;
		}
		UserProfileId organizerProfile = WT.guessProfileIdByPersonalAddress(from.getAddress());
		if (organizerProfile == null) organizerProfile = WT.guessProfileIdByAuthAddress(from.getAddress());
		if (organizerProfile == null) {
			LOGGER.trace("[{}][{}] Message's FROM '{}' does NOT belong to any user", resourceProfileId, messageId, from.getAddress());
			return MessageOperation.TRASH;
		}
		
		// Checks if recognised profile has rights to use the resource
		if (!isResourceAllowed(resourceProfileId, organizerProfile)) {
			LOGGER.trace("[{}][{}] Profile '{}' is NOT allowed to use resource", resourceProfileId, messageId, organizerProfile);
			
			try {
				CoreManager coreMgr = getCoreManager(resourceProfileId);
				Resource resource = coreMgr.getResource(resourceProfileId.getUserId(), BitFlags.noneOf(ResourceGetOption.class));
				WT.notify(organizerProfile, new ResourceErrorSM(CALENDAR_SERVICE_ID, resource, "notallowed"));
			} catch (WTException ex1) {
				LOGGER.warn("[{}][{}] Error generating organizer notification", resourceProfileId, messageId, ex1);
			}
			return MessageOperation.TRASH;
		}
		
		List<InternetAddress> tos = MimeMessageParser.parseToAddresses(message, true);
		if (!InternetAddressUtils.isInList(resourceIa, tos)) {
			LOGGER.trace("[{}][{}] Message's TO does NOT match with resource address '{}'", resourceProfileId, messageId, resourceIa.getAddress());
			return MessageOperation.TRASH;
		}
		
		// Parse Calendar content
		final Calendar iCal;
		try {
			iCal = ICalendarUtils.parse(calendarContent);
		} catch (IOException | ParserException ex) {
			LOGGER.trace("[{}][{}] Error parsing iCalendar content", resourceProfileId, messageId, ex);
			return MessageOperation.TRASH;
		}
		
		ICalendarManager calMgr = getCalendarManager(resourceProfileId);
		try {
			final VEvent ve = ICalendarUtils.getVEvent(iCal);
			if (ve == null) throw new WTParseException("Calendar object does not contain any events");
			final String uid = ICalendarUtils.getUidValue(ve);
			if (StringUtils.isBlank(uid)) throw new WTParseException("Event object does not provide a valid Uid");
			final InternetAddress iaOrganizer = ICalendarUtils.getOrganizerAddress(ve);
			if (iaOrganizer == null) throw new WTParseException("Event object does not provide a valid Organizer");
			
			if (CalendarMethod.REQUEST.equals(calendarMethod)) {
				final int calendarId = calMgr.getBuiltInCalendarId();
				final String prodId = ICalendarUtils.buildProdId(ManagerUtils.getProductName());
				String sentFolder = getMailUserSettings(resourceProfileId).getFolderSent();
				EmailMessage reply = null;
				PartStat response = null;
				try {
					response = PartStat.ACCEPTED;
					// Prepares reply before actually inserting the event, this allows a clever management in case of errors!
					reply = ICalendarHelper.prepareICalendarReply(prodId, iCal, resourceIa, iaOrganizer, response, getProfileLocale(resourceProfileId));
					
					final BitFlags<HandleICalInviationOption> options = BitFlags.with(
							HandleICalInviationOption.CONSTRAIN_AVAILABILITY,
							HandleICalInviationOption.EVENT_LOOKUP_SCOPE_STRICT,
							HandleICalInviationOption.IGNORE_ICAL_CLASSIFICATION,
							HandleICalInviationOption.IGNORE_ICAL_TRASPARENCY,
							HandleICalInviationOption.IGNORE_ICAL_ALARMS
						);
					final Event event = calMgr.handleInvitationFromICal(iCal, calendarId, options);
					
				} catch (WTConstraintException ex1) {
					try {
						calMgr.deleteEvent(uid, calendarId, false);
					} catch (Exception ex2) {
						LOGGER.warn("[{}][{}] Error deleting previous reservation", resourceProfileId, messageId, ex2);
					}
					
					response = PartStat.DECLINED;
					// The following should not fail, all infos have been already verified in prepareICalendarReply in try section above...
					reply = ICalendarHelper.prepareICalendarReply(prodId, iCal, resourceIa, iaOrganizer, response, getProfileLocale(resourceProfileId));	
				}
				
				try {
					CoreManager coreMgr = getCoreManager(resourceProfileId);
					Resource resource = coreMgr.getResource(resourceProfileId.getUserId(), BitFlags.noneOf(ResourceGetOption.class));
					WT.notify(organizerProfile, new ResourceReservationReplySM(CALENDAR_SERVICE_ID, resource, uid, ICalendarUtils.getSummary(ve), response));
				} catch (WTException ex1) {
					LOGGER.warn("[{}][{}] Error generating organizer notification", resourceProfileId, messageId, ex1);
				}
				
				try {
					WT.sendEmail(resourceProfileId, reply, sentFolder);
					return MessageOperation.ARCHIVE;
					
				} catch (WTEmailSendException ex1) {
					if (!ex1.isMessageSent()) {
						LOGGER.error("[{}][{}] Unable to send reply message", resourceProfileId, messageId, ex1);
						return MessageOperation.NOOP;
					} else if (!ex1.isMessageMoved()) {
						LOGGER.error("[{}][{}] Unable to move message into '{}' folder, but reply message was successfully sent.", resourceProfileId, messageId, sentFolder, ex1);
						return MessageOperation.ARCHIVE;
					} else {
						return MessageOperation.ARCHIVE;
					}
				}
				
			} else if (CalendarMethod.CANCEL.equals(calendarMethod)) {
				final BitFlags<HandleICalInviationOption> options = BitFlags.with(
						HandleICalInviationOption.EVENT_LOOKUP_SCOPE_STRICT
					);
				calMgr.handleInvitationFromICal(iCal, null, options);
				return MessageOperation.ARCHIVE;
				
			} else {
				// Method not supported for resources, ignore the message!
				return MessageOperation.TRASH;
			}
			
		} catch (WTParseException ex) {
			return MessageOperation.TRASH;
			
		} catch (Exception ex) {
			// Do not touch message allowing admins to dig into the problem
			LOGGER.error("[{}][{}] Unexpected error", resourceProfileId, messageId, ex);
			return MessageOperation.NOOP;
		}
	}
	
	private static class InboxConnectionListener implements ConnectionListener {
		private final ResourcesAutoresponderManager manager;
		
		public InboxConnectionListener(ResourcesAutoresponderManager manager) {
			this.manager = manager;
		}
		
		@Override
		public void opened(ConnectionEvent ce) {}

		@Override
		public void disconnected(ConnectionEvent ce) {}

		@Override
		public void closed(ConnectionEvent ce) {
			if (ce.getSource() instanceof IMAPFolder) {
				final Folder eventedFolder = (Folder)ce.getSource();
				manager.onInboxFolderConnectionClosed(eventedFolder);
			}
		}
	}
	
	private static class InboxMessageCountListener implements MessageCountListener {
		private final ResourcesAutoresponderManager manager;
		
		public InboxMessageCountListener(ResourcesAutoresponderManager manager) {
			this.manager = manager;
		}
		
		@Override
		public void messagesAdded(MessageCountEvent mce) {
			final Folder eventedFolder = (Folder)mce.getSource();
			final Message[] messages = mce.getMessages();
			manager.onInboxFolderMessagesAdded(eventedFolder, messages);
		}

		@Override
		public void messagesRemoved(MessageCountEvent mce) {}
	}
	
	private Locale getProfileLocale(UserProfileId profileId) {
		UserProfile.Data pdata = WT.getUserData(profileId);
		return (pdata == null) ? LangUtils.languageTagToLocale("en_EN") : pdata.getLocale();
	}
	
	private InternetAddress getProfilePersonalEmail(UserProfileId profileId) {
		UserProfile.Data pdata = WT.getUserData(profileId);
		return (pdata == null) ? null : pdata.getPersonalEmail();
	}
	
	private Session createIdleManagerSession() {
		return Session.getInstance(WT.getMailSessionPropsBuilder(false, false).build());
	}
	
	private MailUserSettings getMailUserSettings(UserProfileId profileId) {
		return getMailUserSettings(profileId, new MailServiceSettings(service.SERVICE_ID, profileId.getDomainId()));
	}
	
	private MailUserSettings getMailUserSettings(UserProfileId profileId, MailServiceSettings mss) {
		return new MailUserSettings(profileId, mss);
	}
	
	private ICalendarManager getCalendarManager(UserProfileId profileId) {
		return (ICalendarManager)WT.getServiceManager(CALENDAR_SERVICE_ID, true, profileId);
	}
	
	private CoreManager getCoreManager(UserProfileId profileId) {
		return WT.getCoreManager(true, profileId);
	}
	
	private boolean isResourceAllowed(UserProfileId resourceId, UserProfileId userId) {
		return resourceAllowedProfilesCache.get(CId.build(resourceId.toString(), userId.toString()).toString());
	}
	
	private class ResourceAllowedProfilesCacheLoader implements CacheLoader<String, Boolean> {

		@Override
		public Boolean load(String k) throws Exception {
			try {
				LOGGER.trace("[ResourceAllowedProfilesCache] Loading... [{}]", k);
				CId cid = new CId(k);
				UserProfileId resourceProfileId = new UserProfileId(cid.getToken(0));
				UserProfileId userProfileId = new UserProfileId(cid.getToken(1));
				
				CoreManager coreMgr = getCoreManager(userProfileId);
				return coreMgr.evaluateFolderSharePermission(CoreManifest.ID, "RESOURCE", resourceProfileId, FolderSharing.Scope.wildcard(), true, FolderShare.EvalTarget.FOLDER, ServicePermission.ACTION_READ);
				
			} catch (Exception ex) {
				LOGGER.error("[ResourceAllowedProfilesCache] Lookup error [{}]", ex, k);
				return false;
			}
		}
	}
}
