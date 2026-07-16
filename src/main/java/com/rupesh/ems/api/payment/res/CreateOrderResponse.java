package com.rupesh.ems.api.payment.res;

import java.math.BigDecimal;

public class CreateOrderResponse {

  private final Long paymentId;
  private final String razorpayOrderId;
  private final BigDecimal amount;
  private final String currency;
  private final String razorpayKeyId;

  public CreateOrderResponse(
      Long paymentId,
      String razorpayOrderId,
      BigDecimal amount,
      String currency,
      String razorpayKeyId) {
    this.paymentId = paymentId;
    this.razorpayOrderId = razorpayOrderId;
    this.amount = amount;
    this.currency = currency;
    this.razorpayKeyId = razorpayKeyId;
  }

  public Long getPaymentId() {
    return paymentId;
  }

  public String getRazorpayOrderId() {
    return razorpayOrderId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getRazorpayKeyId() {
    return razorpayKeyId;
  }
}
