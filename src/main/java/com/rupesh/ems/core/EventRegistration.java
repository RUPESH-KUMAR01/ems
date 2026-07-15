package com.rupesh.ems.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "event_registrations",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"event_id", "user_id"}),
      @UniqueConstraint(columnNames = {"event_id", "team_id"})
    })
public class EventRegistration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false)
  private Long eventId;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "team_id")
  private Long teamId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RegistrationStatus status;

  @Column(name = "registered_at", nullable = false, updatable = false)
  private Instant registeredAt;

  public EventRegistration() {}

  public EventRegistration(
      Long eventId,
      Long userId,
      Long teamId,
      RegistrationStatus status) {
    this.eventId = eventId;
    this.userId = userId;
    this.teamId = teamId;
    this.status = status;
  }

  @PrePersist
  protected void onCreate() {
    registeredAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public Long getEventId() {
    return eventId;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public RegistrationStatus getStatus() {
    return status;
  }

  public Instant getRegisteredAt() {
    return registeredAt;
  }

  public void setStatus(RegistrationStatus status) {
    this.status = status;
  }
}