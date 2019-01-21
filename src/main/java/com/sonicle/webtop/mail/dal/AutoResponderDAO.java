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
import com.sonicle.webtop.mail.bol.OAutoResponder;
import static com.sonicle.webtop.mail.jooq.Tables.AUTORESPONDERS;
import com.sonicle.webtop.mail.jooq.tables.records.AutorespondersRecord;
import java.sql.Connection;
import java.util.List;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class AutoResponderDAO extends BaseDAO {
	private final static AutoResponderDAO INSTANCE = new AutoResponderDAO();
	public static AutoResponderDAO getInstance() {
		return INSTANCE;
	}
	
	public List<OAutoResponder> selectByDomain(Connection con, String domainId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(AUTORESPONDERS)
			.where(
				AUTORESPONDERS.DOMAIN_ID.equal(domainId)
			)
			.fetchInto(OAutoResponder.class);
	}
	
	public OAutoResponder selectByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(AUTORESPONDERS)
			.where(
				AUTORESPONDERS.DOMAIN_ID.equal(domainId)
				.and(AUTORESPONDERS.USER_ID.equal(userId))
			)
			.fetchOneInto(OAutoResponder.class);
	}
	
	public boolean existByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.selectCount()
			.from(AUTORESPONDERS)
			.where(
				AUTORESPONDERS.DOMAIN_ID.equal(domainId)
				.and(AUTORESPONDERS.USER_ID.equal(userId))
			)
			.fetchOne(0, Integer.class) == 1;
	}
	
	public int insert(Connection con, OAutoResponder item) throws DAOException {
		DSLContext dsl = getDSL(con);
		AutorespondersRecord record = dsl.newRecord(AUTORESPONDERS, item);
		return dsl
			.insertInto(AUTORESPONDERS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OAutoResponder item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(AUTORESPONDERS)
			.set(AUTORESPONDERS.ENABLED, item.getEnabled())
			.set(AUTORESPONDERS.SUBJECT, item.getSubject())
			.set(AUTORESPONDERS.MESSAGE, item.getMessage())
			.set(AUTORESPONDERS.ADDRESSES, item.getAddresses())	
			.set(AUTORESPONDERS.DAYS_INTERVAL, item.getDaysInterval())
			.set(AUTORESPONDERS.START_DATE, item.getStartDate())
			.set(AUTORESPONDERS.END_DATE, item.getEndDate())
			//.set(AUTORESPONDERS.SKIP_MAILING_LISTS, item.getSkipMailingLists())
			.where(
				AUTORESPONDERS.DOMAIN_ID.equal(item.getDomainId())
				.and(AUTORESPONDERS.USER_ID.equal(item.getUserId()))
			)
			.execute();
	}
	
	public int deleteByDomain(Connection con, String domainId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(AUTORESPONDERS)
			.where(AUTORESPONDERS.DOMAIN_ID.equal(domainId))
			.execute();
	}
	
	public int deleteByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(AUTORESPONDERS)
			.where(
				AUTORESPONDERS.DOMAIN_ID.equal(domainId)
				.and(AUTORESPONDERS.USER_ID.equal(userId))
			)
			.execute();
	}
}
