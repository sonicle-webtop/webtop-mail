package com.sonicle.webtop.mail.swagger.v1.api;

import com.sonicle.webtop.mail.swagger.v1.model.ApiFolder;
import com.sonicle.webtop.mail.swagger.v1.model.ApiFolderInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/me/folders")
@Api(description = "the MeFolders API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-10-27T12:40:58.613+01:00[Europe/Rome]")
public abstract class MeFoldersApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @ApiOperation(value = "Create Folder", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response createFolder(@QueryParam("folder_id")   String folderId) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @ApiOperation(value = "Delete Folder", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response deleteFolder(@QueryParam("folder_id")   String folderId,@QueryParam("trash")   Boolean trash) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/info")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get folder info", notes = "", response = ApiFolderInfo.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiFolderInfo.class)
    })
    public Response getFolderInfo(@QueryParam("folder_id")   String folderId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/all")
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists root folders", notes = "returns an unordered list of all full ids of the folders tree", response = String.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "List")
    })
    public Response listAllFolders() {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/children")
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists children folders", notes = "", response = ApiFolder.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiFolder.class, responseContainer = "List")
    })
    public Response listChildrenFolders(@QueryParam("folder_id")  @ApiParam("The full folder name")  String folderId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists root folders", notes = "", response = ApiFolder.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiFolder.class, responseContainer = "List")
    })
    public Response listRootFolders() {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @ApiOperation(value = "Rename Folder", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response renameFolder(@QueryParam("folder_id")   String folderId,@QueryParam("new_folder_id")   String newFolderId) {
        return Response.ok().entity("magic!").build();
    }
}
