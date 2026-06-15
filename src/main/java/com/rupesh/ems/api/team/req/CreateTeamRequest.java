package com.rupesh.ems.api.team.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateTeamRequest {
  @NotBlank private String name;

  @Min(1)
  private Integer maxMembers = 1;

  public CreateTeamRequest(String name) {
    this.name = name;
  }

  public CreateTeamRequest(String name, Integer maxMembers) {
    this.name = name;
    this.maxMembers = maxMembers;
  }

  public CreateTeamRequest() {}

  public String getName() {
    return name;
  }

  public Integer getMaxMembers() {
    return maxMembers;
  }
}
