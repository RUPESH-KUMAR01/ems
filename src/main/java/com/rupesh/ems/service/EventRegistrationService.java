package com.rupesh.ems.service;

import com.rupesh.ems.api.registration.res.EventRegistrationResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.*;
import com.rupesh.ems.db.EventDao;
import com.rupesh.ems.db.EventRegistrationDao;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.ForbiddenException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventRegistrationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventRegistrationService.class);

  private final EventDao eventDao;
  private final TeamDao teamDao;
  private final EventRegistrationDao registrationDao;

  public EventRegistrationService(
      EventDao eventDao, TeamDao teamDao, EventRegistrationDao registrationDao) {
    this.eventDao = eventDao;
    this.teamDao = teamDao;
    this.registrationDao = registrationDao;
  }

  public EventRegistrationResponse register(Long eventId, Long teamId, UserPrincipal user) {
    LOGGER.info("Registering user {} for event {} with teamId {}", user.getId(), eventId, teamId);
    Event event =
        eventDao
            .getEventById(eventId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("Event with id {} not found", eventId);
                  return new NotFoundException("Event not found");
                });

    if (event.getStatus() != EventStatus.PUBLISHED) {
      LOGGER.warn("Event with id {} is not published", eventId);
      throw new BadRequestException("Event is not published");
    }

    if (Instant.now().isAfter(event.getRegistrationDeadline())) {
      LOGGER.warn("Registration deadline has passed for event with id {}", eventId);
      throw new BadRequestException("Registration deadline has passed");
    }

    if (event.getType() == EventType.SOLO) {
      LOGGER.info("Registering user {} for solo event {}", user.getId(), eventId);
      registrationDao
          .findByEventIdAndUserId(eventId, user.getId())
          .ifPresent(
              r -> {
                LOGGER.warn(
                    "User with id {} is already registered for event with id {}",
                    user.getId(),
                    eventId);
                throw new ConflictException("Already registered");
              });

      RegistrationStatus initialStatus =
          (event.getRegistrationFee() == null
                  || event.getRegistrationFee().compareTo(java.math.BigDecimal.ZERO) == 0)
              ? RegistrationStatus.REGISTERED
              : RegistrationStatus.PENDING;
      LOGGER.info("Initial registration status for user {} is {}", user.getId(), initialStatus);
      EventRegistration registration =
          new EventRegistration(eventId, user.getId(), null, initialStatus);
      LOGGER.info("Created registration for user {}: {}", user.getId(), registration);
      return new EventRegistrationResponse(registrationDao.create(registration));
    }

    if (teamId == null) {
      LOGGER.warn("Team ID is required for team event registration for user {}", user.getId());
      throw new BadRequestException("Team is required");
    }

    Team team =
        teamDao
            .findByEventIdAndTeamId(eventId, teamId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("Team with id {} not found for event with id {}", teamId, eventId);
                  return new NotFoundException("Team not found");
                });

    if (!team.getOwnerId().equals(user.getId())) {
      LOGGER.warn("User with id {} is not the owner of team with id {}", user.getId(), teamId);
      throw new BadRequestException("Only the team owner can register the team");
    }

    if (team.getMemberCount() < event.getMinTeamSize()) {
      LOGGER.warn(
          "Team with id {} does not meet the minimum size requirement for event with id {}",
          teamId,
          eventId);
      throw new BadRequestException("Team size does not meet the minimum requirement");
    }

    registrationDao
        .findByEventIdAndTeamId(eventId, teamId)
        .ifPresent(
            r -> {
              LOGGER.warn(
                  "Team with id {} is already registered for event with id {}", teamId, eventId);
              throw new ConflictException("Team already registered");
            });

    RegistrationStatus initialStatus =
        (event.getRegistrationFee() == null
                || event.getRegistrationFee().compareTo(java.math.BigDecimal.ZERO) == 0)
            ? RegistrationStatus.REGISTERED
            : RegistrationStatus.PENDING;
    LOGGER.info("Initial registration status for team {} is {}", teamId, initialStatus);
    EventRegistration registration = new EventRegistration(eventId, null, teamId, initialStatus);
    LOGGER.info("Created registration for team {}: {}", teamId, registration);
    return new EventRegistrationResponse(registrationDao.create(registration));
  }

  public void cancel(Long registrationId, UserPrincipal user) {
    LOGGER.info("Attempting to cancel registration with ID {}", registrationId);
    EventRegistration registration =
        registrationDao
            .getById(registrationId)
            .orElseThrow(() -> new NotFoundException("Registration not found"));

    if (registration.isSoloRegistration()) {
      if (!registration.getUserId().equals(user.getId())) {
        LOGGER.warn(
            "User with id {} is not authorized to cancel registration with id {}",
            user.getId(),
            registrationId);
        throw new ForbiddenException("You can only cancel your own registration");
      }
    } else if (registration.isTeamRegistration()) {
      Team team =
          teamDao
              .getTeamById(registration.getTeamId())
              .orElseThrow(() -> new NotFoundException("Team not found"));
      if (!team.getOwnerId().equals(user.getId())) {
        LOGGER.warn(
            "User with id {} is not the owner of team with id {}", user.getId(), team.getId());
        throw new ForbiddenException("Only the team owner can cancel the registration");
      }
    }

    registration.setStatus(RegistrationStatus.CANCELLED);
    LOGGER.info("Registration with ID {} has been cancelled", registrationId);
    registrationDao.update(registration);
  }
}
