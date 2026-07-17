package com.rupesh.ems.security.interceptors;

import com.rupesh.ems.security.annotations.RateLimit;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

public class RateLimitFilter implements ContainerRequestFilter {

  private final RateLimit rateLimit;

  public RateLimitFilter(RateLimit rateLimit) {
    this.rateLimit = rateLimit;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
        
  }
}
