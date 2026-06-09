package com.rupesh.ems.mappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
  private final String timestamp;
  private final int status;
  private final String error;
  private final String code;
  private final String message;
  private final String path;
  private final List<String> details;

  public ApiErrorResponse(
      int status, String error, String code, String message, String path, List<String> details) {
    this.timestamp = Instant.now().toString();
    this.status = status;
    this.error = error;
    this.code = code;
    this.message = message;
    this.path = path;
    this.details = details;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public int getStatus() {
    return status;
  }

  public String getError() {
    return error;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public String getPath() {
    return path;
  }

  public List<String> getDetails() {
    return details;
  }
}
