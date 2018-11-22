package com.sonicle.webtop.mail.swagger.v1.model;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Account   {
  
  private @Valid String userId = null;
  private @Valid String displayName = null;
  private @Valid String mailUsername = null;

  /**
   **/
  public Account userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   **/
  public Account displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public Account mailUsername(String mailUsername) {
    this.mailUsername = mailUsername;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("mailUsername")
  public String getMailUsername() {
    return mailUsername;
  }
  public void setMailUsername(String mailUsername) {
    this.mailUsername = mailUsername;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Account account = (Account) o;
    return Objects.equals(userId, account.userId) &&
        Objects.equals(displayName, account.displayName) &&
        Objects.equals(mailUsername, account.mailUsername);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, displayName, mailUsername);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Account {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

