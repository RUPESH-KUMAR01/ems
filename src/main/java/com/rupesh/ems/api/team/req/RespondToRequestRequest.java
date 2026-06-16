package com.rupesh.ems.api.team.req;

import com.rupesh.ems.core.RequestStatus;
import jakarta.validation.constraints.NotNull;

public class RespondToRequestRequest {

  @NotNull private RequestStatus status;

  public RespondToRequestRequest() {}

  public RequestStatus getStatus() {
    return status;
  }

  public void setStatus(RequestStatus status) {
    this.status = status;
  }

  public boolean isApproved() {
    return this.status == RequestStatus.APPROVED;
  }

  public boolean isRejected() {
    return this.status == RequestStatus.REJECTED;
  }
}
