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

/**
 *
 * @author gabriele.bulfon
 */
public class ProActiveSecurityRules {
	
	private boolean active;
	private boolean linkDomainCheck;
	private boolean myDomainCheck;
	private boolean frequentContactCheck;
	private boolean anyContactsCheck;
	private boolean trustedContactsCheck;
	private boolean fakePatternsCheck;
	private boolean unsubscribeDirectivesCheck;
	private boolean displaynameCheck;
	private boolean spamScoreVisualization;
	private boolean linkClickPrompt;
	private boolean zipCheck;
	private boolean forgedSenderCheck;
	private boolean linkGeolocalization;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean hasLinkDomainCheck() {
		return linkDomainCheck;
	}

	public void setLinkDomainCheck(boolean linkDomainCheck) {
		this.linkDomainCheck = linkDomainCheck;
	}

	public boolean hasMyDomainCheck() {
		return myDomainCheck;
	}

	public void setMyDomainCheck(boolean myDomainCheck) {
		this.myDomainCheck = myDomainCheck;
	}

	public boolean hasFrequentContactCheck() {
		return frequentContactCheck;
	}

	public void setFrequentContactCheck(boolean frequentContactCheck) {
		this.frequentContactCheck = frequentContactCheck;
	}

	public boolean hasAnyContactsCheck() {
		return anyContactsCheck;
	}

	public void setAnyContactsCheck(boolean anyContactsCheck) {
		this.anyContactsCheck = anyContactsCheck;
	}

	public boolean hasTrustedContactsCheck() {
		return trustedContactsCheck;
	}

	public void setTrustedContactsCheck(boolean trustedContactsCheck) {
		this.trustedContactsCheck = trustedContactsCheck;
	}

	public boolean hasFakePatternsCheck() {
		return fakePatternsCheck;
	}

	public void setFakePatternsCheck(boolean fakePatternsCheck) {
		this.fakePatternsCheck = fakePatternsCheck;
	}

	public boolean hasUnsubscribeDirectivesCheck() {
		return unsubscribeDirectivesCheck;
	}

	public void setUnsubscribeDirectivesCheck(boolean unsubscribeDirectivesCheck) {
		this.unsubscribeDirectivesCheck = unsubscribeDirectivesCheck;
	}

	public boolean hasDisplaynameCheck() {
		return displaynameCheck;
	}

	public void setDisplaynameCheck(boolean displaynameCheck) {
		this.displaynameCheck = displaynameCheck;
	}

	public boolean hasSpamScoreVisualization() {
		return spamScoreVisualization;
	}

	public void setSpamScoreVisualization(boolean spamScoreVisualization) {
		this.spamScoreVisualization = spamScoreVisualization;
	}

	public boolean hasLinkClickPrompt() {
		return linkClickPrompt;
	}

	public void setLinkClickPrompt(boolean linkClickPrompt) {
		this.linkClickPrompt = linkClickPrompt;
	}

	public boolean hasZipCheck() {
		return zipCheck;
	}

	public void setZipCheck(boolean zipCheck) {
		this.zipCheck = zipCheck;
	}

	public boolean hasForgedSenderCheck() {
		return forgedSenderCheck;
	}

	public void setForgedSenderCheck(boolean forgedSenderCheck) {
		this.forgedSenderCheck = forgedSenderCheck;
	}

	public boolean hasLinkGeolocalization() {
		return linkGeolocalization;
	}

	public void setLinkGeolocalization(boolean linkGeolocalization) {
		this.linkGeolocalization = linkGeolocalization;
	}
	


}