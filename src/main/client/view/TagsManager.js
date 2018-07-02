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
		'Sonicle.webtop.mail.model.Tag',
		'Sonicle.webtop.mail.view.TagEditor'
	],
	
	dockableConfig: {
		title: '{tagsmgr.tit}',
		//iconCls: 'wtmail-icon-format-quickpart-xs',
		width: 400,
		height: 300
	},
	promptConfirm: false,
	full: true,
	modal: true,
	
	mys: null,
	fn: null,
	scope: null,

	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			tbar: [
				Ext.create('Ext.button.Button', {
					tooltip: WT.res('act-add.lbl'),
					iconCls: 'wt-icon-add-xs',
					handler: function() {
						WT.createView(me.mys.ID,'view.TagEditor',{
							viewCfg: {
								mys: me.mys,
								listeners: {
									viewok: function(s,description,color) {
										if (me.gpTags.getStore().findRecord("tagId",description) || me.gpTags.getStore().findRecord("description",description)) {
											WT.error(me.mys.res("tagsmgr.error.tageidalreadypresent"));
										} else {
											me.gpTags.getStore().add({ tagId: description, description: description, color: color });
											me.mys.reloadTags();
										}
									}
								}
							}
						}).show();
					}
				}),
				Ext.create('Ext.button.Button', {
					tooltip: WT.res('act-edit.lbl'),
					iconCls: 'wt-icon-edit-xs',
					handler: function() {
						if (me.gpTags.getSelectionModel().hasSelection()) {
							var sel = me.gpTags.getSelectionModel().getSelection(),
								rec=sel[0];
							WT.createView(me.mys.ID,'view.TagEditor',{
								viewCfg: {
									mys: me.mys,
									description: rec.get("description"),
									color: rec.get("color"),
									listeners: {
										viewok: function(s,description,color) {
											me.wait();
											rec.set({ description: description, color: color });
											me.mys.reloadTags();
										}
									}
								}
							}).show();
						}
					}
				}),
				Ext.create('Ext.button.Button', {
					tooltip: WT.res('act-remove.lbl'),
					iconCls: 'wt-icon-remove-xs',
					handler: function() {
						if (me.gpTags.getSelectionModel().hasSelection()) {
							WT.confirm(WT.res('confirm.delete'), function(bid) {
								if(bid == 'yes') {
									var sel = me.gpTags.getSelectionModel().getSelection();
									if (sel.length>0) {
										me.gpTags.getStore().remove(sel[0]);
										me.mys.reloadTags();
									}
								}
							});
						}
					}
				})
			]
		});
		
		me.callParent(arguments);
		
		me.gpTags = Ext.create({
			xtype: 'gridpanel',
			region: 'center',
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
			columns: [
				{
					dataIndex: 'description',
					header: '',
					flex: 1,
					renderer: function(value,metadata,record,rowIndex,colIndex,store) {
						return "<span style='color: "+record.get("color")+"'>"+record.get("description")+"</span>";
						
					}
}
			]
		});
		
		me.add(me.gpTags);
	},
	
	addTag: function(description,color) {
		
	}
	
});
