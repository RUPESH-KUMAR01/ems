package com.rupesh.ems.service;

import com.rupesh.ems.api.team.req.CreateTeamRequest;
import com.rupesh.ems.api.team.req.UpdateTeamRequest;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.Team;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.util.List;

public class TeamService {

  private final TeamDao teamDao;

  public TeamService(TeamDao teamDao) {
    this.teamDao = teamDao;
  }

  private Team getOwnedTeam(Long teamId, UserPrincipal user) {
    return teamDao
        .getTeamById(teamId)
        .filter(team -> team.getOwnerId().equals(user.getId()))
        .orElseThrow(() -> new NotFoundException("Team not found"));
  }

  public TeamResponse createTeam(CreateTeamRequest request, UserPrincipal user) {

    teamDao
        .findByOwnerIdAndName(user.getId(), request.getName())
        .ifPresent(
            existing -> {
              throw new ConflictException("Team already exists");
            });

    Team team = new Team(request.getName(), user.getId());

    team = teamDao.create(team);

    return new TeamResponse(team);
  }

  public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request, UserPrincipal user) {

    Team team = getOwnedTeam(teamId, user);

    teamDao
        .findByOwnerIdAndName(user.getId(), request.getName())
        .filter(existing -> !existing.getId().equals(teamId))
        .ifPresent(
            existing -> {
              throw new ConflictException("Another team with the same name already exists");
            });

    team.setName(request.getName());

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

    Team team = getOwnedTeam(teamId, user);

    teamDao.delete(team);
  }
}
