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

Ext.define('Sonicle.webtop.mail.ArchiveTree', {
	extend: 'Sonicle.webtop.mail.SimpleImapTree',
	
	constructor: function(cfg) {
		var me = this;
		
		Ext.apply(cfg,{
			viewConfig: {
/*				plugins: { 
					ptype: 'imaptreeviewdragdrop' ,
					moveFolder: function(src,dst) {
						cfg.mys.moveFolder(src,dst);
					},
					moveMessages: function(data,dst) {
						data.view.grid.moveSelection(data.srcFolder,dst,data.records,true);
					},
					copyMessages: function(data,dst) {
						data.view.grid.copySelection(data.srcFolder,dst,data.records,true);
					},
					copyAttachment: function(data,dst) {
						cfg.mys.copyAttachment(data.params.folder,dst,data.params.idmessage,data.params.idattach);
					}
				},*/
				markDirty: false,
				loadMask: true,
				animate: true
			},
			
			store: Ext.create('Ext.data.TreeStore', {
				model: 'Sonicle.webtop.mail.model.ImapTreeModel',
				proxy: WTF.proxy(cfg.mys.ID,'GetArchiveTree'),
				root: {
					id: '/',
					text: 'Archive Tree',
					folder: cfg.mys.res("archive.external.lbl")+" ("+WT.getVar("userDisplayName")+")",
					unread: 0,
					iconCls: 'wtmail-icon-archive',
					expanded: false
				},
				rootVisible: false,
				acct: cfg.acct
			})
		});

		me.callParent([cfg]);
		
	}
	
	
});

