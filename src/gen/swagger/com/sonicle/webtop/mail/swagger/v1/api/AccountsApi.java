package com.sonicle.webtop.mail.swagger.v1.api;

import com.sonicle.webtop.mail.swagger.v1.model.ApiAccount;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/accounts")
@Api(description = "the accounts API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-03T16:31:33.680+02:00[Europe/Berlin]")
public abstract class AccountsApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists accounts", notes = "", response = ApiAccount.class, authorizations = {
        
        @Authorization(value = "Basic authentication")
         }, tags={ "accounts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiAccount.class)
    })
    public Response getAccounts(@QueryParam("targetProfileId")   String targetProfileId) {
        return Response.ok().entity("magic!").build();
    }
}
