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
		{ name: "reminder", type: 'boolean' },
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
        { name: "origuid", type: 'int' }
	]
	
});

Ext.define('Sonicle.webtop.mail.model.MessageRecipientModel', {
    extend: 'WTA.model.Autosave',
    fields: [
//        WTF.fkField('string'),
		{ name: "msgId", type: 'int', reference: { parent: 'Sonicle.webtop.mail.model.MessageModel', inverse: 'recipients' } },
		{ name: "rtype", type: 'string' },
		{ name: "email", type: 'string' }
	]
});

Ext.define('Sonicle.webtop.mail.model.AttachmentModel', {
    extend: 'WTA.model.Autosave',
    fields: [
//        WTF.fkField('string'),
		{ name: "msgId", type: 'int', reference: { parent: 'Sonicle.webtop.mail.model.MessageModel', inverse: 'attachments' } },
		{ name: "uploadId", type: 'string' },
		{ name: "fileName", type: 'string' },
		{ name: "cid", type: 'string' },
		{ name: "inline", type: 'boolean' },
		{ name: "fileSize", type: 'int' }
	]
});
