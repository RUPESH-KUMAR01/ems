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

public class EventRegistrationService {

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

    Event event =
        eventDao.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));

    if (event.getType() == EventType.SOLO) {

      registrationDao
          .findByEventIdAndUserId(eventId, user.getId())
          .ifPresent(
              r -> {
                throw new ConflictException("Already registered");
              });

      RegistrationStatus initialStatus =
          (event.getRegistrationFee() == null
                  || event.getRegistrationFee().compareTo(java.math.BigDecimal.ZERO) == 0)
              ? RegistrationStatus.REGISTERED
              : RegistrationStatus.PENDING;

      EventRegistration registration =
          new EventRegistration(eventId, user.getId(), null, initialStatus);

      return new EventRegistrationResponse(registrationDao.create(registration));
    }

    if (teamId == null) {
      throw new BadRequestException("Team is required");
    }

    teamDao
        .findByEventIdAndTeamId(eventId, teamId)
        .orElseThrow(() -> new NotFoundException("Team not found"));

    registrationDao
        .findByEventIdAndTeamId(eventId, teamId)
        .ifPresent(
            r -> {
              throw new ConflictException("Team already registered");
            });

    RegistrationStatus initialStatus =
        (event.getRegistrationFee() == null
                || event.getRegistrationFee().compareTo(java.math.BigDecimal.ZERO) == 0)
            ? RegistrationStatus.REGISTERED
            : RegistrationStatus.PENDING;

    EventRegistration registration = new EventRegistration(eventId, null, teamId, initialStatus);

    return new EventRegistrationResponse(registrationDao.create(registration));
  }

  public void cancel(Long registrationId, UserPrincipal user) {

    EventRegistration registration =
        registrationDao
            .getById(registrationId)
            .orElseThrow(() -> new NotFoundException("Registration not found"));

    if (registration.isSoloRegistration()) {
      if (!registration.getUserId().equals(user.getId())) {
        throw new ForbiddenException("You can only cancel your own registration");
      }
    } else if (registration.isTeamRegistration()) {
      Team team =
          teamDao
              .getTeamById(registration.getTeamId())
              .orElseThrow(() -> new NotFoundException("Team not found"));
      if (!team.getOwnerId().equals(user.getId())) {
        throw new ForbiddenException("Only the team owner can cancel the registration");
      }
    }

    registration.setStatus(RegistrationStatus.CANCELLED);

    registrationDao.update(registration);
  }
}
