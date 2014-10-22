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

import java.io.*;


public class HTMLInputStream extends InputStream {

  BufferedInputStream istream=null;
  byte startTag[]={'<','h','t','m','l','>'};
  byte endTag[]={'<','/','h','t','m','l','>'};
  byte startBuffer[]=new byte[startTag.length];
  byte endBuffer[]=new byte[endTag.length];
  int bytesToEof=0;
  boolean isRealEof=false;

  public HTMLInputStream(InputStream istream) {
    this.istream=new BufferedInputStream(istream);
  }

  public boolean isRealEof() {
    return isRealEof;
  }

  public void newDocument() {
    bytesToEof=-1;
  }

  public int read() throws IOException {
    if (isRealEof) return -1;

    if (bytesToEof==-1) {
      boolean found=lookFor(endTag,istream,endBuffer);
      if (found) bytesToEof=endTag.length;
    }

    if (bytesToEof==-1) return nextByte();

    if (bytesToEof==0) return -1;

    --bytesToEof;
    return nextByte();
  }

  private int nextByte() throws IOException {
    int i=istream.read();
    if (i==-1) isRealEof=true;
    return i;
  }

  private boolean lookFor(byte tag[], BufferedInputStream in, byte buf[]) throws IOException {
    in.mark(tag.length);
    int len=readFully(in,buf);
    boolean found=compare(buf,tag,len);
    in.reset();
    return found;
  }

  private int readFully(InputStream in, byte buf[]) throws IOException {
    int len=0;
    int pos=0;
    while(pos<buf.length && (len=in.read(buf,pos,buf.length-pos))>=0) {
      pos+=len;
    }
    return pos;
  }

  private boolean compare(byte buf1[], byte buf2[], int len) {

    if (len!=buf1.length||len!=buf2.length) return false;

    boolean equals=true;
    for(int i=0;i<buf1.length;++i) {
      char char1=Character.toLowerCase((char)buf1[i]);
      char char2=Character.toLowerCase((char)buf2[i]);
      if (char1!=char2) {
        equals=false;
        break;
      }
    }
    return equals;
  }
}