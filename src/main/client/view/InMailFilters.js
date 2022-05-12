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
Ext.define('Sonicle.webtop.mail.view.InMailFilters', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.grid.column.Action',
		'Sonicle.webtop.mail.model.InMailFilters',
		'Sonicle.webtop.mail.store.SieveVacationDays'
	],
	uses: [
		'Sonicle.Date',
		'Sonicle.webtop.mail.view.SieveFilter'
	],
	
	dockableConfig: {
		title: '{inMailFilters.tit}',
		iconCls: 'wtmail-icon-inMailFilters',
		width: 560,
		height: 600,
		modal: true
	},
	modelName: 'Sonicle.webtop.mail.model.InMailFilters',
	
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
		Ext.apply(me, {
			tbar: [
				'-',
				me.addAct('addMailFilter', {
					ignoreSize: true,
					text: null,
					tooltip: me.res('inMailFilters.addMailFilter.tip'),
					handler: function() {
						me.addMailFilterUI();
					}
				}),
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
					labelWidth: 140,
					width: 240,
					listeners: {
						select: function(s, rec) {
							if (rec.getId() !== me.self.WT_SCRIPT) {
								WT.warn(me.res('inMailFilters.warn.notwebtop', me.self.WT_SCRIPT));
							}
						}
					}
				})
			],
			bbar: {
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
			}
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: 'vbox',
			items: [{
				xtype: 'gridpanel',
				reference: 'gpfilters',
				bind: {
					store: '{record.filters}'
				},
				border: false,
				viewConfig: {
					deferEmptyText: false,
					plugins: [
						{
							ptype: 'sogridviewddordering',
							orderField: 'order',
							zeroBased: false
						}
					]
				},
				columns: [
					{
						xtype: 'soiconcolumn',
						dataIndex: 'enabled',
						header: '',
						sortable: false,
						menuDisabled: true,
						stopSelection: true,
						getIconCls: function (value, rec) {
							return WTF.cssIconCls(WT.XID, 'traffic-light-' + (value ? 'green' : 'red'));
						},
						getTip: function(v,rec) {
							if (v === true) {
								return me.res('inMailFilters.gp-filters.enabled.true');
							} else {
								return me.res('inMailFilters.gp-filters.enabled.false');
							}
						},
						handler: function (grid, rix, cix, e, rec) {
							rec.set('enabled', !rec.get('enabled'));
						},
						width: 30
					}, {
						dataIndex: 'name',
						header: me.res('inMailFilters.gp-filters.name.lbl'),
						flex: 1
					}, {
						xtype: 'soactioncolumn',
						items: [
							{
								iconCls: 'far fa-trash-alt',
								tooltip: WT.res('act-remove.lbl'),
								handler: function(g, ridx) {
									var rec = g.getStore().getAt(ridx);
									me.deleteMailFilterUI(rec);
								}
							}
						]
					}
				],
				listeners: {
					rowdblclick: function(s, rec) {
						me.editMailFilterUI(rec);
					}
				},
				width: '100%',
				flex: 1
			}, {
				xtype: 'wtform',
				title: me.res('inMailFilters.autoResponder.tit'),
				modelValidation: true,
				defaults: {
					labelWidth: 150
				},
				items: [
					{
						xtype: 'checkbox',
						reference: 'fldAutoRespEnabled',
						bind: '{foAutoRespEnabled}',
						hideEmptyLabel: false,
						boxLabel: me.res('sieveFilter.autoResponder.fld-enabled.lbl')
					}, {
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
								width: 220
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
								width: 220
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
	
	addMailFilter: function(data, opts) {
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.SieveFilter', {
				swapReturn: true
			});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: Ext.applyIf(data || {}, {
					enabled: true,
					match: 'all'
				})
			});
		});
	},
	
	privates: {
		onViewLoad: function(s, success) {
			if (!success) return;
			var me = this,
				mo = me.getModel();

			// A new record to add is passed from outside
			if (me.opts.addMailFilter) {
				me.addMailFilter(me.opts.addMailFilter, {
					callback: function(success, model) {
						if (success) me.addFilterRec(model.getData());
					}
				});
			}

			if (mo.get('activeScript') !== me.self.WT_SCRIPT) {
				WT.confirm(me.res('inMailFilters.confirm.notwebtop', WT.getPlatformName()), function(bid) {
					if (bid === 'yes') mo.set('activeScript', me.self.WT_SCRIPT);
				}, this);
			}
		},
		
		addMailFilterUI: function() {
			var me = this;
			me.addMailFilter({}, {
				callback: function(success, model) {
					if (success) me.addFilterRec(model.getData());
				}
			});
		},

		editMailFilterUI: function(rec) {
			var me = this,
				vw = WT.createView(me.mys.ID, 'view.SieveFilter', {
					swapReturn: true
				});

			vw.on('viewsave', function(s, success, model) {
				if (success) me.updateFilterRec(model.getId(), model.getData());
			});
			vw.showView(function() {
				vw.begin('edit', {
					data: me.toSieveFilterData(rec.getData())
				});
			});
		},

		deleteMailFilterUI: function(rec) {
			var me = this,
				grid = me.lref('gpfilters'),
				sto = grid.getStore();

			WT.confirm(WT.res('confirm.delete'), function(bid) {
				if (bid === 'yes') {
					sto.remove(rec);
				}
			}, me);
		},
		
		updateFilterRec: function(id, data) {
			var me = this,
				sto = me.lref('gpfilters').getStore(),
				rec = sto.getById(id);
			if (rec) {
				rec.set(me.fromSieveFilterData(data));
			}
		},

		addFilterRec: function(data) {
			var me = this,
				sto = me.lref('gpfilters').getStore(),
				recData = me.fromSieveFilterData(data);
			recData.order = me.calcNewOrder(sto);
			sto.add(sto.createModel(recData));
		},
		
		calcNewOrder: function(store) {
			var order = 0;
			store.each(function(rec) {
				order = Math.max(order, rec.get('order'));
			}, this, {filtered: true});
			return order+1;
		},

		toSieveFilterData: function(data) {
			return {
				filterId: data.filterId,
				name: data.name,
				enabled: data.enabled,
				match: data.sieveMatch,
				rules: data.sieveRules,
				actions: data.sieveActions
			};
		},

		fromSieveFilterData: function(data) {
			return {
				name: data.name,
				enabled: data.enabled,
				sieveMatch: data.match,
				sieveRules: data.rules,
				sieveActions: data.actions
			};
		}
	},
	
	statics: {
		WT_SCRIPT: 'webtop5'
	}
});
