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
import com.sonicle.commons.web.json.JsonResult;
import static com.sonicle.webtop.mail.MailSettings.*;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author gbulfon
 */
public class MailUserSettings extends BaseUserSettings {
	
	private MailServiceSettings mss; //TODO: portare le chiavi di default qui?
	
	public MailUserSettings(UserProfileId profileId) {
		super("com.sonicle.webtop.mail",profileId);
	}
	
	public MailUserSettings(UserProfileId profileId, MailServiceSettings mss) {
		super("com.sonicle.webtop.mail",profileId);
		this.mss=mss;
	}
	
	public String getSimpleDMSArchivingMailFolder() {
		return getString(ARCHIVING_SIMPLE_DMS_MAIL_FOLDER, null);
	}
	
	public boolean setSimpleArchivingMailFolder(String value) {
		return setString(ARCHIVING_SIMPLE_DMS_MAIL_FOLDER, value);
	}
	
	public String getDMSMethod() {
		return getString(ARCHIVING_DMS_METHOD, "none");
	}
	
	public boolean setDMSMethod(String value) {
		return setString(ARCHIVING_DMS_METHOD, value);
	}
	
	public String getArchiveMode() {
		return getString(ARCHIVING_MODE, ARCHIVING_MODE_SINGLE);
	}
	
	public boolean setArchiveMode(String value) {
		return setString(ARCHIVING_MODE, value);
	}
	
	public String getArchiveExternalUserFolder() {
		return getString(ARCHIVING_EXTERNAL_USERFOLDER, null);
	}
	
	public boolean setArchiveExternalUserFolder(String value) {
		return setString(ARCHIVING_EXTERNAL_USERFOLDER, value);
	}
	
	public boolean isArchiveKeepFoldersStructure() {
		return getBoolean(ARCHIVING_KEEP_FOLDERS_STRUCTURE, false);
	}
	
