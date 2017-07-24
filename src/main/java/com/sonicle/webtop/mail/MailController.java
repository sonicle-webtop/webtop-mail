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

import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.interfaces.IControllerHandlesProfiles;

/**
 *
 * @author malbinola
 */
public class MailController extends BaseController implements IControllerHandlesProfiles {
	
	private static ServiceVersion SV_5_0_14=new ServiceVersion("5.0.14");
	
	@Override
	public void addProfile(UserProfileId profileId) throws WTException {
		MailManager manager = new MailManager(true, profileId);
		manager.addBuiltinTags();
	}

	@Override
	public void removeProfile(UserProfileId profileId, boolean deep) throws WTException {
	}

	@Override
	public void upgradeProfile(UserProfileId profileId, ServiceVersion current, ServiceVersion lastSeen) throws WTException {
		//Add default tag labels on v5.0.14
		if (current.compareTo(SV_5_0_14)>=0 && lastSeen.compareTo(SV_5_0_14)<0) {
			MailManager manager = new MailManager(true, profileId);
			manager.addBuiltinTags();
		}
	}
}
