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
Ext.define('Sonicle.webtop.mail.view.AutoResponder', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.grid.column.Action',
		'Sonicle.webtop.mail.model.InMailAutoResponder',
		'Sonicle.webtop.mail.store.SieveVacationDays'
	],
	uses: [
		'Sonicle.Date',
		'Sonicle.webtop.mail.view.SieveFilter'
	],
	
	dockableConfig: {
		title: '{inMailFilters.autoResponder.tit}',
		iconCls: 'wtmail-icon-autoResponder',
		width: 560,
		height: 600,
		modal: true
	},
	modelName: 'Sonicle.webtop.mail.model.InMailAutoResponder',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foSieveAvail: WTF.foGetFn('record', 'scriptsCount', function(v) {
				return v !== -1;
			}),
			foActiveScriptVisible: WTF.foGetFn('record', 'scriptsCount', function(v) {
				return v >= 2;
			}),
			foAutoRespEnabled: WTF.checkboxBind('record.autoResponder', 'enabled'),
			foAutoRespStartEnabled: {
				bind: {bindTo: '{record.autoResponder.activationStartDate}'},
				get: function(val) {
					return Ext.isDate(val);
				},
				set: function(val) {
					var rec = this.get('record.autoResponder');
					if (val === true) {
						rec.setActivationStartDate(new Date());
					} else {
						rec.set('activationStartDate', null);
					}
				}
			},
			foAutoRespStartDate: {
				bind: {bindTo: '{record.autoResponder.activationStartDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record.autoResponder').setActivationStartDate(val);
				}
			},
			foAutoRespEndEnabled: {
				bind: {bindTo: '{record.autoResponder.activationEndDate}'},
				get: function(val) {
					return Ext.isDate(val);
				},
				set: function(val) {
					var rec = this.get('record.autoResponder');
					if (val === true) {
						var sdt = rec.get('activationStartDate');
						rec.setActivationEndDate(Sonicle.Date.add(Ext.isDate(sdt) ? sdt : new Date(), {days: 1}, true));
					} else {
						rec.set('activationEndDate', null);
					}
				}
			},
			foAutoRespEndDate: {
				bind: {bindTo: '{record.autoResponder.activationEndDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record.autoResponder').setActivationEndDate(val);
				}
			}
		});
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: 'vbox',
			items: [{
				xtype: 'wtfieldspanel',
				//title: me.res('inMailFilters.autoResponder.tit'),
				paddingTop: true,
				paddingSides: true,
				scrollable: true,
				modelValidation: true,
				defaults: {
					labelWidth: 150
				},
				items: [
					{
						xtype: 'textfield',
						bind: {
							value: '{record.autoResponder.subject}',
							disabled: '{!fldAutoRespEnabled.checked}'
						},
						fieldLabel: me.res('sieveFilter.autoResponder.fld-subject.lbl'),
						emptyText: me.res('sieveFilter.autoResponder.fld-subject.emp'),
						anchor: '100%'
					},{
						xtype: 'textareafield',
						bind: {
							value: '{record.autoResponder.message}',
							disabled: '{!fldAutoRespEnabled.checked}'
						},
						fieldLabel: me.res('sieveFilter.autoResponder.fld-message.lbl'),
						height: 80,
						anchor: '100%'
					}, {
						xtype: 'tagfield',
						bind: {
							value: '{record.autoResponder.addresses}',
							disabled: '{!fldAutoRespEnabled.checked}'
						},
						createNewOnEnter: true,
						createNewOnBlur: true,
						filterPickList: false,
						forceSelection: false,
						hideTrigger: true,
						autoLoadOnValue: true,
						store: {
							model: 'WTA.ux.data.ValueModel'
						},
						maskRe: /^[a-zA-Z0-9_\-\.\@]/,
						fieldLabel: me.res('sieveFilter.autoResponder.fld-addresses.lbl'),
						emptyText: me.res('sieveFilter.autoResponder.fld-addresses.emp'),
						anchor: '100%'
					},
					WTF.lookupCombo('id', 'desc', {
						bind: {
							value: '{record.autoResponder.daysInterval}',
							disabled: '{!fldAutoRespEnabled.checked}'
						},
						store: {
							type: 'wtmailsievevacationdays',
							autoLoad: true
						},
						fieldLabel: me.res('sieveFilter.autoResponder.fld-daysInterval.lbl'),
						emptyText: '7',
						width: 230
					}),
					{
						xtype: 'soformseparator',
						bind: {
							disabled: '{!fldAutoRespEnabled.checked}'
						}
					}, {
						xtype: 'fieldcontainer',
						bind: {
							disabled: '{!fldAutoRespEnabled.checked}'
						},
						defaults: {
							margin: '0 0 0 10'
						},
						layout: 'hbox',
						items: [
							{
								xtype: 'checkbox',
								reference: 'fldautorespstartenabled',
								bind: '{foAutoRespStartEnabled}',
								hideEmptyLabel: true,
								boxLabel: me.res('sieveFilter.autoResponder.fld-enableOn.lbl'),
								width: 240
							}, {
								xtype: 'datefield',
								bind: {
									value: '{foAutoRespStartDate}',
									disabled: '{!fldautorespstartenabled.checked}'
								},
								startDay: WT.getStartDay(),
								format: WT.getShortDateFmt(),
								width: 140
							}
						]
					}, {
						xtype: 'fieldcontainer',
						bind: {
							disabled: '{!fldAutoRespEnabled.checked}'
						},
						defaults: {
							margin: '0 0 0 10'
						},
						layout: 'hbox',
						items: [
							{
								xtype: 'checkbox',
								reference: 'fldautorespendenabled',
								bind: '{foAutoRespEndEnabled}',
								hideEmptyLabel: true,
								boxLabel: me.res('sieveFilter.autoResponder.fld-disableOn.lbl'),
								width: 240
							}, {
								xtype: 'datefield',
								bind: {
									value: '{foAutoRespEndDate}',
									disabled: '{!fldautorespendenabled.checked}'
								},
								startDay: WT.getStartDay(),
								format: WT.getShortDateFmt(),
								width: 140
							}
						]
					}
				],
				width: '100%'
			}]
		});
		
		me.on('viewload', me.onViewLoad);
	},
	
	initTBar: function() {
		var me = this,
			SoU = Sonicle.Utils;
		
		me.dockedItems = SoU.mergeDockedItems(me.dockedItems, 'top', [
			me.createTopToolbar1Cfg(me.prepareTopToolbarItems())
		]);
		me.dockedItems = SoU.mergeDockedItems(me.dockedItems, 'bottom', [
			me.createStatusbarCfg()
		]);
	},
			
	privates: {
		onViewLoad: function(s, success) {
			if (!success) return;
			var me = this,
				mo = me.getModel();

			if (mo.get('activeScript') !== me.self.WT_SCRIPT) {
				WT.confirm(me.res('inMailFilters.confirm.notwebtop', WT.getPlatformName()), function(bid) {
					if (bid === 'yes') mo.set('activeScript', me.self.WT_SCRIPT);
				}, this);
			}
		},
		
		prepareTopToolbarItems: function() {
			var me = this;
			return [
				{
					xtype: 'checkbox',
					reference: 'fldAutoRespEnabled',
					bind: '{foAutoRespEnabled}',
					hideEmptyLabel: true,
					boxLabel: me.res('sieveFilter.autoResponder.fld-enabled.lbl')
				},
				'->',
				WTF.lookupCombo('id', 'desc', {
					bind: {
						value: '{record.activeScript}',
						visible: '{foActiveScriptVisible}'
					},
					store: {
						autoLoad: true,
						model: 'WTA.model.Simple',
						proxy: WTF.proxy(me.mys.ID, 'LookupSieveScripts')
					},
					fieldLabel: me.res('inMailFilters.fld-activeScript.lbl'),
					labelAlign: 'right',
					labelWidth: 180,
					width: 300,
					listeners: {
						select: function(s, rec) {
							if (rec.getId() !== me.self.WT_SCRIPT) {
								WT.warn(me.res('inMailFilters.warn.notwebtop', me.self.WT_SCRIPT));
							}
						}
					}
				})
			];
		},
		
		createStatusbarCfg: function() {
			var me = this;
			return {
				xtype: 'statusbar',
				items: [
					{
						xtype: 'tbtext',
						bind: {
							hidden: '{foSieveAvail}'
						},
						text: me.res('inMailFilters.warn.nosieve')
					}
				]
			};
		}
	},
	
	statics: {
		WT_SCRIPT: 'webtop5'
	}
});
