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
	uses: [
		'Sonicle.ColorUtils'
	],

	favoritesTree: null,
	archiveTree: null,
	imapTree: null,
	
	acctTrees: {},
	
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
	currentAccount: null,
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
		var me = this,
				sue = me.getVar('showUpcomingEvents'),
				sut = me.getVar('showUpcomingTasks'),
				todayColor = me.getVar('todayRowColor'),
				todaySelColor;
		
		if (Sonicle.ColorUtils.bestForeColor(todayColor) === '#FFFFFF') {
			todaySelColor = Ext.util.Color.create(todayColor).createLighter().toHex();
		} else {
			todaySelColor = Ext.util.Color.create(todayColor).createDarker().toHex();
		}
		Sonicle.CssUtils.addRule('.wtmail-row-today', 'background:' + todayColor);
		Sonicle.CssUtils.addRule('table.x-grid-item-selected .wtmail-row-today', 'background:' + todaySelColor);

		me.initActions();

		//tags ctx menu needs this store to be prepared
		me.tagsStore=Ext.create('Ext.data.JsonStore',{
			autoLoad: true,
			model: 'Sonicle.webtop.mail.model.Tag',
			proxy: WTF.apiProxy(me.ID, 'ManageTags','data')
		});
		
		me.initCxm();
		
		me.viewmaxtos=me.getVar('messageViewMaxTos');
		me.viewmaxccs=me.getVar('messageViewMaxCcs');

		//create early but set imap store later to avoid early events on Firefox
		var mp=Ext.create('Sonicle.webtop.mail.MessagesPanel', {
			stateful: WT.plTags.desktop ? true : false,
			stateId: me.buildStateId('messagespanel'),
			viewMode: me.getVar('viewMode'),
			previewLocation: WT.plTags.desktop ? 'right' : 'off',
			pageSize: me.getVar('pageRows'),
			//viewRegion: me.getVar('messageViewRegion','east'),
			//viewWidth: me.getVar('messageViewWidth',600),
			//viewHeight: me.getVar('messageViewHeight',400),
			//viewCollapsed: me.getVar('messageViewCollapsed',false),
			saveColumnSizes: true,
			saveColumnVisibility: true,
			saveColumnOrder: true,
			savePaneSize: true,
			gridMenu: me.getRef('cxmGrid'),
			mys: me
		});
		me.messagesPanel=mp;
	
		
		me.favoritesTree=Ext.create('Sonicle.webtop.mail.FavoritesTree',{
			mys: me,
			acct: 'main',
			width: '100%',
			hideHeaders: true,
			rootVisible: true,
			padding: '0 0 20 0',
			border: false,
			bodyStyle: {
				borderTopColor: 'transparent'
			},
			
			listeners: {
				itemcontextmenu: function(v, rec, itm, i, e, eopts) {
					WT.showContextMenu(e, me.getRef('cxmFavorites'), { rec: rec });
				},
				containercontextmenu: function(v, e, eopts) {
					WT.showContextMenu(e, me.getRef('cxmBackTree'), { });
				},
				rowclick: function(t, r, tr, ix, e, eopts) {
					//me.imapTree.setSelection(null);
					//if (me.archiveTree) me.archiveTree.setSelection(null);
					me._unselectAllTreesBut(me.favoritesTree);
					me.folderClicked(r.get("account"), r, tr, ix, e, eopts);
					
					if(t.getSelection()[0].isRoot()) {
						var messagePanel = me.messagesPanel;
						messagePanel.clearGridSelections();
					}
				}
			}
			
		});
		
		if (me.getVar("isArchivingExternal")) {
			me.archiveTree=Ext.create('Sonicle.webtop.mail.ArchiveTree',{
				mys: me,
				acct: 'archive',
				width: '100%',
				hidden: true,
				hideHeaders: true,
				rootVisible: true,
				padding: '0 0 20 0',
				border: false,
				bodyStyle: {
					borderTopColor: 'transparent'
				},

				listeners: {
					itemcontextmenu: function(v, rec, itm, i, e, eopts) {
						WT.showContextMenu(e, me.getRef('cxmArchiveTree'), { rec: rec });
					},
					containercontextmenu: function(v, e, eopts) {
						WT.showContextMenu(e, me.getRef('cxmBackTree'), { });
					},
					rowclick: function(t, r, tr, ix, e, eopts) {
						//me.imapTree.setSelection(null);
						//me.favoritesTree.setSelection(null);
						me._unselectAllTreesBut(me.archiveTree);
						me.folderClicked(me.archiveTree.acct, r, tr, ix, e, eopts);
						
						if(t.getSelection()[0].isRoot()) {
							var messagePanel = me.messagesPanel;
							messagePanel.clearGridSelections();
						}
						
					}
				}

			});
		}
		
		me.imapTree=Ext.create('Sonicle.webtop.mail.ImapTree',{
			mys: me,
			acct: 'main',
			width: '100%',
			//region: 'center',
			hideHeaders: true,
			rootVisible: true,
			padding: '0 0 20 0',
			border: false,
			bodyStyle: {
				borderTopColor: 'transparent'
			},
			
			stateEvents : ['collapsenode', 'expandnode'],
			stateId : 'imaptree-state-id',
			statefulFolders : true,
			
			listeners: {
				itemcontextmenu: function(v, rec, itm, i, e, eopts) {
					me.updateCxmTree(rec);
					WT.showContextMenu(e, me.getRef('cxmTree'), { rec: rec });
				},
				containercontextmenu: function(v, e, eopts) {
					WT.showContextMenu(e, me.getRef('cxmBackTree'), { });
				},
				rowclick: function(t, r, tr, ix, e, eopts) {
					//me.favoritesTree.setSelection(null);
					//if (me.archiveTree) me.archiveTree.setSelection(null);
					me._unselectAllTreesBut(me.imapTree);
					me.folderClicked(me.imapTree.acct, r, tr, ix, e, eopts);
					
					if(t.getSelection()[0].isRoot()) {
						var messagePanel = me.messagesPanel;
						messagePanel.clearGridSelections();
					}
				},
				load: function(t,records,s,o,n) {
					if (n.id==='/') {
						//keep enabled loadMask only for root loading
						me.imapTree.getView().loadMask=false;
						me.selectInbox();
					}
					Ext.each(records,function(r) {
						me.updateFavoritesUnreads(r.get("account"),r.get("id"),r.get("unread"));
					});
				},
				foldersstaterestored: function(t,expandedNodes) {
					var me=this;
					//if no state was present, defaults to expand shared folders
					if (!expandedNodes) {
						var n=me.getRootNode();
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
			if (!r.get("canRename")) return false;
		});

		var trees=[
			me.favoritesTree,
		];
		if (me.archiveTree) {
			trees.push(me.archiveTree);
			me.acctTrees[me.archiveTree.acct]=me.archiveTree;
		}
		
		trees.push(me.imapTree);
		me.acctTrees[me.imapTree.acct]=me.imapTree;
		
		var extacc=me.getVar("externalAccounts");
		if (extacc.length>0) {
			var extids=extacc.split(",");
			Ext.each(extids,function(extid) {
				var tree=Ext.create('Sonicle.webtop.mail.ExternalAccountTree',{
					mys: me,
					acct: extid,
					readOnly: me.getVar('externalAccountReadOnly.'+extid),
					width: '100%',
					//region: 'center',
					hideHeaders: true,
					rootVisible: true,
					padding: '0 0 20 0',
					border: false,
					bodyStyle: {
						borderTopColor: 'transparent'
					},

					//stateEvents : ['collapsenode', 'expandnode'],
					//stateId : 'imaptree-state-id',
					//statefulFolders : true,

					listeners: {
						itemcontextmenu: function(v, rec, itm, i, e, eopts) {
							me.updateCxmTree(rec);
							WT.showContextMenu(e, me.getRef('cxmTree'), { rec: rec });
						},
						containercontextmenu: function(v, e, eopts) {
							WT.showContextMenu(e, me.getRef('cxmBackTree'), { });
						},
						rowclick: function(t, r, tr, ix, e, eopts) {
							//me.favoritesTree.setSelection(null);
							//if (me.archiveTree) me.archiveTree.setSelection(null);
							me._unselectAllTreesBut(tree);
							me.folderClicked(tree.acct, r, tr, ix, e, eopts);
							
							if(t.getSelection()[0].isRoot()) {
								var messagePanel = me.messagesPanel;
								messagePanel.clearGridSelections();
							}
						},
						load: function(t,records,s,o,n) {
							if (n.id==='/') {
								//keep enabled loadMask only for root loading
								tree.getView().loadMask=false;
							}
							Ext.each(records,function(r) {
								me.updateFavoritesUnreads(r.get("account"),r.get("id"),r.get("unread"));
							});
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

				tree.getPlugin('cellediting').on("beforeedit",function(editor , context , eOpts) {
					var r=context.record;
					if (!r.get("canRename")) return false;
				});

				trees.push(tree);
				me.acctTrees[extid]=tree;

			});

		}
		me.trees=Ext.create({
				xtype: 'panel',
				border: false,
				layout: 'vbox',
				region: 'center',
				scrollable: true,
				items: trees
		});
		
		//me.messagesPanel.setImapStore(me.imapTree.getStore());
		
		me.setMainComponent(me.messagesPanel);
		if (me.getVar('showUpcomingEvents')) {
			var capi=WT.getServiceApi("com.sonicle.webtop.calendar");
			if (capi)
				me.calendarTool=capi.createEventsPortletBody({
					title: WT.res("com.sonicle.webtop.calendar","portlet.events.tit"),
					border: false,
					width: '100%',
					flex: 1
				});
		}
		if (me.getVar('showUpcomingTasks')) {
			var tapi=WT.getServiceApi("com.sonicle.webtop.tasks");
			if (tapi)
				me.tasksTool=tapi.createTasksPortletBody({
					title: WT.res("com.sonicle.webtop.tasks","portlet.tasks.tit"),
					split: true,
					border: false,
					width: '100%',
					flex: 1
				});
		}
		
		var subtools=null;
		
		if (me.calendarTool||me.tasksTool) {
			var items=new Array(), sth = 0;
			if (me.calendarTool) {
				sth += 150;
				items.push(me.calendarTool);
			}
			if (me.tasksTool) {
				sth += 150;
				items.push(me.tasksTool);
			}
			subtools=Ext.create({
				region: 'south',
				xtype: 'panel',
				stateful: true,
				stateId: me.buildStateId('pnlint-main'),
				split: true,
				border: false,
				height: sth,
				layout: 'vbox',
				items: items
			});
		}

		var toolitems=new Array();
		toolitems.push(me.trees);
		if (subtools) toolitems.push(subtools);
		var tool = Ext.create({
			xtype: 'panel',
			title: me.getName(),
			width: 200,
			layout: 'border',
			border: false,
			items: toolitems
		});
		me.setToolComponent(tool);

		me.onMessage('unread',me.unreadChanged,me);
		me.onMessage('recent',me.recentMessage,me);
		me.onMessage('addContact', me.addContact, me);
		
        var xb=new Array();
		xx=0;
		me.toolbar=mp.toolbar;
        me.toolbar.insert(0,[
/*			xb[xx++]=me._TB("print"),
			xb[xx++]=me._TB("delete"),
			xb[xx++]=me._TB("spam"),
			xb[xx++]=me._TB('movetofolder'),
			"-",*/
			//{ xtype: 'tbspacer', width: 200 },
			/*"-",
			xb[xx++]=me._TB("check"),
			xb[xx++]=me._TB("print"),
			xb[xx++]=me._TB("delete"),*/
			"-",
			//xb[xx++]=me._TB("inMailFilters"),
			xb[xx++]=me.getActAs("inMailFilters","button",{ text: null, ui: 'default-toolbar' }),
			/*"-",
			xb[xx++]=me._TB("reply",true),
			xb[xx++]=me._TB("replyall",true),
			xb[xx++]=me._TB("forward",true),
			"-",*/
			//xb[xx++]=me._TB("advsearch")
			xb[xx++]=me.getActAs("advsearch","button",{ text: null, ui: 'default-toolbar' })
			/*"-"
			xb[xx++]=me._TB("check"),
			"-",
			xb[xx++]=me._TB("markseen"),
			xb[xx++]=me._TB("markunseen"),
			"-",*/
			//xb[xx++]=me._TB("inMailFilters")
		]
				);

		/*if (WT.isPermitted(me.ID,'FAX','ACCESS'))
			me.toolbar.insert(0,me._TB("newfax"));*/
		
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
		
		me.on("activate", function() {
			if (me.calendarTool) me.calendarTool.refresh();
			if (me.tasksTool) me.tasksTool.refresh();
		});
		
	},
	
	_unselectAllTreesBut: function(tree) {
		var me=this;
		Ext.iterate(me.acctTrees,function(k,i) {
			if (i!==tree) i.setSelection(null);
		});
		if (tree!==me.favoritesTree) me.favoritesTree.setSelection(null);
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
	
	
	_TB: function(actionname,text,scale) {
		var bt=Ext.create('Ext.button.Button',this.getAct(actionname));
		if (!text) bt.setText('');
		bt.setScale(scale||'medium');
		bt.setUI('default-toolbar');
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
	
        me.addNewAction("newmsg",{ handler: me.actionNew, scope: me, iconCls: 'wtmail-icon-newmsg' });
		if (WT.isPermitted(me.ID,'FAX','ACCESS'))
			me.addNewAction("newfax",{ handler: me.actionNewFax, scope: me});
		
        me.addAct("open",{ handler: me.gridAction(me,'Open'), iconCls: '' });
        me.addAct("opennew",{ handler: me.gridAction(me,'OpenNew'), iconCls: '' });
        
        me.addAct("newfax",{ handler: function() { me.actionNewFax(); }, text: null });
        me.addAct("print",{ handler: function() { me.messagesPanel.printMessageView(); }, iconCls: 'wt-icon-print', hidden: !WT.plTags.desktop });
        me.addAct("reply",{ handler: me.gridAction(me,'Reply'), iconCls: 'wtmail-icon-reply'  });
        me.addAct("replyall",{ handler: me.gridAction(me,'ReplyAll'), iconCls: 'wtmail-icon-replyall' });
        me.addAct("forward",{ handler: me.gridAction(me,'Forward'), iconCls: 'wtmail-icon-forward'  });
        me.addAct("forwardeml",{ handler: me.gridAction(me,'ForwardEml') });
		if (me.getVar("messageEditSubject") === true ) {
			me.addAct("editSubject",{ handler: me.gridAction(me,'EditSubject') });
		}
		
        me.addAct('inMailFilters', {
			text: me.res("inMailFilters.tit"),
			handler: function() {
				me.editInMailFilters();
			},
			iconCls: 'wtmail-icon-inMailFilters'
		});
		
		me.addAct("multisearch",{ handler: function() { me.messagesPanel.actionMultiSearch(); } , iconCls: 'wt-icon-search-multi', enableToggle: true });
		
		
        me.addAct("special",{ text: me.res('message.flag.special'), glyph: 'xf005@FontAwesome', iconCls: 'wtmail-flag-special', handler: me.gridAction(me,'Flag','special') });
        me.addAct("flagred",{ text: me.res('message.flag.red'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-red', handler: me.gridAction(me,'Flag','red')});
        me.addAct("flagblue",{ text: me.res('message.flag.blue'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-blue', handler: me.gridAction(me,'Flag','blue')});
        me.addAct("flagyellow",{ text: me.res('message.flag.yellow'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-yellow', handler: me.gridAction(me,'Flag','yellow')});
        me.addAct("flaggreen",{ text: me.res('message.flag.green'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-green', handler: me.gridAction(me,'Flag','green')});
        me.addAct("flagorange",{ text: me.res('message.flag.orange'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-orange', handler: me.gridAction(me,'Flag','orange')});
        me.addAct("flagpurple",{ text: me.res('message.flag.purple'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-purple', handler: me.gridAction(me,'Flag','purple')});
        me.addAct("flagblack",{ text: me.res('message.flag.black'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-black', handler: me.gridAction(me,'Flag','black')});
        me.addAct("flaggray",{ text: me.res('message.flag.gray'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-gray', handler: me.gridAction(me,'Flag','gray')});
        me.addAct("flagwhite",{ text: me.res('message.flag.white'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-white', handler: me.gridAction(me,'Flag','white')});
        me.addAct("flagbrown",{ text: me.res('message.flag.brown'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-brown', handler: me.gridAction(me,'Flag','brown')});
        me.addAct("flagazure",{ text: me.res('message.flag.azure'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-azure', handler: me.gridAction(me,'Flag','azure')});
        me.addAct("flagpink",{ text: me.res('message.flag.pink'), glyph: 'xf02e@FontAwesome', iconCls: 'wtmail-flag-pink', handler: me.gridAction(me,'Flag','pink')});
        me.addAct("flagcomplete",{ text: me.res('message.flag.complete'), glyph: 'xf00c@FontAwesome', iconCls: 'wtmail-flag-complete', handler: me.gridAction(me,'Flag','complete')});
        me.addAct("clear",{ handler: me.gridAction(me,'Flag','clear'), iconCls: '' });
		
        //me.addAct("newtag",{ handler: me.gridAction(me,'NewTag') });
        me.addAct("managetags",{ handler: me.gridAction(me,'ManageTags'), iconCls: null });
        me.addAct("removetags",{ handler: me.gridAction(me,'RemoveAllTags'), iconCls: null });
		
		
        me.addAct("addnote",{ handler: me.gridAction(me,'AddNote') });
	   
        me.addAct("markseen",{ handler: me.gridAction(me,'MarkSeen') });
        me.addAct("markunseen",{ handler: me.gridAction(me,'MarkUnseen') });
        me.addAct("spam",{ handler: me.gridAction(me,'Spam'), iconCls: 'wt-icon-block-xs' });
        me.addAct("delete",{ handler: me.gridAction(me,'Delete'), iconCls: 'wt-icon-delete' });
        me.addAct("movetofolder",{ handler: me.gridAction(me,'MoveToFolder') });
		//me.addAct('uploadtofolder', {} );
	
        me.addAct("check",{ handler: function() { me.selectInbox(); }, iconCls: 'wt-icon-refresh' });
        me.addAct("savemail",{ handler: me.gridAction(me,'SaveMail'), iconCls: 'wt-icon-save-xs' });
        me.addAct("createreminder",{ handler: me.gridAction(me,'CreateReminder'), iconCls: 'wtcal-icon-newEvent-xs' });
        me.addAct("archive",{ handler: me.gridAction(me,'Archive'), iconCls: 'wtmail-icon-archive-xs' });
        me.addAct("resetcolumns",{ handler: me.gridAction(me,'ResetColumns'), iconCls: '' });
        me.addAct("viewheaders",{ handler: me.gridAction(me,'ViewHeaders'), iconCls: '' });
        me.addAct("viewsource",{ handler: me.gridAction(me,'ViewSource'), iconCls: '' });
		
        me.addAct("showarchive",{ handler: null, iconCls: '', tooltip: null });
        //me.addAct("filterrow",{ handler: me.gridAction(me,'FilterRow'), enableToggle: true });		
        me.addAct("advsearch",{ handler: me.actionAdvancedSearch, scope: me, iconCls: 'wt-icon-search-adv' });
        me.addAct("sharing",{ handler: me.actionSharing, scope: me, iconCls: 'wt-icon-sharing-xs' });
        me.addAct("showsharings",{ handler: null, iconCls: '', tooltip: null });
        me.addAct("threaded",{ handler: function() { me.messagesPanel.actionThreaded(); }, iconCls: 'wtmail-icon-threaded-xs', enableToggle: true });
        me.addAct("emptyfolder",{ handler: me.actionEmptyFolder, scope: me });
        me.addAct("deletefolder",{ handler: me.actionDeleteFolder, scope: me, iconCls: 'wt-icon-delete' });
        me.addAct("renamefolder",{ handler: me.actionRenameFolder, scope: me });
        me.addAct("newfolder",{ handler: me.actionNewFolder, scope: me });
        me.addAct("newmainfolder",{ handler: me.actionNewMainFolder, scope: me });
        me.addAct("movetomain",{ handler: me.actionMoveToMainFolder, scope: me, iconCls: '' });
        me.addAct("refresh",{ handler: me.actionFolderRefresh, scope: me, iconCls: 'wt-icon-refresh' });
        me.addAct("refreshtree",{ handler: me.actionTreeRefresh, scope: me, iconCls: '' });
        me.addAct("favorite",{ handler: me.actionFavorite, scope: me, iconCls: 'wtmail-icon-favoriteadd-xs' });
        me.addAct("removefavorite",{ handler: me.actionRemoveFavorite, scope: me, iconCls: 'wtmail-icon-favoriteremove-xs' });

        me.addAct("scanfolder",{ handler: null, iconCls: '', tooltip: null });
		
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
				me.getAct("archive"),
				{
					text: me.res("menu-operations"),
					menu: {
						items: [
							me.getAct('movetofolder'),
							me.getAct('savemail'),
							me.getAct('editSubject'),
							me.getAct('createreminder'),
							me.getAct('resetcolumns')
						]      
					}
				}
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
		
		//        cxmGrid.add('-');
		//        cxmGrid.add(me.getAct('resetcolumns'));
        cxmGrid.add('-');
		cxmGrid.add({
			text: me.res("menu-headers"),
			menu: {
				items: [	
					me.getAct('viewheaders'),
					me.getAct('viewsource')
				]      
			}
		});
		//imap tree menu
		var mscan,mshowsharings,mshowarchive;
		me.addRef('cxmTree', Ext.create({
			xtype: 'menu',
			items: [
                me.getAct('advsearch'),
				me.getAct('favorite'),
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
                me.getAct('refreshtree'),
                '-',
                me.getAct('sharing'),
				mshowsharings=Ext.create('Ext.menu.CheckItem',me.getAct('showsharings')),
				'-',
				mshowarchive=Ext.create('Ext.menu.CheckItem',me.getAct('showarchive')),
				'-',
				mscan=Ext.create('Ext.menu.CheckItem',me.getAct('scanfolder')),
                '-',
                me.getAct('downloadmails'),
				Ext.create({
					xtype:'souploadmenuitem',
					tooltip: null,
					text: me.res('act-uploademail.lbl'),
					uploaderConfig: WTF.uploader(me.ID,'UploadToFolder',{
						mimeTypes: [
						 {title: "Email", extensions: "eml"}
						]
					}),
					listeners: {
						beforeupload: function(s,file) {
						},
						uploadcomplete: function(s,fok,ffailed) {
						},
						uploaderror: function(s, file, cause) {
						},
						uploadprogress: function(s,file) {
						},
						fileuploaded: function(s,file,resp) {
							var n=me.lastMenuData.rec,
								ctxAccount=me.getAccount(n),
								ctxFolder=n.get("id");
							WT.ajaxReq(me.ID, 'UploadToFolder', {
								params: {
									account: ctxAccount,
									folder:  ctxFolder,
									uploadId: resp.data.uploadId,
								},
								callback: function(success,json) {
									if (success) {
										me.selectAndShowFolder(ctxAccount,ctxFolder);
									} else {
										WT.error(json.text);
									}
								}
							});							
						}
					}
				}),
                '-',
				me.getAct('managehiddenfolders'),
                me.getAct('hidefolder'),
                '-',
                me.getAct('markseenfolder')
			],
			listeners: {
				beforeshow: function(s) {
					me.lastMenuData=s.menuData;
				}
			}
		}));
		mscan.on('click',me.actionScanFolder,me);
		me.addRef("mnuScan",mscan);
		mshowsharings.on('click',me.actionShowSharings,me);
		me.addRef("mnuShowSharings",mshowsharings);
		if (!me.getVar("isArchivingExternal")) mshowarchive.setDisabled(true);
		else {
			mshowarchive.on('click',me.actionShowArchive,me);
		}
		
		me.addRef('cxmBackTree', Ext.create({
			xtype: 'menu',
            items: [
                me.getAct('newmainfolder'),
                '-',
                me.getAct('managehiddenfolders'),
                '-',
                me.getAct('refreshtree')
            ]
        }));
		
		
		//archive tree menu
		if (me.archiveTree) {
			me.addRef('cxmArchiveTree', Ext.create({
				xtype: 'menu',
				items: [
					me.getAct('advsearch'),
					'-',
					me.getAct('refresh'),
					me.getAct('refreshtree'),
					'-',
					me.getAct('downloadmails')
				]
			}));

		}
		//favorite tree menu
		me.addRef('cxmFavorites', Ext.create({
			xtype: 'menu',
            items: [
                me.getAct('removefavorite')
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
		me.imapTree.getSelectionModel().select(1);
		me.showFolder(me.imapTree.acct,me.getFolderInbox());
	},

    /*folderSelected: function(t, r, ix) {
        var folderid=r.get("id");
		console.log("folderSelected: "+folderid);
		this.showFolder(folderid);
    },*/
	
	folderClicked: function(acct, r, tr, ix, e, eopts) {
		if (e && e.target.classList.contains('x-tree-expander')) return;
        
		var folderid=r.get("id");
		this.showFolder(acct,folderid);
	},
	
	selectAndShowFolder: function(acct,folderid,uid,rid,page,tid) {
		var me=this;
		
		me.acctTrees[acct].expandNodePath(folderid,me.getVar("folderSeparator"),true);
		me.showFolder(acct,folderid,uid,rid,page,tid);
	},
	
	//if uid, try to select specific uid
	showFolder: function(acct,folderid,uid,rid,page,tid) {
        var me=this,
		mp=me.messagesPanel;
		if (!mp) return;
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
		me.currentAccount=acct;
        me.currentFolder=folderid;
		
		var params,
		 keepFilters = mp.keepFilterButton.pressed;
		
		if(keepFilters)
			params={start:0,limit:mp.getPageSize(),refresh:refresh,pattern:'',quickfilter:'any',threaded:2};
		else {
			mp.searchComponent.setValue('');
			params = {start:0,limit:mp.getPageSize(),refresh:refresh,quickfilter:'any',threaded:2, query:'{"allText":"","conditions":[]}'};
		}
		
        mp.reloadFolder(acct,folderid,params,uid,rid,page,tid);
	}, 
	
	getAccount: function(node) {
		var account = node.data.account;
		if(!account)
			account = node.getTreeStore().acct;
		return account;
	},
	
	unreadChanged: function(msg,unreadOnly) {
		var me=this,
			pl=msg.payload,
			tree=me.acctTrees[pl.accountid],
			node=tree.getStore().getById(pl.foldername);

		//console.log("unread changed : "+pl.accountid+" - "+pl.foldername);
		if (node) {
			var folder=node.get("folder");
			var oldunread=node.get("unread");
			if (!unreadOnly) node.set('hasUnread',pl.hasUnreadChildren);
			if (pl.unread!==oldunread) {
				node.set('unread',pl.unread);
				node.set('folder','');
				node.set('folder',folder);
			}
		}
		me.updateFavoritesUnreads(pl.accountid,pl.foldername,pl.unread);
	},
	
	addContact: function(msg) {
		var me = this,
			pl = msg.payload,
			email = pl.email,
			personal = pl.personal;
			
		if(me.getVar('autoAddContact')) {
			var contactService = WT.getServiceApi('com.sonicle.webtop.contacts');
			var data = {
				displayName: personal,
				email1: email
			};
			contactService.addContact(data);
		}
			
	},
	
	updateFavoritesUnreads: function(account,foldername,unread) {
		var me=this,
			fnode=null;
		
		if (account!=='archive')
			me.favoritesTree.getStore().findBy(function(rec) {
				if (account===rec.get("account") && foldername===rec.get("id")) {
					fnode=rec;
					return true;
				}
				return false;
			});
		
		if (fnode) {
			folder=fnode.get("folder");
			oldunread=fnode.get("unread");
			if (unread!==oldunread) {
				fnode.set('unread',unread);
				fnode.set('folder','');
				fnode.set('folder',folder);
			}
		}
	},
	
	recentMessage: function(msg) {
		var me=this,
		pl=msg.payload;
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
					account: pl.accountid,
					foldername: pl.foldername
				}
			}, {
				callbackService: true
			});
			
		}
		if (me.currentAccount===pl.accountid && me.currentFolder===pl.foldername) {
			me.messagesPanel.refreshGridWhenIdle(pl.foldername);
		}
	},
	
	notificationCallback: function(type, tag, data) {
		if (type==='desktop') {
			var me=this,
				tree=me.acctTrees[data.account];
			WT.activateService(me.ID);
			tree.expandNodePath(data.foldername,me.getVar("folderSeparator"),true);
			me.showFolder(data.account,data.foldername);
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
			forwardedfrom: data.forwardedfrom,
			draftuid: data.draftuid,
			draftfolder: data.draftfolder
		});

		return true;
	},
	
	getFolderGroup: function(acct,foldername) {
		var me=this,
		node=me.acctTrees[acct].getStore().getById(foldername),
		group='';
		if (node) {
			group=node.get('group');
		}
		return group;
	},
	
	setFolderGroup: function(acct,foldername,group) {
		var me=this,
		node=me.acctTrees[acct].getStore().getById(foldername);
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
		
		var me=this,ident;
		
		if(opts.identityId) {
			ident = me.getIdentityFromId(opts.identityId)
		} else {
			ident = me.getFolderIdentity(idfolder);
		}
		rcpts=opts.recipients||[{ rtype: 'to', email: ''}];
	
		/*        for(var i=1;i<identities.length;++i) {
            var ifolder=identities[i].mainFolder;
            if (ifolder && idfolder.substring(0,ifolder.length)===ifolder) {
                identIndex=i;
                ident=identities[i];
                break;
            }
        }*/

		var meditor=WT.createView(me.ID,'view.MessageEditor',{
			swapReturn: true,
			viewCfg: {
				dockableConfig: {
					title: opts.fax?'{fax.tit}':'{message.tit}',
					iconCls: opts.fax?'wtmail-icon-newfax-xs':'wtmail-icon-newmsg',
					width: 830,
					height: 500
				},
				msgId: opts.msgId||0,
				mys: me,
				identityIndex: ident.index||0,
				fontFace: me.getVar('fontName'),
				fontSize: me.getVar('fontSize'),
				fontColor: me.getVar('fontColor'),
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
							if (me.getFolderNodeById(me.currentAccount,me.currentFolder).get("isSent") || (f && f===me.currentFolder)) {
								me.messagesPanel.reloadGrid();
							}
							else if (me.isDrafts(me.currentAccount,me.currentFolder) || (opts.draftuid>0 && opts.draftfolder===me.currentFolder)) {
								me.reloadFolderList();
								me.messagesPanel.clearMessageView();
							}
						}
					},
					viewclose: function() {
							if (me.isDrafts(me.currentAccount,me.currentFolder) || (opts.draftuid>0 && opts.draftfolder===me.currentFolder)) {
								me.reloadFolderList();
								me.messagesPanel.clearMessageView();
							}
					}
				}
			},
		});
	
		meditor.showView(function() {
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
				fax: opts.fax,
				draftuid: opts.draftuid,
				draftfolder: opts.draftfolder
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
	
	getIdentityFromId: function(identityId) {
		var me = this, identity,
				identities = me.getVar("identities");
		
		identities.forEach(function(element) {
			if(element.identityId === identityId) {
				identity = element;
				return;
			}
		});
		return identity;
	},	
	
	actionAdvancedSearch: function(s,e) {
		var me=this,
			fn=me.getCtxNode(e)||me.imapTree.getSelection()[0]||me.favoritesTree.getSelection()[0],
			acct= fn ? me.getAccount(fn) : null;
		
		if (acct) {
			WT.createView(me.ID,'view.AdvancedSearchDialog',{
				viewCfg: {
					mys: me,
					acct: acct,
					compactView: me.getVar("viewMode")==="compact",
					gridMenu: me.getRef('cxmGrid'),
					folder: fn.get("id"),
					folderText: fn.get("text"),
					folderHasChildren: true //fn.hasChildNodes()
				}
			}).show();
		}
	},
	
	actionSharing: function(s,e) {
		var me=this,
			fn=me.getCtxNode(e)||me.imapTree.getSelection()[0];
	
		me.showSharingView(fn);
	},
	
	runSmartSearch: function() {
		var me = this,
			pattern = me.messagesPanel.searchComponent.getValue(),
			acct = me.messagesPanel.currentAccount ? me.messagesPanel.currentAccount : me.imapTree.acct,
			vw = WT.createView(me.ID, 'view.SmartSearchDialog', {
				viewCfg: {
					mys: me,
					acct: acct,
					pattern: pattern,
					myFoldersText: acct!=='main'?me.acctTrees[acct].getStore().getRoot().get("folder"):null
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
	
	actionShowArchive: function(mi,e) {
		var me=this;
		if (me.archiveTree) {
			me.getRef('cxmTree').hide();
			me.imapTree.setLoading(true);
			WT.ajaxReq(me.ID, mi.checked?"ShowArchive":"HideArchive", {
				callback: function(success,json) {
					me.imapTree.setLoading(false);
					if (success) {
						me.archiveTree.setHidden(!mi.checked);
					} else {
						WT.error(json.message);
					}
				}
			});										
		}
	},
	
	actionShowSharings: function(mi,e) {
		this.imapTree.showSharings(mi.checked);
	},
	
	actionEmptyFolder: function(s,e) {
		var me=this,
			r=me.getCtxNode(e),
			folder=r.get("id"),
			acct=me.getAccount(r);
	
		WT.confirm(me.res('sureprompt'),function(bid) {
			if (bid==='yes') {
				me.emptyFolder(acct,folder);
			}
		});
	},
	
	actionDeleteFolder: function(s,e) {
		var me=this,
			r=me.getCtxNode(e),
			folder=r.get("id"),
			pn=r.parentNode,
			acct=me.getAccount(r);
	
		if (pn && pn.get("isTrash")) {
			WT.confirm(me.res('sureprompt'),function(bid) {
				if (bid==='yes') {
					me.deleteFolder(acct,folder);
				}
			});
		} else {
			if (me.checkIsInFavorites(folder)) {
				WT.confirm(me.res('folder.fav.confirm.delete'), function(bid) {
					if (bid === 'yes') me.trashFolder(acct, folder);
				});
			} else {
				me.trashFolder(acct, folder);
			}	
		}
	},
	
	checkIsInFavorites: function(folderId) {
		var tree = this.favoritesTree,
				found = false;
		if (tree) {
			tree.getStore().each(function(node) {
				if (node.get('depth') === 1) {
					if (Ext.String.startsWith(node.get('id'), folderId)) {
						found = true;
						return false;
					}
				}
			});
		}
		return found;
	},
	
	actionRenameFolder: function(s,e) {
		var me=this,
			r=me.getCtxNode(e),
			acct=me.getAccount(r);
	
		me.acctTrees[acct].startEdit(r,0);
	},
	
    actionNewFolder: function(s,e) {
		var me=this,
			node=me.getCtxNode(e),
			folder=node.get("id"),
			acct=me.getAccount(node);
	
        WT.prompt('',{
			title: me.res("act-newfolder.lbl"),
			fn: function(btn,value) {
				if (btn==="ok" && value && value!=="") me.createFolder(acct,folder,value);
			}
		});
    },
	
    actionNewMainFolder: function(s,e) {
		var me=this,
			node=me.getCtxNode(e),
			acct=me.getAccount(node);
	
		WT.prompt('',{
			title: me.res("act-newmainfolder.lbl"),
			fn: function(btn,value) {
				if (btn==="ok" && value && value!=="") me.createFolder(acct,null,value);
			}
		});        
    },
	
	actionMoveToMainFolder: function(s,e) {
		var me=this,
			rec=me.getCtxNode(e),
			acct=me.getAccount(rec);
			
		me.moveFolder(acct, rec.get("id"), null);
	},
	
	actionFolderRefresh: function(s,e) {
		var me=this,
			rec=me.getCtxNode(e);
	
		if (rec) me.refreshFolder(rec);
		else {
			me.reloadTree(me.getAccount(rec));
		}
	},
	
	actionTreeRefresh: function(s,e) {
		var me=this,
			rec=me.getCtxNode(e);
		this.reloadTree(me.getAccount(rec));
	},
	
	actionFavorite: function(s, e) {
		var me = this,
			n = me.getCtxNode(e),
			folder = n.get("id"),
			name = n.get("folder"),
			acct=me.getAccount(n);
	
        WT.prompt('',{
			title: me.res("act-newfavorite.lbl"),
			value: name,
			fn: function(btn,value) {
				if (btn==="ok" && value && value!=="") me.addToFavorites(acct,folder,value);
			}
		});
	},
	
	actionRemoveFavorite: function(s, e) {
		var me = this,
			n = me.getCtxNode(e),
			folder = n.get("id"),
			acct=me.getAccount(n);
			
		me.removeFavorite(acct,folder);
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
		var me=this,
			acct = me.acctTrees.main.acct;
		WT.createView(me.ID,'view.HiddenFolders',{
			viewCfg: {
				callback: function() {
					me.reloadTree(acct);
				}
			}
		}).show();
	},
	
	actionFolderMarkSeen: function(s,e) {
		var me=this,
			n=me.getCtxNode(e),
			folder=n.get("id"),
			acct=me.getAccount(n);
	
		if (n.hasChildNodes()) {
			WT.confirm(me.res('recursive'),function(bid) {
				me.markSeenFolder(acct,folder,(bid=='yes'));
			});
		} else {
			me.markSeenFolder(acct,folder,false);
		}
	},
	
	actionDownloadMails: function(s,e) {
		var me=this,
			rec=me.getCtxNode(e),
			acct=me.getAccount(rec);
	
		me.downloadMails(acct,rec.get("id"));
	},
	
	reloadTags: function() {
		this.tagsStore.reload();
	},
	
	reloadTree: function(acct) {
		var me=this,
			tree=me.acctTrees[acct];
		//this.imapTree.getStore().reload();
		tree.getStore().load({
			node: tree.getRootNode(),
			callback: function(recs,op,success) {
				if (success) {
					if (tree.restoreFoldersState) tree.restoreFoldersState();
				}
			}
		});
	},
	
	reloadFavorites: function() {
		//this.favoritesTree.getStore().reload();
		this.favoritesTree.getStore().load({
			node: this.favoritesTree.getRootNode()
		});
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
		var df=this.getVar('inboxFolder');
		if (df) return df;
		return "INBOX";
	},
	
	getFolderDrafts: function(id) {
		if (!id || id=='main' || id=='archive') return this.getVar('folderDrafts');
		return this.getVar('externalAccountDrafts.'+id);
	},
	
	getFolderSent: function(id) {
		if (!id || id=='main' || id=='archive') return this.getVar('folderSent');
		return this.getVar('externalAccountSent.'+id);
	},
	
	getFolderSpam: function(id) {
		if (!id || id=='main' || id=='archive') return this.getVar('folderSpam');
		return this.getVar('externalAccountSpam.'+id);
	},
	
	getFolderTrash: function(id) {
		if (!id || id=='main' || id=='archive') return this.getVar('folderTrash');
		return this.getVar('externalAccountTrash.'+id);
	},
	
	getFolderArchive: function(id) {
		if (!id || id=='main' || id=='archive') return this.getVar('folderArchive');
		return this.getVar('externalAccountArchive.'+id);
	},
	
	getFolderNodeById: function(acct,foldername) {
		return this.acctTrees[acct].getStore().getNodeById(foldername);
	},

    downloadMails: function(acct,folder) {
        var params={
			account: acct,
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
	
	addToFavorites: function(acct,folder,desc) {
		var me=this;
		WT.ajaxReq(me.ID, 'AddToFavorites', {
			params: {
				account: acct,
				folder: folder,
				description: desc
			},
			callback: function(success,json) {
				if (success) {
					me.reloadFavorites();
				} else {
					WT.error(json.text);
				}
			}
		});					
	},	
	
	removeFavorite: function(acct,folder) {
		var me=this;
		WT.ajaxReq(me.ID, 'RemoveFavorite', {
			params: {
				account: acct,
				folder: folder
			},
			callback: function(success,json) {
				if (success) {
					me.reloadFavorites();
				} else {
					WT.error(json.text);
				}
			}
		});					
	},	
	
	markSeenFolder: function(acct,folder,recursive) {
		var me=this;
		WT.ajaxReq(me.ID, 'SeenFolder', {
			params: {
				account: acct,
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
	
    createFolder: function(acct,parent,name) {
		var me=this;
		WT.ajaxReq(me.ID, 'NewFolder', {
			params: {
				account: acct,
				folder: parent,
				name: name
			},
			callback: function(success,json) {
				if (json.result) {
					var tr=me.acctTrees[acct],
					v=tr.getView(),
					s=tr.store,
					n=(parent?s.getNodeById(parent):s.getRoot()),
					newname=name,
					newfullname=json.fullname;
					me._createNode(n,newfullname,newname);
					n.expand(false,function(nodes) {
						var newnode=n.findChild("text",newname);
						v.setSelection(newnode);
						me.folderClicked(acct,newnode);
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
			hasUnread: false,
			canRename: true
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
		var me=this,
			acct=me.getAccount(n);
		WT.ajaxReq(me.ID, 'RenameFolder', {
			params: {
				account: acct,
				folder: n.get("id"),
				name: newName
			},
			callback: function(success,json) {
				if (json.result) {
					n.set("id",json.newid);
					me.reloadFavorites();
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
    moveFolder: function(acct, src, dst) {
		var me = this,
				message = me.checkIsInFavorites(src) ? me.res('folder.fav.confirm.delete') : me.res('sureprompt');
		
		WT.confirm(message, function(bid) {
			if (bid==='yes') {
				WT.ajaxReq(me.ID, 'MoveFolder', {
					params: {
						account: acct,
						folder: src,
						to: dst
					},
					callback: function(success,json) {
						if (json.result) {
							var tr=me.acctTrees[acct],
							s=tr.store,
							n=s.getById(json.oldid);
							if (n) n.remove();
							if (json.parent!=null && json.parent===me.getFolderInbox()) {
								me.reloadTree(acct);
							} else {
								n=(json.parent?s.getNodeById(json.parent):s.getRoot());
								if (n.get("leaf")) n.set("leaf",false);
								n.expand(false,function(nodes) {
									Ext.defer(function() {
										me.selectChildNode(acct,n,json.newid);
									},200);
								});
							}
							me.reloadFavorites();
						} else {
							WT.error(json.text);
						}
					}
				});	
			}
			//me.focus();
		},me);
    },
	
	deleteFolder: function(acct,folder) {
		var me=this;
		WT.ajaxReq(me.ID, 'DeleteFolder', {
			params: {
				account: acct,
				folder: folder
			},
			callback: function(success,json) {
				if (json.result) {
					var node=me.acctTrees[acct].getStore().getById(folder);
					if (node) node.remove();
					me.reloadFavorites();
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
	trashFolder: function(acct,folder) {
		var me=this;
		WT.ajaxReq(me.ID, 'TrashFolder', {
			params: {
				account: acct,
				folder: folder
			},
			callback: function(success,json) {
				if (json.result) {
					var s=me.acctTrees[acct].getStore(),
					n=s.getById(folder);
					if (n) n.remove();
					if (json.newid && json.trashid) {
						n=s.getById(json.trashid);
						n.set("leaf",false);
						me.selectChildNode(acct,n,json.newid);
					}
					me.reloadFavorites();
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
	emptyFolder: function(acct,folder) {
		var me=this;
		WT.ajaxReq(me.ID, 'EmptyFolder', {
			params: {
				account: acct,
				folder: folder
			},
			callback: function(success,json) {
				var tr=me.acctTrees[acct],
				s=tr.getStore(),
				n=s.getById(folder);
			
				if (json.result) {
					s.load({
						node: n
					});
				} else {
					WT.error(json.text);
				}
				if (folder===me.currentFolder && acct===me.currentAccount)
					me.folderClicked(acct,n);
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
	
	selectChildNode: function(acct, parentNode, childId) {
		var me=this,
			tr=me.acctTrees[acct];
	
		if (parentNode.isExpanded()) {
			tr.getStore().load({ node: parentNode });
		}
		parentNode.expand(false,function(nodes) {
			Ext.each(nodes,function(newnode) {
				if (newnode.getId()===childId) {
					tr.getView().setSelection(newnode);
					me.folderClicked(acct,newnode);
				}
			});
		});
	},
	
	refreshFolder: function(rec) {
		var me=this,
			acct=me.getAccount(rec);
		me.acctTrees[acct].getSelectionModel().select(rec);
		me.showFolder(acct,rec.get("id"));
	},
	
    openEml: function(acct,folder,idmessage,idattach) {
		var win=WT.createView(this.ID,'view.EmlMessageView',{
			viewCfg: {
				mys: this,
				acct: acct,
				folder: folder,
				idmessage: idmessage,
				idattach: idattach
			}
		});
		win.show(false,function() {
			win.getView().showEml();
		});
	},
	
    copyAttachment: function(acctfrom,from,acctto,to,idmessage,idattach) {
		var me=this;
		WT.ajaxReq(me.ID, "CopyAttachment", {
			params: {
				fromaccount: acctfrom,
				fromfolder: from,
				toaccount: acctto,
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
	
	isDrafts: function(acct,folder) {
		var rfolder=this.acctTrees[acct].getStore().getById(folder);
		return rfolder?rfolder.get("isDrafts"):false;
	},
	
	isTrash: function(acct,folder) {
		var rfolder=this.acctTrees[acct].getStore().getById(folder);
		return rfolder?rfolder.get("isTrash"):false;
	},
	
	updateCxmGrid: function(r) {
		var me=this,
			menu=me.getRef('mnutag'),
			tags=r.get("tags");
			ro=me.messagesPanel.folderList.readonly;

		me.tagsStore.each(function(xr) {
			var comp=menu.getComponent(xr.get("hashId"));
			comp.setChecked(false,true);
		});
		if (tags) {
			Ext.iterate(tags,function(tag) {
				var xr=me.tagsStore.findRecord('tagId',tag);
				if (xr) menu.getComponent(xr.get("hashId")).setChecked(true,true);
			});
		}
		me.getAct('spam').setDisabled(ro);
		me.getAct('delete').setDisabled(ro);
		me.getAct('archive').setDisabled(ro);
		me.getAct('delete').setDisabled(ro);
		me.getAct('movetofolder').setDisabled(ro);
	},
	
	updateCxmTree: function(r) {
		var me=this,
			d=r.getData(),
			id=r.get("id"),
			acct=me.getAccount(r),
			rootid=me.acctTrees[acct].getRootNode().get("id"),
			readonly=me.getVar('externalAccountReadOnly.'+acct);
	
		me.getAct('emptyfolder').setDisabled(readonly || (!r.get("isTrash")&&!r.get("isSpam")));

		me.getAct('hidefolder').setDisabled(me.specialFolders[id]);
		me.getAct('deletefolder').setDisabled(readonly || me.specialFolders[id]);
		me.getAct('renamefolder').setDisabled(readonly || me.specialFolders[id]);
		me.getAct('movetomain').setDisabled(readonly || (me.specialFolders[id]?true:(r.parentNode.get("id")===rootid)));
		me.getAct('newfolder').setDisabled(readonly);
		me.getAct('newmainfolder').setDisabled(readonly);

		var ismain=(acct=='main');
		me.getAct('sharing').setDisabled(!ismain);
		me.getAct('showsharings').setDisabled(!ismain);
		me.getAct('showarchive').setDisabled(!ismain);
		me.getAct('scanfolder').setDisabled(!ismain);
		me.getAct('managehiddenfolders').setDisabled(!ismain);
		me.getAct('hidefolder').setDisabled(!ismain);
		
		if (ismain) {
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
		} else {
		}
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
