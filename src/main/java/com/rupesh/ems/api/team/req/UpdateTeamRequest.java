package com.rupesh.ems.api.team.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public class UpdateTeamRequest {
  @NotBlank private String name;

  @Min(1)
  private Integer maxMembers;

  public UpdateTeamRequest() {}

  public UpdateTeamRequest(String name) {
    this.name = name;
  }

  public UpdateTeamRequest(String name, Integer maxMembers) {
    this.name = name;
    this.maxMembers = maxMembers;
  }

  public String getName() {
    return name;
  }

  public Integer getMaxMembers() {
    return maxMembers;
  }
}
