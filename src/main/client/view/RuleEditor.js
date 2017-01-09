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


Ext.define('Sonicle.webtop.mail.view.RuleEditor', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.mail.model.RuleModel',
		'Sonicle.form.field.TreeComboBox'
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
	record: null,
	store: null,

	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			tbar: [
				{
					xtype: 'combo',
					reference: "cboCondition",
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
					value: me.record?me.record.get("condition"):'ANY',
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
						var r=me.record,s=me.store;
							action=me.lref("grpActions").getValue().ruleaction,
							value=me.lref("fld"+action).getValue(),
							condition=me.lref('cboCondition').getValue(),
							from=me.lref('fldFrom').getValue(),
							to=me.lref('fldTo').getValue(),
							subject=me.lref('fldSubject').getValue();
							
						if (me.fn) me.fn.call(me.scope||me,true);
						if (r) {
							r.set('condition',condition);
							r.set('action',action);
							r.set('value',value);
							r.set('from',from);
							r.set('to',to);
							r.set('subject',subject);
						} else {
							s.add({
								active: true,
								condition: condition,
								action: action,
								value: value,
								from: from,
								to: to,
								subject: subject
							});
						}
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
                { border: false, bodyStyle: 'font-size: 20px;', width: 100, html: me.res('rule-editor-if') }, {
					xtype: 'form',
					border: true,
					bodyPadding: 10,
					fieldDefaults: {
						labelWidth: 150,
						labelAlign: 'right'
					},
					items: [
						{ xtype: 'textfield', reference: 'fldFrom', fieldLabel: me.res('rule-editor-from'), width: 400, value: (me.record?me.record.get("from"):'') },
						{ xtype: 'textfield', reference: 'fldTo', fieldLabel: me.res('rule-editor-to'), width: 400, value: (me.record?me.record.get("to"):'') },
						{ xtype: 'textfield', reference: 'fldSubject', fieldLabel: me.res('rule-editor-subject'), width: 400, value: (me.record?me.record.get("subject"):'') }
					]

				},
                { border: false, bodyStyle: 'font-size: 20px;', width: 100, html: me.res('rule-editor-then') }, {
					xtype: 'radiogroup',
					reference: 'grpActions',
					border: true,
					padding: 10,
					columns: [300],
					items: [
						{
							xtype: 'fieldcontainer',
							layout: 'hbox',
							padding: 10,
							getFormId: function() { return null; },
							items: [	
								{
									xtype: 'radio',
									reference: 'rdoFile',
									checked: true,
									boxLabel: me.res('rule-editor-file'),
									name: "ruleaction",
									inputValue: 'FILE',
									width: 100
								},
								{ 
									xtype: 'sotreecombo',
									reference: 'fldFILE',
									width: 300,
									store: Ext.create('Ext.data.TreeStore', {
										model: 'Sonicle.webtop.mail.model.ImapTreeModel',
										proxy: WTF.proxy(me.mys.ID,'GetImapTree'),
										root: {
											text: 'Imap Tree',
											expanded: true
										},
										rootVisible: false
									}),
									value: '',
									listeners: {
										focus: {
											fn: function() {
												me.lref('rdoFile').setValue(true);
											}
										}
									}
								}
							]
						},
						{
							xtype: 'fieldcontainer',
							layout: 'hbox',
							padding: 10,
							getFormId: function() { return null; },
							items: [
								{
									xtype: 'radio',
									reference: 'rdoForward',
									checked: false,
									boxLabel: me.res('rule-editor-forward'),
									name: "ruleaction",
									inputValue: 'FORWARD',
									width: 100
								},
								{
									xtype: 'textfield', 
									reference: 'fldFORWARD',
									value: '',
									width: 300 ,
									listeners: {
										focus: {
											fn: function() {
												me.lref('rdoForward').setValue(true);
											}
										}
									}
								}
							]
						},
						{
							xtype: 'fieldcontainer',
							layout: 'hbox',
							padding: 10,
							getFormId: function() { return null; },
							items: [
								{
									xtype: 'radio',
									checked: false,
									boxLabel: me.res('rule-editor-discard'),
									name: "ruleaction",
									inputValue: 'DISCARD',
									width: 100
								},
								{
									xtype: 'textfield', 
									reference: 'fldDISCARD',
									value: '',
									width: 300,
									hidden: true
								}
							]
						},
						{
							xtype: 'fieldcontainer',
							layout: 'hbox',
							padding: 10,
							getFormId: function() { return null; },
							items: [
								{
									xtype: 'radio',
									reference: 'rdoReject',
									checked: false,
									boxLabel: me.res('rule-editor-reject'),
									name: "ruleaction",
									inputValue: 'REJECT',
									width: 100
								},
								{
									xtype: 'textarea', 
									reference: 'fldREJECT',
									value: '',
									width: 300,
									height: 80,
									listeners: {
										focus: {
											fn: function() {
												me.lref('rdoReject').setValue(true);
											}
										}
									}
								}
							]
						}
					]

				}
            ]
			
		});
		
		if (me.record) {
			var action=me.record.get('action');
			me.lref("grpActions").setValue({ ruleaction: action });
			me.lref("fld"+action).setValue(me.record.get("value"));
		}
	},
	
	res: function(key) {
		return this.mys.res(key);
	}
});
