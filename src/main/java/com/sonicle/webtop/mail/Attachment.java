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

import java.io.File;
import org.apache.commons.vfs2.FileObject;

public class Attachment {

  private File file=null;
  private FileObject fileObject=null;
  private String vfsuri=null;
  private String name=null;
  private String contentType=null;
  private String cid=null;
  private boolean inline=false;

  public Attachment(File file, String name, String contentType, String cid, boolean inline) {
    this.file=file;
    this.name=name;
    this.contentType=contentType;
    this.cid=cid;
    this.inline=inline;
  }

  public Attachment(FileObject fileObject, String name, String contentType, String vfsuri) {
    this.fileObject=fileObject;
    this.name=name;
    this.contentType=contentType;
    this.vfsuri=vfsuri;
  }

  public File getFile() {
    return file;
  }

  public FileObject getFileObject() {
    return fileObject;
  }
  
  public String getVFSUri() {
      return vfsuri;
  }

  public String getName() {
    return name;
  }

  public String getContentType() {
    return contentType;
  }

  public String getCid() {
    return cid;
  }

  public boolean isInline() {
    return inline;
  }

  public String toString() {
    return "filename="+file.getName()+" name="+name+" contentType="+contentType+" cid="+cid+" inline="+inline;
  }
}
