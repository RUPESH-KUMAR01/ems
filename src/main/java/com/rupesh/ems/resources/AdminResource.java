package com.rupesh.ems.resources;

import java.util.List;

import com.rupesh.ems.Util.PasswordUtil;
import com.rupesh.ems.api.admin.req.ChangeUserRoleRequest;
import com.rupesh.ems.api.admin.req.CreateManagedUserRequest;
import com.rupesh.ems.api.admin.res.AdminMessageResponse;
import com.rupesh.ems.api.User.res.UserResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;

import io.dropwizard.auth.Auth;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;


@Path("/api/admin")
@RolesAllowed("ADMIN")
public class AdminResource {
    private final UserDao userDao;

    public AdminResource(UserDao userDao) {
        this.userDao = userDao;
    }

    @POST
    @Path("/users")
    public UserResponse createUser(@Valid CreateManagedUserRequest request) {
        if (userDao.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("User already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPasswordHash(PasswordUtil.generateHash(request.getPassword()));
        user.setRole(request.getRole());

        return new UserResponse(userDao.create(user));
    }

    @GET
    @Path("/users/{id}")
    public UserResponse getUserById(@PathParam("id") Long userId) {
        User user = userDao.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return new UserResponse(user);
    }

    @GET
    @Path("/users/search")
    public UserResponse getUserByEmail(
            @QueryParam("email") String email
    ) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        return new UserResponse(user);
    }

    @GET
    @Path("/users")
    public List<UserResponse> getAllUsers() {
        return userDao.findAll().stream().map(UserResponse::new).toList();
    }

    @PUT
    @Path("/users/{id}/role")
    public UserResponse changeUserRole(
            @PathParam("id") Long userId,
            @Valid ChangeUserRoleRequest request,
            @Auth UserPrincipal admin
    ) {
        User user = userDao.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (admin.getId().equals(userId) && request.getRole() != user.getRole()) {
            throw new BadRequestException("Admin cannot change own role");
        }

        user.setRole(request.getRole());
        userDao.update(user);

        return new UserResponse(user);
    }

    @DELETE
    @Path("/users/{id}")
    public AdminMessageResponse deleteUser(
            @PathParam("id") Long userId,
            @Auth UserPrincipal admin
    ) {
        if (admin.getId().equals(userId)) {
            throw new BadRequestException("Admin cannot delete own account");
        }

        boolean deleted = userDao.delete(userId);
        if (!deleted) {
            throw new NotFoundException("User not found");
        }

        return new AdminMessageResponse("User deleted successfully");
    }

}
