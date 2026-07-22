package com.rupesh.ems.service;

import com.rupesh.ems.api.admin.req.AdminForceRegisterRequest;
import com.rupesh.ems.api.admin.req.AdminResetPasswordRequest;
import com.rupesh.ems.api.admin.req.AdminUpdateEventStatusRequest;
import com.rupesh.ems.api.admin.req.AdminUpdatePaymentStatusRequest;
import com.rupesh.ems.api.admin.req.AdminUpdateRegistrationStatusRequest;
import com.rupesh.ems.api.admin.req.AdminUpdateVerificationRequest;
import com.rupesh.ems.api.admin.req.ChangeUserRoleRequest;
import com.rupesh.ems.api.admin.req.CreateManagedUserRequest;
import com.rupesh.ems.api.admin.req.UpdateUserRequest;
import com.rupesh.ems.api.admin.res.AdminMessageResponse;
import com.rupesh.ems.api.admin.res.SystemStatsResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.api.event.req.CreateEventRequest;
import com.rupesh.ems.api.event.req.UpdateEventRequest;
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
import com.rupesh.ems.core.Team;
import com.rupesh.ems.core.TeamMember;
import com.rupesh.ems.core.TeamMembershipRequest;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.EventDao;
import com.rupesh.ems.db.EventRegistrationDao;
import com.rupesh.ems.db.PaymentDao;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.db.TeamMemberDao;
import com.rupesh.ems.db.TeamMembershipRequestDao;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import com.rupesh.ems.util.PasswordUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);

  private final UserDao userDao;
  private final TeamDao teamDao;
  private final TeamMemberDao teamMemberDao;
  private final TeamMembershipRequestDao teamMembershipRequestDao;
  private final EventDao eventDao;
  private final EventRegistrationDao eventRegistrationDao;
  private final PaymentDao paymentDao;

  public AdminService(
      UserDao userDao,
      TeamDao teamDao,
      TeamMemberDao teamMemberDao,
      TeamMembershipRequestDao teamMembershipRequestDao,
      EventDao eventDao,
      EventRegistrationDao eventRegistrationDao,
      PaymentDao paymentDao) {
    this.userDao = userDao;
    this.teamDao = teamDao;
    this.teamMemberDao = teamMemberDao;
    this.teamMembershipRequestDao = teamMembershipRequestDao;
    this.eventDao = eventDao;
    this.eventRegistrationDao = eventRegistrationDao;
    this.paymentDao = paymentDao;
  }

  public AdminService(
      UserDao userDao,
      TeamDao teamDao,
      TeamMemberDao teamMemberDao,
      TeamMembershipRequestDao teamMembershipRequestDao,
      EventDao eventDao) {
    this(userDao, teamDao, teamMemberDao, teamMembershipRequestDao, eventDao, null, null);
  }

  // --- USER MANAGEMENT ---

  public UserResponse createUser(CreateManagedUserRequest request) {
    LOGGER.info("Creating managed user email={}", request.getEmail());
    if (userDao.findByEmail(request.getEmail()).isPresent()) {
      throw new ConflictException("User already exists");
    }

    if (userDao.findByPhone(request.getPhone()).isPresent()) {
      throw new ConflictException("Phone number already in use");
    }

    User user = new User();
    user.setEmail(request.getEmail());
    user.setName(request.getName());
    user.setPasswordHash(PasswordUtil.hash(request.getPassword()));
    user.setRole(request.getRole());
    user.setPhone(request.getPhone());
    user.setEmailVerified(true);
    user.setPhoneVerified(true);

    User created = userDao.create(user);
    LOGGER.info("Successfully created managed user userId={}", created.getId());
    return new UserResponse(created);
  }

  public UserResponse getUserById(Long userId) {
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    return new UserResponse(user);
  }

  public UserResponse getUserByEmail(String email) {
    User user =
        userDao.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    return new UserResponse(user);
  }

  public UserResponse getUserByPhone(String phone) {
    User user =
        userDao.findByPhone(phone).orElseThrow(() -> new NotFoundException("User not found"));
    return new UserResponse(user);
  }

  public List<UserResponse> getAllUsers() {
    LOGGER.info("Fetching all users for admin");
    return userDao.findAll().stream().map(UserResponse::new).toList();
  }

  public UserResponse changeUserRole(
      Long userId, ChangeUserRoleRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} changing role for userId={} to {}",
        admin != null ? admin.getId() : "system",
        userId,
        request.getRole());
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (admin != null && admin.getId().equals(userId) && request.getRole() != user.getRole()) {
      throw new BadRequestException("Admin cannot change own role");
    }

    user.setRole(request.getRole());
    userDao.update(user);
    LOGGER.info("Successfully changed role for userId={}", userId);
    return new UserResponse(user);
  }

  public AdminMessageResponse deleteUser(Long userId, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} deleting user userId={}",
        admin != null ? admin.getId() : "system",
        userId);
    if (admin != null && admin.getId().equals(userId)) {
      throw new BadRequestException("Admin cannot delete own account");
    }

    boolean deleted = userDao.delete(userId);
    if (!deleted) {
      throw new NotFoundException("User not found");
    }

    LOGGER.info("Successfully deleted user userId={}", userId);
    return new AdminMessageResponse("User deleted successfully");
  }

  public UserResponse updateUser(Long userId, UpdateUserRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} updating user userId={}",
        admin != null ? admin.getId() : "system",
        userId);
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (request.getName() != null) {
      user.setName(request.getName());
    }

    if (request.getEmail() != null) {
      user.setEmail(request.getEmail());
    }

    if (request.getPhone() != null) {
      user.setPhone(request.getPhone());
    }

    if (request.getRole() != null) {
      user.setRole(request.getRole());
    }

    if (request.getEmailVerified() != null) {
      user.setEmailVerified(request.getEmailVerified());
    }

    if (request.getPhoneVerified() != null) {
      user.setPhoneVerified(request.getPhoneVerified());
    }

    userDao.update(user);
    LOGGER.info("Successfully updated user userId={}", userId);
    return new UserResponse(user);
  }

  public UserResponse resetUserPassword(
      Long userId, AdminResetPasswordRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} resetting password for userId={}",
        admin != null ? admin.getId() : "system",
        userId);
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    user.setPasswordHash(PasswordUtil.hash(request.getNewPassword()));
    userDao.update(user);
    LOGGER.info("Successfully reset password for userId={}", userId);
    return new UserResponse(user);
  }

  public UserResponse updateUserVerification(
      Long userId, AdminUpdateVerificationRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} updating verification status for userId={}",
        admin != null ? admin.getId() : "system",
        userId);
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (request.getEmailVerified() != null) {
      user.setEmailVerified(request.getEmailVerified());
    }

    if (request.getPhoneVerified() != null) {
      user.setPhoneVerified(request.getPhoneVerified());
    }

    userDao.update(user);
    LOGGER.info("Successfully updated verification for userId={}", userId);
    return new UserResponse(user);
  }

  public List<TeamResponse> getUserTeams(Long userId) {
    LOGGER.info("Fetching teams for userId={}", userId);
    userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    return teamDao.findByUserId(userId).stream().map(TeamResponse::new).toList();
  }

  public List<EventRegistrationResponse> getUserRegistrations(Long userId) {
    LOGGER.info("Fetching registrations for userId={}", userId);
    userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    if (eventRegistrationDao == null) {
      return List.of();
    }
    return eventRegistrationDao.findByUserId(userId).stream()
        .map(EventRegistrationResponse::new)
        .toList();
  }

  // --- TEAM MANAGEMENT ---

  public TeamResponse getTeamById(Long teamId) {
    return new TeamResponse(getTeamOrThrow(teamId));
  }

  public List<TeamResponse> getAllTeams() {
    LOGGER.info("Fetching all teams for admin");
    return teamDao.findAll().stream().map(TeamResponse::new).toList();
  }

  public List<UserResponse> getTeamMembers(Long teamId) {
    getTeamOrThrow(teamId);
    return teamMemberDao.getUsersByTeamId(teamId).stream().map(UserResponse::new).toList();
  }

  public List<TeamMembershipResponse> getTeamMembershipRequests(Long teamId) {
    getTeamOrThrow(teamId);
    return teamMembershipRequestDao.getRequestsByTeamId(teamId).stream()
        .map(TeamMembershipResponse::new)
        .toList();
  }

  public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} updating teamId={}", admin != null ? admin.getId() : "system", teamId);
    Team team = getTeamOrThrow(teamId);

    if (request.getName() != null) {
      team.setName(request.getName());
    }

    Team updated = teamDao.update(team);
    LOGGER.info("Successfully updated teamId={}", teamId);
    return new TeamResponse(updated);
  }

  public TeamResponse transferTeamOwnership(Long teamId, Long newOwnerId) {
    LOGGER.info("Transferring ownership of teamId={} to userId={}", teamId, newOwnerId);
    Team team = getTeamOrThrow(teamId);
    User newOwner =
        userDao.getUserById(newOwnerId).orElseThrow(() -> new NotFoundException("User not found"));

    if (!newOwner.isFullyVerified()) {
      throw new BadRequestException("New owner must be fully verified");
    }

    teamMemberDao
        .findByTeamIdAndUserId(teamId, newOwnerId)
        .orElseThrow(() -> new NotFoundException("New owner must be a member of the team"));

    team.setOwnerId(newOwnerId);
    LOGGER.info("Successfully transferred ownership of teamId={} to userId={}", teamId, newOwnerId);
    return new TeamResponse(teamDao.update(team));
  }

  public TeamResponse forceAddUserToTeam(Long teamId, Long userId, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} force adding userId={} to teamId={}",
        admin != null ? admin.getId() : "system",
        userId,
        teamId);
    Team team = getTeamOrThrow(teamId);
    userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (teamMemberDao.findByTeamIdAndUserId(teamId, userId).isPresent()) {
      throw new ConflictException("User is already a member of this team");
    }

    TeamMember member = new TeamMember(userId, teamId, team.getEventId());
    teamMemberDao.create(member);

    teamMembershipRequestDao
        .findByTeamIdAndUserId(teamId, userId)
        .ifPresent(teamMembershipRequestDao::delete);

    LOGGER.info("Successfully force added userId={} to teamId={}", userId, teamId);
    return new TeamResponse(team);
  }

  public TeamMembershipResponse forceRespondToTeamRequest(
      Long teamId, Long userId, RespondToRequestRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} responding to team request for userId={} on teamId={} with status={}",
        admin != null ? admin.getId() : "system",
        userId,
        teamId,
        request.getStatus());
    Team team = getTeamOrThrow(teamId);
    TeamMembershipRequest membershipRequest =
        teamMembershipRequestDao
            .findByTeamIdAndUserId(teamId, userId)
            .orElseThrow(() -> new NotFoundException("Membership request not found"));

    membershipRequest.setStatus(request.getStatus());
    teamMembershipRequestDao.update(membershipRequest);

    if (request.getStatus() == RequestStatus.APPROVED
        && teamMemberDao.findByTeamIdAndUserId(teamId, userId).isEmpty()) {
      TeamMember member = new TeamMember(userId, teamId, team.getEventId());
      teamMemberDao.create(member);
    }

    LOGGER.info("Successfully resolved request for userId={} on teamId={}", userId, teamId);
    return new TeamMembershipResponse(membershipRequest);
  }

  public AdminMessageResponse removeUserFromTeam(Long teamId, Long userId) {
    LOGGER.info("Removing userId={} from teamId={}", userId, teamId);
    Team team = getTeamOrThrow(teamId);
    if (team.getOwnerId().equals(userId)) {
      throw new BadRequestException("Owner cannot be removed from team");
    }

    TeamMember membership =
        teamMemberDao
            .findByTeamIdAndUserId(teamId, userId)
            .orElseThrow(() -> new NotFoundException("Team member not found"));

    teamMemberDao.delete(membership);

    TeamMembershipRequest request =
        teamMembershipRequestDao.findByTeamIdAndUserId(teamId, userId).orElse(null);
    if (request != null) {
      teamMembershipRequestDao.delete(request);
    }

    LOGGER.info("Successfully removed userId={} from teamId={}", userId, teamId);
    return new AdminMessageResponse("User removed from team successfully");
  }

  public AdminMessageResponse deleteTeam(Long teamId) {
    LOGGER.info("Deleting teamId={}", teamId);
    Team team = getTeamOrThrow(teamId);

    teamMemberDao.deleteByTeamId(teamId);
    teamMembershipRequestDao.deleteByTeamId(teamId);
    teamDao.delete(team);

    LOGGER.info("Successfully deleted teamId={}", teamId);
    return new AdminMessageResponse("Team deleted successfully");
  }

  // --- EVENT MANAGEMENT ---

  public List<EventResponse> getAllEvents() {
    LOGGER.info("Fetching all events for admin");
    return eventDao.getAllEvents().stream().map(EventResponse::new).toList();
  }

  public EventResponse getEventById(Long eventId) {
    Event event =
        eventDao.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
    return new EventResponse(event);
  }

  public EventResponse createEvent(CreateEventRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} creating event name={}",
        admin != null ? admin.getId() : "system",
        request.getName());

    Event event = new Event();
    event.setName(request.getName());
    event.setDescription(request.getDescription());
    event.setStartTime(request.getStartTime());
    event.setEndTime(request.getEndTime());
    if (request.getRegistrationFee() != null) {
      event.setRegistrationFee(request.getRegistrationFee());
    }
    if (request.getRegistrationDeadline() != null) {
      event.setRegistrationDeadline(request.getRegistrationDeadline());
    }
    if (request.getMaxParticipants() != null) {
      event.setMaxParticipants(request.getMaxParticipants());
    }
    if (request.getMinTeamSize() != null) {
      event.setMinTeamSize(request.getMinTeamSize());
    }
    if (request.getMaxTeamSize() != null) {
      event.setMaxTeamSize(request.getMaxTeamSize());
    }
    event.setType(request.getType() != null ? request.getType() : EventType.SOLO);
    event.setVisibility(
        request.getVisibility() != null ? request.getVisibility() : EventVisibility.PUBLIC);
    event.setStatus(EventStatus.DRAFT);
    event.setCreatedBy(admin != null ? admin.getId() : 1L);

    Event created = eventDao.create(event);
    LOGGER.info("Successfully created event eventId={}", created.getId());
    return new EventResponse(created);
  }

  public EventResponse updateEvent(Long eventId, UpdateEventRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} updating eventId={}", admin != null ? admin.getId() : "system", eventId);
    Event event =
        eventDao.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));

    if (request.getName() != null) {
      event.setName(request.getName());
    }
    if (request.getDescription() != null) {
      event.setDescription(request.getDescription());
    }
    if (request.getStartTime() != null) {
      event.setStartTime(request.getStartTime());
    }
    if (request.getEndTime() != null) {
      event.setEndTime(request.getEndTime());
    }
    if (request.getRegistrationFee() != null) {
      event.setRegistrationFee(request.getRegistrationFee());
    }
    if (request.getRegistrationDeadline() != null) {
      event.setRegistrationDeadline(request.getRegistrationDeadline());
    }
    if (request.getMaxParticipants() != null) {
      event.setMaxParticipants(request.getMaxParticipants());
    }
    if (request.getMinTeamSize() != null) {
      event.setMinTeamSize(request.getMinTeamSize());
    }
    if (request.getMaxTeamSize() != null) {
      event.setMaxTeamSize(request.getMaxTeamSize());
    }
    if (request.getVisibility() != null) {
      event.setVisibility(request.getVisibility());
    }
    if (request.getType() != null) {
      event.setType(request.getType());
    }
    if (request.getStatus() != null) {
      event.setStatus(request.getStatus());
    }

    eventDao.update(event);
    LOGGER.info("Successfully updated eventId={}", eventId);
    return new EventResponse(event);
  }

  public EventResponse updateEventStatus(
      Long eventId, AdminUpdateEventStatusRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} updating status for eventId={} to {}",
        admin != null ? admin.getId() : "system",
        eventId,
        request.getStatus());
    Event event =
        eventDao.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));

    event.setStatus(request.getStatus());
    eventDao.update(event);
    LOGGER.info("Successfully updated status for eventId={}", eventId);
    return new EventResponse(event);
  }

  public AdminMessageResponse deleteEvent(Long eventId, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} deleting eventId={}", admin != null ? admin.getId() : "system", eventId);
    Event event =
        eventDao.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));

    eventDao.delete(event);
    LOGGER.info("Successfully deleted eventId={}", eventId);
    return new AdminMessageResponse("Event deleted successfully");
  }

  public List<TeamResponse> getEventTeams(Long eventId) {
    LOGGER.info("Fetching teams for eventId={}", eventId);
    eventDao.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
    return teamDao.findByEventId(eventId).stream().map(TeamResponse::new).toList();
  }

  public List<EventRegistrationResponse> getEventRegistrations(Long eventId) {
    LOGGER.info("Fetching registrations for eventId={}", eventId);
    eventDao.getEventById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
    if (eventRegistrationDao == null) {
      return List.of();
    }
    return eventRegistrationDao.findByEventId(eventId).stream()
        .map(EventRegistrationResponse::new)
        .toList();
  }

  // --- REGISTRATION MANAGEMENT ---

  public List<EventRegistrationResponse> getAllRegistrations() {
    LOGGER.info("Fetching all registrations for admin");
    if (eventRegistrationDao == null) {
      return List.of();
    }
    return eventRegistrationDao.findAll().stream().map(EventRegistrationResponse::new).toList();
  }

  public EventRegistrationResponse getRegistrationById(Long registrationId) {
    if (eventRegistrationDao == null) {
      throw new NotFoundException("Registration not found");
    }
    EventRegistration reg =
        eventRegistrationDao
            .getById(registrationId)
            .orElseThrow(() -> new NotFoundException("Registration not found"));
    return new EventRegistrationResponse(reg);
  }

  public EventRegistrationResponse forceRegisterUser(
      AdminForceRegisterRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} force registering userId={} for eventId={}",
        admin != null ? admin.getId() : "system",
        request.getUserId(),
        request.getEventId());

    eventDao
        .getEventById(request.getEventId())
        .orElseThrow(() -> new NotFoundException("Event not found"));
    userDao
        .getUserById(request.getUserId())
        .orElseThrow(() -> new NotFoundException("User not found"));

    if (request.getTeamId() != null) {
      teamDao
          .getTeamById(request.getTeamId())
          .orElseThrow(() -> new NotFoundException("Team not found"));
    }

    if (eventRegistrationDao == null) {
      throw new BadRequestException("Registration service unavailable");
    }

    EventRegistration reg =
        new EventRegistration(
            request.getEventId(),
            request.getUserId(),
            request.getTeamId(),
            RegistrationStatus.REGISTERED);

    EventRegistration created = eventRegistrationDao.create(reg);
    LOGGER.info("Successfully force registered registrationId={}", created.getId());
    return new EventRegistrationResponse(created);
  }

  public EventRegistrationResponse updateRegistrationStatus(
      Long registrationId, AdminUpdateRegistrationStatusRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} updating status for registrationId={} to {}",
        admin != null ? admin.getId() : "system",
        registrationId,
        request.getStatus());

    if (eventRegistrationDao == null) {
      throw new NotFoundException("Registration not found");
    }

    EventRegistration reg =
        eventRegistrationDao
            .getById(registrationId)
            .orElseThrow(() -> new NotFoundException("Registration not found"));

    reg.setStatus(request.getStatus());
    eventRegistrationDao.update(reg);
    LOGGER.info("Successfully updated status for registrationId={}", registrationId);
    return new EventRegistrationResponse(reg);
  }

  public AdminMessageResponse deleteRegistration(Long registrationId, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} deleting registrationId={}",
        admin != null ? admin.getId() : "system",
        registrationId);

    if (eventRegistrationDao == null) {
      throw new NotFoundException("Registration not found");
    }

    EventRegistration reg =
        eventRegistrationDao
            .getById(registrationId)
            .orElseThrow(() -> new NotFoundException("Registration not found"));

    eventRegistrationDao.delete(reg);
    LOGGER.info("Successfully deleted registrationId={}", registrationId);
    return new AdminMessageResponse("Registration deleted successfully");
  }

  // --- PAYMENT MANAGEMENT ---

  public List<PaymentResponse> getAllPayments() {
    LOGGER.info("Fetching all payments for admin");
    if (paymentDao == null) {
      return List.of();
    }
    return paymentDao.findAll().stream().map(PaymentResponse::new).toList();
  }

  public PaymentResponse getPaymentById(Long paymentId) {
    if (paymentDao == null) {
      throw new NotFoundException("Payment not found");
    }
    Payment payment =
        paymentDao.getById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found"));
    return new PaymentResponse(payment);
  }

  public PaymentResponse getPaymentByOrderId(String orderId) {
    if (paymentDao == null) {
      throw new NotFoundException("Payment not found");
    }
    Payment payment =
        paymentDao
            .findByProviderOrderId(orderId)
            .orElseThrow(() -> new NotFoundException("Payment not found for orderId"));
    return new PaymentResponse(payment);
  }

  public PaymentResponse getPaymentByRegistrationId(Long registrationId) {
    if (paymentDao == null) {
      throw new NotFoundException("Payment not found");
    }
    Payment payment =
        paymentDao
            .findByRegistrationId(registrationId)
            .orElseThrow(() -> new NotFoundException("Payment not found for registrationId"));
    return new PaymentResponse(payment);
  }

  public PaymentResponse updatePaymentStatus(
      Long paymentId, AdminUpdatePaymentStatusRequest request, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} updating status for paymentId={} to {}",
        admin != null ? admin.getId() : "system",
        paymentId,
        request.getStatus());

    if (paymentDao == null) {
      throw new NotFoundException("Payment not found");
    }

    Payment payment =
        paymentDao.getById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found"));

    payment.setStatus(request.getStatus());
    if (request.getStatus() == PaymentStatus.COMPLETED && payment.getPaidAt() == null) {
      payment.setPaidAt(Instant.now());
    }

    paymentDao.update(payment);
    LOGGER.info("Successfully updated status for paymentId={}", paymentId);
    return new PaymentResponse(payment);
  }

  public AdminMessageResponse deletePayment(Long paymentId, UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} deleting paymentId={}",
        admin != null ? admin.getId() : "system",
        paymentId);

    if (paymentDao == null) {
      throw new NotFoundException("Payment not found");
    }

    Payment payment =
        paymentDao.getById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found"));

    paymentDao.delete(payment);
    LOGGER.info("Successfully deleted paymentId={}", paymentId);
    return new AdminMessageResponse("Payment deleted successfully");
  }

  // --- SYSTEM STATS & DASHBOARD ---

  public SystemStatsResponse getSystemStats() {
    LOGGER.info("Generating system statistics for admin dashboard");

    List<User> users = userDao.findAll();
    long totalUsers = users.size();
    Map<String, Long> usersByRole =
        users.stream()
            .collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()));

    List<Event> events = eventDao.getAllEvents();
    long totalEvents = events.size();
    Map<String, Long> eventsByStatus =
        events.stream()
            .collect(Collectors.groupingBy(e -> e.getStatus().name(), Collectors.counting()));

    List<Team> teams = teamDao.findAll();
    long totalTeams = teams.size();

    List<EventRegistration> registrations =
        eventRegistrationDao != null ? eventRegistrationDao.findAll() : List.of();
    long totalRegistrations = registrations.size();
    Map<String, Long> registrationsByStatus =
        registrations.stream()
            .collect(Collectors.groupingBy(r -> r.getStatus().name(), Collectors.counting()));

    List<Payment> payments = paymentDao != null ? paymentDao.findAll() : List.of();
    long totalPayments = payments.size();
    Map<String, Long> paymentsByStatus =
        payments.stream()
            .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));

    BigDecimal totalRevenue =
        payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getAmount() != null)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new SystemStatsResponse(
        totalUsers,
        usersByRole,
        totalEvents,
        eventsByStatus,
        totalTeams,
        totalRegistrations,
        registrationsByStatus,
        totalPayments,
        paymentsByStatus,
        totalRevenue);
  }

  private Team getTeamOrThrow(Long teamId) {
    return teamDao.getTeamById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));
  }
}
