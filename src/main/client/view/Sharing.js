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
Ext.define('Sonicle.webtop.mail.view.Sharing', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.Data',
		'Sonicle.form.field.ComboBox',
		'WTA.model.AclSubjectLkp',
		'Sonicle.webtop.mail.model.Sharing'
	],
	
	dockableConfig: {
		title: '{sharing.tit@com.sonicle.webtop.core}',
		width: 800,
		height: 590
	},
	modelName: 'Sonicle.webtop.mail.model.Sharing',
	fieldTitle: 'description',
	
	viewModel: {
		data: {
			data: {
				preset: null
			}
		}
	},

	constructor: function(cfg) {
		var me = this;
		me.methodName = Ext.id(null, 'method-');
		me.callParent([cfg]);
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			foIsAccount: WTF.foIsEqual('record', 'method', 'all'),
			foShowApplyTo: WTF.foIsEmpty('record', 'method', true),
			foApplyTo: WTF.radioGroupBind('record', 'method', me.methodName),
			foShowRights: WTF.foMultiGetFn(undefined, ['gprights.selection', 'data.preset'], function(v) {
				if (!v['gprights.selection'] || v['data.preset'] !== 'custom') return false;
				return true;
			})
		});
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.aclSubjectStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.AclSubjectLkp',
			proxy: WTF.proxy(WT.ID, 'LookupAclSubjects', null, {
				extraParams: {
					users: true,
					groups: false
				}
			})
		});
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'wtfieldspanel',
					paddingTop: true,
					paddingSides: true,
					flex: 1,
					layout: 'fit',
					items: [
						{
							xtype: 'gridpanel',
							reference: 'gprights',
							bind: {
								store: '{record.rights}'
							},
							border: true,
							columns: [
								{
									dataIndex: 'subjectDescription',
									header: me.res('sharing.gp-rights.subjectName.lbl'),
									flex: 2
								}, {
									xtype: 'checkcolumn',
									dataIndex: 'shareIdentity',
									text: me.res('sharing.gp-rights.opts.shareIdentity.lbl'),
									flex: 1,
									listeners: {
										checkchange: function(s, ridx, val, rec) {
											if (!val) {
												rec.set('forceMailcard', false);
												rec.set('alwaysCc', false);
											}
										}
									}
								}, {
									xtype: 'checkcolumn',
									text: me.res('sharing.gp-rights.opts.forceMailcard.lbl'),
									dataIndex: 'forceMailcard',
									flex: 1,
									listeners: {
										checkchange: function(s, ridx, val, rec) {
											if (val) {
												rec.set('shareIdentity', true);
											}
										}
									}
								},{
									xtype: 'checkcolumn',
									text: me.res('sharing.gp-rights.opts.alwaysCc.lbl'),
									dataIndex: 'alwaysCc',
									flex: 1,
									listeners: {
										checkchange: function(s, ridx, val, rec) {
											if (val) {
												rec.set('shareIdentity', true);
											} else {
												rec.set('alwaysCcEmail', '');
											}
										}
									}
								},{
									text: me.mys.res('sharing.gp-rights.opts.alwaysCcEmail.lbl'),
									dataIndex: 'alwaysCcEmail',
									editor: 'textfield',
									renderer: function(val, meta, rec) {
										if (Ext.isEmpty(val) && rec.get('alwaysCc')) {
											return me.styleAsEmpty(me.mys.getIdentity(0).email);
										}
										return val;
									},
									flex: 1
								}, {
									xtype: 'soactioncolumn',
									items: [
										{
											iconCls: 'wt-glyph-clone',
											tooltip: WT.res('act-clone.lbl'),
											handler: function(g, ridx) {
												var rec = g.getStore().getAt(ridx);
												me.cloneRightsUI(rec);
											}
										}, {
											iconCls: 'wt-glyph-delete',
											tooltip: WT.res('act-remove.lbl'),
											handler: function(g, ridx) {
												var rec = g.getStore().getAt(ridx);
												me.deleteRightsUI(rec);
											}
										}
									]
								}
							],
							tbar: [
								{
									text: WT.res('act-add.lbl'),
									ui: '{secondary|toolbar}',
									handler: function() {
										me.addRightsUI();
									}
								}
							],
							listeners: {
								selectionchange: function(s, recs) {
									if (!Ext.isEmpty(recs)) {
										me.getVM().set('data.preset', recs[0].guessPreset());
									}
								}
							}
						}
					]
				}, {
					xtype: 'wtfieldspanel',
					paddingTop: true,
					paddingSides: true,
					defaults: {
						labelWidth: 140
					},
					items: [
						{
							xtype: 'fieldcontainer',
							bind: {
								hidden: '{!foShowRights}'
								//fieldLabel: '{foItemsRightsLabel}'
							},
							layout: {
								type: 'table',
								columns: 3
							},
							defaults: {
								xtype: 'checkbox',
								width: 200
							},
							items: [
								{
									bind: '{gprights.selection.l}',
									boxLabel: me.res('sharing.fld-l.lbl')
								}, {
									bind: '{gprights.selection.r}',
									boxLabel: me.res('sharing.fld-r.lbl')
								}, {
									bind: '{gprights.selection.s}',
									boxLabel: me.res('sharing.fld-s.lbl')
								}, {
									bind: '{gprights.selection.w}',
									boxLabel: me.res('sharing.fld-w.lbl')
								}, {
									bind: '{gprights.selection.i}',
									boxLabel: me.res('sharing.fld-i.lbl')
								}, {
									bind: '{gprights.selection.p}',
									boxLabel: me.res('sharing.fld-p.lbl')
								}, {
									bind: '{gprights.selection.k}',
									boxLabel: me.res('sharing.fld-k.lbl')
								}, {
									bind: '{gprights.selection.a}',
									boxLabel: me.res('sharing.fld-a.lbl')
								}, {
									bind: '{gprights.selection.x}',
									boxLabel: me.res('sharing.fld-x.lbl')
								}, {
									bind: '{gprights.selection.t}',
									boxLabel: me.res('sharing.fld-t.lbl')
								}, /*{
									bind: '{gprights.selection.n}',
									boxLabel: me.mys.res('sharing.fld-n.lbl')
								},*/ {
									bind: '{gprights.selection.e}',
									boxLabel: me.res('sharing.fld-e.lbl')
								}
							],
							fieldLabel: me.res('sharing.folderRights.lbl')
						}
					]
				}, {
					xtype: 'wtfieldspanel',
					paddingSides: true,
					defaults: {
						labelWidth: 140
					},
					items: [
						WTF.lookupCombo('id', 'desc', {
							reference: 'cbopresets',
							bind: {
								value: '{data.preset}',
								disabled: '{!gprights.selection}'
							},
							store: {
								type: 'array',
								autoLoad: true,
								fields: [
									{name: 'id', type: 'string'},
									{name: 'desc', type: 'string'},
									{name: 'info', type: 'string'}
								],
								data: [
									['ro', me.res('sharing.cbo-presets.ro.lbl'), me.res('sharing.cbo-presets.ro.tip')],
									['full', me.res('sharing.cbo-presets.full.lbl'), me.res('sharing.cbo-presets.full.tip')],
									['custom', me.res('sharing.cbo-presets.custom.lbl'), me.res('sharing.cbo-presets.custom.tip')]
								]
							},
							matchFieldWidth: false,
							listConfig: {
								getInnerTpl: function(displayField) {
									return '{'+displayField+'}</br>'
										+ '<span class="wt-text-off wt-color-off">{info}</span>';
								},
								width: 400
							},
							fieldLabel: WT.res('folderSharing.cbo-presets.lbl'),
							listeners: {
								select: function(s, rec) {
									var preset = rec.get('id'), recs;
									if ('custom' !== preset) {
										recs = me.lref('gprights').getSelection();
										if (!Ext.isEmpty(recs)) {
											recs[0].applyPreset(preset);
										}
									}
								}
							},
							width: 300
						})
					]
				}
			]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	doDestroy: function() {
		var me = this;
		delete me.subjectPicker;
		delete me.aclSubjectStore;
		me.callParent();
	},
	
	formatFieldTitle: function(model) {
		var me = this;
		if (model && model.getId() === '/') {
			return Ext.String.format(me.fieldTitleFormat, Sonicle.String.htmlEncode(WT.getVar('userDisplayName') || ''));
		} else {
			return me.callParent(arguments);
		}
	},
	
	initTBar: function() {
		var me = this,
			SoU = Sonicle.Utils;
		
		me.dockedItems = SoU.mergeDockedItems(me.dockedItems, 'top', [
			me.createTopToolbar1Cfg([
				me.res('sharing.apply-to.lbl')+':',
				' ',
				{
					xtype: 'radiogroup',
					bind: {
						value: '{foApplyTo}',
						hidden: '{!foShowApplyTo}'
					},
					hidden: true,
					layout: 'hbox',
					defaults: {
						name: me.methodName,
						margin: '0 20 0 0'
					},
					items: [
						{
							boxLabel: me.res('sharing.chk-this.lbl'),
							bind: {
								hidden: '{foIsAccount}'
							},
							inputValue: 'this'
						}, {
							boxLabel: me.res('sharing.chk-branch.lbl'),
							bind: {
								hidden: '{foIsAccount}'
							},
							inputValue: 'branch'
						}, {
							boxLabel: me.res('sharing.chk-all.lbl'),
							bind: {
								hidden: '{!foIsAccount}'
							},
							inputValue: 'all'
						}
					]
				}
			])
		]);
	},
	
	addRightsUI: function() {
		this.showSubjectPicker();
	},
	
	deleteRightsUI: function(rec) {
		var me = this,
			sto = me.lref('gprights').getStore();
		sto.remove(rec);
	},
	
	cloneRightsUI: function(rec) {
		var me = this;
		me.cloneRec = rec;
		me.showSubjectPicker();
	},
	
	privates: {
		styleAsEmpty: function(text) {
			return '<span style="color:gray;font-style:italic;">' + text + '</span>';
		},
		
		onViewLoad: function(s, success) {
			var me = this,
				mo = me.getModel();
			
			if (success) {
				mo.getProxy().setTimeout(WT.getVar('ajaxLongTimeout'));
			}
		},
		
		addRights: function(subjectSid, cloneRec) {
			var me = this,
				grid = me.lref('gprights'),
				sto = grid.getStore(),
				data, rec, subDesc, subRec;
			
			if (sto.indexOfId(subjectSid) !== -1) return null;
			if (cloneRec) data = Sonicle.Object.remap(cloneRec.getData(), ['shareIdentity', 'forceMailcard', 'alwaysCc', 'l', 'r', 's', 'w', 'i', 'p', 'k', 'a', 'x', 't', 'n', 'e']);
			
			subDesc = null;
			if (me.aclSubjectStore) {
				subRec = me.aclSubjectStore.getById(subjectSid);
				if (subRec) subDesc = subRec.get('name') + ' [' + subRec.get('desc') + ']';
			}
			rec = sto.add(Ext.apply(data || {}, {
					subjectSid: subjectSid,
					subjectDescription: subDesc
				}, {
					l: true,
					r: true,
					s: true
			}))[0];
			return rec;
		},
		
		showSubjectPicker: function() {
			var me = this,
				usedSubjects = Sonicle.Data.collectValues(me.lref('gprights').getStore());
			me.subjectPicker = me.createSubjectPicker();
			me.subjectPicker.getComponent(0).setSkipValues(usedSubjects);
			me.subjectPicker.show();
		},
		
		createSubjectPicker: function() {
			var me = this;
			return Ext.create({
				xtype: 'wtpickerwindow',
				title: me.res('sharing.subjectPicker.tit'),
				height: 350,
				items: [
					{
						xtype: 'solistpicker',
						store: {
							xclass: 'Ext.data.ChainedStore',
							source: me.aclSubjectStore
						},
						valueField: 'id',
						displayField: 'name',
						searchField: 'search',
						emptyText: WT.res('grid.emp'),
						searchText: WT.res('textfield.search.emp'),
						selectedText: WT.res('grid.selected.lbl'),
						okText: WT.res('act-ok.lbl'),
						cancelText: WT.res('act-cancel.lbl'),
						allowMultiSelection: true,
						listeners: {
							cancelclick: function() {
								if (me.subjectPicker) me.subjectPicker.close();
							}
						},
						handler: me.onSubjectPickerPick,
						scope: me
					}
				]
			});
		},
		
		onSubjectPickerPick: function(s, values, recs, button) {
			var me = this, lastRec;
			Ext.iterate(values, function(value) {
				lastRec = me.addRights(value, me.cloneRec);
			});
			delete me.cloneRec;
			me.subjectPicker.close();
			me.subjectPicker = null;
			if (lastRec) me.lref('gprights').setSelection(lastRec);
		}
	}
});
