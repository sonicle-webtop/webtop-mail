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

Ext.define('Sonicle.webtop.mail.view.AddressBookView', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.ux.AddressBook'
	],
	
	dockableConfig: {
		title: '{addressbook.tit}',
		iconCls: 'wt-icon-addressbook-xs',
		width: 700,
		height: 500
	},
	full: true,
	confirm: 'yn',
	
	dirty: false,
	
	/**
	 * @event save
	 * Fires when save button is pressed.
	 * @param {Sonicle.webtop.mail.view.AddressBookView} me
	 */
	
	mys: null,
	recipients: null,
	
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
				WT.res("word.contacts")+":",
				{
					xtype: 'combo',
					reference: "cboContacts",
					store: {
						model: 'WTA.ux.data.SimpleModel',
						proxy: WTF.apiProxy(WT.ID, 'ListInternetRecipientsSources', 'sources'),
						autoLoad: true,
						listeners: {
							load: {
								fn: function(s) {
									s.insert(0,{id:'*',desc:'('+WT.res('word.all.male')+')'});
									me.lref('cboContacts').setValue('*');
								}
							}
						}
					},
					editable: false,
					mode: 'local',
					width: 200,
					displayField: 'desc',
					triggerAction: 'all',
					valueField: 'id',
					listeners: {
						select: {
							fn: function(c,r,o) {
								me.lref("abook").setSource(r.get('id'));
							}
						}
					}
				}	
			]
		});
		
		me.callParent(arguments);
		
        me.add({
			xtype: 'wtaddressbook',
			reference: 'abook',
            region: "center",
			recipients: me.recipients
		});
	},
	
	actionSave: function() {
		this.fireEvent('save', this);
		this.closeView(false);
	},
	
	getRecipients: function() {
		return this.lref("abook").getRecipients();
	},
	
	canCloseView: function() {
		var me=this;
		return !me.dirty && !me.lref("abook").dirty;
	},
	
	res: function(key) {
		return this.mys.res(key);
	}
	
});
