package com.rupesh.ems.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.configs.JWTConfig;
import com.rupesh.ems.core.Role;
import com.rupesh.ems.exceptions.UnauthorizedException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JWTService {
  private static final Logger LOGGER = LoggerFactory.getLogger(JWTService.class);

  private final Long expireInMilliSec;
  private final JWTVerifier verifier;
  private final Algorithm algorithm;

  public JWTService(JWTConfig jwtConfig) {
    this.expireInMilliSec = jwtConfig.getExpireInMilliSec();
    this.algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
    this.verifier = JWT.require(algorithm).withIssuer("ems").build();
  }

  public String generateJWT(UserPrincipal user) {
    Builder builder =
        JWT.create()
            .withIssuer("ems")
            .withClaim("id", user.getId())
            .withClaim("name", user.getName())
            .withClaim("email", user.getEmail())
            .withClaim("role", user.getRole().name());

    if (expireInMilliSec != null && expireInMilliSec > 0) {
      builder.withExpiresAt(new Date(System.currentTimeMillis() + expireInMilliSec));
    }

    return builder.sign(algorithm);
  }

  public DecodedJWT verifyJwt(String jwt) {
    try {
      DecodedJWT decodedJWT = verifier.verify(jwt);
      LOGGER.info("JWT verified successfully for user id: {}", decodedJWT.getClaim("id").asLong());
      return decodedJWT;
    } catch (JWTVerificationException e) {
      LOGGER.warn("JWT verification failed: {}", e.getMessage());
      throw new UnauthorizedException("Invalid JWT token", e);
    }
  }

  public Long getUserId(DecodedJWT jwt) {
    return jwt.getClaim("id").asLong();
  }

  public String getName(DecodedJWT jwt) {
    return jwt.getClaim("name").asString();
  }

  public String getEmail(DecodedJWT jwt) {
    return jwt.getClaim("email").asString();
  }

  public Role getRole(DecodedJWT jwt) {
    try {
      return Role.valueOf(jwt.getClaim("role").asString());
    } catch (Exception e) {
      LOGGER.warn("Invalid role in token: {}", e.getMessage());
      throw new UnauthorizedException("Invalid role in token");
    }
  }
}
