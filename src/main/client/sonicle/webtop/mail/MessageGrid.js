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
	extend: 'Ext.grid.View',
    
    ixAutoSelect: -1,
    //enableGroupingMenu: false,
	
	initComponent: function() {
		var me=this;
		me.navigationModel=Ext.create('Ext.view.NavigationModel',{});
		me.callParent(arguments);
	},
    
	loadMask: { msg: WT.res("loading") },
	getRowClass: function(record, index, rowParams, store ) {
		var unread=record.get('unread');
		var tdy=record.get('istoday');
		cls1=unread?'wtmail-row-unread':'';
		cls2=tdy?'wtmail-row-today':'';
		return cls1+' '+cls2;
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

Ext.define('Sonicle.webtop.mail.MessagesModel',{
	extend: 'Ext.data.Model',
	fields: []
});

Ext.define('Sonicle.webtop.mail.MessageGrid',{
	extend: 'Ext.grid.Panel',

    frame: false,
    //iconCls:'icon-grid',
	//TODO: ddGroup
    //ddGroup: 'mail',
    //enableDragDrop: true,
    enableColumnMove: true,
/*	viewConfig: {
		loadMask: { msg: WT.res("loading") },
		getRowClass: function(record, index, rowParams, store ) {
			var unread=record.get('unread');
			var tdy=record.get('istoday');
			cls1=unread?'wtmail-row-unread':'';
			cls2=tdy?'wtmail-row-today':'';
			return cls1+' '+cls2;
		}
	},*/
	
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
    ms: null,

    initComponent: function() {
        var me=this;
		
		//TODO: add events
        /*me.addEvents(
            'deleting',
            'moving'
        );*/
		
        this.view=Ext.create('Sonicle.webtop.mail.MessageListView',{});

        var n=0;
        var fields=new Array();
        var idx='idmessage';
        if (this.multifolder) {
            fields[n++]='folder';
            fields[n++]='folderdesc';
            idx='idmandfolder';
            fields[n++]=idx;
        }
        fields[n++]='idmessage';
        fields[n++]={name:'priority',type:'int'};
        fields[n++]='status';
        fields[n++]='from';
        fields[n++]='to';
        fields[n++]='subject';
        fields[n++]={name:'date', type:'date'};
        fields[n++]='gdate';
        fields[n++]={name:'unread', type:'boolean'};
        fields[n++]={name:'size', type:'int'};
        fields[n++]='flag';
        fields[n++]='note';
        fields[n++]={name:'istoday', type:'boolean'};
        if (me.arch) fields[n++]={name:'arch', type:'boolean'};
        fields[n++]={name:'atts', type:'boolean'};
        fields[n++]={name:'scheddate', type:'date'};


        //this.store = new Ext.data.Store({
        me.store = Ext.create('Ext.data.JsonStore',{
            proxy: WT.proxy(me.ms.id, me.reloadAction,'messages'),
			model: Ext.create('Sonicle.webtop.mail.MessagesModel',{
				id: idx,
				fields: fields
			}),
			
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
            grid: me,
			ms: me.ms
        });

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
            header: me.ms.imageTag('headerpriority.gif',7,16),
            width: 35,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'priority',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					var others="border=0"
					if (value<3) tag=this.ms.imageTag('priorityhigh.gif',others);
					else tag=WT.globalImageTag('empty.gif',7,16,others);
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
                      id: 0,
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
            header: me.ms.imageTag('headerstatus.gif',15,16),
            width: 30,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'status',
            hidden: false,
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					//var sdate=record.get("scheddate");
					//if (sdate) value="scheduled";
					var imgname=Ext.String.format("status{0}.png",value);
					var imgtag=this.ms.imageTag(imgname,16,16);
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
                      id: 0,
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
                var tdy=record.get("istoday");
                var tag;
                if (tdy || store.groupField=='gdate') tag="<span ext:qtip='"+Ext.util.Format.date(value,'d-M-Y')+"'>"+Ext.util.Format.date(value,'H:i:s')+"</span>";
                else tag="<span ext:qtip='"+Ext.util.Format.date(value,'H:i:s')+"'>"+Ext.util.Format.date(value,'d-M-Y')+"</span>";
                return tag;
            },
            dataIndex: 'date',
            filter: {}
        };
        dcols[n++]={//Date
            header: me.res("column-date"),
            //width: 80,
            //sortable: true,
            /*renderer: function(value,metadata,record,rowIndex,colIndex,store) {
                var tdy=record.get("istoday");
                var tag;
                if (tdy) tag="<span ext:qtip='"+Ext.util.Format.date(value,'d-M-Y')+"'>"+Ext.util.Format.date(value,'H:i:s')+"</span>";
                else tag="<span ext:qtip='"+Ext.util.Format.date(value,'H:i:s')+"'>"+Ext.util.Format.date(value,'d-M-Y')+"</span>";
                return tag;
            },*/
            hidden: true,
            dataIndex: 'gdate',
            filter: {}
        };
        dcols[n++]={//Dimension
            header: me.res("column-size"),
            width: 50,
            sortable: true,
            dataIndex: 'size',
            hidden: me.multifolder,
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
                return WT.getSizeString(parseInt(value));
            },
            filter: {}
        };
        dcols[n++]={//Attachment
			header: me.ms.imageTag('headerattach.gif',15,16),
            width: 30,
            sortable: false,
            menuDisabled: true,
            dataIndex: 'atts',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					if (value) tag=this.ms.imageTag('attach.gif',16,16,"border=0 style='cursor: pointer'");
					else tag=WT.globalImageTag('empty.gif',16,16,"border=0");
					return tag;
			},
			scope: me,
            filter: {}
            
        };
        dcols[n++]={//Flag
            header: me.ms.imageTag('headerflag.gif',15,16),
            width: 30,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'flag',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					var others="border=0";
					if (value!=='') tag=this.ms.imageTag("flag"+value+".gif",16,16,others);
					else tag=WT.globalImageTag('empty.gif',16,16,others);
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
                      id: 0,
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
			header: me.ms.imageTag('headermailnote.gif',15,16),
            width: 30,
            sortable: true,
            menuDisabled: true,
            dataIndex: 'note',
            renderer: function(value,metadata,record,rowIndex,colIndex,store) {
					var tag;
					if (value) tag=this.ms.imageTag('mailnote.gif',16,16,"border=0 style='cursor: pointer'");
					else tag=WT.globalImageTag('empty.gif',16,16,"border=0");
					return tag;
			},
			scope: me,
            filter: {}
            
        };
        
        if (me.arch) {
            dcols[n++]={//Archived
				header: me.ms.imageTag('headerdocmgt.png',16,16),
                width: 30,
                sortable: true,
                menuDisabled: true,
                dataIndex: 'arch',
                hidden: me.multifolder,
                renderer: function(value,metadata,record,rowIndex,colIndex,store) {
						var tag;
						var others="border=0";
						if (value) tag=this.ms.imageTag("docmgt.png",16,16,others);
						else tag=WT.globalImageTag('empty.gif',16,16,others);
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
		config=config||{};
		if(!folder_id) return;
		Ext.applyIf(config, {
			start: 0,
			limit: 50,
			refresh: 1
		});

		//TODO: sort info
        //this.store.sortInfo=null;
		//TODO: baseParams??
		//this.store.baseParams = {service: 'mail', action: 'ListMessages', folder: folder_id};
		this.store.load({
			params: {
				start: config.start, limit: config.limit, refresh: config.refresh, folder: folder_id
			}
		});
		//this.render();
		this.currentFolder = folder_id;
	},
	
    res: function(s) {
        return this.ms.res(s);
    }
	
});