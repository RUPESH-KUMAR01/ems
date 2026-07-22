package com.rupesh.ems.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rupesh.ems.api.team.req.RespondToRequestRequest;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.*;
import com.rupesh.ems.db.*;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public class EventTeamRequestServiceRigorousConcurrencyTest {

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
  private EventTeamService teamService;
  private TeamDao teamDao;
  private TeamMemberDao teamMemberDao;
  private TeamMembershipRequestDao requestDao;
  private EventDao eventDao;
  private EventRegistrationDao eventRegistrationDao;
  private UserDao userDao;

  @BeforeEach
  public void setUp() {
    teamDao = new TeamDao(daoTestRule.getSessionFactory());
    teamMemberDao = new TeamMemberDao(daoTestRule.getSessionFactory());
    requestDao = new TeamMembershipRequestDao(daoTestRule.getSessionFactory());
    eventDao = new EventDao(daoTestRule.getSessionFactory());
    eventRegistrationDao = new EventRegistrationDao(daoTestRule.getSessionFactory());
    userDao = new UserDao(daoTestRule.getSessionFactory());

    teamService =
        new EventTeamService(eventDao, teamDao, teamMemberDao, requestDao, eventRegistrationDao);
    requestService = new EventTeamRequestService(userDao, requestDao, teamService);
  }

  @Test
  public void testPessimisticLockPreventsTeamOverfill() throws InterruptedException {
    // We will seed the database with an event that has a maxTeamSize of 2.
    // We will have an owner, and a team.
    // We will spawn 10 threads trying to approve 10 different users at the exact same time.
    // We will assert that exactly 1 gets approved (since owner is 1 member, 1 more makes 2).

    Long ownerId =
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

    Long eventId =
        daoTestRule.inTransaction(
            () -> {
              Event event = new Event();
              event.setName("Test Event");
              event.setDescription("Test Desc");
              event.setCreatedBy(ownerId);
              event.setType(EventType.TEAM);
              event.setStatus(EventStatus.PUBLISHED);
              event.setRegistrationDeadline(Instant.now().plusSeconds(3600));
              event.setStartTime(Instant.now().plusSeconds(3600));
              event.setEndTime(Instant.now().plusSeconds(7200));
              event.setMaxTeamSize(2);
              return eventDao.create(event).getId();
            });

    Long teamId =
        daoTestRule.inTransaction(
            () -> {
              Team team = new Team(eventId, "Concurrency Team", ownerId);
              team.setMemberCount(1);
              teamDao.create(team);
              teamMemberDao.create(new TeamMember(ownerId, team.getId(), eventId));
              return team.getId();
            });

    // Create 10 users and 10 pending requests
    List<Long> userIds = new ArrayList<>();
    daoTestRule.inTransaction(
        () -> {
          for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setName("User " + i);
            user.setPhone("98765432" + i);
            user.setPasswordHash("hash");
            user.setEmailVerified(true);
            user.setPhoneVerified(true);
            userDao.create(user);
            userIds.add(user.getId());

            TeamMembershipRequest req =
                new TeamMembershipRequest(teamId, user.getId(), RequestType.JOIN_REQUEST);
            requestDao.create(req);
          }
        });

    User ownerUser = new User();
    ownerUser.setEmail("owner@example.com");
    ownerUser.setName("Owner");
    ownerUser.setPhone("1234567890");
    ownerUser.setEmailVerified(true);
    ownerUser.setPhoneVerified(true);
    // Assuming id isn't explicitly settable here, we'll construct Principal with full constructor
    // if we could,
    // but UserPrincipal has constructor UserPrincipal(Long, String, String, String, Role, boolean,
    // boolean)
    UserPrincipal ownerPrincipal =
        new UserPrincipal(
            ownerId, "owner@example.com", "1234567890", "Owner", Role.USER, true, true);

    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(1);
    List<Future<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      final Long userId = userIds.get(i);
      futures.add(
          executor.submit(
              () -> {
                // Wait for all threads to be ready
                latch.await();

                // Each thread needs its own session to test true database concurrency
                Session session = daoTestRule.getSessionFactory().openSession();
                ManagedSessionContext.bind(session);
                Transaction tx = session.beginTransaction();
                boolean success = false;
                try {
                  RespondToRequestRequest response = new RespondToRequestRequest();
                  response.setStatus(RequestStatus.APPROVED);

                  // The core service call we are testing
                  requestService.respondToRequest(
                      eventId, teamId, userId, response, ownerPrincipal);
                  tx.commit();
                  success = true;
                } catch (Exception e) {
                  tx.rollback();
                  // Exception is expected for 9 out of 10 threads
                } finally {
                  session.close();
                  ManagedSessionContext.unbind(daoTestRule.getSessionFactory());
                }
                return success;
              }));
    }

    // Release all threads simultaneously
    latch.countDown();

    int successCount = 0;
    for (Future<Boolean> future : futures) {
      try {
        if (future.get()) {
          successCount++;
        }
      } catch (ExecutionException e) {
        // ignored
      }
    }

    executor.shutdown();

    // 1 owner + 1 approved user = 2 members (maxTeamSize)
    assertEquals(1, successCount, "Only exactly 1 user should be approved due to pessimistic lock");

    long finalMemberCount = daoTestRule.inTransaction(() -> teamMemberDao.countByTeamId(teamId));
    assertEquals(2, finalMemberCount, "Final team size must be exactly 2");
  }
}
