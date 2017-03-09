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


Ext.define('Sonicle.webtop.mail.MessageListView', {
	extend: 'Ext.view.Table',
    
	isTree: false,
    //enableGroupingMenu: false,
	
	initComponent: function() {
		var me=this;
		me.navigationModel=Ext.create('Ext.view.NavigationModel',{});
		me.callParent(arguments);
	},
    
	getRowClass: function(record, index, rowParams, store ) {
		//TODO : manage threading state
		var unread=record.get('unread');
		console.log("index "+index+" unread="+unread);
		var cls=unread?'wtmail-row-unread':'';
		if (!this.grid.getSelectionModel().isSelected(index)) {
			var tdy=record.get('istoday');
			cls+=tdy?' wtmail-row-today':'';
		}
		
		return cls;
    }
	
	
//TODO: MessageListView various
/*    onLoad : function(){
        var g=this.grid;
        var ix=this.ixAutoSelect;
        var c=g.store.getCount();
        if (c>0 && ix>=0) {
          if (ix>=c) ix=c-1;
          g.getSelectionModel().selectRow(ix);
          this.ixAutoSelect=-1;
        } else {
          if (!g.multifolder) this.scrollToTop();
        }
    },

    insertRows : function(dm, firstRow, lastRow, isUpdate){
        try {
            var last = dm.getCount() - 1;
            if(!isUpdate && firstRow === 0 && lastRow >= last){
                this.refresh();
            }else{
                if(!isUpdate){
                    this.fireEvent("beforerowsinserted", this, firstRow, lastRow);
                }
                var html = this.renderRows(firstRow, lastRow),
                    before = this.getRow(firstRow);
                if(before){
                    if(firstRow === 0){
                        Ext.fly(this.getRow(0)).removeClass(this.firstRowCls);
                    }
                    Ext.DomHelper.insertHtml('beforeBegin', before, html);
                }else{
                    var r = this.getRow(last - 1);
                    if(r){
                        Ext.fly(r).removeClass(this.lastRowCls);
                    }
                    Ext.DomHelper.insertHtml('beforeEnd', this.mainBody.dom, html);
                }
                if(!isUpdate){
                    this.fireEvent("rowsinserted", this, firstRow, lastRow);
                    this.processRows(firstRow);
                }else if(firstRow === 0 || firstRow >= last){
                    //ensure first/last row is kept after an update.
                    Ext.fly(this.getRow(firstRow)).addClass(firstRow === 0 ? this.firstRowCls : this.lastRowCls);
                }
            }
            this.syncFocusEl(firstRow);
        } catch(ex) {
            
        }
    }*/

});

Ext.define('Sonicle.webtop.mail.NavigationModel',{
	extend: 'Ext.grid.NavigationModel',
	
	//remove cell focus style
	focusCls: '',
	
    // Home moves the focus to first row.
    onKeyHome: function(keyEvent) {
        var me = this,
            view = keyEvent.view;

//        // ALT+Home - go to first visible record in grid.
//        if (keyEvent.altKey) {
            if (view.bufferedRenderer) {
                // If rendering is buffered, we cannot just increment the row - the row may not be there
                // We have to ask the BufferedRenderer to navigate to the target.
                // And that may involve asynchronous I/O, so must postprocess in a callback.
                me.lastKeyEvent = keyEvent;
                view.bufferedRenderer.scrollTo(0, false, me.afterBufferedScrollTo, me);
            } else {
                // Walk forwards to the first record
                me.setPosition(view.walkRecs(keyEvent.record, -view.dataSource.indexOf(keyEvent.record)), null, keyEvent);
            }
//        }
//        // Home moves the focus to the First cell in the current row.
//        else {
//            me.setPosition(keyEvent.record, 0, keyEvent);
//        }
    },
	
    // End moves the focus to the last cell in the current row.
    onKeyEnd: function(keyEvent) {
        var me = this,
            view = keyEvent.view;

//        // ALT/End - go to last visible record in grid.
//        if (keyEvent.altKey) {
            if (view.bufferedRenderer) {
                // If rendering is buffered, we cannot just increment the row - the row may not be there
                // We have to ask the BufferedRenderer to navigate to the target.
                // And that may involve asynchronous I/O, so must postprocess in a callback.
                me.lastKeyEvent = keyEvent;
                view.bufferedRenderer.scrollTo(view.store.getCount() - 1, false, me.afterBufferedScrollTo, me);
            } else {
                 // Walk forwards to the end record
                me.setPosition(view.walkRecs(keyEvent.record, view.dataSource.getCount() - 1 - view.dataSource.indexOf(keyEvent.record)), null, keyEvent);
            }
//        }
//        // End moves the focus to the last cell in the current row.
//        else {
//            me.setPosition(keyEvent.record, keyEvent.view.getVisibleColumnManager().getColumns().length - 1, keyEvent);
//        }
    },
	
    onKeyRight: function(keyEvent) {
        var me = this,
            view = keyEvent.view,
			g = view.grid,
			s = g.getSelection();
	
		if (s) {
			var r=s[0],
				tid=r.get("threadId"),
				tindent=r.get("threadIndent"),
				topen=r.get("threadOpen");
			if (tid && tid>0 && tindent===0 && !topen) {
				g.collapseClicked(g.store.indexOf(r));
			}
		}
	},
	
	onKeyLeft: function(keyEvent) {
        var me = this,
            view = keyEvent.view,
			g = view.grid,
			s = g.getSelection();
	
		if (s) {
			var r=s[0],
				tid=r.get("threadId"),
				tindent=r.get("threadIndent"),
				topen=r.get("threadOpen");
			if (tid && tid>0 && tindent===0 && topen) {
				g.collapseClicked(g.store.indexOf(r));
			}
		}
	}
	
});



Ext.define('Sonicle.webtop.mail.MessagesModel',{
	extend: 'Ext.data.Model',
	idProperty: 'idmessage',
	fields: [
        { name: 'idmessage' },
        { name: 'priority', type: 'int' },
        { name: 'status' },
        { name: 'from' },
        { name: 'fromemail' },
        { name: 'to' },
        { name: 'toemail' },
        { name: 'subject' },
        { name: "threadId", type: 'int' },
        { name: "threadIndent", type: 'int' },
        { name: 'date', type:'date' },
		{ name: 'fmtd', type:'boolean'},
		{ name: 'fromfolder' },
        { name: 'gdate' },
        { name: 'sdate' },
        { name: 'xdate' },
        { name: 'unread', type:'boolean' },
        { name: 'size', type:'int' },
        { name: 'flag' },
        { name: 'note' },
        { name: 'istoday', type:'boolean' },
        { name: 'arch', type:'boolean' },
        { name: 'atts', type:'boolean' },
		{ name: 'threadOpen', type:'boolean' },
        { name: 'scheddate', type:'date' },
		{ name: 'autoedit', type:'boolean' }
	]
});

Ext.define('Sonicle.webtop.mail.MultiFolderMessagesModel',{
	extend: 'Ext.data.Model',
	idProperty: 'idmandfolder',
	fields: [
		{ name: 'folder' },
		{ name: 'folderdesc' },
		{ name: 'idmandfolder' },
        { name: 'idmessage' },
        { name: 'priority', type: 'int' },
        { name: 'status' },
        { name: 'from' },
        { name: 'fromemail' },
        { name: 'to' },
        { name: 'toemail' },
        { name: 'subject' },
        { name: "threadId", type: 'int' },
        { name: "threadIndent", type: 'int' },
        { name: 'date', type:'date' },
		{ name: 'fmtd', type:'boolean'},
		{ name: 'fromfolder' },
        { name: 'gdate' },
        { name: 'sdate' },
        { name: 'xdate' },
        { name: 'unread', type:'boolean' },
        { name: 'size', type:'int' },
        { name: 'flag' },
        { name: 'note' },
        { name: 'istoday', type:'boolean' },
        { name: 'arch', type:'boolean' },
        { name: 'atts', type:'boolean' },
		{ name: 'threadOpen', type:'boolean' },
        { name: 'scheddate', type:'date' },
		{ name: 'autoedit', type:'boolean' }
	]
});

