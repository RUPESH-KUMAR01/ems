package com.rupesh.ems.api.registration.res;

import com.rupesh.ems.core.EventRegistration;
import com.rupesh.ems.core.RegistrationStatus;
import java.time.Instant;

public class EventRegistrationResponse {

  private final Long id;
  private final Long eventId;
  private final Long userId;
  private final Long teamId;
  private final RegistrationStatus status;
  private final Instant registeredAt;

  public EventRegistrationResponse(EventRegistration registration) {
    this.id = registration.getId();
    this.eventId = registration.getEventId();
    this.userId = registration.getUserId();
    this.teamId = registration.getTeamId();
    this.status = registration.getStatus();
    this.registeredAt = registration.getRegisteredAt();
  }

  public Long getId() {
    return id;
  }

  public Long getEventId() {
    return eventId;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public RegistrationStatus getStatus() {
    return status;
  }

  public Instant getRegisteredAt() {
    return registeredAt;
  }
}