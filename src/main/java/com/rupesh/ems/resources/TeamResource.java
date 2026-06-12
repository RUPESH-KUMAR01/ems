package com.rupesh.ems.resources;

import com.rupesh.ems.api.team.req.CreateTeamRequest;
import com.rupesh.ems.api.team.req.UpdateTeamRequest;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.TeamService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
}
