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
		'Sonicle.form.Separator',
		'Sonicle.form.field.Palette',
		'WTA.store.Fonts',
		'WTA.store.MailboxProtocols',
		'Sonicle.webtop.mail.model.ServiceVars',
		'Sonicle.webtop.mail.model.Identity',
		'Sonicle.webtop.mail.model.GridExternalAccount',
		'Sonicle.webtop.mail.store.ArchiveMode',
		'Sonicle.webtop.mail.store.EditingFormat',
		'Sonicle.webtop.mail.store.ReadReceiptConfirmation',
		'Sonicle.webtop.mail.store.ViewMode',
		'Sonicle.webtop.mail.view.ExternalAccount',
		'Sonicle.webtop.mail.model.ExternalAccountProvider'
	],
	uses: [
		'Sonicle.form.field.tinymce.tool.FontSelect',
		'Sonicle.webtop.mail.view.MailcardEditor'
	],
	
	//overridable properties to influence UI
	mainTodayRowColorWidth: 210,
	mainTodayRowColorHidden: false,
	editingFontSizeWidth: 210,
	editingFontColorWidth: 210,
	identitiesColumnFaxHidden: false,
	advancedRegisterMailtoPack: 'center',
	
	viewModel: {
		formulas: {
			//NB: use field's name as formula's name otherwise checks after changes will not work!
			receipt: WTF.checkboxBind('record', 'receipt'),
			autoAddContact: WTF.checkboxBind('record', 'autoAddContact'),
			priority: WTF.checkboxBind('record', 'priority'),
			noMailcardOnReplyForward: WTF.checkboxBind('record', 'noMailcardOnReplyForward'),
			archiveKeepFoldersStructure: WTF.checkboxBind('record', 'archiveKeepFoldersStructure'),
			scanAll: WTF.checkboxBind('record', 'scanAll'),
			sharedSeen: WTF.checkboxBind('record', 'sharedSeen'),
			manualSeen: WTF.checkboxBind('record', 'manualSeen'),
			seenOnOpen: WTF.checkboxBind('record', 'seenOnOpen'),
			favoriteNotifications: WTF.checkboxBind('record', 'favoriteNotifications'),
			gridShowPreview: WTF.checkboxBind('record', 'gridShowPreview'),
			gridAlwaysShowTime: WTF.checkboxBind('record', 'gridAlwaysShowTime'),
			showUpcomingEvents: WTF.checkboxBind('record', 'showUpcomingEvents'),
			showUpcomingTasks: WTF.checkboxBind('record', 'showUpcomingTasks')
		}
	},
		
	initComponent: function() {
		var me = this, vm;
		me.callParent(arguments);
		
		vm = me.getViewModel();
		vm.setFormulas(Ext.apply(vm.getFormulas() || {}, {
			foCanManageAccount: function(get) {
				if (WT.isAdmin() || me.isAdminOnBehalf()) return true;
				return get('record.permAccountManage');
			},
			foCanManageMailcard: function(get) {
				if (WT.isAdmin() || me.isAdminOnBehalf()) return true;
				return get('record.permMailcardManage');
			},
			foCanManageDomainMailcard: function(get) {
				if (WT.isAdmin() || me.isAdminOnBehalf()) return true;
				return get('record.permDomainMailcardManage');
			},
			foCanManageExternalAccounts: function(get) {
				if (WT.isAdmin() || me.isAdminOnBehalf()) return true;
				return get('record.permExternalAccountManage');
			},
			foExternalAccountsEnabled: function(get) {
				var bool = get('record.externalAccountEnabled');
				return Ext.isBoolean(bool) ? bool : false;
			}
		}));
		
		me.add({
			xtype: 'wtopttabsection',
			title: me.res('opts.main.tit'),
			items: [{
				xtype: 'textfield',
				bind: {
					value: '{record.replyTo}',
					emptyText: '{record.mainEmail}'
				},
				fieldLabel: me.res('opts.account.fld-replyTo.lbl'),
				width: 430,
				submitEmptyText: false,
				needLogin: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, WTF.lookupCombo('id', 'desc', {
				bind: '{record.readReceiptConfirmation}',
				store: Ext.create('Sonicle.webtop.mail.store.ReadReceiptConfirmation', {
					autoLoad: true
				}),
				fieldLabel: me.res('opts.account.fld-readreceiptconfirmation.lbl'),
				width: 430,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}), {
				xtype: 'checkbox',
				bind: '{sharedSeen}',
				hideEmptyLabel: false,
				boxLabel: me.res('opts.adv.fld-sharedSeen.lbl'),
				listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
			},
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.sharedSort}',
				store: Ext.create('Sonicle.webtop.mail.store.SharedSort', {
					autoLoad: true
				}),
				fieldLabel: me.res('opts.account.fld-sharedSort.lbl'),
				width: 430,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}),
			{
				xtype: 'soformseparator',
				title: me.res('opts.main.grid.tit')
			},
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.viewMode}',
				store: Ext.create('Sonicle.webtop.mail.store.ViewMode', {
					autoLoad: true
				}),
				fieldLabel: me.res('opts.account.fld-viewmode.lbl'),
				width: 430,
				needReload: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}), {
				xtype: 'checkbox',
				bind: '{gridShowPreview}',
				hideEmptyLabel: false,
				boxLabel: me.res('opts.adv.fld-ingridPreview.lbl'),
				needReload: true,
				listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
			}, {
				xtype: 'checkbox',
				bind: '{gridAlwaysShowTime}',
				hideEmptyLabel: false,
				boxLabel: me.res('opts.fld-gridAlwaysShowTime.lbl'),
				needReload: true,
				listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
			}, {
				xtype: 'sopalettefield',
				bind: '{record.todayRowColor}',
				hideTrigger: false,
				colors: WT.getColorPalette('light'),
				tilesPerRow: 11,
				fieldLabel: me.res('opts.main.fld-todayRowColor.lbl'),
				width: me.mainTodayRowColorWidth,
				hidden: me.mainTodayRowColorHidden,
				needReload: true,
				listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
			}, {
				xtype: 'checkbox',
				reference: 'fldmanualSeen',
				bind: '{manualSeen}',
				hideEmptyLabel: false,
				boxLabel: me.res('opts.adv.fld-manualSeen.lbl'),
				listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
			},  {
				xtype: 'checkbox',
				style :'margin-left:30px',
				bind: {
					value:'{seenOnOpen}',					
					disabled: '{!fldmanualSeen.checked}'
				},
				hideEmptyLabel: false,
				boxLabel: me.res('opts.adv.fld-seenonopen.lbl'),
				listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
			}, {
				xtype: 'soformseparator',
				title: me.res('opts.main.panels.tit')
			}, {
				xtype: 'checkbox',
				bind: '{showUpcomingEvents}',
				hideEmptyLabel: false,
				boxLabel: me.res('opts.main.fld-showUpcomingEvents.lbl'),
				labelWidth: 20,
				listeners: {
					change: {
						fn: function(s) {
							//TODO: workaround...il modello veniva salvato prima dell'aggionamento
							Ext.defer(function() {
								me.onBlurAutoSave(s);
							}, 200);
						},
						scope: me
					}
				},
				needReload: true
			}, {
				xtype: 'checkbox',
				bind: '{showUpcomingTasks}',
				hideEmptyLabel: false,
				boxLabel: me.res('opts.main.fld-showUpcomingTasks.lbl'),
				labelWidth: 20,
				listeners: {
					change: {
						fn: function(s) {
							//TODO: workaround...il modello veniva salvato prima dell'aggionamento
							Ext.defer(function() {
								me.onBlurAutoSave(s);
							}, 200);
						},
						scope: me
					}
				},
				needReload: true
			}]
		});
		
		me.add({
			xtype: 'wtopttabsection',
			title: me.res('opts.editing.tit'),
			items: [
				WTF.lookupCombo('id', 'desc', {
					bind: {
						value: '{record.format}'
					},
					store: Ext.create('Sonicle.webtop.mail.store.EditingFormat', {
						autoLoad: true
					}),
					fieldLabel: me.res('opts.editing.fld-format.lbl'),
					width: 400,
					needReload: true,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}), WTF.lookupCombo('id', 'id', {
					bind: '{record.font}',
					store: {
						type: 'wtfonts',
						autoLoad: true
					},
					listConfig: {
						getInnerTpl: Sonicle.form.field.tinymce.tool.FontSelect.buildInnerTpl('id', 'desc')
					},
					needReload: true,
					fieldLabel: WT.res('word.font'),
					width: 400,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}), {
					xtype: 'numberfield',
					bind: '{record.fontSize}',
					fieldLabel: me.res('opts.editing.fld-fontsize'),
					minValue: 0,
					maxValue: 100,
					width: me.editingFontSizeWidth,
					hideTrigger: false,
					keyNavEnabled: false,
					mouseWheelEnabled: false,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}, {
					xtype: 'sopalettefield',
					bind: '{record.fontColor}',
					hideTrigger: false,
					colors: WT.getColorPalette('html'),
					tilesPerRow: 8,
					fieldLabel: me.res('opts.editing.fld-fontcolor'),
					width: me.editingFontColorWidth,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'checkbox',
					bind: '{receipt}',
					boxLabel: me.res('opts.editing.fld-receipt.lbl'),
					hideEmptyLabel: false,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'checkbox',
					bind: '{autoAddContact}',
					boxLabel: me.res('opts.editing.fld-autoAddContact.lbl'),
					hideEmptyLabel: false,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'checkbox',
					bind: '{priority}',
					boxLabel: me.res('opts.editing.fld-priority.lbl'),
					hideEmptyLabel: false,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'checkbox',
					bind: '{noMailcardOnReplyForward}',
					boxLabel: me.res('opts.editing.fld-noMailcardOnReplyForward.lbl'),
					hideEmptyLabel: false,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				}, {
					xtype: 'fieldcontainer',
					fieldLabel: me.res('opts.editing.fld-emaildomainMailcard.lbl'),
					layout: 'hbox',
					bind: {
						hidden: '{!foCanManageDomainMailcard}'
					},
					items: [ 
						{ xtype: 'button', ui: '{secondary|toolbar}', text: WT.res('act-edit.lbl'), iconCls: 'fas fa-pen-to-square', width: 150, handler: function(s) { me.editMailcard('emaildomain', s); } },
						{ xtype: 'displayfield', text: '', width: 10 },
						{ xtype: 'button', ui: '{tertiary|toolbar}', text: WT.res('act-delete.lbl'), iconCls: 'fas fa-trash', width: 150, handler: function(s) { me.delMailcard('emaildomain', s); } }
					]
				}, {
					xtype: 'fieldcontainer',
					fieldLabel: me.res('opts.editing.fld-identity0Mailcard.lbl'),
					layout: 'hbox',
					bind: {
						hidden: '{!foCanManageMailcard}'
					},
					items: [ 
						{ xtype: 'button', ui: '{secondary|toolbar}', text: WT.res('act-edit.lbl'), iconCls: 'fas fa-pen-to-square', width: 150, handler: function(s) { me.editMailcard('identity|0', s); } },
						{ xtype: 'displayfield', text: '', width: 10 },
						{ xtype: 'button', ui: '{tertiary|toolbar}', text: WT.res('act-delete.lbl'), iconCls: 'fas fa-trash', width: 150, handler: function(s) { me.delMailcard('identity|0', s); } }
					]
				}
			]		
		});
		
		var identEdCfg = {};
		if (me.isProfileSelf()) {
			identEdCfg = {
				xtype: 'sotreecombo',
				triggers: {
					clear: WTF.clearTrigger()
				},
				store: Ext.create('Ext.data.TreeStore', {
					model: 'Sonicle.webtop.mail.model.ImapTreeModel',
					proxy: WTF.proxy(me.ID, 'GetImapTree'),
					root: {
						id: '/',
						expanded: true
					},
					rootVisible: false
				})
			};
		} else {
			identEdCfg = {xtype: 'textfield'};
		}
		me.add({
			xtype: 'wtopttabsection',
			title: me.res('opts.ident.tit'),
			layout: 'vbox',
			defaults: {
				width: '100%'
			},
			items: [
				me.createGridCfg({
					xtype: 'gridpanel',
					reference: 'gpidents',
					store: {
						autoLoad: true,
						autoSync: true,
						model: 'Sonicle.webtop.mail.model.Identity',
						proxy: WTF.apiProxy(me.ID, 'ManageIdentities', 'identities', {
							extraParams: {
								optionsProfile: me.profileId,
								type: 'user'
							}
						}),
						listeners: {
							beforesync: function() {
								me.needLogin = true;
							}
						}
					},
					plugins: {
						ptype: 'rowediting',
						clicksToEdit: 2,
						pluginId: 'gpidentseditor',
						saveBtnText: WT.res('act-save.lbl')
					},
					columns: [
						{
							dataIndex: 'displayName',
							header: me.res('opts.ident.displayName.lbl'),
							editor: {xtype: 'textfield'},
							flex: 2
						}, {
							dataIndex: 'email',
							header: me.res('opts.ident.email.lbl'),
							editor: {xtype: 'textfield'},
							flex: 2
						}, {
							dataIndex: 'mainFolder',
							header: me.res('opts.ident.mainFolder.lbl'),
							editor: identEdCfg,
							/*
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
							*/
							flex: 2
						}, {
							xtype: 'checkcolumn',
							hidden: me.identitiesColumnFaxHidden,
							dataIndex: 'fax',
							header: me.res('opts.ident.fax.lbl'),
							editor: { xtype: 'checkbox' },
							width: 45
						}, {
							xtype: 'soactioncolumn',
							items: [
								{
									iconCls: 'fas fa-pen-to-square',
									tooltip: me.res('opts.ident.btn-editMailcard.lbl'),
									handler: function(view, ridx, cidx, itm, e, rec) {
										me.editMailcard(rec.getMailcardId(), null);
									}
								}, {
									iconCls: 'fas fa-ellipsis-vertical',
									menu: [
										{
											text: me.res('opts.ident.btn-editMailcard.lbl'),
											iconCls: 'fas fa-pen-to-square',
											handler: function(s, e) {
												me.editMailcard(e.menuData.rec.getMailcardId(), null);
											}
										}, {
											text: me.res('opts.ident.btn-deleteMailcard.lbl'),
											iconCls: 'fas fa-trash',
											handler: function(s, e) {
												me.delMailcard(e.menuData.rec.getMailcardId(), null);
											}
										}, '-', {
											text: me.res('opts.ident.btn-deleteIdentity.lbl'),
											iconCls: 'fas fa-trash',
											userCls: 'wt-dangerzone',
											handler: function(s, e) {
												me.lref('gpidents').store.remove(e.menuData.rec);
											}
										}
									]
								}
							]
						}
					],
					tbar: {
						border: false,
						items: [
							{
								xtype: 'tbtext',
								text: me.res('opts.idents.manual.tit'),
								cls: 'wt-form-toolbar-title'
							},
							'->',
							{
								xtype: 'button',
								ui: '{secondary|toolbar}',
								reference: 'addIdentity',
								text: me.res('opts.ident.btn-addIdentity.lbl'),
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
							}
						]
					},
					flex: 1
				}),
				{
					xtype: 'sovspacer',
					ui: 'large'
				}, {
					xtype: 'sotext',
					text:  me.res('opts.idents.auto.tit'),
					cls: 'wt-form-body-title'
				},
				me.createGridCfg({					
					xtype: 'gridpanel',
					reference: 'gpautoidents',
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.mail.model.Identity',
						proxy: WTF.apiProxy(me.ID, 'ListIdentities', 'identities', {
							extraParams: {
								optionsProfile: me.profileId,
								type: 'auto'
							}
						})
					},
					columns: [
						{
							dataIndex: 'displayName',
							header: me.res('opts.ident.displayName.lbl'),
							flex: 2
						}, {
							dataIndex: 'email',
							header: me.res('opts.ident.email.lbl'),
							flex: 2
						}, {
							xtype: 'checkcolumn',
							dataIndex: 'forceMailcard',
							header: me.res('opts.ident.force-mailcard.lbl'),
							flex: 1,
							disabled: true
						}
					],
					flex: 0.8
				})
			]
		});
		
		me.add({
			xtype: 'wtopttabsection',
			title: me.res('opts.arch.tit'),
			items: [
				{
					xtype: 'soformseparator',
					title: me.res('opts.arch.local.tit')
				},
				WTF.lookupCombo('id', 'desc', {
					bind: '{record.archiveMode}',
					store: Ext.create('Sonicle.webtop.mail.store.ArchiveMode', {
						autoLoad: true
					}),
					fieldLabel: me.res('opts.arch.fld-archiveMode.lbl'),
					width: 440,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}), {
					xtype: 'checkbox',
					bind: '{archiveKeepFoldersStructure}',
					fieldLabel: me.res('opts.arch.fld-keepFoldersStructure.lbl'),
					width: 100,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
				},
				{
					xtype: 'soformseparator',
					title: me.res('opts.arch.external.tit')
				},
				{
					xtype: 'textfield',
					bind: {
						value: '{record.archiveExternalUserFolder}',
						emptyText: me.res('opts.account.fld-username-empty.lbl')
					},
					fieldLabel: me.res('opts.arch.fld-archiveExternalUserFolder.lbl'),
					width: 440,
					submitEmptyText: false,
					needLogin: true,
					disabled: !WT.isAdmin(),
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}
			]
		});
		
		//if (WT.isPermitted(me.ID,'DOCUMENT_MANAGEMENT','ACCESS')) {
		//}
		
		me.add({
			xtype: 'wtopttabsection',
			title: me.res('opts.account.tit'),
			bind: {
				permStatus: '{record.permAccountManage}'
			},
			plugins: [{
				ptype: 'wttabpermstatus',
				isAdmin: WT.isAdmin() || me.isAdminOnBehalf(),
				info: 'ACCOUNT_SETTINGS:CHANGE'
			}],
			items: Ext.Array.join(
				(!WT.isAdmin() && ! me.isAdminOnBehalf()) ? [
					me.createPermissionFeedbackCfg({
						bind: {
							hidden: '{record.permAccountManage}'
						},
						hidden: true,
						maxWidth: 440
					}),
					{
						xtype: 'sovspacer',
						ui: 'large',
						bind: {
							hidden: '{record.permAccountManage}'
						}
					}
				] : undefined,
				[
					WTF.lookupCombo('id', 'desc', {
						bind: {
							value: '{record.protocol}',
							disabled: '{!foCanManageAccount}'
						},
						store: Ext.create('WTA.store.MailboxProtocols', {
							autoLoad: true
						}),
						fieldLabel: me.res('opts.account.fld-protocol.lbl'),
						width: 260,
						needLogin: true,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}), {
						xtype: 'textfield',
						bind: {
							value: '{record.host}',
							disabled: '{!foCanManageAccount}'
						},
						fieldLabel: me.res('opts.account.fld-host.lbl'),
						width: 440,
						needLogin: true,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}, {
						xtype: 'numberfield',
						bind: {
							value: '{record.port}',
							disabled: '{!foCanManageAccount}'
						},
						fieldLabel: me.res('opts.account.fld-port.lbl'),
						width: 240,
						needLogin: true,
						hideTrigger: true,
						keyNavEnabled: false,
						mouseWheelEnabled: false,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}, {
						xtype: 'textfield',
						bind: {
							value: '{record.username}',
							disabled: '{!foCanManageAccount}'
						},
						plugins: 'sonoautocomplete',
						fieldLabel: me.res('opts.account.fld-username.lbl'),
						width: 440,
						needLogin: true,
						emptyText: me.res('opts.account.fld-username-empty.lbl'),
						submitEmptyText: false,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}, {
						xtype: 'sopasswordfield',
						bind: {
							value: '{record.password}',
							disabled: '{!foCanManageAccount}'
						},
						plugins: 'sonoautocomplete',
						//inputType: 'password',
						fieldLabel: me.res('opts.account.fld-password.lbl'),
						width: 440,
						needLogin: true,
						emptyText: me.res('opts.account.fld-password-empty.lbl'),
						submitEmptyText: false,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}, {
						xtype: 'textfield',
						bind: {
							value: '{record.folderPrefix}',
							disabled: '{!foCanManageAccount}'
						},
						fieldLabel: me.res('opts.account.fld-folderPrefix.lbl'),
						width: 440,
						needLogin: true,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}, {
						xtype: 'textfield',
						bind: {
							value: '{record.folderSent}',
							disabled: '{!foCanManageAccount}'
						},
						fieldLabel: me.res('opts.account.fld-folderSent.lbl'),
						width: 440,
						needLogin: true,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}, {
						xtype: 'textfield',
						bind: {
							value: '{record.folderDrafts}',
							disabled: '{!foCanManageAccount}'
						},
						fieldLabel: me.res('opts.account.fld-folderDrafts.lbl'),
						width: 440,
						needLogin: true,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}, {
						xtype: 'textfield',
						bind: {
							value: '{record.folderTrash}',
							disabled: '{!foCanManageAccount}'
						},
						fieldLabel: me.res('opts.account.fld-folderTrash.lbl'),
						width: 440,
						needLogin: true,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}, {
						xtype: 'textfield',
						bind: {
							value: '{record.folderSpam}',
							disabled: '{!foCanManageAccount}'
						},
						fieldLabel: me.res('opts.account.fld-folderSpam.lbl'),
						width: 440,
						needLogin: true,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}, {
						xtype: 'textfield',
						bind: {
							value: '{record.folderArchive}',
							disabled: '{!foCanManageAccount}'
						},
						fieldLabel: me.res('opts.account.fld-folderArchive.lbl'),
						width: 440,
						needLogin: true,
						listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
					}
				]
			)
		});
		
		me.add({
			xtype: 'wtopttabsection',
			reference: 'tabextaccounts',
			title: me.res('opts.external.accounts.tit'),
			bind: {
				permStatus: '{record.permExternalAccountManage}'
			},
			hidden: true,
			plugins: [{
				ptype: 'wttabpermstatus',
				isAdmin: WT.isAdmin() || me.isAdminOnBehalf(),
				info: 'EXTERNAL_ACCOUNT_SETTINGS:CHANGE'
			}],
			layout: 'fit',
			items: [{
					xtype: 'wtpanel',
					layout: 'border',
					cls: 'wtcore-panel-grids-container',
					items: [{
							region: 'north',
							xtype: 'toolbar',
							border: false,
							items: [ 
								'->', {
									xtype: 'button',
									text: me.res('opts.external.accounts.refresh.lbl'),
									iconCls: 'wt-icon-refresh',
									bind: {
										disabled: '{!foCanManageExternalAccounts}'
									},
									scope: me,
									handler: function() {
										me.reloadExternalAccounts();
									}
								}, {
									xtype: 'splitbutton',
									ui: '{secondary|toolbar}',
									text: me.res('opts.external.accounts.add.lbl'),
									iconCls: 'wt-icon-add',
									bind: {
										disabled: '{!foCanManageExternalAccounts}'
									},
									handler: function(s) {
											s.maybeShowMenu();
									},
									menu: {
										xtype: 'sostoremenu',
										cls: 'wtmail-providers-menu',
										store: {
											autoLoad: true,
											model: 'Sonicle.webtop.mail.model.ExternalAccountProvider',
											proxy: WTF.apiProxy(me.ID, 'ExternalAccountProviders', 'data', {
												extraParams: {
													optionsProfile: me.profileId
												}
											})
										},
										textField: 'id',
										iconField: 'iconUrl',
										listeners: {
											click: function(s,item) {
												var count = me.lref('gpExternalAccounts').getStore().getCount();
												if (count >= 3) {
													WT.warn(me.res('opts.external.accounts.warn.limit'));
												} else {
													me.addExternalAccount(s, item.getItemId(), {
														callback: function(success) {
															if(success) {
																me.needLogin=true;
																me.reloadExternalAccounts();
															}
														}
													});
												}	
											}
										}
									}
								}, 
							]
						}, me.createGridCfg({
							region: 'center',
							xtype: 'gridpanel',
							reference: 'gpExternalAccounts',
							bind: {
									disabled: '{!foCanManageExternalAccounts}'
								},
							scope: me,
							store: {
								autoLoad: true,
								autoSync: true,
								model: 'Sonicle.webtop.mail.model.GridExternalAccount',
								proxy: WTF.apiProxy(me.ID, 'ManageExternalAccountsGrid', 'externalAccount', {
									extraParams: {
										optionsProfile: me.profileId
									}
								}),
								listeners: {
									beforesync: function() {
										me.needReload = true;
									}
								}
							},
							columns: [ {
									dataIndex: 'iconUrl',
									header: me.res('opts.external.accounts.icon.lbl'),
									flex: 1,
									width: 2,
									renderer: function(value) {
										if(value !== null)
										return '<img src="' + value + '" style="width:25px; height:25px"/>';
									}

								}, {
									dataIndex: 'accountDescription',
									header: me.res('opts.external.accounts.accountDescription.lbl'),
									scope: me,
									flex: 2
								}, {
									dataIndex: 'email',
									header: me.res('opts.external.accounts.email.lbl'),
									needLogin: true,
									scope: me,
									flex: 2
								}, {
									xtype: 'soactioncolumn',
									items: [
										{
											iconCls: 'fas fa-pen-to-square',
											tooltip: me.res('opts.external.accounts.delete.lbl'),
											handler: function(view, ridx, cidx, itm, e, rec) {
												me.editExternalAccount(rec.get('externalAccountId'),{
													callback: function(success) {
														if(success) {
															me.needLogin=true;
															me.reloadExternalAccounts();
														}
													}
												});
											}
										}, {
											iconCls: 'fas fa-ellipsis-vertical',
											menu: [
												{
													text: me.res('opts.external.accounts.edit.lbl'),
													iconCls: 'fas fa-pen-to-square',
													handler: function(s, e) {
														me.editExternalAccount(e.menuData.rec.get('externalAccountId'),{
															callback: function(success) {
																if(success) {
																	me.needLogin=true;
																	me.reloadExternalAccounts();
																}
															}
														});
													}
												}, '-', {
													text: me.res('opts.external.accounts.delete.lbl'),
													iconCls: 'fas fa-trash',
													handler: function(s, e) {
														Ext.Msg.show({
															title: WT.res('warning'),
															msg: WT.res('confirm.delete'),
															buttons: Ext.Msg.YESNO,
															icon: Ext.Msg.WARNING,														
															fn: function(bid) {
																if (bid==='yes') {
																	me.needLogin=true;
																	me.deleteExternalAccount(e.menuData.rec.get('externalAccountId'));
																}
															},
															scope: me
														});
													}
												}
											]
										}
									]
								}
							],
							listeners: {
								selectionchange: function(s,recs) {
									me.lref('deleteExternalAccount').setDisabled(!recs.length);
									me.lref('editExternalAccount').setDisabled(!recs.length);
								},
								rowdblclick: function(s, rec) {
									var externalAccountId = rec.get('externalAccountId');

									me.editExternalAccount(externalAccountId, {
										callback: function(success) {
											if(success) me.reloadExternalAccounts();
										}
									});
								}
							}
						})]
				}]
		});

		me.add({
			xtype: 'wtopttabsection',
			title: me.res('opts.adv.tit'),
			items: [{
				xtype: 'checkbox',
				bind: '{scanAll}',
				fieldLabel: me.res('opts.adv.fld-scanAll.lbl'),
				width: 100,
				listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
			}, /*{ Deprecated
				xtype: 'fieldcontainer',
				fieldLabel: me.res('opts.adv.fld-scanCycles.lbl'),
				layout: 'hbox',
				items: [{
					xtype: 'sliderwidget',
					bind: '{record.scanCycles}',
					increment: 1,
					keyIncrement: 1,
					minValue: 3,
					maxValue: 30,
					width: 100,
					listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
					}, {
						xtype: 'displayfield', text: '', width: 10
					}, {
						xtype: 'numberfield',
						bind: '{record.scanSecondsOthers}',
						fieldLabel: ' ',
						width: 150,
						hideTrigger: true,
						keyNavEnabled: false,
						mouseWheelEnabled: false,
						disabled: true
				}]
			},*/ {
				xtype: 'textfield',
				bind: '{record.defaultFolder}',
				fieldLabel: me.res('opts.adv.fld-defaultFolder.lbl'),
				width: 400,
				needLogin: true,
				listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
			}, {
				xtype: 'checkbox',
				bind:  '{favoriteNotifications}',
				fieldLabel: me.res('opts.adv.fld-favoriteFolderNotifications.lbl'),
				width: 100,
				listeners: { change: { fn: function(s) { Ext.defer(function() { me.onBlurAutoSave(s); }, 200); }, scope: me } }
			},{
				xtype: 'container',
				layout: {
					type: 'hbox',
					pack: me.advancedRegisterMailtoPack,
					align: 'middle'
				},
				items: [{
					xtype: 'button',
					ui: '{secondary|toolbar}',
					text: me.res('opts.adv.btn-registerMailto.lbl'),
					width: 250,
					handler: function() {
						Sonicle.ProtocolHandlerMgr.resetPromptState('mailto');
						Sonicle.ProtocolHandlerMgr.register('mailto', location.href.split('?')[0] + '?service=com.sonicle.webtop.mail&action=mailto&args=%s', { friendlyName: 'WebTop' });
					}
				}]
			}]
		});
		
		vm.bind('{record.externalAccountEnabled}', function(nv) {
			if (nv) me.lref('tabextaccounts').tab.show();
		});
	},
	
	getExternalProviderData: function(comp, id) {
		var me = this,
				items = comp.getStore().data.items,
				itemData;
		items.forEach(function(element) {
			if(element.id === id) {
				itemData = element.data;
			}
		});
		return itemData;
	},
	
	addExternalAccount: function(comp, itemId, opts) {
		var me = this,
				itemData = me.getExternalProviderData(comp, itemId),
			view = WT.createView(me.ID, 'view.ExternalAccount', {
				swapReturn: true, 
				viewCfg: {
					profileId: me.profileId,
					providerId: itemData.id,
					iconUrl: itemData.iconUrl,
					email: itemData.email,
					server: itemData.server,
					protocol: itemData.protocol,
					port: itemData.port,
					readOnly: itemData.readOnly,
					folderPrefix: itemData.folderPrefix,
					folderSent: itemData.folderSent,
					folderDrafts: itemData.folderDrafts,
					folderTrash: itemData.folderTrash,
					folderSpam: itemData.folderSpam,
					folderArchive: itemData.folderArchive
				}
			});
		
		view.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		
		view.showView(function(s) {
			view.begin('new', {
				data: {
					_profileId: 'ownerId',
					categoryId: 'categoryId'
				}
			});
		});
	},
	
	editExternalAccount: function(externalAccountId, opts) {
		var me = this,
			view = WT.createView(me.ID, 'view.ExternalAccount', {
				swapReturn: true, 
				viewCfg: {
					profileId: me.profileId
				}
			});
	
		view.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		
		view.showView(function(s) {
			view.begin('edit', {
				data: {
					externalAccountId: externalAccountId
				}
			});
		});
	},
	
	deleteExternalAccount: function(externalAccountId) {
		var me = this;
		WT.ajaxReq(me.ID, "ManageExternalAccounts", {
			params: {
				crud: 'delete',
				optionsProfile: me.profileId,
				externalAccountId: externalAccountId
			},
			callback: function(success, json) {
				if(success)
					me.reloadExternalAccounts();
			}
		});
	},
	
	reloadExternalAccounts: function() {
		var me = this,
			store = me.lref('gpExternalAccounts').getStore();
			store.load();
	},
	
	editMailcard: function(mailcardId) {
		var me = this;
		WT.ajaxReq(me.ID, "ManageMailcard", {
			params: {
				optionsProfile: me.profileId,
				crud: "read",
				mailcardId: mailcardId
			},
			callback: function(success, json) {
				if (success) {
					WT.createView(me.ID, 'view.MailcardEditor', {
						viewCfg: {
							mys: me,
							domainId: me.profileId.split('@')[1],
							html: json.data.html,
							listeners: {
								viewok: function(s,html) {
									me.saveMailcard(mailcardId,html);
								}
							}
						}
					}).show();
				} else {
					WT.error(json.message);
				}
			}
		});
		
	},
	
	saveMailcard: function(mailcardId, html) {
		var me=this;
		
		if(mailcardId === 'emaildomain')
			me._saveMailcard(mailcardId,html);
		else {
			var tokens = mailcardId.split('|');
			if(tokens[0] === 'identity') {
				var i = parseInt(tokens[1]);
				if(i == 0) {
					Ext.Msg.show({
						title: WT.res('warning'),
						msg: me.res('opts.mailcard-editor.target.msg'),
						buttons: Ext.MessageBox.YESNOCANCEL,
						buttonText: {
							yes: me.res('opts.mailcard-editor.target.email'),
							no: me.res('opts.mailcard-editor.target.user'),
							cancel: WT.res('act-cancel.lbl')
						},
						icon: Ext.Msg.QUESTION,
						fn: function(bid) {
							if(bid === 'cancel') return;
							var target = (bid === 'yes') ? 'email' : 'user';
							me._saveMailcard(mailcardId,html,target);
						}
					});
				} else {
					me._saveMailcard(mailcardId,html);
				}
			}
		}
	},
	
	_saveMailcard: function(mailcardId, html, target) {
		var me=this;
		
		WT.ajaxReq(me.ID, "ManageMailcard", {
			params: {
				optionsProfile: me.profileId,
				crud: "update",
				mailcardId: mailcardId,
				target: target,
				html: html
			},
			callback: function(success,json) {
				if (success) {
					me.needReload=true;
				} else {
					WT.error(json.message);
				}
			}
		});
		
	},
	
	delMailcard: function(mailcardId, btn) {
		var me=this;
		
		Ext.Msg.show({
			title: WT.res('warning'),
			msg: WT.res('confirm.delete'),
			buttons: Ext.Msg.YESNO,
			icon: Ext.Msg.WARNING,
			fn: function(bid) {
				if(bid === 'no') return;
				WT.ajaxReq(me.ID, "ManageMailcard", {
					params: {
						optionsProfile: me.profileId,
						crud: "delete",
						mailcardId: mailcardId
					},
					callback: function(success,json) {
						if (success) {
							if(mailcardId === 'emaildomain')
								if (btn) btn.setDisabled(true);
							else {
								var tokens = mailcardId.split('|');
								if(tokens[0] === 'identity') {
									var i = parseInt(tokens[1]);
									if(i == 0) 
										if (btn) btn.setDisabled(true);
								}
							}
							me.needReload=true;
						} else {
							WT.error(json.message);
						}
					}
				});
			}
		});
		
	},
	
	res: function(key) {
		return WT.res(this.ID, key);
	}
});
