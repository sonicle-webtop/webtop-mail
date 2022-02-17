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

Ext.define('Sonicle.webtop.mail.view.MailcardEditor', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.mail.model.Mailcard',
		'Sonicle.webtop.core.ux.field.HTMLEditor', // Remove this line when useNewHTMLEditor is no more necessary!
		'Sonicle.webtop.core.ux.field.htmleditor.Field',
		'Sonicle.webtop.core.ux.field.htmleditor.PublicImageTool',
		'Sonicle.webtop.core.ux.field.htmleditor.VariableTool'
	],
	
	dockableConfig: {
		title: '{opts.mailcard-editor.tit}',
		iconCls: 'wtmail-icon-mailcard',
		modal: true,
		width: 675,
		height: 350
	},
	promptConfirm: false,
	
	/**
	 * @property {String} domainId
	 * The referenced domain ID.
	 */
	domainId: null,
	
	full: true,
	
	html: '',
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			buttons: [
				Ext.create('Ext.Button',{
					text: 'Ok',
					width: 100,
					handler: function() {
						me.fireEvent('viewok', me,me.lref("fldhtmleditor").getValue());
						me.closeView(false);
					}
				})
			]
		});
		
		me.callParent(arguments);
		
		if (WT.getVar('useNewHTMLEditor')) {
			me.add({
				region: 'center',
				xtype: 'wthtmleditor',
				reference: 'fldhtmleditor',
				pasteAllowBlobImages: true,
				uploadBlobImages: false,
				enableFont: true,
				enableFontSize: true,
				enableColors: true,
				enableFormats: true,
				enableAlignments: true,
				enableLists: true,
				enableEmoticons: true,
				enableSymbols: true,
				enableLink: true,
				enableImage: true,
				imageConfig: {
					insertImageFile: false
				},
				enableTable: true,
				enableDevTools: true,
				devToolsConfig: {
					codeSample: false
				},
				customTools: {
					publicimage: {
						xtype: 'wt-htmleditortoolpublicimage',
						store: Ext.create('WTA.store.PublicImages', {
							autoLoad: true,
							domainId: me.domainId
						}),
						nameField: 'desc'
					},
					variable: {
						xtype: 'wt-htmleditortoolvariable',
						store: {
							autoLoad: true,
							fields: ['name', 'info'],
							data: [
								['TITLE', me.res('opts.mailcard-editor.tpl.title')],
								['FIRST_NAME', me.res('opts.mailcard-editor.tpl.firstname')],
								['LAST_NAME', me.res('opts.mailcard-editor.tpl.lastname')],
								['COMPANY', me.res('opts.mailcard-editor.tpl.company')],
								['FUNCTION', me.res('opts.mailcard-editor.tpl.function')],
								['EMAIL', me.res('opts.mailcard-editor.tpl.workemail')],
								['MOBILE', me.res('opts.mailcard-editor.tpl.workmobile')],
								['TELEPHONE', me.res('opts.mailcard-editor.tpl.worktelephone')],
								['FAX', me.res('opts.mailcard-editor.tpl.workfax')],
								['CUSTOM_1', me.res('opts.mailcard-editor.tpl.custom1')],
								['CUSTOM_2', me.res('opts.mailcard-editor.tpl.custom2')],
								['CUSTOM_3', me.res('opts.mailcard-editor.tpl.custom3')],
								['TITLE', me.res('opts.mailcard-editor.tpl.title')]
							]
						},
						tooltipField: 'info'
					}
				},
				value: me.html
			});
			
		} else {
			me.add({
				xtype: 'wthtmleditor_old',
				reference: 'fldhtmleditor',
				region: 'center',
				enableFont: true,
				enableFontSize: true,
				enableFormat: true,
				enableColors: true,
				enableAlignments: false,
				enableLinks: false,
				enableLists: false,
				enableSourceEdit: true,
				enableClean: false,
				enableUrlImages: true,
				initialContent: me.html,
				customButtons: [
					'-',
					Ext.create("Ext.button.Button",{
						tabIndex: -1,
						iconCls: 'wtmail-icon-htmleditor-template',
						tooltip: {
							title: me.res('opts.mailcard-editor.b-tpl.tit'),
							text: me.res('opts.mailcard-editor.b-tpl.tip')
						},
						menu: Ext.create("Ext.menu.Menu",{
							listeners: {
								click: function(mnu,itm) {
									if (itm) me.lref("fldhtmleditor").execCommand('inserthtml', false, '{'+itm.text+'}');
								}
							},
							items: [
								{
									text: 'TITLE',
									tooltip: me.res('opts.mailcard-editor.tpl.title')
								},
								{
									text: 'FIRST_NAME',
									tooltip: me.res('opts.mailcard-editor.tpl.firstname')
								},
								{
									text: 'LAST_NAME',
									tooltip: me.res('opts.mailcard-editor.tpl.lastname')
								},
								{
									text: 'COMPANY',
									tooltip: me.res('opts.mailcard-editor.tpl.company')
								},
								{
									text: 'FUNCTION',
									tooltip: me.res('opts.mailcard-editor.tpl.function')
								},
								{
									text: 'EMAIL',
									tooltip: me.res('opts.mailcard-editor.tpl.workemail')
								},
								{
									text: 'MOBILE',
									tooltip: me.res('opts.mailcard-editor.tpl.workmobile')
								},
								{
									text: 'TELEPHONE',
									tooltip: me.res('opts.mailcard-editor.tpl.worktelephone')
								},
								{
									text: 'FAX',
									tooltip: me.res('opts.mailcard-editor.tpl.workfax')
								},
								{
									text: 'CUSTOM_1',
									tooltip: me.res('opts.mailcard-editor.tpl.custom1')
								},
								{
									text: 'CUSTOM_2',
									tooltip: me.res('opts.mailcard-editor.tpl.custom2')
								},
								{
									text: 'CUSTOM_3',
									tooltip: me.res('opts.mailcard-editor.tpl.custom3')
								}
							]
						})
					}),
					Ext.create("Ext.button.Button",{
						tabIndex: -1,
						iconCls: 'wt-icon-format-insertimageurl',
						tooltip: {
							title: me.res('editor.pubimg.tit'),
							text: me.res('editor.pubimg.tip')
						},
						menu: {
							xtype: 'sostoremenu',
							store: Ext.create('WTA.store.PublicImages', {
								autoLoad: true,
								domainId: me.domainId
							}),
							textField: 'desc',
							listeners: {
								click: {
									fn: function(mnu,itm) {
										if (itm) {
											me.lref("fldhtmleditor").execCommand(
												'insertimage', 
												false, 
												encodeURI(mnu.store.getById(itm.itemId).get('url'))
											);	
										}
									},
									scope: this
								}
							}
						}
					})
				]			
			});
		}
	}
});
