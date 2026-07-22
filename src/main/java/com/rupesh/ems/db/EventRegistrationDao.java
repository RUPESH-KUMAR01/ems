package com.rupesh.ems.db;

import com.rupesh.ems.core.EventRegistration;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;

public class EventRegistrationDao extends AbstractDAO<EventRegistration> {

  public EventRegistrationDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public EventRegistration create(EventRegistration registration) {
    return persist(registration);
  }

  public EventRegistration update(EventRegistration registration) {
    return currentSession().merge(registration);
  }

  public Optional<EventRegistration> getById(Long id) {
    return Optional.ofNullable(get(id));
  }

  public Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId) {
    return currentSession()
        .createQuery(
            """
            FROM EventRegistration
            WHERE eventId = :eventId
              AND userId = :userId
            """,
            EventRegistration.class)
        .setParameter("eventId", eventId)
        .setParameter("userId", userId)
        .uniqueResultOptional();
  }

  public Optional<EventRegistration> findByEventIdAndTeamId(Long eventId, Long teamId) {
    return currentSession()
        .createQuery(
            """
            FROM EventRegistration
            WHERE eventId = :eventId
              AND teamId = :teamId
            """,
            EventRegistration.class)
        .setParameter("eventId", eventId)
        .setParameter("teamId", teamId)
        .uniqueResultOptional();
  }

  public List<EventRegistration> findByEventId(Long eventId) {
    return currentSession()
        .createQuery(
            """
            FROM EventRegistration
            WHERE eventId = :eventId
            """,
            EventRegistration.class)
        .setParameter("eventId", eventId)
        .getResultList();
  }

  public List<EventRegistration> findByUserId(Long userId) {
    return currentSession()
        .createQuery(
            """
            FROM EventRegistration
            WHERE userId = :userId
            """,
            EventRegistration.class)
        .setParameter("userId", userId)
        .getResultList();
  }

  public List<EventRegistration> findAll() {
    return currentSession()
        .createQuery("FROM EventRegistration", EventRegistration.class)
        .getResultList();
  }

  public boolean delete(EventRegistration registration) {
    if (registration == null) {
      return false;
    }

    currentSession().remove(registration);
    return true;
  }
}
