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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A single AI menu entry. Leaves carry an action (mode + prompt map and
 * optionally an input spec); parents carry a non-empty children list.
 *
 * Labels, prompts and input questions are inline language maps
 * (ISO-639 code → localized string) resolved against the user's locale
 * at service-vars build time and at AIPrompt dispatch time.
 */
public final class AIMenuItem {

	private final String id;
	private final Map<String, String> label;
	private final AIMenuMode mode;
	private final boolean source;
	private final Map<String, String> prompt;
	private final AIMenuInputSpec input;
	private final List<AIMenuItem> children;

	public AIMenuItem(
			String id,
			Map<String, String> label,
			AIMenuMode mode,
			boolean source,
			Map<String, String> prompt,
			AIMenuInputSpec input,
			List<AIMenuItem> children) {
		this.id = id;
		this.label = label == null
				? Collections.<String, String>emptyMap()
				: Collections.unmodifiableMap(label);
		this.mode = mode;
		this.source = source;
		this.prompt = prompt == null
				? Collections.<String, String>emptyMap()
				: Collections.unmodifiableMap(prompt);
		this.input = input;
		this.children = children == null
				? Collections.<AIMenuItem>emptyList()
				: Collections.unmodifiableList(children);
	}

	public String getId() { return id; }
	public Map<String, String> getLabel() { return label; }
	public AIMenuMode getMode() { return mode; }
	public boolean isSource() { return source; }
	public Map<String, String> getPrompt() { return prompt; }
	public AIMenuInputSpec getInput() { return input; }
	public List<AIMenuItem> getChildren() { return children; }

	public boolean isGroup() { return !children.isEmpty(); }
	public boolean isLeaf() { return children.isEmpty(); }
}
