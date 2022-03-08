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
import com.sonicle.webtop.mail.bol.ONote;
import java.sql.Connection;
import org.jooq.DSLContext;
import static com.sonicle.webtop.mail.jooq.Tables.*;
import com.sonicle.webtop.mail.jooq.tables.records.NotesRecord;
import java.util.List;

/**
 *
 * @author gbulfon
 */
public class NoteDAO extends BaseDAO {
	
	private final static NoteDAO INSTANCE = new NoteDAO();
	public static NoteDAO getInstance() {
		return INSTANCE;
	}
	
	public ONote selectById(Connection con, String domainId, String messageId) throws DAOException {
		DSLContext dsl = getDSL(con);
		if (messageId.length()>255)
			messageId=messageId.substring(0,255);
		return dsl
			.select()
			.from(NOTES)
			.where(
				NOTES.DOMAIN_ID.equal(domainId)
				.and(NOTES.MESSAGE_ID.equal(messageId))
			)
			.fetchOneInto(ONote.class);
	}
	
	public int insert(Connection con, ONote item) throws DAOException {
		DSLContext dsl = getDSL(con);
		if (item.getMessageId().length()>255)
			item.setMessageId(item.getMessageId().substring(0,255));
		NotesRecord record = dsl.newRecord(NOTES, item);
		return dsl
			.insertInto(NOTES)
			.set(record)
			.execute();
	}
	
	public int deleteById(Connection con, String domainId, String messageId) throws DAOException {
		DSLContext dsl = getDSL(con);
		if (messageId.length()>255)
			messageId=messageId.substring(0,255);
		return dsl
			.delete(NOTES)
			.where(NOTES.DOMAIN_ID.equal(domainId)
					.and(NOTES.MESSAGE_ID.equal(messageId))
			)
			.execute();
	}
	
	public List<ONote> selectByLike(Connection con, String domainId, String pattern) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(NOTES)
			.where(
				NOTES.TEXT.likeIgnoreCase(pattern)
			)
			.fetchInto(ONote.class);
	}
	
	
}
