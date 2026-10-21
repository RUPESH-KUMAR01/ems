package com.rupesh.ems.mappers;

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
public class HibernateConstraintViolationExceptionMapper
    implements ExceptionMapper<ConstraintViolationException> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(HibernateConstraintViolationExceptionMapper.class);

  @Context private UriInfo uriInfo;

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    LOGGER.warn("Database constraint violation: {}", exception.getConstraintName(), exception);
    ApiErrorResponse errorResponse =
        new ApiErrorResponse(
            Response.Status.CONFLICT.getStatusCode(),
            Response.Status.CONFLICT.getReasonPhrase(),
            "CONFLICT",
            "This action conflicts with existing data. Refresh the page and try again.",
            uriInfo == null ? null : uriInfo.getPath(),
            List.of());

    return Response.status(Response.Status.CONFLICT)
        .type(MediaType.APPLICATION_JSON)
        .entity(errorResponse)
        .build();
  }
}
