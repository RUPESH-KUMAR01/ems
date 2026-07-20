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
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventService {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

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

    LOGGER.info("Creating event={} by userId={}", request.getName(), user.getId());

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
    event = eventDao.create(event);
    LOGGER.info("Event created successfully with eventId={}", event.getId());
    return new EventResponse(event);
  }

  public EventResponse updateEvent(UserPrincipal user, Long eventId, UpdateEventRequest request) {
    LOGGER.info("Updating eventId={} by userId={}", eventId, user.getId());

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
    LOGGER.info("Event with eventId={} updated successfully", event.getId());
    return new EventResponse(eventDao.update(event));
  }

  public EventResponse publishEvent(Long eventId, UserPrincipal user) {
    LOGGER.info("Publishing eventId={} by userId={}", eventId, user.getId());

    Event event = getOwnedEvent(eventId, user);
    if (Instant.now().isAfter(event.getRegistrationDeadline())) {
      LOGGER.warn("Event with eventId={} cannot be published after it has started", eventId);
      throw new BadRequestException("Event cannot be published after it has started");
    }
    transitionEvent(event, EventStatus.PUBLISHED);

    return new EventResponse(eventDao.update(event));
  }

  public EventResponse cancelEvent(Long eventId, UserPrincipal user) {
    LOGGER.info("Cancelling eventId={} by userId={}", eventId, user.getId());

    Event event = getOwnedEvent(eventId, user);
    if (Instant.now().isAfter(event.getStartTime())) {
      LOGGER.warn("Event with eventId={} cannot be cancelled after it has started", eventId);
      throw new BadRequestException("Event cannot be cancelled after it has started");
    }
    transitionEvent(event, EventStatus.CANCELLED);

    return new EventResponse(eventDao.update(event));
  }

  public EventResponse completeEvent(Long eventId, UserPrincipal user) {
    LOGGER.info("Completing eventId={} by userId={}", eventId, user.getId());

    Event event = getOwnedEvent(eventId, user);
    if (Instant.now().isBefore(event.getEndTime())) {
      LOGGER.warn("Event with eventId={} cannot be completed before its end time", eventId);
      throw new BadRequestException("Event cannot be completed before its end time");
    }
    transitionEvent(event, EventStatus.COMPLETED);

    return new EventResponse(eventDao.update(event));
  }

  public EventResponse getEventById(Long eventId) {
    LOGGER.info("Fetching eventId={}", eventId);
    Event event =
        eventDao.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
    if (!isPubliclyVisible(event)) {
      LOGGER.warn("Event with eventId={} is not publicly visible", eventId);
      throw new NotFoundException("Event not found");
    }
    return new EventResponse(event);
  }

  public List<EventResponse> getEventsByCreatedBy(UserPrincipal user) {
    LOGGER.info("Fetching events for userId={}", user.getId());
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
    LOGGER.info("Fetching visible events");
    return eventDao.getVisibleEvents().stream().map(EventResponse::new).toList();
  }

  public void deleteEvent(UserPrincipal user, Long eventId) {

    Event event = getOwnedEvent(eventId, user);

    boolean isDeleted = eventDao.delete(event);
    if (isDeleted) {
      LOGGER.info("Event with eventId={} deleted successfully by userId={}", eventId, user.getId());
    } else {
      LOGGER.warn("Failed to delete event with eventId={} by userId={}", eventId, user.getId());
      throw new NotFoundException("Event not found");
    }
  }

  private Event getOwnedEvent(Long eventId, UserPrincipal user) {

    LOGGER.info("Fetching owned event eventId={} by userId={}", eventId, user.getId());
    return eventDao
        .getEventById(eventId)
        .filter(event -> event.getCreatedBy().equals(user.getId()))
        .orElseThrow(
            () -> {
              LOGGER.warn("Event with eventId={} not found for userId={}", eventId, user.getId());
              return new NotFoundException("Event not found");
            });
  }

  private void ensureEventNameAvailable(Long ownerId, String name, Long currentEventId) {

    LOGGER.info("Checking availability of event name={} for userId={}", name, ownerId);
    eventDao
        .findByNameAndCreatedBy(ownerId, name)
        .filter(event -> currentEventId == null || !event.getId().equals(currentEventId))
        .ifPresent(
            event -> {
              LOGGER.warn("Event with name={} already exists for userId={}", name, ownerId);
              throw new ConflictException("Event with same name already exists");
            });
  }

  private void validateEvent(Event event) {

    LOGGER.info("Validating event={}", event.getName());
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
    LOGGER.info("Event={} validation successful", event.getName());
  }

  private void ensureEventCanBeUpdated(Event event) {
    if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
      LOGGER.warn(
          "Event with eventId={} cannot be updated as it is in status={}",
          event.getId(),
          event.getStatus());
      throw new BadRequestException("Cancelled or completed events cannot be updated");
    }
  }

  private void transitionEvent(Event event, EventStatus newStatus) {
    LOGGER.info(
        "Transitioning eventId={} from status={} to status={}",
        event.getId(),
        event.getStatus(),
        newStatus);
    EventStatus currentStatus = event.getStatus();
    if (!ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
      LOGGER.warn(
          "Invalid event status transition from {} to {} for eventId={}",
          currentStatus,
          newStatus,
          event.getId());
      throw new BadRequestException(
          "Invalid event status transition from %s to %s".formatted(currentStatus, newStatus));
    }

    validateEvent(event);
    LOGGER.info(
        "Event with eventId={} transitioned from status={} to status={}",
        event.getId(),
        currentStatus,
        newStatus);
    event.setStatus(newStatus);
  }

  private boolean isPubliclyVisible(Event event) {
    return event.getStatus() == EventStatus.PUBLISHED
        && event.getVisibility() == EventVisibility.PUBLIC;
  }
}
