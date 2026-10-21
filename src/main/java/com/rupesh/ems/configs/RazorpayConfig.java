package com.rupesh.ems.configs;

import jakarta.validation.constraints.NotBlank;

public class RazorpayConfig {

  /** When true, orders and verify skip the Razorpay API (local dev without API keys). */
  private boolean mockPayments = false;

  @NotBlank private String keyId = "rzp_test_unused";

  @NotBlank private String keySecret = "unused";

  @NotBlank private String webhookSecret = "local-dev-webhook";

  public RazorpayConfig() {}

  public boolean isMockPayments() {
    return mockPayments;
  }

  public void setMockPayments(boolean mockPayments) {
    this.mockPayments = mockPayments;
  }

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(String keyId) {
    this.keyId = keyId == null ? null : keyId.trim();
  }

  public String getKeySecret() {
    return keySecret;
  }

  public void setKeySecret(String keySecret) {
    this.keySecret = keySecret == null ? null : keySecret.trim();
  }

  public String getWebhookSecret() {
    return webhookSecret;
  }

  public void setWebhookSecret(String webhookSecret) {
    this.webhookSecret = webhookSecret == null ? null : webhookSecret.trim();
  }
}
