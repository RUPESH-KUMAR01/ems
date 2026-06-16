package com.rupesh.ems.api.team.req;

public class UpdateTeamRequest {
  private String name;

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
