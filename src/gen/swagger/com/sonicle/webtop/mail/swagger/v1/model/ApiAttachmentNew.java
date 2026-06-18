package com.sonicle.webtop.mail.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("AttachmentNew")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-18T16:00:13.341+02:00[Europe/Rome]")
public class ApiAttachmentNew   {
  private @Valid String fileName;
  private @Valid String mimeType;
  private @Valid String cidName;
  private @Valid String base64;
  private @Valid String referenceFolder;
  private @Valid String referenceUid;
  private @Valid String referenceIndex;
  private @Valid String referenceCidName;

  /**
   **/
  public ApiAttachmentNew fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("fileName")
  public String getFileName() {
    return fileName;
  }

  @JsonProperty("fileName")
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   **/
  public ApiAttachmentNew mimeType(String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("mimeType")
  public String getMimeType() {
    return mimeType;
  }

  @JsonProperty("mimeType")
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   **/
  public ApiAttachmentNew cidName(String cidName) {
    this.cidName = cidName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("cidName")
  public String getCidName() {
    return cidName;
  }

  @JsonProperty("cidName")
  public void setCidName(String cidName) {
    this.cidName = cidName;
  }

  /**
   **/
  public ApiAttachmentNew base64(String base64) {
    this.base64 = base64;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("base64")
  public String getBase64() {
    return base64;
  }

  @JsonProperty("base64")
  public void setBase64(String base64) {
    this.base64 = base64;
  }

  /**
   **/
  public ApiAttachmentNew referenceFolder(String referenceFolder) {
    this.referenceFolder = referenceFolder;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("referenceFolder")
  public String getReferenceFolder() {
    return referenceFolder;
  }

  @JsonProperty("referenceFolder")
  public void setReferenceFolder(String referenceFolder) {
    this.referenceFolder = referenceFolder;
  }

  /**
   **/
  public ApiAttachmentNew referenceUid(String referenceUid) {
    this.referenceUid = referenceUid;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("referenceUid")
  public String getReferenceUid() {
    return referenceUid;
  }

  @JsonProperty("referenceUid")
  public void setReferenceUid(String referenceUid) {
    this.referenceUid = referenceUid;
  }

  /**
   **/
  public ApiAttachmentNew referenceIndex(String referenceIndex) {
    this.referenceIndex = referenceIndex;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("referenceIndex")
  public String getReferenceIndex() {
    return referenceIndex;
  }

  @JsonProperty("referenceIndex")
  public void setReferenceIndex(String referenceIndex) {
    this.referenceIndex = referenceIndex;
  }

  /**
   **/
  public ApiAttachmentNew referenceCidName(String referenceCidName) {
    this.referenceCidName = referenceCidName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("referenceCidName")
  public String getReferenceCidName() {
    return referenceCidName;
  }

  @JsonProperty("referenceCidName")
  public void setReferenceCidName(String referenceCidName) {
    this.referenceCidName = referenceCidName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiAttachmentNew attachmentNew = (ApiAttachmentNew) o;
    return Objects.equals(this.fileName, attachmentNew.fileName) &&
        Objects.equals(this.mimeType, attachmentNew.mimeType) &&
        Objects.equals(this.cidName, attachmentNew.cidName) &&
        Objects.equals(this.base64, attachmentNew.base64) &&
        Objects.equals(this.referenceFolder, attachmentNew.referenceFolder) &&
        Objects.equals(this.referenceUid, attachmentNew.referenceUid) &&
        Objects.equals(this.referenceIndex, attachmentNew.referenceIndex) &&
        Objects.equals(this.referenceCidName, attachmentNew.referenceCidName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fileName, mimeType, cidName, base64, referenceFolder, referenceUid, referenceIndex, referenceCidName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAttachmentNew {\n");
    
    sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
    sb.append("    mimeType: ").append(toIndentedString(mimeType)).append("\n");
    sb.append("    cidName: ").append(toIndentedString(cidName)).append("\n");
    sb.append("    base64: ").append(toIndentedString(base64)).append("\n");
    sb.append("    referenceFolder: ").append(toIndentedString(referenceFolder)).append("\n");
    sb.append("    referenceUid: ").append(toIndentedString(referenceUid)).append("\n");
    sb.append("    referenceIndex: ").append(toIndentedString(referenceIndex)).append("\n");
    sb.append("    referenceCidName: ").append(toIndentedString(referenceCidName)).append("\n");
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

