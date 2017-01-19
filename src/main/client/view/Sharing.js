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

Ext.define('Sonicle.webtop.mail.view.Sharing', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.webtop.mail.model.Sharing'
	],
	
	modelName: 'Sonicle.webtop.mail.model.Sharing',
	fieldTitle: 'description',
	
	dockableConfig: {
		title: '{sharing.tit@com.sonicle.webtop.core}',
		width: 600,
		height: 500
	},
	//promptConfirm: false,
	full: true,
	
	//dirty: false,
	
	fullRights: 'lrswipkxtea',

	constructor: function(cfg) {
		var me = this;
		me.methodName = Ext.id(null, 'method-');
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foMethod: WTF.radioGroupBind('record', 'method', me.methodName)
		});
	},
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			tbar: [
				'-',' ',me.mys.res('sharing.apply-to.lbl')+': ',
				{
					xtype: 'radiogroup',
					bind: {
						value: '{foMethod}'
					},
					layout: 'hbox',
					defaults: {
						name: me.methodName,
						margin: '0 20 0 0',
						listeners: {
							afterrender: function() {
								this.getEl().on('click', function() {
									//me.dirty=true;
								});
							}
						}
					},
					items: [
						{
							boxLabel: me.mys.res('sharing.chk-this.lbl'),
							inputValue: 'this'
						},
						{
							boxLabel: me.mys.res('sharing.chk-branch.lbl'),
							inputValue: 'branch'
						},
						{
							boxLabel: me.mys.res('sharing.chk-all.lbl'),
							inputValue: 'all'
						}
					]
				}
			]
		});
		
		me.callParent(arguments);
		
		me.add({
			region: 'north',
			xtype: 'wtfieldspanel',
			height: 60,
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'label',
					height: 24,
					bind: {
						text: me.mys.res("column-folder")+': {record.id}'
					}
				},
				WTF.localCombo('id', 'desc', {
					xtype: 'sosourcecombo',
					reference: 'fldrole',
					store: {
						autoLoad: true,
						model: 'WTA.model.RoleLkp',
						proxy: WTF.proxy(WT.ID, 'LookupDomainRoles', 'roles', {
							extraParams: {
								groups: false,
								roles: false
							}
						})
					},
					sourceField: 'sourceLabel',
					emptyText: WT.res('sharing.fld-role.lbl'),
					listeners: {
						select: function(s, rec) {
							var model = me.addRights(rec.get('id'),rec.get('desc'));
							me.lref('gprights').setSelection(model);
							s.setValue(null);
							//me.dirty=true;
						}
					}
				})]
		});
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			layout: 'fit',
			items: [{
				xtype: 'gridpanel',
				reference: 'gprights',
				bind: {
					store: '{record.rights}'
				},
				border: true,
				columns: [{
					dataIndex: 'roleDescription',
					header: WT.res('sharing.gp-rights.role.lbl'),
					flex: 2
				},{
					xtype: 'checkcolumn',
					header: me.mys.res('sharing.rights-use-my-personal-info.lbl'),
					dataIndex: 'useMyPersonalInfo',
					flex: 1
				},{
					xtype: 'checkcolumn',
					header: me.mys.res('sharing.rights-force-my-mailcard.lbl'),
					dataIndex: 'forceMyMailcard',
					flex: 1
				}],
				tbar: [
					me.addAction('deleteRights', {
						text: WT.res('act-delete.lbl'),
						tooltip: null,
						iconCls: 'wt-icon-delete-xs',
						handler: function() {
							var sm = me.lref('gprights').getSelectionModel();
							me.deleteRights(sm.getSelection());
						},
						disabled: true
					})
				],
				listeners: {
					selectionchange: function(s,recs) {
						if (recs.length==1) {
							var rights="",
								cbo=me.lref('cboRights');
							for(var i=0;i<me.fullRights.length;++i) {
								var c=me.fullRights.charAt(i);
								if (recs[0].get(c)) rights+=c;
							}
							if (rights.length>0 && cbo.store.findExact('rights',rights)>=0) cbo.setValue(rights);
							else cbo.setValue("");
						}
						me.getAction('deleteRights').setDisabled(!recs.length);
						me.lref('permspanel').setDisabled(!recs.length);
					}
				}
			}]
		});
		me.add({
			region: 'south',
			xtype: 'panel',
			reference: 'permspanel',
			height: 170,
			layout: 'border',
			items: [
				{
					xtype: 'toolbar',
					region: 'north',
					items: [{
						xtype: 'combo',
						reference: "cboRights",
						fieldLabel: me.mys.res("sharing.fld-rights.lbl"),
						store: new Ext.data.SimpleStore({
							fields: ['rights','desc'],
							data: [
								[me.fullRights,me.mys.res("sharing.rights-full")],
								['lrs',me.mys.res("sharing.rights-ro")],
								['',me.mys.res("sharing.rights-custom")],
							]
						}),
						editable: false,
						mode: 'local',
						width:300,
						listWidth: 300,
						displayField: 'desc',
						triggerAction: 'all',
						valueField: 'rights',
						listeners: {
							select: function(c,r,i) {
								var id=r.get('rights');
								if (id.length>0) {
									var rec = me.lref('gprights').getSelection()[0];
									for(var i=0;i<me.fullRights.length;++i) {
										var c=me.fullRights.charAt(i);
										rec.set(c,id.indexOf(c)>=0);
									}
								} 
								//me.dirty=true;
							}
						}
					},'->',{
						xtype: 'checkbox',
						boxLabel: me.mys.res('sharing.fld-advanced.lbl'),
						checked: false,
						width: 200,
						listeners: {
							change: {
								fn: function(c,ov,nv,eopts) {
									var v=c.getValue(),
										p=me.lref("elementsperms");
									p.setHidden(!v);
									if (v) p.setDisabled(false);
								}
							}
						}
					}]
				},
				{
					xtype: 'panel',
					region: 'center',
					layout: 'fit',
					items: [
						{
							xtype: 'wtfieldspanel',
							reference: 'elementsperms',
							disabled: true,
							hidden: true,
							layout: {
								type: 'table',
								columns: 3
							},
							defaults: {
								listeners: {
									afterrender: function() {
										this.getEl().on('click', function() {
											me.lref('cboRights').setValue('');
											//me.dirty=true;
										});
									}
								}
							},
							items: [{
								xtype: 'checkbox',
								bind: '{gprights.selection.l}',
								boxLabel: me.mys.res('sharing.fld-l.lbl'),
								width: 200
							}, {
								xtype: 'checkbox',
								bind: '{gprights.selection.r}',
								boxLabel: me.mys.res('sharing.fld-r.lbl'),
								width: 200
							}, {
								xtype: 'checkbox',
								bind: '{gprights.selection.s}',
								boxLabel: me.mys.res('sharing.fld-s.lbl'),
								width: 200
							}, {
								xtype: 'checkbox',
								bind: '{gprights.selection.w}',
								boxLabel: me.mys.res('sharing.fld-w.lbl'),
								width: 200
							}, {
								xtype: 'checkbox',
								bind: '{gprights.selection.i}',
								boxLabel: me.mys.res('sharing.fld-i.lbl'),
								width: 200
							}, {
								xtype: 'checkbox',
								bind: '{gprights.selection.p}',
								boxLabel: me.mys.res('sharing.fld-p.lbl'),
								width: 200
							}, {
								xtype: 'checkbox',
								bind: '{gprights.selection.k}',
								boxLabel: me.mys.res('sharing.fld-k.lbl'),
								width: 200
							}, {
								xtype: 'checkbox',
								bind: '{gprights.selection.a}',
								boxLabel: me.mys.res('sharing.fld-a.lbl'),
								width: 200
							}, {
								xtype: 'checkbox',
								bind: '{gprights.selection.x}',
								boxLabel: me.mys.res('sharing.fld-x.lbl'),
								width: 200
							}, {
								xtype: 'checkbox',
								bind: '{gprights.selection.t}',
								boxLabel: me.mys.res('sharing.fld-t.lbl'),
								width: 200
							}, /*{
								xtype: 'checkbox',
								bind: '{gprights.selection.n}',
								boxLabel: me.mys.res('sharing.fld-n.lbl'),
								width: 200
							},*/ {
								xtype: 'checkbox',
								bind: '{gprights.selection.e}',
								boxLabel: me.mys.res('sharing.fld-e.lbl'),
								width: 200
							}]
						}
					]
				}
			]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this;
		me.lref('fldrole').focus(true);
	},
	
	addRights: function(roleUid,roleDescription) {
		var me = this,
				grid = me.lref('gprights'),
				sto = grid.getStore(),
				rec;
		
		if(sto.indexOfId(roleUid) !== -1) return null;
		rec = sto.add({
			roleUid: roleUid,
			roleDescription: roleDescription,
			folderRead: true
		})[0];
		return rec;
	},
	
	deleteRights: function(rec) {
		var me = this,
				grid = me.lref('gprights');
		
		grid.getStore().remove(rec);
	}
	
	
});
