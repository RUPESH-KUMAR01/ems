package com.rupesh.ems.service;

import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.api.team.req.CreateTeamRequest;
import com.rupesh.ems.api.team.req.UpdateTeamRequest;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.Event;
import com.rupesh.ems.core.EventStatus;
import com.rupesh.ems.core.EventType;
import com.rupesh.ems.core.Team;
import com.rupesh.ems.core.TeamMember;
import com.rupesh.ems.core.TeamMembershipRequest;
import com.rupesh.ems.db.EventDao;
import com.rupesh.ems.db.EventRegistrationDao;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.db.TeamMemberDao;
import com.rupesh.ems.db.TeamMembershipRequestDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.ForbiddenException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventTeamService {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventTeamService.class);

  private final EventDao eventDao;
  private final TeamDao teamDao;
  private final TeamMemberDao teamMemberDao;
  private final TeamMembershipRequestDao teamMembershipRequestDao;
  private final EventRegistrationDao eventRegistrationDao;

  public EventTeamService(
      EventDao eventDao,
      TeamDao teamDao,
      TeamMemberDao teamMemberDao,
      TeamMembershipRequestDao teamMembershipRequestDao,
      EventRegistrationDao eventRegistrationDao) {
    this.eventDao = eventDao;
    this.teamDao = teamDao;
    this.teamMemberDao = teamMemberDao;
    this.teamMembershipRequestDao = teamMembershipRequestDao;
    this.eventRegistrationDao = eventRegistrationDao;
  }

  public TeamResponse createTeam(Long eventId, CreateTeamRequest request, UserPrincipal user) {
    ensureVerified(user);

    Event event = getEventOrThrow(eventId);
    ensureTeamEvent(event);
    ensureRegistrationOpen(event);
    ensureTeamNameAvailable(eventId, request.getName(), null);
    ensureNotParticipantofEvent(eventId, user.getId());

    Team team = teamDao.create(new Team(eventId, request.getName(), user.getId()));
    teamMemberDao.create(new TeamMember(user.getId(), team.getId()));
    LOGGER.info(
        "Team with teamId={} created for eventId={} by userId={}",
        team.getId(),
        eventId,
        user.getId());

    return new TeamResponse(team);
  }

  public TeamResponse updateTeam(
      Long eventId, Long teamId, UpdateTeamRequest request, UserPrincipal user) {
    LOGGER.info("Updating teamId={} for eventId={} by userId={}", teamId, eventId, user.getId());
    ensureVerified(user);

    Event event = getEventOrThrow(eventId);
    ensureRegistrationDeadlineNotPassed(event);

    Team team = getOwnedEventTeam(eventId, teamId, user);

    if (eventRegistrationDao.findByEventIdAndTeamId(eventId, teamId).isPresent()) {
      throw new BadRequestException("Cannot update a registered team");
    }

    if (request.getName() != null) {
      LOGGER.info("Updating team name to {}", request.getName());
      ensureTeamNameNotBlank(request.getName());
      ensureTeamNameAvailable(eventId, request.getName(), teamId);
      team.setName(request.getName());
    }

    LOGGER.info(
        "Team with teamId={} updated for eventId={} by userId={}",
        team.getId(),
        eventId,
        user.getId());
    return new TeamResponse(teamDao.update(team));
  }

  public TeamResponse getTeamById(Long eventId, Long teamId, UserPrincipal user) {
    ensureVerified(user);

    ensureTeamMember(teamId, user.getId(), "You are not a member of this team");
    Team team = getEventTeam(eventId, teamId);

    return new TeamResponse(team);
  }

  public List<TeamResponse> getTeamsForEvent(Long eventId) {
    Event event = getEventOrThrow(eventId);
    ensureTeamEvent(event);

    return teamDao.findByEventId(eventId).stream().map(TeamResponse::new).toList();
  }

  public List<TeamResponse> getTeamsForUser(UserPrincipal user) {
    ensureVerified(user);

    return teamDao.findByUserId(user.getId()).stream().map(TeamResponse::new).toList();
  }

  public void deleteTeam(Long eventId, Long teamId, UserPrincipal user) {
    ensureVerified(user);

    Event event = getEventOrThrow(eventId);
    ensureRegistrationDeadlineNotPassed(event);

    Team team = getOwnedEventTeam(eventId, teamId, user);

    if (eventRegistrationDao.findByEventIdAndTeamId(eventId, teamId).isPresent()) {
      throw new BadRequestException("Cannot delete a registered team");
    }

    teamMemberDao.deleteByTeamId(teamId);
    LOGGER.info("Deleted all team members for teamId={}", teamId);
    teamMembershipRequestDao.deleteByTeamId(teamId);
    LOGGER.info("Deleted all membership requests for teamId={}", teamId);
    teamDao.delete(team);
    LOGGER.info(
        "Team with teamId={} deleted for eventId={} by userId={}", teamId, eventId, user.getId());
  }

  public void removeUserFromTeam(Long eventId, Long teamId, Long userId, UserPrincipal user) {
    ensureVerified(user);

    Event event = getEventOrThrow(eventId);
    ensureRegistrationDeadlineNotPassed(event);

    Team team = getOwnedEventTeam(eventId, teamId, user);
    LOGGER.info(
        "Attempting to remove userId={} from teamId={} of eventId={} by userId={}",
        userId,
        teamId,
        eventId,
        user.getId());

    if (eventRegistrationDao.findByEventIdAndTeamId(eventId, teamId).isPresent()) {
      throw new BadRequestException("Cannot remove member from a registered team");
    }
    if (team.getOwnerId().equals(userId)) {
      LOGGER.warn(
          "Attempted to remove owner userId={} from teamId={} of eventId={}",
          userId,
          teamId,
          eventId);
      throw new BadRequestException("Owner cannot be removed from team");
    }

    TeamMember membership =
        teamMemberDao
            .findByTeamIdAndUserId(teamId, userId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("User with userId={} is not a member of teamId={}", userId, teamId);
                  return new NotFoundException("User is not a member of this team");
                });

    teamMemberDao.delete(membership);
    LOGGER.info(
        "User with userId={} removed from teamId={} of eventId={}", userId, teamId, eventId);
    TeamMembershipRequest request =
        teamMembershipRequestDao.findByTeamIdAndUserId(teamId, userId).orElse(null);
    if (request != null) {
      teamMembershipRequestDao.delete(request);
      LOGGER.info(
          "Deleted membership request for userId={} from teamId={} of eventId={}",
          userId,
          teamId,
          eventId);
    }
  }

  public TeamResponse transferOwnership(
      Long eventId, Long teamId, Long newOwnerId, UserPrincipal user) {
    LOGGER.info(
        "Transferring ownership of teamId={} for eventId={} to newOwnerId={} by userId={}",
        teamId,
        eventId,
        newOwnerId,
        user.getId());
    ensureVerified(user);

    Event event = getEventOrThrow(eventId);
    ensureRegistrationDeadlineNotPassed(event);

    Team team = getOwnedEventTeam(eventId, teamId, user);

    if (team.getOwnerId().equals(newOwnerId)) {
      LOGGER.warn(
          "Attempted to transfer ownership of teamId={} for eventId={} to the same owner userId={}",
          teamId,
          eventId,
          newOwnerId);
      throw new BadRequestException("You are already the owner of this team");
    }

    ensureTeamMember(teamId, newOwnerId, "New owner must be a member of the team");

    team.setOwnerId(newOwnerId);
    LOGGER.info(
        "Ownership of teamId={} for eventId={} transferred to newOwnerId={} by userId={}",
        teamId,
        eventId,
        newOwnerId,
        user.getId());
    return new TeamResponse(teamDao.update(team));
  }

  public List<UserResponse> getTeamMembers(Long eventId, Long teamId, UserPrincipal user) {
    ensureTeamMember(teamId, user.getId(), "You are not a member of this team");
    ensureVerified(user);
    getEventTeam(eventId, teamId);

    return teamMemberDao.getUsersByTeamId(teamId).stream().map(UserResponse::new).toList();
  }

  public Team getEventTeam(Long eventId, Long teamId) {
    return teamDao
        .findByEventIdAndTeamId(eventId, teamId)
        .orElseThrow(
            () -> {
              LOGGER.warn("Team with teamId={} not found for eventId={}", teamId, eventId);
              return new NotFoundException("Team not found");
            });
  }

  public Team getOwnedEventTeam(Long eventId, Long teamId, UserPrincipal user) {
    return teamDao
        .findByEventIdAndTeamId(eventId, teamId)
        .filter(team -> team.getOwnerId().equals(user.getId()))
        .orElseThrow(
            () -> {
              LOGGER.warn(
                  "User with userId={} is not the owner of teamId={} for eventId={}",
                  user.getId(),
                  teamId,
                  eventId);
              return new ForbiddenException("You are not the owner of this team");
            });
  }

  public Event getEventOrThrow(Long eventId) {
    LOGGER.info("Fetching eventId={}", eventId);
    return eventDao
        .getEventById(eventId)
        .orElseThrow(
            () -> {
              LOGGER.warn("Event with eventId={} not found", eventId);
              return new NotFoundException("Event not found");
            });
  }

  public void ensureVerified(UserPrincipal user) {
    if (!user.isFullyVerified()) {
      LOGGER.warn(
          "User with userId={} attempted to perform an action without full verification",
          user.getId());
      throw new ForbiddenException("Email and phone verification required");
    }
  }

  public void ensureTeamCanAcceptMember(Long eventId, Long teamId) {
    Event event = getEventOrThrow(eventId);
    Team team = getEventTeam(eventId, teamId);
    ensureTeamEvent(event);
    ensureRegistrationOpen(event);
    if (teamMemberDao.countByTeamId(team.getId()) >= event.getMaxTeamSize()) {
      LOGGER.warn(
          "Team with teamId={} for eventId={} has reached its member limit of {}",
          teamId,
          eventId,
          event.getMaxTeamSize());
      throw new BadRequestException("Team member limit reached");
    }
  }

  public void ensureNotTeamMember(Long teamId, Long userId, String message) {
    teamMemberDao
        .findByTeamIdAndUserId(teamId, userId)
        .ifPresent(
            existing -> {
              LOGGER.warn("User with userId={} is already a member of teamId={}", userId, teamId);
              throw new ConflictException(message);
            });
  }

  public void addTeamMember(Long teamId, Long userId) {
    teamMemberDao.create(new TeamMember(userId, teamId));
  }

  private void ensureTeamMember(Long teamId, Long userId, String message) {
    teamMemberDao
        .findByTeamIdAndUserId(teamId, userId)
        .orElseThrow(
            () -> {
              LOGGER.warn("User with userId={} is not a member of teamId={}", userId, teamId);
              return new ForbiddenException(message);
            });
  }

  private void ensureTeamEvent(Event event) {
    if (event.getType() != EventType.TEAM) {
      LOGGER.warn("Event with eventId={} is not a team event", event.getId());
      throw new BadRequestException("Teams can only be created for team events");
    }
  }

  public void ensureRegistrationOpen(Event event) {
    if (event.getStatus() != EventStatus.PUBLISHED) {
      LOGGER.warn("Event with eventId={} is not published", event.getId());
      throw new BadRequestException("Event registration is not open");
    }

    ensureRegistrationDeadlineNotPassed(event);
  }

  public void ensureRegistrationDeadlineNotPassed(Event event) {
    if (!Instant.now().isBefore(event.getRegistrationDeadline())) {
      LOGGER.warn("Event with eventId={} has passed its registration deadline", event.getId());
      throw new BadRequestException("Event registration deadline has passed");
    }
  }

  private void ensureTeamNameAvailable(Long eventId, String name, Long currentTeamId) {
    teamDao
        .findByEventIdAndName(eventId, name)
        .filter(team -> currentTeamId == null || !team.getId().equals(currentTeamId))
        .ifPresent(
            existing -> {
              LOGGER.warn("Team with name={} already exists for eventId={}", name, eventId);
              throw new ConflictException("Team with the same name already exists");
            });
  }

  public void ensureNotParticipantofEvent(Long eventId, Long userId) {
    teamMemberDao
        .getMembershipByEventIdAndUserId(eventId, userId)
        .ifPresent(
            existing -> {
              LOGGER.warn(
                  "User with userId={} is already a participant of eventId={}", userId, eventId);
              throw new ConflictException("You are already a participant of this event");
            });
  }

  private void ensureTeamNameNotBlank(String name) {
    if (name.isBlank()) {
      LOGGER.warn("Attempted to set a blank team name");
      throw new BadRequestException("Team name is required");
    }
  }
}
