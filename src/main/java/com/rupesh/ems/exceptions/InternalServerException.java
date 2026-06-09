package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public class InternalServerException extends ApiException {
  public InternalServerException(String message) {
    super(Response.Status.INTERNAL_SERVER_ERROR, message, "INTERNAL_SERVER_ERROR");
  }

  public InternalServerException(String message, Throwable cause) {
    super(Response.Status.INTERNAL_SERVER_ERROR, message, "INTERNAL_SERVER_ERROR", cause);
  }
}
