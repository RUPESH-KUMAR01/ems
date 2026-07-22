package com.rupesh.ems.api.admin.req;

import jakarta.validation.constraints.NotNull;

public class AdminUpdateVerificationRequest {

  @NotNull private Boolean emailVerified;

  @NotNull private Boolean phoneVerified;

  public AdminUpdateVerificationRequest() {}

  public AdminUpdateVerificationRequest(Boolean emailVerified, Boolean phoneVerified) {
    this.emailVerified = emailVerified;
    this.phoneVerified = phoneVerified;
  }

  public Boolean getEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(Boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public Boolean getPhoneVerified() {
    return phoneVerified;
  }

  public void setPhoneVerified(Boolean phoneVerified) {
    this.phoneVerified = phoneVerified;
  }
}
