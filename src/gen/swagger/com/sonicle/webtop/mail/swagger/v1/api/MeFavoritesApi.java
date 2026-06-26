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

@Path("/me/favorites")
@Api(description = "the MeFavorites API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-26T16:48:08.944+02:00[Europe/Rome]")
public abstract class MeFavoritesApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists favorites folders", notes = "", response = ApiFolder.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_favorites" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiFolder.class, responseContainer = "List")
    })
    public Response listFavorites() {
        return Response.ok().entity("magic!").build();
    }
}
