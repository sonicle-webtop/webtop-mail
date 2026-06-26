package com.sonicle.webtop.mail.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.mail.swagger.v1.model.ApiContact;
import com.sonicle.webtop.mail.swagger.v1.model.ApiMatchedEvent;
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
 * Parsed view of a text/calendar (or application/ics) attachment carrying an iTIP payload.
 **/
@ApiModel(description = "Parsed view of a text/calendar (or application/ics) attachment carrying an iTIP payload.")
@JsonTypeName("CalendarPart")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-26T16:48:08.944+02:00[Europe/Rome]")
public class ApiCalendarPart   {
  private @Valid Integer attachmentIndex;
  public enum MethodEnum {

    REQUEST(String.valueOf("REQUEST")), REPLY(String.valueOf("REPLY")), CANCEL(String.valueOf("CANCEL")), COUNTER(String.valueOf("COUNTER")), PUBLISH(String.valueOf("PUBLISH")), REFRESH(String.valueOf("REFRESH"));


    private String value;

    MethodEnum (String v) {
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
	public static MethodEnum fromString(String s) {
        for (MethodEnum b : MethodEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static MethodEnum fromValue(String value) {
        for (MethodEnum b : MethodEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid MethodEnum method;
  private @Valid String eventUid;
  private @Valid Integer sequence;
  private @Valid String summary;
  private @Valid String location;
  private @Valid String startsAt;
  private @Valid String endsAt;
  private @Valid Boolean allDay;
  private @Valid String timezone;
  private @Valid ApiContact organizer;
  private @Valid Boolean userIsAttendee;
  public enum UserResponseStatusEnum {

    NA(String.valueOf("NA")), AC(String.valueOf("AC")), DE(String.valueOf("DE")), TE(String.valueOf("TE"));


    private String value;

    UserResponseStatusEnum (String v) {
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
	public static UserResponseStatusEnum fromString(String s) {
        for (UserResponseStatusEnum b : UserResponseStatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static UserResponseStatusEnum fromValue(String value) {
        for (UserResponseStatusEnum b : UserResponseStatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid UserResponseStatusEnum userResponseStatus;
  private @Valid ApiMatchedEvent matchExistingEvent;
  private @Valid ApiContact replyAttendee;
  public enum ReplyStatusEnum {

    NA(String.valueOf("NA")), AC(String.valueOf("AC")), DE(String.valueOf("DE")), TE(String.valueOf("TE"));


    private String value;

    ReplyStatusEnum (String v) {
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
	public static ReplyStatusEnum fromString(String s) {
        for (ReplyStatusEnum b : ReplyStatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static ReplyStatusEnum fromValue(String value) {
        for (ReplyStatusEnum b : ReplyStatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid ReplyStatusEnum replyStatus;
  private @Valid String comment;

  /**
   * Zero-based index into the message&#39;s attachments array. Use this when calling /me/messages/calendar/apply.
   **/
  public ApiCalendarPart attachmentIndex(Integer attachmentIndex) {
    this.attachmentIndex = attachmentIndex;
    return this;
  }

  
  @ApiModelProperty(value = "Zero-based index into the message's attachments array. Use this when calling /me/messages/calendar/apply.")
  @JsonProperty("attachmentIndex")
  public Integer getAttachmentIndex() {
    return attachmentIndex;
  }

  @JsonProperty("attachmentIndex")
  public void setAttachmentIndex(Integer attachmentIndex) {
    this.attachmentIndex = attachmentIndex;
  }

  /**
   * iTIP METHOD value.
   **/
  public ApiCalendarPart method(MethodEnum method) {
    this.method = method;
    return this;
  }

  
  @ApiModelProperty(value = "iTIP METHOD value.")
  @JsonProperty("method")
  public MethodEnum getMethod() {
    return method;
  }

  @JsonProperty("method")
  public void setMethod(MethodEnum method) {
    this.method = method;
  }

  /**
   **/
  public ApiCalendarPart eventUid(String eventUid) {
    this.eventUid = eventUid;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("eventUid")
  public String getEventUid() {
    return eventUid;
  }

  @JsonProperty("eventUid")
  public void setEventUid(String eventUid) {
    this.eventUid = eventUid;
  }

  /**
   **/
  public ApiCalendarPart sequence(Integer sequence) {
    this.sequence = sequence;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sequence")
  public Integer getSequence() {
    return sequence;
  }

  @JsonProperty("sequence")
  public void setSequence(Integer sequence) {
    this.sequence = sequence;
  }

  /**
   **/
  public ApiCalendarPart summary(String summary) {
    this.summary = summary;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("summary")
  public String getSummary() {
    return summary;
  }

  @JsonProperty("summary")
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   **/
  public ApiCalendarPart location(String location) {
    this.location = location;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("location")
  public String getLocation() {
    return location;
  }

  @JsonProperty("location")
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * ISO 8601 timestamp. For all-day events represents local midnight on the start date.
   **/
  public ApiCalendarPart startsAt(String startsAt) {
    this.startsAt = startsAt;
    return this;
  }

  
  @ApiModelProperty(value = "ISO 8601 timestamp. For all-day events represents local midnight on the start date.")
  @JsonProperty("startsAt")
  public String getStartsAt() {
    return startsAt;
  }

  @JsonProperty("startsAt")
  public void setStartsAt(String startsAt) {
    this.startsAt = startsAt;
  }

  /**
   **/
  public ApiCalendarPart endsAt(String endsAt) {
    this.endsAt = endsAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("endsAt")
  public String getEndsAt() {
    return endsAt;
  }

  @JsonProperty("endsAt")
  public void setEndsAt(String endsAt) {
    this.endsAt = endsAt;
  }

  /**
   **/
  public ApiCalendarPart allDay(Boolean allDay) {
    this.allDay = allDay;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("allDay")
  public Boolean getAllDay() {
    return allDay;
  }

  @JsonProperty("allDay")
  public void setAllDay(Boolean allDay) {
    this.allDay = allDay;
  }

  /**
   * TZID from DTSTART, when present.
   **/
  public ApiCalendarPart timezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

  
  @ApiModelProperty(value = "TZID from DTSTART, when present.")
  @JsonProperty("timezone")
  public String getTimezone() {
    return timezone;
  }

  @JsonProperty("timezone")
  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  /**
   **/
  public ApiCalendarPart organizer(ApiContact organizer) {
    this.organizer = organizer;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("organizer")
  public ApiContact getOrganizer() {
    return organizer;
  }

  @JsonProperty("organizer")
  public void setOrganizer(ApiContact organizer) {
    this.organizer = organizer;
  }

  /**
   * True when the current user&#39;s personal email matches an ATTENDEE on the iTIP payload.
   **/
  public ApiCalendarPart userIsAttendee(Boolean userIsAttendee) {
    this.userIsAttendee = userIsAttendee;
    return this;
  }

  
  @ApiModelProperty(value = "True when the current user's personal email matches an ATTENDEE on the iTIP payload.")
  @JsonProperty("userIsAttendee")
  public Boolean getUserIsAttendee() {
    return userIsAttendee;
  }

  @JsonProperty("userIsAttendee")
  public void setUserIsAttendee(Boolean userIsAttendee) {
    this.userIsAttendee = userIsAttendee;
  }

  /**
   * Short PARTSTAT code for the current user. Present iff userIsAttendee.
   **/
  public ApiCalendarPart userResponseStatus(UserResponseStatusEnum userResponseStatus) {
    this.userResponseStatus = userResponseStatus;
    return this;
  }

  
  @ApiModelProperty(value = "Short PARTSTAT code for the current user. Present iff userIsAttendee.")
  @JsonProperty("userResponseStatus")
  public UserResponseStatusEnum getUserResponseStatus() {
    return userResponseStatus;
  }

  @JsonProperty("userResponseStatus")
  public void setUserResponseStatus(UserResponseStatusEnum userResponseStatus) {
    this.userResponseStatus = userResponseStatus;
  }

  /**
   **/
  public ApiCalendarPart matchExistingEvent(ApiMatchedEvent matchExistingEvent) {
    this.matchExistingEvent = matchExistingEvent;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("matchExistingEvent")
  public ApiMatchedEvent getMatchExistingEvent() {
    return matchExistingEvent;
  }

  @JsonProperty("matchExistingEvent")
  public void setMatchExistingEvent(ApiMatchedEvent matchExistingEvent) {
    this.matchExistingEvent = matchExistingEvent;
  }

  /**
   **/
  public ApiCalendarPart replyAttendee(ApiContact replyAttendee) {
    this.replyAttendee = replyAttendee;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("replyAttendee")
  public ApiContact getReplyAttendee() {
    return replyAttendee;
  }

  @JsonProperty("replyAttendee")
  public void setReplyAttendee(ApiContact replyAttendee) {
    this.replyAttendee = replyAttendee;
  }

  /**
   * For method&#x3D;REPLY only: short PARTSTAT code (NA/AC/DE/TE) carried by the REPLY.
   **/
  public ApiCalendarPart replyStatus(ReplyStatusEnum replyStatus) {
    this.replyStatus = replyStatus;
    return this;
  }

  
  @ApiModelProperty(value = "For method=REPLY only: short PARTSTAT code (NA/AC/DE/TE) carried by the REPLY.")
  @JsonProperty("replyStatus")
  public ReplyStatusEnum getReplyStatus() {
    return replyStatus;
  }

  @JsonProperty("replyStatus")
  public void setReplyStatus(ReplyStatusEnum replyStatus) {
    this.replyStatus = replyStatus;
  }

  /**
   * Optional COMMENT property value from the VEVENT.
   **/
  public ApiCalendarPart comment(String comment) {
    this.comment = comment;
    return this;
  }

  
  @ApiModelProperty(value = "Optional COMMENT property value from the VEVENT.")
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
    ApiCalendarPart calendarPart = (ApiCalendarPart) o;
    return Objects.equals(this.attachmentIndex, calendarPart.attachmentIndex) &&
        Objects.equals(this.method, calendarPart.method) &&
        Objects.equals(this.eventUid, calendarPart.eventUid) &&
        Objects.equals(this.sequence, calendarPart.sequence) &&
        Objects.equals(this.summary, calendarPart.summary) &&
        Objects.equals(this.location, calendarPart.location) &&
        Objects.equals(this.startsAt, calendarPart.startsAt) &&
        Objects.equals(this.endsAt, calendarPart.endsAt) &&
        Objects.equals(this.allDay, calendarPart.allDay) &&
        Objects.equals(this.timezone, calendarPart.timezone) &&
        Objects.equals(this.organizer, calendarPart.organizer) &&
        Objects.equals(this.userIsAttendee, calendarPart.userIsAttendee) &&
        Objects.equals(this.userResponseStatus, calendarPart.userResponseStatus) &&
        Objects.equals(this.matchExistingEvent, calendarPart.matchExistingEvent) &&
        Objects.equals(this.replyAttendee, calendarPart.replyAttendee) &&
        Objects.equals(this.replyStatus, calendarPart.replyStatus) &&
        Objects.equals(this.comment, calendarPart.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attachmentIndex, method, eventUid, sequence, summary, location, startsAt, endsAt, allDay, timezone, organizer, userIsAttendee, userResponseStatus, matchExistingEvent, replyAttendee, replyStatus, comment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiCalendarPart {\n");
    
    sb.append("    attachmentIndex: ").append(toIndentedString(attachmentIndex)).append("\n");
    sb.append("    method: ").append(toIndentedString(method)).append("\n");
    sb.append("    eventUid: ").append(toIndentedString(eventUid)).append("\n");
    sb.append("    sequence: ").append(toIndentedString(sequence)).append("\n");
    sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
    sb.append("    location: ").append(toIndentedString(location)).append("\n");
    sb.append("    startsAt: ").append(toIndentedString(startsAt)).append("\n");
    sb.append("    endsAt: ").append(toIndentedString(endsAt)).append("\n");
    sb.append("    allDay: ").append(toIndentedString(allDay)).append("\n");
    sb.append("    timezone: ").append(toIndentedString(timezone)).append("\n");
    sb.append("    organizer: ").append(toIndentedString(organizer)).append("\n");
    sb.append("    userIsAttendee: ").append(toIndentedString(userIsAttendee)).append("\n");
    sb.append("    userResponseStatus: ").append(toIndentedString(userResponseStatus)).append("\n");
    sb.append("    matchExistingEvent: ").append(toIndentedString(matchExistingEvent)).append("\n");
    sb.append("    replyAttendee: ").append(toIndentedString(replyAttendee)).append("\n");
    sb.append("    replyStatus: ").append(toIndentedString(replyStatus)).append("\n");
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

