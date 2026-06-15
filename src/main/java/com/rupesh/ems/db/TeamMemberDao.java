package com.rupesh.ems.db;

import com.rupesh.ems.core.TeamMember;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;

public class TeamMemberDao extends AbstractDAO<TeamMember> {

  public TeamMemberDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public TeamMember create(TeamMember teamMember) {
    return persist(teamMember);
  }

  public List<TeamMember> getTeamsByUserId(Long userId) {
    return currentSession()
        .createQuery("FROM TeamMember WHERE userId = :userId", TeamMember.class)
        .setParameter("userId", userId)
        .getResultList();
  }

  public List<TeamMember> getUsersByTeamId(Long teamId) {
    return currentSession()
        .createQuery("FROM TeamMember WHERE teamId = :teamId", TeamMember.class)
        .setParameter("teamId", teamId)
        .getResultList();
  }

  public Long countByTeamId(Long teamId) {
    return currentSession()
        .createQuery("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.teamId = :teamId", Long.class)
        .setParameter("teamId", teamId)
        .uniqueResult();
  }

  public Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId) {

    return currentSession()
        .createQuery(
            "FROM TeamMember WHERE teamId = :teamId AND userId = :userId", TeamMember.class)
        .setParameter("teamId", teamId)
        .setParameter("userId", userId)
        .uniqueResultOptional();
  }

  public boolean delete(TeamMember teamMember) {
    if (teamMember != null) {
      currentSession().remove(teamMember);
      return true;
    } else {
      return false;
    }
  }
}
