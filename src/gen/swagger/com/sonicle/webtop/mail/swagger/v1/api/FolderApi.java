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
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-13T15:40:34.652+01:00[Europe/Rome]")
public abstract class FolderApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Path("/message/{uid}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get full message of folder by uid", notes = "", response = ApiMessage.class, authorizations = {
        
        @Authorization(value = "Basic authentication")
         }, tags={ "message" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiMessage.class)
    })
    public Response getMessage(@PathParam("id") String id,@PathParam("uid") String uid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/messages")
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists messages envelops of folder", notes = "", response = ApiMessage.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "Basic authentication")
         }, tags={ "messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiMessage.class, responseContainer = "List")
    })
    public Response getMessages(@PathParam("id") String id) {
        return Response.ok().entity("magic!").build();
    }
}
