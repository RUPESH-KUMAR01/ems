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

  public List<Team> findAll() {
    return currentSession().createQuery("FROM Team", Team.class).getResultList();
  }

  public Optional<Team> getTeamById(Long id) {
    return Optional.ofNullable(get(id));
  }

  public List<Team> findByOwnerId(Long ownerId) {
    return currentSession()
        .createQuery("FROM Team WHERE ownerId = :ownerId", Team.class)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  public Optional<Team> findByOwnerIdAndName(Long ownerId, String name) {
    return currentSession()
        .createQuery("FROM Team WHERE ownerId = :ownerId AND name = :name", Team.class)
        .setParameter("ownerId", ownerId)
        .setParameter("name", name)
        .uniqueResultOptional();
  }

  public boolean delete(Team team) {
    if (team != null) {
      currentSession().remove(team);
      return true;
    } else {
      return false;
    }
  }

  public List<Team> getTeamsByUserId(Long userId) {
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

  public List<Team> getJoinableTeamsForUser(Long userId) {
    return currentSession()
        .createQuery(
            """
            SELECT t
            FROM Team t
            LEFT JOIN TeamMember tm
              ON tm.teamId = t.id
            WHERE NOT EXISTS (
              SELECT memberCheck.id
              FROM TeamMember memberCheck
              WHERE memberCheck.teamId = t.id
                AND memberCheck.userId = :userId
            )
            GROUP BY t.id, t.name, t.ownerId, t.maxMembers, t.createdAt, t.updatedAt
            HAVING COUNT(tm.id) < t.maxMembers
            """,
            Team.class)
        .setParameter("userId", userId)
        .getResultList();
  }
}
