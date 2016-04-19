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
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.text.MessageFormat;
import java.util.HashMap;

/**
 *
 * @author gbulfon
 */
public class MailUserSettings extends BaseUserSettings {

	public static final String MESSAGE_QUICKPART = "message.quickpart@{0}"; // !IMPORTANT
	public static final String MESSAGE_LIST_GROUP = "messagelist.group@{0}"; // was : "messagelist-group-{0}"
	public static final String MESSAGE_LIST_SORT = "messagelist.sort@{0}"; // was : "messagelist-{0}-sort"
	public static final String MESSAGE_LIST_THREADED = "messagelist.threaded@{0}"; // was : "list-threaded-{0}"
	public static final String COLUMN_SIZE_PREFIX = "column.size@"; //was : "column-{0}"
	public static final String COLUMN_SIZE = COLUMN_SIZE_PREFIX+"{0}"; //was : "column-{0}"
	public static final String COLUMN_VISIBLE = "column.visible@{0}";
	public static final String COLUMNS_ORDER = "columns.order";
	public static final String SHARED_SEEN = "sharedseen";
	public static final String MANUAL_SEEN = "manualseen";
	public static final String SHARING_RIGHTS = "sharing.rights";
	public static final String FOLDER_PEFFIX = "folder.prefix";
	public static final String SCAN_ALL = "scan.all";
	public static final String SCAN_SECONDS = "scan.seconds";
	public static final String SCAN_CYCLES = "scan.cycles";
	public static final String FOLDER_SENT = "folder.sent";
	public static final String FOLDER_DRAFTS = "folder.drafts";
	public static final String FOLDER_TRASH = "folder.trash";
	public static final String FOLDER_SPAM = "folder.spam";
	public static final String DEFAULT_FOLDER = "defaultfolder";
	public static final String REPLY_TO = "reply.to";
	public static final String SHARED_SORT = "shared.sort";
	public static final String INCLUDE_MESSAGE_IN_REPLY = "include.message.in.reply";
	public static final String PAGE_ROWS = "page.rows";
	public static final String HOST="host";
	public static final String PORT="port";
	public static final String USERNAME="username";
	public static final String PASSWORD="password";
	public static final String PROTOCOL="protocol";
	public static final String MESSAGE_VIEW_REGION = "message.view.region";
	public static final String MESSAGE_VIEW_WIDTH = "message.view.width";
	public static final String MESSAGE_VIEW_HEIGHT = "message.view.height";
	public static final String MESSAGE_VIEW_COLLAPSED = "message.view.collapsed";
	public static final String FONT_NAME = "font.name";
	public static final String FONT_SIZE = "font.size";
	public static final String RECEIPT = "receipt";
    
	private MailServiceSettings mss; //TODO: portare le chiavi di default qui?
	
	public MailUserSettings(UserProfile.Id profileId) {
		super("com.sonicle.webtop.mail",profileId);
	}
	
	public MailUserSettings(UserProfile.Id profileId, MailServiceSettings mss) {
		super("com.sonicle.webtop.mail",profileId);
		this.mss=mss;
	}
	
	/**
	 * [string]
	 * Archiving operative method. One of: simple, structured, webtop.
	 * A null value indicated no method.
	 */
	public static final String ARCHIVING_METHOD = "archiving.method";
	
	public static final String ARCHIVING_METHOD_NONE = "none";
	public static final String ARCHIVING_METHOD_SIMPLE = "simple";
	public static final String ARCHIVING_METHOD_STRUCTURED = "structured";
	public static final String ARCHIVING_METHOD_WEBTOP = "webtop";
	
	/**
	 * [string]
	 * IMAP folder to be monitored by the archiving process
	 */
	public static final String SIMPLE_ARCHIVING_MAIL_FOLDER = "archiving.simple.mailfolder";
	
	public String getSimpleArchivingMailFolder() {
		return getString(SIMPLE_ARCHIVING_MAIL_FOLDER, null);
	}
	
	public boolean setSimpleArchivingMailFolder(String value) {
		return setString(SIMPLE_ARCHIVING_MAIL_FOLDER, value);
	}
	
	public String getArchivingMethod() {
		return getString(ARCHIVING_METHOD, "none");
	}
	
	public boolean setArchivingMethod(String value) {
		return setString(ARCHIVING_METHOD, value);
	}
	
    
    public boolean isSharedSeen() {
        return getBoolean(SHARED_SEEN, false);
    }
	
    public boolean setSharedSeen(boolean b) {
        return setBoolean(SHARED_SEEN, b);
    }
	
    public boolean isManualSeen() {
        return getBoolean(MANUAL_SEEN, false);
    }
	
	public boolean setManualSeen(boolean b) {
        return setBoolean(MANUAL_SEEN, b);
	}
	
