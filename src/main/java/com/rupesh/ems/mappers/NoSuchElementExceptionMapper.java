package com.rupesh.ems.mappers;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.NoSuchElementException;

@Provider
public class NoSuchElementExceptionMapper implements ExceptionMapper<NoSuchElementException> {
  @Context private UriInfo uriInfo;

  @Override
  public Response toResponse(NoSuchElementException exception) {
    ApiErrorResponse errorResponse =
        new ApiErrorResponse(
            Response.Status.NOT_FOUND.getStatusCode(),
            Response.Status.NOT_FOUND.getReasonPhrase(),
            "NOT_FOUND",
            exception.getMessage(),
            uriInfo == null ? null : uriInfo.getPath(),
            List.of());

    return Response.status(Response.Status.NOT_FOUND)
        .type(MediaType.APPLICATION_JSON)
        .entity(errorResponse)
        .build();
  }
}
