package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

public class ValidationException extends ApiException {
  private final List<String> violations;

  public ValidationException(String message) {
    this(message, Collections.emptyList());
  }

  public ValidationException(String message, List<String> violations) {
    super(Response.Status.BAD_REQUEST, message, "VALIDATION_ERROR");
    this.violations = violations == null ? Collections.emptyList() : List.copyOf(violations);
  }

  public List<String> getViolations() {
    return violations;
  }
}
