package com.rupesh.ems.mappers;

import com.rupesh.ems.exceptions.ApiException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
  @Context private UriInfo uriInfo;

  @Override
  public Response toResponse(ApiException exception) {
    ApiErrorResponse errorResponse =
        new ApiErrorResponse(
            exception.getStatus().getStatusCode(),
            exception.getStatus().getReasonPhrase(),
            exception.getErrorCode(),
            exception.getMessage(),
            uriInfo == null ? null : uriInfo.getPath(),
            exception instanceof com.rupesh.ems.exceptions.ValidationException validationException
                ? validationException.getViolations()
                : List.of());

    return Response.status(exception.getStatus())
        .type(MediaType.APPLICATION_JSON)
        .entity(errorResponse)
        .build();
  }
}
