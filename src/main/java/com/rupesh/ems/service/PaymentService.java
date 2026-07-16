package com.rupesh.ems.service;

import com.rupesh.ems.core.Payment;
import com.rupesh.ems.core.PaymentStatus;
import com.rupesh.ems.db.EventRegistrationDao;
import com.rupesh.ems.db.PaymentDao;
import com.rupesh.ems.exceptions.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;

public class PaymentService {

  private final PaymentDao paymentDao;
  private final EventRegistrationDao registrationDao;

  public PaymentService(
      PaymentDao paymentDao,
      EventRegistrationDao registrationDao) {
    this.paymentDao = paymentDao;
    this.registrationDao = registrationDao;
  }

  public Payment createPayment(
      Long registrationId,
      BigDecimal amount,
      String razorpayOrderId) {

    registrationDao.getById(registrationId)
        .orElseThrow(() -> new NotFoundException("Registration not found"));

    Payment payment =
        new Payment(
            registrationId,
            amount,
            razorpayOrderId);

    return paymentDao.create(payment);
  }

  public Payment completePayment(
      String razorpayOrderId,
      String razorpayPaymentId) {

    Payment payment =
        paymentDao.findByProviderOrderId(razorpayOrderId)
            .orElseThrow(() -> new NotFoundException("Payment not found"));

    payment.setProviderPaymentId(razorpayPaymentId);
    payment.setStatus(PaymentStatus.COMPLETED);
    payment.setPaidAt(Instant.now());

    return paymentDao.update(payment);
  }

  public Payment failPayment(String razorpayOrderId) {

    Payment payment =
        paymentDao.findByProviderOrderId(razorpayOrderId)
            .orElseThrow(() -> new NotFoundException("Payment not found"));

    payment.setStatus(PaymentStatus.FAILED);

    return paymentDao.update(payment);
  }
}