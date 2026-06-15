package com.rupesh.ems.db;

import org.hibernate.SessionFactory;

import com.rupesh.ems.core.TeamMembershipRequest;

import io.dropwizard.hibernate.AbstractDAO;

public class TeamMembershipRequestDao extends AbstractDAO<TeamMembershipRequest> {

  public TeamMembershipRequestDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public TeamMembershipRequest create(TeamMembershipRequest request) {
    return persist(request);
  }
  
}
