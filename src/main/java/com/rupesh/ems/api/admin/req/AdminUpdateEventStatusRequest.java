package com.rupesh.ems.api.admin.req;

import com.rupesh.ems.core.EventStatus;
import jakarta.validation.constraints.NotNull;

public class AdminUpdateEventStatusRequest {

  @NotNull private EventStatus status;

  public AdminUpdateEventStatusRequest() {}

  public AdminUpdateEventStatusRequest(EventStatus status) {
    this.status = status;
  }

  public EventStatus getStatus() {
    return status;
  }

  public void setStatus(EventStatus status) {
    this.status = status;
  }
}
