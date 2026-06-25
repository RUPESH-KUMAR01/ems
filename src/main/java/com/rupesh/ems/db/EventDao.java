package com.rupesh.ems.db;

import com.rupesh.ems.core.Event;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;

public class EventDao extends AbstractDAO<Event> {

  public EventDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Event create(Event event) {
    return persist(event);
  }

  public Event update(Event event) {
    return currentSession().merge(event);
  }

  public Optional<Event> getEventById(Long id) {
    return Optional.ofNullable(get(id));
  }

  public boolean delete(Event event) {
    if (event != null) {
      currentSession().remove(event);
      return true;
    } else {
      return false;
    }
  }

  public List<Event> getEventsByCreatedBy(Long createdBy) {
    return currentSession()
        .createQuery("FROM Event WHERE createdBy = :createdBy", Event.class)
        .setParameter("createdBy", createdBy)
        .getResultList();
  }

  public List<Event> getEventsByName(String name) {
    return currentSession()
        .createQuery("FROM Event WHERE name = :name", Event.class)
        .setParameter("name", name)
        .getResultList();
  }

  public List<Event> getAllEvents() {
    return currentSession().createQuery("FROM Event", Event.class).getResultList();
  }

  public Optional<Event> findByNameAndCreatedBy(Long ownerId, String name) {
    return currentSession()
        .createQuery("FROM Event WHERE createdBy = :ownerId AND name = :name", Event.class)
        .setParameter("ownerId", ownerId)
        .setParameter("name", name)
        .uniqueResultOptional();
  }
}
