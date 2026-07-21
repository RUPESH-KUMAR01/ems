package com.rupesh.ems.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "team_members",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id", "user_id"}),
        @UniqueConstraint(columnNames = {"event_id", "user_id"})
    })
public class TeamMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "team_id", nullable = false)
  private Long teamId;

  @Column(name = "event_id", nullable = false)
  private Long eventId;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  public TeamMember() {}

  public TeamMember(Long userId, Long teamId, Long eventId) {
    this.userId = userId;
    this.teamId = teamId;
    this.eventId = eventId;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public Long getEventId() {
    return eventId;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }

  @PrePersist
  protected void onCreate() {
    joinedAt = Instant.now();
  }
}
