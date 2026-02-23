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



@JsonTypeName("ExternalArchivingSettings")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-03T16:31:33.680+02:00[Europe/Berlin]")
public class ApiExternalArchivingSettings   {
  private @Valid Boolean enabled;
  private @Valid String type;
  private @Valid String host;
  private @Valid Integer port;
  private @Valid String protocol;
  private @Valid String username;
  private @Valid String password;
  private @Valid String folderPrefix;
  private @Valid Integer minage;

  /**
   * Enabled status
   **/
  public ApiExternalArchivingSettings enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Enabled status")
  @JsonProperty("enabled")
  @NotNull
  public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty("enabled")
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Archiving type
   **/
  public ApiExternalArchivingSettings type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Archiving type")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  /**
   * IMAP server host
   **/
  public ApiExternalArchivingSettings host(String host) {
    this.host = host;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "IMAP server host")
  @JsonProperty("host")
  @NotNull
  public String getHost() {
    return host;
  }

  @JsonProperty("host")
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * IMAP server port
   **/
  public ApiExternalArchivingSettings port(Integer port) {
    this.port = port;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "IMAP server port")
  @JsonProperty("port")
  @NotNull
  public Integer getPort() {
    return port;
  }

  @JsonProperty("port")
  public void setPort(Integer port) {
    this.port = port;
  }

  /**
   * IMAP server protocol
   **/
  public ApiExternalArchivingSettings protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "IMAP server protocol")
  @JsonProperty("protocol")
  @NotNull
  public String getProtocol() {
    return protocol;
  }

  @JsonProperty("protocol")
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * Account username
   **/
  public ApiExternalArchivingSettings username(String username) {
    this.username = username;
    return this;
  }

  
  @ApiModelProperty(value = "Account username")
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  @JsonProperty("username")
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Account password
   **/
  public ApiExternalArchivingSettings password(String password) {
    this.password = password;
    return this;
  }

  
  @ApiModelProperty(value = "Account password")
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }

  @JsonProperty("password")
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * IMAP folder prefix
   **/
  public ApiExternalArchivingSettings folderPrefix(String folderPrefix) {
    this.folderPrefix = folderPrefix;
    return this;
  }

  
  @ApiModelProperty(value = "IMAP folder prefix")
  @JsonProperty("folderPrefix")
  public String getFolderPrefix() {
    return folderPrefix;
  }

  @JsonProperty("folderPrefix")
  public void setFolderPrefix(String folderPrefix) {
    this.folderPrefix = folderPrefix;
  }

  /**
   * Archive messages older than days
   **/
  public ApiExternalArchivingSettings minage(Integer minage) {
    this.minage = minage;
    return this;
  }

  
  @ApiModelProperty(value = "Archive messages older than days")
  @JsonProperty("minage")
  public Integer getMinage() {
    return minage;
  }

  @JsonProperty("minage")
  public void setMinage(Integer minage) {
    this.minage = minage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiExternalArchivingSettings externalArchivingSettings = (ApiExternalArchivingSettings) o;
    return Objects.equals(this.enabled, externalArchivingSettings.enabled) &&
        Objects.equals(this.type, externalArchivingSettings.type) &&
        Objects.equals(this.host, externalArchivingSettings.host) &&
        Objects.equals(this.port, externalArchivingSettings.port) &&
        Objects.equals(this.protocol, externalArchivingSettings.protocol) &&
        Objects.equals(this.username, externalArchivingSettings.username) &&
        Objects.equals(this.password, externalArchivingSettings.password) &&
        Objects.equals(this.folderPrefix, externalArchivingSettings.folderPrefix) &&
        Objects.equals(this.minage, externalArchivingSettings.minage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, type, host, port, protocol, username, password, folderPrefix, minage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiExternalArchivingSettings {\n");
    
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
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}

