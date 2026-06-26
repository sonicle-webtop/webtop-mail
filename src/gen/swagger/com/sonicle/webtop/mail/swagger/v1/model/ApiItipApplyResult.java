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



@JsonTypeName("ItipApplyResult")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-26T16:48:08.944+02:00[Europe/Rome]")
public class ApiItipApplyResult   {
  public enum OutcomeEnum {

    CREATED(String.valueOf("created")), UPDATED(String.valueOf("updated")), REMOVED(String.valueOf("removed")), RSVP_RECORDED(String.valueOf("rsvp_recorded")), IGNORED(String.valueOf("ignored")), NO_MATCH(String.valueOf("no_match"));


    private String value;

    OutcomeEnum (String v) {
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
	public static OutcomeEnum fromString(String s) {
        for (OutcomeEnum b : OutcomeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static OutcomeEnum fromValue(String value) {
        for (OutcomeEnum b : OutcomeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid OutcomeEnum outcome;
  private @Valid String eventInstanceId;
  private @Valid Integer calendarId;
  private @Valid Boolean replySent;

  /**
   **/
  public ApiItipApplyResult outcome(OutcomeEnum outcome) {
    this.outcome = outcome;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("outcome")
  public OutcomeEnum getOutcome() {
    return outcome;
  }

  @JsonProperty("outcome")
  public void setOutcome(OutcomeEnum outcome) {
    this.outcome = outcome;
  }

  /**
   * Present for outcomes created/updated/rsvp_recorded.
   **/
  public ApiItipApplyResult eventInstanceId(String eventInstanceId) {
    this.eventInstanceId = eventInstanceId;
    return this;
  }

  
  @ApiModelProperty(value = "Present for outcomes created/updated/rsvp_recorded.")
  @JsonProperty("eventInstanceId")
  public String getEventInstanceId() {
    return eventInstanceId;
  }

  @JsonProperty("eventInstanceId")
  public void setEventInstanceId(String eventInstanceId) {
    this.eventInstanceId = eventInstanceId;
  }

  /**
   **/
  public ApiItipApplyResult calendarId(Integer calendarId) {
    this.calendarId = calendarId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("calendarId")
  public Integer getCalendarId() {
    return calendarId;
  }

  @JsonProperty("calendarId")
  public void setCalendarId(Integer calendarId) {
    this.calendarId = calendarId;
  }

  /**
   **/
  public ApiItipApplyResult replySent(Boolean replySent) {
    this.replySent = replySent;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("replySent")
  public Boolean getReplySent() {
    return replySent;
  }

  @JsonProperty("replySent")
  public void setReplySent(Boolean replySent) {
    this.replySent = replySent;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiItipApplyResult itipApplyResult = (ApiItipApplyResult) o;
    return Objects.equals(this.outcome, itipApplyResult.outcome) &&
        Objects.equals(this.eventInstanceId, itipApplyResult.eventInstanceId) &&
        Objects.equals(this.calendarId, itipApplyResult.calendarId) &&
        Objects.equals(this.replySent, itipApplyResult.replySent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(outcome, eventInstanceId, calendarId, replySent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiItipApplyResult {\n");
    
    sb.append("    outcome: ").append(toIndentedString(outcome)).append("\n");
    sb.append("    eventInstanceId: ").append(toIndentedString(eventInstanceId)).append("\n");
    sb.append("    calendarId: ").append(toIndentedString(calendarId)).append("\n");
    sb.append("    replySent: ").append(toIndentedString(replySent)).append("\n");
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

