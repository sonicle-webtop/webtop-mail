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

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;

public class HTMLMailData {

  private final ArrayList<Part> dispParts=new ArrayList<>();
  private final HashMap<String,Part> cidParts=new HashMap<>();
  private final HashMap<String,Part> urlParts=new HashMap<>();
  private final ArrayList<Part> unknownParts=new ArrayList<> ();
  private final ArrayList<Part> attachmentParts=new ArrayList<> ();
  private ArrayList<String> referencedCids=new ArrayList<String>();

  private MimeMessage message=null;
  private Folder folder=null;
  private int nid=0;

  public HTMLMailData(MimeMessage msg, Folder folder) throws MessagingException {
    this.message=msg;
    this.folder=folder;
    this.nid=msg.getMessageNumber();
  }

  public Folder getFolder() {
    return folder;
  }

  public String getFolderName() {
    return folder.getFullName();
  }

  public int getMessageNumber() {
    return nid;
  }

  public MimeMessage getMessage() {
    return message;
  }

  public void addDisplayPart(Part part) {
    if (!dispParts.contains(part)) dispParts.add(part);
  }

  public void addUnknownPart(Part part) {
    if (!unknownParts.contains(part)) unknownParts.add(part);
  }

  public void addAttachmentPart(Part part) {
    if (!attachmentParts.contains(part)) attachmentParts.add(part);
  }

  public void addCidPart(String name, Part part) {
    if (!cidParts.containsKey(name)) cidParts.put(name, part);
  }

  public void addUrlPart(String url, Part part) {
    if (!urlParts.containsKey(url)) urlParts.put(url, part);
  }
  
  public void addReferencedCid(String name) {
	  referencedCids.add(name);
  }
  
  public int getDisplayPartCount() {
    return dispParts.size();
  }

  public int getUnknownPartCount() {
    return unknownParts.size();
  }

  public int getAttachmentPartCount() {
    return attachmentParts.size();
  }

  public int getCidPartCount() {
    return cidParts.size();
  }

  public Part getDisplayPart(int index) {
    return dispParts.get(index);
  }

  public Part getUnknownPart(int index) {
    return unknownParts.get(index);
  }

  public int getUnknownIndex(Part p) {
    return unknownParts.indexOf(p);
  }

  public Part getAttachmentPart(int index) {
    return attachmentParts.get(index);
  }

  public int getAttachmentIndex(Part p) {
    return attachmentParts.indexOf(p);
  }

  public Part getCidPart(String name) {
    return(Part)cidParts.get(name);
  }

  public Set<String> getCidNames() {
    return cidParts.keySet();
  }

  public boolean isReferencedCid(String name) {
	  return referencedCids.contains(name);
  }

  public boolean conatinsUrlPart(String url) {
    return urlParts.containsKey(url);
  }

  public Part getUrlPart(String url) {
    return(Part)urlParts.get(url);
  }

  public void removeUnknownPart(Part part) {
    unknownParts.remove(part);
  }

  public void removeAttachmentPart(Part part) {
    attachmentParts.remove(part);
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
  
}
