package com.rupesh.ems.api.registration.req;

import java.util.List;

public class UpdateRegistrationRequest {

  private Long teamId;
  private List<Long> participantUserIds;

  public UpdateRegistrationRequest() {}

  public Long getTeamId() {
    return teamId;
  }

  public List<Long> getParticipantUserIds() {
    return participantUserIds;
  }
}
