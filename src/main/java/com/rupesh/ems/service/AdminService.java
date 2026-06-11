package com.rupesh.ems.service;

import com.rupesh.ems.Util.PasswordUtil;
import com.rupesh.ems.api.admin.req.ChangeUserRoleRequest;
import com.rupesh.ems.api.admin.req.CreateManagedUserRequest;
import com.rupesh.ems.api.admin.req.UpdateUserRequest;
import com.rupesh.ems.api.admin.res.AdminMessageResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.util.List;

public class AdminService {
  private final UserDao userDao;

  public AdminService(UserDao userDao) {
    this.userDao = userDao;
  }

  public UserResponse createUser(CreateManagedUserRequest request) {
    if (userDao.findByEmail(request.getEmail()).isPresent()) {
      throw new ConflictException("User already exists");
    }

    if (userDao.findByPhone(request.getPhone()).isPresent()) {
      throw new ConflictException("Phone number already in use");
    }

    User user = new User();
    user.setEmail(request.getEmail());
    user.setName(request.getName());
    user.setPasswordHash(PasswordUtil.hash(request.getPassword()));
    user.setRole(request.getRole());
    user.setPhone(request.getPhone());
    user.setEmailVerified(true);
    user.setPhoneVerified(true);

    return new UserResponse(userDao.create(user));
  }

  public UserResponse getUserById(Long userId) {
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    return new UserResponse(user);
  }

  public UserResponse getUserByEmail(String email) {
    User user =
        userDao.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

    return new UserResponse(user);
  }

  public UserResponse getUserByPhone(String phone) {
    User user =
        userDao.findByPhone(phone).orElseThrow(() -> new NotFoundException("User not found"));

    return new UserResponse(user);
  }

  public List<UserResponse> getAllUsers() {
    return userDao.findAll().stream().map(UserResponse::new).toList();
  }

  public UserResponse changeUserRole(
      Long userId, ChangeUserRoleRequest request, UserPrincipal admin) {
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (admin.getId().equals(userId) && request.getRole() != user.getRole()) {
      throw new BadRequestException("Admin cannot change own role");
    }

    user.setRole(request.getRole());
    userDao.update(user);

    return new UserResponse(user);
  }

  public AdminMessageResponse deleteUser(Long userId, UserPrincipal admin) {
    if (admin.getId().equals(userId)) {
      throw new BadRequestException("Admin cannot delete own account");
    }

    boolean deleted = userDao.delete(userId);
    if (!deleted) {
      throw new NotFoundException("User not found");
    }

    return new AdminMessageResponse("User deleted successfully");
  }

  public UserResponse updateUser(Long userId, UpdateUserRequest request, UserPrincipal admin) {
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (request.getName() != null) {
      user.setName(request.getName());
    }

    if (request.getEmail() != null) {
      user.setEmail(request.getEmail());
    }

    if (request.getPhone() != null) {
      user.setPhone(request.getPhone());
    }

    if (request.getRole() != null) {
      user.setRole(request.getRole());
    }

    if (request.getEmailVerified() != null) {
      user.setEmailVerified(request.getEmailVerified());
    }

    if (request.getPhoneVerified() != null) {
      user.setPhoneVerified(request.getPhoneVerified());
    }

    userDao.update(user);

    return new UserResponse(user);
  }
}
