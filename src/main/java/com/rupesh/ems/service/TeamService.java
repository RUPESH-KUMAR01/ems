package com.rupesh.ems.service;

import com.rupesh.ems.api.team.req.CreateTeamRequest;
import com.rupesh.ems.api.team.req.RespondToRequestRequest;
import com.rupesh.ems.api.team.req.UpdateTeamRequest;
import com.rupesh.ems.api.team.res.TeamMemberResponse;
import com.rupesh.ems.api.team.res.TeamMembershipResponse;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.RequestStatus;
import com.rupesh.ems.core.RequestType;
import com.rupesh.ems.core.Team;
import com.rupesh.ems.core.TeamMember;
import com.rupesh.ems.core.TeamMembershipRequest;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.db.TeamMemberDao;
import com.rupesh.ems.db.TeamMembershipRequestDao;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.ForbiddenException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TeamService {
  private final UserDao userDao;
  private final TeamDao teamDao;
  private final TeamMemberDao teamMemberDao;
  private final TeamMembershipRequestDao teamMembershipRequestDao;

  public TeamService(
      UserDao userDao,
      TeamDao teamDao,
      TeamMemberDao teamMemberDao,
      TeamMembershipRequestDao teamMembershipRequestDao) {
    this.userDao = userDao;
    this.teamDao = teamDao;
    this.teamMemberDao = teamMemberDao;
    this.teamMembershipRequestDao = teamMembershipRequestDao;
  }

  public TeamResponse createTeam(CreateTeamRequest request, UserPrincipal user) {
    ensureVerified(user);
    ensureTeamNameAvailable(user.getId(), request.getName(), null);

    Team team =
        teamDao.create(new Team(request.getName(), user.getId(), request.getMaxMembers()));
    teamMemberDao.create(new TeamMember(team.getOwnerId(), team.getId()));

    return new TeamResponse(team);
  }

  public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedTeam(teamId, user);

    if (request.getName() != null) {
      ensureTeamNameNotBlank(request.getName());
      ensureTeamNameAvailable(user.getId(), request.getName(), teamId);
      team.setName(request.getName());
    }

    if (request.getMaxMembers() != null) {
      ensureValidMemberLimit(request.getMaxMembers());
      ensureMemberLimitCanFitCurrentMembers(teamId, request.getMaxMembers());
      team.setMaxMembers(request.getMaxMembers());
    }

    return new TeamResponse(teamDao.update(team));
  }

  public TeamResponse getTeamById(Long teamId, UserPrincipal user) {
    ensureVerified(user);

    return new TeamResponse(getTeam(teamId));
  }

  public List<TeamResponse> getTeamsForUser(UserPrincipal user) {
    ensureVerified(user);

    return teamMemberDao.getTeamsByUserId(user.getId()).stream()
        .map(teamMember -> getTeam(teamMember.getTeamId()))
        .map(TeamResponse::new)
        .toList();
  }

  public void deleteTeam(Long teamId, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedTeam(teamId, user);

    teamMemberDao.deleteByTeamId(teamId);
    teamMembershipRequestDao.deleteByTeamId(teamId);

    teamDao.delete(team);
  }

  public TeamMembershipResponse requestToJoinTeam(Long teamId, UserPrincipal user) {
    ensureVerified(user);

    Team team = getTeam(teamId);
    if (team.getOwnerId().equals(user.getId())) {
      throw new BadRequestException("You are the owner of this team");
    }

    ensureTeamHasCapacity(teamId, team);
    ensureNotTeamMember(teamId, user.getId(), "You are already a member of this team");
    ensureNoMembershipRequest(
        teamId, user.getId(), "You already have a membership request for this team");

    TeamMembershipRequest request =
        new TeamMembershipRequest(teamId, user.getId(), RequestType.JOIN_REQUEST);

    return new TeamMembershipResponse(teamMembershipRequestDao.create(request));
  }

  public TeamMembershipResponse inviteUserToTeam(Long teamId, String email, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedTeam(teamId, user);
    User invitee =
        userDao.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

    if (invitee.getId().equals(user.getId())) {
      throw new BadRequestException("Cannot invite yourself");
    }
    if (!invitee.isFullyVerified()) {
      throw new BadRequestException("Cannot invite unverified user");
    }

    ensureTeamHasCapacity(teamId, team);
    ensureNotTeamMember(teamId, invitee.getId(), "User is already a member of this team");
    ensureNoMembershipRequest(
        teamId, invitee.getId(), "User already has a membership request for this team");

    TeamMembershipRequest request =
        new TeamMembershipRequest(teamId, invitee.getId(), RequestType.INVITATION);

    return new TeamMembershipResponse(teamMembershipRequestDao.create(request));
  }

  public void deleteInvitation(Long teamId, Long userId, UserPrincipal user) {
    ensureVerified(user);

    getOwnedTeam(teamId, user);

    teamMembershipRequestDao.delete(
        getPendingMembershipRequest(
            teamId, userId, RequestType.INVITATION, "Invitation not found"));
  }

  public void deleteRequest(Long teamId, UserPrincipal user) {
    ensureVerified(user);

    teamMembershipRequestDao.delete(
        getPendingMembershipRequest(
            teamId, user.getId(), RequestType.JOIN_REQUEST, "Request not found"));
  }

  public List<TeamMembershipResponse> getPendingRequestsForTeam(Long teamId, UserPrincipal user) {
    ensureVerified(user);

    getOwnedTeam(teamId, user);

    return teamMembershipRequestDao.getPendingRequestsByTeamId(teamId).stream()
        .map(TeamMembershipResponse::new)
        .toList();
  }

  public List<TeamMembershipResponse> getPendingRequestsForUser(UserPrincipal user) {
    ensureVerified(user);

    return teamMembershipRequestDao.getPendingRequestsByUserId(user.getId()).stream()
        .map(TeamMembershipResponse::new)
        .toList();
  }

  public TeamMembershipResponse respondToRequest(
      Long teamId, Long userId, RespondToRequestRequest request, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedTeam(teamId, user);
    TeamMembershipRequest membershipRequest =
        getPendingMembershipRequest(
            teamId, userId, RequestType.JOIN_REQUEST, "Request not found");

    if (request.isApproved()) {
      ensureTeamHasCapacity(teamId, team);
      ensureNotTeamMember(teamId, userId, "User is already a member of this team");
      teamMemberDao.create(new TeamMember(userId, teamId));
    }

    applyMembershipResponse(membershipRequest, request);
    return new TeamMembershipResponse(teamMembershipRequestDao.update(membershipRequest));
  }

  public TeamMembershipResponse respondToInvitation(
      Long teamId, RespondToRequestRequest request, UserPrincipal user) {
    ensureVerified(user);

    TeamMembershipRequest invitation =
        getPendingMembershipRequest(
            teamId, user.getId(), RequestType.INVITATION, "Invitation not found");

    if (request.isApproved()) {
      ensureTeamHasCapacity(teamId, getTeam(teamId));
      ensureNotTeamMember(teamId, user.getId(), "You are already a member of this team");
      teamMemberDao.create(new TeamMember(user.getId(), teamId));
    }

    applyMembershipResponse(invitation, request);
    return new TeamMembershipResponse(teamMembershipRequestDao.update(invitation));
  }

  public void removeUserFromTeam(Long teamId, Long userId, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedTeam(teamId, user);
    if (userId.equals(team.getOwnerId())) {
      throw new BadRequestException("Owner cannot be removed from team");
    }
    TeamMember membership =
        teamMemberDao
            .findByTeamIdAndUserId(teamId, userId)
            .orElseThrow(() -> new NotFoundException("Team member not found"));

    teamMemberDao.delete(membership);
    TeamMembershipRequest request =
        teamMembershipRequestDao.findByTeamIdAndUserId(teamId, userId).orElse(null);
    if (request != null) {
      teamMembershipRequestDao.delete(request);
    }
  }

  public List<TeamResponse> getAllTeams() {
    return teamDao.findAll().stream().map(TeamResponse::new).toList();
  }

  public List<TeamResponse> getTeamsCanJoin(UserPrincipal user) {
    ensureVerified(user);

    Set<Long> userTeamIds =
        teamMemberDao.getTeamsByUserId(user.getId()).stream()
            .map(TeamMember::getTeamId)
            .collect(Collectors.toSet());

    return teamDao.findAll().stream()
        .filter(team -> !userTeamIds.contains(team.getId()))
        .filter(team -> teamMemberDao.countByTeamId(team.getId()) < team.getMaxMembers())
        .map(TeamResponse::new)
        .toList();
  }

  public TeamResponse transferOwnership(Long teamId, Long newOwnerId, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedTeam(teamId, user);

    if (team.getOwnerId().equals(newOwnerId)) {
      throw new BadRequestException("You are already the owner of this team");
    }

    User newOwner =
        userDao
            .getUserById(newOwnerId)
            .orElseThrow(() -> new NotFoundException("User not found"));

    if (!newOwner.isFullyVerified()) {
      throw new BadRequestException("New owner must be fully verified");
    }

    teamMemberDao
        .findByTeamIdAndUserId(teamId, newOwnerId)
        .orElseThrow(() -> new NotFoundException("New owner must be a member of the team"));

    ensureTeamNameAvailable(newOwnerId, team.getName(), null);
    team.setOwnerId(newOwnerId);

    return new TeamResponse(teamDao.update(team));
  }

  public List<TeamMemberResponse> getTeamMembers(Long teamId, UserPrincipal user) {
    ensureVerified(user);

    getOwnedTeam(teamId, user);

    return teamMemberDao.getUsersByTeamId(teamId).stream()
        .map(TeamMemberResponse::new)
        .toList();
  }

  private Team getTeam(Long teamId) {
    return teamDao
        .getTeamById(teamId)
        .orElseThrow(() -> new NotFoundException("Team not found"));
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

  private void ensureTeamNameAvailable(Long ownerId, String name, Long currentTeamId) {
    teamDao
        .findByOwnerIdAndName(ownerId, name)
        .filter(existing -> currentTeamId == null || !existing.getId().equals(currentTeamId))
        .ifPresent(
            existing -> {
              throw new ConflictException("Team with the same name already exists");
            });
  }

  private void ensureTeamNameNotBlank(String name) {
    if (name.isBlank()) {
      throw new BadRequestException("Team name is required");
    }
  }

  private void ensureValidMemberLimit(Integer maxMembers) {
    if (maxMembers < 1) {
      throw new BadRequestException("Team member limit must be at least 1");
    }
  }

  private void ensureMemberLimitCanFitCurrentMembers(Long teamId, Integer maxMembers) {
    if (maxMembers < teamMemberDao.countByTeamId(teamId)) {
      throw new BadRequestException("Team member limit cannot be less than current member count");
    }
  }

  private void ensureTeamHasCapacity(Long teamId, Team team) {
    if (teamMemberDao.countByTeamId(teamId) >= team.getMaxMembers()) {
      throw new BadRequestException("Team member limit reached");
    }
  }

  private void ensureNotTeamMember(Long teamId, Long userId, String message) {
    teamMemberDao
        .findByTeamIdAndUserId(teamId, userId)
        .ifPresent(
            existing -> {
              throw new ConflictException(message);
            });
  }

  private void ensureNoMembershipRequest(Long teamId, Long userId, String message) {
    teamMembershipRequestDao
        .findByTeamIdAndUserId(teamId, userId)
        .ifPresent(
            existing -> {
              throw new ConflictException(message);
            });
  }

  private TeamMembershipRequest getPendingMembershipRequest(
      Long teamId, Long userId, RequestType type, String notFoundMessage) {
    return teamMembershipRequestDao
        .findByTeamIdAndUserId(teamId, userId)
        .filter(request -> request.getType() == type)
        .filter(request -> request.getStatus() == RequestStatus.PENDING)
        .orElseThrow(() -> new NotFoundException(notFoundMessage));
  }

  private void applyMembershipResponse(
      TeamMembershipRequest membershipRequest, RespondToRequestRequest response) {
    if (!response.isApproved() && !response.isRejected()) {
      throw new BadRequestException("Invalid request status");
    }

    membershipRequest.setStatus(response.getStatus());
    membershipRequest.setRespondedAt(Instant.now());
  }
}
