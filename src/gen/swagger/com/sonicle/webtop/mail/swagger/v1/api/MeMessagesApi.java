package com.sonicle.webtop.mail.swagger.v1.api;

import com.sonicle.webtop.mail.swagger.v1.model.ApiMessage;
import com.sonicle.webtop.mail.swagger.v1.model.ApiMessageNew;
import com.sonicle.webtop.mail.swagger.v1.model.ApiNote;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/me/messages")
@Api(description = "the MeMessages API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-19T11:30:26.347+02:00[Europe/Rome]")
public abstract class MeMessagesApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @DELETE
    @ApiOperation(value = "Trash message by folder and uid", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response deleteMessage(@QueryParam("folder_id")   String folderId,@QueryParam("uid")   String uid,@QueryParam("trash")   Boolean trash) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/forward")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get forward message of folder by uid", notes = "", response = ApiMessageNew.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiMessageNew.class)
    })
    public Response getForwardMessage(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("uid")  @ApiParam("The message UID in a folder")  String uid,@QueryParam("includeAttachments")   Boolean includeAttachments) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get full message of folder by uid", notes = "", response = ApiMessage.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiMessage.class)
    })
    public Response getMessage(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("uid")  @ApiParam("The message UID in a folder")  String uid,@QueryParam("set_seen")  @ApiParam("Set message as seen")  Boolean setSeen,@QueryParam("index")  @ApiParam("Attachment index for inner eml")  String index) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/attachments/$value")
    @Produces({ "application/octet-stream" })
    @ApiOperation(value = "Get attachment bytes from message of folder by uid / index", notes = "", response = Object.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Object.class)
    })
    public Response getMessageAttachmentBytes(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("uid")  @ApiParam("The message UID in a folder")  String uid,@QueryParam("index")  @ApiParam("The index of the attachment as received during get")  String index) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/cids/$value")
    @Produces({ "application/octet-stream" })
    @ApiOperation(value = "Get Cid bytes from message of folder by uid / cidName", notes = "", response = Object.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Object.class)
    })
    public Response getMessageCidBytes(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("uid")  @ApiParam("The message UID in a folder")  String uid,@QueryParam("cid_name")  @ApiParam("The Content-ID name of an inline attachment")  String cidName) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/flags")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get message flag by folder and uid", notes = "", response = String.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = String.class)
    })
    public Response getMessageFlag(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("uid")  @ApiParam("The message UID in a folder")  String uid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/notes")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Note from message", notes = "", response = ApiNote.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiNote.class)
    })
    public Response getMessageNote(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("uid")  @ApiParam("The message UID in a folder")  String uid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/seen")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get message seen state by folder and uid", notes = "", response = Boolean.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Boolean.class)
    })
    public Response getMessageSeenState(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("uid")  @ApiParam("The message UID in a folder")  String uid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/tags")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get message tags by folder and uid", notes = "", response = String.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "List")
    })
    public Response getMessageTags(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("uid")  @ApiParam("The message UID in a folder")  String uid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/reply")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get reply message of folder by uid", notes = "", response = ApiMessageNew.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiMessageNew.class)
    })
    public Response getReplyMessage(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("uid")  @ApiParam("The message UID in a folder")  String uid,@QueryParam("replyAll")   Boolean replyAll,@QueryParam("includeAttachments")   Boolean includeAttachments) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/list")
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists messages envelops of folder", notes = "", response = ApiMessage.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiMessage.class, responseContainer = "List")
    })
    public Response listMessages(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId,@QueryParam("page_no")  @ApiParam("The page number to return, providing a value actually activates pagination. Optional.")  Integer pageNo,@QueryParam("page_size")  @ApiParam("How many items to return when paginating. Defaults to 50.")  Integer pageSize,@QueryParam("filter")  @ApiParam("A RSQL filter query to filter out resulting items. Optional.")  String filter,@QueryParam("orderBy")  @ApiParam("Field namee and direction (ASC or DESC) to sort resulting items. Optional.")  String orderBy,@QueryParam("returnCount")  @ApiParam("Specifies whether to compute and return the full count of a list of items. Useful when dealing with paginated data. Optional.")  Boolean returnCount) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @ApiOperation(value = "", notes = "Move message to another folder", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response moveMessage(@QueryParam("from_folder_id")   String fromFolderId,@QueryParam("to_folder_id")   String toFolderId,@QueryParam("uid")   String uid) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/send")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Send new message", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response sendMessage(@Valid ApiMessageNew apiMessageNew) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/flags")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Set Message Flag", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response setMessageFlag(@QueryParam("folder_id")   String folderId,@QueryParam("uid")   String uid,@Valid String body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/notes")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Set Message Note Text", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response setMessageNote(@Valid ApiNote apiNote) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/seen")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Set Message Seen State", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response setMessageSeenState(@QueryParam("folder_id")   String folderId,@QueryParam("uid")   String uid,@Valid Boolean body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/tags")
    @Consumes({ "application/json" })
    @ApiOperation(value = "Set Message Tags", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response setMessageTags(@QueryParam("folder_id")   String folderId,@QueryParam("uid")   String uid,@Valid List<String> requestBody) {
        return Response.ok().entity("magic!").build();
    }
}
