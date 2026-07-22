package com.rupesh.ems.resources;

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
import com.rupesh.ems.service.AdminService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class AdminResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(AdminResource.class);

  private final AdminService adminService;

  public AdminResource(AdminService adminService) {
    this.adminService = adminService;
  }

  // --- USER MANAGEMENT ---

  @POST
  @UnitOfWork
  @Path("/users")
  public UserResponse createUser(
      @Valid CreateManagedUserRequest request, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting createUser email={}",
        admin.getId(),
        admin.getEmail(),
        request.getEmail());
    return adminService.createUser(request);
  }

  @PUT
  @Path("/users/{id}")
  @UnitOfWork
  public UserResponse updateUser(
      @PathParam("id") Long userId, @Valid UpdateUserRequest request, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting updateUser targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        userId);
    return adminService.updateUser(userId, request, admin);
  }

  @GET
  @UnitOfWork
  @Path("/users/{id}")
  public UserResponse getUserById(@PathParam("id") Long userId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getUserById targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        userId);
    return adminService.getUserById(userId);
  }

  @GET
  @UnitOfWork
  @Path("/users/search/email")
  public UserResponse getUserByEmail(@QueryParam("email") String email, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getUserByEmail email={}",
        admin.getId(),
        admin.getEmail(),
        email);
    return adminService.getUserByEmail(email);
  }

  @GET
  @UnitOfWork
  @Path("/users/search/phone")
  public UserResponse getUserByPhone(@QueryParam("phone") String phone, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getUserByPhone phone={}",
        admin.getId(),
        admin.getEmail(),
        phone);
    return adminService.getUserByPhone(phone);
  }

  @GET
  @UnitOfWork
  @Path("/users")
  public List<UserResponse> getAllUsers(@Auth UserPrincipal admin) {
    LOGGER.info("Admin userId={} email={} requesting getAllUsers", admin.getId(), admin.getEmail());
    return adminService.getAllUsers();
  }

  @PUT
  @UnitOfWork
  @Path("/users/{id}/role")
  public UserResponse changeUserRole(
      @PathParam("id") Long userId,
      @Valid ChangeUserRoleRequest request,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting changeUserRole targetUserId={} newRole={}",
        admin.getId(),
        admin.getEmail(),
        userId,
        request.getRole());
    return adminService.changeUserRole(userId, request, admin);
  }

  @PUT
  @UnitOfWork
  @Path("/users/{id}/password")
  public UserResponse resetUserPassword(
      @PathParam("id") Long userId,
      @Valid AdminResetPasswordRequest request,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting resetUserPassword targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        userId);
    return adminService.resetUserPassword(userId, request, admin);
  }

  @PUT
  @UnitOfWork
  @Path("/users/{id}/verification")
  public UserResponse updateUserVerification(
      @PathParam("id") Long userId,
      @Valid AdminUpdateVerificationRequest request,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting updateUserVerification targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        userId);
    return adminService.updateUserVerification(userId, request, admin);
  }

  @DELETE
  @UnitOfWork
  @Path("/users/{id}")
  public AdminMessageResponse deleteUser(@PathParam("id") Long userId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting deleteUser targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        userId);
    return adminService.deleteUser(userId, admin);
  }

  @GET
  @UnitOfWork
  @Path("/users/{id}/teams")
  public List<TeamResponse> getUserTeams(@PathParam("id") Long userId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getUserTeams targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        userId);
    return adminService.getUserTeams(userId);
  }

  @GET
  @UnitOfWork
  @Path("/users/{id}/registrations")
  public List<EventRegistrationResponse> getUserRegistrations(
      @PathParam("id") Long userId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getUserRegistrations targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        userId);
    return adminService.getUserRegistrations(userId);
  }

  // --- TEAM MANAGEMENT ---

  @GET
  @UnitOfWork
  @Path("/teams")
  public List<TeamResponse> getAllTeams(@Auth UserPrincipal admin) {
    LOGGER.info("Admin userId={} email={} requesting getAllTeams", admin.getId(), admin.getEmail());
    return adminService.getAllTeams();
  }

  @GET
  @UnitOfWork
  @Path("/teams/{teamId}")
  public TeamResponse getTeamById(@PathParam("teamId") Long teamId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getTeamById teamId={}",
        admin.getId(),
        admin.getEmail(),
        teamId);
    return adminService.getTeamById(teamId);
  }

  @GET
  @UnitOfWork
  @Path("/teams/{teamId}/members")
  public List<UserResponse> getTeamMembers(
      @PathParam("teamId") Long teamId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getTeamMembers teamId={}",
        admin.getId(),
        admin.getEmail(),
        teamId);
    return adminService.getTeamMembers(teamId);
  }

  @GET
  @UnitOfWork
  @Path("/teams/{teamId}/requests")
  public List<TeamMembershipResponse> getTeamMembershipRequests(
      @PathParam("teamId") Long teamId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getTeamMembershipRequests teamId={}",
        admin.getId(),
        admin.getEmail(),
        teamId);
    return adminService.getTeamMembershipRequests(teamId);
  }

  @PUT
  @UnitOfWork
  @Path("/teams/{teamId}")
  public TeamResponse updateTeam(
      @PathParam("teamId") Long teamId,
      @Valid UpdateTeamRequest request,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting updateTeam teamId={}",
        admin.getId(),
        admin.getEmail(),
        teamId);
    return adminService.updateTeam(teamId, request, admin);
  }

  @PUT
  @UnitOfWork
  @Path("/teams/{teamId}/owner/{userId}")
  public TeamResponse transferTeamOwnership(
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting transferTeamOwnership teamId={} newOwnerId={}",
        admin.getId(),
        admin.getEmail(),
        teamId,
        userId);
    return adminService.transferTeamOwnership(teamId, userId);
  }

  @POST
  @UnitOfWork
  @Path("/teams/{teamId}/members/{userId}")
  public TeamResponse forceAddUserToTeam(
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting forceAddUserToTeam teamId={} targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        teamId,
        userId);
    return adminService.forceAddUserToTeam(teamId, userId, admin);
  }

  @PUT
  @UnitOfWork
  @Path("/teams/{teamId}/requests/{userId}")
  public TeamMembershipResponse forceRespondToTeamRequest(
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Valid RespondToRequestRequest request,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting forceRespondToTeamRequest teamId={} targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        teamId,
        userId);
    return adminService.forceRespondToTeamRequest(teamId, userId, request, admin);
  }

  @DELETE
  @UnitOfWork
  @Path("/teams/{teamId}/members/{userId}")
  public AdminMessageResponse removeUserFromTeam(
      @PathParam("teamId") Long teamId,
      @PathParam("userId") Long userId,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting removeUserFromTeam teamId={} targetUserId={}",
        admin.getId(),
        admin.getEmail(),
        teamId,
        userId);
    return adminService.removeUserFromTeam(teamId, userId);
  }

  @DELETE
  @UnitOfWork
  @Path("/teams/{teamId}")
  public AdminMessageResponse deleteTeam(
      @PathParam("teamId") Long teamId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting deleteTeam teamId={}",
        admin.getId(),
        admin.getEmail(),
        teamId);
    return adminService.deleteTeam(teamId);
  }

  // --- EVENT MANAGEMENT ---

  @POST
  @UnitOfWork
  @Path("/events")
  public EventResponse createEvent(@Valid CreateEventRequest request, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting createEvent name={}",
        admin.getId(),
        admin.getEmail(),
        request.getName());
    return adminService.createEvent(request, admin);
  }

  @GET
  @UnitOfWork
  @Path("/events")
  public List<EventResponse> getAllEvents(@Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getAllEvents", admin.getId(), admin.getEmail());
    return adminService.getAllEvents();
  }

  @GET
  @UnitOfWork
  @Path("/events/{eventId}")
  public EventResponse getEventById(@PathParam("eventId") Long eventId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getEventById eventId={}",
        admin.getId(),
        admin.getEmail(),
        eventId);
    return adminService.getEventById(eventId);
  }

  @PUT
  @UnitOfWork
  @Path("/events/{eventId}")
  public EventResponse updateEvent(
      @PathParam("eventId") Long eventId,
      @Valid UpdateEventRequest request,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting updateEvent eventId={}",
        admin.getId(),
        admin.getEmail(),
        eventId);
    return adminService.updateEvent(eventId, request, admin);
  }

  @PUT
  @UnitOfWork
  @Path("/events/{eventId}/status")
  public EventResponse updateEventStatus(
      @PathParam("eventId") Long eventId,
      @Valid AdminUpdateEventStatusRequest request,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting updateEventStatus eventId={} status={}",
        admin.getId(),
        admin.getEmail(),
        eventId,
        request.getStatus());
    return adminService.updateEventStatus(eventId, request, admin);
  }

  @DELETE
  @UnitOfWork
  @Path("/events/{eventId}")
  public AdminMessageResponse deleteEvent(
      @PathParam("eventId") Long eventId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting deleteEvent eventId={}",
        admin.getId(),
        admin.getEmail(),
        eventId);
    return adminService.deleteEvent(eventId, admin);
  }

  @GET
  @UnitOfWork
  @Path("/events/{eventId}/teams")
  public List<TeamResponse> getEventTeams(
      @PathParam("eventId") Long eventId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getEventTeams eventId={}",
        admin.getId(),
        admin.getEmail(),
        eventId);
    return adminService.getEventTeams(eventId);
  }

  @GET
  @UnitOfWork
  @Path("/events/{eventId}/registrations")
  public List<EventRegistrationResponse> getEventRegistrations(
      @PathParam("eventId") Long eventId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getEventRegistrations eventId={}",
        admin.getId(),
        admin.getEmail(),
        eventId);
    return adminService.getEventRegistrations(eventId);
  }

  // --- REGISTRATION MANAGEMENT ---

  @GET
  @UnitOfWork
  @Path("/registrations")
  public List<EventRegistrationResponse> getAllRegistrations(@Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getAllRegistrations", admin.getId(), admin.getEmail());
    return adminService.getAllRegistrations();
  }

  @GET
  @UnitOfWork
  @Path("/registrations/{registrationId}")
  public EventRegistrationResponse getRegistrationById(
      @PathParam("registrationId") Long registrationId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getRegistrationById registrationId={}",
        admin.getId(),
        admin.getEmail(),
        registrationId);
    return adminService.getRegistrationById(registrationId);
  }

  @POST
  @UnitOfWork
  @Path("/registrations")
  public EventRegistrationResponse forceRegisterUser(
      @Valid AdminForceRegisterRequest request, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting forceRegisterUser userId={} eventId={}",
        admin.getId(),
        admin.getEmail(),
        request.getUserId(),
        request.getEventId());
    return adminService.forceRegisterUser(request, admin);
  }

  @PUT
  @UnitOfWork
  @Path("/registrations/{registrationId}/status")
  public EventRegistrationResponse updateRegistrationStatus(
      @PathParam("registrationId") Long registrationId,
      @Valid AdminUpdateRegistrationStatusRequest request,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting updateRegistrationStatus registrationId={} status={}",
        admin.getId(),
        admin.getEmail(),
        registrationId,
        request.getStatus());
    return adminService.updateRegistrationStatus(registrationId, request, admin);
  }

  @DELETE
  @UnitOfWork
  @Path("/registrations/{registrationId}")
  public AdminMessageResponse deleteRegistration(
      @PathParam("registrationId") Long registrationId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting deleteRegistration registrationId={}",
        admin.getId(),
        admin.getEmail(),
        registrationId);
    return adminService.deleteRegistration(registrationId, admin);
  }

  // --- PAYMENT MANAGEMENT ---

  @GET
  @UnitOfWork
  @Path("/payments")
  public List<PaymentResponse> getAllPayments(@Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getAllPayments", admin.getId(), admin.getEmail());
    return adminService.getAllPayments();
  }

  @GET
  @UnitOfWork
  @Path("/payments/{paymentId}")
  public PaymentResponse getPaymentById(
      @PathParam("paymentId") Long paymentId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getPaymentById paymentId={}",
        admin.getId(),
        admin.getEmail(),
        paymentId);
    return adminService.getPaymentById(paymentId);
  }

  @GET
  @UnitOfWork
  @Path("/payments/order/{orderId}")
  public PaymentResponse getPaymentByOrderId(
      @PathParam("orderId") String orderId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getPaymentByOrderId orderId={}",
        admin.getId(),
        admin.getEmail(),
        orderId);
    return adminService.getPaymentByOrderId(orderId);
  }

  @GET
  @UnitOfWork
  @Path("/payments/registration/{registrationId}")
  public PaymentResponse getPaymentByRegistrationId(
      @PathParam("registrationId") Long registrationId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getPaymentByRegistrationId registrationId={}",
        admin.getId(),
        admin.getEmail(),
        registrationId);
    return adminService.getPaymentByRegistrationId(registrationId);
  }

  @PUT
  @UnitOfWork
  @Path("/payments/{paymentId}/status")
  public PaymentResponse updatePaymentStatus(
      @PathParam("paymentId") Long paymentId,
      @Valid AdminUpdatePaymentStatusRequest request,
      @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting updatePaymentStatus paymentId={} status={}",
        admin.getId(),
        admin.getEmail(),
        paymentId,
        request.getStatus());
    return adminService.updatePaymentStatus(paymentId, request, admin);
  }

  @DELETE
  @UnitOfWork
  @Path("/payments/{paymentId}")
  public AdminMessageResponse deletePayment(
      @PathParam("paymentId") Long paymentId, @Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting deletePayment paymentId={}",
        admin.getId(),
        admin.getEmail(),
        paymentId);
    return adminService.deletePayment(paymentId, admin);
  }

  // --- SYSTEM STATS & DASHBOARD ---

  @GET
  @UnitOfWork
  @Path("/stats")
  public SystemStatsResponse getSystemStats(@Auth UserPrincipal admin) {
    LOGGER.info(
        "Admin userId={} email={} requesting getSystemStats", admin.getId(), admin.getEmail());
    return adminService.getSystemStats();
  }
}
