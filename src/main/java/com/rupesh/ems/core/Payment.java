package com.rupesh.ems.core;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "registration_id", nullable = false)
  private Long registrationId;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "provider_order_id", nullable = false, unique = true)
  private String providerOrderId;

  @Column(name = "provider_payment_id")
  private String providerPaymentId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "paid_at")
  private Instant paidAt;

  public Payment() {}

  public Payment(Long registrationId, BigDecimal amount, String providerOrderId) {
    this.registrationId = registrationId;
    this.amount = amount;
    this.providerOrderId = providerOrderId;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
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

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public void setProviderPaymentId(String providerPaymentId) {
    this.providerPaymentId = providerPaymentId;
  }

  public void setPaidAt(Instant paidAt) {
    this.paidAt = paidAt;
  }
}
