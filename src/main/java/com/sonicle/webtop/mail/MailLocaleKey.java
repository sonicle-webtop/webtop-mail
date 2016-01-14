/*
* WebTop Groupware is a bundle of WebTop Services developed by Sonicle S.r.l.
* Copyright (C) 2011 Sonicle S.r.l.
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

/**
 *
 * @author gbulfon
 */
public class MailLocaleKey {

    public static final String SERVICE_DESCRIPTION="service.description";
    public static final String MSG_FROMTITLE="msg.fromtitle";
    public static final String MSG_TOTITLE="msg.totitle";
    public static final String MSG_CCTITLE="msg.cctitle";
    public static final String MSG_DATETITLE="msg.datetitle";
    public static final String MSG_SUBJECTTITLE="msg.subjecttitle";
    public static final String ACTION_CHECKMAIL="action.checkmail";
    public static final String ACTION_NEWMESSAGE="action.newmessage";
    public static final String ACTION_WEBPOPUP="action.webpopup";
    public static final String FOLDERS_INBOX="folders.inbox";
    public static final String FOLDERS_SENT="folders.sent";
    public static final String FOLDERS_DRAFTS="folders.drafts";
    public static final String FOLDERS_TRASH="folders.trash";
    public static final String FOLDERS_SPAM="folders.spam";
    public static final String FOLDERS_SHARED="folders.shared";
    public static final String FILTERS_FILE="filters.file";
    public static final String FILTERS_DISCARD="filters.discard";
    public static final String FILTERS_FORWARD="filters.forward";
    public static final String FILTERS_REJECT="filters.reject";
    public static final String FILTERS_IF="filters.if";
    public static final String FILTERS_OR="filters.or";
    public static final String FILTERS_AND="filters.and";
    public static final String FILTERS_CONTAINS="filters.contains";
    public static final String FILTERS_GREATERTHAN="filters.greaterthan";
    public static final String FILTERS_LESSTHAN="filters.lessthan";
    public static final String FILTERS_FROM="filters.from";
    public static final String FILTERS_TO="filters.to";
    public static final String FILTERS_SUBJECT="filters.subject";
    public static final String FILTERS_SIZE="filters.size";
    public static final String FILTERS_FIELD="filters.field";
    public static final String ADDRESS_ERROR="address.error";
	public static final String FAX_ADDRESS_ERROR="fax.address.error";
	public static final String FAX_MAXADDRESS_ERROR="fax.maxaddress.error";
    public static final String ICAL_ATTENDEES="ical.attendees";
    public static final String RECIPIENTS_SOURCE_AUTO="recipients.source.auto";
    public static final String SCHEDULED_SENT_SUBJECT="scheduled.sent.subject";
    public static final String SCHEDULED_SENT_HTML="scheduled.sent.html";
	public static final String ICAL_INVITED_BY="ical.invited.by";
	public static final String ICAL_INVITED_TO="ical.invited.to";
	public static final String ICAL_ACCEPTED="ical.accpeted";
	public static final String ICAL_DECLINED="ical.declined";
	public static final String ICAL_CANCELED="ical.canceled";
	public static final String ICAL_REPLY_ACCEPTED="ical.reply.accepted";
	public static final String ICAL_REPLY_DECLINED="ical.reply.declined";

    public static String FILTERS_OPERATOR(String operator) {
        return "filters."+operator;
    }

    public static String FILTERS_FIELD(String field) {
        return "filters."+field;
    }

    public static String FILTERS_COMPARISON(String comparison) {
        return "filters."+comparison;
    }

    public static String FILTERS_ACTION(String action) {
        return "filters."+action;
    }



}