	public String getMessageListGroup(String foldername) {
		return getString(MessageFormat.format(MESSAGE_LIST_GROUP, foldername),"");
	}
	
	public boolean setMessageListGroup(String foldername, String group) {
		return setString(MessageFormat.format(MESSAGE_LIST_GROUP, foldername),group);
	}
	
	public int getColumnSize(String name) {
		return getInteger(MessageFormat.format(COLUMN_SIZE,name),100);
	}
	
	public boolean setColumnSize(String name, int size) {
		return setInteger(MessageFormat.format(COLUMN_SIZE,name),size);
	}
	
	public ColumnsOrderSetting getColumnsOrderSetting() {
		return LangUtils.value(getSetting(COLUMNS_ORDER),new ColumnsOrderSetting(), ColumnsOrderSetting.class);
	}
	
	public boolean setColumnsOrderSetting(ColumnsOrderSetting cos) {
		return setObject(COLUMNS_ORDER,cos,ColumnsOrderSetting.class);
	}
    
	public void clearColumnsOrderSetting() {
		clear(COLUMNS_ORDER);
	}
	
	public ColumnVisibilitySetting getColumnVisibilitySetting(String foldername) {
		return LangUtils.value(
				getSetting(MessageFormat.format(COLUMN_VISIBLE, foldername)),
				new ColumnVisibilitySetting(), ColumnVisibilitySetting.class
		);
	}
	
	public boolean setColumnVisibilitySetting(String foldername, ColumnVisibilitySetting cvs) {
		return setObject(MessageFormat.format(COLUMN_VISIBLE, foldername),cvs,ColumnVisibilitySetting.class);
	}
    
	public boolean clearColumnVisibilitySetting(String foldername) {
		return clear(MessageFormat.format(COLUMN_VISIBLE, foldername));
	}
	
	public String getMessageListSort(String foldername) {
		return getString(MessageFormat.format(MESSAGE_LIST_SORT, foldername),"date|DESC");
	}
	
	public boolean setMessageListSort(String foldername, String value) {
		return setString(MessageFormat.format(MESSAGE_LIST_SORT, foldername),value);
	}
	
	public boolean setMessageListSort(String foldername, String field, String direction) {
		return setString(MessageFormat.format(MESSAGE_LIST_SORT, foldername),field+"|"+direction);
	}
	
	public boolean isMessageListThreaded(String foldername) {
		return getBoolean(MessageFormat.format(MESSAGE_LIST_THREADED, foldername),false);
	}
	
	public boolean setMessageListThreaded(String foldername, boolean threaded) {
		return setBoolean(MessageFormat.format(MESSAGE_LIST_THREADED, foldername),threaded);
	}
	
	public String getFolderPrefix() {
		String s=getString(FOLDER_PEFFIX, null);
		if (s==null) s=mss.getDefaultFolderPrefix();
		return s;
	}
	
	public boolean setFolderPrefix(String prefix) {
		return setString(FOLDER_PEFFIX,prefix);
	}
	
	public boolean isScanAll() {
		Boolean b=getBoolean(SCAN_ALL,null);
		if (b==null) b=mss.isDefaultScanAll();
		return b;
	}
	
	public boolean setScanAll(boolean b) {
		return setBoolean(SCAN_ALL,b);
	}
	
	public int getScanSeconds() {
		Integer i=getInteger(SCAN_SECONDS,null);
		if (i==null) i=mss.getDefaultScanSeconds();
		return i;
	}
	
	public boolean setScanSeconds(int seconds) {
		return setInteger(SCAN_SECONDS,seconds);
	}
	
	public int getScanCycles() {
		Integer i=getInteger(SCAN_CYCLES,null);
		if (i==null) i=mss.getDefaultScanCycles();
		return i;
	}
	
	public boolean setScanCycles(int cycles) {
		return setInteger(SCAN_CYCLES,cycles);
	}
	
	public String getFolderSent() {
		String s=getString(FOLDER_SENT,null);
		if (s==null) s=mss.getDefaultFolderSent();
		return s;
	}
	
	public boolean setFolderSent(String name) {
		return setString(FOLDER_SENT,name);
	}
	
	public String getFolderDrafts() {
		String s=getString(FOLDER_DRAFTS,null);
		if (s==null) s=mss.getDefaultFolderDrafts();
		return s;
	}
	
	public boolean setFolderDrafts(String name) {
		return setString(FOLDER_DRAFTS,name);
	}
	
	public String getFolderTrash() {
		String s=getString(FOLDER_TRASH,null);
		if (s==null) s=mss.getDefaultFolderTrash();
		return s;
	}

	public boolean setFolderTrash(String name) {
		return setString(FOLDER_TRASH,name);
	}
	
