package com.rupesh.ems.auth;

import java.security.Principal;

import com.rupesh.ems.core.Role;
import com.rupesh.ems.core.User;

public class UserPrincipal implements Principal {

    private final Long id;
    private final String email;
    private final String phone;
    private final String name;
    private final Role role;

    public UserPrincipal(Long id,
                         String email,
                         String phone,
                         String name,
                         Role role) {

        this.id = id;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.role = role;
    }

    public UserPrincipal(User user){
        this.id = user.getId();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.name = user.getName();
        this.role = user.getRole();
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

    public boolean hasPermission(Role requiredRole) {
        return role.hasPermission(requiredRole);
    }
}