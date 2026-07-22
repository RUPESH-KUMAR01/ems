package com.rupesh.ems.api.admin.req;

import com.rupesh.ems.core.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class AdminUpdatePaymentStatusRequest {

  @NotNull private PaymentStatus status;

  public AdminUpdatePaymentStatusRequest() {}

  public AdminUpdatePaymentStatusRequest(PaymentStatus status) {
    this.status = status;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }
}
