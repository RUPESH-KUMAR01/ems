package com.rupesh.ems.auth;

import com.rupesh.ems.core.Role;
import com.rupesh.ems.core.User;
import java.security.Principal;

public class UserPrincipal implements Principal {

  private final Long id;
  private final String email;
  private final String phone;
  private final String name;
  private final Role role;
  private final boolean emailVerified;
  private final boolean phoneVerified;

  public UserPrincipal(Long id, String email, String phone, String name, Role role, boolean emailVerified, boolean phoneVerified) {
    this.id = id;
    this.email = email;
    this.phone = phone;
    this.name = name;
    this.role = role;
    this.emailVerified = emailVerified;
    this.phoneVerified = phoneVerified;
  }

  public UserPrincipal(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.phone = user.getPhone();
    this.name = user.getName();
    this.role = user.getRole();
    this.emailVerified = user.isEmailVerified();
    this.phoneVerified = user.isPhoneVerified();
  }

  @Override
  public String getName() {
    return name;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public Role getRole() {
    return role;
  }

  public boolean isEmailVerified() {
    return emailVerified;
  }
  public boolean isPhoneVerified() {
    return phoneVerified;
  }

  public boolean isFullyVerified() {
    return emailVerified && phoneVerified;
  }

  public boolean hasPermission(Role requiredRole) {
    return role.hasPermission(requiredRole);
  }
}
