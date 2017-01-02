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
		'Sonicle.webtop.mail.model.RuleModel'
	],
	
	dockableConfig: {
		title: '{rule-editor.tit}',
		iconCls: 'wt-icon-rules-xs',
		width: 600,
		height: 450
	},
	promptConfirm: false,
	full: true,
	modal: true,
	
	mys: null,
	onSender: '',
	onRecipient: '',

	initComponent: function() {
		var me = this;
		Ext.apply(me, {
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
						{ xtype: 'textfield', fieldLabel: me.res('rule-editor-from'), width: 300, value: (me.onSender?me.onSender:'') },
						{ xtype: 'textfield', fieldLabel: me.res('rule-editor-to'), width: 300, value: (me.onRecipient?me.onRecipient:'') },
						{ xtype: 'textfield', fieldLabel: me.res('rule-editor-subject'), width: 300 }
					]

				},
                { border: false, bodyStyle: 'font-size: 20px;', width: 200, html: me.res('rule-editor-then') }, {
					
				}
            ]
			
		});
	},
	
	res: function(key) {
		return this.mys.res(key);
	}
});
