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
 */
package com.sonicle.webtop.mail;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket client shim that plugs into {@link MailPushManager#setGateway} and
 * bridges the local mail-event fan-out to an external Node.js push gateway
 * over a persistent bidirectional connection. No APNs / FCM / device state
 * lives in Java — this class only forwards events and honours
 * subscribe/unsubscribe commands issued by the gateway.
 *
 * <p>Threading: {@link #pushMailEvent} is called from IMAP-IDLE / scan
 * background threads and must not block; it enqueues to a bounded outbound
 * queue and returns immediately. A single sender thread drains the queue.
 * Inbound frames are dispatched on the WebSocket library's read thread; the
 * subscribe/unsubscribe handlers bind a sysadmin-impersonated Subject before
 * calling into {@link MailPushManager} so the shared MailManager machinery
 * warms up under a valid security context.</p>
 */
public class NodejsMailPushGateway implements MailPushGateway {

	private static final Logger logger = LoggerFactory.getLogger(NodejsMailPushGateway.class);
	private static final Gson GSON = new Gson();
	private static final int OUTBOUND_QUEUE_CAP = 4096;
	private static final long RECONNECT_MIN_MS = 5_000L;
	private static final long RECONNECT_MAX_MS = 60_000L;
	// Matches WebTopApp.webappVersionCheck cadence — no point polling faster
	// than the value we're reading actually refreshes.
	private static final long LATEST_CHECK_INTERVAL_MS = 60_000L;
	// Hard caps on per-event UID/item counts we ship on the wire. Anything
	// larger is omitted, which the mobile client interprets as "do a full
	// folder refresh" — safer than blowing the ~4 KB APNs / FCM payload
	// budget or the shim's outbound queue.
	private static final int FLAGS_ITEMS_CAP = 30;
	private static final int MDEL_UIDS_CAP = 200;

	private final URI gatewayUri;
	private final String serverId;
	private final String sharedSecret;
	private final ScheduledExecutorService scheduler;
	private final LinkedBlockingQueue<JsonObject> outbound = new LinkedBlockingQueue<>(OUTBOUND_QUEUE_CAP);
	private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
	private final AtomicBoolean connected = new AtomicBoolean(false);
	private final Random reconnectJitter = new Random();

	private volatile WebSocketClient client;
	private volatile Thread sender;
	private volatile ScheduledFuture<?> reconnectFuture;
	private volatile ScheduledFuture<?> isLatestCheckFuture;
	private long backoffMs = RECONNECT_MIN_MS;

	// Captured from the calling (webapp-startup) thread where Shiro's static
	// holder is populated. The Java-WebSocket read thread is a plain
	// non-webapp thread where SecurityUtils.getSecurityManager() throws
	// UnavailableSecurityManagerException; caching the reference here lets
	// dispatchSubscribe build a Subject from that thread.
	private volatile SecurityManager securityManager;

	public NodejsMailPushGateway(URI gatewayUri, String serverId, String sharedSecret) {
		this.gatewayUri = gatewayUri;
		this.serverId = serverId;
		this.sharedSecret = sharedSecret;
		this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
			Thread t = new Thread(r, "push-shim-scheduler");
			t.setDaemon(true);
			return t;
		});
	}

	public synchronized void start() {
		if (shuttingDown.get()) throw new IllegalStateException("already shut down");
		try {
			securityManager = SecurityUtils.getSecurityManager();
		} catch (Throwable t) {
			logger.error("push shim start: no SecurityManager available on calling thread — subscribes will fail", t);
		}
		if (sender == null) {
			sender = new Thread(this::senderLoop, "push-shim-sender");
			sender.setDaemon(true);
			sender.start();
		}
		// Poll isLatest and self-terminate on the first flip to false. Under
		// Tomcat parallel deployment the old webapp instance keeps running
		// (draining browser sessions) but must stop touching the gateway —
		// see the class-level comment on pushMailEvent for why.
		isLatestCheckFuture = scheduler.scheduleAtFixedRate(
			this::checkIsLatestAndMaybeShutdown,
			LATEST_CHECK_INTERVAL_MS,
			LATEST_CHECK_INTERVAL_MS,
			TimeUnit.MILLISECONDS
		);
		connect();
	}

	private void checkIsLatestAndMaybeShutdown() {
		if (shuttingDown.get()) return;
		try {
			if (!WT.isLatestWebApp()) {
				logger.info("push shim: no longer the latest webapp instance — shutting down");
				shutdown();
			}
		} catch (Throwable t) {
			logger.warn("isLatest poll failed", t);
		}
	}

	public synchronized void shutdown() {
		shuttingDown.set(true);
		if (reconnectFuture != null) reconnectFuture.cancel(false);
		if (isLatestCheckFuture != null) isLatestCheckFuture.cancel(false);
		if (client != null) {
			try { client.close(); } catch (Throwable ignored) {}
		}
		scheduler.shutdownNow();
		if (sender != null) sender.interrupt();
	}

	@Override
	public void pushMailEvent(UserProfileId profileId, Set<String> deviceIds, String accountId,
			String foldername, MailEventType type, ServiceMessage msg) {
		// Tomcat parallel deployment: two webapp classloaders share
		// webtop.properties and each boots its own shim with the same
		// serverId. Only the "latest" instance owns push forwarding; the
		// draining older instance still has warm IMAP IDLE (until Tomcat
		// undeploys it) and would otherwise duplicate every event.
		if (!WT.isLatestWebApp()) return;
		// Forward every event we see: the gateway needs per-folder counts to
		// keep in-app unseen indicators live, and the shim can't know which
		// folders the client cares about. The gateway is the one authority on
		// what turns into a user-visible push versus a data-only update.
		try {
			JsonObject frame = new JsonObject();
			frame.addProperty("op", "event");
			frame.addProperty("profileId", profileId.toString());
			frame.add("deviceIds", GSON.toJsonTree(deviceIds));
			frame.addProperty("accountId", accountId);
			frame.addProperty("folder", foldername);
			frame.addProperty("type", type.name());

			Integer count = extractCount(type, msg);
			frame.add("count", count == null ? JsonNull.INSTANCE : new JsonPrimitive(count));

			JsonObject alert = extractAlert(type, msg);
			if (alert != null) frame.add("alert", alert);

			JsonArray items = extractItems(type, msg);
			if (items != null) frame.add("items", items);

			JsonArray uids = extractUids(type, msg);
			if (uids != null) frame.add("uids", uids);

			if (!outbound.offer(frame)) {
				logger.warn("[{}] outbound queue full ({}); dropping {} event", profileId, OUTBOUND_QUEUE_CAP, type);
			}
		} catch (Throwable t) {
			logger.error("[{}] failed to enqueue mail event", profileId, t);
		}
	}

	/**
	 * Pull the INBOX unread count from the mail service-message payload. Uses
	 * Gson to walk the payload's fields so we don't hard-depend on the Js*
	 * class shapes across module boundaries. Returns null for RECENT (banner
	 * event; count comes from the following UNREAD dispatch), FLAGS and MDEL.
	 */
	private Integer extractCount(MailEventType type, ServiceMessage msg) {
		if (type != MailEventType.UNREAD) return null;
		try {
			Object payload = msg.getPayload();
			if (payload == null) return null;
			JsonElement el = GSON.toJsonTree(payload);
			if (!el.isJsonObject()) return null;
			JsonElement u = el.getAsJsonObject().get("unread");
			if (u == null || !u.isJsonPrimitive() || !u.getAsJsonPrimitive().isNumber()) return null;
			return u.getAsInt();
		} catch (Throwable t) {
			logger.debug("could not extract unread count from {}", type, t);
			return null;
		}
	}

	/**
	 * FLAGS payload: JsFlagsChangedMessage.items — a list of per-UID flag
	 * snapshots the mobile client can apply in place without refetching.
	 * If the list is larger than {@link #FLAGS_ITEMS_CAP} we drop it entirely;
	 * the client interprets a missing items array as "unknown scope — refresh".
	 * A null items on the source (server-side bulk operation) is the same
	 * refresh signal.
	 */
	private JsonArray extractItems(MailEventType type, ServiceMessage msg) {
		if (type != MailEventType.FLAGS) return null;
		try {
			Object payload = msg.getPayload();
			if (payload == null) return null;
			JsonElement el = GSON.toJsonTree(payload);
			if (!el.isJsonObject()) return null;
			JsonElement itemsEl = el.getAsJsonObject().get("items");
			if (itemsEl == null || !itemsEl.isJsonArray()) return null;
			JsonArray arr = itemsEl.getAsJsonArray();
			if (arr.size() > FLAGS_ITEMS_CAP) return null;
			JsonArray out = new JsonArray();
			for (JsonElement e : arr) {
				if (e.isJsonObject()) out.add(e.getAsJsonObject());
			}
			return out;
		} catch (Throwable t) {
			logger.debug("could not extract items from {}", type, t);
			return null;
		}
	}

	/**
	 * MDEL payload: JsMessagesDeletedMessage.uids — a list of removed UIDs.
	 * Capped at {@link #MDEL_UIDS_CAP}; larger deletions drop through as
	 * "refresh" for the same reason as {@link #extractItems}.
	 */
	private JsonArray extractUids(MailEventType type, ServiceMessage msg) {
		if (type != MailEventType.MDEL) return null;
		try {
			Object payload = msg.getPayload();
			if (payload == null) return null;
			JsonElement el = GSON.toJsonTree(payload);
			if (!el.isJsonObject()) return null;
			JsonElement uidsEl = el.getAsJsonObject().get("uids");
			if (uidsEl == null || !uidsEl.isJsonArray()) return null;
			JsonArray arr = uidsEl.getAsJsonArray();
			if (arr.size() > MDEL_UIDS_CAP) return null;
			JsonArray out = new JsonArray();
			for (JsonElement e : arr) {
				if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) out.add(e.getAsLong());
			}
			return out;
		} catch (Throwable t) {
			logger.debug("could not extract uids from {}", type, t);
			return null;
		}
	}

	/**
	 * For RECENT events, build a banner alert (title=subject, body=from). For
	 * everything else, return null so the gateway sends a silent update.
	 */
	private JsonObject extractAlert(MailEventType type, ServiceMessage msg) {
		if (type != MailEventType.RECENT) return null;
		try {
			Object payload = msg.getPayload();
			if (payload == null) return null;
			JsonElement el = GSON.toJsonTree(payload);
			if (!el.isJsonObject()) return null;
			JsonObject po = el.getAsJsonObject();
			String from = po.has("from") && po.get("from").isJsonPrimitive() ? po.get("from").getAsString() : "";
			String subject = po.has("subject") && po.get("subject").isJsonPrimitive() ? po.get("subject").getAsString() : "";
			JsonObject alert = new JsonObject();
			alert.addProperty("title", subject);
			alert.addProperty("body", from);
			return alert;
		} catch (Throwable t) {
			logger.debug("could not extract alert from {}", type, t);
			return null;
		}
	}

	private void senderLoop() {
		while (!shuttingDown.get()) {
			try {
				JsonObject frame = outbound.take();
				// Guard against a race: pushMailEvent may have enqueued this
				// frame just before isLatest flipped. Drop it silently rather
				// than forward a stale event from a draining instance.
				if (!WT.isLatestWebApp()) continue;
				WebSocketClient c;
				while ((c = client) == null || !connected.get()) {
					if (shuttingDown.get()) return;
					Thread.sleep(50);
				}
				try {
					c.send(GSON.toJson(frame));
				} catch (Throwable t) {
					logger.warn("send failed, requeuing", t);
					// best-effort requeue at head — LinkedBlockingQueue lacks that;
					// drop instead. UNREAD is cumulative — the next event resyncs.
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private synchronized void connect() {
		if (shuttingDown.get()) return;
		if (client != null) return;
		final long ts = System.currentTimeMillis();
		final String nonce = randomNonce();
		final String signature = signHex(sharedSecret, ts + ":" + nonce);

		try {
			client = new WebSocketClient(gatewayUri) {
				@Override
				public void onOpen(ServerHandshake handshake) {
					JsonObject hello = new JsonObject();
					hello.addProperty("op", "hello");
					hello.addProperty("serverId", serverId);
					hello.addProperty("timestamp", ts);
					hello.addProperty("nonce", nonce);
					hello.addProperty("signature", signature);
					send(GSON.toJson(hello));
				}

				@Override
				public void onMessage(String message) {
					handleInbound(message);
				}

				@Override
				public void onMessage(ByteBuffer bytes) {
					// binary frames not expected on this link
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					logger.info("gateway connection closed code={} reason={} remote={}", code, reason, remote);
					connected.set(false);
					synchronized (NodejsMailPushGateway.this) {
						client = null;
					}
					scheduleReconnect();
				}

				@Override
				public void onError(Exception ex) {
					logger.warn("gateway ws error", ex);
				}
			};
			client.setConnectionLostTimeout(0); // handled by our own heartbeat
			client.connect();
		} catch (Throwable t) {
			logger.error("gateway connect failed", t);
			scheduleReconnect();
		}
	}

	private synchronized void scheduleReconnect() {
		if (shuttingDown.get()) return;
		long jitter = (long) (backoffMs * (0.9 + reconnectJitter.nextDouble() * 0.2));
		long delay = Math.min(RECONNECT_MAX_MS, jitter);
		backoffMs = Math.min(RECONNECT_MAX_MS, backoffMs * 2);
		reconnectFuture = scheduler.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
	}

	private void handleInbound(String raw) {
		JsonObject frame;
		try {
			JsonElement el = GSON.fromJson(raw, JsonElement.class);
			if (!el.isJsonObject()) { logger.warn("non-object frame"); return; }
			frame = el.getAsJsonObject();
		} catch (Throwable t) {
			logger.warn("unparseable frame", t);
			return;
		}
		String op = frame.has("op") && frame.get("op").isJsonPrimitive() ? frame.get("op").getAsString() : "";
		switch (op) {
			case "hello-ack":
				connected.set(true);
				backoffMs = RECONNECT_MIN_MS;
				logger.info("gateway handshake complete");
				break;
			case "hello-reject": {
				String reason = frame.has("reason") ? frame.get("reason").getAsString() : "unknown";
				logger.error("gateway rejected hello: {}", reason);
				break;
			}
			case "ping":
				JsonObject pong = new JsonObject();
				pong.addProperty("op", "pong");
				if (client != null) client.send(GSON.toJson(pong));
				break;
			case "pong":
				break;
			case "subscribe":
				dispatchSubscribe(frame, true);
				break;
			case "unsubscribe":
				dispatchSubscribe(frame, false);
				break;
			case "resubscribe-all": {
				JsonElement entries = frame.get("entries");
				if (entries != null && entries.isJsonArray()) {
					for (JsonElement e : entries.getAsJsonArray()) {
						if (!e.isJsonObject()) continue;
						dispatchSubscribe(e.getAsJsonObject(), true);
					}
				}
				break;
			}
			default:
				logger.warn("unexpected op: {}", op);
		}
	}

	private void dispatchSubscribe(JsonObject frame, boolean isSubscribe) {
		// Same isLatest gate as pushMailEvent: a draining old instance must
		// not warm its MailPushManager (or acquire fresh ServiceManager
		// subscription refcounts) in response to a subscribe frame the
		// gateway may have sent before it noticed the new instance.
		if (!WT.isLatestWebApp()) return;
		String profileIdStr = frame.has("profileId") && frame.get("profileId").isJsonPrimitive()
				? frame.get("profileId").getAsString() : null;
		String deviceId = frame.has("deviceId") && frame.get("deviceId").isJsonPrimitive()
				? frame.get("deviceId").getAsString() : null;
		if (profileIdStr == null || deviceId == null) {
			logger.warn("subscribe frame missing profileId/deviceId");
			return;
		}
		UserProfileId profileId = parseProfileId(profileIdStr);
		if (profileId == null) {
			logger.warn("unparseable profileId '{}'", profileIdStr);
			return;
		}
		// Bind the TARGET USER's Subject (not sysadmin's): MailManager.
		// ensureAccountsStartedAsync captures the current Subject via
		// SecurityUtils.getSubject() and binds it on the warmup thread; the
		// per-account init (mail-store impersonation, IMAP IDLE start) needs
		// user context, not sysadmin — without it IDLE never starts and
		// events never fire.
		//
		// Uses the SecurityManager cached at start() rather than
		// SecurityUtils.getSecurityManager(), because the Java-WebSocket
		// read thread has no bound Subject and no VM-static holder.
		if (securityManager == null) {
			logger.error("securityManager not captured; subscribe skipped for {} / {}", profileId, deviceId);
			return;
		}
		Subject userSubject = RunContext.buildSubject(securityManager, profileId);
		ThreadState state = new SubjectThreadState(userSubject);
		try {
			state.bind();
			if (isSubscribe) MailPushManager.getInstance().subscribe(profileId, deviceId);
			else MailPushManager.getInstance().unsubscribe(profileId, deviceId);
		} catch (Throwable t) {
			logger.error("subscribe dispatch failed for {} / {}", profileId, deviceId, t);
		} finally {
			state.clear();
		}
	}

	private UserProfileId parseProfileId(String s) {
		int at = s.indexOf('@');
		if (at <= 0 || at == s.length() - 1) return null;
		return new UserProfileId(s.substring(at + 1), s.substring(0, at));
	}

	private static String signHex(String secret, String data) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
			byte[] sig = mac.doFinal(data.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(sig.length * 2);
			for (byte b : sig) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception ex) {
			throw new RuntimeException("HMAC failed", ex);
		}
	}

	private static String randomNonce() {
		byte[] buf = new byte[16];
		new SecureRandom().nextBytes(buf);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
	}
}
