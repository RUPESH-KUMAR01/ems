package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public class ForbiddenException extends ApiException {
  public ForbiddenException(String message) {
    super(Response.Status.FORBIDDEN, message, "FORBIDDEN");
  }

  public ForbiddenException(String message, Throwable cause) {
    super(Response.Status.FORBIDDEN, message, "FORBIDDEN", cause);
  }
}
