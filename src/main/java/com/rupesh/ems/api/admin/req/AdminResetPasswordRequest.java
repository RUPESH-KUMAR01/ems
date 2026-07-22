package com.rupesh.ems.api.admin.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminResetPasswordRequest {

  @NotBlank
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String newPassword;

  public AdminResetPasswordRequest() {}

  public AdminResetPasswordRequest(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
}
