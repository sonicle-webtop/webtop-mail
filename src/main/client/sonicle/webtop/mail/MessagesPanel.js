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
		'Sonicle.webtop.mail.MessageView',
		'Sonicle.webtop.mail.MessageGrid',
		'Sonicle.webtop.mail.store.QuickFilter'
	],
    layout:'border',
    border: false,
	
	viewRegion: 'east',
	viewWidth: 600,
	viewHeight: 400,
    
    filterTextField: null,
    filterCombo: null,
    groupCombo: null,
    bMultiSearch: null,
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

		//TODO: addEvents
		/*
        this.addEvents(
            'gridselectionchange',
            'gridselectiondelete',
            'gridrowdblclick',
            'gridcellclick'
        );*/
            
        me.folderList=Ext.create('Sonicle.webtop.mail.MessageGrid',{
            region:'center',
			pageSize: 50,//me.pageSize,
			mys: me.mys,
			mp: me,
			createPagingToolbar: true
        });
		if (me.gridMenu) {
			me.folderList.on("itemcontextmenu",function(s, rec, itm, i, e) {
				WT.showContextMenu(e, me.gridMenu, { rec: rec, row: i, grid: me.folderList });
			});
		}
		
        var msgSelModel=me.folderList.getSelectionModel();
        msgSelModel.on('selectionchange',me.selectionChanged,me);
		me.folderList.on('keydelete',me.actionDelete,me);
		me.folderList.on('deleting',me.clearMessageView,me);
		me.folderList.on('moving',me.clearMessageView,me);
		//TODO: grid events        
        //me.folderList.on('rowdblclick',me.rowDblClicked,me);
        //me.folderList.on('cellclick',me.cellClicked,me);

        if (me.saveColumnSizes) me.folderList.on('columnresize',me.columnResized,me);
		if (me.saveColumnVisibility) {
			me.folderList.on('columnhide', function(ct,col) { me.columnHiddenChange(ct,col,true); });
			me.folderList.on('columnshow', function(ct,col) { me.columnHiddenChange(ct,col,false); });
		}
		if (me.saveColumnOrder) me.folderList.on('columnmove', me.columnMoved, me);

        me.messageView=Ext.create('Sonicle.webtop.mail.MessageView',{
			mys: me.mys,
			mp: me
        });
        me.messageView.on('messageviewed',me.messageViewed,me);

        me.quickFilterCombo=Ext.create(WTF.localCombo('id', 'desc', {
            width: 80,
			matchFieldWidth: false,
			listConfig: { width: 120 },
			store: Ext.create('Sonicle.webtop.mail.store.QuickFilter', {
				autoLoad: true
			}),
            value: 'any',
			tooltip: me.res('quickfilter.tip'),
			plugins: [ 'sofieldtooltip' ],
			listeners: {
				select: function(cb,r,eopts) {
					me.quickFilterChanged(r.get("id"));
				}
			}
        }));
        me.filterCombo=Ext.create(WTF.localCombo('id', 'desc', {
            width: 80,
			matchFieldWidth: false,
			listConfig: { width: 120 },
			store: Ext.create('Sonicle.webtop.mail.store.Filter', {
				autoLoad: true
			}),
            value: 'subject',
			tooltip: me.res('filter.tip'),
			plugins: [ 'sofieldtooltip' ],
			listeners: {
				change: function(cb,nv,ov) {
					me.filterTextField.setSuggestionContext("filter"+nv);
				}
			}
        }));
        me.filterTextField=Ext.create({
			xtype: 'wtsuggestcombo',
			sid: me.mys.ID,
			suggestionContext: 'filtersubject',
            width: 150,
			tooltip: me.res('filtertext.tip'),
			listeners: {
				enterkey: function(tf,e) {
					me.filterAction(tf,e);
				}
			}
			
        });
		//TODO: grouping
		/*
        this.groupCombo=new Ext.form.ComboBox({
            forceSelection: true,
            mode: 'local',
            displayField: 'desc',
            triggerAction: 'all',
            selectOnFocus: true,
            width: 60,
            editable: false,
            store: new Ext.data.SimpleStore({
                fields: ['id','desc'],
                data: [
                    ['none',this.res('group-none')],
                    ['gdate',this.res('column-date')],
                    ['from',this.res('sender')],
                    ['to',this.res('to')]
                ]
            }),
            value: '',
            valueField: 'id'
        });
        this.groupCombo.on('select',function(cb,r,ix) {
            this.groupChanged(r.get("id"));
        },this);
        this.folderList.store.on("metachange",function(s,meta) {
            var gg=meta.groupField;
            this.groupCombo.setValue(gg);
        },this);
        
		 */
        me.toolbar=Ext.create('Ext.toolbar.Toolbar',{ 
			items:[
				me.bMultiSearch=me.mys._TB("multisearch"),
				"-",
				me.quickFilterCombo,
                "-",
				me.filterCombo,
                me.filterTextField,
				me.mys._TB("advsearch")/*,
                "-",
                this.res("groupby")+":",
                this.groupCombo*/
            ]
		});

        me.messageViewContainer=Ext.create('WT.ux.Panel',{
            region: me.viewRegion,
            cls: 'wtmail-mv-container',
            layout: 'fit',
            split: true,
            collapseMode: 'mini',
			header: false,
            collapsible : true,
			collapsed: me.viewCollapsed,
            height: me.viewHeight,
			width: me.viewWidth,
            bodyBorder: false,
            border: false,
			tbar: Ext.create('Ext.toolbar.Toolbar',{
				hidden: true,
				items: [
					me.getAction("delete"),
					'-',
					me.getAction("reply"),
					me.getAction("replyall"),
					me.getAction("forward"),
					'-',
					me.getAction("markseen"),
					me.getAction("markunseen")
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
				me.saveMessageView(me.viewRegion==='east'?w:h);
			}
		});
		me.messageViewContainer.on("expand",function() {
			var sel=me.folderList.getSelection();
			if (sel.length==1) {
				var r=sel[0],
					id=r.get('idmessage'),
					fldr=r.get('fromfolder');
				if (!fldr) fldr=me.currentFolder;
				me.showMessage(fldr,id);
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
	
	getAction: function(name) {
		return this.mys.getAction(name);
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
	
	moveViewPanel: function(r,w,h,save,collapsed) {
		var me=this,
			mv=me.messageView,
			mvc=me.messageViewContainer,
			idm=mv.idmessage,
			idf=mv.folder;
	
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
			w=640;
			h=400;
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
		if (save) me.saveMessageView();
		me.showMessage(idf,idm);
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
            this.doLayout();
        }
    },
    
    printMessageView: function() {
        this.messageView.print();
    },
	
    gridContextMenu: function(e,t) {
        var grid=this.folderList;
        WT.MessageGrid.setContextGrid(grid);
        var row = grid.view.findRowIndex(t);
        var sm=grid.getSelectionModel();
        if (row>=0 && !sm.isSelected(row)) sm.selectRow(row, false);

    },
    
    groupChanged: function(nv) {
        WT.JsonAjaxRequest({
            url: "ServiceRequest",
            params: {
                service: 'mail',
                action: 'GroupChanged',
                group: nv,
                folder: this.currentFolder
            },
            method: "POST",
            callback: function(o,options) {
                this.reloadGrid();
            },
            scope: this
        });
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
		var me=this,
			s=me.folderList.store,
			ix=s.findExact('idmessage',idmessage);
        
		if (ix>=0) {
			var r=s.getAt(ix);
			if (r.get("unread")) {
				r.set("unread",false);
				var st=r.get("status");
				if (st==="unread"||st==="new") r.set("status","read");
				//TODO: not needed with websocket?
				//var o=s.getProxy().getReader().rawData;
				//o.millis=millis;
				//o.unread--;
				//me.mys.updateUnreads(me.currentFolder,o,false);
			}
		}
    },
   
    filterAction: function(tf,e) {
        var me=this;
		me.reloadFiltered(me.quickFilterCombo.getValue(),me.filterCombo.getValue(),tf.getValue());
    },
	
    quickFilterChanged: function(nv) {
		var me=this;
		me.reloadFiltered(me.quickFilterCombo.getValue(),me.filterCombo.getValue(),me.filterTextField.getValue());
	},
	    
	reloadFiltered: function(quickfilter,field,pattern) {
		var me=this;
        me.depressMultiSearchButton();
//        me.folderList.store.baseParams={service: 'mail', action: 'ListMessages', folder: this.currentFolder, quickfilter: quickfilter, searchfield: field, pattern: pattern, refresh:1};
//        me.folderList.store.reload({
//          params: {start:0,limit:me.ms.pageRows}
//        });
//        me.folderList.store.baseParams.refresh=0;      
		me.reloadCurrentFolder({
			start:0,
			limit: me.getPageSize(),
			quickfilter: quickfilter,
			searchfield: field,
			pattern: pattern,
			refresh: 1
		});
	},
     
    depressMultiSearchButton: function() {
		var me=this;
        if (me.bMultiSearch && me.bMultiSearch.pressed) {
            me.bMultiSearch.toggle();
            me.folderList.hideMultiSearch();
        }
    },
    
	setPageSize: function(size) {
		this.folderList.setPageSize(size);
	},
	
	getPageSize: function() {
		return this.folderList.getPageSize();
	},
     
	/*
    actionMultiSearch: function() {
        if (this.bMultiSearch.pressed) this.folderList.showMultiSearch();
        else this.folderList.hideMultiSearch();
    },

    
    
    clearGridSelections: function() {
        this.folderList.getSelectionModel().clearSelections(true);
    },*/
    
    reloadFolder: function(folderid,config) {
        this.currentFolder=folderid;
        this.folderList.reloadFolder(folderid,config);
    },

    reloadCurrentFolder: function(config) {
        this.folderList.reloadFolder(this.currentFolder,config);
    },
	
	//TODO no more ctrlshift?
    selectionChanged: function(sm,r,eopts) {
        var me=this,
			c=sm.getCount();
		if (!me.viewCollapsed) {
			//if (c==1&&!ctrlshift) {
			if (c===1) {
				var id=r[0].get('idmessage');
				var fldr=r[0].get('fromfolder');
				if (!fldr) fldr=this.currentFolder;
				if (id!==me.messageView.idmessage)
					me.showMessage(fldr,id);
			} else {
				me.clearMessageView();
			}
		}
        me.fireEvent('gridselectionchanged',sm/*,ctrlshift*/);
    },
    
	showMessage: function(folder,id) {
		if (folder && id) {
			var me=this,
				tbar=me.messageViewContainer.getTopBar();
			tbar.show();
			//tbar.setHeight(24);
			me.messageView._showMessage(folder,id);
		}
	},

    clearMessageView: function() {
		var me=this,
			tbar=me.messageViewContainer.getTopBar();
		tbar.hide();
		//tbar.setHeight(0);
        me.messageView._clear();
    },
	
    reloadGrid: function() {
        this.folderList.store.reload();
    },
    

    actionDelete: function(g,e,sm) {
		var me=this;
        if (e.ctrlKey) me.mys.actionSpam();
		else me.folderList.actionDelete(e);
    },
/*    
    rowDblClicked: function(grid, rowIndex, e) {
        this.fireEvent('gridrowdblclick',grid,rowIndex,e);
    },
    
    cellClicked: function(grid, rowIndex, colIndex, e) {
        this.fireEvent('gridcellclick',grid,rowIndex,colIndex, e);
    },
    */
   
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
			},
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
   
    res: function(name) {
		return this.mys.res(name);
	},
	
    getGridStore: function() {
		return this.folderList.store;
	}
   
});