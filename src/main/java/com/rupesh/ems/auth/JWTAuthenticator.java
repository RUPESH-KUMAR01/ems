package com.rupesh.ems.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.service.JWTService;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.hibernate.UnitOfWork;
import java.util.Optional;

public class JWTAuthenticator implements Authenticator<String, UserPrincipal> {
  private final JWTService jwtService;
  private final UserDao userDao;

  public JWTAuthenticator(JWTService jwtService, UserDao userDao) {
    this.jwtService = jwtService;
    this.userDao = userDao;
  }

  @Override
  @UnitOfWork
  public Optional<UserPrincipal> authenticate(String token) throws AuthenticationException {
    try {
      DecodedJWT decodedJWT = jwtService.verifyJwt(token);
      User dbuser =
          userDao
              .getUserById(decodedJWT.getClaim("id").asLong())
              .orElseThrow(() -> new AuthenticationException("User not found"));
      UserPrincipal user = new UserPrincipal(dbuser);
      return Optional.of(user);
    } catch (Exception e) {
      throw new AuthenticationException("Invalid token", e);
    }
  }
}
