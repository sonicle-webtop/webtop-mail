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
		'Sonicle.resizer.BorderSplitter',
		'Sonicle.webtop.core.ux.field.SuggestCombo',
		'Sonicle.webtop.mail.MessageView',
		'Sonicle.webtop.mail.MessageGrid'
	],
	uses: [
		'Sonicle.button.PlainToggle'
	],
    layout: 'border',
    border: false,
	
	config: {
		viewMode: 'auto',
		previewLocation: 'right'
	},
    
	keepFilterButton: null,
	searchComponent: null,
    //groupCombo: null,
	labelMessages: null,
	progressQuota: null,
    folderList: null,
    messageView: null,
    messageViewContainer: null,
    folderPanel: null,
    gridMenu: null,
    saveColumnSizes: false,
	saveColumnVisibility: false,
	
    initComponent: function() {
		var me = this, state;
		me.callParent(arguments);
		
		// State is usually restored after this initComponent code.
		// Here we want previewLocation is being available soon... so do it manually!!!
		if (me.stateful && me.getStateId()) {
			state = Ext.state.Manager.get(me.getStateId());
			if (state && state.previewLocation) me.previewLocation = state.previewLocation;
		}
		
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
			html: '0'
		});
		
		me.progressQuota=Ext.create({
			xtype: 'progressbar',
			value: 0,
			width: 200,
			text: "",
			hidden: true
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
				labelAlign: 'left',
				label: me.res('fld-search.field.to.lbl')
			}, {
				name: 'cc',
				type: 'string',
				labelAlign: 'left',
				label: me.res('fld-search.field.cc.lbl')
			}, {
				name: 'bcc',
				type: 'string',
				labelAlign: 'left',
				label: me.res('fld-search.field.bcc.lbl')
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
				boolKeyword: 'has',
				label: me.res('fld-search.field.attachment.lbl')
			}, {
				name: 'unread',
				type: 'boolean',
				boolKeyword: 'has',
				label: me.res('fld-search.field.unread.lbl')
			}, {
				name: 'flagged',
				type: 'boolean',
				boolKeyword: 'has',
				label: me.res('fld-search.field.flagged.lbl')
			}, /*{
				name: 'tagged',
				type: 'boolean',
				boolKeyword: 'has',
				label: me.res('fld-search.field.tagged.lbl')
			},*/ {
				name: 'unanswered',
				type: 'boolean',
				boolKeyword: 'has',
				label: me.res('fld-search.field.unanswered.lbl')
			}, {
				name: 'priority',
				type: 'boolean',
				boolKeyword: 'has',
				label: me.res('fld-search.field.priority.lbl')
			}, {
				name: 'notes',
				type: 'string',
				labelAlign: 'left',
				label: me.res('fld-search.field.notes.lbl'),
				customConfig: {
					emptyText: me.res('fld-search.field.notes.empty')
				}
			}, {
				name: 'tag',
				type: 'tag',
				label: me.res('fld-search.field.tags.lbl'),
				customConfig: {
					store: WT.getTagsStore(), // This is filterable, let's do a separate copy!
					valueField: 'id',
					displayField: 'name',
					colorField: 'color',
					sourceField: 'source',
					sourceCls: 'wt-source'
				}
			}],
			tooltip:  me.res('fld-search.tip'),
			emptyText:  me.res('fld-search.emp'),
			highlightKeywords: ['from', 'to', 'subject', 'message'],
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
		
        me.folderList=Ext.create('Sonicle.webtop.mail.MessageGrid',{
            region:'center',
			pageSize: 50,//me.pageSize,
			mys: me.mys,
			mp: me,
			currentAccount: me.currentAccount,
			previewLocation: me.getPreviewLocation(),
			compactView: me.computeShowCompactView(me.getViewMode(), me.getPreviewLocation()),
			breadcrumb: me.bcFolders,
			createPagingToolbar: true,
			stateful: WT.plTags.desktop ? true : false,
			stateId: me.mys.buildStateId('messagegrid')
			//baseStateId: me.mys.buildStateId('messagegrid')
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
			
			me.searchComponent.highlight(me.folderList.getEl(), '.x-grid-item-container');
		});
		
		me.folderList.on('totals',function(g,total,realTotal,quotaLimit,quotaUsage) {
			var text=''+total;
			if (me.folderList.threaded && realTotal)
				text+=" ["+realTotal+"]";
			if (quotaLimit) {
				var val=quotaUsage/quotaLimit;
				me.progressQuota.setHidden(false);
				me.progressQuota.updateText(
						WTU.humanReadableSize(quotaUsage*1024, { decimals: 0 })+" / "+WTU.humanReadableSize(quotaLimit*1024, { decimals: 0 })+" - "+
						Math.round(100*val)+"%"
				);
				me.progressQuota.setValue(val);
				if (val<0.9) {
					me.progressQuota.removeCls("wtmail-quota-progress-warn");
					me.progressQuota.removeCls("wtmail-quota-progress-over");
				}
				else if (val<1.0) {
					me.progressQuota.addCls("wtmail-quota-progress-warn");
					me.progressQuota.removeCls("wtmail-quota-progress-over");
				} else {
					me.progressQuota.removeCls("wtmail-quota-progress-warn");
					me.progressQuota.addCls("wtmail-quota-progress-over");
					WT.toast({
						 html: me.res('quota.over.msg'),
						 title: me.res('quota.over.title'),
						 width: 300,
						 align: 'br',
						 timeout: 10000
					});
				}
				text+=" - "+WT.res("word.quota")+": ";
			}
			me.labelMessages.setHtml(text);
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
		me.folderList.store.on("metachange",function(s,meta) {
            var gg=meta.groupField;
			if (gg==='') gg='none';
            //me.groupCombo.setValue(gg);
            //me.bThreaded.toggle(meta.threaded);
        });

        //if (me.saveColumnSizes) me.folderList.on('columnresize',me.columnResized,me);
		//if (me.saveColumnVisibility) {
		//	me.folderList.on('columnhide', function(ct,col) { me.columnHiddenChange(ct,col,true); });
		//	me.folderList.on('columnshow', function(ct,col) { me.columnHiddenChange(ct,col,false); });
		//}
		//if (me.saveColumnOrder) me.folderList.on('columnmove', me.columnMoved, me);
        
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
				me.labelMessages,
/*				{
					xtype: 'gauge',
					padding: 0,
					value: 45,
					minValue: 0,
					maxValue: 100,
					trackStart: 90,
					trackLength: 360,
					style: 'color: transparent;'
				},*/
				me.progressQuota
			]
		});
		
		var previewCfg = me.computePreviewSettings(me.viewMode, me.previewLocation),
				viewctStateful = WT.plTags.desktop,
				viewctStateId = me.mys.buildStateId('ctmessageview'),
				viewctState, saveState;
		
		// Reset some state properties in order to avoid conflicts when 
		// transitioning view: right <-> bottom <-> off
		if (viewctStateful) {
			viewctState = Ext.state.Manager.get(viewctStateId);
			if (viewctState) {
				saveState = false;
				if (viewctState.collapsed && (me.previewLocation !== 'off')) {
					delete viewctState.collapsed;
					saveState = true;
				}
				if (me.previewLocation === 'off' || (viewctState.region && (viewctState.region !== previewCfg.region))) {
					delete viewctState.region;
					delete viewctState.width;
					delete viewctState.height;
					saveState = true;	
				}
				if (saveState) Ext.state.Manager.set(viewctStateId, viewctState);
			}	
		}
		
		me.messageView=Ext.create('Sonicle.webtop.mail.MessageView',{
			mys: me.mys,
			mp: me,
			lockSplitter: previewCfg.lockSplitter
        });
        me.messageView.on('messageviewed',me.messageViewed,me);

		var fbar=me.mys.hasAudit()?
			Ext.create('Ext.toolbar.Toolbar',{
				hidden: true,
//				border: "1 0 0 0",
//				style: "border-top-width: 1 !important",
				items: [
					me.getAct("auditRead"),
					me.getAct("auditReplied"),
					me.getAct("auditForwarded"),
					me.getAct("auditPrinted"),
					me.getAct("auditTagged")
				]
			})
			:null;
			
        me.messageViewContainer=Ext.create({
			xtype: 'wtpanel',
			layout: 'fit',
			region: previewCfg.region,
			stateful: viewctStateful,
			stateId: viewctStateId,
			cls: 'wtmail-mv-container',
			header: false,
			split: {
				xtype: 'sobordersplitter'
			},
            collapseMode: 'mini',
			collapsible: false,
			collapsed: previewCfg.collapsed,
			width: previewCfg.width,
			height: previewCfg.height,
            bodyBorder: false,
            border: false,
			tbar: Ext.create('Ext.toolbar.Toolbar',{
				hidden: true,
				enableOverflow: true,
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
			fbar: fbar,
            items: [ me.messageView ]
        });
        me.add(me.folderList);
        me.add(me.messageViewContainer);
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
                
        me.on('resize',function(el, width, height, oldWidth, oldHeight, eOpts) {
            var min=200,
                fw=me.folderList.getWidth();
                
            //when folder list size too small, set it to half new width
            //this may happen while shrinking width
            if (fw<min) {
                me.messageViewContainer.setWidth(width/2);
            }
            //else, when message view size too small, keep it at min
            //this may happen while growing back width and view was set small
            else if ((width-fw)<min) {
                me.messageViewContainer.setWidth(width-min);
            }
        });
    },
	
	//setImapStore: function(store) {
	//	this.bcFolders.setStore(store);
	//},
	
	getState: function() {
		var me = this,
				state = me.callParent();
		state = me.addPropertyToState(state, 'previewLocation');
		return state;
	},
	
	getAct: function(name) {
		return this.mys.getAct(name);
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
	
	updatePreviewLocation: function(nv, ov) {
		var me = this,
				previewCfg = me.computePreviewSettings(me.getViewMode(), nv),
				previewCt = me.messageViewContainer,
				mview = me.messageView,
				accId = mview ? mview.acct : null, // dump data here before clear!
				folId = mview ? mview.folder : null, // dump data here before clear!
				msgId = mview ? mview.idmessage : null, // dump data here before clear!
				msgMo = mview ? mview.model : null; // dump data here before clear!
		
		// Skip persisting state on config initialization and when value is not changed
		if (ov !== undefined && nv !== ov) me.saveState();
		
		if (me.folderList) me.folderList.setCompactView(me.computeShowCompactView(me.getViewMode(), nv));
		if (me.rendered && nv !== ov) {
			Ext.suspendLayouts();
			me.clearMessageView();
			previewCt.lockSplitter = previewCfg.lockSplitter;
			previewCt.setRegion(previewCfg.region);
			previewCt.setWidth(previewCfg.width);
			previewCt.setHeight(previewCfg.height);
			previewCt.setCollapsed(previewCfg.collapsed);
			Ext.resumeLayouts(true);
			previewCt.splitter.refresh();
			if (mview && nv !== 'off') me.showMessage(accId, folId, msgId, msgMo);
		}
	},
    
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
	
	getSelectedMails: function() {
		return this.folderList.getSelection();
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
		me.folderList.reloadFolder(acct,node.id,config,uid,rid,page,tid,node.get('isReadOnly'));
		me.reloadingFolder=false;
	},

	 reloadCurrentFolder: function(config) {
		var me=this,
			node=me.mys.getFolderNodeById(me.currentAccount,me.currentFolder);
		me.folderList.reloadFolder(
				me.currentAccount,me.currentFolder,config,
				null, null, null, null, 
				node.get('isReadOnly')
		);
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
    },
    
	showMessage: function(acct,folder,id,rec) {
		var me=this;
		if (!me.messageView) return;
		if (acct && folder && id) {
			if (WT.plTags.desktop) {
				me.messageViewContainer.getTopBar().show();
				if (me.mys.hasAudit()) me.messageViewContainer.getBottomBar().show();
			}
			me.messageView._showMessage(acct, folder, id, !me.mys.getVar("manualSeen"), rec);
		} else {
			me.messageView._clear();
		}
	},

    clearMessageView: function() {
		var me=this;
		if (!me.messageView) return;
		if (WT.plTags.desktop) {
			me.messageViewContainer.getTopBar().hide();
			if (me.mys.hasAudit()) me.messageViewContainer.getBottomBar().hide();
		}
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
		else if (e.shiftKey) me.folderList.actionDeletePermanently();
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
	},
	
	computeShowCompactView: function(viewMode, previewLocation) {
		if (viewMode === 'compact') {
			return true;
		} else if (viewMode === 'columns') {
			return WT.plTags.desktop ? false : true;
		} else { // viewMode=auto	
			if (previewLocation === 'off' || previewLocation === 'bottom') {
				// Multi columns are preferred (except for mobile)
				return WT.plTags.desktop ? false : true;
			} else {
				// Single column is preferred
				return true;
			}
		}
	},
	
	computePreviewSettings: function(viewMode, previewLocation) {
		if (previewLocation === 'off') {
			return {
				region: 'east',
				collapsed: true,
				lockSplitter: true,
				width: undefined,
				height: undefined
			};
		} else if (previewLocation === 'bottom') {
			return {
				region: 'south',
				collapsed: WT.plTags.desktop ? false : true,
				lockSplitter: false,
				width: undefined,
				height: '60%'
			};
		} else {
			return {
				region: 'east',
				collapsed: WT.plTags.desktop ? false : true,
				lockSplitter: false,
				width: '60%',
				height: undefined
			};
		}
	}
});
