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

import com.sonicle.webtop.mail.bol.model.Identity;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class SimpleMessage {
  private long id=0;
  private Identity from=null;
  private String to="";
  private String cc="";
  private String bcc="";
  private String replyTo="";
  private String subject="";
  private String content="";
  private String textcontent="";
  private String mime="";
  private int priority=3;
  private boolean receipt=false;
  Part[] attachments=null;
  private Vector headers=new Vector();
  private String inreplyto=null;
  private String references[]=null;
  private String replyfolder=null;
  private String forwardedfrom=null;
  private String forwardedfolder=null;

  public static final int FORMAT_HTML=1;
  public static final int FORMAT_PREFORMATTED=2;
  public static final int FORMAT_TEXT=3;

  /**
   * Default constructor.
   */
  public SimpleMessage(long id) {
    this.id=id;
  }

  /**
   * Constructor.
   * Takes as parameters the TO, Cc, Bcc and subject fields as
   * well as the message's content.
   */
  public SimpleMessage(long id, String to, String cc, String bcc,
      String subject, String content) {
    this.id=id;
    setTo(to);
    setCc(cc);
    setBcc(bcc);
    setSubject(subject);
    setContent(content);
  }

  /**
   * Constructor.
   * Creates a SimpleMessage from a Message object passed as parameter.
   */
  public SimpleMessage(long id, Message msg) {
    try {
      this.id=id;
      setTo(msg.getRecipients(Message.RecipientType.TO));
      setCc(msg.getRecipients(Message.RecipientType.CC));
      setBcc(msg.getRecipients(Message.RecipientType.BCC));
      setSubject(msg.getSubject());

      String h[]=msg.getHeader("In-Reply-To");
      if (h!=null && h[0]!=null) inreplyto=h[0];
      references=msg.getHeader("References");

      h=msg.getHeader("Forwarded-From");
      if (h!=null && h[0]!=null) forwardedfrom=h[0];

      if(msg.isMimeType("text/plain")) {
        setContent((String)msg.getContent());
      }
    } catch(Exception e) {
      Service.logger.error("Exception",e);
//      Service.logger.debug("*** SimpleMessage: " +e);
    }
  }

  public long getId() {
    return id;
  }

  /**
   * Returns the TO field of SimpleMessage.
   */
  public String getTo() {
    return to;
  }

  /**
   * Sets the TO field of SimpleMessage.
   */
  public void setTo(String to) {
    this.to=to==null?"":to.trim();
  }

  /**
   * Accepts an array of addresses and adds them to the TO field of
   * SimpleMessage.
   */
  public void setTo(Address[] addr) {
    setTo(convert(addr));
  }

  /**
   * Accepts an array of Strings, converts its contents to addresses and
   * adds them to the TO field of SimpleMessage.
   */
  public void addTo(String[] addr) {
    setTo((to.length()>0?to+"; ":"")+convert(addr));
  }

  /**
   * Returns the Cc field of SimpleMessage.
   */
  public String getCc() {
    return cc;
  }

  /**
   * Sets the Cc field of SimpleMessage.
   */
  public void setCc(String cc) {
    this.cc=cc==null?"":cc.trim();
  }

  /**
   * Takes an array of addresses and adds them to the Cc field of SimpleMessage.
   */
  public void setCc(Address[] addr) {
    setCc(convert(addr));
  }

  /**
   * Takes an array of Strings, converts its contents to addresses and adds them
   * to the Cc field of SimpleMessage.
   */
  public void addCc(String[] addr) {
    setCc((cc.length()>0?cc+"; ":"")+convert(addr));
  }

  /**
   * Returns the Bcc field of SimpleMessage.
   */
  public String getBcc() {
    return bcc;
  }

  /**
   * Sets the Bcc field of SimpleMessage.
   */
  public void setBcc(String bcc) {
    this.bcc=bcc==null?"":bcc.trim();
  }

  /**
   * Takes an array of addresses and adds them to the Bcc field of SimpleMessage.
   */
  public void setBcc(Address[] addr) {
    setBcc(convert(addr));
  }

  /**
   * Takes an array of Strings, converts its contents to addresses and adds them
   * to the Cc field of SimpleMessage.
   */
  public void addBcc(String[] addr) {
    setBcc((bcc.length()>0?bcc+"; ":"")+convert(addr));
  }

  public void setFrom(Identity ident) {
    from=ident;
  }

  public Identity getFrom() {
    return from;
  }

  /**
   * Returns the reply to address of SimpleMessage.
   */
  public String getReplyTo() {
    return replyTo;
  }

  /**
   * Sets the reply to address of SimpleMessage.
   */
  public void setReplyTo(String replyTo) {
    this.replyTo=replyTo==null?"":replyTo.trim();
  }

  /**
   * Returns the subject of SimpleMessage.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject of SimpleMessage.
   */
  public void setSubject(String subject) {
    this.subject=subject==null?"":subject.trim();
  }

  public void setPriority(int p) {
    priority=p;
  }

  public int getPriority() {
    return priority;
  }

  public void setReceipt(boolean b) {
    receipt=b;
  }

  public boolean getReceipt() {
    return receipt;
  }

  public void addHeaderLine(String headerline) {
    headers.addElement(headerline);
  }

  public String[] getHeaderLines() {
    String hl[]=new String[headers.size()];
    for(int i=0;i<hl.length;++i) hl[i]=(String)headers.elementAt(i);
    return hl;
  }

  public void setInReplyTo(String s) {
      this.inreplyto=s;
  }

  public String getInReplyTo() {
      return inreplyto;
  }

  public void setReferences(String s[]) {
      this.references=s;
  }

  public String[] getReferences() {
      return references;
  }

  public void setReplyFolder(String foldername) {
      replyfolder=foldername;
  }

  public String getReplyFolder() {
      return replyfolder;
  }

  public void setForwardedFrom(String s) {
      this.forwardedfrom=s;
  }

  public String getForwardedFrom() {
      return forwardedfrom;
  }

  public void setForwardedFolder(String s) {
      this.forwardedfolder=s;
  }

  public String getForwardedFolder() {
      return forwardedfolder;
  }

  /**
   * Returns the content of SimpleMessage.
   */
  public String getContent() {
    return content;
  }

  public String getTextContent() {
    return textcontent;
  }

  public String getMime() {
    return mime;
  }

  /**
   * Sets the subject of SimpleMessage.
   */
  public void setContent(String content) {
    this.content=content==null?"":content;
    this.textcontent=content;
    this.mime="text/plain";
  }

  public void setContent(String richcontent, String textcontent, String mime) {
    this.content=richcontent;
    this.textcontent=textcontent;
    this.mime=mime;
  }

  /**
   * Returns an array of Parts containing the attachments in SimpleMessage.
   */
  public Part[] getAttachments() {
    return this.attachments;
  }

  /**
   * Sets the attachments of SimpleMessage.
   */
  public void setAttachments(Part[] attachs) {
    this.attachments=attachs;
  }

  /**
   * Break down one string containing all the email addresses into
   * an array of addresses
   * It recognizes comma and semi-colon as delimiter.
   *
   */
  public static String[] breakAddr(String addr) {
    if(addr==null||addr.length()==0) {
      return new String[0];
    } else {
      Vector addrlist=new Vector();
      boolean insideString=false;

      // Delimiters are comma, semi-colon
      //
//      StringTokenizer st = new StringTokenizer(addr, "\",;",true);
      //StringTokenizer st=new StringTokenizer(addr, "\";", true);
      StringTokenizer st=new StringTokenizer(addr, ";", true);
      String address="";
      while(st.hasMoreTokens()) {
        String token=st.nextToken();
        if(token==null||token.trim().length()==0) {
          continue;
        } else if(token.equals("\"")) {
          insideString=!insideString;
          if(insideString) {
            address="";
          }
          address+="\"";
          continue;
        }
//        else if (token.equals(";") || token.equals(",")) {
        else if(token.equals(";")) {
          if(insideString) {
            address+=token;
          }
          continue;
        } else {
          if(!insideString) {
            addrlist.addElement((address+token).trim());
            address="";
            insideString=false;
          } else {
            address+=token;
          }
        }
      }

      // Convert and store into an array of strings
      //
      String[] a=new String[addrlist.size()];
      for(int i=0; i<a.length; i++) {
        a[i]=(String)addrlist.elementAt(i);
      }
      return a;
    }
  }

  /**
   * Convert an array of Address to a string
   *
   * There is a similar method convert(String[] addr)
   */
  public static String convert(Address[] addr) {
    if(addr!=null) {
      StringBuffer str=new StringBuffer();
      for(int i=0; i<addr.length; i++) {
        InternetAddress ia=(InternetAddress)addr[i];
        String personal=ia.getPersonal();
        String mail=ia.getAddress();
        if(personal==null||personal.equals(mail)) {
          str.append(mail);
        } else {
          str.append(personal+" <"+mail+">");
        }
        if(i!=addr.length-1) {
          str.append("; ");
        }
      }
      return str.toString();
    } else {
      return "";
    }
  }

  /**
   * Convert an array of String to a address format string
   *
   * There is a similar method convert(Address[] addr)
   */
  public static String convert(String[] addr) {
    if(addr!=null) {
      StringBuffer str=new StringBuffer();
      for(int i=0; i<addr.length; i++) {
        str.append(addr[i]);
        if(i!=addr.length-1) {
          str.append("; ");
        }
      }
      return str.toString();
    } else {
      return "";
    }
  }

  public static SimpleMessage createFromDraft(int id, Message msg) {
    SimpleMessage smsg=null;
    try {
      String html=null;
      String text=null;
      if(msg.isMimeType("multipart/*")) {
        Multipart mp=(Multipart)msg.getContent();
        for(int x=0;x<mp.getCount();++x) {
          Part p=(Part)mp.getBodyPart(x);
          if (p.isMimeType("multipart/*")) {
            mp=(Multipart)p.getContent();
            for(int i=0; i<mp.getCount(); ++i) {
              p=(Part)mp.getBodyPart(i);
              if(html==null&&p.isMimeType("text/html")) {
                html=(String)p.getContent();
              } else if(text==null&&p.isMimeType("text/plain")) {
                text=(String)p.getContent();
              }
            }
          } else {
            p=(Part)mp.getBodyPart(x);
            if(html==null&&p.isMimeType("text/html")) {
              html=(String)p.getContent();
            } else if(text==null&&p.isMimeType("text/plain")) {
              text=(String)p.getContent();
            }
          }
        }
      } else {
        if(msg.isMimeType("text/plain")) {
          text=(String)msg.getContent();
        }
      }
      if(html!=null||text!=null) {
        smsg=new SimpleMessage(id, msg);
        smsg.setTo(msg.getRecipients(Message.RecipientType.TO));
        smsg.setCc(msg.getRecipients(Message.RecipientType.CC));
        smsg.setBcc(msg.getRecipients(Message.RecipientType.BCC));
        smsg.setSubject(msg.getSubject());
        if(html!=null) {
          smsg.setContent(html, text, "text/html");
        } else {
          smsg.setContent(text);
        }
        String spriority="3";
        String vheader[]=msg.getHeader("X-Priority");
        if(vheader!=null&&vheader[0]!=null) {
          spriority=vheader[0];
        }
        smsg.setPriority(Integer.parseInt(spriority));
        boolean receipt=false;
        vheader=msg.getHeader("Disposition-Notification-To");
        if(vheader!=null&&vheader[0]!=null) {
          receipt=true;
        }
        smsg.setReceipt(receipt);
        vheader=msg.getHeader("In-Reply-To");
        if(vheader!=null&&vheader[0]!=null) {
            smsg.inreplyto=vheader[0];
        }
        vheader=msg.getHeader("References");
        if(vheader!=null) {
            smsg.references=vheader;
        }
      }
    } catch(Exception exc) {
      Service.logger.error("Exception",exc);
      smsg=null;
    }
    return smsg;
  }

}

