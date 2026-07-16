package com.rupesh.ems.resources;

import com.rupesh.ems.api.registration.req.RegisterEventRequest;
import com.rupesh.ems.api.registration.res.EventRegistrationResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.EventRegistrationService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/events/{eventId}/registrations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class EventRegistrationResource {

  private final EventRegistrationService registrationService;

  public EventRegistrationResource(EventRegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @POST
  @UnitOfWork
  public EventRegistrationResponse register(
      @PathParam("eventId") Long eventId,
      @Valid RegisterEventRequest request,
      @Auth UserPrincipal user) {

    return registrationService.register(eventId, request.getTeamId(), user);
  }

  @DELETE
  @Path("/{registrationId}")
  @UnitOfWork
  public void cancelRegistration(
      @PathParam("registrationId") Long registrationId, @Auth UserPrincipal user) {

    registrationService.cancel(registrationId);
  }
}
