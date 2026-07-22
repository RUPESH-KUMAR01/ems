package com.rupesh.ems.resources;

import com.rupesh.ems.api.payment.req.CreateOrderRequest;
import com.rupesh.ems.api.payment.req.VerifyPaymentRequest;
import com.rupesh.ems.api.payment.res.CreateOrderResponse;
import com.rupesh.ems.api.payment.res.PaymentResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.PaymentService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class PaymentResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResource.class);

  private final PaymentService paymentService;

  public PaymentResource(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @POST
  @Path("/orders")
  @UnitOfWork
  public CreateOrderResponse createOrder(
      @Valid CreateOrderRequest request, @Auth UserPrincipal user) {
    LOGGER.info("Creating order for registration {}", request.getRegistrationId());
    return paymentService.createOrder(request.getRegistrationId());
  }

  @POST
  @Path("/verify")
  @UnitOfWork
  public PaymentResponse verifyPayment(
      @Valid VerifyPaymentRequest request, @Auth UserPrincipal user) {
    LOGGER.info(
        "Verifying payment for order {} and payment {}",
        request.getRazorpayOrderId(),
        request.getRazorpayPaymentId());
    return paymentService.verifyAndCompletePayment(
        request.getRazorpayOrderId(),
        request.getRazorpayPaymentId(),
        request.getRazorpaySignature());
  }

  @POST
  @Path("/fail/{razorpayOrderId}")
  @UnitOfWork
  public PaymentResponse failPayment(
      @PathParam("razorpayOrderId") String razorpayOrderId, @Auth UserPrincipal user) {
    LOGGER.info("Marking payment as failed for order {}", razorpayOrderId);
    return paymentService.failPayment(razorpayOrderId);
  }

  @GET
  @Path("/registration/{registrationId}")
  @UnitOfWork
  public PaymentResponse getPaymentByRegistration(
      @PathParam("registrationId") Long registrationId, @Auth UserPrincipal user) {
    LOGGER.info("Fetching payment information for registration {}", registrationId);
    return paymentService.getPaymentByRegistrationId(registrationId);
  }
}
