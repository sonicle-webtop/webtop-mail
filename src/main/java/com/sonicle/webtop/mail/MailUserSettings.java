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

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import java.text.MessageFormat;

/**
 *
 * @author gbulfon
 */
public class MailUserSettings extends BaseUserSettings {

	public static final String COLUMN_VISIBLE = "column.visible@{0}";
	public static final String MESSAGE_QUICKPART = "message.quickpart";
	public static final String SHARED_SEEN = "sharedseen";
	public static final String SHARING_RIGHTS = "sharing.rights";
	public static final String MESSAGE_LIST_GROUP = "messagelist-group-{0}";
	public static final String MESSAGE_LIST_SORT = "messagelist-{0}-sort";
	public static final String COLUMN_SIZE = "column-{0}";
	public static final String FOLDER_PEFFIX = "{0}.folder.prefix";
	public static final String SCAN_ALL = "{0}.scan.all";
	public static final String SCAN_SECONDS = "{0}.scan.seconds";
	public static final String SCAN_CYCLES = "{0}.scan.cycles";
	public static final String FOLDER_SENT = "{0}.folder.sent";
	public static final String FOLDER_DRAFTS = "{0}.folder.drafts";
	public static final String FOLDER_TRASH = "{0}.folder.trash";
	public static final String FOLDER_SPAM = "{0}.folder.spam";
	public static final String REPLY_TO = "{0}.reply.to";
	public static final String SHARED_SORT = "{0}.shared.sort";
	public static final String INCLUDE_MESSAGE_IN_REPLY = "{0}.include.message.in.reply";
	public static final String NUM_MESSAGE_LIST = "{0}.num.message.list";
    
	private MailServiceSettings mss;
	
    public MailUserSettings(MailServiceSettings mss, String domainId, String userId, String serviceId) {
        super(domainId, userId, serviceId);
		this.mss=mss;
    }
    
    public boolean isSharedSeen() {
        return getUserSetting(SHARED_SEEN, false);
    }
	
	public String getMessageListGroup(String foldername) {
		return getUserSetting(MessageFormat.format(MESSAGE_LIST_GROUP, foldername),"");
	}
	
	public int getColumnSize(String name) {
		return getUserSetting(MessageFormat.format(COLUMN_SIZE,name),100);
	}
	
	public ColumnVisibilitySetting getColumnVisibilitySetting(String foldername) {
		return LangUtils.value(
				getUserSetting(MessageFormat.format(COLUMN_VISIBLE, foldername)),
				new ColumnVisibilitySetting(), ColumnVisibilitySetting.class
		);
	}
    
	public String getMessageListSort(String foldername) {
		return getUserSetting(MessageFormat.format(MESSAGE_LIST_SORT, foldername),"date|DESC");
	}
	
	public String getFolderPrefix(String mailUser) {
		return getUserSetting(MessageFormat.format(FOLDER_PEFFIX, mailUser), mss.getDefaultFolderPrefix());
	}
	
	public boolean isScanAll(String mailUser) {
		return getUserSetting(MessageFormat.format(SCAN_ALL, mailUser),mss.isDefaultScanAll());
	}
	
	public int getScanSeconds(String mailUser) {
		return getUserSetting(MessageFormat.format(SCAN_SECONDS, mailUser),mss.getDefaultScanSeconds());
	}
	
	public int getScanCycles(String mailUser) {
		return getUserSetting(MessageFormat.format(SCAN_CYCLES, mailUser),mss.getDefaultScanCycles());
	}
	
	public String getFolderSent(String mailUser) {
		return getUserSetting(MessageFormat.format(FOLDER_SENT, mailUser),mss.getDefaultFolderSent());
	}
	
	public String getFolderDrafts(String mailUser) {
		return getUserSetting(MessageFormat.format(FOLDER_DRAFTS, mailUser),mss.getDefaultFolderDrafts());
	}
	
	public String getFolderTrash(String mailUser) {
		return getUserSetting(MessageFormat.format(FOLDER_TRASH, mailUser),mss.getDefaultFolderTrash());
	}

	public String getFolderSpam(String mailUser) {
		return getUserSetting(MessageFormat.format(FOLDER_SPAM, mailUser),mss.getDefaultFolderSpam());
	}
	
	public String getReplyTo(String mailUser) {
		return getUserSetting(MessageFormat.format(REPLY_TO, mailUser));
	}
	
	public String getSharedSort(String mailUser) {
		return getUserSetting(MessageFormat.format(SHARED_SORT, mailUser),"N");
	}
	
	public boolean isIncludeMessageInReply(String mailUser) {
		return getUserSetting(MessageFormat.format(INCLUDE_MESSAGE_IN_REPLY, mailUser), mss.isDefaultIncludeMessageInReply());
	}
	
	public int getNumMessageList(String mailUser) {
		return getUserSetting(MessageFormat.format(NUM_MESSAGE_LIST, mailUser),mss.getDefaultNumMessageList());
	}
}
