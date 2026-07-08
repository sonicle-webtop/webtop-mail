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

import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.Set;

/**
 * Outbound seam for mobile push delivery. {@link MailPushManager} translates
 * the shared MailManager's fan-out events into calls on this interface for
 * every user with at least one registered device; a real implementation (e.g.
 * a bridge to an external nodejs gateway doing FCM/APNs) is plugged in via
 * {@link MailPushManager#setGateway}. The default implementation only logs.
 *
 * <p>Called from idle/scan background threads: implementations must be
 * thread-safe, must not block and must not throw — hand the event off to
 * their own delivery machinery (queue, http client, ...) and return.</p>
 *
 * @author gbulfon
 */
public interface MailPushGateway {

	/**
	 * @param profileId The user the event belongs to.
	 * @param deviceIds Snapshot of the user's registered device ids.
	 * @param accountId The mail account the event belongs to.
	 * @param foldername The full folder name the event belongs to.
	 * @param type The kind of event; RECENT signals new mail, UNREAD a badge
	 * count change — FLAGS/MDEL are forwarded too and may be ignored.
	 * @param msg The pre-built service message (same payload web clients get).
	 */
	void pushMailEvent(UserProfileId profileId, Set<String> deviceIds, String accountId, String foldername, MailEventType type, ServiceMessage msg);
}
