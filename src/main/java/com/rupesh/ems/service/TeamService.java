package com.rupesh.ems.service;

import com.rupesh.ems.api.team.req.CreateTeamRequest;
import com.rupesh.ems.api.team.req.UpdateTeamRequest;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.Team;
import com.rupesh.ems.core.TeamMember;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.db.TeamMemberDao;
import com.rupesh.ems.db.TeamMembershipRequestDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.ForbiddenException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.util.List;

public class TeamService {

  private final TeamDao teamDao;
  private final TeamMemberDao teamMemberDao;
  private final TeamMembershipRequestDao teamMembershipRequestDao;

  public TeamService(
      TeamDao teamDao,
      TeamMemberDao teamMemberDao,
      TeamMembershipRequestDao teamMembershipRequestDao) {
    this.teamDao = teamDao;
    this.teamMemberDao = teamMemberDao;
    this.teamMembershipRequestDao = teamMembershipRequestDao;
  }

  // create team
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

    teamMemberDao.create(new TeamMember(team.getOwnerId(), team.getId()));

    return new TeamResponse(team);
  }

  // update team
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
        throw new BadRequestException("Team member limit cannot be less than current member count");
      }
      team.setMaxMembers(request.getMaxMembers());
    }

    team = teamDao.update(team);

    return new TeamResponse(team);
  }

  // get team
  public TeamResponse getTeamById(Long teamId, UserPrincipal user) {
    return teamDao
        .getTeamById(teamId)
        .map(team -> new TeamResponse(team))
        .orElseThrow(() -> new NotFoundException("Team Not Found"));
  }

  // get all teams by user
  public List<TeamResponse> getTeamsForUser(UserPrincipal user) {
    return teamMemberDao.getTeamsByUserId(user.getId()).stream()
        .map(
            tm ->
                teamDao
                    .getTeamById(tm.getTeamId())
                    .orElseThrow(() -> new NotFoundException("Team not found")))
        .map(TeamResponse::new)
        .toList();
  }

  // delete team
  public void deleteTeam(Long teamId, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedTeam(teamId, user);

    teamMemberDao.deleteByTeamId(teamId);
    teamMembershipRequestDao.deleteByTeamId(teamId);

    teamDao.delete(team);
  }

  // user requesting team owner

  // team owner inviting user by email

  // team owner deleting invitation

  // user deleting request

  // get all pending requests for team

  // get all pending requests for user

  // team owner approving/rejecting

  // user accepting/rejecting

  // team owner adding user to team

  // team owner removing user from team

  // get all teams
  public List<TeamResponse> getAllTeams() {
    return teamDao.findAll().stream().map(team -> new TeamResponse(team)).toList();
  }

  private Team getOwnedTeam(Long teamId, UserPrincipal user) {
    return teamDao
        .getTeamById(teamId)
        .filter(team -> team.getOwnerId().equals(user.getId()))
        .orElseThrow(() -> new NotFoundException("Team not found"));
  }

  private void ensureVerified(UserPrincipal user) {
    if (!user.isFullyVerified()) {
      throw new ForbiddenException("Email and phone verification required");
    }
  }
}
