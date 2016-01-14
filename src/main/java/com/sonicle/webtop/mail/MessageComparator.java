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

import com.sonicle.mail.imap.SonicleIMAPMessage;
import java.util.Comparator;
import javax.mail.Message;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.Flags;
import javax.mail.MessagingException;

public class MessageComparator implements Comparator<Message> {

  public static final int SORT_BY_NONE=-1;
  public static final int SORT_BY_MSGIDX=0;
  public static final int SORT_BY_SENDER=1;
  public static final int SORT_BY_RCPT=2;
  public static final int SORT_BY_SUBJECT=3;
  public static final int SORT_BY_DATE=4;
  public static final int SORT_BY_SIZE=5;
  public static final int SORT_BY_PRIORITY=6;
  public static final int SORT_BY_STATUS=7;
  public static final int SORT_BY_FLAG=8;

  private int sort_by=SORT_BY_MSGIDX;
  boolean ascending=true;

  private Service ms=null;
  
  public MessageComparator(Service ms) {
	  this.ms=ms;
  }
  
  public void setSortBy(int by) {
    sort_by=by;
  }

  public void setAscending() {
    ascending=true;
  }

  public void setDescending() {
    ascending=false;
  }

  public int compare(Message m1, Message m2) {
      int result=0;
      SonicleIMAPMessage im=(SonicleIMAPMessage) m1;
      if (m1.isExpunged() || m2.isExpunged()) return 0;
      switch(sort_by) {
        case SORT_BY_MSGIDX:
          result=m1.getMessageNumber()-m2.getMessageNumber();
          long lresult=((SonicleIMAPMessage)m1).getUID()-((SonicleIMAPMessage)m2).getUID();
		  result=(lresult>0?1:result<0?-1:0);
          break;
        case SORT_BY_SENDER:
          String sender1="";
          String sender2="";
          try {
            Address senders[]=m1.getFrom();
            if (senders!=null) {
              InternetAddress ia=(InternetAddress)senders[0];
              String personal=ia.getPersonal();
              String email=ia.getAddress();
              if (personal==null) sender1=email;
              else sender1=personal+" <"+email+">";
            }
          } catch(Exception exc) {}
          try {
            Address senders[]=m2.getFrom();
            if (senders!=null) {
              InternetAddress ia=(InternetAddress)senders[0];
              String personal=ia.getPersonal();
              String email=ia.getAddress();
              if (personal==null) sender2=email;
              else sender2=personal+" <"+email+">";
            }
          } catch(Exception exc) {}
          result=sender1.compareToIgnoreCase(sender2);
          break;
        case SORT_BY_RCPT:
          String rcpt1="";
          String rcpt2="";
          try {
            Address rcpts[]=m1.getRecipients(javax.mail.Message.RecipientType.TO);
            if (rcpts!=null) {
              InternetAddress ia=(InternetAddress)rcpts[0];
              String personal=ia.getPersonal();
              String email=ia.getAddress();
              if (personal==null) rcpt1=email;
              else rcpt1=personal+" <"+email+">";
            }
          } catch(Exception exc) {}
          try {
            Address rcpts[]=m2.getRecipients(javax.mail.Message.RecipientType.TO);
            if (rcpts!=null) {
              InternetAddress ia=(InternetAddress)rcpts[0];
              String personal=ia.getPersonal();
              String email=ia.getAddress();
              if (personal==null) rcpt2=email;
              else rcpt2=personal+" <"+email+">";
            }
          } catch(Exception exc) {}
          result=rcpt1.compareToIgnoreCase(rcpt2);
          break;
        case SORT_BY_SUBJECT:
          String subject1=null;
          String subject2=null;
          try { subject1=m1.getSubject(); } catch(Exception exc) {}
          try { subject2=m2.getSubject(); } catch(Exception exc) {}
          if (subject1==null) subject1="";
          if (subject2==null) subject2="";
          result=subject1.compareToIgnoreCase(subject2);
          break;
        case SORT_BY_DATE:
          java.util.Date date1=null;
          java.util.Date date2=null;
          try { date1=m1.getSentDate(); if (date1==null) date1=m1.getReceivedDate(); } catch(Exception exc) { exc.printStackTrace();}
          try { date2=m2.getSentDate(); if (date2==null) date2=m2.getReceivedDate(); } catch(Exception exc) {exc.printStackTrace();}
          if (date1==null) {
            if (date2==null) result=0;
            else result=-1;
          } else if (date2==null) {
            result=1;
          } else {
            result=date1.compareTo(date2);
          }
          break;
        case SORT_BY_SIZE:
          int size1=0;
          int size2=0;
          try { size1=m1.getSize(); } catch(Exception exc) {}
          try { size2=m2.getSize(); } catch(Exception exc) {}
          result=size1-size2;
          break;
        case SORT_BY_STATUS:
          try {
            Flags fl1=m1.getFlags();
            Flags fl2=m2.getFlags();
            int f1=fl1.contains(Flags.Flag.ANSWERED)
                ?(fl1.contains(Flags.Flag.SEEN)?3:1)
                :fl1.contains(Flags.Flag.SEEN)?2:0;
            int f2=fl2.contains(Flags.Flag.ANSWERED)
                ?(fl2.contains(Flags.Flag.SEEN)?3:1)
                :fl2.contains(Flags.Flag.SEEN)?2:0;
            result=f1-f2;
          } catch(MessagingException exc) {
            exc.printStackTrace();
            result=0;
          }
          break;
        case SORT_BY_PRIORITY:
          try {
              int p1=Service.getPriority(m1);
              int p2=Service.getPriority(m2);
              result=p1-p2;
          } catch(MessagingException exc) {
              exc.printStackTrace();
              result=0;
          }
          break;
        case SORT_BY_FLAG:
          try {
            Flags fl1=m1.getFlags();
            Flags fl2=m2.getFlags();
            int len=ms.allFlagStrings.length;
            int f1=99;
            int f2=99;
            for(int i=0;i<len;++i) {
                String fs=ms.allFlagStrings[i];
                if (fl1.contains(fs)) f1=i;
                if (fl2.contains(fs)) f2=i;
            }
            result=f1-f2;
          } catch(MessagingException exc) {
            exc.printStackTrace();
            result=0;
          }
          break;
      }
      
      if (!ascending) result*=-1;

      return result;
  }

  public boolean equals(Message m) {
    /**@todo Implement this java.util.Comparator method*/
    throw new java.lang.UnsupportedOperationException("Method equals() not yet implemented.");
  }
}
