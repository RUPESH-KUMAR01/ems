package com.rupesh.ems.api.team.req;

import jakarta.validation.constraints.NotBlank;

public class CreateTeamRequest {
  @NotBlank private String name;


  public CreateTeamRequest(String name) {
    this.name = name;
  }

  public CreateTeamRequest(String name, Integer maxMembers) {
    this.name = name;
  }

  public CreateTeamRequest() {}

  public String getName() {
    return name;
  }
}
