package com.rupesh.ems.core;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "event_participants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"}))
public class EventParticipant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "registration_id", nullable = false)
  private Long registrationId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "event_id", nullable = false)
  private Long eventId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public EventParticipant() {}

  public EventParticipant(Long registrationId, Long userId) {
    this.registrationId = registrationId;
    this.userId = userId;
  }

  public Long getId() {
    return id;
  }

  public Long getRegistrationId() {
    return registrationId;
  }

  public Long getUserId() {
    return userId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Long getEventId() {
    return eventId;
  }

  public void setRegistrationId(Long registrationId) {
    this.registrationId = registrationId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }

  @PrePersist
  public void onCreate() {
    createdAt = Instant.now();
  }
}
