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

Ext.define('Sonicle.webtop.mail.view.SmartSearchDialog', {
	extend: 'WTA.sdk.DockableView',
	
	dockableConfig: {
		iconCls: 'wtmail-icon-smartsearch-xs',
		title: '{smartsearch.tit}',
		width: 800,
		height: 600
	},
	promptConfirm: false,
	full: true,
	
	mys: null,
	pattern: null,
	
	searchToolbar: null,
	
	initComponent: function() {
		var me = this;
		
		me.pattern=me.pattern||'';
		
		me.searchToolbar=new Ext.Toolbar({
			items: [
				'->',
				Ext.create({
					xtype: 'wtsuggestcombo',
					reference: 'fldSearch',
					preventEnterFiringOnPickerExpanded: false,
					sid: me.mys.ID,
					suggestionContext: 'smartsearch',
					width: 400,
					tooltip: me.mys.res('smartsearch.tip'),
					value: me.pattern,
					triggers: {
						search: {
							hidden: false,
							cls: Ext.baseCSSPrefix + 'form-search-trigger',
							handler: function(tf) {
								me.runSearch(true);
							}
						}
					},
					listeners: {
						enterkey: function(tf,e) {
							me.runSearch(true);
						},
						select: function(tf,r,eopts) {
							me.runSearch(true);
						}
					}

				})
			]
		});
			
		Ext.apply(me, {
			
			tbar: me.searchToolbar,		
			buttons: [
			]
		});
		
		me.callParent(arguments);
		
		me.add({
			xtype: 'wtpanel',
			region: 'west',
			width: 200,
			layout: 'vbox',
			bodyPadding: '0 10 0 10',
			items: [
				{
					xtype: 'wtform',
					layout: 'vbox',
					width: "100%",
					title: me.mys.res('smartsearch-view.tit'),
					items: [
						{ 
							xtype: 'checkbox', 
							reference: 'chkFromMe', 
							boxLabel: me.mys.res('smartsearch-view-fromme',0), 
							listeners: { 
								change: function() {
									me.runSearch(false);
								}
							}
						},
						{ 
							xtype: 'checkbox', 
							reference: 'chkToMe', 
							boxLabel: me.mys.res('smartsearch-view-tome',0),
							listeners: { 
								change: function() {
									me.runSearch(false);
								}
							}
						},
						{ 
							xtype: 'checkbox', 
							reference: 'chkAttachments', 
							boxLabel: me.mys.res('smartsearch-view-attachments',0) ,
							listeners: { 
								change: function() {
									me.runSearch(false);
								}
							}
						}
					]
				},
				{
					xtype: 'grid',
					reference: 'gridPersons',
					title: me.mys.res('smartsearch-persons.tit'),
					hideHeaders: true,
					width: "100%",
					height: 200,
					cls: '',
					store: {
						fields: [{ name: 'type', type: 'int' }, 'name', 'email', { name: 'total', type: 'int' }],
						data: [
//							{ name: "Ezio Tirelli", email: "ezio.tirelli@encodata.it", total: 145 },
//							{ name: "Gabriele Bulfon", email: "gabriele.bulfon@sonicle.com", total: 98 }
						]
					},
					columns: [
						{ 
							dataIndex: 'name', flex: 1, tdCls: 'x-window-header-title-default wtmail-smartsearch-name-column',
							renderer: function(value,metadata,record,rowIndex,colIndex,store) {
								var t=record.get("type"),
									v=Ext.String.htmlEncode(value);
							
								if (t===1) v="<b>"+v+"</b>";
								else if (t==-1) v="<strike>"+v+"</strike>";
								return v;
							}
						},
						{ dataIndex: 'total', width: 50, tdCls: 'wtmail-smartsearch-total-column' }
					],					
					listeners: {
						rowclick: function(grid, r, tr, rowIndex, e, eopts) {
							me.confirmType("", r.get("type"), r.get("name"), "smartsearch.confirm.filterperson",function(bid) {
								if (bid === 'ok') {
									var target = WTA.Util.getCheckedRadioUsingDOM(['ftypenone', 'ftypeinclude', 'ftypeexclude']),
										rtype=target==="ftypenone"?0:target==="ftypeinclude"?1:-1;
									r.set("type",rtype);
									me.runSearch(false);
								}
							});
						}
					}
				},
				{
					xtype: 'grid',
					reference: 'gridMailFolders',
					title: me.mys.res('smartsearch-mailfolders.tit'),
					hideHeaders: true,
					width: "100%",
					height: 200,
					store: {
						fields: [ { name: 'type', type: 'int' }, 'id', 'name', { name: 'total', type: 'int' }],
						data: [
//							{ id: "Clienti/Encodata", name: "Encodata", total: 320 },
//							{ id: "Sonicle", name: "Sonicle", total: 120 }
						]
					},
					columns: [
						{ 
							dataIndex: 'name', flex: 1, tdCls: 'x-window-header-title-default wtmail-smartsearch-name-column',
							renderer: function(value,metadata,record,rowIndex,colIndex,store) {
								var t=record.get("type"),
									v=Ext.String.htmlEncode(value);
							
								if (t===1) v="<b>"+v+"</b>";
								else if (t==-1) v="<strike>"+v+"</strike>";
								return v;
							}
						},
						{ dataIndex: 'total', width: 50, tdCls: 'wtmail-smartsearch-total-column' }
					],
					listeners: {
						rowclick: function(grid, r, tr, rowIndex, e, eopts) {
							me.confirmType("", r.get("type"), r.get("name"), "smartsearch.confirm.filterfolder", function(bid) {
								if (bid === 'ok') {
									var target = WTA.Util.getCheckedRadioUsingDOM(['ftypenone', 'ftypeinclude', 'ftypeexclude']),
										rtype=target==="ftypenone"?0:target==="ftypeinclude"?1:-1;
									r.set("type",rtype);
									me.runSearch(false);
								}
							});
						}
					}
				}
			]
			
		});
		me.add({
			xtype: 'container',
			region: 'center',
			layout: 'border',
			id: 'panel1',
			items: [
				{
					xtype: 'wtform',
					reference: 'panelGraph',
					layout: 'border',
					region: 'north',
					height: 300,
					collapsible: true,
					collapsed: true,
					collapseMode: 'header',
					title: me.mys.res('smartsearch-search.tit',me.pattern),
					tools: [
						{
							type: 'graph', handler: function() { me.lref("panelGraph").toggleCollapse(); }
						},
						{
							type: 'empty', handler: function() { me.lref("panelGraph").toggleCollapse(); }
						},
						{
							type: 'empty', handler: function() { me.lref("panelGraph").toggleCollapse(); }
						}
					],
					items: [
					{
							xtype: 'cartesian',
							reference: 'yearsChart',
							region: 'center',
							store: {
								fields: [ { name: 'year', type: 'int' }, 'month', { name: 'day', type: 'int' }, { name: 'total', type: 'int' }],
								data: [
								]
							},
							plugins: {
								ptype: 'chartitemevents',
								moveEvents: true
							},
							axes: [{
								type: 'numeric',
								position: 'left',
								titleMargin: 20,
/*								majorTickSteps: 1,
								minimum: 0,
								label: {
									renderer: function (v) {
										return v.toFixed(0); 
									}
								}*/
							}, {
								type: 'category',
								position: 'bottom'
							}],
							animation: Ext.isIE8 ? false : true,
							series: {
								type: 'bar',
								xField: 'year',
								yField: 'total',
								style: {
									minGapWidth: 20
								},
								highlight: {
									strokeStyle: 'black',
									fillStyle: 'gold'
								}//,
								//label: {
								//	field: 'highF',
								//	display: 'insideEnd',
								//	renderer: 'onSeriesLabelRender'
								//}
							},
							listeners: { // Listen to itemclick events on all series.
								itemclick: function (chart, item, event) {
									var year=item.record.get("year"),
										month=item.record.get("month");
								
									if (year>0) {
										me.runSearch(false,{ year: year });
									}
									else if (month) {
										me.runSearch(false,{ year: chart.sprites[0].text, month: Ext.Date.getMonthNumber(month)+1 });
									}
								}
							}
						}
					]
				},
				{
					xtype: 'wtpanel',
					layout: 'border',
					region: 'center',
					id: 'panel4',
					tbar: [
						{ xtype: 'label', reference: 'labelResultTotals', html: me.mys.res('smartsearch-resulttotals',0,0) },
						' ',
						' ',
						{ xtype: 'button', reference: 'buttonOpenInFolder', text: me.mys.res('smartsearch-act-openinfolder'), disabled: true }/*,
						'->',
						me.mys.res('smartsearch-sortby.lbl')*/
					],
					items: [
						{
							xtype: 'grid',
							reference: 'gridMessages',
							hideHeaders: true,
							region: 'center',
							store: {
								fields: [	{ name: 'uid', type: 'int' }, 'folderid', 'subject', 'from', 'to', 'date', 'text' ],
								data: [
//									{ uid: 1, folderid: "Clienti/Encodata", subject: "Fwd: verifica utenti", from: 'Gabriele Bulfon', date: '10/05/2013' },
//									{ uid: 2, folderid: "Sonicle", subject: "Re: push wt5", from: 'Matteo Albinola', date: '13/09/2016' }
								]
							},
							columns: [
								{ dataIndex: 'subject', flex: 1, tdCls: 'x-window-header-title-default wtmail-smartsearch-name-column' },
								{ dataIndex: 'date', width: 160 }
							],
							features: [{
								ftype: 'rowbody',
								getAdditionalData: function (data, idx, record, orig) {
									var eto=Ext.String.escape(record.get("to")),
										to=Ext.String.htmlEncode(record.get("to")),
										from=Ext.String.htmlEncode(record.get("from")),
										text=Ext.String.htmlEncode(record.get("text"));
									
									
									return {
										rowBody:"<div style='padding: 1em; width: 200px; overflow: ellipsis; white-space: nowrap' data-qtip='"+eto+"'>"+
													'<b>'+me.mys.res("from")+':</b> '+from+'    '+
													'<b>'+me.mys.res("to")+':</b> '+to+'</div>'+
												'<pre style="padding: 1em">'+text+'</pre>',
										rowBodyCls: "smartsearch-gridbody"
									};
								}
							}],
							listeners: {
								rowdblclick: function(grid, r, tr, rowIndex, e, eopts) {
									me.openMessage(r);
								},
								selectionchange: function(grid, recs, eopts) {
									me.lref("buttonOpenInFolder").setDisabled(!recs || recs.length===0);
								}
							}
						}
					]
				}
			]
		});
	},
	
	runSearch: function(clear,timedata) {
		var me=this,
			minlen=3,
			pattern=me.lref("fldSearch").getValue(),
			personfilters=clear?{ is: [], isnot: [] }:me.getFilters("gridPersons","email"),
			folderfilters=clear?{ is: [], isnot: [] }:me.getFilters("gridMailFolders","id");
	
		if (pattern.length<minlen) {
			WT.error(me.mys.res("smartsearch.error.shortword"),minlen);
			return;
		}
	
		me.lref("panelGraph").setTitle(me.mys.res('smartsearch-search.tit',pattern+" (...0%...)"));
		//me.wait(WT.res("loading"));
		
		WT.ajaxReq(me.mys.ID, 'RunSmartSearch', {
			params: {
				pattern: pattern,
				fromme: me.lref("chkFromMe").getValue(),
				tome: me.lref("chkToMe").getValue(),
				attachments: me.lref("chkAttachments").getValue(),
				ispersonfilters: personfilters.is,
				isnotpersonfilters: personfilters.isnot,
				isfolderfilters: folderfilters.is,
				isnotfolderfilters: folderfilters.isnot,
				year: timedata?timedata.year:null,
				month: timedata?timedata.month:null,
				day: timedata?timedata.day:null
			},
			callback: function(success,json) {
				//me.unwait();
				if (success) {
					if (!me.polltask) me.polltask=new Ext.util.DelayedTask();
					me.polltask.delay(1000, me.doPolling, me);
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
	doPolling: function() {
		var me=this;
	
		WT.ajaxReq(me.mys.ID, 'PollSmartSearch', {
			callback: function(success,json) {
				if (success) {
					var pg=me.lref("panelGraph");
					me.updateTotals(json.data);
					if (!json.data.finished) {
						me.polltask.delay(1000, me.doPolling, me);
						pg.setTitle(me.mys.res('smartsearch-search.tit',pattern+" (..."+json.data.progress+"%...)"));
					} else {
						var pattern=me.lref("fldSearch").getValue();
						pg.setTitle(me.mys.res('smartsearch-search.tit',pattern));
					}
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
	getFilters: function(lref,idfield) {
		var me=this,
			pstore=me.lref(lref).getStore(),
			filters={ is: [], isnot: [] };
	
		pstore.each(function(r) {
			var t=r.get("type");
			if (t!==0) {
				var a=(t===1)?filters.is:filters.isnot;
				a[a.length]=r.get(idfield);
			}
		});
		
		return filters;
	},
	
	updateTotals: function(data) {
		var me=this;
		
		me.lref("chkFromMe").setBoxLabel(me.mys.res('smartsearch-view-fromme',data.viewFromMe));
		me.lref("chkToMe").setBoxLabel(me.mys.res('smartsearch-view-tome',data.viewToMe));
		me.lref("chkAttachments").setBoxLabel(me.mys.res('smartsearch-view-attachments',data.viewAttachments));		
		me.lref("labelResultTotals").setHtml(me.mys.res('smartsearch-resulttotals',data.visibleRows,data.totalRows));
		
		var pstore=me.lref("gridPersons").getStore();
		pstore.removeAll();
		Ext.each(data.persons,function(p) {
			pstore.add({ type: p.type, name: p.name, email: p.email, total: p.total });
		});
		
		var fstore=me.lref("gridMailFolders").getStore();
		fstore.removeAll();
		Ext.each(data.folders, function(f) {
			fstore.add({ type: f.type, id: f.id, name: f.name, total: f.total });
		});
		
		var mstore=me.lref("gridMessages").getStore();
		mstore.removeAll();
		Ext.each(data.messages, function(m) {
			mstore.add({ uid: m.uid, folderid: m.folderid, subject: m.subject, from: m.from, to: m.to, date: m.date, text: m.text });
		});

		var chart=me.lref("yearsChart"),cstore=chart.getStore();
		cstore.removeAll();
		if (data.day>0) {
			
		}
		else if (data.month>0) {
			chart.getSeries()[0].setXField("day");
			chart.setSprites({ type: 'text', text: ''+Ext.Date.getShortMonthName(data.month-1)+' '+data.year, fontSize: 22, width: 100, height: 30, x: 100, y: 20 });
			Ext.each(data.years, function(y) {
				if (data.year===y.year) {
					Ext.each(y.months, function(m) {
						if (data.month===m.month) {
							Ext.each(m.days, function(d) {
								cstore.add({ day: ''+d.day, total: d.total });
							});
							return false;
						}
					});
					return false;
				}
			});
		}
		else if (data.year>0) {
			chart.getSeries()[0].setXField("month");
			chart.setSprites({ type: 'text', text: ''+data.year, fontSize: 22, width: 100, height: 30, x: 100, y: 20 });
			Ext.each(data.years, function(y) {
				if (data.year===y.year) {
					Ext.each(y.months, function(m) {
						cstore.add({ month: Ext.Date.getShortMonthName(m.month-1), total: m.total });
					});
					return false;
				}
			});
		}
		else {
			chart.getSeries()[0].setXField("year");
			chart.setSprites();
			Ext.each(data.years, function(y) {
				cstore.add({ year: y.year, total: y.total });
			});
		}
		chart.redraw();
	},
	
    openMessage: function(r) {
		var me=this;
		
		var win=WT.createView(me.mys.ID,'view.DockableMessageView',{
			viewCfg: {
				mys: me.mys,
				folder: r.get('folderid'),
				idmessage: r.get('uid'),
				title: r.get('subject'),
				messageGrid: me.mys.messagesPanel.folderList
			}
		});
		win.show(false,function() {
			win.getView().showMessage();
		});
			
    },
	
	confirmType: function(msg, id, value, resprefix, cb, scope, opts) {
		var me = this, html;
		html = "</br></br>"
				+ "<table width='70%' style='font-size: 12px'>"
				+ "<tr><td><input type='radio' name='filtertype' id='ftypenone' "+(id===0?"checked='true'":"")+"/></td><td width='95%'>"+me.mys.res(resprefix+".none",value)+"</td></tr>"
				+ "<tr><td><input type='radio' name='filtertype' id='ftypeinclude' "+(id===1?"checked='true'":"")+"/></td><td width='95%'>"+me.mys.res(resprefix+".include",value)+"</td></tr>"
				+ "<tr><td><input type='radio' name='filtertype' id='ftypeexclude' "+(id===-1?"checked='true'":"")+"/></td><td width='95%'>"+me.mys.res(resprefix+".exclude",value)+"</td></tr>"
				+ "</table>";
		WT.confirm(msg + html, cb, scope, Ext.apply({
			buttons: Ext.Msg.OKCANCEL
		}, opts));
	},
	
	
	
	
		
});
