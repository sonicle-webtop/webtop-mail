package com.sonicle.webtop.mail.swagger.v1.api;

import com.sonicle.webtop.mail.swagger.v1.model.ExternalArchivingSettings;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/settings")
@Api(description = "the settings API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2018-11-21T12:32:34.177+01:00")
public abstract class SettingsApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Path("/external-archiving")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get configuration data", notes = "", response = ExternalArchivingSettings.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ExternalArchivingSettings.class) })
    public Response getExternalArchivingConfiguration(@QueryParam("targetProfileId")    String targetProfileId) {
        return Response.ok().entity("magic!").build();
    }
}
