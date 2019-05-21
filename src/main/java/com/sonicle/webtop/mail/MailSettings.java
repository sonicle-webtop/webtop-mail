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
import java.util.ArrayList;

/**
 *
 * @author malbinola
 */
public class MailSettings {
	
	/**
	 * [user+domain+system][default]
	 * [long]
	 * Maximum file size single message attachment
	 */
	public static final String ATTACHMENT_MAXFILESIZE = "attachment.maxfilesize";
	
	/**
	 * [system+domain][default]
	 * [int]
	 * The sieve management port.
	 */
	public static final String SIEVE_PORT = "sieve.port";
	
	/**
	 * [system+domain][default]
	 * [boolean]
	 * Turns on/off balance-tags during preparation of html mail preview.
	 * Default is true (on).
	 */
	public static final String PREVIEW_BALANCE_TAGS = "preview.balancetags";
	
	/**
	 * [user][default]
	 * [boolean]
	 * Activate or not message body preview in messages grid
	 */
	public static final String SHOW_MESSAGE_PREVIEW_ON_ROW = "ingrid.preview";
	
	/**
	 * [user][default] (#F8F8C8)
	 * [string]
	 * Define the row background color for today messages.
	 */
	public static final String GRID_TODAY_ROW_COLOR = "grid.today.color";
	
	/**
	 * [user][default]
	 * [boolean]
	 * Activate a view showing all upcoming events.
	 */
	public static final String SHOW_UPCOMING_EVENTS = "upcoming.events.show";
	
	/**
	 * [user][default]
	 * [boolean]
	 * Activate a view showing all upcoming tasks.
	 */
	public static final String SHOW_UPCOMING_TASKS = "upcoming.tasks.show";
	
	/**
	 * [domain+system]
	 * [boolean]
	 * If false, disables recipients removing if included in my identities. Default to true.
	 */
	public static final String MESSAGE_REPLYALL_STRIPMYIDENTITIES = "message.replyall.stripmyidentities";
	
	
	/**
	 * [system+domain]
	 * [boolean]
	 * Is external archiving active. Defaults to false.
	 */
	public static final String ARCHIVING_EXTERNAL = "archiving.external";
	
	/**
	 * [system+domain]
	 * [string]
	 * Type of external archiving: imapsync | mailstore
	 */
	public static final String ARCHIVING_EXTERNAL_TYPE = "archiving.external.type";
	
	/**
	 * [system+domain]
	 * [string]
	 * IMAP host of the external archiving account. Defaults to "localhost".
	 */
	public static final String ARCHIVING_EXTERNAL_HOST = "archiving.external.host";
	
	/**
	 * [system+domain]
	 * [int]
	 * IMAP port of the external archiving account. Defaults to 143.
	 */
	public static final String ARCHIVING_EXTERNAL_PORT = "archiving.external.port";
	
	/**
	 * [system+domain]
	 * [int]
	 * Protocol of the external archiving account. May be imap or imaps. Defaults to imap.
	 */
	public static final String ARCHIVING_EXTERNAL_PROTOCOL = "archiving.external.protocol";
	
	/**
	 * [system+domain]
	 * [string]
	 * IMAP user of the external archiving account. Defaults to "domain-archive".
	 */
	public static final String ARCHIVING_EXTERNAL_USERNAME = "archiving.external.username";

	/**
	 * [system+domain]
	 * [string]
	 * IMAP password of the external archiving account. Defaults to "secret".
	 */
	public static final String ARCHIVING_EXTERNAL_PASSWORD = "archiving.external.password";

	/**
	 * [system+domain]
	 * [string]
	 * IMAP folder prefix of the external archiving account. Defaults to null.
	 */
	public static final String ARCHIVING_EXTERNAL_FOLDER_PREFIX = "archiving.external.folder.prefix";
	
	/**
	 * [system+domain]
	 * [integer]
	 * Messages older that X days will be archived, newer messages will be skipped.
	 */
	public static final String ARCHIVING_EXTERNAL_MINAGE = "archiving.external.minage";

	/**
	 * [system+domain][default]
	 * [string]
	 * Transform WebTop Public Resource links (e.g. Public images for mailcards) into attached inline images (cids).
	 */
	public static final String PUBLIC_RESOURCE_LINKS_AS_INLINE_ATTACHMENTS = "public.resource.links.as.inline.attachments";
	
    public static final String SPECIALFOLDERS_AUTOCREATE = "specialfolders.autocreate";
	public static final String DMS_ARCHIVE = "archive";
	public static final String ATTACHMENT_MAXSIZE = "attachment.maxsize";
	public static final String ATTACHMENT_DIR = "attachment.dir";
	public static final String MESSAGE_VIEW_MAX_TOS = "message.view.max.tos";
	public static final String MESSAGE_VIEW_MAX_CCS = "message.view.max.ccs";
	public static final String MESSAGE_EDIT_SUBJECT = "message.edit.subject";
	public static final String SORT_FOLDERS = "sort.folders";
	public static final String SPAMADM_SPAM = "spamadm.spam";
	public static final String ADMIN_USER = "admin.user";
	public static final String ADMIN_PASSWORD = "admin.password";
	public static final String NETHTOP_VMAIL_SECRET = "nethtop.vmail.secret";
	public static final String SCHEDULED_EMAILS_DISABLED = "scheduled-emails.disabled";
	public static final String SIEVE_SPAMFILTER_DISABLED = "sieve.spamfilter.disabled";
	public static final String IMAP_ACL_LOWERCASE="imap.acl.lowercase";


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
	public static final String SEEN_ON_OPEN = "seenonopen";
	public static final String SHARING_RIGHTS = "sharing.rights";
	public static final String FOLDER_PEFFIX = "folder.prefix";
	public static final String SCAN_ALL = "scan.all";
	public static final String SCAN_SECONDS = "scan.seconds";
	public static final String SCAN_CYCLES = "scan.cycles";
	public static final String FOLDER_SENT = "folder.sent";
	public static final String FOLDER_DRAFTS = "folder.drafts";
	public static final String FOLDER_TRASH = "folder.trash";
	public static final String FOLDER_ARCHIVE = "folder.archive";
	public static final String FOLDER_SPAM = "folder.spam";
	public static final String DEFAULT_FOLDER = "defaultfolder";
	
