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
Ext.define('Sonicle.webtop.mail.ux.SieveRuleGrid', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtmailsieverulegrid',
	requires: [
		'Sonicle.grid.column.Action',
		'Sonicle.grid.plugin.DDOrdering',
		'WTA.ux.data.SimpleModel',
		'Sonicle.webtop.mail.store.SieveRuleField',
		'Sonicle.webtop.mail.store.SieveRuleOperator',
		'Sonicle.webtop.mail.store.SieveRuleOperatorText',
		'Sonicle.webtop.mail.store.SieveRuleOperatorSize'
	],
	
	border: false,
	
	/*
	 * @private
	 * @readonly
	 */
	fieldStore: null,
	
	initComponent: function() {
		var me = this,
				sid = 'com.sonicle.webtop.mail';
		
		me.fieldStore = Ext.create('Sonicle.webtop.mail.store.SieveRuleField', {
			autoLoad: true
		});
		
		me.columnLines = true;
		me.selModel = 'rowmodel';
		me.plugins = me.plugins || [];
		me.plugins.push({
			ptype: 'cellediting',
			clicksToEdit: 1,
			listeners: {
				//beforeedit: me.onCellBeforeEdit,
				edit: me.onCellEdit,
				//validateedit: me.onCellValidateEdit,
				scope: me
			}
		});
		
		if(!me.viewConfig) {
			me.viewConfig = {
				deferEmptyText: false,
				emptyText: WT.res(sid, 'wtmailsieverulegrid.emp'),
				plugins: [{
					ptype: 'sogridviewddordering',
					orderField: 'order'
				}]
			};
		}
		
		if(!me.columns) {
			me.hideHeaders = true;
			me.columns = [{
				xtype: 'solookupcolumn',
				dataIndex: 'field',
				store: me.fieldStore,
				displayField: 'desc',
				editor: Ext.create(WTF.lookupCombo('id', 'desc', {
					allowBlank: false,
					store: me.fieldStore
				})),
				width: 150
			}, {
				dataIndex: 'argument',
				getEditor: me.getArgumentEditor.bind(me),
				renderer: function(val, meta, rec) {
					var fld = rec.get('field');
					if (Ext.isEmpty(val)) {
						if (['header'].indexOf(fld) !== -1) {
							return me.styleAsEmpty(WT.res(sid, 'wtmailsieverulegrid.argument.'+fld+'.emp'));
						}
					}
					return val;
				},
				flex: 1
			}, {
				xtype: 'solookupcolumn',
				dataIndex: 'operator',
				store: Ext.create('Sonicle.webtop.mail.store.SieveRuleOperator', {
					autoLoad: true
				}),
				displayField: 'desc',
				getEditor: me.getOperatorEditor.bind(me),
				width: 150
			}, {
				dataIndex: 'value',
				getEditor: me.getValueEditor.bind(me),
				renderer: function(val, meta, rec) {
					var fld = rec.get('field'), op = rec.get('operator');
					if (Ext.isEmpty(val)) {
						if (['size'].indexOf(fld) !== -1) {
							return me.styleAsEmpty(WT.res(sid, 'wtmailsieverulegrid.value.'+fld+'.emp'));
						} else {
							if (op.indexOf('multi') !== -1) {
								return me.styleAsEmpty(WT.res(sid, 'wtmailsieverulegrid.valuemulti.emp'));
							} else {
								return me.styleAsEmpty(WT.res(sid, 'wtmailsieverulegrid.value.emp'));
							}
						}
					}
					if (fld === 'size') {
						var bytes = parseInt(val);
						if (Ext.isNumber(bytes)) return Sonicle.Bytes.format(bytes);	
					}
					return val;
				},
				flex: 2
			}, {
				xtype: 'soactioncolumn',
				items: [
					{
						iconCls: 'far fa-trash-alt',
						tooltip: WT.res('act-remove.lbl'),
						handler: function(g, ridx) {
							g.getStore().removeAt(ridx);
						}
					}
				]
			}];
		}

		me.tools = me.tools || [];
		me.tools.push({
			type: 'plus',
			callback: function() {
				me.addRule();
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
			'optext': new Ext.grid.CellEditor({
				field: Ext.create(WTF.lookupCombo('id', 'desc', {
					allowBlank: false,
					store: Ext.create('Sonicle.webtop.mail.store.SieveRuleOperatorText', {
						autoLoad: true
					})
				}))
			}),
			'opsize': new Ext.grid.CellEditor({
				field: Ext.create(WTF.lookupCombo('id', 'desc', {
					allowBlank: false,
					store: Ext.create('Sonicle.webtop.mail.store.SieveRuleOperatorSize', {
						autoLoad: true
					})
				}))
			}),
			'vtext': new Ext.grid.CellEditor({
				field: Ext.create('Ext.form.field.Text', {
					allowBlank: false,
					selectOnFocus: true
				})
			}),
			'vsize': new Ext.grid.CellEditor({
				field: Ext.create('Sonicle.form.field.Bytes', {
					allowBlank: false,
					selectOnFocus: true,
					emptyText: WT.res(sid, 'wtmailsieverulegrid.ed.vsize.emp')
				})
			})
		};
	},
	
	destroy: function() {
		var me = this;
		me.callParent();
		me.fieldStore = null;
	},
	
	onCellBeforeEdit: function(ed, cntx) {
		//console.log('onCellBeforeEdit');
	},
	
	onCellEdit: function(ed, cntx) {
		if (cntx.field === 'field') {
			var rec = cntx.record,
					nv = cntx.value,
					ov = cntx.originalValue;

			if ((nv === 'size' && ov !== 'size')
					|| (nv === 'header' && ov !== 'header')
					|| (ov === 'size' || ov === 'header')) {

				ed.editors.removeAtKey(rec.getId());
				rec.set({
					argument: null,
					operator: (nv === 'size') ? 'greaterthan' : 'contains',
					value: null
				});
			}
		}
	},
	
	onCellValidateEdit: function(ed, cntx) {
		//console.log('onCellBeforeEdit');
	},
	
	getArgumentEditor: function(rec, col) {
		if (!rec) return false;
		var field = rec.get('field');
		return (field === 'header') ? this.getCellEditor('argtext', rec) : false;
	},
	
	getOperatorEditor: function(rec, col) {
		if (!rec) return false;
		var field = rec.get('field'), key;
		if (field === 'size') {
			key = 'opsize';
		} else {
			key = 'optext';
		}
		return this.getCellEditor(key, rec);
	},
	
	getValueEditor: function(rec, col) {
		if (!rec) return false;
		var field = rec.get('field'), key;
		if (field === 'size') {
			key = 'vsize';
		} else {
			key = 'vtext';
		}
		return this.getCellEditor(key, rec);
	},
	
	getCellEditor: function(key, rec) {
		var me = this,
				ed = me.editors[key],
				fld;
		
		fld = ed.field;
		if (fld && fld.ui === 'default' && !fld.hasOwnProperty('ui')) {
			fld.ui = me.editingPlugin.defaultFieldUI;	
		}
		
		// Give the editor a unique ID because the CellEditing plugin caches them
		ed.editorId = rec.getId();
		//ed.field.column = me.valueColumn;
		return ed;
	},
	
	styleAsEmpty: function(text) {
		return '<span style="color:gray;font-style:italic;">' + text + '</span>';
	},
	
	addRule: function() {
		var me = this,
				edp = me.findPlugin('cellediting'),
				sto = me.getStore();
		
		edp.completeEdit();
		sto.add(sto.createModel({
			field: 'subject',
			operator: 'contains'
		}));
		edp.startEditByPosition({row: sto.getCount(), column: 0});
	},
	
	
	privates: {
		
		onAddClick: function() {
			var me = this;
			me.picker = me.createPicker();
			me.picker.show();
		},
		
		onRemoveClick: function() {
			var me = this,
					rec = me.getSelection()[0];
			if(rec) me.getStore().remove(rec);
		},

		onPickerPick: function(s, val, rec) {
			var me = this;
			me.fireEvent('pick', me, val, rec);
			me.picker.close();
			me.picker = null;
		}
	}
});
