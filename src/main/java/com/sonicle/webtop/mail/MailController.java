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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.flags.BitFlagsEnum;
import com.sonicle.mail.Mailbox;
import com.sonicle.mail.MailboxConfig;
import com.sonicle.mail.StoreHostParams;
import com.sonicle.mail.StoreUtils;
import com.sonicle.security.auth.directory.LdapNethDirectory;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.auth.LdapWebTopDirectory;
import com.sonicle.webtop.core.app.auth.WebTopDirectory;
import com.sonicle.webtop.core.app.events.ResourceUpdateEvent;
import com.sonicle.webtop.core.app.events.UserUpdateEvent;
import com.sonicle.webtop.core.app.sdk.WTOperationalException;
import com.sonicle.webtop.core.app.sdk.interfaces.IControllerServiceHooks;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Set;
import net.engio.mbassy.listener.Handler;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class MailController extends BaseController implements IControllerServiceHooks {
	private static final Logger LOGGER = WT.getLogger(MailController.class);
	private static ServiceVersion V_5_0_14 = new ServiceVersion("5.0.14");
	private static ServiceVersion V_5_7_9 = new ServiceVersion("5.7.9");
	
	@Override
	public void initProfile(ServiceVersion current, UserProfileId profileId) throws WTException {
		MailManager manager = new MailManager(true, profileId);
		//manager.addOldBuiltinTags();
	}
	
	@Override
	public void upgradeProfile(ServiceVersion current, UserProfileId profileId, ServiceVersion profileLastSeen) throws WTException {
		if (current.compareTo(V_5_0_14)>=0 && profileLastSeen.compareTo(V_5_0_14)<0) {
			MailManager manager = new MailManager(true, profileId);
			manager.addOldBuiltinTags();
		}
		
		if (current.compareTo(V_5_7_9)>=0 && profileLastSeen.compareTo(V_5_7_9)<0) {
			MailManager manager = new MailManager(true, profileId);
			manager.convertToCoreTags(profileId);
		}
	}
	
	@Handler
	public void onUserUpdateEvent(UserUpdateEvent event) {
		if (UserUpdateEvent.Type.CREATE.equals(event.getType())) {
			try {
				CoreManager coreMgr = WT.getCoreManager(true, event.getUserProfileId());
				String dirScheme = coreMgr.getAuthDirectoryScheme();
				MailServiceSettings mss = createMailServiceSettings(event.getUserProfileId());
				BitFlags<MailboxCreateOption> options = BitFlags.noneOf(MailboxCreateOption.class);
				if (WebTopDirectory.SCHEME.equals(dirScheme) || LdapWebTopDirectory.SCHEME.equals(dirScheme)) {
					options.set(MailboxCreateOption.CREATE_MAILBOX);
					options.set(MailboxCreateOption.CONFIGURE_SIEVE);
				}
				if (mss.isAutocreateSpecialFolders()) options.set(MailboxCreateOption.CREATE_DEFAULT_FOLDERS);
				
				createMailbox(coreMgr.getAuthDirectoryScheme(), event.getUserProfileId(), options, mss);

			} catch (Exception ex) {
				throw new WTRuntimeException(ex, "Error initializing mailbox for '{}': \"{}\"", event.getUserProfileId().toString(), ex.getMessage());
			}
			
		} else if (UserUpdateEvent.Type.DELETE.equals(event.getType())) {
			try {
				deleteMailbox(event.getUserProfileId());
				
			} catch (Exception ex) {
				throw new WTRuntimeException(ex, "Error deleting mailbox for '{}': \"{}\"", event.getUserProfileId().toString(), ex.getMessage());
			}
		}
	}
	
	@Handler
	public void onResourceUpdateEvent(ResourceUpdateEvent event) {
		if (ResourceUpdateEvent.Type.CREATE.equals(event.getType())) {
			try {
				CoreManager coreMgr = WT.getCoreManager(true, event.getResourceProfileId());
				String dirScheme = coreMgr.getAuthDirectoryScheme();
				BitFlags<MailboxCreateOption> options = BitFlags.noneOf(MailboxCreateOption.class);
				if (WebTopDirectory.SCHEME.equals(dirScheme) || LdapWebTopDirectory.SCHEME.equals(dirScheme)) {
					options.set(MailboxCreateOption.CREATE_MAILBOX);
					options.set(MailboxCreateOption.CONFIGURE_SIEVE);
				} else if (LdapNethDirectory.SCHEME.equals(dirScheme)) {
					options.set(MailboxCreateOption.CREATE_MAILBOX);
				}
				options.set(MailboxCreateOption.CREATE_DEFAULT_FOLDERS);
				
				createMailbox(coreMgr.getAuthDirectoryScheme(), event.getResourceProfileId(), options, createMailServiceSettings(event.getResourceProfileId()));
				
			} catch (Exception ex) {
				throw new WTRuntimeException(ex, "Error initializing mailbox for '{}': \"{}\"", event.getResourceProfileId().toString(), ex.getMessage());
			}
		} else if (ResourceUpdateEvent.Type.DELETE.equals(event.getType())) {
			try {
				deleteMailbox(event.getResourceProfileId());
				
			} catch (Exception ex) {
				throw new WTRuntimeException(ex, "Error deleting mailbox for '{}': \"{}\"", event.getResourceProfileId().toString(), ex.getMessage());
			}
		}
	}
	
	private void createMailbox(final String dirScheme, final UserProfileId profileId, final BitFlags<MailboxCreateOption> options, final MailServiceSettings mss) throws WTOperationalException, WTException  {
		MailUserSettings mus = new MailUserSettings(profileId, mss);
		String user = WT.buildDomainInternetAddress(profileId.getDomainId(), profileId.getUserId(), null).getAddress();
		
		if (options.has(MailboxCreateOption.CREATE_MAILBOX)) {
			if (WebTopDirectory.SCHEME.equals(dirScheme) || LdapWebTopDirectory.SCHEME.equals(dirScheme)) {
				try {
					if (WebTopDirectory.SCHEME.equals(dirScheme)) user = profileId.getUserId();

					final StoreHostParams hostParams = mss.getMailboxHostParamsAsAdmin();
					Set<String> acls = LangUtils.asSet(user + ":" + StoreUtils.FOLDER_FULL_RIGHTS, hostParams.getUsername() + ":" + StoreUtils.FOLDER_FULL_RIGHTS);
					StoreUtils.createMailbox(StoreUtils.createSession(hostParams, 1, WT.getProperties()), hostParams.getProtocol(), "user", user, acls);
					
				} catch (Exception ex) {
					throw new WTOperationalException("MAILBOX", ex, "Error creating mailbox");
				}
				
			} else if (LdapNethDirectory.SCHEME.equals(dirScheme)) {
				// This scheme does not really support mailbox creation, 
				// so only check if exists and otherwise rise an error!
				
				try {
					//final StoreHostParams hostParams = mss.getMailboxHostParamsAsVMail(profileId.getDomainId());
					//StoreUtils.createMailbox(StoreUtils.createSession(hostParams, 1, WT.getProperties()), hostParams.getProtocol(), null, profileId.getUserId());
					final StoreHostParams hostParams = mus.getMailboxHostParams(user, null, true);
					Mailbox mailbox = null;
					Folder inbox = null;
					try {
						mailbox = new Mailbox(hostParams, createMailboxConfig(mus), WT.getMailSessionPropsBuilder(false, true).build());
						inbox = mailbox.getInboxFolder();
						if (inbox == null || !inbox.exists()) throw new WTException("Root folder not exists");
					
					} finally {
						StoreUtils.closeQuietly(inbox, true);
						if (mailbox != null) mailbox.disconnect();
					}
				
				} catch (Exception ex) {
					throw new WTOperationalException("MAILBOX", ex, "Mailbox seems NOT existing: have you already created it?");
				}
			}
		}
		
		if (options.has(MailboxCreateOption.CREATE_DEFAULT_FOLDERS)) {
			try {
				final String prefix = mus.getFolderPrefix();
				if (WebTopDirectory.SCHEME.equals(dirScheme) || LdapWebTopDirectory.SCHEME.equals(dirScheme)) {
					final StoreHostParams hostParams = mus.getMailboxHostParams(user, null, true);
					Store store = null;
					try {
						store = StoreUtils.open(StoreUtils.createSession(hostParams, 1, WT.getProperties()), hostParams.getProtocol());
						// https://github.com/cyrusimap/cyrus-imapd/issues/2592
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderSent(), prefix), LangUtils.asSet("/specialuse:\\Sent"));
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderDrafts(), prefix), LangUtils.asSet("/specialuse:\\Drafts"));
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderTrash(), prefix), LangUtils.asSet("/specialuse:\\Trash"));
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderSpam(), prefix), LangUtils.asSet("/specialuse:\\Junk"));
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderArchive(), prefix), LangUtils.asSet("/specialuse:\\Archive"));

					} finally {
						StoreUtils.closeQuietly(store);
					}
					
				} else if (LdapNethDirectory.SCHEME.equals(dirScheme)) {
					final StoreHostParams hostParams = mus.getMailboxHostParams(user, null, true);
					Store store = null;
					try {
						store = StoreUtils.open(StoreUtils.createSession(hostParams, 1, WT.getProperties()), hostParams.getProtocol());
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderSent(), prefix));
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderDrafts(), prefix));
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderTrash(), prefix));
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderSpam(), prefix));
						StoreUtils.createFolderIfNecessary(store, StoreUtils.toBaseFolderName(mss.getDefaultFolderArchive(), prefix));

					} finally {
						StoreUtils.closeQuietly(store);
					}
				}

			} catch (Exception ex) {
				throw new WTOperationalException("MAILBOX-DEFAULTFOLDERS", ex, "Error creating mailbox default folders");
			}
		}
		
		// If enabled, initialize (create & activate) default Sieve script (eg. for SPAM rule)
		if (options.has(MailboxCreateOption.CONFIGURE_SIEVE)) {
			try {
				MailManager mailMgr = (MailManager)WT.getServiceManager(SERVICE_ID, true, profileId);
				mailMgr.setSieveConfiguration(mss.getDefaultHost(), mss.getSievePort(), mss.getAdminUser(), mss.getAdminPassword(), user);
				mailMgr.initDefaultSieveScript();

			} catch (Exception ex) {
				throw new WTOperationalException("SIEVE", ex, "Error initializing sieve");
			}
		}
	}
	
	private void createMailboxFolders(final StoreHostParams hostParams, final Collection<String> folders, final String folderPrefix) throws GeneralSecurityException, MessagingException {
		Store store = null;
		try {
			store = StoreUtils.open(StoreUtils.createSession(hostParams, 1, WT.getProperties()), hostParams.getProtocol());
			for (String folder : folders) {
				final String name = StoreUtils.toBaseFolderName(folder, folderPrefix);
				StoreUtils.createFolderIfNecessary(store, name);
			}
			
		} finally {
			StoreUtils.closeQuietly(store);
		}
	}
	
	private void deleteMailbox(final UserProfileId profileId) throws WTException  {
		CoreManager coreMgr = WT.getCoreManager(true, profileId);
		String dirScheme = coreMgr.getAuthDirectoryScheme();
		if (WebTopDirectory.SCHEME.equals(dirScheme) || LdapWebTopDirectory.SCHEME.equals(dirScheme)) {
			// Defines the mailbox's username
			String user = null;
			if (LdapWebTopDirectory.SCHEME.equals(dirScheme)) {
				user = WT.buildDomainInternetAddress(profileId.getDomainId(), profileId.getUserId(), null).getAddress();
			} else {
				user = profileId.getUserId();
			}
			
			//TODO: Mailbox deletion can be a lengthy operation, maybe add some configuration to reject the "deleted" recipient
			//https://www.howtoforge.com/community/threads/postfix-reject-incoming-email-for-specific-user.31433/
			LOGGER.warn("Mailbox deletion can be a lengthy operation. Please delete '{}' mailbox manually.", user);
		}
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
	
	private MailServiceSettings createMailServiceSettings(final UserProfileId profileId) {
		return new MailServiceSettings(SERVICE_ID, profileId.getDomainId());
	}
	
	private static enum MailboxCreateOption implements BitFlagsEnum<MailboxCreateOption> {
		CREATE_MAILBOX(1 << 1), CREATE_DEFAULT_FOLDERS(1 << 2), CONFIGURE_SIEVE(1 << 4);
		
		private int mask = 0;
		private MailboxCreateOption(int mask) { this.mask = mask; }
		@Override
		public long mask() { return this.mask; }
	}
}
