/*
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
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.mail.bol.model;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.bean.QueryObj;
import com.sonicle.commons.web.json.bean.QueryObj.Condition;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.model.Tag;
import com.sonicle.webtop.mail.MailManager;
import com.sonicle.webtop.mail.Service;
import com.sonicle.webtop.mail.TagsHelper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SubjectTerm;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Inis
 */
public class ImapQuery {
	
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
	private static final String UNREAD = "unread";
	private static final String FLAGGED = "flagged";
//	private static final String TAGGED = "tagged";
	private static final String UNANSWERED = "unanswered";
	private static final String PRIORITY = "priority";
	private static final String TAG = "tag";
	private static final String NOTES = "notes";
	
	private SearchTerm searchTerm;
	private String notePattern;
	private boolean hasAttachment=false;
	
	public ImapQuery(String allFlagStrings[], QueryObj query, DateTimeZone timezone) {
		parseQuery(allFlagStrings, query, timezone);
	}
	
	public ImapQuery(boolean hasAttachment) {
		this.searchTerm=null;
		this.hasAttachment=hasAttachment;
	}
	
	public ImapQuery(SearchTerm searchTerm, boolean hasAttachment) {
		this.searchTerm=searchTerm;
		this.hasAttachment=hasAttachment;
	}
	
	private void parseQuery(String allFlagStrings[], QueryObj query, DateTimeZone timezone) {
		ArrayList<SearchTerm> terms = new ArrayList<SearchTerm>();
		
		if(query != null) {
			ArrayList<Condition> conditionsList = query.conditions;
			String allText = query.allText;
			
			if(allText !=null && allText.trim().length() > 0) {
				SearchTerm defaultterms[] = toDefaultSearchTerm(allText);
					terms.add(new OrTerm(defaultterms));
			}
		
			conditionsList.forEach(condition -> {
				String key = condition.keyword;
				String value = condition.value;

				if(key.equals(FROM)) {
					terms.add(new FromStringTerm(value));
				} else if(key.equals(TO)) {
					terms.add(new RecipientStringTerm(Message.RecipientType.TO, value));
				} else if(key.equals(CC)) {
					terms.add(new RecipientStringTerm(Message.RecipientType.CC, value));
				} else if(key.equals(BCC)) {
					terms.add(new RecipientStringTerm(Message.RecipientType.BCC, value));
				} else if(key.equals(SUBJECT)) {
					terms.add(new SubjectTerm(value));
				} else if(key.equals(MESSAGE)) {
					terms.add(new BodyTerm(value));
				} else if(key.equals(EVERYWHERE)) {
					SearchTerm anyterms[] = toAnySearchTerm(value);
					terms.add(new OrTerm(anyterms));
				} else if(key.equals(AFTER)) {
					Date afterDate = ImapQuery.parseDate(value, timezone);
					terms.add(new ReceivedDateTerm(DateTerm.GE, afterDate));
					terms.add(new SentDateTerm(DateTerm.GE, afterDate));
				} else if(key.equals(BEFORE)) {
					Date beforeDate = ImapQuery.parseDate(value, timezone);
					terms.add(new ReceivedDateTerm(DateTerm.LE, beforeDate));
					terms.add(new SentDateTerm(DateTerm.LE, beforeDate));

				} else if(key.equals(TAG)) {
					try {
						terms.add(
								new FlagTerm(
										new Flags(
												TagsHelper.tagIdToFlagString(
														WT.getCoreManager().getTag(value)
												)
										),true
								)
						);
					} catch(Exception exc) {
						
					}
				} else if(key.equals(NOTES)) {
					terms.add(new FlagTerm(MailManager.getFlagNote(), true));
					if (!"*".equals(value)) notePattern=valueToLikePattern(value);
				} else if(value.equals(ATTACHMENT)) {
					hasAttachment=true;
				} else if(value.equals(UNREAD)) {
					terms.add(new FlagTerm(new Flags(Flag.SEEN), false));
				} else if(value.equals(FLAGGED)) {
					FlagTerm fts[] = new FlagTerm[allFlagStrings.length + 1];
					fts[0] = new FlagTerm(new Flags(Flag.FLAGGED), true);
					for(int i = 0;i < allFlagStrings.length; ++i)
						fts[i+1] = new FlagTerm(new Flags(allFlagStrings[i]), true);
					terms.add(new OrTerm(fts));
/*				} else if(value.equals(TAGGED)) {
					try {
						Collection<Tag> tags=WT.getCoreManager().listTags().values();
						FlagTerm fts[] = new FlagTerm[tags.size()];
						int i = 0;
						for(Tag tag: tags) {
							fts[i++] = new FlagTerm(new Flags(TagsHelper.tagIdToFlagString(tag)), true);
						}
						terms.add(new OrTerm(fts));
					} catch(Exception exc) {
					}*/
				} else if(value.equals(UNANSWERED)) {
					 terms.add(new FlagTerm(new Flags(Flag.ANSWERED), false));
				} else if(value.equals(PRIORITY)) {
					 HeaderTerm p1 = new HeaderTerm("X-Priority", "1");
						HeaderTerm p2 = new HeaderTerm("X-Priority", "2");
						terms.add(new OrTerm(p1,p2));
				}

			});
		}
		
		int n = terms.size();
		if (n==1) {
			searchTerm = terms.get(0);
		}
		else if (n>1) {
			SearchTerm vterms[] = new SearchTerm[n];
			terms.toArray(vterms);
			searchTerm = new AndTerm(vterms);
		}
	}
	
	public SearchTerm getSearchTerm() {
		return searchTerm;
	}
	
	public String getNotePattern() {
		return notePattern;
	}
	
	public boolean hasNotePattern() {
		return notePattern!=null;
	}
	
	public boolean hasAttachment() {
		return hasAttachment;
	}
	
	private String valueToLikePattern(String value) {
		value = StringUtils.replace(value, "*", "%");
		value = StringUtils.replace(value, "\\*", "*");
		return value;
	}
	
	
	private static Date parseDate(String value, DateTimeZone timezone) {
		String date = StringUtils.replace(value, "/", "-");
		Instant instant = DateTimeUtils.toInstant(DateTimeUtils.parseLocalDate(date), DateTimeUtils.toZoneId(timezone));
		return new Date(instant.toEpochMilli());
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
	
}
