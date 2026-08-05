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

import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.products.ConnectProduct;
import com.sonicle.webtop.mail.bg.ResourcesAutoresponderManager;
import com.sonicle.webtop.core.sdk.BaseBackgroundService;
import com.sonicle.webtop.mail.bg.ResourcesAutoresponderReloadTask;
import com.sonicle.webtop.mail.bg.ScheduledSendTask;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class BackgroundService extends BaseBackgroundService {
	private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundService.class);

	// Properties consumed by the mobile-push shim. Only two are needed:
	// webtop.push-gateway.enabled=true and webtop.push-gateway.url pointing
	// at the wss:// endpoint. The shim's identity + credential is entirely
	// derived at boot: baseUrl comes from WT.getPublicBaseUrl(<licensed
	// domain>), licenseToken from LicenseManager. Shim only boots when at
	// least one domain on this instance holds an active "WebTop Connect"
	// license — see reconcileLicense() below.
	private static final String PROP_PUSH_GATEWAY_ENABLED = "webtop.push-gateway.enabled";
	private static final String PROP_PUSH_GATEWAY_URL = "webtop.push-gateway.url";

	// Matches WebTopApp.webappVersionCheck and the shim's own isLatest poll —
	// no reason to react faster than the underlying LicenseManager cache
	// actually refreshes. Cloud revocations arrive silently via a daily job,
	// so this poll is the only place we notice them.
	private static final long LICENSE_POLL_MS = 60_000L;

	private ResourcesAutoresponderManager resourceAutoresponderMgr;
	private volatile NodejsMailPushGateway pushGateway;
	private ScheduledExecutorService licensePoller;

	@Override
	public void initialize() throws Exception {
		resourceAutoresponderMgr = new ResourcesAutoresponderManager(this);
		// License reconcile handles both cold-start (boot if licensed now)
		// and runtime transitions — no explicit boot call needed here.
		startLicensePoller();
	}

	@Override
	public void cleanup() throws Exception {
		if (resourceAutoresponderMgr != null) resourceAutoresponderMgr.cleanup();
		if (licensePoller != null) licensePoller.shutdownNow();
		NodejsMailPushGateway shim = pushGateway;
		pushGateway = null;
		if (shim != null) {
			try { shim.shutdown(); } catch (Throwable t) { LOGGER.warn("push gateway shutdown", t); }
		}
	}

	private void startLicensePoller() {
		licensePoller = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "push-license-poller");
			t.setDaemon(true);
			return t;
		});
		// Immediate first tick so a licensed instance boots the shim without
		// waiting a whole poll cycle after startup.
		licensePoller.scheduleWithFixedDelay(this::reconcileLicense, 0, LICENSE_POLL_MS, TimeUnit.MILLISECONDS);
	}

	/**
	 * Reconcile the shim's running state against the current license state.
	 *  - Licensed + not running → boot (uses the freshest license token).
	 *  - Not licensed + running → clean unregister (gateway drops the row).
	 *  - Both already aligned → no-op.
	 *
	 * The shim self-terminates on its own isLatest transition; when that
	 * happens `isShutDown()` is true and we treat it as not running. We do
	 * NOT auto-reboot in that case (isLatest=false means this JVM is being
	 * drained; the newer webapp instance owns the shim now).
	 */
	private synchronized void reconcileLicense() {
		try {
			LicenseSnapshot snap = checkConnectLicense();
			NodejsMailPushGateway shim = pushGateway;
			boolean running = shim != null && !shim.isShutDown();
			if (snap.licensed && !running && WT.isLatestWebApp()) {
				pushGateway = null;
				bootPushGatewayIfConfigured(snap);
			} else if (!snap.licensed && running) {
				LOGGER.info("Connect license no longer active — unregistering push gateway shim");
				pushGateway = null;
				try { shim.unregister(); } catch (Throwable t) { LOGGER.warn("unregister failed", t); }
			}
		} catch (Throwable t) {
			LOGGER.warn("push license reconcile failed", t);
		}
	}

	private void bootPushGatewayIfConfigured(LicenseSnapshot snap) {
		Properties props = WebTopApp.getInstanceProperties();
		if (!Boolean.parseBoolean(props.getProperty(PROP_PUSH_GATEWAY_ENABLED, "false"))) {
			LOGGER.debug("push gateway disabled ({}=false)", PROP_PUSH_GATEWAY_ENABLED);
			return;
		}
		String url = trimToNull(props.getProperty(PROP_PUSH_GATEWAY_URL));
		if (url == null) {
			LOGGER.error("push gateway enabled but {} missing", PROP_PUSH_GATEWAY_URL);
			return;
		}
		// baseUrl is derived from the licensed domain's public URL — same
		// value the mobile app will register against. No operator-set
		// property needed.
		String baseUrl = trimToNull(WT.getPublicBaseUrl(snap.licensedDomainId));
		if (baseUrl == null) {
			LOGGER.error("push gateway: no public base URL for domain {}", snap.licensedDomainId);
			return;
		}
		try {
			NodejsMailPushGateway shim = new NodejsMailPushGateway(
				URI.create(url), baseUrl, snap.licenseToken);
			MailPushManager.getInstance().setGateway(shim);
			shim.start();
			pushGateway = shim;
			LOGGER.info("push gateway shim started (url={}, baseUrl={})", url, baseUrl);
		} catch (Throwable t) {
			LOGGER.error("push gateway shim failed to start", t);
			pushGateway = null;
		}
	}

	/**
	 * Snapshot of the instance-wide Connect licensing state: does ANY enabled
	 * domain hold a valid Connect license? If so, capture the raw license
	 * string to hand to the gateway AND the domainId — the domain's public
	 * base URL is what the shim announces as its identity to the gateway,
	 * and it must be the licensed one (otherwise a rogue unlicensed domain
	 * could piggyback). First-licensed-domain wins for MVP; a multi-domain
	 * instance with several licensed domains still only opens one shim.
	 */
	private LicenseSnapshot checkConnectLicense() {
		try {
			Set<String> domains = WebTopApp.getInstance().getWebTopManager()
				.listDomainIds(EnabledCond.ENABLED_ONLY);
			if (domains == null) return LicenseSnapshot.UNLICENSED;
			for (String d : domains) {
				ConnectProduct p = new ConnectProduct(d);
				if (!WT.isLicensed(p)) continue;
				ProductLicense plic = WT.findProductLicense(p);
				String token = null;
				if (plic != null) {
					try {
						String act = plic.getActivatedLicenseString();
						token = (act != null && !act.isEmpty()) ? act : plic.getLicenseString();
					} catch (Throwable ignore) {
						token = plic.getLicenseString();
					}
				}
				return new LicenseSnapshot(true, d, token);
			}
		} catch (Throwable t) {
			LOGGER.warn("connect license check failed", t);
		}
		return LicenseSnapshot.UNLICENSED;
	}

	private static String trimToNull(String s) {
		if (s == null) return null;
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	private static final class LicenseSnapshot {
		static final LicenseSnapshot UNLICENSED = new LicenseSnapshot(false, null, null);
		final boolean licensed;
		final String licensedDomainId;
		final String licenseToken;
		LicenseSnapshot(boolean licensed, String licensedDomainId, String licenseToken) {
			this.licensed = licensed;
			this.licensedDomainId = licensedDomainId;
			this.licenseToken = licenseToken;
		}
	}

	@Override
	protected Collection<TaskDefinition> createTasks() {
		if (isCalendarServiceInstalled()) {
			return Arrays.asList(
				new TaskDefinition(
					ScheduledSendTask.class,
					TriggerBuilder.newTrigger()
						.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(2))
						.build()
				),	
				new TaskDefinition(
					ResourcesAutoresponderReloadTask.class,
					TriggerBuilder.newTrigger()
						.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(1))
						.build()
				)
			);
		} else {
			return Arrays.asList();
		}
	}
	
	private boolean isCalendarServiceInstalled() {
		try {
			if (WT.getServiceManager("com.sonicle.webtop.calendar", true, RunContext.getRunProfileId()) != null) return true;
		} catch (Exception ex) {}
		return false;
	}
	
	public ResourcesAutoresponderManager getResourcesAutoresponderManager() {
		return resourceAutoresponderMgr;
	}
}
