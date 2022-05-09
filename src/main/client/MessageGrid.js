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
		var unread=record.get('unread'),
			pecstatus=record.get('pecstatus');
		var cls=unread && !this.grid.getCompactView() ?'wtmail-row-unread':'';
		
		if (pecstatus) {
			if (pecstatus=='accettazione') cls+=' wtmail-row-pec-accepted';
			else if (pecstatus=='avvenuta-consegna') cls+=' wtmail-row-pec-delivered';
		}
		else if (!this.grid.getSelectionModel().isSelected(index)) {
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
		{ name: 'threadHasChildren', type:'boolean' },
        { name: 'threadUnseenChildren', type:'int' },
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
		{ name: 'threadHasChildren', type:'boolean' },
        { name: 'threadUnseenChildren', type:'int' },
        { name: 'scheddate', type:'date' },
		{ name: 'autoedit', type:'boolean' }
	]
});

Ext.define('Sonicle.webtop.mail.GridFeatureGrouping', {
	extend: 'Ext.grid.feature.Grouping',
	alias: 'feature.mailgrouping',
	expandTip: '',
	collapseTip: ''
//	init: function() {
//		var me=this;
//		me.callParent(arguments);
//		me.collapsible = true;
//	}
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
//	onColumnMove: function() {
//		console.log('onColumnMove overridden!');
//	}
});

