package com.rupesh.ems.service;

import com.rupesh.ems.api.auth.req.CreateUserRequest;
import com.rupesh.ems.api.auth.req.LoginRequest;
import com.rupesh.ems.api.auth.res.LoginResponse;
import com.rupesh.ems.api.auth.res.RegisterResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import com.rupesh.ems.exceptions.UnauthorizedException;
import com.rupesh.ems.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

  private final UserDao userDao;
  private final JWTService jwtService;
  private final VerificationService verificationService;

  public AuthService(
      UserDao userDao, JWTService jwtService, VerificationService verificationService) {
    this.userDao = userDao;
    this.jwtService = jwtService;
    this.verificationService = verificationService;
  }

  public RegisterResponse register(CreateUserRequest req) {
    LOGGER.info("Attempting to register user with email: {}", req.getEmail());
    if (userDao.findByEmail(req.getEmail()).isPresent()) {
      LOGGER.warn("Attempt to register with existing email: {}", req.getEmail());
      throw new ConflictException("User already exists");
    }

    if (userDao.findByPhone(req.getPhone()).isPresent()) {
      LOGGER.warn("Attempt to register with existing phone number: {}", req.getPhone());
      throw new ConflictException("Phone number already in use");
    }

    User user = new User();
    user.setEmail(req.getEmail());
    user.setName(req.getName());
    user.setPasswordHash(PasswordUtil.hash(req.getPassword()));
    user.setPhone(req.getPhone());

    User savedUser = userDao.create(user);
    String token = jwtService.generateJWT(new UserPrincipal(savedUser));
    LOGGER.info("User registered successfully with email: {}", req.getEmail());
    return new RegisterResponse(token);
  }

  public LoginResponse login(LoginRequest req) {
    LOGGER.info("Attempting to login user with email: {}", req.getEmail());
    User user =
        userDao
            .findByEmail(req.getEmail())
            .orElseThrow(() -> {
              LOGGER.warn("Attempt to login with invalid email: {}", req.getEmail());
              return new UnauthorizedException("Invalid email or password");
            });

    if (!PasswordUtil.verify(req.getPassword(), user.getPasswordHash())) {
      LOGGER.warn("Attempt to login with invalid password for user: {}", req.getEmail());
      throw new UnauthorizedException("Invalid email or password");
    }

    String token = jwtService.generateJWT(new UserPrincipal(user));
    LOGGER.info("User logged in successfully with email: {}", req.getEmail());
    return new LoginResponse(token);
  }

  public UserResponse getUserInfo(UserPrincipal user) {
    LOGGER.info("Fetching user info for user with ID: {}", user.getId());
    User dbUser =
        userDao
            .getUserById(user.getId())
            .orElseThrow(() -> new NotFoundException("User not Found"));
    return new UserResponse(dbUser);
  }
}
