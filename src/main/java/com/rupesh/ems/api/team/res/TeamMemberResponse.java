package com.rupesh.ems.api.team.res;

import com.rupesh.ems.core.TeamMember;
import java.time.Instant;

public class TeamMemberResponse {

  private Long userId;
  private Long teamId;
  private Instant joinedAt;

  public TeamMemberResponse(TeamMember teamMember) {
    this.userId = teamMember.getUserId();
    this.teamId = teamMember.getTeamId();
    this.joinedAt = teamMember.getJoinedAt();
  }

  public Long getUserId() {
    return userId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }
}
