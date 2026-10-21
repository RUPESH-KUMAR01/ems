package com.rupesh.ems.api.team.res;

import com.rupesh.ems.core.TeamMembershipRequest;
import com.rupesh.ems.core.User;

public class TeamMembershipResponse {
  private Long id;
  private Long teamId;
  private Long userId;
  /** @deprecated use {@link #status} — kept for older clients */
  private String type;
  private String status;
  private String requestType;
  private Long eventId;
  private String userName;
  private String userEmail;

  public TeamMembershipResponse(Long id, Long teamId, Long userId, String type) {
    this.id = id;
    this.teamId = teamId;
    this.userId = userId;
    this.type = type;
    this.status = type;
  }

  public TeamMembershipResponse() {}

  public TeamMembershipResponse(TeamMembershipRequest request) {
    this(request, null);
  }

  public TeamMembershipResponse(TeamMembershipRequest request, Long eventId) {
    this(request, eventId, null);
  }

  public TeamMembershipResponse(TeamMembershipRequest request, Long eventId, User user) {
    this.id = request.getId();
    this.teamId = request.getTeamId();
    this.userId = request.getUserId();
    this.status = request.getStatus().name();
    this.requestType = request.getType().name();
    this.type = this.status;
    this.eventId = eventId;
    if (user != null) {
      this.userName = user.getName();
      this.userEmail = user.getEmail();
    }
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

  public String getStatus() {
    return status;
  }

  public String getRequestType() {
    return requestType;
  }

  public Long getEventId() {
    return eventId;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserEmail() {
    return userEmail;
  }
}
