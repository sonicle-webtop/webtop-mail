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
import com.sonicle.webtop.mail.bol.OInFilter;
import static com.sonicle.webtop.mail.jooq.Sequences.SEQ_IN_FILTERS;
import static com.sonicle.webtop.mail.jooq.Tables.IN_FILTERS;
import com.sonicle.webtop.mail.jooq.tables.records.InFiltersRecord;
import java.sql.Connection;
import java.util.List;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class InFilterDAO extends BaseDAO {
	private final static InFilterDAO INSTANCE = new InFilterDAO();
	public static InFilterDAO getInstance() {
		return INSTANCE;
	}

	public Long getSequence(Connection con) throws DAOException {
		DSLContext dsl = getDSL(con);
		Long nextID = dsl.nextval(SEQ_IN_FILTERS);
		return nextID;
	}
	
	public List<OInFilter> selectByDomain(Connection con, String domainId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(IN_FILTERS)
			.where(
				IN_FILTERS.DOMAIN_ID.equal(domainId)
			)
			.fetchInto(OInFilter.class);
	}
	
	public List<OInFilter> selectByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(IN_FILTERS)
			.where(
				IN_FILTERS.DOMAIN_ID.equal(domainId)
				.and(IN_FILTERS.USER_ID.equal(userId))
			)
			.orderBy(
				IN_FILTERS.ORDER.asc(),
				IN_FILTERS.NAME.asc()
			)
			.fetchInto(OInFilter.class);
	}
	
	public int insert(Connection con, OInFilter item) throws DAOException {
		DSLContext dsl = getDSL(con);
		InFiltersRecord record = dsl.newRecord(IN_FILTERS, item);
		return dsl
			.insertInto(IN_FILTERS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OInFilter item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(IN_FILTERS)
			.set(IN_FILTERS.ENABLED, item.getEnabled())
			.set(IN_FILTERS.ORDER, item.getOrder())
			.set(IN_FILTERS.NAME, item.getName())
			.set(IN_FILTERS.SIEVE_MATCH, item.getSieveMatch())
			.set(IN_FILTERS.SIEVE_RULES, item.getSieveRules())
			.set(IN_FILTERS.SIEVE_ACTIONS, item.getSieveActions())
			.where(
				IN_FILTERS.IN_FILTER_ID.equal(item.getInFilterId())
			)
			.execute();
	}
	
	public int delete(Connection con, int filterId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(IN_FILTERS)
			.where(IN_FILTERS.IN_FILTER_ID.equal(filterId))
			.execute();
	}
}
