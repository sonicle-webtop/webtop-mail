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
Ext.define('Sonicle.webtop.mail.model.SieveRule', {
	extend: 'Ext.data.Model',
	
	fields: [
		{name: 'order', type: 'integer', validators: [
			{type: 'presence'}
		]},
		{name: 'field', type: 'string', validators: [
			{type: 'presence'}
		]},
		{name: 'argument', type: 'string', validators: [
			{type: 'wtmail-sieveruleargument'}
		]},
		{name: 'operator', type: 'string', validators: [
			{type: 'presence'},
			{type: 'wtmail-sieveruleoperator'}
		]},
		{name: 'value', type: 'string', validators: [
			{type: 'presence'},
			{type: 'wtmail-sieverulevalue'}
		]}
	]
});
Ext.define('Sonicle.webtop.mail.model.VSieveRuleArgument', {
	extend: 'Ext.data.validator.Validator',
	alias: 'data.validator.wtmail-sieveruleargument',
	
	constructor: function(cfg) {
		this.vpre = Ext.create('Ext.data.validator.Presence');
		this.callParent([cfg]);
	},
	
	validate: function(v, rec) {
		if (rec.get('field') === 'header') {
			return this.vpre.validate(v, rec);
		}
		return true;
	}
});
Ext.define('Sonicle.webtop.mail.model.VSieveRuleOperator', {
	extend: 'Ext.data.validator.Validator',
	alias: 'data.validator.wtmail-sieveruleoperator',
	
	validate: function(v, rec) {
		if (rec.get('field') === 'size') {
			return ['isunder','isover'].indexOf(v) !== -1;
		}
		return true;
	}
});
Ext.define('Sonicle.webtop.mail.model.VSieveRuleValue', {
	extend: 'Ext.data.validator.Validator',
	alias: 'data.validator.wtmail-sieverulevalue',
	
	validate: function(v, rec) {
		if (rec.get('field') === 'size') {
			return /^[0-9]+$/i.test(v);
		}
		return true;
	}
});
