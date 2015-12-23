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

import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class UserOptionsService extends BaseUserOptionsService {
	public static final Logger logger = WT.getLogger(UserOptionsService.class);
	
	@Override
	public void processUserOptions(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String payload) {
		//Connection con = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			
			/*
			MailUserSettings mus = new MailUserSettings(getServiceId(), getTargetProfileId());
			
			if(crud.equals(Crud.READ)) {
				JsUserOptions jso = new JsUserOptions(getTargetProfileId().toString());
				
				// Main
				jso.view = cus.getCalendarView();
				jso.workdayStart = hmf.print(cus.getWorkdayStart());
				jso.workdayEnd = hmf.print(cus.getWorkdayEnd());
				jso.eventReminderDelivery = cus.getEventReminderDelivery();
				
				new JsonResult(jso).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsUserOptions> pl = ServletUtils.getPayload(payload, JsUserOptions.class);
				
				// Main
				if(pl.map.has("view")) cus.setCalendarView(pl.data.view);
				if(pl.map.has("workdayStart")) cus.setWorkdayStart(hmf.parseLocalTime(pl.data.workdayStart));
				if(pl.map.has("workdayEnd")) cus.setWorkdayEnd(hmf.parseLocalTime(pl.data.workdayEnd));
				if(pl.map.has("eventReminderDelivery")) cus.setEventReminderDelivery(pl.data.eventReminderDelivery);
				
				new JsonResult().printTo(out);
			}
			*/
			
		} catch (Exception ex) {
			logger.error("Error executing UserOptions", ex);
			new JsonResult(false).printTo(out);
		} finally {
			//DbUtils.closeQuietly(con);
		}
	}
}
