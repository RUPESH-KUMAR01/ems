package com.rupesh.ems.api.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageResponse {
  private String message;

  public MessageResponse() {}

  public MessageResponse(String message) {
    this.message = message;
  }

  @JsonProperty
  public String getMessage() {
    return message;
  }

  @JsonProperty
  public void setMessage(String message) {
    this.message = message;
  }
}
