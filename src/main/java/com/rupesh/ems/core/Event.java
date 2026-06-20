package com.rupesh.ems.core;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "events")
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, length = 5000)
  private String description;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventVisibility visibility;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventStatus status = EventStatus.DRAFT;

  @Column(name = "max_participants")
  private Integer maxParticipants;

  @Column(name = "min_team_size")
  private Integer minTeamSize;

  @Column(name = "max_team_size")
  private Integer maxTeamSize;

  @Column(name = "registration_deadline", nullable = false)
  private Instant registrationDeadline;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  public Event() {}

  public Event(String name, String description, Long createdBy, EventType type, EventVisibility visibility,
               Integer maxParticipants, Integer minTeamSize, Integer maxTeamSize, Instant registrationDeadline) {
    this.name = name;
    this.description = description;
    this.createdBy = createdBy;
    this.type = type;
    this.visibility = visibility;
    this.maxParticipants = maxParticipants;
    this.minTeamSize = minTeamSize;
    this.maxTeamSize = maxTeamSize;
    this.registrationDeadline = registrationDeadline;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public EventType getType() {
    return type;
  }


  public EventVisibility getVisibility() {
    return visibility;
  }


  public EventStatus getStatus() {
    return status;
  }

  public Integer getMaxParticipants() {
    return maxParticipants;
  }

  public Integer getMinTeamSize() {
    return minTeamSize;
  }

  public Integer getMaxTeamSize() {
    return maxTeamSize;
  } 

  public Instant getRegistrationDeadline() {
    return registrationDeadline;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setStatus(EventStatus status) {
    this.status = status;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setType(EventType type) {
    this.type = type;
  }
  public void setVisibility(EventVisibility visibility) {
    this.visibility = visibility;
  }

  public void setMaxParticipants(Integer maxParticipants) {
    this.maxParticipants = maxParticipants;
  }

  public void setMinTeamSize(Integer minTeamSize) {
    this.minTeamSize = minTeamSize;
  }

  public void setMaxTeamSize(Integer maxTeamSize) {
    this.maxTeamSize = maxTeamSize;
  }

  public void setRegistrationDeadline(Instant registrationDeadline) {
    this.registrationDeadline = registrationDeadline;
  }

  public void setCreatedBy(Long createdBy) {
    this.createdBy = createdBy;
  }
}
