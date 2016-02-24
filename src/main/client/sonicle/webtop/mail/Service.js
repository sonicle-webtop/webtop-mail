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

Ext.define('Sonicle.webtop.mail.Service', {
	extend: 'WT.sdk.Service',
	requires: [
		'Sonicle.webtop.mail.MessagesPanel',
		'Sonicle.webtop.mail.view.MessageEditor',
		'Sonicle.webtop.mail.plugin.ImapTreeViewDragDrop'
	],

	imapTree: null,
	toolbar: null,
	messagesPanel: null,

	treeEditor: null,
	baloon: null,
	actionNode: null,
	sna: null,

	treeMenu: null,
	btreeMenu: null,

	ctxgrid: null,


	mtfwin: null,

	//util vars
/*	aPrint: null,
	aMove: null,
	aDelete: null,
	aSpam: null,
	aReply: null,
	aReplayAll: null,
	aForward: null,
	aForwardEml: null,
	aSeen: null,
	aUnseen: null,
	aDocMgt: null,
	aDocMgtwt: null,*/
	newmsgid: 0,

	//settings
	mailcard: null,
	maxattachsize: null,
	fontface: null,
	fontsize: null,
	differentDefaultFolder: null,
/*	folderInbox: 'INBOX',
	folderTrash: null,
	folderSpam: null,
	folderSent: null,
	folderDrafts: null,*/
	uncheckedFolders: {},
	specialFolders: {},
	identities: null,
	separator: '.',
	askreceipt: false,

	//state vars
	currentFolder: null,

	//default protocol ports
	protPorts: null,
    

	init: function() {
		//Ext.require('Sonicle.webtop.mail.MessagesPanel');
		var me=this;
		
		me.initActions();
		me.initCxm();
		
		//TODO load settings
		//this.loadSettings();
		
		me.viewmaxtos=me.getOption('messageViewMaxTos');
		me.viewmaxccs=me.getOption('messageViewMaxCcs');
		
		me.imapTree=Ext.create('Sonicle.webtop.mail.ImapTree',{
			//title: "Email", //this.title,
			autoScroll: true,

			viewConfig: {
				plugins: { 
					ptype: 'imaptreeviewdragdrop' ,
					moveFolder: function(src,dst) {
						me.moveFolder(src,dst);
					},
					moveMessages: function(data,dst) {
						data.view.grid.moveSelection(data.srcFolder,dst,data.records);
					},
					copyMessages: function(data,dst) {
						data.view.grid.copySelection(data.srcFolder,dst,data.records);
					}
				},
				markDirty: false,
				
			},

			store: Ext.create('Ext.data.TreeStore', {
				model: 'Sonicle.webtop.mail.ImapTreeModel',
				proxy: WTF.proxy(me.ID,'GetImapTree'),
				root: {
					text: 'Imap Tree',
					expanded: true
				},
				rootVisible: false
			}),


			columns: {
				items: [
					{
						xtype: 'treecolumn', //this is so we know which column will show the tree
						text: 'Folder',
						dataIndex: 'folder',
						flex: 3,
						renderer: function(v,p,r) {
							var unr=r.get('unread'),
								hunr=r.get('hasUnread');
							return (unr!==0||hunr?'<b>'+v+'</b>':v);
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

		var tool = Ext.create({
				xtype: 'panel',
				title: 'Mail Toolbox',
				width: 200,
				layout: 'fit',
				items: [
					me.imapTree
				]
		});
		me.setToolComponent(tool);

		//TODO: tree editor
        //this.treeEditor=new WT.ImapTreeEditor(this.imapTree);
        //this.treeEditor.on('beforecomplete',this.renamingFolder,this);

		me.imapTree.on("itemcontextmenu",function(v, rec, itm, i, e, eopts) {
			WT.showContextMenu(e, me.getRef('cxmTree'), { rec: rec });
		});
		me.imapTree.on("containercontextmenu",function(v, e, eopts) {
			WT.showContextMenu(e, me.getRef('cxmBackTree'), { });
		});

        //this.imapTree.on('contextmenu',this.treeContextMenu,this);
		
        //me.imapTree.on('select',me.folderSelected,me);
		me.imapTree.on('rowclick',me.folderClicked,me);
		//TODO: drag&drop
        //this.imapTree.on('nodedragover',this.draggingOver,this);
        //this.imapTree.on('beforenodedrop',this.dropping,this);
		//TODO: tree on load
        me.imapTree.on('load',function(t,r,s,o,n) {
            //if (n.id=='imaproot') {
            //    setTimeout(this.actionCheck.createDelegate(this),1000);
            //    WT.addServerEventListener("recents",this);
            //    //setTimeout(this.checkFolders.createDelegate(this),1000);
            //}
			if (n.id==='root') {
				me.selectInbox();
			}
        },this);
		//TODO: context menu
        /*this.imapTree.on('render',function(t) {
            t.body.on('contextmenu',this.treeBodyContextMenu,this);
        },this);*/
		
		var mp=Ext.create('Sonicle.webtop.mail.MessagesPanel',{
			pageSize: me.getOption('pageRows'),
			viewRegion: me.getOption('messageViewRegion','east'),
			viewWidth: me.getOption('messageViewWidth',600),
			viewHeight: me.getOption('messageViewHeight',400),
			viewCollapsed: me.getOption('messageViewCollapsed',false),
			saveColumnSizes: true,
			saveColumnVisibility: true,
			saveColumnOrder: true,
			savePaneSize: true,
			gridMenu: me.getRef('cxmGrid'),
			mys: me
		});
		me.messagesPanel=mp;
		me.setMainComponent(me.messagesPanel);
		
		me.onMessage('unread',me.unreadChanged,me);

		me.toolbar=mp.toolbar;
        //var xb1,xb2,xb3,xb4,xb5,xb6,xb7;
        var xb=new Array();
        var xx=0;
        me.toolbar.insert(0,[
                xb[xx++]=me._TB("print"),
                xb[xx++]=me._TB("delete"),
                xb[xx++]=me._TB("spam"),
                xb[xx++]=me._TB('movetofolder'),
                "-",
                xb[xx++]=me._TB("reply"),
                xb[xx++]=me._TB("replyall"),
                xb[xx++]=me._TB("forward"),
                "-",
                xb[xx++]=me._TB("check"),
                "-",
                xb[xx++]=me._TB("markseen"),
                xb[xx++]=me._TB("markunseen"),
                "-",
                xb[xx++]=me._TB("filters")
            ]
        );
		//TODO FAX
        //if (WT.hasFax) this.toolbar.insertButton(0,xb[xx++]=new Ext.Button(this.getAction('newfax')));
		//TODO DOCMGT
        //if (WT.docmgt) this.toolbar.insertButton(4,xb[xx++]=new Ext.Button(this.getAction('docmgt')));
        //else if (WT.docmgtwt) this.toolbar.insertButton(4,xb[xx++]=new Ext.Button(this.getAction('docmgtwt')));
		
		//NOT NEEDED ANYMORE
        //for(xx=0;xx<xb.length;++xx) xb[xx].setText('');

		
        mp.folderList.on("viewready",me.resizeColumns,me);
		
		me.setToolbar(me.toolbar);
		
		console.log("mail service init completed");
	},
	
	_TB: function(actionname) {
		var bt=Ext.create('Ext.button.Button',this.getAction(actionname));
		bt.setText('');
		return bt;
	},
	
	nextNewMsgId: function() {
		return ++this.newmsgid;
	},
	
	
	gridAction: function(me,actionName,obj) {
		return function() {
			var g=me.getCtxGrid(),
				fname='action'+actionName,
				fn=g[fname];
		
			if (Ext.isFunction(fn)) fn.call(g,obj);
			else Ext.Error.raise('missing grid action function '+fname);
		};
	},
	
	initActions: function() {
		var me = this;
	
        me.addNewAction("newmsg",{ handler: me.actionNew, scope: me});
		//TODO FAX
/*        if (WT.hasFax) {
            this.sna=new Array();
            this.sna[0]=this.addAction("newfax",this.actionNewFax,this);
        }*/
		
        me.addAction("open",{ handler: me.gridAction(me,'Open'), iconCls: '' });
        me.addAction("opennew",{ handler: me.gridAction(me,'OpenNew'), iconCls: '' });
        
        me.addAction("print",{ handler: me.gridAction(me,'Print'), iconCls: 'wt-icon-print-xs' });
        me.addAction("reply",{ handler: me.gridAction(me,'Reply') });
        me.addAction("replyall",{ handler: me.gridAction(me,'ReplyAll') });
        me.addAction("forward",{ handler: me.gridAction(me,'Forward') });
        me.addAction("forwardeml",{ handler: me.gridAction(me,'ForwardEml') });
        me.addAction("filters",{ handler: me.gridAction(me,'Filters'), iconCls: 'wt-icon-filter-xs' });
        
		me.addAction("multisearch",{ handler: function() { me.messagesPanel.actionMultiSearch(); } , iconCls: 'wt-icon-search-multi-xs', enableToggle: true });
		
		
        me.addAction("special",{ handler: me.gridAction(me,'Flag','special') });
        me.addAction("flagred",{ handler: me.gridAction(me,'Flag','red')});
        me.addAction("flagblue",{ handler: me.gridAction(me,'Flag','blue')});
        me.addAction("flagyellow",{ handler: me.gridAction(me,'Flag','yellow')});
        me.addAction("flaggreen",{ handler: me.gridAction(me,'Flag','green')});
        me.addAction("flagorange",{ handler: me.gridAction(me,'Flag','orange')});
        me.addAction("flagpurple",{ handler: me.gridAction(me,'Flag','purple')});
        me.addAction("flagblack",{ handler: me.gridAction(me,'Flag','black')});
        me.addAction("flaggray",{ handler: me.gridAction(me,'Flag','gray')});
        me.addAction("flagwhite",{ handler: me.gridAction(me,'Flag','white')});
        me.addAction("flagbrown",{ handler: me.gridAction(me,'Flag','brown')});
        me.addAction("flagazure",{ handler: me.gridAction(me,'Flag','azure')});
        me.addAction("flagpink",{ handler: me.gridAction(me,'Flag','pink')});
        me.addAction("flagcomplete",{ handler: me.gridAction(me,'Flag','complete')});
        me.addAction("clear",{ handler: me.gridAction(me,'Flag','clear'), iconCls: '' });
		
        me.addAction("addnote",{ handler: me.gridAction(me,'AddNote') });
	   
        me.addAction("markseen",{ handler: me.gridAction(me,'MarkSeen') });
        me.addAction("markunseen",{ handler: me.gridAction(me,'MarkUnseen') });
        me.addAction("spam",{ handler: me.gridAction(me,'Spam'), iconCls: 'wt-icon-block-xs' });
        me.addAction("delete",{ handler: me.gridAction(me,'Delete'), iconCls: 'wt-icon-delete-xs' });
        me.addAction("movetofolder",{ handler: me.gridAction(me,'MoveToFolder') });
        me.addAction("check",{ handler: function() { me.selectInbox(); }, iconCls: 'wt-icon-refresh-xs' });
        me.addAction("savemail",{ handler: me.gridAction(me,'SaveMail'), iconCls: 'wt-icon-save-xs' });
        me.addAction("viewheaders",{ handler: me.gridAction(me,'ViewHeaders'), iconCls: '' });
        me.addAction("viewsource",{ handler: me.gridAction(me,'ViewSource'), iconCls: '' });
		
        //me.addAction("filterrow",{ handler: me.gridAction(me,'FilterRow'), enableToggle: true });		
        me.addAction("advsearch",{ handler: me.actionAdvancedSearch, scope: me, iconCls: 'wt-icon-search-adv-xs' });
        me.addAction("emptyfolder",{ handler: me.actionEmptyFolder, scope: me });
        me.addAction("deletefolder",{ handler: me.actionDeleteFolder, scope: me, iconCls: 'wt-icon-delete-xs' });
        me.addAction("renamefolder",{ handler: me.actionRenameFolder, scope: me });
        me.addAction("newfolder",{ handler: me.actionNewFolder, scope: me });
        me.addAction("newmainfolder",{ handler: me.actionNewMainFolder, scope: me });
        me.addAction("movetomain",{ handler: me.actionMoveToMainFolder, scope: me, iconCls: '' });
        me.addAction("refresh",{ handler: me.actionFoldersRefresh, scope: me, iconCls: 'wt-icon-refresh-xs' });

        //me.aScan=this.addAction("scanfolder",null,null,'');
        me.addAction("scanfolder",{ handler: null, iconCls: '' });
		
        me.addAction("markseenfolder",{ handler: me.actionFolderMarkSeen, scope: me, iconCls: 'wtmail-icon-markseen-xs' });
        me.addAction("downloadmails",{ handler: me.actionDownloadMails, scope: me, iconCls: 'wt-icon-save-xs' });

        /*if (WT.docmgt) this.aDocMgt=this.addDependencyAction("docmgt","webtop/js/mail/DocMgt.js","actionDocMgt",this,'iconDocMgt');
        if (WT.docmgtwt) this.aDocMgtwt=this.addDependencyAction("docmgtwt","webtop/js/mail/DocMgt.js","actionDocMgtWt",this,'iconDocMgt');
		*/
	},
	
	initCxm: function() {
		var me = this;
		
		//grid menu
		var cxmGrid=me.addRef('cxmGrid', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('open'),
				me.getAction('opennew'),
				me.getAction('print'),
				'-',
				me.getAction('reply'),
				me.getAction('replyall'),
				me.getAction('forward'),
				me.getAction('forwardeml'),
				me.getAction('special'),
				{
					xtype: 'menu',
					text: me.res("menu-complete"),
					menu: {
						items: [
						  me.getAction('flagred'),
						  me.getAction('flagorange'),
						  me.getAction('flaggreen'),
						  me.getAction('flagblue'),
						  me.getAction('flagpurple'),
						  me.getAction('flagyellow'),
						  me.getAction('flagblack'),
						  me.getAction('flaggray'),
						  me.getAction('flagwhite'),
						  me.getAction('flagbrown'),
						  me.getAction('flagazure'),
						  me.getAction('flagpink'),
						  '-',
						  me.getAction('flagcomplete'),
						  //me.getAction('addmemo'),
						  me.getAction('clear')
						]
					}
				},
				me.getAction('addnote'),
				me.getAction('markseen'),
				me.getAction('markunseen'),
				//me.getAction('categories'),
				'-',
				//me.getAction('findall'),
				//me.getAction('createrule'),
				me.getAction('spam'),
				'-',
				me.getAction('delete'),
				'-',
				me.getAction('movetofolder'),
				me.getAction('savemail')
			],
			listeners: {
				beforeshow: function() {
				}
			}
		}));
			
		//TODO: document management
        /*if (WT.docmgt) {
            cxmGrid.add('-');
            cxmGrid.add(me.getAction('docmgt'));
        } else if (WT.docmgtwt) {
            cxmGrid.add('-');
            cxmGrid.add(me.getAction('docmgtwt'));
        }*/
		
        cxmGrid.add('-');
        cxmGrid.add(me.getAction('viewheaders'));
        cxmGrid.add(me.getAction('viewsource'));
		
		//tree menu
		var mscan;
		var cxmTree=me.addRef('cxmTree', Ext.create({
			xtype: 'menu',
			items: [
                me.getAction('advsearch'),
                me.getAction('emptyfolder'),
                '-',
                me.getAction('deletefolder'),
                me.getAction('renamefolder'),
                me.getAction('newfolder'),
                '-',
                me.getAction('movetomain'),
                me.getAction('newmainfolder'),
                '-',
                me.getAction('refresh'),
                '-',
				mscan=Ext.create('Ext.menu.CheckItem',me.getAction('scanfolder')),
                '-',
                me.getAction('downloadmails'),
                '-',
                me.getAction('markseenfolder')
			]
		}));
        mscan.on('checkchange',me.actionScanFolder,me);
        //cxmTree.on('hide',this.treeMenuHidden,this);
		
		var cxmBackTree=me.addRef('cxmBackTree', Ext.create({
			xtype: 'menu',
            items: [
                this.getAction('newmainfolder'),
                '-',
                this.getAction('refresh')
            ]
        }));
	},
	
	resizeColumns: function() {
		var me=this;
        if (!me.resizedcols) {
			var colsizes=me.getOptionAsObject('columnSizes');
            if (colsizes && me.messagesPanel && me.messagesPanel.folderList) {
                var cols=me.messagesPanel.folderList.getColumns();
                var ctot=cols.length;
                for(var i=0;i<ctot;++i) {
					var col=cols[i],
						s=colsizes[col.dataIndex];
                  if (s) {
                      col.setWidth(s);
                  }
                }
            }
            this.resizedcols=true;
        }
        
    },
	
	selectInbox: function() {
		var me=this;
		me.imapTree.getSelectionModel().select(0);
		me.showFolder(me.getFolderInbox());
	},

    /*folderSelected: function(t, r, ix) {
        var folderid=r.get("id");
		console.log("folderSelected: "+folderid);
		this.showFolder(folderid);
    },*/
	
	folderClicked: function(t, r, tr, ix, e, eopts) {
		if (e.target.classList.contains('x-tree-expander')) return;
        
		var folderid=r.get("id");
		this.showFolder(folderid);
	},
	
	showFolder: function(folderid) {
        var me=this,
			mp=me.messagesPanel;
		//TODO: folder clicked
        //mp.depressFilterRowButton();
        mp.clearGridSelections();
        mp.clearMessageView();
        //var a=n.attributes;
        var refresh=true; //a.changed?'1':'0';
		//TODO: stop flash title and baloon hide
        /*if (folderid==this.getFolderInbox()) {
            WT.app.stopFlashTitle();
            if (this.baloon) this.baloon.hide();
        }*/
		//TODO: disable spam button
        me.setActionDisabled('spam',(folderid===me.getFolderSpam()));
        me.currentFolder=folderid;
		//TODO: clear filter textfield
        mp.filterTextField.setValue('');
		mp.quickFilterCombo.setValue('any');
        
        mp.reloadFolder(folderid,{start:0,limit:mp.getPageSize(),refresh:refresh});
	}, 
	
	unreadChanged: function(cfg) {
		//console.log('unreadChanged on '+cfg.foldername+" (unread="+cfg.unread+", hasUnreadChildren"+cfg.hasUnreadChildren+")");
		var me=this;
		var node=me.imapTree.getStore().getById(cfg.foldername);
		if (node) {
			var folder=node.get("folder");
			node.set('hasUnread',cfg.hasUnreadChildren);
			node.set('unread',cfg.unread);
			node.set('folder','');
			node.set('folder',folder);
		}
	},
	
	actionNew: function() {
		var me=this;
		var v=WT.createView(me.ID,'view.MessageEditor');
		v.show(false,function() {
			v.getComponent(0).beginNew({
				data: {
					messageId: 1,
					subject: 'Default Subject',
					recipients: [
						{ rtype: 'to', email: 'matteo.albinola@sonicle.com'},
						{ rtype: 'to', email: 'cristian@sonicle.com'},
						{ rtype: 'to', email: 'filippo@sonicle.com'},
						{ rtype: 'cc', email: 'raffaele.fullone@sonicle.com'},
						{ rtype: 'bcc', email: 'sergio.decillis@sonicle.com'},
						{ rtype: 'bcc', email: 'nethesis@sonicle.com'}
					],
					html: '<BR><B>Test bind HTML</B><BR><BR><I>Let\'s see if it works!</I><BR><BR>Gabry'
				}
			});
		});
	},
	
	actionAdvancedSearch: function() {},
	actionEmptyFolder: function() {},
	actionDeleteFolder: function() {},
	actionRenameFolder: function() {},
	actionNewFolder: function() {},
	actionNewMainFolder: function() {},
	actionMoveToMainFolder: function() {},
	
	actionFoldersRefresh: function() {
		var me=this,
			rec=me.getCtxNode();
	
		if (rec) {
			me.imapTree.getSelectionModel().select(rec);
			me.showFolder(rec.get("id"));
		}
	},
	
	actionScanFolder: function() {},
	actionFolderMarkSeen: function() {},
	actionDownloadMails: function() {},
	
    reloadFolderList: function() {
        this.messagesPanel.reloadGrid();
    },
	
	getCtxGrid: function() {
		var md=WT.getContextMenuData();
		return (md && md.grid) ? md.grid : this.messagesPanel.folderList;
	},
	
	getCtxNode: function() {
		var md=WT.getContextMenuData();
		return (md && md.rec) ? md.rec : null;
	},
	
	getFolderInbox: function() {
		return "INBOX";
	},
	
	getFolderDrafts: function() {
		return this.getOption('folderDrafts');
	},
	
	getFolderSent: function() {
		return this.getOption('folderSent');
	},
	
	getFolderSpam: function() {
		return this.getOption('folderSpam');
	},
	
	getFolderTrash: function() {
		return this.getOption('folderTrash');
	},
	
    moveFolder: function(src,dst) {
		var me=this;
		WT.ajaxReq(me.ID, 'MoveFolder', {
			params: {
				folder: src,
				to: dst
			},
			callback: function(success,json) {
				if (json.result) {
					var tr=me.imapTree,
						v=tr.getView(),
						s=tr.store,
						n=s.getById(json.oldid);
					if (n) n.remove();
					n=(json.parent?s.getNodeById(json.parent):s.getRoot());
					
					if (n.isExpanded()) {
						s.load({ node: n });
					}
					n.expand(false,function(nodes) {
						Ext.each(nodes,function(newnode) {
							if (newnode.getId()===json.newid) {
								v.setSelection(newnode);
								me.folderClicked(tr,newnode);
							}
						});
					});
				} else {
					WT.error(json.text);
				}
			}
		});					
    },
	
    openEml: function(folder,idmessage,idattach) {
		var win=WT.createView(this.ID,'view.EmlMessageView',{
			viewCfg: {
				mys: this,
				folder: folder,
				idmessage: idmessage,
				idattach: idattach
			}
		});
		win.show(false,function() {
			win.getView().showEml();
		});
	},
	
	isDrafts: function(folder) {
		return this.imapTree.getStore().getById(folder).get("isDrafts");
	}
	
	
});
