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
 * Pointer to an existing calendar event matched by UID. Carried inside CalendarPart.
 **/
@ApiModel(description = "Pointer to an existing calendar event matched by UID. Carried inside CalendarPart.")
@JsonTypeName("MatchedEvent")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-26T16:48:08.944+02:00[Europe/Rome]")
public class ApiMatchedEvent   {
  private @Valid String eventInstanceId;
  private @Valid Integer calendarId;
  private @Valid String calendarName;
  private @Valid Integer currentSequence;
  private @Valid Boolean isOrganizer;

  /**
   **/
  public ApiMatchedEvent eventInstanceId(String eventInstanceId) {
    this.eventInstanceId = eventInstanceId;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public ApiMatchedEvent calendarId(Integer calendarId) {
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
  public ApiMatchedEvent calendarName(String calendarName) {
    this.calendarName = calendarName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("calendarName")
  public String getCalendarName() {
    return calendarName;
  }

  @JsonProperty("calendarName")
  public void setCalendarName(String calendarName) {
    this.calendarName = calendarName;
  }

  /**
   * iCalendar SEQUENCE of the matched event, when known. Null otherwise.
   **/
  public ApiMatchedEvent currentSequence(Integer currentSequence) {
    this.currentSequence = currentSequence;
    return this;
  }

  
  @ApiModelProperty(value = "iCalendar SEQUENCE of the matched event, when known. Null otherwise.")
  @JsonProperty("currentSequence")
  public Integer getCurrentSequence() {
    return currentSequence;
  }

  @JsonProperty("currentSequence")
  public void setCurrentSequence(Integer currentSequence) {
    this.currentSequence = currentSequence;
  }

  /**
   * True when the current user is the organizer of the matched event.
   **/
  public ApiMatchedEvent isOrganizer(Boolean isOrganizer) {
    this.isOrganizer = isOrganizer;
    return this;
  }

  
  @ApiModelProperty(value = "True when the current user is the organizer of the matched event.")
  @JsonProperty("isOrganizer")
  public Boolean getIsOrganizer() {
    return isOrganizer;
  }

  @JsonProperty("isOrganizer")
  public void setIsOrganizer(Boolean isOrganizer) {
    this.isOrganizer = isOrganizer;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiMatchedEvent matchedEvent = (ApiMatchedEvent) o;
    return Objects.equals(this.eventInstanceId, matchedEvent.eventInstanceId) &&
        Objects.equals(this.calendarId, matchedEvent.calendarId) &&
        Objects.equals(this.calendarName, matchedEvent.calendarName) &&
        Objects.equals(this.currentSequence, matchedEvent.currentSequence) &&
        Objects.equals(this.isOrganizer, matchedEvent.isOrganizer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventInstanceId, calendarId, calendarName, currentSequence, isOrganizer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiMatchedEvent {\n");
    
    sb.append("    eventInstanceId: ").append(toIndentedString(eventInstanceId)).append("\n");
    sb.append("    calendarId: ").append(toIndentedString(calendarId)).append("\n");
    sb.append("    calendarName: ").append(toIndentedString(calendarName)).append("\n");
    sb.append("    currentSequence: ").append(toIndentedString(currentSequence)).append("\n");
    sb.append("    isOrganizer: ").append(toIndentedString(isOrganizer)).append("\n");
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

