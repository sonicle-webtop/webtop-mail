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
    ixAutoSelect: -1,
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
	
    frame: false,
    //iconCls:'icon-grid',
	//TODO: ddGroup
    //ddGroup: 'mail',
    //enableDragDrop: true,
    enableColumnMove: true,
	viewConfig: {
		navigationModel: Ext.create('Sonicle.webtop.mail.NavigationModel',{}),
		getRowClass: function(record, index, rowParams, store ) {
			var unread=record.get('unread');
			var tdy=record.get('istoday');
			cls1=unread?'wtmail-row-unread':'';
			cls2=tdy?'wtmail-row-today':'';
			return cls1+' '+cls2;
		}
	},
	
	features: [
		{
			ftype:'grouping',
			groupHeaderTpl: Ext.create('Ext.XTemplate',
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
			)
		}
	],
	selModel: { 
		mode: 'MULTI'
	},
	selType: 'rowmodel',
	multiColumnSort: true,
	
	
    clickTimeoutId: 0,
    clickEvent: null,
	//TODO: DocMgt
    //arch: WT.docmgt || WT.docmgtwt,
    multifolder: false,
    reloadAction: 'ListMessages',
    firstShow: true,
	
    initComponent: function() {
        var me=this;

		//TODO: add events
        /*me.addEvents(
            'deleting',
            'moving'
        );*/
		
        /*this.view=Ext.create('Sonicle.webtop.mail.MessageListView',{
			loadMask: { msg: WT.res("loading") },
			grid: this
		});*/
		
		me.viewConfig.loadMask={ msg: WT.res("loading") };

/*        var n=0;
        var fields=new Array();
        var idx='idmessage';*/
		var smodel='Sonicle.webtop.mail.MessagesModel';
        if (this.multifolder) {
/*            fields[n++]='folder';
            fields[n++]='folderdesc';
            idx='idmandfolder';
            fields[n++]=idx;*/
			smodel='Sonicle.webtop.mail.MultiFolderMessagesModel';
        }
/*        fields[n++]='idmessage';
        fields[n++]={name:'priority',type:'int'};
        fields[n++]='status';
        fields[n++]='from';
        fields[n++]='to';
        fields[n++]='subject';
        fields[n++]={name:'date', type:'date'};
        fields[n++]='gdate';
        fields[n++]='sdate';
        fields[n++]='xdate';
        fields[n++]={name:'unread', type:'boolean'};
        fields[n++]={name:'size', type:'int'};
        fields[n++]='flag';
        fields[n++]='note';
        fields[n++]={name:'istoday', type:'boolean'};
        if (me.arch) fields[n++]={name:'arch', type:'boolean'};
        fields[n++]={name:'atts', type:'boolean'};
        fields[n++]={name:'scheddate', type:'date'};*/


        me.store = Ext.create('Ext.data.JsonStore',{
            proxy: WTF.proxy(me.mys.ID, me.reloadAction,'messages'),
			model: smodel,
			pageSize: me.pageSize,
			groupField: 'sdate',
			groupDir: 'DESC',
			/*grouper: Ext.create('Ext.util.Grouper',{
				property: 'date',
				direction: 'DESC',
				groupFn: function(r) {
					var d1=r.get("date");
					var d2=new Date(d1.getFullYear(),d1.getMonth(),d1.getDate());
					return d2;
				},
				sorterFn: function(r1,r2) {
					var a=r1.get("date");
					var b=r2.get("date");
					return (a>b)-(a<b);
				}
			}),*/
			
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
        });

/*		if (me.createPagingToolbar) {
			me.ptoolbar=new Ext.PagingToolbar({
				region: "center",
				store: me.store,
				pageSize: me.pageSize,
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
			me.ptoolbar.remove(me.ptoolbar.displayItem);
			me.ptoolbar.add(me.ptoolbar.displayItem=Ext.create('Ext.Button',{
				tooltip: me.res("changepagerows"),
				handler: me.changePageRows,
				scope: me
			}));
			me.tbar=me.ptoolbar;
		}*/

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
                          params: {start:0,limit:50}
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
        dcols[n++]={//Priority
            header: WTF.imageTag(me.mys.ID,'headerpriority.gif',7,16),
            width: 35,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'priority',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					var others="border=0"
					if (value<3) tag=WTF.imageTag(me.mys.ID,'priorityhigh.gif',others);
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
            header: WTF.imageTag(me.mys.ID,'headerstatus.gif',15,16),
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
			header: WTF.imageTag(me.mys.ID,'headerattach.gif',15,16),
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
            header: WTF.imageTag(me.mys.ID,'headerflag.gif',15,16),
            width: 30,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'flag',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					var others="border=0";
					if (value!=='') tag=WTF.imageTag(me.mys.ID,"flag"+value+".gif",16,16,others);
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
                          ['complete',me.res('flagcomplete'),'iconFlagComplete']
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
			header: WTF.imageTag(me.mys.ID,'headermailnote.gif',15,16),
            width: 30,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'note',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					if (value) tag=WTF.imageTag(me.mys.ID,'mailnote.gif',16,16,"border=0 style='cursor: pointer'");
					else tag=WTF.globalImageTag('empty.gif',16,16,"border=0");
					return tag;
			},
			scope: me,
            filter: {}
            
        };
        
        if (me.arch) {
            dcols[n++]={//Archived
				header: WTF.imageTag(me.mys.ID,'headerdocmgt.png',16,16),
                width: 30,
                sortable: true,
                menuDisabled: true,
                dataIndex: 'arch',
                hidden: me.multifolder,
                renderer: function(value,metadata,record,rowIndex,colIndex,store) {
						var tag;
						var others="border=0";
						if (value) tag=WTF.imageTag(me.mys.ID,"docmgt.png",16,16,others);
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
        me.store.on('load',me.loaded,this);
        me.on('afterrender',function() {
            //this.filterRow.hideFilterRow();
        }, me);*/
		
        me.callParent(arguments);
    },
	
	reloadFolder: function(folder_id, config){
		var me = this,
			proxy = me.store.getProxy();
	
		config=config||{};
		if(!folder_id) return;
		Ext.applyIf(config, {
			start: 0,
			limit: 50,
			refresh: 1
		});

		//TODO: sort info && threaded
		//if (!Ext.isDefined(config.threaded)) config.threaded=2;
        //this.store.sortInfo=null;
		//TODO: baseParams??
		//this.store.baseParams = {service: 'mail', action: 'ListMessages', folder: folder_id};
		var params={
				start: config.start, limit: config.limit, refresh: config.refresh, folder: folder_id, threaded: config.threaded
			};
		proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), params));

		
		me.store.load();
		//this.render();
		me.currentFolder = folder_id;
	},
	
    res: function(s) {
        return this.mys.res(s);
    }
	
});
