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

Ext.define('Sonicle.webtop.mail.view.AdvancedSearchGrid',{
	extend: 'Ext.panel.Panel',

    frame: true,
    layout: 'form',
    padding: 5,
	andorGroup: null,
	
	mys: null,

    searchfields: [
        [ 'field1', 'Field1'],
        [ 'field2', 'Field2'],
        [ 'field3', 'Field3']
    ],
    searchdata: {
        'field1': null,
        'field2': {
            xtype: 'combo',
            forceSelection: true,
            mode: 'local',
            displayField: 'desc',
            triggerAction: 'all',
            //selectOnFocus: true,
            editable: false,
            store: new Ext.data.SimpleStore({
                fields: ['id','desc'],
                data: [
                    ['1','One'],
                    ['2','Two'],
                    ['3','Three']
                ]
            }),
            //value: this.method,
            valueField: 'id'
        },
        'field3': null
    },

    initComponent: function() {
		var me=this;
		me.andorGroup=new Ext.form.RadioGroup({
			items: [
				{ boxLabel: me.mys.res('advsearch-and'), name: 'andor', inputValue: 'and', width: 300, checked: true },
				{ boxLabel: me.mys.res('advsearch-or'), name: 'andor', inputValue: 'or', width: 300 }
			]
		});
		me.tbar=new Ext.Panel({
			layout: 'hbox',
			items: me.andorGroup
		});

		me.callParent(arguments);

		var se=me.addEntry();
		se.disableMinusButton();
    },

    addEntry: function(se) {
		var me=this;
		if (me.items && me.items.getCount()==1) {
			me.items.first().enableMinusButton();
		}
		var id=me.searchfields[0][0];
		var newse=Ext.create('Sonicle.webtop.mail.view.AdvancedSearchEntry',{
			mys: me.mys,
			searchfields: me.searchfields,
			searchdata: me.searchdata,
			field: id,
			method: 'contains'
		});
		newse.on('plus',me.addEntry,me);
		newse.on('minus',me.delEntry,me);

		me.add(newse);
		me.doLayout();

		return newse;
    },

    delEntry: function(se) {
		var me=this;
		
		me.remove(se,true)
		if (me.items && me.items.getCount()==1) {
			me.items.first().disableMinusButton();
		}
		me.doLayout();
    },

    getAndOr: function() {
        return this.andorGroup.getValue().el.dom.value;
    },

    getEntries: function() {
        return this.items;
    }

});

Ext.define('Sonicle.webtop.mail.view.AdvancedSearchEntry',{
	extend: 'Ext.panel.Panel',
	
    border: false,
    bodyBorder: false,
    layout: 'hbox',
    padding: 10,
    width: 600,
    height: 40,

    searchfields: null,
    searchdata: null,
    field: 'field1',
    method: 'contains',

    cbField: null,
    cbMethod: null,
    cbValue: null,
    plusButton: null,
    minusButton: null,
    pValue: null,

    mdatafull: null,
    mdatashort: null,
	
	mys: null,

    initComponent: function() {
		var me=this;
		
		me.callParent(arguments);

        //me.addEvents('plus','minus');

        me.mdatafull=[
            ['contains',me.mys.res('advsearch-contains')],
//            ['startswith',WT.res('advsearch','startswith')],
//            ['is',WT.res('advsearch','is')],
//            ['isnot',WT.res('advsearch','isnot')],
//            ['endswith',WT.res('advsearch','endswith')]
            ['notcontains',me.mys.res('advsearch-notcontains')]
        ];

        this.mdatashort=[
            ['is',me.mys.res('advsearch-is')],
            ['isnot',me.mys.res('advsearch-isnot')]
        ];

        this.mdatadate=[
            ['is',me.mys.res('advsearch-is')],
            ['since',me.mys.res('advsearch-since')],
            ['upto',me.mys.res('advsearch-upto')]
        ];

        var sf=me.searchfields,
			sd=me.searchdata,
			comps=new Array();
	
        for(var i=0;i<sf.length;++i) {
            var f=sf[i][0],
				d=sd[f],
				comp=null;
		
            if (d) {
                var conf={};
                Ext.apply(conf,d,{ id: me.id+"-"+f, allowBlank: false });
                comp=Ext.create(conf);
                comp.setWidth(200);
            } else {
                comp=Ext.create('Ext.form.TextField',{ id: me.id+"-"+f, width: 200, value: '', allowBlank: false });
            }
            comps[i]=comp;
        }

        me.pValue=Ext.create("Ext.panel.Panel",{
            border: false,
            bodyBorder: false,
            activeItem: 0,
            bodyStyle: 'padding:0px',
            width: 200,
            //height: 40,
            defaults: {
                border:false
            },
            layout: 'card',
            padding: 0,
            items: comps
        });

        me.cbField=Ext.create('Ext.form.ComboBox',{
            forceSelection: true,
            width: 150,
            mode: 'local',
            displayField: 'desc',
            triggerAction: 'all',
            //selectOnFocus: true,
            editable: false,
            store: new Ext.data.SimpleStore({
                fields: ['id','desc'],
                data: sf
            }),
            value: me.field,
            valueField: 'id'
        });
        me.cbField.on('select',me.fieldSelected,me);
        me.cbMethod=Ext.create('Ext.form.ComboBox',{
            forceSelection: true,
            width: 100,
            mode: 'local',
            displayField: 'desc',
            triggerAction: 'all',
            //selectOnFocus: true,
            editable: false,
            store: new Ext.data.SimpleStore({
                fields: ['id','desc'],
                data: me.mdatafull
            }),
            value: me.method,
            valueField: 'id'
        });

        me.plusButton=Ext.create('Ext.Button',{ 
			text:'+', width: 30, handler: function() {
				me.fireEvent('plus',me);
			}
		});
        me.minusButton=Ext.create('Ext.Button',{ 
			text:'-', width: 30, handler: function() {
				me.fireEvent('minus',me);
			}
		});

        me.add(me.cbField);
        me.add(me.cbMethod);
        me.add(me.pValue);
        me.add(me.plusButton);
        me.add(me.minusButton);
    },

    getEntryField: function() {
        return this.cbField.getValue();
    },

    getEntryMethod: function() {
        return this.cbMethod.getValue();
    },

    getEntryValue: function() {
        return this.pValue.getLayout().activeItem.getValue();
    },

    isEntryValid: function() {
        return this.pValue.getLayout().activeItem.validate();
    },

    enableMinusButton: function() {
        this.minusButton.enable();
    },
    
    disableMinusButton: function() {
        this.minusButton.disable();
    },

    fieldSelected: function(cb,r,i) {
		var me=this;
			id=r.get("id");
        me.pValue.layout.setActiveItem(this.id+"-"+id);
        var sd=me.searchdata[id];
        if (sd!=null && (sd.xtype=='combo'||sd.xtype=='soiconcombo') && sd.forceSelection) {
            me.cbMethod.getStore().loadData(this.mdatashort,false);
            me.cbMethod.setValue('is');
        } else if (sd!=null && sd.xtype=='datefield') {
            me.cbMethod.getStore().loadData(this.mdatadate,false);
            me.cbMethod.setValue('is');
        } else {
            me.cbMethod.getStore().loadData(this.mdatafull,false);
            me.cbMethod.setValue('contains');
        }
    }

});