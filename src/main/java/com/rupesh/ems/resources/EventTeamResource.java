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

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class EventTeamResource {
  private final EventTeamService eventTeamService;
  private final EventTeamRequestService eventTeamRequestService;

  public EventTeamResource(
      EventTeamService eventTeamService, EventTeamRequestService eventTeamRequestService) {
    this.eventTeamService = eventTeamService;
    this.eventTeamRequestService = eventTeamRequestService;
  }

  @POST
  @Path("/events/{eventId}/teams")
  @UnitOfWork
  public TeamResponse createTeam(
      @PathParam("eventId") Long eventId,
      @Valid CreateTeamRequest request,
      @Auth UserPrincipal user) {
    return eventTeamService.createTeam(eventId, request, user);
  }

  @GET
  @Path("/events/{eventId}/teams")
  @UnitOfWork
  public List<TeamResponse> getTeamsForEvent(@PathParam("eventId") Long eventId) {
    return eventTeamService.getTeamsForEvent(eventId);
  }

  @GET
  @Path("/teams/me")
  @UnitOfWork
  public List<TeamResponse> getTeamsForUser(@Auth UserPrincipal user) {
    return eventTeamService.getTeamsForUser(user);
  }

  @GET
  @Path("/events/{eventId}/teams/{teamId}")
  @UnitOfWork
  public TeamResponse getTeamById(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    return eventTeamService.getTeamById(eventId, teamId, user);
  }

  @PUT
  @Path("/events/{eventId}/teams/{teamId}")
  @UnitOfWork
  public TeamResponse updateTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Valid UpdateTeamRequest request,
      @Auth UserPrincipal user) {
    return eventTeamService.updateTeam(eventId, teamId, request, user);
  }

  @DELETE
  @Path("/events/{eventId}/teams/{teamId}")
  @UnitOfWork
  public MessageResponse deleteTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    eventTeamService.deleteTeam(eventId, teamId, user);
    return new MessageResponse("Team deleted successfully");
  }

  @GET
  @Path("/events/{eventId}/teams/{teamId}/members")
  @UnitOfWork
  public List<UserResponse> getTeamMembers(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    return eventTeamService.getTeamMembers(eventId, teamId, user);
  }

  @DELETE
  @Path("/events/{eventId}/teams/{teamId}/members/{userId}")
  @UnitOfWork
  public MessageResponse removeUserFromTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal user) {
    eventTeamService.removeUserFromTeam(eventId, teamId, userId, user);
    return new MessageResponse("User removed from team successfully");
  }

  @PUT
  @Path("/events/{eventId}/teams/{teamId}/owner/{userId}")
  @UnitOfWork
  public TeamResponse transferOwnership(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal user) {
    return eventTeamService.transferOwnership(eventId, teamId, userId, user);
  }

  @POST
  @Path("/events/{eventId}/teams/{teamId}/requests")
  @UnitOfWork
  public TeamMembershipResponse requestToJoinTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    return eventTeamRequestService.requestToJoinTeam(eventId, teamId, user);
  }

  @DELETE
  @Path("/events/{eventId}/teams/{teamId}/requests")
  @UnitOfWork
  public MessageResponse deleteRequest(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    eventTeamRequestService.deleteRequest(eventId, teamId, user);
    return new MessageResponse("Request deleted successfully");
  }

  @GET
  @Path("/team-requests/me")
  @UnitOfWork
  public List<TeamMembershipResponse> getPendingRequestsForUser(@Auth UserPrincipal user) {
    return eventTeamRequestService.getPendingRequestsForUser(user);
  }

  @GET
  @Path("/events/{eventId}/teams/{teamId}/requests")
  @UnitOfWork
  public List<TeamMembershipResponse> getPendingRequestsForTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Auth UserPrincipal user) {
    return eventTeamRequestService.getPendingRequestsForTeam(eventId, teamId, user);
  }

  @PUT
  @Path("/events/{eventId}/teams/{teamId}/requests/{userId}")
  @UnitOfWork
  public TeamMembershipResponse respondToRequest(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Valid RespondToRequestRequest request,
      @Auth UserPrincipal user) {
    return eventTeamRequestService.respondToRequest(eventId, teamId, userId, request, user);
  }

  @POST
  @Path("/events/{eventId}/teams/{teamId}/invitations")
  @UnitOfWork
  public TeamMembershipResponse inviteUserToTeam(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Valid InviteUserRequest request,
      @Auth UserPrincipal user) {
    return eventTeamRequestService.inviteUserToTeam(eventId, teamId, request.getEmail(), user);
  }

  @DELETE
  @Path("/events/{eventId}/teams/{teamId}/invitations/{userId}")
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
  @Path("/events/{eventId}/teams/{teamId}/invitations")
  @UnitOfWork
  public TeamMembershipResponse respondToInvitation(
      @PathParam("eventId") Long eventId,
      @PathParam("teamId") Long teamId,
      @Valid RespondToRequestRequest request,
      @Auth UserPrincipal user) {
    return eventTeamRequestService.respondToInvitation(eventId, teamId, request, user);
  }
}
