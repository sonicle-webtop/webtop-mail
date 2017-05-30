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
Ext.define('Sonicle.webtop.mail.view.SieveFilter', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.webtop.mail.ux.SieveRuleGrid',
		'Sonicle.webtop.mail.ux.SieveActionGrid',
		'Sonicle.webtop.mail.store.SieveMatch',
		'Sonicle.webtop.mail.model.SieveRule',
		'Sonicle.webtop.mail.model.SieveAction',
		'Sonicle.webtop.mail.model.SieveFilter'
	],
	
	dockableConfig: {
		title: '{sieveFilter.tit}',
		iconCls: 'wtmail-icon-sieveFilter-xs',
		width: 650,
		height: 500
	},
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.mail.model.SieveFilter',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foEnabled: WTF.checkboxBind('record', 'enabled')
		});
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			xtype: 'container',
			region: 'center',
			layout: 'vbox',
			items: [{
				xtype: 'wtform',
				modelValidation: true,
				defaults: {
					labelWidth: 150
				},
				items: [{
						xtype: 'textfield',
						reference: 'fldname',
						bind: '{record.name}',
						fieldLabel: me.mys.res('sieveFilter.fld-name.lbl'),
						width: 500
					}, {
						xtype: 'checkbox',
						bind: '{foEnabled}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('sieveFilter.fld-enabled.lbl')
					}, {
						xtype: 'formseparator'
					},
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.match}',
						store: Ext.create('Sonicle.webtop.mail.store.SieveMatch', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('sieveFilter.fld-match.lbl'),
						width: 500
					})
				],
				width: '100%'
			}, {
				xtype: 'container',
				layout: 'vbox',
				items: [{
					xtype: 'wtmailsieverulegrid',
					reference: 'gprules',
					title: me.mys.res('sieveFilter.rules.tit'),
					store: Ext.create('Ext.data.ArrayStore', {
						model: 'Sonicle.webtop.mail.model.SieveRule'
					}),
					reserveScrollbar: true,
					width: '100%',
					flex: 1
				}, {
					xtype: 'wtmailsieveactiongrid',
					reference: 'gpactions',
					title: me.mys.res('sieveFilter.actions.tit'),
					store: Ext.create('Ext.data.ArrayStore', {
						model: 'Sonicle.webtop.mail.model.SieveAction'
					}),
					reserveScrollbar: true,
					width: '100%',
					flex: 1
				}],
				width: '100%',
				flex: 1
			}]
		});
		
		me.on('viewload', me.onViewLoad);
		me.on('beforemodelvalidate', me.onBeforeModelValidate);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				mo = me.getModel();
		
		me.loadStoreData(me.lref('gprules').getStore(), mo.get('rules'));
		me.loadStoreData(me.lref('gpactions').getStore(), mo.get('actions'));
		me.lref('fldname').focus(true);
	},
	
	onBeforeModelValidate: function(s, model) {
		var me = this;
		model.set('rules', me.dumpStoreData(me.lref('gprules').getStore()));
		model.set('actions', me.dumpStoreData(me.lref('gpactions').getStore()));
		
	},
	
	loadStoreData: function(sto, json) {
		if (json) {
			sto.loadData(Ext.JSON.decode(json, true) || []);
		}
	},
	
	dumpStoreData: function(sto) {
		return Ext.JSON.encode(Ext.pluck(sto.data.items, 'data'));
	}
});
