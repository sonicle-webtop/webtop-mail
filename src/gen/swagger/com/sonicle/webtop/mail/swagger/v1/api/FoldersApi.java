package com.sonicle.webtop.mail.swagger.v1.api;

import com.sonicle.webtop.mail.swagger.v1.model.ApiFolder;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/folders")
@Api(description = "the folders API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-03T16:31:33.680+02:00[Europe/Berlin]")
public abstract class FoldersApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Path("/{id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists folders", notes = "", response = ApiFolder.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "Basic authentication")
         }, tags={ "folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiFolder.class, responseContainer = "List")
    })
    public Response getFolders(@PathParam("id") String id) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists folders", notes = "", response = ApiFolder.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "Basic authentication")
         }, tags={ "folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiFolder.class, responseContainer = "List")
    })
    public Response getRootFolders() {
        return Response.ok().entity("magic!").build();
    }
}
