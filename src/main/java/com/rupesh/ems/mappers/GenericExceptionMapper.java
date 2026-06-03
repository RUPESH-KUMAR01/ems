package com.rupesh.ems.mappers;

import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException webApplicationException) {
            Response response = webApplicationException.getResponse();
            if (response != null && response.getEntity() != null) {
                return response;
            }
        }

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "INTERNAL_SERVER_ERROR",
                exception.getMessage() == null ? "Unexpected error" : exception.getMessage(),
                uriInfo == null ? null : uriInfo.getPath(),
                List.of());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}