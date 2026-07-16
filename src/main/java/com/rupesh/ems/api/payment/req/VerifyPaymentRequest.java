package com.rupesh.ems.api.payment.req;

import jakarta.validation.constraints.NotBlank;

public class VerifyPaymentRequest {

  @NotBlank(message = "Razorpay order id is required")
  private String razorpayOrderId;

  @NotBlank(message = "Razorpay payment id is required")
  private String razorpayPaymentId;

  @NotBlank(message = "Razorpay signature is required")
  private String razorpaySignature;

  public VerifyPaymentRequest() {}

  public String getRazorpayOrderId() {
    return razorpayOrderId;
  }

  public void setRazorpayOrderId(String razorpayOrderId) {
    this.razorpayOrderId = razorpayOrderId;
  }

  public String getRazorpayPaymentId() {
    return razorpayPaymentId;
  }

  public void setRazorpayPaymentId(String razorpayPaymentId) {
    this.razorpayPaymentId = razorpayPaymentId;
  }

  public String getRazorpaySignature() {
    return razorpaySignature;
  }

  public void setRazorpaySignature(String razorpaySignature) {
    this.razorpaySignature = razorpaySignature;
  }
}
