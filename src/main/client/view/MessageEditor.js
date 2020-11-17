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
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.webtop.core.ux.RecipientsGrid',
		'Sonicle.webtop.core.ux.field.SuggestCombo',
		'Sonicle.webtop.core.ux.field.HTMLEditor', // Remove this line when useNewHTMLEditor is no more necessary!
		'Sonicle.webtop.core.ux.field.htmleditor.Field',
		'Sonicle.webtop.core.ux.field.htmleditor.PublicImageTool',
		'Sonicle.webtop.core.ux.field.htmleditor.TemplateTool',
		'Sonicle.webtop.mail.model.QuickPart',
		'Sonicle.webtop.mail.model.MessageModel',
		'Sonicle.webtop.mail.view.AddressBookView',
		'Sonicle.upload.Button',
		'Sonicle.webtop.mail.ux.ChooseListConfirmBox'
	],
	
	statics: {
		buildMsgId: function() {
			return (new Date()).getTime();
		}
	},
	
	dockableConfig: {
		title: '{message.tit}',
		iconCls: 'wtmail-icon-newmsg',
		width: 890,
		height: 500
	},
	modelName: 'Sonicle.webtop.mail.model.MessageModel',
	
	confirm: 'yn',
	
	autoToolbar: false,
	identityIndex: 0,
	selectedIdentity: null,
	fontFace: 'Arial',
	fontSize: 12,
	fontColor: '#000000',
	format: 'html',
	
    showSave: true,
    showAddressBook: true,
    showReceipt: true,
    showPriority: true,
	showReminder: true,
    showAttach: true,
	showImportEmail: true,
    showIdentities: true,
    showEditToolbar: true,
    showSourceEdit: true,
    showCloud: true,
	
    autosave: false,
    autosaveDirty: false,
    autosaveTask: null,
    autosaveDelay: 10000,
	
	showMailcard: true,
	
	fax: false,
	faxident: null,
	
	msgId: 0,
	dirty: false,
    
    sendMask: null,

	dumbMailcard: "<p id='wt-mailcard-dumb' style='padding: 0; margin: 0;'>&#160;</p>",
	
	initComponent: function() {
		var me=this;
		
		me.plugins=me.plugins||[];
		me.plugins.push({
			ptype: 'sofiledrop',
			text: WT.res('sofiledrop.text')
		});

		me.callParent(arguments);
		
		if (me.msgId===0) me.msgId=Sonicle.webtop.mail.view.MessageEditor.buildMsgId();
		
		me.identities=me.mys.getVar("identities");
		//save hashed identities, by identityId
		me.identHash={};
		Ext.each(me.identities,function(ident) { me.identHash[''+ident.identityId]=ident},me);
		
        me.attcontainer=Ext.create('Ext.container.Container', {
            width: 250,
            region: 'east',
            scrollable: true,
            cls: 'x-panel-body-default wtmail-table-editor-attachment-container',
            items: [
                me.attlist=Ext.create('Sonicle.webtop.mail.EditorAttachments', {
                    region: 'center',
                    mys: me.mys,
                    bind: {
                        store: '{record.attachments}'
                    }
                })                
            ]
        });
		
        me.attcontainer.on('afterrender',function() {
            var dz=new Ext.dd.DropZone( me.attcontainer.getEl(), {
                ddGroup : 'attachment', 
                getTargetFromEvent: function( e ) {
                    return  me.attcontainer;
                },
                onNodeOver : function(target, dd, e, data){ 
                    return Ext.dd.DropZone.prototype.dropAllowed;
                },                
                onNodeDrop : function(target, dd, e, data){
                    switch(dd.ddGroup) {
						case "attachment":
							me.attachFromMail(data.params);
							break;
						case "mail":
							me.attachFromMessages(data.grid,data.srcFolder,data.records);
							break;
						case "wtvfs-storefile":
							if (data.storeFile) {
								me.attachFromCloud(data.storeFile);
							}
					}
                    return true;
                }
            });		
			dz.addToGroup("mail");
			dz.addToGroup("wtvfs-storefile");
        });
		
		me.recgrid=Ext.create({
			xtype: 'wtrecipientsgrid',
			height: 90,
			region: 'center',
			tabIndex: 2,
			fields: { recipientType: 'rtype', email: 'email' },
			rftype: me.fax?'fax':'email',
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
			disabled: me.faxsubject?true:false,
			enableKeyEvents: true,
			fieldLabel: WT.res('word.subject'),
			labelWidth: 60,
			labelAlign: 'right',
			listeners: {
				keydown: function(cb, e, eOpts) {
					if (e.getKey() === e.TAB) {
						e.stopEvent();
						if (WT.getVar('useNewHTMLEditor')) {
							me.htmlEditor.focus();			
						} else {
							me.htmlEditor.focusEditor();
						}
					}
				}
			},
			bind: '{record.subject}'
        });
		var tbitems=new Array(),
			tbx=0;
		
		var smenu=[],sx=0;
		if (!me.fax) {
			smenu[sx++]={ text: me.res('editor.send.btn-send.lbl'), iconCls: 'wtmail-icon-send-xs', handler: me.actionSend, scope: me };
			if (!me.mys.getVar("schedDisabled"))
				smenu[sx++]={ text: me.res('editor.send.btn-schedule.lbl'), iconCls: 'wtmail-icon-schedule-xs', handler: me.actionSchedule, scope: me };
			tbitems[tbx++]={
				xtype: 'splitbutton',
				text: me.res('editor.send.btn-send.lbl'),
				tooltip: me.res('editor.send.btn-send.lbl'),
				iconCls: 'wtmail-icon-send-xs',
				handler: me.actionSend,
				scope: me,
				menu: smenu
			};
		} else {
			smenu[sx++]={ text: me.res('editor.send.btn-sendfax.lbl'), iconCls: 'wtmail-icon-sendfax-xs', handler: me.actionSend, scope: me };
			if (!me.mys.getVar("schedDisabled"))
				smenu[sx++]={ text: me.res('editor.send.btn-schedule.lbl'), iconCls: 'wtmail-icon-schedule-xs', handler: me.actionSchedule, scope: me };
			tbitems[tbx++]={
				xtype: 'splitbutton',
				text: me.res('editor.send.btn-sendfax.lbl'),
				tooltip: me.res('editor.send.btn-sendfax.lbl'),
				iconCls: 'wtmail-icon-sendfax-xs',
				handler: me.actionSend,
				scope: me,
				menu: smenu
			};
		}
		tbitems[tbx++]='-';
		
		if (me.showSave) {
			tbitems[tbx++]={
				xtype: 'splitbutton',
				//text: me.res('editor.send.btn-save.tip'),
				tooltip: me.res('editor.btn-save.tip'),
				iconCls: 'wt-icon-save-xs',
				handler: me.actionSave,
				scope: me,
				menu: [
					{
						text: me.res('editor.btn-save.tip'),
						//tooltip: me.res('editor.btn-save.tip'),
						iconCls: 'wt-icon-save-xs',
						handler: me.actionSave,
						scope: me				
					},
					{
						text: me.res('editor.btn-save-new.tip'),
						//tooltip: me.res('editor.btn-save-new.tip'),
						iconCls: 'wt-icon-save-new-xs',
						handler: me.actionSaveNew,
						scope: me				
					},
				]
			};
			tbitems[tbx++]='-';
		}
		
		if (me.showAddressBook) {
			tbitems[tbx++]={
				xtype: 'button',
				tooltip: me.res('editor.btn-addressbook.tip'),
				iconCls: 'wtmail-icon-addressbook-xs',
				handler: me.actionAddressBook,
				scope: me
			};
			tbitems[tbx++]='-';
		}
		
        var dash=false;
        if (me.showReceipt) {
			tbitems[tbx++]=me.addRef('chkReceipt', Ext.create({
				xtype: 'button',
				enableToggle: true,
				tooltip: me.res('editor.btn-receipt.tip'),
				iconCls: 'wtmail-icon-msgReceipt',
				handler: me.actionReceipt,
				scope: me
			}));
            dash=true;
        }
        if (this.showPriority) {
			tbitems[tbx++]=me.addRef('chkPriority', Ext.create({
				xtype: 'button',
				enableToggle: true,
				tooltip: me.res('editor.btn-priority.tip'),
				iconCls: 'wtmail-icon-msgPriorityHigh',
				handler: me.actionPriority,
				scope: me
			}));
            dash=true;
        }
        if (this.showReminder) {
			tbitems[tbx++]={
				xtype: 'button',
				enableToggle: true,
				tooltip: me.res('editor.btn-reminder.tip'),
				iconCls: 'wtmail-icon-msgAddReminder',
				handler: me.actionReminder,
				scope: me
			};
            dash=true;
        }
        if (dash) {
            tbitems[tbx++]='-';
        }
		
        if (me.showAttach) {
			tbitems[tbx++]={
				xtype:'souploadbutton',
				tooltip: me.res('editor.btn-attach.tip'),
				iconCls: 'wtmail-icon-attachment-xs',
				uploaderConfig: WTF.uploader(me.mys.ID,'UploadAttachment',{
					extraParams: {
						tag: me.msgId
					},
                    dropElement: me.getId(),
					maxFileSize: me.mys.getVar('attachmentMaxFileSize')
				}),
				listeners: {
					beforeupload: function(s,file) {
						if (WT.getVar('useNewHTMLEditor')) {
							me.wait(file.name, true);
						} else {
							me.htmlEditor.showProgress(file.name);
						}
					},
					uploadcomplete: function(s,fok,ffailed) {
						//console.log("Upload completed - ok: "+fok.length+" - failed: "+ffailed.length);
					},
					uploaderror: function(s, file, cause) {
						if (WT.getVar('useNewHTMLEditor')) {
							me.unwait();
						} else {
							me.htmlEditor.hideProgress();
						}
						WTA.ux.UploadBar.handleUploadError(s, file, cause);
					},
					uploadprogress: function(s,file) {
						if (WT.getVar('useNewHTMLEditor')) {
							me.wait(Ext.String.format('{0}: {1}%', file.name, file.percent), true);
						} else {
							me.htmlEditor.setProgress(file.percent);
						}
					},
					fileuploaded: function(s,file,resp) {
						if (WT.getVar('useNewHTMLEditor')) {
							me.unwait();
						} else {
							me.htmlEditor.hideProgress();
						}
						me.attlist.addAttachment({
							msgId: me.msgId,
							uploadId: resp.data.uploadId,
							fileName: file.name,
							cid: null,
							inline: false,
							fileSize: file.size,
							editable: resp.data.editable
						});
					}
				}
			};		
		}
		
		if (me.showImportEmail) {
			tbitems[tbx++]={
				xtype: 'splitbutton',
				tooltip: me.res('act-pasteList.lbl'),
				iconCls: 'wt-icon-clipboard-paste',
				handler: me.pasteList,
				scope: me,
				menu: [
					{
						text: me.res('act-pasteList.lbl'),
						tooltip: me.res('act-pasteList.tip'),
						iconCls: 'wt-icon-clipboard-paste',
						handler: me.pasteList,
						scope: me				
					},
					{
						text: me.res('act-pasteContactsList.lbl'),
						iconCls: 'wt-icon-clipboard-paste',
						handler: me.pasteContactsList,
						scope: me				
					}
				]
			};
		}
		
		tbitems[tbx++] = me.addRef('showMailcard', Ext.create({
			xtype: 'button',
			enableToggle: true,
			pressed: true,
			iconCls: 'wtmail-icon-mailcardedit-xs',
			tooltip: me.res('editor.btn-showMailcard.tip'),
			handler: me.showMailcardAction,
			scope: me
		}));
			
		if (me.showIdentities) {
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
				
				tbitems[tbx++]='-';
				tbitems[tbx++]={
					xtype:'combo',
					bind: '{record.identityId}',
					tooltip: me.res('editor.cbo-identities.tip'),
					queryMode: 'local',
					displayField: 'description',
					valueField: 'identityId',
					width:me.customToolbarButtons?270:370,
					matchFieldWidth: false,
					listConfig: {
						width: 400
					},
					store: Ext.create('Ext.data.Store', {
						model: 'Sonicle.webtop.mail.model.Identity',
						data : idents
					}),
					//value: selident.identityId,
					listeners: {
						change: {
							fn: function(s,nv,ov) {
								// We are using the select event because change event is
								// fired only after blur. Within this event we have to
								// calculate new and old values manually.
								//var ov = s.lastValue || s.startValue;
								//var nv = r.get('id');
								if (!Ext.isDefined(nv) || !Ext.isDefined(ov) || nv === null || ov === null || ov === nv) return;
								me.selectedIdentity=me.identHash[nv];
                                var format=me.mys.getVar("format");
								if (!this.htmlEditor.isReady()) me.setContent(me.prepareContent(me.htmlEditor.getValue(),format,true,me.identHash[nv].mailcard),format);
								else {
									if(me.showMailcard)
									me.setContent(me.replaceMailcard(me.htmlEditor.getValue(), me.identHash[ov].mailcard.html, me.identHash[nv].mailcard.html),format);
								}
							},
							scope: this
						}
					}
					
				};

            }
		}
		
		//TODO complete implementation
		if (me.showCloud) {
			var vfsapi = WT.getServiceApi('com.sonicle.webtop.vfs');
            if (vfsapi) {
				tbitems[tbx++]='-';
				tbitems[tbx++]={
					xtype: 'souploadbutton',
					itemId: 'btncloudattach',
					tooltip: me.res('editor.btn-cloud-download.tip'),
					iconCls: 'wtmail-icon-cloud-download-xs',
					uploaderConfig: WTF.uploader(me.mys.ID, 'UploadCloudFile', {
						extraParams: {
							tag: me.msgId
						},
						maxFileSize: vfsapi.getVar('privateUploadMaxFileSize'),
						listeners: {
							beforeupload: function(s,file) {
								if (WT.getVar('useNewHTMLEditor')) {
									me.wait(file.name, true);
								} else {
									me.htmlEditor.showProgress(file.name);
								}
							},
							uploadcomplete: function(s,fok,ffailed) {
								//console.log("Upload completed - ok: "+fok.length+" - failed: "+ffailed.length);
							},
							uploaderror: function(s, file, cause) {
								if (WT.getVar('useNewHTMLEditor')) {
									me.unwait();
								} else {
									me.htmlEditor.hideProgress();
								}
								WTA.ux.UploadBar.handleUploadError(s, file, cause);
							},
							uploadprogress: function(s,file) {
								if (WT.getVar('useNewHTMLEditor')) {
									me.wait(Ext.String.format('{0}: {1}%', file.name, file.percent), true);
								} else {
									me.htmlEditor.setProgress(file.percent);
								}
							},
							fileuploaded: function(s,file,resp) {
								if (WT.getVar('useNewHTMLEditor')) {
									me.unwait();
								} else {
									me.htmlEditor.hideProgress();
								}
								vfsapi.addSharingLinkForDownload({
									fileId: vfsapi.buildFileId(0,resp.data.storeId,resp.data.filePath)
								},{
									callback: function(success,result) {
										if (success) {
											if (WT.getVar('useNewHTMLEditor')) {
												me.htmlEditor.editorInsertContent("<br>"+result.embed+"<br>");
											} else {
												me.htmlEditor.execCommand('inserthtml', false, "<br>"+result.embed+"<br>");
											}
										}
									}
								});
							}
						}
					})
					
				};
				tbitems[tbx++]={
					xtype: 'button',
					tooltip: me.res('editor.btn-cloud-upload.tip'),
					iconCls: 'wtmail-icon-cloud-upload-xs',
					handler: function() {
						var subject=me.subject.getValue().trim();
						
						if (Ext.isEmpty(subject)) {
							WT.error(me.res("warn-cloud-empty-subject"));
							return;
						}
						
						WT.ajaxReq(me.mys.ID, "RequestCloudFile", {
							params: {
								subject: subject
							},
							callback: function(success,json) {
								if (success) {
									vfsapi.addSharingLinkForUpload({
										fileId: vfsapi.buildFileId(0,json.storeId,json.filePath)
									},{
										callback: function(success,result) {
											if (success) {
												if (WT.getVar('useNewHTMLEditor')) {
													me.htmlEditor.editorInsertContent("<br>"+result.embed+"<br>");
												} else {
													me.htmlEditor.execCommand('inserthtml', false, "<br>"+result.embed+"<br>");
												}
											}	
										}
									});
								} else {
									WT.error(json.text);
								}
							}
						});					
						
					}
				};
			}
		}
		
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
			height: 160,
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
						me.attcontainer
                    ]
				}),
				me.subject
			]
		}));
		
		if (WT.getVar('useNewHTMLEditor')) {
			me.htmlEditor = me.add({
				region: 'center',
				xtype: 'wthtmleditor',
				bind: '{record.content}',
				disabled: me.faxsubject ? true : false,
				wysiwyg: me.format === 'plain' ? false : true,
				enableFont: true,
				defaultFont: me.fontFace,
				enableFontSize: true,
				defaultFontSize: me.fontSize+'px',
				enableColors: true,
				defaultForeColor: Sonicle.String.removeStart(me.fontColor, '#'),
				enableFormats: true,
				enableAlignments: true,
				enableLists: true,
				enableEmoticons: true,
				enableSymbols: true,
				enableLink: true,
				enableImage: true,
				imageConfig: {
					useEditorProgress: false,
					uploaderConfig: WTF.uploader(me.mys.ID, 'UploadCid',{
						extraParams: {
							tag: me.msgId
						},
						maxFileSize: me.mys.getVar('attachmentMaxFileSize'),
						mimeTypes: 'image/'+'*'
					}),
					prepareImageData: function(file, resp) {
						var upid = resp.data.uploadId;
						return {
							url: WTF.processBinUrl(me.mys.ID, 'PreviewAttachment', {
								uploadId: upid,
								cid: upid
							}),
							name: file.name
						};
					},
					listeners: {
						imagebeforeupload: function(s, upl, file) {
							me.wait(file.name);
						},
						imageuploadprogress: function(s, upl, file) {
							me.wait(Ext.String.format('{0}: {1}%', file.name, file.percent), true);
						},
						imageuploaderror: function(s, upl, file, cause) {
							me.unwait();
							WTA.ux.UploadBar.handleUploadError(s, file, cause);
						},
						imageuploaded: function(s, upl, file, resp) {
							me.unwait();
							var upid = resp.data.uploadId;
							me.attlist.addAttachment({ 
								msgId: me.msgId, 
								uploadId: upid, 
								fileName: file.name, 
								cid: upid,
								inline: true,
								fileSize: file.size,
								editable: resp.data.editable
							});
						}
					}
				},
				enableTable: true,
				enableDevTools: true,
				customTools: {
					template: {
						xtype: 'wt-htmleditortooltemplate',
						store: {
							autoLoad: true,
							model: 'Sonicle.webtop.mail.model.QuickPart',
							proxy: WTF.apiProxy(me.mys.ID, 'ManageQuickParts', null, {
								writer: {
									allowSingle: false
								}
							})
						},
						tooltip: {title: me.mys.res('htmleditor.tool.template.quickpart.tip.tit'), text: me.mys.res('htmleditor.tool.template.quickpart.tip.txt')}
					}
				},
				imagesUploadHandler: function(blobInfo, successCb, failureCb, progress) {
					var blob = blobInfo.blob(),
							payload = {
								filename: blobInfo.filename(),
								mediaType: blob.type,
								size: blob.size,
								base64: blobInfo.base64()
							};
					me.wait('Processing pasted images', true);
					WT.ajaxReq(me.mys.ID, 'UploadBlobInfo', {
						params: {
							tag: me.msgId
						},
						jsonData: payload,
						callback: function(success, json) {
							me.unwait();
							if (success) {
								var upid = json.data;
								me.attlist.addAttachment({ 
									msgId: me.msgId, 
									uploadId: upid, 
									fileName: payload.filename, 
									cid: upid,
									inline: true,
									fileSize: payload.size,
									editable: false
								});
								successCb(WTF.processBinUrl(me.mys.ID, 'PreviewAttachment', {
									uploadId: upid,
									cid: upid
								}));
							} else {
								failureCb();
							}
						}
					});
				},
				contentStyle: [
					'div#wt-mailcard { border: 1px dotted lightgray !important; }'
				].join(' ')
			});
			
		} else {
			me.add(
				me.htmlEditor=Ext.create({
					xtype: 'wthtmleditor_old',
					region: 'center',
					bind: '{record.content}',
					disabled: me.faxsubject?true:false,
					enableFont: true,
					enableFontSize: true,
					enableFormat: true,
					enableColors: true,
					enableEmoticons: true,
					enableAlignments: true,
					enableLinks: true,
					enableLists: true,
					enableSourceEdit: true,
					enableClean: true,
					enableImageUrls: true,
					customButtons: [
						{
							xtype:'souploadbutton',
							tooltip: {
								title: me.res('editor.btn-insertimagefile.tit'),
								text: me.res('editor.btn-insertimagefile.tip'),
								cls: Ext.baseCSSPrefix + 'html-editor-tip'
							},
							iconCls: 'wtmail-icon-format-insertimagefile-xs',
							uploaderConfig: WTF.uploader(me.mys.ID, 'UploadCid',{
								extraParams: {
									tag: me.msgId
								},
								maxFileSize: me.mys.getVar('attachmentMaxFileSize')
							}),
							listeners: {
								beforeupload: function(s,file) {
									me.htmlEditor.showProgress(file.name);
								},
								uploadcomplete: function(s,fok,ffailed) {
									//console.log("Upload completed - ok: "+fok.length+" - failed: "+ffailed.length);
								},
								uploaderror: function(s, file, cause) {
									me.htmlEditor.hideProgress();
									WTA.ux.UploadBar.handleUploadError(s, file, cause);
								},
								uploadprogress: function(s,file) {
									me.htmlEditor.setProgress(file.percent);
								},
								fileuploaded: function(s,file,resp) {
									var uid=resp.data.uploadId;
									me.htmlEditor.hideProgress();
									me.attlist.addAttachment(
											{ 
												msgId: me.msgId, 
												uploadId: uid, 
												fileName: file.name, 
												cid: uid,
												inline: true,
												fileSize: file.size,
												editable: resp.data.editable
											}
									);
									me.htmlEditor.execCommand('insertimage', false, 
										WTF.processBinUrl(me.mys.ID,"PreviewAttachment",{
											uploadId: uid,
											cid: uid
										})
									);
								}
							}
						},
						me.addRef('btnQuickp', {
							xtype:'splitbutton',
							tabIndex: -1,
							iconCls: 'wtmail-icon-format-quickpart-xs',
							tooltip: {
								title: me.res('editor.btn-quickpart.tit'),
								text: me.res('editor.btn-quickpart.tip'),
								cls: Ext.baseCSSPrefix + 'html-editor-tip'
							},
							listeners: {
								click: {
									fn: function() {
										var sel = me.htmlEditor.getSelection(false);
										if(Ext.isEmpty(sel.textContent)) {
											WT.error(me.res('editor.quickpart.error.selection'));
											return;
										}
										WT.prompt(me.res('editor.quickpart.prompt.id'), {
											title: me.res('editor.quickpart.f-id.lbl'),
											fn: function(bid, id) {
												if(bid === 'ok') {
													if(Ext.isEmpty(id)) {
														WT.error(me.res('editor.quickpart.error.id'));
													} else {
														WT.createView(me.mys.ID,'view.QuickPartEditor',{
															viewCfg: {
																mys: me.mys,
																html: sel.html,
																listeners: {
																	viewok: function(s,html) {
																		me.addQuickPart(id,html);
																	}
																}
															}
														}).show();
													}
												}
											}
										});
									}
								},
								afterrender: {
									fn: function(s) {
										me.reloadQuickParts();
									}
								}
							},
							menu: new Ext.menu.Menu({
								items: [
									new Ext.menu.Item({
										itemId: 'manage',
										text: me.res('editor.quickpart.b-manage.lbl'),
										handler: function() {
											WT.createView(me.mys.ID,'view.QuickPartsManager',{
												viewCfg: {
													mys: me.mys,
													listeners: {
														viewclose: function() {
															me.reloadQuickParts();
														}
													}
												}
											}).show();
										},
										scope: this
									}),
									'-'
								],
								listeners: {
									click: {
										fn: function(menu,item,e) {
											if (item && item.getItemId()!=='manage') me.htmlEditor.execCommand('inserthtml', false, item.tag);
										}
									}
								}
							})
						})

					],
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
		}

		me.on('viewdiscard', me.onDiscard);
		me.on('viewload', me.onViewLoad);
		me.on('viewclose',function() {
			if (me.autosaveTask) me.autosaveTask.cancel();
			me.mys.cleanupUploadedFiles(me.msgId);
		});
		me.on('beforemodelsave', function() {
			if (me.autosaveTask) me.autosaveTask.cancel();
		});
		me.on('modelsave', function(s,success) {
			if (success) {
			} else {
				if (me.autosaveTask) me.autosaveTask.delay(me.autosaveDelay);
				me.enableControls(false,true);
			}
		});
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		
		var me=this,
			rg=me.recgrid,
			c=rg.getRecipientsCount();
	
        //me.sendMask=new Ext.LoadMask(me.htmlEditor.wrap, {msg:WT.res("loading")});
		if (c===1) {
			var r=c-1,
				email=rg.getRecipientAt(r);
		
			if (email==="") rg.startEditAt(r);
		} else {
			if (me.subject.getValue()==="") me.subject.focus();
			else {
				if (WT.getVar('useNewHTMLEditor')) {
					me.htmlEditor.focus();
				} else {
					me.htmlEditor.focusEditor();
				}
			}
		}
        if (me.autosave) {
			me.clearAutosaveDirty();
            me.autosaveTask=new Ext.util.DelayedTask(me.doAutosave,me);
            me.autosaveTask.delay(me.autosaveDelay);
        }
	},
	
	startNew: function(data) {
		var me=this,mc=null;
		if ((data.forwardedfrom||data.inreplyto)&&me.mys.getVar("noMailcardOnReplyForward")) {
			mc={
				source: '',
				html: me.dumbMailcard,
				text: ''
			};
			me.getRef("showMailcard").toggle(false,true);
		}
		
		// MessageEditor view needs to be created with the right format.
		if (WT.getVar('useNewHTMLEditor')) {
			data.format = me.mys.getVar("format");
		}
		
		if (!data.fax && !data.contentReady) data.content=me.prepareContent(data.content,data.format,(data.contentAfter===undefined?true:data.contentAfter),mc);
		//default of html editor is html, so no need to enable html mode
		//also calling it seems to break binding
		/*if (data.format==="html") me.htmlEditor.enableHtmlMode();
		else */if (data.format==="plain") {
			if (WT.getVar('useNewHTMLEditor')) {
				// Not needed anymore, MessageEditor view needs to be created with the right format. See above!
			} else {
				me.htmlEditor.enableTextMode();
			}
		}
		
		//check for empty recipients, force an empty one in case
		if (!data.recipients || data.recipients.length==0) {
			data.recipients=[ { rtype: 'to', email: '' } ];
		}
		
		if (data.receipt) me.getRef("chkReceipt").toggle(true);
		if (data.priority) me.getRef("chkPriority").toggle(true);
		
		this.originalContent=data.content;
		this.originalSubject=data.subject;
		
		me.beginNew({
			data: data
		});
		
		if (WT.getVar('useNewHTMLEditor')) {
			// Not needed, focusing already handled internally by new HTMLEditor
		} else {
			if (data.format === "plain") {
				var textarea = Ext.getDom(me.htmlEditor.tmce.inputEl.id);
				var setPosition = new Ext.util.DelayedTask(function(){
					textarea.setSelectionRange(0,0);
					textarea.scrollTo(0,0);
				});
				setPosition.delay(350);
			}
		}
	},
	
	actionReceipt: function(b) {
		this.getModel().set("receipt",b.pressed);
	},
    
	actionPriority: function(b) {
		this.getModel().set("priority",b.pressed);
	},
    
	actionReminder: function(b) {
		this.getModel().set("reminder",b.pressed);
	},
	
	showMailcardAction: function(b) {
		var me = this,
				 format=me.mys.getVar("format"),
				 dumbMailcard = {
					mailcard: {
						html: me.dumbMailcard
					}
				};
				me.showMailcard = b.pressed;
			if(me.showMailcard) {
				me.setContent(me.replaceMailcard(me.htmlEditor.getValue(), dumbMailcard.mailcard.html, me.selectedIdentity.mailcard.html), format);
			} else {
				me.setContent(me.replaceMailcard(me.htmlEditor.getValue(), me.selectedIdentity.mailcard.html, dumbMailcard.mailcard.html), format);
			}
	},
    
    actionSend: function() {
        var me = this,
			attachmentSize = me.attlist.all.elements.length,
			_sbj=me.subject.getValue(),
			newSubject=Ext.isEmpty(_sbj)?'':_sbj.trim(),
			oldSubject=Ext.isEmpty(me.originalSubject)?'':me.originalSubject.trim(),
			newSubjectDiffs = ( newSubject === oldSubject ) ? '' : newSubject,
			newText, oldText, newTextDiffs;
	
		if (me.format === 'html') {
			newText = Sonicle.String.htmlToText(me.htmlEditor.getValue());
			oldText = Sonicle.String.htmlToText(me.originalContent);
		} else {
			newText = me.htmlEditor.getValue();
			oldText = me.originalContent;
		}
		newTextDiffs = me.diffsAsString(oldText,newText);
	
		var attachmentsWarning = attachmentSize > 0 ? false : me.checkForAttachment(newSubjectDiffs.toLowerCase(), newTextDiffs.toLowerCase());
	
        me.recgrid.completeEdit();
		if (newSubject.length > 0){
			if(attachmentsWarning) {
				WT.confirm(me.res('editor.warn.noattachment'), function(bid) {
							if (bid==='yes') {
								me._actionSend();
							}
						});
			}
			else {
				me._actionSend();
			}
		}
		else {
			WT.confirm(me.res('warn-empty-subject'),function(bid) {
				if (bid==='yes') {
					if(attachmentsWarning) {
						WT.confirm(me.res('editor.warn.noattachment'), function(bid) {
							if (bid==='yes') {
								me._actionSend();
							}
						});
					}
					else {
						me._actionSend();
					}
				}
			});
		}
    },
	
	diffsAsString: function(oldText,newText) {
		var oldRows=difflib.stringAsLines(oldText),
			newRows=difflib.stringAsLines(newText),
			sm = new difflib.SequenceMatcher(oldRows, newRows),
			opcodes = sm.get_opcodes(),
			text="";
	
		for(var o=0;o<opcodes.length;++o) {
			var opcode=opcodes[o][0];
			if (opcode==="insert"||opcode==="replace") {
				var		start=opcodes[o][3],
					end=opcodes[o][4];
				for(var i=start;i<end;++i) {
					text+=newRows[i]+"\n";
				}
			}
		}
		return text;
	},
	
//	checkForAttachment: function(subject, body) {
//		var me = this,
//				attachPatterns = me.res('editor.detect.attach.patterns'),
//				attachList = attachPatterns.split(','),
//				containsAttachKeyword = false;
//		
//		attachList.forEach(function(element) {
//			if(subject.includes(element) || body.includes(element)) {
//				containsAttachKeyword = true;
//				return containsAttachKeyword;
//			}
//		});
//		return containsAttachKeyword;
//	},
	
	checkForAttachment: function(subject, body) {
		var me = this,
				patternsKey = 'editorAttachPatterns',
				containsAttachKeyword = false,
				detectedLanguage,
				keyWords, keyWordsList;
		
		guessLanguage.detect(body + " " + subject, function(language) {
				detectedLanguage = language;
		});
		
		keyWords = WT.getVar(patternsKey + "-" + detectedLanguage); 
		if(keyWords) {
			keyWordsList = keyWords.split(',');
		} 
		else {
			var myLanguage = WT.getVar('language');
			keyWords = WT.getVar(patternsKey + "-" + myLanguage.substr(0, myLanguage.indexOf('_')));
			if(keyWords)
			{
				keyWordsList = keyWords.split(',');
			}
		}
		if(!keyWordsList) {
			keyWordsList =  WT.getVar(patternsKey + '-en').split(',');
		}
		
		
		keyWordsList.forEach(function(element) {
			if(subject.includes(element) || body.includes(element)) {
				containsAttachKeyword = true;
				return containsAttachKeyword;
			}
		});
		return containsAttachKeyword;
	},
	
	_actionSend: function() {
        var me=this;
        me.disableControls(/*false,*/true);
        if (me.fireEvent('beforesend',me)) {
            me.sendMessage();
        } else {
            me.enableControls(false,true);
        }
	},
	
	sendMessage: function() {
        var me=this,
			mo=me.getModel();
	
		mo.getProxy().setTimeout(WT.getVar("ajaxSpecialTimeout"));
        mo.setExtraParams({
			msgId: me.msgId,
            action: 'SendMessage',
			sendAction: 'send',
			isFax: me.fax
        });
		if (me.getModel().get("reminder")) {
			today=new Date(),
			tomorrow=new Date(),
			from=me.selectedIdentity.displayName+" <"+me.selectedIdentity.email+">",
			to=me.recgrid.store.getAt(0).get("email"),
			subject=me.subject.getValue(),
			description=
				me.mys.res("column-date")+": "+Ext.util.Format.date(today,'d-M-Y')+"\n"+
				me.mys.res("column-from")+": "+from+"\n"+
				me.mys.res("column-to")+": "+to+"\n";
			me.on("modelsave",function(me, success) {
				if (success) {
					var capi=WT.getServiceApi("com.sonicle.webtop.calendar");
					
					capi.addEvent({
						startDate: tomorrow,
						endDate: Sonicle.Date.add(tomorrow, { minutes: 30 }),
						title: subject,
						description: description,
						reminder: 5
					},{
						dirty: true
					});
				}
			},me,{ single: true });
		}
        me.saveView(true);
	},
    
    actionSave: function() {
        var me=this;
        me.recgrid.completeEdit();
        me.disableControls(/*false,*/true);
        if (me.fireEvent('beforesave',me)) {
            me.saveMessage();
        } else {
            me.enableControls(false,true);
        }
    },
    
    actionSaveNew: function() {
        var me=this;
		me.getModel().set("origuid",0);
		me.actionSave();
	},
	
    saveMessage: function() {
        var me=this;
        me.getModel().setExtraParams({
			msgId: me.msgId,
            action: 'SaveMessage',
			sendAction: 'save'
        });
        me.saveView(true);
    },
	
	actionAddressBook: function() {
		var me=this,
			rcpts=[];
		
		Ext.each(me.recgrid.store.getRange(),function(r) {
			rcpts[rcpts.length]={ type: r.get("rtype"), email: r.get("email") };
		});
		
		WT.createView(me.mys.ID,'view.AddressBookView',{
			viewCfg: {
				mys: me.mys,
				recipients: rcpts,
				listeners: {
					save: {
						fn: function(abv) {
							var rcpts=abv.getRecipients();
							me.recgrid.clear();
							if (rcpts && rcpts.length>0) {
								Ext.each(rcpts,function(r) {
									me.recgrid.addRecipient(r.type,r.email);
								});
							} else {
								me.recgrid.addRecipient('to','');
							}
						}
					}
				}
			}
		}).show();
	},
    
    actionSchedule: function() {
        var me=this;
        me.recgrid.completeEdit();
		WT.createView(me.mys.ID,'view.ScheduleDialog',{
			viewCfg: {
				mys: me.mys,
				listeners: {
					viewok: function(v,date,time,notify) {
						me.disableControls(/*false,*/true);
						if (me.fireEvent('beforeschedule',me)) {
							me.schedule(date,time,notify);
						} else {
							this.enableControls(false,true);
						}
					}
				}
			}
		}).show();
    },
	
	schedule: function(date,time,notify) {
        var me=this;
        me.getModel().setExtraParams({
			msgId: me.msgId,
            action: 'ScheduleMessage',
			sendAction: 'schedule',
			scheddate: Ext.Date.format(date,'d/m/Y'),
			schedtime: Ext.Date.format(time,'H:i'),
			schednotify: notify?'true':'false'
        });
        me.saveView(true);
	},
    
    prepareContent: function(content,format,contentAfter,mc) {
		var me=this;
		if (!mc) {
			var ident=me.identities[me.identityIndex];
			if (ident.mailcard) mc=ident.mailcard;
		}
		
		if (format==="html") {
			if (mc) {
				var mchtml='<div id="wt-mailcard">'+mc.html+'</div>';
				if (contentAfter) content=mchtml+content;
				else content+=mchtml;
			}
			
			if (WT.getVar('useNewHTMLEditor')) {
				var HE = Sonicle.form.field.tinymce.HTMLEditor,
					ff = HE.getContentFontFamily(me.htmlEditor.fonts, me.fontFace),
					fs = me.fontSize+'px', // Do NOT be strict, allow any font sizes!
					//fs = HE.getContentFontSize(me.htmlEditor.fontSizes, me.fontSize+'px'), // BE strict, allow only a set of font sizes!
					fc = HE.getContentColor(me.htmlEditor.fontColors, Sonicle.String.removeStart(me.fontColor, '#'));
				content = HE.generateInitialContent(ff, fs, fc, '#000000') + content;
				
			} else {
				content = '<div style="font-family:'+me.fontFace+';font-size:'+me.fontSize+'px;color:'+me.fontColor+';"></div>'
					+ '<div style="font-family:'+me.fontFace+';font-size:'+me.fontSize+'px;color:'+me.fontColor+';"></div>'
					+ content;
			}
		}
		else if (format==="plain") {
			if (mc) {
				var mctext='\n\n'+mc.text+'\n';
				if (contentAfter) content=mctext+content;
				else content+=mctext;
			}
		}
        return content;
    },
	
    setContent: function(content,format) {
		var me=this;
        //me.htmlEditor.initHtmlValue(html);
		me.getModel().set("content",content);
        me.getModel().set("format",format);
		
/*        var w=this.htmlEditor.getWin();
        var d=w.document;
        var n=d.body.firstChild;
        if (w.getSelection) {
            var r=d.createRange();
            r.setStart(n,0);
            r.setEnd(n,0);
            var s = this.htmlEditor.win.getSelection();
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
		
		var me = this,
				hed = me.htmlEditor,
				mcNode,
				origMc, curMc;
		if (WT.getVar('useNewHTMLEditor')) {
			// html arg is no more useful, mailcard is replaced directly here without full setContent need
			mcNode = hed.editorDomQuery('#wt-mailcard', {first: true});
			if (mcNode) {
				origMc = hed.editorSerialize(Ext.DomHelper.createDom({html: omc}));
				curMc = hed.editorSerialize(mcNode);
				if (origMc === curMc) {
					hed.editorSetHtml(mcNode, nmc);
				} else {
					WT.error(me.res('editor.mailcard.replace.no'));
				}
			}
			return html;
			
		} else {
			var htmlDom = Ext.dom.Helper.createDom({html: html}),
				mcDom = htmlDom.querySelector('#wt-mailcard');
			if(mcDom) {
				var htmlOmc, htmlMcEl;
				htmlOmc = this.htmlEditor.cleanUpHtml(omc);
				htmlMcEl = this.htmlEditor.cleanUpHtmlFromDom(mcDom);

				if (htmlMcEl == htmlOmc) {
					mcDom.innerHTML = nmc;
				} else {
					WT.error(this.res('editor.mailcard.replace.no'));
				}
				html=htmlDom.innerHTML;
			}
			return html;
		}	
	},
	
    disableControls: function(/*showProgress,*/showSendmask) {
        var me=this;
        me.toolbar.disable();
        //if (showProgress) this.progress.show();
        //this.aSend.disable();
        //if (this.aSave) this.aSave.disable();
        //if (this.aReceipt) this.aReceipt.disable();
        if (!showSendmask) me.htmlEditor.disable();
        //else me.sendMask.show();
        me.recgrid.disable();
        me.subject.disable();
    },
	
    fillRecipients: function(rcpts,rtypes) {
        Ext.each(
          this.recgrid.store.getRange(),
          function(r,index,allItems) {
            rcpts[index]=Ext.util.Format.htmlDecode(r.get("email"));
            rtypes[index]=r.get("rtype");
          }
        );
    },	

    doAutosave: function() {
		var me=this,o=me.opts.data;
        if (me.isAutosaveDirty()) {
            var mailparams={
				folder: o.folder,
				subject: me.subject.getValue(),
				content: WT.getVar('useNewHTMLEditor') ? me.htmlEditor.getValue() : me.htmlEditor.getEditingValue(),
				identityId: me.selectedIdentity.identityId,
				format: o.format,
				replyfolder: o.replyfolder,
				inreplyto: o.inreplyto,
				references: o.references,
				origuid: o.origuid,
				priority: me.getModel().get("priority"),
				receipt: me.getModel().get("receipt"),
				forwardedfolder: o.forwardedfolder,
				forwardedfrom: o.forwardedfrom,
				recipients: [],
				rtypes: []
            };
            me.fillRecipients(mailparams.recipients,mailparams.rtypes);

			WT.ajaxReq(me.mys.ID, "ManageAutosave", {
				params: {
					crud: "update",
					context: "newmail",
					key: me.msgId
				},
				jsonData: mailparams,
				callback: function(success,json) {
					if (success) {
						// me.mys can be null in case of callback after view closing
						if (me.mys && me.mys.isDrafts(me.mys.currentAccount, me.mys.currentFolder)) {
							me.mys.reloadFolderList();
							me.mys.messagesPanel.clearMessageView();
						}
						
					} else {
						WT.error(json.text);
					}
				}
			});					
			
			me.clearAutosaveDirty();
		} else {
			//console.log("autosave is clean");
		}
		me.autosaveTask.delay(me.autosaveDelay);
	},
	
    attachFromMail: function(params) {
		var me=this;
		
		WT.ajaxReq(me.mys.ID, "AttachFromMail", {
			params: {
				tag: me.msgId,
                folder: params.folder,
                idmessage: params.idmessage,
                idattach: params.idattach
			},
			callback: function(success,json) {
				if (success) {
					var d=json.data;
					me.attlist.addAttachment(
						{ 
							msgId: me.msgId, 
							uploadId: d.uploadId, 
							fileName: d.name, 
							cid: null,
							inline: false,
							fileSize: d.size,
							editable: d.editable
						}
					);
				} else {
					WT.error(json.message);
				}
			}
		});
    },	
	
    attachFromMessages: function(grid,folder,recs) {
		var me=this,
		    data=grid.sel2ids(recs);
		
		WT.ajaxReq(me.mys.ID, "AttachFromMessages", {
			params: {
				tag: me.msgId,
                folder: folder,
				ids: data.ids,
				multifolder: data.multifolder
			},
			callback: function(success,json) {
				if (success) {
					Ext.each(json.data,function(item) {
						me.attlist.addAttachment(
							{ 
								msgId: me.msgId, 
								uploadId: item.uploadId, 
								fileName: item.name, 
								cid: null,
								inline: false,
								fileSize: item.size,
								editable: item.editable
							}
						);
					});
				} else {
					WT.error(json.message);
				}
			}
		});
    },
	
	attachFromCloud: function(storeFileData) {
		var me = this;
		WT.ajaxReq(me.mys.ID, 'AttachFromCloud', {
			params: {
				storeId: storeFileData.storeId,
				path: storeFileData.path,
				tag: me.msgId
			},
			callback: function(success,json) {
				if (success) {
					var data = json.data;
					me.attlist.addAttachment({ 
						fileName: data.name, 
						cid: null,
						inline: false,
						fileSize: data.size,
						uploadId: data.uploadId
					});
				} else {
					WT.error(json.message);
				}
			}
		});
	},
	
	reloadQuickParts: function() {
		var me=this;
		
		WT.ajaxReq(me.mys.ID, "ManageQuickParts", {
			params: {
				crud: 'read'
			},
			callback: function(success,json) {
				if (success) {
					me.fillQPMenu(json.data);
				} else {
					WT.error(json.message);
				}
			}
		});
	},
	
	addQuickPart: function(id, html) {
		var me=this;
		
		WT.ajaxReq(me.mys.ID, "ManageQuickParts", {
			params: {
				crud: 'create', 
				id: id, 
				html: html
			},
			callback: function(success,json) {
				if (success) {
					me.fillQPMenu(json.data);
				} else {
					WT.error(json.message);
				}
			}
		});
	},
	
	fillQPMenu: function(items) {
		var me=this,
			menu = me.getRef('btnQuickp').menu,
			menuitems=menu.items.items;
	
		for(var i=menuitems.length-1;i>=2;--i)
			menu.remove(menuitems[i]);
		
		Ext.each(items,function(item) {
			menu.add(Ext.create({
				xtype: 'menuitem',
				//itemId: item.id,
				text: item.id,
				tag: item.html
			}));
		});
		menu.updateLayout();
	},
	
	
	pasteList: function() {
		var me=this;
		
		WT.prompt('',{
			title: me.mys.res("act-pasteList.tit"),
			fn: function(btn,text) {
				if (btn==='ok') {
					me.recgrid.loadValues(text);
				}
			},
			scope: me,
			width: 400,
			multiline: 200,
			value: ''
		});
	},
	
	pasteContactsList: function() {
		var me = this;
		WT.confirm(me.mys.res('confirmBox.listChoose.lbl'), function(bid, value) {
			if (bid === 'ok') {
				var conSvc = WT.getServiceApi('com.sonicle.webtop.contacts');
					conSvc.expandRecipientsList({
							address: value
						}, {
							callback: function(success, json) {
								if (success) {
									var data = json.data,
											emails = '', i;
									for (i=0; i<data.length; i++) {
										emails += data[i] + '\n';
									}
									this.recgrid.loadValues(emails);
								}
							},
							scope: me
					});
				
			}
		}, me, {
			buttons: Ext.Msg.OKCANCEL,
			title: me.mys.res('act-pasteContactsList.confirm.tit'),
			instClass: 'Sonicle.webtop.mail.ux.ChooseListConfirmBox'
		});
	},
	
    onDiscard: function() {
		var me=this;
		WT.ajaxReq(me.mys.ID, "DiscardMessage", {
			params: {
				msgId: me.msgId
			},
			callback: function(success,json) {
				if (success) {
					//if (me.mys.isDrafts(me.mys.currentFolder)) {
					//	me.mys.reloadFolderList();
					//	me.mys.messagesPanel.clearMessageView();
					//}
				} else {
					WT.error(json.text);
				}
			}
		});					
	},
	
    enableControls: function() {
        var me=this;
        me.toolbar.enable();
        //this.progress.hide();
        //this.aSend.enable();
        //if (this.aSave) this.aSave.enable();
        //if (this.aReceipt) this.aReceipt.enable();
        if (!me.faxsubject) me.htmlEditor.enable();
        //me.sendMask.hide();
        me.recgrid.enable();
        if (!me.faxsubject) me.subject.enable();
    },

	
    isAutosaveDirty: function() {
		var me=this;
		return me.recgrid.isRecipientComboAutosaveDirty() || 
			me.attlist.isAutosaveDirty() ||
			(me.autosaveSubjectValue!==me.subject.getValue()) || 
			(WT.getVar('useNewHTMLEditor') ? me.htmlEditor.isHistoryBookmarkDirty() : me.htmlEditor.isAutosaveDirty()) ||
			me.getModel().isAutosaveDirty();
    },
    
    clearAutosaveDirty: function() {
		var me=this;
        me.recgrid.clearAutosaveDirty();
		me.attlist.clearAutosaveDirty();
        me.autosaveSubjectValue=me.subject.getValue();
		if (WT.getVar('useNewHTMLEditor')) {
			me.htmlEditor.resetHistoryBookmark();
		} else {
			me.htmlEditor.clearAutosaveDirty();
		}
        me.getModel().clearAutosaveDirty();
    },
	
	res: function(key) {
		return this.mys.res(key);
	},	
	
	showConfirm: function() {
		var me = this, msg;
		msg = me.confirmMsg || WT.res('confirm.areyousure');
			WT.confirm(msg, function(bid) {
				if (bid === 'yes') {
					me.onDiscardView();
				}
				else if(bid === 'cancel') {
					me.saveMessage();
				}
			}, me, {
				config: {
					buttonText: {
						cancel: me.res('act-save.lbl')
					}
				}
			});	
				
	}
	
});

