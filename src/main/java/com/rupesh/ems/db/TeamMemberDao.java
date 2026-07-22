package com.rupesh.ems.db;

import com.rupesh.ems.core.TeamMember;
import com.rupesh.ems.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamMemberDao extends AbstractDAO<TeamMember> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TeamMemberDao.class);

  public TeamMemberDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public TeamMember create(TeamMember teamMember) {
    LOGGER.info(
        "DAO: Creating team member userId={} teamId={}",
        teamMember.getUserId(),
        teamMember.getTeamId());
    persist(teamMember);
    currentSession().flush();
    return teamMember;
  }

  public List<TeamMember> getTeamsByUserId(Long userId) {
    LOGGER.debug("DAO: Fetching team memberships for userId={}", userId);
    return currentSession()
        .createQuery("FROM TeamMember WHERE userId = :userId", TeamMember.class)
        .setParameter("userId", userId)
        .getResultList();
  }

  public List<User> getUsersByTeamId(Long teamId) {
    LOGGER.debug("DAO: Fetching users for teamId={}", teamId);
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
    LOGGER.debug("DAO: Counting members for teamId={}", teamId);
    return currentSession()
        .createQuery("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.teamId = :teamId", Long.class)
        .setParameter("teamId", teamId)
        .uniqueResult();
  }

  public List<TeamMember> findByTeamId(Long teamId) {
    LOGGER.debug("DAO: Fetching team members for teamId={}", teamId);
    return currentSession()
        .createQuery("FROM TeamMember WHERE teamId = :teamId", TeamMember.class)
        .setParameter("teamId", teamId)
        .getResultList();
  }

  public Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId) {
    LOGGER.debug("DAO: Fetching team member by teamId={} and userId={}", teamId, userId);
    return currentSession()
        .createQuery(
            "FROM TeamMember WHERE teamId = :teamId AND userId = :userId", TeamMember.class)
        .setParameter("teamId", teamId)
        .setParameter("userId", userId)
        .uniqueResultOptional();
  }

  public boolean delete(TeamMember teamMember) {
    if (teamMember != null) {
      LOGGER.info(
          "DAO: Deleting team member userId={} teamId={}",
          teamMember.getUserId(),
          teamMember.getTeamId());
      currentSession().remove(teamMember);
      return true;
    } else {
      return false;
    }
  }

  public void deleteByTeamId(Long teamId) {
    LOGGER.info("DAO: Deleting all team members for teamId={}", teamId);
    currentSession()
        .createMutationQuery("DELETE FROM TeamMember WHERE teamId = :teamId")
        .setParameter("teamId", teamId)
        .executeUpdate();
  }

  public Optional<TeamMember> getMembershipByEventIdAndUserId(Long eventId, Long userId) {
    LOGGER.debug("DAO: Fetching team membership for eventId={} and userId={}", eventId, userId);
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
