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

import com.sonicle.webtop.core.sdk.BaseServiceSettings;
import static com.sonicle.webtop.mail.MailSettings.*;

/**
 *
 * @author gbulfon
 */
public class MailServiceSettings extends BaseServiceSettings {
    
	public MailServiceSettings(String serviceId, String domainId) {
        super(serviceId, domainId);
    }
	
    public static final String SPECIALFOLDERS_AUTOCREATE = "specialfolders.autocreate";
	public static final String DMS_ARCHIVE = "archive";
	public static final String ATTACHMENT_MAXSIZE = "attachment.maxsize";
	public static final String ATTACHMENT_DIR = "attachment.dir";
	public static final String MESSAGE_VIEW_MAX_TOS = "message.view.max.tos";
	public static final String MESSAGE_VIEW_MAX_CCS = "message.view.max.ccs";
	public static final String SORT_FOLDERS = "sort.folders";
	public static final String SPAMADM_SPAM = "spamadm.spam";
	public static final String ADMIN_USER = "admin.user";
	public static final String ADMIN_PASSWORD = "admin.password";
	public static final String NETHTOP_VMAIL_SECRET = "nethtop.vmail.secret";
	public static final String SCHEDULED_EMAILS_DISABLED = "scheduled-emails.disabled";
	public static final String SIEVE_SPAMFILTER_DISABLED = "sieve.spamfilter.disabled";
	public static final String IMAP_ACL_LOWERCASE="imap.acl.lowercase";

/*	public static final String DEFAULT_FOLDER_PEFFIX = "default.folder.prefix";
	public static final String DEFAULT_SCAN_ALL = "default.scan.all";
	public static final String DEFAULT_SCAN_SECONDS = "default.scan.seconds";
	public static final String DEFAULT_SCAN_CYCLES = "default.scan.cycles";
	public static final String DEFAULT_FOLDER_SENT = "default.folder.sent";
	public static final String DEFAULT_FOLDER_DRAFTS = "default.folder.drafts";
	public static final String DEFAULT_FOLDER_TRASH = "default.folder.trash";
	public static final String DEFAULT_FOLDER_SPAM = "default.folder.spam";
	public static final String DEFAULT_INCLUDE_MESSAGE_IN_REPLY = "default.include.message.in.reply";
	public static final String DEFAULT_PAGE_ROWS = "default.page.rows";
	public static final String DEFAULT_HOST = "default.host";
	public static final String DEFAULT_PORT = "default.port";
	public static final String DEFAULT_PROTOCOL = "default.protocol";*/
	
    public boolean isAutocreateSpecialFolders() {
        return getBoolean(SPECIALFOLDERS_AUTOCREATE,true);
    }
	
	public String getDmsArchivePath() {
		return getString(DMS_ARCHIVE,null);
	}
	
	public long getAttachmentMaxFileSize(boolean fallbackOnDefault) {
		final Long value = getLong(ATTACHMENT_MAXFILESIZE, null);
		if (fallbackOnDefault && (value == null)) {
			return getDefaultAttachmentMaxFileSize();
		} else {
			return value;
		}
	}
	
/*	public String getAttachDir() {
		String adir=getString(ATTACHMENT_DIR,null);
		if (adir==null) adir=new CoreServiceSettings(CoreManifest.ID, domainId).getTempPath();
		return adir;
	}*/
	
	public int getMessageViewMaxTos() {
		return getInteger(MESSAGE_VIEW_MAX_TOS,20);
	}
	
	public void setMessageViewMaxTos(int maxtos) {
		setInteger(MESSAGE_VIEW_MAX_TOS,maxtos);
	}
	
	public int getMessageViewMaxCcs() {
		return getInteger(MESSAGE_VIEW_MAX_CCS,20);
	}
	
	public void setMessageViewMaxCcs(int maxccs) {
		setInteger(MESSAGE_VIEW_MAX_CCS,maxccs);
	}
	
	public boolean isSortFolder() {
		return getBoolean(SORT_FOLDERS,false);
	}
	
	public String getSpamadmSpam() {
		return getString(SPAMADM_SPAM,null);
	}
	
	public String getAdminUser() {
		return getString(ADMIN_USER,null);
	}
	
	public String getAdminPassword() {
		return getString(ADMIN_PASSWORD,null);
	}
	
	public String getNethTopVmailSecret() {
		return getString(NETHTOP_VMAIL_SECRET,null);
	}
	
