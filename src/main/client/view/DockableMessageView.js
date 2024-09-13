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

Ext.define('Sonicle.webtop.mail.view.DockableMessageView', {
	extend: 'WTA.sdk.DockableView',
	
	dockableConfig: {
		iconCls: 'wtmail-icon-message',
		width: 830,
		height: 600
	},
	promptConfirm: false,
	full: true,
	
	mys: null,
	messageView: null,
	messageGrid: null,
	acct: null,
	folder: null,
	idmessage: null,
	title: null,
	model: null,
	fbar: null,

	initComponent: function() {
		var me = this,
			mv=Ext.create('Sonicle.webtop.mail.MessageView',{
				mys: me.mys,
				dockable: true
			});
		
		me.messageView=mv;
		
		Ext.apply(me, {
			tbar: {
				enableOverflow: true,
				cls: 'wtmail-messageview-toolbar',
				items: [
					me.addAct("print",{
						handler: function() {
							mv.print();
						},
						iconCls: 'wt-icon-print'
					}),
					me.addAct("delete",{
						handler: function() {
							me.messageGrid.deleteMessage(me.acct,me.folder,me.idmessage,me);
						},
						iconCls: 'wt-icon-trash'
					}),
					me.addAct("reply",{
						handler: function() {
							me.messageGrid.replyMessageById(me.acct,me.folder,me.idmessage,false);
						},
						iconCls: 'wtmail-icon-reply'
					}),
					me.addAct("replyall",{
						handler: function() {
							me.messageGrid.replyMessageById(me.acct,me.folder,me.idmessage,true);
						},
						iconCls: 'wtmail-icon-replyall'
					}),
					me.addAct("forward",{
						handler: function() {
							me.messageGrid.forwardMessageById(me.acct,me.folder,me.idmessage);
						},
						iconCls: 'wtmail-icon-forward'
					})
				]
			},
			fbar: me.fbar
		});
		
		me.dockableConfig.title=me.title;
		
		me.callParent(arguments);
		
		me.add(mv);
	},
	
	showMessage: function() {
		var me=this,
			mv=me.messageView;
		
		if (me.model) {
			mv.model=me.model;
			mv.on('messageviewed',function() {
				if (!me.mys.getVar("manualSeen")||me.mys.getVar("seenOnOpen")) {
					var r=me.model;
					if (r.get("unread")) {
						r.set("unread",false);
						var st=r.get("status");
						if (st==="unread"||st==="new") r.set("status","read");
						/*var o=s.reader.jsonData;
						o.millis=millis;
						o.unread--;
						this.updateUnreads(this.currentFolder,o,false);*/
					}
				}
			});
		}
		
		mv._showMessage(me.acct, me.folder, me.idmessage, !me.mys.getVar("manualSeen")||me.mys.getVar("seenOnOpen"), me.model, me.nopec);
	}
	
});
