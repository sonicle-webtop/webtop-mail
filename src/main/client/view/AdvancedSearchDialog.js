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

Ext.define('Sonicle.webtop.mail.view.AdvancedSearchDialog', {
	extend: 'WTA.sdk.DockableView',
	
	dockableConfig: {
		iconCls: 'wt-icon-search-adv-xs',
		title: '{act-advsearch.lbl}',
		width: 700,
		height: 500
	},
	promptConfirm: false,
	full: true,
	
	mys: null,
	
	folder: null,
	folderText: null,
	folderHasChildren: false,

    running: false,
    searchButton: null,
    searchCombo: null,
	searchTrashSpam: null,
    sSearchCheckbox: null,
    searchToolbar: null,
    searchGrid: null,
    messageGrid: null,
    searchLabel: null,
    searchProgress: null,
    
	initComponent: function() {
		var me = this;
		
		me.searchToolbar=new Ext.Toolbar({
			items: [
				Ext.create("Ext.form.Label", { text: WT.res('folder')+':' }),
				' ',
				me.searchCombo=Ext.create("Ext.form.ComboBox",{
					forceSelection: true,
					width: 300,
					listWidth: 300,
					mode: 'local',
					displayField: 'desc',
					triggerAction: 'all',
					//selectOnFocus: true,
					editable: false,
					store: new Ext.data.SimpleStore({
						fields: ['id', 'desc'],
						data: [
							[ 'folder:'+me.folder, me.folderText ],
							[ 'all', me.mys.res('allfolders')],
							[ 'personal', me.mys.res('personalfolders')],
							[ 'shared', me.mys.res('sharedfolders')]
						]
					}),
					value: (me.folder==me.mys.getFolderInbox())?'all':'folder:'+me.folder,
					valueField: 'id',
					listeners: {
						'select': {
							fn: function(cb,r,ix) {
							  if (r.get('id').startsWith('folder:')) me.searchCheckbox.show();
							  else me.searchCheckbox.hide();
							}
						}
					}
				}),
				'-',
				me.searchTrashSpam=Ext.create('Ext.form.Checkbox',{ boxLabel: me.mys.res('searchtrashspam'), checked: false }),
				'->',
				me.searchCheckbox=Ext.create('Ext.form.Checkbox',{ boxLabel: me.mys.res('searchsubfolders'), checked: me.folderHasChildren, disabled: !me.folderHasChildren}),'->'
			]
		});
			
		Ext.apply(me, {
			
			tbar: me.searchToolbar,		
			buttons: [
				me.searchButton=Ext.create('Ext.Button',{ width: 100, text: WT.res('search'), handler: me.runstopAdvSearch, scope: me }),' ',
				me.searchLabel=Ext.create('Ext.form.Label',{ html: '<img src=webtop/ext/resources/images/default/grid/loading.gif>', hidden: true }),
				' ',
				me.searchProgress=Ext.create('Ext.form.Label',{ width: 200, hidden: true, text: '0%' }),
				'->',
				Ext.create('Ext.Button',{ width: 100, text: me.mys.res('savesearchfolder'), disabled: true}),
				' ',
				Ext.create('Ext.Button',{ width: 100, text: WT.res('cancel'), handler: me.advSearchCancel, scope: me })
			]
		});
		
		me.callParent(arguments);
		
		me.add({
				xtype: 'panel',
				region: 'center',
				autoScroll: true,
				layout: 'fit',
				items: [
					me.searchGrid=Ext.create('Sonicle.webtop.mail.view.AdvancedSearchGrid',{
						mys: me.mys,
                        autoScroll: true,
                        region: 'center',
                        searchfields: [
                            [ 'subject', WT.res('mail','subject')],
                            [ 'from', WT.res('mail','from')],
                            [ 'to', WT.res('mail','to')],
                            [ 'cc', WT.res('mail','cc')],
                            [ 'tocc', WT.res('mail','tocc')],
                            [ 'alladdr', WT.res('mail','fromtoccbcc')],
                            [ 'date', WT.res('mail','date')],
                            [ 'body', WT.res('mail','message')],
                            [ 'priority', WT.res('mail','priority')],
                            [ 'status', WT.res('mail','status')],
                            [ 'flags', WT.res('mail','flags')],
                            [ 'any', WT.res('mail','anywhere')]
                        ],
                        searchdata: {
                            'subject': null,
                            'from': null,
                            'to': null,
                            'cc': null,
                            'tocc': null,
                            'alladdr': null,
                            'date': {
                                xtype: 'datefield'
                            },
                            'body': null,
                            'priority': {
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
                                        ['1',WT.res('mail','high')],
                                        ['3',WT.res('mail','normal')]
                                    ]
                                }),
                                //value: this.method,
                                valueField: 'id'
                            },
                            'status': {
                                xtype: 'soiconcombo',
                                forceSelection: true,
                                mode: 'local',
                                displayField: 'desc',
                                iconClsField: 'icon',
                                triggerAction: 'all',
                                //selectOnFocus: true,
                                editable: false,
                                store: new Ext.data.SimpleStore({
                                    fields: ['id','desc','icon'],
                                    data: [
                                        ['replied',WT.res('mail','streplied'),'iconStatusReplied'],
                                        ['forwarded',WT.res('mail','stforwarded'),'iconStatusForwarded'],
                                        ['read',WT.res('mail','stread'),'iconStatusRead'],
                                        ['new',WT.res('mail','strecent'),'iconStatusNew']
                                    ]
                                }),
                                //value: this.method,
                                valueField: 'id'
                            },
                            'flags': {
                                xtype: 'soiconcombo',
                                forceSelection: true,
                                mode: 'local',
                                displayField: 'desc',
                                iconClsField: 'icon',
                                triggerAction: 'all',
                                //selectOnFocus: true,
                                editable: false,
                                store: new Ext.data.SimpleStore({
                                    fields: ['id','desc','icon'],
                                    data: [
                                        ['red',WT.res('mail','flagred'),'iconFlagRed'],
                                        ['blue',WT.res('mail','flagblue'),'iconFlagBlue'],
                                        ['yellow',WT.res('mail','flagyellow'),'iconFlagYellow'],
                                        ['green',WT.res('mail','flaggreen'),'iconFlagGreen'],
                                        ['orange',WT.res('mail','flagorange'),'iconFlagOrange'],
                                        ['purple',WT.res('mail','flagpurple'),'iconFlagPurple'],
                                        ['complete',WT.res('mail','flagcomplete'),'iconFlagComplete']
                                    ]
                                }),
                                //value: this.method,
                                valueField: 'id'
                            },
							'any': null
                        }
                      }),
                      me.messageGrid=Ext.create('Sonicle.webtop.mail.MessageGrid',{
                            height: 250,
                            split: true,
                            region:'south',
                            multifolder: true,
                            reloadAction: 'PollAdvancedSearch',
                            localSort: true,
                            loadMask: null,
                            mys: me.mys
                      })
				]
		});
		
		
	}
		
});
