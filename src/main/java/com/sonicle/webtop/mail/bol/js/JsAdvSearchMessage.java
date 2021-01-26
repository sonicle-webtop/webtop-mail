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

/**
 *
 * @author Federico Ballarini
 */
public class JsAdvSearchMessage {
	public String folder;
	public String folderdesc;
	public String idmandfolder;
	public Long idmessage;
	public Integer priority;
	public String status;
	public String to;
	public String from;
	public String subject;
	public String date;
	public Boolean unread;
	public Integer size;
	public String flag;
	public Boolean arch;
	public Boolean istoday;
	public Boolean note;

	public JsAdvSearchMessage(String folder, String folderdesc, String idmandfolder, Long idmessage, Integer priority, String status, String to, String from, String subject, String date, Boolean unread, Integer size, String flag, Boolean arch, Boolean istoday, Boolean note) {
		this.folder = folder;
		this.folderdesc = folderdesc;
		this.idmandfolder = idmandfolder;
		this.idmessage = idmessage;
		this.priority = priority;
		this.status = status;
		this.to = to;
		this.from = from;
		this.subject = subject;
		this.date = date;
		this.unread = unread;
		this.size = size;
		this.flag = flag;
		this.arch = arch;
		this.istoday = istoday;
		this.note = note;
	}
}