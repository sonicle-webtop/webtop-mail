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

import com.sonicle.webtop.mail.model.ExternalAccount;

/**
 *
 * @author Inis
 */
public class JsExternalAccount {
	
	public Integer externalAccountId;
	public String displayName;
	public String accountDescription;
	public String email;
	public String protocol;
	public String host;
	public Integer port;
	public String userName;
	public String password;
	public String folderPrefix;
	public String folderSent;
	public String folderDrafts;
	public String folderTrash;
	public String folderSpam;
	public String folderArchive;
	public boolean readonlyProvider;
	
	public JsExternalAccount(ExternalAccount externalAccount) {
		externalAccountId = externalAccount.getExternalAccountId();
		displayName = externalAccount.getDisplayName();
		accountDescription = externalAccount.getAccountDescription();
		email = externalAccount.getEmail();
		protocol = externalAccount.getProtocol();
		host = externalAccount.getHost();
		port = externalAccount.getPort();
		userName = externalAccount.getUserName();
		password = externalAccount.getPassword();
		folderPrefix = externalAccount.getFolderPrefix();
		folderSent = externalAccount.getFolderSent();
		folderDrafts = externalAccount.getFolderDrafts();
		folderTrash = externalAccount.getFolderTrash();
		folderSpam = externalAccount.getFolderSpam();
		folderArchive = externalAccount.getFolderArchive();
		readonlyProvider = externalAccount.isReadonlyProvider();
	}
	
	public static ExternalAccount createExternalAccount(JsExternalAccount jsExternalAccount) {
		ExternalAccount account = new ExternalAccount();
		account.setExternalAccountId(jsExternalAccount.externalAccountId);
		account.setDisplayName(jsExternalAccount.displayName);
		account.setAccountDescription(jsExternalAccount.accountDescription);
		account.setEmail(jsExternalAccount.email);
		account.setProtocol(jsExternalAccount.protocol);
		account.setHost(jsExternalAccount.host);
		account.setPort(jsExternalAccount.port);
		account.setUserName(jsExternalAccount.userName);
		account.setPassword(jsExternalAccount.password);
		account.setFolderPrefix(jsExternalAccount.folderPrefix);
		account.setFolderSent(jsExternalAccount.folderSent);
		account.setFolderDrafts(jsExternalAccount.folderDrafts);
		account.setFolderTrash(jsExternalAccount.folderTrash);
		account.setFolderSpam(jsExternalAccount.folderSpam);
		account.setFolderArchive(jsExternalAccount.folderArchive);
		account.setReadonlyProvider(jsExternalAccount.readonlyProvider);
		
		return account;
	}
	
}
