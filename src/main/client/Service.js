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
		'Sonicle.webtop.mail.plugin.ImapTreeViewDragDrop',
		'Sonicle.webtop.mail.ServiceApi',
		'Sonicle.webtop.mail.view.HiddenFolders',
		'Sonicle.webtop.mail.ux.SieveRuleGrid',
		'Sonicle.menu.StoreMenu',
		'Sonicle.webtop.mail.model.Tag'
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
	askreceipt: false,

	//state vars
	currentFolder: null,

	//default protocol ports
	protPorts: null,
    

	api: null,
	
	getApiInstance: function() {
		var me = this;
		if (!me.api) me.api = Ext.create('Sonicle.webtop.mail.ServiceApi', {service: me});
		return me.api;
	},
	
	init: function() {
		var me=this;
		
		me.initActions();

		//tags ctx menu needs this store to be prepared
		me.tagsStore=Ext.create('Ext.data.JsonStore',{
			autoLoad: true,
			model: 'Sonicle.webtop.mail.model.Tag',
			proxy: WTF.apiProxy(me.ID, 'ManageTags','data')
		})
		
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
			region: 'center',
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
						//keep enabled loadMask only for root loading
						me.imapTree.getView().loadMask=false;
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
		me.imapTree.getPlugin('cellediting').on("beforeedit",function(editor , context , eOpts) {
			var r=context.record;
			if (r.get("isSharedRoot")||r.get("isInbox")||r.get("isDrafts")||r.get("isSent")||r.get("isTrash")||r.get("isSpam")||r.get("isArchive")||(r.get("depth")===2 && r.get("isUnderShared"))) return false;
		});
		
		var capi=WT.getServiceApi("com.sonicle.webtop.calendar");
		me.calendarTool=capi.createEventsPortletBody({
			region: 'center',
			height: 150,
			title: WT.res("com.sonicle.webtop.calendar","portlet.events.tit")
		});
		var tapi=WT.getServiceApi("com.sonicle.webtop.tasks");
		me.tasksTool=tapi.createTasksPortletBody({
			region: 'south',
			split: true,
			height: 150,
			title: WT.res("com.sonicle.webtop.tasks","portlet.tasks.tit")
		});

		var tool = Ext.create({
				xtype: 'panel',
				title: me.getName(),
				width: 200,
				layout: 'border',
				items: [
					me.imapTree,
					{
						xtype: 'panel',
						height: 300,
						layout: 'border',
						region: 'south',
						split: true,
						items: [
							me.calendarTool,
							me.tasksTool
						]
					}
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
				xb[xx++]=me._TB("inMailFilters")
            ]
        );

		if (WT.isPermitted(me.ID,'FAX','ACCESS'))
			me.toolbar.insert(0,me._TB("newfax"));
		
		//TODO DOCMGT
        //if (WT.docmgt) this.toolbar.insertButton(4,xb[xx++]=new Ext.Button(this.getAct('docmgt')));
        //else if (WT.docmgtwt) this.toolbar.insertButton(4,xb[xx++]=new Ext.Button(this.getAct('docmgtwt')));
		
        mp.folderList.on("viewready",me.resizeColumns,me);
		
		me.setToolbar(me.toolbar);
		
		var ff=me.specialFolders;
		ff[me.getFolderInbox()]=
		 ff[me.getFolderDrafts()]=
		   ff[me.getFolderSent()]=
		    ff[me.getFolderTrash()]=
		     ff[me.getFolderSpam()]=true;

		if (me.getVar('autoResponderActive') === true) {
			Ext.defer(function() {
				WT.warn(me.res('warn.autoresponder'));
			}, 1000);
		}
		
	},
	
	_calendarTool_buildDateTimeInfo: function(start) {
		var me = this,
				soDate = Sonicle.Date,
				eDate = Ext.Date,
				startd = eDate.format(start, WT.getShortDateFmt()),
				startt = eDate.format(start, WT.getShortTimeFmt()),
				now = soDate.setTime(new Date(), 0, 0, 0),
				str0;
		
		if (soDate.diffDays(now, start) === 0) {
			str0 = me.mys.res('portlet.events.gp.dateat.today');
		} else if (soDate.diffDays(now, start) === 1) {
			str0 = me.mys.res('portlet.events.gp.dateat.tomorrow');
		} else {
			str0 = startd;
		}
		return Ext.String.format(me.mys.res('portlet.events.gp.dateat'), str0, startt);
	},
	
	
	_TB: function(actionname) {
		var bt=Ext.create('Ext.button.Button',this.getAct(actionname));
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
		if (WT.isPermitted(me.ID,'FAX','ACCESS'))
			me.addNewAction("newfax",{ handler: me.actionNewFax, scope: me});
		
        me.addAct("open",{ handler: me.gridAction(me,'Open'), iconCls: '' });
        me.addAct("opennew",{ handler: me.gridAction(me,'OpenNew'), iconCls: '' });
        
        me.addAct("newfax",{ handler: function() { me.actionNewFax(); }, text: null });
        me.addAct("print",{ handler: function() { me.messagesPanel.printMessageView(); }, iconCls: 'wt-icon-print-xs' });
        me.addAct("reply",{ handler: me.gridAction(me,'Reply') });
        me.addAct("replyall",{ handler: me.gridAction(me,'ReplyAll') });
        me.addAct("forward",{ handler: me.gridAction(me,'Forward') });
        me.addAct("forwardeml",{ handler: me.gridAction(me,'ForwardEml') });
        me.addAct('inMailFilters', {
			text: null,
			handler: function() {
				me.editInMailFilters();
			}
		});
		
		me.addAct("multisearch",{ handler: function() { me.messagesPanel.actionMultiSearch(); } , iconCls: 'wt-icon-search-multi-xs', enableToggle: true });
		
		
        me.addAct("special",{ handler: me.gridAction(me,'Flag','special') });
        me.addAct("flagred",{ handler: me.gridAction(me,'Flag','red')});
        me.addAct("flagblue",{ handler: me.gridAction(me,'Flag','blue')});
        me.addAct("flagyellow",{ handler: me.gridAction(me,'Flag','yellow')});
        me.addAct("flaggreen",{ handler: me.gridAction(me,'Flag','green')});
        me.addAct("flagorange",{ handler: me.gridAction(me,'Flag','orange')});
        me.addAct("flagpurple",{ handler: me.gridAction(me,'Flag','purple')});
        me.addAct("flagblack",{ handler: me.gridAction(me,'Flag','black')});
        me.addAct("flaggray",{ handler: me.gridAction(me,'Flag','gray')});
        me.addAct("flagwhite",{ handler: me.gridAction(me,'Flag','white')});
        me.addAct("flagbrown",{ handler: me.gridAction(me,'Flag','brown')});
        me.addAct("flagazure",{ handler: me.gridAction(me,'Flag','azure')});
        me.addAct("flagpink",{ handler: me.gridAction(me,'Flag','pink')});
        me.addAct("flagcomplete",{ handler: me.gridAction(me,'Flag','complete')});
        me.addAct("clear",{ handler: me.gridAction(me,'Flag','clear'), iconCls: '' });
		
        //me.addAct("newtag",{ handler: me.gridAction(me,'NewTag') });
        me.addAct("managetags",{ handler: me.gridAction(me,'ManageTags') });
        me.addAct("removetags",{ handler: me.gridAction(me,'RemoveAllTags') });
		
		
        me.addAct("addnote",{ handler: me.gridAction(me,'AddNote') });
	   
        me.addAct("markseen",{ handler: me.gridAction(me,'MarkSeen') });
        me.addAct("markunseen",{ handler: me.gridAction(me,'MarkUnseen') });
        me.addAct("spam",{ handler: me.gridAction(me,'Spam'), iconCls: 'wt-icon-block-xs' });
        me.addAct("delete",{ handler: me.gridAction(me,'Delete'), iconCls: 'wt-icon-delete-xs' });
        me.addAct("movetofolder",{ handler: me.gridAction(me,'MoveToFolder') });
        me.addAct("check",{ handler: function() { me.selectInbox(); }, iconCls: 'wt-icon-refresh-xs' });
        me.addAct("savemail",{ handler: me.gridAction(me,'SaveMail'), iconCls: 'wt-icon-save-xs' });
        me.addAct("createreminder",{ handler: me.gridAction(me,'CreateReminder'), iconCls: 'wtcal-icon-newEvent-xs' });
        me.addAct("archive",{ handler: me.gridAction(me,'Archive'), iconCls: 'wtmail-icon-archive-xs' });
        me.addAct("resetcolumns",{ handler: me.gridAction(me,'ResetColumns'), iconCls: '' });
        me.addAct("viewheaders",{ handler: me.gridAction(me,'ViewHeaders'), iconCls: '' });
        me.addAct("viewsource",{ handler: me.gridAction(me,'ViewSource'), iconCls: '' });
		
        //me.addAct("filterrow",{ handler: me.gridAction(me,'FilterRow'), enableToggle: true });		
        me.addAct("advsearch",{ handler: me.actionAdvancedSearch, scope: me, iconCls: 'wt-icon-search-adv-xs' });
        me.addAct("sharing",{ handler: me.actionSharing, scope: me, iconCls: 'wt-icon-sharing-xs' });
        me.addAct("showsharings",{ handler: null, iconCls: '' });
        me.addAct("threaded",{ handler: function() { me.messagesPanel.actionThreaded(); }, iconCls: 'wtmail-icon-threaded-xs', enableToggle: true });
        me.addAct("emptyfolder",{ handler: me.actionEmptyFolder, scope: me });
        me.addAct("deletefolder",{ handler: me.actionDeleteFolder, scope: me, iconCls: 'wt-icon-delete-xs' });
        me.addAct("renamefolder",{ handler: me.actionRenameFolder, scope: me });
        me.addAct("newfolder",{ handler: me.actionNewFolder, scope: me });
        me.addAct("newmainfolder",{ handler: me.actionNewMainFolder, scope: me });
        me.addAct("movetomain",{ handler: me.actionMoveToMainFolder, scope: me, iconCls: '' });
        me.addAct("refresh",{ handler: me.actionFolderRefresh, scope: me, iconCls: 'wt-icon-refresh-xs' });

        me.addAct("scanfolder",{ handler: null, iconCls: '' });
		
        me.addAct("hidefolder",{ handler: me.actionFolderHide, scope: me, iconCls: '' });
        me.addAct("managehiddenfolders",{ handler: me.actionManageHiddenFolders, scope: me, iconCls: '' });
        me.addAct("markseenfolder",{ handler: me.actionFolderMarkSeen, scope: me, iconCls: 'wtmail-icon-markseen-xs' });
        me.addAct("downloadmails",{ handler: me.actionDownloadMails, scope: me, iconCls: 'wt-icon-save-xs' });

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
				me.getAct('open'),
				me.getAct('opennew'),
				me.getAct('print'),
				'-',				
				me.getAct('reply'),
				me.getAct('replyall'),
				me.getAct('forward'),
				me.getAct('forwardeml'),
				me.getAct('special'),
				{
					text: me.res("menu-complete"),
					menu: {
						items: [
						  me.getAct('flagred'),
						  me.getAct('flagorange'),
						  me.getAct('flaggreen'),
						  me.getAct('flagblue'),
						  me.getAct('flagpurple'),
						  me.getAct('flagyellow'),
						  me.getAct('flagblack'),
						  me.getAct('flaggray'),
						  me.getAct('flagwhite'),
						  me.getAct('flagbrown'),
						  me.getAct('flagazure'),
						  me.getAct('flagpink'),
						  '-',
						  me.getAct('flagcomplete'),
						  //me.getAct('addmemo'),
						  me.getAct('clear')
						]
					}
				},
				{
					text: me.res("menu-tag"),
					menu: me.addRef('mnutag',Ext.create({
						xtype: 'sostoremenu',
						itemClass: 'Ext.menu.CheckItem',
						textField: 'html',
						tagField: 'tagId',
						textAsHtml: true,
						staticItems: [
						  //me.getAct('newtag'),
						  me.getAct('managetags'),
						  '-',
						  me.getAct('removetags'),
						  '-'
						],
						store: me.tagsStore,
						listeners: {
							click: function(menu,item,e) {
								if (item.tag) {
									var grid=me.getCtxGrid(e);
									if (item.checked)
										grid.actionTag(item.tag);
									else
										grid.actionUntag(item.tag);
								}
							}
						}
					}))
				},
				me.getAct('addnote'),
				me.getAct('markseen'),
				me.getAct('markunseen'),
				//me.getAct('categories'),
				'-',
				//me.getAct('findall'),
				//me.getAct('createrule'),
				me.getAct('spam'),
				'-',
				me.getAct('delete'),
				'-',
				me.getAct('movetofolder'),
				me.getAct('savemail'),
				me.getAct('createreminder'),
				'-',
				me.getAct("archive")
			],
			listeners: {
				beforeshow: function() {
				}
			}
		}));
			
		//TODO: document management
        /*if (WT.docmgt) {
            //cxmGrid.add('-');
            cxmGrid.add(me.getAct('docmgt'));
        } else if (WT.docmgtwt) {
            //cxmGrid.add('-');
            cxmGrid.add(me.getAct('docmgtwt'));
        }*/
		
        cxmGrid.add('-');
        cxmGrid.add(me.getAct('resetcolumns'));
        cxmGrid.add('-');
        cxmGrid.add(me.getAct('viewheaders'));
        cxmGrid.add(me.getAct('viewsource'));
		
		//tree menu
		var mscan,mshowsharings;
		me.addRef('cxmTree', Ext.create({
			xtype: 'menu',
			items: [
                me.getAct('advsearch'),
                me.getAct('emptyfolder'),
                '-',
                me.getAct('deletefolder'),
                me.getAct('renamefolder'),
                me.getAct('newfolder'),
                '-',
                me.getAct('movetomain'),
                me.getAct('newmainfolder'),
                '-',
                me.getAct('refresh'),
                '-',
                me.getAct('sharing'),
				mshowsharings=Ext.create('Ext.menu.CheckItem',me.getAct('showsharings')),
				'-',
				mscan=Ext.create('Ext.menu.CheckItem',me.getAct('scanfolder')),
                '-',
                me.getAct('downloadmails'),
                '-',
				me.getAct('managehiddenfolders'),
                me.getAct('hidefolder'),
                '-',
                me.getAct('markseenfolder')
			]
		}));
		mscan.on('click',me.actionScanFolder,me);
		me.addRef("mnuScan",mscan);
		mshowsharings.on('click',me.actionShowSharings,me);
		me.addRef("mnuShowSharings",mshowsharings);
		
		me.addRef('cxmBackTree', Ext.create({
			xtype: 'menu',
            items: [
                me.getAct('newmainfolder'),
                '-',
                me.getAct('managehiddenfolders'),
                '-',
                me.getAct('refresh')
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
	
	selectAndShowFolder: function(folderid,uid,rid,page,tid) {
		var me=this;
		
		me.imapTree.expandAndSelectNode(folderid,me.getVar("folderSeparator"));
		me.showFolder(folderid,uid,rid,page,tid);
	},
	
	//if uid, try to select specific uid
	showFolder: function(folderid,uid,rid,page,tid) {
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
        me.setActDisabled('spam',(folderid===me.getFolderSpam()));
        me.currentFolder=folderid;
		//TODO: clear filter textfield
        mp.filterTextField.setValue('');
		mp.quickFilterCombo.setValue('any');
        
		var params={start:0,limit:mp.getPageSize(),refresh:refresh,pattern:'',quickfilter:'any',threaded:2};
        mp.reloadFolder(folderid,params,uid,rid,page,tid);
	}, 
	
	unreadChanged: function(msg,unreadOnly) {
		var me=this,
			pl=msg.payload,
			node=me.imapTree.getStore().getById(pl.foldername);
		if (node) {
			var folder=node.get("folder");
			var oldunread=node.get("unread");
			if (!unreadOnly) node.set('hasUnread',pl.hasUnreadChildren);
			if (pl.unread!==oldunread) {
				console.log("unreadChanged: pl.foldername="+pl.foldername+" - pl.unread="+pl.unread+" oldunread="+oldunread);
				node.set('unread',pl.unread);
				node.set('folder','');
				node.set('folder',folder);
			}
		}
	},
	
	recentMessage: function(msg) {
		var me=this,
			pl=msg.payload;
		console.log("recentMessage: pl.foldername="+pl.foldername);
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
				body: pl.subject,
				data: {
					foldername: pl.foldername
				}
			}, {
				callbackService: true
			});
			
		}
		if (me.currentFolder===pl.foldername) {
			me.messagesPanel.refreshGridWhenIdle(pl.foldername);
		}
	},
	
	notificationCallback: function(type, tag, data) {
		if (type==='desktop') {
			var me=this;
			WT.activateService(me.ID);
			me.imapTree.expandAndSelectNode(data.foldername,me.getVar("folderSeparator"));
			me.showFolder(data.foldername);
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
			format: data.format,
			subject: data.subject,
			content: data.content,
			recipients: rcpts,
			contentReady: true,
			folder: data.folder,
			identityId: data.identityId,
			replyfolder: data.replyfolder,
			inreplyto: data.inreplyto,
			references: data.references,
			origuid: data.origuid,
			forwardedfolder: data.forwardedfolder,
			forwardedfrom: data.forwardedfrom
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
		var me=this;
		me.startNewMessage(me.currentFolder, { format: me.getVar("format") });
	},
	
	actionNewFax: function() {
		var me=this;
		me.startNewMessage(me.currentFolder, { format: "plain", fax: true, faxsubject: me.getVar("faxSubject") });
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
	 * @param {String} [opts.format] Content format type
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
				dockableConfig: {
					title: opts.fax?'{fax.tit}':'{message.tit}',
					iconCls: opts.fax?'wtmail-icon-newfax-xs':'wtmail-icon-newmsg-xs',
					width: 830,
					height: 500
				},
				msgId: opts.msgId||0,
				mys: me,
				identityIndex: ident.index||0,
				fontFace: me.getVar('fontName'),
				fontSize: me.getVar('fontSize'),
				autosave: true,
				showSave: !opts.fax,
				showReceipt: !opts.fax,
				showPriority: !opts.fax,
				showReminder: !opts.fax,
				showCloud: !opts.fax,
				fax: opts.fax,
				faxsubject: opts.faxsubject,
				listeners: {
					modelsave: function(s,success) {
						if (success) {
							var f=opts.forwardedfolder||opts.replyfolder;
							if (me.getFolderNodeById(me.currentFolder).get("isSent") || (f && f===me.currentFolder)) {
								me.messagesPanel.reloadGrid();
							}
						}
					}
				}
			},
		});
	
		v.show(false,function() {
			var meditor=v.getView();
			meditor.startNew({
				folder: idfolder,
				subject: opts.subject||opts.faxsubject||'',
				receipt: opts.receipt||me.getVar('receipt'),
				priority: opts.priority||me.getVar('priority'),
				from: ident.email,
                identityId: ident.identityId,
				recipients: rcpts,
				attachments: opts.attachments,
				content: opts.content||'',
                contentReady: opts.contentReady,
                contentAfter: opts.contentAfter,
                format: opts.format,
                replyfolder: opts.replyfolder,
                inreplyto: opts.inreplyto,
                references: opts.references,
                origuid: opts.origuid,
                forwardedfolder: opts.forwardedfolder,
                forwardedfrom: opts.forwardedfrom,
				fax: opts.fax
			});
		});
	},
	
	getFolderIdentity: function(idfolder) {
		var me=this,
			identities=me.getVar("identities"),
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
		return this.getVar("identities")[index];
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
	
	runSmartSearch: function() {
		var me=this,
			pattern=me.messagesPanel.filterTextField.getValue(),
			vw=WT.createView(me.ID,'view.SmartSearchDialog',{
				viewCfg: {
					mys: me,
					pattern: pattern
				}
			});
		
		vw.show(false, function() {
			if (pattern && pattern.length>0) vw.getView().runSearch();
		});
	},
	
	showSharingView: function(node) {
		var me=this,
			vw=WT.createView(me.ID,'view.Sharing',{
				viewCfg: {
					mys: me
				}
			});
	
		vw.getView().on("modelsave",function(v, success,r) {
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
	
	actionEmptyFolder: function(s,e) {
		var me=this,
			r=me.getCtxNode(e),
			folder=r.get("id");
	
		WT.confirm(me.res('sureprompt'),function(bid) {
			if (bid==='yes') {
				me.emptyFolder(folder);
			}
		});
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
			});
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
		else {
			me.reloadTree();
		}
	},
	
	actionScanFolder: function(mi,e) {
		var me=this,
			n=me.getCtxNode(e),
			folder=n.get("id"),
			v=mi.checked;
	
		if (n.hasChildNodes()) {
			WT.confirm(me.res('recursive'),function(bid) {
				me.setScanFolder(folder,v,(bid=='yes'));
			});
		} else {
			me.setScanFolder(folder,v,false);
		}
	},
	
	actionFolderHide: function(s,e) {
		var me=this,
			n=me.getCtxNode(e),
			folder=n.get("id");
	
		WT.confirm(me.res('confirm.folder-hide'),function(bid) {
			if (bid=='yes') 
				me.hideFolder(folder);
		});
	},
	
	actionManageHiddenFolders: function(s,e) {
		var me=this;
		WT.createView(me.ID,'view.HiddenFolders',{
			viewCfg: {
				callback: function() {
					me.reloadTree();
				}
			}
		}).show();
	},
	
	actionFolderMarkSeen: function(s,e) {
		var me=this,
			n=me.getCtxNode(e),
			folder=n.get("id");
	
		if (n.hasChildNodes()) {
			WT.confirm(me.res('recursive'),function(bid) {
				me.markSeenFolder(folder,(bid=='yes'));
			});
		} else {
				me.markSeenFolder(folder,false);
		}
	},
	
	actionDownloadMails: function(s,e) {
		var me=this,
			rec=me.getCtxNode(e);
	
		me.downloadMails(rec.get("id"));
	},
	
	reloadTags: function() {
		this.tagsStore.reload();
	},
	
	reloadTree: function() {
		this.imapTree.getStore().reload();
	},
	
    reloadFolderList: function() {
        this.messagesPanel.reloadGrid();
    },
	
	getCtxGrid: function(e) {
		var md=e.menuData;
		return (md && md.grid) ? md.grid : this.messagesPanel.folderList;
	},
	
	getCtxGridRecord: function(e) {
		var md=e.menuData;
		return (md && md.rec) ? md.rec : null;
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
	
	getFolderArchive: function() {
		return this.getVar('folderArchive');
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
	
	hideFolder: function(folder) {
		var me=this;
		WT.ajaxReq(me.ID, 'HideFolder', {
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
						me._createNode(n,newfullname,newname);
						n.expand(false,function(nodes) {
							var newnode=n.findChild("text",newname);
							v.setSelection(newnode);
							me.folderClicked(tr,newnode);
						});
				} else {
					WT.error(json.text);
				}
			}
		});
		
    },
	
	_createNode: function(n,newfullname,newname) {
		var me=this,
			newnode=n.createNode({
				id: newfullname,
				text: newname,
				folder: newname,
				leaf: true,
				iconCls: 'wtmail-icon-imap-folder-xs',
				unread:0,
				hasUnread:false
			}),
			cn=n.childNodes,
			before=null;
	
		for(var c=0;before==null && c<cn.length;++c) {
			var cid=cn[c].id;
			if (me.specialFolders[cid]) continue;
			if (cid>newfullname) before=cn[c];
		}
		if (before) n.insertBefore(newnode,before);
		else n.appendChild(newnode);
		return newnode;
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
						});
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
	
	isTrash: function(folder) {
		return this.imapTree.getStore().getById(folder).get("isTrash");
	},
	
	updateCxmGrid: function(r) {
		var me=this,
			menu=me.getRef('mnutag'),
			tags=r.get("tags");
		me.tagsStore.each(function(xr) {
			menu.getComponent(xr.get("hashId")).setChecked(false,true);
		});
		if (tags) {
			Ext.iterate(tags,function(tag) {
				var xr=me.tagsStore.findRecord('tagId',tag);
				if (xr) menu.getComponent(xr.get("hashId")).setChecked(true,true);
			});
		}
	},
	
	updateCxmTree: function(r) {
		var me=this,
			d=r.getData(),
			id=r.get("id"),
			rootid=me.imapTree.getRootNode().get("id");
	
		me.getAct('emptyfolder').setDisabled(!r.get("isTrash")&&!r.get("isSpam"));

		me.getAct('hidefolder').setDisabled(me.specialFolders[id]);
		me.getAct('deletefolder').setDisabled(me.specialFolders[id]);
		me.getAct('renamefolder').setDisabled(me.specialFolders[id]);
		me.getAct('movetomain').setDisabled(me.specialFolders[id]?true:(r.parentNode.get("id")===rootid));
		me.getAct('sharing').setDisabled(d.isUnderShared||d.isSharedRoot);

		var as=me.getAct('scanfolder');
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
	},
	
	editInMailFilters: function(opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.InMailFilters');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					id: 'in'
				},
				addMailFilter: opts.addMailFilter
			});
		});
	}
});
