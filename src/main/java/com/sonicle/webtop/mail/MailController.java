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

import com.sonicle.mail.cyrus.CyrusManager;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.auth.LdapWebTopDirectory;
import com.sonicle.webtop.core.app.auth.WebTopDirectory;
import com.sonicle.webtop.core.app.sdk.interfaces.IControllerServiceHooks;
import com.sonicle.webtop.core.app.sdk.interfaces.IControllerUserEvents;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import jakarta.mail.MessagingException;

/**
 *
 * @author malbinola
 */
public class MailController extends BaseController implements IControllerServiceHooks, IControllerUserEvents {
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
	
	@Override
	public void onUserAdded(UserProfileId profileId) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(profileId);
		String dirScheme = coreMgr.getAuthDirectoryScheme();
		if (WebTopDirectory.SCHEME.equals(dirScheme) || LdapWebTopDirectory.SCHEME.equals(dirScheme)) {
			MailServiceSettings mss = new MailServiceSettings(SERVICE_ID, profileId.getDomainId());
			
			// Defines the mailbox's username
			String mailboxUser = null;
			if (LdapWebTopDirectory.SCHEME.equals(dirScheme)) {
				mailboxUser = WT.buildDomainInternetAddress(profileId.getDomainId(), profileId.getUserId(), null).getAddress();
			} else {
				mailboxUser = profileId.getUserId();
			}
			
			// Creates the Cyrus mailbox
			try {
				String host = mss.getDefaultHost();
				int port = mss.getDefaultPort();
				String protocol = mss.getDefaultProtocol();
				String adminUser = mss.getAdminUser();
				String adminPassword = mss.getAdminPassword();

				CyrusManager cyrMgr = new CyrusManager(host, port, protocol, adminUser, adminPassword, WT.getProperties());
				cyrMgr.addMailbox(mailboxUser);

			} catch(Throwable t) {
				throw new WTException(t, "Unable to create user's mailbox [{}]", mailboxUser);
			}
			
			// Initialize (create & activate) default Sieve script (eg. for SPAM rule)
			MailManager mailMgr = (MailManager)WT.getServiceManager(SERVICE_ID, true, profileId);
			try {
				mailMgr.setSieveConfiguration(mss.getDefaultHost(), mss.getSievePort(), mss.getAdminUser(), mss.getAdminPassword(), mailboxUser);
				mailMgr.initDefaultSieveScript();
				
			} catch(Throwable t) {
				throw new WTException(t, "Unable to initialize Sieve script [{}]");
			}
		}
		//TODO: maybe add here the default directory structure
	}

	@Override
	public void onUserRemoved(UserProfileId profileId) throws WTException {
		//TODO: Mailbox deletion can be a lengthy operation, maybe add some configuration to reject the "deleted" recipient
		//https://www.howtoforge.com/community/threads/postfix-reject-incoming-email-for-specific-user.31433/
	}
}
