package com.sonicle.webtop.mail.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("Folder")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-01T08:34:42.710+02:00[Europe/Rome]")
public class ApiFolder   {
  private @Valid String id;
  private @Valid String name;
  private @Valid Integer unreadCount;
  private @Valid Integer totalCount;
  private @Valid List<ApiFolder> children;

  /**
   **/
  public ApiFolder id(String id) {
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
  public ApiFolder name(String name) {
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
   **/
  public ApiFolder unreadCount(Integer unreadCount) {
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
  public ApiFolder totalCount(Integer totalCount) {
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
   **/
  public ApiFolder children(List<ApiFolder> children) {
    this.children = children;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("children")
  public List<ApiFolder> getChildren() {
    return children;
  }

  @JsonProperty("children")
  public void setChildren(List<ApiFolder> children) {
    this.children = children;
  }

  public ApiFolder addChildrenItem(ApiFolder childrenItem) {
    if (this.children == null) {
      this.children = new ArrayList<>();
    }

    this.children.add(childrenItem);
    return this;
  }

  public ApiFolder removeChildrenItem(ApiFolder childrenItem) {
    if (childrenItem != null && this.children != null) {
      this.children.remove(childrenItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiFolder folder = (ApiFolder) o;
    return Objects.equals(this.id, folder.id) &&
        Objects.equals(this.name, folder.name) &&
        Objects.equals(this.unreadCount, folder.unreadCount) &&
        Objects.equals(this.totalCount, folder.totalCount) &&
        Objects.equals(this.children, folder.children);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, unreadCount, totalCount, children);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiFolder {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    unreadCount: ").append(toIndentedString(unreadCount)).append("\n");
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
    sb.append("    children: ").append(toIndentedString(children)).append("\n");
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

