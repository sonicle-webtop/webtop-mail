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

Ext.define('Sonicle.webtop.mail.view.RulesManager', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.mail.model.RuleModel'
	],
	
	dockableConfig: {
		title: '{rules-manager.tit}',
		iconCls: 'wt-icon-rules-xs',
		width: 700,
		height: 500
	},
	promptConfirm: false,
	full: true,
	
	mys: null,
	
	context: 'INBOX',
	vactive: false,
	vmessage: '',
	vaddress: '',
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			tbar: [
				{
					xtype: 'button',
					iconCls: 'wt-icon-save-xs', 
					tooltip: WT.res('act-saveClose.lbl'),
					text: WT.res('act-saveClose.lbl'),
					handler: me.actionSave,
					scope: me
				},
				'-',
				{
					xtype: 'button',
					iconCls: 'wt-icon-new-rule-xs', 
					tooltip: me.res('rules-manager-act-new.lbl'),
					handler: me.actionNew,
					scope: me
				},
				{
					xtype: 'button',
					iconCls: 'wt-icon-delete-xs', 
					tooltip: WT.res('act-delete.lbl'),
					handler: me.actionDelete,
					scope: me
				},
				'-',
				me.res('rules-manager-context-label'),
				{
					xtype: 'combo',
					store: Ext.create('Ext.data.SimpleStore',{
						fields: ['id','desc'],
						data: [
							['INBOX',me.res("rules-manager-context-INBOX")],
							['SENT',me.res("rules-manager-context-SENT")]
						]
					}),
					editable: false,
					mode: 'local',
					width: 150,
					listWidth: 200,
					displayField: 'desc',
					triggerAction: 'all',
					value: me.context,
					valueField: 'id',
					listeners: {
						select: {
							fn: function(c,r,i) {
								if (r.get("id")!=me.context) {
									me.context=r.get("id");
									me.reloadContext();
								}
							}, 
							scope: me 
						}
					}
				}
			]
		});
		
		me.callParent(arguments);
		
        me.add({
			xtype: 'gridpanel',
			selModel: {
				type: 'sorowmodel',
				mode: 'MULTI',
				listeners: {
					
				}
			},
			multiColumnSort: false,
            region: "center",
            //plugins: [gddro],
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.mail.model.RuleModel',
				proxy: WTF.apiProxy(me.mys.ID, 'ListRules', 'rules',{
					extraParams: {
						context: me.context
					}
				})
			},
            columns: [
                {//Row
                    header: ' ',
                    width: 24,
                    sortable: false,
                    dataIndex: 'row',
                    align: 'right'
                },{//Active
                    header: ' ',
                    width: 24,
                    sortable: false,
                    menuDisabled: true,
                    dataIndex: 'active',
                    /*renderer: function(value,metadata,record,rowIndex,colIndex,store) {
                        metadata.css=value?"iconOn":"iconOff";
                        return " ";
                    }*/
                },{//Description
                    header: me.res("rules-manager-column-description"),
                    width: 325,
                    sortable: false,
                    dataIndex: 'description'
                },{//Action
                    header: me.res("rules-manager-column-action"),
                    width: 60,
                    sortable: false,
                    dataIndex: 'action'
                },{//Value
                    header: me.res("rules-manager-column-value"),
                    width: 225,
                    sortable: false,
                    dataIndex: 'value'
                }
            ],
			listeners: {
				cellclick: { fn: me.cellClicked, scope: me },
				rowdblclick: { fn: me.rowDblClicked, scope: me },
				render: function(g,opts) {
					var map = new Ext.util.KeyMap({
							target: g.getEl(),
							binding: [
								{
									key: Ext.event.Event.DELETE,
									shift: false,
									fn: function(key,ev) {
										me.actionDelete();
									}
								}
							]
						});		
				}
			}
        });
        me.add({
			xtype: 'form',
            region: "south",
			bodyPadding: 10,
            title: me.res('rules-manager-vacation-title'),
            height: 200,
			fieldDefaults: {
				labelAlign: 'left',
				labelWidth: 200
			},
            items: [
				{
					xtype: 'hidden',
					name: '_edited',
					submitValue: false,
					value: false
				},
                {
					xtype: 'fieldcontainer',
					fieldLabel: me.res('rules-manager-vacation-active'),
					defaultType: 'radiofield',
					defaults: {
						flex: 1
					},
					layout: 'hbox',					
                    height: 20,
                    width: 400,
                    items:[
						{
							xtype: 'radio',
							boxLabel: WT.res('word.yes'),
							checked: me.vactive,
							name: "vactive",
							listeners: {
								change: {
									fn: function(r,v) {
										me.lref("vtextmsg").setDisabled(!v);
										me.lref("vtextaddr").setDisabled(!v);
										me.vactive=v;
									}
								}
							}
						},
						{
							xtype: 'radio',
							boxLabel: WT.res('word.no'),
							checked: !me.vactive,
							name: "vactive"
						}
					]
                },
                {
					xtype: 'textarea',
					reference: "vtextmsg",
					width: 600,
					height: 80,
					fieldLabel: me.res('rules-manager-vacation-message'),
					value: me.vmessage,
					disabled: !me.vactive
				},
                {
					xtype: 'textfield',
					reference: "vtextaddr",
					width: 600,
					fieldLabel: me.res('rules-manager-vacation-addresses'),
					value: me.vaddresses,
					disabled: !me.vactive
				},
            ]
		});
	},
	
	actionNew: function() {
		var me=this;
		
		WT.createView(me.mys.ID,'view.RuleEditor',{
			viewCfg: {
				mys: me
			}
		}).show();
	},
	
	res: function(key) {
		return this.mys.res(key);
	}
	
});
