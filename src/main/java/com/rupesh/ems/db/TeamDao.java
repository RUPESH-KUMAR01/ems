package com.rupesh.ems.db;

import com.rupesh.ems.core.Team;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;

public class TeamDao extends AbstractDAO<Team> {

  public TeamDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Team create(Team team) {
    return persist(team);
  }

  public Team update(Team team) {
    return currentSession().merge(team);
  }

  public Optional<Team> getTeamById(Long teamId) {
    return Optional.ofNullable(get(teamId));
  }

  public Optional<Team> findByEventIdAndTeamId(Long eventId, Long teamId) {
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

    currentSession().remove(team);
    return true;
  }

  public List<Team> findByUserId(Long userId) {
      return currentSession()
          .createQuery("""
              SELECT t
              FROM Team t
              JOIN TeamMember tm
                ON tm.teamId = t.id
              WHERE tm.userId = :userId
              """, Team.class)
          .setParameter("userId", userId)
          .getResultList();
  }
}