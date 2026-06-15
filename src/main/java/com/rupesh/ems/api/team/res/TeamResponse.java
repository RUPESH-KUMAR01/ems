package com.rupesh.ems.api.team.res;

import com.rupesh.ems.core.Team;
import java.time.Instant;

public class TeamResponse {

  private Long id;
  private String name;
  private Long ownerId;
  private Integer maxMembers;
  private Instant createdAt;
  private Instant updatedAt;

  public TeamResponse() {}

  public TeamResponse(
      Long id,
      String name,
      Long ownerId,
      Integer maxMembers,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.ownerId = ownerId;
    this.maxMembers = maxMembers;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public TeamResponse(Team team) {
    this.id = team.getId();
    this.name = team.getName();
    this.ownerId = team.getOwnerId();
    this.maxMembers = team.getMaxMembers();
    this.createdAt = team.getCreatedAt();
    this.updatedAt = team.getUpdatedAt();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(Long ownerId) {
    this.ownerId = ownerId;
  }

  public Integer getMaxMembers() {
    return maxMembers;
  }

  public void setMaxMembers(Integer maxMembers) {
    this.maxMembers = maxMembers;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
