package com.rupesh.ems.resources;

import com.rupesh.ems.api.auth.req.CreateUserRequest;
import com.rupesh.ems.api.auth.req.EmailVerifyRequest;
import com.rupesh.ems.api.auth.req.LoginRequest;
import com.rupesh.ems.api.auth.req.PhoneVerifyRequest;
import com.rupesh.ems.api.auth.res.LoginResponse;
import com.rupesh.ems.api.auth.res.RegisterResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.api.common.MessageResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.AuthService;
import com.rupesh.ems.service.VerificationService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);

  private final AuthService authService;
  private final VerificationService verificationService;

  public AuthResource(AuthService authService, VerificationService verificationService) {
    this.authService = authService;
    this.verificationService = verificationService;
  }

  @POST
  @UnitOfWork
  @Path("/register")
  public RegisterResponse createUser(@Valid CreateUserRequest req) {
    LOGGER.info("Registering user with email: {}", req.getEmail());
    return authService.register(req);
  }

  @POST
  @UnitOfWork
  @Path("/login")
  public LoginResponse loginUser(@Valid LoginRequest req) {
    LOGGER.info("Logging in user with email: {}", req.getEmail());
    return authService.login(req);
  }

  @POST
  @UnitOfWork
  @Path("/verify-email")
  public MessageResponse verifyEmail(@Auth UserPrincipal user, @Valid EmailVerifyRequest req) {
    LOGGER.info("Verifying email for user with ID: {}", user.getId());
    verificationService.verifyEmail(user.getId(), req.getOtp());
    return new MessageResponse("Email verified successfully");
  }

  @POST
  @UnitOfWork
  @Path("/verify-phone")
  public MessageResponse verifyPhone(@Auth UserPrincipal user, @Valid PhoneVerifyRequest req) {
    LOGGER.info("Verifying phone for user with ID: {}", user.getId());
    verificationService.verifyPhone(user.getId(), req.getOtp());
    return new MessageResponse("Phone verified successfully");
  }

  @POST
  @UnitOfWork
  @Path("/generate-email-otp")
  public MessageResponse generateEmailOtp(@Auth UserPrincipal user) {
    LOGGER.info("Generating email OTP for user with ID: {}", user.getId());
    verificationService.generateEmailOtp(user.getId());
    return new MessageResponse("Email OTP generated successfully");
  }

  @POST
  @UnitOfWork
  @Path("/generate-phone-otp")
  public MessageResponse generatePhoneOtp(@Auth UserPrincipal user) {
    LOGGER.info("Generating phone OTP for user with ID: {}", user.getId());
    verificationService.generatePhoneOtp(user.getId());
    return new MessageResponse("Phone OTP generated successfully");
  }

  @GET
  @UnitOfWork
  @Path("/me")
  public UserResponse getUserInfo(@Auth UserPrincipal user) {
    LOGGER.info("Fetching user info for user with ID: {}", user.getId());
    return authService.getUserInfo(user);
  }
}
