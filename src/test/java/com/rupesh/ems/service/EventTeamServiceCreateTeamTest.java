package com.rupesh.ems.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rupesh.ems.api.team.req.CreateTeamRequest;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.*;
import com.rupesh.ems.db.*;
import com.rupesh.ems.exceptions.ConflictException;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
class EventTeamServiceCreateTeamTest {

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

  private EventTeamService teamService;
  private Long eventId;
  private Long ownerId;
  private UserPrincipal ownerPrincipal;

  @BeforeEach
  void setUp() {
    TeamDao teamDao = new TeamDao(daoTestRule.getSessionFactory());
    TeamMemberDao teamMemberDao = new TeamMemberDao(daoTestRule.getSessionFactory());
    TeamMembershipRequestDao requestDao =
        new TeamMembershipRequestDao(daoTestRule.getSessionFactory());
    EventDao eventDao = new EventDao(daoTestRule.getSessionFactory());
    EventRegistrationDao eventRegistrationDao =
        new EventRegistrationDao(daoTestRule.getSessionFactory());
    UserDao userDao = new UserDao(daoTestRule.getSessionFactory());

    teamService =
        new EventTeamService(
            eventDao, teamDao, teamMemberDao, requestDao, eventRegistrationDao);

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

    ownerPrincipal =
        new UserPrincipal(
            ownerId, "owner@example.com", "1234567890", "Owner", Role.USER, true, true);

    daoTestRule.inTransaction(
        () -> {
          Team team = new Team(eventId, "Existing", ownerId);
          team.setMemberCount(1);
          teamDao.create(team);
          teamMemberDao.create(new TeamMember(ownerId, team.getId(), eventId));
        });
  }

  @Test
  void cannotCreateSecondTeamWhenAlreadyOnATeamForEvent() {
    CreateTeamRequest req = new CreateTeamRequest("Another Team");

    assertThrows(
        ConflictException.class,
        () -> teamService.createTeam(eventId, req, ownerPrincipal));
  }
}
