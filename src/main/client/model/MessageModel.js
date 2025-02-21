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

Ext.define('Sonicle.webtop.mail.model.MessageModel', {
    extend: 'WTA.model.Autosave',
    proxy: WTF.apiProxy('com.sonicle.webtop.mail', 'ManageMessage','data', {
		writer: {
			type: 'sojson',
			writeAssociations: true
		}
	}),
    idProperty: 'msgId',
    
    fields: [
		{ name: "msgId", type: 'int' },
		{ name: "receipt", type: 'boolean' },
		{ name: "priority", type: 'boolean' },
		{ name: "from", type: 'string' },
		{ name: "subject", type: 'string' },
		{ name: "content", type: 'string' },
        { name: "format", type: 'string' },
        { name: "identityId", type: 'int' },
        //Reply data
        { name: "replyfolder", type: 'string' },
        { name: "inreplyto", type: 'string' },
        { name: "references", type: 'string' },
        //Forward data
        { name: "forwardedfolder", type: 'string' },
        { name: "forwardedfrom", type: 'string' },
        //Reply/Forward data
        { name: "origuid", type: 'int' },
        { name: "draftuid", type: 'int' },
		{ name: "draftfolder", type: 'string' },
		WTF.field('reminder', 'boolean', true, {persist: false}),
		WTF.field('meetingUrl', 'string', true, {persist: false}),
		WTF.field('meetingSchedule', 'date', true, {persist: false}),
		WTF.field('meetingScheduleTz', 'string', true, {persist: false})
	],
	
	hasMany: [
		WTF.hasMany('torecipients', 'Sonicle.webtop.mail.model.MessageRecipientModel'),
		WTF.hasMany('ccrecipients', 'Sonicle.webtop.mail.model.MessageRecipientModel'),
		WTF.hasMany('bccrecipients', 'Sonicle.webtop.mail.model.MessageRecipientModel'),
		WTF.hasMany('attachments', 'Sonicle.webtop.mail.model.AttachmentModel')
	],
	
	getMaxRecipients: function() {
		return WT.getVar('com.sonicle.webtop.mail', 'newMessageMaxRecipients');
	},
	
	getRecipientsCount: function() {
		var me = this;
		return me.torecipients().getCount() + me.ccrecipients().getCount() + me.bccrecipients().getCount();
	},
	
	getAllRecipients: function() {
		var me=this,
			rcpts=[];
		
		Ext.each(me.torecipients().getRange(),function(r) {
			rcpts[rcpts.length]={ type: "to", email: r.get("email") };
		});
		Ext.each(me.ccrecipients().getRange(),function(r) {
			rcpts[rcpts.length]={ type: "cc", email: r.get("email") };
		});
		Ext.each(me.bccrecipients().getRange(),function(r) {
			rcpts[rcpts.length]={ type: "bcc", email: r.get("email") };
		});
		return rcpts;
	},
	
	getAllEmails: function() {
		var me=this,
			emails=[];
		
		Ext.each(me.torecipients().getRange(),function(r) {
			emails[emails.length]=r.get("email");
		});
		Ext.each(me.ccrecipients().getRange(),function(r) {
			emails[emails.length]=r.get("email");
		});
		Ext.each(me.bccrecipients().getRange(),function(r) {
			emails[emails.length]=r.get("email");
		});
		return emails;
	}
	
	
	/*getRecipients: function() {
		var arr = [];
		this.recipients().each(function(rec) {
			arr.push(rec.get('email'));
		});
		return arr;
	}*/
	
});

Ext.define('Sonicle.webtop.mail.model.MessageRecipientModel', {
    extend: 'WTA.model.Autosave',
    fields: [
		{ name: "email", type: 'string' }
	]
});

Ext.define('Sonicle.webtop.mail.model.AttachmentModel', {
    extend: 'WTA.model.Autosave',
    fields: [
		{ name: "uploadId", type: 'string' },
		{ name: "fileName", type: 'string' },
		{ name: "cid", type: 'string' },
		{ name: "inline", type: 'boolean' },
		{ name: "fileSize", type: 'int' }
	]
});
