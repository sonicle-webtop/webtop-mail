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
import com.sonicle.webtop.core.CoreManifest;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.sdk.BaseServiceSettings;

/**
 *
 * @author gbulfon
 */
public class MailServiceSettings extends BaseServiceSettings {
    
	public MailServiceSettings(String domainId, String serviceId) {
        super(domainId, serviceId);
    }
	
	public static final String SMTP_HOST = "smtp.host";
	public static final String SMTP_PORT = "smtp.port";
    public static final String SPECIALFOLDERS_AUTOCREATE = "specialfolders.autocreate";
	public static final String ARCHIVE = "archive";
	public static final String FAX_PATTERN = "fax.pattern";
	public static final String ATTACHMENT_MAXSIZE = "attachment.maxsize";

	public static final String DEFAULT_FOLDER_PEFFIX = "default.folder.prefix";
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
	public static final String DEFAULT_PROTOCOL = "default.protocol";
    
    public String getSmtpHost() {
        return getSetting(SMTP_HOST);
    }
    
    public String getSmtpPort() {
        return getSetting(SMTP_PORT);
    }
    
    public boolean isAutocreateSpecialFolders() {
        return LangUtils.value(getSetting(SPECIALFOLDERS_AUTOCREATE),true);
    }
	
	public String getArchivePath() {
		return getSetting(ARCHIVE);
	}
	
	public String getFaxPattern() {
		return getSetting(FAX_PATTERN);
	}
	
	public int getAttachmentMaxSize() {
		return Integer.parseInt(getSetting(ATTACHMENT_MAXSIZE));
	}
	
	public String getAttachDir() {
		return new CoreServiceSettings(domainId, CoreManifest.ID).getTempPath();
	}
	
	public String getDefaultFolderPrefix() {
		return getSetting(DEFAULT_FOLDER_PEFFIX);
	}
	
	public boolean isDefaultScanAll() {
		return LangUtils.value(getSetting(DEFAULT_SCAN_ALL),false);
	}
	
	public int getDefaultScanSeconds() {
		return Integer.parseInt(getSetting(DEFAULT_SCAN_SECONDS));
	}
	
	public int getDefaultScanCycles() {
		return Integer.parseInt(getSetting(DEFAULT_SCAN_CYCLES));
	}
	
	public String getDefaultFolderSent() {
		return getSetting(DEFAULT_FOLDER_SENT);
	}
	
	public String getDefaultFolderDrafts() {
		return getSetting(DEFAULT_FOLDER_DRAFTS);
	}
	
	public String getDefaultFolderTrash() {
		return getSetting(DEFAULT_FOLDER_TRASH);
	}

	public String getDefaultFolderSpam() {
		return getSetting(DEFAULT_FOLDER_SPAM);
	}
	
	public boolean isDefaultIncludeMessageInReply() {
		return LangUtils.value(getSetting(DEFAULT_INCLUDE_MESSAGE_IN_REPLY), true);
	}
	
	public int getDefaultPageRows() {
		return Integer.parseInt(getSetting(DEFAULT_PAGE_ROWS));
	}
	
	public String getDefaultHost() {
		return getSetting(DEFAULT_HOST);
	}
	
	public int getDefaultPort() {
		return Integer.parseInt(getSetting(DEFAULT_PORT));
	}
	
	public String getDefaultProtocol() {
		return getSetting(DEFAULT_PROTOCOL);
	}
}
