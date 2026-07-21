package com.rupesh.ems.core;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "team_membership_requests",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"team_id", "user_id"})})
public class TeamMembershipRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "team_id", nullable = false)
  private Long teamId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "responded_at")
  private Instant respondedAt;

  @Version
  @Column(name = "version")
  private Long version;

  @Enumerated(EnumType.STRING)
  @Column(name = "OPTLOCK", nullable = false)
  private RequestType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private RequestStatus status = RequestStatus.PENDING;

  public TeamMembershipRequest() {}

  public TeamMembershipRequest(Long teamId, Long userId, RequestType type) {
    this.teamId = teamId;
    this.userId = userId;
    this.type = type;
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

  public Long getVersion() {
    return version;
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
