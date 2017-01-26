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
Ext.define('Sonicle.webtop.mail.view.UserOptions', {
	extend: 'WTA.sdk.UserOptionsView',
	requires: [
		'Sonicle.webtop.mail.model.ServiceVars',
		'Sonicle.webtop.mail.model.Identity'
	],
	
	viewModel: {
		formulas: {
			receipt: WTF.checkboxBind('record', 'receipt'),
			priority: WTF.checkboxBind('record', 'priority'),
			scanAll: WTF.checkboxBind('record', 'scanAll'),
			sharedSeen: WTF.checkboxBind('record', 'sharedSeen'),
			manualSeen: WTF.checkboxBind('record', 'manualSeen'),
			canChangeAccountSettings: function(get) {
				return get("record.canChangeAccountSettings");
			}
		}
	},
	
		
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			xtype: 'wtopttabsection',
			title: WT.res(me.ID, 'opts.main.tit'),
			items: [
/*			{
				xtype: 'textfield',
				bind: '{record.replyTo}',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-replyTo.lbl'),
				width: 400,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			},*/
			WTF.lookupCombo('id', 'desc', {
				bind: {
					value: '{record.protocol}',
					disabled: '{!canChangeAccountSettings}'
				},
				store: Ext.create('WTA.store.MailboxProtocols', {
					autoLoad: true
				}),
				fieldLabel: WT.res(me.ID, 'opts.main.fld-protocol.lbl'),
				width: 220,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}), {
				xtype: 'textfield',
				bind: {
					value: '{record.host}',
					disabled: '{!canChangeAccountSettings}'
				},
				fieldLabel: WT.res(me.ID, 'opts.main.fld-host.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'numberfield',
				bind: {
					value: '{record.port}',
					disabled: '{!canChangeAccountSettings}'
				},
				fieldLabel: WT.res(me.ID, 'opts.main.fld-port.lbl'),
				width: 200,
				needReload: true,
				hideTrigger: true,
				keyNavEnabled: false,
				mouseWheelEnabled: false,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.username}',
					disabled: '{!canChangeAccountSettings}'
				},
				plugins: 'sonoautocomplete',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-username.lbl'),
				width: 400,
				needLogin: true,
				emptyText: WT.res(me.ID, 'opts.main.fld-username-empty.lbl'),
				submitEmptyText: false,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'sopasswordfield',
				bind: {
					value: '{record.password}',
					disabled: '{!canChangeAccountSettings}'
				},
				plugins: 'sonoautocomplete',
				//inputType: 'password',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-password.lbl'),
				width: 400,
				needLogin: true,
				emptyText: WT.res(me.ID, 'opts.main.fld-password-empty.lbl'),
				submitEmptyText: false,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.folderPrefix}',
					disabled: '{!canChangeAccountSettings}'
				},
				fieldLabel: WT.res(me.ID, 'opts.main.fld-folderPrefix.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.folderSent}',
					disabled: '{!canChangeAccountSettings}'
				},
				fieldLabel: WT.res(me.ID, 'opts.main.fld-folderSent.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.folderDrafts}',
					disabled: '{!canChangeAccountSettings}'
				},
				fieldLabel: WT.res(me.ID, 'opts.main.fld-folderDrafts.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.folderTrash}',
					disabled: '{!canChangeAccountSettings}'
				},
				fieldLabel: WT.res(me.ID, 'opts.main.fld-folderTrash.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.folderSpam}',
					disabled: '{!canChangeAccountSettings}'
				},
				fieldLabel: WT.res(me.ID, 'opts.main.fld-folderSpam.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, WTF.lookupCombo('id', 'desc', {
				bind: '{record.sharedSort}',
				store: Ext.create('Sonicle.webtop.mail.store.SharedSort', {
					autoLoad: true
				}),
				fieldLabel: WT.res(me.ID, 'opts.main.fld-sharedSort.lbl'),
				width: 300,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			})]
		});
		
		me.add({
			xtype: 'wtopttabsection',
			title: WT.res(me.ID, 'opts.editing.tit'),
			items: [
				WTF.lookupCombo('id', 'desc', {
					bind: '{record.font}',
					store: Ext.create('WTA.store.TxtFont', {
						autoLoad: true
					}),
					fieldLabel: WT.res('word.font'),
					width: 400,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}), {
					xtype: 'numberfield',
					bind: '{record.fontSize}',
					fieldLabel: WT.res('word.size'),
					width: 200,
					hideTrigger: true,
					keyNavEnabled: false,
					mouseWheelEnabled: false,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}, {
					xtype: 'checkbox',
					bind: '{receipt}',
					fieldLabel: WT.res(me.ID, 'opts.editing.fld-receipt.lbl'),
					width: 100,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'checkbox',
					bind: '{priority}',
					fieldLabel: WT.res(me.ID, 'opts.editing.fld-priority.lbl'),
					width: 100,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'fieldcontainer',
					fieldLabel: WT.res(me.ID, 'opts.editing.fld-emaildomainMailcard.lbl'),
					layout: 'hbox',
					items: [ 
						{ xtype: 'button', text: WT.res('act-edit.lbl') },
						{ xtype: 'displayfield', text: '', width: 10 },
						{ xtype: 'button', text: WT.res('act-delete.lbl')}
					]
				}, {
					xtype: 'fieldcontainer',
					fieldLabel: WT.res(me.ID, 'opts.editing.fld-identity0Mailcard.lbl'),
					layout: 'hbox',
					items: [ 
						{ xtype: 'button', text: WT.res('act-edit.lbl') },
						{ xtype: 'displayfield', text: '', width: 10 },
						{ xtype: 'button', text: WT.res('act-delete.lbl')}
					]
				}
			]		
		});
		
		me.add({
			xtype: 'wtopttabsection',
			title: WT.res(me.ID, 'opts.ident.tit'),
			layout: 'fit',
			items: [{
				xtype: 'wtpanel',
				layout: 'border',
				items: [{					
					region: 'center',
					xtype: 'gridpanel',
					reference: 'gpidents',
					store: {
						autoLoad: true,
						autoSync: true,
						model: 'Sonicle.webtop.mail.model.Identity',
						proxy: WTF.apiProxy(me.ID, 'ManageIdentities', 'identities', {
							extraParams: {
								id: me.profileId,
								type: 'user',
								options: true
							}
						}),
						listeners: {
							beforesync: function() {
								me.needReload=true;
							}
						}
					},
					plugins: {
						ptype: 'rowediting',
						clicksToEdit: 1,
						pluginId: 'gpidentseditor'
					},
					columns: [{
						dataIndex: 'displayName',
						header: WT.res(me.ID, 'opts.ident.displayName.lbl'),
						editor: { xtype: 'textfield' },
						flex: 2
					}, {
						dataIndex: 'email',
						header: WT.res(me.ID, 'opts.ident.email.lbl'),
						editor: { xtype: 'textfield' },
						flex: 2
					}, {
						dataIndex: 'mainFolder',
						header: WT.res(me.ID, 'opts.ident.mainFolder.lbl'),
						editor: {
							xtype: 'sotreecombo',
							store: Ext.create('Ext.data.TreeStore', {
								model: 'Sonicle.webtop.mail.model.ImapTreeModel',
								proxy: WTF.proxy(me.ID,'GetImapTree'),
								root: {
									text: 'Imap Tree',
									expanded: true
								},
								rootVisible: false
							})
						},
						flex: 2
					}, {
						xtype: 'checkcolumn',
						dataIndex: 'fax',
						header: WT.res(me.ID, 'opts.ident.fax.lbl'),
						editor: { xtype: 'checkbox' },
						flex: 1
					}],
					tbar: [
						me.addAction('addIdentity', {
							text: WT.res('act-add.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-add-xs',
							handler: function() {
								var g=me.lref('gpidents'),
									r=g.store.add({
										type: 'user',
										email: '',
										displayName: '',
										mainFolder: '',
										fax: false
									})[0];
								g.getPlugin('gpidentseditor').startEdit(r);
							}
						}),
						me.addAction('deleteIdentity', {
							text: WT.res('act-delete.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-delete-xs',
							handler: function() {
								var sel = me.lref('gpidents').getSelection();
								if (sel.length>0)
									me.lref('gpidents').store.remove(sel[0]);
							},
							disabled: true
						})
					],
					listeners: {
						selectionchange: function(s,recs) {
							me.getAction('deleteIdentity').setDisabled(!recs.length);
						}
					}
				},{					
					region: 'south',
					xtype: 'gridpanel',
					reference: 'gpautoidents',
					height: 200,
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.mail.model.Identity',
						proxy: WTF.apiProxy(me.ID, 'ListIdentities', 'identities', {
							extraParams: {
								id: me.profileId,
								type: 'auto',
								options: true
							}
						})
					},
					columns: [{
						dataIndex: 'displayName',
						header: WT.res(me.ID, 'opts.ident.displayName.lbl'),
						flex: 2
					}, {
						dataIndex: 'email',
						header: WT.res(me.ID, 'opts.ident.email.lbl'),
						flex: 2
					}, {
						xtype: 'checkcolumn',
						dataIndex: 'forceMailcard',
						header: WT.res(me.ID, 'opts.ident.force-mailcard.lbl'),
						flex: 1,
						disabled: true
					}],
					tbar: [
						WT.res(me.ID, 'opts.autoident.tit')
					]
				}]	
			}]
		});
		
		if (WT.isPermitted(me.ID,'DOCUMENT_MANAGEMENT','ACCESS')) {
			me.add({
				xtype: 'wtopttabsection',
				title: WT.res(me.ID, 'opts.arch.tit'),
				items: [
				]
			});
		}
		
		me.add({
			xtype: 'wtopttabsection',
			title: WT.res(me.ID, 'opts.adv.tit'),
			items: [
				{
					xtype: 'checkbox',
					bind: '{scanAll}',
					fieldLabel: WT.res(me.ID, 'opts.adv.fld-scanAll.lbl'),
					width: 100,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'numberfield',
					bind: '{record.scanSeconds}',
					fieldLabel: WT.res(me.ID, 'opts.adv.fld-scanSeconds.lbl'),
					width: 200,
					hideTrigger: true,
					keyNavEnabled: false,
					mouseWheelEnabled: false,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}, {
					xtype: 'fieldcontainer',
					fieldLabel: WT.res(me.ID, 'opts.adv.fld-scanCycles.lbl'),
					layout: 'hbox',
					items: [ 
						{
							xtype: 'sliderwidget',
							bind: '{record.scanCycles}',
							width: 100,
							increment: 1,
							keyIncrement: 1,
							minValue: 3,
							maxValue: 30,
							listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
						},
						{ xtype: 'displayfield', text: '', width: 10 },
						{
							xtype: 'numberfield',
							bind: '{record.scanSecondsOthers}',
							fieldLabel: ' ',
							width: 150,
							hideTrigger: true,
							keyNavEnabled: false,
							mouseWheelEnabled: false,
							disabled: true
						}
					]
				}, {
					xtype: 'checkbox',
					bind: '{sharedSeen}',
					fieldLabel: WT.res(me.ID, 'opts.adv.fld-sharedSeen.lbl'),
					width: 100,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'checkbox',
					bind: '{manualSeen}',
					fieldLabel: WT.res(me.ID, 'opts.adv.fld-manualSeen.lbl'),
					width: 100,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'textfield',
					bind: '{record.defaultFolder}',
					fieldLabel: WT.res(me.ID, 'opts.adv.fld-defaultFolder.lbl'),
					width: 400,
					needReload: true,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}
			]
		});
		
		if (WT.isPermitted(me.ID,'MAIL_WORKFLOW','ACCESS')) {
			me.add({
				xtype: 'wtopttabsection',
				title: WT.res(me.ID, 'opts.wkf.tit'),
				items: [
				]
			});
		}
	}
});