	public boolean setArchiveKeepFoldersStructure(boolean b) {
		return setBoolean(ARCHIVING_KEEP_FOLDERS_STRUCTURE, b);
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
	public boolean isSeenOnOpen() {
		return getBoolean(SEEN_ON_OPEN, false);
	}
	
	public boolean setSeenOnOpen(boolean b) {
		return setBoolean(SEEN_ON_OPEN, b);
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
	
	public HashMap<String,String> getMessageQuickParts() {
		return getStrings(MessageFormat.format(MESSAGE_QUICKPART, ""));
	}
	
	public String getMessageQuickPart(String name) {
		return getString(MessageFormat.format(MESSAGE_QUICKPART, name),"");
	}
	
	public boolean setMessageQuickPart(String name, String value) {
		return setString(MessageFormat.format(MESSAGE_QUICKPART, name),value);
	}
	
	public boolean deleteMessageQuickPart(String name) {
		return clear(MessageFormat.format(MESSAGE_QUICKPART, name));
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
	
	public String getFolderArchive() {
		String s=getString(FOLDER_ARCHIVE,null);
		if (s==null) s=mss.getDefaultFolderArchive();
		return s;
	}
	
	public boolean setFolderArchive(String name) {
		return setString(FOLDER_ARCHIVE,name);
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
		//if (s==null) {
		//	s=WT.getUserData(profileId).getEmailAddress();
		//}
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
	
	public String getViewMode() {
		String value = getString(VIEW_MODE, null);
		if (value != null) return value;
		return mss.getDefaultViewMode();
	}
	
	public boolean setViewMode(String mode) {
		return setString(VIEW_MODE, mode);
	}
	
	public String getReadReceiptConfirmation() {
		String s=getString(READ_RECEIPT_CONFIRMATION,null);
		if (s==null) s="ask";
		return s;
	}
	
	public boolean setReadReceiptConfirmation(String rrc) {
		return setString(READ_RECEIPT_CONFIRMATION, rrc);
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
		if (name!=null && name.trim().length()==0) name=null;
		return setString(DEFAULT_FOLDER,name);
	}
	
	public HashMap<String,Integer> getColumnSizes() {
		return getIntegers(COLUMN_SIZE_PREFIX);
	}
	
	public String getFormat() {
		String s=getString(FORMAT,null);
		if (s==null) s=mss.getDefaultFormat();
		return s;
	}
	
	public boolean setFormat(String format) {
		return setString(FORMAT, format);
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
	public String getFontColor() {
		String s=getString(FONT_COLOR,null);
		if (s==null) s="#000000";
		return s;
	}
	
	public boolean setFontColor(String fontcolor) {
		return setString(FONT_COLOR, fontcolor);
	}
	
	
	public boolean isReceipt() {
		Boolean b=getBoolean(RECEIPT,null);
		if (b==null) b=mss.isDefaultReceipt();
		return b;
	}
	
	public boolean setReceipt(boolean b) {
		return setBoolean(RECEIPT,b);
	}
	
	public boolean isAutoAddContact() {
		Boolean b = getBoolean(AUTO_ADD_CONTACT, null);
		if (b == null) b = false;
		return b;
	}
	
	public boolean setAutoAddContact(boolean b) {
		return setBoolean(AUTO_ADD_CONTACT, b);
	}
	
	public boolean isPriority() {
		Boolean b=getBoolean(PRIORITY,null);
		if (b==null) b=false;
		return b;
	}
	
	public boolean setPriority(boolean b) {
		return setBoolean(PRIORITY,b);
	}
	
	public boolean isNoMailcardOnReplyForward() {
		Boolean b=getBoolean(NO_MAILCARD_ON_REPLY_FORWARD, null);
		if (b==null) b=mss.isDefaultNoMailcardOnReplyForward();
		return b;
	}
	
	public boolean setNoMailcardOnReplyForward(boolean b) {
		return setBoolean(NO_MAILCARD_ON_REPLY_FORWARD,b);
	}
	
	public boolean getShowMessagePreviewOnRow() {
		Boolean value = getBoolean(SHOW_MESSAGE_PREVIEW_ON_ROW, null);
		return (value != null) ? value : mss.getDefaultShowMessagePreviewOnRow();
	}
	
	public boolean setShowMessagePreviewOnRow(boolean value) {
		return setBoolean(SHOW_MESSAGE_PREVIEW_ON_ROW, value);
	}
	
	public boolean getShowUpcomingEvents() {
		Boolean value = getBoolean(SHOW_UPCOMING_EVENTS, null);
		return (value != null) ? value : mss.getDefaultShowUpcomingEvents();
	}
	
	public boolean setShowUpcomingEvents(boolean value) {
		return setBoolean(SHOW_UPCOMING_EVENTS, value);
	}
	
	public boolean getShowUpcomingTasks() {
		Boolean value = getBoolean(SHOW_UPCOMING_TASKS, null);
		return (value != null) ? value : mss.getDefaultShowUpcomingTasks();
	}
	
	public boolean setShowUpcomingTasks(boolean value) {
		return setBoolean(SHOW_UPCOMING_TASKS, value);
	}

	/**
	 * @deprecated
	 * Remove when transition to new setting is completed
	 */	
	public Favorites getFavorites() {
		return getObject(FAVORITES, new Favorites(), Favorites.class);
	}
	
	/**
	 * @deprecated
	 * Remove when transition to new setting is completed
	 */	
	public boolean setFavorites(Favorites value) {
		return setObject(FAVORITES, value, Favorites.class);
	}
	
	public void deleteOldFavoritesSetting() {
		this.clear(FAVORITES);
	}
	
	/***
	 * new favorites implementation
	 */
	public boolean hasFavoriteFolders() {
		return getString(FAVORITE_FOLDERS,null)!=null;
	}
	
	public FavoriteFolders getFavoriteFolders() {
		return getObject(FAVORITE_FOLDERS, new FavoriteFolders(), FavoriteFolders.class);
	}
	
	public boolean setFavoriteFolders(FavoriteFolders value) {
		return setObject(FAVORITE_FOLDERS, value, FavoriteFolders.class);
	}
	
	public boolean setTodayRowColor(String value) {
		return setString(GRID_TODAY_ROW_COLOR, value);
	}
	
	public String getTodayRowColor() {
		String value = getString(GRID_TODAY_ROW_COLOR, null);
		return (value != null) ? value : mss.getDefaultTodayRowColor();
	}
	
	public static class FavoriteFolder {
		String accountId;
		String folderId;
		String description;
		
		public FavoriteFolder(String accountId, String folderId, String description) {
			this.accountId=accountId;
			this.folderId=folderId;
			this.description=description;
		}
	}
	
	public static class FavoriteFolders extends ArrayList<FavoriteFolder> {
		public FavoriteFolders() {
			super();
		}
		
		public static FavoriteFolders fromJson(String value) {
			return JsonResult.gson.fromJson(value, FavoriteFolders.class);
		}
		
		public static String toJson(FavoriteFolders value) {
			return JsonResult.gson.toJson(value, FavoriteFolders.class);
		}
		
		public FavoriteFolder remove(String accountId, String folderId) {
			for(FavoriteFolder ff: this) {
				if (ff.accountId.equals(accountId) && ff.folderId.equals(folderId)) {
					remove(ff);
					return ff;
				}
			}
			return null;
		}
		
		public boolean contains(String accountId, String folderId) {
			for(FavoriteFolder ff: this) {
				if (ff.accountId.equals(accountId) && ff.folderId.equals(folderId)) {
					return true;
				}
			}
			return false;
		}
		
		public void add(String accountId, String folderId, String description) {
			add(new FavoriteFolder(accountId, folderId, description));
		}
	}
	
	/**
	 * @deprecated
	 * Remove when transition to new setting is completed
	 */	
	public static class Favorites extends ArrayList<String> {
		public Favorites() {
			super();
		}
		
		public static Favorites fromJson(String value) {
			return JsonResult.gson.fromJson(value, Favorites.class);
		}
		
		public static String toJson(Favorites value) {
			return JsonResult.gson.toJson(value, Favorites.class);
		}
		
	}
}
