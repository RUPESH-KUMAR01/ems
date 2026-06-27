package com.rupesh.ems.configs;

import jakarta.validation.constraints.NotBlank;

public class RazorpayConfig {

  @NotBlank private String keyId;

  @NotBlank private String keySecret;

  public RazorpayConfig() {}

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }

  public String getKeySecret() {
    return keySecret;
  }

  public void setKeySecret(String keySecret) {
    this.keySecret = keySecret;
  }
}
