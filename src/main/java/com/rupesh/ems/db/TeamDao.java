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

  public boolean delete(Long id) {
    Team team = get(id);
    if (team != null) {
      currentSession().remove(team);
      return true;
    } else {
      return false;
    }
  }
}
