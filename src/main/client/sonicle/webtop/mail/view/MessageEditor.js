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

Ext.define('Sonicle.webtop.mail.view.MessageEditor', {
	extend: 'WT.sdk.ModelView',
	requires: [
		'Sonicle.webtop.core.ux.RecipientsGrid',
		'Sonicle.webtop.core.ux.field.SuggestCombo',
		'Sonicle.form.field.HTMLEditor',
		'Sonicle.webtop.mail.model.MessageModel',
		'Sonicle.upload.Button'
	],
	
	dockableConfig: {
		title: '{message.tit}',
		iconCls: 'wtmail-icon-newmsg-xs',
		width: 800,
		height: 500
	},
	modelName: 'Sonicle.webtop.mail.model.MessageModel',
	
	autoToolbar: false,
	identityIndex: 0,
	selectedIdentity: null,
	fontFace: 'Arial',
	fontSize: 12,
	
    showSave: true,
    showAddressBook: true,
    showReceipt: true,
    showPriority: true,
    showAttach: true,
    showIdentities: true,
    showEditToolbar: true,
    showSourceEdit: true,
    showCloud: true,
	
	fax: false,
	faxident: null,
	
	initComponent: function() {
		var me=this;
		me.callParent(arguments);
		
		me.identities=me.mys.getOption("identities");
		//save hashed identities, by email
		me.identHash={};
		Ext.each(me.identities,function(ident) { me.identHash[ident.email]=ident},me);
		
		me.attlist= Ext.create({
			xtype: 'panel',
			width: 200,
			autoScroll: true,
			region: 'east',
			layout: 'anchor',
			border: false,
			bodyBorder: false,
			tabIndex: 1
        });
		me.recgrid=Ext.create({
			xtype: 'wtrecipientsgrid',
			height: 90,
			region: 'center',
			tabIndex: 2,
			fields: { recipientType: 'rtype', email: 'email' },
			bind: {
				store: '{record.recipients}'
			},
			listeners: {
				exitfocus: function(recgrid) {
					me.subject.focus();
				}
			}
		});
		
		me.subject=Ext.create({
			xtype: 'wtsuggestcombo',
            sid: me.mys.ID,
            suggestionContext: 'subject',
            width: 600,
			tabIndex: 3,
			enableKeyEvents: true,
			fieldLabel: WT.res('word.subject'),
			labelWidth: 60,
			labelAlign: 'right',
			listeners: {
				keydown: function(cb, e, eOpts) {
					if (e.getKey() === e.TAB) {
						e.stopEvent();
						me.htmlEditor.focusEditor();
					}
				}
			},
			bind: '{record.subject}'
        });
		var tbitems=new Array(),
			tbx=0;
		
		tbitems[tbx++]={ xtype:'button',text:'Send!', handler: me.sendMail, scope: me };
		
		if (this.showIdentities) {
            var idents=new Array(),
				selident=null;
            if (me.identities) {
				var x=0;
                for(var i=0;i<me.identities.length;++i) {
                    var id=me.identities[i];
					if (me.fax) {
						if (me.faxident && !id.fax) continue;
					} else {
						if (id.fax) continue;
					}
                    //var a=new Array);
                    //a[0]=i;
                    //a[1]=id.displayname+" - "+id.email;
                    idents[x]=id;
					if (me.identityIndex===i) selident=id;
					++x;
                }
				if (!selident) selident=idents[0];
				me.selectedIdentity=selident;
				
				tbitems[tbx++]={
					xtype:'combo',
					queryMode: 'local',
					displayField: 'description',
					valueField: 'email',
					width:200,
					matchFieldWidth: false,
					listConfig: {
						width: 300
					},
					store: Ext.create('Ext.data.Store', {
						model: 'Sonicle.webtop.mail.model.Identity',
						data : idents
					}),
					value: selident.email,
					listeners: {
						change: {
							fn: function(s,nv,ov) {
								// We are using the select event because change event is
								// fired only after blur. Within this event we have to
								// calculate new and old values manually.
								//var ov = s.lastValue || s.startValue;
								//var nv = r.get('id');
								if(ov === nv) return;
								//s.lastValue = nv;
								me.setHtml(me.replaceMailcard(me.htmlEditor.getValue(), me.identHash[ov].mailcard.html, me.identHash[nv].mailcard.html));
							},
							scope: this
						}
					}
					
				};

            }
		}
		
		tbitems[tbx++]={
			xtype:'souploadbutton',
			text:'Upload!',
			uploaderConfig: WTF.uploader(me.mys.ID,'UploadAttachment',{
				
			}),
			listeners: {
				fileuploaded: function(up,file) {
					alert("Complete! ("+file.uploadId+")");
				}
			}
		};
		
		me.toolbar=Ext.create({
			xtype: 'toolbar',
			region: 'north',
			items: tbitems
		});
		
		me.add(Ext.create({
			xtype: 'panel',
			region: 'north',
//			bodyCls: 'wt-theme-bg-2',
            border: false,
            bodyBorder: false,
			height: 150,
			layout: 'anchor',
			items: [
				me.toolbar,
				Ext.create({
					xtype: 'panel',
					region: 'center',
                    border: false,
                    bodyBorder: false,
                    height: 90,
                    layout: 'border',
                    items: [
                        me.recgrid,
						me.attlist
                    ]
				}),
				me.subject
			]
		}));
		me.add(
			me.htmlEditor=Ext.create({
				xtype: 'sohtmleditor',
				region: 'center',
				bind: '{record.html}',
				enableFont: true,
				enableFontSize: true,
				enableFormat: true,
				enableColors: true,
				enableAlignments: true,
				enableLinks: true,
				enableLists: true,
				enableSourceEdit: true,
				enableClean: true,
				listeners: {
					init: function() {
						var xdoc=me.htmlEditor.getDoc(),
							xstyle=xdoc.createElement('style');

						xstyle.type='text/css';
						xstyle.appendChild(xdoc.createTextNode(
								'div#wt-mailcard { border: 1px dotted lightgray !important; } '+
								'blockquote { display: block; margin-left: 5px; border-left: solid 2px blue; padding-left: 10px; } '
						));
						xdoc.head.appendChild(xstyle);
					}
				}
			})
		);

		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		
		var me=this,
			rg=me.recgrid,
			c=rg.getRecipientsCount();
	
		if (c===1) {
			var r=c-1,
				email=rg.getRecipientAt(r);
		
			if (email==="") rg.startEditAt(r);
		} else {
			if (me.subject.getValue()==="") me.subject.focus();
			else me.htmlEditor.focusEditor();
		}
	},
	
	startNew: function(data) {
		var me=this;
		data.html=me.prepareHtml(data.html);
		me.beginNew({
			data: data
		});
	},
	
	sendMail: function() {
		console.log("sendMail");
	},
	
    prepareHtml: function(html) {
		var me=this,
			mc=me.identities[me.identityIndex].mailcard;
		if (mc) html='<br><br><div id="wt-mailcard">'+mc.html+'</div>'+html;
        html='<div style="font-family: '+me.fontFace+'; font-size: '+me.fontSize+'px;">'+html+'</div>';
        return html;
    },

    setHtml: function(html) {
		var me=this;
        //me.htmlEditor.initHtmlValue(html);
		me.getModel().set("html",html);
/*        var w=this.htmleditor.getWin();
        var d=w.document;
        var n=d.body.firstChild;
        if (w.getSelection) {
            var r=d.createRange();
            r.setStart(n,0);
            r.setEnd(n,0);
            var s = this.htmleditor.win.getSelection();
            s.removeAllRanges();
            s.addRange(r);
        } else if (d.selection) {
            var r=d.body.createTextRange();
            r.moveToElementText(n);
            r.collapse(true);
            r.select();
        }*/
    },

	replaceMailcard: function(html, omc, nmc) {
		if(Ext.isEmpty(omc) || Ext.isEmpty(nmc)) return html;
		
		var htmlEl = Ext.get(Ext.DomHelper.createDom({html: html})),
			mcEl = htmlEl.query('#wt-mailcard',false);
		if(mcEl) {
			mcEl=mcEl[0];
			var htmlOmc = this.htmlEditor.cleanUpHtml(omc);
			var htmlMcEl = this.htmlEditor.cleanUpHtmlFromDom(mcEl.dom);

			if (htmlMcEl == htmlOmc) {
				mcEl.dom.innerHTML = nmc;
			} else {
				WT.error(this.mys.res('editor.mailcard.replace.no'));
			}
			return htmlEl.dom.innerHTML;
		} else {
			return html;
		}
	}
	
	
});