package com.rupesh.ems.mappers;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
  @Context private UriInfo uriInfo;

  @Override
  public Response toResponse(IllegalArgumentException exception) {
    ApiErrorResponse errorResponse =
        new ApiErrorResponse(
            Response.Status.BAD_REQUEST.getStatusCode(),
            Response.Status.BAD_REQUEST.getReasonPhrase(),
            "BAD_REQUEST",
            exception.getMessage(),
            uriInfo == null ? null : uriInfo.getPath(),
            List.of());

    return Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON)
        .entity(errorResponse)
        .build();
  }
}
