/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
 * Copyright (C) 2026 Sonicle S.r.l.
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
package com.sonicle.webtop.mail.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable AI menu configuration parsed from ai-menu.json.
 *
 * All user-visible strings (labels, prompts, input questions, dialog title,
 * format hints) are carried inline as ISO-639 language maps; resolution
 * against the user's locale happens via {@link #resolve(Map, String)}.
 *
 * Responsibilities:
 *  - Parse the JSON tree into AIMenuItem/AIMenuInputSpec instances.
 *  - Build a flat id→item index for O(1) lookup during AIPrompt dispatch.
 *  - Refuse duplicate ids; refuse leaves without a prompt map.
 */
public final class AIMenuConfig {

	public static final String DEFAULT_LANGUAGE_FALLBACK = "en";

	private final String defaultLanguage;
	private final Map<String, String> dialogTitle;
	private final Map<String, String> htmlFormatHint;
	private final Map<String, String> htmlReplyFormatHint;
	private final List<AIMenuItem> items;
	private final Map<String, AIMenuItem> index;

	private AIMenuConfig(
			String defaultLanguage,
			Map<String, String> dialogTitle,
			Map<String, String> htmlFormatHint,
			Map<String, String> htmlReplyFormatHint,
			List<AIMenuItem> items,
			Map<String, AIMenuItem> index) {
		this.defaultLanguage = defaultLanguage;
		this.dialogTitle = Collections.unmodifiableMap(dialogTitle);
		this.htmlFormatHint = Collections.unmodifiableMap(htmlFormatHint);
		this.htmlReplyFormatHint = Collections.unmodifiableMap(htmlReplyFormatHint);
		this.items = Collections.unmodifiableList(items);
		this.index = Collections.unmodifiableMap(index);
	}

	public String getDefaultLanguage() { return defaultLanguage; }
	public Map<String, String> getDialogTitle() { return dialogTitle; }
	public Map<String, String> getHtmlFormatHint() { return htmlFormatHint; }
	public Map<String, String> getHtmlReplyFormatHint() { return htmlReplyFormatHint; }
	public List<AIMenuItem> getItems() { return items; }

	public AIMenuItem findById(String id) {
		return id == null ? null : index.get(id);
	}

	/**
	 * Pick the entry for {@code lang}; fall back to the configured default
	 * language; fall back to any available entry. Returns null only if the
	 * map is null or empty.
	 */
	public String resolve(Map<String, String> map, String lang) {
		if (map == null || map.isEmpty()) return null;
		if (lang != null) {
			String v = map.get(lang);
			if (v != null) return v;
		}
		String v = map.get(defaultLanguage);
		if (v != null) return v;
		return map.values().iterator().next();
	}

	public static AIMenuConfig load(InputStream in) throws IOException {
		if (in == null) throw new IOException("ai-menu.json resource not found");
		JsonElement root;
		try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			root = new JsonParser().parse(r);
		}
		if (root == null || !root.isJsonObject()) {
			throw new IOException("ai-menu.json: expected an object at root");
		}
		JsonObject rootObj = root.getAsJsonObject();

		String defaultLanguage = getString(rootObj, "defaultLanguage", DEFAULT_LANGUAGE_FALLBACK);
		Map<String, String> dialogTitle = parseLangMap(rootObj, "dialogTitle", false, null);
		Map<String, String> htmlHint = new LinkedHashMap<>();
		Map<String, String> htmlReplyHint = new LinkedHashMap<>();
		if (rootObj.has("formatHints") && rootObj.get("formatHints").isJsonObject()) {
			JsonObject fh = rootObj.getAsJsonObject("formatHints");
			htmlHint = parseLangMap(fh, "html", false, null);
			htmlReplyHint = parseLangMap(fh, "htmlReply", false, null);
		}

