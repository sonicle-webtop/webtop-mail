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
Ext.define('Sonicle.webtop.mail.ux.SieveActionGrid', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtmailsieveactiongrid',
	requires: [
		'Sonicle.grid.column.Action',
		'Sonicle.grid.plugin.DDOrdering',
		'WTA.ux.data.SimpleModel',
		'Sonicle.webtop.mail.store.SieveActionMethod'
	],
	
	border: false,
	
	config: {
		createDisabled: false,
		updateDisabled: false,
		deleteDisabled: false
	},
	
	/*
	 * @private
	 * @readonly
	 */
	methodStore: null,
	
	initComponent: function() {
		var me = this,
				sid = 'com.sonicle.webtop.mail';
		
		me.methodStore = Ext.create('Sonicle.webtop.mail.store.SieveActionMethod', {
			autoLoad: true
		});
		
		me.columnLines = true;
		me.selModel = 'rowmodel';
		me.plugins = me.plugins || [];
		me.plugins.push(
			{
				ptype: 'cellediting',
				clicksToEdit: 1,
				listeners: {
					beforeedit: function() {
						if (!!me.updateDisabled) return false;
					},
					edit: me.onCellEdit,
					//validateedit: me.onCellValidateEdit,
					scope: me
				}
			}
		);
		
		if (!me.viewConfig) {
			me.viewConfig = {
				deferEmptyText: false,
				emptyText: WT.res(sid, 'wtmailsieveactiongrid.emp'),
				plugins: [
					{
						ptype: 'sogridviewddordering',
						orderField: 'order'
					}
				]
			};
		}
		
		if(!me.columns) {
			me.hideHeaders = true;
			me.columns = [
				{
					xtype: 'solookupcolumn',
					dataIndex: 'method',
					store: me.methodStore,
					displayField: 'desc',
					editor: Ext.create(WTF.lookupCombo('id', 'desc', {
						allowBlank: false,
						store: me.methodStore
					})),
					width: 250
				}, {
					dataIndex: 'argument',
					getEditor: Ext.pass(me.getArgumentEditor, [me]),
					renderer: function(val, meta, rec) {
						var mtd = rec.get('method');
						if (Ext.isEmpty(val)) {
							if (['reject', 'redirect', 'fileinto', 'addflag'].indexOf(mtd) !== -1) {
								return me.styleAsEmpty(WT.res(sid, 'wtmailsieveactiongrid.argument.'+mtd+'.emp'));
							}
						}
						if (mtd === 'addflag') {
							return WT.res(sid, 'store.sieveActionArgFlag.'+val);
						}
						return val;
					},
					flex: 1
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'far fa-trash-alt',
							tooltip: WT.res('act-remove.lbl'),
							disabled: me.deleteDisabled,
							handler: function(g, ridx) {
								g.getStore().removeAt(ridx);
							}
						}
					],
					hidden: me.deleteDisabled
				}
			];
		}

		me.tools = me.tools || [];
		me.tools.push({
			type: 'plus',
			disabled: !!me.createDisabled,
			callback: function() {
				me.addAction();
			}
		});
		
		me.callParent(arguments);
		
		me.editors = {
			'argtext': new Ext.grid.CellEditor({
				field: Ext.create('Ext.form.field.Text', {
					allowBlank: false,
					selectOnFocus: true
				})
			}),
			'argtextarea': new Ext.grid.CellEditor({
				field: Ext.create('Ext.form.field.TextArea', {
					allowBlank: false,
					selectOnFocus: true
				})
			}),
			'argemail': new Ext.grid.CellEditor({
				field: Ext.create('Ext.form.field.Text', {
					allowBlank: false,
					selectOnFocus: true,
					emptyText: WT.res(sid, 'wtmailsieveactiongrid.ed.argemail.emp')
				})
			}),
			'argfolder': new Ext.grid.CellEditor({
				field: Ext.create({
					xtype: 'sotreecombo',
					allowBlank: false,
					store: Ext.create('Ext.data.TreeStore', {
						model: 'Sonicle.webtop.mail.model.ImapTreeModel',
						proxy: WTF.proxy(sid, 'GetImapTree'),
						root: {
							id: '/',
							expanded: true
						},
						rootVisible: false
					})
				})	
			}),
			'argflag': new Ext.grid.CellEditor({
				field: Ext.create(WTF.lookupCombo('id', 'desc', {
					allowBlank: false,
					store: Ext.create('Sonicle.webtop.mail.store.SieveActionArgFlag', {
						autoLoad: true
					})
				}))
			})
		};
	},
	
	destroy: function() {
		var me = this;
		me.methodStore = null;
		me.destroyEditors(me.editors);
		me.callParent();
	},
	
	updateCreateDisabled: function(nv, ov) {
		var hd = this.getHeader();
		if (ov !== null && hd) {
			hd.getTools()[0].setDisabled(nv);
		}
	},
	
	updateDeleteDisabled: function(nv, ov) {
		var cm = this.getColumnManager();
		if (ov !== null && cm) {
			cm.getHeaderAtIndex(2).setHidden(nv);
		}
	},
	
	addAction: function() {
		var me = this,
				edp = me.findPlugin('cellediting'),
				sto = me.getStore();
		
		if (!!me.createDisabled) return;
		edp.completeEdit();
		sto.add(sto.createModel({
			method: 'discard'
		}));
		edp.startEditByPosition({row: sto.getCount(), column: 0});
	},
	
	privates: {
		onCellEdit: function(ed, cntx) {
			if (cntx.field === 'method') {
				var rec = cntx.record,
					nv = cntx.value,
					ov = cntx.originalValue,
					methodsWArg = ['keep','discard','stop'],
					newReqArg = methodsWArg.indexOf(nv) !== -1,
					oldReqArg = methodsWArg.indexOf(ov) !== -1;

				if (newReqArg !== oldReqArg) {
					ed.editors.removeAtKey(rec.getId());
					rec.set({argument: null});
				}
			}
		},
		
		getArgumentEditor: function(grid, record) {
			var column = this,
				method = record.get('method');
			switch(method) {
				case 'reject':
					return grid.getCellEditor(record, column, 'argtextarea');
				case 'redirect':
					return grid.getCellEditor(record, column, 'argemail');
				case 'fileinto':
					return grid.getCellEditor(record, column, 'argfolder');
				case 'addflag':
					return grid.getCellEditor(record, column, 'argflag');
				default:
					return false;
			}
		},
		
		getCellEditor: function(record, column, type) {
			var me = this,
				editor = me.editors[type],
				field;

			field = editor.field;
			if (field && field.ui === 'default' && !field.hasOwnProperty('ui')) {
				field.ui = me.editingPlugin.defaultFieldUI;
			}

			// Give the editor a unique ID because the CellEditing plugin caches them
			editor.editorId = type + record.getId();
			editor.field.column = me.column;
			return editor;
		},
		
		destroyEditors: function(editors) {
			var ed;
			for (ed in editors) {
				if (editors.hasOwnProperty(ed)) {
					Ext.destroy(editors[ed]);
				}
			}
		},
		
		styleAsEmpty: function(text) {
			return '<span style="color:gray;font-style:italic;">' + text + '</span>';
		}
	}
});
