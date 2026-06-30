package com.rupesh.ems.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "event_registrations")
public class EventRegistration {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false)
  private Long eventId;

  @Column(name = "team_id", nullable = true)
  private Long teamId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false)
  private PaymentStatus paymentStatus = PaymentStatus.PENDING;

  public EventRegistration(Long eventId, Long teamId) {
    this.eventId = eventId;
    this.teamId = teamId;
  }

  public EventRegistration() {}

  public Long getId() {
    return id;
  }

  public Long getEventId() {
    return eventId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  @PrePersist
  public void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  public void onUpdate() {
    updatedAt = Instant.now();
  }
}
