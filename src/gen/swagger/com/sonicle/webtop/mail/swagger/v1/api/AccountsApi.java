package com.sonicle.webtop.mail.swagger.v1.api;

import com.sonicle.webtop.mail.swagger.v1.model.Account;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/accounts")
@Api(description = "the accounts API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2018-11-22T16:36:14.507+01:00")
public abstract class AccountsApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists accounts", notes = "", response = Account.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "accounts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Account.class) })
    public Response getAccounts(@QueryParam("targetProfileId")    String targetProfileId) {
        return Response.ok().entity("magic!").build();
    }
}
