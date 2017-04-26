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

Ext.define('Sonicle.webtop.mail.MessageViewModel', {
    extend: 'WTA.model.Base',
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
	extend: 'Ext.container.Container',
	
    bodyBorder: false,
    border: false,
    cls: 'wtmail-mv-main',
    dockable: false,
	
    //Elements
    tdHeader: null,
    divSubject: null,
    divDate: null,
    divFromName: null,
    divTos: null,
    divBccs: null,
    divCcs: null,
    divAttach: null,
	divICal: null,
    divLine: null,
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
    toNames: null,
    toEmails: null,
    ccNames: null,
    bccNames: null,
    ccEmails: null,
    ntos: 0,
    nccs: 0,
    attachments: null,
    receipt: null,
    latestId: null,
	
	emailMenu: null,
	attachMenu: null,
    
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

		//create email menu
		var capi=WT.getServiceApi("com.sonicle.webtop.contacts"),
			i=0,
			actions=new Array();
	
		actions[i++]=new Ext.Action({text: me.mys.res("emailmenu.writeemail"), handler: function() {
			var el=me.emailMenu.activeElement;
			me._startNewMessage(el.recDesc,el.recEmail);
		}, iconCls: 'wtmail-icon-newmsg-xs'});
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
			}, iconCls: 'wtcon-icon-newContact-xs'});
		}
		actions[i++]=new Ext.Action({text: me.mys.res("emailmenu.createrule"), handler: function() {
			var ae=me.emailMenu.activeElement;
			me.createRule(ae.recEmail,ae.recType);
		}, iconCls: 'wt-icon-new-rule-xs'});
		me.emailMenu=new Ext.menu.Menu({
			items: actions
		});
		
		
		//create attach menu
		me.attachMenu=new Ext.menu.Menu({
			items: [
				new Ext.Action({text: me.mys.res("attachmenu.save"),handler:function() {
					var el=me.attachMenu.activeElement;
					if (el.linkSave) window.open(el.linkSave);
				}, scope:me, iconCls: 'wt-icon-save-xs'}),
				new Ext.Action({text: me.mys.res("attachmenu.saveall"),handler:function() {
					var el=me.attachMenu.activeElement;
					if (el.linkSaveAll) window.open(el.linkSaveAll);
				}, scope:me, iconCls: 'wt-icon-save-all-xs'})
			]
		});
		
		
    },
	
	onRender: function(parentNode, containerIdx) {
		var me=this;
		
        var t=document.createElement("table");
        t.className="wtmail-mv-table";
        me.el=Ext.get(t);
      
        var tr=t.insertRow(-1);
        var td=tr.insertCell(-1);
        td.className="wtmail-mv-header";
        me.tdHeader=Ext.get(td);

		tr=t.insertRow(-1);
        td=tr.insertCell(-1);
        td.className="wtmail-mv-bodycell";
        me.tdBody=Ext.get(td);

		me.callParent(arguments);
	},
	
    removeElement: function(el) {
        //var p=el.parent();
        //if (p) p.dom.removeChild(el.dom);
		el.destroy();
    },
    
    _clear: function() {
		var me=this;
		
        if (!me.cleared) {
            me.removeElement(me.divSubject);
            me.removeElement(me.divDate);
            me.removeElement(me.divFromName);
			//TODO un click email
            //me.divFromName.un('click', WT.app.emailElementClicked, me.divFromName);

            me.removeElement(me.divTos);
            me.removeElement(me.divCcs);
            me.removeElement(me.divBccs);
            me.removeElement(me.divAttach);
			me.removeElement(me.divICal);
            me.removeElement(me.divLine);
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
			me.icalmethod=null;
			me.icaluid=null;
			me.icalwebtopid=0;
            me.iframes=new Array();
        }
    },
	
    _showMessage: function(folder, id) {
		var me=this;/*,
			idmessage=id,
			params={service: me.mys.ID, action: 'GetMessage', folder: folder, idmessage: idmessage };*/
	
        //if (this.folder==folder && this.idmessage==idmessage) return;
        me._clear();
        me.idmessage=id;
        me.folder=folder;
        me.latestId=id;
		me.proxy.abort();
		WTU.applyExtraParams(me.proxy,{ folder: folder, idmessage: id });
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
			var params=op.getProxy().getExtraParams(),
				provider=params.provider,
				providerid=params.providerid;
            if (!provider) {
                if (params.folder!==me.folder || params.idmessage!==me.idmessage) return;
            }
            me.htmlparts=new Array();
            me.toNames=null;
            me.toEmails=null;
            me.ccNames=null;
            me.bccNames=null;
            me.ccEmails=null;
            me.attachments=null;
            me.receipt=null;
            me.scheddate=null;
            me.ntos=0;
            me.nccs=0;
            me.nbccs=0;
			me.workflow=false;
			me.messageid=null;
            Ext.each(r,me.evalRecord,me);
			
			var div=document.createElement("div");
			div.className="wtmail-mv-hsubject";
			me.divSubject=Ext.get(div);

			div=document.createElement("div");
			div.className="wtmail-mv-hdate";
			me.divDate=Ext.get(div);

			div=document.createElement("div");
			//div.id="mvhFromNameElement";
			div.className="wtmail-mv-hfromname";
			me.divFromName=Ext.get(div);

			me.divTos=Ext.get(document.createElement("div"));
			me.divTos.addCls("wtmail-mv-hto");

			me.divCcs=Ext.get(document.createElement("div"));
			me.divCcs.addCls("wtmail-mv-hcc");

			me.divBccs=Ext.get(document.createElement("div"));
			me.divBccs.addCls("wtmail-mv-hbcc");

			me.divAttach=Ext.get(document.createElement("div"));
			me.divAttach.addCls("wtmail-mv-hattach");
			
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
				WT.confirm(me.mys.res("receipt.confirm"),function(btn) {
                    if (btn=='yes') {
                        me.sendReceipt();
                    }
				});
            }
            
            var tdh=me.tdHeader,
				tdb=me.tdBody,
				laf=WT.getVar('laf');
            
            var htmlDate="<span class='wtmail-mv-hlabeldate'>"+me.mys.res('date')+":&nbsp;</span>"+me.date;
            if (me.scheddate) {
                htmlDate+="&nbsp;&nbsp;-&nbsp;&nbsp;"+WTF.imageTag(me.mys.ID,"statusscheduled_16.png",16,16,"valign=bottom")+"&nbsp;&nbsp;"+me.scheddate;
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
			
			if (me.mp) {
				var wticon=(me.mp.getViewRegion()==='east'?'wt-icon-panel-bottom-xs':'wt-icon-panel-right-xs');
				me.divSubject.update(
					"<table class='wtmail-mv-subject-table'><tr>"+
					"<td>"+me.subject+"</td>"+
					"<td class='"+wticon+"' style='width:24px; padding-right:8px; background-repeat: no-repeat'><a data-qtip='Click me!' data-qtitle='Panel mover' href='javascript:Ext.emptyFn()' panelmover=1>&nbsp;&nbsp;&nbsp;</a></td>"+
					"</tr></table>"
				);
				Ext.each(me.divSubject.query("a[panelmover]"),function(o) { 
					Ext.get(o).on("click",
						function(e,t,o) { 
							me.mp.switchViewPanel(); 
							e.stopEvent(); 
							return false;
						}
					);
				});
			} else {
				me.divSubject.update(
					"<table class='wtmail-mv-subject-table'><tr>"+
					"<td>"+me.subject+"</td>"+
					"</tr></table>"
				);
			}
			//TODO: update tooltips (necessary???)
            //me.divFromName.set({ 'data-qtitle': me.fromName, 'data-qtip': me.fromEmail })
            me.divFromName.update(
				"<a data-qtip='"+me.fromEmail+"' data-qtitle='"+me.fromName+"' href='javascript:Ext.emptyFn()'>"+
					me.fromName+" &lt;"+me.fromEmail+"&gt;</a>"
			);
            tdh.insertFirst(me.divLine);
			
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
                    var imgclass=WTF.fileTypeCssIconCls(WTA.Util.getFileExtension(name),"xs");
                    var aparams;
                    if (!provider) {
                        aparams={
                            folder: me.folder,
                            idmessage: me.idmessage,
                            idattach: att.id
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
					var href=WTF.processBinUrl(me.mys.ID,"GetAttachment",aparams);
                    if (Ext.isIE) href+="&saveas=1";
                    var ics=null;
                    if (name.toLowerCase().endsWith(".ics")) ics=" ics='"+Ext.Object.toQueryString(aparams)+"'";
                    var eml=null;
                    if (att.eml) eml=" eml='"+Ext.Object.toQueryString(aparams)+"'";
                    var html="<a href='"+href.replace("'","%27")+"' target='_blank'"+(ics!=null?ics:"")+(eml!=null?eml:"")+"><div class='"+imgclass+"' style='display:inline-block;width:16px;height:16px'></div>&nbsp;<span>"+name+"</span>&nbsp;("+ssize+")</a>";
                    names=me.appendAttachmentName(names,html);
                }
				var allhref=WTF.processBinUrl(me.mys.ID,"GetAttachments",allparams);
                me.divAttach.update("<span class='wtmail-mv-hlabelattach'><a data-qtip='"+WT.res('saveall-desc')+"' data-qtitle='"+WT.res('saveall')+"' href='"+allhref.replace("'","%27")+"'>"+me.mys.res('attachments')+"</a>:&nbsp;</span>"+names);

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
							tdh.insertFirst(me.divICal);

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

				
                tdh.insertFirst(me.divAttach);
				
				
                Ext.each(me.divAttach.query("a[eml]"),function(o) { 
					Ext.get(o).on("click",function(e,t,o) { 
						var xel=t; //sometimes returns SPAN or IMG instead of A
						if (t.tagName==="SPAN"||t.tagName==="IMG") xel=t.parentElement;
						me.showEml(xel,xel.getAttribute("eml")); 
						e.stopEvent(); 
						return false;
					},me );
				},me);
				//TODO setAttachElements
                me._setAttachElements(me.divAttach.first().next(),vparams);
            }
            if (me.bccNames) {
                me.divBccs.update("<span class='wtmail-mv-hlabelbcc'>"+me.mys.res('bcc')+":&nbsp;</span>"+me.bccNames);
                tdh.insertFirst(me.divBccs);
                me._setEmailElements(me.divBccs.first().next(),'bcc');
            }
            if (me.ccNames) {
                me.divCcs.update("<span class='wtmail-mv-hlabelcc'>"+me.mys.res('cc')+":&nbsp;</span>"+me.ccNames);
                tdh.insertFirst(me.divCcs);
				//TODO setEmailElements
                me._setEmailElements(me.divCcs.first().next(),'cc');
            }
            if (me.toNames) {
                me.divTos.update("<span class='wtmail-mv-hlabelto'>"+me.mys.res('to')+":&nbsp;</span>"+me.toNames);
                tdh.insertFirst(me.divTos);
				//TODO setEmailElements
                me._setEmailElements(me.divTos.first().next(),'to');
            }
            tdh.insertFirst(me.divDate);
            tdh.insertFirst(me.divFromName);
            tdh.insertFirst(me.divSubject);
            me.divBody.dom.style.width=tdh.dom.scrollWidth-5;//"100%";
            tdb.insertFirst(me.divBody);
            me._setEmailElement(me.divFromName.first(),'from');
            
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
				var ct=me.ownerCt;
				//was -2, changed to -26 to keep space for h scrollbar
                h=ct.getEl().getHeight()-26;
                w=ct.getEl().getWidth();
                me.viewResized(me,w,h,w,h);
            //}
            
            if (me.icalmethod) console.log("ical - method: "+me.icalmethod+" , uid: "+me.icaluid+" , webtopid: "+me.icalwebtopid);
			
            me.cleared=false;
            if (!provider) me.fireEvent('messageviewed',params.idmessage,me.proxy.getReader().rawData.millis,me.workflow);
        }
    },
	
    createRule: function(email,type) {
		var me=this,context="INBOX";
		//if (ms.currentFolder==ms.folderSent) context="SENT";
		me.mys.addRule({
			from: type==="from"?email:null,
			to: type==="to"||type==="cc"||type==="bcc"?email:null
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
            var href=el.first().dom.href;
			//TODO isIE saveas=1?
            if (!Ext.isIE) href+="&saveas=1";
            me.setAttachElement(el,href,me.divAttach.first().first().dom.href);
            
            me._setDD(el,vparams[i]);
            
            el=el.next();
            ++i;
        }
    }, 
    
    _setDD: function(el,params) {
		var me=this;
		new Ext.dd.DragZone(el,{
            params: params,
            ddGroup : 'attachment', 
            getDragData: function(ev) {
				var ddel=el;
				if (ddel.contains(ev.getTarget())) {
					return {
						ddel: ddel.dom.cloneNode(true),
						params: params,
						sourceEl: el,
						repairXY: Ext.fly(el).getXY()
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

		var xstyle=doc.createElement('style');
		xstyle.type='text/css';
		xstyle.appendChild(doc.createTextNode(
				'pre { white-space: pre-wrap !important; } '+
				'body { font-family: "Lucida Grande",Verdana,Arial,Helvetica,sans-serif; } '+
				'blockquote { display: block; margin-left: 5px; border-left: solid 2px blue; padding-left: 10px; } '
		));
		doc.head.appendChild(xstyle);
		
        var thebody=doc.body;
        thebody.style.padding="0px 0px 0px 0px";
        thebody.style.margin="0px 0px 0px 0px";
        //var newheight=thebody.scrollHeight;
        //var newwidth=thebody.scrollWidth;
        var newheight=thebody.offsetHeight;
        var newwidth=thebody.offsetWidth;
        if (newheight<thebody.scrollHeight) newheight=thebody.scrollHeight;
        if (newwidth<thebody.scrollWidth) newwidth=thebody.scrollWidth;
        me.setIframeSize(xif,newwidth,newheight);
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

    setIframeSize: function(xif,newwidth,newheight) {
        var tcd=xif.wtTcd;
        xif.height=newheight+30;
        if (newwidth<tcd.scrollWidth) newwidth=tcd.scrollWidth-10;
        else if (newwidth>tcd.scrollWidth) {
            //this.divBody.dom.style.width=this.divBody.dom.scrollWidth;
        }
        xif.width=newwidth;
    },
    
    evalRecord: function(item,index,allItems) {
		var me=this,
			iddata=item.get('iddata'),
			maxtos=me.mys.viewmaxtos||20,
			maxccs=me.mys.viewmaxccs||20,
			maxbccs=me.mys.viewmaxccs;
	
        //alert("iddata="+iddata);
        if (iddata=='subject') me.subject=item.get('value1');
        else if (iddata=='messageid') {me.messageid=item.get('value1');} 
        else if (iddata=='date') { me.date=item.get('value1'); }
        else if (iddata=='scheddate') { me.scheddate=item.get('value1'); }
        else if (iddata=='from') { me.fromName=item.get('value1'); me.fromEmail=item.get('value2') }
        else if (iddata=='to') {
            if (me.ntos<maxtos) {
                me.toNames=me.appendEmail(me.toNames,item.get('value1'),item.get('value2'));
            } else if (me.ntos==maxtos) {
                me.toNames=me.appendEmail(me.toNames,'...','...');
            }
            ++me.ntos;
        }
        else if (iddata=='cc') {
            if (me.nccs<maxccs) {
                me.ccNames=me.appendEmail(me.ccNames,item.get('value1'),item.get('value2'));
            } else if (me.nccs==maxccs) {
                me.ccNames=me.appendEmail(me.ccNames,'...','...');
            }
            ++me.nccs;
        }
        else if (iddata=='bcc') {
            if (me.nbccs<maxbccs) {
                me.bccNames=me.appendEmail(me.bccNames,item.get('value1'),item.get('value2'));
            } else if (me.nbccs==maxbccs) {
                me.bccNames=me.appendEmail(me.bccNames,'...','...');
            }
            ++me.nbccs;
        }
        else if (iddata=='attach' || iddata=='eml') {
            if (!me.attachments) me.attachments=new Array();
            var o={id: item.get('value1'), name: item.get('value2'), size: parseInt(item.get('value3')/4*3), imgname: item.get('value4'), eml: iddata=='eml' };
            me.attachments[me.attachments.length]=o;
        }
        else if (iddata=='html') {
            me.htmlparts[me.htmlparts.length]=item.get('value1');
        }
        else if (iddata=='receipt') {
            me.receipt=item.get('value1');
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
        if (name==null) name="";
        else name+=" - ";
        name+="<span data-qtip='"+email+"' data-qtitle='"+desc+"'><a data-qtip='"+email+"' data-qtitle='"+desc+"' href='javascript:Ext.emptyFn()'>"+desc+"</a></span>";
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
			newh=ah-hh-5,
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
                this.setIframeSize(xif,newwidth,newheight);
            }
        }
    },

    sendReceipt: function() {
		var me=this,
			ident=me.mys.getFolderIdentity(me.folder);
	
		WT.ajaxReq(me.mys.ID, 'SendReceipt', {
			params: {
				from: ident.displayName+" <"+ident.email+">",
                subject: me.subject,
                to: me.receipt,
                folder: me.folder
			},
			callback: function(success,json) {
				if (success) {
				} else {
					WT.error(json.text);
				}
			}
		});					
    },

    print: function() {
        var me=this,
			//html='<html><head><link rel="stylesheet" type="text/css" href="webtop/themes/'+WT.theme+'/mail/mail.css" /></head><body>'+
			html="<div style='border-bottom: 2px black solid'><font face=Arial size=3><b>"+me.mys.getIdentity(0).displayName+"</b></font></div><br>"
        html+=me.tdHeader.dom.innerHTML;
        if (me.iframes) {
            var len=me.iframes.length;
            for(var i=0;i<len;++i) {
                if (i>0) html+="<hr><br>";
                var xif=me.iframes[i];
                var data=me.getFrameData(xif.name);
                var thebody=data.doc.body;
                html+='<span>'+thebody.innerHTML+"</span><br>";
            }
            //html+='</body></html>';
            Sonicle.PrintMgr.print(html);
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
    
    showEml: function(t,urlparams) {
        var params=Ext.Object.fromQueryString(urlparams);
        this.mys.openEml(params.folder,params.idmessage,params.idattach);
    },
    
    loadEml: function(folder,idmessage,idattach) {
		var me=this;
        me._clear();
        me.idmessage=idmessage;
        me.folder=folder;
        me.idattach=idattach;
		me.proxy.abort();
		WTU.applyExtraParams(me.proxy,{ folder: folder, idmessage: idmessage, idattach: idattach });
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
        if (email && !email.startsWith('undisclosed-recipients')) {
			//TODO check if really used: can't find a class like this
            //if (!e.hasClass("wtmail-element-email")) e.addClass("wtmail-element-email");
            e.recDesc=desc;
            e.recEmail=email;
            e.recType=type;
            e.mys=me;
            me.setElementContextMenu(e,me.emailMenu);
            e.on('click', function() {
				me._startNewMessage(e.recDesc,e.recEmail);
			});
        }
    },
	
	_startNewMessage: function(desc, email) {
		var me=this,
			email=(desc===email)?email:desc+" <"+email+">";
	
		me.mys.startNewMessage(me.folder,{
			recipients: [ { rtype: 'to', email: email } ],
			format: me.mys.varsData.format
		});
	},

    setAttachElement: function(e,linkSave,linkSaveAll) {
		var me=this;
        if (me.attachMenu) {
            e.linkSave=linkSave;
            e.linkSaveAll=linkSaveAll;
            e.mys=me;
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
    
    importCalendarEvent: function(params) {
		var me=this;
        WT.confirm(me.res('ical.import.message'), function(bid) {
			if (bid==='yes') {
				me._importCalendarEvent('accept',params);
			}
        });	
    },	
	
	_actionCalendarEvent: function(act, params) {
		var me=this;
		params.calaction=act;
		delete params.nowriter;
		WT.ajaxReq(me.mys.ID, 'CalendarRequest', {
			params: params,
			callback: function(success,json) {
				if (success) {
					var capi=WT.getServiceApi("com.sonicle.webtop.calendar");
					capi.reloadEvents();
					if (act==="accept"){
						WT.confirm(me.res('ical.import.confirm.edit'), function(bid) {
							if (bid==='yes') {
								capi.editEvent({ ekey: json.data });
							}
						},this);	
					}
					else if (act==="cancel") {
						//canceled
					}
					else if (act==="update") {
						//updated
					}
					if (me.divICal) me.removeElement(me.divICal);
				} else {
					WT.error(json.message);
				}
			}
		});					
    },
	
    _updateCalendarReply: function(params) {
		var me=this;
		WT.ajaxReq(me.mys.ID, 'UpdateCalendarReply', {
			params: params,
			callback: function(success,json) {
				if (success) {
					WT.getApp().getService('com.sonicle.webtop.calendar').scheduler.editEvent(json.event_id);
					if (me.divICal) me.removeElement(me.divICal);
				} else {
					WT.error(json.text);
				}
			}
		});					
    },
	
    _declineCalendarEvent: function(params) {
		var me=this;
		WT.ajaxReq(me.mys.ID, 'DeclineInvitation', {
			params: params,
			callback: function(success,json) {
				if (success) {
					if (me.divICal) me.removeElement(me.divICal);
				} else {
					WT.error(json.text);
				}
			}
		});					
    },
	
    res: function(s) {
        return this.mys.res(s);
    }
	
	
});