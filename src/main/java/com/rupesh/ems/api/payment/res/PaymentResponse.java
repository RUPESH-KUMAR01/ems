package com.rupesh.ems.api.payment.res;

import com.rupesh.ems.core.Payment;
import com.rupesh.ems.core.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public class PaymentResponse {

  private final Long id;
  private final Long registrationId;
  private final BigDecimal amount;
  private final PaymentStatus status;
  private final String providerOrderId;
  private final String providerPaymentId;
  private final Instant createdAt;
  private final Instant paidAt;

  public PaymentResponse(Payment payment) {
    this.id = payment.getId();
    this.registrationId = payment.getRegistrationId();
    this.amount = payment.getAmount();
    this.status = payment.getStatus();
    this.providerOrderId = payment.getProviderOrderId();
    this.providerPaymentId = payment.getProviderPaymentId();
    this.createdAt = payment.getCreatedAt();
    this.paidAt = payment.getPaidAt();
  }

  public Long getId() {
    return id;
  }

  public Long getRegistrationId() {
    return registrationId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public String getProviderOrderId() {
    return providerOrderId;
  }

  public String getProviderPaymentId() {
    return providerPaymentId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getPaidAt() {
    return paidAt;
  }
}
