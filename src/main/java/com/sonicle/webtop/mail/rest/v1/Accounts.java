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
package com.sonicle.webtop.mail.rest.v1;

import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.mail.MailManager;
import com.sonicle.webtop.mail.MailServiceSettings;
import com.sonicle.webtop.mail.MailUserProfile;
import com.sonicle.webtop.mail.MailUserSettings;
import com.sonicle.webtop.mail.swagger.v1.api.AccountsApi;
import com.sonicle.webtop.mail.swagger.v1.model.Account;
import com.sonicle.webtop.mail.swagger.v1.model.ApiError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Accounts extends AccountsApi {
	private static final Logger logger = LoggerFactory.getLogger(Accounts.class);
	
	private MailUserProfile getMailUserProfile(CoreManager coreMgr, Map<String, AbstractDirectory> dirCache, UserProfileId pid) {
		logger.debug("getMailUserProfile - Start");
		MailServiceSettings mss = new MailServiceSettings(SERVICE_ID, pid.getDomainId());
		MailUserSettings mus = new MailUserSettings(pid, mss);
		MailManager mailMgr = (MailManager)WT.getServiceManager(SERVICE_ID, pid);
		
		AbstractDirectory dir = null;
		if (dirCache.containsKey(pid.getDomainId())) {
			dir = dirCache.get(pid.getDomainId());
			logger.debug("Dir found in cache");
		} else {
			logger.debug("Dir NOT found in cache");
			try {
				dir = coreMgr.getAuthDirectory(pid.getDomainId());
				if (dir == null) return null;
				dirCache.put(pid.getDomainId(), dir);
				logger.debug("ok, retrieved");
			} catch(WTException ex) {
				return null;
			}	
		}
		
		logger.debug("Building profile");
		MailUserProfile mailProfile = new MailUserProfile(mailMgr, mss, mus, dir.getScheme());
		logger.debug("getMailUserProfile - End");
		return mailProfile;
	}
	
	@Override
	public Response getAccounts(String targetProfileId) {
		
		try {
			//TODO: Allow calls for domain admin
			RunContext.ensureIsWebTopAdmin();
			
			UserProfileId targetPid = getTargetProfileId(targetProfileId);
			if (targetPid == null) return respErrorBadRequest("Missing parameter [targetProfileId]");
			
			logger.debug("Getting avail users...");
			CoreManager coreMgr = WT.getCoreManager(targetPid);
			List<OUser> ousers = null;
			if (RunContext.isWebTopAdmin()) {
				ousers = coreMgr.listUsers(true);
			} else {
				OUser ouser = coreMgr.getUser();
				if (ouser == null) 
				ousers = new ArrayList<>(Arrays.asList(ouser));
			}
			logger.debug("Found {} users", ousers.size());
			
			Map<String, AbstractDirectory> dirCache = new HashMap<>();
			ArrayList<Account> items = new ArrayList<>();
			for (OUser ouser : ousers) {
				UserProfileId pid = new UserProfileId(ouser.getDomainId(), ouser.getUserId());
				logger.debug("Checking {}", pid.toString());
				MailUserProfile mailProfile = getMailUserProfile(coreMgr, dirCache, pid);
				if (mailProfile == null) continue;
				
				items.add(createAccount(ouser, mailProfile));
			}
			return respOk(items);
			
		} catch(Exception ex) {
			logger.error("[{}] getAccounts()", RunContext.getRunProfileId(), ex);
			return respError(ex);
		}
	}
	
	private Account createAccount(OUser ouser, MailUserProfile mailProfile) {
		return new Account()
				.userId(ouser.getUserId())
				.displayName(ouser.getDisplayName())
				.mailUsername(mailProfile.getMailUsername());
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
				.code(status.getStatusCode())
				.description(message);
	}
}
