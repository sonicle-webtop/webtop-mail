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
 * @author gabriele.bulfon
 */
public class JsProActiveSecurity {
	
	public ArrayList<String> externalLinkHosts = null;
	public ArrayList<String> dangerousExtensions = null;
	Boolean isSenderTrusted;
	Boolean isNewsletter;
	Boolean isSenderMyDomain;
	Boolean isSenderFrequent;
	Boolean isSenderAnyContact;
	Boolean isSenderDisplaynameConsistentWithContact;
	Boolean isSenderFakePattern;
	Boolean hasZipAttachment;
	Boolean isSpam;
	Boolean hasForgedSender;
	Float	spamScore;
	Float	spamThreshold;

	public void setIsSenderTrusted(boolean isSenderTrusted) {
		this.isSenderTrusted = isSenderTrusted;
	}

	public void setIsSenderMyDomain(boolean isSenderMyDomain) {
		this.isSenderMyDomain = isSenderMyDomain;
	}

	public void setIsSenderFrequent(boolean isSenderFrequent) {
		this.isSenderFrequent = isSenderFrequent;
	}

	public void setIsSenderAnyContact(boolean isSenderAnyContact) {
		this.isSenderAnyContact = isSenderAnyContact;
	}

	public void setIsSenderDisplaynameConsistentWithContact(boolean isSenderDisplaynameConsistentWithContact) {
		this.isSenderDisplaynameConsistentWithContact = isSenderDisplaynameConsistentWithContact;
	}
	
	public void setIsSenderFakePattern(boolean isSenderFakePattern) {
		this.isSenderFakePattern = isSenderFakePattern;
	}

	public void addExternalLinkHost(String host) {
		if (externalLinkHosts==null) externalLinkHosts=new ArrayList<String>();
		externalLinkHosts.add(host);
	}

	public void setHasZipAttachment(Boolean hasZipAttachment) {
		this.hasZipAttachment = hasZipAttachment;
	}
	
	public void addDangerousExtension(String ext) {
		if (dangerousExtensions==null) dangerousExtensions=new ArrayList<String>();
		dangerousExtensions.add(ext);
	}
	
	public void setIsNewsletter(Boolean isNewsletter) {
		this.isNewsletter = isNewsletter;
	}

	public void setIsSpam(Boolean isSpam, Float spamScore, Float spamThreshold) {
		this.isSpam = isSpam;
		this.spamScore = spamScore;
		this.spamThreshold = spamThreshold;
	}
	
	public void setHasForgedSender(Boolean hasForgedSender) {
		this.hasForgedSender = hasForgedSender;
	}
}
