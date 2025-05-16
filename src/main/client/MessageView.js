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
 * details.cl
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

Ext.define('Sonicle.webtop.mail.MessageViewModel', {
    extend: 'WTA.ux.data.BaseModel',
    idProperty: 'id',
    fields: [
		{ name: "id", type: 'int' },
		{ name: "iddata", type: 'string' },
		{ name: "value1", type: 'string' },
		{ name: "value2", type: 'string' },
		{ name: "value3", type: 'int' },
		{ name: "value4", type: 'string' }
	]
});


Ext.define('Sonicle.webtop.mail.MessageView',{
	extend: 'Ext.Component',
	uses: [
		'Sonicle.form.field.InitialsAvatar',
		'Sonicle.grid.column.Avatar'
	],
	mixins: [
		'WTA.mixin.Waitable'
	],
	
    bodyBorder: false,
    border: false,
    cls: 'wtmail-mv-main',
    dockable: false,
	
    //Elements
    tdHeader: null,
	tdHeaderInnerTable: null,
    divSubject: null,
    divDate: null,
    divFromName: null,
	divThroughName: null,
    divTos: null,
    divBccs: null,
    divCcs: null,
    divAttach: null,
	divPas: null,
	divTags: null,
	divICal: null,
    divLine: null,
	colAvatar: null,
    tdBody: null,
    divBody: null,
    tableContents: null,
    iframes: null,
    htmlparts: null,
    cleared: true,
    
    //Message data
    folder: null,
    idmessage: null,
    subject: '',
    date: '',
    scheddate: '',
    fromName: '',
    fromEmail: '',
	throughName: '',
	throughEmail: '',
    toNames: null,
	toNamesFull: null,
    toEmails: null,
    ccNames: null,
	ccNamesFull: null,
    bccNames: null,
	bccNamesFull: null,
    ccEmails: null,
    ntos: 0,
    nccs: 0,
    attachments: null,
    receipt: null,
    receiptTo: null,
    latestId: null,
	
	emailMenu: null,
	attachMenu: null,
	p7mAttachMenu: null,
    
    mys: null,
	
    initComponent: function() {
		var me=this;
		
		me.callParent(arguments);
		me.proxy=Ext.create(
				"Ext.data.proxy.Ajax",
				WTF.proxyReader(me.mys.ID,'GetMessage','message',{ 
					model: 'Sonicle.webtop.mail.MessageViewModel',
					listeners: {
						exception: Ext.emptyFn
					}
				})
		);
        
        me.addListener('resize',me.viewResized,me);

        var t=document.createElement("table");
        t.className="wtmail-mv-table";
        me.el=Ext.get(t);
		
		me.colAvatar = Ext.create('Sonicle.grid.column.Avatar', {
			
			//Define getName to return the sender name
			getName: function() {
				return me.fromName;
			}
		});
    },
	
	saveInCloud: function(data) {
		var me = this;
		//TODO: provide a better loading indicator (maybe a such sort of background task)
		me.wait();
		WT.ajaxReq(me.mys.ID, "SaveFileToCloud", {
			params: {
				fileId: data.fileId,
				path: data.path,
				storeId: data.storeId,
				folder : data.folder,
				idAttach : data.idAttach,
				idMessage : data.idMessage
			},
			callback: function(success, json) {
				if (success) {
					Ext.defer(function() {
						me.unwait();
					}, 500);
				} else {
					WT.error(json.message);
				}
			}
		});
	},
	
	onRender: function(parentNode, containerIdx) {
		var me=this;
		
        var t=me.el.dom;
      
        var tr=t.insertRow(-1);
        var td=tr.insertCell(-1);
        td.className="wtmail-mv-header";
        me.tdHeader=Ext.get(td);

		tr=t.insertRow(-1);
        td=tr.insertCell(-1);
        td.className="wtmail-mv-bodycell";
        me.tdBody=Ext.get(td);
		me.addEmptyEl();
		me.callParent(arguments);
	},
	
    removeElement: function(el) {
        //var p=el.parent();
        //if (p) p.dom.removeChild(el.dom);
		el.destroy();
    },
    
    _clear: function(noEmptyMsg, selCount) {
		var me=this;
		
        if (!me.cleared) {
            me.removeElement(me.divSubject);
            me.removeElement(me.divDate);
            me.removeElement(me.divFromName);
			if (me.divThroughName) {
				me.removeElement(me.divThroughName);
				me.divThroughName = null;
			}
			//TODO un click email
            //me.divFromName.un('click', WT.app.emailElementClicked, me.divFromName);

            me.removeElement(me.divTos);
            me.removeElement(me.divCcs);
            me.removeElement(me.divBccs);
            me.removeElement(me.divAttach);
			if (me.divPas) me.removeElement(me.divPas);
            me.removeElement(me.divTags);
			me.removeElement(me.divICal);
            me.removeElement(me.divLine);
			me.removeElement(Ext.get(me.tdHeaderInnerTable));
            var bd=me.divBody.dom;
            if ( bd.hasChildNodes() ) {
                while ( bd.childNodes.length > 0 ){
                    bd.removeChild( bd.firstChild );
                }
            }


            me.removeElement(me.divBody);
            me.tableContents=null;
            me.htmlparts=null;
            me.cleared=true;
            me.idmessage=null;
			me.model=null;
			me.icalmethod=null;
			me.icaluid=null;
			me.icalwebtopid=0;
            me.iframes=new Array();
			if(me.tdBody !== null) {
				me.clearBody(noEmptyMsg, selCount);
			}	
        } else {
			if (noEmptyMsg === true) me.clearEmptyEl();
			else if(me.tdBody !== null) {
				me.clearBody(noEmptyMsg, selCount);
			}	
		}
    },
	
	addEmptyEl: function(selCount) {
		var me = this, html;
		if (me.tdBody && !me.emptyEl) {
			if (selCount) {
				html = '<div class="wt-centered">'
					+ '<div class="wt-pane-body-title wt-theme-text-color-title">'
					+ Ext.String.htmlEncode(me.mys.res('messagePreview.count.tit', selCount))
					+ '</div>'
					+ '</div>';
			} else {
				html = '<div class="wt-centered">'
					+ '<div class="wt-pane-body-title wt-theme-text-color-title">'
					+ Ext.String.htmlEncode(me.mys.res('messagePreview.no.tit'))
					+ '</div>'
					+ '<div class="wt-pane-body-subtitle wt-theme-text-color-subtitle">'
					+ Ext.String.htmlEncode(me.mys.res('messagePreview.no.txt'))
					+ '</div>'
					+ '</div>';
			}
			me.emptyEl = Ext.core.DomHelper.insertHtml('beforeEnd', me.tdBody.dom, html);
		}
	},
	
	clearEmptyEl: function() {
		var el = this.emptyEl;
		if (el) Ext.removeNode(el);
		this.emptyEl = null;
	},
	
	clearBody: function(noEmptyMsg, selCount) {
		var me = this, dom;
		me.clearEmptyEl();
		if (me.tdBody !== null) {
			dom = me.tdBody.dom;
			if (dom && dom.hasChildNodes() ) {
				while (dom.childNodes.length > 0) {
					dom.removeChild(dom.firstChild);
				}
			}
		}
		if (noEmptyMsg !== true) me.addEmptyEl(selCount);
	},
	
	showMessage: function(data) {
		// At the moment, the purpose of this method is providing a way to
		// show the empty element, this is always called with NULL data!
		// I hope this component will be fully reviewed in future!
		this.addEmptyEl();
	},
	
    _showMessage: function(acct, folder, id, setseen, rec, nopec) {
		var me=this;/*,
			idmessage=id,
			params={service: me.mys.ID, action: 'GetMessage', folder: folder, idmessage: idmessage };*/
	
        //if (this.folder==folder && this.idmessage==idmessage) return;
        me._clear(true);
        me.idmessage=id;
		me.acct=acct;
        me.folder=folder;
        me.latestId=id;
		me.model=rec;
		me.proxy.abort();
		WTU.applyExtraParams(me.proxy,{ account: acct,  folder: folder, idmessage: id, setseen: setseen });
		if (nopec) WTU.applyExtraParams(me.proxy,{ nopec: true });
		me.proxy.doRequest(
			me.proxy.createOperation('read',{
				url: WTF.requestBaseUrl(),
				//params: params,
				callback: me.messageRead,
				scope: me
			})
		);
    },
	
    messageRead: function(r, op, success) {
		var me=this;
        if (op && success) {
			var extraParams=op.getProxy().getExtraParams(),
				provider=extraParams.provider,
				providerid=extraParams.providerid;
            if (!provider) {
                if (extraParams.folder!==me.folder || extraParams.idmessage!==me.idmessage) return;
            }
            me.htmlparts=new Array();
            me.toNames=null;
			me.toNamesFull=null;
            me.toEmails=null;
            me.ccNames=null;
			me.ccNamesFull=null;
            me.bccNames=null;
			me.bccNamesFull=null;
            me.ccEmails=null;
            me.attachments=null;
			me.tags=null;
            me.receipt=null;
            me.receiptTo=null;
            me.scheddate=null;
            me.ntos=0;
            me.nccs=0;
            me.nbccs=0;
			me.workflow=false;
			me.messageid=null;
			me.listUnsubscribe=null;
            Ext.each(r,me.evalRecord,me);
			me.tags=me.proxy.getReader().rawData.tags;
			me.pec=me.proxy.getReader().rawData.pec;
			me.pas=me.proxy.getReader().rawData.pas;
			
			
			var div=document.createElement("div");
			div.className="wtmail-mv-hsubject";
			me.divSubject=Ext.get(div);

			div=document.createElement("div");
			div.className="wtmail-mv-hdate";
			me.divDate=Ext.get(div);

			div=document.createElement("div");
			div.className="wtmail-mv-hfromname";
			me.divFromName=Ext.get(div);

			if (me.throughEmail) {
				div=document.createElement("div");
				div.className="wtmail-mv-hthroughname";
				me.divThroughName=Ext.get(div);
			}
			
			me.divTos=Ext.get(document.createElement("div"));
			me.divTos.addCls("wtmail-mv-hto");

			me.divCcs=Ext.get(document.createElement("div"));
			me.divCcs.addCls("wtmail-mv-hcc");

			me.divBccs=Ext.get(document.createElement("div"));
			me.divBccs.addCls("wtmail-mv-hbcc");

			me.divAttach=Ext.get(document.createElement("div"));
			me.divAttach.addCls("wtmail-mv-hattach");
			
			if (me.pas && !me.pas.isSenderTrusted && 
					(me.icalmethod || me.pas.externalLinkHosts || me.pas.dangerousExtensions || me.pas.hasZipAttachment)) {
						me.divPas=Ext.get(document.createElement("div"));
						me.divPas.addCls("wtmail-mv-pas");
						
						var pasText=WTF.imageTag(me.mys.ID,"pas-attention_16.svg",16,16,"valign=center")+"&nbsp;&nbsp;";
						if (me.icalmethod) {
							pasText+=Ext.String.htmlEncode(me.res("pas.text.untrusted-invite"));
						} else {
							pasText+=Ext.String.htmlEncode(me.res("pas.text.untrusted-sender"))+
									"<ul style='list-style-type: disc;'>";
							if (me.pas.externalLinkHosts) {
								pasText+="<li><b>"+Ext.String.htmlEncode(me.res("pas.text.external-links"))+"</b>";
								Ext.each(me.pas.externalLinkHosts, function(host) {
									pasText+=" "+host;
								});
								pasText+="</li>";

							}
							if (me.pas.dangerousExtensions) {
								pasText+="<li><b>"+Ext.String.htmlEncode(me.res("pas.text.dangerous-extensions"))+"</b>";
								Ext.each(me.pas.dangerousExtensions, function(ext) {
									pasText+=" "+ext;
								});
								pasText+="</li>";
							}
							if (me.pas.hasZipAttachment) {
								pasText+="<li><b>"+Ext.String.htmlEncode(me.res("pas.text.zip-attachments"))+"</b> "+
										Ext.String.htmlEncode(me.res("pas.text.zip-content-may-be-dangerous"))+
										"</li>";
							}
							pasText+="</ul>";
						}
						me.divPas.update(pasText);
			}
			
			me.divTags=Ext.get(document.createElement("div"));
			me.divTags.addCls("wtmail-mv-tags");
			
			me.divICal=Ext.get(document.createElement("div"));
			me.divICal.addCls("wtmail-mv-hical")
		
			div=document.createElement("div");
			div.className="wtmail-mv-hline";
			me.divLine=Ext.get(div);
			
			div=document.createElement("div");
			div.className="wtmail-mv-bodydiv";
			me.divBody=Ext.get(div);
			
			//TODO workflow
			if (me.workflow) {
			/*
                WT.alert(
                    "Workflow",
                    me.mys.res('alert-workflow')
                );
			*/
			}
			//no receipt if workflow
            else if (me.receipt) {
				if (me.receipt==="ask") {
					WT.confirm(me.mys.res("receipt.confirm"),function(btn) {
						if (btn=='yes') {
							me.sendReceipt(true);
						}
						else if (btn=='no') {
							me.sendReceipt(false);
						}
					});
				}
				else if (me.receipt==="always") {
					me.sendReceipt(true);
				}
            }
            
            var tdh=me.tdHeader,
				tdb=me.tdBody,
				laf=WT.getVar('laf');
            
            var htmlDate=me.date;
            if (me.scheddate) {
                htmlDate+="&nbsp;&nbsp;-&nbsp;&nbsp;"+WTF.imageTag(me.mys.ID,"mail-schedule_16.svg",16,16,"valign=bottom")+"&nbsp;&nbsp;"+me.scheddate;
            }
			//TODO workflow
			/*
            if (me.workflow) {
                htmlDate+="&nbsp;&nbsp;-&nbsp;&nbsp;<a href='javascript:Ext.emptyFn()' workflow=1><img src='webtop/themes/"+laf+"/mail/workflow.png' valign=bottom width=16 height=16>&nbsp;&nbsp;Workflow</a>";
            }*/
            me.divDate.update(htmlDate);
			//TODO workflow
			/*
			Ext.each(me.divDate.query("a[workflow]"),
				function(o) { 
					var el=Ext.get(o);
					el.on("click",
						function(e,t,o) { 
							this.winReadMailWorklow(this.wkfsubject,this.wkfdescription,this.wkfdate,this.wkfdeadline,this.wkfcontact,this.fromEmail,this.messageid,this.wkffrom,this.fromName,this.wkfdaterequest);
							e.stopEvent(); 
							return false;
						},
						this
					);
				},
				this
			);*/

			var tdPEC=(me.pec?"<td style='width:24px; padding-right:8px; background-repeat: no-repeat' class='wtmail-pec'><a data-qtip='Visualizza busta completa' data-qtitle='Posta Certificata' href='javascript:Ext.emptyFn()' certificata=1 style='text-decoration:none'>&nbsp;&nbsp;&nbsp;</a></td>":"");
			var tdPasSpam="";
			if (me.pas && Ext.isDefined(me.pas.isSpam)) {
				var threshold1=me.pas.spamThreshold/2;
				var tdPasSpamClass=
						(me.pas.spamThreshold==0)?"wtmail-icon-pas-spam-unknown":
							(me.pas.spamScore<threshold1)?"wtmail-icon-pas-spam-green":
								(me.pas.spamScore<me.pas.spamThreshold)?"wtmail-icon-pas-spam-yellow":"wtmail-icon-pas-spam-red";
				var qtip=(me.pas.spamThreshold==0)?
						me.res('pas.text.spam-unknown'):
								me.res('pas.text.spam-score')+"&nbsp;:&nbsp;"+me.pas.spamScore+"&nbsp;/&nbsp;"+me.pas.spamThreshold;
						
				tdPasSpam="<td style='width:24px; height:24px; padding-right:8px; background-repeat: no-repeat' "+
						"data-qtip='"+qtip+"' "+
						"class='"+tdPasSpamClass+"'></td>"; 
			}
			me.divSubject.update(
				"<table class='wtmail-mv-subject-table'><tr>"+
				tdPEC+
				"<td>"+Ext.String.htmlEncode(me.subject)+"</td>"+
				//tdPasSpam+
				"</tr></table>"
			);
			
			Ext.each(me.divSubject.query("a[certificata]"),function(o) { 
				Ext.get(o).on("click",
					function(e,t,o) { 
						var win=WT.createView(me.mys.ID,'view.DockableMessageView',{
							viewCfg: {
								mys: me.mys,
								acct: me.acct,
								folder: me.folder,
								idmessage: me.idmessage,
								title: me.subject,
								model: me.model,
								messageGrid: me.mys.messagesPanel.folderList,
								nopec: true
							}
						});
						win.show(false,function() {
							win.getView().showMessage();
						});
						
						e.stopEvent(); 
						return false;
					}
				);
			});
			//TODO: update tooltips (necessary???)
            //me.divFromName.set({ 'data-qtitle': me.fromName, 'data-qtip': me.fromEmail })
			
			var canMarkAsTrusted=false;
			var divPasSender="";
			if (me.pas) {
				canMarkAsTrusted=!me.pas.isSenderTrusted && !me.pas.isSpam && !me.pas.hasForgedSender;
				
				var qTitle=Sonicle.String.htmlAttributeEncode(me.pas.isSenderTrusted?me.res("pas.sender.trusted"):me.res("pas.sender.untrusted"));
				var qTip="";
				if (me.pas.isSpam) qTip=me._addPasTip(qTip, "spam", me.pas.isSpam);
				if (me.pas.hasForgedSender) qTip=me._addPasTip(qTip, "forged-sender", me.pas.hasForgedSender);
				if (me.pas.isNewsletter) qTip=me._addPasTip(qTip, "newsletter", me.pas.isNewsletter);
				if (Ext.isDefined(me.pas.isSenderMyDomain)) qTip=me._addPasTip(qTip, "mydomain", me.pas.isSenderMyDomain);
				if (Ext.isDefined(me.pas.isSenderFrequent)) qTip=me._addPasTip(qTip, "frequent", me.pas.isSenderFrequent);
				if (Ext.isDefined(me.pas.isSenderAnyContact)) qTip=me._addPasTip(qTip, "anycontact", me.pas.isSenderAnyContact);
				if (Ext.isDefined(me.pas.isSenderDisplaynameConsistentWithContact)) qTip=me._addPasTip(qTip, "contact-consistent", me.pas.isSenderDisplaynameConsistentWithContact, true);
				if (Ext.isDefined(me.pas.isSenderFakePattern)) qTip=me._addPasTip(qTip, "fake-pattern", me.pas.isSenderFakePattern);
				
				if (canMarkAsTrusted) {
					qTip+="<br><br>"+me.res("pas.sender.clicktotrust");
				}
				divPasSender="&nbsp;<div "+
						"data-qtitle='" + qTitle + "' "+
						"data-qtip='" + qTip + "' "+
						"class='wtmail-icon-pas-"+
							(me.pas.isSpam?"danger":me.pas.isSenderTrusted?"ok":"warning")
						+"' style='display:inline-block; width: 16px;height: 16px;"+(canMarkAsTrusted?"cursor: pointer;":"")+"'></div>";
			}
			
			var divListUnsubscribe="";
			if (me.listUnsubscribe) {
				var prefix=me.listUnsubscribe.substring(0,6).toLowerCase();
				if (prefix == 'mailto') {
					var ix = me.listUnsubscribe.indexOf('?'),
						email = ix >0 ? me.listUnsubscribe.substring(7,ix) : me.listUnsubscribe.substring(7),
						params = ix > 0 ? Ext.Object.fromQueryString(me.listUnsubscribe.substring(ix+1)):null,
						subject = params ? (params.subject ? params.subject : '') : '',
						body = params ? (params.body ? params.body : '') : '',
						sparams="'"+email+"','"+subject+"','"+body+"'";
					divListUnsubscribe="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class='wtmail-unsubscribe-link' href='javascript:Ext.emptyFn()' onclick=\"parent.WT.handleMailAddress(" + sparams + "); return false;\">"+me.res("unsubscribe")+"</a>";
				} else { //is http
					divListUnsubscribe="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class='wtmail-unsubscribe-link' href='"+me.listUnsubscribe+"' target='_blank'>"+me.res("unsubscribe")+"</a>";
				}
			}
			
            me.divFromName.update(
				"<table class='wtmail-mv-from-table'><tr>"+
				"<td>"+
					"<a data-qtip='" + me.fromEmail + "' data-qtitle='" + Sonicle.String.htmlAttributeEncode(me.fromName) + "' href='javascript:Ext.emptyFn()'>" +
						Ext.String.htmlEncode(me.fromName) + " &lt;" + me.fromEmail + "&gt;" + 
					"</a>"+
					divPasSender+
					divListUnsubscribe+
				"</td>"+
				tdPasSpam+
				"</tr></table>"
			);
	
			if (me.divThroughName) {
				me.divThroughName.update(
					"<span class='wtmail-mv-hlabelthrough'>" + me.mys.res('through') + ":&nbsp;</span>"+
					"<a data-qtip='" + me.throughEmail + "' data-qtitle='" + Sonicle.String.htmlAttributeEncode(me.throughName) + "' href='javascript:Ext.emptyFn()'>" +
						Ext.String.htmlEncode(me.throughName) + " &lt;" + me.throughEmail + "&gt;" + "</a><br>" +
					"<span class='wtmail-mv-hlabelthrough'>" + me.mys.res('date') + ":&nbsp;</span>" +
						me.throughDate
				);
			}

			//prepare email menu
			var capi=WT.getServiceApi("com.sonicle.webtop.contacts"),
				i=0, actions=new Array();
	
			actions[i++]=new Ext.Action({text: me.mys.res("emailmenu.writeemail"), handler: function() {
				var el=me.emailMenu.activeElement;
				me._startNewMessage(el.recDesc,el.recEmail);
			}, iconCls: 'wtmail-icon-mailNew'});
			if (capi) {
				actions[i++]=new Ext.Action({text: me.mys.res("emailmenu.addcontact"), handler: function() {
					var el=me.emailMenu.activeElement,
						email=el.recEmail,
						desc=(el.recDesc===el.recEmail)?null:el.recDesc;
						firstName=null,
						lastName=null;

					firstName=desc;

					if (desc!=null) {
						var ix=desc.lastIndexOf(' ');
						if (ix>0) {
							firstName=desc.substring(0,ix);
							lastName=desc.substring(ix+1);
						}
					}

					capi.addContact({
						firstName: firstName,
						lastName: lastName,
						workEmail: email
					}, {
						dirty: true
					});
				}, iconCls: 'wtcon-icon-newContact'});
			}
			actions[i++]=new Ext.Action({text: me.mys.res("emailmenu.createrule"), handler: function() {
				var ae=me.emailMenu.activeElement;
				me.createRule(ae.recEmail,ae.recType);
			}, iconCls: 'wtmail-icon-addMailFilter'});
			actions[i++]=new Ext.Action({text: WT.res("act-copy.lbl"), handler: function() {
				var ae=me.emailMenu.activeElement;
				Sonicle.ClipboardMgr.copy(ae.dom.innerText);
			}, iconCls: 'wt-icon-copy'});
			if (canMarkAsTrusted) {
				actions[i++]=new Ext.Action({text: me.mys.res("emailmenu.marktrusted"), handler: function() {
					var ae=me.emailMenu.activeElement;
					me.markEmailAsTrusted(ae.recEmail,ae.recType);
				}, iconCls: 'wtmail-icon-pas-ok'});
			}
			if (me.mys.isMainAccountInbox(me.acct, me.folder)) {
				actions.push('-');
				actions.push(new Ext.Action({
					text: me.mys.res("emailmenu.blocksender"),
					iconCls: 'wt-icon-block',
					handler: function() {
						var ae=me.emailMenu.activeElement;
						me.blockSenderAddressUI(ae.recEmail);
					}
				}));
			}	
			me.emailMenu=new Ext.menu.Menu({
				items: actions
			});


			//create attach menu
			var p7mActions=new Array(), j=0;
			i=0;
			actions=new Array();
			
			//this only for p7m attachments
			p7mActions[j++]=new Ext.Action({
				text: me.mys.res("attachmenu.p7m"),
				handler: function(mi) {
					var el=mi.parentMenu.activeElement;
					if (el.linkSave) me.p7mDecryptAndOpen(el.linkSave);
				},
				scope: me,
				iconCls: 'wtmail-icon-p7m-unlocked'
			});
			p7mActions[j++]='-';
			
			p7mActions[j++]=actions[i++]=new Ext.Action({
				text: me.mys.res("attachmenu.save"),
				handler: function(mi) {
					var el=mi.parentMenu.activeElement;
					if (el.linkSave) window.open(el.linkSave);
				},
				scope: me,
				iconCls: 'wt-icon-save'
			});
			
			p7mActions[j++]=actions[i++]=new Ext.Action({
				text: me.mys.res("attachmenu.saveall"),
				handler: function(mi) {
					var el=mi.parentMenu.activeElement;
					if (el.linkSaveAll) window.open(el.linkSaveAll);
				},
				scope: me,
				iconCls: 'wt-icon-saveAll'
			});
			p7mActions[j++]=actions[i++]='-';
			
			p7mActions[j++]=actions[i++]=new Ext.Action({text: me.mys.res("attachment.savetocloud"),
				disabled: !WT.getApp().hasDescriptor('com.sonicle.webtop.vfs'),
				handler: function(mi) {
					var el = mi.parentMenu.activeElement;
					if (el.linkSave) {
						var api = WT.getApp().getServiceApi('com.sonicle.webtop.vfs');
						api.chooseFolder({
							callback: function(s, file) {
								if (s === false) return;
								me.saveInCloud({
									fileId : file.fileId,
									path : file.path,
									storeId : file.storeId,
									folder : el.attachItem.folder,
									idAttach : el.attachItem.idAttach,
									idMessage : el.attachItem.idMessage
								});
							}
						});
					}
				},
				scope:me, iconCls: 'wt-icon-save'
			});
			
			me.attachMenu=new Ext.menu.Menu({ items: actions });
			me.p7mAttachMenu=new Ext.menu.Menu({ items: p7mActions });
			
	
            tdh.insertFirst(me.divLine);
			
			if (me.divPas) tdh.insertFirst(me.divPas);
			
			
			if (me.tags) {
				var tagsHtml="";
				Ext.iterate(me.tags, function(tag) {
					var r=me.mys.tagsStore.findRecord('id',tag);
					if (r) {
						tagsHtml+="<span style='background-color: "+r.get("color")+"'>&nbsp;"+r.get("name")+"&nbsp;</span>&nbsp;"
					}
				});
				me.divTags.update(tagsHtml);
				tdh.insertFirst(me.divTags);
			}

			me.tdHeaderInnerTable=document.createElement("table");
			me.tdHeaderInnerTable.className="wtmail-mv-table";
			tdh.insertFirst(me.tdHeaderInnerTable);
			var tdhTr=me.tdHeaderInnerTable.insertRow(-1);
			var tdhTdAvatar=tdhTr.insertCell(-1);
			tdhTdAvatar.className="wtmail-mv-header wtmail-mv-header-avatar";
			var tdhDivAvatar=document.createElement("div");
			Ext.get(tdhDivAvatar).update(
				//Reuse Avatar column code to create avatar div with colors and initials.
				//Passes true instead of rec to force code to call getName function
				//wich is defined during colAvatar creation.
				me.colAvatar.buildHtml(me.fromName, true)
			);
			tdhDivAvatar.className="so-avatar-wrap so-avatar-initials wtmail-mv-avatar";
			tdhTdAvatar.appendChild(tdhDivAvatar);
			
			var tdhTdData=Ext.get(tdhTr.insertCell(-1));
			
            if (me.attachments) {
                var names=null,
					atts=me.attachments,
					len=atts.length,
					allparams=null;
                if (!provider) {
                    allparams={
                        folder: me.folder,
                        idmessage: me.idmessage,
                        ids: []
                    }
                } else {
                    allparams={
                        provider: provider,
                        providerid: providerid,
                        ids: []
                    }
                }
                var ids=allparams.ids;
                var vparams=[];
                for(var i=0;i<len;++i) {
                    var att=atts[i];
                    var name=att.name;
                    var imgclass=WTF.fileTypeCssIconCls(WTA.Util.getFileExtension(name));
                    var aparams;
                    if (!provider) {
                        aparams={
							acct: me.acct,
                            folder: me.folder,
                            idmessage: me.idmessage,
                            idattach: att.id,
							fileName: name
                        }
                    } else {
                        aparams={
                            provider: provider,
                            providerid: providerid,
                            idattach: att.id
                        }
                    }
                    vparams[i]=aparams;
                    ids[ids.length]=att.id;
                    var ssize=Sonicle.String.humanReadableSize(att.size);
					var doEdit=WT.getVar('docServerEnabled')&&att.editable;
					var qtip=WT.res("act-open.lbl");
					//var href=doEdit?WTF.processUrl(me.mys.ID,"EditAttachment",aparams):WTF.processBinUrl(me.mys.ID,"GetAttachment",aparams);
					var href=WTF.processBinUrl(me.mys.ID,"GetAttachment",aparams);
                    if (Ext.isIE) href+="&saveas=1";
					var docedit=null;
					if (doEdit) docedit=" docedit='"+Ext.Object.toQueryString(aparams)+"'";
                    var ics=null;
                    if (name.toLowerCase().endsWith(".ics")) ics=" ics='"+Ext.Object.toQueryString(aparams)+"'";
					var vcf = null;
					if(name.toLowerCase().endsWith(".vcf")) vcf = " vcf='" + Ext.Object.toQueryString(aparams)+"'";
					var p7m = null;
					if(name.toLowerCase().endsWith(".p7m")) {
						p7m = " p7m='" + Ext.Object.toQueryString(aparams)+"'";
						imgclass = 'wtmail-icon-p7m-locked';
						qtip = me.mys.res("attachment.p7m.tip");
					}
                    var eml=null;
                    if (att.eml) eml=" eml='"+Ext.Object.toQueryString(aparams)+"'";
                    var html="<a href='"+href.replaceAll("'","%27")+"' target='_blank'"
								+" filename='"+Sonicle.String.htmlAttributeEncode(name)+"'"
								+" data-qtip='"+qtip+"'"+
								+(ics != null ? ics : "")
								+(eml != null ? eml : "")
								+(vcf !== null ? vcf : "")
								+(p7m !== null ? p7m : "")
								+(docedit != null ? docedit : "")+
							"><div class='"+imgclass+"' style='display:inline-block;width:16px;height:16px'></div>&nbsp;<span>"+Ext.String.htmlEncode(name)+"</span>&nbsp;("+ssize+")</a>";
                    names=me.appendAttachmentName(names,html);
                }
				var allhref=WTF.processBinUrl(me.mys.ID,"GetAttachments",allparams);
                me.divAttach.update("<span class='wtmail-mv-hlabelattach'><a data-qtip='"+me.mys.res('saveall-desc')+"' data-qtitle='"+me.mys.res('saveall')+" ("+len+")' href='"+allhref.replaceAll("'","%27")+"' filename='*'><i class='fas fa-paperclip'></i></a>&nbsp;&nbsp;</span>"+names);
				
				if (WT.getApp().getService('com.sonicle.webtop.calendar')) {
					
					var aics=me.divAttach.query("a[ics]");
					
					if (me.icalmethod) {
						var icalhtml=null,
							txtaction=me.res("ical.action"),
							txtaccept=me.res("ical.accept"),
							txtdecline=me.res("ical.decline"),
							//txtconfirm=ms.res("ical-confirm"),
							txtcancel=me.res("ical.cancel"),
							txtupdate=me.res("ical.update");
						if (me.icalmethod==="REQUEST") {
							if (me.icalwebtopid<0) {
								icalhtml="<DIV class='wtmail-mv-hicalmessage'>"+me.res("ical.invite.top.message")+".&nbsp;&nbsp;&nbsp;"+
										"<a ext:qtip='"+txtaccept+"' ext:qtitle='"+txtaction+"' href='javascript:Ext.emptyFn()' ical='accept'>&nbsp;"+txtaccept+"&nbsp;</a>&nbsp;&nbsp;&nbsp;"+
										"<a ext:qtip='"+txtdecline+"' ext:qtitle='"+txtaction+"' href='javascript:Ext.emptyFn()' ical='decline'>&nbsp;"+txtdecline+"&nbsp;</a>&nbsp;&nbsp;&nbsp;"+
										//"<a ext:qtip='"+txtconfirm+"' ext:qtitle='"+txtaction+"' href='javascript:Ext.emptyFn()' ical='later'>&nbsp;"+txtconfirm+"&nbsp;</a>"+
										"</DIV>";
							} else if (me.icalwebtopid===0) {
								icalhtml="<DIV class='wtmail-mv-hicalmessage'>"+me.res("ical.modified.top.message")+".&nbsp;&nbsp;&nbsp;"+
										"<a ext:qtip='"+txtupdate+"' ext:qtitle='"+txtaction+"' href='javascript:Ext.emptyFn()' ical='update'>&nbsp;"+txtupdate+"&nbsp;</a>&nbsp;&nbsp;&nbsp;"+
										"</DIV>";
							} else {
								icalhtml="<DIV class='wtmail-mv-hicalmessage'>"+me.res("ical.processed.top.message")+"."+
										"</DIV>";
							}
						} else if (me.icalmethod==="REPLY") {
							if (this.icalwebtopid==0) {
								icalhtml="<DIV class='wtmail-mv-hicalmessage'>"+me.res("ical.reply.top.message")+".&nbsp;&nbsp;&nbsp;"+
										"<a ext:qtip='"+txtupdate+"' ext:qtitle='"+txtaction+"' href='javascript:Ext.emptyFn()' ical='updatereply'>&nbsp;"+txtupdate+"&nbsp;</a>&nbsp;&nbsp;&nbsp;"+
										"</DIV>";
							} else if (this.icalwebtopid>0) {
								icalhtml="<DIV class='wtmail-mv-hicalmessage'>"+me.res("ical.reply.top.message")+"."+
										"</DIV>";
							}
						} else if (me.icalmethod==="CANCEL") {
							if (me.icalwebtopid>=0) {
								icalhtml="<DIV class='wtmail-mv-hicalmessage'>"+me.res("ical.cancel.top.message")+".&nbsp;&nbsp;&nbsp;"+
										"<a ext:qtip='"+txtcancel+"' ext:qtitle='"+txtaction+"' href='javascript:Ext.emptyFn()' ical='cancel'>&nbsp;"+txtcancel+"&nbsp;</a>&nbsp;&nbsp;&nbsp;"+
										"</DIV>";
							} else {
								icalhtml="<DIV class='wtmail-mv-hicalmessage'>"+me.res("ical.processed.top.message")+"."+
										"</DIV>";
							}
						}

						if (icalhtml) {
							me.divICal.update(icalhtml);
							tdhTdData.insertFirst(me.divICal);

							var xel=aics[0];
							Ext.each(me.divICal.query("a[ical]"),function(o) { 
								Ext.get(o).on("click",function(e,t,o) { 
									var act=t.getAttribute('ical');
									//WT.debug('action='+act+" ics="+xel.getAttribute("ics"));
									me.actionCalendarEvent(act,xel,xel.getAttribute("ics")); 
									e.stopEvent(); 
									return false;
								},me );
							},me);
						}
					} else {
						Ext.each(aics,function(o) { 
							Ext.get(o).on("click",function(e,t,o) { 
								var xel=t; //sometimes returns SPAN or IMG instead of A
								if (t.tagName==="SPAN"||t.tagName==="IMG") xel=t.parentElement;
								me.importCalendarEvent(xel.getAttribute("ics")); 
								e.stopEvent(); 
								return false;
							},me );
						},me);
					}
                }
				
				if (WT.getVar('docServerEnabled')) {
					var adocedit=me.divAttach.query("a[docedit]");
						Ext.each(adocedit,function(o) { 
							Ext.get(o).on("click",function(e,t,o) { 
								var xel=t; //sometimes returns SPAN or IMG instead of A
								if (t.tagName==="SPAN"||t.tagName==="IMG") xel=t.parentElement;
								me.viewFile(xel.getAttribute("docedit"));
								e.stopEvent(); 
								return false;
							},me );
						},me);
				}

				
                tdhTdData.insertFirst(me.divAttach);
				
				
                Ext.each(me.divAttach.query("a[eml]"),function(o) { 
					Ext.get(o).on("click",function(e,t,o) { 
						var xel=t; //sometimes returns SPAN or IMG instead of A
						if (t.tagName==="SPAN"||t.tagName==="IMG") xel=t.parentElement;
						me.showEml(xel,xel.getAttribute("eml")); 
						e.stopEvent(); 
						return false;
					},me );
				},me);
				
				Ext.each(me.divAttach.query("a[vcf]"),function(o) {
					Ext.get(o).on("click",function(e,t,o) { 
						var xel=t; //sometimes returns SPAN or IMG instead of A
						if (t.tagName==="SPAN"||t.tagName==="IMG") xel=t.parentElement;
						var params = Ext.Object.fromQueryString(xel.getAttribute("vcf"));
						me.confirmImportAttachAsContact(params.folder, params.idmessage, params.idattach);
						e.stopEvent(); 
						return false;
					},me );
				},me);
				
				Ext.each(me.divAttach.query("a[p7m]"),function(o) {
					Ext.get(o).on("click",function(e,t,o) { 
						var xel=t; //sometimes returns SPAN or IMG instead of A
						if (t.tagName==="SPAN"||t.tagName==="IMG") xel=t.parentElement;
						me.p7mConfirmDecryptAndOpen(xel);
						e.stopEvent(); 
						return false;
					},me );
				},me);
				
				//if pas and has dengerous extensions or zip, add prompt
				if (me.pas && (me.pas.dangerousExtensions||me.pas.hasZipAttachment)) {
					Ext.each(me.divAttach.query("a"),function(o) {
						var xel=Ext.get(o),
							fname=xel.getAttribute("filename"),
							ix=fname.lastIndexOf('.'),
							fext=(ix>=0)?fname.substring(ix+1):'',
							dexts=me.pas.dangerousExtensions;
						if (me.pas.hasZipAttachment) dexts=['zip'];
						if (fname==='*'||dexts.includes(fext.toLowerCase()))
							xel.on("click",function(e,t,o) {
								WT.confirm(me.res('pas.prompt-attachment'), function(bid) {
									if (bid === 'yes') window.open(xel.getAttribute("href"));
								},
								this, {
									config: { icon: Ext.Msg.WARNING  } 
								});
								e.stopEvent();
								return false;
							},me );
					},me);
				}
				
				//TODO setAttachElements
                me._setAttachElements(me.divAttach.first().next(),vparams);
            }
            if (me.bccNames) {
                me.divBccs.update("<span class='wtmail-mv-hlabelbcc'>"+me.mys.res('bcc')+":&nbsp;</span>"+me.bccNames);
                tdhTdData.insertFirst(me.divBccs);
                me._setEmailElements(me.divBccs.first().next(),'bcc');
            }
            if (me.ccNames) {
                me.divCcs.update("<span class='wtmail-mv-hlabelcc'>"+me.mys.res('cc')+":&nbsp;</span>"+me.ccNames);
                tdhTdData.insertFirst(me.divCcs);
				//TODO setEmailElements
                me._setEmailElements(me.divCcs.first().next(),'cc');
            }
            if (me.toNames) {
                me.divTos.update("<span class='wtmail-mv-hlabelto'>"+me.mys.res('to')+":&nbsp;</span>"+me.toNames);
                tdhTdData.insertFirst(me.divTos);
				//TODO setEmailElements
                me._setEmailElements(me.divTos.first().next(),'to');
            }

            tdhTdData.insertFirst(me.divDate);
			if (me.divThroughName) tdhTdData.insertFirst(me.divThroughName);
            tdhTdData.insertFirst(me.divFromName);
			
            tdh.insertFirst(me.divSubject);
            me.divBody.dom.style.width=tdh.dom.scrollWidth-5;//"100%";
            tdb.insertFirst(me.divBody);

			var divFromNameA=Ext.get(me.divFromName.query("a")[0]);
			var divFromNamePAS=divFromNameA.next();
            me._setEmailElement(divFromNameA,'from');
			if (me.divThroughName) {
				var divThroughNameA=Ext.get(me.divThroughName.query("a")[0]);
				me._setEmailElement(divThroughNameA,'from');
			}
			
			if (canMarkAsTrusted)
				divFromNamePAS.on('click', function() {
					me.markEmailAsTrusted(divFromNameA.getAttribute('data-qtip'));
				});

            
            var tc=document.createElement("table");
            tc.cellPadding=0;
            tc.cellSpacing=0;
            tc.border=0;
            tc.className="wtmail-mv-tablecontents";
            //tc.style.width="100%";
            me.tableContents=Ext.get(tc);
            me.divBody.dom.appendChild(tc);
            var len=me.htmlparts.length;
            var isIE=Ext.isIE;
            me.iframes=new Array();
            for(var xi=0;xi<len;++xi) {
                var xifname=me.id+"-iframe"+xi;
                //var xhtml="<iframe name='"+xifname+"' id='"+xifname+"' scrolling='no' frameborder=0 class='wtmail-mv-bodyiframe' height=0 onload='WT.iframeLoaded(\""+xifname+"\");'></iframe>";
                var xif=document.createElement("iframe");
                xif.scrolling="no";
                xif.frameBorder=0;
                xif.className="wtmail-mv-bodyiframe";
                xif.id=xifname;
                xif.name=xifname;
                var tcr=tc.insertRow(-1);
                var tcd=tcr.insertCell(-1);
                tcd.appendChild(xif);
                //tcd.innerHTML=xhtml;
                xif.wtView=me;
                xif.wtTcd=tcd;
                me.iframes[xi]=xif;
                if (!Ext.isIE) xif.onload=me.iframeLoaded.bind(xif);
                else {
                    //xif.attachEvent('onload',this.iframeLoaded.createDelegate(xif));
                    xif.onreadystatechange=me.iframeState.bind(xif);
                }
                
                var data=WTA.Util.getIFrameData(xifname),
					doc=data.doc,
					donedoc=false;
                if (doc) {
                    doc.open();
                    doc.write(me.htmlparts[xi]);
                    doc.close();
                    if (doc.body) {
						//if PAS enabled and sender is untrusted and we have external links
						// enable confirm on link click
						if (me.pas && !me.pas.isSenderTrusted && me.pas.externalLinkHosts) {
							Ext.each(Ext.get(doc.body).query("a"),function(o) { 
								var attrHref=o.attributes.getNamedItem('href');
								if (attrHref && attrHref.nodeValue!='#' && Ext.isEmpty(o.onclick)) {
									var href=o.href;
									o.onclick=function() { 
										parent.WT.handleConfirmAndFollowLink(href);
										return false;
									}
									//o.href='#';
								}
							},me);
						}
						
						me._fixTables(doc.body);
                        me.adjustIframe(xif.name);
                        donedoc=true;
                    }
                }
                if (!donedoc) {
                    var task=new Ext.util.DelayedTask(me.adjustIframe);
                    task.delay(200, me.adjustIframe, me, [xif.name,task]);
                }
            }

            var h,w;
            //if (!me.dockable) {
				var ct=me.ownerCt, ht=46, qt=ct.query('[cls=wtmail-messageview-toolbar-container]');
				if (qt && qt.length>0) ht = qt[0].getHeight();
				h = ct.getEl().getHeight() - ht;
				if (me.mys.hasAuditUI()) h -= 33;
                w=ct.getEl().getWidth();
                me.viewResized(me,w,h,w,h);
            //}
            
            //if (me.icalmethod) console.log("ical - method: "+me.icalmethod+" , uid: "+me.icaluid+" , webtopid: "+me.icalwebtopid);
			
            me.cleared=false;
            if (!provider) me.fireEvent('messageviewed',extraParams.idmessage,me.proxy.getReader().rawData.millis,me.workflow);
        }
    },
	
	_addPasTip: function(tip,resname,state, nobr) {
		var res=this.res("pas.sender."+(state?"":"not.")+resname);
		if (!Ext.isEmpty(tip)) {
			if (nobr) tip+="&nbsp;";
			else tip+=",<br>";
		} else {
			res=res.charAt(0).toUpperCase()+res.substring(1);
		}
		return tip+Ext.String.htmlEncode(res);
	},
	
	_fixTables: function(rootEl) {
		Ext.each(Ext.dom.Query.select("table",rootEl),function(el) {
			if (el.getAttribute('height') === '100%' || (el.style || {}).height === '100%') {
				el.setAttribute('height', '');
				Ext.get(el).setStyle('height', 'initial');
			}
		});
	},
	
    createRule: function(email,type) {
		var me = this, rfld, rval;
		if (type === 'from') {
			rfld = 'from';
			rval = email;
		} else if (type === 'to' || type === 'cc' || type === 'bcc') {
			rfld = 'to_cc';
			rval = email;
		}
		if (rfld && rval) {
			me.mys.editInMailFilters({
				addMailFilter: {
					rules: Ext.JSON.encode([{field: rfld, operator: 'contains', value: rval}])
				}
			});
		}
    },
	
	blockSenderAddressUI: function(address) {
		var me = this;
		WT.confirm(me.mys.res('message.confirm.blocksender', address), function(bid) {
			if ('ok' === bid) {
				me.blockSenderAddress(address, {
					callback: function(success, data, json) {
						if (success) WT.toast(me.mys.res('message.toast.blocksender.added', address));
						WT.handleError(success, json);
					}
				});
			}
		}, me, {
			buttons: Ext.Msg.OKCANCEL,
			config: {
				buttonText: {
					ok: me.mys.res('message.confirm.blocksender.ok')
				}
			}
		});
	},
	
	blockSenderAddress: function(address, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'BlockSenderAddress', {
			params: {
				address: address
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	markEmailAsTrusted: function(email,type) {
		var me = this;
		WT.confirm(me.mys.res("mark-email-as-trusted.confirm",email),function(btn) {
			if (btn=='yes') {
				WT.ajaxReq(me.mys.ID, 'MarkEmailAsTrusted', {
					params: {
						email: email
					},
					callback: function(success,json) {
						if (success) {
							var acct=me.acct,
								folder=me.folder,
								idmessage=me.idmessage,
								model=me.model;
							me._clear();
							me._showMessage(acct, folder, idmessage, false, model);
						} else {
							WT.error(json.message);
						}
					}
				});					
			}
		});
	},

    
	//TODO Workflow
	/*
    getStoreMailWorklowDelegate:function(){
       var sci=new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
            baseParams: {service:'mail',action:'GetMailWorkFlowContact',mode:'D'},
            autoLoad: true,
            reader: new Ext.data.JsonReader({
                root: 'get_contact',
                fields: ['id','value']
            })
      });  
      return sci;
    },
    
    
    answerMailWorklow:function(from,fromEmail,messageAnswer,date,answer,delegate,idmessage,subject,description,deadline,daterequest){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:WT.res("mail","wait")});
        this.waitMessage.show();
        WT.JsonAjaxRequest({
            url: 'ServiceRequest',
            params: {service:'mail',action:'AnswerMailWorkflow',
                messageAnswer:messageAnswer,
                from:from,
                fromEmail:fromEmail,
                date:date,
                answer:answer,
                delegate:delegate,
                idmessage:idmessage,
                subject:subject,
                description:description,
                deadline:deadline,
				dateRequest:daterequest,
				foldername: this.folder
            },
            method: "POST",
            callback: function(o){
                this.waitMessage.hide();
                if (o.success){
					this._hideWorkflowLink();
					this.ms.actionMarkSeen();
                }else{
                    WT.res('error');
                }
            },
            scope: this
        }); 
    },
	
	_hideWorkflowLink: function() {
		var me=this;
		Ext.each(me.divDate.query("a[workflow]"),
			function(o) { 
				var el=Ext.get(o);
				el.setStyle("visibility","hidden");
			},
			me
		);
	},
	
    winReadMailWorklow:function(subjectv,description,date,deadline,contact,fromEmail,idmessage,from,fromName,daterequest){
		WT.debug("daterequest="+daterequest);
        var request_from=new Ext.form.TextField({
            width: 400,
            allowBlank:false,
            fieldLabel: WT.res("mail","from"),
            value:fromName +" ("+from+")"+" - "+fromEmail,
            disabled:true
        });
        var subject=new WT.SuggestTextField({ 
            lookupService: 'mail',
            lookupContext: 'subject',
            width: 400,
            allowBlank:false,
            fieldLabel: WT.res("mail","subject"),
            value:subjectv,
            disabled:true
        });
        var dateDelivery=new Ext.form.DateField({
            id: 'datedelivery',
            minValue: new Date(),
            format: 'd/m/Y',
            fieldLabel: WT.res("mail","date-delivery.lbl"),
            disabled:deadline,
            value:date,
            allowBlank:false
        });
        var storeMailWorklowDelegate=this.getStoreMailWorklowDelegate();
        var delegateTo = new Ext.form.ComboBox({
            name: 'delegateto',
            fieldLabel: WT.res('mail','delegate-to.lbl'),
            width: 400,
            listWidth: 400,
            mode: 'remote',
            store: storeMailWorklowDelegate,
            triggerAction: 'all',
            displayField: 'value',
            valueField: 'id',
            selectOnFocus: true,
            autoSelect: false,
            minChars: 1,
            editable: true,
            loadingText: "",
            queryDelay: 500
         });
         var message_request = new Ext.form.TextArea({
            fieldLabel: WT.res('mail',"message_request.lbl"),
            name: 'message_request',
            width:400,
            height:100,
            readOnly:true,
            value:description
         });
         var message = new Ext.form.TextArea({
            fieldLabel: WT.res('mail',"message"),
            name: 'message',
            width:400,
            height:150,
            maxLength : 1000
         });
         var confirmButton=new Ext.Button({
           text:WT.res("mail","confirm.lbl"),
           scope:this,
           handler: function(b,e){
                if (formReadMailWorkflow.getForm().isValid()){
                    var m=message.getValue();
                    var date=dateDelivery.getValue();
                    var delegate=delegateTo.getValue();
                    this.answerMailWorklow(from,fromEmail,m,date,"C",delegate,idmessage,subject.getValue(),description,deadline,daterequest);
                    win.close();                  
                }
           }
         });
         var rejectButton=new Ext.Button({
           text:WT.res("mail","reject.lbl"),
           scope:this,
           handler: function(b,e){
                WT.confirm(WT.res("mail","reject.lbl"),WT.res("mail","sureprompt"),function(btn,text) {
                    if (btn=="yes") {
                        var m=message.getValue();
                        var date=dateDelivery.getValue();
                        var delegate=delegateTo.getValue();
                        this.answerMailWorklow(from,fromEmail,m,date,"R",delegate,idmessage,subject.getValue(),description,deadline,daterequest);
                        win.close();
                    }
                },this,false);
                    
           }
         });
         var formReadMailWorkflow=new Ext.FormPanel({
           id:'formReadMailWorkflow',
           labelWidth:150,
           bodyCssClass: "x-panel-mc", 
           labelAlign: 'right',
           border:false,
           bodyStyle:'padding:5px 5px 0',
           defaultType: 'textfield',
           background:'trasparent',
           width:400,
           height:450,
           request_from:request_from,
           dateDelivery:dateDelivery,
           delegateTo:delegateTo,
           message:message,
           message_request:message_request,
           buttonAlign:'center',
           items: [
               request_from,
               subject,
               dateDelivery,
               delegateTo,
               message_request,
               message
           ],
           buttons: [
               rejectButton,
               confirmButton
           ]
        });
        var win=WT.app.createWindow({
            width: 700,
            height: 440,
            iconCls: 'icon-mail-task',
            title: WT.res('mail','task.lbl'),
            layout: 'fit',
            modal:true,
            minimizable:false,
            maximizable:false,
            formMailWithWorkflow:formReadMailWorkflow,
            items: [
                formReadMailWorkflow
            ]
        });
        
        win.show();  
         
        
    },*/
	
    _setEmailElements: function(e,type) {
        while(e) {
            this._setEmailElement(e,type);
            e=e.next();
        }
    },

    _setEmailElement: function(e,type) {
        var desc=e.getAttribute('data-qtitle');
        var email=e.getAttribute('data-qtip');
        this.setEmailElement(e,desc,email,type);
    },

    _setAttachElements: function(el,vparams) {
        var me=this,i=0;
        while(el) {
            var href=el.first().dom.href,
					attachItem = {
						folder : vparams[i].folder,
						idMessage : vparams[i].idmessage,
						idAttach : vparams[i].idattach
					};
			//TODO isIE saveas=1?
            if (!Ext.isIE) href+="&saveas=1";
            me.setAttachElement(el,href,me.divAttach.first().first().dom.href, attachItem);
            
            me._setDD(el,vparams[i]);
            
            el=el.next();
            ++i;
        }
    }, 
    
    _setDD: function(el,params) {
		var me=this;
		new Ext.dd.DragZone(el,{
            params: params,
			//TODO: provide a standard and unique group name eg. 'wtmail-msgattachment'
            ddGroup : 'attachment', 
            getDragData: function(ev) {
				var ddel=el;
				if (ddel.contains(ev.getTarget())) {
					return {
						ddel: ddel.dom.cloneNode(true),
						params: params, // We cannot move to the new property below, we have to ensure compatibility with legacy code
						msgAttachment: {
							accountId: params.acct,
							folder: params.folder,
							msgId: params.idmessage,
							attachId: params.idattach,
							attachName: params.fileName
						},
						sourceEl: el,
						repairXY: Ext.fly(el).getXY(),
						records: []
					};
				}
            },
			
			getRepairXY: function() {
				return this.dragData.repairXY;
			}			
		});
    },

    iframeState: function() { //called in the context of the iframe element
		var xif=this;
        if (xif.readyState=="complete" && xif.wtView) {
            xif.wtView.adjustIframe(xif.name);
        }
    },

    iframeLoaded: function() { //called in the context of the iframe element
		var xif=this;
        if (xif.wtView) {
            xif.wtView.adjustIframe(xif.name);
        }
    },

    adjustIframe: function(xifname,task) {
		var me=this;
        var data=WTA.Util.getIFrameData(xifname),
			doc=data.doc,
			xif=data.iframe;
        if (!doc || !doc.body) {
            if (!task) task=new Ext.util.DelayedTask(me.adjustIframe);
            task.delay(200, me.adjustIframe, me, [xifname,task]);
            return;
        }

		me._fixTables(doc.body);
		
		if (!doc._xstyle_appended) {
			var xstyle=doc.createElement('style');
			xstyle.type='text/css';
			xstyle.appendChild(doc.createTextNode(
					'pre { white-space: pre-wrap !important; } '+
					'body { font-family: "Lucida Grande",Verdana,Arial,Helvetica,sans-serif; } '+
					'blockquote { display: block; margin-left: 5px; border-left: solid 2px blue; padding-left: 10px; } '+
					'img:not([width]), img:not([height]) { max-width: 100% !important; height: auto !important; } '
			));
			doc.head.appendChild(xstyle);
			doc._xstyle_appended=true;
		}
		
        var thebody=doc.body;
        thebody.style.padding="0px 0px 0px 0px";
        thebody.style.margin="0px 0px 0px 0px";
        //var newheight=thebody.scrollHeight;
        //var newwidth=thebody.scrollWidth;
        var newheight=thebody.offsetHeight;
        var newwidth=thebody.offsetWidth;
        if (newheight<thebody.scrollHeight) newheight=thebody.scrollHeight;
        if (newwidth<thebody.scrollWidth) newwidth=thebody.scrollWidth;
        me.setIframeSize(xif,data,newwidth,newheight);
        if (Ext.isIE) {
            if (!thebody.lastScrollHeight || thebody.lastScrollHeight!=newheight) {
                if (!thebody.wtAdjustCount) thebody.wtAdjustCount=1;
                else thebody.wtAdjustCount++;
                if (thebody.wtAdjustCount<500) {
                    thebody.lastScrollHeight=newheight;
                    if (!task) task=new Ext.util.DelayedTask(me.adjustIframe);
                    task.delay(200, me.adjustIframe, me, [xifname,task]);
                }
            }
        }
    },

    setIframeSize: function(xif,data,newwidth,newheight) {
        var tcd=xif.wtTcd;
        xif.height=newheight+30;
        if (newwidth<tcd.scrollWidth) {
			newwidth = tcd.scrollWidth -10;
		} else if (newwidth>=tcd.scrollWidth) {
            //try to force constrained width to window width
			newwidth = this.divBody.getSize().width -22;
        }
		//here iframe will force a layout to new constrained width
        xif.width=newwidth;
		//if new layout does not fit, something is keeping horizontal space
		//e.g. a large width image, so set iframe width to scroll width to
		//allow scrollbar to appear
		if (data.doc.body.scrollWidth>newwidth) xif.width=data.doc.body.scrollWidth;
    },
    
    evalRecord: function(item,index,allItems) {
		var me=this,
			iddata=item.get('iddata'),
			maxtos=me.mys.viewmaxtos||10,
			maxccs=me.mys.viewmaxccs||10,
			maxbccs=me.mys.viewmaxccs;
	
        //alert("iddata="+iddata);
        if (iddata=='subject') me.subject=item.get('value1');
        else if (iddata=='messageid') {me.messageid=item.get('value1');} 
        else if (iddata=='listUnsubscribe') {me.listUnsubscribe=item.get('value1');} 
        else if (iddata=='date') { me.date=item.get('value1'); }
        else if (iddata=='scheddate') { me.scheddate=item.get('value1'); }
        else if (iddata=='from') { me.fromName=item.get('value1'); me.fromEmail=item.get('value2') }
		else if (iddata=='through') { me.throughName=item.get('value1'); me.throughEmail=item.get('value2'); me.throughDate=item.get('value4') }
        else if (iddata=='to') {
			me.toNamesFull = me.appendEmail(me.toNamesFull,item.get('value1'),item.get('value2'));
            if (me.ntos<maxtos) {
                me.toNames=me.appendEmail(me.toNames,item.get('value1'),item.get('value2'));
            } else if (me.ntos==maxtos) {
                me.toNames=me.appendEmail(me.toNames,'...','...');
            }
            ++me.ntos;
        }
        else if (iddata=='cc') {
			me.ccNamesFull = me.appendEmail(me.ccNamesFull,item.get('value1'),item.get('value2'));
            if (me.nccs<maxccs) {
                me.ccNames=me.appendEmail(me.ccNames,item.get('value1'),item.get('value2'));
            } else if (me.nccs==maxccs) {
                me.ccNames=me.appendEmail(me.ccNames,'...','...');
            }
            ++me.nccs;
        }
        else if (iddata=='bcc') {
			me.bccNamesFull = me.appendEmail(me.bccNamesFull,item.get('value1'),item.get('value2'));
            if (me.nbccs<maxbccs) {
                me.bccNames=me.appendEmail(me.bccNames,item.get('value1'),item.get('value2'));
            } else if (me.nbccs==maxbccs) {
                me.bccNames=me.appendEmail(me.bccNames,'...','...');
            }
            ++me.nbccs;
        }
        else if (iddata=='attach' || iddata=='eml') {
            if (!me.attachments) me.attachments=new Array();
            var o={
				id: item.get('value1'), 
				name: item.get('value2'), 
				size: parseInt(item.get('value3')/4*3), 
				imgname: item.get('value4'), 
				eml: iddata=='eml',
				editable: item.get('editable')
			};
            me.attachments[me.attachments.length]=o;
        }
		else if (iddata=='html') {
            me.htmlparts[me.htmlparts.length]=item.get('value1');
        }
        else if (iddata=='receipt') {
            me.receipt=item.get('value1');
            me.receiptTo=item.get('value2');
		}
        else if (iddata=='ical') {
            me.icalmethod=item.get('value1');
            me.icaluid=item.get('value2');
			me.icalwebtopid=item.get('value3');
		}
		else if (iddata=='workflow1') {
			me.workflow=true;
			me.wkfsubject=item.get('value1');
			me.wkfdescription=item.get('value2');
			me.wkfcontact=item.get('value4');
		}
		else if (iddata=='workflow2') {
			me.wkfdate=item.get('value1');
			me.wkfdeadline=item.get('value2');
			me.wkffrom=item.get('value4');
        }
		else if (iddata=='workflow3') {
			me.wkfdaterequest=item.get('value1');
        }
		
    },
    
    appendEmail: function(name, desc, email) {
        if (name === null) name = "";
        else name += " - ";
        name += "<span data-qtip='" + email + "' data-qtitle='" + Sonicle.String.htmlAttributeEncode(desc) + "'><a data-qtip='" + email + "' data-qtitle='" + Sonicle.String.htmlAttributeEncode(desc) + "' href='javascript:Ext.emptyFn()'>" + Ext.String.htmlEncode(desc) + "</a></span>";
        return name;
    },

    appendAttachmentName: function(name, s) {
        if (name==null) name="";
        else name+=" - ";
        name+="<span>"+s+"</span>";
        return name;
    },
    
    viewResized: function(comp,aw,ah,rw,rh) {
		var me=this,
			hh=me.tdHeader.getHeight(),
			newh=ah-hh-18,
			neww=aw-10;
        me.el.setHeight(ah-2);
		if (!me.divBody || !me.divBody.dom) return;
		
        if (newh<0) newh=0;
        me.divBody.setHeight(newh);
        if (neww>0) {
            if (me.dockable) neww+=2;
            me.divBody.setWidth(neww);
        }
        if (me.iframes) {
            var len=me.iframes.length;
            for(var i=0;i<len;++i) {
                var xif=me.iframes[i];
                var data=WTA.Util.getIFrameData(xif.name);
                var thebody=data.doc.body;
                var newheight=thebody.scrollHeight;
                var newwidth=thebody.scrollWidth;
                this.setIframeSize(xif,data,newwidth,newheight);
            }
        }
    },

    sendReceipt: function(send) {
		var me=this,
			ident=me.mys.getFolderIdentity(me.folder),
			from=ident.displayName + " <" + ident.email + ">";
	
		WT.ajaxReq(me.mys.ID, 'SendReceipt', {
			params: {
				identityId: ident.identityId,
                subject: me.subject,
                to: me.receiptTo,
                folder: me.folder,
				messageid: me.messageid,
				bodyuserlang: me.mys.res("receipt.message", from, me.subject),
				send: send
			},
			callback: function(success,json) {
				if (success) {
				} else {
					WT.error(json.message);
				}
			}
		});					
    },

    print: function() {
        var me=this,
			html="<div style='border-bottom: 2px black solid'><font face=Arial size=3><b>"+me.mys.getIdentity(0).displayName+"</b></font></div><br>";
			
		var styles="<style>\n";
		//pre style to force wrapping of preofrmatted text
		styles+=" pre { white-space: pre-wrap !important; }\n";
		//fix chrome bug on page breaks
		styles+=" * { page: unset !important; }\n";
		//remove decoration and color from attachments text
		styles+=" .wtmail-mv-hattach span a { text-decoration: none; color: black; }\n";
		//remove PAS
		styles+=" .wtmail-mv-pas  { visibility: hidden; }\n";
		
		styles+="</style>\n";
        
		me.expandRecipientsList('to');
		me.expandRecipientsList('cc');
		me.expandRecipientsList('bcc');
		html+=me.tdHeader.dom.innerHTML;
        html+="<hr>";
		
		if (me.iframes) {
            var len=me.iframes.length;
            for(var i=0;i<len;++i) {
                if (i>0) html+="<hr><br>";
                var xif=me.iframes[i];
                var data=me.getFrameData(xif.name);
                html+='<span>'+data.doc.body.innerHTML+"</span><br>";
				styles+=data.doc.head.innerHTML;
            }
			html=styles+html;
            Sonicle.PrintMgr.print(html);
			
			//inform service of print action
			WT.ajaxReq(me.mys.ID, 'MessagePrinted', {
				params: {
					account: me.acct,
					folder: me.folder,
					idmessage: me.idmessage
				}
			});
        }
    },
	
	getFrameData: function(xifname) {
		var data={doc: null, xif: document.getElementById(xifname)};
		if (Ext.isIE) {
			if (window.frames[xifname]) data.doc=window.frames[xifname].document;
		} else {
			data.doc=data.xif.contentDocument;
		}
		return data;
	},
	
	confirmImportAttachAsContact: function(folder, messageId, attachId) {
		var me = this;
		WT.confirm(me.res('act-importAttachAsContact.lbl'), function(bid) {
			if (bid === 'yes') me.addContactFromVCard(folder, messageId, attachId, WT.uiid(Ext.id()));
		});
	},
	
	addContactFromVCard: function(folder, messageId, attachId, uploadTag) {
		var me = this,
				contApi = WT.getServiceApi('com.sonicle.webtop.contacts');
		if (contApi) {
			WT.ajaxReq(me.mys.ID, "GetContactFromVCard", {
				params: {
					folder: folder,
					messageId: messageId,
					attachId: attachId,
					uploadTag: uploadTag
				},
				callback: function(success, json) {
					if (success) contApi.addContact(json.data, {dirty: true, uploadTag: uploadTag});
				}
			});
		}
	},
	
	p7mConfirmDecryptAndOpen: function(el) {
		var me = this;
		WT.confirm(me.res('act-p7mDecryptAndOpen.lbl'), function(bid) {
			if (bid === 'yes') me.p7mDecryptAndOpen(el.href);
		});
	},
	
	p7mDecryptAndOpen: function(href) {
		var me = this;
		window.open(href+"&p7m=1");
	},
	
    showEml: function(t,urlparams) {
        var params=Ext.Object.fromQueryString(urlparams);
        this.mys.openEml(params.acct, params.folder,params.idmessage,params.idattach);
    },
    
    loadEml: function(acct, folder,idmessage,idattach) {
		var me=this;
        me._clear();
        me.idmessage=idmessage;
		me.acct=acct,
        me.folder=folder;
        me.idattach=idattach;
		me.proxy.abort();
		WTU.applyExtraParams(me.proxy,{ account: acct, folder: folder, idmessage: idmessage, idattach: idattach });
		me.proxy.doRequest(
			me.proxy.createOperation('read',{
				url: WTF.requestBaseUrl(),
				//params: params,
				callback: me.messageRead,
				scope: me
			})
		);
        this.latestId=idmessage;
    },
    /*
    loadProviderEml: function(provider,providerid) {
        WT.debug("loadProviderEml: "+provider+" - "+providerid);
        this._clear();
        this.idmessage=-1;
        this.folder=null;
        this.idattach=null;
        var params={service: 'mail', action: 'GetMessage', provider: provider, providerid: providerid };
        var rparams={provider: provider, providerid: providerid };
        this.latestId=-1;
        this.proxy.doRequest(
            'read',null,
            params,
            this.reader,
            this.messageRead,
            this,
            rparams
        );
    },*/
    
   /* as from original main.js */
    setEmailElement: function(e,desc,email,type) {
		var me=this;
        if (email && !Ext.String.startsWith(email, 'undisclosed-recipients')) {
			//TODO check if really used: can't find a class like this
            //if (!e.hasClass("wtmail-element-email")) e.addClass("wtmail-element-email");
            e.recDesc=desc;
            e.recEmail=email;
            e.recType=type;
            e.mys=me;
            me.setElementContextMenu(e,me.emailMenu);
            e.on('click', function() {
				e.recEmail !== "..." ? me._startNewMessage(e.recDesc,e.recEmail) : me.expandRecipientsList(e.recType);
			});
        }
    },
	
	expandRecipientsList: function(type) {
		var me = this;
		
		switch(type) {
			case "to":
			default:
				me.divTos.update("<span class='wtmail-mv-hlabelto'>"+me.mys.res('to')+":&nbsp;</span>"+me.toNamesFull);
				//TODO setEmailElements
                if (me.divTos.first()) me._setEmailElements(me.divTos.first().next(),'to');
				break;
			case "cc":
				me.divCcs.update("<span class='wtmail-mv-hlabelcc'>"+me.mys.res('cc')+":&nbsp;</span>"+me.ccNamesFull);
				//TODO setEmailElements
                if (me.divCcs.first()) me._setEmailElements(me.divCcs.first().next(),'cc');
				break;
			case "bcc":
				me.divBccs.update("<span class='wtmail-mv-hlabelbcc'>"+me.mys.res('bcc')+":&nbsp;</span>"+me.bccNamesFull);
                if (me.divBccs.first()) me._setEmailElements(me.divBccs.first().next(),'bcc');
				break;
		}
	},
	
	_startNewMessage: function(desc, email) {
		var me=this,
			email=(desc===email)?email:desc+" <"+email+">";
	
		me.mys.startNewMessage(me.folder,{
			torecipients: [ { rtype: 'to', email: email } ],
			format: me.mys.getVar("format")
		});
	},
 
    setAttachElement: function(e,linkSave,linkSaveAll, attachItem) {
		var me=this;
		e.linkSave=linkSave;
		e.linkSaveAll=linkSaveAll;
		e.mys=me;
		e.attachItem = attachItem;
		if (e.getFirstChild().getAttribute('p7m') && me.p7mAttachMenu) {
            me.setElementContextMenu(e,me.p7mAttachMenu);
		}
        else if (me.attachMenu) {
            me.setElementContextMenu(e,me.attachMenu);
        }
    },

    setElementContextMenu: function(el,menu) {
        Ext.apply(el,{
			//called in the context of element
            onContextMenu : function(e, t){
				var el=this;
                e.stopEvent();
				el.addCls('wt-element-toclick-selected');
                el.ctxMenu.activeElement=el;
                el.ctxMenu.on('hide',function() {
                    this.activeElement.removeCls('wt-element-toclick-selected');
                },this.ctxMenu);
                this.ctxMenu.showAt(e.getXY());
            }
        });
        el.on('contextmenu', el.onContextMenu, el);
        el.ctxMenu=menu;
    },

    setComponentContextMenu: function(comp,menu,callback,scope) {
        var el=comp.getEl();
        Ext.apply(comp,{
			//called in the context of component
            webtopContextMenu : function(e, t){
				var el=this.getEl();
                e.stopEvent();
                if (el.params) el.params.callback.apply(scope,[e,t]);
                el.ctxMenu.showAt(e.getXY());
            }
        });
        if (el) {
          el.on('contextmenu', comp.webtopContextMenu, comp);
          el.ctxMenu=menu;
          el.params=null;
          if (callback && scope) el.params={callback: callback, scope: scope};
        } else {
            comp.on('render', function() {
              var el=this.getEl();
              el.on('contextmenu', comp.webtopContextMenu);
              el.ctxMenu=menu;
              el.params=null;
              if (callback && scope) el.params={callback: callback, scope: scope};
            },comp);
        }
    },
   
    viewFile: function(qsparams) {
		var me=this,
		params=Ext.Object.fromQueryString(qsparams);
		WT.ajaxReq(me.mys.ID, 'ViewAttachment', {
			params: params,
			callback: function(success, json) {
				if (success) {
					var editingCfg=json.data;
					var vw = WT.createView(WT.ID, 'view.DocEditor', {
						swapReturn: true,
						viewCfg: {
							editingConfig: editingCfg
						}
					});
					vw.showView(function() {
						vw.begin('view');
					});
				}
			}
		});
    },	
	
    actionCalendarEvent: function(act,t,qsparams) {
		var me=this,
			params=Ext.Object.fromQueryString(qsparams);
		WT.confirm(me.res('ical.'+act+'.message'),function(bid) {
			if (bid==='yes') {				   
				if (act==='accept'||act==='update'||act==='cancel') me._actionCalendarEvent(act, params);
				else if (act==='updatereply') me._actionCalendarEvent('update',params);
				else if (act==='decline') me._declineCalendarEvent(params);
			}
		},me);
    },
    
    importCalendarEvent: function(qsparams) {
		var me=this,
		params=Ext.Object.fromQueryString(qsparams);
        WT.confirm(me.res('ical.import.message'), function(bid) {
			if (bid==='yes') {
				me._actionCalendarEvent('import',params);
			}
        });	
    },	
	
	_actionCalendarEvent: function(act, params) {
		var me=this,
			trashAfterAction = me.mys.getVar('invitationTrashAfterAction'),
			askEditAfterAction = me.mys.getVar('invitationAskEditAfterAction');
		params.calaction=act;
		delete params.nowriter;
		WT.ajaxReq(me.mys.ID, 'CalendarRequest', {
			params: params,
			callback: function(success,json) {
				if (success) {
					var capi=WT.getServiceApi("com.sonicle.webtop.calendar");
					capi.reloadEvents();
					if (act==="accept" || act==="import"){
						if (askEditAfterAction) {
							WT.confirm(me.res('ical.import.confirm.edit'), function(bid) {
								if (bid==='yes') {
									capi.editEvent({ ekey: json.data });
								}
							},this);	
						}
						if (trashAfterAction) me.mys.messagesPanel.folderList.actionDelete();
					}
					else if (act==="cancel") {
						//canceled
					}
					else if (act==="update") {
						//updated
						if (trashAfterAction) me.mys.messagesPanel.folderList.actionDelete();
					}
					if (me.divICal) me.removeElement(me.divICal);
				} else {
					WT.error(json.message);
				}
			}
		});					
    },
	
    _updateCalendarReply: function(params) {
		var me=this,
			trashAfterAction = me.mys.getVar('invitationTrashAfterAction');
		WT.ajaxReq(me.mys.ID, 'UpdateCalendarReply', {
			params: params,
			callback: function(success,json) {
				if (success) {
					WT.getApp().getService('com.sonicle.webtop.calendar').scheduler.editEvent(json.event_id);
					if (trashAfterAction) me.mys.messagesPanel.folderList.actionDelete();
					if (me.divICal) me.removeElement(me.divICal);
				} else {
					WT.error(json.message);
				}
			}
		});					
    },
	
    _declineCalendarEvent: function(params) {
		var me=this,
			trashAfterAction = me.mys.getVar('invitationTrashAfterAction');
		WT.ajaxReq(me.mys.ID, 'DeclineInvitation', {
			params: params,
			callback: function(success,json) {
				if (success) {
					if (trashAfterAction) me.mys.messagesPanel.folderList.actionDelete();
					if (me.divICal) me.removeElement(me.divICal);
				} else {
					WT.error(json.message);
				}
			}
		});					
    },
	
    res: function(s) {
        return this.mys.res(s);
    }
	
	
});
