package com.rupesh.ems.mappers;

import java.util.List;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {
    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(IllegalStateException exception) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                Response.Status.CONFLICT.getStatusCode(),
                Response.Status.CONFLICT.getReasonPhrase(),
                "CONFLICT",
                exception.getMessage(),
                uriInfo == null ? null : uriInfo.getPath(),
                List.of());

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}