	public boolean isScheduledEmailsDisabled() {
		return getBoolean(SCHEDULED_EMAILS_DISABLED,false);
	}

	public boolean isSieveSpamFilterDisabled() {
		return getBoolean(SIEVE_SPAMFILTER_DISABLED,false);
	}
	
	public int getSievePort() {
		Integer value = getInteger(MailSettings.SIEVE_PORT, null);
		return (value != null) ? value : getDefaultSievePort();
	}
	public boolean isImapAclLowercase() {
		return getBoolean(IMAP_ACL_LOWERCASE,false);
	}
	
	//DEFAULTS
	
	public String getDefaultFolderPrefix() {
		return getString(DEFAULT_PREFIX+MailUserSettings.FOLDER_PEFFIX,null);
	}
	
	public boolean isDefaultScanAll() {
		return getBoolean(DEFAULT_PREFIX+MailUserSettings.SCAN_ALL,false);
	}
	
	public int getDefaultScanSeconds() {
		return getInteger(DEFAULT_PREFIX+MailUserSettings.SCAN_SECONDS,30);
	}
	
	public int getDefaultScanCycles() {
		return getInteger(DEFAULT_PREFIX+MailUserSettings.SCAN_CYCLES,10);
	}
	
	public String getDefaultFolderSent() {
		return getString(DEFAULT_PREFIX+MailUserSettings.FOLDER_SENT,null);
	}
	
	public String getDefaultFolderDrafts() {
		return getString(DEFAULT_PREFIX+MailUserSettings.FOLDER_DRAFTS,null);
	}
	
	public String getDefaultFolderTrash() {
		return getString(DEFAULT_PREFIX+MailUserSettings.FOLDER_TRASH,null);
	}

	public String getDefaultFolderArchive() {
		return getString(DEFAULT_PREFIX+MailUserSettings.FOLDER_ARCHIVE,null);
	}

	public String getDefaultFolderSpam() {
		return getString(DEFAULT_PREFIX+MailUserSettings.FOLDER_SPAM,null);
	}
	
	public boolean isDefaultFolderDraftsDeleteMsgOnSend() {
		return getBoolean(DEFAULT_PREFIX+MailUserSettings.FOLDER_DRAFTS_DELETEMSGONSEND,false);
	}
	
	public boolean isDefaultIncludeMessageInReply() {
		return getBoolean(DEFAULT_PREFIX+MailUserSettings.INCLUDE_MESSAGE_IN_REPLY,true);
	}
	
	public int getDefaultPageRows() {
		return getInteger(DEFAULT_PREFIX+MailUserSettings.PAGE_ROWS,50);
	}
	
	public String getDefaultHost() {
		return getString(DEFAULT_PREFIX+MailUserSettings.HOST,"localhost");
	}
	
	public int getDefaultPort() {
		return getInteger(DEFAULT_PREFIX+MailUserSettings.PORT,25);
	}
	
	public String getDefaultProtocol() {
		return getString(DEFAULT_PREFIX+MailUserSettings.PROTOCOL,"imap");
	}
	
	public String getDefaultFormat() {
		return getString(DEFAULT_PREFIX+MailUserSettings.FORMAT,"html");
	}
	
	public String getDefaultFontName() {
		return getString(DEFAULT_PREFIX+MailUserSettings.FONT_NAME,"Arial");
	}
	
	public int getDefaultFontSize() {
		return getInteger(DEFAULT_PREFIX+MailUserSettings.FONT_SIZE,12);
	}
	
	public boolean isDefaultReceipt() {
		return getBoolean(DEFAULT_PREFIX+MailUserSettings.RECEIPT,false);
	}
	
	public int getDefaultSievePort() {
		return getInteger(DEFAULT_PREFIX + SIEVE_PORT, 2000);
	}
	
	public boolean getDefaultShowUpcomingEvents() {
		return getBoolean(DEFAULT_PREFIX + SHOW_UPCOMING_EVENTS, false);
	}
	
	public boolean getDefaultShowUpcomingTasks() {
		return getBoolean(DEFAULT_PREFIX + SHOW_UPCOMING_TASKS, false);
	}
	
	public long getDefaultAttachmentMaxFileSize() {
		return getLong(DEFAULT_PREFIX + ATTACHMENT_MAXFILESIZE, (long)10485760); // 10MB
	}
}
