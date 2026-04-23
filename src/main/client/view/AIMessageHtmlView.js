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

Ext.define('Sonicle.webtop.mail.view.AIMessageHtmlView', {
	extend: 'Sonicle.webtop.core.view.AIView',
	
	question: '',
	when: '',
	instructions: '',
	hidePrompt: false,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.add({
			xtype: 'wtpanel',
			itemId: 'aiPanel',
			title: 'Cerca tramite A.I.',
			hidden: me.hidePrompt,
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			region: 'south',
			items: [{
				xtype: 'fieldcontainer',
			    layout: 'anchor',
				padding: 10,
			    defaults: {
			      anchor: '100%'
				},
				items: [{
				    xtype: 'textarea',
					itemId: 'aiQuestion',
					fieldLabel: 'Cosa',
					value: me.question
				}, {
				    xtype: 'textfield',
					itemId: 'aiWhen',
					fieldLabel: 'Quando',
					value: me.when
				}, {
				    xtype: 'textarea',
					itemId: 'aiInstructions',
					fieldLabel: 'Istruzioni',
					value: me.instructions
				}, {
					xtype: 'toolbar',
					border: false,
					items: ['->',{
						xtype: 'button',
						text: 'Prompt',
						handler: function() {
							me.runPrompt();
						}
					}]
				}]
			}]
		});
	},
	
	runPrompt: function() {
		var me=this, panel = me.down('#aiPanel'),
			formatting = "\nUsa una bella formattazione moderna in stile dark nella tua risposta. Non includere assolutamente nessun link.";
		panel.setDisabled(true);
		me.askRAG(me.mys.ID, "AIRAG", {
			question: me.down('#aiQuestion').getValue(),
			format: 'html',
			when: me.down('#aiWhen').getValue(),
			instructions: me.down('#aiInstructions').getValue()+"\n"+formatting
		}, function() {
			panel.setDisabled(false);
		});
	},
	
	setData: function(data) {
		var me = this, aiLoader = me.down('#aiLoader');
		me.remove(aiLoader);
		
		var store = Ext.create('Ext.data.Store', {
			fields:[ 'date', 'subject', 'folder', 'messageId', 'briefing'],
			data: data
		});		
		
		me.add({
			xtype: 'grid',
			itemId: 'aiLoader',
			store: store,
			region: 'center',
			columns: [
				{ text: 'Date', dataIndex: 'date' },
				{ text: 'From', dataIndex: 'from' },
				{ text: 'To', dataIndex: 'to' },
				{ text: 'Subject', dataIndex: 'subject', flex: 1 }
				//{ text: 'Folder', dataIndex: 'folder' }
				//{ text: 'Message-ID', dataIndex: 'messageId' }
			],
			features: [{
				ftype: 'rowbody',
				getAdditionalData: function(data, idx, record, orig) {
					return {
						rowBody: '<div style="padding: 4px; font-style: italic">' + record.get("briefing") + '</div>',
						//rowBodyCls: "my-body-class"
					};
				}
			}],
		listeners: {
				rowdblclick: function(g, r) {
					me.openMessage(r);
				}
			}
		});
	},
	
	openMessage: function(r) {
		var me = this;
		
		me.setLoading(true);
		WT.ajaxReq(me.mys.ID, 'GetMessageUID', {
			params: {
				account: me.mys.currentAccount,
				folder: r.get('folder'),
				messageid: r.get('messageId')
			},
			callback: function(success,json) {
				if (success && json.success) {
					var uid = parseInt(json.message),
						win = WT.createView(me.mys.ID,'view.DockableMessageView', {
							viewCfg: {
								mys: me.mys,
								acct: me.mys.currentAccount,
								folder: r.get('folder'),
								idmessage: uid,
								title: r.get('subject'),
								model: r,
								messageGrid: me.mys.messagesPanel.folderList
							}
						});
					win.show(false, function() {
						win.getView().showMessage();
					});
				}
				me.setLoading(false);
			}
		});

	}
	
});