Ext.define('Sonicle.webtop.mail.EditorAttachments', {
	extend: 'Ext.view.View',
	
//	cls: 'x-panel-body-default',
	overItemCls: 'wtmail-table-editor-attachement-over',
	itemSelector: 'table.wtmail-table-editor-attachment',
	
	mys: null,
	autosaveDirty: false,
	
	tpl: new Ext.XTemplate(
		"<tpl for='.'>",
			"<table border=0 cellspacing=0 cellpadding=0 class='wtmail-table-editor-attachment'>",
			  "<tr>",
				"<td class='wtmail-td-editor-attachment-icon'>",
					"<div class='{[WTF.fileTypeCssIconCls(WTA.Util.getFileExtension(values['fileName']))]}' style='width:16px;height:16px'>",
				"</td>",
				"<td class='wtmail-td-editor-attachment-text'>",
					"<a href='javascript:Ext.emptyFn()' title='{fileName}'>",
						"&nbsp;{fileName}",
					"</a>",
				"</td>",
				"<td class='wtmail-td-editor-attachment-delete-icon'>",
					"<div class='wt-icon-delete' style='width: 16px; height: 16px;'>",
				"</td>",
			  "</tr>",
			"</table>",
		"</tpl>"
	),
	
	listeners: {
		itemclick: function(s, r, item, ix, e) {
			var me=this,
				tgt=e.getTarget(null,null,true);

			if (tgt.hasCls('wt-icon-delete')) {
				//me.ownerCt.remove(this);
				//me.fireEvent('remove',me);
				me.getStore().remove(r);
				me.autosaveDirty=true;
			}
			else {
				var params={
					uploadId: r.get("uploadId")
				}
				if (r.get("editable")) me.viewFile(params);
				else {
					var href=WTF.processBinUrl(me.mys.ID,"PreviewAttachment",params);
					Sonicle.URLMgr.open(href,true,"location=no,menubar=no,resizable=yes,scrollbars=yes,status=yes,titlebar=yes,toolbar=no,top=10,left=10,width=770,height=480");
				}
			}
		}
	},
	
    viewFile: function(params) {
		var me=this;
		WT.ajaxReq(me.mys.ID, 'DocPreviewAttachment', {
			params: params,
			callback: function(success, json) {
				if (success) {
					var editingCfg=json.data;
					var vw = WT.createView(WT.ID, 'view.DocEditor', {
						swapReturn: true,
						viewCfg: {
							editingId: editingCfg.editingId,
							editorConfig: {
								editable: editingCfg.writeSupported,
								token: editingCfg.token,
								docType: editingCfg.docType,
								docExtension: editingCfg.docExtension,
								docKey: editingCfg.docKey,
								docTitle: editingCfg.docName,
								docUrl: editingCfg.docUrl,
								//autosave: false,
								callbackUrl: editingCfg.callbackUrl
							}
						}
					});
					vw.showView(function() {
						vw.begin('view');
					});
				}
			}
		});
    },	
	
	
	
	addAttachment: function(config) {
		this.getStore().add(config);
		this.autosaveDirty=true;
	},
	
	isAutosaveDirty: function() {
		return this.autosaveDirty;
	},
	
	clearAutosaveDirty: function() {
		this.autosaveDirty=false;
	}
});

