package com.rupesh.ems.api.auth.res;

public class RegisterResponse {
  private String token;

  public RegisterResponse(String token) {
    this.token = token;
  }

  public RegisterResponse() {}

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
