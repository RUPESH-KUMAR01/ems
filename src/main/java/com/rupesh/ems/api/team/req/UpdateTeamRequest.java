package com.rupesh.ems.api.team.req;

public class UpdateTeamRequest {
  private String name;

  public UpdateTeamRequest() {}

  public UpdateTeamRequest(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
