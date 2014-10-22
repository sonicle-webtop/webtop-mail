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

import com.sonicle.webtop.core.sdk.BasicEnvironment;
import com.sonicle.webtop.core.sdk.UserProfile;

/**
 *
 * @author gbulfon
 */
public class MailUserProfile {
    
    private BasicEnvironment env;
    private String folderPrefix;
    private boolean scanAll;
    private int scanSeconds;
    private int scanCycles;
    private String folderSent;
    private String folderDrafts;
    private String folderTrash;
    private String folderSpam;
    private String mailProtocol;
	private String mailHost;
    private int mailPort;
	private String mailUsername;
	private String mailPassword;
	private String replyTo;
	private Identity identities[];
	private String sharedSort;
	private boolean includeMessageInReply;
	private int numMessageList;

    public MailUserProfile(BasicEnvironment env) {
        this.env=env;
		UserProfile profile=env.getProfile();
        
        // TODO: initialize from profile
        folderPrefix="";
        scanAll=false;
        scanSeconds=30;
        scanCycles=10;
        folderSent="Sent";
        folderDrafts="Drafts";
        folderTrash="Trash";
        folderSpam="Spam";
        mailProtocol="imap";
		mailHost="www.sonicle.com";
        mailPort=143;
		mailUsername="gabriele.bulfon@sonicle.com";
		mailPassword="nrdstg88";
		replyTo="";
		identities=new Identity[2];
		identities[0]=new Identity(profile.getDisplayName(), profile.getEmailAddress(),null);
		identities[1]=new Identity("Ciccio", "ciccio@sonicle.com",null);
		sharedSort="N";
		includeMessageInReply=true;
		numMessageList=50;
    }
    
    public String getFolderPrefix() {
        return folderPrefix;
    }
    
    public boolean isScanAll() {
        return scanAll;
    }
    
    public int getScanSeconds() {
        return scanSeconds;
    }
    
    public int getScanCycles() {
        return scanCycles;
    }
    
    public String getFolderSent() {
        return folderSent;
    }

    public String getFolderDrafts() {
        return folderDrafts;
    }
    
    public String getFolderTrash() {
        return folderTrash;
    }
    
    public String getFolderSpam() {
        return folderSpam;
    }
	
    public String getMailProtocol() {
        return mailProtocol;
    }
    
    public String getMailHost() {
        return mailHost;
    }
	
    public int getMailPort() {
        return mailPort;
    }
    
    public String getMailUsername() {
        return mailUsername;
    }
	
    public String getMailPassword() {
        return mailPassword;
    }
	
	public String getReplyTo() {
		return replyTo;
	}
	
	public Identity[] getIdentities() {
		return identities;
	}
	
	public Identity getIdentity(int index) {
		return identities[index];
	}
	
	public Identity getIdentity(String foldername) {
		for(Identity ident: identities) {
			if (ident.mainfolder!=null && ident.mainfolder.length()>0 && ident.mainfolder.equals(foldername)) {
				return ident;
		}
	  }
	  return null;
	}
		
	public String getSharedSort() {
		return sharedSort;
	}
	
	public boolean isIncludeMessageInReply() {
		return includeMessageInReply;
	}
	
	public int getNumMsgList() {
		return numMessageList;
	}
	
}
