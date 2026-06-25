package com.rupesh.ems.api.event.req;

import com.rupesh.ems.core.EventStatus;
import com.rupesh.ems.core.EventType;
import com.rupesh.ems.core.EventVisibility;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import java.time.Instant;

public class UpdateEventRequest {

  private String name;

  private String description;

  private EventType type;

  private EventVisibility visibility;

  private EventStatus status;

  @Min(1)
  private Integer maxParticipants;

  @Min(1)
  private Integer minTeamSize;

  @Min(1)
  private Integer maxTeamSize;

  @Future private Instant registrationDeadline;

  private Instant startTime;

  private Instant endTime;

  public UpdateEventRequest() {}

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
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

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }
}
