/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
 * Copyright (C) 2025 Sonicle S.r.l.
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
Ext.define('Sonicle.webtop.mail.store.MessageFlags', {
	extend: 'Ext.data.ArrayStore',
	
	autoLoad: true,
	fields: [
		{name: 'id', type: 'string'},
		{name: 'name', type: 'string'},
		{name: 'iconCls', type: 'string'},
		{name: 'color', type: 'string'}
	],
	data: [
		['red','','',''],
		['blue','','',''],
		['yellow','','',''],
		['green','','',''],
		['orange','','',''],
		['purple','','',''],
		['black','','',''],
		['gray','','',''],
		['white','','',''],
		['brown','','',''],
		['azure','','',''],
		['pink','','',''],
		['complete','','','']
	],
	
	constructor: function(cfg) {
		var me = this,
			SoCU = Sonicle.ColorUtils,
			findFlagColor = function(flag) {
				var rawColor = Sonicle.CssUtils.getRule('.wtmail-flag-'+flag, 'color'),
					ret = '#737373', // Neutral/500
					colorObj;
				if (!Ext.isEmpty(rawColor)) {
					colorObj = SoCU.parseColor(rawColor);
					if (colorObj) ret = SoCU.rgb2hex(colorObj, true);
				}
				return ret;
			};
		Ext.each(me.config.data, function(row) {
			row[1] = WT.res('com.sonicle.webtop.mail', 'message.flag.'+row[0]);
			row[2] = row[0] === 'complete' ? 'fas fa-check' : 'fas fa-bookmark';
			row[3] = findFlagColor(row[0]);
		});
		me.callParent([cfg]);
	}
});