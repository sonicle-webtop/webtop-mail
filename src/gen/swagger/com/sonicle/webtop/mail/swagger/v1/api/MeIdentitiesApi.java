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

@Path("/me/identities")
@Api(description = "the MeIdentities API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-19T11:30:26.347+02:00[Europe/Rome]")
public abstract class MeIdentitiesApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists identities", notes = "", response = ApiIdentity.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_identities" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiIdentity.class, responseContainer = "List")
    })
    public Response listIdentities() {
        return Response.ok().entity("magic!").build();
    }
}
