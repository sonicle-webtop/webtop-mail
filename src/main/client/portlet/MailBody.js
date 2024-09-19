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
	
	storeRecents: null,
	storeSearch: null,
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			
			bbar: {
				xtype: 'statusbar',
				reference: 'statusBar',
				hidden: true
			}
		});
		
		me.callParent(arguments);
		
		me.storeRecents=Ext.create('Ext.data.Store',{
				autoLoad: true,
				model: 'Sonicle.webtop.mail.model.PletMail',
				proxy: WTF.apiProxy(me.mys.ID, 'PortletMail', 'data', {
					extraParams: {
						query: null
					}
				})
		});
		me.storeSearch=Ext.create('Ext.data.Store',{
				autoLoad: false,
				model: 'Sonicle.webtop.mail.model.PletMail',
				data: [
				]
		});
		
		me.add({
			xtype: 'grid',
			reference: 'gridMessages',
			hideHeaders: true,
			region: 'center',
			store: me.storeRecents,
			columns: [
				{ dataIndex: 'subject', flex: 1, tdCls: 'wt-theme-text-color-hyperlink wtmail-smartsearch-name-column' },
				{ dataIndex: 'date', width: 160 }
			],
			features: [{
				ftype: 'rowbody',
				getAdditionalData: function (data, idx, record, orig) {
					var efolderid=Ext.String.escape(record.get("folderid")),
						foldername=Ext.String.htmlEncode(record.get("foldername")),
						eto=Ext.String.escape(record.get("to")),
						to=Ext.String.htmlEncode(record.get("to")),
						from=Ext.String.htmlEncode(record.get("from")),
						text=Ext.String.htmlEncode(record.get("text"));


					return {
						rowBody:"<div style='padding: 1em; width: 200px; overflow: ellipsis; white-space: nowrap' data-qtip='"+efolderid+"'>"+
								foldername+"</div>"+
								"<div style='padding: 1em; width: 200px; overflow: ellipsis; white-space: nowrap' data-qtip='"+eto+"'>"+
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
				}
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
					me.mys.selectAndShowFolder(json.account,folderid,uid,json.row,json.page,json.threadid);
				} else {
					WT.error(json.message);
				}
			}
		});					
    },
			
	refresh: function() {
		if (!this.isSearch)
			this.lref('gridMessages').getStore().load();
	},
	
	recents: function() {
		var me=this;
		me.isSearch = false;
		me.lref('gridMessages').setStore(me.storeRecents);
		me.lref("statusBar").hide();
	},
	
	search: function(s) {
		var me=this;
		me.isSearch = true;
		me.lref('gridMessages').setStore(me.storeSearch);
		me.lref("statusBar").setStatus("(...0%...)");
		me.lref("statusBar").show();
		
		WT.ajaxReq(me.mys.ID, 'PortletRunSearch', {
			params: {
				pattern: s
			},
			callback: function(success,json) {
				if (success) {
					if (!me.polltask) me.polltask=new Ext.util.DelayedTask();
					me.polltask.delay(1000, me.doSearchPolling, me);
				} else {
					WT.error(json.message);
				}
			}
		});					
		
	},
	
	doSearchPolling: function() {
		var me=this;
	
		WT.ajaxReq(me.mys.ID, 'PortletPollSearch', {
			callback: function(success,json) {
				if (success) {
					me.storeSearch.removeAll();
					Ext.each(json.data.messages, function(m) {
						me.storeSearch.add({ uid: m.uid, folderid: m.folderid, foldername: m.foldername, subject: m.subject, from: m.from, to: m.to, date: m.date, text: m.text });
					});
					if (!json.data.finished) {
						me.polltask.delay(1000, me.doSearchPolling, me);
						me.lref("statusBar").setStatus(Ext.String.format(me.mys.res("portlet.mail.sbar.progress"),json.data.curfoldername,json.data.progress));
					} else {
						me.lref("statusBar").setStatus(Ext.String.format(me.mys.res("portlet.mail.sbar.count"),json.data.totalRows));
					}
				} else {
					WT.error(json.message);
				}
			}
		});					
	}
});
