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
package com.sonicle.webtop.mail.rest.v1;

import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.mail.email.AttachmentResource;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.mail.email.EmailMessageBuilder;
import com.sonicle.mail.email.EmailPopulatingBuilder;
import com.sonicle.mail.email.Recipient;
import com.sonicle.mail.parser.MimeMessageParser;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.WTEmailSendException;
import com.sonicle.webtop.core.model.Tag;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.mail.MailManager;
import com.sonicle.webtop.mail.bol.model.Identity;
import com.sonicle.webtop.mail.swagger.v1.api.MeMessagesApi;
import com.sonicle.webtop.mail.swagger.v1.model.ApiApiError;
import com.sonicle.webtop.mail.swagger.v1.model.ApiAttachment;
import com.sonicle.webtop.mail.swagger.v1.model.ApiAttachmentNew;
import com.sonicle.webtop.mail.swagger.v1.model.ApiContact;
import com.sonicle.webtop.mail.swagger.v1.model.ApiMessage;
import com.sonicle.webtop.mail.swagger.v1.model.ApiMessageNew;
import com.sun.mail.imap.IMAPMessage;
import jakarta.activation.DataSource;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gbulfon
 */
public class MeMessages extends MeMessagesApi {
	private static final Logger logger = LoggerFactory.getLogger(MeMessages.class);
	
	
	@Override
	public Response listMessages(String folderId, Integer pageNo, Integer pageSize) {
		try {
			ArrayList<ApiMessage> items = new ArrayList<>();
			UserProfileId targetPid = RunContext.getRunProfileId();
			MailManager mmgr = MailRestApiUtils.getMailManager(targetPid);
			Map<String, Tag> tagsMap = WT.getCoreManager().listTags();
			int ipageNo = pageNo!=null ? pageNo.intValue() : -1;
			int ipageSize = pageSize!=null ? pageSize.intValue() : 50;
			mmgr.consumeMessages(folderId, ipageNo, ipageSize, new MailManager.MessagesConsumer() {
				@Override
				public void consume(Message msg, long uid) throws MessagingException, IOException {
					MimeMessage mmsg = (MimeMessage) msg;
					ApiMessage am = new ApiMessage();
					String hmid[] = mmsg.getHeader("Message-ID");
					if (hmid!=null && hmid.length>0) {
						am.setId(hmid[0]);
						am.setUid((int)uid);
						am.setSubject(mmsg.getSubject());
						am.setBody("");

	//sender
						Address addrs[] = mmsg.getFrom();
						ApiContact ac = new ApiContact();
						if (addrs!=null & addrs.length>0) {
							InternetAddress iaddr = (InternetAddress) addrs[0];
							ac.setName(iaddr.getPersonal());
							ac.setEmail(iaddr.getAddress());
						}
						am.setSender(ac);

						//recipients
						addrs = mmsg.getRecipients(Message.RecipientType.TO);
						if (addrs!=null) {
							for(Address addr: addrs) {
								InternetAddress iaddr = (InternetAddress) addr;
								ac = new ApiContact();
								ac.setName(iaddr.getPersonal());
								ac.setEmail(iaddr.getAddress());
								am.addRecipientsItem(ac);
							}
						}
						else am.setRecipients(new ArrayList<ApiContact>());

						//ccs
						addrs = mmsg.getRecipients(Message.RecipientType.CC);
						if (addrs!=null) {
							for(Address addr: addrs) {
								InternetAddress iaddr = (InternetAddress) addr;
								ac = new ApiContact();
								ac.setName(iaddr.getPersonal());
								ac.setEmail(iaddr.getAddress());
								am.addCcItem(ac);
							}
						}
						else am.setCc(new ArrayList<ApiContact>());

						//bccs
						addrs = mmsg.getRecipients(Message.RecipientType.BCC);
						if (addrs!=null) {
							for(Address addr: addrs) {
								InternetAddress iaddr = (InternetAddress) addr;
								ac = new ApiContact();
								ac.setName(iaddr.getPersonal());
								ac.setEmail(iaddr.getAddress());
								am.addBccItem(ac);
							}
						}
						else am.setBcc(new ArrayList<ApiContact>());

						Calendar cal = Calendar.getInstance();
						Date date = mmsg.getReceivedDate();
						if (date == null) date = new Date();
						cal.setTime(date);
						am.setDate(
								cal.get(Calendar.YEAR)+"-"+
								StringUtils.leftPad(""+(cal.get(Calendar.MONTH)+1), 2, '0')+"-"+
								StringUtils.leftPad(""+cal.get(Calendar.DAY_OF_MONTH), 2, '0')+" "+
								StringUtils.leftPad(""+cal.get(Calendar.HOUR_OF_DAY), 2, '0')+":"+
								StringUtils.leftPad(""+cal.get(Calendar.MINUTE), 2, '0'));

						
						am.setIsRead(mmsg.isSet(Flags.Flag.SEEN));
						
						Flags flags = mmsg.getFlags();
						am.setFlag(mmgr.getFlagString(flags));
						am.setStatus(mmgr.getStatusString(flags, false, false));
						am.setTags(mmgr.flagsToTagsIds(flags,tagsMap));

						ArrayList<ApiAttachment> attachments = new ArrayList<>();
						if (mmgr.hasAttachments(msg)) attachments.add(new ApiAttachment());
						am.setAttachments(attachments);
					}
					items.add(am);
				}
			});
			return respOk(items);
			
		} catch(Exception ex) {
			logger.error("[{}] getMessages()", RunContext.getRunProfileId(), ex);
			return respError(ex);
		}
	}

