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

/**
 * 
 * Remove this Class (and related) when useNewHTMLEditor is no more necessary!
 */
Ext.define('Sonicle.webtop.mail.view.QuickPartEditor', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.ux.field.HTMLEditor'
	],
	
	dockableConfig: {
		title: '{editor.btn-quickpart.tit}',
		iconCls: 'wtmail-icon-format-quickpart-xs',
		modal: true,
		width: 400,
		height: 300
	},
	promptConfirm: false,
	full: true,
	
	mys: null,
	html: '',
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			buttons: [
				Ext.create('Ext.Button',{
					text: 'Ok',
					width: 100,
					handler: function() {
						me.fireEvent('viewok', me,me.lref("fldhtmleditor").getValue());
						me.closeView(false);
					}
				})
			]
		});
		
		me.callParent(arguments);
		
		me.add({
			xtype: 'wthtmleditor_old',
			reference: 'fldhtmleditor',
			region: 'center',
			enableFont: true,
			enableFontSize: true,
			enableFormat: true,
			enableColors: true,
			enableAlignments: false,
			enableLinks: false,
			enableLists: false,
			enableSourceEdit: true,
			enableClean: false,
			initialContent: me.html
		});
	}
	
});
