package com.rupesh.ems.api.registration.req;

import java.util.List;

public class CreateRegistrationRequest {

  private Long teamId;

  private List<Long> participantUserIds;

  public CreateRegistrationRequest() {}

  public CreateRegistrationRequest(Long teamId, List<Long> participantUserIds) {
    this.teamId = teamId;
    this.participantUserIds = participantUserIds;
  }

  public Long getTeamId() {
    return teamId;
  }

  public List<Long> getParticipantUserIds() {
    return participantUserIds;
  }
}
