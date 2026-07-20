package com.rupesh.ems.resources;

import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.api.common.MessageResponse;
import com.rupesh.ems.api.team.req.CreateTeamRequest;
import com.rupesh.ems.api.team.req.InviteUserRequest;
import com.rupesh.ems.api.team.req.RespondToRequestRequest;
import com.rupesh.ems.api.team.req.UpdateTeamRequest;
import com.rupesh.ems.api.team.res.TeamMembershipResponse;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.EventTeamRequestService;
import com.rupesh.ems.service.EventTeamService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/events/{eventId}/teams")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class EventTeamResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventTeamResource.class);

  private final EventTeamService eventTeamService;
  private final EventTeamRequestService eventTeamRequestService;

  public EventTeamResource(
      EventTeamService eventTeamService, EventTeamRequestService eventTeamRequestService) {
    this.eventTeamService = eventTeamService;
    this.eventTeamRequestService = eventTeamRequestService;
  }

  @POST
  @UnitOfWork
  public TeamResponse createTeam(
      @PathParam("eventId") Long eventId,
      @Valid CreateTeamRequest request,
      @Auth UserPrincipal user) {
    LOGGER.info(
        "Creating team={} for eventId={} by userId={}", request.getName(), eventId, user.getId());
    return eventTeamService.createTeam(eventId, request, user);
  }

  @GET
  @UnitOfWork
  public List<TeamResponse> getTeamsForEvent(@PathParam("eventId") Long eventId) {
    LOGGER.info("Fetching teams for eventId={}", eventId);
    return eventTeamService.getTeamsForEvent(eventId);
  }

  @GET
  @Path("/{teamId}")
  @UnitOfWork
  public TeamResponse getTeamById(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    LOGGER.info("Fetching teamId={} for eventId={} by userId={}", teamId, eventId, user.getId());
    return eventTeamService.getTeamById(eventId, teamId, user);
  }

  @PUT
  @Path("/{teamId}")
  @UnitOfWork
  public TeamResponse updateTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Valid UpdateTeamRequest request,
      @Auth UserPrincipal user) {
    LOGGER.info("Updating teamId={} for eventId={} by userId={}", teamId, eventId, user.getId());
    return eventTeamService.updateTeam(eventId, teamId, request, user);
  }

  @DELETE
  @Path("/{teamId}")
  @UnitOfWork
  public MessageResponse deleteTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    LOGGER.info("Deleting teamId={} for eventId={} by userId={}", teamId, eventId, user.getId());
    eventTeamService.deleteTeam(eventId, teamId, user);
    return new MessageResponse("Team deleted successfully");
  }

  @GET
  @Path("/{teamId}/members")
  @UnitOfWork
  public List<UserResponse> getTeamMembers(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    LOGGER.info(
        "Fetching members for teamId={} of eventId={} by userId={}", teamId, eventId, user.getId());
    return eventTeamService.getTeamMembers(eventId, teamId, user);
  }

  @DELETE
  @Path("/{teamId}/members/{userId}")
  @UnitOfWork
  public MessageResponse removeUserFromTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal user) {
    LOGGER.info(
        "Removing userId={} from teamId={} of eventId={} by userId={}",
        userId,
        teamId,
        eventId,
        user.getId());
    eventTeamService.removeUserFromTeam(eventId, teamId, userId, user);
    return new MessageResponse("User removed from team successfully");
  }

  @PUT
  @Path("/{teamId}/owner/{userId}")
  @UnitOfWork
  public TeamResponse transferOwnership(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal user) {
    LOGGER.info(
        "Transferring ownership of teamId={} for eventId={} to newOwnerId={} by userId={}",
        teamId,
        eventId,
        userId,
        user.getId());
    return eventTeamService.transferOwnership(eventId, teamId, userId, user);
  }

  @POST
  @Path("/{teamId}/requests")
  @UnitOfWork
  public TeamMembershipResponse requestToJoinTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    LOGGER.info(
        "UserId={} requesting to join teamId={} for eventId={}", user.getId(), teamId, eventId);
    return eventTeamRequestService.requestToJoinTeam(eventId, teamId, user);
  }

  @DELETE
  @Path("/{teamId}/requests")
  @UnitOfWork
  public MessageResponse deleteRequest(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    LOGGER.info(
        "UserId={} deleting request to join teamId={} for eventId={}",
        user.getId(),
        teamId,
        eventId);
    eventTeamRequestService.deleteRequest(eventId, teamId, user);
    return new MessageResponse("Request deleted successfully");
  }

  @GET
  @Path("/{teamId}/requests")
  @UnitOfWork
  public List<TeamMembershipResponse> getPendingRequestsForTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    LOGGER.info(
        "Fetching pending requests for teamId={} of eventId={} by userId={}",
        teamId,
        eventId,
        user.getId());
    return eventTeamRequestService.getPendingRequestsForTeam(eventId, teamId, user);
  }

  @PUT
  @Path("/{teamId}/requests/{userId}")
  @UnitOfWork
  public TeamMembershipResponse respondToRequest(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Valid RespondToRequestRequest request,
      @Auth UserPrincipal user) {
    LOGGER.info(
        "UserId={} responding to request of userId={} for teamId={} of eventId={}",
        user.getId(),
        userId,
        teamId,
        eventId);
    return eventTeamRequestService.respondToRequest(eventId, teamId, userId, request, user);
  }

  @POST
  @Path("/{teamId}/invitations")
  @UnitOfWork
  public TeamMembershipResponse inviteUserToTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Valid InviteUserRequest request,
      @Auth UserPrincipal user) {
    return eventTeamRequestService.inviteUserToTeam(eventId, teamId, request.getEmail(), user);
  }

  @DELETE
  @Path("/{teamId}/invitations/{userId}")
  @UnitOfWork
  public MessageResponse deleteInvitation(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal user) {
    eventTeamRequestService.deleteInvitation(eventId, teamId, userId, user);
    return new MessageResponse("Invitation deleted successfully");
  }

  @PUT
  @Path("/{teamId}/invitations")
  @UnitOfWork
  public TeamMembershipResponse respondToInvitation(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Valid RespondToRequestRequest request,
      @Auth UserPrincipal user) {
    return eventTeamRequestService.respondToInvitation(eventId, teamId, request, user);
  }
}
