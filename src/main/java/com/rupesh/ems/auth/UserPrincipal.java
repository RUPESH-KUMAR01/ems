package com.rupesh.ems.auth;

import java.security.Principal;

import com.rupesh.ems.core.Role;

public class UserPrincipal implements Principal {

    private final Long id;
    private final String email;
    private final String name;
    private final Role role;

    public UserPrincipal(Long id,
                         String email,
                         String name,
                         Role role) {

        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    public boolean hasPermission(Role requiredRole) {
        return role.hasPermission(requiredRole);
    }
}