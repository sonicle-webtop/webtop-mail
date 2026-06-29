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

/**
 * Body for POST /me/messages/calendar/apply.
 **/
@ApiModel(description = "Body for POST /me/messages/calendar/apply.")
@JsonTypeName("ItipApplyRequest")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-29T14:51:59.285+02:00[Europe/Rome]")
public class ApiItipApplyRequest   {
  private @Valid String folderId;
  private @Valid Integer uid;
  private @Valid Integer attachmentIndex;
  public enum ActionEnum {

    ACCEPT(String.valueOf("accept")), TENTATIVE(String.valueOf("tentative")), DECLINE(String.valueOf("decline")), APPLY(String.valueOf("apply")), IMPORT(String.valueOf("import")), IGNORE(String.valueOf("ignore"));


    private String value;

    ActionEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static ActionEnum fromString(String s) {
        for (ActionEnum b : ActionEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static ActionEnum fromValue(String value) {
        for (ActionEnum b : ActionEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid ActionEnum action;
  private @Valid String targetCalendarId;
  private @Valid Boolean notify = true;
  private @Valid String comment;

  /**
   **/
  public ApiItipApplyRequest folderId(String folderId) {
    this.folderId = folderId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("folderId")
  @NotNull
  public String getFolderId() {
    return folderId;
  }

  @JsonProperty("folderId")
  public void setFolderId(String folderId) {
    this.folderId = folderId;
  }

  /**
   **/
  public ApiItipApplyRequest uid(Integer uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("uid")
  @NotNull
  public Integer getUid() {
    return uid;
  }

  @JsonProperty("uid")
  public void setUid(Integer uid) {
    this.uid = uid;
  }

  /**
   **/
  public ApiItipApplyRequest attachmentIndex(Integer attachmentIndex) {
    this.attachmentIndex = attachmentIndex;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("attachmentIndex")
  @NotNull
  public Integer getAttachmentIndex() {
    return attachmentIndex;
  }

  @JsonProperty("attachmentIndex")
  public void setAttachmentIndex(Integer attachmentIndex) {
    this.attachmentIndex = attachmentIndex;
  }

  /**
   * Caller-supplied verb. The server crosses it with the iTIP METHOD on the payload to compute the actual effect.
   **/
  public ApiItipApplyRequest action(ActionEnum action) {
    this.action = action;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Caller-supplied verb. The server crosses it with the iTIP METHOD on the payload to compute the actual effect.")
  @JsonProperty("action")
  @NotNull
  public ActionEnum getAction() {
    return action;
  }

  @JsonProperty("action")
  public void setAction(ActionEnum action) {
    this.action = action;
  }

  /**
   * Optional. Required for REQUEST + accept/tentative when no existing match. When omitted, the server picks the user&#39;s default calendar (or built-in if default is shared).
   **/
  public ApiItipApplyRequest targetCalendarId(String targetCalendarId) {
    this.targetCalendarId = targetCalendarId;
    return this;
  }

  
  @ApiModelProperty(value = "Optional. Required for REQUEST + accept/tentative when no existing match. When omitted, the server picks the user's default calendar (or built-in if default is shared).")
  @JsonProperty("targetCalendarId")
  public String getTargetCalendarId() {
    return targetCalendarId;
  }

  @JsonProperty("targetCalendarId")
  public void setTargetCalendarId(String targetCalendarId) {
    this.targetCalendarId = targetCalendarId;
  }

  /**
   * When true (default), the server emits an iTIP REPLY mail back to the organizer. Set false to suppress.
   **/
  public ApiItipApplyRequest notify(Boolean notify) {
    this.notify = notify;
    return this;
  }

  
  @ApiModelProperty(value = "When true (default), the server emits an iTIP REPLY mail back to the organizer. Set false to suppress.")
  @JsonProperty("notify")
  public Boolean getNotify() {
    return notify;
  }

  @JsonProperty("notify")
  public void setNotify(Boolean notify) {
    this.notify = notify;
  }

  /**
   * Optional RSVP comment included in the outgoing REPLY.
   **/
  public ApiItipApplyRequest comment(String comment) {
    this.comment = comment;
    return this;
  }

  
  @ApiModelProperty(value = "Optional RSVP comment included in the outgoing REPLY.")
  @JsonProperty("comment")
  public String getComment() {
    return comment;
  }

  @JsonProperty("comment")
  public void setComment(String comment) {
    this.comment = comment;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiItipApplyRequest itipApplyRequest = (ApiItipApplyRequest) o;
    return Objects.equals(this.folderId, itipApplyRequest.folderId) &&
        Objects.equals(this.uid, itipApplyRequest.uid) &&
        Objects.equals(this.attachmentIndex, itipApplyRequest.attachmentIndex) &&
        Objects.equals(this.action, itipApplyRequest.action) &&
        Objects.equals(this.targetCalendarId, itipApplyRequest.targetCalendarId) &&
        Objects.equals(this.notify, itipApplyRequest.notify) &&
        Objects.equals(this.comment, itipApplyRequest.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(folderId, uid, attachmentIndex, action, targetCalendarId, notify, comment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiItipApplyRequest {\n");
    
    sb.append("    folderId: ").append(toIndentedString(folderId)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    attachmentIndex: ").append(toIndentedString(attachmentIndex)).append("\n");
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    targetCalendarId: ").append(toIndentedString(targetCalendarId)).append("\n");
    sb.append("    notify: ").append(toIndentedString(notify)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
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

