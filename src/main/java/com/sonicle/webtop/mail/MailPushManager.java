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

import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
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

	//Machinery warm-ups triggered by push subscriptions are THROTTLED through this
	//bounded executor: at gateway boot a resubscribe-all frame can carry hundreds
	//of devices, and the per-manager async warm-up (one thread each, DB settings +
	//identities reads + IMAP connects) exhausted the JDBC pool when they all ran
	//at once. Only WARMUP_CONCURRENCY machineries start concurrently; the rest
	//queue and drain. Web logins do not pass through here (they warm on their own
	//request thread) so interactive latency is unaffected.
	private static final String PROP_PUSH_WARMUP_CONCURRENCY = "webtop.push-gateway.warmup-concurrency";
	private static final int DEFAULT_WARMUP_CONCURRENCY = 2;
	private final Object warmupLock = new Object();
	private ExecutorService warmupExecutor = null;
	//terminal flag: once stopWarmups() ran (module shutdown), a late gateway
	//frame must NOT lazily resurrect the pool — in a stopping context that
	//would pin the dying classloader with daemon threads and queued tasks
	private boolean warmupsStopped = false;

	//The app-launch signal itself (X-WT-App-Launch header, dedupe, debounce,
	//eviction of ALL sessionless-only managers) is CORE-OWNED: it is processed
	//at the auth layer on any service's API call (see core AuthBearer /
	//ServiceManager.onAppLaunch). This module only (a) forwards the equivalent
	//signal when a KNOWN push device re-registers, and (b) listens for the
	//evictions to re-attach its durable push state on the fresh mail instance.
	private final ServiceManager.SessionlessEvictionListener evictionListener = new ServiceManager.SessionlessEvictionListener() {
		@Override
		public void onSessionlessManagersEvicted(UserProfileId profileId, java.util.List<String> evictedServiceIds) {
			reattachPushState(profileId, evictedServiceIds);
		}
	};

	private MailPushManager() {}

	private ExecutorService getWarmupExecutor() {
		synchronized (warmupLock) {
			if (warmupsStopped) return null;
			if (warmupExecutor == null || warmupExecutor.isShutdown()) {
				int concurrency = DEFAULT_WARMUP_CONCURRENCY;
				try {
					concurrency = Integer.parseInt(WebTopApp.getInstanceProperties()
						.getProperty(PROP_PUSH_WARMUP_CONCURRENCY, String.valueOf(DEFAULT_WARMUP_CONCURRENCY)));
				} catch (Throwable t) {
					logger.warn("Invalid {} value, using default {}", PROP_PUSH_WARMUP_CONCURRENCY, DEFAULT_WARMUP_CONCURRENCY);
				}
				if (concurrency < 1) concurrency = 1;
				final AtomicInteger seq = new AtomicInteger(0);
				warmupExecutor = Executors.newFixedThreadPool(concurrency, r -> {
					Thread t = new Thread(r, "mailPushWarmup-" + seq.incrementAndGet());
					t.setDaemon(true);
					return t;
				});
				logger.info("push warm-up executor started (concurrency={})", concurrency);
			}
			return warmupExecutor;
		}
	}

	/**
	 * Stops the warm-up executor, dropping any still-queued warm-ups (their
	 * machineries will lazily start on first real use). Called by the module's
	 * BackgroundService cleanup so pool threads never outlive the webapp.
	 */
	public void stopWarmups() {
		synchronized (warmupLock) {
			warmupsStopped = true;
			if (warmupExecutor != null) {
				warmupExecutor.shutdownNow();
				warmupExecutor = null;
			}
		}
	}

	private void enqueueWarmup(UserProfileId profileId, MailManager mmgr) {
		//capture the caller's authenticated Subject NOW (subscribe runs with the
		//target user's Subject bound, see NodejsMailPushGateway.dispatchSubscribe)
		//and re-bind it on the pool thread: initAccounts resolves profile data
		//through RunContext/WT, which need it on the running thread
		Subject subject = null;
		try {
			subject = SecurityUtils.getSubject();
		} catch (Throwable t) {
			logger.debug("[{}] no Subject available for queued warm-up", profileId, t);
		}
		final Subject boundSubject = subject;
		try {
			ExecutorService executor = getWarmupExecutor();
			if (executor == null) {
				logger.debug("[{}] warm-up skipped: executor stopped (module shutting down)", profileId);
				return;
			}
			executor.execute(() -> {
				ThreadState threadState = (boundSubject != null) ? new SubjectThreadState(boundSubject) : null;
				if (threadState != null) threadState.bind();
				try {
					//synchronous on purpose: running the warm-up on THIS pool thread
					//is what bounds the number of machineries starting at once
					//(idempotent + shuttingDown-gated inside)
					mmgr.ensureAccountsStarted();
				} catch (Throwable t) {
					logger.error("[{}] queued machinery warm-up failed", profileId, t);
				} finally {
					if (threadState != null) threadState.clear();
				}
			});
		} catch (Throwable t) {
			//executor stopped mid-shutdown: machinery will start on first real use
			logger.debug("[{}] warm-up enqueue rejected", profileId, t);
		}
	}

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
		//NB: a re-subscribe of a known device is NOT an app-launch signal.
		//Devices and the gateway re-send subscribe frames for housekeeping
		//(reconnects, periodic token re-registration, gateway-boot
		//resubscribe-all) — none of which mean the app restarted, and rebuilding
		//here would mass-evict managers on every such event (observed in dev:
		//re-subscribes ~30s apart force-rebuilt everything). The app-launch
		//signal is EXCLUSIVELY the app-declared X-WT-App-Launch header,
		//processed at the core auth layer.
		MailManager mmgr = (MailManager)WT.acquireServiceManagerSubscription(getServiceId(), profileId, deviceId);
		if (mmgr == null) {
			logger.error("[{}] push subscribe failed: no shared manager available", profileId);
			return;
		}
		UserSubscription us = subscriptions.computeIfAbsent(profileId, UserSubscription::new);
		us.deviceIds.add(deviceId);
		us.manager = mmgr;
		mmgr.registerMailEventListener(us); //CopyOnWriteArraySet: re-add is a no-op
		enqueueWarmup(profileId, mmgr); //throttled: see warm-up executor above
		logger.info("[{}] push device '{}' subscribed ({} total)", profileId, deviceId, us.deviceIds.size());
	}

	/**
	 * Hooks this module into core's app-launch pipeline (registered by the
	 * module's BackgroundService, unregistered at its cleanup).
	 */
	public void registerEvictionListener() {
		WT.addSessionlessEvictionListener(evictionListener);
	}

	public void unregisterEvictionListener() {
		WT.removeSessionlessEvictionListener(evictionListener);
	}

	/**
	 * Core evicted the profile's sessionless-only managers (app-launch signal,
	 * arriving on any service's API call or from a device re-registration). If
	 * MAIL was among them, the old holder's m: refs and fan-out listener died
	 * with it: re-acquire the subscription refs of ALL the user's devices,
	 * re-hook the listener on the fresh instance and kick the throttled
	 * machinery warm-up. No-push users need nothing here — their fresh
	 * instance materializes and warms on the app's own (access-token
	 * authenticated) next call; resolving it on THIS thread could yield a
	 * throwaway (non-app auth) whose warm-up would leak an IMAP stack.
	 */
	private void reattachPushState(UserProfileId profileId, java.util.List<String> evictedServiceIds) {
		String sid = getServiceId();
		if (!evictedServiceIds.contains(sid)) return; //mail was young or absent: refs still valid
		UserSubscription us = subscriptions.get(profileId);
		if (us == null || us.deviceIds.isEmpty()) return;
		synchronized (this) {
			MailManager fresh = null;
			for (String devId : us.deviceIds) {
				MailManager m = (MailManager)WT.acquireServiceManagerSubscription(sid, profileId, devId);
				if (m != null) fresh = m;
			}
			if (fresh != null) {
				us.manager = fresh;
				fresh.registerMailEventListener(us);
				enqueueWarmup(profileId, fresh);
				logger.info("[{}] push state re-attached on rebuilt manager ({} devices)", profileId, us.deviceIds.size());
			}
		}
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
