/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sonicle.webtop.mail;

import java.io.InputStream;
import jakarta.mail.*;

/**
 *
 * @author gbulfon
 */
public abstract class MessageContentProvider {
    
    protected int priority=3;
    protected boolean receipt=false;
    protected String subject=null;
    protected Address[] tos=null;
    protected Address[] ccs=null;
    protected Address[] bccs=null;
    protected String html="";
    protected String attachmentNames[]=null;
    protected InputStream attachments[]=null;
    protected InputStream source=null;
    protected String contentTypes[]=null;
    
    public InputStream getSource() {
        return source;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public boolean getReceipt() {
        return receipt;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public boolean hasRecipients() {
        return tos!=null || ccs!=null || bccs!=null;
    }
    
    public Address[] getToRecipients() {
        return tos;
    }
    
    public Address[] getCcRecipients() {
        return ccs;
    }
    
    public Address[] getBccRecipients() {
        return bccs;
    }
    
    public String getHtml() {
        return html;
    }
    
    public boolean hasAttachments() {
        return attachments!=null;
    }
    
    public int getAttachmentsCount() {
        return attachments.length;
    }
    
    public InputStream getAttachment(int i) {
        return attachments[i];
    }
    
    public String getAttachmentName(int i) {
        return attachmentNames[i];
    }
    
    public String getContentType(int i) {
        return contentTypes[i];
    }
}
