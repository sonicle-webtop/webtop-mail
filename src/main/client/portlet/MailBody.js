/* 
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.mail.portlet.MailBody', {
	extend: 'WTA.sdk.PortletBody',
	requires: [
		'Sonicle.webtop.mail.model.PletMail'
	],
	
	isSearch: false,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.add({
			xtype: 'grid',
			reference: 'gridMessages',
			hideHeaders: true,
			region: 'center',
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.mail.model.PletMail',
				proxy: WTF.apiProxy(me.mys.ID, 'PortletMail', 'data', {
					extraParams: {
						query: null
					}
				})
			},
			columns: [
				{ dataIndex: 'subject', flex: 1, tdCls: 'x-window-header-title-default wtmail-smartsearch-name-column' },
				{ dataIndex: 'date', width: 160 }
			],
			features: [{
				ftype: 'rowbody',
				getAdditionalData: function (data, idx, record, orig) {
					var eto=Ext.String.escape(record.get("to")),
						to=Ext.String.htmlEncode(record.get("to")),
						from=Ext.String.htmlEncode(record.get("from")),
						text=Ext.String.htmlEncode(record.get("text"));


					return {
						rowBody:"<div style='padding: 1em; width: 200px; overflow: ellipsis; white-space: nowrap' data-qtip='"+eto+"'>"+
									'<b>'+me.mys.res("from")+':</b> '+from+'    '+
									'<b>'+me.mys.res("to")+':</b> '+to+'</div>'+
								'<pre style="padding: 1em">'+text+'</pre>',
						rowBodyCls: "smartsearch-gridbody"
					};
				}
			}],
			listeners: {
				rowdblclick: function(grid, r, tr, rowIndex, e, eopts) {
					me.openMessage(r);
				}/*,
				selectionchange: function(grid, recs, eopts) {
					me.lref("buttonOpenInFolder").setDisabled(!recs || recs.length===0);
				}*/
			}
		});
	},
	
    openMessage: function(r) {
		var me=this;
			
		var uid=r.get("uid"),
			folderid=r.get("folderid");

		WT.ajaxReq(me.mys.ID, 'GetMessagePage', {
			params: {
				folder: folderid,
				uid: uid,
				rowsperpage: 50
			},
			callback: function(success,json) {
				if (success) {
					WT.activateService(me.mys.ID);
					me.mys.selectAndShowFolder(folderid,uid,json.page,json.threadid);
				} else {
					WT.error(json.message);
				}
			}
		});					
    },
			
	refresh: function() {
		this.lref('gridMessages').getStore().load();
	},
	
	recents: function() {
		this.isSearch = false;
		WTU.loadWithExtraParams(this.lref('gridMessages').getStore(), {query: null});
	},
	
	search: function(s) {
		this.isSearch = true;
		WTU.loadWithExtraParams(this.lref('gridMessages').getStore(), {query: s});
	}
});
