package com.rupesh.ems.api.admin.req;

import jakarta.validation.constraints.NotNull;

public class AdminForceRegisterRequest {

  @NotNull private Long eventId;

  @NotNull private Long userId;

  private Long teamId;

  public AdminForceRegisterRequest() {}

  public AdminForceRegisterRequest(Long eventId, Long userId, Long teamId) {
    this.eventId = eventId;
    this.userId = userId;
    this.teamId = teamId;
  }

  public Long getEventId() {
    return eventId;
  }

  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }
}
