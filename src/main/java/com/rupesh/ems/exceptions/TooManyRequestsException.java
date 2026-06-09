package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public class TooManyRequestsException extends ApiException {
  public TooManyRequestsException(String message) {
    super(Response.Status.TOO_MANY_REQUESTS, message, "TOO_MANY_REQUESTS");
  }

  public TooManyRequestsException(String message, Throwable cause) {
    super(Response.Status.TOO_MANY_REQUESTS, message, "TOO_MANY_REQUESTS", cause);
  }
}
