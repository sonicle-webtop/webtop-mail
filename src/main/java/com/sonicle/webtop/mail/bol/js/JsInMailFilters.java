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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.mail.model.AutoResponder;
import com.sonicle.webtop.mail.model.MailFilter;
import com.sonicle.webtop.mail.model.MailFiltersType;
import com.sonicle.webtop.mail.model.SieveActionList;
import com.sonicle.webtop.mail.model.SieveRuleList;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

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
	public static DateTimeZone profileTimeZone;
	
	public JsInMailFilters(int scriptsCount, String activeScript, AutoResponder autoResponder, List<MailFilter> mailFilters, DateTimeZone profileTimeZone) {
		this.scriptsCount = scriptsCount;
		this.activeScript = activeScript;
		this.autoResponder = createJsAutoResponder(autoResponder);
		this.profileTimeZone = profileTimeZone;
		
		for(MailFilter filter : mailFilters) {
			JsMailFilter jsmf = new JsMailFilter(this.id);
			jsmf.filterId = filter.getFilterId();
			jsmf.enabled = filter.getEnabled();
			jsmf.order = filter.getOrder();
			jsmf.name = filter.getName();
			jsmf.sieveMatch = filter.getSieveMatch();
			jsmf.sieveRules = LangUtils.serialize(filter.getSieveRules(), SieveRuleList.class);
			jsmf.sieveActions = LangUtils.serialize(filter.getSieveActions(), SieveActionList.class);
			filters.add(jsmf);
		}
	}
	
	private JsAutoResponder createJsAutoResponder(AutoResponder aut) {
		JsAutoResponder js = new JsAutoResponder();
		js.enabled = aut.getEnabled();
		js.message = aut.getMessage();
		js.addresses = aut.getAddresses();
		js.activationStartDate = DateTimeUtils.printYmdHmsWithZone(aut.getActivationStartDate(), profileTimeZone);
		js.activationEndDate = DateTimeUtils.printYmdHmsWithZone(aut.getActivationEndDate(), profileTimeZone);
		js.daysInterval = aut.getDaysInterval();
		return js;
	}
	
	public static AutoResponder createAutoResponder(JsInMailFilters jsimf) {
		AutoResponder aut = new AutoResponder();
		aut.setEnabled(jsimf.autoResponder.enabled);
		aut.setMessage(jsimf.autoResponder.message);
		aut.setAddresses(jsimf.autoResponder.addresses);
		
		DateTime activationStartDate = null;
		DateTime activationEndDate = null;
		if(jsimf.autoResponder.activationStartDate != null) {
			activationStartDate = changeTimeTo(DateTime.parse(jsimf.autoResponder.activationStartDate), 0, 0, 0);
		   
		}
		if(jsimf.autoResponder.activationEndDate != null) {
			 activationEndDate = changeTimeTo(DateTime.parse(jsimf.autoResponder.activationEndDate), 0, 0, 0);
		}
		
		String activationStartDateWithoutMillis = removeMillis(activationStartDate);
		String activationEndDateWithoutMillis = removeMillis(activationEndDate);
		
		aut.setActivationStartDate(DateTimeUtils.parseYmdHmsWithZone(activationStartDateWithoutMillis, profileTimeZone));
		aut.setActivationEndDate(DateTimeUtils.parseYmdHmsWithZone(activationEndDateWithoutMillis, profileTimeZone));
		aut.setDaysInterval(jsimf.autoResponder.daysInterval);
		return aut;
	}
	
	private static String removeMillis(DateTime dateTime) {
		if(dateTime == null) return null;
		String dateTimeString = dateTime.toString();
		return dateTimeString.substring(0, dateTimeString.indexOf("."));	
	}
	
	private static DateTime changeTimeTo(DateTime dateTime, int hour, int minutes, int seconds) {
		if(dateTime == null) return null;
		return dateTime.withHourOfDay(hour).withMinuteOfHour(minutes).withSecondOfMinute(seconds);
	}
	
	public static MailFilter createMailFilter(JsMailFilter jsmf) {
		if (jsmf == null) return null;
		MailFilter mf = new MailFilter();
		mf.setFilterId(jsmf.filterId);
		mf.setEnabled(jsmf.enabled);
		mf.setOrder(jsmf.order);
		mf.setName(jsmf.name);
		mf.setSieveMatch(jsmf.sieveMatch);
		mf.setSieveRules(LangUtils.deserialize(jsmf.sieveRules, new SieveRuleList(), SieveRuleList.class));
		mf.setSieveActions(LangUtils.deserialize(jsmf.sieveActions, new SieveActionList(), SieveActionList.class));
		return mf;
	}
	
	public static ArrayList<MailFilter> createMailFilterList(JsInMailFilters jsimf) {
		ArrayList<MailFilter> list = new ArrayList<>();
		for(JsMailFilter jsmf : jsimf.filters) {
			list.add(createMailFilter(jsmf));
		}
		return list;
	}
}
