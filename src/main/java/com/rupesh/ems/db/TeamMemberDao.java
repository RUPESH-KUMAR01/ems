package com.rupesh.ems.db;

import com.rupesh.ems.core.TeamMember;
import com.rupesh.ems.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;

public class TeamMemberDao extends AbstractDAO<TeamMember> {

  public TeamMemberDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public TeamMember create(TeamMember teamMember) {
    persist(teamMember);
    currentSession().flush();
    return teamMember;
  }

  public List<TeamMember> getTeamsByUserId(Long userId) {
    return currentSession()
        .createQuery("FROM TeamMember WHERE userId = :userId", TeamMember.class)
        .setParameter("userId", userId)
        .getResultList();
  }

  public List<User> getUsersByTeamId(Long teamId) {
    return currentSession()
        .createQuery(
            """
                SELECT u
                FROM User u
                JOIN TeamMember tm
                  ON tm.userId = u.id
                WHERE tm.teamId = :teamId
                """,
            User.class)
        .setParameter("teamId", teamId)
        .getResultList();
  }

  public long countByTeamId(Long teamId) {
    return currentSession()
        .createQuery("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.teamId = :teamId", Long.class)
        .setParameter("teamId", teamId)
        .uniqueResult();
  }

  public List<TeamMember> findByTeamId(Long teamId) {
    return currentSession()
        .createQuery("FROM TeamMember WHERE teamId = :teamId", TeamMember.class)
        .setParameter("teamId", teamId)
        .getResultList();
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

  public void deleteByTeamId(Long teamId) {
    currentSession()
        .createMutationQuery("DELETE FROM TeamMember WHERE teamId = :teamId")
        .setParameter("teamId", teamId)
        .executeUpdate();
  }

  public Optional<TeamMember> getMembershipByEventIdAndUserId(Long eventId, Long userId) {
    return currentSession()
        .createQuery(
            """
                SELECT tm
                FROM TeamMember tm
                JOIN Team t
                  ON t.id = tm.teamId
                WHERE t.eventId = :eventId AND tm.userId = :userId
                """,
            TeamMember.class)
        .setParameter("eventId", eventId)
        .setParameter("userId", userId)
        .uniqueResultOptional();
  }
}
