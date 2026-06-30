package com.rupesh.ems.api.registration.res;

import com.rupesh.ems.core.EventRegistration;
import com.rupesh.ems.core.PaymentStatus;
import java.time.Instant;
import java.util.List;

public class RegistrationResponse {

  private final Long id;
  private final Long eventId;
  private final Long teamId;
  private final PaymentStatus paymentStatus;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final List<RegistrationParticipantResponse> participants;

  public RegistrationResponse(
      EventRegistration registration, List<RegistrationParticipantResponse> participants) {
    this.id = registration.getId();
    this.eventId = registration.getEventId();
    this.teamId = registration.getTeamId();
    this.paymentStatus = registration.getPaymentStatus();
    this.createdAt = registration.getCreatedAt();
    this.updatedAt = registration.getUpdatedAt();
    this.participants = participants;
  }

  public Long getId() {
    return id;
  }

  public Long getEventId() {
    return eventId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public List<RegistrationParticipantResponse> getParticipants() {
    return participants;
  }
}
