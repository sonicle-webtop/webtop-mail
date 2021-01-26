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

import java.util.ArrayList;

/**
 *
 * @author Federico Ballarini
 */
public class JsListedMessage {
	public Long idmessage;
	public Integer priority;
	public String status;
	public String to;
	public String toemail;
	public String from;
	public String fromemail;
	public String subject;
	public String msgtext;
	public Long threadId;
	public Integer threadIndent;
	public String date;
	public String gdate;
	public String sdate;
	public String xdate;
	public Boolean unread;
	public Integer size;
	public ArrayList<String> tags;
	public String pecstatus;
	public String flag;
	public Boolean note;
	public Boolean arch;
	public Boolean istoday;
	public Boolean atts;
	public String scheddate;
	public Boolean threadOpen;
	public Boolean threadHasChildren;
	public Integer threadUnseenChildren;
	public Boolean fmtd;
	public String fromfolder;

	public JsListedMessage(Long idmessage, Integer priority, String status, String to, String toemail, String from, String fromemail, String subject, String msgtext, Long threadId, Integer threadIndent, String date, String gdate, String sdate, String xdate, Boolean unread, Integer size, ArrayList<String> tags, String pecstatus, String flag, Boolean note, Boolean arch, Boolean istoday, Boolean atts, String scheddate, Boolean threadOpen, Boolean threadHasChildren, Integer threadUnseenChildren, Boolean fmtd, String fromfolder) {
		this.idmessage = idmessage;
		this.priority = priority;
		this.status = status;
		this.to = to;
		this.toemail = toemail;
		this.from = from;
		this.fromemail = fromemail;
		this.subject = subject;
		this.msgtext = msgtext;
		this.threadId = threadId;
		this.threadIndent = threadIndent;
		this.date = date;
		this.gdate = gdate;
		this.sdate = sdate;
		this.xdate = xdate;
		this.unread = unread;
		this.size = size;
		this.tags = tags;
		this.pecstatus = pecstatus;
		this.flag = flag;
		this.note = note;
		this.arch = arch;
		this.istoday = istoday;
		this.atts = atts;
		this.scheddate = scheddate;
		this.threadOpen = threadOpen;
		this.threadHasChildren = threadHasChildren;
		this.threadUnseenChildren = threadUnseenChildren;
		this.fmtd = fmtd;
		this.fromfolder = fromfolder;
	}
}
