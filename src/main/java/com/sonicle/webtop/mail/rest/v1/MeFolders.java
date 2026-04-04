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

import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.mail.MailManager;
import com.sonicle.webtop.mail.swagger.v1.api.MeFoldersApi;
import com.sonicle.webtop.mail.swagger.v1.model.ApiApiError;
import com.sonicle.webtop.mail.swagger.v1.model.ApiFolder;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import java.util.ArrayList;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gbulfon
 */
public class MeFolders extends MeFoldersApi {
	private static final Logger logger = LoggerFactory.getLogger(MeFolders.class);
	
	@Override
	public Response listRootFolders() {
		UserProfileId targetPid = RunContext.getRunProfileId();
		try {
			MailManager mmgr = MailRestApiUtils.getMailManager(targetPid);
			ArrayList<Folder> folders = mmgr.getRootFolders();
			ArrayList<ApiFolder> items = new ArrayList<>();
			for(Folder folder: folders) {
				ApiFolder af = new ApiFolder();
				af.setId(folder.getFullName());
				af.setName(folder.getName());
				try {
					af.setUnreadCount(folder.getUnreadMessageCount());
					af.setTotalCount(folder.getMessageCount());
				} catch(MessagingException exc) {}
				af.setChildren(new ArrayList<>());
				items.add(af);
			}
			return respOk(items);
			
		} catch(Exception ex) {
			logger.error("[{}] getRootFolders()", targetPid, ex);
			return respError(ex);
		}
	}
	
	@Override
	public Response listChildrenFolders(String folderId) {
		UserProfileId targetPid = RunContext.getRunProfileId();
		try {
			MailManager mmgr = MailRestApiUtils.getMailManager(targetPid);
			ArrayList<Folder> folders = mmgr.getFolders(folderId);
			ArrayList<ApiFolder> items = new ArrayList<>();
			for(Folder folder: folders) {
				ApiFolder af = new ApiFolder();
				af.setId(folder.getFullName());
				af.setName(folder.getName());
				try {
					af.setUnreadCount(folder.getUnreadMessageCount());
					af.setTotalCount(folder.getMessageCount());
				} catch(MessagingException exc) {}
				af.setChildren(new ArrayList<>());
				items.add(af);
			}
			return respOk(items);
			
		} catch(Exception ex) {
			logger.error("[{}] getFolders()", targetPid, folderId, ex);
			return respError(ex);
		}
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiApiError()
				.code(status.getStatusCode())
				.description(message);
	}

}
