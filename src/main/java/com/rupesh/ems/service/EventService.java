package com.rupesh.ems.service;

import com.rupesh.ems.api.event.req.CreateEventRequest;
import com.rupesh.ems.api.event.req.UpdateEventRequest;
import com.rupesh.ems.api.event.res.EventResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.Event;
import com.rupesh.ems.core.EventStatus;
import com.rupesh.ems.core.EventType;
import com.rupesh.ems.core.EventVisibility;
import com.rupesh.ems.db.EventDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventService {

  private static final Map<EventStatus, Set<EventStatus>> ALLOWED_STATUS_TRANSITIONS =
      new EnumMap<>(
          Map.of(
              EventStatus.DRAFT,
              EnumSet.of(EventStatus.PUBLISHED),
              EventStatus.PUBLISHED,
              EnumSet.of(EventStatus.CANCELLED, EventStatus.COMPLETED),
              EventStatus.CANCELLED,
              EnumSet.noneOf(EventStatus.class),
              EventStatus.COMPLETED,
              EnumSet.noneOf(EventStatus.class)));

  private final EventDao eventDao;

  public EventService(EventDao eventDao) {
    this.eventDao = eventDao;
  }

  public EventResponse createEvent(UserPrincipal user, CreateEventRequest request) {

    ensureEventNameAvailable(user.getId(), request.getName(), null);

    Event event =
        new Event(
            request.getName(),
            request.getDescription(),
            user.getId(),
            request.getType(),
            request.getVisibility(),
            request.getMaxParticipants(),
            request.getMinTeamSize(),
            request.getMaxTeamSize(),
            request.getRegistrationFee(),
            request.getRegistrationDeadline(),
            request.getStartTime(),
            request.getEndTime());

    validateEvent(event);

    return new EventResponse(eventDao.create(event));
  }

  public EventResponse updateEvent(UserPrincipal user, Long eventId, UpdateEventRequest request) {

    Event event = getOwnedEvent(eventId, user);
    ensureEventCanBeUpdated(event);

    if (request.getName() != null) {
      ensureEventNameAvailable(user.getId(), request.getName(), eventId);
      event.setName(request.getName());
    }

    if (request.getDescription() != null) {
      event.setDescription(request.getDescription());
    }

    if (event.getStatus() == EventStatus.DRAFT) {

      if (request.getType() != null) {
        event.setType(request.getType());
      }

      if (request.getMaxParticipants() != null) {
        event.setMaxParticipants(request.getMaxParticipants());
      }

      if (request.getMinTeamSize() != null) {
        event.setMinTeamSize(request.getMinTeamSize());
      }

      if (request.getMaxTeamSize() != null) {
        event.setMaxTeamSize(request.getMaxTeamSize());
      }

      if (request.getRegistrationFee() != null) {
        event.setRegistrationFee(request.getRegistrationFee());
      }
    }

    if (request.getVisibility() != null) {
      event.setVisibility(request.getVisibility());
    }

    if (request.getRegistrationDeadline() != null) {
      event.setRegistrationDeadline(request.getRegistrationDeadline());
    }

    if (request.getStartTime() != null) {
      event.setStartTime(request.getStartTime());
    }

    if (request.getEndTime() != null) {
      event.setEndTime(request.getEndTime());
    }

    validateEvent(event);

    return new EventResponse(eventDao.update(event));
  }

  public EventResponse publishEvent(Long eventId, UserPrincipal user) {

    Event event = getOwnedEvent(eventId, user);
    transitionEvent(event, EventStatus.PUBLISHED);

    return new EventResponse(eventDao.update(event));
  }

  public EventResponse cancelEvent(Long eventId, UserPrincipal user) {

    Event event = getOwnedEvent(eventId, user);
    transitionEvent(event, EventStatus.CANCELLED);

    return new EventResponse(eventDao.update(event));
  }

  public EventResponse completeEvent(Long eventId, UserPrincipal user) {

    Event event = getOwnedEvent(eventId, user);
    transitionEvent(event, EventStatus.COMPLETED);

    return new EventResponse(eventDao.update(event));
  }

  public EventResponse getEventById(Long eventId) {
    Event event =
        eventDao.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
    if (!isPubliclyVisible(event)) {
      throw new NotFoundException("Event not found");
    }
    return new EventResponse(event);
  }

  public List<EventResponse> getEventsByCreatedBy(UserPrincipal user) {

    return eventDao.getEventsByCreatedBy(user.getId()).stream().map(EventResponse::new).toList();
  }

  public List<EventResponse> getEventsByName(String name) {

    return eventDao.getEventsByName(name).stream()
        .filter(this::isPubliclyVisible)
        .map(EventResponse::new)
        .toList();
  }

  public List<EventResponse> getAllEvents() {

    return eventDao.getAllEvents().stream().map(EventResponse::new).toList();
  }

  public List<EventResponse> getVisibleEvents() {
    return eventDao.getVisibleEvents().stream().map(EventResponse::new).toList();
  }

  public void deleteEvent(UserPrincipal user, Long eventId) {

    Event event = getOwnedEvent(eventId, user);

    eventDao.delete(event);
  }

  private Event getOwnedEvent(Long eventId, UserPrincipal user) {

    return eventDao
        .getEventById(eventId)
        .filter(event -> event.getCreatedBy().equals(user.getId()))
        .orElseThrow(() -> new NotFoundException("Event not found"));
  }

  private void ensureEventNameAvailable(Long ownerId, String name, Long currentEventId) {

    eventDao
        .findByNameAndCreatedBy(ownerId, name)
        .filter(event -> currentEventId == null || !event.getId().equals(currentEventId))
        .ifPresent(
            event -> {
              throw new ConflictException("Event with same name already exists");
            });
  }

  private void validateEvent(Event event) {

    if (event.getRegistrationDeadline() == null) {
      throw new BadRequestException("Registration deadline is required");
    }

    if (event.getStartTime() == null) {
      throw new BadRequestException("Event start time is required");
    }

    if (event.getEndTime() == null) {
      throw new BadRequestException("Event end time is required");
    }

    if (event.getRegistrationFee() == null) {
      throw new BadRequestException("Registration fee is required");
    }

    if (event.getRegistrationFee().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException("Registration fee cannot be negative");
    }

    if (!event.getRegistrationDeadline().isBefore(event.getStartTime())) {
      throw new BadRequestException("Registration deadline must be before event start time");
    }

    if (!event.getStartTime().isBefore(event.getEndTime())) {
      throw new BadRequestException("Event start time must be before end time");
    }

    if (event.getType() == EventType.SOLO) {

      if (event.getMinTeamSize() != null || event.getMaxTeamSize() != null) {

        throw new BadRequestException("Solo events cannot have team sizes");
      }

    } else {

      if (event.getMinTeamSize() == null || event.getMaxTeamSize() == null) {

        throw new BadRequestException("Team events must define team sizes");
      }

      if (event.getMinTeamSize() > event.getMaxTeamSize()) {

        throw new BadRequestException("Minimum team size cannot exceed maximum team size");
      }
    }
  }

  private void ensureEventCanBeUpdated(Event event) {
    if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
      throw new BadRequestException("Cancelled or completed events cannot be updated");
    }
  }

  private void transitionEvent(Event event, EventStatus newStatus) {
    EventStatus currentStatus = event.getStatus();
    if (!ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
      throw new BadRequestException(
          "Invalid event status transition from %s to %s".formatted(currentStatus, newStatus));
    }

    validateEvent(event);
    event.setStatus(newStatus);
  }

  private boolean isPubliclyVisible(Event event) {
    return event.getStatus() == EventStatus.PUBLISHED
        && event.getVisibility() == EventVisibility.PUBLIC;
  }
}
