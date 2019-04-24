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
package com.sonicle.webtop.mail.dal;

import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.mail.bol.OExternalAccount;
import com.sonicle.webtop.mail.jooq.Sequences;
import static com.sonicle.webtop.mail.jooq.Tables.EXTERNAL_ACCOUNTS;
import com.sonicle.webtop.mail.jooq.tables.records.ExternalAccountsRecord;
import java.sql.Connection;
import java.util.List;
import org.jooq.DSLContext;

/**
 *
 * @author Inis
 */
public class ExternalAccountDAO extends BaseDAO {
	
	private final static ExternalAccountDAO INSTANCE = new ExternalAccountDAO();
	
	public static ExternalAccountDAO getInstance() {
		return INSTANCE;
	}
	
	public Long getSequence(Connection con) throws DAOException {
		DSLContext dsl = getDSL(con);
		Long nextID = dsl.nextval(Sequences.EXTERNAL_ACCOUNTS_EXTERNAL_ACCOUNT_ID_SEQ);
		return nextID;
	}
	
	public List<OExternalAccount> selectByDomainUser(Connection conn, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(conn);
		return dsl
			.select()
			.from(EXTERNAL_ACCOUNTS)
			.where(
				EXTERNAL_ACCOUNTS.DOMAIN_ID.equal(domainId)
				.and(EXTERNAL_ACCOUNTS.USER_ID.equal(userId))
			)
			.fetchInto(OExternalAccount.class);
	}
	
	public OExternalAccount selectById(Connection conn, int externalAccountId) throws DAOException {
		DSLContext dsl = getDSL(conn);
		return dsl
			.select()
			.from(EXTERNAL_ACCOUNTS)
			.where(EXTERNAL_ACCOUNTS.EXTERNAL_ACCOUNT_ID.equal(externalAccountId))
			.fetchOneInto(OExternalAccount.class);
	}
	
	public int deleteById(Connection conn, int externalAccountId) throws DAOException {
		DSLContext dsl = getDSL(conn);
		return dsl
			.delete(EXTERNAL_ACCOUNTS)
			.where(EXTERNAL_ACCOUNTS.EXTERNAL_ACCOUNT_ID.equal(externalAccountId))
			.execute();
	}
	
	public int insert(Connection conn, OExternalAccount externalAccount) throws DAOException {
		DSLContext dsl = getDSL(conn);
		ExternalAccountsRecord record = dsl.newRecord(EXTERNAL_ACCOUNTS, externalAccount);
		return dsl
			.insertInto(EXTERNAL_ACCOUNTS)
			.set(record)
			.execute();
	}
	
	public int update(Connection conn, OExternalAccount externalAccount) throws DAOException {
		DSLContext dsl = getDSL(conn);
		return dsl
			.update(EXTERNAL_ACCOUNTS)
			.set(EXTERNAL_ACCOUNTS.DISPLAY_NAME, externalAccount.getDisplayName())
			.set(EXTERNAL_ACCOUNTS.DESCRIPTION, externalAccount.getDescription())
			.set(EXTERNAL_ACCOUNTS.EMAIL, externalAccount.getEmail())
			.set(EXTERNAL_ACCOUNTS.PROTOCOL, externalAccount.getProtocol())
			.set(EXTERNAL_ACCOUNTS.HOST, externalAccount.getHost())
			.set(EXTERNAL_ACCOUNTS.PORT, externalAccount.getPort())
			.set(EXTERNAL_ACCOUNTS.READ_ONLY, externalAccount.getReadOnly())
			.set(EXTERNAL_ACCOUNTS.USERNAME, externalAccount.getUsername())
			.set(EXTERNAL_ACCOUNTS.PASSWORD, externalAccount.getPassword())
			.set(EXTERNAL_ACCOUNTS.FOLDER_PREFIX, externalAccount.getFolderPrefix())
			.set(EXTERNAL_ACCOUNTS.FOLDER_SENT, externalAccount.getFolderSent())
			.set(EXTERNAL_ACCOUNTS.FOLDER_DRAFTS, externalAccount.getFolderDrafts())
			.set(EXTERNAL_ACCOUNTS.FOLDER_TRASH, externalAccount.getFolderTrash())
			.set(EXTERNAL_ACCOUNTS.FOLDER_SPAM, externalAccount.getFolderSpam())
			.set(EXTERNAL_ACCOUNTS.FOLDER_ARCHIVE, externalAccount.getFolderArchive())
			.where(
				EXTERNAL_ACCOUNTS.EXTERNAL_ACCOUNT_ID.equal(externalAccount.getExternalAccountId())
			)
			.execute();
	}
}
