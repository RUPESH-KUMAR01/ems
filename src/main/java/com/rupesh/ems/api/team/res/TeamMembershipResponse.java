package com.rupesh.ems.api.team.res;

import com.rupesh.ems.core.TeamMembershipRequest;

public class TeamMembershipResponse {
  private Long id;
  private Long teamId;
  private Long userId;
  private String type;

  public TeamMembershipResponse(Long id, Long teamId, Long userId, String type) {
    this.id = id;
    this.teamId = teamId;
    this.userId = userId;
    this.type = type;
  }

  public TeamMembershipResponse() {}

  public TeamMembershipResponse(TeamMembershipRequest request) {
    this.id = request.getId();
    this.teamId = request.getTeamId();
    this.userId = request.getUserId();
    this.type = request.getStatus().name();
  }

  public Long getId() {
    return id;
  }

  public Long getTeamId() {
    return teamId;
  }

  public Long getUserId() {
    return userId;
  }

  public String getType() {
    return type;
  }
}
