package com.rupesh.ems.resources;

import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.api.team.req.CreateTeamRequest;
import com.rupesh.ems.api.team.req.InviteUserRequest;
import com.rupesh.ems.api.team.req.RespondToRequestRequest;
import com.rupesh.ems.api.team.req.UpdateTeamRequest;
import com.rupesh.ems.api.team.res.TeamMembershipResponse;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.TeamService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import java.util.List;

@Path("/api/teams")
@RolesAllowed("USER")
public class TeamResource {

  private final TeamService teamService;

  public TeamResource(TeamService teamService) {
    this.teamService = teamService;
  }

  @POST
  @UnitOfWork
  public TeamResponse createTeam(@Valid CreateTeamRequest request, @Auth UserPrincipal user) {
    return teamService.createTeam(request, user);
  }

  @GET
  @UnitOfWork
  public List<TeamResponse> getTeams(@Auth UserPrincipal user) {
    return teamService.getTeamsForUser(user);
  }

  @GET
  @Path("/all")
  @RolesAllowed("ADMIN")
  @UnitOfWork
  public List<TeamResponse> getAllTeams() {
    return teamService.getAllTeams();
  }

  @GET
  @Path("/discover")
  @UnitOfWork
  public List<TeamResponse> getTeamsCanJoin(@Auth UserPrincipal user) {
    return teamService.getTeamsCanJoin(user);
  }

  @GET
  @Path("/{teamId}")
  @UnitOfWork
  public TeamResponse getTeamById(@PathParam("teamId") Long teamId, @Auth UserPrincipal user) {
    return teamService.getTeamById(teamId, user);
  }

  @PUT
  @Path("/{teamId}")
  @UnitOfWork
  public TeamResponse updateTeam(
      @PathParam("teamId") Long teamId,
      @Valid UpdateTeamRequest request,
      @Auth UserPrincipal user) {
    return teamService.updateTeam(teamId, request, user);
  }

  @DELETE
  @Path("/{teamId}")
  @UnitOfWork
  public void deleteTeam(@PathParam("teamId") Long teamId, @Auth UserPrincipal user) {
    teamService.deleteTeam(teamId, user);
  }

  @GET
  @Path("/{teamId}/members")
  @UnitOfWork
  public List<UserResponse> getTeamMembers(
      @PathParam("teamId") Long teamId, @Auth UserPrincipal user) {
    return teamService.getTeamMembers(teamId, user);
  }

  @DELETE
  @Path("/{teamId}/members/{userId}")
  @UnitOfWork
  public void removeUserFromTeam(
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal user) {
    teamService.removeUserFromTeam(teamId, userId, user);
  }

  @PUT
  @Path("/{teamId}/owner/{userId}")
  @UnitOfWork
  public TeamResponse transferOwnership(
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal user) {
    return teamService.transferOwnership(teamId, userId, user);
  }

  @POST
  @Path("/{teamId}/requests")
  @UnitOfWork
  public TeamMembershipResponse requestToJoinTeam(
      @PathParam("teamId") Long teamId, @Auth UserPrincipal user) {
    return teamService.requestToJoinTeam(teamId, user);
  }

  @DELETE
  @Path("/{teamId}/requests")
  @UnitOfWork
  public void deleteRequest(@PathParam("teamId") Long teamId, @Auth UserPrincipal user) {
    teamService.deleteRequest(teamId, user);
  }

  @GET
  @Path("/requests/me")
  @UnitOfWork
  public List<TeamMembershipResponse> getPendingRequestsForUser(@Auth UserPrincipal user) {
    return teamService.getPendingRequestsForUser(user);
  }

  @GET
  @Path("/{teamId}/requests")
  @UnitOfWork
  public List<TeamMembershipResponse> getPendingRequestsForTeam(
      @PathParam("teamId") Long teamId, @Auth UserPrincipal user) {
    return teamService.getPendingRequestsForTeam(teamId, user);
  }

  @PUT
  @Path("/{teamId}/requests/{userId}")
  @UnitOfWork
  public TeamMembershipResponse respondToRequest(
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Valid RespondToRequestRequest request,
      @Auth UserPrincipal user) {
    return teamService.respondToRequest(teamId, userId, request, user);
  }

  @POST
  @Path("/{teamId}/invitations")
  @UnitOfWork
  public TeamMembershipResponse inviteUserToTeam(
      @PathParam("teamId") Long teamId,
      @Valid InviteUserRequest request,
      @Auth UserPrincipal user) {
    return teamService.inviteUserToTeam(teamId, request.getEmail(), user);
  }

  @DELETE
  @Path("/{teamId}/invitations/{userId}")
  @UnitOfWork
  public void deleteInvitation(
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal user) {
    teamService.deleteInvitation(teamId, userId, user);
  }

  @PUT
  @Path("/{teamId}/invitations")
  @UnitOfWork
  public TeamMembershipResponse respondToInvitation(
      @PathParam("teamId") Long teamId,
      @Valid RespondToRequestRequest request,
      @Auth UserPrincipal user) {
    return teamService.respondToInvitation(teamId, request, user);
  }
}
