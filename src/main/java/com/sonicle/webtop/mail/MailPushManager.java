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

import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mobile push subscription seam (application-wide singleton). A registered
 * device does two things:
 * <ul>
 * <li>holds a durable SUBSCRIPTION reference on the user's shared MailManager
 * (registry ref prefix {@code m:}), so the per-user machinery — folder caches,
 * IMAP IDLE — stays alive with zero web sessions and zero REST traffic;</li>
 * <li>subscribes the user to the manager's event fan-out: every folder event
 * is forwarded to the configured {@link MailPushGateway} together with a
 * snapshot of the user's device ids.</li>
 * </ul>
 *
 * <p>The default gateway is a logging no-op: this class is the seam, the real
 * delivery bridge (e.g. towards an external nodejs gateway doing FCM/APNs)
 * plugs in later via {@link #setGateway}.</p>
 *
 * <p>Subscriptions are IN-MEMORY only: they do not survive a webapp restart.
 * A durable implementation must persist device registrations and re-subscribe
 * them at startup (binding a proper Subject before calling
 * {@link #subscribe}, which warms the machinery through the caller's
 * security context).</p>
 *
 * @author gbulfon
 */
public class MailPushManager {
	private static final Logger logger = LoggerFactory.getLogger(MailPushManager.class);
	private static final MailPushManager INSTANCE = new MailPushManager();

	public static MailPushManager getInstance() {
		return INSTANCE;
	}

	private static final MailPushGateway NOOP_GATEWAY = new MailPushGateway() {
		@Override
		public void pushMailEvent(UserProfileId profileId, Set<String> deviceIds, String accountId, String foldername, MailEventType type, ServiceMessage msg) {
			if (logger.isDebugEnabled()) logger.debug("[push-noop] {} {}:{} {} -> {} device(s)", profileId, accountId, foldername, type, deviceIds.size());
		}
	};

	private volatile MailPushGateway gateway = NOOP_GATEWAY;
	private final ConcurrentHashMap<UserProfileId, UserSubscription> subscriptions = new ConcurrentHashMap<>();
	private volatile String serviceId = null;

	private MailPushManager() {}

	/**
	 * Plugs in the real outbound gateway (null restores the logging no-op).
	 */
	public void setGateway(MailPushGateway gateway) {
		this.gateway = (gateway != null) ? gateway : NOOP_GATEWAY;
	}

	/**
	 * Registers a device for a user: acquires the durable subscription ref on
	 * the shared MailManager, kicks the machinery warm-up (async) and hooks the
	 * user into the event fan-out. Idempotent per (user, deviceId). Must be
	 * called with the caller's Subject bound (e.g. from an authenticated REST
	 * request): the background warm-up runs under that security context.
	 */
	public synchronized void subscribe(UserProfileId profileId, String deviceId) {
		MailManager mmgr = (MailManager)WT.acquireServiceManagerSubscription(getServiceId(), profileId, deviceId);
		if (mmgr == null) {
			logger.error("[{}] push subscribe failed: no shared manager available", profileId);
			return;
		}
		UserSubscription us = subscriptions.computeIfAbsent(profileId, UserSubscription::new);
		us.deviceIds.add(deviceId);
		us.manager = mmgr;
		mmgr.registerMailEventListener(us); //CopyOnWriteArraySet: re-add is a no-op
		mmgr.ensureAccountsStartedAsync();
		logger.info("[{}] push device '{}' subscribed ({} total)", profileId, deviceId, us.deviceIds.size());
	}

	/**
	 * Unregisters a device; when it was the user's last one, unhooks the user
	 * from the fan-out. Releasing the subscription ref lets the idle sweeper
	 * reclaim the machinery once no other refs remain. Safe to call for a
	 * device that was never (or no longer is) subscribed.
	 */
	public synchronized void unsubscribe(UserProfileId profileId, String deviceId) {
		UserSubscription us = subscriptions.get(profileId);
		if (us != null) {
			us.deviceIds.remove(deviceId);
			if (us.deviceIds.isEmpty()) {
				subscriptions.remove(profileId);
				MailManager mmgr = us.manager;
				if (mmgr != null) mmgr.unregisterMailEventListener(us);
			}
			logger.info("[{}] push device '{}' unsubscribed ({} remaining)", profileId, deviceId, us.deviceIds.size());
		}
		WT.releaseServiceManagerSubscription(getServiceId(), profileId, deviceId);
	}

	private String getServiceId() {
		String sid = serviceId;
		if (sid == null) serviceId = sid = WT.findServiceId(Service.class);
		return sid;
	}

	/**
	 * Per-user fan-out hook: forwards every event to the gateway with a device
	 * snapshot. Runs on idle/scan threads — never throws.
	 */
	private class UserSubscription implements MailEventListener {
		final UserProfileId profileId;
		final Set<String> deviceIds = ConcurrentHashMap.newKeySet();
		volatile MailManager manager;

		UserSubscription(UserProfileId profileId) {
			this.profileId = profileId;
		}

		@Override
		public void onMailEvent(String accountId, String foldername, MailEventType type, ServiceMessage msg) {
			try {
				gateway.pushMailEvent(profileId, new LinkedHashSet<>(deviceIds), accountId, foldername, type, msg);
			} catch (Throwable t) {
				logger.error("[{}] push gateway error", profileId, t);
			}
		}
	}
}
