package com.rupesh.ems.db;

import com.rupesh.ems.core.RequestStatus;
import com.rupesh.ems.core.TeamMembershipRequest;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamMembershipRequestDao extends AbstractDAO<TeamMembershipRequest> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TeamMembershipRequestDao.class);

  public TeamMembershipRequestDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public TeamMembershipRequest create(TeamMembershipRequest request) {
    LOGGER.info(
        "DAO: Creating membership request userId={} teamId={} type={}",
        request.getUserId(),
        request.getTeamId(),
        request.getType());
    return persist(request);
  }

  public TeamMembershipRequest update(TeamMembershipRequest request) {
    LOGGER.info(
        "DAO: Updating membership request id={} status={}", request.getId(), request.getStatus());
    return currentSession().merge(request);
  }

  public Optional<TeamMembershipRequest> findByTeamIdAndUserId(Long teamId, Long userId) {
    LOGGER.debug("DAO: Fetching membership request for teamId={} and userId={}", teamId, userId);
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
    LOGGER.debug("DAO: Fetching membership requests for teamId={}", teamId);
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
    LOGGER.debug("DAO: Fetching membership requests for userId={}", userId);
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
    LOGGER.debug("DAO: Fetching pending requests for teamId={}", teamId);
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

  public List<TeamMembershipRequest> getPendingRequestsByUserId(Long userId) {
    LOGGER.debug("DAO: Fetching pending requests for userId={}", userId);
    return currentSession()
        .createQuery(
            """
            FROM TeamMembershipRequest
            WHERE userId = :userId
              AND status = :status
            """,
            TeamMembershipRequest.class)
        .setParameter("userId", userId)
        .setParameter("status", RequestStatus.PENDING)
        .getResultList();
  }

  public boolean delete(TeamMembershipRequest request) {
    if (request != null) {
      LOGGER.info("DAO: Deleting membership request id={}", request.getId());
      currentSession().remove(request);
      return true;
    }
    return false;
  }

  public void deleteByTeamId(Long teamId) {
    LOGGER.info("DAO: Deleting all membership requests for teamId={}", teamId);
    currentSession()
        .createMutationQuery("DELETE FROM TeamMembershipRequest WHERE teamId = :teamId")
        .setParameter("teamId", teamId)
        .executeUpdate();
  }
}
