package com.rupesh.ems.api.registration.res;

import com.rupesh.ems.core.EventParticipant;
import java.time.Instant;

public class RegistrationParticipantResponse {

  private final Long id;
  private final Long userId;
  private final Instant createdAt;

  public RegistrationParticipantResponse(EventParticipant participant) {
    this.id = participant.getId();
    this.userId = participant.getUserId();
    this.createdAt = participant.getCreatedAt();
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
