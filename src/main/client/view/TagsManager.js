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

Ext.define('Sonicle.webtop.mail.view.TagsManager', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.grid.column.Action',
		'Sonicle.webtop.mail.model.Tag'
	],
	uses: [
		'Sonicle.webtop.mail.view.TagEditor'
	],
	
	dockableConfig: {
		title: '{tagsmgr.tit}',
		width: 400,
		height: 300,
		modal: true
	},
	promptConfirm: false,

	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			tbar: [
				'->',
				Ext.create('Ext.button.Button', {
					tooltip: WT.res('act-add.lbl'),
					iconCls: 'wt-icon-add-xs',
					handler: function() {
						me.addTagUI();
					}
				})
			]
		});
		
		Ext.apply(me, {
			buttons: [{
				text: WT.res('act-ok.lbl'),
				handler: me.onOkClick,
				scope: me
			}]
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'gridpanel',
			reference: 'gp',
			border: false,
			loadMask: {msg: WT.res('loading')},
			selType: 'sorowmodel',
			store: {
				autoLoad: true,
				autoSync: true,
				model: 'Sonicle.webtop.mail.model.Tag',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageTags','data'),
				listeners: {
					write: function(s,op) {
						me.unwait();
					}
				}
			},
			columns: [{
				dataIndex: 'description',
				header: '',
				flex: 1,
				renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					return "<span style='color: "+record.get("color")+"'>"+record.get("description")+"</span>";

				}
			}, {
				xtype: 'soactioncolumn',
				items: [{
					iconCls: 'fa fa-edit',
					tooltip: WT.res('act-edit.lbl'),
					handler: function(g, ridx) {
						var rec = g.getStore().getAt(ridx);
						me.editTagUI(rec);
					}
				}, {
					iconCls: 'fa fa-trash-o',
					tooltip: WT.res('act-remove.lbl'),
					handler: function(g, ridx) {
						var rec = g.getStore().getAt(ridx);
						me.removeTagUI(rec);
					}
				}]
			}]
		});
	},
	
	onOkClick: function() {
		this.closeView(false);
	},
	
	addTagUI: function() {
		var me = this,
				gp = me.lref('gp'),
				sto = gp.getStore();
		WT.createView(me.mys.ID, 'view.TagEditor', {
			swapReturn: true,
			viewCfg: {
				mys: me.mys,
				listeners: {
					viewok: function(s,description,color) {
						if (sto.findRecord('tagId', description) || sto.findRecord('description', description)) {
							WT.error(me.mys.res('tagsmgr.error.tageidalreadypresent'));
						} else {
							sto.add({tagId: description, description: description, color: color});
							me.mys.reloadTags();
						}
					}
				}
			}
		}).showView();
	},
	
	editTagUI: function(rec) {
		var me = this;
		WT.createView(me.mys.ID,'view.TagEditor', {
			swapReturn: true,
			viewCfg: {
				mys: me.mys,
				description: rec.get('description'),
				color: rec.get('color'),
				listeners: {
					viewok: function(s,description,color) {
						me.wait();
						rec.set({description: description, color: color});
						me.mys.reloadTags();
					}
				}
			}
		}).showView();
	},
	
	removeTagUI: function(rec) {
		var me = this,
				gp = me.lref('gp');
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if (bid === 'yes') {
				gp.getStore().remove(rec);
				me.mys.reloadTags();
			}
		});
	}
});