Ext.define('Sonicle.webtop.mail.GridFeatureGrouping', {
	extend: 'Ext.grid.feature.Grouping',
	alias: 'feature.mailgrouping',
	/*init: function() {
		var me=this;
		me.callParent(arguments);
		me.collapsible = true;
	},*/
	//groupHeaderTpl: '{columnName}: name',
/*	groupHeaderTpl: Ext.create('Ext.XTemplate',
		'{[this.formatColumnName(values)]}: {[this.formatName(parent,values)]}',
		{
			formatColumnName: function(v) {
				if (v.groupField==='threadId') return WT.res('word.thread');
				return v.columnName;
			},
			formatName: function(p,v) {
				if (v.groupField==='threadId') {
					var rec=p.view.store.findRecord("idmessage",v.name);
					if (rec) return rec.get("subject");
				}
				return v.name;
			}
		}
	),*/
	onColumnMove: function() {
		console.log('onColumnMove overridden!');
	}
});

Ext.define('Sonicle.webtop.mail.MessageGrid',{
	extend: 'Ext.grid.Panel',
	requires: [
		'Sonicle.data.BufferedStore',
		'Sonicle.selection.RowModel',
		'Sonicle.webtop.mail.plugin.MessageGridViewDragDrop',
		'Sonicle.plugin.FilterBar',
		'Sonicle.form.field.IconComboBox',
		'Sonicle.grid.column.Bytes'
	],
	
	pageSize: 50,	
    frame: false,

	enableColumnMove: true,
	plugins: [
		{
			ptype: 'sofilterbar',
			pluginId: 'gridfilterbar',
			hidden: true
		}
	],
		
	features: [
		{
			ftype:'mailgrouping'
		}
	],
	selModel: { 
		type: 'sorowmodel',
		mode: 'MULTI'
	},
	multiColumnSort: false,
	
    clickTimeoutId: 0,
    clickEvent: null,
	//TODO: DocMgt
    //arch: WT.docmgt || WT.docmgtwt,
    multifolder: false,
    reloadAction: 'ListMessages',
    firstShow: true,
	key2flag: ['clear','red','orange','green','blue','purple','yellow','black','gray','white'],
	createPagingToolbar: false,
	threaded: false,
	openThreads: {},
	
    /**
     * @event keydelete
     * Fired after a DELETE key is pressed on the grid
     * @param {Sonicle.webtop.mail.MessageGrid} this
	 * @param {Ext.event.Event} ev The original event
     * @param {Ext.data.Model[]} selected The selected records
     */	
	
    /**
     * @event deleting
     * Fired when deleting messages
     * @param {Sonicle.webtop.mail.MessageGrid} this
     */	
	
    /**
     * @event moving
     * Fired when moving messages
     * @param {Sonicle.webtop.mail.MessageGrid} this
     */	
	
	listeners: {
		render: function(g,opts) {
			var me=this,
				map = new Ext.util.KeyMap({
					target: me.getEl(),
					binding: [
						{
							key: "0123456789",
							shift: false,
							fn: function(key,ev) {
								me.actionFlag(me.key2flag[key-48]);
							}
						},
						{
							key: Ext.event.Event.INSERT,
							shift: false,
							fn: function(key,ev) {
								me.actionFlag("complete");
							}
						},
						{
							key: Ext.event.Event.DELETE,
							shift: false,
							fn: function(key,ev) {
								me.fireEvent('keydelete', me, ev, me.getSelection());
							}
						}
					]
				});		
		}
	},
	
    initComponent: function() {
        var me=this;
		
		me.viewConfig={
			//preserveScrollOnRefresh: true,
			preserveScrollOnReload: true,
			markDirty: false,
			navigationModel: Ext.create('Sonicle.webtop.mail.NavigationModel',{}),
			loadMask: { msg: WT.res("loading") },
			getRowClass: function(record, index, rowParams, store ) {
				var unread=record.get('unread');
				var tdy=record.get('istoday');
				//var ti=record.get('threadIndent');
				cls1=unread?'wtmail-row-unread':'';
				cls2=tdy?'wtmail-row-today':'';
				//cls3=ti>0?'wtmail-row-hidden':'';
				return cls1+' '+cls2/*+' '+cls3*/;
			},
			
			plugins: [
				{
					ptype: 'messagegridviewdragdrop'
				}
			]

		};

		var smodel='Sonicle.webtop.mail.MessagesModel';
        if (this.multifolder) {
			smodel='Sonicle.webtop.mail.MultiFolderMessagesModel';
        }

		if (me.useNormalStore) {
			me.store = Ext.create('Ext.data.Store',{
				proxy: {
					type: 'ajax',
					url: WTF.requestBaseUrl(),
					extraParams: {
						action: me.reloadAction,
						service: me.mys.ID
					},
					reader: {
						keepRawData: true,
						rootProperty: 'messages',
						totalProperty: 'total',
						idProperty: 'idmessage'
					}
				},
				model: smodel,
				pageSize: me.pageSize
			});
		} else {
			me.store = Ext.create('Sonicle.data.BufferedStore',{
				proxy: {
					type: 'ajax',
					url: WTF.requestBaseUrl(),
					extraParams: {
						action: me.reloadAction,
						service: me.mys.ID
					},
					reader: {
						keepRawData: true,
						rootProperty: 'messages',
						totalProperty: 'total',
						idProperty: 'idmessage'
					}
				},
				//purgePageCount: 3,
				leadingBufferZone: 50,
				trailingBufferZone: 50,
				model: smodel,
				pageSize: me.pageSize
			});

		}
		
/*		if (me.createPagingToolbar) {
			var tb=me.ptoolbar=Ext.create('Ext.toolbar.Paging',{
				store: me.store,
				displayInfo: true,
				displayMsg: me.res("pagemessage"),
				emptyMsg: me.res("nomessages"),
				afterPageText: me.res("afterpagetext"),
				beforePageText: me.res("beforepagetext"),
				firstText: me.res("firsttext"),
				lastText: me.res("lasttext"),
				nextText: me.res("nexttext"),
				prevText: me.res("prevtext"),
				refreshText: me.res("refreshtext")
			});
			tb.remove(tb.getComponent('displayItem'));
			tb.add(Ext.create('Ext.button.Button',{
				itemId: "displayItem",
				tooltip: me.res("changepagesize"),
				handler: me.changePageSize,
				scope: me
			}));
			me.tbar=me.ptoolbar;
		}
*/		
		if (!me.stateful) {
			me.columns=me.createColumnsFromState(
					me.createDefaultState()
			);
		}
		
        me.store.on('beforeload',function() {
			me.storeLoading=true;
		});
		
        me.store.on('load',function(s,r,o) {
			me.storeLoading=false;
			me.loaded(s,r,o);
		});
		
		
        me.on('rowdblclick',function(grid, r, tr, rowIndex, e, eopts) {
			var folder=me.multifolder?r.get("folder"):me.currentFolder;
			if (me.mys.isDrafts(folder)) me.editMessage(r);
			else me.openMessage(r);
		});
		me.on('cellclick',function(grid, td, cellIndex, r, rowIndex, e, eopts) {
			if (me.getColumnManager().getHeaderAtIndex(cellIndex).dataIndex==="note") {
				if (r.get("note")) {
					var folder=me.multifolder?r.get("folder"):me.currentFolder;
					me.editNote(r.get("idmessage"),folder);
				}
			}
		});
		
        me.callParent(arguments);
    },
	
	collapseClicked: function(rowIndex) {
		var me=this,
			sel=me.getSelection(),
			s=me.store,
		    r=s.getAt(rowIndex);
	
		if (sel) {
			me.selectOnLoad=me.store.indexOf(sel[0]);
			me.view.on('refresh',function() {
				if (me.selectOnLoad) {
					me.getSelectionModel().select(me.selectOnLoad);
					delete me.selectOnLoad;
				}
			},me,{ single: true });
		}
		me.store.reload({ 
			params: {
				threadaction: r.get("threadOpen")?'close':'open',
				threadactionuid: r.get("idmessage"),
				timestamp: Date.now()
			}
		});
	},

	setPageSize: function(size) {
/*		var me=this;
		me.pageSize=size;
		if (me.store) me.store.setPageSize(size);*/
	},
	
	getPageSize: function() {
		return this.pageSize;
	},
	
	reloadFolder: function(folder_id, config){
		var me = this,
			proxy = me.store.getProxy();
	
		config=config||{};
		if(!folder_id) return;
		//me.setPageSize(this.ms.pageRows);
		//console.log("reloadFolder "+folder_id+" pageSize="+this.ptoolbar.pageSize);
		Ext.applyIf(config, {
			start: 0,
			limit: me.pageSize,
			refresh: 1,
			folder: folder_id,
			timestamp: Date.now()
		});

		//TODO: sort info && threaded
		//if (!Ext.isDefined(config.threaded)) config.threaded=2;
        //this.store.sortInfo=null;
		//TODO: baseParams??
		//this.store.baseParams = {service: 'mail', action: 'ListMessages', folder: folder_id};
		proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), config));


		var groupField=me.mys.getFolderGroup(folder_id),
			s=me.store,
			meta=s.getProxy().getReader().metaData;
		if (groupField && groupField!=='none' && groupField!=='threadId' && groupField!=='') {
			var dir=meta?meta.sortInfo.direction:'ASC';
			//s.blockLoad(); //TODO: We are using ExtJs 6.2, is this still necessary?
			s.group(null, null);
			s.group(groupField, dir);
			me.threaded=groupField==='threadId';
			//s.unblockLoad(false); //TODO: We are using ExtJs 6.2, is this still necessary?
		} else {
			//s.blockLoad(); //TODO: We are using ExtJs 6.2, is this still necessary?
			s.group(null, null);
			me.threaded=groupField==='threadId';
			//s.unblockLoad(false); //TODO: We are using ExtJs 6.2, is this still necessary?
		}
		me.currentFolder = folder_id;
		me.initFolderState();
		me.hideFilterBar();
		me.getStore().clearFilter(true);
		s.load();
	},
	
    /**
     * Initializes the state of the object upon construction.
     * @private
     */
    initFolderState: function(reset){
        var me = this,
            id = me.stateful && me.getStateId(),
            hasListeners = me.hasListeners,
            state,
            combinedState,
            i, len,
            plugins,
            plugin,
            pluginType;
 
        if (id) {
            if (reset) Ext.state.Manager.clear(id);
			else combinedState = Ext.state.Manager.get(id);
			
            if (combinedState) {
                state = Ext.apply({}, combinedState);
            } else {
				state=me.createDefaultState();
			}
			
			var node=me.mys.getFolderNodeById(me.currentFolder),
				issentfolder=node?(node.data.isSent||node.data.isUnderSent):false;

			if (state.storeState) {
				delete state.storeState.sorters;
				delete state.storeState.filters;
				delete state.storeState.grouper;
			}
			Ext.each(state.columns,function(col) {
				switch(col.id) {
					case 'stid-from':
						me._updateColHiddenState(col,issentfolder);
						break;
					case 'stid-to':
						me._updateColHiddenState(col,!issentfolder);
						break;
				}
			});
			
			var newcols=me.createColumnsFromState(state);
			
			if (!hasListeners.beforestaterestore || me.fireEvent('beforestaterestore', me, state) !== false) {

				//Notify all plugins FIRST (if interested) in new state 
				plugins = me.getPlugins() || [];
				for (i = 0, len = plugins.length; i < len; i++) {
					plugin = plugins[i];
					if (plugin) {
						pluginType = plugin.ptype;
						if (plugin.applyState) {
							plugin.applyState(state[pluginType], state);
						}
						delete state[pluginType];  //clean to prevent unwanted props on the component in final phase 
						
						if (plugin.configureColumns) {
							plugin.configureColumns(newcols);
						}
					}
				}

			}
			
			me.reconfigure(newcols);
			
			me.applyState(state);
			if (hasListeners.staterestore) {
				me.fireEvent('staterestore', me, state);
			}
			
        }
    },
	
	//add hidden only if not already set and new state true
	//if already set and false, delete it
	_updateColHiddenState: function(col,hidden) {
		if (!Ext.isDefined(col.hidden) && hidden) col.hidden=hidden;
		else if (!col.hidden) delete col.hidden;
	},
 	
	updateTotals: function(total,realTotal) {
		var me=this;
		me.msgsTotal=total;
		me.msgsRealTotal=realTotal;
		me.fireEvent('totals',me,me.msgsTotal,me.msgsRealTotal);
	},
	
    loaded: function(s,r,o) {
		var me=this,
			//meta=s.getProxy().getReader().metaData,
			json=s.proxy.reader.rawData;

		me.updateTotals(json.total,json.realTotal);
		
		me.fireEvent('load',me,me.currentFolder,json);
		
		//TODO: autoedit
		/*
        var ae=meta.autoedit;
		if (ae) {
            for(var i=0; i<ae.length; i++) {
				var r=s.getById(ae[i]);
				this.ms.editMessage(r,true);
			}
		}*/
		
        /*
        if (!this.multifolder) {
            var b=s.reader.jsonData.issent;
            var cm=this.getColumnModel();
            cm.setHidden(2,b);
            cm.setHidden(3,!b);
        }
		*/
    },
	
	actionPrint: function() {
		
	},
	
    actionOpen: function(rowIndex) {
        var me=this,
			recs=(rowIndex>=0)?
				me.getStore().getAt(rowIndex):
				me.getSelection();
        me.openMessages(recs);
    },
	
	openMessages: function(recs) {
		Ext.each(recs,this.openMessage,this);
	},

    openMessage: function(r) {
		var me=this;
		
		var win=WT.createView(me.mys.ID,'view.DockableMessageView',{
			viewCfg: {
				mys: me.mys,
				folder: r.get('folder')||me.currentFolder,
				idmessage: r.get('idmessage'),
				title: r.get('subject'),
				model: r,
				messageGrid: me
			}
		});
		win.show(false,function() {
			win.getView().showMessage();
		});
			
    },

    actionReply: function(rowIndex) {
        this._actionReply(rowIndex,false);
    },
	
    actionReplyAll: function(rowIndex) {
        this._actionReply(rowIndex,true);
    },
	
    _actionReply: function(rowIndex,all) {
        var me=this,
			recs=(rowIndex>=0)?
				me.getStore().getAt(rowIndex):
				me.getSelection();
        me.replyMessage(recs[0],all);
    },
	
	replyMessage: function(r,all) {
        this.replyMessageById(r.get('folder')||this.currentFolder,r.get("idmessage"),all);        
	},
	
	replyMessageById: function(idfolder,idmessage,all) {
        var me=this;
        
		WT.ajaxReq(me.mys.ID, 'GetReplyMessage', {
			params: {
				folder: idfolder,
				idmessage: idmessage,
				replyall: (all?'1':'0')
			},
			callback: function(success,json) {
				if (json.result) {
					me.mys.startNewMessage(idfolder,{
						subject: json.subject,
						recipients: json.recipients,
						content: json.content,
                        format: json.format,
						replyfolder: json.replyfolder,
						inreplyto: json.inreplyto,
						references: json.references,
						origuid: json.origuid
					});
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
    actionForward: function(rowIndex) {
        this._actionForward(rowIndex,false);
    },
	
    actionForwardEml: function(rowIndex) {
        this._actionForward(rowIndex,true);
    },
	
    _actionForward: function(rowIndex,eml) {
        var me=this,
			recs=(rowIndex>=0)?
				me.getStore().getAt(rowIndex):
				me.getSelection();
        me.forwardMessage(recs[0],eml);
    },
	
	forwardMessage: function(r,eml) {
        this.forwardMessageById(r.get('folder')||this.currentFolder,r.get("idmessage"),eml);
	},
	
	forwardMessageById: function(idfolder,idmessage,eml) {
		var me=this,msgId=Sonicle.webtop.mail.view.MessageEditor.buildMsgId();
		
		WT.ajaxReq(me.mys.ID, 'GetForwardMessage', {
			params: {
				folder: idfolder,
				idmessage: idmessage,
				attached: eml?1:0,
				newmsgid: msgId
			},
			callback: function(success,json) {
				if (json.result) {
					me.mys.startNewMessage(idfolder,{
						subject: json.subject,
						content: json.content,
                        format: json.format,
						msgId: msgId,
						attachments: json.attachments,
						forwardedfolder: json.forwardedfolder,
						forwardedfrom: json.forwardedfrom,
						origuid: json.origuid
					});
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
    actionOpenNew: function(rowIndex) {
        var me=this,
			recs=(rowIndex>=0)?
				me.getStore().getAt(rowIndex):
				me.getSelection();    
        me.editMessage(recs[0],false);
    },
    
    editMessage: function(r,del) {
        this.editMessageById(r.get('folder')||this.currentFolder,r.get("idmessage"),del);
    },
    
    editMessageById: function(idfolder,idmessage,del) {
        var me=this,msgId=Sonicle.webtop.mail.view.MessageEditor.buildMsgId();
        
		WT.ajaxReq(me.mys.ID, 'GetEditMessage', {
			params: {
				folder: idfolder,
				idmessage: idmessage,
                newmsgid: msgId
			},
			callback: function(success,json) {
				if (json.result) {
					me.mys.startNewMessage(idfolder,{
						subject: json.subject,
						recipients: json.recipients,
						content: json.content,
                        contentReady: true,
                        proprity: json.priority,
						replyfolder: json.replyfolder,
                        format: json.format,
                        msgId: msgId,
						attachments: json.attachments,
						forwardedfolder: json.forwardedfolder,
						forwardedfrom: json.forwardedfrom,
						replyfolder: json.replyfolder,
						inreplyto: json.inreplyto,
						references: json.references,
						origuid: json.origuid
					});
                    if (del) {
                        //TODO: remove original
                    }
				} else {
					WT.error(json.text);
				}
			}
		});					
    },
	
    actionDelete: function() {
		if (this.storeLoading) {
			return;
		}
		
		var me=this,
			curfolder=me.currentFolder,
			sm=me.getSelectionModel(),
			selection=sm.getSelection(),
			ftrash=me.mys.getFolderTrash();
	
		if (!selection || selection.length==0) return;
		
        if (ftrash) {
			if (!me.multifolder && curfolder===ftrash) {
				//TODO: warning
				WT.confirm(me.res('sureprompt'),function(bid) {
					if (bid==='yes') {
						me.deleteSelection(curfolder,selection);
					}
					me.focus();
				},me);
          } else {
              me.moveSelection(curfolder,ftrash,selection);
              me.focus();
          }
        }
    },	
	
    actionSpam: function() {
		if (this.storeLoading) {
			return;
		}
		
		var me=this,
			curfolder=me.currentFolder,
			sm=me.getSelectionModel(),
			selection=sm.getSelection(),
			fspam=me.mys.getFolderSpam();
	
		if (!selection || selection.length==0) return;
		
        if (fspam) {
			me.moveSelection(curfolder,fspam,selection);
			me.focus();
        }
    },

    actionMarkSeen: function() {
		this.actionMarkSeenState(true);
    },
	
    actionMarkUnseen: function() {
		this.actionMarkSeenState(false);
    },
	
	actionMarkSeenState: function(seen) {
		if (this.storeLoading) return;
		
		var me=this;
		me.markSelectionSeenState(
			me.currentFolder,
			me.getSelectionModel().getSelection(),
			seen
		);
		me.focus();
	},
	
	actionMoveToFolder: function() {
		var me=this;
		if (!me.mcmdialog) {
			me.mcmdialog=WT.createView(me.mys.ID,'view.MoveCopyMessagesDialog',{
				viewCfg: {
					mys: me,
					multifolder: me.multifolder,
					fromFolder: me.currentFolder,
					grid: me
				}
			});
		} else {
			me.mcmdialog.fromFolder=me.currentFolder;
			me.mcmdialog.grid=me;
		}
		
		me.mcmdialog.show();
	},
	
	changePageSize: function() {
		var me=this;
		WT.prompt(me.res("changepagesizetext"),{
			title: me.res("changepagesizetitle"),
			fn: function(btn,text) {
				if (btn=='ok') {
					var n=parseInt(text);
					if (isNaN(n)) {
						WT.error(me.res("changepagesizenan"));
					} else {
						me.setPageSize(n);
						me.store.reload({
						  params: {start:0,limit:n,timestamp: Date.now()}
						});
						WT.ajaxReq(me.mys.ID, 'SavePageRows', {
							params: {
								pagerows: n
							}
						});					
					}
				}
			},
			scope: me,
			value: me.getPageSize()
		});
	},
	
	removeRecords: function(ids) {
		var me=this;
		me.getSelectionModel().removeIds(ids,{ timestamp: Date.now() });
	},
	
    deleteSelection: function(from,selection) {
        var me=this,data=me.sel2ids(selection);
		data.cb=function(result) {
			if (result) {
				me.removeRecords(data.ids);
			}
		}
        me.deleteMessages(from,data);
    },
    
    deleteMessages: function(folder,data) {
		var me=this;
			me.fireEvent('deleting',me);
		WT.ajaxReq(me.mys.ID, 'DeleteMessages', {
			params: {
				fromfolder: folder,
				ids: data.ids,
				multifolder: data.multifolder
			},
			callback: function(success,json) {
				if (json.result) {
					Ext.callback(data.cb,data.scope||me,[json.result]);
				} else {
					WT.error(json.text);
				}
			}
		});					
    },

    deleteMessage: function(folder,idmessage,dview) {
		var me=this,
			curfolder=me.currentFolder,
			data={ 
				ids: [ idmessage ], 
				multifolder: false,
				cb: function(result) {
					if (result) {
						me.removeRecords(data.ids);
					}
				}
			},
			ftrash=me.mys.getFolderTrash();
		
		if (curfolder===ftrash) {
			//TODO: warning
			WT.confirm(me.res('sureprompt'),function(bid) {
				if (bid==='yes') {
					me.deleteMessages(folder,data);
					dview.closeView();
				}
			});
		} else {
			me.moveMessages(folder,ftrash,data);
			dview.closeView();
		}
    },	
	
    moveSelection: function(from,to,selection) {
        var me=this, 
            data=me.sel2ids(selection);
		data.cb=function(result) {
			if (result) {
				me.removeRecords(data.ids);
			}
		}
        me.moveMessages(from,to,data);
    },
	
    copySelection: function(from,to,selection) {
        var me=this, 
            data=me.sel2ids(selection);
        me.copyMessages(from,to,data)
    },
	
    moveMessages: function(from,to,data) {
		var me=this;
		
        me.fireEvent('moving',me);
		
        me.operateMessages("MoveMessages",from,to,data);
    },	
	
    copyMessages: function(from,to,data) {
        this.operateMessages("CopyMessages",from,to,data);
    },

	//TODO: customer,causal
	//operateMessages: function(action,from,to,data,customer_id,causal_id) {
    operateMessages: function(action,from,to,data) {
		var me=this;
		WT.ajaxReq(me.mys.ID, action, {
			params: {
				//customer_id:customer_id,
				//causal_id:causal_id
				fromfolder: from,
				ids: data.ids,
				tofolder: to,
				multifolder: data.multifolder
			},
			callback: function(success,json) {
				Ext.callback(data.cb,data.scope||me,[json.result]);
				if (json.result) {
					var d=data,
					//TODO: update imap tree?
					//tree=me.mys.imapTree,
					seen=d.seen,
						s=me.store;
					//TODO: update imap tree?
					//var nt=tree.getNodeById(options.tofolder);
					if (!d.multifolder) {
						//s.reload();
					} else {
											//TODO: update imap tree?
						/*var ids=d.ids;
						var dorel=false;
						var remove=(action!="CopyMessages");
						for(var i=0;i<ids.length;++i) {
						  var r=s.getById(ids[i]);
						  if (!seen[i]) {
							var fid=r.get("folder");
							if (fid==this.currentFolder) dorel=true;
							else {
								var n=this.ms.imapTree.getNodeById(fid);
								if (n) {
									var a=n.attributes;
									jd={ unread: a.unread-1, millis: o.millis };
									this.ms.updateUnreads(fid,jd,false);
								}
							}
						  }
						  if (remove) s.remove(r);
						}*/
						//if (dorel) {
							//s.reload();
						//}
					}
					//TODO: update imap tree?
					/*
					var info=nt.attributes;
					info.millis=o.millis;
					for(var i=0;i<seen.length;++i) {
						if (!seen[i]) info.unread++;
					}
					this.ms.updateUnreads(options.tofolder,info,false);
					*/
				} else {
					WT.error(json.text);
				}
			}
		});							
    },
	
    operateAllFiltered: function(action,from,to,handler,scope) {
		var me=this,oparams=me.store.proxy.extraParams,
			params={
				action: action,
				fromfolder: from,
				tofolder: to,
				allfiltered: true
			};
		if (oparams.sort) {
			params.sort=oparams.sort.property;
			params.dir=oparams.sort.dir;
		}
        if (oparams.quickfilter) params.quickfilter=oparams.quickfilter;
        if (oparams.searchfield) params.searchfield=oparams.searchfield;
        if (oparams.pattern) params.pattern=oparams.pattern;
		
		WT.ajaxReq(me.mys.ID, action, {
			params: params,
			callback: function(success,json) {
				if (handler) Ext.callback(handler,scope||me,[json.result]);
				if (json.result) {
					//if (options.win) {
					//	options.win.enable();
					//	options.win.tree.bfiltered.setValue(false);
					//	options.win.hide();
					//}
					me.store.reload({ 
						params: {
							timestamp: Date.now()
						}
					});
					//var tree=this.ms.imapTree;
					//var n=tree.getNodeById(options.tofolder);
					//if (n) {
					//	var info=n.attributes;
					//	info.unread=o.unread;
					//	info.millis=o.millis;
					//	this.ms.updateUnreads(options.tofolder,info,false);
					//}
				} else {
					WT.error(json.text);
				}
			}

		});							
    },	
	
    markSelectionSeenState: function(from,selection,seen) {
        var me=this, 
            data=me.sel2ids(selection);
	
		data.cb=function(result) {
			if (result) {
                Ext.each(
                    selection,
                    function(r,index,allItems) {
						this.updateRecordSeenState(r,seen);
					},this
				);
			}
		}
        me.markMessagesSeenState(from,data,seen);
    },
    
    markMessageSeenState: function(r,seen) {
        var me=this,
			idmsg=r.get("idmessage"),
			folder=me.multifolder?r.get("folder"):me.currentFolder,
            id=me.multifolder?folder+"|"+idmsg:idmsg,
            data={ 
                ids: [ id ],
                multifolder: me.multifolder,
                cb: function() {
                    me.updateRecordSeenState(r,seen);
                }
            };
        me.markMessagesSeenState(folder,data,seen);
    },

    markMessagesSeenState: function(folder,data,seen) {
		var me=this;
		WT.ajaxReq(me.mys.ID, seen?"SeenMessages":"UnseenMessages", {
			params: {
				fromfolder: folder,
				ids: data.ids,
				multifolder: data.multifolder
			},
			callback: function(success,json) {
				Ext.callback(data.cb,data.scope||me,[json.result]);
				if (!json.result)
					WT.error(json.text);
			}
		});
	},
	
	updateRecordSeenStateAtIndex: function(ix,seen) {
		if (ix>=0) {
			var r=this.store.getAt(ix);
			this.updateRecordSeenState(r,seen);
		}
	},
	
	updateRecordSeenState: function(r,seen) {
		if (r && r.get("unread")===seen) {
			r.set("unread",!seen);
			var st=r.get("status");
			if (seen) {
				if (st==="unread"||st==="new") r.set("status","read");
			} else {
				if (st==="read") r.set("status","unread");
			}
			//TODO: not needed with websocket?
			//var o=s.getProxy().getReader().rawData;
			//o.millis=millis;
			//o.unread--;
			//me.mys.updateUnreads(me.currentFolder,o,false);
		}
	},
	
    actionFlag: function(flagstring) {
        var me=this,
			selection=me.getSelection(),
			folder=me.currentFolder,
			data=me.sel2ids(selection),
			ids=data.ids,
			params={
                flag: flagstring,
                fromfolder: folder,
                ids: ids,
                multifolder: data.multifolder
			};
			
        if (data.folders) params.folders=data.folders;
		
		WT.ajaxReq(me.mys.ID, 'FlagMessages', {
			params: params,
			callback: function(success,json) {
              if (json.result) {
                  var dorel=false,
					  fl=me.mys.messagesPanel.folderList,
					  fs=flagstring;
			  
                  if (fs==='clear') fs="";
                  
                  Ext.each(
                    selection,
                    function(r,index,allItems) {
                      if (me!==fl) {
                        var ff=(me.multifolder?r.get("folder"):me.currentFolder);
                        if (ff===fl.currentFolder) dorel=true;
                      }
					  if (fs==="special") {
						  if (r.get("flag")==="special") r.set("flag","");
						  else r.set("flag",fs);
					  } else if (fs!=="complete") {
                          r.set("flag",fs);
                      } else {
                          var xflag=r.get("flag");
                          if (xflag==='') {
                              r.set("flag",'complete');
                          } 
                          else if (!xflag.endsWith("complete")) {
                              r.set("flag",xflag+"-"+fs);
                          }
                      }
                      
                    },this
                  );
                  if (dorel) this.mys.reloadFolderList();

              } else {
                  WT.error(json.text);
              }
			}
		});					
		
    },
	
	actionAddNote: function() {
		var r=this.getSelectionModel().getSelection()[0];
	
		this.editNote(r.get('idmessage'),r.get("folder")||this.currentFolder);
	},
	
    editNote: function(id,folder) {
		var me=this;
		WT.ajaxReq(me.mys.ID, 'GetMessageNote', {
			params: {
                folder: folder,
                idmessage: id
			},
			callback: function(success,json) {
				if (success) {
					WT.prompt('',{
						title: me.res("mailnote"),
						fn: function(btn,text) {
							if (btn=='ok') {
								me.saveNote(folder,id,text);
							}
						},
						scope: me,
						width: 400,
						multiline: 200,
						value: json.message
					});
				} else {
					WT.error(json.text);
				}
			}
		});					
    },
    
    saveNote: function(folder,id,text) {
		var me=this;
		WT.ajaxReq(me.mys.ID, 'SaveMessageNote', {
			params: {
                folder: folder,
                idmessage: id,
                text: text
			},
			callback: function(success,json) {
				if (success) {
					if (folder===me.currentFolder)
						me.mys.reloadFolderList();
				} else {
					WT.error(json.text);
				}
			}
		});					
    },	
	
	actionCreateReminder: function() {
		var me=this,
			r=me.getSelectionModel().getSelection()[0],
			capi=WT.getServiceApi("com.sonicle.webtop.calendar"),
			from=me.decodeEntities(r.get("from"))+" <"+r.get("fromemail")+">",
			to=me.decodeEntities(r.get("to"))+" <"+r.get("toemail")+">",
			subject=me.decodeEntities(r.get("subject")),
			date=r.get("date"),
			description=
				me.res("column-date")+": "+Ext.util.Format.date(date,'d-M-Y')+"\n"+
				me.res("column-from")+": "+from+"\n"+
				me.res("column-to")+": "+to+"\n",
			tomorrow=Sonicle.Date.add(new Date(),{ days: 1 });
			
			capi.addEvent({
				startDate: tomorrow,
				endDate: Sonicle.Date.add(tomorrow, { minutes: 30 }),
				title: subject,
				description: description,
				reminder: 5	
			},{
				dirty: true
			});
		
	},
	
	decodeEntities : function(str) {
	   var temp_div = document.createElement('div');
	   temp_div.innerHTML = str;
	   return temp_div.firstChild.nodeValue;
	},	
	
	actionSaveMail: function() {
		var r=this.getSelectionModel().getSelection()[0];
	
		this.saveMail(r.get('idmessage'),r.get("folder")||this.currentFolder);
	},
	
	saveMail: function(id,folder) {
        var params={
            folder: folder,
            id: id
        };
		
        var url=WTF.processBinUrl(this.mys.ID,"SaveMail",params);;
        window.open(url);
	},
	
    actionResetColumns: function() {
        this.initFolderState(true);
		proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), { timestamp: Date.now() }));
		this.store.load();
    },

    actionViewHeaders: function() {
        this.actionViewSource(true);
    },

    actionViewSource: function(headers) {
        var me=this,
			r=me.getSelectionModel().getSelection()[0];
	
		WT.ajaxReq(me.mys.ID, 'GetSource', {
			params: {
                folder: r.get('folder')||me.currentFolder,
                id: r.get('idmessage'),
                headers: !!headers
			},
			callback: function(success,json) {
				if (json.result) {
					WT.createView(me.mys.ID,'view.HeadersView',{
						viewCfg: {
							source: json.source
						}
					}).show();

				} else {
					WT.error(json.text);
				}
			}
		});					
	
    },
	
	reload: function() {
		this.store.reload({ 
			params: {
				timestamp: Date.now()
			}
		});
	},
	
	changeGrouping: function(newgroup) {
		var me=this;
		WT.ajaxReq(me.mys.ID, 'GroupChanged', {
			params: {
                group: newgroup,
                folder: me.currentFolder
			},
			callback: function(success,json) {
				if (success) {
					if (newgroup && newgroup!=='' && newgroup!=='none') {
						var s=me.store;
						//s.blockLoad(); //TODO: We are using ExtJs 6.2, is this still necessary?
						s.sort('date', 'DESC');
						//s.unblockLoad(false); //TODO: We are using ExtJs 6.2, is this still necessary?
					}
					me.mys.setFolderGroup(me.currentFolder,newgroup);
					me.mys.showFolder(me.currentFolder);
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
	showFilterBar: function() {
		var me=this;
		me.getPlugin('gridfilterbar').setHidden(false);
		me.fireEvent('showfilterbar', me);
	},
	
	hideFilterBar: function() {
		var me=this;
		me.getPlugin('gridfilterbar').setHidden(true);
		me.fireEvent('hidefilterbar', me);
	},
	
	clearFilterBar: function() {
		this.getPlugin('gridfilterbar').clearFilterFields();
		this.getStore().clearFilter();
	},
	
	indexOfMessage: function(id) {
		return this.store.findExact('idmessage',id);
	},

    sel2ids: function(selection) {
        var me=this,
			ids=[],
			seen=[],
			mf=me.multifolder;
	
        Ext.each(
          selection,
          function(r,index,allItems) {
              var id=r.get("idmessage");
              if (mf) id=r.get("folder")+"|"+id;
              ids[index]=id;
              seen[index]=(r.get("unread")?false:true);
          }
        );
        return {selection: selection, ids: ids, seen: seen, multifolder: mf};
    },
	
    /**
     * Override: Gets the state id against the current folder.
     * @return {String} The 'stateId' or the implicit 'id' specified by component configuration.
     * @private
     */
    getStateId: function() {
        var me = this;
        return (me.baseStateId || (me.baseStateId=me.mys.buildStateId('messagegrid')))+"@"+me.currentFolder;
    },
	
	createColumnsFromState: function(state) {
		var me=this,n=0,dcols=new Array();
		
		Ext.each(state.columns,function(scol) {
		
			switch(scol.id) {
				
				case 'stid-folder':
					dcols[n++]=Ext.create({//Folder
						xtype: 'gridcolumn',
						header: '',
						width: 100,
						sortable: true,
						dataIndex: 'folder',
						stateId: 'stid-folder',
						hidden: true,
						filter: { xtype: 'textfield'}
					});
					break;
					
				case 'stid-folderdesc':
					dcols[n++]=Ext.create({//Folder Descripion
						xtype: 'gridcolumn',
						header: me.res("column-folder"),
						width: 100,
						sortable: true,
						dataIndex: 'folderdesc',
						stateId: 'stid-folderdesc',
						hidden: scol.hidden,
						filter: { xtype: 'textfield'}
					});
					break;
					
				case 'stid-priority':
					dcols[n++]=Ext.create({//Priority
						xtype: 'gridcolumn',
						//header: '<i class="wtmail-icon-header-priority-xs">\u00a0\u00a0\u00a0\u00a0\u00a0</i>',
						header: WTF.headerWithGlyphIcon('fa fa-exclamation'),
						cls: 'wtmail-header-text-clip',
						width: 35,
						sortable: true,
						menuDisabled: true,
						dataIndex: 'priority',
						stateId: 'stid-priority',
						hidden: scol.hidden,
						renderer: function(value,metadata,record,rowIndex,colIndex,store) {
								var tag;
								var others="border=0";
								if (value<3) tag=WTF.imageTag(me.mys.ID,'priorityhigh_16.gif',others);
								else tag=WTF.globalImageTag('empty.gif',7,16,others);
								return tag;
						},
						scope: me,
						filter: {
							xtype: 'soicononlycombobox',
							editable: false,
							width: 24,
							listConfig: {
								minWidth: 42
							},
							hideTrigger: true,
							store: [
								['','\u00a0',''],
								['1','\u00a0','wtmail-icon-priority-high-xs']
							]
						}
					});
					break;
					
				case 'stid-unread':
					dcols[n++]=Ext.create({//Status
						xtype: 'soiconcolumn',
						header: WTF.headerWithGlyphIcon('fa fa-eye'),
						width: 28,
						sortable: true,
						menuDisabled: true,
						stopSelection: true,
						dataIndex: 'unread',
						stateId: 'stid-unread',
						hidden: scol.hidden,
						getIconCls: function(value,rec) {
							return Ext.isEmpty(value)?WTF.cssIconCls(WT.XID, 'empty', 'xs'):WTF.cssIconCls(me.mys.XID, 'status-'+(value?"seen":"unseen"), 'xs');
						},
						handler: function(grid, rix, cix, e, rec) {
							var newunread=!rec.get('unread');
							me.markMessageSeenState(rec,!newunread);
						},
						scope: me,
						filter: {
							xtype: 'soicononlycombobox',
							editable: false,
							width: 24,
							listConfig: {
								minWidth: 42
							},
							hideTrigger: true,
							store: [
								['','\u00a0',''],
								["true",'\u00a0',WTF.cssIconCls(me.mys.XID, 'status-seen', 'xs')],
								["false",'\u00a0',WTF.cssIconCls(me.mys.XID, 'status-unseen', 'xs')]
							]
						}
					});
					break;
					
				case 'stid-from':
					dcols[n++]=Ext.create({//From
						xtype: 'gridcolumn',
						header: me.res("column-from"),
						width: 200,
						sortable: true,
						dataIndex: 'from',
						stateId: 'stid-from',
						hidden: scol.hidden,
						filter: { xtype: 'textfield'}
					});
					break;
					
				case 'stid-to':
					dcols[n++]=Ext.create({//To
						xtype: 'gridcolumn',
						header: me.res("column-to"),
						width: 200,
						sortable: true,
						dataIndex: 'to',
						stateId: 'stid-to',
						hidden: scol.hidden, //!me.multifolder,
						filter: { xtype: 'textfield'}
					});
					break;
					
				case 'stid-subject':
					dcols[n++]=Ext.create({//Subject
						xtype: 'gridcolumn',
						header: me.res("column-subject"),
						width: 300,
						sortable: true,
						dataIndex: 'subject',
						stateId: 'stid-subject',
						hidden: scol.hidden, 
						renderer: function(value,metadata,record,rowIndex,colIndex,store) {
							var status=record.get("status"),
								tid=record.get("threadId"),
								tindent=record.get("threadIndent"),
								topen=record.get("threadOpen"),
								tchildren=record.get("threadHasChildren"),
								imgtag="";

							if (me.threaded) {                    
								if (tindent===0 && tchildren) {
									var cls=topen?"":"x-grid-group-hd-collapsed";
									imgtag="<div class='x-grid-group-hd-collapsible "+cls+"'>"+
											"<span class='x-grid-group-title wtmail-element-toclick'"+
											  " onclick='WT.getApp().getService(\""+me.mys.ID+"\").messagesPanel.folderList.collapseClicked("+rowIndex+");'"+
											">&nbsp;</span>";
									var tuc=record.get("threadUnseenChildren")
									if (!topen && tuc && tuc>0) imgtag+="<b>+"+tuc+"</b>&nbsp;";
								} else {
									imgtag="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
								}
								for(var i=0;i<tindent;++i) imgtag+="&nbsp;&nbsp;&nbsp;";
							}
							if (status!=="read" && status!=="unread") {
								var imgname=Ext.String.format("status{0}_16.png",status);
								imgtag+=WTF.imageTag(me.mys.ID,imgname,16,16)+"&nbsp;";
							}
							imgtag+=value;
							if (me.threaded && tindent===0) {
								imgtag+="</div>";
							}
							return imgtag;
						},
						filter: { xtype: 'textfield'}
					});
					break;
					
				case 'stid-date':
					dcols[n++]=Ext.create({//Date
						xtype: 'gridcolumn',
						header: me.res("column-date"),
						width: 80,
						sortable: true,
						renderer: function(value,metadata,record,rowIndex,colIndex,store) {
							var tdy=record.get("istoday"),
								fmtd=record.get("fmtd"),
								tag;
							if (!fmtd && (tdy || store.groupField=='gdate')) tag="<span ext:qtip='"+Ext.util.Format.date(value,'d-M-Y')+"'>"+Ext.util.Format.date(value,'H:i:s')+"</span>";
							else tag="<span ext:qtip='"+Ext.util.Format.date(value,'H:i:s')+"'>"+Ext.util.Format.date(value,'d-M-Y')+"</span>";
							return tag;
						},
						dataIndex: 'date',
						stateId: 'stid-date',
						hidden: scol.hidden,
						filter: { xtype: 'textfield'}
					});
					break;
					
				case 'stid-gdate':
					dcols[n++]=Ext.create({//Date
						xtype: 'gridcolumn',
						header: me.res("column-date"),
						hidden: true,
						dataIndex: 'gdate',
						stateId: 'stid-gdate',
						filter: {}
					});
					break;
					
				case 'stid-sdate':
					dcols[n++]=Ext.create({//Date
						xtype: 'gridcolumn',
						header: me.res("column-date"),
						hidden: true,
						dataIndex: 'sdate',
						stateId: 'stid-sdate',
						filter: {}
					});
					break;
					
				case 'stid-xdate':
					dcols[n++]=Ext.create({//Date
						xtype: 'gridcolumn',
						header: me.res("column-date"),
						hidden: true,
						dataIndex: 'xdate',
						stateId: 'stid-xdate',
						filter: {}
					});
					break;
					
				case 'stid-size':
					dcols[n++]=Ext.create({//Dimension
						xtype: 'sobytescolumn',
						header: me.res("column-size"),
						width: 50,
						sortable: true,
						dataIndex: 'size',
						stateId: 'stid-size',
						hidden: scol.hidden, //me.multifolder,
			/*            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
							return WTU.humanReadableSize(parseInt(value));
						},*/
						filter: { xtype: 'textfield'}
					});
					break;
					
				case 'stid-atts':
					dcols[n++]=Ext.create({//Attachment
						xtype: 'gridcolumn',
						//header: '<i class="wtmail-icon-header-attach-xs">\u00a0\u00a0\u00a0\u00a0\u00a0</i>',
						header: WTF.headerWithGlyphIcon('fa fa-paperclip'),
						cls: 'wtmail-header-text-clip',
						width: 30,
						sortable: false,
						menuDisabled: true,
						dataIndex: 'atts',
						stateId: 'stid-atts',
						hidden: scol.hidden,
						xtype: 'soiconcolumn',
						getIconCls: function(value,rec) {
							return Ext.isEmpty(value)?WTF.cssIconCls(WT.XID, 'empty', 'xs'):WTF.cssIconCls(me.mys.XID, 'attachment', 'xs');
						},
						iconSize: WTU.imgSizeToPx('xs'),
						scope: me,
						filter: { xtype: 'textfield'}

					});
					break;
					
				case 'stid-flag':
					dcols[n++]=Ext.create({//Flag
						xtype: 'gridcolumn',
						//header: '<i class="wtmail-icon-header-flag-xs">\u00a0\u00a0\u00a0\u00a0\u00a0</i>',
						header: WTF.headerWithGlyphIcon('fa fa-flag-o'),
						cls: 'wtmail-header-text-clip',
						width: 30,
						sortable: true,
						menuDisabled: true,
						dataIndex: 'flag',
						stateId: 'stid-flag',
						hidden: scol.hidden,
						renderer: function(value,metadata,record,rowIndex,colIndex,store) {
								var tag;
								var others="border=0";
								if (value!=='') tag=WTF.imageTag(me.mys.ID,"flag"+value+"_16.gif",16,16,others);
								else tag=WTF.globalImageTag('empty.gif',16,16,others);
								return tag;
						},
						scope: me,
						filter: {
							xtype: 'soicononlycombobox',
							editable: false,
							width: 24,
							listConfig: {
								minWidth: 42
							},
							hideTrigger: true,
							store: [
								['','\u00a0',''],
								['red','\u00a0','wtmail-icon-flagred-xs'],
								['blue','\u00a0','wtmail-icon-flagblue-xs'],
								['yellow','\u00a0','wtmail-icon-flagyellow-xs'],
								['green','\u00a0','wtmail-icon-flaggreen-xs'],
								['orange','\u00a0','wtmail-icon-flagorange-xs'],
								['purple','\u00a0','wtmail-icon-flagpurple-xs'],
								['black','\u00a0','wtmail-icon-flagblack-xs'],
								['gray','\u00a0','wtmail-icon-flaggray-xs'],
								['white','\u00a0','wtmail-icon-flagwhite-xs'],
								['brown','\u00a0','wtmail-icon-flagbrown-xs'],
								['azure','\u00a0','wtmail-icon-flagazure-xs'],
								['pink','\u00a0','wtmail-icon-flagpink-xs']
							]
						}
						/*filter: {
							fieldEvents: ["select"],
							field: {
								xtype: "iconcombo",
								editable: false,
								mode: 'local',
								width: 24,
			//                        autoCreate: {tag: "input", type: "text", size: "8", autocomplete: "off", disabled: 'disabled'},
			//                        triggerConfig: {tag: "img", src: 'webtop/themes/win/minitrigger.gif', cls: "x-form-minitrigger ", width: 5},
								store: new Ext.data.ArrayStore({
								  //id: 0,
								  fields: ['value','text','icon'],
								  data: [
									  ['','\u00a0',''],
									  ['red',me.res('flagred'),'iconFlagRed'],
									  ['blue',me.res('flagblue'),'iconFlagBlue'],
									  ['yellow',me.res('flagyellow'),'iconFlagYellow'],
									  ['green',me.res('flaggreen'),'iconFlagGreen'],
									  ['orange',me.res('flagorange'),'iconFlagOrange'],
									  ['purple',me.res('flagpurple'),'iconFlagPurple'],
									  ['black',me.res('flagblack'),'iconFlagBlack'],
									  ['gray',me.res('flaggray'),'iconFlagGray'],
									  ['white',me.res('flagwhite'),'iconFlagWhite'],
									  ['brown',me.res('flagbrown'),'iconFlagBrown'],
									  ['azure',me.res('flagazure'),'iconFlagAzure'],
									  ['pink',me.res('flagpink'),'iconFlagPink']
								  ]
								}),
								valueField: 'value',
								displayField: 'text',
								iconClsField: 'icon',
								triggerAction: 'all',
								value: ""
							}
						}*/

					});
					break;
					
				case 'stid-note':
					dcols[n++]=Ext.create({//Mail note
						xtype: 'gridcolumn',
						//header: '<i class="wtmail-icon-header-note-xs">\u00a0\u00a0\u00a0\u00a0\u00a0</i>',
						header: WTF.headerWithGlyphIcon('fa fa-sticky-note-o'),
						cls: 'wtmail-header-text-clip',
						width: 30,
						sortable: true,
						menuDisabled: true,
						dataIndex: 'note',
						stateId: 'stid-note',
						hidden: scol.hidden,
						renderer: function(value,metadata,record,rowIndex,colIndex,store) {
								var tag;
								if (value) tag=WTF.imageTag(me.mys.ID,'mailnote_16.gif',16,16,"border=0 style='cursor: pointer'");
								else tag=WTF.globalImageTag('empty.gif',16,16,"border=0");
								return tag;
						},
						scope: me,
						filter: { xtype: 'textfield'}

					});
					break;
					
				case 'stid-arch':
					dcols[n++]=Ext.create({//Archived
						xtype: 'gridcolumn',
						//header: '<i class="wtmail-icon-header-docmgt-xs">\u00a0\u00a0\u00a0\u00a0\u00a0</i>',
						header: WTF.headerWithGlyphIcon('fa fa-archive'),
						cls: 'wtmail-header-text-clip',
						width: 30,
						sortable: true,
						menuDisabled: true,
						dataIndex: 'arch',
						stateId: 'stid-arch',
						hidden: scol.hidden, //me.multifolder,
						renderer: function(value,metadata,record,rowIndex,colIndex,store) {
								var tag;
								var others="border=0";
								if (value) tag=WTF.imageTag(me.mys.ID,"docmgt_16.png",16,16,others);
								else tag=WTF.globalImageTag('empty.gif',16,16,others);
								return tag;
						},
						scope: me,
						filter: { xtype: 'textfield'}
					});
					break;
			}
		});
		
		return dcols;
	},
	
	createDefaultState: function() {
        var me=this,n=0,stcols=new Array();
		
/*		a={"columns":[
				{"id":"stid-priority","width":40},
				{"id":"stid-unread","width":40},
				{"id":"stid-from","width":198},
				{"id":"stid-subject","width":675},
				{"id":"stid-to","width":375,"hidden":true},
				{"id":"stid-date","width":96},
				{"id":"stid-gdate","width":100},
				{"id":"stid-sdate","width":100},
				{"id":"stid-xdate","width":100},
				{"id":"stid-size","width":89},
				{"id":"stid-atts","width":40},
				{"id":"stid-flag","width":47},
				{"id":"stid-note","width":40}],
			"weight":0
		}*/
        if (me.multifolder) {
            stcols[n++]={ id: 'stid-folder', width: 100 };
            stcols[n++]={ id: 'stid-folderdesc', width: 100 };
        }
        stcols[n++]={ id: 'stid-priority', width: 40 };
        stcols[n++]={ id: 'stid-unread', width: 40 };
        stcols[n++]={ id: 'stid-from', width: 198 };
        stcols[n++]={ id: 'stid-to', width: 198 };
        stcols[n++]={ id: 'stid-subject', width: 675 };
        stcols[n++]={ id: 'stid-date', width: 96 };
        stcols[n++]={ id: 'stid-gdate', width: 96 };
        stcols[n++]={ id: 'stid-sdate', width: 96 };
        stcols[n++]={ id: 'stid-xdate', width: 96 };
        stcols[n++]={ id: 'stid-size', width: 89 };
        stcols[n++]={ id: 'stid-atts', width: 40 };
        stcols[n++]={ id: 'stid-flag', width: 47 };
        stcols[n++]={ id: 'stid-note', width: 40 };
        
        if (me.arch) {
            stcols[n++]={ id: 'stid-arch', width: 40 };
        }
		
		
		state={
			"columns": stcols,
			"weight":0
		};
		return state;
	},
	
    res: function(s) {
        return this.mys.res(s);
    }
	
});
