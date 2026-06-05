package com.rupesh.ems.api.admin.req;

import com.rupesh.ems.core.Role;

import jakarta.validation.constraints.NotNull;

public class ChangeUserRoleRequest {

    @NotNull
    private Role role;

    public ChangeUserRoleRequest() {
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
