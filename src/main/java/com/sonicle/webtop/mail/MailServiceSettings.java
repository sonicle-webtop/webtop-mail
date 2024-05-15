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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonicle.mail.ImpersonateMode;
import com.sonicle.mail.StoreHostParams;
import com.sonicle.mail.StoreProtocol;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.BaseServiceSettings;
import static com.sonicle.webtop.mail.MailSettings.*;
import com.sonicle.webtop.mail.model.ViewMode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jooq.tools.StringUtils;

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
	
	public int getArchivingExternalMinAge() {
		return getInteger(ARCHIVING_EXTERNAL_MINAGE, 365*5);
	}
	
	public boolean isExternalAccountEnabled() {
		return getBoolean(EXTERNAL_ACCOUNT_ENABLED, false);
	}
	
	public String getInlineableMimeTypes() {
		return getString(INLINEABLE_MIME_TYPES,null);
	}
	
	public String getPasDangerousExtensions() {
		return getString(PAS_DANGEROUS_EXTENSIONS,"exe,bat,dll,com,cmd,bin,cab,js,jar");
	}
	
	public float getPasSpamThreshold() {
		return getFloat(PAS_SPAM_THRESHOLD,6.0f);
	}
	
	public String getPasAdditionalDomainsWhitelist() {
		return getString(PAS_ADDITIONAL_DOMAINS_WHITELIST,null);
	}
    public boolean isAutocreateSpecialFolders() {
        return getBoolean(SPECIALFOLDERS_AUTOCREATE,true);
    }
	
    public boolean isPreviewBalanceTags() {
        return getBoolean(PREVIEW_BALANCE_TAGS,true);
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
	
	public boolean isMessageEditSubject() {
		return getBoolean(MESSAGE_EDIT_SUBJECT, false);
	}
	
	public void setMessageEditSubject(boolean messageEditSubject) {
		setBoolean(MESSAGE_EDIT_SUBJECT, messageEditSubject);
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
	
	public StoreHostParams getMailboxHostParamsAsAdmin() {
		StoreHostParams shd = new StoreHostParams(getDefaultHost(), getDefaultPort(), StoreProtocol.parse(getDefaultProtocol(), false));
		shd.withUsername(getAdminUser());
		shd.withPassword(getAdminPassword());
		return shd;
	}
	
	/*
	public StoreHostParams getMailboxHostParamsAsVMail(final String domainId) {
		final String vmailAtDomain = WT.buildDomainInternetAddress(domainId, "vmail", null).getAddress();
		StoreHostParams shd = new StoreHostParams(getDefaultHost(), getDefaultPort(), StoreProtocol.parse(getDefaultProtocol(), false));
		shd.withUsername(vmailAtDomain);
		shd.withVMAILImpersonate(getNethTopVmailSecret());
		return shd;
	}
	*/
	
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
	
	public boolean getDefaultGridShowMessagePreview() {
		return getBoolean(DEFAULT_PREFIX + GRID_SHOW_MESSAGE_PREVIEW, true);
	}
	
	public boolean getDefaultGridAlwaysShowTime() {
		return getBoolean(DEFAULT_PREFIX + GRID_MESSAGE_TIME_SHOWALWAYS, false);
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
	
	public ViewMode getDefaultViewMode() {
		return getEnum(DEFAULT_PREFIX + VIEW_MODE, ViewMode.AUTO, ViewMode.class);
	}
	
	public boolean getDefaultFavoriteNotifications() {
		return getBoolean(FAVORITE_NOTIFICATIONS, true);
	}
	
	public boolean isToolbarCompact() {
		return getBoolean(TOOLBAR_COMPACT,false);
	}
	
	public List<ExternalProvider> getExternalProviders() {
		String json=getString(EXTERNAL_ACCOUNT_PROVIDERS, EXTERNAL_ACCOUNT_DEFAULT_PROVIDERS);
		ExternalProvider.List externalProviders = ExternalProvider.List.fromJson(json);
		for(ExternalProvider externalProvider: externalProviders) {
			if (StringUtils.isEmpty(externalProvider.iconUrl)) {
				externalProvider.iconUrl=WT.getServiceLafUrl(domainId, CoreManifest.ID, "default")+"/emailproviders/"+externalProvider.id+".svg";
			}
		}
		return externalProviders;
	}
	
	public boolean isAuthUserStripDomain() {
		return getBoolean(AUTH_USER_STRIP_DOMAIN,false);
	}
	
	public ACLDomainSuffixPolicy getACLDomainSuffixPolicy(final String directoryScheme) {
		ACLDomainSuffixPolicy policyOverride = getEnum(ACL_DOMAINSUFFIX_POLICY_OVERRIDE, null, ACLDomainSuffixPolicy.class);
		if (policyOverride != null) {
			return policyOverride;
		} else {
			if ("ad".equals(directoryScheme) || directoryScheme.startsWith("ldap")) {
				return ACLDomainSuffixPolicy.APPEND;
			} else {
				return ACLDomainSuffixPolicy.STRIP;
			}
		}
	}
	
	public boolean isReFwSanitizeDownlevelRevealedComments() {
		return getBoolean(RE_FW_SANITIZE_DOWNLEVEL_REVEALED_COMMENTS,false);
	}
	
	public boolean isInvitationTrashAfterAction() {
		return getBoolean(INVITATION_TRASH_AFTER_ACTION,false);
	}
	
	public boolean isIdleSharedInboxFolderEnabled() {
		return getBoolean(IDLE_SHAREDINBOX_ENABLED, true);
	}
	
	public boolean isIdleFavoriteFolderEnabled() {
		return getBoolean(IDLE_FAVORITE_ENABLED, true);
	}
	
	public int getImapEventMessageBufferTTL() {
		return getInteger(IMAP_EVENT_MESSAGE_BUFFERTTL, 500);
	}
}