Ext.define('Sonicle.webtop.mail.MessageGrid',{
	extend: 'Ext.grid.Panel',
	requires: [
		'Sonicle.data.BufferedStore',
		'Sonicle.selection.RowModel',
		'Sonicle.webtop.mail.plugin.MessageGridViewDragDrop',
		'Sonicle.form.field.IconComboBox',
		'Sonicle.grid.column.Bytes',
		'Sonicle.webtop.mail.ux.grid.column.Message'
	],
	
	config: {
		compactView: true
	},
	pageSize: 50,	
    frame: false,
	enableColumnMove: true,
	skipViewOptionsCheckChange: 0,
	
	features: [
		{
			ftype:'mailgrouping'
		},
		{
			ftype: 'rowbody',
			getAdditionalData: function (data, idx, record, orig) {
				var msgtext=Ext.String.htmlEncode(record.get("msgtext"));
				return {
					rowBody: msgtext!=null?
						"<span style='padding-left: 50px'>"+msgtext+"</span>":
						null,
					rowBodyCls: msgtext!=null?"wtmail-row-body":"wtmail-row-body-hidden"
				}
			}
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
	readonly: false,
	openThreads: {},
	showToolbar: true,
	
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
							//shift: false,
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
			emptyText: me.mys.res('messageGrid.emp'),
			navigationModel: Ext.create('Sonicle.webtop.mail.NavigationModel',{}),
			loadMask: { msg: WT.res("loading") },
			getRowClass: function(record, index, rowParams, store ) {
				var unread=record.get('unread');
				    pecstatus=record.get('pecstatus'),
				    tdy=record.get('istoday'),
					cls1=(unread && !this.grid.getCompactView()) ?'wtmail-row-unread':'',
					cls2=tdy?'wtmail-row-today':'';
					/*cls3=(pecstatus=='accettazione')?'wtmail-row-pec-accepted':
							(pecstatus=='avvenuta-consegna')?'wtmail-row-pec-delivered':
							(pecstatus=='errore')?'wtmail-row-pec-error':'';*/
				return cls1+' '+cls2/*+' '+cls3*/;
			},
			
			plugins: [
				{
					ptype: 'messagegridviewdragdrop',
					pluginId: 'messagegridviewdragdrop'
				}
			]

		};
		
		me.plugins = [
			{
				ptype: 'so-gridstateresetmenu',
				menuStateResetText: WT.res('act-clearColumnState.lbl'),
				menuStateResetTooltip: WT.res('act-clearColumnState.tip'),
				listeners: {
					stateresetclick: function(s, grid) {
						Ext.state.Manager.clear(me.getStateId());
						me.reconfigure(me.buildColumnsCfg(me.compactView, me.evaluateActAsSent(), me.multifolder));
					}
				}
			}
		];

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
					},
					timeout: WT.getVar("ajaxSpecialTimeout")
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
					},
					timeout: WT.getVar("ajaxSpecialTimeout")
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
		/*
		if (!me.stateful) {
			me.columns=me.createColumnsFromState(
					me.createDefaultState()
			);
		}
		*/
		me.columns = me.buildColumnsCfg(me.compactView, me.evaluateActAsSent(), me.multifolder);
		
        me.store.on('beforeload',function() {
			me.storeLoading=true;
			me.idleRefreshFolder=null;
		});
		
        me.store.on('load',function(s,r,o) {
			me.storeLoading=false;
			me.loaded(s,r,o);
		});
		
		
        me.on('rowdblclick',function(grid, r, tr, rowIndex, e, eopts) {
			var folder=me.multifolder?r.get("folder"):me.currentFolder,
				acct=me.currentAccount;
			if (me.mys.isDrafts(acct,folder)) me.editMessage(r,true);
			else me.openMessage(r);
			//if(me.mys.getVar("seenOnOpen")) me.actionMarkSeenState(true);
		});
		me.on('cellclick',function(grid, td, cellIndex, r, rowIndex, e, eopts) {
			if (me.getColumnManager().getHeaderAtIndex(cellIndex).dataIndex==="note") {
				if (r.get("note")) {
					var folder=me.multifolder?r.get("folder"):me.currentFolder,
						acct=me.currentAccount;
					me.editNote(acct,r.get("idmessage"),folder);
				}
			}
		});
		
        me.callParent(arguments);
		
		var tbitems=[
			me.breadcrumb,
			'->'
		];
		
		if (!me.mys.getVar("toolbarCompact")) {
			tbitems=tbitems.concat([
				me.mys._TB("reply",null,'small'),
				me.mys._TB("replyall",null,'small'),
				me.mys._TB("forward",null,'small'),
				'-',
				me.mys._TB("print",null,'small'),
				me.mys._TB("delete",null,'small'),
				me.mys._TB("spam",null,'small'),
				'-'
			]);
		}
		tbitems=tbitems.concat([
			me.mys._TB("special",null,'small'),
			{
				text: null,
				iconCls: 'wtmail-icon-tag',
				menu: {
					xtype: 'wttagmenu',
					restoreSelectedTags: function(md) {
						var grid=me.mys.getMenuDataGrid(md);
						return me.mys.toMutualTags(grid.getSelection());
					},
					listeners: {
						tagclick: function(s, tagId, checked, itm, e) {
							var grid=me.mys.getCtxGrid(e);
							if (checked)
								grid.actionTag(tagId);
							else
								grid.actionUntag(tagId);						
						}
					}
				}
			},
			{
				text: null,
				iconCls: 'fas fa-bookmark wt-theme-glyph',
				menu: {
					items: [
						me.mys.getAct('flagred'),
						me.mys.getAct('flagorange'),
						me.mys.getAct('flaggreen'),
						me.mys.getAct('flagblue'),
						me.mys.getAct('flagpurple'),
						me.mys.getAct('flagyellow'),
						me.mys.getAct('flagblack'),
						me.mys.getAct('flaggray'),
						me.mys.getAct('flagwhite'),
						me.mys.getAct('flagbrown'),
						me.mys.getAct('flagazure'),
						me.mys.getAct('flagpink'),
						'-',
						me.mys.getAct('flagcomplete'),
						//me.getAct('addmemo'),
						me.mys.getAct('clear')
					]
				}
			},
			'-'
		]);
		
		var previewModeGroup = Ext.id(null, 'mail-previewmode-'),
				sortFieldGroup = Ext.id(null, 'mail-sortfield-'),
				sortDirGroup = Ext.id(null, 'mail-sortdir-'),
				groupByGroup = Ext.id(null, 'mail-groupby-');
		
		tbitems.push({
			xtype: 'button',
			tooltip: me.res('messagesPanel.viewOptions.tip'),
			iconCls: 'wt-icon-viewOptions',
			menu: {
				defaults: {
					scope: me
				},
				items: [{
					text: me.res('messagesPanel.viewOptions.previewMode.lbl'),
					disabled: !WT.plTags.desktop,
					menu: {
						defaults: {
							group: previewModeGroup,
							checked: false,
							checkHandler: me.onPreviewModeCheckChange,
							scope: me
						},
						items: [{
							itemId: 'previewMode-right',
							text: me.mys.res('messagesPanel.previewMode.right.lbl'),
							checkedCls: 'wt-menuitem-dock-right-checked',
							uncheckedCls: 'wt-menuitem-dock-right-unchecked'
						}, {
							itemId: 'previewMode-bottom',
							text: me.mys.res('messagesPanel.previewMode.bottom.lbl'),
							checkedCls: 'wt-menuitem-dock-bottom-checked',
							uncheckedCls: 'wt-menuitem-dock-bottom-unchecked'
						}, {
							itemId: 'previewMode-off',
							text: me.mys.res('messagesPanel.previewMode.off.lbl'),
							checkedCls: 'wt-menuitem-dock-none-checked',
							uncheckedCls: 'wt-menuitem-dock-none-unchecked'
						}],
						listeners: {
							beforeshow: function(s) {
								me.skipViewOptionsCheckChange++;
								var itm = s.getComponent('previewMode-' + me.mys.messagesPanel.getPreviewLocation());
								if (itm) itm.setChecked(true);
								me.skipViewOptionsCheckChange--;
							}
						}
					}
				}, '-', {
					xtype: 'somenuheader',
					text: me.res('messagesPanel.viewOptions.sortField.lbl')
				}, {
					itemId: 'sortField-readStatus',
					text: me.mys.res('messagesPanel.sortField.readStatus.lbl'),
					checked: false,
					group: sortFieldGroup,
					checkHandler: me.onSortFieldCheckChange
				}, {
					itemId: 'sortField-from',
					text: me.mys.res('messagesPanel.sortField.from.lbl'),
					checked: false,
					group: sortFieldGroup,
					checkHandler: me.onSortFieldCheckChange
				}, {
					itemId: 'sortField-to',
					text: me.mys.res('messagesPanel.sortField.to.lbl'),
					checked: false,
					group: sortFieldGroup,
					checkHandler: me.onSortFieldCheckChange
				}, {
					itemId: 'sortField-subject',
					text: me.mys.res('messagesPanel.sortField.subject.lbl'),
					checked: false,
					group: sortFieldGroup,
					checkHandler: me.onSortFieldCheckChange
				}, {
					itemId: 'sortField-date',
					text: me.mys.res('messagesPanel.sortField.date.lbl'),
					checked: false,
					group: sortFieldGroup,
					checkHandler: me.onSortFieldCheckChange
				}, {
					itemId: 'sortField-size',
					text: me.mys.res('messagesPanel.sortField.size.lbl'),
					checked: false,
					group: sortFieldGroup,
					checkHandler: me.onSortFieldCheckChange
				}, {
					itemId: 'sortField-flag',
					text: me.mys.res('messagesPanel.sortField.flag.lbl'),
					checked: false,
					group: sortFieldGroup,
					checkHandler: me.onSortFieldCheckChange
				}, {
					itemId: 'sortField-priority',
					text: me.mys.res('messagesPanel.sortField.priority.lbl'),
					checked: false,
					group: sortFieldGroup,
					checkHandler: me.onSortFieldCheckChange
				}, '-', {
					itemId: 'sortDirection-asc',
					text: me.mys.res('messagesPanel.sortDirection.asc.lbl'),
					checked: false,
					group: sortDirGroup,
					checkHandler: me.onSortDirCheckChange
				}, {
					itemId: 'sortDirection-desc',
					text: me.mys.res('messagesPanel.sortDirection.desc.lbl'),
					checked: false,
					group: sortDirGroup,
					checkHandler: me.onSortDirCheckChange
				}, '-', {
					xtype: 'somenuheader',
					text: me.res('messagesPanel.viewOptions.groupBy.lbl')
				}, {
					itemId: 'groupBy-none',
					text: me.mys.res('messagesPanel.groupBy.none.lbl'),
					checked: false,
					group: groupByGroup,
					checkHandler: me.onGroupByCheckChange
				}, {
					itemId: 'groupBy-date',
					text: me.mys.res('messagesPanel.groupBy.date.lbl'),
					checked: false,
					group: groupByGroup,
					checkHandler: me.onGroupByCheckChange
				}, {
					itemId: 'groupBy-from',
					text: me.mys.res('messagesPanel.groupBy.from.lbl'),
					checked: false,
					group: groupByGroup,
					checkHandler: me.onGroupByCheckChange
				}, {
					itemId: 'groupBy-to',
					text: me.mys.res('messagesPanel.groupBy.to.lbl'),
					checked: false,
					group: groupByGroup,
					checkHandler: me.onGroupByCheckChange
				}, {
					itemId: 'groupBy-thread',
					text: me.mys.res('messagesPanel.groupBy.thread.lbl'),
					checked: false,
					group: groupByGroup,
					checkHandler: me.onGroupByCheckChange
				}],
				listeners: {
					beforeshow: function(s) {
						var sto = me.store,
								meta = sto.getProxy().getReader().metaData,
								sortField = (meta && meta.sortInfo) ? meta.sortInfo.property : null,
								sortDir = (meta && meta.sortInfo) ? meta.sortInfo.direction : null,
								groupField = meta ? meta.groupField : null,
								itm;
						
						sortField = sortField || 'date';
						sortDir = sortDir || me.defaultFieldDirection(sortField);
						
						me.skipViewOptionsCheckChange++;
						if (!sto.isLoading()) {
							itm = s.getComponent('sortField-' + me.sortField_field2key(sortField));
							if (itm) itm.setChecked(true);
							itm = s.getComponent('sortDirection-' + me.sortDirection_dir2key(sortDir));
							if (itm) itm.setChecked(true);
							itm = s.getComponent('groupBy-' + me.groupField_field2key(groupField));
							if (itm) itm.setChecked(true);
						}
						me.skipViewOptionsCheckChange--;
					}
				}
			}
		}, ' ');

		if (me.showToolbar) {
			me.addDocked({
				xtype: 'toolbar',
				dock: 'top',
				border: false,
				referenceHolder: true,
				bodyStyle: {
					borderTopColor: 'transparent'
				},
				overflowHandler: 'scroller',
				items: tbitems
			});			
		}
    },
	
	evaluateActAsSent: function() {
		var me = this, node;
		if (me.currentAccount && me.currentFolder) {
			node = me.mys.getFolderNodeById(me.currentAccount, me.currentFolder);
			if (node && (node.data.isSent||node.data.isUnderSent)) return true;
		}
		return false;
	},
	
	sortField_key2field: function(key) {
		if (key === 'readStatus') {
			return 'unread';
		} else if (key === 'hasAtts') {
			return 'atts';
		} else {
			return key;
		}
	},
	
	sortField_field2key: function(field) {
		if (field === 'unread') {
			return 'readStatus';
		} else if (field === 'atts') {
			return 'hasAtts';
		} else {
			return field;
		}
	},
	
	sortDirection_key2dir: function(key) {
		return key.toUpperCase();
	},
	
	sortDirection_dir2key: function(dir) {
		return dir.toLowerCase();
	},
	
	defaultFieldDirection: function(field) {
		return ['date', 'size', 'atts'].indexOf(field) !== -1 ? 'DESC' : 'ASC';
	},
	
	groupField_key2field: function(key) {
		if (key === 'none') {
			return null;
		} else if (key === 'date') {
			return 'gdate';
		} else if (key === 'thread') {
			return 'threadId';
		} else {
			return key;
		}
	},
	
	groupField_field2key: function(field) {
		if (!field) {
			return 'none';
		} else if (field === 'gdate') {
			return 'date';
		} else if (field === 'threadId') {
			return 'thread';
		} else {
			return field;
		}
	},
	
	onPreviewModeCheckChange: function(s, checked) {
		var me = this, key;
		if (!me.skipViewOptionsCheckChange && checked) {
			key = Sonicle.String.removeStart(s.getItemId(), 'previewMode-');
			me.mys.messagesPanel.setPreviewLocation(key);
		}
	},
	
	onSortFieldCheckChange: function(s, checked) {
		var me = this, key, field;
		if (!me.skipViewOptionsCheckChange && checked) {
			key = Sonicle.String.removeStart(s.getItemId(), 'sortField-');
			if (key !== 'date' && me.store.getGroupField()) {
				me.changeGrouping(me.groupField_key2field('none'));
			}
			// Sort direction will follow the best option for the choosen field;
			// beforeshow will keep sortDirection value updated accordingly.
			field = me.sortField_key2field(key);
			me.store.sort(field, me.defaultFieldDirection(field));
		}
	},
	
	onSortDirCheckChange: function(s, checked) {
		var me = this, key;
		if (!me.skipViewOptionsCheckChange && checked) {
			key = Sonicle.String.removeStart(s.getItemId(), 'sortDirection-');
			var sto = me.store,
					meta = sto.getProxy().getReader().metaData,
					field = (meta && meta.sortInfo) ? meta.sortInfo.property : null;
			field = field || 'date';
			sto.sort(field, me.sortDirection_key2dir(key)); 
		}
	},
	
	onGroupByCheckChange: function(s, checked) {
		var me = this, key;
		if (!me.skipViewOptionsCheckChange && checked) {
			key = Sonicle.String.removeStart(s.getItemId(), 'groupBy-');
			me.changeGrouping(me.groupField_key2field(key));
		}
	},
	
	setCurrentAccount: function(acct) {
		var me=this,
			plugin=me.getView().getPlugin("messagegridviewdragdrop");
		me.currentAccount=acct;
		me.readonly=acct==="archive";
	},
	
	collapseClicked: function(rowIndex) {
		var me=this;
		
		me.collapseThread(me.store.getAt(rowIndex));
	},
	
	collapseThread: function(r) {
		var me=this,
			sel=me.getSelection();
	
		if (sel) {
			//var recindex=me.store.indexOf(sel[0]);
			me.selectOnRefresh(sel);
		}
		me.store.reload({ 
			params: {
				threadaction: r.get("threadOpen")?'close':'open',
				threadactionuid: r.get("idmessage"),
				timestamp: Date.now()
			}
		});
	},
	
	//sel must be an array of records
	selectOnRefresh: function(sel) {
		var me=this;
		me.selectOnLoad=sel;
		me.view.on('refresh',function() {
			if (me.selectOnLoad) {
				var sm=me.getSelectionModel(),s=me.getStore(),lastrec=null;;
				sm.deselectAll(true);
				Ext.each(me.selectOnLoad, function(rec) {
					sm.select(lastrec=s.findRecord("idmessage",rec.get("idmessage")),true);
				});
				if (lastrec && me.amIFocused()) me.view.focusRow(lastrec);
				delete me.selectOnLoad;
			}
		},me,{ single: true });
	},
	
	setPageSize: function(size) {
/*		var me=this;
		me.pageSize=size;
		if (me.store) me.store.setPageSize(size);*/
	},
	
	getPageSize: function() {
		return this.pageSize;
	},
	
	reloadFolder: function(acct,folder_id, config, uid, rid, page, tid, isReadOnly){
		config = config || {};
		if (!folder_id) return;
		var me = this,
			folderChanged = folder_id !== me.currentFolder,
			proxy = me.store.getProxy();
	
		//me.setPageSize(this.ms.pageRows);
		//console.log("reloadFolder "+folder_id+" pageSize="+this.ptoolbar.pageSize);
		Ext.applyIf(config, {
			start: 0,
			limit: me.pageSize,
			refresh: 1,
			account: acct,
			folder: folder_id,
			timestamp: Date.now()
		});

		//TODO: sort info && threaded
		//if (!Ext.isDefined(config.threaded)) config.threaded=2;
        //this.store.sortInfo=null;
		//TODO: baseParams??
		//this.store.baseParams = {service: 'mail', action: 'ListMessages', folder: folder_id};
		proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), config));


		var groupField=me.mys.getFolderGroup(acct,folder_id),
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
		
		me.setCurrentAccount(acct);
		me.currentFolder = folder_id;
		//me.initFolderState(!WT.plTags.desktop);
		if (folderChanged) {
			me.reconfigure(me.buildColumnsCfg(me.compactView, me.evaluateActAsSent(), me.multifolder));
		}
		me.getStore().clearFilter(true);
		if (uid) {
			me.autoselectUid=uid;
			me.autoselectRow=rid;
			me.autoselectPage=page;
			me.autoselectTid=tid;
		} else {
			me.autoselectUid=null;
			me.autoselectPage=null;
			me.autoselectTid=null;
		}
		s.load();
		me.readonly = isReadOnly;
	},
	
	updateCompactView: function(nv, ov) {
		var me = this;
		me.setHideHeaders(nv);
		//if (me.rendered) me.initFolderState(true);
		if (me.rendered) {
			me.reconfigure(me.buildColumnsCfg(me.compactView, me.evaluateActAsSent(), me.multifolder));
		}
	},
	
	getState: function() {
		var state = this.callParent();
		delete state.storeState;
		return state;
	},
	
	/*
	getState: function() {
		var me = this,
				//state = me.callParent(),
				state = {},
				cols = me.getColumns(),
				scols = new Array(cols.length);
		
		delete state.storeState;
		delete state.columns;
		
		Ext.each(cols, function(itm, idx) {
			scols[idx] = {
				id: itm.stateId,
				width: itm.width,
				hidden: itm.hidden
			};
		});
		
		return Ext.apply(state, {
			columns: scols
		});
	},
	*/
	
    /**
     * Initializes the state of the object upon construction.
     * @private
     */
    /*
	initFolderState: function(reset){
        var me = this,
            id = me.getStateId(),
            hasListeners = me.hasListeners,
            state,
            combinedState,
			defaultState,
            i, len,
            plugins,
            plugin,
            pluginType;
 
        if (id) {
            if (reset) Ext.state.Manager.clear(id);
			else combinedState = Ext.state.Manager.get(id);
			console.log(combinedState);
            if (combinedState) {
                state = Ext.apply({}, combinedState);
				defaultState=false;
            } else {
				state=me.createDefaultState();
				defaultState=true;
			}
			
			var node=me.mys.getFolderNodeById(me.currentAccount,me.currentFolder),
				issentfolder=node?(node.data.isSent||node.data.isUnderSent):false;

			if (state.storeState) {
				delete state.storeState.sorters;
				delete state.storeState.filters;
				delete state.storeState.grouper;
			}
			if (defaultState) {
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
			}
			
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
			
			if (!me.getCompactView()) {
				console.log(state);
				me.applyState(state);
				if (hasListeners.staterestore) {
					me.fireEvent('staterestore', me, state);
				}
			}
			me.folderStateInitialized=true;
        }
    },
	*/
	
	//add hidden only if not already set and new state true
	//if already set and false, delete it
	_updateColHiddenState: function(col,hidden) {
		//if (!Ext.isDefined(col.hidden) && hidden) col.hidden=hidden;
		//else if (!col.hidden) delete col.hidden;
		if (!Ext.isDefined(col.hidden)) col.hidden=hidden;
		else if (!col.hidden) delete col.hidden;
	},
 	
	updateTotals: function(total,realTotal,quotaLimit,quotaUsage) {
		var me=this;
		me.msgsTotal=total;
		me.msgsRealTotal=realTotal;
		me.fireEvent('totals',me,me.msgsTotal,me.msgsRealTotal,quotaLimit,quotaUsage);
	},
	
    loaded: function(s,r,o) {
		var me=this,
			//meta=s.getProxy().getReader().metaData,
			json=s.proxy.reader.rawData;

		if (json) { 
			
			/*
			var tba=me.getDockedItems('toolbar[dock="top"]');
			var tb=tba.length>0? tba[0]:null;
			if (tb) {
				var gf=json.metaData?json.metaData.groupField:null;
				if (!gf || gf==='') gf='none';
				var itm=tb.lookupReference('itmgmg'+gf);
				if (itm) itm.setChecked(true);

				var sf=json.metaData?json.metaData.sortInfo.property:'date';
				itm=tb.lookupReference('itmsmg'+sf);
				if (itm) itm.setChecked(true);

				var dir=json.metaData?json.metaData.sortInfo.direction:'ASC';
				itm=tb.lookupReference('itmdsmg'+((dir==='DESC')?'desc':'asc'));
				if (itm) itm.setChecked(true);
			}
			*/
			
			me.updateTotals(json.total,json.realTotal,json.quotaLimit,json.quotaUsage);
			me.fireEvent('load',me,me.currentFolder,json);
		}
		
		//autoselect
		if (me.autoselectUid) {
			me.ensureVisible(me.autoselectRow, {
				animate: true,
				highlight: true,
				select: false,
				callback: function(success,rec,node) {
					if (success) {
						if (me.autoselectTid) {
							var fr=me.store.findRecord('idmessage',me.autoselectUid);
							//if rec is in store, thread is already open, all is fine
							if (fr) {
								Ext.defer(function() { me.getSelectionModel().select(fr); },1000);
								me.autoSelectUid=null;
								me.autoSelectPage=null;
								me.autoSelectTid=null;
							} else {
								//if not open thread, causing another try with thread open
								fr=me.store.findRecord('idmessage',me.autoselectTid);
								Ext.defer(function() { me.collapseThread(fr); },1000);
							}
						} else {
							me.getSelectionModel().select(rec);
							me.autoSelectUid=null;
							me.autoSelectPage=null;
							me.autoSelectTid=null;
						}
					} else {
						console.log("error during ensureVisibile");
						me.autoSelectUid=null;
						me.autoSelectPage=null;
						me.autoSelectTid=null;
					}
				}
			});			
		}
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
		
		var fbar = me.mys.hasAudit() ?
			Ext.create('Ext.toolbar.Toolbar', {
				padding: '5 5 5 5',
				items: [
					'->',
					me.mys.getAct("auditRead"),
					me.mys.getAct("auditReplied"),
					me.mys.getAct("auditForwarded"),
					me.mys.getAct("auditPrinted"),
					me.mys.getAct("auditTagged"),
					me.mys.getAct("messageAuditLog")
				]
			})
			: null;
		
		var win = WT.createView(me.mys.ID,'view.DockableMessageView', {
			viewCfg: {
				mys: me.mys,
				acct: me.currentAccount,
				folder: r.get('folder') || me.currentFolder,
				idmessage: r.get('idmessage'),
				title: r.get('subject'),
				model: r,
				messageGrid: me,
				fbar: fbar
			}
		});
		win.show(false, function() {
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
		var me=this;
        me.replyMessageById(me.currentAccount,r.get('folder')||me.currentFolder,r.get("idmessage"),all);
	},
	
	replyMessageById: function(acct,idfolder,idmessage,all) {
        var me=this;
        
		WT.ajaxReq(me.mys.ID, 'GetReplyMessage', {
			params: {
				account: acct,
				folder: idfolder,
				idmessage: idmessage,
				replyall: (all?'1':'0')
			},
			callback: function(success,json) {
				if (json.success) {
					var data = json.data;
					me.mys.startNewMessage(idfolder,{
						subject: data.subject,
						recipients: data.recipients,
						content: data.content,
						attachments: data.attachments,
                        format: data.format,
						replyfolder: data.replyfolder,
						inreplyto: data.inreplyto,
						references: data.references,
						origuid: data.origuid
					});
				} else {
					WT.error(json.message);
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
        me.forwardMessage(recs, eml);
    },
	
	forwardMessage: function(recs, eml) {
		var me=this,
				messagesIds = [];
		
		recs.forEach(function(element) {
			messagesIds.push(element.get('idmessage'));
		});
				
		
        me.forwardMessageById(me.currentAccount, recs[0].get('folder')||me.currentFolder, messagesIds, eml);
	},
	
	forwardMessageById: function(acct, idfolder, messageIds, eml) {
		var me = this, msgId = Sonicle.webtop.mail.view.MessageEditor.buildMsgId();
		
		WT.ajaxReq(me.mys.ID, 'GetForwardMessage', {
			params: {
				account: acct,
				folder: idfolder,
				messageIds: messageIds,
				attached: eml?1:0,
				newmsgid: msgId
			},
			callback: function(success,json) {
				if (json.success) {
					var data = json.data;
					me.mys.startNewMessage(idfolder,{
						subject: data.subject,
						content: data.content,
                        format: data.format,
						msgId: msgId,
						attachments: data.attachments,
						forwardedfolder: data.forwardedfolder,
						forwardedfrom: data.forwardedfrom,
						inreplyto: data.inreplyto,
						references: data.references,
						origuid: data.origuid
					});
				} else {
					WT.error(json.message);
				}
			}
		});					
	},
	
	actionEditSubject: function(rowIndex) {
        this._actionEditSubject(rowIndex);
    },
	
	_actionEditSubject: function(rowIndex) {
        var me=this,
			recs=(rowIndex>=0)?
				me.getStore().getAt(rowIndex):
				me.getSelection();
        me.editSubject(recs[0]);
    },
	
	editSubject: function(email) {
		var me = this;
		var messageId = email.data.idmessage;
		var subject = email.data.subject;
		var currentFolder=me.currentFolder;
		
		 WT.prompt('',{
			title: me.res("act-editSubject.lbl"),
			value: subject,
			fn: function(btn,value) {
				if (btn==="ok" && value && value!=="") me.changeSubject(messageId, currentFolder, value);
			}
		});
	},
	
	changeSubject: function(messageId, currentFolder, newSubject) {
		var me = this;
		
		WT.ajaxReq(me.mys.ID, 'EditEmailSubject', {
			params: {
                messageId: messageId,
				currentFolder: currentFolder,
				subject: newSubject
			},
			callback: function(success,json) {
				if (success) {
					me.mys.reloadFolderList();
				} else {
					WT.error(json.message);
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
    
    editMessage: function(r,isdraft) {
		var me=this;
        me.editMessageById(me.currentAccount,r.get('folder')||me.currentFolder,r.get("idmessage"),isdraft);
    },
    
    editMessageById: function(acct,idfolder,idmessage,isdraft) {
        var me=this,msgId=Sonicle.webtop.mail.view.MessageEditor.buildMsgId();
        
		WT.ajaxReq(me.mys.ID, 'GetEditMessage', {
			params: {
				account: acct,
				folder: idfolder,
				idmessage: idmessage,
                newmsgid: msgId
			},
			callback: function(success,json) {
				if (json.success) {
					var data = json.data;
					me.mys.startNewMessage(idfolder,{
						subject: data.subject,
						recipients: data.recipients,
						content: data.content,
                        contentReady: true,
						identityId: data.identityId,
                        proprity: data.priority,
						replyfolder: data.replyfolder,
                        format: data.format,
                        msgId: msgId,
						attachments: data.attachments,
						forwardedfolder: data.forwardedfolder,
						forwardedfrom: data.forwardedfrom,
						inreplyto: data.inreplyto,
						references: data.references,
						origuid: data.origuid,
						draftuid: isdraft?idmessage:0,
						draftfolder: isdraft?idfolder:null
					});
					if (data.deleted && data.folder === me.mys.currentFolder) {
						me.mys.reloadFolderList();
						me.mys.messagesPanel.clearMessageView();
					}
				} else {
					WT.error(json.message);
				}
			}
		});					
    },
	
    actionDeletePermanently: function() {
		if (this.storeLoading || this.readonly) {
			return;
		}
		
		var me=this,
			acct=me.currentAccount,
			curfolder=me.currentFolder,
			sm=me.getSelectionModel(),
			selection=sm.getSelection();
	
		if (!selection || selection.length==0) return;
		
		me.deleteSelection(acct,curfolder,selection);
		me.focus();
    },	
	
    actionDelete: function() {
		if (this.storeLoading || this.readonly) {
			return;
		}
		
		var me=this,
			acct=me.currentAccount,
			curfolder=me.currentFolder,
			sm=me.getSelectionModel(),
			selection=sm.getSelection(),
			ftrash=me.mys.getFolderTrash(acct);
	
		if (!selection || selection.length==0) return;
		
        if (ftrash) {
			if (!me.multifolder && me.mys.isTrash(acct,curfolder)) {
				//TODO: warning
				WT.confirm(me.res('sureprompt'),function(bid) {
					if (bid==='yes') {
						me.deleteSelection(acct,curfolder,selection);
					}
					me.focus();
				},me);
          } else {
              me.moveSelection(acct,curfolder,acct,ftrash,selection);
              me.focus();
          }
        }
    },	
	
    actionSpam: function() {
		if (this.storeLoading || this.readonly) {
			return;
		}
		
		var me=this,
			acct=me.currentAccount,
			curfolder=me.currentFolder,
			sm=me.getSelectionModel(),
			selection=sm.getSelection(),
			fspam=me.mys.getFolderSpam();
	
		if (!selection || selection.length==0) return;
		
        if (fspam) {
			me.moveSelection(acct,curfolder,acct,fspam,selection);
			me.focus();
        }
    },

    actionArchive: function() {
		if (this.storeLoading || this.readonly) {
			return;
		}
		
		var me=this,
			acct=me.currentAccount,
			curfolder=me.currentFolder,
			sm=me.getSelectionModel(),
			selection=sm.getSelection(),
			farchive=me.mys.getFolderArchive();
	
		if (!selection || selection.length==0) return;
		
        if (farchive) {
			me.moveSelection(acct,curfolder,acct,farchive,selection);
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
			me.currentAccount,
			me.currentFolder,
			me.getSelectionModel().getSelection(),
			seen
		);
		me.focus();
	},
	
	actionMoveToFolder: function() {
		var me=this;
		if (this.readonly) return;
		
//		if (!me.mcmdialog) {
			me.mcmdialog=WT.createView(me.mys.ID,'view.MoveCopyMessagesDialog',{
				viewCfg: {
					mys: me,
					account: me.currentAccount,
					multifolder: me.multifolder,
					fromFolder: me.currentFolder,
					grid: me
				}
			});
//		} else {
//			me.mcmdialog.getView().fromFolder=me.currentFolder;
//			me.mcmdialog.getView().grid=me;
//		}
		
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
	
    deleteSelection: function(acct,from,selection) {
        var me=this,data=me.sel2ids(selection);
		data.cb=function(result) {
			if (result) {
				me.removeRecords(data.ids);
			}
		}
        me.deleteMessages(acct,from,data,selection);
    },

    deleteMessages: function(acct,folder,data,selection) {
		var me=this;
		me.checkBrokenThreads(selection,function(fullThreads) {
			me._deleteMessages(acct,folder,data,fullThreads);
		});
	},
	
    _deleteMessages: function(acct,folder,data,fullThreads) {
		var me=this;
			me.fireEvent('deleting',me);
		WT.ajaxReq(me.mys.ID, 'DeleteMessages', {
			params: {
				account: acct,
				fromfolder: folder,
				ids: data.ids,
				multifolder: data.multifolder,
				fullthreads: fullThreads
			},
			callback: function(success,json) {
				if (json.success) {
					Ext.callback(data.cb,data.scope||me,[json.success]);
				} else {
					WT.error(json.message);
				}
			}
		});					
    },

    deleteMessage: function(acct,folder,idmessage,dview) {
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
		
		if (me.mys.isTrash(acct,curfolder)) {
			//TODO: warning
			WT.confirm(me.res('sureprompt'),function(bid) {
				if (bid==='yes') {
					me.deleteMessages(acct,folder,data);
					dview.closeView();
				}
			});
		} else {
			me.moveMessages(acct,folder,acct, ftrash, data);
			dview.closeView();
		}
    },	
	
    moveSelection: function(acctfrom,from,acctto,to,selection,isdd) {
        var me=this, 
            data=me.sel2ids(selection);
		data.cb=function(result) {
			if (result) {
				me.removeRecords(data.ids);
			}
		}
        me.moveMessages(acctfrom,from,acctto,to,data,selection,isdd);
    },
	
    copySelection: function(acctfrom,from,acctto,to,selection,isdd) {
        var me=this, 
            data=me.sel2ids(selection);
        me.copyMessages(acctfrom,from,acctto,to,data,selection,isdd);
    },
	
    moveMessages: function(acctfrom,from,acctto,to,data,selection,isdd) {
		var me=this;
		
        me.fireEvent('moving',me);
		
        me.operateMessages("MoveMessages",acctfrom,from,acctto,to,data,selection,isdd);
    },	
	
    copyMessages: function(acctfrom,from,acctto,to,data,selection,isdd) {
        this.operateMessages("CopyMessages",acctfrom,from,acctto,to,data,selection,isdd);
    },

	//TODO: customer,causal
	//operateMessages: function(action,from,to,data,customer_id,causal_id) {
    operateMessages: function(action,acctfrom,from,acctto,to,data,selection,isdd) {
		var me=this;
		
		me.checkBrokenThreads(selection,function(fullThreads) {
			me._operateMessages(action,acctfrom,from,acctto,to,data,fullThreads,isdd);
		});
    },
	
	_operateMessages: function(action,acctfrom,from,acctto,to,data,fullThreads,isdd) {
		var me=this;
		WT.ajaxReq(me.mys.ID, action, {
			params: {
				//customer_id:customer_id,
				//causal_id:causal_id
				fromaccount: acctfrom,
				fromfolder: from,
				toaccount: acctto,
				ids: data.ids,
				tofolder: to,
				multifolder: data.multifolder,
				fullthreads: fullThreads,
				isdd: isdd
			},
			callback: function(success,json) {
				Ext.callback(data.cb,data.scope||me,[json.success]);
				if (json.success) {
					/*var tonode=me.mys.getFolderNodeById(acctto,to),
						unseen=0;
				
					for(var i=0;i<data.seen.length;++i)
						if (!data.seen[i]) ++unseen;
					
					if (unseen) {
						me.mys.unreadChanged({
							accountid: acctto,
							foldername: to,
							unread: tonode.get('unread')+unseen
						},true)
					}*/
					
				    //if archiving, reload archive branch
					if (json.data.archiving) me._refreshArchiveNode(acctto,json.data.tofolder);
				} else {
					WT.error(json.message);
				}
			}
		});							
	},
	
	_refreshArchiveNode: function(acct,fname) {
		var me=this,
			tree=me.mys.acctTrees[acct],
			s=tree.getStore(),
			n=s.getNodeById(fname);
		if (!n) {
			//look for parent to reload (e.g. shared folder)
			var x=fname.lastIndexOf(me.mys.getVar("folderSeparator"));
			if (x>0) n=s.getNodeById(fname.substring(0,x));
			if (!n) me.mys.reloadTree();
			else s.load({ node: n });
		} else {
			var expanded=n.isExpanded();
			s.load({
				node: n,
				callback: function() {
					if (!expanded) {
						if (n) n.expand(false,function() {
							if (n)
								Ext.defer(function() { n.collapse(); },500);
						});
					}
				}
			});
		}
		
	},

	checkBrokenThreads: function(selection,cb,scope) {
		var me=this;
		if (me.threaded) {
			var brokenThread=false;
			Ext.each(
			  selection,
			  function(r,index,allItems) {
				  if (r.get("threadIndent")===0 && r.get("threadHasChildren")) {
						brokenThread=true;
						return false;
				  }
			  }
			);
			if (brokenThread) {
				Ext.Msg.show({
					title: WT.res('warning'),
					msg: me.res("confirm.full-threads"),
					buttons: Ext.MessageBox.YESNOCANCEL,
					buttonText: {
						yes: WT.res('word.yes'),
						no: WT.res('word.no'),
						cancel: WT.res('act-cancel.lbl')
					},
					icon: Ext.Msg.QUESTION,
					fn: function(bid) {
						if(bid === 'cancel') return;
						Ext.callback(cb,scope||me,[bid==='yes']);
					}
				});
				return;
			}
		}
		Ext.callback(cb,scope||me,false);
	},
		
    operateAllFiltered: function(action,acct,from,to,handler,scope) {
		var me=this,oparams=me.store.proxy.extraParams,
			params={
				action: action,
				fromaccount: acct,
				toaccount: acct,
				account: acct,
				fromfolder: from,
				tofolder: to,
				allfiltered: true,
				isdd: false
			};
		if (oparams.sort) {
			params.sort=oparams.sort.property;
			params.dir=oparams.sort.dir;
		}
        if (oparams.query) params.query=oparams.query;
		
		WT.ajaxReq(me.mys.ID, action, {
			timeout: WT.getVar("ajaxLongTimeout"),
			params: params,
			callback: function(success,json) {
				if (handler) Ext.callback(handler,scope||me,[json.success]);
				if (json.success) {
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
					if (json.data.archiving) me._refreshArchiveNode(acct,json.data.tofolder);
				} else {
					WT.error(json.message);
				}
			}

		});							
    },	
	
    markSelectionSeenState: function(acct,from,selection,seen) {
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
        me.markMessagesSeenState(acct,from,data,seen);
    },
    
    markMessageSeenState: function(r,seen) {
        var me=this,
			acct=me.currentAccount,
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
        me.markMessagesSeenState(acct,folder,data,seen);
    },

    markMessagesSeenState: function(acct,folder,data,seen) {
		var me=this;
		WT.ajaxReq(me.mys.ID, seen?"SeenMessages":"UnseenMessages", {
			params: {
				account: acct,
				fromfolder: folder,
				ids: data.ids,
				multifolder: data.multifolder
			},
			callback: function(success,json) {
				Ext.callback(data.cb,data.scope||me,[json.success]);
				if (!json.success)
					WT.error(json.message);
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
		var me=this;
		if (r && r.get("unread")===seen) {
			r.set("unread",!seen);
			var st=r.get("status");
			if (seen) {
				if (st==="unread"||st==="new") r.set("status","read");
			} else {
				if (st==="read") r.set("status","unread");
			}
			var row=me.getView().getRow(r);
			if (row && row.parentElement) {
				var rowbody=row.parentElement.querySelector(".x-grid-rowbody-tr"),
					cls="x-grid-rowbody-tr ";
				if (rowbody) rowbody.className+=seen?cls+" wtmail-row-body-hidden":cls+" wtmail-row-body";
			}
			//TODO: not needed with websocket?
			//var o=s.getProxy().getReader().rawData;
			//o.millis=millis;
			//o.unread--;
			//me.mys.updateUnreads(me.currentFolder,o,false);
		}
	},
	
	actionTag: function(tagId) {
        var me=this,
			selection=me.getSelection(),
			folder=me.currentFolder,
			data=me.sel2ids(selection),
			ids=data.ids,
			params={
                tagId: tagId,
                fromfolder: folder,
                ids: ids,
                multifolder: data.multifolder
			};
			
        if (data.folders) params.folders=data.folders;
		
		if(!me.readonly) {
		  WT.ajaxReq(me.mys.ID, 'TagMessages', {
			params: params,
			callback: function(success,json) {
              if (success) {
                  var dorel=false,
					  fl=me.mys.messagesPanel.folderList;
			  
                  Ext.each(
                    selection,
                    function(r,index,allItems) {
                      if (me!==fl) {
						var ff=(me.multifolder?r.get("folder"):me.currentFolder);
                        if (ff===fl.currentFolder) dorel=true;
                      }
					  
					  var tags=r.get("tags"),
						  ix=-1;
					  if (!tags) tags=[];
					  else ix=Ext.Array.indexOf(tags,tagId);
					  if (ix<0) {
						  Ext.Array.insert(tags,0,[tagId]);
					  }
					  r.set("tags",null);
					  r.set("tags",tags);
					  
					  me._checkUpdateMessageView(r);
                    }
                  );
                  if (dorel) this.mys.reloadFolderList();

              } else {
                  WT.error(json.message);
              }
			}
		  });
		}
		else {
			WT.error(me.mys.res('mail.permission.denied'));
		}
	},
	
	actionApplyTags: function(tagIds) {
        var me=this,
			selection=me.getSelection(),
			folder=me.currentFolder,
			data=me.sel2ids(selection),
			ids=data.ids,
			params={
                fromfolder: folder,
                ids: ids,
                multifolder: data.multifolder,
				tagIds: tagIds
			};
			
        if (data.folders) params.folders=data.folders;
		
		if(!me.readonly) {
		  WT.ajaxReq(me.mys.ID, 'ApplyMessagesTags', {
			params: params,
			callback: function(success,json) {
              if (success) {
                  var dorel=false,
					  fl=me.mys.messagesPanel.folderList;
			  
                  Ext.each(
                    selection,
                    function(r,index,allItems) {
                      if (me!==fl) {
                        var ff=(me.multifolder?r.get("folder"):me.currentFolder);
                        if (me.multifolder && ff===fl.currentFolder) dorel=true;
                      }
					  
					  r.set("tags",tagIds);
					  me._checkUpdateMessageView(r);
                    }
                  );
                  if (dorel) me.mys.reloadFolderList();

              } else {
                  WT.error(json.messages);
              }
			}
		  });
		}
		else {
			WT.error(me.mys.res('mail.permission.denied'));
		}
	},
	
	actionUntag: function(tagId) {
        var me=this,
			selection=me.getSelection(),
			folder=me.currentFolder,
			data=me.sel2ids(selection),
			ids=data.ids,
			params={
                tagId: tagId,
                fromfolder: folder,
                ids: ids,
                multifolder: data.multifolder
			};
			
        if (data.folders) params.folders=data.folders;
		if(!me.readonly) {
		  WT.ajaxReq(me.mys.ID, 'UntagMessages', {
			params: params,
			callback: function(success,json) {
              if (success) {
                  var dorel=false,
					  fl=me.mys.messagesPanel.folderList;
			  
                  Ext.each(
                    selection,
                    function(r,index,allItems) {
                      if (me!==fl) {
                        var ff=(me.multifolder?r.get("folder"):me.currentFolder);
                        if (ff===fl.currentFolder) dorel=true;
                      }
					  
					  var tags=r.get("tags");
					  if (tags) {
						Ext.Array.remove(tags,tagId);
						r.set("tags",null);
						if (tags.length>0) r.set("tags",tags);
						me._checkUpdateMessageView(r);
					  }
                    }
                  );
                  if (dorel) this.mys.reloadFolderList();

              } else {
                  WT.error(json.message);
              }
			}
		  });
		}
		else {
			WT.error(me.mys.res('mail.permission.denied'));
		}
	},
	
	_checkUpdateMessageView: function(r) {
		var me=this,
			mp=me.mys.messagesPanel,
			mv=mp.messageView;
			ff=(me.multifolder?r.get("folder"):me.currentFolder),
			acct=me.currentAccount;
			idmessage=r.get("idmessage");
	
		if (mv.folder===ff && mv.idmessage===idmessage) {
			mp.clearMessageView();
			mp.showMessage(acct,ff,idmessage);
		}
	},
	
    actionFlag: function(flagstring) {
        var me=this,
			selection=me.getSelection(),
			acct=me.currentAccount,
			folder=me.currentFolder,
			data=me.sel2ids(selection),
			ids=data.ids,
			params={
                flag: flagstring,
				account: acct,
                fromfolder: folder,
                ids: ids,
                multifolder: data.multifolder
			};
			
        if (data.folders) params.folders=data.folders;
		
		if(!me.readonly) {
		  WT.ajaxReq(me.mys.ID, 'FlagMessages', {
			params: params,
			callback: function(success,json) {
              if (success) {
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
                  WT.error(json.message);
              }
			}
		  });
		}
		else {
			WT.error(me.mys.res('mail.permission.denied'));
		}		
    },
	
	actionAddNote: function() {
		var me=this,r=me.getSelectionModel().getSelection()[0],
			acct=me.currentAccount;
	
		me.editNote(acct,r.get('idmessage'),r.get("folder")||me.currentFolder);
	},
	
    editNote: function(acct,id,folder) {
		var me=this;
		WT.ajaxReq(me.mys.ID, 'GetMessageNote', {
			params: {
				account: acct,
                folder: folder,
                idmessage: id
			},
			callback: function(success,json) {
				if (success) {
					WT.prompt('',{
						title: me.res("mailnote"),
						fn: function(btn,text) {
							if (btn=='ok') {
								me.saveNote(acct,folder,id,text);
							}
						},
						scope: me,
						width: 400,
						multiline: 200,
						value: json.message
					});
				} else {
					WT.error(json.message);
				}
			}
		});					
    },
    
    saveNote: function(acct,folder,id,text) {
		var me=this;
		WT.ajaxReq(me.mys.ID, 'SaveMessageNote', {
			params: {
				account: acct,
                folder: folder,
                idmessage: id,
                text: text
			},
			callback: function(success,json) {
				if (success) {
					if (folder===me.currentFolder)
						me.mys.reloadFolderList();
				} else {
					WT.error(json.message);
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
		var me=this,
			r=me.getSelectionModel().getSelection()[0],
			acct=me.currentAccount;
	
		me.saveMail(acct,r.get('idmessage'),r.get("folder")||this.currentFolder);
	},
	
	saveMail: function(acct,id,folder) {
        var params={
			account: acct,
            folder: folder,
            id: id
        };
		
        var url=WTF.processBinUrl(this.mys.ID,"SaveMail",params);;
        window.open(url);
	},
	
	/*
    actionResetColumns: function() {
        this.initFolderState(true);
		this.proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), { timestamp: Date.now() }));
		this.store.load();
    },
	*/

    actionViewHeaders: function() {
        this.actionViewSource(true);
    },

    actionViewSource: function(headers) {
        var me=this,
			r=me.getSelectionModel().getSelection()[0],
			acct=me.currentAccount;
	
		WT.ajaxReq(me.mys.ID, 'GetSource', {
			params: {
				account: acct,
                folder: r.get('folder')||me.currentFolder,
                id: r.get('idmessage'),
                headers: !!headers
			},
			callback: function(success,json) {
				if (json.success) {
					WT.createView(me.mys.ID,'view.HeadersView',{
						viewCfg: {
							source: json.source
						}
					}).show();

				} else {
					WT.error(json.message);
				}
			}
		});					
	
    },
	
	getMessageAuditLog: function(r, action, text, dataToStringFunction) {
		var me = this,
				context = 'MAIL';
		
		WT.ajaxReq(me.mys.ID, 'GetMessageId', {
			params: {
				account: me.currentAccount,
                folder: r.get('folder') || me.currentFolder,
                idmessage: r.get('idmessage')
			},
			callback: function(success, json) {
				if (success) {
					var messageId = json.message;
					WT.getServiceApi(WT.ID).showAuditLog(me.mys.ID, context, action, messageId, dataToStringFunction, {
						text: text
					});
				} else {
					WT.error(json.message);
				}
			}
		});					
	},
	
	actionAuditRead: function() {
		var me = this,
				r = me.getSelectionModel().getSelection()[0],
				action = 'VIEW';
	
		me.getMessageAuditLog(r, action, me.res('act-auditRead.lbl'), function(data) {
			var str = '';
			Ext.each(data, function(el) {
				str += me.mys.auditBaseStringFormat(el);
				str += me.mys.auditMessageDetailsFormat(el.data, action);
			});
			return str;
		});
	},
	
	actionAuditReplied: function() {
		var me = this,
				r = me.getSelectionModel().getSelection()[0],
				action = 'REPLY';
	
		me.getMessageAuditLog(r, action, me.res('act-auditReplied.lbl'), function(data) {
			var str = '';
			Ext.each(data, function(el) {
				str += me.mys.auditBaseStringFormat(el);
				str += me.mys.auditMessageDetailsFormat(el.data, action);
			});
			return str;
		});		
	},
	
	actionAuditForwarded: function() {
		var me = this,
				r = me.getSelectionModel().getSelection()[0],
				action = 'FORWARD';
	
		me.getMessageAuditLog(r, action, me.res('act-auditForwarded.lbl'), function(data) {
			var str = '';
			Ext.each(data, function(el) {
				str += me.mys.auditBaseStringFormat(el, action);
				str += me.mys.auditMessageDetailsFormat(el.data, action);
			});
			return str;
		});		
	},
	
	actionAuditPrinted: function() {
		var me = this,
				r = me.getSelectionModel().getSelection()[0];
	
		me.getMessageAuditLog(r, 'PRINT', me.res('act-auditPrinted.lbl'), function(data) {
			var str = '';
			Ext.each(data, function(el) {
				str += me.mys.auditBaseStringFormat(el);
			});
			return str;
		});
	},
	
	actionAuditTagged: function() {
		var me = this,
				r = me.getSelectionModel().getSelection()[0],
				action = 'TAG';
	
		me.getMessageAuditLog(r, action, me.res('act-auditTagged.lbl'), function(data) {
			var str = '';
			Ext.each(data, function(el) {
				str += me.mys.auditBaseStringFormat(el);
				str += me.mys.auditMessageDetailsFormat(el.data, action);
			});
			return str;
		});		
	},
	
	actionMessageAuditLog: function() {
		var me = this,
				r = me.getSelectionModel().getSelection()[0];
		
		me.getMessageAuditLog(r, null, null, function(data) {
			var str = '';
			Ext.each(data, function(el) {
				str += me.mys.auditActionBaseStringFormat(el, 'MAIL');
				str += me.mys.auditMessageDetailsFormat(el.data, el.action);
			});
			return str;
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
				account: me.currentAccount,
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
					me.mys.setFolderGroup(me.currentAccount,me.currentFolder,newgroup);
					me.mys.showFolder(me.currentAccount,me.currentFolder);
				} else {
					WT.error(json.message);
				}
			}
		});					
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
	
	getStateId: function() {
		var me = this,
				folder = me.currentFolder || 'INBOX',
				id = me.callParent();
		if (me.getCompactView()) id += '-compact';
		return id + '@' + folder;
	},
	
    /**
     * Override: Gets the state id against the current folder.
     * @return {String} The 'stateId' or the implicit 'id' specified by component configuration.
     * @private
     */
    /*
	getStateId: function() {
        var me = this,
			prefix = (me.baseStateId || (me.baseStateId=me.mys.buildStateId('messagegrid')));
		if (me.getCompactView()) prefix+="-compact";
        return prefix+"@"+me.currentFolder;
    },
	*/
	
	buildColumnsCfg: function(compactView, actAsSent, multiFolder) {
		var me = this,
				ret = [];
		
		ret.push({ // Hidden column for grouping
			dataIndex: 'gdate',
			header: me.res('column-date'),
			hidden: true,
			hideable: false
		});
		
		if (compactView) {
			ret.push({ // Hidden column for grouping
				dataIndex: 'from',
				header: me.res('column-from'),
				hidden: true
			});
			ret.push({ // Hidden column for grouping
				dataIndex: 'to',
				header: me.res('column-to'),
				hidden: true
			});
			
		} else {
			ret.push({ // Hidden column for grouping
				dataIndex: 'sdate',
				header: me.res('column-date'),
				hidden: true,
				hideable: false
			});
			ret.push({ // Hidden column for grouping
				dataIndex: 'xdate',
				header: me.res('column-date'),
				hidden: true,
				hideable: false
			});
		}
		if (multiFolder) {
			ret.push({
				dataIndex: 'folder',
				stateId: 'stid-folder',
				header: '',
				hidden: true,
				sortable: true,
				width: 100
			});
			ret.push({
				dataIndex: 'folderdesc',
				stateId: 'stid-folderdesc',
				header: me.res('column-folder'),
				hidden: false,
				sortable: true,
				renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					return "<span data-qtip='"+record.get('fullfolderdesc')+"'>"+value+"</span>";
				},
				width: 150
			});
		}
			
		if (compactView) {
			ret.push({
				xtype: 'soavatarcolumn',
				sortable: false,
				groupable: false,
				sentMode: actAsSent,
				getName: function(v, rec) {
					var fld = this.sentMode ? 'to' : 'from';
					return Ext.isEmpty(rec.get(fld)) ? rec.get(fld+'email') : rec.get(fld);
				},
				width: 60
			});
			ret.push(me.buildRedUnreadColCfg());
			ret.push({
				xtype: 'wtmail-mailmessagecolumn',
				tagsStore: me.mys.tagsStore,
				threaded: me.threaded,
				sentMode: actAsSent,
				alwaysShowTime: me.mys.getVar('gridAlwaysShowTime'),
				dateShortFormat: WT.getShortDateFmt(),
				dateLongFormat:  WT.getLongDateFmt(),
				timeShortFormat: WT.getShortTimeFmt(),
				timeLongFormat: WT.getLongTimeFmt(),
				collapseTooltip: me.mys.res('wtmailmailmessagecolumn.collapseTooltip'),
				noteTooltip: me.mys.res('wtmailmailmessagecolumn.noteTooltip'),
				noSubjectText: me.mys.res('wtmailmailmessagecolumn.nosubject'),
				flagsTexts: {
					red: me.mys.res('message.flag.red'),
					orange: me.mys.res('message.flag.orange'),
					green: me.mys.res('message.flag.green'),
					blue: me.mys.res('message.flag.blue'),
					purple: me.mys.res('message.flag.purple'),
					yellow: me.mys.res('message.flag.yellow'),
					black: me.mys.res('message.flag.black'),
					gray: me.mys.res('message.flag.gray'),
					white: me.mys.res('message.flag.white'),
					brown: me.mys.res('message.flag.brown'),
					azure: me.mys.res('message.flag.azure'),
					pink: me.mys.res('message.flag.pink'),
					complete: me.mys.res('message.flag.complete'),
					special: me.mys.res('message.flag.special')
				},
				collapseHandler: function(view, ridx) {
					me.mys.messagesPanel.folderList.collapseClicked(ridx);
				},
				noteHandler: function(view, ridx, cidx, e, rec) {
					var folder = me.multifolder ? rec.get("folder") : me.currentFolder;
					me.editNote(me.currentAccount, rec.get("idmessage"), folder);
				},
				flex: 1
			});
			
		} else {
			ret.push({
				xtype: 'soiconcolumn',
				dataIndex: 'priority',
				stateId: 'stid-priority',
				header: WTF.headerWithGlyphIcon('fa fa-exclamation'),
				cls: 'wtmail-header-text-clip',
				sortable: true,
				menuDisabled: true,
				getIconCls: function(value,rec) {
					var pstatus = Sonicle.webtop.mail.ux.grid.column.Message.buildTypeIcon(rec.get('pecstatus'));
					if (pstatus) {
						return pstatus.iconCls;
					} else {
						return value<3 ? 'fas fa-exclamation wt-theme-text-error' : '';
					}
				},
				scope: me,
				width: 35
			});
			ret.push(me.buildRedUnreadColCfg());
			ret.push({
				dataIndex: 'from',
				stateId: 'stid-from',
				header: me.res("column-from"),		
				sortable: true,
				hidden: actAsSent,
				minWidth: 100,
				maxWidth: 250,
				flex: 1
			});
			ret.push({
				dataIndex: 'to',
				stateId: 'stid-to',
				header: me.res('column-to'),
				sortable: true,
				hidden: !actAsSent,
				minWidth: 100,
				maxWidth: 250,
				flex: 1
			});
			ret.push({
				dataIndex: 'subject',
				stateId: 'stid-subject',
				header: me.res('column-subject'),
				sortable: true,
				renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var status=record.get("status"),
						tid=record.get("threadId"),
						tindent=record.get("threadIndent"),
						topen=record.get("threadOpen"),
						tchildren=record.get("threadHasChildren"),
						tuc=record.get("threadUnseenChildren"),
						tags=record.get("tags"),
						uline=me.threaded && !topen && tuc && tuc>0,
						ctag=false,
						imgtag="";

					if (me.threaded) {                    
						if (tindent===0 && tchildren) {
							var cls=topen?"":"x-grid-group-hd-collapsed";
							imgtag="<div class='x-grid-group-hd-collapsible "+cls+"'>"+
									"<span class='x-grid-group-title wtmail-element-toclick'"+
									  " onclick='WT.getApp().getService(\""+me.mys.ID+"\").messagesPanel.folderList.collapseClicked("+rowIndex+");'"+
									">&nbsp;</span>";
							if (!topen && tuc && tuc>0) imgtag+="<b>+"+tuc+"</b>&nbsp;";
						} else {
							imgtag="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
						}
						for(var i=0;i<tindent;++i) imgtag+="&nbsp;&nbsp;&nbsp;";
					}
					if (status!=="read" && status!=="unread") {
						var statusIcon = Sonicle.webtop.mail.ux.grid.column.Message.buildStatusIcon(status);
						if (!Ext.isEmpty(statusIcon)) {
							imgtag += ('<div class="' + statusIcon + '" style="display:inline-block;width:16px;height:16px;margin-right:4px;"></div>');
						}
						//var imgname=Ext.String.format("status{0}_16.svg",status);
						//imgtag+=WTF.imageTag(me.mys.ID,imgname,16,16,"valign=top")+"&nbsp;";
					}
					if (tags) {
						var r=me.mys.tagsStore.findRecord('id',tags[0]);
						if (r) {
							ctag=true;
							imgtag+="<span style='color: "+r.get("color")+"'>"
						}
					}
					if (uline) imgtag+="<u>";
					imgtag+=value;
					if (uline) imgtag+="</u>";
					if (ctag) imgtag+="</span>";
					if (me.threaded && tindent===0) {
						imgtag+="</div>";
					}
					return imgtag;
				},
				flex: 2.5
			});
			ret.push({
				dataIndex: 'date',
				stateId: 'stid-date',
				header: me.res('column-date'),
				sortable: true,
				renderer: function(value, metadata, record, rowIndex, colIndex, store) {
					var fmt = Ext.Date.format,
							tdy = record.get("istoday"),
							fmtd = record.get("fmtd"),
							showTime = me.mys.getVar('gridAlwaysShowTime'),
							sdateFmt = WT.getShortDateFmt(),
							ltimeFmt = WT.getLongTimeFmt(),
							tag;

					if (!fmtd && (tdy || (store.getGroupField && store.getGroupField()==='gdate'))) {
						tag="<span data-qtip='"+fmt(value,sdateFmt)+"'>"+fmt(value,ltimeFmt)+"</span>";
					} else {
						tag="<span data-qtip='"+fmt(value,ltimeFmt)+"'>"+fmt(value,sdateFmt+(showTime ? ' ' + WT.getShortTimeFmt() : ''))+"</span>";
					}
					return tag;
				},
				minWidth: 80,
				width: 140
			});
			ret.push({
				xtype: 'sobytescolumn',
				dataIndex: 'size',
				stateId: 'stid-size',
				header: me.res('column-size'),
				sortable: true,
				align: 'end',
				headerAlign: 'start',
				minWidth: 90,
				width: 90
			});
			ret.push({
				xtype: 'soiconcolumn',
				dataIndex: 'atts',
				stateId: 'stid-atts',
				header: WTF.headerWithGlyphIcon('fas fa-paperclip'),
				cls: 'wtmail-header-text-clip',
				iconSize: WTU.imgSizeToPx('xs'),
				sortable: false,
				menuDisabled: true,
				getIconCls: function(value,rec) {
					return value ? 'fas fa-paperclip' : '';
				},
				scope: me,
				width: 35
			});
			ret.push({
				xtype: 'soiconcolumn',
				dataIndex: 'flag',
				stateId: 'stid-flag',
				header: WTF.headerWithGlyphIcon('far fa-bookmark'),
				cls: 'wtmail-header-text-clip',
				iconSize: WTU.imgSizeToPx('xs'),
				sortable: true,
				menuDisabled: true,
				getIconCls: function(value,rec) {
					var flag = Sonicle.webtop.mail.ux.grid.column.Message.buildFlag(value, {flagsTexts: {}});
					return flag ? (flag.glyphCls + ' ' + flag.colorCls) : '';
				},
				scope: me,
				width: 35
			});
			ret.push({
				xtype: 'soiconcolumn',
				dataIndex: 'note',
				stateId: 'stid-note',
				header: WTF.headerWithGlyphIcon('far fa-sticky-note'),
				cls: 'wtmail-header-text-clip',
				iconSize: WTU.imgSizeToPx('xs'),
				sortable: true,
				menuDisabled: true,
				getIconCls: function(value,rec) {
					return value ? 'fas fa-sticky-note wt-theme-text-warn' : '';
				},
				scope: me,
				width: 35
			});
			ret.push({
				xtype: 'soiconcolumn',
				dataIndex: 'arch',
				stateId: 'stid-arch',
				header: WTF.headerWithGlyphIcon('fas fa-archive'),
				cls: 'wtmail-header-text-clip',
				iconSize: WTU.imgSizeToPx('xs'),
				sortable: true,
				menuDisabled: true,
				getIconCls: function(value,rec) {
					return value ? 'fas fa-archive' : '';
				},
				scope: me,
				width: 35
			});
		}
		return ret;
	},
	
	buildRedUnreadColCfg: function() {
		var me = this;
		return {
			xtype: 'soiconcolumn',
			dataIndex: 'unread',
			stateId: 'stid-unread',
			header: WTF.headerWithGlyphIcon('far fa-eye'),
			sortable: true,
			menuDisabled: true,
			stopSelection: true,
			iconSize: 10,
			getIconCls: function(value,rec) {
				var ico = 'wtmail-cell-ico-unseen';
				if (value) {
					ico += ' wtmail-icon-status-unseen';
				} else {
					ico += ' wtmail-icon-status-seen';
				}
				return ico;
			},
			getTip: function(value) {
				return me.mys.res('messageGrid.readstatus.' + (value ? 'unread' : 'read'));
			},
			handler: function(grid, rix, cix, e, rec) {
				var newunread=!rec.get('unread');
				me.markMessageSeenState(rec,!newunread);
			},
			scope: me,
			width: 35
		};
	},
	
	/*
	createColumnsFromState: function(state) {
		var me=this,n=0,dcols=new Array();
		
		if (me.getCompactView()) {
			var issentfolder = false,
					node;
			if (me.currentAccount && me.currentFolder) {
				node=me.mys.getFolderNodeById(me.currentAccount,me.currentFolder);
				if (node) issentfolder=node?(node.data.isSent||node.data.isUnderSent):false;
			}
			
			//hidden columns holding header titles for groupings
			dcols[n++]=Ext.create({//Date
				xtype: 'gridcolumn',
				header: me.res("column-date"),
				hidden: true,
				dataIndex: 'gdate',
				hideable: false
			});
			dcols[n++]=Ext.create({//From
				xtype: 'gridcolumn',
				header: me.res("column-from"),
				dataIndex: 'from',
				hidden: true
			});
			dcols[n++]=Ext.create({//To
				xtype: 'gridcolumn',
				header: me.res("column-to"),
				dataIndex: 'to',
				hidden: true
			});
			
			if (me.multifolder) {
				dcols[n++]=Ext.create({//Folder
					xtype: 'gridcolumn',
					header: '',
					width: 100,
					sortable: true,
					dataIndex: 'folder',
					stateId: 'stid-folder',
					hidden: true
				});
				dcols[n++]=Ext.create({ //Folder Descripion
					xtype: 'gridcolumn',
					header: me.res("column-folder"),
					width: 150,
					sortable: true,
					dataIndex: 'folderdesc',
					stateId: 'stid-folderdesc',
					hidden: false,
					renderer: function(value,metadata,record,rowIndex,colIndex,store) {
							return "<span data-qtip='"+record.get('fullfolderdesc')+"'>"+value+"</span>";
					}
				});
			}
			dcols[n++]={
				xtype: 'soavatarcolumn',
				sortable: false,
				groupable: false,
				sentMode: issentfolder,
				getName: function(v, rec) {
					var fld = this.sentMode ? 'to' : 'from';
					return Ext.isEmpty(rec.get(fld)) ? rec.get(fld+'email') : rec.get(fld);
				},
				width: 60
			};
			//dcols[n++]=me.createPriorityColumn(false);
			dcols[n++]=me.createUnreadColumn(false);
			dcols[n++]={//Folder
				xtype: 'wtmail-mailmessagecolumn',
				tagsStore: me.mys.tagsStore,
				threaded: me.threaded,
				sentMode: issentfolder,
				alwaysShowTime: me.mys.getVar('gridAlwaysShowTime'),
				dateShortFormat: WT.getShortDateFmt(),
				dateLongFormat:  WT.getLongDateFmt(),
				timeShortFormat: WT.getShortTimeFmt(),
				timeLongFormat: WT.getLongTimeFmt(),
				collapseTooltip: me.mys.res('wtmailmailmessagecolumn.collapseTooltip'),
				noteTooltip: me.mys.res('wtmailmailmessagecolumn.noteTooltip'),
				noSubjectText: me.mys.res('wtmailmailmessagecolumn.nosubject'),
				flagsTexts: {
					red: me.mys.res('message.flag.red'),
					orange: me.mys.res('message.flag.orange'),
					green: me.mys.res('message.flag.green'),
					blue: me.mys.res('message.flag.blue'),
					purple: me.mys.res('message.flag.purple'),
					yellow: me.mys.res('message.flag.yellow'),
					black: me.mys.res('message.flag.black'),
					gray: me.mys.res('message.flag.gray'),
					white: me.mys.res('message.flag.white'),
					brown: me.mys.res('message.flag.brown'),
					azure: me.mys.res('message.flag.azure'),
					pink: me.mys.res('message.flag.pink'),
					complete: me.mys.res('message.flag.complete'),
					special: me.mys.res('message.flag.special')
				},
				collapseHandler: function(view, ridx) {
					me.mys.messagesPanel.folderList.collapseClicked(ridx);
				},
				noteHandler: function(view, ridx, cidx, e, rec) {
					var folder = me.multifolder ? rec.get("folder") : me.currentFolder;
					me.editNote(me.currentAccount, rec.get("idmessage"), folder);
				},
				flex: 1
			};
			//me.setHideHeaders(true);
		} else {
			//me.setHideHeaders(false);
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
							hidden: true
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
							renderer: function(value,metadata,record,rowIndex,colIndex,store) {
									return "<span data-qtip='"+record.get('fullfolderdesc')+"'>"+value+"</span>";
							}
						});
						break;

					case 'stid-priority':
						dcols[n++]=me.createPriorityColumn(scol.hidden);
						break;

					case 'stid-unread':
						dcols[n++]=me.createUnreadColumn(scol.hidden);
						break;

					case 'stid-from':
						dcols[n++]=Ext.create({//From
							xtype: 'gridcolumn',
							header: me.res("column-from"),
							width: 200,
							sortable: true,
							dataIndex: 'from',
							stateId: 'stid-from',
							hidden: scol.hidden
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
							hidden: scol.hidden //!me.multifolder,
						});
						break;

					case 'stid-subject':
						dcols[n++]=Ext.create({//Subject
							xtype: 'gridcolumn',
							header: me.res("column-subject"),
							width: WT.plTags.desktop?400:200,
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
									tuc=record.get("threadUnseenChildren"),
									tags=record.get("tags"),
									uline=me.threaded && !topen && tuc && tuc>0,
									ctag=false,
									imgtag="";

								if (me.threaded) {                    
									if (tindent===0 && tchildren) {
										var cls=topen?"":"x-grid-group-hd-collapsed";
										imgtag="<div class='x-grid-group-hd-collapsible "+cls+"'>"+
												"<span class='x-grid-group-title wtmail-element-toclick'"+
												  " onclick='WT.getApp().getService(\""+me.mys.ID+"\").messagesPanel.folderList.collapseClicked("+rowIndex+");'"+
												">&nbsp;</span>";
										if (!topen && tuc && tuc>0) imgtag+="<b>+"+tuc+"</b>&nbsp;";
									} else {
										imgtag="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
									}
									for(var i=0;i<tindent;++i) imgtag+="&nbsp;&nbsp;&nbsp;";
								}
								if (status!=="read" && status!=="unread") {
									var statusIcon = Sonicle.webtop.mail.ux.grid.column.Message.buildStatusIcon(status);
									if (!Ext.isEmpty(statusIcon)) {
										imgtag += ('<div class="' + statusIcon + '" style="display:inline-block;width:16px;height:16px;margin-right:4px;"></div>');
									}
									//var imgname=Ext.String.format("status{0}_16.svg",status);
									//imgtag+=WTF.imageTag(me.mys.ID,imgname,16,16,"valign=top")+"&nbsp;";
								}
								if (tags) {
									var r=me.mys.tagsStore.findRecord('id',tags[0]);
									if (r) {
										ctag=true;
										imgtag+="<span style='color: "+r.get("color")+"'>"
									}
								}
								if (uline) imgtag+="<u>";
								imgtag+=value;
								if (uline) imgtag+="</u>";
								if (ctag) imgtag+="</span>";
								if (me.threaded && tindent===0) {
									imgtag+="</div>";
								}
								return imgtag;
							}
						});
						break;

					case 'stid-date':
						dcols[n++]=Ext.create({//Date
							xtype: 'gridcolumn',
							header: me.res("column-date"),
							width: 80 + (me.mys.getVar('gridAlwaysShowTime') ? 60 : 0),
							sortable: true,
							renderer: function(value, metadata, record, rowIndex, colIndex, store) {
								var fmt = Ext.Date.format,
										tdy = record.get("istoday"),
										fmtd = record.get("fmtd"),
										showTime = me.mys.getVar('gridAlwaysShowTime'),
										sdateFmt = WT.getShortDateFmt(),
										ltimeFmt = WT.getLongTimeFmt(),
										tag;
								
								if (!fmtd && (tdy || (store.getGroupField && store.getGroupField()==='gdate'))) {
									tag="<span data-qtip='"+fmt(value,sdateFmt)+"'>"+fmt(value,ltimeFmt)+"</span>";
								} else {
									tag="<span data-qtip='"+fmt(value,ltimeFmt)+"'>"+fmt(value,sdateFmt+(showTime ? ' ' + WT.getShortTimeFmt() : ''))+"</span>";
								}
								return tag;
							},
							dataIndex: 'date',
							stateId: 'stid-date',
							hidden: scol.hidden
						});
						break;

					case 'stid-gdate':
						dcols[n++]=Ext.create({//Date
							xtype: 'gridcolumn',
							header: me.res("column-date"),
							hidden: true,
							dataIndex: 'gdate',
							stateId: 'stid-gdate',
							hideable: false
						});
						break;

					case 'stid-sdate':
						dcols[n++]=Ext.create({//Date
							xtype: 'gridcolumn',
							header: me.res("column-date"),
							hidden: true,
							dataIndex: 'sdate',
							stateId: 'stid-sdate',
							hideable: false
						});
						break;

					case 'stid-xdate':
						dcols[n++]=Ext.create({//Date
							xtype: 'gridcolumn',
							header: me.res("column-date"),
							hidden: true,
							dataIndex: 'xdate',
							stateId: 'stid-xdate',
							hideable: false
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
							hidden: scol.hidden //me.multifolder,
				            //renderer: function(value,metadata,record,rowIndex,colIndex,store) {
							//	return WTU.humanReadableSize(parseInt(value));
							//},
						});
						break;

					case 'stid-atts':
						dcols[n++]=Ext.create({//Attachment
							xtype: 'soiconcolumn',
							header: WTF.headerWithGlyphIcon('fas fa-paperclip'),
							cls: 'wtmail-header-text-clip',
							width: 30,
							sortable: false,
							menuDisabled: true,
							dataIndex: 'atts',
							stateId: 'stid-atts',
							hidden: scol.hidden,
							getIconCls: function(value,rec) {
								return value ? 'fas fa-paperclip' : '';
							},
							iconSize: WTU.imgSizeToPx('xs'),
							scope: me
						});
						break;

					case 'stid-flag':
						dcols[n++]=Ext.create({//Flag
							xtype: 'soiconcolumn',
							header: WTF.headerWithGlyphIcon('far fa-bookmark'),
							cls: 'wtmail-header-text-clip',
							width: 30,
							sortable: true,
							menuDisabled: true,
							dataIndex: 'flag',
							stateId: 'stid-flag',
							hidden: scol.hidden,
							getIconCls: function(value,rec) {
								var flag = Sonicle.webtop.mail.ux.grid.column.Message.buildFlag(value, {flagsTexts: {}});
								return flag ? (flag.glyphCls + ' ' + flag.colorCls) : '';
								//return !Ext.isEmpty(value) ? ('fas fa-bookmark wtmail-flag-'+value) : '';
							},
							iconSize: WTU.imgSizeToPx('xs'),
							scope: me
						});
						break;

					case 'stid-note':
						dcols[n++]=Ext.create({//Mail note
							xtype: 'soiconcolumn',
							header: WTF.headerWithGlyphIcon('far fa-sticky-note'),
							cls: 'wtmail-header-text-clip',
							width: 30,
							sortable: true,
							menuDisabled: true,
							dataIndex: 'note',
							stateId: 'stid-note',
							hidden: scol.hidden,
							getIconCls: function(value,rec) {
								return value ? 'fas fa-sticky-note wt-theme-text-warn' : '';
							},
							iconSize: WTU.imgSizeToPx('xs'),
							scope: me
						});
						break;

					case 'stid-arch':
						dcols[n++]=Ext.create({//Archived
							xtype: 'soiconcolumn',
							header: WTF.headerWithGlyphIcon('fas fa-archive'),
							cls: 'wtmail-header-text-clip',
							width: 30,
							sortable: true,
							menuDisabled: true,
							dataIndex: 'arch',
							stateId: 'stid-arch',
							hidden: scol.hidden, //me.multifolder,
							getIconCls: function(value,rec) {
								return value ? 'fas fa-archive' : '';
							},
							iconSize: WTU.imgSizeToPx('xs'),
							scope: me
						});
						break;
				}
			});

		}
		return dcols;
	},
	*/
	
	/*
	createUnreadColumn: function(hidden) {
		var me=this;
		return Ext.create({//Status
			xtype: 'soiconcolumn',
			header: WTF.headerWithGlyphIcon('far fa-eye'),
			width: 28,
			sortable: true,
			menuDisabled: true,
			stopSelection: true,
			dataIndex: 'unread',
			stateId: 'stid-unread',
			hidden: hidden,
			iconSize: 10,
			getIconCls: function(value,rec) {
				var ico = 'wtmail-cell-ico-unseen';
				if (value) {
					ico += ' wtmail-icon-status-unseen';
				} else {
					ico += ' wtmail-icon-status-seen';
				}
				return ico;
			},
			getTip: function(value) {
				return me.mys.res('messageGrid.readstatus.' + (value ? 'unread' : 'read'));
			},
			handler: function(grid, rix, cix, e, rec) {
				var newunread=!rec.get('unread');
				me.markMessageSeenState(rec,!newunread);
			},
			scope: me
		});		
	},
	*/
	/*
	createPriorityColumn: function(hidden) {
		var me=this;
		return Ext.create({//Priority
			xtype: 'soiconcolumn',
			header: WTF.headerWithGlyphIcon('fa fa-exclamation'),
			cls: 'wtmail-header-text-clip',
			width: 35,
			sortable: true,
			menuDisabled: true,
			dataIndex: 'priority',
			stateId: 'stid-priority',
			hidden: hidden,
			//renderer: function(value,metadata,record,rowIndex,colIndex,store) {
			//		var tag;
			//		var others="border=0";
			//		if (value<3) tag=WTF.imageTag(me.mys.ID,'priorityhigh_16.gif',others);
			//		else tag=WTF.globalImageTag('empty.gif',7,16,others);
			//		return tag;
			//},
			getIconCls: function(value,rec) {
				var pstatus = Sonicle.webtop.mail.ux.grid.column.Message.buildTypeIcon(rec.get('pecstatus'));
				if (pstatus) {
					return pstatus.iconCls;
				} else {
					return value<3 ? 'fas fa-exclamation wt-theme-text-error' : '';
				}
				//var pecstatus=rec.get('pecstatus');
				//if (pecstatus) {
				//	switch (pecstatus) {
				//		case 'posta-certificata':
				//			return 'wtmail-pec';
				//		case 'accettazione':
				//			return 'wtmail-pec-accepted';
				//		case 'non-accettazione':
				//			return 'wtmail-pec-not-accepted';
				//		case 'avvenuta-consegna':
				//			return 'wtmail-pec-delivered';
				//		case 'errore':
				//			return 'wtmail-pec-error';
				//		default:
				//			return '';
				//	}
				//} else {
				//	return value<3 ? 'fas fa-exclamation wt-theme-text-error' : '';
				//}
			},
			//getCellCls: function(value,rec) {
			//	var pecstatus=rec.get('pecstatus'),
			//		cls=(pecstatus==='accettazione')?'wtmail-row-pec-accepted':
			//			(pecstatus==='avvenuta-consegna')?'wtmail-row-pec-delivered':
			//			(pecstatus==='errore')?'wtmail-row-pec-error':null;
			//	return cls;
			//},
			scope: me
		});
	},
	*/
	/*
	createDefaultState: function() {
        var me=this,n=0,stcols=new Array(),
			issentfolder=false;
		
		if (me.mys && me.currentFolder) {
			var node=me.mys.getFolderNodeById(me.currentAccount, me.currentFolder);
			if (node) issentfolder=(node.data.isSent||node.data.isUnderSent);
		}
	
		
		//a={"columns":[
		//		{"id":"stid-priority","width":40},
		//		{"id":"stid-unread","width":40},
		//		{"id":"stid-from","width":198},
		//		{"id":"stid-subject","width":675},
		//		{"id":"stid-to","width":375,"hidden":true},
		//		{"id":"stid-date","width":96},
		//		{"id":"stid-gdate","width":100},
		//		{"id":"stid-sdate","width":100},
		//		{"id":"stid-xdate","width":100},
		//		{"id":"stid-size","width":89},
		//		{"id":"stid-atts","width":40},
		//		{"id":"stid-flag","width":47},
		//		{"id":"stid-note","width":40}],
		//	"weight":0
		//}
        if (me.multifolder) {
            stcols[n++]={ id: 'stid-folder', width: 100 };
            stcols[n++]={ id: 'stid-folderdesc', width: 100 };
        }
        stcols[n++]={ id: 'stid-priority', width: 40 };
        stcols[n++]={ id: 'stid-unread', width: 40 };
        stcols[n++]={ id: 'stid-from', width: 198, hidden: issentfolder?true:false };
        stcols[n++]={ id: 'stid-to', width: 198, hidden: issentfolder?false:true };
        stcols[n++]={ id: 'stid-subject', width: WT.plTags.desktop?400:200 };
        stcols[n++]={ id: 'stid-date', width: 96 + (me.mys.getVar('gridAlwaysShowTime') ? 40 : 0) };
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
	*/
	
    res: function(s) {
        return this.mys.res(s);
    },
	
	privates: {
		amIFocused: function() {
			var el = this.getEl(),
					ael = document.activeElement;
			return ael && el && el.isAncestor(ael);
		}
	}
});

