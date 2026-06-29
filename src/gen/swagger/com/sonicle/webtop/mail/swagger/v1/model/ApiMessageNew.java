package com.sonicle.webtop.mail.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.mail.swagger.v1.model.ApiAttachment;
import com.sonicle.webtop.mail.swagger.v1.model.ApiAttachmentNew;
import com.sonicle.webtop.mail.swagger.v1.model.ApiCalendarPart;
import com.sonicle.webtop.mail.swagger.v1.model.ApiContact;
import com.sonicle.webtop.mail.swagger.v1.model.ApiMessage;
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



@JsonTypeName("MessageNew")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-29T14:51:59.285+02:00[Europe/Rome]")
public class ApiMessageNew extends ApiMessage  {
  private @Valid String folderId;
  private @Valid Boolean receipt;
  private @Valid Boolean priority;
  private @Valid String format;
  private @Valid Integer identityId;
  private @Valid String identityMainFolder;
  private @Valid String replyFolder;
  private @Valid String inReplyTo;
  private @Valid String references;
  private @Valid String forwardedFolder;
  private @Valid String forwardedFrom;
  private @Valid String origuUd;
  private @Valid String draftUid;
  private @Valid String draftFolder;
  private @Valid Boolean deleted;
  private @Valid List<ApiAttachmentNew> attachmentsNew;

