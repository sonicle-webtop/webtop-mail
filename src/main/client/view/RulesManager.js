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
	full: true,
	confirm: 'yn',
	
	dirty: false,
	
	mys: null,
	
	context: 'INBOX',
	vacation: {
		active: true,
		message: '',
		addresses: '',		
	},
	
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
				}/*,
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
				}*/
			]
		});
		
		me.callParent(arguments);
		
        me.add({
			xtype: 'gridpanel',
			reference: 'grdRules',
			selModel: {
				type: 'sorowmodel',
				mode: 'MULTI',
				listeners: {
					
				}
			},
			multiColumnSort: false,
            region: "center",
			viewConfig: {
				plugins: {
					ptype: 'gridviewdragdrop',
					dragGroup: 'ddRules',
					dropGroup: 'ddRules'
				}/*,
				listeners: {
					drop: function(node, data, dropRec, dropPosition) {
						if (dropRec) {
							var s=dropRec.store,
								ix=s.indexOf(dropRec);
							if (dropPosition==='after') ++ix;
							
							dropRec.store.remove(data.records);
							dropRec.store.insert(ix,data.records);
						}
					}
				}*/
			},
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
                    dataIndex: 'rule_id',
                    align: 'right',
					hidden: true
                },{//Active
					xtype: 'soiconcolumn',
					stopSelection: true,
                    header: ' ',
                    width: 30,
                    sortable: false,
                    menuDisabled: true,
                    dataIndex: 'active',
					getIconCls: function(value,rec) {
						return WTF.cssIconCls(WT.XID, 'traffic-light-'+(value?'green':'red'), 'xs');
					},
					handler: function(grid, rix, cix, e, rec) {
						var active=!rec.get('active');
						rec.set("active",active);
					}
                    /*renderer: function(value,metadata,record,rowIndex,colIndex,store) {
                        metadata.css=value?"iconOn":"iconOff";
                        return " ";
                    }*/
                },{//Description
                    header: me.res("rules-manager-column-description"),
                    width: 325,
                    sortable: false,
					renderer: function(v,md,r,ri,ci,s) {
						var val=me._appendConditionDescription(r,"from","",true);
						val=me._appendConditionDescription(r,"to",val,true);
						val=me._appendConditionDescription(r,"subject",val,false);
						
						return val;
					}
                },{//Action
                    header: me.res("rules-manager-column-action"),
                    width: 80,
                    sortable: false,
                    dataIndex: 'action',
					renderer: function(value,metadata,record,rowIndex,colIndex,store) {
						return me.res("rule-editor-"+value.toLowerCase());
					}
                },{//Value
                    header: me.res("rules-manager-column-value"),
                    width: 225,
                    sortable: false,
                    dataIndex: 'value'
                }
            ],
			listeners: {
				rowdblclick: {
					fn: function(g,r) {
						me.actionEdit(r);
					}
				},
				render: {
					fn: function(g,opts) {
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
							checked: me.vacation.active,
							name: "vactive",
							listeners: {
								change: {
									fn: function(r,v) {
										me.lref("vtextmsg").setDisabled(!v);
										me.lref("vtextaddr").setDisabled(!v);
										me.vacation.active=v;
										me.dirty=true;
									}
								}
							}
						},
						{
							xtype: 'radio',
							boxLabel: WT.res('word.no'),
							checked: !me.vacation.active,
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
					value: me.vacation.message,
					disabled: !me.vacation.active
				},
                {
					xtype: 'textfield',
					reference: "vtextaddr",
					width: 600,
					fieldLabel: me.res('rules-manager-vacation-addresses'),
					value: me.vacation.addresses,
					disabled: !me.vacation.active
				},
            ]
		});
	},
	
	actionNew: function() {
		var me=this;
		
		WT.createView(me.mys.ID,'view.RuleEditor',{
			viewCfg: {
				mys: me,
				store: me.lref('grdRules').getStore()
			}
		}).show();
		me.dirty=true;
	},
	
	actionEdit: function(r) {
		var me=this;
		
		WT.createView(me.mys.ID,'view.RuleEditor',{
			viewCfg: {
				mys: me,
				record: r
			}
		}).show();
		me.dirty=true;
	},
	
	actionDelete: function() {
		var me=this,
			grid=me.lref("grdRules"),
			sel=grid.getSelection();
	
		if (sel) {
			grid.getStore().remove(sel);
			me.dirty=true;
		}
	},
	
	actionSave: function() {
		var me=this,
			params={
				rules: Ext.util.JSON.encode(Ext.Array.pluck(me.lref("grdRules").store.getRange(),'data')),
				vactive: me.vacation.active,
				vmessage: me.lref("vtextmsg").getValue(),
				vaddresses: me.lref("vtextaddr").getValue()
			};
			
		me.mask(WT.res("saving"));
	
		WT.ajaxReq(me.mys.ID, 'SaveRules', {
			params: params,
			callback: function(success,json) {
				if (success) {
					me.mys.varsData.vacationActive=params.vactive;
					me.mys.varsData.vacationMessage=params.vmessage;
					me.mys.varsData.vacationAddresses=params.vaddresses;
					me.closeView(false);
				} else {
					me.unmask();
					WT.error(json.message);
				}
			}
		});					
		
	},
	
	canCloseView: function() {
		var me=this;
		return !me.dirty && !me.lref("vtextmsg").dirty && !me.lref("vtextaddr").dirty;
	},
	
	_appendConditionDescription: function(r,f,v,spaces) {
		var me=this,fv=r.get(f);
		if (fv && !Ext.isEmpty(fv)) {
			if (Ext.isEmpty(v)) v=me.res("rule-editor-if")+" ";
			else v+=" "+(r.get("condition")==='ANY'?me.res("rule-editor-or"):me.res("rule-editor-and"))+" ";
		
			v+=me.res("rules-manager-"+f)+" ";
			
			var regex="[\\s,\\,\\;]";
			if (!spaces) regex="\\z";
			if (fv.indexOf('"')>=0) regex="\"";
			var tokens=fv.split(regex),
			    brkts=tokens.length>1;
			if (brkts) v+="(";
			
			var vv="";
			Ext.each(tokens,function(ttoken,ix) {
				ttoken=ttoken.trim();
				if (ttoken.length>0) {
				  if (ttoken.charAt(0)===',' || ttoken.charAt(0)===';')
					ttoken=ttoken.substring(1);
				  if (ttoken.length>0) {
					  if (!Ext.isEmpty(vv)) vv+=" "+me.res("rule-editor-or")+" ";
					  vv+='"'+ttoken+'"';
				  }
				}
			});
			v+=vv;
			if (brkts) v+=")";
			
		}
		
		return v;
	},
	
	res: function(key) {
		return this.mys.res(key);
	}
	
});
