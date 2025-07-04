/*
 * webtop-calendar is a WebTop Service developed by Sonicle S.r.l.
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
Ext.define('Sonicle.webtop.mail.model.SharingRights', {
	extend: 'WTA.ux.data.BaseModel',
	requires: [
		'Sonicle.String'
	],
	
	identifier: 'negativestring',
	idProperty: 'subjectSid',
	fields: [
		WTF.field('subjectSid', 'string', false),
		WTF.field('subjectDescription', 'string', false),
		WTF.field('imapId', 'string', false),
		WTF.field('shareIdentity', 'boolean', false, {defaultValue: false}),
		WTF.field('forceMailcard', 'boolean', false, {defaultValue: false}),
		WTF.field('alwaysCc', 'boolean', false, {defaultValue: false}),
		WTF.field('alwaysCcEmail', 'string', false ),
		WTF.field('l', 'boolean', false, {defaultValue: false}),
		WTF.field('r', 'boolean', false, {defaultValue: false}),
		WTF.field('s', 'boolean', false, {defaultValue: false}),
		WTF.field('w', 'boolean', false, {defaultValue: false}),
		WTF.field('i', 'boolean', false, {defaultValue: false}),
		WTF.field('p', 'boolean', false, {defaultValue: false}),
		WTF.field('k', 'boolean', false, {defaultValue: false}),
		WTF.field('a', 'boolean', false, {defaultValue: false}),
		WTF.field('x', 'boolean', false, {defaultValue: false}),
		WTF.field('t', 'boolean', false, {defaultValue: false}),
		WTF.field('n', 'boolean', false, {defaultValue: false}),
		WTF.field('e', 'boolean', false, {defaultValue: false})
	],
	
	applyPreset: function(preset) {
		var set = Ext.bind(this.set, this);
		if ('ro' === preset) {
			set({
				l: true,
				r: true,
				s: true,
				w: false,
				i: false,
				p: false,
				k: false,
				a: false,
				x: false,
				t: false,
				n: false,
				e: false
			});
		} else if ('full' === preset) {
			set({
				l: true,
				r: true,
				s: true,
				w: true,
				i: true,
				p: true,
				k: true,
				a: true,
				x: true,
				t: true,
				n: false,
				e: true
			});
		}
	},
	
	guessPreset: function() {
		var get = Ext.bind(this.get, this);
		if (get('l')
				&& get('r')
				&& get('s')
				&& !get('w')
				&& !get('i')
				&& !get('p')
				&& !get('k')
				&& !get('a')
				&& !get('x')
				&& !get('t')
				&& !get('n')
				&& !get('e')) {
			return 'ro';
			
		} else if (get('l')
				&& get('r')
				&& get('s')
				&& get('w')
				&& get('i')
				&& get('p')
				&& get('k')
				&& get('a')
				&& get('x')
				&& get('t')
				&& !get('n')
				&& get('e')) {
			return 'full';
			
		} else {
			return 'custom';
		}
	}
});
