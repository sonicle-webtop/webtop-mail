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

import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.BaseService;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class MailServiceTest extends BaseService {
	
	public static final Logger logger = WT.getLogger(MailServiceTest.class);
	
	@Override
	public void initialize() {
		
	}

	@Override
	public void cleanup() {
		
	}
	
	public void processGetImapTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String pfoldername=request.getParameter("node");
		ArrayList<JSonFolder> jsfolders=new ArrayList<>();
		logger.debug("GetImapTree - node={}",pfoldername);
		switch(pfoldername) {
			case "root":
				jsfolders.add(new JSonFolder("folder1","Folder 1",0,false,true,"root"));
				jsfolders.add(new JSonFolder("folder2","Folder 2",172,false,false,"root"));
				break;
				
			case "folder2":
				jsfolders.add(new JSonFolder("subfolder1","Subfolder 1",0,false,true,pfoldername));
				jsfolders.add(new JSonFolder("subfolder2","Subfolder 2",125,false,true,pfoldername));
				jsfolders.add(new JSonFolder("subfolder3","Subfolder 3",47,false,true,pfoldername));
				break;
		}
			
		out.println(JsonResult.gson().toJson(jsfolders));
	}
	
	private class JSonFolder {
		String id;
		String folder;
		int unread;
		boolean hasUnread;
		boolean leaf;
		String parentId;
		
		String text;
		
		JSonFolder(String id,String folder, int unread, boolean hasUnread, boolean leaf, String parentId) {
			this.id=id;
			this.folder=folder;
			this.unread=unread;
			this.hasUnread=hasUnread;
			this.leaf=leaf;
			this.parentId=parentId;
			
			this.text=folder;
		}
	}
}
