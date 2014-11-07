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

Ext.define('Sonicle.webtop.mail.ImapTreeModel', {
    extend: 'Ext.data.TreeModel',
    idProperty: 'id',
    fields: [{
        name: "id",
        convert: undefined
    }, {
        name: "folder",
        convert: undefined
    }, {
        name: "unread",
        convert: undefined
    }, {
        name: "hasunread",
        convert: undefined
    }]
});

Ext.define('Sonicle.webtop.mail.ImapTree', {
        extend: 'Ext.tree.Panel'
});

Ext.define('Sonicle.webtop.mail.MailService', {
	extend: 'WT.sdk.Service',
	requires: [
		'Sonicle.webtop.mail.MessagesPanel'
	],

        imapTree: null,
        toolbar: null,
        messagesPanel: null,
        
        treeEditor: null,
        baloon: null,
        actionNode: null,
        sna: null,
    
        gridMenu: null,
        treeMenu: null,
        btreeMenu: null,

        ctxgrid: null,
    

        mtfwin: null,
    
        //util vars
        aPrint: null,
        aMove: null,
        aDelete: null,
        aSpam: null,
        aReply: null,
        aReplayAll: null,
        aForward: null,
        aForwardEml: null,
        aDocMgt: null,
        aDocMgtwt: null,
        newmsgid: 0,
        bFilterRow: null,

        //settings
        mailcard: null,
        maxattachsize: null,
        fontface: null,
        fontsize: null,
        folderTrash: null,
        folderSpam: null,
        folderSent: null,
        folderDrafts: null,
        uncheckedFolders: {},
        specialFolders: {},
        pageRows: 50,
        identities: null,
        separator: '.',
        askreceipt: false,

        //state vars
        currentFolder: null,

        //default protocol ports
        protPorts: null,
    

	init: function() {
		//Ext.require('Sonicle.webtop.mail.MessagesPanel');
		
		this.addAction('new', 'testaction', {
			tooltip: null,
			handler: function(s) {
				alert(s.text+' clicked');
			},
			scope: this
		});
		
		var me=this;
		//**LATER**
		//this.loadSettings();
		//this.initTreeMenu();
		//this.initGridMenu();
		
		me.imapTree=Ext.create('Sonicle.webtop.mail.ImapTree',{
			title: "Email", //this.title,
			autoScroll: true,

			//enableDD: true, ???
			//ddGroup: 'mail', ???
			//ddScroll: true, ???
			//containerScroll: true, ???
			viewConfig: {
				plugins: { ptype: 'treeviewdragdrop' }
			},

			//
			//loader: new Ext.tree.TreeLoader({
			//    dataUrl:'ServiceRequest',
			//    baseParams: {service: 'mail', action:'GetImapTree'},
			//    baseAttrs: {uiProvider: WT.ImapTreeNodeUI}
			//}),
			store: Ext.create('Ext.data.TreeStore', {
				model: 'Sonicle.webtop.mail.ImapTreeModel',
				proxy: {
					type: 'ajax',
					reader: 'json',
					url: 'ServiceRequest',
					extraParams: {
						service: 'com.sonicle.webtop.mail',
						action: 'GetImapTree'
					}
				},
				root: {
					text: 'Imap Tree',
					expanded: true
				},
				rootVisible: false
			}),


			//root: new Ext.tree.AsyncTreeNode({
			//    text: 'Imap Tree',
			//    draggable:false,
			//    id:'imaproot'
			//}),

//                root: {
//                    text: 'Imap Tree',
//                },

			columns: {
				items: [
					{
						xtype: 'treecolumn', //this is so we know which column will show the tree
						text: 'Folder',
						dataIndex: 'folder',
						flex: 3,
						renderer: function(v,p,r) {
							return (r.get('unread')!==0||r.get('hasUnread')?'<b>'+v+'</b>':v);
						}
					},
					{
						text: 'Unread', 
						dataIndex: 'unread',
						align: 'right',
						flex: 1,
						renderer: function(v,p,r) {
							return (v===0?'':'<b>'+v+'</b>');
						}
					}
			  ]
			},

			useArrows: true,
			rootVisible: false
		});
		
		me.setToolComponent(me.imapTree);

		//TODO: tree editor
        //this.treeEditor=new WT.ImapTreeEditor(this.imapTree);
        //this.treeEditor.on('beforecomplete',this.renamingFolder,this);
		
		//TODO: context menu
        //this.imapTree.on('contextmenu',this.treeContextMenu,this);
        me.imapTree.on('itemclick',me.folderClicked,me);
		//TODO: drag&drop
        //this.imapTree.on('nodedragover',this.draggingOver,this);
        //this.imapTree.on('beforenodedrop',this.dropping,this);
		//TODO: tree on load
/*        this.imapTree.on('load',function(n) {
            if (n.id=='imaproot') {
                setTimeout(this.actionCheck.createDelegate(this),1000);
                WT.addServerEventListener("recents",this);
                //setTimeout(this.checkFolders.createDelegate(this),1000);
            }
        },this);*/
		//TODO: context menu
        /*this.imapTree.on('render',function(t) {
            t.body.on('contextmenu',this.treeBodyContextMenu,this);
        },this);*/
		
		
		me.messagesPanel=Ext.create('Sonicle.webtop.mail.MessagesPanel',{
			ms: me
		});
		me.setMainComponent(me.messagesPanel);

		me.addWSAction('unread',me.unreadChanged,this);
		WT.Log.debug('Sonicle.webtop.mail.MailService initialized!');
	},
	
    folderClicked: function(t, r, item, ix) {
        var mp=this.messagesPanel;
		//TODO: folder clicked
        //mp.depressFilterRowButton();
        //mp.clearGridSelections();
        //mp.clearMessageView();
        //var a=n.attributes;
        var refresh=true; //a.changed?'1':'0';
        var folderid=r.get("id");
		//TODO: stop flash title and baloon hide
        /*if (folderid=='INBOX') {
            WT.app.stopFlashTitle();
            if (this.baloon) this.baloon.hide();
        }*/
		//TODO: disable spam button
        //this.aSpam.setDisabled((folderid==this.folderSpam));
        this.currentFolder=folderid;
		//TODO: clear filter textfield
        //mp.filterTextField.setValue('');
        
        mp.reloadFolder(folderid,{start:0,limit:this.pageRows,refresh:refresh});
    },
	
	unreadChanged: function(cfg) {
		WT.Log.debug('unreadChanged on '+cfg.foldername+" (unread="+cfg.unread+", hasUnreadChildren"+cfg.hasUnreadChildren+")");
		var me=this;
		var node=me.imapTree.getStore().getById(cfg.foldername);
		WT.Log.debug("found node "+node);
		if (node) {
			node.set('hasUnread',cfg.hasUnreadChildren);
			node.set('unread',cfg.unread);
		}
	}
	
});