	/**
	 * @deprecated
	 * Remove when transition to new setting is completed
	 */
	public static final String FAVORITES = "favorites";
	public static final String FAVORITE_FOLDERS = "favorite.folders";
	
	public static final String FOLDER_DRAFTS_DELETEMSGONSEND = "folder.drafts.deletemsgonsend";
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
	public static final String FORMAT = "format";
	public static final String FONT_NAME = "font.name";
	public static final String FONT_SIZE = "font.size";
	public static final String FONT_COLOR = "font.color";
	public static final String RECEIPT = "receipt";
	public static final String AUTO_ADD_CONTACT = "autoAddContact";
	public static final String PRIORITY = "priority";
	public static final String NO_MAILCARD_ON_REPLY_FORWARD = "no.mailcard.on.reply.forward";
	public static final String VIEW_MODE = "viewmode";
	public static final String TOOLBAR_COMPACT = "toolbar.compact";
	public static final String READ_RECEIPT_CONFIRMATION = "readreceiptconfirmation";
	
	
	/**
	 * [string]
	 * Archiving operative method. One of: simple, structured, webtop.
	 * A null value indicated no method.
	 */
	public static final String ARCHIVING_MODE = "archiving.mode";
	public static final String ARCHIVING_KEEP_FOLDERS_STRUCTURE = "archiving.keep-folders-structure";
	public static final String ARCHIVING_EXTERNAL_USERFOLDER = "archiving.external.userfolder";
	public static final String ARCHIVING_DMS_METHOD = "archiving.dms-method";
	
	public static final String ARCHIVING_MODE_SINGLE = "single";
	public static final String ARCHIVING_MODE_YEAR = "year";
	public static final String ARCHIVING_MODE_MONTH = "month";
	
	public static final String ARCHIVING_DMS_METHOD_NONE = "none";
	public static final String ARCHIVING_DMS_METHOD_SIMPLE = "simple";
	public static final String ARCHIVING_DMS_METHOD_STRUCTURED = "structured";
	public static final String ARCHIVING_DMS_METHOD_WEBTOP = "webtop";
	
	/**
	 * [string]
	 * IMAP folder to be monitored by the archiving process
	 */
	public static final String ARCHIVING_SIMPLE_DMS_MAIL_FOLDER = "archiving.dms-simple.mailfolder";
	
	public static final String EXTERNAL_ACCOUNT_PROVIDERS = "external-account.providers";
	
	public static final String EXTERNAL_ACCOUNT_DEFAULT_PROVIDERS = "[\n" +
					" { 'id': 'imap', 'readOnly': 'false'}, " +
					" { 'id': 'gmail', 'email': '@gmail.com', 'server': 'imap.gmail.com', 'protocol': 'imaps', 'port': '993', 'folderPrefix':'[Gmail]/', 'folderSent': 'Sent Mail', 'folderDrafts': 'Drafts', 'folderTrash': 'Trash', 'folderSpam': 'Spam', 'folderArchive': 'Archive', 'readOnly': 'true' },\n" +
					" { 'id': 'outlook', 'email': '@outlook.com', 'server': 'imap.outlook.com', 'protocol': 'imaps', 'port': '993', 'folderPrefix':'', 'folderSent': 'Sent', 'folderDrafts': 'Drafts', 'folderTrash': 'Trash', 'folderSpam': 'Spam', 'folderArchive': 'Archive', 'readOnly': 'false' },\n" +
					" { 'id': 'icloud', 'email': '@icloud.com', 'server': 'imap.icloud.com', 'protocol': 'imaps', 'port': '993', 'folderPrefix':'', 'folderSent': 'Sent Messages', 'folderDrafts': 'Drafts', 'folderTrash': 'Trash', 'folderSpam': 'Spam', 'folderArchive': 'Archive', 'readOnly': 'false' },\n" +
					" { 'id': 'yahoo', 'email': '@yahoo.com', 'server': 'imap.yahoo.com', 'protocol': 'imaps', 'port': '993', 'folderPrefix':'', 'folderSent': 'Sent', 'folderDrafts': 'Drafts', 'folderTrash': 'Trash', 'folderSpam': 'Spam', 'folderArchive': 'Archive', 'readOnly': 'false' }\n" +
					"]";
	
	public static class ExternalProvider {
		public String id;
		public String email;
		public String server;
		public String protocol;
		public int port;
		public String folderPrefix;
		public String folderSent;
		public String folderDrafts;
		public String folderTrash;
		public String folderSpam;
		public String folderArchive;
		public String iconUrl;
		public boolean readOnly;
		
		public static class List extends ArrayList<ExternalProvider> {
			public static ExternalProvider.List fromJson(String value) {
				return JsonResult.gson.fromJson(value, ExternalProvider.List.class);
			}

			public static String toJson(ExternalProvider.List value) {
				return JsonResult.gson.toJson(value, ExternalProvider.List.class);
			}
		}
		
	}
}
