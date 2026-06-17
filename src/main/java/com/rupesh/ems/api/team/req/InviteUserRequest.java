package com.rupesh.ems.api.team.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class InviteUserRequest {

  @NotBlank @Email private String email;

  public InviteUserRequest() {}

  public String getEmail() {
    return email;
  }
}
