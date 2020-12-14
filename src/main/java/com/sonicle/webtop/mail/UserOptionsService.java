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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ParameterException;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.UserProfile.PersonalInfo;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.mail.MailSettings.ExternalProvider;
import com.sonicle.webtop.mail.bol.js.JsExternalAccount;
import com.sonicle.webtop.mail.bol.js.JsGridExternalAccount;
import com.sonicle.webtop.mail.bol.js.JsUserOptions;
import com.sonicle.webtop.mail.bol.js.JsMailcard;
import com.sonicle.webtop.mail.bol.model.Identity;
import com.sonicle.webtop.mail.model.ExternalAccount;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class UserOptionsService extends BaseUserOptionsService {

	public static final Logger logger = WT.getLogger(UserOptionsService.class);

	@Override
	public void processUserOptions(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);

			MailServiceSettings mss = new MailServiceSettings(SERVICE_ID, getTargetDomainId());
			MailUserSettings mus = new MailUserSettings(getTargetProfileId(), mss);

			if (crud.equals(Crud.READ)) {
				JsUserOptions jso = new JsUserOptions(getTargetProfileId().toString());
				jso.permAccountManage = RunContext.isPermitted(true, getTargetProfileId(), SERVICE_ID, "ACCOUNT_SETTINGS", "CHANGE");
				jso.permExternalAccountManage = RunContext.isPermitted(true, getTargetProfileId(), SERVICE_ID, "EXTERNAL_ACCOUNT_SETTINGS", "CHANGE");
				jso.permMailcardManage = RunContext.isPermitted(true, getTargetProfileId(), SERVICE_ID, "MAILCARD_SETTINGS", "CHANGE");
				jso.permDomainMailcardManage = RunContext.isPermitted(true, getTargetProfileId(), SERVICE_ID, "DOMAIN_MAILCARD_SETTINGS", "CHANGE");
				
				jso.dmsSimpleMailFolder = mus.getSimpleDMSArchivingMailFolder();
				jso.dmsMethod = mus.getDMSMethod();
				jso.archiveMode = mus.getArchiveMode();
				jso.archiveKeepFoldersStructure = mus.isArchiveKeepFoldersStructure();
				jso.archiveExternalUserFolder = mus.getArchiveExternalUserFolder();
				jso.gridShowPreview = mus.getGridShowMessagePreview();
				jso.gridAlwaysShowTime = mus.getGridAlwaysShowTime();
				jso.sharedSeen = mus.isSharedSeen();
				jso.manualSeen = mus.isManualSeen();
				jso.seenOnOpen = mus.isSeenOnOpen();
				jso.scanAll = mus.isScanAll();
				//jso.scanSeconds = mus.getScanSeconds(); Deprecated
				//jso.scanCycles = mus.getScanCycles(); Deprecated
				jso.folderPrefix = mus.getFolderPrefix();
				jso.folderSent = mus.getFolderSent();
				jso.folderDrafts = mus.getFolderDrafts();
				jso.folderTrash = mus.getFolderTrash();
				jso.folderSpam = mus.getFolderSpam();
				jso.folderArchive = mus.getFolderArchive();
				jso.mainEmail = WT.getUserData(getTargetProfileId()).getEmailAddress();
				jso.replyTo = mus.getReplyTo();
				jso.sharedSort = mus.getSharedSort();
				jso.viewMode = EnumUtils.toSerializedName(mus.getViewMode());
				jso.readReceiptConfirmation = mus.getReadReceiptConfirmation();
				jso.includeMessageInReply = mus.isIncludeMessageInReply();
				jso.host = mus.getHost();
				jso.port = mus.getPort();
				jso.username = mus.getUsername();
				jso.password = mus.getPassword();
				jso.protocol = mus.getProtocol();
				jso.defaultFolder = mus.getDefaultFolder();
				jso.format = mus.getFormat();
				jso.font = mus.getFontName();
				jso.fontSize = mus.getFontSize();
				jso.fontColor = mus.getFontColor();
				jso.receipt = mus.isReceipt();
				jso.autoAddContact = mus.isAutoAddContact();
				jso.priority = mus.isPriority();
				jso.noMailcardOnReplyForward = mus.isNoMailcardOnReplyForward();
				jso.showUpcomingEvents = mus.getShowUpcomingEvents();
				jso.showUpcomingTasks = mus.getShowUpcomingTasks();
				jso.todayRowColor = mus.getTodayRowColor();
				jso.favoriteNotifications = mus.isFavoriteNotifications();
				
				// External accounts
				jso.externalAccountEnabled = mss.isExternalAccountEnabled();

				new JsonResult(jso).printTo(out);

			} else if (crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsUserOptions> pl = ServletUtils.getPayload(request, JsUserOptions.class);
				
				if (pl.map.has("dmsSimpleMailFolder")) mus.setSimpleArchivingMailFolder(pl.data.dmsSimpleMailFolder);
				if (pl.map.has("dmsMethod")) mus.setDMSMethod(pl.data.dmsMethod);
				if (pl.map.has("includeMessageInReply")) mus.setIncludeMessageInReply(pl.data.includeMessageInReply);
				
				// Main
				if (pl.map.has("replyTo")) mus.setReplyTo(pl.data.replyTo);
				if (pl.map.has("viewMode")) mus.setViewMode(pl.data.viewMode);
				if (pl.map.has("readReceiptConfirmation")) mus.setReadReceiptConfirmation(pl.data.readReceiptConfirmation);
				if (pl.map.has("gridShowPreview")) mus.setGridShowMessagePreview(pl.data.gridShowPreview);
				if (pl.map.has("gridAlwaysShowTime")) mus.setGridAlwaysShowTime(pl.data.gridAlwaysShowTime);
				
				if (pl.map.has("manualSeen")) mus.setManualSeen(pl.data.manualSeen);
				if (pl.map.has("sharedSeen")) mus.setSharedSeen(pl.data.sharedSeen);
				if (pl.map.has("seenOnOpen")) mus.setSeenOnOpen(pl.data.seenOnOpen);
				if (pl.map.has("sharedSort")) mus.setSharedSort(pl.data.sharedSort);
				if (pl.map.has("showUpcomingEvents")) mus.setShowUpcomingEvents(pl.data.showUpcomingEvents);
				if (pl.map.has("showUpcomingTasks")) mus.setShowUpcomingTasks(pl.data.showUpcomingTasks);
				if (pl.map.has("todayRowColor")) mus.setTodayRowColor(pl.data.todayRowColor);
				if (pl.map.has("favoriteNotifications")) mus.setFavoriteNotifications(pl.data.favoriteNotifications);
				// Editing
				if (pl.map.has("format")) mus.setFormat(pl.data.format);
				if (pl.map.has("font")) mus.setFontName(pl.data.font);
				if (pl.map.has("fontSize")) mus.setFontSize(pl.data.fontSize);
				if (pl.map.has("fontColor")) mus.setFontColor(pl.data.fontColor);
				if (pl.map.has("receipt")) mus.setReceipt(pl.data.receipt);
				if(pl.map.has("autoAddContact")) mus.setAutoAddContact(pl.data.autoAddContact);
				if (pl.map.has("priority")) mus.setPriority(pl.data.priority);
				if (pl.map.has("noMailcardOnReplyForward")) mus.setNoMailcardOnReplyForward(pl.data.noMailcardOnReplyForward);

				
				// Archive
				if (pl.map.has("archiveMode")) mus.setArchiveMode(pl.data.archiveMode);
				if (pl.map.has("archiveKeepFoldersStructure")) mus.setArchiveKeepFoldersStructure(pl.data.archiveKeepFoldersStructure);
				if (pl.map.has("archiveExternalUserFolder")) mus.setArchiveExternalUserFolder(pl.data.archiveExternalUserFolder);
				
				// Account
				if (RunContext.isWebTopAdmin() || RunContext.isPermitted(true, getTargetProfileId(), SERVICE_ID, "ACCOUNT_SETTINGS", "CHANGE")) {
					if (pl.map.has("protocol")) mus.setProtocol(pl.data.protocol);
					if (pl.map.has("host")) mus.setHost(pl.data.host);
					if (pl.map.has("port")) mus.setPort(pl.data.port);
					if (pl.map.has("username")) mus.setUsername(pl.data.username);
					if (pl.map.has("password")) mus.setPassword(pl.data.password);
					if (pl.map.has("folderPrefix")) mus.setFolderPrefix(pl.data.folderPrefix);
					if (pl.map.has("folderSent")) mus.setFolderSent(pl.data.folderSent);
					if (pl.map.has("folderDrafts")) mus.setFolderDrafts(pl.data.folderDrafts);
					if (pl.map.has("folderTrash")) mus.setFolderTrash(pl.data.folderTrash);
					if (pl.map.has("folderSpam")) mus.setFolderSpam(pl.data.folderSpam);
					if (pl.map.has("folderArchive")) mus.setFolderArchive(pl.data.folderArchive);
				}
				
				// Advanced
				if (pl.map.has("scanAll")) mus.setScanAll(pl.data.scanAll);
				//if (pl.map.has("scanSeconds")) mus.setScanSeconds(pl.data.scanSeconds); Deprecated
				//if (pl.map.has("scanCycles")) mus.setScanCycles(pl.data.scanCycles); Deprecated
				if (pl.map.has("defaultFolder")) mus.setDefaultFolder(pl.data.defaultFolder);

				new JsonResult().printTo(out);
			}

		} catch (Exception ex) {
			logger.error("Error executing UserOptions", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}

	public void processListIdentities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String type = ServletUtils.getStringParameter(request, "type", true);
			boolean any = type.equals("any");
			MailManager mman = (MailManager) WT.getServiceManager(SERVICE_ID, true, getTargetProfileId()); // new MailManager(getTargetProfileId());
			List<Identity> idents = mman.listIdentities();
			List<Identity> jsidents = new ArrayList<>();
			for (Identity ident : idents) {
				if (!ident.isMainIdentity()) {
					if (any || ident.getType().equals(type)) {
						jsidents.add(ident);
					}
				}
			}
			new JsonResult("identities", jsidents).printTo(out);

		} catch (Exception ex) {
			logger.error("Error listing identities", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}

	public void processManageIdentities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String crud = null;
		try {
			MailManager mman = (MailManager) WT.getServiceManager(SERVICE_ID, true, getTargetProfileId());
			crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				processListIdentities(request, response, out);
			} else if (crud.equals(Crud.CREATE)) {
				Payload<MapItem, Identity> pl = ServletUtils.getPayload(request, Identity.class);
				Identity ident=pl.data;
				Identity newident=mman.addIdentity(ident);
				List<Identity> jsidents = new ArrayList<>();
				jsidents.add(newident);
				new JsonResult("identities", jsidents).printTo(out);
			} else if (crud.equals(Crud.DELETE)) {
				Payload<MapItem, Identity> pl = ServletUtils.getPayload(request, Identity.class);
				mman.deleteIdentity(pl.data);
				new JsonResult().printTo(out);
			} else if (crud.equals(Crud.UPDATE)) {
				Payload<MapItem, Identity> pl = ServletUtils.getPayload(request, Identity.class);
				Identity uident=mman.updateIdentity(pl.data.getIdentityId(), pl.data);
				List<Identity> jsidents = new ArrayList<>();
				jsidents.add(uident);
				new JsonResult("identities", jsidents).printTo(out);
			}
		} catch (Exception ex) {
			logger.error("Error managing quickparts", ex);
			new JsonResult(false, "Error managing quickparts").printTo(out);
		}
	}
	
	public void processManageExternalAccountsGrid(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailManager mailMgr = (MailManager) WT.getServiceManager(SERVICE_ID, true, getTargetProfileId());
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				List<JsGridExternalAccount> items = new ArrayList<>();
				List<ExternalAccount> externalAccounts = mailMgr.listExternalAccounts();
				externalAccounts.forEach(account -> {
					String iconUrl = getIconUrlFromExternalProvider(account.getProviderId());
					items.add(new JsGridExternalAccount(account, iconUrl));
				});
				
				new JsonResult("externalAccount", items).printTo(out);
			}
		} catch (Throwable t) {
			logger.error("Error in ManageExternalAccountsGrid", t);
			new JsonResult(t).printTo(out);
		} 
	}
	
	public void processManageExternalAccounts(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			MailServiceSettings mss = new MailServiceSettings(SERVICE_ID, getTargetDomainId());
			MailManager mailMgr = (MailManager) WT.getServiceManager(SERVICE_ID, true, getTargetProfileId());
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				int accountId = Integer.parseInt(id);
				
				ExternalAccount externalAccount = mailMgr.getExternalAccount(accountId);
				JsExternalAccount item = new JsExternalAccount(externalAccount);
				item.iconUrl = getIconUrlFromExternalProvider(item.providerId);
				item.readOnlyProvider = getReadOnlyPropertyFromExternalProvider(item.providerId);
				new JsonResult(item).printTo(out);
				
			} else if (crud.equals(Crud.CREATE)) {
				if (!mss.isExternalAccountEnabled()) throw new WTException("External accounts not enabled for this domain");
				Payload<MapItem, JsExternalAccount> pl = ServletUtils.getPayload(request, JsExternalAccount.class);
				ExternalAccount account = JsExternalAccount.createExternalAccount(pl.data);
				mailMgr.addExternalAccount(account);
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				String id = ServletUtils.getStringParameter(request, "externalAccountId", true);
				int accountId = Integer.parseInt(id);
				
				mailMgr.removeExternalAccount(accountId);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				if (!mss.isExternalAccountEnabled()) throw new WTException("External accounts not enabled for this domain");
				Payload<MapItem, JsExternalAccount> pl = ServletUtils.getPayload(request, JsExternalAccount.class);
				ExternalAccount account = JsExternalAccount.createExternalAccount(pl.data);
				mailMgr.updateExternalAccount(account);
				new JsonResult().printTo(out);
			}
			
		} catch (Throwable t) {
			logger.error("Error in ManageExternalAccounts", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processExternalAccountProviders(HttpServletRequest request, HttpServletResponse respomse , PrintWriter out) {
		MailServiceSettings mss = new MailServiceSettings(SERVICE_ID, getTargetDomainId());
		MailUserSettings mus = new MailUserSettings(getTargetProfileId(), mss);
		
		List<ExternalProvider> list = mss.getExternalProviders();
		
		list.forEach(item -> {
			if(item.id.equals("internet")) {
				item.protocol = "imaps";
				item.port = 993;
				item.folderArchive = mus.getFolderArchive();
				item.folderSent = mus.getFolderSent();
				item.folderDrafts = mus.getFolderDrafts();
				item.folderSpam = mus.getFolderSpam();
				item.folderTrash = mus.getFolderTrash();
			}
		});
		new JsonResult("data", list).printTo(out);
	}
	
	private String getIconUrlFromExternalProvider(String providerId) {
		MailServiceSettings mss = new MailServiceSettings(SERVICE_ID, getTargetDomainId());
		MailUserSettings mus = new MailUserSettings(getTargetProfileId(), mss);
		
		List<ExternalProvider> list = mss.getExternalProviders();
		
		return list.stream().filter(provider -> provider.id.equals(providerId)).findAny().orElse(new ExternalProvider()).iconUrl;
	}
	
	private boolean getReadOnlyPropertyFromExternalProvider(String providerId) {
		MailServiceSettings mss = new MailServiceSettings(SERVICE_ID, getTargetDomainId());
		MailUserSettings mus = new MailUserSettings(getTargetProfileId(), mss);
		
		List<ExternalProvider> list = mss.getExternalProviders();
		
		return list.stream().filter(provider -> provider.id.equals(providerId)).findAny().orElse(new ExternalProvider()).readOnly;
	}
	
	public void processManageMailcard(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {

		try {
			UserProfileId profileId=getTargetProfileId();
			String domainId=profileId.getDomainId();
			String userId=profileId.getUserId();
			String emailAddress=WT.getUserData(profileId).getEmailAddress();
			MailManager mman = (MailManager) WT.getServiceManager(SERVICE_ID, true, profileId);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			String mailcardId = ServletUtils.getStringParameter(request, "mailcardId", true);
			PersonalInfo ppi=null;
			Mailcard mc=null;

			if (crud.equals(Crud.READ)) {
				if (mailcardId.equals("emaildomain")) {
					mc = mman.getEmailDomainMailcard(domainId,emailAddress);
				} else if (StringUtils.startsWith(mailcardId, "identity")) {
					String[] tokens = StringUtils.split(mailcardId, '|');
					int identId = Integer.parseInt(tokens[1]);
					if (identId == 0) {
						mc = mman.getMailcard(profileId);
					} else {
						Identity ide = mman.findIdentity(identId);
						mc = mman.getMailcard(ide);
					}
				}
				new JsonResult(new JsMailcard(mailcardId, mc)).printTo(out);

			} else if (crud.equals(Crud.UPDATE)) {
				String html = ServletUtils.getStringParameter(request, "html", true);
				if (mailcardId.equals("emaildomain")) {
					mman.setEmailDomainMailcard(emailAddress,html);
					mc = mman.getEmailDomainMailcard(domainId,emailAddress);

				} else if (StringUtils.startsWith(mailcardId, "identity")) {
					String[] tokens = StringUtils.split(mailcardId, '|');
					int identityId = Integer.parseInt(tokens[1]);
					if (identityId == 0) {
						String target = ServletUtils.getStringParameter(request, "target", true);
						if (target.equals(Mailcard.TYPE_EMAIL)) {
							mman.setEmailMailcard(emailAddress, html);
							mman.setUserMailcard(null);
						} else {
							mman.setUserMailcard(html);
							mman.setEmailMailcard(emailAddress, null);
						}
						mc = mman.getMailcard(profileId);
						ppi = WT.getUserPersonalInfo(profileId);
					} else {
						Identity ide = mman.findIdentity(identityId);
						mman.setIdentityMailcard(ide, html);
						mc = mman.getMailcard(ide);
						ppi = getPersonalInfo(ide);
					}
					mc.substitutePlaceholders(ppi);
				}
				new JsonResult(new JsMailcard(mailcardId, mc)).printTo(out);

			} else if (crud.equals(Crud.DELETE)) {
				if (mailcardId.equals("emaildomain")) {
					mman.setEmailDomainMailcard(emailAddress, null);
					mc = mman.getEmailDomainMailcard(domainId, emailAddress);

				} else if (StringUtils.startsWith(mailcardId, "identity")) {
					String[] tokens = StringUtils.split(mailcardId, '|');
					int identId = Integer.parseInt(tokens[1]);
					if (identId == 0) {
						mman.setEmailMailcard(emailAddress, null);
						mman.setUserMailcard(null);
						mc = mman.getMailcard(profileId);
						ppi = WT.getUserPersonalInfo(profileId);
					} else {
						Identity ide = mman.findIdentity(identId);
						mman.setIdentityMailcard(ide, null);
						mc = mman.getMailcard(ide);
						ppi = getPersonalInfo(ide);
					}
					mc.substitutePlaceholders(ppi);
				}
				new JsonResult(new JsMailcard(mailcardId, mc)).printTo(out);
			}
		} catch (Exception ex) {
			logger.error("Error managing mailcard.", ex);
			new JsonResult(false, "Unable to manage mailcard.").printTo(out);
		}
	}

	private PersonalInfo getPersonalInfo(Identity identity) {
		UserProfileId pid=identity.getOriginPid();
		if (pid!=null) {
			return WT.getUserPersonalInfo(pid);
		}
		return null;
	}

}
