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

import java.util.List;

/**
 *
 * @author gbulfon
 */
public class JsMessage {

	public String folder;
    public Boolean receipt;
    public Boolean priority;
    public String from;
    public String subject;
    public String content;
    public String format;
    public int identityId;
    public List<JsAttachment> attachments;
    public List<JsRecipient> torecipients;
    public List<JsRecipient> ccrecipients;
    public List<JsRecipient> bccrecipients;
    //Reply data
    public String replyfolder;
    public String inreplyto;
    public String references;
    //Forward data
    public String forwardedfolder;
    public String forwardedfrom;
    //Reply/Forward data
    public long origuid;
	public long draftuid;
	public String draftfolder;
	public Boolean deleted;

	public JsMessage() {
	}
	
	public JsMessage(String subject, String content, String format, int identityId, List<JsAttachment> attachments, List<JsRecipient> torecipients, List<JsRecipient> ccrecipients, List<JsRecipient> bccrecipients, String replyfolder, String inreplyto, String references, long origuid) {
		this.subject = subject;
		this.content = content;
		this.format = format;
		this.identityId = identityId;
		this.attachments = attachments;
		this.torecipients = torecipients;
		this.ccrecipients = ccrecipients;
		this.bccrecipients = bccrecipients;
		this.replyfolder = replyfolder;
		this.inreplyto = inreplyto;
		this.references = references;
		this.origuid = origuid;
	}

	public JsMessage(String subject, String content, String format, int identityId, List<JsAttachment> attachments, String forwardedfolder, String forwardedfrom, String inreplyto, String references, long origuid) {
		this.subject = subject;
		this.content = content;
		this.format = format;
		this.identityId = identityId;
		this.attachments = attachments;
		this.forwardedfolder = forwardedfolder;
		this.forwardedfrom = forwardedfrom;
		this.inreplyto = inreplyto;
		this.references = references;
		this.origuid = origuid;
	}

	public JsMessage(String folder, Boolean receipt, Boolean priority, String subject, String content, String format, int identityId, List<JsAttachment> attachments, List<JsRecipient> torecipients, List<JsRecipient> ccrecipients, List<JsRecipient> bccrecipients, String replyfolder, String inreplyto, String references, String forwardedfolder, String forwardedfrom, long origuid, Boolean deleted) {
		this.folder = folder;
		this.receipt = receipt;
		this.priority = priority;
		this.subject = subject;
		this.content = content;
		this.format = format;
		this.identityId = identityId;
		this.attachments = attachments;
		this.torecipients = torecipients;
		this.ccrecipients = ccrecipients;
		this.bccrecipients = bccrecipients;
		this.replyfolder = replyfolder;
		this.inreplyto = inreplyto;
		this.references = references;
		this.forwardedfolder = forwardedfolder;
		this.forwardedfrom = forwardedfrom;
		this.origuid = origuid;
	}
}
