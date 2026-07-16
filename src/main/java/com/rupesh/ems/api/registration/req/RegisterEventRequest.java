package com.rupesh.ems.api.registration.req;

import jakarta.validation.constraints.Positive;

public class RegisterEventRequest {

  @Positive(message = "Team id must be positive")
  private Long teamId;

  public RegisterEventRequest() {}

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }
}
