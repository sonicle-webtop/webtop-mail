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



@JsonTypeName("Identity")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-15T10:50:05.077+02:00[Europe/Rome]")
public class ApiIdentity   {
  private @Valid Integer id;
  private @Valid String uid;
  private @Valid String type;
  private @Valid String email;
  private @Valid String displayName;
  private @Valid String mainFolder;
  private @Valid String mailcard;
  private @Valid Boolean fax;
  private @Valid Boolean forceMailcard;
  private @Valid Boolean lockMailcard;
  private @Valid Boolean isMain;

  /**
   **/
  public ApiIdentity id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   **/
  public ApiIdentity uid(String uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("uid")
  public String getUid() {
    return uid;
  }

  @JsonProperty("uid")
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   **/
  public ApiIdentity type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public ApiIdentity email(String email) {
    this.email = email;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   **/
  public ApiIdentity displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public ApiIdentity mainFolder(String mainFolder) {
    this.mainFolder = mainFolder;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("mainFolder")
  public String getMainFolder() {
    return mainFolder;
  }

  @JsonProperty("mainFolder")
  public void setMainFolder(String mainFolder) {
    this.mainFolder = mainFolder;
  }

  /**
   **/
  public ApiIdentity mailcard(String mailcard) {
    this.mailcard = mailcard;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("mailcard")
  public String getMailcard() {
    return mailcard;
  }

  @JsonProperty("mailcard")
  public void setMailcard(String mailcard) {
    this.mailcard = mailcard;
  }

  /**
   **/
  public ApiIdentity fax(Boolean fax) {
    this.fax = fax;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("fax")
  public Boolean getFax() {
    return fax;
  }

  @JsonProperty("fax")
  public void setFax(Boolean fax) {
    this.fax = fax;
  }

  /**
   **/
  public ApiIdentity forceMailcard(Boolean forceMailcard) {
    this.forceMailcard = forceMailcard;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("forceMailcard")
  public Boolean getForceMailcard() {
    return forceMailcard;
  }

  @JsonProperty("forceMailcard")
  public void setForceMailcard(Boolean forceMailcard) {
    this.forceMailcard = forceMailcard;
  }

  /**
   **/
  public ApiIdentity lockMailcard(Boolean lockMailcard) {
    this.lockMailcard = lockMailcard;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lockMailcard")
  public Boolean getLockMailcard() {
    return lockMailcard;
  }

  @JsonProperty("lockMailcard")
  public void setLockMailcard(Boolean lockMailcard) {
    this.lockMailcard = lockMailcard;
  }

  /**
   **/
  public ApiIdentity isMain(Boolean isMain) {
    this.isMain = isMain;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("isMain")
  public Boolean getIsMain() {
    return isMain;
  }

  @JsonProperty("isMain")
  public void setIsMain(Boolean isMain) {
    this.isMain = isMain;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiIdentity identity = (ApiIdentity) o;
    return Objects.equals(this.id, identity.id) &&
        Objects.equals(this.uid, identity.uid) &&
        Objects.equals(this.type, identity.type) &&
        Objects.equals(this.email, identity.email) &&
        Objects.equals(this.displayName, identity.displayName) &&
        Objects.equals(this.mainFolder, identity.mainFolder) &&
        Objects.equals(this.mailcard, identity.mailcard) &&
        Objects.equals(this.fax, identity.fax) &&
        Objects.equals(this.forceMailcard, identity.forceMailcard) &&
        Objects.equals(this.lockMailcard, identity.lockMailcard) &&
        Objects.equals(this.isMain, identity.isMain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uid, type, email, displayName, mainFolder, mailcard, fax, forceMailcard, lockMailcard, isMain);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiIdentity {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    mainFolder: ").append(toIndentedString(mainFolder)).append("\n");
    sb.append("    mailcard: ").append(toIndentedString(mailcard)).append("\n");
    sb.append("    fax: ").append(toIndentedString(fax)).append("\n");
    sb.append("    forceMailcard: ").append(toIndentedString(forceMailcard)).append("\n");
    sb.append("    lockMailcard: ").append(toIndentedString(lockMailcard)).append("\n");
    sb.append("    isMain: ").append(toIndentedString(isMain)).append("\n");
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

