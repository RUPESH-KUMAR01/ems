package com.rupesh.ems.api.admin.req;

import com.rupesh.ems.core.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

public class AdminUpdateRegistrationStatusRequest {

  @NotNull private RegistrationStatus status;

  public AdminUpdateRegistrationStatusRequest() {}

  public AdminUpdateRegistrationStatusRequest(RegistrationStatus status) {
    this.status = status;
  }

  public RegistrationStatus getStatus() {
    return status;
  }

  public void setStatus(RegistrationStatus status) {
    this.status = status;
  }
}
