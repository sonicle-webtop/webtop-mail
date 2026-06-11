/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.mail;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.rsql.parser.Operator;
import com.sonicle.commons.time.JavaTimeUtils;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.mail.model.MessageFlag;
import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Message;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.BodyTerm;
import jakarta.mail.search.DateTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.FromStringTerm;
import jakarta.mail.search.HeaderTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.RecipientStringTerm;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.SentDateTerm;
import jakarta.mail.search.SubjectTerm;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class SearchTermBuildingVisitor extends com.sonicle.mail.imap.SearchTermBuildingVisitor {

	private static final String DEFAULT = "default";
	private static final String FROM = "from";
	private static final String TO = "to";
	private static final String CC = "cc";
	private static final String BCC = "bcc";
	private static final String SUBJECT = "subject";
	private static final String MESSAGE = "message";
	private static final String EVERYWHERE = "everywhere";
	private static final String AFTER = "after";
	private static final String BEFORE = "before";
	private static final String ATTACHMENT = "attachment";
	private static final String ATTACHMENT_NAME = "attachname";
	private static final String UNREAD = "unread";
	private static final String FLAGGED = "flagged";
	private static final String FLAG = "flag";
	private static final String COMPLETED = "completed";
	private static final String NOTCOMPLETED = "notcompleted";
//	private static final String TAGGED = "tagged";
	private static final String UNANSWERED = "unanswered";
	private static final String PRIORITY = "priority";
	private static final String TAG = "tag";
	private static final String NOTES = "notes";
	
	private SearchTerm searchTerm;
	private String notePattern;
	private boolean hasAttachment=false;
	private String attachmentName=null;
	
	@Override
	protected SearchTerm buildSearchTerm(String fieldName, Operator operator, Collection<?> values, DateTimeZone timezone) {
		
		if (StringUtils.isEmpty(fieldName))
			throw new UnsupportedOperationException("Field not supported: " + fieldName);

		ArrayList<SearchTerm> terms = new ArrayList<>();
		ArrayList<SearchTerm> tagTerms = new ArrayList<SearchTerm>();
		
		values.forEach(value -> {
			String svalue = (String) value;
			boolean negated = svalue.equalsIgnoreCase("false");
			
			if(fieldName.equals(FROM)) {
				terms.add(new FromStringTerm(svalue));
			} else if(fieldName.equals(TO)) {
				terms.add(new RecipientStringTerm(Message.RecipientType.TO, svalue));
			} else if(fieldName.equals(CC)) {
				terms.add(new RecipientStringTerm(Message.RecipientType.CC, svalue));
			} else if(fieldName.equals(BCC)) {
				terms.add(new RecipientStringTerm(Message.RecipientType.BCC, svalue));
			} else if(fieldName.equals(SUBJECT)) {
				terms.add(new SubjectTerm(svalue));
			} else if(fieldName.equals(MESSAGE)) {
				terms.add(new BodyTerm(svalue));
			} else if(fieldName.equals(DEFAULT)) {
				SearchTerm defterms[] = toDefaultSearchTerm(svalue);
				terms.add(new OrTerm(defterms));
			} else if(fieldName.equals(EVERYWHERE)) {
				SearchTerm anyterms[] = toAnySearchTerm(svalue);
				terms.add(new OrTerm(anyterms));
			} else if(fieldName.equals(AFTER)) {
				Date afterDate = parseDate(svalue, timezone);
				terms.add(new ReceivedDateTerm(DateTerm.GE, afterDate));
				terms.add(new SentDateTerm(DateTerm.GE, afterDate));
			} else if(fieldName.equals(BEFORE)) {
				Date beforeDate = parseDate(svalue, timezone);
				terms.add(new ReceivedDateTerm(DateTerm.LE, beforeDate));
				terms.add(new SentDateTerm(DateTerm.LE, beforeDate));

			} else if(fieldName.equals(NOTES)) {
				terms.add(new FlagTerm(MailManager.getFlagNote(), true));
				if (!"*".equals(value)) notePattern=valueToLikePattern(svalue);
			} else if(fieldName.equals(ATTACHMENT_NAME)) {
				attachmentName=value!=null?svalue.toLowerCase():null;
			} else if(fieldName.equals(ATTACHMENT)) {
				hasAttachment=true;
			} else if(fieldName.equals(UNREAD)) {
				terms.add(new FlagTerm(new Flags(Flag.SEEN), negated));

			} else if(fieldName.equals(FLAGGED)) {
				final List<MessageFlag> availFlags = EnumUtils.allTypesOf(MessageFlag.class);
				final FlagTerm flagTerms[] = new FlagTerm[availFlags.size()*2 +1];
				flagTerms[0] = new FlagTerm(new Flags(Flag.FLAGGED), !negated);
				int i = 1;
				for (MessageFlag flag : availFlags) {
					flagTerms[i] = new FlagTerm(flag.toImapFlag(), !negated);
					flagTerms[i+1] = new FlagTerm(flag.toOldImapFlag(), !negated);
					i += 2;
				}
				if (negated) {
					terms.add(new AndTerm(flagTerms));
				} else {
					terms.add(new OrTerm(flagTerms));
				}

			} else if (fieldName.equals(FLAG)) {
				final MessageFlag flag = EnumUtils.forSerializedName(svalue, MessageFlag.class);
				if (flag != null) tagTerms.add(new FlagTerm(flag.toImapFlag(), !negated));

			} else if (fieldName.equals(COMPLETED)) {
				final FlagTerm ft1 = new FlagTerm(MessageFlag.COMPLETE.toImapFlag(), !negated);
				final FlagTerm ft2 = new FlagTerm(MessageFlag.COMPLETE.toOldImapFlag(), !negated);
				if (negated) {
					terms.add(new AndTerm(ft1, ft2));
				} else {
					terms.add(new OrTerm(ft1, ft2));
				}

			} else if (fieldName.equals(NOTCOMPLETED)) { // Remove when UI will be able to save negated states!
				final FlagTerm ft1 = new FlagTerm(MessageFlag.COMPLETE.toImapFlag(), negated);
				final FlagTerm ft2 = new FlagTerm(MessageFlag.COMPLETE.toOldImapFlag(), negated);
				if (negated) {
					terms.add(new OrTerm(ft1, ft2));
				} else {
					terms.add(new AndTerm(ft1, ft2));
				}
			} else if (fieldName.equals(TAG)) {
				try {
					final String tagId = TagsHelper.tagIdToFlagString(WT.getCoreManager().getTag(svalue));
					tagTerms.add(new FlagTerm(new Flags(tagId), !negated));
				} catch (Exception ex) { }

			} else if (fieldName.equals(UNANSWERED)) {
				terms.add(new FlagTerm(new Flags(Flag.ANSWERED), negated));

			} else if (fieldName.equals(PRIORITY)) {
				final HeaderTerm ht1 = new HeaderTerm("X-Priority", "1");
				final HeaderTerm ht2 = new HeaderTerm("X-Priority", "2");
				if (negated) {
					terms.add(new AndTerm(ht1, ht2));
				} else {
					terms.add(new OrTerm(ht1, ht2));
				}
			} else {
				throw new UnsupportedOperationException("Field not supported: " + fieldName);
			}

		});
		
		//add tags terms as or
		int n = tagTerms.size();
		if (n == 1) {
			terms.add(tagTerms.get(0));
		} 
		else if (n>1) {
			SearchTerm tterms[] = new SearchTerm[n];
			tagTerms.toArray(tterms);
			terms.add(new OrTerm(tterms));
		}

		n = terms.size();
		if (n == 1) {
			searchTerm = terms.get(0);
		}
		else if (n>1) {
			SearchTerm vterms[] = new SearchTerm[n];
			terms.toArray(vterms);
			searchTerm = new AndTerm(vterms);
		}
		
		return searchTerm;
	}
	
	public boolean hasAttachment() {
		return hasAttachment;
	}
	
	public String getAttachmentName() {
		return attachmentName;
	}
	
	public boolean hasNotePattern() {
		return !StringUtils.isEmpty(notePattern);
	}
	
	public String getNotePattern() {
		return notePattern;
	}

	public static SearchTerm[] toAnySearchTerm(String value) {
		SearchTerm anyterms[] = new SearchTerm[6];
		anyterms[0] = new SubjectTerm(value);
		anyterms[1] = new RecipientStringTerm(Message.RecipientType.TO, value);
		anyterms[2] = new RecipientStringTerm(Message.RecipientType.CC, value);
		anyterms[3] = new RecipientStringTerm(Message.RecipientType.BCC, value);
		anyterms[4] = new FromStringTerm(value);
		anyterms[5] = new BodyTerm(value);
		return anyterms;
	}
	
	public static SearchTerm[] toDefaultSearchTerm(String value) {
		SearchTerm anyterms[] = new SearchTerm[5];
		anyterms[0] = new SubjectTerm(value);
		anyterms[1] = new RecipientStringTerm(Message.RecipientType.TO, value);
		anyterms[2] = new RecipientStringTerm(Message.RecipientType.CC, value);
		anyterms[3] = new RecipientStringTerm(Message.RecipientType.BCC, value);
		anyterms[4] = new FromStringTerm(value);
		return anyterms;
	}

	private static Date parseDate(String value, DateTimeZone timezone) {
		String date = StringUtils.replace(value, "/", "-");
		Instant instant = JavaTimeUtils.toInstant(JavaTimeUtils.parseLocalDateYMD(date), JavaTimeUtils.toZoneId(timezone));
		return new Date(instant.toEpochMilli());
	}
	
	private String valueToLikePattern(String value) {
		value = StringUtils.replace(value, "*", "%");
		value = StringUtils.replace(value, "\\*", "*");
		return value;
	}
	
}
