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
		'Sonicle.String',
		'Sonicle.button.Toggle',
		'Sonicle.webtop.core.ux.RecipientsGrid',
		'Sonicle.webtop.core.ux.field.SuggestCombo',
		'Sonicle.webtop.core.ux.field.htmleditor.Field',
		'Sonicle.webtop.core.ux.field.htmleditor.PublicImageTool',
		'Sonicle.webtop.core.ux.field.htmleditor.TemplateTool',
		'Sonicle.webtop.mail.model.QuickPart',
		'Sonicle.webtop.mail.model.MessageModel',
		'Sonicle.webtop.mail.view.AddressBookView',
		'Sonicle.upload.Button',
		'Sonicle.webtop.mail.ux.ChooseListConfirmBox'
	],
	uses: [
		'Sonicle.webtop.core.view.Meeting'
	],
	
	statics: {
		buildMsgId: function() {
			return (new Date()).getTime();
		}
	},
	
	dockableConfig: {
		title: '{message.tit}',
		iconCls: 'wtmail-icon-mailNew',
		width: 860,
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
	
	/**
	 * @cfg {html|plain} [contentFormat]
	 */
	contentFormat: 'html',
	
    showSave: true,
    showAddressBook: true,
    showReceipt: true,
    showPriority: true,
	showReminder: true,
	showMeeting: true,
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
	
	htmlStartMailcard: "<div id=\"wt-mailcard\"",
	
	initComponent: function() {
		var me = this,
				vm = me.getVM();
		
		me.plugins=me.plugins||[];
		me.plugins.push({
			ptype: 'sofiledrop',
			text: WT.res('sofiledrop.text')
		});
		WTU.applyFormulas(vm, {
			foHasMeeting: WTF.foIsEmpty('record', 'meetingUrl', true),
			foMeetingDisabled: {
				bind: {bindTo: '{record.meetingUrl}'},
				get: function(val) {
					if (Ext.isEmpty(WT.getMeetingProvider()) || !WT.isPermitted(WT.ID, 'MEETING', 'CREATE')) return true;
					return !Ext.isEmpty(val);
				}
			}
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
						me.htmlEditor.focus();
					}
				}
			},
			bind: '{record.subject}'
        });
		var tbitems=new Array(),
			tbx=0;
		
		var smenu=[],sx=0;
		if (!me.fax) {
			smenu[sx++]={ text: me.res('editor.send.btn-send.lbl'), iconCls: 'wtmail-icon-mailSend', handler: me.actionSend, scope: me };
			if (!me.mys.getVar("schedDisabled"))
				smenu[sx++]={ text: me.res('editor.send.btn-schedule.lbl'), iconCls: 'wtmail-icon-mailSchedule', handler: me.actionSchedule, scope: me };
			tbitems[tbx++]={
				xtype: 'splitbutton',
				text: me.res('editor.send.btn-send.lbl'),
				tooltip: me.res('editor.send.btn-send.lbl'),
				iconCls: 'wtmail-icon-mailSend',
				handler: me.actionSend,
				scope: me,
				menu: smenu
			};
		} else {
			smenu[sx++]={ text: me.res('editor.send.btn-sendfax.lbl'), iconCls: 'wtmail-icon-faxSend', handler: me.actionSend, scope: me };
			if (!me.mys.getVar("schedDisabled"))
				smenu[sx++]={ text: me.res('editor.send.btn-schedule.lbl'), iconCls: 'wtmail-icon-faxSchedule', handler: me.actionSchedule, scope: me };
			tbitems[tbx++]={
				xtype: 'splitbutton',
				text: me.res('editor.send.btn-sendfax.lbl'),
				tooltip: me.res('editor.send.btn-sendfax.lbl'),
				iconCls: 'wtmail-icon-faxSend',
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
				iconCls: 'wt-icon-save',
				handler: me.actionSave,
				scope: me,
				menu: [
					{
						text: me.res('editor.btn-save.tip'),
						//tooltip: me.res('editor.btn-save.tip'),
						iconCls: 'wt-icon-save',
						handler: me.actionSave,
						scope: me				
					}, {
						text: me.res('editor.btn-save-new.tip'),
						//tooltip: me.res('editor.btn-save-new.tip'),
						iconCls: 'wt-icon-save-new',
						handler: me.actionSaveNew,
						scope: me				
					}
				]
			};
			tbitems[tbx++]='-';
		}
		
		if (me.showAddressBook) {
			tbitems[tbx++]={
				xtype: 'button',
				tooltip: me.res('editor.btn-addressbook.tip'),
				iconCls: 'wtmail-icon-addressbook',
				handler: me.actionAddressBook,
				scope: me
			};
			tbitems[tbx++]='-';
		}
		
        var dash=false;
        if (me.showReceipt) {
			tbitems[tbx++]=me.addRef('chkReceipt', Ext.create({
				xtype: 'sotogglebutton',
				offTooltip: {title: me.res('editor.btn-receipt.tip.tit'), text: me.res('editor.btn-receipt.off.tip.txt')},
				onTooltip: {title: me.res('editor.btn-receipt.tip.tit'), text: me.res('editor.btn-receipt.on.tip.txt')},
				offIconCls: 'wtmail-icon-msgSetReceipt-grayed',
				onIconCls: 'wtmail-icon-msgSetReceipt',
				handler: me.actionReceipt,
				scope: me
			}));
            dash=true;
        }
        if (this.showPriority) {
			tbitems[tbx++]=me.addRef('chkPriority', Ext.create({
				xtype: 'sotogglebutton',
				offTooltip: {title: me.res('editor.btn-priority.tip.tit'), text: me.res('editor.btn-priority.off.tip.txt')},
				onTooltip: {title: me.res('editor.btn-priority.tip.tit'), text: me.res('editor.btn-priority.on.tip.txt')},
				offIconCls: 'wtmail-icon-msgSetPriorityHigh-grayed',
				onIconCls: 'wtmail-icon-msgSetPriorityHigh',
				handler: me.actionPriority,
				scope: me
			}));
            dash=true;
        }
        if (this.showReminder && WT.getServiceApi('com.sonicle.webtop.calendar')) {
			tbitems[tbx++]={
				xtype: 'sotogglebutton',
				offTooltip: {title: me.res('editor.btn-reminder.tip.tit'), text: me.res('editor.btn-reminder.off.tip.txt')},
				onTooltip: {title: me.res('editor.btn-reminder.tip.tit'), text: me.res('editor.btn-reminder.on.tip.txt')},
				offIconCls: 'wtmail-icon-msgAddReminder-grayed',
				onIconCls: 'wtmail-icon-msgAddReminder',
				handler: me.actionReminder,
				scope: me
			};
            dash=true;
        }
		if (me.showMeeting) {
			tbitems[tbx++] = me.addAct('addMeeting', {
				text: null,
				tooltip: WT.res(WT.ID, 'act-addMeeting.lbl', WT.getMeetingConfig().name),
				iconCls: 'wt-icon-newMeeting',
				bind: {
					disabled: '{foMeetingDisabled}'
				},
				handler: function(s) {
					me.addMeetingUI();
				}
			});
			dash = true;
		}
        if (dash) {
            tbitems[tbx++]='-';
        }
		
        if (me.showAttach) {
			tbitems[tbx++]={
				xtype:'souploadbutton',
				tooltip: me.res('editor.btn-attach.tip'),
				iconCls: 'wtmail-icon-attachment',
				uploaderConfig: WTF.uploader(me.mys.ID,'UploadAttachment',{
					extraParams: {
						tag: me.msgId
					},
                    dropElement: me.getId(),
					maxFileSize: me.mys.getVar('attachmentMaxFileSize')
				}),
				listeners: {
					beforeupload: function(s,file) {
						me.wait(file.name, true);
					},
					uploadcomplete: function(s,fok,ffailed) {
						//console.log("Upload completed - ok: "+fok.length+" - failed: "+ffailed.length);
					},
					uploaderror: function(s, file, cause) {
						me.unwait();
						WTA.ux.UploadBar.handleUploadError(s, file, cause);
					},
					uploadprogress: function(s,file) {
						me.wait(Ext.String.format('{0}: {1}%', file.name, file.percent), true);
					},
					fileuploaded: function(s,file,resp) {
						me.unwait();
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
			var mitems = [
					{
						text: me.res('act-pasteList.lbl'),
						tooltip: me.res('act-pasteList.tip'),
						iconCls: 'wt-icon-clipboard-paste',
						handler: me.pasteList,
						scope: me				
					}
			];
			if (WT.getServiceApi('com.sonicle.webtop.contacts'))
				mitems[1] = {
					text: me.res('act-pasteContactsList.lbl'),
					iconCls: 'wt-icon-clipboard-paste',
					handler: me.pasteContactsList,
					scope: me				
				};

			tbitems[tbx++]={
				xtype: 'splitbutton',
				tooltip: me.res('act-pasteList.lbl'),
				iconCls: 'wt-icon-clipboard-paste',
				handler: me.pasteList,
				scope: me,
				menu: mitems
			};
		}
		
		tbitems[tbx++]='-';
		tbitems[tbx++] = me.addRef('showMailcard', Ext.create({
			xtype: 'sotogglebutton',
			offTooltip: {title: me.res('editor.btn-mailcard.tip.tit'), text: me.res('editor.btn-mailcard.off.tip.txt')},
			onTooltip: {title: me.res('editor.btn-mailcard.tip.tit'), text: me.res('editor.btn-mailcard.on.tip.txt')},
			offIconCls: 'wtmail-icon-msgSetMailcard-grayed',
			onIconCls: 'wtmail-icon-msgSetMailcard',
			handler: me.showMailcardAction,
			pressed: me.showMailcard,
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
				
				tbitems[tbx++]={
					xtype:'combo',
					bind: '{record.identityId}',
					tooltip: me.res('editor.cbo-identities.tip'),
					queryMode: 'local',
					displayField: 'description',
					valueField: 'identityId',
					width:me.customToolbarButtons?270:370,
					editable: false,
					selectOnFocus: false,
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
                                var format=me.contentFormat;
								if (!me.htmlEditor.isReady()) {
									me.setContent(me.prepareContent(me.htmlEditor.getValue(),format,true,me.identHash[nv].mailcard),format);
								} else {
									if (me.showMailcard) {
										me.replaceMailcardUI(me.identHash[nv].mailcard, me.identHash[ov].mailcard);
									}
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
					iconCls: 'wtmail-icon-cloud-download',
					uploaderConfig: WTF.uploader(me.mys.ID, 'UploadCloudFile', {
						extraParams: {
							tag: me.msgId
						},
						maxFileSize: vfsapi.getVar('privateUploadMaxFileSize'),
						multiSelection: false,
						listeners: {
							beforeupload: function(s,file) {
								me.wait(file.name, true);
							},
							uploadcomplete: function(s,fok,ffailed) {
								//console.log("Upload completed - ok: "+fok.length+" - failed: "+ffailed.length);
							},
							uploaderror: function(s, file, cause) {
								me.unwait();
								WTA.ux.UploadBar.handleUploadError(s, file, cause);
							},
							uploadprogress: function(s,file) {
								me.wait(Ext.String.format('{0}: {1}%', file.name, file.percent), true);
							},
							fileuploaded: function(s,file,resp) {
								me.unwait();
								vfsapi.addSharingLinkForDownload({
									fileId: vfsapi.buildFileId(0,resp.data.storeId,resp.data.filePath)
								},{
									callback: function(success,result) {
										if (success) {
											var format = me.contentFormat,
													newContent;
											
											if ('html' === format) {
												newContent = result.embed;
											} else if ('plain' === format) {
												newContent = Sonicle.String.htmlToText(result.embed, {preserveHyperlinksHref: true});
											}
											me.htmlEditor.insertContent(newContent);
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
					iconCls: 'wtmail-icon-cloud-upload',
					handler: function() {
						var subject=me.subject.getValue();
						if (subject) subject=subject.trim();
						
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
												var format = me.contentFormat,
													newContent;
												
												if ('html' === format) {
													newContent = result.embed;
												} else if ('plain' === format) {
													newContent = Sonicle.String.htmlToText(result.embed, {preserveHyperlinksHref: true});
												}
												me.htmlEditor.insertContent(newContent);
											}	
										}
									});
								} else {
									WT.error(json.message);
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
		
		me.htmlEditor = me.add({
			region: 'center',
			xtype: 'wthtmleditor',
			bind: '{record.content}',
			disabled: me.faxsubject ? true : false,
			wysiwyg: me.contentFormat === 'plain' ? false : true,
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
					pos: 19,
					xtype: 'wt-htmleditortooltemplate',
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.mail.model.QuickPart',
						sorters: 'name',
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
		if (!success) return;
		
		var me = this,
			mo = me.getModel(),
			stoRcpts = mo.recipients();
		
		if (stoRcpts.getCount() === 0) {
			stoRcpts.add(stoRcpts.createModel({rtype: 'to', email: ''}));
			// Defers it otherwise grid's internal editor may not be available at the moment of method call!
			Ext.defer(function() { me.recgrid.startEditAt(0); }, 200);
		} else if (Ext.isEmpty(mo.get('subject'))) {
			me.subject.focus();
		} else {
			me.htmlEditor.focus();
		}
		
		if (me.autosave) {
			me.clearAutosaveDirty();
			me.autosaveTask = new Ext.util.DelayedTask(me.doAutosave, me);
			me.autosaveTask.delay(me.autosaveDelay);
		}
	},
	
	startNew: function(data) {
		var me=this,mc=null,
				contentAfter = Ext.isBoolean(data.contentAfter) ? data.contentAfter : true;
		delete data.contentAfter;
		// Make sure that the format used for UI initialization is the same in data!
		data.format = me.contentFormat;
		if ((data.forwardedfrom||data.inreplyto)&&me.mys.getVar("noMailcardOnReplyForward")) {
			mc={
				source: '',
				html: me.dumbMailcard,
				text: ''
			};
			me.getRef("showMailcard").toggle(false,true);
		}
		
		if (!data.fax && !data.contentReady) data.content=me.prepareContent(data.content,me.contentFormat,contentAfter,mc);
		//default of html editor is html, so no need to enable html mode
		//also calling it seems to break binding
		/*if (me.contentFormat==="html") me.htmlEditor.enableHtmlMode();
		else */
		
		//check for empty recipients, force an empty one in case
		//if (!data.recipients || data.recipients.length==0) {
		//	data.recipients=[ { rtype: 'to', email: '' } ];
		//}
		
		if (data.receipt) me.getRef("chkReceipt").toggle(true);
		if (data.priority) me.getRef("chkPriority").toggle(true);
		
		this.originalContent=data.content;
		this.originalSubject=data.subject;
		
		me.beginNew({
			data: data
		});
		
		if (data.contentReady && data.content.indexOf(me.htmlStartMailcard)<0) {
			me.getRef("showMailcard").toggle(false,true);
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
			dumbMailcard = {
			   mailcard: {
				   html: me.dumbMailcard,
				   text: ''
			   }
		   };
		   me.showMailcard = b.pressed;
		
		if (me.showMailcard) {
			me.replaceMailcardUI(me.selectedIdentity.mailcard, dumbMailcard.mailcard);
		} else {
			me.replaceMailcardUI(dumbMailcard.mailcard, me.selectedIdentity.mailcard);
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
	
		if (me.contentFormat === 'html') {
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
		var me = this,
				timeout = WT.getVar('ajaxSpecialTimeout'),
				mo = me.getModel(),
				hasReminder = mo.get('reminder') === true,
				hasMeeting = !Ext.isEmpty(mo.get('meetingUrl')),
				eventDesc = hasReminder || hasMeeting ? me.res('editor.newEventTemplate') : null;
		
		if (Ext.isNumber(timeout)) mo.getProxy().setTimeout(timeout);
		mo.setExtraParams({
			msgId: me.msgId,
			action: 'SendMessage',
			sendAction: 'send',
			isFax: me.fax
		});
		
		me.saveView(true, {
			callback: function(success, model) {
				if (success) {
					if (hasReminder || hasMeeting) {
						var SoD = Sonicle.Date,
								capi = WT.getServiceApi('com.sonicle.webtop.calendar');
						if (capi) {
							var now = new Date(),
									sentOn = Ext.Date.format(now, WT.getShortDateFmt()),
									subject = mo.get('subject'),
									desc = Ext.String.format(eventDesc, sentOn, subject, mo.get('from'), Sonicle.String.ellipsisJoin(', ', 5, mo.getRecipients()));
							
							if (hasMeeting) {
								WT.confirm(me.res('editor.confirm.meetingFound'), function(bid) {
									if (bid === 'yes') {
										var schedAt = SoD.idate(mo.get('meetingSchedule'), now),
												link = mo.get('meetingUrl');
										capi.addEvent({
											startDate: schedAt,
											endDate: SoD.add(schedAt, {minutes: 30}),
											timezone: mo.get('meetingScheduleTz'),
											title: subject,
											location: link,
											description: link + '\n\n' + desc
										},{dirty: true});
										
									} else if (bid === 'no') {
										Sonicle.URLMgr.open(mo.get('meetingUrl'), true);
									}
								}, me, {
									buttons: Ext.Msg.YESNOCANCEL,
									config: {
										buttonText: {
											yes: me.res('editor.confirm.meetingFound.reminder'),
											no: me.res('editor.confirm.meetingFound.join'),
											cancel: me.res('editor.confirm.meetingFound.cancel')
										}
									}
								});
								
							} else if (hasReminder) {
								capi.addEvent({
									startDate: now,
									endDate: SoD.add(now, {minutes: 30}),
									title: subject,
									description: desc,
									reminder: 5
								},{dirty: true});
							}
						} else {
							WT.confirm(me.res('editor.confirm.meetingFound'), function(bid) {
								if (bid === 'yes') {
									Sonicle.URLMgr.open(mo.get('meetingUrl'), true);
								}
							}, me, {
								buttons: Ext.Msg.YESNO,
								config: {
									buttonText: {
										yes: me.res('editor.confirm.meetingFound.join'),
										no: me.res('editor.confirm.meetingFound.cancel')
									}
								}
							});
						}
					}
				}
			}
		});
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
	
	prepareContent: function(content, format, contentAfter, mailcard) {
		var me = this,
				mcContent = '',
				ret = null;
		
		if (!mailcard) {
			var ident = me.identities[me.identityIndex];
			if (ident.mailcard) mailcard = ident.mailcard;
		}
		
		if ('html' === format) {
			if (mailcard) {
				if (mailcard.text.trim()) mcContent = me.htmlStartMailcard+'>' + mailcard.html + '</div>';
				else mcContent = me.htmlStartMailcard+' style="display: none !important">' + mailcard.html + '</div>';
			}
			
			var HE = Sonicle.form.field.tinymce.HTMLEditor,
				ff = HE.getContentFontFamily(me.htmlEditor.fonts, me.fontFace),
				fs = me.fontSize+'px', // Do NOT be strict, allow any font sizes!
				//fs = HE.getContentFontSize(me.htmlEditor.fontSizes, me.fontSize+'px'), // BE strict, allow only a set of font sizes!
				fc = HE.getContentColor(me.htmlEditor.fontColors, Sonicle.String.removeStart(me.fontColor, '#'));

			if (contentAfter) {
				ret = HE.generateInitialParagraph('', ff, fs, fc, '#000000') + mcContent + content;
			} else {
				ret = HE.generateInitialParagraph(content, ff, fs, fc, '#000000') + mcContent;
			}
				
		} else if ('plain' === format) {
			if (mailcard && mailcard.text.trim()) mcContent = '\n\n' + mailcard.text + '\n';
			ret = contentAfter ? (mcContent + content) : (content + mcContent);
		}
		return ret;
	},
	
    setContent: function(content, format) {
		var me = this;
        //me.htmlEditor.initHtmlValue(html);
		me.getModel().set({
			content: content,
			format: format
		});
		//me.getModel().set("content",content);
        //me.getModel().set("format",format);
		
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
	
	replaceMailcardUI: function(nmc, omc) {
		var me = this;
		if (!me.replaceMailcard(nmc, omc)) {
			WT.error(me.res('editor.mailcard.replace.no'));
		}
	},
	
	replaceMailcard: function(nmc, omc) {
		var me = this,
				hed = me.htmlEditor,
				mcNode, origMc, curMc;
		if (!Ext.isEmpty(nmc.html) && !Ext.isEmpty(omc.html)) {
			mcNode = hed.editorDomQuery('#wt-mailcard', {first: true});
			if (mcNode) {
				origMc = hed.editorSerialize(Ext.DomHelper.createDom({html: omc.html}));
				curMc = hed.editorSerialize(mcNode);
				if (origMc === curMc) {
					var ed=hed.getEditor();
					if (ed) {
						var newDisplayStyle;
						if (!Ext.isEmpty(Ext.String.trim(nmc.text))) newDisplayStyle = 'block !important';
						else newDisplayStyle = 'none !important';
						mcNode.style="display: "+newDisplayStyle;
						ed.dom.setStyle(mcNode,'display',newDisplayStyle);
					}					
					hed.editorSetHtml(mcNode, nmc.html);
				}
			} else {
				var tpl=hed.editorGetDocument().createElement("template");
				tpl.innerHTML=me.htmlStartMailcard+'>' + nmc.html + '</div>';
				hed.editorGetBody().appendChild(tpl.content.firstChild);
			}
			return true;
		}
		return false;
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
				content: me.htmlEditor.getValue(),
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
						WT.error(json.message);
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
					//me.fillQPMenu(json.data);
					me.reloadQuickParts();
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
					WT.error(json.message);
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
			me.htmlEditor.isHistoryBookmarkDirty() ||
			me.getModel().isAutosaveDirty();
    },
    
    clearAutosaveDirty: function() {
		var me=this;
        me.recgrid.clearAutosaveDirty();
		me.attlist.clearAutosaveDirty();
        me.autosaveSubjectValue=me.subject.getValue();
		me.htmlEditor.resetHistoryBookmark();
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
				
	},
	
	addMeetingUI: function() {
		var me = this,
				Meeting = Sonicle.webtop.core.view.Meeting,
				name = WT.getVar('userDisplayName'),
				fmt = Ext.String.format;
		Meeting.promptForInfo({
			whatAsRoomName: true,
			callback: function(ok, values) {
				if (ok) {
					me.wait();
					Meeting.getMeetingLink(values[0], {
						callback: function(success, data) {
							me.unwait();
							if (success) {
								var pdate = values[1], ptz = values[2],
										sdate = Ext.isDate(pdate) ? Ext.Date.format(pdate, WT.getShortDateTimeFmt()) + ' ('+ptz+')' : null,
										subj = fmt(data.embedTexts.subject, name),
										desc = sdate ? fmt(data.embedTexts.schedDescription, name, sdate, data.link) : fmt(data.embedTexts.unschedDescription, name, data.link),
										format = me.contentFormat,
										mo = me.getModel(),
										newContent;
								
								if (Ext.isEmpty(mo.get('subject'))) {
									mo.set('subject', subj);
								}
								if ('html' === format) {
									newContent = Sonicle.String.htmlEncodeLineBreaks(desc.linkify());
								} else if ('plain' === format) {
									/* Nothing to do: newContent is already in text format! */
								}
								
								mo.set('meetingUrl', data.link);
								mo.set('meetingSchedule', pdate);
								mo.set('meetingScheduleTz', ptz);
								me.htmlEditor.insertContent(newContent);
							}
						}
					});
				}
			}
		});
	},
	
	contentNewLines: function(howMany) {
		if (!Ext.isNumber(howMany)) howMany = 1;
		var format = this.contentFormat, sep = '';
		if ('html' === format) {
			sep = '<br>';
		} else if ('plain' === format) {
			sep = '\n';
		}
		return Sonicle.String.repeat(sep, howMany);
	},
	
	joinContent: function(content, appendContent, location, newLines) {
		if (!Ext.isNumber(newLines)) newLines = 0;
		var me = this,
				SoS = Sonicle.String,
				sep = me.contentNewLines(newLines);
		if ('below' === location) {
			return SoS.join(sep, content, appendContent);
		} else if ('above' === location) {
			return SoS.join(sep, appendContent, content);
		} else {
			return content;
		}
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
			"<table border=0 cellspacing=0 cellpadding=0 class='wtmail-table-editor-attachment'"+
					" style='visibility:{[Ext.isEmpty(values['cid'])?'visibile':'hidden']}'>",
			  "<tr>",
				"<td class='wtmail-td-editor-attachment-icon'>",
					"<div class='{[WTF.fileTypeCssIconCls(WTA.Util.getFileExtension(values['fileName']))]}' style='width:16px;height:16px'>",
				"</td>",
				"<td class='wtmail-td-editor-attachment-text'>",
					"<a href='javascript:Ext.emptyFn()' title='{fileName:htmlAttributeEncode}'>",
						"&nbsp;{fileName:htmlEncode}",
					"</a>",
				"</td>",
				"<td class='wtmail-td-editor-attachment-size'>",
					"({[WTU.humanReadableSize(values['fileSize'])]})",
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
				};
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

