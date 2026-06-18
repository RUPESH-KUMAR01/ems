package com.rupesh.ems.api.auth.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

  @NotBlank private String name;

  @NotBlank @Email private String email;

  @NotBlank
  @Size(min = 6)
  private String password;

  @Pattern(
    regexp = "^\\d{10}$",
  message = "Phone number must contain exactly 10 digits")
  @NotBlank private String phone;

  public CreateUserRequest() {}

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
}
