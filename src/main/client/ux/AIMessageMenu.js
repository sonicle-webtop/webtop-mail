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

Ext.define('Sonicle.webtop.mail.ux.AIMessageMenu', {
	extend: 'Ext.menu.Menu',
	alias: 'widget.soaimessagemenu',

	plain: true,

	mys: null,
	mp: null,
	mv: null,

	initComponent: function() {
		var me = this;
		me.callParent(arguments);

		var cfg = me.mys.getVar('aiMenu');
		if (!cfg || !cfg.items || !cfg.items.length) {
			var msg = WT.res('ai.notConfigured.message');
			me.add({
				text: msg,
				handler: function() { WT.info(msg); }
			});
			return;
		}
		var items = cfg.items;
		for (var i = 0; i < items.length; i++) {
			me.add(me.buildMenuItem(items[i]));
		}
	},

	buildMenuItem: function(def) {
		var me = this;
		if (def.children && def.children.length) {
			var kids = [];
			for (var i = 0; i < def.children.length; i++) {
				kids.push(me.buildMenuItem(def.children[i]));
			}
			return {
				text: def.label,
				menu: { items: kids }
			};
		}
		return {
			text: def.label,
			handler: function() {
				me.runAction(def);
			}
		};
	},

	runAction: function(def) {
		var me = this;
		if (def.input) {
			WT.prompt(def.input.question, {
				title: def.input.title,
				multiline: !!def.input.multiline,
				fn: function(btn, value) {
					if (btn !== 'ok') return;
					if (def.input.required && (!value || value === '')) return;
					me.dispatch(def, value || '');
				}
			});
		} else {
			me.dispatch(def, '');
		}
	},

	dispatch: function(def, userInput) {
		var me = this;
		var v = WT.createView(WT.ID, 'view.AIView', { viewCfg: {} }),
			view = v.getView();
		v.show();
		view.setTitle("A.I. sta pensando...");
		view.setBaloons();
		me.callServer(def, userInput, function(success, answer, params) {
			if (!success) {
				view.setError(answer);
				return;
			}
			if (def.mode === 'reply') {
				v.close();
				me.mys.messagesPanel.folderList.replyMessageById(params.account, params.folder, params.idmessage, true, answer);
				//if (me.mp) me.mp.folderList.replyMessageById(params.account, params.folder, params.idmessage, true, answer);
				//else view.setAnswer(answer, params.format);
			} else {
				view.setAnswer(answer, params.format);
			}
		});
	},

	callServer: function(def, userInput, cb) {
		var me = this,
			params = me.prepareParams(def, userInput);
		WT.ajaxReq(me.mys.ID, 'AIPrompt', {
			timeout: WT.getVar('ajaxLongTimeout'),
			params: params,
			callback: function(success, json) {
				if (!success) {
					cb(false, json.message, params);
					return;
				}
				if (json.success) cb(true, json.data, params);
				else cb(false, json.message, params);
			},
			failure: function(response) {
				if (response.status === 0) {
					cb(false, 'Request failed: possible timeout or network issue', params);
				} else {
					cb(false, 'Failure with status: ' + response.statusText, params);
				}
			}
		});
	},

	prepareParams: function(def, userInput) {
		var me = this;
		if (!me.mv && me.mp) me.mv = me.mp.getMessageView();
		return {
			account: me.mv.acct,
			folder: me.mv.folder,
			idmessage: me.mv.idmessage,
			menuaction: def.id,
			userInput: userInput || '',
			format: 'html'
		};
	}

});
