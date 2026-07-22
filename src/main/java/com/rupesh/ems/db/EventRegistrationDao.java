package com.rupesh.ems.db;

import com.rupesh.ems.core.EventRegistration;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventRegistrationDao extends AbstractDAO<EventRegistration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventRegistrationDao.class);

  public EventRegistrationDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public EventRegistration create(EventRegistration registration) {
    LOGGER.info(
        "DAO: Creating registration eventId={} userId={} teamId={}",
        registration.getEventId(),
        registration.getUserId(),
        registration.getTeamId());
    return persist(registration);
  }

  public EventRegistration update(EventRegistration registration) {
    LOGGER.info(
        "DAO: Updating registration id={} status={}",
        registration.getId(),
        registration.getStatus());
    return currentSession().merge(registration);
  }

  public Optional<EventRegistration> getById(Long id) {
    LOGGER.debug("DAO: Fetching registration by id={}", id);
    return Optional.ofNullable(get(id));
  }

  public Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId) {
    LOGGER.debug("DAO: Fetching registration by eventId={} and userId={}", eventId, userId);
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
    LOGGER.debug("DAO: Fetching registration by eventId={} and teamId={}", eventId, teamId);
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
    LOGGER.debug("DAO: Fetching registrations by eventId={}", eventId);
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
    LOGGER.debug("DAO: Fetching registrations by userId={}", userId);
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
    LOGGER.debug("DAO: Fetching all registrations");
    return currentSession()
        .createQuery("FROM EventRegistration", EventRegistration.class)
        .getResultList();
  }

  public boolean delete(EventRegistration registration) {
    if (registration == null) {
      return false;
    }
    LOGGER.info("DAO: Deleting registration id={}", registration.getId());
    currentSession().remove(registration);
    return true;
  }
}
