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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.model.IncomingShareRoot;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfile.Data;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.mail.bol.OIdentity;
import com.sonicle.webtop.mail.bol.model.Identity;
import com.sonicle.webtop.mail.dal.IdentityDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;

/**
 *
 * @author gabriele.bulfon
 */
public class MailManager extends BaseManager {

	public static final Logger logger = WT.getLogger(MailManager.class);
	public static final String RESOURCE_IMAPFOLDER = "IMAPFOLDER";
	
	List<Identity> identities=null;
	
	public MailManager() {
		this(RunContext.getProfileId());
	}
	
	public MailManager(UserProfile.Id targetProfileId) {
		super(targetProfileId);
	}
	
	public Identity createIdentity(OIdentity oi) throws WTException {
		try {
			Identity i=new Identity();
			BeanUtils.copyProperties(i,oi);
			return i;
		} catch(Exception ex) {
			throw new WTException(ex,"Error creating identity");
		}
	}
	
	public List<Identity> listIdentities() throws WTException {
		if (identities==null)
			identities=buildIdentities();
		
		return identities;
	}
	
	class ImapFolderData {
		boolean useMyPersonalInfo;
		boolean forceMyMailcard;
	}
	
	private List<Identity> buildIdentities() throws WTException {
		Connection con=null;
		List<Identity> idents=new ArrayList();
		try {
			UserProfile.Id pid=getTargetProfileId();
			//first add main identity
			Data udata=WT.getUserData(pid);
			Identity id=new Identity(udata.getEmail().getAddress(),udata.getDisplayName(),null);
			id.setIsMainIdentity(true);
			idents.add(id);
			
			//add configured additional identities
			con=WT.getConnection(SERVICE_ID);
			IdentityDAO idao=IdentityDAO.getInstance();
			List<OIdentity> items=idao.selectById(con, pid.getDomainId(),pid.getUserId());
			for(OIdentity oi: items) {
				id=createIdentity(oi);
				idents.add(id);
			}
			
			//add automatic shared identities
			CoreManager core=WT.getCoreManager(getTargetProfileId());
			for(IncomingShareRoot share: core.listIncomingShareRoots(SERVICE_ID, RESOURCE_IMAPFOLDER)) {
				for(OShare folder: core.listIncomingShareFolders(share.getShareId(), SERVICE_ID, RESOURCE_IMAPFOLDER)) {
					UserProfile.Id opid=share.getOriginPid();
					udata=core.getUserData(opid);
					ImapFolderData ifd=core.getIncomingShareFolderData(folder.getShareId().toString(),ImapFolderData.class);
					id = new Identity(Identity.TYPE_AUTO,udata.getEmail().getAddress(), udata.getDisplayName(), folder.getInstance(), false, ifd.useMyPersonalInfo);
					id.setOriginPid(opid);
					idents.add(id);
				}
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return idents;
	}
	
}
