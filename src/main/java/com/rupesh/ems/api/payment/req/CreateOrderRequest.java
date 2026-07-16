package com.rupesh.ems.api.payment.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateOrderRequest {

  @NotNull(message = "Registration id is required")
  @Positive(message = "Registration id must be positive")
  private Long registrationId;

  public CreateOrderRequest() {}

  public Long getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(Long registrationId) {
    this.registrationId = registrationId;
  }
}
