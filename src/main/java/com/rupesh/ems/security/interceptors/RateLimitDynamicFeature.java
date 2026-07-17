package com.rupesh.ems.security.interceptors;

import com.rupesh.ems.security.annotations.RateLimit;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class RateLimitDynamicFeature implements DynamicFeature {

  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitDynamicFeature.class);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {

    Method method = resourceInfo.getResourceMethod();

    RateLimit annotation = method.getAnnotation(RateLimit.class);

    if (annotation == null) {
      return;
    }

    LOGGER.info(
        "Registered rate limit: {}#{} [strategy={}, capacity={}, refill={}/{} {}]",
        resourceInfo.getResourceClass().getSimpleName(),
        method.getName(),
        annotation.strategy(),
        annotation.capacity(),
        annotation.refillTokens(),
        annotation.refillDuration(),
        annotation.unit());

    context.register(new RateLimitFilter(annotation));
  }
}
