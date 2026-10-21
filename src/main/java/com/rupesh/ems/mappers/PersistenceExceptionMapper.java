package com.rupesh.ems.mappers;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceExceptionMapper.class);

  @Context private UriInfo uriInfo;

  @Override
  public Response toResponse(PersistenceException exception) {
    Throwable cause = exception.getCause();
    if (cause instanceof ConstraintViolationException constraint) {
      LOGGER.warn("Database constraint violation: {}", constraint.getConstraintName(), exception);
      return conflict("This action conflicts with existing data. Try refreshing and retrying.");
    }

    LOGGER.error("Unhandled persistence error", exception);
    ApiErrorResponse errorResponse =
        new ApiErrorResponse(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "INTERNAL_SERVER_ERROR",
            "A database error occurred",
            uriInfo == null ? null : uriInfo.getPath(),
            List.of());

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON)
        .entity(errorResponse)
        .build();
  }

  private Response conflict(String message) {
    ApiErrorResponse errorResponse =
        new ApiErrorResponse(
            Response.Status.CONFLICT.getStatusCode(),
            Response.Status.CONFLICT.getReasonPhrase(),
            "CONFLICT",
            message,
            uriInfo == null ? null : uriInfo.getPath(),
            List.of());

    return Response.status(Response.Status.CONFLICT)
        .type(MediaType.APPLICATION_JSON)
        .entity(errorResponse)
        .build();
  }
}
