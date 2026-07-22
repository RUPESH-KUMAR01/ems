package com.rupesh.ems.db;

import com.rupesh.ems.core.Event;
import com.rupesh.ems.core.EventStatus;
import com.rupesh.ems.core.EventVisibility;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventDao extends AbstractDAO<Event> {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventDao.class);

  public EventDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Event create(Event event) {
    LOGGER.info("DAO: Creating event name={} createdBy={}", event.getName(), event.getCreatedBy());
    return persist(event);
  }

  public Event update(Event event) {
    LOGGER.info("DAO: Updating event eventId={}", event.getId());
    return currentSession().merge(event);
  }

  public Optional<Event> getEventById(Long id) {
    LOGGER.debug("DAO: Fetching event by id={}", id);
    return Optional.ofNullable(get(id));
  }

  public boolean delete(Event event) {
    if (event != null) {
      LOGGER.info("DAO: Deleting event eventId={}", event.getId());
      currentSession().remove(event);
      return true;
    } else {
      return false;
    }
  }

  public List<Event> getEventsByCreatedBy(Long createdBy) {
    LOGGER.debug("DAO: Fetching events by createdBy={}", createdBy);
    return currentSession()
        .createQuery("FROM Event WHERE createdBy = :createdBy", Event.class)
        .setParameter("createdBy", createdBy)
        .getResultList();
  }

  public List<Event> getEventsByName(String name) {
    LOGGER.debug("DAO: Fetching events by name={}", name);
    return currentSession()
        .createQuery("FROM Event WHERE name = :name", Event.class)
        .setParameter("name", name)
        .getResultList();
  }

  public List<Event> getAllEvents() {
    LOGGER.debug("DAO: Fetching all events");
    return currentSession().createQuery("FROM Event", Event.class).getResultList();
  }

  public Optional<Event> findByNameAndCreatedBy(Long ownerId, String name) {
    LOGGER.debug("DAO: Fetching event by ownerId={} and name={}", ownerId, name);
    return currentSession()
        .createQuery("FROM Event WHERE createdBy = :ownerId AND name = :name", Event.class)
        .setParameter("ownerId", ownerId)
        .setParameter("name", name)
        .uniqueResultOptional();
  }

  public List<Event> getVisibleEvents() {
    LOGGER.debug("DAO: Fetching visible events");
    return currentSession()
        .createQuery(
            """
          FROM Event
          WHERE status = :status
            AND visibility = :visibility
          """,
            Event.class)
        .setParameter("status", EventStatus.PUBLISHED)
        .setParameter("visibility", EventVisibility.PUBLIC)
        .getResultList();
  }
}
