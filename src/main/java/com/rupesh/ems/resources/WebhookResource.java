package com.rupesh.ems.resources;

import com.rupesh.ems.service.PaymentService;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/webhooks/razorpay")
@Consumes(MediaType.APPLICATION_JSON)
public class WebhookResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebhookResource.class);

  private final PaymentService paymentService;

  public WebhookResource(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @POST
  @UnitOfWork
  public void handleWebhook(String payload, @HeaderParam("X-Razorpay-Signature") String signature) {

    paymentService.handleWebhook(payload, signature);
  }
}
