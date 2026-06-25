package com.rupesh.ems.api.event.res;

import com.rupesh.ems.core.Event;
import com.rupesh.ems.core.EventStatus;
import com.rupesh.ems.core.EventType;
import com.rupesh.ems.core.EventVisibility;
import java.time.Instant;

public class EventResponse {

  private final Long id;
  private final String name;
  private final String description;
  private final Long createdBy;
  private final EventType type;
  private final EventVisibility visibility;
  private final EventStatus status;
  private final Integer maxParticipants;
  private final Integer minTeamSize;
  private final Integer maxTeamSize;
  private final Instant registrationDeadline;
  private final Instant createdAt;
  private final Instant updatedAt;

  public EventResponse(Event event) {
    this.id = event.getId();
    this.name = event.getName();
    this.description = event.getDescription();
    this.createdBy = event.getCreatedBy();
    this.type = event.getType();
    this.visibility = event.getVisibility();
    this.status = event.getStatus();
    this.maxParticipants = event.getMaxParticipants();
    this.minTeamSize = event.getMinTeamSize();
    this.maxTeamSize = event.getMaxTeamSize();
    this.registrationDeadline = event.getRegistrationDeadline();
    this.createdAt = event.getCreatedAt();
    this.updatedAt = event.getUpdatedAt();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public EventType getType() {
    return type;
  }

  public EventVisibility getVisibility() {
    return visibility;
  }

  public EventStatus getStatus() {
    return status;
  }

  public Integer getMaxParticipants() {
    return maxParticipants;
  }

  public Integer getMinTeamSize() {
    return minTeamSize;
  }

  public Integer getMaxTeamSize() {
    return maxTeamSize;
  }

  public Instant getRegistrationDeadline() {
    return registrationDeadline;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
