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



@JsonTypeName("Attachment")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-04T17:12:25.224+02:00[Europe/Rome]")
public class ApiAttachment   {
  private @Valid String id;
  private @Valid String fileName;
  private @Valid Integer fileSize;
  private @Valid String mimeType;
  private @Valid String downloadUrl;
  private @Valid String cidName;

  /**
   **/
  public ApiAttachment id(String id) {
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
  public ApiAttachment fileName(String fileName) {
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
  public ApiAttachment fileSize(Integer fileSize) {
    this.fileSize = fileSize;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("fileSize")
  public Integer getFileSize() {
    return fileSize;
  }

  @JsonProperty("fileSize")
  public void setFileSize(Integer fileSize) {
    this.fileSize = fileSize;
  }

  /**
   **/
  public ApiAttachment mimeType(String mimeType) {
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
  public ApiAttachment downloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("downloadUrl")
  public String getDownloadUrl() {
    return downloadUrl;
  }

  @JsonProperty("downloadUrl")
  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  /**
   **/
  public ApiAttachment cidName(String cidName) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiAttachment attachment = (ApiAttachment) o;
    return Objects.equals(this.id, attachment.id) &&
        Objects.equals(this.fileName, attachment.fileName) &&
        Objects.equals(this.fileSize, attachment.fileSize) &&
        Objects.equals(this.mimeType, attachment.mimeType) &&
        Objects.equals(this.downloadUrl, attachment.downloadUrl) &&
        Objects.equals(this.cidName, attachment.cidName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, fileName, fileSize, mimeType, downloadUrl, cidName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAttachment {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
    sb.append("    fileSize: ").append(toIndentedString(fileSize)).append("\n");
    sb.append("    mimeType: ").append(toIndentedString(mimeType)).append("\n");
    sb.append("    downloadUrl: ").append(toIndentedString(downloadUrl)).append("\n");
    sb.append("    cidName: ").append(toIndentedString(cidName)).append("\n");
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

