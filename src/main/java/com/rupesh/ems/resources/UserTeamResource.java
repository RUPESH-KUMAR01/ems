package com.rupesh.ems.resources;

import com.rupesh.ems.api.team.res.TeamMembershipResponse;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.EventTeamRequestService;
import com.rupesh.ems.service.EventTeamService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class UserTeamResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserTeamResource.class);

  private final EventTeamService eventTeamService;
  private final EventTeamRequestService eventTeamRequestService;

  public UserTeamResource(
      EventTeamService eventTeamService, EventTeamRequestService eventTeamRequestService) {
    this.eventTeamService = eventTeamService;
    this.eventTeamRequestService = eventTeamRequestService;
  }

  @GET
  @Path("/teams/me")
  @UnitOfWork
  public List<TeamResponse> getTeamsForUser(@Auth UserPrincipal user) {
    LOGGER.info("Fetching teams for userId={}", user.getId());
    return eventTeamService.getTeamsForUser(user);
  }

  @GET
  @Path("/team-requests/me")
  @UnitOfWork
  public List<TeamMembershipResponse> getPendingRequestsForUser(@Auth UserPrincipal user) {
    return eventTeamRequestService.getPendingRequestsForUser(user);
  }
}
