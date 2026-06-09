package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public class NotFoundException extends ApiException {
  public NotFoundException(String message) {
    super(Response.Status.NOT_FOUND, message, "NOT_FOUND");
  }

  public NotFoundException(String message, Throwable cause) {
    super(Response.Status.NOT_FOUND, message, "NOT_FOUND", cause);
  }
}
