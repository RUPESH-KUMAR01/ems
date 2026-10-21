package com.rupesh.ems.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rupesh.ems.api.team.req.RespondToRequestRequest;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.*;
import com.rupesh.ems.db.*;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
class EventTeamRequestServiceTest {

  public DAOTestExtension daoTestRule =
      DAOTestExtension.newBuilder()
          .addEntityClass(Event.class)
          .addEntityClass(EventRegistration.class)
          .addEntityClass(Payment.class)
          .addEntityClass(Team.class)
          .addEntityClass(TeamMember.class)
          .addEntityClass(TeamMembershipRequest.class)
          .addEntityClass(User.class)
          .addEntityClass(VerificationCode.class)
          .build();

  private EventTeamRequestService requestService;
  private TeamMembershipRequestDao requestDao;
  private Long eventId;
  private Long teamId;
  private Long ownerId;
  private Long applicantId;
  private UserPrincipal ownerPrincipal;
  private UserPrincipal applicantPrincipal;

  @BeforeEach
  void setUp() {
    TeamDao teamDao = new TeamDao(daoTestRule.getSessionFactory());
    TeamMemberDao teamMemberDao = new TeamMemberDao(daoTestRule.getSessionFactory());
    requestDao = new TeamMembershipRequestDao(daoTestRule.getSessionFactory());
    EventDao eventDao = new EventDao(daoTestRule.getSessionFactory());
    EventRegistrationDao eventRegistrationDao =
        new EventRegistrationDao(daoTestRule.getSessionFactory());
    UserDao userDao = new UserDao(daoTestRule.getSessionFactory());

    EventTeamService teamService =
        new EventTeamService(eventDao, teamDao, teamMemberDao, requestDao, eventRegistrationDao);
    requestService = new EventTeamRequestService(userDao, requestDao, teamService);

    ownerId =
        daoTestRule.inTransaction(
            () -> {
              User user = new User();
              user.setEmail("owner@example.com");
              user.setName("Owner");
              user.setPhone("1234567890");
              user.setPasswordHash("hash");
              user.setEmailVerified(true);
              user.setPhoneVerified(true);
              return userDao.create(user).getId();
            });

    applicantId =
        daoTestRule.inTransaction(
            () -> {
              User user = new User();
              user.setEmail("applicant@example.com");
              user.setName("Applicant");
              user.setPhone("9876543210");
              user.setPasswordHash("hash");
              user.setEmailVerified(true);
              user.setPhoneVerified(true);
              return userDao.create(user).getId();
            });

    eventId =
        daoTestRule.inTransaction(
            () -> {
              Event event = new Event();
              event.setName("Team Event");
              event.setDescription("Desc");
              event.setCreatedBy(ownerId);
              event.setType(EventType.TEAM);
              event.setStatus(EventStatus.PUBLISHED);
              event.setRegistrationDeadline(Instant.now().plusSeconds(3600));
              event.setStartTime(Instant.now().plusSeconds(3600));
              event.setEndTime(Instant.now().plusSeconds(7200));
              event.setMaxTeamSize(5);
              return eventDao.create(event).getId();
            });

    teamId =
        daoTestRule.inTransaction(
            () -> {
              Team team = new Team(eventId, "Test Team", ownerId);
              team.setMemberCount(1);
              teamDao.create(team);
              teamMemberDao.create(new TeamMember(ownerId, team.getId(), eventId));
              return team.getId();
            });

    ownerPrincipal =
        new UserPrincipal(
            ownerId, "owner@example.com", "1234567890", "Owner", Role.USER, true, true);
    applicantPrincipal =
        new UserPrincipal(
            applicantId,
            "applicant@example.com",
            "9876543210",
            "Applicant",
            Role.USER,
            true,
            true);
  }

  @Test
  void rejectedJoinRequestCanBeSubmittedAgain() {
    requestService.requestToJoinTeam(eventId, teamId, applicantPrincipal);

    RespondToRequestRequest reject = new RespondToRequestRequest();
    reject.setStatus(RequestStatus.REJECTED);
    requestService.respondToRequest(eventId, teamId, applicantId, reject, ownerPrincipal);

    daoTestRule.inTransaction(
        () ->
            assertTrue(
                requestDao.findByTeamIdAndUserId(teamId, applicantId).isEmpty(),
                "Rejected request row should be removed"));

    requestService.requestToJoinTeam(eventId, teamId, applicantPrincipal);

    daoTestRule.inTransaction(
        () -> {
          TeamMembershipRequest stored =
              requestDao
                  .findByTeamIdAndUserId(teamId, applicantId)
                  .orElseThrow();
          assertEquals(RequestStatus.PENDING, stored.getStatus());
          assertEquals(RequestType.JOIN_REQUEST, stored.getType());
          assertEquals(null, stored.getRespondedAt());
        });
  }
}
