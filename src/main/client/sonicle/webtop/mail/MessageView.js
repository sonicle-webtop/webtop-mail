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
    extend: 'WT.model.Base',
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
    windowed: false,
	
    //Elements
    tdHeader: null,
    divSubject: null,
    divDate: null,
    divFromName: null,
    divTos: null,
    divCcs: null,
    divAttach: null,
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

        me.proxy=Ext.create("Ext.data.proxy.Ajax",{
            url:'service-request',
			model: 'Sonicle.webtop.mail.MessageViewModel',
			reader: {
				type: 'json',
				rootProperty: 'message',
				totalProperty: 'totalRecords'
			}
        });
        
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

        me.addListener('resize',me.viewResized,me);

		//create email menu
		var cs=WT.app.getService('com.sonicle.webtop.contacts');
		var i=0;
        var actions=new Array();
		actions[i++]=new Ext.Action({text: me.mys.res("emailmenu.writeemail"), handler: function() {
			me.mys.beginNewMessage(me._getEmailFromElement(me.emailMenu.activeElement));
		}, iconCls: 'wtmail-icon-newmsg-xs'});
		if (cs) {
			actions[i++]=new Ext.Action({text: me.mys.res("emailmenu.addcontact"), handler: function() {
				var el=me.emailMenu.activeElement;
				var desc=el.recDesc;
				var email=el.recEmail;
				if (desc==email) desc=null;
				cs.beginNewContact(desc,email);
			}, iconCls: 'wtcontacts-icon-newcontact-xs'});
		}
		actions[i++]=new Ext.Action({text: me.mys.res("emailmenu.createfilter"), handler: function() {
			var ae=me.emailMenu.activeElement;
			me.createFilter(ae.recEmail,ae.recType);
		}, iconCls: 'wtmail-icon-newfilter-xs'});
		me.emailMenu=new Ext.menu.Menu({
			items: actions
		});
		
		
		//create attach menu
		me.attachMenu=new Ext.menu.Menu({
			items: [
				new Ext.Action({text: me.mys.res("attachmenu.save"),handler:function() {
					var el=me.attachMenu.activeElement;
					if (el.linkSave) window.open(el.linkSave);
				},scope:me}),
				new Ext.Action({text: me.mys.res("attachmenu.saveall"),handler:function() {
					var el=me.attachMenu.activeElement;
					if (el.linkSaveAll) window.open(el.linkSaveAll);
				},scope:me})
			]
		});
		
		
    },
	
    removeElement: function(el) {
        //var p=el.parent();
        //if (p) p.dom.removeChild(el.dom);
		el.destroy();
    },
    
    clear: function() {
		var me=this;
		
        if (!me.cleared) {
            me.removeElement(me.divSubject);
            me.removeElement(me.divDate);
            me.removeElement(me.divFromName);
			//TODO un click email
            //me.divFromName.un('click', WT.app.emailElementClicked, me.divFromName);

            me.removeElement(me.divTos);
            me.removeElement(me.divCcs);
            me.removeElement(me.divAttach);
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
            me.iframes=new Array();
        }
    },
	
    showMessage: function(folder, id) {
		var me=this,
			idmessage=id,
			params={service: me.mys.ID, action: 'GetMessage', folder: folder, idmessage: idmessage };
	
        //if (this.folder==folder && this.idmessage==idmessage) return;
        me.clear();
        me.idmessage=idmessage;
        me.folder=folder;
        me.latestId=idmessage;
		me.proxy.abort();
		me.proxy.doRequest(
			me.proxy.createOperation('read',{
				params: params,
				callback: me.messageRead,
				scope: me
			})
		);
    },
	
    messageRead: function(r, op, success) {
		var me=this;
        if (op && success) {
			var params=op.getParams(),
				provider=params.provider,
				providerid=params.providerid;
            if (!provider) {
                if (params.folder!==me.folder || params.idmessage!==me.idmessage) return;
            }
            me.htmlparts=new Array();
            me.toNames=null;
            me.toEmails=null;
            me.ccNames=null;
            me.ccEmails=null;
            me.attachments=null;
            me.receipt=null;
            me.scheddate=null;
            me.ntos=0;
            me.nccs=0;
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

			me.divAttach=Ext.get(document.createElement("div"));
			me.divAttach.addCls("wtmail-mv-hattach");

			div=document.createElement("div");
			div.className="wtmail-mv-hline";
			me.divLine=Ext.get(div);
			
			div=document.createElement("div");
			div.className="wtmail-mv-bodydiv";
			me.divBody=Ext.get(div);
			
			//TODO workflow
			/*
			if (me.workflow) {
                WT.alert(
                    "Workflow",
                    me.mys.res('alert-workflow')
                );
			}
			//no receipt if workflow
			//TODO receipt
            else if (this.receipt) {
              var config={
                mv: this,
                title: ms.res('receipt'),
                msg: ms.res('receiptprompt'),
                buttons: Ext.Msg.YESNO,
                fn: function(btn) {
                    if (btn=='yes') {
                        this.mv.sendReceipt();
                    }
                },
                icon: Ext.Msg.WARNING
              };
              config.scope=config;
              WT.show(config);
            }*/
            
            var tdh=me.tdHeader,
				tdb=me.tdBody,
				laf=WT.getOption('laf');
            
            var htmlDate="<span class='wtmail-mv-hlabeldate'>"+me.mys.res('date')+":&nbsp;</span>"+me.date;
            if (me.scheddate) {
                htmlDate+="&nbsp;&nbsp;-&nbsp;&nbsp;<img src='webtop/themes/"+laf+"/mail/statusscheduled.gif' valign=bottom width=16 height=16>&nbsp;&nbsp;"+me.scheddate;
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
			
            me.divSubject.update(me.subject);
			//TODO: update tooltips (necessary???)
            //me.divFromName.set({ 'data-qtitle': me.fromName, 'data-qtip': me.fromEmail })
            me.divFromName.update("<a data-qtip='"+me.fromEmail+"' data-qtitle='"+me.fromName+"' href='javascript:Ext.emptyFn()'>"+me.fromName+" ["+me.fromEmail+"]</a>");
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
                    var imgname=att.imgname;
                    if (!imgname) imgname=WT.Util.imageUrl(WT.ID,"/filetypes/"+WT.Util.normalizeFileType(name)+"_16.gif");
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
                    var ssize=WT.Util.humanReadableSize(att.size);
					var href=WT.Util.serviceRequestBinaryUrl(me.mys.ID,"GetAttachment",aparams);
                    if (Ext.isIE) href+="&saveas=1";
                    var ics=null;
                    if (name.toLowerCase().endsWith(".ics")) ics=" ics='"+Ext.Object.toQueryString(aparams)+"'";
                    var eml=null;
                    if (att.eml) eml=" eml='"+Ext.Object.toQueryString+"'";
                    var html="<a href='"+href+"' target='_blank'"+(ics!=null?ics:"")+(eml!=null?eml:"")+"><img src='"+imgname+"' width=16 height=16>&nbsp;<span>"+name+"</span>&nbsp;("+ssize+")</a>";
                    names=me.appendAttachmentName(names,html);
                }
				var allhref=WT.Util.serviceRequestBinaryUrl(me.mys.ID,"GetAttachments",allparams);
                me.divAttach.update("<span class='wtmail-mv-hlabelattach'><a data-qtip='"+WT.res('saveall-desc')+"' data-qtitle='"+WT.res('saveall')+"' href='"+allhref+"'>"+me.mys.res('attachments')+"</a>:&nbsp;</span>"+names);
                tdh.insertFirst(me.divAttach);
                if (WT.getApp().getService('com.sonicle.webtop.calendar')) {
                    Ext.each(me.divAttach.query("a[ics]"),function(o) { Ext.get(o).on("click",function(e,t,o) { me.importCalendarEvent(t,t.getAttribute("ics")); e.stopEvent(); return false;},me );},me);
                }
                Ext.each(me.divAttach.query("a[eml]"),function(o) { 
					Ext.get(o).on("click",function(e,t,o) { 
						//WT.debugObject(t); 
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
                
                var data=WT.Util.getIFrameData(xifname),
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
            if (!me.windowed) {
                h=me.ownerCt.getEl().getHeight()-2; //??getComputedHeight
                w=me.ownerCt.getEl().getWidth(); //??getComputedWidth
                me.viewResized(me,w,h,w,h);
            } else {
//                h=this.ownerCt.getEl().getComputedHeight()-40;
//                w=this.ownerCt.getEl().getComputedWidth()-10;
//                this.viewResized(this,w,h,w,h);
            }
            
            me.cleared=false;
            if (!provider) me.fireEvent('messageviewed',params.idmessage,me.proxy.getReader().rawData.millis,me.workflow);
        }
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

    _setAttachElements: function(e,vparams) {
        var i=0;
        while(e) {
            var href=e.first().dom.href;
			//TODO isIE saveas=1?
            //if (!Ext.isIE) href+="&saveas=1";
            this.setAttachElement(e,href,this.divAttach.first().first().dom.href);
            
			//TOTO set DD
            //this._setDD(e,vparams[i]);
            
            e=e.next();
            ++i;
        }
    }, 
    
	//TODO set DD
	/*
    _setDD: function(e,params) {
          new Ext.dd.DragZone(e,{
            params: params,
            ddGroup : 'attachment', 
            getDragData: function(e) {
				var ddel=this.getEl();
				if (ddel.contains(e.getTarget())) {
					return {
						ddel: ddel.cloneNode(true),
						params: this.params
					};
				}
            }
			
          });
    },*/

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
        var data=WT.Util.getIFrameData(xifname),
			doc=data.doc,
			xif=data.iframe;
        if (!doc || !doc.body) {
            if (!task) task=new Ext.util.DelayedTask(me.adjustIframe);
            task.delay(200, me.adjustIframe, me, [xifname,task]);
            return;
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
			maxccs=me.mys.viewmaxccs||20;
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
            if (me.windowed) neww+=2;
            me.divBody.setWidth(neww);
        }
        if (me.iframes) {
            var len=me.iframes.length;
            for(var i=0;i<len;++i) {
                var xif=me.iframes[i];
                var data=WT.Util.getIFrameData(xif.name);
                var thebody=data.doc.body;
                var newheight=thebody.scrollHeight;
                var newwidth=thebody.scrollWidth;
                this.setIframeSize(xif,newwidth,newheight);
            }
        }
    },

	//TODO sendReceipt
	/*
    sendReceipt: function() {
        WT.JsonAjaxRequest({
            url: "ServiceRequest",
            params: {
                service: 'mail',
                action: 'SendReceipt',
                subject: this.subject,
                to: this.receipt,
                folder: this.folder
            },
            options: {
            },
            method: "POST",
            callback: function(o,options) {
              if (!o.result) {
                  WT.alert(o.text);
              }
            },
            scope: this
        });
    },*/

	// TODO print
	/*
    print: function() {
        var html='<html><head><link rel="stylesheet" type="text/css" href="webtop/themes/'+WT.theme+'/mail/mail.css" /></head><body>';
        html+="<div style='border-bottom: 2px black solid'><font face=Arial size=3><b>"+this.ms.identities[0].displayname+"</b></font></div><br>"
        html+=this.tdHeader.dom.innerHTML;
        if (this.iframes) {
            var len=this.iframes.length;
            for(var i=0;i<len;++i) {
                if (i>0) html+="<hr><br>";
                var xif=this.iframes[i];
                var data=WT.getFrameData(xif.name);
                var thebody=data.doc.body;
                html+='<span>'+thebody.innerHTML+"</span><br>";
            }
            html+='</body></html>';
            WT.app.print(html);
        }
    },*/
    
    showEml: function(t,urlparams) {
        var params=Ext.Object.fromQueryString(urlparams);
        this.mys.openEml(params.folder,params.idmessage,params.idattach);
    },
    
	//TODO loadEml
	/*
    loadEml: function(folder,idmessage,idattach) {
		var me=this;
        me.clear();
        me.idmessage=idmessage;
        me.folder=folder;
        me.idattach=idattach;
        var params={service: 'mail', action: 'GetMessage', folder: folder, idmessage: idmessage, idattach: idattach };
        var rparams={folder: folder, idmessage: idmessage, idattach: idattach};
        this.latestId=idmessage;
        this.proxy.doRequest(
            'read',null,
            params,
            this.reader,
            this.messageRead,
            this,
            rparams
        );
    },
    
    loadProviderEml: function(provider,providerid) {
        WT.debug("loadProviderEml: "+provider+" - "+providerid);
        this.clear();
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
    
	//TODO importCalendarEvent
	/*
    importCalendarEvent: function(t,params) {
        Ext.Msg.show({
           title:this.ms.res('importcaltitle'),
           msg: this.ms.res('importcalquestion'),
           buttons: Ext.Msg.YESNO,
           fn: this._importCalendarEvent,
           scope: this,
           icon: Ext.MessageBox.QUESTION,
           options: {
               t: t,
               params: params
           }
        });
    },
    
    _importCalendarEvent: function(b,t,o) {
        if (b=='yes') {
            WT.JsonAjaxRequest({
                url: "ServiceRequest?service=mail&action=GetCalendarEvent&"+o.options.params,
            options: {
            },
            method: "POST",
            callback: function(o,options) {
              if (o.result) {
                var ev=o.event;
                if (ev.event_id!=undefined && ev.event_id==""){
                    WT.app.getServiceByName('calendar').actionNewByAttachment(
                        ev,
                        o.planninggrid
                        
                    );
                  }else{
                      if (ev.method=="CANCEL"){
                          WT.app.getServiceByName('calendar').actionDeleteByAttachment(ev.event_id);
                      }else{
                          WT.app.getServiceByName('calendar').actionEditEventByAttachment(ev.event_id,ev,o.planninggrid);
                          
                          
                      }
                  }
              }
            },
            scope: this
            });
     } else {
            window.open(o.options.t.href);
        }
    }*/
    
   /* as from original main.js */
    setEmailElement: function(e,desc,email,type) {
		var me=this;
        if (!email.startsWith('undisclosed-recipients')) {
			//TODO check if really used: can't find a class like this
            //if (!e.hasClass("wtmail-element-email")) e.addClass("wtmail-element-email");
            e.recDesc=desc;
            e.recEmail=email;
            e.recType=type;
            e.mys=me;
            me.setElementContextMenu(e,me.emailMenu);
            e.on('click', me.emailElementClicked,e);
        }
    },

	//called in the context of element
    emailElementClicked: function(e,t,o) {
		var el=this;
        var email=(el.recDesc==el.recEmail)?el.recEmail:el.recDesc+" <"+el.recEmail+">";
        this.beginNewMessage(email);
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
                    this.activeElement.removeClass('wt-element-toclick-selected');
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
    }
   
});