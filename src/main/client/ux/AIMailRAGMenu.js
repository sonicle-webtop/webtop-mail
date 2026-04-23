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

Ext.define('Sonicle.webtop.mail.ux.AIMailRAGMenu', {
	extend: 'Ext.menu.Menu',
	alias: 'widget.soaimailragmenu',
	requires: [
		'Sonicle.webtop.mail.view.AIMessageGridView',
		'Sonicle.webtop.mail.view.AIMessageHtmlView'
	],
	
	plain: true,

	mys: null,
	
	initComponent: function() {
		var me = this, today = new Date();
		me.callParent(arguments);
		
		me.add({
			text: 'Trova messaggi relativi a...',
			handler: function() {
				me.runGridView();
			}
		});
		me.add({
			text: 'Riassunto di oggi',
			handler: function() {
				me.runHtmlView(
					{
						question: "Elenca i messaggi di oggi",
						when: "Solo oggi",
						instructions: "fai un riassunto raggruppando le email correlate tra di loro in ordine di data descrescente",
						hidePrompt: true
					}
				);
			}
		});
		me.add({
			text: 'Riassunto di ieri',
			handler: function() {
				me.runHtmlView(
					{
						question: "Elenca i messaggi di ieri",
						when: "Solo ieri",
						instructions: "fai un riassunto raggruppando le email correlate tra di loro in ordine di data descrescente",
						hidePrompt: true
					}
				);
			}
		});
		me.add({
			text: 'Riassunto della settimana',
			handler: function() {
				me.runHtmlView(
					{
						question: "Elenca i messaggi di questa settimana",
						when: "fino a 7 giorni fa",
						instructions: "fai un riassunto raggruppando le email correlate tra di loro in ordine di data descrescente",
						hidePrompt: true
					}
				);
			}
		});
		me.add({
			text: 'Trova con istruzioni',
			handler: function() {
				me.runHtmlView();
			}
		});
	},
	
	runGridView: function() {
		var me=this;
		WT.createView(me.mys.ID,'view.AIMessageGridView').show();
	},
	
	runHtmlView: function(cfg) {
		var me=this, 
			win = WT.createView(me.mys.ID,'view.AIMessageHtmlView',{
			viewCfg: cfg
		});
		win.show(false, function() {
			if (cfg) win.getView().runPrompt();
		});
	}
	
});