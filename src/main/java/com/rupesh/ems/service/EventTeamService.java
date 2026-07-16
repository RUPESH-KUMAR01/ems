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
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.db.TeamMemberDao;
import com.rupesh.ems.db.TeamMembershipRequestDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.ForbiddenException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.time.Instant;
import java.util.List;

public class EventTeamService {

  private final EventDao eventDao;
  private final TeamDao teamDao;
  private final TeamMemberDao teamMemberDao;
  private final TeamMembershipRequestDao teamMembershipRequestDao;

  public EventTeamService(
      EventDao eventDao,
      TeamDao teamDao,
      TeamMemberDao teamMemberDao,
      TeamMembershipRequestDao teamMembershipRequestDao) {
    this.eventDao = eventDao;
    this.teamDao = teamDao;
    this.teamMemberDao = teamMemberDao;
    this.teamMembershipRequestDao = teamMembershipRequestDao;
  }

  public TeamResponse createTeam(Long eventId, CreateTeamRequest request, UserPrincipal user) {
    ensureVerified(user);

    Event event = getEventOrThrow(eventId);
    ensureTeamEvent(event);
    ensureRegistrationOpen(event);
    ensureTeamNameAvailable(eventId, request.getName(), null);
    ensureUserDoesNotOwnTeamForEvent(eventId, user.getId());

    Team team = teamDao.create(new Team(eventId, request.getName(), user.getId()));
    teamMemberDao.create(new TeamMember(user.getId(), team.getId()));

    return new TeamResponse(team);
  }

  public TeamResponse updateTeam(
      Long eventId, Long teamId, UpdateTeamRequest request, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedEventTeam(eventId, teamId, user);

    if (request.getName() != null) {
      ensureTeamNameNotBlank(request.getName());
      ensureTeamNameAvailable(eventId, request.getName(), teamId);
      team.setName(request.getName());
    }

    return new TeamResponse(teamDao.update(team));
  }

  public TeamResponse getTeamById(Long eventId, Long teamId, UserPrincipal user) {
    ensureVerified(user);

    Team team = getEventTeam(eventId, teamId);
    ensureTeamMember(teamId, user.getId(), "You are not a member of this team");

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

    Team team = getOwnedEventTeam(eventId, teamId, user);

    teamMemberDao.deleteByTeamId(teamId);
    teamMembershipRequestDao.deleteByTeamId(teamId);
    teamDao.delete(team);
  }

  public void removeUserFromTeam(Long eventId, Long teamId, Long userId, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedEventTeam(eventId, teamId, user);
    if (team.getOwnerId().equals(userId)) {
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

  public TeamResponse transferOwnership(
      Long eventId, Long teamId, Long newOwnerId, UserPrincipal user) {
    ensureVerified(user);

    Team team = getOwnedEventTeam(eventId, teamId, user);

    if (team.getOwnerId().equals(newOwnerId)) {
      throw new BadRequestException("You are already the owner of this team");
    }

    ensureTeamMember(teamId, newOwnerId, "New owner must be a member of the team");
    ensureUserDoesNotOwnTeamForEvent(eventId, newOwnerId);

    team.setOwnerId(newOwnerId);
    return new TeamResponse(teamDao.update(team));
  }

  public List<UserResponse> getTeamMembers(Long eventId, Long teamId, UserPrincipal user) {
    ensureVerified(user);
    getEventTeam(eventId, teamId);
    ensureTeamMember(teamId, user.getId(), "You are not a member of this team");

    return teamMemberDao.getUsersByTeamId(teamId).stream().map(UserResponse::new).toList();
  }

  public Team getEventTeam(Long eventId, Long teamId) {
    return teamDao
        .findByEventIdAndTeamId(eventId, teamId)
        .orElseThrow(() -> new NotFoundException("Team not found"));
  }

  public Team getOwnedEventTeam(Long eventId, Long teamId, UserPrincipal user) {
    return teamDao
        .findByEventIdAndTeamId(eventId, teamId)
        .filter(team -> team.getOwnerId().equals(user.getId()))
        .orElseThrow(() -> new NotFoundException("Team not found"));
  }

  public Event getEventOrThrow(Long eventId) {
    return eventDao
        .getEventById(eventId)
        .orElseThrow(() -> new NotFoundException("Event not found"));
  }

  public void ensureVerified(UserPrincipal user) {
    if (!user.isFullyVerified()) {
      throw new ForbiddenException("Email and phone verification required");
    }
  }

  public void ensureTeamCanAcceptMember(Long eventId, Long teamId) {
    Event event = getEventOrThrow(eventId);
    Team team = getEventTeam(eventId, teamId);
    ensureTeamEvent(event);
    ensureRegistrationOpen(event);
    if (teamMemberDao.countByTeamId(team.getId()) >= event.getMaxTeamSize()) {
      throw new BadRequestException("Team member limit reached");
    }
  }

  public void ensureNotTeamMember(Long teamId, Long userId, String message) {
    teamMemberDao
        .findByTeamIdAndUserId(teamId, userId)
        .ifPresent(
            existing -> {
              throw new ConflictException(message);
            });
  }

  public void addTeamMember(Long teamId, Long userId) {
    teamMemberDao.create(new TeamMember(userId, teamId));
  }

  private void ensureTeamMember(Long teamId, Long userId, String message) {
    teamMemberDao
        .findByTeamIdAndUserId(teamId, userId)
        .orElseThrow(() -> new NotFoundException(message));
  }

  private void ensureTeamEvent(Event event) {
    if (event.getType() != EventType.TEAM) {
      throw new BadRequestException("Teams can only be created for team events");
    }
  }

  private void ensureRegistrationOpen(Event event) {
    if (event.getStatus() != EventStatus.PUBLISHED) {
      throw new BadRequestException("Event registration is not open");
    }

    if (!Instant.now().isBefore(event.getRegistrationDeadline())) {
      throw new BadRequestException("Event registration deadline has passed");
    }
  }

  private void ensureTeamNameAvailable(Long eventId, String name, Long currentTeamId) {
    teamDao
        .findByEventIdAndName(eventId, name)
        .filter(team -> currentTeamId == null || !team.getId().equals(currentTeamId))
        .ifPresent(
            existing -> {
              throw new ConflictException("Team with the same name already exists");
            });
  }

  private void ensureUserDoesNotOwnTeamForEvent(Long eventId, Long userId) {
    teamDao
        .findByEventIdAndOwnerId(eventId, userId)
        .ifPresent(
            existing -> {
              throw new ConflictException("User already owns a team for this event");
            });
  }

  private void ensureTeamNameNotBlank(String name) {
    if (name.isBlank()) {
      throw new BadRequestException("Team name is required");
    }
  }
}
