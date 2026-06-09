package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public class ServiceUnavailableException extends ApiException {
  public ServiceUnavailableException(String message) {
    super(Response.Status.SERVICE_UNAVAILABLE, message, "SERVICE_UNAVAILABLE");
  }

  public ServiceUnavailableException(String message, Throwable cause) {
    super(Response.Status.SERVICE_UNAVAILABLE, message, "SERVICE_UNAVAILABLE", cause);
  }
}
