package com.sonicle.webtop.mail.swagger.v1.api;

import com.sonicle.webtop.mail.swagger.v1.model.ApiIdentity;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/identities")
@Api(description = "the identities API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-01T08:34:42.710+02:00[Europe/Rome]")
public abstract class IdentitiesApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists identities", notes = "", response = ApiIdentity.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "folders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiIdentity.class, responseContainer = "List")
    })
    public Response getIdentities() {
        return Response.ok().entity("magic!").build();
    }
}
