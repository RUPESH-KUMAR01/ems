package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public class UnauthorizedException extends ApiException {
  public UnauthorizedException(String message) {
    super(Response.Status.UNAUTHORIZED, message, "UNAUTHORIZED");
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(Response.Status.UNAUTHORIZED, message, "UNAUTHORIZED", cause);
  }
}
