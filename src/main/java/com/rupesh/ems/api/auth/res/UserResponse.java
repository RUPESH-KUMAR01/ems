package com.rupesh.ems.api.auth.res;

import com.rupesh.ems.core.Role;
import com.rupesh.ems.core.User;

public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private Role role;
    private boolean emailVerified;
    private boolean phoneVerified;

    public UserResponse() {
    }

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
        this.emailVerified = user.isEmailVerified();
        this.phoneVerified = user.isPhoneVerified();
    }

    public UserResponse(
            Long id,
            String email,
            String name,
            Role role,
            boolean emailVerified,
            boolean phoneVerified
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
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

}