		JsonElement itemsEl = rootObj.get("items");
		if (itemsEl == null || !itemsEl.isJsonArray()) {
			throw new IOException("ai-menu.json: missing 'items' array");
		}
		List<AIMenuItem> parsed = new ArrayList<>();
		Map<String, AIMenuItem> idx = new HashMap<>();
		for (JsonElement el : itemsEl.getAsJsonArray()) {
			parsed.add(parseItem(el, idx));
		}
		return new AIMenuConfig(defaultLanguage, dialogTitle, htmlHint, htmlReplyHint, parsed, idx);
	}

	private static AIMenuItem parseItem(JsonElement el, Map<String, AIMenuItem> idx) throws IOException {
		if (el == null || !el.isJsonObject()) {
			throw new IOException("ai-menu.json: item must be an object");
		}
		JsonObject o = el.getAsJsonObject();
		String id = getString(o, "id", null);
		if (id == null || id.isEmpty()) throw new IOException("ai-menu.json: item missing 'id'");
		Map<String, String> label = parseLangMap(o, "label", true, "item '" + id + "'");

		List<AIMenuItem> children = new ArrayList<>();
		if (o.has("children") && o.get("children").isJsonArray()) {
			JsonArray arr = o.getAsJsonArray("children");
			for (JsonElement cel : arr) children.add(parseItem(cel, idx));
		}

		AIMenuMode mode = null;
		boolean source = false;
		int upcomingEventsDays = 0;
		Map<String, String> prompt = null;
		AIMenuInputSpec input = null;

		if (children.isEmpty()) {
			mode = AIMenuMode.parse(getString(o, "mode", null), AIMenuMode.SHOW);
			source = getBoolean(o, "source", false);
			upcomingEventsDays = Math.max(0, getInt(o, "upcomingEventsDays", 0));
			prompt = parseLangMap(o, "prompt", true, "leaf '" + id + "'");
			if (o.has("input") && o.get("input").isJsonObject()) {
				JsonObject ino = o.getAsJsonObject("input");
				Map<String, String> question = parseLangMap(ino, "question", true, "input of '" + id + "'");
				boolean multiline = getBoolean(ino, "multiline", false);
				boolean required = getBoolean(ino, "required", true);
				input = new AIMenuInputSpec(question, multiline, required);
			}
		}

		AIMenuItem item = new AIMenuItem(id, label, mode, source, upcomingEventsDays, prompt, input, children);
		if (idx.put(id, item) != null) {
			throw new IOException("ai-menu.json: duplicate id '" + id + "'");
		}
		return item;
	}

	private static Map<String, String> parseLangMap(JsonObject parent, String field, boolean required, String ownerDesc) throws IOException {
		JsonElement el = parent.get(field);
		if (el == null || el.isJsonNull()) {
			if (required) throw new IOException("ai-menu.json: " + ownerDesc + " missing '" + field + "' map");
			return new LinkedHashMap<>();
		}
		if (!el.isJsonObject()) {
			throw new IOException("ai-menu.json: '" + field + "' must be a {lang: string} object"
					+ (ownerDesc == null ? "" : " (" + ownerDesc + ")"));
		}
		Map<String, String> out = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> e : el.getAsJsonObject().entrySet()) {
			JsonElement v = e.getValue();
			if (v == null || v.isJsonNull()) continue;
			out.put(e.getKey(), v.getAsString());
		}
		if (required && out.isEmpty()) {
			throw new IOException("ai-menu.json: " + ownerDesc + " has empty '" + field + "' map");
		}
		return out;
	}

	private static String getString(JsonObject o, String key, String def) {
		JsonElement e = o.get(key);
		if (e == null || e.isJsonNull()) return def;
		return e.getAsString();
	}

	private static boolean getBoolean(JsonObject o, String key, boolean def) {
		JsonElement e = o.get(key);
		if (e == null || e.isJsonNull()) return def;
		return e.getAsBoolean();
	}

	private static int getInt(JsonObject o, String key, int def) {
		JsonElement e = o.get(key);
		if (e == null || e.isJsonNull()) return def;
		return e.getAsInt();
	}
}
