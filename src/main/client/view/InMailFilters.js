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
		'Sonicle.webtop.mail.model.InMailFilters',
		'Sonicle.webtop.mail.view.SieveFilter'
	],
	
	dockableConfig: {
		title: '{inMailFilters.tit}',
		iconCls: 'wtmail-icon-inMailFilters-xs',
		width: 560,
		height: 440,
		modal: true
	},
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.mail.model.InMailFilters',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foActiveScriptVisible: WTF.foCompare('record', 'scriptsCount', function(v) {
				return v >= 2;
			}),
			foAutoRespEnabled: WTF.checkboxBind('record.autoResponder', 'enabled')
		});
	},
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			tbar: [
				'-',
				me.addAct('addMailFilter', {
					text: null,
					tooltip: me.mys.res('inMailFilters.addMailFilter.tip'),
					handler: function() {
						me.addMailFilterUI();
					}
				}),
				me.addAct('deleteMailFilter', {
					text: null,
					tooltip: me.mys.res('inMailFilters.deleteMailFilter.tip'),
					handler: function() {
						var sm = me.lref('gpfilters').getSelectionModel();
						me.deleteMailFilterUI(sm.getSelection()[0]);
					},
					disabled: true
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
					fieldLabel: me.mys.res('inMailFilters.fld-activeScript.lbl'),
					labelAlign: 'right',
					labelWidth: 120,
					width: 220,
					listeners: {
						select: function(s, rec) {
							if (rec.getId() !== 'webtop5') {
								WT.warn(me.mys.res('inMailFilters.warn.notwebtop', WT.getPlatformName()));
							}
						}
					}
				})
			]
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
					plugins: [{
						ptype: 'sogridviewddordering',
						orderField: 'order'
					}]
				},
				columns: [{
					xtype: 'soiconcolumn',
					dataIndex: 'enabled',
					header: '',
					sortable: false,
					menuDisabled: true,
					stopSelection: true,
					getIconCls: function (value, rec) {
						return WTF.cssIconCls(WT.XID, 'traffic-light-' + (value ? 'green' : 'red'), 'xs');
					},
					getTip: function(v,rec) {
						if (v === true) {
							return me.mys.res('inMailFilters.gp-enabled.true');
						} else {
							return me.mys.res('inMailFilters.gp-enabled.false');
						}
					},
					handler: function (grid, rix, cix, e, rec) {
						rec.set('enabled', !rec.get('enabled'));
					},
					width: 30
				}, {
					dataIndex: 'name',
					header: me.mys.res('inMailFilters.gp-filters.name.lbl'),
					flex: 1
				}],
				listeners: {
					selectionchange: function(s,recs) {
						me.getAct('deleteMailFilter').setDisabled(!recs.length);
					},
					rowdblclick: function(s, rec) {
						me.editMailFilterUI(rec);
					}
				},
				width: '100%',
				flex: 1
			}, {
				xtype: 'wtform',
				title: me.mys.res('inMailFilters.autoResponder.tit'),
				modelValidation: true,
				defaults: {
					labelWidth: 150
				},
				items: [{
					xtype: 'checkbox',
					reference: 'fldAutoRespEnabled',
					bind: '{foAutoRespEnabled}',
					hideEmptyLabel: false,
					boxLabel: me.mys.res('sieveFilter.autoResponder.fld-enabled.lbl')
				}, {
					xtype: 'textareafield',
					bind: {
						value: '{record.autoResponder.message}',
						disabled: '{!fldAutoRespEnabled.checked}'
					},
					fieldLabel: me.mys.res('sieveFilter.autoResponder.fld-message.lbl'),
					height: 80,
					anchor: '100%'
				}, {
					xtype: 'textfield',
					bind: {
						value: '{record.autoResponder.addresses}',
						disabled: '{!fldAutoRespEnabled.checked}'
					},
					fieldLabel: me.mys.res('sieveFilter.autoResponder.fld-addresses.lbl'),
					emptyText: me.mys.res('sieveFilter.autoResponder.fld-addresses.emp'),
					anchor: '100%'
				}, {
					xtype: 'sospacer'
				}],
				width: '100%'
			}]
		});
		
		me.on('viewload', me.onViewLoad);
	},
	
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
		
		if (mo.get('activeScript') !== 'webtop5') {
			WT.warn(me.mys.res('inMailFilters.warn.notwebtop', WT.getPlatformName()));
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
				vct = WT.createView(me.mys.ID, 'view.SieveFilter');
		
		vct.getView().on('viewsave', function(s, success, model) {
			if (success) me.updateFilterRec(model.getId(), model.getData());
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: me.toSieveFilterData(rec.getData())
			});
		});
	},
	
	deleteMailFilterUI: function(rec) {
		var me = this,
				grid = me.lref('gpfilters'),
				sto = grid.getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				sto.remove(rec);
			}
		}, me);
	},
	
	addMailFilter: function(data, opts) {
		var me = this,
				vct = WT.createView(me.mys.ID, 'view.SieveFilter');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: Ext.applyIf(data || {}, {
					enabled: true,
					match: 'all'
				})
			});
		});
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
		recData.order = sto.getCount()+1;
		sto.add(sto.createModel(recData));
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
});
	