	@Override
	public Response getMessage(String folderId, String suid) {
		try {
			ApiMessage am = new ApiMessage();
			UserProfileId targetPid = RunContext.getRunProfileId();
			MailManager mmgr = MailRestApiUtils.getMailManager(targetPid);
			Map<String, Tag> tagsMap = WT.getCoreManager().listTags();
			long uid = Long.parseLong(suid);
			mmgr.consumeMessage(folderId, uid, new MailManager.MessageConsumer() {
				@Override
				public void consume(Message msg, long uid, MimeMessageParser.ParsedMimeMessageComponents parsed) throws MessagingException, IOException {
					IMAPMessage mmsg = (IMAPMessage) msg;
					String hmid[] = mmsg.getHeader("Message-ID");
					if (hmid!=null && hmid.length>0) {
						am.setId(hmid[0]);
						am.setUid((int)uid);
						am.setSubject(mmsg.getSubject());
						
						ArrayList<MimeMessageParser.ParsedMimeMessageComponents.HTMLPart> htmlparts = parsed.getProcessedHTMLParts();
						String html = "<html><body></body><html>";
						if (htmlparts.size()>0) html = htmlparts.get(0).html;
						am.setBody(html);

						//sender
						Address addrs[] = mmsg.getFrom();
						ApiContact ac = new ApiContact();
						if (addrs!=null & addrs.length>0) {
							InternetAddress iaddr = (InternetAddress) addrs[0];
							ac.setName(iaddr.getPersonal());
							ac.setEmail(iaddr.getAddress());
						}
						am.setSender(ac);

						//recipients
						addrs = mmsg.getRecipients(Message.RecipientType.TO);
						if (addrs!=null) {
							for(Address addr: addrs) {
								InternetAddress iaddr = (InternetAddress) addr;
								ac = new ApiContact();
								ac.setName(iaddr.getPersonal());
								ac.setEmail(iaddr.getAddress());
								am.addRecipientsItem(ac);
							}
						}
						else am.setRecipients(new ArrayList<ApiContact>());

						//ccs
						addrs = mmsg.getRecipients(Message.RecipientType.CC);
						if (addrs!=null) {
							for(Address addr: addrs) {
								InternetAddress iaddr = (InternetAddress) addr;
								ac = new ApiContact();
								ac.setName(iaddr.getPersonal());
								ac.setEmail(iaddr.getAddress());
								am.addCcItem(ac);
							}
						}
						else am.setCc(new ArrayList<ApiContact>());

						//bccs
						addrs = mmsg.getRecipients(Message.RecipientType.BCC);
						if (addrs!=null) {
							for(Address addr: addrs) {
								InternetAddress iaddr = (InternetAddress) addr;
								ac = new ApiContact();
								ac.setName(iaddr.getPersonal());
								ac.setEmail(iaddr.getAddress());
								am.addBccItem(ac);
							}
						}
						else am.setBcc(new ArrayList<ApiContact>());

						Calendar cal = Calendar.getInstance();
						Date date = mmsg.getReceivedDate();
						if (date == null) date = new Date();
						cal.setTime(date);
						am.setDate(
								cal.get(Calendar.YEAR)+"-"+
								StringUtils.leftPad(""+(cal.get(Calendar.MONTH)+1), 2, '0')+"-"+
								StringUtils.leftPad(""+cal.get(Calendar.DAY_OF_MONTH), 2, '0')+" "+
								StringUtils.leftPad(""+cal.get(Calendar.HOUR_OF_DAY), 2, '0')+":"+
								StringUtils.leftPad(""+cal.get(Calendar.MINUTE), 2, '0'));

						am.setIsRead(mmsg.isSet(Flags.Flag.SEEN));

						Flags flags = mmsg.getFlags();
						am.setFlag(mmgr.getFlagString(flags));
						am.setStatus(mmgr.getStatusString(flags, false, false));
						am.setTags(mmgr.flagsToTagsIds(flags,tagsMap));

						ArrayList<Part> parts = parsed.getAttachmentParts();
						ArrayList<ApiAttachment> attachments = new ArrayList<ApiAttachment>();
						int ix=0;
						for(Part part: parts) {
							ApiAttachment attachment = new ApiAttachment();
							attachment.setFileName(mmgr.getPartName(part));
							attachment.setCidName(mmgr.getCidName(part));
							attachment.setId(""+ix);
							attachment.setMimeType(part.getContentType());
							int size = part.getSize();
							int lines = (size / 76);
							int rsize = size - (lines * 2);//(p.getSize()/4)*3;
							attachment.setFileSize(rsize);
							attachments.add(attachment);
							++ix;
						}
						am.setAttachments(attachments);
					}
				}
			});
			return respOk(am);
			
		} catch(Exception ex) {
			logger.error("[{}] getMessage()", RunContext.getRunProfileId(), ex);
			return respError(ex);
		}
	}

