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

Ext.define('Sonicle.webtop.mail.ImapTree', {
	extend: 'Sonicle.webtop.mail.SimpleImapTree',
	
	plugins: {
        ptype: 'cellediting',
		pluginId: 'cellediting',
        clicksToEdit: 2,
		listeners: {
			'edit': function(ed,ctx) { 
				Ext.defer(function() {
					ctx.record.getTreeStore().load({ node: ctx.record });
				},2000);
			}
		}
    },	

	constructor: function(cfg) {
		var me = this;
		
		Ext.apply(cfg,{
			viewConfig: {
				plugins: { 
					ptype: 'imaptreeviewdragdrop' ,
					moveFolder: function(src,dst) {
						cfg.mys.moveFolder(me.acct,src,dst);
					},
					moveMessages: function(data,dst) {
						data.view.grid.moveSelection(data.srcAccount,data.srcFolder,me.acct,dst,data.records,true);
					},
					copyMessages: function(data,dst) {
						data.view.grid.copySelection(data.srcAccount,data.srcFolder,me.acct,dst,data.records,true);
					},
					copyAttachment: function(data,dst) {
						cfg.mys.copyAttachment(data.params.acct,data.params.folder,me.acct,dst,data.params.idmessage,data.params.idattach);
					}
				},
				markDirty: false,
				loadMask: true,
				animate: true
			},
			
			store: Ext.create('Ext.data.TreeStore', {
				model: 'Sonicle.webtop.mail.model.ImapTreeModel',
				proxy: WTF.proxy(cfg.mys.ID,'GetImapTree'),
				root: {
					id: '/',
					text: WT.getVar("userDisplayName"),
					folder: WT.getVar("userDisplayName"),
					unread: 0,
					iconCls: 'wtmail-icon-mailAccount',
					expanded: true
				},
				rootVisible: false,
				acct: cfg.acct
			})
		});
		
		me.callParent([cfg]);
		if (false !== me.statefulFolders) me.setupStateful();
	},
	
	startEdit: function(record,c) {
		var me = this;
		me.getPlugin('cellediting').startEdit(record, me.getView().ownerCt.getColumnManager().getHeaderAtIndex(c));
	},
	
	//Stateful implementations
	setupStateful: function() {
		var me = this;
		me.expandedNodes = {};
		me.stateEvents = ['expandnode', 'collapsenode'];
		me.getState = function() {
			return { expandedNodes: me.expandedNodes };
		};
		me.on({
			scope: me,
			render: me.onStatefulRender,
			beforeitemexpand: me.beforeStatefulItemExpand,
			beforeitemcollapse: me.beforeStatefulItemCollapse
		});										  
		me.setStateful(true);
	},
	
	onStatefulRender: function() {
		var me = this,
				sto = me.store;
		if (sto) {
			if (sto.isLoaded()) {
				me.restoreFoldersState();
			} else {
				sto.on('load', function() {
					me.restoreFoldersState();
				}, me, {single: true});
			}
		}
	},
	
	restoreFoldersState: function() {
		var me = this,
				state = Ext.state.Manager.get(me.stateId);
		if (state && state.expandedNodes) {
			me.expandedNodes = state.expandedNodes;
			me.expandNodes(Ext.Array.sort(Ext.Object.getAllKeys(state.expandedNodes)), function() {
				me.fireEvent('foldersstaterestored', me, me.expandedNodes);
			});
		} else {
			me.fireEvent('foldersstaterestored', me, null);
		}
	},
	
	expandNodes: function(nodeIds, callback, idx) {
		if (idx === undefined) idx = 0;
		var me = this, node;
		if (idx < nodeIds.length) {
			me.restoringState = true;
			node = me.store.getById(nodeIds[idx]);
			if (node) {
				me.expandNode(node, false, function() {
					me.expandNodes(nodeIds, callback, idx+1);
				});
			} else {
				me.expandNodes(nodeIds, callback, idx+1);
			}
		} else {
			me.restoringState = false;
			Ext.callback(callback, me);
		}
	},
	
	beforeStatefulItemExpand:function(n) {
		var me = this;
		if (!me.restoringState && n.id) {
			me.expandedNodes[n.id] = n.getPath();
			me.saveState();
		}
	},
	
	beforeStatefulItemCollapse:function(n) {
		var me = this;
		if (n.id) {
			delete(me.expandedNodes[n.id]);
			n.cascade(function(child) {
				if (child.id) {
					delete(me.expandedNodes[child.id]);
				}
			}, this);
			me.saveState();
		}
	}
});
