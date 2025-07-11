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

import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.webtop.mail.model.AutoResponder;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class JsAutoResponder {
	public Boolean enabled;
	public String subject;
	public String message;
	public String addresses;
	public String activationStartDate;
	public String activationEndDate;
	public Short daysInterval;
	
	public JsAutoResponder() {}
	
	public JsAutoResponder(AutoResponder autoResponder, DateTimeZone profileTz) {
		this.enabled = autoResponder.getEnabled();
		this.subject = autoResponder.getSubject();
		this.message = autoResponder.getMessage();
		this.addresses = autoResponder.getAddresses();
		this.activationStartDate = JodaTimeUtils.printYMDHMS(profileTz, autoResponder.getActivationStartDate());
		this.activationEndDate = JodaTimeUtils.printYMDHMS(profileTz, autoResponder.getActivationEndDate());
		this.daysInterval = autoResponder.getDaysInterval();
	}
	
	public AutoResponder createAutoResponderForUpdate(DateTimeZone profileTz) {
		AutoResponder item = new AutoResponder();
		item.setEnabled(enabled);
		item.setSubject(subject);
		item.setMessage(message);
		item.setAddresses(addresses);
		item.setDaysInterval(daysInterval);
		item.setActivationStartDate(JodaTimeUtils.withTimeAtStartOfDay(JodaTimeUtils.parseDateTimeYMDHMS(profileTz, activationStartDate)));
		item.setActivationEndDate(JodaTimeUtils.withTimeAtStartOfDay(JodaTimeUtils.parseDateTimeYMDHMS(profileTz, activationEndDate)));
		return item;
	}
}
