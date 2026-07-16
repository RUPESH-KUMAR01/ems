package com.rupesh.ems.service;

import com.rupesh.ems.api.team.req.RespondToRequestRequest;
import com.rupesh.ems.api.team.res.TeamMembershipResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.RequestStatus;
import com.rupesh.ems.core.RequestType;
import com.rupesh.ems.core.Team;
import com.rupesh.ems.core.TeamMembershipRequest;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.TeamMembershipRequestDao;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.time.Instant;
import java.util.List;

public class EventTeamRequestService {

  private final UserDao userDao;
  private final TeamMembershipRequestDao teamMembershipRequestDao;
  private final EventTeamService eventTeamService;

  public EventTeamRequestService(
      UserDao userDao,
      TeamMembershipRequestDao teamMembershipRequestDao,
      EventTeamService eventTeamService) {
    this.userDao = userDao;
    this.teamMembershipRequestDao = teamMembershipRequestDao;
    this.eventTeamService = eventTeamService;
  }

  public TeamMembershipResponse requestToJoinTeam(Long eventId, Long teamId, UserPrincipal user) {
    eventTeamService.ensureVerified(user);

    Team team = eventTeamService.getEventTeam(eventId, teamId);
    if (team.getOwnerId().equals(user.getId())) {
      throw new BadRequestException("You are the owner of this team");
    }

    eventTeamService.ensureTeamCanAcceptMember(eventId, teamId);
    eventTeamService.ensureNotTeamMember(
        teamId, user.getId(), "You are already a member of this team");
    ensureNoPendingMembershipRequest(
        teamId, user.getId(), "You already have a membership request for this team");

    TeamMembershipRequest request =
        new TeamMembershipRequest(teamId, user.getId(), RequestType.JOIN_REQUEST);

    return new TeamMembershipResponse(teamMembershipRequestDao.create(request));
  }

  public TeamMembershipResponse inviteUserToTeam(
      Long eventId, Long teamId, String email, UserPrincipal user) {
    eventTeamService.ensureVerified(user);

    eventTeamService.getOwnedEventTeam(eventId, teamId, user);
    User invitee =
        userDao.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

    if (invitee.getId().equals(user.getId())) {
      throw new BadRequestException("Cannot invite yourself");
    }

    if (!invitee.isFullyVerified()) {
      throw new BadRequestException("Cannot invite unverified user");
    }

    eventTeamService.ensureTeamCanAcceptMember(eventId, teamId);
    eventTeamService.ensureNotTeamMember(
        teamId, invitee.getId(), "User is already a member of this team");
    ensureNoPendingMembershipRequest(
        teamId, invitee.getId(), "User already has a membership request for this team");

    TeamMembershipRequest request =
        new TeamMembershipRequest(teamId, invitee.getId(), RequestType.INVITATION);

    return new TeamMembershipResponse(teamMembershipRequestDao.create(request));
  }

  public void deleteInvitation(Long eventId, Long teamId, Long userId, UserPrincipal user) {
    eventTeamService.ensureVerified(user);
    eventTeamService.getOwnedEventTeam(eventId, teamId, user);

    teamMembershipRequestDao.delete(
        getPendingMembershipRequest(
            teamId, userId, RequestType.INVITATION, "Invitation not found"));
  }

  public void deleteRequest(Long eventId, Long teamId, UserPrincipal user) {
    eventTeamService.ensureVerified(user);
    eventTeamService.getEventTeam(eventId, teamId);

    teamMembershipRequestDao.delete(
        getPendingMembershipRequest(
            teamId, user.getId(), RequestType.JOIN_REQUEST, "Request not found"));
  }

  public List<TeamMembershipResponse> getPendingRequestsForTeam(
      Long eventId, Long teamId, UserPrincipal user) {
    eventTeamService.ensureVerified(user);
    eventTeamService.getOwnedEventTeam(eventId, teamId, user);

    return teamMembershipRequestDao.getPendingRequestsByTeamId(teamId).stream()
        .map(TeamMembershipResponse::new)
        .toList();
  }

  public List<TeamMembershipResponse> getPendingRequestsForUser(UserPrincipal user) {
    eventTeamService.ensureVerified(user);

    return teamMembershipRequestDao.getPendingRequestsByUserId(user.getId()).stream()
        .map(TeamMembershipResponse::new)
        .toList();
  }

  public TeamMembershipResponse respondToRequest(
      Long eventId,
      Long teamId,
      Long userId,
      RespondToRequestRequest response,
      UserPrincipal user) {
    eventTeamService.ensureVerified(user);
    eventTeamService.getOwnedEventTeam(eventId, teamId, user);

    TeamMembershipRequest membershipRequest =
        getPendingMembershipRequest(teamId, userId, RequestType.JOIN_REQUEST, "Request not found");

    if (response.isApproved()) {
      eventTeamService.ensureTeamCanAcceptMember(eventId, teamId);
      eventTeamService.ensureNotTeamMember(teamId, userId, "User is already a member of this team");
      eventTeamService.addTeamMember(teamId, userId);
    }

    return applyMembershipResponse(membershipRequest, response);
  }

  public TeamMembershipResponse respondToInvitation(
      Long eventId, Long teamId, RespondToRequestRequest response, UserPrincipal user) {
    eventTeamService.ensureVerified(user);
    eventTeamService.getEventTeam(eventId, teamId);

    TeamMembershipRequest invitation =
        getPendingMembershipRequest(
            teamId, user.getId(), RequestType.INVITATION, "Invitation not found");

    if (response.isApproved()) {
      eventTeamService.ensureTeamCanAcceptMember(eventId, teamId);
      eventTeamService.ensureNotTeamMember(
          teamId, user.getId(), "You are already a member of this team");
      eventTeamService.addTeamMember(teamId, user.getId());
    }

    return applyMembershipResponse(invitation, response);
  }

  private TeamMembershipResponse applyMembershipResponse(
      TeamMembershipRequest membershipRequest, RespondToRequestRequest response) {
    if (!response.isApproved() && !response.isRejected()) {
      throw new BadRequestException("Invalid request status");
    }

    membershipRequest.setStatus(response.getStatus());
    membershipRequest.setRespondedAt(Instant.now());

    return new TeamMembershipResponse(teamMembershipRequestDao.update(membershipRequest));
  }

  private void ensureNoPendingMembershipRequest(Long teamId, Long userId, String message) {
    teamMembershipRequestDao
        .findByTeamIdAndUserId(teamId, userId)
        .filter(request -> request.getStatus() == RequestStatus.PENDING)
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
}
