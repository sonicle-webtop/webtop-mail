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

Ext.define('Sonicle.webtop.mail.MessagesPanel', {
	extend: 'Ext.panel.Panel',
	requires: [
		'Sonicle.webtop.core.ux.field.SuggestCombo',
		'Sonicle.webtop.mail.MessageView',
		'Sonicle.webtop.mail.MessageGrid'
	],
	uses: [
		'Sonicle.button.PlainToggle'
	],
    layout:'border',
    border: false,
	
	viewRegion: 'east',
	viewWidth: "40%",
	viewHeight: "40%",
	viewCollapsed: false,
    
	keepFilterButton: null,
	searchComponent: null,
    //groupCombo: null,
	labelMessages: null,
    folderList: null,
    messageView: null,
    messageViewContainer: null,
    folderPanel: null,
    gridMenu: null,
    saveColumnSizes: false,
	saveColumnVisibility: false,
    savePaneSize: false,
	
    initComponent: function() {
		var me=this;
		
		me.callParent(arguments);
		
		//The breadcrumb store will be set later through setImapStore, to avoid too early events
		me.bcFolders=Ext.create({
						xtype: 'sobreadcrumb',
						overflowHandler: 'scroller',
						hidden: !WT.plTags.desktop,
						minDepth: 1,
							listeners: {
							change: function(s, node) {
								if(node && node.getId()!=='/' && !me.reloadingFolder) {
									me.mys.selectAndShowFolder(me.currentAccount,node.getId());
								}
							}
						},
						flex: 1
					});

		me.labelMessages=Ext.create({
			xtype: 'tbtext',
			text: '0'
		});
		
		me.searchComponent = Ext.create({
			xtype: 'wtsearchfield',
					reference: 'fldsearch',
					fields: [{
						name: 'from',
						type: 'string',
						label: me.res('fld-search.field.from.lbl')
					}, {
						name: 'to',
						type: 'string',
						label: me.res('fld-search.field.to.lbl')
					}, {
						name: 'subject',
						type: 'string',
						label: me.res('fld-search.field.subject.lbl')
					}, {
						name: 'message',
						type: 'string',
						label: me.res('fld-search.field.message.lbl')
					}, /*{
						name: 'everywhere',
						type: 'string',
						textSink: true,
						label: me.res('fld-search.field.everywhere.lbl')
					}, */{
						name: 'after',
						type: 'date',
						labelAlign: 'left',
						label: me.res('fld-search.field.after.lbl')
					}, {
						name: 'before',
						type: 'date',
						labelAlign: 'left',
						label: me.res('fld-search.field.before.lbl')
					}, {
						name: 'attachment',
						type: 'boolean',
						label: me.res('fld-search.field.attachment.lbl')
					}, {
						name: 'unread',
						type: 'boolean',
						label: me.res('fld-search.field.unread.lbl')
					}, {
						name: 'flagged',
						type: 'boolean',
						label: me.res('fld-search.field.flagged.lbl')
					}, {
						name: 'tagged',
						type: 'boolean',
						label: me.res('fld-search.field.tagged.lbl')
					}, {
						name: 'unanswered',
						type: 'boolean',
						label: me.res('fld-search.field.unanswered.lbl')
					}, {
						name: 'priority',
						type: 'boolean',
						label: me.res('fld-search.field.priority.lbl')
					}],
					tooltip:  me.res('fld-search.tip'),
					emptyText:  me.res('fld-search.emp'),
					listeners: {
						query: function(s, value, qObj) {
							me.queryMails(qObj);
						},
						enterkeypress: function(s, e) {
							if(e.ctrlKey) {
								me.runSmartSearch();
								return false;
							}
						}
					}
		});
		
		me.keepFilterButton = Ext.create({
			xtype: 'soplaintogglebutton',
			onIconCls: 'wtmail-icon-search-locked',
			offIconCls: 'wtmail-icon-search-unlocked',
			onTooltip: {title: me.res('fld-keepFilter.on.tip.tit'), text: me.res('fld-keepFilter.on.tip.txt')},
			offTooltip: {title: me.res('fld-keepFilter.off.tip.tit'), text: me.res('fld-keepFilter.off.tip.txt')},
			pressed: false
		});
        /*me.groupCombo=Ext.create(WTF.localCombo('id', 'desc', {
            width: 120,
			matchFieldWidth: false,
			listConfig: { width: 100 },
			store: Ext.create('Sonicle.webtop.mail.store.Group', {
				autoLoad: true
			}),
            value: '',
			tooltip: me.res('group.tip'),
			plugins: [ 'sofieldtooltip' ],
			listeners: {
				select: function(cb,r,eopts) {
					me.folderList.changeGrouping(r.get('id'));
				}
			}
        }));*/
		
		var compactView=me.mys.getVar("viewMode")==="compact";
		
        me.folderList=Ext.create('Sonicle.webtop.mail.MessageGrid',{
            region:'center',
			pageSize: 50,//me.pageSize,
			mys: me.mys,
			mp: me,
			currentAccount: me.currentAccount,
			compactView: compactView,
			breadcrumb: me.bcFolders,
			createPagingToolbar: true,
			stateful: WT.plTags.desktop?true:false,
			baseStateId: me.mys.buildStateId('messagegrid'),			
/*			dockedItems: [
				Ext.create('Ext.toolbar.Toolbar',{
					border: false,
					bodyStyle: {
						borderTopColor: 'transparent'
					},
					items: tbitems
				})
			]*/
        });
		if (me.gridMenu) {
			me.folderList.on("itemcontextmenu",function(s, rec, itm, i, e) {
				me.mys.updateCxmGrid(rec);
				WT.showContextMenu(e, me.gridMenu, { rec: rec, row: i, grid: me.folderList });
			});
		}
		
        var msgSelModel=me.folderList.getSelectionModel();
        msgSelModel.on('selectionchange',me.selectionChanged,me);
		me.folderList.on('keydelete',me.actionDelete,me);
		me.folderList.on('deleting',me.clearMessageView,me);
		me.folderList.on('moving',me.clearMessageView,me);

		//trick to reset unreads on empty folders' nodes
		me.folderList.on('load',function(g,foldername,data) {
			if (data && data.total && data.total===0) {
				me.mys.unreadChanged({
					foldername: foldername,
					unread: 0
				},true);
			} 
		});
		
		me.folderList.on('totals',function(g,total,realTotal,quotaLimit,quotaUsage) {
			var text=''+total;
			if (me.folderList.threaded && realTotal)
				text+=" ["+realTotal+"]";
			if (quotaLimit) {
				text+=" - "+WT.res("word.quota")+": "+Math.round(quotaUsage/1024)+"MB / "+
						Math.round(quotaLimit/1024)+"MB ("+
						Math.round(100*quotaUsage/quotaLimit)+"%)";
			}
			me.labelMessages.setText(text);
		});
		
		me.folderList.on("afterrender",function() {
			me.gridMonitor = Ext.create('Sonicle.ActivityMonitor', {
				trackMoveEvents: false,
				targetEl: me.folderList.body
			});
			me.gridMonitor.start(10000);
			me.gridMonitor.on('change',function(gm,idle) {
				//console.log("idle change event: "+idle+" idleRefreshFolder="+me.folderList.idleRefreshFolder);
				if (idle && me.currentFolder && me.currentFolder===me.folderList.idleRefreshFolder) {
					me._refreshIdleGrid();
				}
			});
		});


        //if (me.saveColumnSizes) me.folderList.on('columnresize',me.columnResized,me);
		//if (me.saveColumnVisibility) {
		//	me.folderList.on('columnhide', function(ct,col) { me.columnHiddenChange(ct,col,true); });
		//	me.folderList.on('columnshow', function(ct,col) { me.columnHiddenChange(ct,col,false); });
		//}
		//if (me.saveColumnOrder) me.folderList.on('columnmove', me.columnMoved, me);

        me.messageView=Ext.create('Sonicle.webtop.mail.MessageView',{
			mys: me.mys,
			mp: me
        });
        me.messageView.on('messageviewed',me.messageViewed,me);

        me.folderList.store.on("metachange",function(s,meta) {
            var gg=meta.groupField;
			if (gg==='') gg='none';
            //me.groupCombo.setValue(gg);
            //me.bThreaded.toggle(meta.threaded);
        });
        
        me.toolbar=Ext.create('Ext.toolbar.Toolbar',{ 
			defaults: {
				scale: WT.getHeaderScale()
			},			
			items:[
				'->',
				me.searchComponent,
				me.keepFilterButton,
				{ xtype: 'tbspacer', width: 100 },
				//me.res("groupby")+":",
				//me.groupCombo,
				{ xtype: 'tbspacer', width: 100 },
				'->',
				me.res('messages')+":",
				me.labelMessages
			]
		});

        me.messageViewContainer=Ext.create({
			xtype: 'wtpanel',
			stateful: WT.plTags.desktop,
			stateId: me.mys.buildStateId('ctmessageview'),
            region: WT.plTags.desktop?"east":"south", //me.viewRegion,
            cls: 'wtmail-mv-container',
            layout: 'fit',
            split: true,
            collapseMode: 'mini',
			header: false,
            collapsible : WT.plTags.desktop,
			collapsed: false, //me.viewCollapsed,
            height: WT.plTags.desktop?'40%':'60%', //me.viewHeight,
			width: '40%', //me.viewWidth,
            bodyBorder: false,
            border: false,
			tbar: Ext.create('Ext.toolbar.Toolbar',{
				hidden: true,
				items: [
					me.getAct("reply"),
					me.getAct("replyall"),
					me.getAct("forward"),
					'-',
					me.getAct("delete"),
					me.getAct("spam")
					/*'-',
					me.getAct("markseen"),
					me.getAct("markunseen")*/
				]
			}),
            items: [ me.messageView ]
        });
        me.add(me.folderList);
        me.add(me.messageViewContainer);
		me.messageViewContainer.on('resize', function(c,w,h) {
			if (!me.movingPanel) {
				//if (me.viewRegion==='east') me.viewWidth=w;
				//else me.viewHeight=h;
				//me.saveMessageView(me.viewRegion==='east'?w:h);
			}
		});
		me.messageViewContainer.on("expand",function() {
			var sel=me.folderList.getSelection();
			if (sel.length==1) {
				var r=sel[0],
					acct=me.currentAccount,
					id=r.get('idmessage'),
					fldr=r.get('fromfolder');
				if (!fldr) fldr=me.currentFolder;
				me.showMessage(acct,fldr,id,r);
			} else {
				me.clearMessageView();
			}
		});
		
		//TODO: save pane size
		/*
        if (this.savePaneSize) {
            this.on("afterlayout",function() {
                var r = this.getLayout()["south"];
                r.split.un("beforeapply", r.onSplitMove, r);            
                r.onSplitMove = r.onSplitMove.createSequence(this.messageViewResized, this);
                r.split.on("beforeapply", r.onSplitMove, r);            
            },this);
        }
		*/


    },
	
	//setImapStore: function(store) {
	//	this.bcFolders.setStore(store);
	//},
	
	getAct: function(name) {
		return this.mys.getAct(name);
	},
	
	getViewRegion: function() {
		return this.viewRegion;
	},
	
	switchViewPanel: function() {
		var me=this,
			r=(me.viewRegion==='east'?'south':'east'),
			w=me.viewWidth,
			h=me.viewHeight;
	
		me.moveViewPanel(r,w,h,true);
	},
	
	refreshGridWhenIdle: function(foldername) {
		var me=this;
		//console.log("refreshGridWhenIdle: idleRefreshFolder="+me.folderList.idleRefreshFolder);
		if (me.currentFolder===foldername && me.folderList.idleRefreshFolder!==foldername) {
			//console.log("refreshGridWhenIdle: foldername="+foldername);
			if (me.gridMonitor && me.gridMonitor.isIdle()) {
				me._refreshIdleGrid();
			} else {
				//console.log("not idle, saving idleRefreshFolder");
				me.folderList.idleRefreshFolder=foldername;
			}
		}
	},
	
	_refreshIdleGrid: function() {
		var me=this,
			sel=me.folderList.getSelection();
		if (sel) me.folderList.selectOnRefresh(sel);
		me.reloadGrid();
	},
	
	moveViewPanel: function(r,w,h,save,collapsed) {
		var me=this,
			mv=me.messageView,
			mvc=me.messageViewContainer,
			idm=mv.idmessage,
			idf=mv.folder,
			acct=mv.account;
	
		//TODO: necessary???
		////trick for switched user forced as south standard, no save
		//if (WT.loginusername!=WT.username) {
		//	r=null;
		//	save=false;
		//}
		
		me.movingPanel=true;
		me.clearMessageView();
		if (!r) {
			r="south";
			w="40%";
			h="40%";
		}
		me.viewRegion=r;
		me.viewWidth=w;
		me.viewHeight=h;
		me.viewCollapsed=collapsed;
		mvc.setRegion(me.viewRegion);
		mvc.setWidth(me.viewWidth);
		mvc.setHeight(me.viewHeight);
		me.updateLayout();
		if (me.viewCollapsed) mvc.collapse();
		//if (save) me.saveMessageView();
		me.showMessage(acct,idf,idm,mv.model);
		me.movingPanel=false;
	},
	
	saveMessageView: function(newsize) {
		var me=this;
		//collapsed has value string right/... or false
		me.viewCollapsed=me.messageViewContainer.collapsed!==false;
		if (!me.viewCollapsed && newsize) {
			if (me.viewRegion==="south") me.viewHeight=newsize;
			else me.viewWidth=newsize;
		}
		WT.ajaxReq(me.mys.ID, 'SetMessageView', {
			params: {
				region: me.viewRegion,
				width: me.viewWidth,
				height: me.viewHeight,
				collapsed: me.viewCollapsed
			}
		});
		
	},
	
/*    
    setViewSize: function(hperc) {
        if (this.ownerCt) {
            var h=parseInt(this.ownerCt.getSize().height*hperc/100);
            this.messageViewContainer.setHeight(h);
            this.updateLayout();
        }
    },
*/
    
    printMessageView: function() {
        this.messageView.print();
    },
/*	
    gridContextMenu: function(e,t) {
        var grid=this.folderList;
        WT.MessageGrid.setContextGrid(grid);
        var row = grid.view.findRowIndex(t);
        var sm=grid.getSelectionModel();
        if (row>=0 && !sm.isSelected(row)) sm.selectRow(row, false);

    },
    
    archiveSelection: function(from,to,selection) {
        this.folderList.archiveSelection(from,to,selection);
    },
    
    archiveSelectionWt: function(from,to,selection,context,customer_id) {
        this.folderList.archiveSelectionWt(from,to,selection,context,customer_id);
    },
    */
   
    //TODO verify rawData and getById
    messageViewed: function(idmessage,millis) {
		var fl=this.folderList;
        
		if (!this.mys.getVar("manualSeen"))
			fl.updateRecordSeenStateAtIndex(fl.indexOfMessage(idmessage),true);
    },
	
	queryMails: function(query) {
		var isString = Ext.isString(query),
			obj = {
				allText: isString ? query : query.anyText,
				conditions: isString ? [] : query.conditionArray
			};
		this.reloadCurrentFolder({query: Ext.JSON.encode(obj)});
	},
	    
	reloadFiltered: function(quickfilter, query) {
		var me=this;
		
//        me.folderList.store.baseParams={service: 'mail', action: 'ListMessages', folder: this.currentFolder, quickfilter: quickfilter, searchfield: field, pattern: pattern, refresh:1};
//        me.folderList.store.reload({
//          params: {start:0,limit:me.ms.pageRows}
//        });
//        me.folderList.store.baseParams.refresh=0;      
		me.reloadCurrentFolder({
			start:0,
			query: query,
			limit: me.getPageSize(),
			quickfilter: quickfilter,
			refresh: 1
		});
	},
     
	setPageSize: function(size) {
		this.folderList.setPageSize(size);
	},
	
	getPageSize: function() {
		return this.folderList.getPageSize();
	},
     
/*	actionThreaded: function() {
        var me=this;
		me.reloadFolder(me.currentFolder,{
			threaded: me.bThreaded.pressed?1:0
		});
        me.folderList.focus();
    },    */
    
    clearGridSelections: function() {
        this.folderList.getSelectionModel().deselectAll();
    },
    
    reloadFolder: function(acct,folderid,config,uid,rid,page,tid) {
		var me=this,
			tree=me.mys.acctTrees[acct],
			store=tree.getStore();
        me.currentAccount=acct;
        me.currentFolder=folderid;
		me.bcFolders.setStore(store);
		if (store.getCount()>1) me._runReloadFolder(tree,store,acct,folderid,config,uid,rid,page,tid);
		else {
			tree.expandNode(tree.getRootNode(),false,function() {
				me._runReloadFolder(tree,store,acct,folderid,config,uid,rid,page,tid);
			});
		}
    },
	
	_runReloadFolder: function(tree,store,acct,folderid,config,uid,rid,page,tid) {
		var me=this,
			node=store.getById(folderid);
		//node may be a favorite, so may not be already loaded in imap tree
		if (node) {
			me._updateBreadcrumbAndReloadFolderList(acct,node,config,uid,rid,page,tid,true);
		} else {
			tree.expandNodePath(folderid,me.mys.getVar("folderSeparator"),false,null,function() {
				node=me.bcFolders.getStore().getById(folderid);
				me._updateBreadcrumbAndReloadFolderList(acct,node,config,uid,rid,page,tid,false);
			});
		}
	},
	
	_updateBreadcrumbAndReloadFolderList: function(acct,node,config,uid,rid,page,tid,updateLeafState) {
		var me=this;
		me.reloadingFolder=true;
		me.bcFolders.setSelection(node);
		if(!node.data.leaf && node.data.expandable && !node.isLoaded()) {
			var leaf=node.isLeaf();
			me.bcFolders.getStore().load({
				node:node,
				callback: function() {
					if (updateLeafState && !leaf) {
						node.set("leaf",true);
						node.set("leaf",false);
					}
				}
			});
		}
		me.folderList.reloadFolder(acct,node.id,config,uid,rid,page,tid);
		me.reloadingFolder=false;
	},

	 reloadCurrentFolder: function(config) {
		var me=this;
        me.folderList.reloadFolder(me.currentAccount,me.currentFolder,config);
    },
	
	//TODO no more ctrlshift?
    selectionChanged: function(sm,r,eopts) {
        var me=this,
			c=sm.getCount();
		if (!me.messageViewContainer.collapsed) {
			//if (c==1&&!ctrlshift) {
			if (c===1) {
				var id=r[0].get('idmessage'),
					fldr=r[0].get('fromfolder'),
					acct=me.currentAccount;
				
				if (!fldr) fldr=this.currentFolder;
				if (id!==me.messageView.idmessage)
					me.showMessage(acct,fldr,id,r[0]);
			} else {
				me.clearMessageView();
			}
		}
        me.fireEvent('gridselectionchanged',sm/*,ctrlshift*/);
		if(r.length === 0)
			me.messageView.createEmptyItem();
    },
    
	showMessage: function(acct,folder,id,rec) {
		if (acct && folder && id) {
			var me=this;
			if (WT.plTags.desktop) me.messageViewContainer.getTopBar().show();
			me.messageView._showMessage(acct, folder, id, !me.mys.getVar("manualSeen"), rec);
		}
	},

    clearMessageView: function() {
		var me=this;
		if (WT.plTags.desktop) me.messageViewContainer.getTopBar().hide();
        me.messageView._clear();
    },
	
    reloadGrid: function() {
        this.folderList.reload();
    },
    
	runSmartSearch: function() {
		var me = this;
		me.mys.runSmartSearch();
	},

    actionDelete: function(g,e,sm) {
		var me=this;
        if (e.ctrlKey) me.folderList.actionSpam();
		else {
			if (!e.altKey) me.folderList.actionDelete(e);
		}
    },
   
    columnResized: function(ct, col, w) {
		WT.ajaxReq(this.mys.ID, 'SaveColumnSize', {
			params: {
                name: col.dataIndex,
                size: w
			}
		});					
        
    },
	
	columnHiddenChange: function(ct,col,h) {
		WT.ajaxReq(this.mys.ID, 'SaveColumnVisibility', {
			params: {
				folder: this.currentFolder,
				name: col.dataIndex,
				visible: !h
			}
		});
	},
	
	columnMoved: function(ct,col,oi,ni) {
		if(this.folderList.updatingColumns) return;
		var arr = [], i, cols=this.folderList.getColumns();
		for(i=0; i<cols.length; i++) {
			arr.push(cols[i].dataIndex);
		}
		WT.ajaxReq(this.mys.ID, 'SaveColumnsOrder', {
			params: {
				orderInfo: Ext.JSON.encode(arr)
			}
		});
	},
	
	/*
    messageViewResized: function() {
        var ah=this.messageViewContainer.getHeight();
        if (ah==this.lastHView) return;
        this.lastHView=ah;
        var hperc=(ah*100/this.getHeight());
        WT.JsonAjaxRequest({
            url: "ServiceRequest",
            params: {
                service: 'mail',
                action: 'SavePaneSize',
                hperc: hperc
            },
            method: "POST",
            callback: function(o,options) {

            },
            scope: this
        });
        
    },
    
	*/
   
    /*rowDblClicked: function(grid, r, tr, rowIndex, e, eopts) {
		var me=this;
        if (this.mys.isDrafts(this.currentFolder)) me.folderList.editMessage(r);
        else me.folderList.openMessage(r);
    },
    
    cellClicked: function(grid, td, cellIndex, r, rowIndex, e, eopts) {
		var me=this;
        if (grid.getColumnManager().getHeaderAtIndex(cellIndex).dataIndex==="note") {
            if (r.get("note")) {
                me.folderList.editNote(r.get("idmessage"),me.currentFolder);
            }
        }
    },*/    
	
    res: function(name) {
		return this.mys.res(name);
	}
   
});
