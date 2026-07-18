package com.rupesh.ems.ratelimit;

import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.exceptions.TooManyRequestsException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

public class RateLimitFilter implements ContainerRequestFilter {

  @Context private HttpServletRequest request;

  private final RateLimitService service;

  private static final Map<String, RateLimitPolicy> POLICIES =
      Map.of(
          "api/auth/login",
          new RateLimitPolicy(5, 5, Duration.ofMinutes(1)),
          "api/auth/register",
          new RateLimitPolicy(3, 3, Duration.ofMinutes(10)),
          "api/auth/generate-email-otp",
          new RateLimitPolicy(3, 3, Duration.ofMinutes(10)),
          "api/auth/generate-phone-otp",
          new RateLimitPolicy(3, 3, Duration.ofMinutes(10)),
          "api/auth/verify-email",
          new RateLimitPolicy(10, 10, Duration.ofMinutes(10)),
          "api/auth/verify-phone",
          new RateLimitPolicy(10, 10, Duration.ofMinutes(10)),
          "api/payments/orders",
          new RateLimitPolicy(20, 20, Duration.ofMinutes(1)),
          "api/payments/verify",
          new RateLimitPolicy(30, 30, Duration.ofMinutes(1)));

  public RateLimitFilter(RateLimitService service) {
    this.service = service;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    String path = requestContext.getUriInfo().getPath();

    RateLimitPolicy policy = POLICIES.get(path);

    if (policy == null) {
      return;
    }

    String key = buildKey(path, requestContext);

    Bucket bucket = service.resolveBucket(key, policy);

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (!probe.isConsumed()) {
      throw new TooManyRequestsException("Too many requests. Please try again later.");
    }
  }

  private String buildKey(String path, ContainerRequestContext requestContext) {

    switch (path) {

        // Unauthenticated endpoints -> IP
      case "api/auth/login":
      case "api/auth/register":
        return path + ":" + getIp();

        // Authenticated endpoints -> User ID
      case "api/auth/generate-email-otp":
      case "api/auth/generate-phone-otp":
      case "api/auth/verify-email":
      case "api/auth/verify-phone":
      case "api/payments/orders":
      case "api/payments/verify":
        UserPrincipal user = (UserPrincipal) requestContext.getSecurityContext().getUserPrincipal();
        if (user == null) {
          throw new TooManyRequestsException("Unable to determine rate limit key.");
        }
        return path + ":" + user.getId();

      default:
        return path;
    }
  }

  private String getIp() {

    String forwarded = request.getHeader("X-Forwarded-For");

    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }

    return request.getRemoteAddr();
  }
}
