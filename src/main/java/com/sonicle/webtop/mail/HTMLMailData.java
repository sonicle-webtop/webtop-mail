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

import com.sonicle.mail.MimeUtils;
import com.sonicle.mail.imap.SonicleIMAPMessage;
import com.sonicle.mail.parser.MimeMessageParser;
import com.sonicle.mail.parser.MimeMessageParser.ParsedMimeMessageComponents;
import com.sonicle.mail.pec.PECUtils;
import java.util.*;
import java.io.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class HTMLMailData {
  private ParsedMimeMessageComponents parsed;
  private final ArrayList<String> referencedCids=new ArrayList<>();

  private MimeMessage message=null;
  private Folder folder=null;
  private long nuid=0;
  
  private ICalendarRequest icalRequest=null;
  private boolean hasICalAttachment=false;
  
  private boolean isPec=false;
  
  public HTMLMailData(MimeMessage msg, FolderCache fc) throws MessagingException {
		this.message=msg;
		this.folder=fc.getFolder();
		if (msg instanceof SonicleIMAPMessage) this.nuid=((SonicleIMAPMessage)msg).getUID();
		if (fc.isPEC()) isPec = PECUtils.isPECEnvelope(msg);
		this.parsed = MimeMessageParser.parseMimeMessage(message, isPec);
  }
  
  public ParsedMimeMessageComponents getParsedMimeMessageComponents() {
	  return parsed;
  }
  
  public boolean isPEC() {
	  return isPec;
  }

  public Folder getFolder() {
    return folder;
  }

  public String getFolderName() {
    return folder.getFullName();
  }

  public long getUID() {
    return nuid;
  }

  public MimeMessage getMessage() {
    return message;
  }
  
  public ICalendarRequest getICalRequest() {
	  return icalRequest;
  }
  
  public void setICalRequest(ICalendarRequest ir) {
	  icalRequest=ir;
  }  

  public void setHasICalAttachment(boolean b) {
	  hasICalAttachment=b;
  }
  
  /**
   * @deprecated legacy method to replicate original logic: use hasCalendar instead
   */
  @Deprecated
  public boolean hasICalAttachment() {
	  return hasICalAttachment;
  }

  public void addAttachmentPart(Part part, int depth) {
    if (!parsed.geAttachmentParts().contains(part)) {
		boolean addPart = true;
		try {
			if (MimeUtils.isMimeType(part, "application/ics") || MimeUtils.isMimeType(part, "text/calendar")) {
				if (!parsed.hasICalAttachment) {
					parsed.hasICalAttachment = true;
				} else {
					addPart = false;
				}
			}
		} catch(MessagingException exc) {}
		if (addPart) parsed.appendAttachmentPart(part, depth);
	}
  }
  
  public void addReferencedCid(String name) {
	  referencedCids.add(name);
  }
  
  public int getDisplayPartCount() {
    return parsed.getDisplayParts().size();
  }

  public int getUnknownPartCount() {
    return parsed.getUnknownParts().size();
  }

  public int getAttachmentPartCount() {
    return parsed.geAttachmentParts().size();
  }

  public int getCidPartCount() {
    return parsed.getCidParts().size();
  }
  
  public int getPartLevel(Part p) {
      Integer i=parsed.getPartsDepthMap().get(p);
      return i!=null?i:0;
  }

  public Part getDisplayPart(int index) {
    return parsed.getDisplayParts().get(index);
  }

  public Part getUnknownPart(int index) {
    return parsed.getUnknownParts().get(index);
  }

  public int getUnknownIndex(Part p) {
    return parsed.getUnknownParts().indexOf(p);
  }

  public Part getAttachmentPart(int index) {
    return parsed.geAttachmentParts().get(index);
  }

  public int getAttachmentIndex(Part p) {
    return parsed.geAttachmentParts().indexOf(p);
  }

  public Part getCidPart(String name) {
    return(Part)parsed.getCidParts().get(name);
  }

  public Set<String> getCidNames() {
    return parsed.getCidParts().keySet();
  }

  public boolean isReferencedCid(String name) {
	  return referencedCids.contains(name);
  }

  public boolean conatinsUrlPart(String url) {
    return parsed.getUrlParts().containsKey(url);
  }

  public Part getUrlPart(String url) {
    return(Part)parsed.getUrlParts().get(url);
  }

  public void removeUnknownPart(Part part) {
    parsed.getUnknownParts().remove(part);
  }

  public void removeAttachmentPart(Part part) {
    parsed.geAttachmentParts().remove(part);
  }
  
  public int getRealPartSize(Part part) throws MessagingException, IOException {
    InputStream is = part.getInputStream();
    byte[] b = new byte[64 * 1024];
    int len = 0;
    int tot = 0;

    while ((len = is.read(b)) != -1) tot+=len;
    is.close();
    return tot;
  }
  
/*  public void setCidProperties(String cid, CidProperties props) {
	  cidProperties.put(cid, props);
  }
  
  public CidProperties getCidProperties(String cid) {
	  return cidProperties.get(cid);
  }*/
}
