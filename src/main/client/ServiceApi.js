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
Ext.define('Sonicle.webtop.mail.ServiceApi', {
	extend: 'WTA.sdk.ServiceApi',
	
	/**
	 * Gets the choosen format for new messages.
	 * @return {html|plain} Compose message content format.
	 */
	getComposeFormat: function() {
		return this.service.getVar('format');
	},
	
	/**
	 * Create a new message.
	 * @param {Object} data An object containing event data.
	 * @param {plain|html} [data.format] The email format.
	 * @param {String} [data.subject] The email subject.
	 * @param {String} [data.content] The email content, plain text or html.
	 * @param {Object[]} [data.recipients] The email recipients:
     *     recipients: [
     *       { rtype: 'to', email: 'example1@domain.tld' },
     *       { rtype: 'cc', email: 'example2@domain.tld' },
     *       { rtype: 'bcc', email: 'example3@domain.tld' }
     *     ]	 
	 * @param {Object[]} [data.attachments] The email attachments:
     *     attachments: [
     *       { uploadId: [server UploadedFile id], fileName: "[the file name]", fileSize: [the file size] }
     *     ]	 
	 * @param {Object} opts An object containing configuration.
	 * @param {Number} [opts.messageEditorId] the message editor id for reference in the attachements
	 * @param {Boolean} [opts.contentReady] True if content is ready without any need of processing (e.g. mailcard).
	 * @param {Boolean} [opts.appendContent] When contentReady=false, append content to processing (e.g. mailcard).
	 * @param {Function} [opts.callback] Callback method for 'viewsave' event.
	 * @param {Object} [opts.scope] The callback method scope.
	 * @param {Boolean} [opts.dirty] The dirty state of the model.
	 */
	newMessage: function(data,opts) {
		var folder=this.service.getFolderInbox();
		opts = opts || {};
		this.service.startNewMessage(folder, { 
			format: data.format,
			subject: data.subject,
			content: data.content,
			recipients: data.recipients,
			attachments: data.attachments,
			msgId: opts.messageEditorId,
			contentReady: opts.contentReady,
			contentAfter: opts.appendContent,
			folder: folder
		});
	},
	
	/**
	 * Build a new msg id for editing context.
	 */
	buildMessageEditorId: function() {
		return Sonicle.webtop.mail.view.MessageEditor.buildMsgId();
	}
	
});
