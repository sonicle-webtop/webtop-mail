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
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-03T16:31:33.680+02:00[Europe/Berlin]")
public abstract class SettingsApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get configuration data", notes = "", response = ApiExternalArchivingSettings.class, authorizations = {
        
        @Authorization(value = "Basic authentication")
         }, tags={ "settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiExternalArchivingSettings.class)
    })
    public Response getExternalArchivingConfiguration(@QueryParam("targetProfileId")   String targetProfileId) {
        return Response.ok().entity("magic!").build();
    }
}
