package com.sonicle.webtop.mail.swagger.v1.api;


import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/me/user-setting")
@Api(description = "the MeUserSetting API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-07-09T14:49:58.479+02:00[Europe/Rome]")
public abstract class MeUserSettingApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get user setting", notes = "", response = String.class, authorizations = {
        
        @Authorization(value = "basicAuth"),
        
        @Authorization(value = "bearerAuth")
         }, tags={ "me_user_setting" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = String.class)
    })
    public Response getUserSetting(@QueryParam("key")   String key) {
        return Response.ok().entity("magic!").build();
    }
}
