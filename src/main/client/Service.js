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
	extend: 'WTA.sdk.Service',
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
		var me=this;
		
		me.initActions();
		me.initCxm();
		
		me.viewmaxtos=me.getVar('messageViewMaxTos');
		me.viewmaxccs=me.getVar('messageViewMaxCcs');
		
		var mp=Ext.create('Sonicle.webtop.mail.MessagesPanel',{
			pageSize: me.getVar('pageRows'),
			viewRegion: me.getVar('messageViewRegion','east'),
			viewWidth: me.getVar('messageViewWidth',600),
			viewHeight: me.getVar('messageViewHeight',400),
			viewCollapsed: me.getVar('messageViewCollapsed',false),
			saveColumnSizes: true,
			saveColumnVisibility: true,
			saveColumnOrder: true,
			savePaneSize: true,
			gridMenu: me.getRef('cxmGrid'),
			mys: me
		});
		me.messagesPanel=mp;
		me.setMainComponent(me.messagesPanel);
		
		me.imapTree=Ext.create('Sonicle.webtop.mail.ImapTree',{
			mys: me,
			listeners: {
				itemcontextmenu: function(v, rec, itm, i, e, eopts) {
					me.updateCxmTree(rec);
					WT.showContextMenu(e, me.getRef('cxmTree'), { rec: rec });
				},
				containercontextmenu: function(v, e, eopts) {
					WT.showContextMenu(e, me.getRef('cxmBackTree'), { });
				},
				rowclick: function(t, r, tr, ix, e, eopts) {
					me.folderClicked(t, r, tr, ix, e, eopts);
				},
				load: function(t,r,s,o,n) {
					if (n.id==='root') {
						me.selectInbox();
						Ext.each(n.childNodes,function(cn,cx,an) {
							if (cn.get("isSharedRoot")) {
								cn.expand();
							}
						});
					}
				},
				edit: function(ed, e) {
					if (e.colIdx===0)
						me.renameFolder(e.record,e.originalValue,e.value);
				},
				columnshow: function(ct,c) {
					if (c.getIndex()==2)
						me.getRef("mnuShowSharings").setChecked(true,true);
				},
				columnhide: function(ct,c) {
					if (c.getIndex()==2)
						me.getRef("mnuShowSharings").setChecked(false,true);
				}
			}
		});

		var tool = Ext.create({
				xtype: 'panel',
				//title: 'Mail Toolbox',
				width: 200,
				layout: 'fit',
				items: [
					me.imapTree
				]
		});
		me.setToolComponent(tool);

		me.onMessage('unread',me.unreadChanged,me);
		me.onMessage('recent',me.recentMessage,me);
		
        var xb=new Array();
			xx=0;
		me.toolbar=mp.toolbar;
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
                xb[xx++]=me._TB("rules")
            ]
        );
		//TODO FAX
        //if (WT.hasFax) this.toolbar.insertButton(0,xb[xx++]=new Ext.Button(this.getAction('newfax')));
		//TODO DOCMGT
        //if (WT.docmgt) this.toolbar.insertButton(4,xb[xx++]=new Ext.Button(this.getAction('docmgt')));
        //else if (WT.docmgtwt) this.toolbar.insertButton(4,xb[xx++]=new Ext.Button(this.getAction('docmgtwt')));
		
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
        
        me.addAction("print",{ handler: function() { me.messagesPanel.printMessageView(); }, iconCls: 'wt-icon-print-xs' });
        me.addAction("reply",{ handler: me.gridAction(me,'Reply') });
        me.addAction("replyall",{ handler: me.gridAction(me,'ReplyAll') });
        me.addAction("forward",{ handler: me.gridAction(me,'Forward') });
        me.addAction("forwardeml",{ handler: me.gridAction(me,'ForwardEml') });
        me.addAction("rules",{ handler: me.actionRules, scope: me, iconCls: 'wt-icon-rules-xs' });
        
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
        me.addAction("resetcolumns",{ handler: me.gridAction(me,'ResetColumns'), iconCls: '' });
        me.addAction("viewheaders",{ handler: me.gridAction(me,'ViewHeaders'), iconCls: '' });
        me.addAction("viewsource",{ handler: me.gridAction(me,'ViewSource'), iconCls: '' });
		
        //me.addAction("filterrow",{ handler: me.gridAction(me,'FilterRow'), enableToggle: true });		
        me.addAction("advsearch",{ handler: me.actionAdvancedSearch, scope: me, iconCls: 'wt-icon-search-adv-xs' });
        me.addAction("sharing",{ handler: me.actionSharing, scope: me, iconCls: 'wt-icon-sharing-xs' });
        me.addAction("showsharings",{ handler: null, iconCls: '' });
        me.addAction("threaded",{ handler: function() { me.messagesPanel.actionThreaded(); }, iconCls: 'wtmail-icon-threaded-xs', enableToggle: true });
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
        cxmGrid.add(me.getAction('resetcolumns'));
        cxmGrid.add('-');
        cxmGrid.add(me.getAction('viewheaders'));
        cxmGrid.add(me.getAction('viewsource'));
		
		//tree menu
		var mscan,mshowsharings;
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
                me.getAction('sharing'),
				mshowsharings=Ext.create('Ext.menu.CheckItem',me.getAction('showsharings')),
				'-',
				mscan=Ext.create('Ext.menu.CheckItem',me.getAction('scanfolder')),
                '-',
                me.getAction('downloadmails'),
                '-',
                me.getAction('markseenfolder')
			]
		}));
        //mscan.on('checkchange',me.actionScanFolder,me);
		mscan.on('click',me.actionScanFolder,me);
		me.addRef("mnuScan",mscan);
		mshowsharings.on('click',me.actionShowSharings,me);
		me.addRef("mnuShowSharings",mshowsharings);
        //cxmTree.on('hide',this.treeMenuHidden,this);
		
		var cxmBackTree=me.addRef('cxmBackTree', Ext.create({
			xtype: 'menu',
            items: [
                this.getAction('newmainfolder'),
                '-',
                this.getAction('refresh')/*,
                '-',
                me.getAction('sharing')*/
            ]
        }));
	},
	
	resizeColumns: function() {
		var me=this;
        if (!me.resizedcols) {
			var colsizes=me.getVarAsObject('columnSizes');
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
        
        mp.reloadFolder(folderid,{start:0,limit:mp.getPageSize(),refresh:refresh,pattern:'',quickfilter:'any',threaded:2});
	}, 
	
	unreadChanged: function(msg,unreadOnly) {
		var me=this,
			pl=msg.payload;
			node=me.imapTree.getStore().getById(pl.foldername);
		if (node) {
			var folder=node.get("folder");
			if (!unreadOnly) node.set('hasUnread',pl.hasUnreadChildren);
			node.set('unread',pl.unread);
			node.set('folder','');
			node.set('folder',folder);

		}
	},
	
	recentMessage: function(msg) {
		var pl=msg.payload;
		if (pl.foldername==='INBOX') {
			//var msg=me.res('ntf.newmsg.inbox-has')+" "+cfg.unread+" ";
			//if (cfg.unread===1) msg+=me.res('ntf.newmsg.new-message');
			//else msg+=me.res('ntf.newmsg.new-messages');
			//WT.showDesktopNotification(me.ID,{
			//	title: me.res('ntf.newmsg.recent-messages'),
			//	body: msg
			//});
			WT.showDesktopNotification(this.ID,{
				title: Ext.String.ellipsis(pl.from,30),
				body: pl.subject
			});
		}
	},
	
	autosaveRestore: function(value) {
		var me=this,
			data=Ext.JSON.decode(value),
			rcpts=[];
	
		if (data.recipients) {
			for(var i=0;i<data.recipients.length;++i) {
				rcpts[rcpts.length]={
					rtype: data.rtypes[i]||'to',
					email: data.recipients[i]||''
				};
			}
		}
		
		me.startNewMessage(me.getFolderInbox(), { 
			mime: 'text/html',
			subject: data.subject,
			content: data.content,
			recipients: rcpts,
			contentReady: true,
			folder: data.folder,
			identityId: data.identityId,
			mime: data.mime,
			replyfolder: data.replyfolder,
			inreplyto: data.inreplyto,
			references: data.references,
			origuid: data.origuid,
			forwardedfolder: data.forwardedfolder,
			forwardedfrom: data.forwardedfrom,
		});

		return true;
	},
	
	getFolderGroup: function(foldername) {
		var me=this,
			node=me.imapTree.getStore().getById(foldername),
			group='';
		if (node) {
			group=node.get('group');
		}
		return group;
	},
	
	setFolderGroup: function(foldername,group) {
		var me=this,
			node=me.imapTree.getStore().getById(foldername);
		if (node) {
			node.set('group',group);
		}
	},
	
	actionNew: function() {
		this.startNewMessage(this.currentFolder, { mime: 'text/plain' });
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
//			identIndex=0,
//			identities=me.varsData.identities,
//          ident=identities[0],
			ident=me.getFolderIdentity(idfolder);
			rcpts=opts.recipients||[{ rtype: 'to', email: ''}];
	
/*        for(var i=1;i<identities.length;++i) {
            var ifolder=identities[i].mainFolder;
            if (ifolder && idfolder.substring(0,ifolder.length)===ifolder) {
                identIndex=i;
                ident=identities[i];
                break;
            }
        }*/

		var v=WT.createView(me.ID,'view.MessageEditor',{
			viewCfg: {
				msgId: opts.msgId||0,
				mys: me,
				identityIndex: ident.index||0,
				fontFace: me.getVar('fontName'),
				fontSize: me.getVar('fontSize'),
				autosave: true
			}
		});
	
		v.show(false,function() {
			var meditor=v.getView();
			meditor.startNew({
				folder: idfolder,
				subject: opts.subject||'',
				receipt: opts.receipt||me.getVar('receipt'),
				priority: opts.priority||me.getVar('priority'),
				from: ident.email,
                identityId: ident.identityId,
				recipients: rcpts,
				attachments: opts.attachments,
				content: opts.content||'',
                contentReady: opts.contentReady,
                mime: opts.mime,
                replyfolder: opts.replyfolder,
                inreplyto: opts.inreplyto,
                references: opts.references,
                origuid: opts.origuid,
                forwardedfolder: opts.forwardedfolder,
                forwardedfrom: opts.forwardedfrom
			});
		});
	},
	
	getFolderIdentity: function(idfolder) {
		var me=this,
			identities=me.varsData.identities,
            ident=identities[0];
	
        for(var i=1;i<identities.length;++i) {
            var ifolder=identities[i].mainFolder;
            if (ifolder && idfolder.substring(0,ifolder.length)===ifolder) {
                ident=identities[i];
                ident.index=i;
                break;
            }
        }
		
		return ident;
	},
	
	getIdentity: function(index) {
		return this.varsData.identities[index];
	},
	
	actionAdvancedSearch: function(s,e) {
		var me=this,
			fn=me.getCtxNode(e)||me.imapTree.getSelection()[0];
	
		WT.createView(me.ID,'view.AdvancedSearchDialog',{
			viewCfg: {
				mys: me,
				gridMenu: me.getRef('cxmGrid'),
				folder: fn.get("id"),
				folderText: fn.get("text"),
				folderHasChildren: true //fn.hasChildNodes()
			}
		}).show();
	},
	
	actionSharing: function(s,e) {
		var me=this,
			fn=me.getCtxNode(e)||me.imapTree.getSelection()[0];
	
		me.showSharingView(fn);
	},
	
	showSharingView: function(node) {
		var me=this,
			vw=WT.createView(me.ID,'view.Sharing',{
				viewCfg: {
					mys: me
				}
			});
	
		vw.getView().on("modelsave",function(v, op, success,r) {
			if (success) {
				var s=me.imapTree.getStore(),
					n=s.getNodeById(r.get("id")),
					m=r.get("method"),
					pn=m==="all"?s.getRootNode():n.parentNode;
					
				
				s.load({
					node: pn,
					callback: function() {
						if (m==="branch") s.getNodeById(r.get("id")).expand();
					}
				});
			}
		},me,{ single: true });
	
		vw.show(false, function() {
			vw.getView().begin('edit', {
				data: {
					id: node.get("id")
				}
			});
		});
	},
	
	actionShowSharings: function(mi,e) {
		this.imapTree.showSharings(mi.checked);
	},
	
	actionRules: function() {
		var me=this;
		
		WT.createView(me.ID,'view.RulesManager',{
			viewCfg: {
				mys: me,
				context: 'INBOX',
				vacation: {
					active: me.varsData.vacationActive,
					message: me.varsData.vacationMessage,
					addresses: me.varsData.vacationAddresses
				}
			}
		}).show();
	},
	
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
	
	actionScanFolder: function(mi,e) {
		var me=this,
			n=me.getCtxNode(e),
			folder=n.get("id"),
			v=mi.checked;
	
		if (n.hasChildNodes()) {
			WT.confirm(me.res('recursive'),function(bid) {
				me.setScanFolder(folder,v,(bid=='yes'));
			},me);
		} else {
			me.setScanFolder(folder,v,false);
		}
	},
	
	actionFolderMarkSeen: function(s,e) {
		var me=this,
			n=me.getCtxNode(e),
			folder=n.get("id");
	
		if (n.hasChildNodes()) {
			WT.confirm(me.res('recursive'),function(bid) {
				me.markSeenFolder(folder,(bid=='yes'));
			},me);
		} else {
				me.markSeenFolder(folder,false);
		}
	},
	
	actionDownloadMails: function(s,e) {
		var me=this,
			rec=me.getCtxNode(e);
	
		me.downloadMails(rec.get("id"));
	},
	
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
		return this.getVar('folderDrafts');
	},
	
	getFolderSent: function() {
		return this.getVar('folderSent');
	},
	
	getFolderSpam: function() {
		return this.getVar('folderSpam');
	},
	
	getFolderTrash: function() {
		return this.getVar('folderTrash');
	},
	
	getFolderNodeById: function(foldername) {
		return this.imapTree.getStore().getNodeById(foldername);
	},

    downloadMails: function(folder) {
        var params={
            folder: folder
        };
        var url=WTF.processBinUrl(this.ID,"DownloadMails",params);;
        window.open(url);
    },
	
	markSeenFolder: function(folder,recursive) {
		var me=this;
		WT.ajaxReq(me.ID, 'SeenFolder', {
			params: {
				folder: folder,
				recursive: (recursive?"1":"0")
			},
			callback: function(success,json) {
				if (json.result) {
					me.reloadFolderList();
				} else {
					WT.error(json.text);
				}
			}
		});					
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
	
    copyAttachment: function(from,to,idmessage,idattach) {
		var me=this;
		WT.ajaxReq(me.ID, "CopyAttachment", {
			params: {
				fromfolder: from,
				tofolder: to,
				idmessage: idmessage,
				idattach: idattach
			},
			callback: function(success,json) {
				if (success) {
					if (to===me.currentFolder) {
						me.reloadFolderList();
					}
				} else {
					WT.error(json.message);
				}
			}
		});							
    },	
	
	isDrafts: function(folder) {
		return this.imapTree.getStore().getById(folder).get("isDrafts");
	},
	
	updateCxmTree: function(r) {
		var me=this,
			d=r.getData(),
			id=r.get("id"),
			rootid=me.imapTree.getRootNode().get("id");
	
		me.getAction('emptyfolder').setDisabled(!r.get("isTrash")&&!r.get("isSpam"));

		me.getAction('deletefolder').setDisabled(me.specialFolders[id]);
		me.getAction('renamefolder').setDisabled(me.specialFolders[id]);
		me.getAction('movetomain').setDisabled(me.specialFolders[id]?true:(r.parentNode.get("id")===rootid));
		me.getAction('sharing').setDisabled(d.isUnderShared||d.isSharedRoot);

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
	},
	
	getCalendarApi: function() {
		return WT.getServiceApi("com.sonicle.webtop.calendar");
	}
	
});
