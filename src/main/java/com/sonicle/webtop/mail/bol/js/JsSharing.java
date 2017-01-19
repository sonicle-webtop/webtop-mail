/*
 * webtop-calendar is a WebTop Service developed by Sonicle S.r.l.
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
 * @author malbinola
 */
public class JsSharing {
	public String id;
	public String description;
	public String method;
	public List<SharingRights> rights;
	
	public JsSharing() {}
	
	public JsSharing(String id, String description, String method, List<SharingRights> rights) {
		this.id = id;
		this.description = description;
		this.method = method;
		this.rights = rights;
	}
	
	public boolean hasRoleUid(String roleUid) {
		boolean found=false;
		for(SharingRights sr: rights) {
			if (sr.roleUid.equals(roleUid)) {
				found=true;
				break;
			}
		}
		return found;
	}
	
	public boolean hasImapId(String imapId) {
		boolean found=false;
		for(SharingRights sr: rights) {
			if (sr.imapId.equals(imapId)) {
				found=true;
				break;
			}
		}
		return found;
	}
	
	public static class SharingRights {
		public String _fk;
		public String roleUid;
		public String roleDescription;
		public String imapId;
		public Boolean shareIdentity;
		public Boolean forceMailcard;
		public Boolean l;
		public Boolean r;
		public Boolean s;
		public Boolean w;
		public Boolean i;
		public Boolean p;
		public Boolean k;
		public Boolean a;
		public Boolean x;
		public Boolean t;
		public Boolean n;
		public Boolean e;
		
		public SharingRights() {}
		
		public SharingRights(String _fk, String ruid, String rdesc, String imapId, boolean shareIdentity, boolean forceMailcard, boolean l, boolean r, boolean s, boolean w, boolean i, boolean p, boolean k, boolean a, boolean x, boolean t, boolean n, boolean e) {
			this._fk = _fk;
			roleUid = ruid;
			roleDescription = rdesc;
			this.imapId=imapId;
			this.shareIdentity=shareIdentity;
			this.forceMailcard=forceMailcard;
			this.l=l;
			this.r=r;
			this.s=s;
			this.w=w;
			this.i=i;
			this.p=p;
			this.k=k;
			this.a=a;
			this.x=x;
			this.t=t;
			this.n=n;
			this.e=e;
		}
		
		public String toString() {
			StringBuffer sb=new StringBuffer();
			if (l) sb.append('l');
			if (r) sb.append('r');
			if (s) sb.append('s');
			if (w) sb.append('w');
			if (i) sb.append('i');
			if (p) sb.append('p');
			if (k) sb.append('k');
			if (a) sb.append('a');
			if (x) sb.append('x');
			if (t) sb.append('t');
			if (n) sb.append('n');
			if (e) sb.append('e');
			return sb.toString();
		}
	}
}
