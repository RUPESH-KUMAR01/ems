package com.rupesh.ems.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rupesh.ems.api.admin.req.AdminForceRegisterRequest;
import com.rupesh.ems.api.admin.req.AdminResetPasswordRequest;
import com.rupesh.ems.api.admin.req.AdminUpdateEventStatusRequest;
import com.rupesh.ems.api.admin.req.AdminUpdatePaymentStatusRequest;
import com.rupesh.ems.api.admin.req.AdminUpdateRegistrationStatusRequest;
import com.rupesh.ems.api.admin.req.AdminUpdateVerificationRequest;
import com.rupesh.ems.api.admin.req.ChangeUserRoleRequest;
import com.rupesh.ems.api.admin.req.CreateManagedUserRequest;
import com.rupesh.ems.api.admin.res.SystemStatsResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.api.event.res.EventResponse;
import com.rupesh.ems.api.payment.res.PaymentResponse;
import com.rupesh.ems.api.registration.res.EventRegistrationResponse;
import com.rupesh.ems.api.team.req.RespondToRequestRequest;
import com.rupesh.ems.api.team.req.UpdateTeamRequest;
import com.rupesh.ems.api.team.res.TeamMembershipResponse;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.Event;
import com.rupesh.ems.core.EventRegistration;
import com.rupesh.ems.core.EventStatus;
import com.rupesh.ems.core.EventType;
import com.rupesh.ems.core.EventVisibility;
import com.rupesh.ems.core.Payment;
import com.rupesh.ems.core.PaymentStatus;
import com.rupesh.ems.core.RegistrationStatus;
import com.rupesh.ems.core.RequestStatus;
import com.rupesh.ems.core.RequestType;
import com.rupesh.ems.core.Role;
import com.rupesh.ems.core.Team;
import com.rupesh.ems.core.TeamMember;
import com.rupesh.ems.core.TeamMembershipRequest;
import com.rupesh.ems.core.User;
import com.rupesh.ems.core.VerificationCode;
import com.rupesh.ems.db.EventDao;
import com.rupesh.ems.db.EventRegistrationDao;
import com.rupesh.ems.db.PaymentDao;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.db.TeamMemberDao;
import com.rupesh.ems.db.TeamMembershipRequestDao;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.BadRequestException;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public class AdminServiceTest {

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

  private UserDao userDao;
  private TeamDao teamDao;
  private TeamMemberDao teamMemberDao;
  private TeamMembershipRequestDao requestDao;
  private EventDao eventDao;
  private EventRegistrationDao registrationDao;
  private PaymentDao paymentDao;
  private AdminService adminService;

  private UserPrincipal adminPrincipal;
  private Long adminId;

  @BeforeEach
  public void setUp() {
    userDao = new UserDao(daoTestRule.getSessionFactory());
    teamDao = new TeamDao(daoTestRule.getSessionFactory());
    teamMemberDao = new TeamMemberDao(daoTestRule.getSessionFactory());
    requestDao = new TeamMembershipRequestDao(daoTestRule.getSessionFactory());
    eventDao = new EventDao(daoTestRule.getSessionFactory());
    registrationDao = new EventRegistrationDao(daoTestRule.getSessionFactory());
    paymentDao = new PaymentDao(daoTestRule.getSessionFactory());

    adminService =
        new AdminService(
            userDao, teamDao, teamMemberDao, requestDao, eventDao, registrationDao, paymentDao);

    adminId =
        daoTestRule.inTransaction(
            () -> {
              User adminUser = new User();
              adminUser.setEmail("admin@example.com");
              adminUser.setName("System Admin");
              adminUser.setPhone("0000000000");
              adminUser.setPasswordHash("hash");
              adminUser.setRole(Role.ADMIN);
              adminUser.setEmailVerified(true);
              adminUser.setPhoneVerified(true);
              return userDao.create(adminUser).getId();
            });

    adminPrincipal =
        new UserPrincipal(
            adminId, "admin@example.com", "0000000000", "System Admin", Role.ADMIN, true, true);
  }

  @Test
  public void testUserManagementCapabilities() {
    // 1. Create managed user
    CreateManagedUserRequest createReq = new CreateManagedUserRequest();
    createReq.setEmail("user1@example.com");
    createReq.setName("User One");
    createReq.setPhone("1111111111");
    createReq.setPassword("password123");
    createReq.setRole(Role.USER);

    UserResponse createdUser = daoTestRule.inTransaction(() -> adminService.createUser(createReq));
    assertNotNull(createdUser.getId());
    assertEquals("User One", createdUser.getName());

    // 2. Reset password & update verification
    daoTestRule.inTransaction(
        () ->
            adminService.resetUserPassword(
                createdUser.getId(),
                new AdminResetPasswordRequest("newsecret123"),
                adminPrincipal));

    AdminUpdateVerificationRequest verifyReq = new AdminUpdateVerificationRequest(true, false);
    UserResponse updatedVerification =
        daoTestRule.inTransaction(
            () ->
                adminService.updateUserVerification(
                    createdUser.getId(), verifyReq, adminPrincipal));
    assertTrue(updatedVerification.isEmailVerified());
    assertFalse(updatedVerification.isPhoneVerified());

    // 3. Change role
    ChangeUserRoleRequest roleReq = new ChangeUserRoleRequest();
    roleReq.setRole(Role.MODERATOR);
    UserResponse roleUpdated =
        daoTestRule.inTransaction(
            () -> adminService.changeUserRole(createdUser.getId(), roleReq, adminPrincipal));
    assertEquals(Role.MODERATOR, roleUpdated.getRole());

    // 4. Admin cannot change own role or delete self
    assertThrows(
        BadRequestException.class,
        () ->
            daoTestRule.inTransaction(
                () -> adminService.changeUserRole(adminId, roleReq, adminPrincipal)));

    assertThrows(
        BadRequestException.class,
        () -> daoTestRule.inTransaction(() -> adminService.deleteUser(adminId, adminPrincipal)));
  }

  @Test
  public void testEventAndRegistrationManagement() {
    // 1. Admin creates event
    EventResponse eventResponse =
        daoTestRule.inTransaction(
            () -> {
              Event event = new Event();
              event.setName("Tech Conference");
              event.setDescription("Annual developer meet");
              event.setCreatedBy(adminId);
              event.setStartTime(Instant.now().plusSeconds(3600));
              event.setEndTime(Instant.now().plusSeconds(7200));
              event.setRegistrationDeadline(Instant.now().plusSeconds(3600));
              event.setRegistrationFee(new BigDecimal("99.99"));
              event.setMaxParticipants(200);
              event.setType(EventType.SOLO);
              event.setVisibility(EventVisibility.PUBLIC);
              return new EventResponse(eventDao.create(event));
            });
    assertNotNull(eventResponse.getId());

    // 2. Admin updates status to PUBLISHED
    AdminUpdateEventStatusRequest statusReq =
        new AdminUpdateEventStatusRequest(EventStatus.PUBLISHED);
    EventResponse publishedEvent =
        daoTestRule.inTransaction(
            () -> adminService.updateEventStatus(eventResponse.getId(), statusReq, adminPrincipal));
    assertEquals(EventStatus.PUBLISHED, publishedEvent.getStatus());

    // 3. Force register user
    AdminForceRegisterRequest regReq =
        new AdminForceRegisterRequest(eventResponse.getId(), adminId, null);
    EventRegistrationResponse regResponse =
        daoTestRule.inTransaction(() -> adminService.forceRegisterUser(regReq, adminPrincipal));
    assertEquals(RegistrationStatus.REGISTERED, regResponse.getStatus());

    // 4. Update registration status
    AdminUpdateRegistrationStatusRequest updateRegReq =
        new AdminUpdateRegistrationStatusRequest(RegistrationStatus.CANCELLED);
    EventRegistrationResponse updatedReg =
        daoTestRule.inTransaction(
            () ->
                adminService.updateRegistrationStatus(
                    regResponse.getId(), updateRegReq, adminPrincipal));
    assertEquals(RegistrationStatus.CANCELLED, updatedReg.getStatus());
  }

  @Test
  public void testTeamManagementAndPayments() {
    // 1. Seed event & user
    Long eventId =
        daoTestRule.inTransaction(
            () -> {
              Event event = new Event();
              event.setName("Hackathon");
              event.setDescription("Coding challenge");
              event.setCreatedBy(adminId);
              event.setType(EventType.TEAM);
              event.setStatus(EventStatus.PUBLISHED);
              event.setStartTime(Instant.now().plusSeconds(3600));
              event.setEndTime(Instant.now().plusSeconds(7200));
              event.setRegistrationDeadline(Instant.now().plusSeconds(3600));
              return eventDao.create(event).getId();
            });

    Long user2Id =
        daoTestRule.inTransaction(
            () -> {
              User u = new User();
              u.setEmail("member@example.com");
              u.setName("Team Member");
              u.setPhone("2222222222");
              u.setPasswordHash("hash");
              u.setEmailVerified(true);
              u.setPhoneVerified(true);
              return userDao.create(u).getId();
            });

    // 2. Seed team & member request
    Long teamId =
        daoTestRule.inTransaction(
            () -> {
              Team team = new Team(eventId, "Coders", adminId);
              teamDao.create(team);
              teamMemberDao.create(new TeamMember(adminId, team.getId(), eventId));

              TeamMembershipRequest req =
                  new TeamMembershipRequest(team.getId(), user2Id, RequestType.JOIN_REQUEST);
              requestDao.create(req);
              return team.getId();
            });

    // 3. Force resolve request by admin
    RespondToRequestRequest respondReq = new RespondToRequestRequest();
    respondReq.setStatus(RequestStatus.APPROVED);
    TeamMembershipResponse membershipResp =
        daoTestRule.inTransaction(
            () ->
                adminService.forceRespondToTeamRequest(
                    teamId, user2Id, respondReq, adminPrincipal));
    assertEquals("APPROVED", membershipResp.getType());

    // 4. Update team details
    UpdateTeamRequest updateTeam = new UpdateTeamRequest("Super Coders");
    TeamResponse updatedTeam =
        daoTestRule.inTransaction(
            () -> adminService.updateTeam(teamId, updateTeam, adminPrincipal));
    assertEquals("Super Coders", updatedTeam.getName());

    // 5. Seed payment & update payment status
    Long regId =
        daoTestRule.inTransaction(
            () -> {
              EventRegistration reg =
                  new EventRegistration(eventId, user2Id, null, RegistrationStatus.REGISTERED);
              return registrationDao.create(reg).getId();
            });

    Long paymentId =
        daoTestRule.inTransaction(
            () -> {
              Payment payment = new Payment(regId, new BigDecimal("150.00"), "order_12345");
              return paymentDao.create(payment).getId();
            });

    AdminUpdatePaymentStatusRequest updatePayReq =
        new AdminUpdatePaymentStatusRequest(PaymentStatus.COMPLETED);
    PaymentResponse updatedPay =
        daoTestRule.inTransaction(
            () -> adminService.updatePaymentStatus(paymentId, updatePayReq, adminPrincipal));
    assertEquals(PaymentStatus.COMPLETED, updatedPay.getStatus());
    assertNotNull(updatedPay.getPaidAt());

    // 6. Test system stats
    SystemStatsResponse stats = daoTestRule.inTransaction(() -> adminService.getSystemStats());
    assertTrue(stats.getTotalUsers() >= 2);
    assertTrue(stats.getTotalEvents() >= 1);
    assertTrue(stats.getTotalTeams() >= 1);
    assertTrue(stats.getTotalRegistrations() >= 1);
    assertTrue(stats.getTotalPayments() >= 1);
    assertEquals(new BigDecimal("150.00"), stats.getTotalRevenue());
  }
}
