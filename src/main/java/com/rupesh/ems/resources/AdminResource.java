package com.rupesh.ems.resources;

import com.rupesh.ems.api.admin.req.ChangeUserRoleRequest;
import com.rupesh.ems.api.admin.req.CreateManagedUserRequest;
import com.rupesh.ems.api.admin.req.UpdateUserRequest;
import com.rupesh.ems.api.admin.res.AdminMessageResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.AdminService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.util.List;

@Path("/api/admin")
@RolesAllowed("ADMIN")
public class AdminResource {
  private final AdminService adminService;

  public AdminResource(AdminService adminService) {
    this.adminService = adminService;
  }

  @POST
  @UnitOfWork
  @Path("/users")
  public UserResponse createUser(@Valid CreateManagedUserRequest request) {
    return adminService.createUser(request);
  }

  @PUT
  @Path("/users/{id}")
  @UnitOfWork
  public UserResponse updateUser(
      @PathParam("id") Long userId, @Valid UpdateUserRequest request, @Auth UserPrincipal admin) {
    return adminService.updateUser(userId, request, admin);
  }

  @GET
  @UnitOfWork
  @Path("/users/{id}")
  public UserResponse getUserById(@PathParam("id") Long userId) {
    return adminService.getUserById(userId);
  }

  @GET
  @UnitOfWork
  @Path("/users/search/email")
  public UserResponse getUserByEmail(@QueryParam("email") String email) {
    return adminService.getUserByEmail(email);
  }

  @GET
  @UnitOfWork
  @Path("/users/search/phone")
  public UserResponse getUserByPhone(@QueryParam("phone") String phone) {
    return adminService.getUserByPhone(phone);
  }

  @GET
  @UnitOfWork
  @Path("/users")
  public List<UserResponse> getAllUsers() {
    return adminService.getAllUsers();
  }

  @PUT
  @UnitOfWork
  @Path("/users/{id}/role")
  public UserResponse changeUserRole(
      @PathParam("id") Long userId,
      @Valid ChangeUserRoleRequest request,
      @Auth UserPrincipal admin) {
    return adminService.changeUserRole(userId, request, admin);
  }

  @DELETE
  @UnitOfWork
  @Path("/users/{id}")
  public AdminMessageResponse deleteUser(@PathParam("id") Long userId, @Auth UserPrincipal admin) {
    return adminService.deleteUser(userId, admin);
  }
}
