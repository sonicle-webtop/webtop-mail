package com.sonicle.webtop.mail.swagger.v1.api;

import com.sonicle.webtop.mail.swagger.v1.model.ApiMessage;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/folder/{id}")
@Api(description = "the folder API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-01T08:34:42.710+02:00[Europe/Rome]")
public abstract class FolderApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Path("/message/{uid}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get full message of folder by uid", notes = "", response = ApiMessage.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "message" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiMessage.class)
    })
    public Response getMessage(@PathParam("id") String id,@PathParam("uid") String uid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/message/{uid}/attachment/{index}/$value")
    @Produces({ "application/octet-stream" })
    @ApiOperation(value = "Get attachment bytes from message of folder by uid / index", notes = "", response = Object.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "message" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Object.class)
    })
    public Response getMessageAttachmentBytes(@PathParam("id") String id,@PathParam("uid") String uid,@PathParam("index") String index) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/message/{uid}/cid/{name}/$value")
    @Produces({ "application/octet-stream" })
    @ApiOperation(value = "Get Cid bytes from message of folder by uid / cidName", notes = "", response = Object.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "message" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Object.class)
    })
    public Response getMessageCidBytes(@PathParam("id") String id,@PathParam("uid") String uid,@PathParam("name") String name) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/messages")
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists messages envelops of folder", notes = "", response = ApiMessage.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiMessage.class, responseContainer = "List")
    })
    public Response getMessages(@PathParam("id") String id,@QueryParam("_page_no")  @ApiParam("The page number to return, providing a value actually activates pagination. Optional.")  Integer pageNo,@QueryParam("_page_size")  @ApiParam("How many items to return when paginating. Defaults to 50.")  Integer pageSize) {
        return Response.ok().entity("magic!").build();
    }
}
