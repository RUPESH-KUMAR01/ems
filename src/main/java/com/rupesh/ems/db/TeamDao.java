package com.rupesh.ems.db;

import com.rupesh.ems.core.Team;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamDao extends AbstractDAO<Team> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TeamDao.class);

  public TeamDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Team create(Team team) {
    LOGGER.info("DAO: Creating team name={} for eventId={}", team.getName(), team.getEventId());
    return persist(team);
  }

  public Team update(Team team) {
    LOGGER.info("DAO: Updating team teamId={}", team.getId());
    return currentSession().merge(team);
  }

  public Optional<Team> getTeamById(Long teamId) {
    LOGGER.debug("DAO: Fetching team by teamId={}", teamId);
    return Optional.ofNullable(get(teamId));
  }

  public Optional<Team> getTeamByIdForUpdate(Long teamId) {
    LOGGER.debug("DAO: Fetching team by teamId={} for update", teamId);
    Team team = currentSession().find(Team.class, teamId, LockModeType.PESSIMISTIC_WRITE);
    if (team != null) {
      currentSession().refresh(team);
    }
    return Optional.ofNullable(team);
  }

  public Optional<Team> findByEventIdAndTeamId(Long eventId, Long teamId) {
    LOGGER.debug("DAO: Fetching team by eventId={} and teamId={}", eventId, teamId);
    return currentSession()
        .createQuery(
            """
            FROM Team
            WHERE id = :teamId
              AND eventId = :eventId
            """,
            Team.class)
        .setParameter("teamId", teamId)
        .setParameter("eventId", eventId)
        .uniqueResultOptional();
  }

  public List<Team> findByEventId(Long eventId) {
    LOGGER.debug("DAO: Fetching teams by eventId={}", eventId);
    return currentSession()
        .createQuery(
            """
            FROM Team
            WHERE eventId = :eventId
            ORDER BY createdAt
            """,
            Team.class)
        .setParameter("eventId", eventId)
        .getResultList();
  }

  public Optional<Team> findByEventIdAndName(Long eventId, String name) {
    LOGGER.debug("DAO: Fetching team by eventId={} and name={}", eventId, name);
    return currentSession()
        .createQuery(
            """
            FROM Team
            WHERE eventId = :eventId
              AND name = :name
            """,
            Team.class)
        .setParameter("eventId", eventId)
        .setParameter("name", name)
        .uniqueResultOptional();
  }

  public Optional<Team> findByEventIdAndOwnerId(Long eventId, Long ownerId) {
    LOGGER.debug("DAO: Fetching team by eventId={} and ownerId={}", eventId, ownerId);
    return currentSession()
        .createQuery(
            """
            FROM Team
            WHERE eventId = :eventId
              AND ownerId = :ownerId
            """,
            Team.class)
        .setParameter("eventId", eventId)
        .setParameter("ownerId", ownerId)
        .uniqueResultOptional();
  }

  public boolean delete(Team team) {
    if (team == null) {
      return false;
    }
    LOGGER.info("DAO: Deleting team teamId={}", team.getId());
    currentSession().remove(team);
    return true;
  }

  public List<Team> findByUserId(Long userId) {
    LOGGER.debug("DAO: Fetching teams by userId={}", userId);
    return currentSession()
        .createQuery(
            """
              SELECT t
              FROM Team t
              JOIN TeamMember tm
                ON tm.teamId = t.id
              WHERE tm.userId = :userId
              """,
            Team.class)
        .setParameter("userId", userId)
        .getResultList();
  }

  public List<Team> findAll() {
    LOGGER.debug("DAO: Fetching all teams");
    return currentSession().createQuery("FROM Team", Team.class).getResultList();
  }
}
