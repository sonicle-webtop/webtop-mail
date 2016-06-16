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

Ext.define('Sonicle.webtop.mail.Service', {
	extend: 'WT.sdk.Service',
	requires: [
		'Sonicle.webtop.mail.ImapTree',
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

	newmsgid: 0,

	//settings
	mailcard: null,
	maxattachsize: null,
	fontface: null,
	fontsize: null,
	differentDefaultFolder: null,
	uncheckedFolders: {},
	specialFolders: {},
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
		
		me.imapTree=Ext.create('Sonicle.webtop.mail.ImapTree',{ mys: me });

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
			me.updateCxmTree(rec);
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
		
		me.imapTree.on('edit', function(ed, e) {
			if (e.colIdx===0)
				me.renameFolder(e.record,e.originalValue,e.value);
		});
		
		
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
		
		var ff=me.specialFolders;
		ff[me.getFolderInbox()]=
		 ff[me.getFolderDrafts()]=
		   ff[me.getFolderSent()]=
		    ff[me.getFolderTrash()]=
		     ff[me.getFolderSpam()]=true;
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
		return function(s,e) {
			var g=me.getCtxGrid(e),
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
        me.addAction("refresh",{ handler: me.actionFolderRefresh, scope: me, iconCls: 'wt-icon-refresh-xs' });

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
		me.addRef("mnuScan",mscan);
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
		if (e && e.target.classList.contains('x-tree-expander')) return;
        
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
	
	unreadChanged: function(cfg,unreadOnly) {
		var me=this;
		var node=me.imapTree.getStore().getById(cfg.foldername);
		if (node) {
			var folder=node.get("folder");
			if (!unreadOnly) node.set('hasUnread',cfg.hasUnreadChildren);
			node.set('unread',cfg.unread);
			node.set('folder','');
			node.set('folder',folder);
		}
	},
	
	actionNew: function() {
		this.startNewMessage(this.currentFolder);
	},
	
	/**
	 * Starts a new message with preconfigured options.
	 * @param {String} Reference folder id 
	 * @param {String} [opts.from] Initial from
	 * @param {String} [opts.subject] Initial subject
	 * @param {bool} [opts.receipt] ask receipt
     * @param {bool} [opts.priority] high priority
	 * @param {Object[]} [opts.recipients] Array of recipient objects with rtype/email pairs
	 * @param {Object[]} [opts.attachments] Array of attachment objects with uploadId/fileName/fileSize data
	 * @param {String} [opts.content] Initial content
	 * @param {String} [opts.mime] Content mime type
	 */
	startNewMessage: function(idfolder, opts) {
		opts=opts||{};
		
		var me=this,
			identIndex=0,
			identities=me.optionsData.identities,
			rcpts=opts.recipients||[{ rtype: 'to', email: ''}];
	
        for(var i=1;i<identities.length;++i) {
            var ifolder=identities[i].mainFolder;
            if (ifolder && idfolder.substring(0,ifolder.length)===ifolder) {
                identIndex=i;
                break;
            }
        }

		var v=WT.createView(me.ID,'view.MessageEditor',{
			viewCfg: {
				msgId: opts.msgId||0,
				mys: me,
				identityIndex: identIndex,
				fontFace: me.getOption('fontName'),
				fontSize: me.getOption('fontSize')
			}
		});
	
		v.show(false,function() {
			var meditor=v.getView();
			meditor.startNew({
				subject: opts.subject||'',
				receipt: me.getOption('receipt'),
				priority: me.getOption('priority'),
				from: identities[identIndex].email,
				recipients: rcpts,
				attachments: opts.attachments,
				content: opts.content||'',
                mime: opts.mime
			});
		});
	},
	
	actionAdvancedSearch: function() {},
	
	actionEmptyFolder: function(s,e) {
		var me=this,
			r=me.getCtxNode(e),
			folder=r.get("id");
	
		WT.confirm(me.res('sureprompt'),function(bid) {
			if (bid==='yes') {
				me.emptyFolder(folder);
			}
		},me);
	},
	
	actionDeleteFolder: function(s,e) {
		var me=this,
			r=me.getCtxNode(e),
			folder=r.get("id"),
			pn=r.parentNode;
	
		if (pn && pn.get("isTrash")) {
			WT.confirm(me.res('sureprompt'),function(bid) {
				if (bid==='yes') {
					me.deleteFolder(folder);
				}
			},me);
		}
		else {
			me.trashFolder(folder);
		}
	},
	
	actionRenameFolder: function(s,e) {
		var me=this,
			r=me.getCtxNode(e);
	
		me.imapTree.startEdit(r,0);
	},
	
    actionNewFolder: function(s,e) {
		var me=this,
			folder=me.getCtxNode(e).get("id");
        WT.prompt('',{
			title: me.res("act-newfolder.lbl"),
			fn: function(btn,value) {
				if (btn==="ok" && value && value!=="") me.createFolder(folder,value);
			}
		});
    },
	
    actionNewMainFolder: function() {
		var me=this;
		WT.prompt('',{
			title: me.res("act-newmainfolder.lbl"),
			fn: function(btn,value) {
				if (btn==="ok" && value && value!=="") me.createFolder(null,value);
			}
		});        
    },
	
	actionMoveToMainFolder: function(s,e) {
		var me=this,
			rec=me.getCtxNode(e);
	
		me.moveFolder(rec.get("id"),null);
	},
	
	actionFolderRefresh: function(s,e) {
		var me=this,
			rec=me.getCtxNode(e);
	
		if (rec) me.refreshFolder(rec);
	},
	
	actionScanFolder: function(mi,v) {
		var me=this,
			n=me.getCtxNode(e),
			folder=n.get("id");
	
		if (n.hasChildNodes()) {
			WT.confirm(me.res('recursive'),function(bid) {
				me.setScanFolder(folder,v,(bid=='yes'));
			},me);
		} else {
			me.setScanFolder(folder,v,false);
		}
	},
	
	actionFolderMarkSeen: function() {},
	actionDownloadMails: function() {},
	
    reloadFolderList: function() {
        this.messagesPanel.reloadGrid();
    },
	
	getCtxGrid: function(e) {
		var md=e.menuData;
		return (md && md.grid) ? md.grid : this.messagesPanel.folderList;
	},
	
	getCtxNode: function(e) {
		var md=e.menuData;
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

    createFolder: function(parent,name) {
		var me=this;
		WT.ajaxReq(me.ID, 'NewFolder', {
			params: {
				folder: parent,
				name: name
			},
			callback: function(success,json) {
				if (json.result) {
					var tr=me.imapTree,
						v=tr.getView(),
						s=tr.store,
						n=(parent?s.getNodeById(parent):s.getRoot()),
						newname=name,
						newfullname=json.fullname;
					n.expand(false,function(nodes) {
						var newnode=n.findChild("text",newname);
						if (!newnode) {
							newnode=n.createNode({
								id: newfullname,
								text: newname,
								folder: newname,
								leaf: true,
								iconCls: 'wtmail-icon-imap-folder-xs',
								unread:0,
								hasUnread:false
							});
							var cn=n.childNodes;
							var before=null;
							for(var c=0;before==null && c<cn.length;++c) {
								var cid=cn[c].id;
								if (me.specialFolders[cid]) continue;
								if (cid>newfullname) before=cn[c];
							}
							if (before) n.insertBefore(newnode,before);
							else n.appendChild(newnode);
						}
						v.setSelection(newnode);
						me.folderClicked(tr,newnode);
					});
				} else {
					WT.error(json.text);
				}
			}
		});
		
    },
	
	renameFolder: function(n,oldName,newName) {
		var me=this;
		WT.ajaxReq(me.ID, 'RenameFolder', {
			params: {
				folder: n.get("id"),
				name: newName
			},
			callback: function(success,json) {
				if (json.result) {
					n.set("id",json.newid);
				} else {
					WT.error(json.text);
				}
			}
		});					
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
						s=tr.store,
						n=s.getById(json.oldid);
					if (n) n.remove();
					n=(json.parent?s.getNodeById(json.parent):s.getRoot());
					me.selectChildNode(n,json.newid);
				} else {
					WT.error(json.text);
				}
			}
		});					
    },
	
	deleteFolder: function(folder) {
		var me=this;
		WT.ajaxReq(me.ID, 'DeleteFolder', {
			params: {
				folder: folder
			},
			callback: function(success,json) {
				if (json.result) {
					var node=me.imapTree.getStore().getById(folder);
					if (node) node.remove();
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
	trashFolder: function(folder) {
		var me=this;
		WT.ajaxReq(me.ID, 'TrashFolder', {
			params: {
				folder: folder
			},
			callback: function(success,json) {
				if (json.result) {
					var s=me.imapTree.getStore(),
						n=s.getById(folder);
					if (n) n.remove();
					if (json.newid && json.trashid) {
						n=s.getById(json.trashid);
						n.set("leaf",false);
						me.selectChildNode(n,json.newid);
					}
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
	emptyFolder: function(folder) {
		var me=this;
		WT.ajaxReq(me.ID, 'EmptyFolder', {
			params: {
				folder: folder
			},
			callback: function(success,json) {
				var tr=me.imapTree,
					s=tr.getStore(),
					n=s.getById(folder);
			
				if (json.result) {
					s.load({
						node: n
					});
				} else {
					WT.error(json.text);
				}
				if (folder===me.currentFolder)
					me.folderClicked(tr,n);
			}
		});					
	},
	
    setScanFolder: function(folder,v,recursive) {
		var me=this;
		WT.ajaxReq(me.ID, 'SetScanFolder', {
			params: {
				folder: folder,
				value: (v?"1":"0"),
				recursive: (recursive?"1":"0")
			},
			callback: function(success,json) {
				var tr=me.imapTree,
					s=tr.getStore(),
					n=s.getById(folder);
			
				if (json.result) {
					n.set("scanEnabled",v);
					if (!v) me.unreadChanged({ foldername: folder, unread: 0 },true);
					else me.refreshFolder(n);
					if (recursive)
						n.cascadeBy(function(n) {
							n.set("scanEnabled",v);
							if (!v) me.unreadChanged({ foldername: n.get("id"), unread: 0 },true);
							else me.refreshFolder(n);
						},me);
				} else {
					WT.error(json.text);
				}
			}
		});					
	},	
	
	selectChildNode: function(parentNode, childId) {
		var me=this,
			tr=me.imapTree;
	
		if (parentNode.isExpanded()) {
			tr.getStore().load({ node: parentNode });
		}
		parentNode.expand(false,function(nodes) {
			Ext.each(nodes,function(newnode) {
				if (newnode.getId()===childId) {
					tr.getView().setSelection(newnode);
					me.folderClicked(tr,newnode);
				}
			});
		});
	},
	
	refreshFolder: function(rec) {
		var me=this;
		me.imapTree.getSelectionModel().select(rec);
		me.showFolder(rec.get("id"));
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
	},
	
	updateCxmTree: function(r) {
		var me=this,
			id=r.get("id"),
			rootid=me.imapTree.getRootNode().get("id");
	
		me.getAction('emptyfolder').setDisabled(!r.get("isTrash")&&!r.get("isSpam"));

		me.getAction('deletefolder').setDisabled(me.specialFolders[id]);
		me.getAction('renamefolder').setDisabled(me.specialFolders[id]);
		me.getAction('movetomain').setDisabled(me.specialFolders[id]?true:(r.parentNode.get("id")===rootid));

		var as=me.getAction('scanfolder');
		var mi=me.getRef("mnuScan");
		if (r.get("scanOff")) { as.setDisabled(true); mi.setChecked(false,true); }
		else if (r.get("scanOn")) { as.setDisabled(true); mi.setChecked(true,true); }
		else {
			as.setDisabled(false);
			if (r.get("scanEnabled")) mi.setChecked(true,true);
            else mi.setChecked(false,true);
		}
        /*var as=this.aScan;
        var mi=this.miScan;
        if (a.scanOff) {as.setDisabled(true);mi.setChecked(false,true);}
        else if (a.scanOn) {as.setDisabled(true);mi.setChecked(true,true);}
        else {
            this.aScan.setDisabled(false);
            if (a.scanEnabled) mi.setChecked(true,true);
            else mi.setChecked(false,true);
        }*/
	}
	
	
});
