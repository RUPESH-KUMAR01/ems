package com.rupesh.ems.api.User.res;

import com.rupesh.ems.core.Role;
import com.rupesh.ems.core.User;

public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private Role role;

    public UserResponse() {
    }

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
    }

    public UserResponse(
            Long id,
            String email,
            String name,
            Role role
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
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
}