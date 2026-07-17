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

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class PaymentResource {

  private final PaymentService paymentService;

  public PaymentResource(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @POST
  @Path("/orders")
  @UnitOfWork
  public CreateOrderResponse createOrder(
      @Valid CreateOrderRequest request, @Auth UserPrincipal user) {

    return paymentService.createOrder(request.getRegistrationId());
  }

  @POST
  @Path("/verify")
  @UnitOfWork
  public PaymentResponse verifyPayment(
      @Valid VerifyPaymentRequest request, @Auth UserPrincipal user) {

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

    return paymentService.failPayment(razorpayOrderId);
  }

  @GET
  @Path("/registration/{registrationId}")
  @UnitOfWork
  public PaymentResponse getPaymentByRegistration(
      @PathParam("registrationId") Long registrationId, @Auth UserPrincipal user) {

    return paymentService.getPaymentByRegistrationId(registrationId);
  }
}
