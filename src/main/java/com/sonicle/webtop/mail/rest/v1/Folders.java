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
import com.sonicle.webtop.mail.MailServiceSettings;
import com.sonicle.webtop.mail.swagger.v1.api.FoldersApi;
import com.sonicle.webtop.mail.swagger.v1.model.ApiApiError;
import com.sonicle.webtop.mail.swagger.v1.model.ApiAttachment;
import com.sonicle.webtop.mail.swagger.v1.model.ApiContact;
import com.sonicle.webtop.mail.swagger.v1.model.ApiFolder;
import com.sonicle.webtop.mail.swagger.v1.model.ApiMessage;
import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gbulfon
 */
public class Folders extends FoldersApi {
	private static final Logger logger = LoggerFactory.getLogger(Folders.class);
	
	@Override
	public Response getRootFolders() {
		
		try {
			UserProfileId targetPid = RunContext.getRunProfileId();
			MailManager mmgr = new MailManager(true, targetPid);
			ArrayList<Folder> folders = mmgr.getRootFolders();
			ArrayList<ApiFolder> items = new ArrayList<>();
			for(Folder folder: folders) {
				ApiFolder af = new ApiFolder();
				af.setId(folder.getFullName());
				af.setName(folder.getName());
				af.setUnreadCount(0);
				af.setTotalCount(0);
				af.setChildren(new ArrayList<>());
				items.add(af);
			}
			return respOk(items);
			
		} catch(Exception ex) {
			logger.error("[{}] getExternalArchivingConfiguration()", RunContext.getRunProfileId(), ex);
			return respError(ex);
		}
	}
	
	@Override
	public Response getFolders(String id) {
		
		try {
			UserProfileId targetPid = RunContext.getRunProfileId();
			MailManager mmgr = new MailManager(true, targetPid);
			ArrayList<Folder> folders = mmgr.getFolders(id.replace("|", "/"));
			ArrayList<ApiFolder> items = new ArrayList<>();
			for(Folder folder: folders) {
				ApiFolder af = new ApiFolder();
				af.setId(folder.getFullName().replace("/", "|"));
				af.setName(folder.getName());
				af.setUnreadCount(0);
				af.setTotalCount(0);
				af.setChildren(new ArrayList<>());
				items.add(af);
			}
			return respOk(items);
			
		} catch(Exception ex) {
			logger.error("[{}] getExternalArchivingConfiguration()", RunContext.getRunProfileId(), ex);
			return respError(ex);
		}
	}
	
/*	public ArrayList<ApiFolder> loadChildren(Folder folders[]) throws MessagingException {
		for(Folder folder: folders) {
			ApiFolder af = new ApiFolder();
			af.setId(folder.getFullName());
			af.setName(folder.getName());
			af.setUnreadCount(0);
			af.setTotalCount(0);
			ArrayList<Folder> children = new ArrayList<>();
			for(Folder child: folder.list()) children.add(child);
			af.setChildren(loadItems(children, new ArrayList<ApiFolder>()));
			apiFolders.add(af);
		}
		return apiFolders;
	}*/
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiApiError()
				.code(status.getStatusCode())
				.description(message);
	}

	
}
