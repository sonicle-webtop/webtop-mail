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
	},
	
	onKeyLeft: function(keyEvent) {
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
        { name: 'to' },
        { name: 'subject' },
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
        { name: 'to' },
        { name: 'subject' },
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
        { name: 'scheddate', type:'date' },
		{ name: 'autoedit', type:'boolean' }
	]
});

Ext.define('Sonicle.webtop.mail.MessageGrid',{
	extend: 'Ext.grid.Panel',
	requires: [
		'Sonicle.data.BufferedStore',
		'Sonicle.selection.RowModel',
		'Sonicle.webtop.mail.plugin.MessageGridViewDragDrop'
	],
	
	pageSize: 50,	
    frame: false,
    enableColumnMove: true,
	viewConfig: {
		//preserveScrollOnRefresh: true,
		navigationModel: Ext.create('Sonicle.webtop.mail.NavigationModel',{}),
		getRowClass: function(record, index, rowParams, store ) {
			var unread=record.get('unread');
			var tdy=record.get('istoday');
			cls1=unread?'wtmail-row-unread':'';
			cls2=tdy?'wtmail-row-today':'';
			return cls1+' '+cls2;
		},
		plugins: {
            ptype: 'messagegridviewdragdrop'
        }
		
	},
	
	features: [
		{
			ftype:'grouping',
			groupHeaderTpl: '{columnName}: {name}'
			/*groupHeaderTpl: Ext.create('Ext.XTemplate',
				'{children:this.getHeaderPrefix}',
				'<span>{children:this.getHeaderString}</span>',
				{
					getHeaderPrefix: function(children) {
						var xdate=children[0].get("xdate");
						return (xdate.length>0)?xdate+"&nbsp;:&nbsp;":"";
					},
					getHeaderString: function(children) {
						return children[0].get("gdate");
					}
				}
			)*/
		}
	],
	selModel: { 
		mode: 'MULTI'
	},
	selType: 'sorowmodel',
	multiColumnSort: true,
	
	
    clickTimeoutId: 0,
    clickEvent: null,
	//TODO: DocMgt
    //arch: WT.docmgt || WT.docmgtwt,
    multifolder: false,
    reloadAction: 'ListMessages',
    firstShow: true,
	key2flag: ['clear','red','orange','green','blue','purple','yellow','black','gray','white'],
	createPagingToolbar: false,
	
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
								me.setFlag(me.key2flag[key-48]);
							}
						},
						{
							key: Ext.event.Event.INSERT,
							shift: false,
							fn: function(key,ev) {
								me.setFlag("complete");
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

        /*this.view=Ext.create('Sonicle.webtop.mail.MessageListView',{
			loadMask: { msg: WT.res("loading") },
			grid: this
		});*/
		
		me.viewConfig.loadMask={ msg: WT.res("loading") };

		var smodel='Sonicle.webtop.mail.MessagesModel';
        if (this.multifolder) {
			smodel='Sonicle.webtop.mail.MultiFolderMessagesModel';
        }

        me.store = Ext.create('Sonicle.data.BufferedStore',{
            //proxy: WTF.proxy(me.mys.ID, me.reloadAction,'messages'),
			proxy: {
				// load using script tags for cross domain, if the data in on the same domain as
				// this page, an Ajax proxy would be better
				type: 'ajax',
				url: WTF.requestBaseUrl(),
				extraParams: {
					action: me.reloadAction,
					service: me.mys.ID
				},
				reader: {
					rootProperty: 'messages',
					totalProperty: 'total'
				}
				// sends single sort as multi parameter
				//simpleSortMode: true,
				// sends single group as multi parameter
				//simpleGroupMode: true,

				// This particular service cannot sort on more than one field, so grouping === sorting.
				//groupParam: 'sort',
				//groupDirectionParam: 'dir'
			},
			model: smodel,
			pageSize: me.pageSize
			//leadingBufferZone: 100,
			
			//groupField: 'gdate',
			//groupDir: 'DESC'
		});
        me.store.on("metachange",function(s,meta) {
			if (meta.groupField && meta.groupField!=='none' && meta.groupField!=='') {
				s.blockLoad();
				s.group(null, null);
				s.group(meta.groupField, meta.sortInfo.direction);
				s.unblockLoad(false);
			} else {
				s.blockLoad();
				s.group(null, null);
				s.unblockLoad(false);
				//s.sort(meta.sortInfo.sortField,meta.sortInfo.direction);
			}
        });
		
        //me.store = Ext.create('Ext.data.JsonStore',{
        //    proxy: WTF.proxy(me.mys.ID, me.reloadAction,'messages'),
		//	model: smodel,
		//	pageSize: me.pageSize,
		//	groupField: 'sdate',
		//	groupDir: 'DESC',
			
			//TODO: sort
            /*sortInfo: {field: (this.multifolder?'folderdesc':'date'), direction: (this.multifolder?'ASC':'DESC')},
            remoteSort: !this.localSort,
            groupOnSort: false,*/
			
            /*reader: new Ext.data.JsonReader({
                root: 'messages',
                totalProperty: 'totalCount',
                id: idx,
                fields: fields
            }),*/
            
			//TODO: reload group
            /*reload: function(config) {
                this.sortInfo=null;
                Ext.data.GroupingStore.superclass.reload.call(this,config);
            },*/
            
			//TODO: onMetaChange
            /*onMetaChange : function(meta){
                
                if(this.reader.meta.groupField!=null) {
                    this.groupField=this.reader.meta.groupField;
                }
                this.recordType = this.reader.recordType;
                this.fields = this.recordType.prototype.fields;
                delete this.snapshot;
                if(this.reader.meta.sortInfo){
                    this.sortInfo = this.reader.meta.sortInfo;
                }else if(this.sortInfo  && !this.fields.get(this.sortInfo.field)){
                    delete this.sortInfo;
                }
                if(this.writer){
                    this.writer.meta = this.reader.meta;
                }
                this.modified = [];
                this.fireEvent('metachange', this, this.reader.meta);
            },*/
            //grid: me,
			//ms: me.ms
        //});

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
		//TODO: FilterRow
/*        if (!Ext.isIE) {
            var filterRow = new WT.GridFilterRow({
                autoFilter: false,
                hidden: true,
                listeners: {
                    change: function(data) {
                        var patterns='';
                        var fields='';
                        for(p in data) {
                            var v=data[p];
                            if (v.length>0) {
                                if (patterns.length>0) {
                                    patterns+="|";
                                    fields+="|";
                                }
                                patterns+=v;
                                fields+=p;
                            }
                        }
                        var s=this.grid.store;
                        var cf=s.baseParams.folder;
                        s.baseParams={service: 'mail', action: this.grid.reloadAction, folder: cf, searchfield: fields, pattern: patterns, refresh:1};
                        s.reload({
                          params: {start:0,limit:me.pageSize}
                        });
                        s.baseParams.refresh=0;
                        //this.grid.store.load({params: data});
                    }
                }
            });
            this.plugins=[ filterRow ];
            this.filterRow=filterRow;
        }*/


        n=0;
        var dcols=new Array();
        if (me.multifolder) {
            dcols[n++]={//Folder
                header: '',
                width: 100,
                sortable: true,
                dataIndex: 'folder',
                hidden: true,
                filter: {}
            };
            dcols[n++]={//Folder Descripion
                header: me.res("column-folder"),
                width: 100,
                sortable: true,
                dataIndex: 'folderdesc',
                filter: {}
            };
        }
        me.priIndex=n;
/*		dcols[n++]={
			text : 'row',
			dataIndex: 'rowIndex',
			width: 50,
			sortable : false,
			// other config you need..
			renderer : function(value, metaData, record, rowIndex)
			{
				return rowIndex+1;
			}
		};*/
        dcols[n++]={//Priority
            header: WTF.imageTag(me.mys.ID,'headerpriority_16.gif',7,16),
            width: 35,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'priority',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					var others="border=0"
					if (value<3) tag=WTF.imageTag(me.mys.ID,'priorityhigh_16.gif',others);
					else tag=WTF.globalImageTag('empty.gif',7,16,others);
					return tag;
			},
			scope: me,
            filter: {
                fieldEvents: ["select"],
                field: {
                    xtype: "iconcombo",
                    editable: false,
                    mode: 'local',
                    width: 24,
    //                        autoCreate: {tag: "input", type: "text", size: "1", autocomplete: "off", disabled: 'disabled'},
    //                        triggerConfig: {tag: "img", src: 'webtop/themes/win/minitrigger.gif', cls: "x-form-minitrigger ", width: 5},
                    store: new Ext.data.ArrayStore({
                      //id: 0,
                      fields: ['value','text','icon'],
                      data: [['','\u00a0',''], ['1',me.res('prihigh'),'iconPriorityHigh']]
                    }),
                    valueField: 'value',
                    displayField: 'text',
                    iconClsField: 'icon',
                    triggerAction: 'all',
                    value: ""
                }
            }
        };
        dcols[n++]={//Status
            header: WTF.imageTag(me.mys.ID,'headerstatus_16.gif',15,16),
            width: 30,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'status',
            hidden: false,
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					//var sdate=record.get("scheddate");
					//if (sdate) value="scheduled";
					var imgname=Ext.String.format("status{0}_16.png",value);
					var imgtag=WTF.imageTag(me.mys.ID,imgname,16,16);
					//if (sdate) tag="<span ext:qtip='"+Ext.util.Format.date(sdate,'d-M-Y')+" "+Ext.util.Format.date(sdate,'H:i:s')+"'>"+imgtag+"</span>";
					//else tag=imgtag;
					return imgtag;
			},
			scope: me,
            filter: {
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
                          ['unread',me.res('stunread'),'iconStatusUnread'],
                          ['new',me.res('strecent'),'iconStatusNew'],
                          ['replied',me.res('streplied'),'iconStatusReplied'],
                          ['forwarded',me.res('stforwarded'),'iconStatusForwarded'],
                          ['repfwd',me.res('strepfwd'),'iconStatusRepFwd'],
                          ['read',me.res('stread'),'iconStatusRead']
                      ]
                    }),
                    valueField: 'value',
                    displayField: 'text',
                    iconClsField: 'icon',
                    triggerAction: 'all',
                    value: ""
                }
            }
            
        };
        dcols[n++]={//From
            header: me.res("column-from"),
            width: 200,
            sortable: true,
            dataIndex: 'from',
            filter: {}
        };
        dcols[n++]={//To
            header: me.res("column-to"),
            width: 200,
            sortable: true,
            dataIndex: 'to',
            hidden: !me.multifolder,
            filter: {}
        };
        dcols[n++]={//Subject
            header: me.res("column-subject"),
            width: 400,
            sortable: true,
            dataIndex: 'subject',
            filter: {}
        };
        dcols[n++]={//Date
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
            filter: {}
        };
        dcols[n++]={//Date
            header: me.res("column-date"),
            hidden: true,
            dataIndex: 'gdate',
            filter: {}
        };
        dcols[n++]={//Date
            header: me.res("column-date"),
            hidden: true,
            dataIndex: 'sdate',
            filter: {}
        };
        dcols[n++]={//Date
            header: me.res("column-date"),
            hidden: true,
            dataIndex: 'xdate',
            filter: {}
        };
        dcols[n++]={//Dimension
            header: me.res("column-size"),
            width: 50,
            sortable: true,
            dataIndex: 'size',
            hidden: me.multifolder,
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
                return WTU.humanReadableSize(parseInt(value));
            },
            filter: {}
        };
        dcols[n++]={//Attachment
			header: WTF.imageTag(me.mys.ID,'headerattach_16.gif',15,16),
            width: 30,
            sortable: false,
            menuDisabled: true,
            dataIndex: 'atts',
			xtype: 'soiconcolumn',
			iconField: function(value,rec) {
				return Ext.isEmpty(value)?WTF.cssIconCls(WT.XID, 'empty', 'xs'):WTF.cssIconCls(me.mys.XID, 'attachment', 'xs');
			},
			iconSize: WTU.imgSizeToPx('xs'),
			scope: me,
            filter: {}
            
        };
        dcols[n++]={//Flag
            header: WTF.imageTag(me.mys.ID,'headerflag_16.gif',15,16),
            width: 30,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'flag',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					var others="border=0";
					if (value!=='') tag=WTF.imageTag(me.mys.ID,"flag"+value+"_16.gif",16,16,others);
					else tag=WTF.globalImageTag('empty.gif',16,16,others);
					return tag;
			},
			scope: me,
            filter: {
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
            }
            
        };
        dcols[n++]={//Mail note
			header: WTF.imageTag(me.mys.ID,'headermailnote_16.gif',15,16),
            width: 30,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'note',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					if (value) tag=WTF.imageTag(me.mys.ID,'mailnote_16.gif',16,16,"border=0 style='cursor: pointer'");
					else tag=WTF.globalImageTag('empty.gif',16,16,"border=0");
					return tag;
			},
			scope: me,
            filter: {}
            
        };
        
        if (me.arch) {
            dcols[n++]={//Archived
				header: WTF.imageTag(me.mys.ID,'headerdocmgt_16.png',16,16),
                width: 30,
                sortable: true,
                menuDisabled: true,
                dataIndex: 'arch',
                hidden: me.multifolder,
                renderer: function(value,metadata,record,rowIndex,colIndex,store) {
						var tag;
						var others="border=0";
						if (value) tag=WTF.imageTag(me.mys.ID,"docmgt_16.png",16,16,others);
						else tag=WTF.globalImageTag('empty.gif',16,16,others);
						return tag;
				},
				scope: me,
                filter: {}
            };
        }
        //me.cm=new Ext.grid.ColumnModel(dcols);
		me.columns=dcols;
		//TODO: selection and events
		/*
        me.selModel=new WT.GridSelectionModel({singleSelect:false});
		*/
        me.store.on('beforeload',function() {
			me.storeLoading=true;
		});
		//me.store.on('load',me.loaded,me);
        me.store.on('load',function(s,r,o) {
			me.storeLoading=false;
			me.loaded(s,r,o);
		});
		/*
        me.on('afterrender',function() {
            //this.filterRow.hideFilterRow();
        }, me);*/
		
		
        me.callParent(arguments);
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
			folder: folder_id
		});

		//TODO: sort info && threaded
		//if (!Ext.isDefined(config.threaded)) config.threaded=2;
        //this.store.sortInfo=null;
		//TODO: baseParams??
		//this.store.baseParams = {service: 'mail', action: 'ListMessages', folder: folder_id};
		proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), config));

		
		me.store.load();
		//this.render();
		me.currentFolder = folder_id;
	},
	
    loaded: function(s,r,o) {
/*		var me=this,
			meta=me.store.proxy.reader.metaData,
			ci2=meta.colsInfo2;
        if (ci2) {
			me.updatingColumns = true;
			me.suspendEvents(false);
            for(var i=0; i<ci2.length; i++) {
				var col2=ci2[i],
					cm=me.columnManager,
					ix=cm.getHeaderByDataIndex(col2.dataIndex).getIndex(),
					col=cm.columns[ix];
                if(ix && ix>=0) {
					var ix2=col2.index;
					col.setHidden(col2.hidden);
					if(ix2 && ix2!==-1 && ix!==ix2 && col.isVisible()) me.headerCt.move(ix, ix2);
				}
            }
			me.updatingColumns = false;
			me.resumeEvents();
        }*/
		//TODO: autoedit
		/*
        var ae=meta.autoedit;
		if (ae) {
            for(var i=0; i<ae.length; i++) {
				var r=s.getById(ae[i]);
				this.ms.editMessage(r,true);
			}
		}*/
		
		//TODO: discussions
		/*
		if (s.reader.jsonData.threaded===1) {
			this.ms.messagesPanel.pressDiscussionsButton();
		} else {
			this.ms.messagesPanel.depressDiscussionsButton();
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
		var me=this,
			win=WT.createView(me.mys.ID,'view.DockableMessageView',{
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

    actionDelete: function() {
		if (this.storeLoading) {
			return;
		}
		
		var me=this,
			curfolder=me.currentFolder,
			sm=me.getSelectionModel(),
			selection=sm.getSelection(),
			ftrash=me.mys.getFolderTrash();
		
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
						  params: {start:0,limit:n}
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
	
    deleteSelection: function(from,selection) {
        var me=this,data=me.sel2ids(selection);
        me.deleteMessages(from,data)
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
					//me.store.reload();
					//me.store.remove(data.selection);
					me.getSelectionModel().removeSelection(data.selection);
				} else {
					WT.error(json.text);
				}
			}
		});					
    },

    deleteMessage: function(folder,idmessage) {
		this.deleteMessages(folder, { ids: [ idmessage ], multifolder: false });
    },	
	
    moveSelection: function(from,to,selection) {
        var me=this, 
            data=me.sel2ids(selection);
		data.cb=function(result) {
			if (result) {
				//me.store.remove(data.selection);
				me.getSelectionModel().removeSelection(data.selection);
			}
		}
        me.moveMessages(from,to,data)
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
	
    setFlag: function(flagstring) {
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
	
    res: function(s) {
        return this.mys.res(s);
    }
	
});
