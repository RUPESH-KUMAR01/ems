package com.rupesh.ems.resources;

import com.rupesh.ems.api.registration.res.EventRegistrationResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.EventRegistrationService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/registrations")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class UserRegistrationsResource {

  private final EventRegistrationService registrationService;

  public UserRegistrationsResource(EventRegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @GET
  @Path("/mine")
  @UnitOfWork
  public List<EventRegistrationResponse> listMine(@Auth UserPrincipal user) {
    return registrationService.listForUser(user.getId());
  }
}
