package com.rupesh.ems.configs;

import jakarta.validation.constraints.NotBlank;

public class JWTConfig {

  @NotBlank private String secret;

  private Long expireInMilliSec;

  public JWTConfig() {}

  public JWTConfig(String secret, Long expireInMilliSec) {
    this.secret = secret;
    this.expireInMilliSec = expireInMilliSec;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public Long getExpireInMilliSec() {
    return expireInMilliSec;
  }

  public void setExpireInMilliSec(Long expireInMilliSec) {
    this.expireInMilliSec = expireInMilliSec;
  }
}
