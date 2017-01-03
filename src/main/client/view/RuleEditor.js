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


Ext.define('Sonicle.webtop.mail.TreeCombo', {
	extend: 'Ext.form.field.ComboBox',
	alias: ['widget.sotreecombo', 'widget.sotreecombobox'],
	
	tree: null,
	
	createPicker: function() {
		var me = this;

		if (!me.tree) {
			var store = new Ext.data.TreeStore({
				root: {
					expanded: true,
					children: [
						{ text: "Numbers", expanded: false, children: [
							{ text: '1', leaf: true },
							{ text: '2', leaf: true },
							{ text: '3', leaf: true },
							{ text: '4', leaf: true },
							{ text: '5', leaf: true },
						] },
						{ text: "Letters", expanded: true, children: [
							{ text: 'A', leaf: true },
							{ text: 'B', leaf: true },
							{ text: 'C', leaf: true },
							{ text: 'D', leaf: true },
							{ text: 'E', leaf: true },
						] },
						{ text: "Colors", expanded: false, children: [
							{ text: 'Red', leaf: true },
							{ text: 'Green', leaf: true },
							{ text: 'Blue', leaf: true }
						] }
					]
				}
			});

			me.tree = new Ext.tree.Panel({
				width:  100,
				//height: 'auto',
				store:  store,

				rootVisible: false,
				floating: true,
				hidden: true,
				viewConfig: { focusOnToFront: true }
			});

		}
		
		me.tree.pickerField=me;
		me.tree.refresh=function() {
			me.tree.store.reload();
		}

		me.mon(me.tree, {
			afteritemexpand: me.alignPicker,
			afteritemcollapse: me.alignPicker,
			scope:  me
		});

		return me.tree;
	}
});

Ext.define('Sonicle.webtop.mail.view.RuleEditor', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.mail.model.RuleModel'
	],
	
	dockableConfig: {
		title: '{rule-editor.tit}',
		iconCls: 'wt-icon-rules-xs',
		modal: true,
		width: 600,
		height: 450
	},
	promptConfirm: false,
	full: true,
	
	mys: null,
	onSender: '',
	onRecipient: '',
	action: 'file',
	condition: 'ANY',

	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			tbar: [
				{
					xtype: 'combo',
					store: new Ext.data.SimpleStore({
						fields: ['id','desc'],
						data: [
							['ANY',me.res("rule-editor-condition-ANY")],
							['ALL',me.res("rule-editor-condition-ALL")]
						]
					}),
					editable: false,
					mode: 'local',
					width:400,
					listWidth: 400,
					displayField: 'desc',
					triggerAction: 'all',
					value: this.condition,
					valueField: 'id',
					listeners: {
						select: {
							fn: function(c,r,i) {
								this.condition=r.get("id");
							}
						}
					}
				}	
			],
			buttons: [
				{
					xtype: 'button',
					text: WT.res('act-ok.lbl'),
					width: 100,
					handler: function() {
						if (me.fn) me.fn.call(me.scope||me,true);
						me.closeView(false);
					}
				},
				{
					xtype: 'button',
					text: WT.res('act-cancel.lbl'),
					width: 100,
					handler: function() {
						if (me.fn) me.fn.call(me.scope||me,false);
						me.closeView(false);
					}
				}
			]
		});
		
		me.callParent(arguments);
		
		me.add({
			type: 'panel',
            region: 'center',
			bodyPadding: 20,
            layout: {
				type: 'table',
                columns: 2,
                tableAttrs: {
                    style: {
                            width: '100%'
                    }
                }
			},
            items: [
                { border: false, bodyStyle: 'font-size: 20px;', width: 200, html: me.res('rule-editor-if') }, {
					xtype: 'form',
					bodyPadding: 10,
					fieldDefaults: {
						labelWidth: 150,
						labelAlign: 'right'
					},
					items: [
						{ xtype: 'textfield', fieldLabel: me.res('rule-editor-from'), width: 500, value: (me.onSender?me.onSender:'') },
						{ xtype: 'textfield', fieldLabel: me.res('rule-editor-to'), width: 500, value: (me.onRecipient?me.onRecipient:'') },
						{ xtype: 'textfield', fieldLabel: me.res('rule-editor-subject'), width: 500 }
					]

				},
                { border: false, bodyStyle: 'font-size: 20px;', width: 200, html: me.res('rule-editor-then') }, {
					xtype: 'form',
					bodyPadding: 10,
					fieldDefaults: {
						labelWidth: 150,
						labelAlign: 'right'
					},
					items: [
						{ xtype: 'sotreecombo', fieldLabel: me.res('rule-editor-fileinto'), width: 550 },
						{ xtype: 'textfield', fieldLabel: me.res('rule-editor-forward'), width: 550 },
						{ xtype: 'textfield', fieldLabel: me.res('rule-editor-discard'), width: 550 },
						{ xtype: 'textarea', fieldLabel: me.res('rule-editor-reject'), width: 550, height: 80 }
					]

				}
            ]
			
		});
	},
	
	res: function(key) {
		return this.mys.res(key);
	}
});
