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
	extend: 'WT.sdk.UserOptionsView',
	requires: [
	],
	
	viewModel: {
		formulas: {
			receipt: WTF.checkboxBind('record', 'receipt'),
			scanAll: WTF.checkboxBind('record', 'scanAll'),
			sharedSeen: WTF.checkboxBind('record', 'sharedSeen'),
			manualSeen: WTF.checkboxBind('record', 'manualSeen')
		}
	},
	
		
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			xtype: 'wtopttabsection',
			title: WT.res(me.ID, 'opts.main.tit'),
			items: [
			{
				xtype: 'textfield',
				bind: '{record.replyTo}',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-replyTo.lbl'),
				width: 400,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, WTF.lookupCombo('id', 'desc', {
				bind: '{record.protocol}',
				store: Ext.create('WT.store.MailboxProtocols', {
					autoLoad: true
				}),
				fieldLabel: WT.res(me.ID, 'opts.main.fld-protocol.lbl'),
				width: 220,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}), {
				xtype: 'textfield',
				bind: '{record.host}',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-host.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'numberfield',
				bind: '{record.port}',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-port.lbl'),
				width: 200,
				needReload: true,
				hideTrigger: true,
				keyNavEnabled: false,
				mouseWheelEnabled: false,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: '{record.username}',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-username.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: '{record.password}',
				inputType: 'password',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-password.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: '{record.folderPrefix}',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-folderPrefix.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: '{record.folderSent}',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-folderSent.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: '{record.folderDrafts}',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-folderDrafts.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: '{record.folderTrash}',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-folderTrash.lbl'),
				width: 400,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'textfield',
				bind: '{record.folderSpam}',
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
					store: Ext.create('WT.store.TxtFont', {
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
						model: 'Sonicle.webtop.mail.model.Identity',
						proxy: WTF.apiProxy(me.ID, 'ListIdentities', 'identities', {
							extraParams: {
								id: me.profileId,
								options: true
							}
						})
					},
					columns: [{
						dataIndex: 'displayName',
						header: WT.res(me.ID, 'opts.ident.displayName.lbl'),
						flex: 1
					}, {
						dataIndex: 'email',
						header: WT.res(me.ID, 'opts.ident.email.lbl'),
						flex: 1
					}, {
						dataIndex: 'mainFolder',
						header: WT.res(me.ID, 'opts.ident.mainFolder.lbl'),
						flex: 1
					}, {
						dataIndex: 'fax',
						header: WT.res(me.ID, 'opts.ident.fax.lbl'),
						flex: 1
					}, {
						dataIndex: 'useMyPersonalInfos',
						header: WT.res(me.ID, 'opts.ident.useMyPersonalInfos.lbl'),
						flex: 1
					}],
					tbar: [
						me.addAction('addIdentity', {
							text: WT.res('act-add.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-add-xs',
							handler: function() {
								me.addIdentity();
							}
						}),
						me.addAction('deleteIdentity', {
							text: WT.res('act-delete.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-delete-xs',
							handler: function() {
								var sm = me.lref('gpidents').getSelectionModel();
								me.deleteIdentity(sm.getSelection());
							},
							disabled: true
						})
					],
					listeners: {
						selectionchange: function(s,recs) {
							me.getAction('deleteIdentity').setDisabled(!recs.length);
						}
					}
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
					xtype: 'textfield',
					bind: '{record.defaultFolder}',
					fieldLabel: WT.res(me.ID, 'opts.main.fld-defaultFolder.lbl'),
					width: 400,
					needReload: true,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}, {
					xtype: 'checkbox',
					bind: '{manualSeen}',
					fieldLabel: WT.res(me.ID, 'opts.adv.fld-manualSeen.lbl'),
					width: 100,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
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
