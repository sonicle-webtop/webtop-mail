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

Ext.define('Sonicle.webtop.mail.view.MoveCopyMessagesDialog', {
	extend: 'WTA.sdk.DockableView',
	
	dockableConfig: {
		iconCls: 'wtmail-icon-movetofolder-xs',
		title: '{act-movetofolder.lbl}',
		width: 700,
		height: 500
	},
	promptConfirm: false,
	full: true,
	
	mys: null,
	multifolder: false,
	grid: null,
	fromFolder: null,
	
	chkfiltered: null,

	initComponent: function() {
		var me = this;
		
		var tree=Ext.create("Sonicle.webtop.mail.SimpleImapTree",{
			mys: me.mys,
			store: me.mys.imapTree.getStore()
		});			

		
		var chkfiltered=me.chkfiltered=Ext.create("Ext.form.field.Checkbox",{
			boxLabel: me.mys.res("filtered"),
			width: 250,
			disabled: me.multifolder
		});
		
		var btnmove=Ext.create("Ext.Button",{
			text: WT.res("act-move.lbl"),
			disabled: true,
			width:80,
			handler: function() {
				me.disable();
                if (chkfiltered.getValue()) {
                    me.grid.operateAllFiltered("MoveMessages",me.fromFolder,me.curnode.id,me.ajaxResultCallback,me);
                } else {
                    me.grid.moveSelection(me.fromFolder,me.curnode.id,me.grid.getSelection());
					me.enable();
					me.closeView(false);
                }
			}
		});
		
		var btncopy=Ext.create("Ext.Button",{
			text: WT.res("act-copy.lbl"),
			disabled: true,
			width:80,
			handler: function() {
				me.disable();
                if (chkfiltered.getValue()) {
                    me.grid.operateAllFiltered("CopyMessages",me.fromFolder,me.curnode.id,me.ajaxResultCallback,me);
                } else {
                    me.grid.copySelection(me.fromFolder,me.curnode.id,me.grid.getSelection());
					me.enable();
					me.closeView(false);
                }
			}
		});
		
		var btndelete=Ext.create("Ext.Button",{
			text: WT.res("act-delete.lbl"),
			disabled: false,
			width:80,
			handler: function() {
				WT.confirm(me.mys.res('suredeletepermanently'),function(bid) {
					if (bid==='yes') {
						me.disable();
						if (chkfiltered.getValue()) {
							me.grid.operateAllFiltered("DeleteMessages",me.fromFolder,null,me.ajaxResultCallback,me);
						} else {
							me.grid.deleteSelection(me.fromFolder,me.grid.getSelection());
							me.enable();
							me.closeView(false);
						}
					}
					me.focus();
				},me);
			}
		});
		
		var btnarchive=Ext.create("Ext.Button",{
			text: me.mys.res("act-archive.lbl"),
			disabled: false,
			width:80,
			handler: function() {
				var tofolder=me.mys.getFolderArchive();
				me.disable();
                if (chkfiltered.getValue()) {
                    me.grid.operateAllFiltered("MoveMessages",me.fromFolder,tofolder,me.ajaxResultCallback,me);
                } else {
                    me.grid.moveSelection(me.fromFolder,tofolder,me.grid.getSelection());
					me.enable();
					me.closeView(false);
                }
			}
		});
		
		/*var btncancel=Ext.create("Ext.Button",{
			text: WT.res("act-cancel.lbl"),
			width:80,
			handler: function() {
				me.closeView(false);
			}
		});*/
		
		Ext.apply(me, {
			buttons: [
				chkfiltered,
				'->',
				btnmove,' ',' ',' ',' ',
				btncopy,' ',' ',' ',' ',
				btnarchive,' ',' ',' ',' ',
				btndelete/*,' ',' ',' ',' ',
				btncancel*/
			]
		});
		
		me.callParent(arguments);
		
		tree.on("select",function(t,n,i,eopts) {
			btncopy.enable();
			btnmove.enable();
			me.curnode=n;
		});

		me.add({
				xtype: 'panel',
				region: 'center',
				//frame: false, 
				//border: false, 
				//bodyBorder: false, 
				autoScroll: true,
				layout: 'fit',
				items: [ tree ]
		});
	},
	
	ajaxResultCallback: function(result) {
		var me=this;
		
		me.enable();
		if (result) {
			me.chkfiltered.setValue(false);
			me.closeView(false);
		}
	}
	
});
