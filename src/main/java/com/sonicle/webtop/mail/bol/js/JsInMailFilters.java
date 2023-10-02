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
package com.sonicle.webtop.mail.bol.js;

import com.sonicle.webtop.mail.model.AutoResponder;
import com.sonicle.webtop.mail.model.MailFilter;
import com.sonicle.webtop.mail.model.MailFiltersType;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class JsInMailFilters {
	public MailFiltersType id = MailFiltersType.INCOMING;
	public Integer scriptsCount;
	public String activeScript;
	public JsAutoResponder autoResponder;
	public ArrayList<JsMailFilter> filters = new ArrayList<>();
	
	public JsInMailFilters(int scriptsCount, String activeScript, AutoResponder autoResponder, List<MailFilter> mailFilters, DateTimeZone profileTz) {
		this.scriptsCount = scriptsCount;
		this.activeScript = activeScript;
		this.autoResponder = new JsAutoResponder(autoResponder, profileTz);
		for (MailFilter filter : mailFilters) {
			this.filters.add(new JsMailFilter(filter));
		}
	}
	
	public AutoResponder createAutoResponderForUpdate(DateTimeZone profileTz) {
		return autoResponder.createAutoResponderForUpdate(profileTz);
	}
	
	public ArrayList<MailFilter> createMailFiltersForUpdate() {
		ArrayList<MailFilter> list = new ArrayList<>();
		for (JsMailFilter filter : filters) {
			list.add(filter.createMailFilterForUpdate());
		}
		return list;
	}
}
