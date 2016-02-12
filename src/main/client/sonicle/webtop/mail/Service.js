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
	aPrint: null,
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
	aDocMgtwt: null,
	newmsgid: 0,

	//settings
	mailcard: null,
	maxattachsize: null,
	fontface: null,
	fontsize: null,
	differentDefaultFolder: null,
	folderInbox: 'INBOX',
	folderTrash: null,
	folderSpam: null,
	folderSent: null,
	folderDrafts: null,
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
		//TODO context menus
		me.initCxm();
		//this.initTreeMenu();
		
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
		
		//TODO: context menu
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
				me.imapTree.getSelectionModel().select(0);
				me.showFolder(me.folderInbox);
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
		//TODO old toolbar
        //this.toolbar=mp.toolbar;
		//TODO grid events
        //mp.on('gridrowdblclick',this.rowDblClicked,this);
        //mp.on('gridcellclick',this.cellClicked,this);
		
		me.onMessage('unread', me.unreadChanged, this);

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
	
	
	initActions: function() {
		var me = this;
	
        me.addNewAction("newmsg",{ handler: me.actionNew, scope: me});
		//TODO FAX
/*        if (WT.hasFax) {
            this.sna=new Array();
            this.sna[0]=this.addAction("newfax",this.actionNewFax,this);
        }*/
        me.addAction("open",{ handler: me.actionOpen, scope: me, iconCls: '' });
        me.addAction("opennew",{ handler: me.actionOpenNew, scope: me, iconCls: '' });
        
		me.addAction("print",{ handler: me.actionPrint, scope: me, iconCls: 'wt-icon-print-xs' });
        me.addAction("reply",{ handler: me.actionReply, scope: me });
        me.addAction("replyall",{ handler: me.actionReplyAll, scope: me });
        me.addAction("forward",{ handler: me.actionForward, scope: me });
        me.addAction("forwardeml",{ handler: me.actionForwardEml, scope: me });
		me.addAction("special",{ handler: me.actionSpecial, scope: me });
        me.addAction("filters", { handler: me.actionFilters, scope: me, iconCls: 'wt-icon-filter-xs' });
        me.addAction("multisearch", { handler: me.actionMultiSearch, scope: me, iconCls: 'wt-icon-search-multi-xs' });
		
		
        me.addAction("flagred",{ handler: me.actionFlagRed, scope: me });
        me.addAction("flagblue",{ handler: me.actionFlagBlue, scope: me });
        me.addAction("flagyellow",{ handler: me.actionFlagYellow, scope: me });
        me.addAction("flaggreen",{ handler: me.actionFlagGreen, scope: me });
        me.addAction("flagorange",{ handler: me.actionFlagOrange, scope: me });
        me.addAction("flagpurple",{ handler: me.actionFlagPurple, scope: me });
        me.addAction("flagblack",{ handler: me.actionFlagBlack, scope: me });
        me.addAction("flaggray",{ handler: me.actionFlagGray, scope: me });
        me.addAction("flagwhite",{ handler: me.actionFlagWhite, scope: me });
        me.addAction("flagbrown",{ handler: me.actionFlagBrown, scope: me });
        me.addAction("flagazure",{ handler: me.actionFlagAzure, scope: me });
        me.addAction("flagpink",{ handler: me.actionFlagPink, scope: me });
        me.addAction("flagcomplete",{ handler: me.actionFlagComplete, scope: me });
        me.addAction("addnote",{ handler: me.actionAddNote, scope: me });
        me.addAction("clear",{ handler: me.actionClear, scope: me, iconCls: '' });
		
	   
        me.addAction("markseen",{ handler: me.actionMarkSeen, scope: me });
        me.addAction("markunseen",{ handler: me.actionMarkUnseen, scope: me });
        me.addAction("spam",{ handler: me.actionSpam, scope: me, iconCls: 'wt-icon-block-xs' });
        me.addAction("delete",{ handler: me.actionDelete, scope: me, iconCls: 'wt-icon-delete-xs' });
        me.addAction("movetofolder",{ handler: me.actionMoveToFolder, scope: me });
        me.addAction("check",{ handler: me.actionCheck, scope: me, iconCls: 'wt-icon-refresh-xs' });
        me.addAction("advsearch", { handler: me.actionAdvancedSearch, scope: me, iconCls: 'wt-icon-search-adv-xs' });
		me.addAction("filterrow",{ handler: me.actionFilterRow, scope: me, enableToggle: true });
		
		
        me.addAction("emptyfolder",{ handler: me.actionEmptyFolder, scope: me });
        me.addAction("deletefolder",{ handler: me.actionDeleteFolder, scope: me });
        me.addAction("renamefolder",{ handler: me.actionRenameFolder, scope: me });
        me.addAction("newfolder",{ handler: me.actionNewFolder, scope: me });
        me.addAction("newmainfolder",{ handler: me.actionNewMainFolder, scope: me });
        me.addAction("movetomain",{ handler: me.actionMoveToMainFolder, scope: me, iconsCls: '' });
        me.addAction("refresh",{ handler: me.actionFoldersRefresh, scope: me, iconsCls: '' });
        //me.aScan=this.addAction("scanfolder",null,null,'');
        me.addAction("markseenfolder",{ handler: me.actionFolderMarkSeen, scope: me, iconsCls: '' });
        me.addAction("savemail",{ handler: me.actionSaveMail, scope: me, iconsCls: 'wt-icon-save-xs' });
        me.addAction("downloadmails",{ handler: me.actionDownloadMails, scope: me, iconsCls: 'wt-icon-save-xs' });
        /*me.addDependencyAction("viewheaders","webtop/js/mail/ViewSource.js","actionViewHeaders",this);
        me.addDependencyAction("viewsource","webtop/js/mail/ViewSource.js","actionViewSource",this);
        if (WT.docmgt) this.aDocMgt=this.addDependencyAction("docmgt","webtop/js/mail/DocMgt.js","actionDocMgt",this,'iconDocMgt');
        if (WT.docmgtwt) this.aDocMgtwt=this.addDependencyAction("docmgtwt","webtop/js/mail/DocMgt.js","actionDocMgtWt",this,'iconDocMgt');
		*/
	},

	initCxm: function() {
		var me = this;
		me.addRef('cxmGrid', Ext.create({
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
                new Ext.menu.Item({
                    text: me.res("menu-complete"),
                    menu: Ext.create({
						xtype: 'menu',
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
                    })
                }),
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

    /*folderSelected: function(t, r, ix) {
        var folderid=r.get("id");
		console.log("folderSelected: "+folderid);
		this.showFolder(folderid);
    },*/
	
	folderClicked: function(t, r, tr, ix, e, eopts) {
        var folderid=r.get("id");
		this.showFolder(folderid);
	},
	
	showFolder: function(folderid) {
        var mp=this.messagesPanel;
		//TODO: folder clicked
        //mp.depressFilterRowButton();
        //mp.clearGridSelections();
        //mp.clearMessageView();
        //var a=n.attributes;
        var refresh=true; //a.changed?'1':'0';
		//TODO: stop flash title and baloon hide
        /*if (folderid==this.folderInbox) {
            WT.app.stopFlashTitle();
            if (this.baloon) this.baloon.hide();
        }*/
		//TODO: disable spam button
        //this.aSpam.setDisabled((folderid==this.folderSpam));
        this.currentFolder=folderid;
		//TODO: clear filter textfield
        //mp.filterTextField.setValue('');
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
	
    actionDelete: function() {
        this.getCtxGrid().actionDelete();
	},	
	
    actionSpam: function() {
        this.getCtxGrid().actionSpam();
	},	
	
    actionMarkSeen: function() {
        this.getCtxGrid().actionMarkSeen();
    },
    
    actionMarkUnseen: function() {
        this.getCtxGrid().actionMarkUnseen();
    },
	
    actionSpecial: function() { this.getCtxGrid().setFlag("special");},
    actionFlagRed: function() { this.getCtxGrid().setFlag("red");},
    actionFlagBlue: function() { this.getCtxGrid().setFlag("blue");},
    actionFlagYellow: function() { this.getCtxGrid().setFlag("yellow");},
    actionFlagGreen: function() { this.getCtxGrid().setFlag("green");},
    actionFlagOrange: function() { this.getCtxGrid().setFlag("orange");},
    actionFlagPurple: function() { this.getCtxGrid().setFlag("purple");},
    actionFlagBlack: function() { this.getCtxGrid().setFlag("black");},
    actionFlagGray: function() { this.getCtxGrid().setFlag("gray");},
    actionFlagWhite: function() { this.getCtxGrid().setFlag("white");},
    actionFlagBrown: function() { this.getCtxGrid().setFlag("brown");},
    actionFlagAzure: function() { this.getCtxGrid().setFlag("azure");},
    actionFlagPink: function() { this.getCtxGrid().setFlag("pink");},
    actionFlagComplete: function() { this.getCtxGrid().setFlag("complete");},
    actionClear: function() { this.getCtxGrid().setFlag("clear");},	
	
    reloadFolderList: function() {
        this.messagesPanel.reloadGrid();
    },
	
	getCtxGrid: function() {
		var md=WT.getContextMenuData();
		return (md && md.grid) ? md.grid : this.messagesPanel.folderList;
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
					//n.mailservice=this;
					//n.newname=o.newname;
					//n.newid=o.newid;
					
					//TODO: not refreshing on node already expanded!!!
					if (n.isExpanded()) {
						v.refreshNode(n);
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
/*        WT.JsonAjaxRequest({
          url: "ServiceRequest",
          params: {
              service: "mail",
              action: "MoveFolder",
              folder: src,
              to: dst
          },
          method: "POST",
		  options: { leaf: this.imapTree.getNodeById(src).leaf },
          callback: function(o,options) {
            if (o.result) {
            } else {
              WT.alert(WT.res("error"),o.text);
            }
          },
          scope: this
        });*/
    }
	
});
