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
import com.rupesh.ems.exceptions.ForbiddenException;
import com.rupesh.ems.exceptions.InternalServerException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

  private static final String EVENT_PAYMENT_CAPTURED = "payment.captured";
  private static final String EVENT_PAYMENT_FAILED = "payment.failed";

  private final PaymentDao paymentDao;
  private final EventRegistrationDao registrationDao;
  private final EventDao eventDao;
  private final RazorpayClient razorpayClient;
  private final String razorpayKeyId;
  private final String razorpayKeySecret;
  private final String webhookSecret;

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
    this.webhookSecret = razorpayConfig.getWebhookSecret();

    try {
      this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    } catch (RazorpayException e) {
      throw new RuntimeException("Failed to initialize Razorpay client", e);
    }
  }

  public CreateOrderResponse createOrder(Long registrationId) {
    LOGGER.info("Creating order for registration {}", registrationId);
    EventRegistration registration =
        registrationDao
            .getById(registrationId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("Registration not found for ID: {}", registrationId);
                  throw new NotFoundException("Registration not found");
                });

    if (registration.getStatus() == RegistrationStatus.CANCELLED) {
      LOGGER.warn("Cannot create order for cancelled registration ID: {}", registrationId);
      throw new BadRequestException("Cannot pay for a cancelled registration");
    }

    paymentDao
        .findByRegistrationId(registrationId)
        .ifPresent(
            existing -> {
              if (existing.getStatus() == PaymentStatus.COMPLETED) {
                LOGGER.warn("Payment already completed for registration ID: {}", registrationId);
                throw new ConflictException("Payment already completed for this registration");
              }
            });

    Event event =
        eventDao
            .getEventById(registration.getEventId())
            .orElseThrow(
                () -> {
                  LOGGER.warn("Event not found for registration ID: {}", registrationId);
                  throw new NotFoundException("Event not found");
                });

    BigDecimal fee = event.getRegistrationFee();

    if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
      LOGGER.warn("Event {} does not require payment", event.getId());
      registration.setStatus(RegistrationStatus.REGISTERED);
      registrationDao.update(registration);
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
      LOGGER.info(
          "Razorpay order created with ID: {} for registration {}",
          razorpayOrderId,
          registrationId);
      Payment payment = new Payment(registrationId, fee, razorpayOrderId);
      payment = paymentDao.create(payment);
      LOGGER.info(
          "Payment record created with ID: {} for registration {}",
          payment.getId(),
          registrationId);
      return new CreateOrderResponse(payment.getId(), razorpayOrderId, fee, "INR", razorpayKeyId);

    } catch (RazorpayException e) {
      LOGGER.error(
          "Failed to create Razorpay order for registration {}: {}",
          registrationId,
          e.getMessage());
      throw new InternalServerException("Failed to create Razorpay order: " + e.getMessage());
    }
  }

  public PaymentResponse verifyAndCompletePayment(
      String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
    LOGGER.info(
        "Verifying payment for order {} and payment {}", razorpayOrderId, razorpayPaymentId);
    Payment payment =
        paymentDao
            .findByProviderOrderId(razorpayOrderId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("Payment not found for Razorpay order ID: {}", razorpayOrderId);
                  throw new NotFoundException("Payment not found");
                });

    if (payment.getStatus() == PaymentStatus.COMPLETED) {
      LOGGER.warn("Payment already completed for Razorpay order ID: {}", razorpayOrderId);
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
        LOGGER.warn(
            "Payment signature verification failed for registration ID: {}",
            payment.getRegistrationId());
        paymentDao.update(payment);
        throw new BadRequestException("Payment signature verification failed");
      }
    } catch (RazorpayException e) {
      payment.setStatus(PaymentStatus.FAILED);
      LOGGER.error(
          "Failed to verify payment for registration ID: {}", payment.getRegistrationId(), e);
      paymentDao.update(payment);
      throw new BadRequestException("Payment verification failed: " + e.getMessage());
    }

    payment = completePayment(payment, razorpayPaymentId);
    LOGGER.info("Payment completed for registration ID: {}", payment.getRegistrationId());
    return new PaymentResponse(payment);
  }

  public PaymentResponse failPayment(String razorpayOrderId) {
    LOGGER.info("Marking payment as failed for order {}", razorpayOrderId);
    Payment payment =
        paymentDao
            .findByProviderOrderId(razorpayOrderId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("Payment not found for Razorpay order ID: {}", razorpayOrderId);
                  throw new NotFoundException("Payment not found");
                });

    payment.setStatus(PaymentStatus.FAILED);
    payment = paymentDao.update(payment);

    LOGGER.info("Payment marked as failed for order {}", razorpayOrderId);
    return new PaymentResponse(payment);
  }

  public PaymentResponse getPaymentByRegistrationId(Long registrationId) {
    LOGGER.info("Fetching payment information for registration {}", registrationId);
    Payment payment =
        paymentDao
            .findByRegistrationId(registrationId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("Payment not found for registration ID: {}", registrationId);
                  throw new NotFoundException("Payment not found for this registration");
                });

    LOGGER.info("Payment information fetched for registration {}", registrationId);
    return new PaymentResponse(payment);
  }

  public void handleWebhook(String payload, String signature) {
    LOGGER.info("Received webhook with payload: {}", payload);
    LOGGER.info("Webhook signature: {}", signature);
    if (signature == null || signature.isBlank()) {
      LOGGER.warn("Missing webhook signature");
      throw new ForbiddenException("Invalid webhook signature");
    }

    try {
      Utils.verifyWebhookSignature(payload, signature, webhookSecret);
    } catch (RazorpayException e) {
      LOGGER.warn("Invalid webhook signature");
      throw new ForbiddenException("Invalid webhook signature");
    }

    JSONObject event = new JSONObject(payload);

    switch (event.getString("event")) {
      case EVENT_PAYMENT_CAPTURED -> handlePaymentCaptured(event);

      case EVENT_PAYMENT_FAILED -> handlePaymentFailed(event);

      default -> {
        LOGGER.info("Ignoring unsupported event type: {}", event.getString("event"));
        // Ignore unsupported events
      }
    }
  }

  private void handlePaymentCaptured(JSONObject event) {
    LOGGER.info("Handling payment captured event");
    JSONObject paymentEntity = extractPaymentEntity(event);
    String orderId = paymentEntity.getString("order_id");
    String paymentId = paymentEntity.getString("id");

    Payment payment = findPaymentByProviderOrderId(orderId);

    if (payment.getStatus() == PaymentStatus.COMPLETED) {
      LOGGER.info("Payment already completed for order {}", orderId);
      return;
    }
    completePayment(payment, paymentId);
  }

  private void handlePaymentFailed(JSONObject event) {
    LOGGER.info("Handling payment failed event");
    JSONObject paymentEntity = extractPaymentEntity(event);
    String orderId = paymentEntity.getString("order_id");

    Payment payment = findPaymentByProviderOrderId(orderId);

    LOGGER.info("Payment failed for order {}", orderId);
    if (payment.getStatus() == PaymentStatus.COMPLETED) {
      LOGGER.info("Payment already completed for order {}", orderId);
      return;
    }

    LOGGER.info("Updating payment status for order {}", orderId);
    payment.setStatus(PaymentStatus.FAILED);
    paymentDao.update(payment);
  }

  private Payment completePayment(Payment payment, String providerPaymentId) {
    LOGGER.info("Completing payment for registration {}", payment.getRegistrationId());
    payment.setProviderPaymentId(providerPaymentId);
    payment.setStatus(PaymentStatus.COMPLETED);
    payment.setPaidAt(Instant.now());

    Payment updatedPayment = paymentDao.update(payment);
    LOGGER.info("Payment completed for registration {}", updatedPayment.getRegistrationId());
    EventRegistration registration =
        registrationDao
            .getById(updatedPayment.getRegistrationId())
            .orElseThrow(
                () -> {
                  LOGGER.warn(
                      "Registration not found for ID: {}", updatedPayment.getRegistrationId());
                  throw new NotFoundException("Registration not found");
                });

    registration.setStatus(RegistrationStatus.REGISTERED);
    registrationDao.update(registration);
    LOGGER.info(
        "Registration status updated to REGISTERED for registration {}",
        updatedPayment.getRegistrationId());
    return updatedPayment;
  }

  private Payment findPaymentByProviderOrderId(String orderId) {
    LOGGER.info("Finding payment by provider order ID: {}", orderId);
    return paymentDao
        .findByProviderOrderId(orderId)
        .orElseThrow(() -> new NotFoundException("Payment not found"));
  }

  private JSONObject extractPaymentEntity(JSONObject event) {
    LOGGER.info("Extracting payment entity from event");
    return event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
  }
}
