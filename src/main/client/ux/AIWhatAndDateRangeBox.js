/* 
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.mail.ux.AIWhatAndDateRangeBox', {
	extend: 'WTA.ux.window.CustomPromptMsgBox',
	requires: [
		'Sonicle.Utils',
		'Sonicle.webtop.core.ux.panel.Fields'
	],
	
	defaultFocus: 'what',
	startDay: 1,
	dateFormat: 'Y-m-d',
	timeFormat: 'H:i',
	whatText: '',
	whenText: '',
	instructionsText: '',
	
	hideWhat: false,
	hideWhen: false,
	hideInstructions: false,
	
	/*constructor: function(cfg) {
		var me = this,
			icfg = Sonicle.Utils.getConstructorConfigs(me, cfg, [
				{hideWhat: true, hideDateTime: true}
			]);
		if (icfg.hideWhat === true && icfg.hideDateTime === false) cfg.defaultFocus = 'date';
		me.callParent([cfg]);
	},*/
	
	createCustomPrompt: function() {
		var me = this;
		return {
			xtype: 'wtfieldspanel',
			bodyCls: 'wt-theme-bg-dialog',
			referenceHolder: true,
			defaults: {
				labelAlign: 'top'
			},
			items: [
				{
					xtype: 'textfield',
					itemId: 'what',
					reference: 'what',
					enableKeyEvents: true,
					hidden: me.hideWhat,
					listeners: {
						keydown: me.onPromptKey,
						scope: me
					},
					fieldLabel: me.whatText,
					anchor: '100%'
				}, {
					xtype: 'textfield',
					itemId: 'when',
					reference: 'when',
					enableKeyEvents: true,
					hidden: me.hideWhen,
					listeners: {
						keydown: me.onPromptKey,
						scope: me
					},
					fieldLabel: me.whenText,
					anchor: '100%'
				}, {
					xtype: 'textfield',
					itemId: 'instructions',
					reference: 'instructions',
					enableKeyEvents: true,
					hidden: me.hideInstructions,
					listeners: {
						keydown: me.onPromptKey,
						scope: me
					},
					fieldLabel: me.instructionsText,
					anchor: '100%'
				}/* ,{
					xtype: 'fieldcontainer',
					layout: 'hbox',
					items: [
						{
							xtype: 'datefield',
							reference: 'datestart',
							startDay: me.startDay,
							format: me.dateFormat,
							enableKeyEvents: true,
							listeners: {
								keydown: me.onPromptKey,
								scope: me
							},
							margin: '0 5 0 0',
							maxWidth: 120,
							flex: 1
						}, {
							xtype: 'datefield',
							reference: 'dateend',
							startDay: me.startDay,
							format: me.dateFormat,
							enableKeyEvents: true,
							listeners: {
								keydown: me.onPromptKey,
								scope: me
							},
							margin: '0 5 0 0',
							maxWidth: 120,
							flex: 1
						}
					],
					fieldLabel: me.whenText,
					anchor: '100%'
				}/*,
				WTF.localCombo('id', 'desc', {
					reference: 'timezone',
					hidden: me.hideDateTime,
					store: {
						type: 'wttimezone',
						autoLoad: true
					},
					enableKeyEvents: true,
					listeners: {
						keydown: me.onPromptKey,
						scope: me
					},
					anchor: '100%'
				})*/
			],
			width: 300
		};
	},
	
	setCustomPromptValue: function(value) {
		var SoD = Sonicle.Date,
				cp = this.customPrompt;
		if (Ext.isArray(value)) {
			if (value.length>0) cp.lookupReference('what').setValue(value[0]);
			if (value.length>1) cp.lookupReference('when').setValue(value[1]);
			if (value.length>2) cp.lookupReference('instructions').setValue(value[2]);
			//cp.lookupReference('datestart').setValue(SoD.clone(value[1]));
			//cp.lookupReference('dateend').setValue(SoD.clone(value[2]));
		}
	},
	
	getCustomPromptValue: function() {
		var SoD = Sonicle.Date,
				cp = this.customPrompt/*,
				datestart = cp.lookupReference('datestart').getValue(),
				dateend = cp.lookupReference('dateend').getValue();*/
		return [
			cp.lookupReference('what').getValue(),
			cp.lookupReference('when').getValue(),
			cp.lookupReference('instructions').getValue(),/*,
			(!Ext.isDate(datestart) ? null : SoD.clone(datestart)),
			(!Ext.isDate(dateend) ? null : SoD.clone(dateend))*/
		];
	}
});
