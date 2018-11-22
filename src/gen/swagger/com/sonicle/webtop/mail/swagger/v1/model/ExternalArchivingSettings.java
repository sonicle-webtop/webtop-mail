package com.sonicle.webtop.mail.swagger.v1.model;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ExternalArchivingSettings   {
  
  private @Valid Boolean enabled = null;
  private @Valid String type = null;
  private @Valid String host = null;
  private @Valid Integer port = null;
  private @Valid String protocol = null;
  private @Valid String username = null;
  private @Valid String password = null;
  private @Valid String folderPrefix = null;
  private @Valid Integer minage = null;

  /**
   * Enabled status
   **/
  public ExternalArchivingSettings enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Enabled status")
  @JsonProperty("enabled")
  @NotNull
  public Boolean isEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Archiving type
   **/
  public ExternalArchivingSettings type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Archiving type")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * IMAP server host
   **/
  public ExternalArchivingSettings host(String host) {
    this.host = host;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "IMAP server host")
  @JsonProperty("host")
  @NotNull
  public String getHost() {
    return host;
  }
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * IMAP server port
   **/
  public ExternalArchivingSettings port(Integer port) {
    this.port = port;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "IMAP server port")
  @JsonProperty("port")
  @NotNull
  public Integer getPort() {
    return port;
  }
  public void setPort(Integer port) {
    this.port = port;
  }

  /**
   * IMAP server protocol
   **/
  public ExternalArchivingSettings protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "IMAP server protocol")
  @JsonProperty("protocol")
  @NotNull
  public String getProtocol() {
    return protocol;
  }
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * Account username
   **/
  public ExternalArchivingSettings username(String username) {
    this.username = username;
    return this;
  }

  
  @ApiModelProperty(value = "Account username")
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Account password
   **/
  public ExternalArchivingSettings password(String password) {
    this.password = password;
    return this;
  }

  
  @ApiModelProperty(value = "Account password")
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * IMAP folder prefix
   **/
  public ExternalArchivingSettings folderPrefix(String folderPrefix) {
    this.folderPrefix = folderPrefix;
    return this;
  }

  
  @ApiModelProperty(value = "IMAP folder prefix")
  @JsonProperty("folderPrefix")
  public String getFolderPrefix() {
    return folderPrefix;
  }
  public void setFolderPrefix(String folderPrefix) {
    this.folderPrefix = folderPrefix;
  }

  /**
   * Archive messages older than days
   **/
  public ExternalArchivingSettings minage(Integer minage) {
    this.minage = minage;
    return this;
  }

  
  @ApiModelProperty(value = "Archive messages older than days")
  @JsonProperty("minage")
  public Integer getMinage() {
    return minage;
  }
  public void setMinage(Integer minage) {
    this.minage = minage;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExternalArchivingSettings externalArchivingSettings = (ExternalArchivingSettings) o;
    return Objects.equals(enabled, externalArchivingSettings.enabled) &&
        Objects.equals(type, externalArchivingSettings.type) &&
        Objects.equals(host, externalArchivingSettings.host) &&
        Objects.equals(port, externalArchivingSettings.port) &&
        Objects.equals(protocol, externalArchivingSettings.protocol) &&
        Objects.equals(username, externalArchivingSettings.username) &&
        Objects.equals(password, externalArchivingSettings.password) &&
        Objects.equals(folderPrefix, externalArchivingSettings.folderPrefix) &&
        Objects.equals(minage, externalArchivingSettings.minage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, type, host, port, protocol, username, password, folderPrefix, minage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExternalArchivingSettings {\n");
    
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    host: ").append(toIndentedString(host)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    folderPrefix: ").append(toIndentedString(folderPrefix)).append("\n");
    sb.append("    minage: ").append(toIndentedString(minage)).append("\n");
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

