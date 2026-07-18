package com.rupesh.ems.resources;

import com.rupesh.ems.api.common.MessageResponse;
import com.rupesh.ems.api.event.req.CreateEventRequest;
import com.rupesh.ems.api.event.req.UpdateEventRequest;
import com.rupesh.ems.api.event.res.EventResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.EventService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventResource.class);

  private final EventService eventService;

  public EventResource(EventService eventService) {
    this.eventService = eventService;
  }

  @POST
  @UnitOfWork
  @RolesAllowed("MODERATOR")
  public EventResponse createEvent(@Auth UserPrincipal user, @Valid CreateEventRequest request) {
    return eventService.createEvent(user, request);
  }

  @PUT
  @Path("/{eventId}")
  @RolesAllowed("MODERATOR")
  @UnitOfWork
  public EventResponse updateEvent(
      @Auth UserPrincipal user,
      @PathParam("eventId") Long eventId,
      @Valid UpdateEventRequest request) {
    return eventService.updateEvent(user, eventId, request);
  }

  @DELETE
  @Path("/{eventId}")
  @RolesAllowed("MODERATOR")
  @UnitOfWork
  public MessageResponse deleteEvent(@Auth UserPrincipal user, @PathParam("eventId") Long eventId) {
    eventService.deleteEvent(user, eventId);
    return new MessageResponse("Event deleted successfully");
  }

  @GET
  @Path("/{eventId}")
  @UnitOfWork
  public EventResponse getEventById(@PathParam("eventId") Long eventId) {
    return eventService.getEventById(eventId);
  }

  @GET
  @Path("/me")
  @UnitOfWork
  @RolesAllowed("MODERATOR")
  public List<EventResponse> getMyEvents(@Auth UserPrincipal user) {
    return eventService.getEventsByCreatedBy(user);
  }

  @GET
  @UnitOfWork
  public List<EventResponse> getVisibleEvents() {
    return eventService.getVisibleEvents();
  }

  @PUT
  @Path("/{eventId}/publish")
  @RolesAllowed("MODERATOR")
  @UnitOfWork
  public EventResponse publishEvent(@Auth UserPrincipal user, @PathParam("eventId") Long eventId) {
    return eventService.publishEvent(eventId, user);
  }

  @PUT
  @Path("/{eventId}/cancel")
  @RolesAllowed("MODERATOR")
  @UnitOfWork
  public EventResponse cancelEvent(@Auth UserPrincipal user, @PathParam("eventId") Long eventId) {
    return eventService.cancelEvent(eventId, user);
  }

  @PUT
  @RolesAllowed("MODERATOR")
  @Path("/{eventId}/complete")
  @UnitOfWork
  public EventResponse completeEvent(@Auth UserPrincipal user, @PathParam("eventId") Long eventId) {
    return eventService.completeEvent(eventId, user);
  }
}
