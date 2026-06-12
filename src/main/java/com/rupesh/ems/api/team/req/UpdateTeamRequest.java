package com.rupesh.ems.api.team.req;

import jakarta.validation.constraints.NotBlank;

public class UpdateTeamRequest {
  @NotBlank private String name;

  public UpdateTeamRequest() {}

  public UpdateTeamRequest(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