  /**
   **/
  public ApiMessageNew folderId(String folderId) {
    this.folderId = folderId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("folderId")
  public String getFolderId() {
    return folderId;
  }

  @JsonProperty("folderId")
  public void setFolderId(String folderId) {
    this.folderId = folderId;
  }

  /**
   **/
  public ApiMessageNew receipt(Boolean receipt) {
    this.receipt = receipt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("receipt")
  public Boolean getReceipt() {
    return receipt;
  }

  @JsonProperty("receipt")
  public void setReceipt(Boolean receipt) {
    this.receipt = receipt;
  }

  /**
   **/
  public ApiMessageNew priority(Boolean priority) {
    this.priority = priority;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("priority")
  public Boolean getPriority() {
    return priority;
  }

  @JsonProperty("priority")
  public void setPriority(Boolean priority) {
    this.priority = priority;
  }

  /**
   **/
  public ApiMessageNew format(String format) {
    this.format = format;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("format")
  public String getFormat() {
    return format;
  }

  @JsonProperty("format")
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   **/
  public ApiMessageNew identityId(Integer identityId) {
    this.identityId = identityId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("identityId")
  public Integer getIdentityId() {
    return identityId;
  }

  @JsonProperty("identityId")
  public void setIdentityId(Integer identityId) {
    this.identityId = identityId;
  }

  /**
   **/
  public ApiMessageNew identityMainFolder(String identityMainFolder) {
    this.identityMainFolder = identityMainFolder;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("identityMainFolder")
  public String getIdentityMainFolder() {
    return identityMainFolder;
  }

  @JsonProperty("identityMainFolder")
  public void setIdentityMainFolder(String identityMainFolder) {
    this.identityMainFolder = identityMainFolder;
  }

  /**
   **/
  public ApiMessageNew replyFolder(String replyFolder) {
    this.replyFolder = replyFolder;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("replyFolder")
  public String getReplyFolder() {
    return replyFolder;
  }

  @JsonProperty("replyFolder")
  public void setReplyFolder(String replyFolder) {
    this.replyFolder = replyFolder;
  }

  /**
   **/
  public ApiMessageNew inReplyTo(String inReplyTo) {
    this.inReplyTo = inReplyTo;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("inReplyTo")
  public String getInReplyTo() {
    return inReplyTo;
  }

  @JsonProperty("inReplyTo")
  public void setInReplyTo(String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  /**
   **/
  public ApiMessageNew references(String references) {
    this.references = references;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("references")
  public String getReferences() {
    return references;
  }

  @JsonProperty("references")
  public void setReferences(String references) {
    this.references = references;
  }

  /**
   **/
  public ApiMessageNew forwardedFolder(String forwardedFolder) {
    this.forwardedFolder = forwardedFolder;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("forwardedFolder")
  public String getForwardedFolder() {
    return forwardedFolder;
  }

  @JsonProperty("forwardedFolder")
  public void setForwardedFolder(String forwardedFolder) {
    this.forwardedFolder = forwardedFolder;
  }

  /**
   **/
  public ApiMessageNew forwardedFrom(String forwardedFrom) {
    this.forwardedFrom = forwardedFrom;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("forwardedFrom")
  public String getForwardedFrom() {
    return forwardedFrom;
  }

  @JsonProperty("forwardedFrom")
  public void setForwardedFrom(String forwardedFrom) {
    this.forwardedFrom = forwardedFrom;
  }

  /**
   **/
  public ApiMessageNew origuUd(String origuUd) {
    this.origuUd = origuUd;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("origuUd")
  public String getOriguUd() {
    return origuUd;
  }

  @JsonProperty("origuUd")
  public void setOriguUd(String origuUd) {
    this.origuUd = origuUd;
  }

  /**
   **/
  public ApiMessageNew draftUid(String draftUid) {
    this.draftUid = draftUid;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("draftUid")
  public String getDraftUid() {
    return draftUid;
  }

  @JsonProperty("draftUid")
  public void setDraftUid(String draftUid) {
    this.draftUid = draftUid;
  }

  /**
   **/
  public ApiMessageNew draftFolder(String draftFolder) {
    this.draftFolder = draftFolder;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("draftFolder")
  public String getDraftFolder() {
    return draftFolder;
  }

  @JsonProperty("draftFolder")
  public void setDraftFolder(String draftFolder) {
    this.draftFolder = draftFolder;
  }

  /**
   **/
  public ApiMessageNew deleted(Boolean deleted) {
    this.deleted = deleted;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("deleted")
  public Boolean getDeleted() {
    return deleted;
  }

  @JsonProperty("deleted")
  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  /**
   **/
  public ApiMessageNew attachmentsNew(List<ApiAttachmentNew> attachmentsNew) {
    this.attachmentsNew = attachmentsNew;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("attachmentsNew")
  public List<ApiAttachmentNew> getAttachmentsNew() {
    return attachmentsNew;
  }

  @JsonProperty("attachmentsNew")
  public void setAttachmentsNew(List<ApiAttachmentNew> attachmentsNew) {
    this.attachmentsNew = attachmentsNew;
  }

  public ApiMessageNew addAttachmentsNewItem(ApiAttachmentNew attachmentsNewItem) {
    if (this.attachmentsNew == null) {
      this.attachmentsNew = new ArrayList<>();
    }

    this.attachmentsNew.add(attachmentsNewItem);
    return this;
  }

  public ApiMessageNew removeAttachmentsNewItem(ApiAttachmentNew attachmentsNewItem) {
    if (attachmentsNewItem != null && this.attachmentsNew != null) {
      this.attachmentsNew.remove(attachmentsNewItem);
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
    ApiMessageNew messageNew = (ApiMessageNew) o;
    return Objects.equals(this.folderId, messageNew.folderId) &&
        Objects.equals(this.receipt, messageNew.receipt) &&
        Objects.equals(this.priority, messageNew.priority) &&
        Objects.equals(this.format, messageNew.format) &&
        Objects.equals(this.identityId, messageNew.identityId) &&
        Objects.equals(this.identityMainFolder, messageNew.identityMainFolder) &&
        Objects.equals(this.replyFolder, messageNew.replyFolder) &&
        Objects.equals(this.inReplyTo, messageNew.inReplyTo) &&
        Objects.equals(this.references, messageNew.references) &&
        Objects.equals(this.forwardedFolder, messageNew.forwardedFolder) &&
        Objects.equals(this.forwardedFrom, messageNew.forwardedFrom) &&
        Objects.equals(this.origuUd, messageNew.origuUd) &&
        Objects.equals(this.draftUid, messageNew.draftUid) &&
        Objects.equals(this.draftFolder, messageNew.draftFolder) &&
        Objects.equals(this.deleted, messageNew.deleted) &&
        Objects.equals(this.attachmentsNew, messageNew.attachmentsNew) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(folderId, receipt, priority, format, identityId, identityMainFolder, replyFolder, inReplyTo, references, forwardedFolder, forwardedFrom, origuUd, draftUid, draftFolder, deleted, attachmentsNew, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiMessageNew {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    folderId: ").append(toIndentedString(folderId)).append("\n");
    sb.append("    receipt: ").append(toIndentedString(receipt)).append("\n");
    sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
    sb.append("    format: ").append(toIndentedString(format)).append("\n");
    sb.append("    identityId: ").append(toIndentedString(identityId)).append("\n");
    sb.append("    identityMainFolder: ").append(toIndentedString(identityMainFolder)).append("\n");
    sb.append("    replyFolder: ").append(toIndentedString(replyFolder)).append("\n");
    sb.append("    inReplyTo: ").append(toIndentedString(inReplyTo)).append("\n");
    sb.append("    references: ").append(toIndentedString(references)).append("\n");
    sb.append("    forwardedFolder: ").append(toIndentedString(forwardedFolder)).append("\n");
    sb.append("    forwardedFrom: ").append(toIndentedString(forwardedFrom)).append("\n");
    sb.append("    origuUd: ").append(toIndentedString(origuUd)).append("\n");
    sb.append("    draftUid: ").append(toIndentedString(draftUid)).append("\n");
    sb.append("    draftFolder: ").append(toIndentedString(draftFolder)).append("\n");
    sb.append("    deleted: ").append(toIndentedString(deleted)).append("\n");
    sb.append("    attachmentsNew: ").append(toIndentedString(attachmentsNew)).append("\n");
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

