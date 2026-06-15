package com.rupesh.ems.db;

import com.rupesh.ems.core.RequestStatus;
import com.rupesh.ems.core.TeamMembershipRequest;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;

public class TeamMembershipRequestDao extends AbstractDAO<TeamMembershipRequest> {

  public TeamMembershipRequestDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public TeamMembershipRequest create(TeamMembershipRequest request) {
    return persist(request);
  }

  public TeamMembershipRequest update(TeamMembershipRequest request) {
    return currentSession().merge(request);
  }

  public Optional<TeamMembershipRequest> findByTeamIdAndUserId(Long teamId, Long userId) {

    return currentSession()
        .createQuery(
            """
            FROM TeamMembershipRequest
            WHERE teamId = :teamId
              AND userId = :userId
            """,
            TeamMembershipRequest.class)
        .setParameter("teamId", teamId)
        .setParameter("userId", userId)
        .uniqueResultOptional();
  }

  public List<TeamMembershipRequest> getRequestsByTeamId(Long teamId) {

    return currentSession()
        .createQuery(
            """
            FROM TeamMembershipRequest
            WHERE teamId = :teamId
            """,
            TeamMembershipRequest.class)
        .setParameter("teamId", teamId)
        .getResultList();
  }

  public List<TeamMembershipRequest> getRequestsByUserId(Long userId) {

    return currentSession()
        .createQuery(
            """
            FROM TeamMembershipRequest
            WHERE userId = :userId
            """,
            TeamMembershipRequest.class)
        .setParameter("userId", userId)
        .getResultList();
  }

  public List<TeamMembershipRequest> getPendingRequestsByTeamId(Long teamId) {

    return currentSession()
        .createQuery(
            """
            FROM TeamMembershipRequest
            WHERE teamId = :teamId
              AND status = :status
            """,
            TeamMembershipRequest.class)
        .setParameter("teamId", teamId)
        .setParameter("status", RequestStatus.PENDING)
        .getResultList();
  }

  public boolean delete(TeamMembershipRequest request) {

    if (request != null) {
      currentSession().remove(request);
      return true;
    }

    return false;
  }

  public void deleteByTeamId(Long teamId) {
    currentSession()
        .createMutationQuery("DELETE FROM TeamMembershipRequest WHERE teamId = :teamId")
        .setParameter("teamId", teamId)
        .executeUpdate();
  }
}
