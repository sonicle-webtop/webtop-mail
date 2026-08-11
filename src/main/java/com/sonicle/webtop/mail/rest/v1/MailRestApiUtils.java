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
package com.sonicle.webtop.mail.rest.v1;

import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.mail.MailManager;
import com.sonicle.webtop.mail.Service;
import javax.ws.rs.core.HttpHeaders;

/**
 *
 * @author gabriele.bulfon
 */
public class MailRestApiUtils {

	/**
	 * Request header a REST caller sets (value "full") to declare it needs the
	 * full account machinery (accounts, folder caches, idle) running — e.g. a
	 * mobile app polling folders/messages. Without it, REST is served through
	 * the cold pooled-mailbox paths and never spins up per-user idle stacks
	 * (so bulk integrations sweeping many users stay cheap). Self-healing: the
	 * registry may idle-evict the manager; the next call bearing the header
	 * simply re-warms it.
	 */
	public static final String HEADER_MACHINERY = "X-WT-Mail-Machinery";
	public static final String HEADER_MACHINERY_FULL = "full";
	//NB: the app-restart signal is NOT a mail header — it is the core-owned
	//X-WT-App-Launch header, processed at the AUTH layer on ANY service's API
	//call (see core AuthBearer / ServiceManager.onAppLaunch): core rebuilds all
	//the user's app-side shared managers and MailPushManager re-attaches its
	//push state via the eviction listener. This class only handles the
	//machinery warm-up header.

	public static MailManager getMailManager(UserProfileId targetPid) {
		//Resolve through the shared-manager registry: REST reuses the same warm
		//per-user instance across all the user's app devices instead of
		//building a throwaway.
		return (MailManager)WT.getServiceManager(WT.findServiceId(Service.class), false, targetPid);
	}

	public static MailManager getMailManager(UserProfileId targetPid, HttpHeaders headers) {
		MailManager mmgr = getMailManager(targetPid);
		if (mmgr != null && headers != null
				&& HEADER_MACHINERY_FULL.equalsIgnoreCase(headers.getHeaderString(HEADER_MACHINERY))) {
			//async: the header KICKS the warm-up, it must not hold this response
			//for the full IMAP start-up (seconds); cold paths have fallbacks
			mmgr.ensureAccountsStartedAsync();
		}
		return mmgr;
	}
}
