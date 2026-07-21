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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventTeamRequestService {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventTeamRequestService.class);

  private final UserDao userDao;
  private final TeamMembershipRequestDao teamMembershipRequestDao;
  private final EventTeamService eventTeamService;
  private final EventRegistrationService eventRegistrationService;

  public EventTeamRequestService(
      UserDao userDao,
      TeamMembershipRequestDao teamMembershipRequestDao,
      EventTeamService eventTeamService,
      EventRegistrationService eventRegistrationService) {
    this.userDao = userDao;
    this.teamMembershipRequestDao = teamMembershipRequestDao;
    this.eventTeamService = eventTeamService;
    this.eventRegistrationService = eventRegistrationService;
  }

  public TeamMembershipResponse requestToJoinTeam(Long eventId, Long teamId, UserPrincipal user) {
    eventTeamService.ensureVerified(user);

    Team team = eventTeamService.getEventTeam(eventId, teamId);
    if (team.getOwnerId().equals(user.getId())) {
      LOGGER.info(
          "UserId={} is the owner of teamId={} for eventId={}", user.getId(), teamId, eventId);
      throw new BadRequestException("You are the owner of this team");
    }

    eventTeamService.ensureTeamCanAcceptMember(eventId, teamId);
    eventTeamService.ensureNotTeamMember(
        teamId, user.getId(), "You are already a member of this team");
    ensureNoPendingMembershipRequest(
        teamId, user.getId(), "You already have a membership request for this team");

    TeamMembershipRequest request =
        new TeamMembershipRequest(teamId, user.getId(), RequestType.JOIN_REQUEST);
    LOGGER.info(
        "UserId={} has requested to join teamId={} for eventId={}", user.getId(), teamId, eventId);
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
    eventTeamService.ensureRegistrationDeadlineNotPassed(eventTeamService.getEventOrThrow(eventId));
    eventTeamService.getOwnedEventTeam(eventId, teamId, user);

    teamMembershipRequestDao.delete(
        getPendingMembershipRequest(
            teamId, userId, RequestType.INVITATION, "Invitation not found"));
  }

  public void deleteRequest(Long eventId, Long teamId, UserPrincipal user) {
    eventTeamService.ensureVerified(user);
    eventTeamService.ensureRegistrationDeadlineNotPassed(eventTeamService.getEventOrThrow(eventId));
    eventTeamService.getEventTeam(eventId, teamId);

    teamMembershipRequestDao.delete(
        getPendingMembershipRequest(
            teamId, user.getId(), RequestType.JOIN_REQUEST, "Request not found"));
    LOGGER.info(
        "UserId={} deleted request to join teamId={} for eventId={}",
        user.getId(),
        teamId,
        eventId);
  }

  public List<TeamMembershipResponse> getPendingRequestsForTeam(
      Long eventId, Long teamId, UserPrincipal user) {
    eventTeamService.ensureVerified(user);
    eventTeamService.getOwnedEventTeam(eventId, teamId, user);
    LOGGER.info(
        "Fetching pending requests for teamId={} of eventId={} by userId={}",
        teamId,
        eventId,
        user.getId());
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
    eventTeamService.ensureRegistrationDeadlineNotPassed(eventTeamService.getEventOrThrow(eventId));
    eventTeamService.getOwnedEventTeam(eventId, teamId, user);

    TeamMembershipRequest membershipRequest =
        getPendingMembershipRequest(teamId, userId, RequestType.JOIN_REQUEST, "Request not found");

    if (response.isApproved()) {
      LOGGER.info("Approving join request for userId={} to teamId={} in eventId={}", userId, teamId, eventId);
      eventTeamService.addTeamMember(eventId, teamId, userId);
    } else {
      LOGGER.info("Rejecting join request for userId={} to teamId={} in eventId={}", userId, teamId, eventId);
    }

    return applyMembershipResponse(membershipRequest, response);
  }

  public TeamMembershipResponse respondToInvitation(
      Long eventId, Long teamId, RespondToRequestRequest response, UserPrincipal user) {
    eventTeamService.ensureVerified(user);
    eventTeamService.ensureRegistrationDeadlineNotPassed(eventTeamService.getEventOrThrow(eventId));
    eventTeamService.getEventTeam(eventId, teamId);

    TeamMembershipRequest invitation =
        getPendingMembershipRequest(
            teamId, user.getId(), RequestType.INVITATION, "Invitation not found");

    if (response.isApproved()) {
      LOGGER.info("Approving invitation for userId={} to teamId={} in eventId={}", user.getId(), teamId, eventId);
      eventTeamService.addTeamMember(eventId, teamId, user.getId());
    } else {
      LOGGER.info("Rejecting invitation for userId={} to teamId={} in eventId={}", user.getId(), teamId, eventId);
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
              LOGGER.info(
                  "UserId={} already has a pending membership request for teamId={}",
                  userId,
                  teamId);
              throw new ConflictException(message);
            });
  }

  private TeamMembershipRequest getPendingMembershipRequest(
      Long teamId, Long userId, RequestType type, String notFoundMessage) {
    return teamMembershipRequestDao
        .findByTeamIdAndUserId(teamId, userId)
        .filter(request -> request.getType() == type)
        .filter(request -> request.getStatus() == RequestStatus.PENDING)
        .orElseThrow(
            () -> {
              LOGGER.info(
                  "No pending {} membership request found for userId={} in teamId={}",
                  type,
                  userId,
                  teamId);
              return new NotFoundException(notFoundMessage);
            });
  }
}
