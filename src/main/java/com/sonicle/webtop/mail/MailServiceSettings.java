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
	
	public boolean isArchivingExternal() {
        return getBoolean(ARCHIVING_EXTERNAL,false);
	}
	
	public String getArchivingExternalType() {
		return getString(ARCHIVING_EXTERNAL_TYPE,"imapsync");
	}
	
	public String getArchivingExternalHost() {
		return getString(ARCHIVING_EXTERNAL_HOST,"localhost");
	}
	
	public int getArchivingExternalPort() {
		return getInteger(ARCHIVING_EXTERNAL_PORT,143);
	}
	
	public String getArchivingExternalProtocol() {
		return getString(ARCHIVING_EXTERNAL_PROTOCOL,"imap");
	}
	
	public String getArchivingExternalUsername() {
		return getString(ARCHIVING_EXTERNAL_USERNAME,"domain-archive");
	}
	
	public String getArchivingExternalPassword() {
		return getString(ARCHIVING_EXTERNAL_PASSWORD,"secret");
	}
	
	public String getArchivingExternalFolderPrefix() {
		return getString(ARCHIVING_EXTERNAL_FOLDER_PREFIX,null);
	}
	
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
	
	public boolean getMessageReplyAllStripMyIdentities() {
		return getBoolean(MESSAGE_REPLYALL_STRIPMYIDENTITIES, true);
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
	
	public boolean isPublicResourceLinksAsInlineAttachments() {
		return getBoolean(PUBLIC_RESOURCE_LINKS_AS_INLINE_ATTACHMENTS, false);
	}
	
	//DEFAULTS
	
	public String getDefaultFolderPrefix() {
		return getString(DEFAULT_PREFIX+MailSettings.FOLDER_PEFFIX,null);
	}
	
	public boolean isDefaultScanAll() {
		return getBoolean(DEFAULT_PREFIX+MailSettings.SCAN_ALL,false);
	}
	
	public int getDefaultScanSeconds() {
		return getInteger(DEFAULT_PREFIX+MailSettings.SCAN_SECONDS,30);
	}
	
	public int getDefaultScanCycles() {
		return getInteger(DEFAULT_PREFIX+MailSettings.SCAN_CYCLES,10);
	}
	
	public String getDefaultFolderSent() {
		return getString(DEFAULT_PREFIX+MailSettings.FOLDER_SENT,null);
	}
	
	public String getDefaultFolderDrafts() {
		return getString(DEFAULT_PREFIX+MailSettings.FOLDER_DRAFTS,null);
	}
	
	public String getDefaultFolderTrash() {
		return getString(DEFAULT_PREFIX+MailSettings.FOLDER_TRASH,null);
	}

	public String getDefaultFolderArchive() {
		return getString(DEFAULT_PREFIX+MailSettings.FOLDER_ARCHIVE,null);
	}

	public String getDefaultFolderSpam() {
		return getString(DEFAULT_PREFIX+MailSettings.FOLDER_SPAM,null);
	}
	
	public boolean isDefaultFolderDraftsDeleteMsgOnSend() {
		return getBoolean(DEFAULT_PREFIX+MailSettings.FOLDER_DRAFTS_DELETEMSGONSEND,false);
	}
	
	public boolean isDefaultIncludeMessageInReply() {
		return getBoolean(DEFAULT_PREFIX+MailSettings.INCLUDE_MESSAGE_IN_REPLY,true);
	}
	
	public boolean isDefaultNoMailcardOnReplyForward() {
		return getBoolean(DEFAULT_PREFIX+MailSettings.NO_MAILCARD_ON_REPLY_FORWARD,false);
	}
	
	public int getDefaultPageRows() {
		return getInteger(DEFAULT_PREFIX+MailSettings.PAGE_ROWS,50);
	}
	
	public String getDefaultHost() {
		return getString(DEFAULT_PREFIX+MailSettings.HOST,"localhost");
	}
	
	public int getDefaultPort() {
		return getInteger(DEFAULT_PREFIX+MailSettings.PORT,25);
	}
	
	public String getDefaultProtocol() {
		return getString(DEFAULT_PREFIX+MailSettings.PROTOCOL,"imap");
	}
	
	public String getDefaultFormat() {
		return getString(DEFAULT_PREFIX+MailSettings.FORMAT,"html");
	}
	
	public String getDefaultFontName() {
		return getString(DEFAULT_PREFIX+MailSettings.FONT_NAME,"Arial");
	}
	
	public int getDefaultFontSize() {
		return getInteger(DEFAULT_PREFIX+MailSettings.FONT_SIZE,12);
	}
	
	public boolean isDefaultReceipt() {
		return getBoolean(DEFAULT_PREFIX+MailSettings.RECEIPT,false);
	}
	
	public int getDefaultSievePort() {
		return getInteger(DEFAULT_PREFIX + SIEVE_PORT, 2000);
	}
	
	public boolean getDefaultShowMessagePreviewOnRow() {
		return getBoolean(DEFAULT_PREFIX + SHOW_MESSAGE_PREVIEW_ON_ROW, true);
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
	
	public String getDefaultTodayRowColor() {
		return getString(DEFAULT_PREFIX + MailSettings.GRID_TODAY_ROW_COLOR, "#F8F8C8");
	}
}