	public String getFolderSpam() {
		String s=getString(FOLDER_SPAM,null);
		if (s==null) s=mss.getDefaultFolderSpam();
		return s;
	}
	
	public boolean setFolderSpam(String name) {
		return setString(FOLDER_SPAM,name);
	}
	
	public String getReplyTo() {
		String s=getString(REPLY_TO, null);
		if (s==null) {
			s=WT.getUserData(profileId).getEmail().toString();
		}
		return s;
	}
	
	public boolean setReplyTo(String replyto) {
		return setString(REPLY_TO,replyto);
	}
	
	public String getSharedSort() {
		return getString(SHARED_SORT,"N");
	}
	
	public boolean setSharedSort(String sharedSort) {
		return setString(SHARED_SORT,sharedSort);
	}
	
	public boolean isIncludeMessageInReply() {
		Boolean b=getBoolean(INCLUDE_MESSAGE_IN_REPLY, null);
		if (b==null) b=mss.isDefaultIncludeMessageInReply();
		return b;
	}
	
	public boolean setIncludeMessageInReply(boolean b) {
		return setBoolean(INCLUDE_MESSAGE_IN_REPLY,b);
	}
	
	public int getPageRows() {
		Integer i=getInteger(PAGE_ROWS,null);
		if (i==null) i=mss.getDefaultPageRows();
		return i;
	}
	
	public boolean setPageRows(int rows) {
		return setInteger(PAGE_ROWS, rows);
	}
	
	public String getHost() {
		String s=getString(HOST,null);
		if (s==null) s=mss.getDefaultHost();
		return s;
	}
	
	public boolean setHost(String host) {
		return setString(HOST, host);
	}
	
	public int getPort() {
		Integer i=getInteger(PORT,null);
		if (i==null) i=mss.getDefaultPort();
		return i;
	}
	
	public boolean setPort(int port) {
		return setInteger(PORT, port);
	}
	
	public String getUsername() {
		return getString(USERNAME,null);
	}
	
	public boolean setUsername(String username) {
		return setString(USERNAME, username);
	}
	
	public String getPassword() {
		return getString(PASSWORD,null);
	}
	
	public boolean setPassword(String password) {
		return setString(PASSWORD, password);
	}
	
	public String getProtocol() {
		String s=getString(PROTOCOL,null);
		if (s==null) s=mss.getDefaultProtocol();
		return s;
	}
	
	public boolean setProtocol(String protocol) {
		return setString(PROTOCOL, protocol);
	}
	
	public String getMessageViewRegion() {
		return getString(MESSAGE_VIEW_REGION,"south");
	}
	
	public boolean setMessageViewRegion(String region) {
		return setString(MESSAGE_VIEW_REGION,region);
	}
	
	public int getMessageViewWidth() {
		return getInteger(MESSAGE_VIEW_WIDTH,640);
	}
	
	public boolean setMessageViewWidth(int width) {
		return setInteger(MESSAGE_VIEW_WIDTH, width);
	}
	
	public int getMessageViewHeight() {
		return getInteger(MESSAGE_VIEW_HEIGHT,400);
	}
	
	public boolean setMessageViewHeight(int height) {
		return setInteger(MESSAGE_VIEW_HEIGHT,height);
	}
	
	public boolean getMessageViewCollapsed() {
		return getBoolean(MESSAGE_VIEW_COLLAPSED,false);
	}
	
	public boolean setMessageViewCollapsed(boolean collapsed) {
		return setBoolean(MESSAGE_VIEW_COLLAPSED,collapsed);
	}
	
	public String getDefaultFolder() {
		return getString(DEFAULT_FOLDER,null);
	}
	
	public boolean setDefaultFolder(String name) {
		return setString(DEFAULT_FOLDER,name);
	}
	
	public HashMap<String,Integer> getColumnSizes() {
		return getIntegers(COLUMN_SIZE_PREFIX);
	}
	
	public String getFontName() {
		String s=getString(FONT_NAME,null);
		if (s==null) s=mss.getDefaultFontName();
		return s;
	}
	
	public boolean setFontName(String fontname) {
		return setString(FONT_NAME, fontname);
	}
	
	public int getFontSize() {
		Integer i=getInteger(FONT_SIZE,null);
		if (i==null) i=mss.getDefaultFontSize();
		return i;
	}
	
	public boolean setFontSize(int size) {
		return setInteger(FONT_SIZE, size);
	}
	
	public boolean isReceipt() {
		Boolean b=getBoolean(RECEIPT,null);
		if (b==null) b=mss.isDefaultReceipt();
		return b;
	}
	
	public boolean setReceipt(boolean b) {
		return setBoolean(RECEIPT,b);
	}
	
}
