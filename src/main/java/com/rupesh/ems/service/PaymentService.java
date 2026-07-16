package com.rupesh.ems.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.rupesh.ems.api.payment.res.CreateOrderResponse;
import com.rupesh.ems.api.payment.res.PaymentResponse;
import com.rupesh.ems.configs.RazorpayConfig;
import com.rupesh.ems.core.Event;
import com.rupesh.ems.core.EventRegistration;
import com.rupesh.ems.core.Payment;
import com.rupesh.ems.core.PaymentStatus;
import com.rupesh.ems.core.RegistrationStatus;
import com.rupesh.ems.db.EventDao;
import com.rupesh.ems.db.EventRegistrationDao;
import com.rupesh.ems.db.PaymentDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.InternalServerException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import org.json.JSONObject;

public class PaymentService {

  private final PaymentDao paymentDao;
  private final EventRegistrationDao registrationDao;
  private final EventDao eventDao;
  private final RazorpayClient razorpayClient;
  private final String razorpayKeyId;
  private final String razorpayKeySecret;

  public PaymentService(
      PaymentDao paymentDao,
      EventRegistrationDao registrationDao,
      EventDao eventDao,
      RazorpayConfig razorpayConfig) {
    this.paymentDao = paymentDao;
    this.registrationDao = registrationDao;
    this.eventDao = eventDao;
    this.razorpayKeyId = razorpayConfig.getKeyId();
    this.razorpayKeySecret = razorpayConfig.getKeySecret();

    try {
      this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    } catch (RazorpayException e) {
      throw new RuntimeException("Failed to initialize Razorpay client", e);
    }
  }

  public CreateOrderResponse createOrder(Long registrationId) {

    EventRegistration registration =
        registrationDao
            .getById(registrationId)
            .orElseThrow(() -> new NotFoundException("Registration not found"));

    if (registration.getStatus() == RegistrationStatus.CANCELLED) {
      throw new BadRequestException("Cannot pay for a cancelled registration");
    }

    paymentDao
        .findByRegistrationId(registrationId)
        .ifPresent(
            existing -> {
              if (existing.getStatus() == PaymentStatus.COMPLETED) {
                throw new ConflictException("Payment already completed for this registration");
              }
            });

    Event event =
        eventDao
            .getEventById(registration.getEventId())
            .orElseThrow(() -> new NotFoundException("Event not found"));

    BigDecimal fee = event.getRegistrationFee();

    if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException("This event does not require payment");
    }

    // Razorpay expects amount in paise (smallest currency unit)
    int amountInPaise = fee.multiply(new BigDecimal("100")).intValue();

    try {
      JSONObject orderRequest = new JSONObject();
      orderRequest.put("amount", amountInPaise);
      orderRequest.put("currency", "INR");
      orderRequest.put("receipt", "reg_" + registrationId);

      Order razorpayOrder = razorpayClient.orders.create(orderRequest);

      String razorpayOrderId = razorpayOrder.get("id");

      Payment payment = new Payment(registrationId, fee, razorpayOrderId);
      payment = paymentDao.create(payment);

      return new CreateOrderResponse(
          payment.getId(), razorpayOrderId, fee, "INR", razorpayKeyId);

    } catch (RazorpayException e) {
      throw new InternalServerException("Failed to create Razorpay order: " + e.getMessage());
    }
  }

  public PaymentResponse verifyAndCompletePayment(
      String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {

    Payment payment =
        paymentDao
            .findByProviderOrderId(razorpayOrderId)
            .orElseThrow(() -> new NotFoundException("Payment not found for this order"));

    if (payment.getStatus() == PaymentStatus.COMPLETED) {
      throw new ConflictException("Payment already completed");
    }

    // Verify signature using Razorpay Utils
    try {
      JSONObject attributes = new JSONObject();
      attributes.put("razorpay_order_id", razorpayOrderId);
      attributes.put("razorpay_payment_id", razorpayPaymentId);
      attributes.put("razorpay_signature", razorpaySignature);

      boolean isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

      if (!isValid) {
        payment.setStatus(PaymentStatus.FAILED);
        paymentDao.update(payment);
        throw new BadRequestException("Payment signature verification failed");
      }
    } catch (RazorpayException e) {
      payment.setStatus(PaymentStatus.FAILED);
      paymentDao.update(payment);
      throw new BadRequestException("Payment verification failed: " + e.getMessage());
    }

    // Mark payment as completed
    payment.setProviderPaymentId(razorpayPaymentId);
    payment.setStatus(PaymentStatus.COMPLETED);
    payment.setPaidAt(Instant.now());
    payment = paymentDao.update(payment);

    // Update registration status to REGISTERED
    registrationDao
        .getById(payment.getRegistrationId())
        .ifPresent(
            registration -> {
              registration.setStatus(RegistrationStatus.REGISTERED);
              registrationDao.update(registration);
            });

    return new PaymentResponse(payment);
  }

  public PaymentResponse failPayment(String razorpayOrderId) {

    Payment payment =
        paymentDao
            .findByProviderOrderId(razorpayOrderId)
            .orElseThrow(() -> new NotFoundException("Payment not found"));

    payment.setStatus(PaymentStatus.FAILED);
    payment = paymentDao.update(payment);

    return new PaymentResponse(payment);
  }

  public PaymentResponse getPaymentByRegistrationId(Long registrationId) {

    Payment payment =
        paymentDao
            .findByRegistrationId(registrationId)
            .orElseThrow(() -> new NotFoundException("Payment not found for this registration"));

    return new PaymentResponse(payment);
  }
}