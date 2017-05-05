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

Ext.define('Sonicle.webtop.mail.view.SmartSearchDialog', {
	extend: 'WTA.sdk.DockableView',
	
	dockableConfig: {
		iconCls: 'wtmail-icon-smartsearch-xs',
		title: '{act-smartsearch.lbl}',
		width: 800,
		height: 600
	},
	promptConfirm: false,
	full: true,
	
	mys: null,
	pattern: null,
	
	searchToolbar: null,
	
	initComponent: function() {
		var me = this;
		
		me.pattern=me.pattern||'';
		
		me.searchToolbar=new Ext.Toolbar({
			items: [
				'->',
				Ext.create({
					xtype: 'wtsuggestcombo',
					preventEnterFiringOnPickerExpanded: false,
					sid: me.mys.ID,
					suggestionContext: 'smartsearch',
					width: 400,
					tooltip: me.res('act-smartsearch.tip'),
					value: me.pattern,
					triggers: {
						search: {
							hidden: false,
							cls: Ext.baseCSSPrefix + 'form-search-trigger',
							handler: function(tf) {
								me.runSearch(tf.getValue());
							}
						}
					},
					listeners: {
						enterkey: function(tf,e) {
							me.runSearch(tf.getValue());
						},
						select: function(tf,r,eopts) {
							me.runSearch(tf.getValue());
						}
					}

				})
			]
		});
			
		Ext.apply(me, {
			
			tbar: me.searchToolbar,		
			buttons: [
			]
		});
		
		me.callParent(arguments);
		
		me.add({
			xtype: 'wtpanel',
			region: 'west',
			width: 200,
			layout: 'vbox',
			bodyPadding: '0 10 0 10',
			items: [
				{
					xtype: 'wtform',
					layout: 'vbox',
					width: "100%",
					title: 'Visualizza',
					items: [
						//{ xtype: 'label', html: 'Visualizza' },
						{ xtype: 'checkbox', boxLabel: 'Da me (132)' },
						{ xtype: 'checkbox', boxLabel: 'A me (12)' },
						{ xtype: 'checkbox', boxLabel: 'Allegati (84)' }
					]
				},
				{
					xtype: 'grid',
					title: 'Persone',
					hideHeaders: true,
					width: "100%",
					height: 200,
					cls: '',
					store: {
						fields: ['name', 'email', { name: 'total', type: 'int' }],
						data: [
							{ name: "Ezio Tirelli", email: "ezio.tirelli@encodata.it", total: 145 },
							{ name: "Gabriele Bulfon", email: "gabriele.bulfon@sonicle.com", total: 98 }
						]
					},
					columns: [
						{ dataIndex: 'name', flex: 1, tdCls: 'x-window-header-title-default wtmail-smartsearch-name-column' },
						{ dataIndex: 'total', width: 50, tdCls: 'wtmail-smartsearch-total-column' }
					]
				},
				{
					xtype: 'grid',
					title: 'Cartelle di posta',
					hideHeaders: true,
					width: "100%",
					height: 200,
					store: {
						fields: [ 'id', 'name', { name: 'total', type: 'int' }],
						data: [
							{ id: "Clienti/Encodata", name: "Encodata", total: 320 },
							{ id: "Sonicle", name: "Sonicle", total: 120 }
						]
					},
					columns: [
						{ dataIndex: 'name', flex: 1, tdCls: 'x-window-header-title-default wtmail-smartsearch-name-column' },
						{ dataIndex: 'total', width: 50, tdCls: 'wtmail-smartsearch-total-column' }
					]
				}
			]
			
		});
		me.add({
			xtype: 'container',
			region: 'center',
			layout: 'border',
			id: 'panel1',
			items: [
				{
					xtype: 'wtform',
					reference: 'panelGraph',
					layout: 'border',
					region: 'north',
					height: 100,
					collapsible: true,
					collapsed: true,
					collapseMode: 'header',
					title: 'Cerca&nbsp;:&nbsp;'+me.pattern,
					tools: [
						{
							type: 'graph', handler: function() { me.lref("panelGraph").toggleCollapse(); }
						},
						{
							type: 'empty', handler: function() { me.lref("panelGraph").toggleCollapse(); }
						},
						{
							type: 'empty', handler: function() { me.lref("panelGraph").toggleCollapse(); }
						}
					],
					items: [
						{ xtype: 'label', region: 'center', html: 'GRAFICO' }
					]
				},
				{
					xtype: 'wtpanel',
					layout: 'border',
					region: 'center',
					id: 'panel4',
					tbar: [
						'10 di 999',
						' ',
						' ',
						{ xtype: 'button', text: 'Aprire la posta come lista' },
						'->',
						'Ordina per: '
					],
					items: [
						{
							xtype: 'grid',
							hideHeaders: true,
							region: 'center',
							store: {
								fields: [	{ name: 'uid', type: 'int' }, 'folderid', 'subject', 'from', 'date' ],
								data: [
									{ uid: 1, folderid: "Clienti/Encodata", subject: "Fwd: verifica utenti", from: 'Gabriele Bulfon', date: '10/05/2013' },
									{ uid: 2, folderid: "Sonicle", subject: "Re: push wt5", from: 'Matteo Albinola', date: '13/09/2016' }
								]
							},
							columns: [
								{ dataIndex: 'subject', flex: 1, tdCls: 'x-window-header-title-default wtmail-smartsearch-name-column' },
								{ dataIndex: 'from', width: 100 },
								{ dataIndex: 'date', width: 80 }
							]
						}
					]
				}
			]
		});
	},
	
	runSearch: function(value) {
	},
	
	res: function(key) {
		return this.mys.res(key);
	}
		
});
