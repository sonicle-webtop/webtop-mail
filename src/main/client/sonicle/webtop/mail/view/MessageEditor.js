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

Ext.define('Sonicle.webtop.mail.view.MessageEditor', {
	extend: 'WT.sdk.ModelView',
	requires: [
		'Sonicle.webtop.mail.model.MessageModel',
		'Sonicle.webtop.core.ux.RecipientsGrid',
		'Sonicle.webtop.core.ux.SuggestCombo',
		'Sonicle.form.field.HTMLEditor'
	],
	
	title: '@message.tit',
	iconCls: 'wtmail-icon-newmsg-xs',
	model: 'Sonicle.webtop.mail.model.MessageModel',
	
	autoToolbar: false,
	identityIndex: 0,
	
	initComponent: function() {
		var me=this;
		me.callParent(arguments);
		
		me.attlist= Ext.create({
			xtype: 'panel',
			width: 200,
			autoScroll: true,
			region: 'east',
			layout: 'anchor',
			border: false,
			bodyBorder: false
        });
		me.recgrid=Ext.create({
			xtype: 'wtrecipientsgrid',
			height: 90,
			region: 'center',
			fields: { recipientType: 'rtype', email: 'email' },
			bind: {
				store: '{record.recipients}'
			}
		});
		
		me.subject=Ext.create({
			xtype: 'wtsuggestcombo',
            sid: me.mys.ID,
            suggestionContext: 'subject',
            width: 600,
			fieldLabel: WT.res('word.subject'),
			labelWidth: 60,
			labelAlign: 'right'/*,
			bind: '{record.subject}'*/
        });
		me.xcombo=Ext.create({
			xtype: 'combo',
			typeAhead: true,
			queryMode: 'local',
			forceSelection: true,
			selectOnFocus: true,
			store: Ext.create('Ext.data.Store', {
				fields: ['id','fs'],
				data : [
					{ id: 1, fs: "8" },
					{ id: 2, fs: "10"},
					{ id: 3, fs: "12"},
					{ id: 4, fs: "14"},
					{ id: 5, fs: "18"},
					{ id: 6, fs: "24"},
					{ id: 7, fs: "36"}
				]
			}),
            width: 600,
			valueField: 'id',
			displayField: 'fs',
			fieldLabel: 'Suck!',
			labelWidth: 60,
			labelAlign: 'right'
		});
		
		var idents=me.mys.getOption("identities");
		me.toolbar=Ext.create({
			xtype: 'toolbar',
			region: 'north',
			items: [
				{ xtype:'button',text:'Send!', handler: me.sendMail, scope: me },
				{
					xtype:'combo',
					queryMode: 'local',
					displayField: 'description',
					valueField: 'email',
					width:200,
					matchFieldWidth: false,
					listConfig: {
						width: 300
					},
					store: Ext.create('Ext.data.Store', {
						model: 'Sonicle.webtop.mail.model.Identity',
						data : idents
					}),
					value: idents[me.identityIndex].email
				}
			]
		});
		
		me.add(Ext.create({
			xtype: 'panel',
			region: 'north',
//			bodyCls: 'wt-theme-bg-2',
            border: false,
            bodyBorder: false,
			height: 150,
			layout: 'anchor',
			items: [
				me.toolbar,
				Ext.create({
					xtype: 'panel',
					region: 'center',
                    border: false,
                    bodyBorder: false,
                    height: 90,
                    layout: 'border',
                    items: [
                        me.recgrid,
						me.attlist
                    ]
				}),
				me.subject,
				me.xcombo
			]
		}));
		me.add(
			me.htmlEditor=Ext.create({
				xtype: 'sohtmleditor',
				region: 'center',
				bind: '{record.html}',
				enableFont: true,
				enableFontSize: true,
				enableFormat: true,
				enableColors: true,
				enableAlignments: true,
				enableLinks: true,
				enableLists: true,
				enableSourceEdit: true,
				enableClean: true
			})
		);

		console.log("init");
	},
	
	sendMail: function() {
		console.log("sendMail");
	}
	
});