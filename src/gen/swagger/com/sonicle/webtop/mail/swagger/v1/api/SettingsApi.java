package com.sonicle.webtop.mail.swagger.v1.api;

import com.sonicle.webtop.mail.swagger.v1.model.ApiExternalArchivingSettings;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/settings/external-archiving")
@Api(description = "the settings API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-01T08:34:42.710+02:00[Europe/Rome]")
public abstract class SettingsApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get configuration data", notes = "", response = ApiExternalArchivingSettings.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiExternalArchivingSettings.class)
    })
    public Response getExternalArchivingConfiguration(@QueryParam("targetProfileId")   String targetProfileId) {
        return Response.ok().entity("magic!").build();
    }
}
