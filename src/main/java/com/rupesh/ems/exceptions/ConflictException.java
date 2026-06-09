package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public class ConflictException extends ApiException {
  public ConflictException(String message) {
    super(Response.Status.CONFLICT, message, "CONFLICT");
  }

  public ConflictException(String message, Throwable cause) {
    super(Response.Status.CONFLICT, message, "CONFLICT", cause);
  }
}
