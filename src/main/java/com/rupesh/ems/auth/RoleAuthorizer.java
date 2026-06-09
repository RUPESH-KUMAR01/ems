package com.rupesh.ems.auth;

import com.rupesh.ems.core.Role;
import io.dropwizard.auth.Authorizer;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.jspecify.annotations.Nullable;

public class RoleAuthorizer implements Authorizer<UserPrincipal> {

  @Override
  public boolean authorize(
      UserPrincipal user, String role, @Nullable ContainerRequestContext requestContext) {
    if (user == null || user.getRole() == null) {
      return false;
    }

    try {
      Role requiredRole = Role.valueOf(role);
      return user.getRole().hasPermission(requiredRole);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