	@Override
	public Response getMessageAttachmentBytes(String folderId, String suid, String sindex) {
		try {
			ApiMessage am = new ApiMessage();
			UserProfileId targetPid = RunContext.getRunProfileId();
			MailManager mmgr = MailRestApiUtils.getMailManager(targetPid);
			long uid = Long.parseLong(suid);
			int index = Integer.parseInt(sindex);
			MimeMessageParser.ParsedMimeMessageComponents parsed = mmgr.getParsedMimeMessageComponents(folderId, uid);
			String contentType = parsed.getContentType(index);
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					mmgr.streamMessageAttachmentData(parsed, index, os);
				}
			};

			return respOk(stream, contentType);
			
		} catch(Exception ex) {
			logger.error("[{}] getMessageAttachmentBytes()", RunContext.getRunProfileId(), ex);
			return respError(ex);
		}
	}
	
	@Override
	public Response getMessageCidBytes(String folderId, String suid, String cidName) {
		try {
			ApiMessage am = new ApiMessage();
			UserProfileId targetPid = RunContext.getRunProfileId();
			MailManager mmgr = MailRestApiUtils.getMailManager(targetPid);
			long uid = Long.parseLong(suid);
			MimeMessageParser.ParsedMimeMessageComponents parsed = mmgr.getParsedMimeMessageComponents(folderId, uid);
			String contentType = parsed.getContentType(cidName);
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					mmgr.streamMessageCidData(parsed, cidName, os);
				}
			};

			return respOk(stream, contentType);
			
		} catch(Exception ex) {
			logger.error("[{}] getMessageAttachmentBytes()", RunContext.getRunProfileId(), ex);
			return respError(ex);
		}
	}

	@Override
	public Response sendMessage(ApiMessageNew amn) {
		UserProfileId targetPid = RunContext.getRunProfileId();
		MailManager mmgr = MailRestApiUtils.getMailManager(targetPid);
		
		ApiContact sender = amn.getSender();
		
		//from
		EmailPopulatingBuilder epb = EmailMessageBuilder.startingBlank()
				.from(sender.getName(), sender.getEmail());
		
		//tos
		List<ApiContact> rcpts = amn.getRecipients();
		if (rcpts != null)
			for (ApiContact rcpt: rcpts)
				epb.to(rcpt.getName(), rcpt.getEmail());
		
		//ccs
		rcpts = amn.getCc();
		if (rcpts != null)
			for (ApiContact rcpt: rcpts)
				epb.cc(rcpt.getName(), rcpt.getEmail());

		//bccs
		rcpts = amn.getBcc();
		if (rcpts != null)
			for (ApiContact rcpt: rcpts)
				epb.bcc(rcpt.getName(), rcpt.getEmail());
		
		epb.withSubject(amn.getSubject());
		
		if (amn.getFormat().equals("html")) epb.withHTMLText(amn.getBody());
		else epb.withPlainText(amn.getBody());
		
		List<ApiAttachmentNew> atts = amn.getAttachmentsNew();
		if (atts!=null) {
			for(ApiAttachmentNew att: atts) {
				epb.withAttachment(java.util.Base64.getDecoder().decode(att.getBase64()), att.getMimeType(), att.getFileName());
			}
		}

		try {
			mmgr.sendMessage(targetPid, epb, mmgr.findIdentity(amn.getIdentityId()));
			return respOk();
		} catch(Exception exc) {
			logger.error("Error during sendMessage", exc);
			return respError(exc);
		}
	}

	@Override
	public Response getReplyMessage(String folderId, String suid, Boolean replyAll, Boolean includeAttachments) {
		UserProfileId targetPid = RunContext.getRunProfileId();
		MailManager mmgr = MailRestApiUtils.getMailManager(targetPid);
		long uid = Long.parseLong(suid);
		try {
			EmailMessage msg = mmgr.getReplyMessage(folderId, uid, replyAll, false, true, true, true);
			ArrayList<ApiContact> tos = new ArrayList<>();
			ArrayList<ApiContact> ccs = new ArrayList<>();
			ArrayList<ApiContact> bccs = new ArrayList<>();
			for(Recipient rcpt: msg.getRecipients()) {
				InternetAddress ia = InternetAddressUtils.toInternetAddress(rcpt.toString());
				ApiContact ac = new ApiContact();
				ac.setName(rcpt.getName());
				ac.setEmail(rcpt.getAddress());
				if (rcpt.getType().equals(RecipientType.TO)) tos.add(ac);
				else if (rcpt.getType().equals(RecipientType.CC)) ccs.add(ac);
				else if (rcpt.getType().equals(RecipientType.BCC)) bccs.add(ac);
			}
			ApiMessageNew am = new ApiMessageNew();
			am.setRecipients(tos);
			am.setCc(ccs);
			am.setBcc(bccs);
			am.setSubject(msg.getSubject());
			am.setBody(msg.getHTMLText());
			
			List<AttachmentResource> atts = msg.getAttachments();
			if (atts != null)
				for (AttachmentResource att: atts) {
					DataSource ds = att.getDataSource();
					ApiAttachmentNew aatt = new ApiAttachmentNew();
					aatt.setBase64(java.util.Base64.getEncoder().encodeToString(att.readAllBytes()));
					aatt.setCidName(att.getCidName());
					aatt.setFileName(ds.getName());
					aatt.setMimeType(ds.getContentType());
					am.addAttachmentsNewItem(aatt);
				}
			return respOk(am);
		} catch(Exception exc) {
			logger.error("Error during sendMessage", exc);
			return respError(exc);
		}
	}

	@Override
	public Response getForwardMessage(String folderId, String uid, Boolean includeAttachments) {
		return super.getForwardMessage(folderId, uid, includeAttachments); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiApiError()
				.code(status.getStatusCode())
				.description(message);
	}

}
