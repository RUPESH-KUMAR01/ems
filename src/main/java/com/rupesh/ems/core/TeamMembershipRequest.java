package com.rupesh.ems.core;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "team_membership_requests",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"team_id", "user_id"})})
public class TeamMembershipRequest {

  private Long id;

  private Long teamId;

  private Long userId;

  @Enumerated(EnumType.STRING)
  private RequestType type;

  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  private Instant createdAt;

  private Instant respondedAt;

  public Long getId() {
    return id;
  }

  public Long getTeamId() {
    return teamId;
  }

  public Long getUserId() {
    return userId;
  }

  public RequestType getType() {
    return type;
  }

  public RequestStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getRespondedAt() {
    return respondedAt;
  }

  public void setStatus(RequestStatus status) {
    this.status = status;
  }

  public void setRespondedAt(Instant respondedAt) {
    this.respondedAt = respondedAt;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }
}
