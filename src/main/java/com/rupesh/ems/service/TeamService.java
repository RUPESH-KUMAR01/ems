package com.rupesh.ems.service;

import com.rupesh.ems.api.team.req.CreateTeamRequest;
import com.rupesh.ems.api.team.req.UpdateTeamRequest;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.Team;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.db.TeamMemberDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.ForbiddenException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.util.List;

public class TeamService {

  private final TeamDao teamDao;
  private final TeamMemberDao teamMemberDao;

  public TeamService(TeamDao teamDao, TeamMemberDao teamMemberDao) {
    this.teamDao = teamDao;
    this.teamMemberDao = teamMemberDao;
  }

  private Team getOwnedTeam(Long teamId, UserPrincipal user) {
    return teamDao
        .getTeamById(teamId)
        .filter(team -> team.getOwnerId().equals(user.getId()))
        .orElseThrow(() -> new NotFoundException("Team not found"));
  }
  private void ensureVerified(UserPrincipal user) {
      if (!user.isFullyVerified()) {
          throw new ForbiddenException(
              "Email and phone verification required");
      }
  }
  public TeamResponse createTeam(CreateTeamRequest request, UserPrincipal user) {
    ensureVerified(user);

    teamDao
        .findByOwnerIdAndName(user.getId(), request.getName())
        .ifPresent(
            existing -> {
              throw new ConflictException("Team already exists");
            });

    Team team = new Team(request.getName(), user.getId(), request.getMaxMembers());

    team = teamDao.create(team);

    return new TeamResponse(team);
  }

  public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedTeam(teamId, user);

    teamDao
        .findByOwnerIdAndName(user.getId(), request.getName())
        .filter(existing -> !existing.getId().equals(teamId))
        .ifPresent(
            existing -> {
              throw new ConflictException("Another team with the same name already exists");
            });

    team.setName(request.getName());
    if (request.getMaxMembers() != null) {
      Long currentMemberCount = teamMemberDao.countByTeamId(teamId);
      if (request.getMaxMembers() < currentMemberCount) {
        throw new BadRequestException(
            "Team member limit cannot be less than current member count");
      }
      team.setMaxMembers(request.getMaxMembers());
    }

    team = teamDao.update(team);

    return new TeamResponse(team);
  }

  public TeamResponse getTeamById(Long teamId, UserPrincipal user) {

    return new TeamResponse(getOwnedTeam(teamId, user));
  }

  public List<TeamResponse> getTeamsForUser(UserPrincipal user) {

    return teamDao.findByOwnerId(user.getId()).stream().map(TeamResponse::new).toList();
  }

  public void deleteTeam(Long teamId, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedTeam(teamId, user);

    teamDao.delete(team);
  }
}
