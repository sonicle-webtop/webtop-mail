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



@JsonTypeName("FolderInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-07-03T14:22:13.645+02:00[Europe/Rome]")
public class ApiFolderInfo   {
  private @Valid String id;
  private @Valid String name;
  public enum TypeEnum {

    INBOX(String.valueOf("inbox")), SENT(String.valueOf("sent")), DRAFTS(String.valueOf("drafts")), TRASH(String.valueOf("trash")), SPAM(String.valueOf("spam")), ARCHIVE(String.valueOf("archive")), SHARED(String.valueOf("shared")), OTHER(String.valueOf("other"));


    private String value;

    TypeEnum (String v) {
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
	public static TypeEnum fromString(String s) {
        for (TypeEnum b : TypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static TypeEnum fromValue(String value) {
        for (TypeEnum b : TypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid TypeEnum type;
  private @Valid Integer unreadCount;
  private @Valid Integer totalCount;
  private @Valid Boolean hasChildren;

  /**
   **/
  public ApiFolderInfo id(String id) {
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
  public ApiFolderInfo name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Machine-readable folder classification: the special-folder role (inbox/sent/drafts/trash/spam/archive, matching the user&#39;s configuration, including the same roles under shared mailboxes), &#39;shared&#39; for other folders under a shared-namespace prefix, &#39;other&#39; for regular folders.
   **/
  public ApiFolderInfo type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "Machine-readable folder classification: the special-folder role (inbox/sent/drafts/trash/spam/archive, matching the user's configuration, including the same roles under shared mailboxes), 'shared' for other folders under a shared-namespace prefix, 'other' for regular folders.")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   **/
  public ApiFolderInfo unreadCount(Integer unreadCount) {
    this.unreadCount = unreadCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("unreadCount")
  public Integer getUnreadCount() {
    return unreadCount;
  }

  @JsonProperty("unreadCount")
  public void setUnreadCount(Integer unreadCount) {
    this.unreadCount = unreadCount;
  }

  /**
   **/
  public ApiFolderInfo totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("totalCount")
  public Integer getTotalCount() {
    return totalCount;
  }

  @JsonProperty("totalCount")
  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  /**
   * True when the folder has at least one subfolder: clients can render an expand handle without fetching children.
   **/
  public ApiFolderInfo hasChildren(Boolean hasChildren) {
    this.hasChildren = hasChildren;
    return this;
  }

  
  @ApiModelProperty(value = "True when the folder has at least one subfolder: clients can render an expand handle without fetching children.")
  @JsonProperty("hasChildren")
  public Boolean getHasChildren() {
    return hasChildren;
  }

  @JsonProperty("hasChildren")
  public void setHasChildren(Boolean hasChildren) {
    this.hasChildren = hasChildren;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiFolderInfo folderInfo = (ApiFolderInfo) o;
    return Objects.equals(this.id, folderInfo.id) &&
        Objects.equals(this.name, folderInfo.name) &&
        Objects.equals(this.type, folderInfo.type) &&
        Objects.equals(this.unreadCount, folderInfo.unreadCount) &&
        Objects.equals(this.totalCount, folderInfo.totalCount) &&
        Objects.equals(this.hasChildren, folderInfo.hasChildren);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, unreadCount, totalCount, hasChildren);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiFolderInfo {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    unreadCount: ").append(toIndentedString(unreadCount)).append("\n");
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
    sb.append("    hasChildren: ").append(toIndentedString(hasChildren)).append("\n");
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

