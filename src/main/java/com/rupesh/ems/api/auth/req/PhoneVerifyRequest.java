package com.rupesh.ems.api.auth.req;

public class PhoneVerifyRequest {
  private String otp;

  public PhoneVerifyRequest() {}

  public PhoneVerifyRequest(String otp) {
    this.otp = otp;
  }

  public String getOtp() {
    return otp;
  }
}
