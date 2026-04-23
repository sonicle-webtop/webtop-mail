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



@JsonTypeName("Account")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-15T14:28:08.246+02:00[Europe/Rome]")
public class ApiAccount   {
  private @Valid String userId;
  private @Valid String displayName;
  private @Valid String mailUsername;

  /**
   **/
  public ApiAccount userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   **/
  public ApiAccount displayName(String displayName) {
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
  public ApiAccount mailUsername(String mailUsername) {
    this.mailUsername = mailUsername;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("mailUsername")
  public String getMailUsername() {
    return mailUsername;
  }

  @JsonProperty("mailUsername")
  public void setMailUsername(String mailUsername) {
    this.mailUsername = mailUsername;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiAccount account = (ApiAccount) o;
    return Objects.equals(this.userId, account.userId) &&
        Objects.equals(this.displayName, account.displayName) &&
        Objects.equals(this.mailUsername, account.mailUsername);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, displayName, mailUsername);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAccount {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    mailUsername: ").append(toIndentedString(mailUsername)).append("\n");
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

