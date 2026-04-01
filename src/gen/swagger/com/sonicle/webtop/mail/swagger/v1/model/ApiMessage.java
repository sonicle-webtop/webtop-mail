package com.sonicle.webtop.mail.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.mail.swagger.v1.model.ApiAttachment;
import com.sonicle.webtop.mail.swagger.v1.model.ApiContact;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("Message")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-01T08:34:42.710+02:00[Europe/Rome]")
public class ApiMessage   {
  private @Valid String id;
  private @Valid Integer uid;
  private @Valid String subject;
  private @Valid String body;
  private @Valid ApiContact sender;
  private @Valid List<ApiContact> recipients;
  private @Valid List<ApiContact> cc;
  private @Valid List<ApiContact> bcc;
  private @Valid String date;
  private @Valid Boolean isRead;
  private @Valid List<ApiAttachment> attachments;
  private @Valid String status;
  private @Valid String flag;
  private @Valid List<String> tags;

  /**
   **/
  public ApiMessage id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public ApiMessage uid(Integer uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("uid")
  public Integer getUid() {
    return uid;
  }

  @JsonProperty("uid")
  public void setUid(Integer uid) {
    this.uid = uid;
  }

  /**
   **/
  public ApiMessage subject(String subject) {
    this.subject = subject;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subject")
  public String getSubject() {
    return subject;
  }

  @JsonProperty("subject")
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   **/
  public ApiMessage body(String body) {
    this.body = body;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("body")
  public String getBody() {
    return body;
  }

  @JsonProperty("body")
  public void setBody(String body) {
    this.body = body;
  }

  /**
   **/
  public ApiMessage sender(ApiContact sender) {
    this.sender = sender;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sender")
  public ApiContact getSender() {
    return sender;
  }

  @JsonProperty("sender")
  public void setSender(ApiContact sender) {
    this.sender = sender;
  }

  /**
   **/
  public ApiMessage recipients(List<ApiContact> recipients) {
    this.recipients = recipients;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("recipients")
  public List<ApiContact> getRecipients() {
    return recipients;
  }

  @JsonProperty("recipients")
  public void setRecipients(List<ApiContact> recipients) {
    this.recipients = recipients;
  }

  public ApiMessage addRecipientsItem(ApiContact recipientsItem) {
    if (this.recipients == null) {
      this.recipients = new ArrayList<>();
    }

    this.recipients.add(recipientsItem);
    return this;
  }

  public ApiMessage removeRecipientsItem(ApiContact recipientsItem) {
    if (recipientsItem != null && this.recipients != null) {
      this.recipients.remove(recipientsItem);
    }

    return this;
  }
  /**
   **/
  public ApiMessage cc(List<ApiContact> cc) {
    this.cc = cc;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("cc")
  public List<ApiContact> getCc() {
    return cc;
  }

  @JsonProperty("cc")
  public void setCc(List<ApiContact> cc) {
    this.cc = cc;
  }

  public ApiMessage addCcItem(ApiContact ccItem) {
    if (this.cc == null) {
      this.cc = new ArrayList<>();
    }

    this.cc.add(ccItem);
    return this;
  }

  public ApiMessage removeCcItem(ApiContact ccItem) {
    if (ccItem != null && this.cc != null) {
      this.cc.remove(ccItem);
    }

    return this;
  }
  /**
   **/
  public ApiMessage bcc(List<ApiContact> bcc) {
    this.bcc = bcc;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("bcc")
  public List<ApiContact> getBcc() {
    return bcc;
  }

  @JsonProperty("bcc")
  public void setBcc(List<ApiContact> bcc) {
    this.bcc = bcc;
  }

  public ApiMessage addBccItem(ApiContact bccItem) {
    if (this.bcc == null) {
      this.bcc = new ArrayList<>();
    }

    this.bcc.add(bccItem);
    return this;
  }

  public ApiMessage removeBccItem(ApiContact bccItem) {
    if (bccItem != null && this.bcc != null) {
      this.bcc.remove(bccItem);
    }

    return this;
  }
  /**
   **/
  public ApiMessage date(String date) {
    this.date = date;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("date")
  public String getDate() {
    return date;
  }

  @JsonProperty("date")
  public void setDate(String date) {
    this.date = date;
  }

  /**
   **/
  public ApiMessage isRead(Boolean isRead) {
    this.isRead = isRead;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("isRead")
  public Boolean getIsRead() {
    return isRead;
  }

  @JsonProperty("isRead")
  public void setIsRead(Boolean isRead) {
    this.isRead = isRead;
  }

  /**
   **/
  public ApiMessage attachments(List<ApiAttachment> attachments) {
    this.attachments = attachments;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("attachments")
  public List<ApiAttachment> getAttachments() {
    return attachments;
  }

  @JsonProperty("attachments")
  public void setAttachments(List<ApiAttachment> attachments) {
    this.attachments = attachments;
  }

  public ApiMessage addAttachmentsItem(ApiAttachment attachmentsItem) {
    if (this.attachments == null) {
      this.attachments = new ArrayList<>();
    }

    this.attachments.add(attachmentsItem);
    return this;
  }

  public ApiMessage removeAttachmentsItem(ApiAttachment attachmentsItem) {
    if (attachmentsItem != null && this.attachments != null) {
      this.attachments.remove(attachmentsItem);
    }

    return this;
  }
  /**
   **/
  public ApiMessage status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public ApiMessage flag(String flag) {
    this.flag = flag;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("flag")
  public String getFlag() {
    return flag;
  }

  @JsonProperty("flag")
  public void setFlag(String flag) {
    this.flag = flag;
  }

  /**
   **/
  public ApiMessage tags(List<String> tags) {
    this.tags = tags;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }

  @JsonProperty("tags")
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public ApiMessage addTagsItem(String tagsItem) {
    if (this.tags == null) {
      this.tags = new ArrayList<>();
    }

    this.tags.add(tagsItem);
    return this;
  }

  public ApiMessage removeTagsItem(String tagsItem) {
    if (tagsItem != null && this.tags != null) {
      this.tags.remove(tagsItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiMessage message = (ApiMessage) o;
    return Objects.equals(this.id, message.id) &&
        Objects.equals(this.uid, message.uid) &&
        Objects.equals(this.subject, message.subject) &&
        Objects.equals(this.body, message.body) &&
        Objects.equals(this.sender, message.sender) &&
        Objects.equals(this.recipients, message.recipients) &&
        Objects.equals(this.cc, message.cc) &&
        Objects.equals(this.bcc, message.bcc) &&
        Objects.equals(this.date, message.date) &&
        Objects.equals(this.isRead, message.isRead) &&
        Objects.equals(this.attachments, message.attachments) &&
        Objects.equals(this.status, message.status) &&
        Objects.equals(this.flag, message.flag) &&
        Objects.equals(this.tags, message.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uid, subject, body, sender, recipients, cc, bcc, date, isRead, attachments, status, flag, tags);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiMessage {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    subject: ").append(toIndentedString(subject)).append("\n");
    sb.append("    body: ").append(toIndentedString(body)).append("\n");
    sb.append("    sender: ").append(toIndentedString(sender)).append("\n");
    sb.append("    recipients: ").append(toIndentedString(recipients)).append("\n");
    sb.append("    cc: ").append(toIndentedString(cc)).append("\n");
    sb.append("    bcc: ").append(toIndentedString(bcc)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    isRead: ").append(toIndentedString(isRead)).append("\n");
    sb.append("    attachments: ").append(toIndentedString(attachments)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    flag: ").append(toIndentedString(flag)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}

