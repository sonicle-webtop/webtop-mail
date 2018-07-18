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

import com.sonicle.webtop.core.sdk.bol.js.JsUserOptionsBase;

/**
 *
 * @author malbinola
 */
public class JsUserOptions extends JsUserOptionsBase {
	public boolean permAccountManage;	
	public boolean permMailcardManage;	
	public boolean permDomainMailcardManage;	
	public String archiveMode;
	public boolean archiveKeepFoldersStructure;
	public String dmsSimpleMailFolder;
	public String dmsMethod;
	public boolean sharedSeen;
	public boolean manualSeen;
	public boolean ingridPreview;
	public boolean scanAll;
	public int scanSeconds;
	public int scanCycles;
	public String folderPrefix;
	public String folderSent;
	public String folderDrafts;
	public String folderTrash;
	public String folderSpam;
	public String folderArchive;
	public char folderSeparator;
	public String mainEmail;
	public String replyTo;
	public String sharedSort;
	public String readReceiptConfirmation;
	public boolean includeMessageInReply;
	public String host;
	public int port;
	public String username;
	public String password;
	public String protocol;
	public String defaultFolder;
	public String format;
	public String font;
	public String fontColor;
	public int fontSize;
	public boolean receipt;
	public boolean priority;
	public boolean noMailcardOnReplyForward;
	public boolean showUpcomingEvents;
	public boolean showUpcomingTasks;
	
	public JsUserOptions() {}
	
	public JsUserOptions(String id) {
		super(id);
	}
}
