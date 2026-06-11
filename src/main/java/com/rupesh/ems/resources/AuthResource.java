package com.rupesh.ems.resources;

import com.rupesh.ems.api.auth.req.CreateUserRequest;
import com.rupesh.ems.api.auth.req.EmailVerifyRequest;
import com.rupesh.ems.api.auth.req.LoginRequest;
import com.rupesh.ems.api.auth.req.PhoneVerifyRequest;
import com.rupesh.ems.api.auth.res.LoginResponse;
import com.rupesh.ems.api.auth.res.RegisterResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.AuthService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api/auth")
public class AuthResource {
  private final AuthService authService;

  public AuthResource(AuthService authService) {
    this.authService = authService;
  }

  @POST
  @UnitOfWork
  @Path("/register")
  public RegisterResponse createUser(@Valid CreateUserRequest req) {
    return authService.register(req);
  }

  @POST
  @UnitOfWork
  @Path("/login")
  public LoginResponse loginUser(@Valid LoginRequest req) {
    return authService.login(req);
  }

  @POST
  @UnitOfWork
  @Path("/verify-email")
  public void verifyEmail(@Auth UserPrincipal user, @Valid EmailVerifyRequest req) {
    authService.verifyEmail(user.getId(), req.getOtp());
  }

  @POST
  @UnitOfWork
  @Path("/verify-phone")
  public void verifyPhone(@Auth UserPrincipal user, @Valid PhoneVerifyRequest req) {
    authService.verifyPhone(user.getId(), req.getOtp());
  }

  @POST
  @UnitOfWork
  @Path("/generate-email-otp")
  public void generateEmailOtp(@Auth UserPrincipal user) {
    authService.generateEmailOtp(user.getId());
  }

  @POST
  @UnitOfWork
  @Path("/generate-phone-otp")
  public void generatePhoneOtp(@Auth UserPrincipal user) {
    authService.generatePhoneOtp(user.getId());
  }

  @GET
  @UnitOfWork
  @Path("/me")
  public UserResponse getUserInfo(@Auth UserPrincipal user) {
    return authService.getUserInfo(user);
  }
}
