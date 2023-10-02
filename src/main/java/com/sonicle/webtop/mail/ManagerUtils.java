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
package com.sonicle.webtop.mail;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.mail.MailboxConfig;
import com.sonicle.mail.sieve.SieveMatch;
import com.sonicle.mail.sieve.SieveRule;
import com.sonicle.mail.sieve.SieveRuleField;
import com.sonicle.mail.sieve.SieveRuleOperator;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.mail.bol.OInFilter;
import com.sonicle.webtop.mail.model.MailFilter;
import com.sonicle.webtop.mail.model.MailFilterBase;
import com.sonicle.webtop.mail.model.SieveActionList;
import com.sonicle.webtop.mail.model.SieveRuleList;
import java.util.List;

/**
 *
 * @author malbinola
 */
public class ManagerUtils {
	
	public static String getProductName() {
		return WT.getPlatformName() + " Mail";
	}
	
	public static String findActiveScriptName(final List<com.fluffypeople.managesieve.SieveScript> scripts) {
		for (com.fluffypeople.managesieve.SieveScript script : scripts) {
			if (script.isActive()) return script.getName();
		}
		return null;
	}
	
	public static MailboxConfig createMailboxConfig(MailUserSettings mus) {
		return new MailboxConfig.Builder()
			.withUserFoldersPrefix(mus.getFolderPrefix())
			.withSentFolderName(mus.getFolderSent())
			.withDraftsFolderName(mus.getFolderDrafts())
			.withTrashFolderName(mus.getFolderTrash())
			.withSpamFolderName(mus.getFolderSpam())
			.withArchiveFolderName(mus.getFolderArchive())
			.build();
	}
	
	static <T extends MailFilter> T fillMailFilter(T tgt, OInFilter src) {
		fillMailFilterBase((MailFilterBase)tgt, src);
		if ((tgt != null) && (src != null)) {
			tgt.setFilterId(src.getInFilterId());
		}
		return tgt;
	}
	
	static <T extends MailFilterBase> T fillMailFilterBase(T tgt, OInFilter src) {
		if ((tgt != null) && (src != null)) {
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setEnabled(src.getEnabled());
			tgt.setOrder(src.getOrder());
			tgt.setName(src.getName());
			tgt.setSieveMatch(EnumUtils.forSerializedName(src.getSieveMatch(), SieveMatch.class));
			SieveRuleList rules = LangUtils.deserialize(src.getSieveRules(), null, SieveRuleList.class);
			if (rules != null) tgt.getSieveRules().addAll(rules);
			SieveActionList acts = LangUtils.deserialize(src.getSieveActions(), null, SieveActionList.class);
			if (acts != null) tgt.getSieveActions().addAll(acts);
		}
		return tgt;
	}
	
	static final Short MAILFILTER_SENDERBLACKLIST_BUILTIN = 1;
	
	static MailFilter createSenderBlacklistMailFilter() {
		return fillSenderBlacklistMailFilterWithDefaults(new MailFilter());
	}
	
	static MailFilter fillSenderBlacklistMailFilterWithDefaults(MailFilter tgt) {
		tgt.setBuiltIn(MAILFILTER_SENDERBLACKLIST_BUILTIN);
		tgt.setEnabled(true);
		tgt.setOrder((short)0);
		tgt.setName("Sender Blacklist (built-in)");
		tgt.setSieveMatch(SieveMatch.ANY);
		tgt.setSieveActions(SieveActionList.discardAndStop());
		return tgt;
	}
	
	static OInFilter fillOInFilter(OInFilter tgt, MailFilter src) {
		fillOInFilter(tgt, (MailFilterBase)src);
		if ((tgt != null) && (src != null)) {
			tgt.setInFilterId(src.getFilterId());
		}
		return tgt;
	}
	
	static OInFilter fillOInFilter(OInFilter tgt, MailFilterBase src) {
		if ((tgt != null) && (src != null)) {
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setEnabled(src.getEnabled());
			tgt.setOrder(src.getOrder());
			tgt.setName(src.getName());
			tgt.setSieveMatch(EnumUtils.toSerializedName(src.getSieveMatch()));
			tgt.setSieveRules(LangUtils.serialize(src.getSieveRules(), SieveRuleList.class));
			tgt.setSieveActions(LangUtils.serialize(src.getSieveActions(), SieveActionList.class));
		}
		return tgt;
	}
}
