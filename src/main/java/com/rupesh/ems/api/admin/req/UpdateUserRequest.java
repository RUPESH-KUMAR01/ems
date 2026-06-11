package com.rupesh.ems.api.admin.req;

import com.rupesh.ems.core.Role;

public class UpdateUserRequest {

  private String name;
  private String email;
  private String phone;
  private Boolean emailVerified;
  private Boolean phoneVerified;
  private Role role;

  public UpdateUserRequest() {}

  public UpdateUserRequest(
      String name,
      String email,
      String phone,
      Boolean emailVerified,
      Boolean phoneVerified,
      Role role) {
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.emailVerified = emailVerified;
    this.phoneVerified = phoneVerified;
    this.role = role;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
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

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }
}
