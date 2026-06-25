package com.rupesh.ems.service;

import com.rupesh.ems.Util.PasswordUtil;
import com.rupesh.ems.api.admin.req.ChangeUserRoleRequest;
import com.rupesh.ems.api.admin.req.CreateManagedUserRequest;
import com.rupesh.ems.api.admin.req.UpdateUserRequest;
import com.rupesh.ems.api.admin.res.AdminMessageResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.api.event.res.EventResponse;
import com.rupesh.ems.api.team.res.TeamMembershipResponse;
import com.rupesh.ems.api.team.res.TeamResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.Team;
import com.rupesh.ems.core.TeamMember;
import com.rupesh.ems.core.TeamMembershipRequest;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.EventDao;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.db.TeamMemberDao;
import com.rupesh.ems.db.TeamMembershipRequestDao;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.BadRequestException;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import java.util.List;
import java.util.Optional;

public class AdminService {
  private final UserDao userDao;
  private final TeamDao teamDao;
  private final TeamMemberDao teamMemberDao;
  private final TeamMembershipRequestDao teamMembershipRequestDao;
  private final EventDao eventDao;

  public AdminService(
      UserDao userDao,
      TeamDao teamDao,
      TeamMemberDao teamMemberDao,
      TeamMembershipRequestDao teamMembershipRequestDao,
      EventDao eventDao) {
    this.userDao = userDao;
    this.teamDao = teamDao;
    this.teamMemberDao = teamMemberDao;
    this.teamMembershipRequestDao = teamMembershipRequestDao;
    this.eventDao = eventDao;
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

  public List<TeamResponse> getAllTeams() {
    return teamDao.findAll().stream().map(TeamResponse::new).toList();
  }

  public TeamResponse getTeamById(Long teamId) {
    return new TeamResponse(getTeamOrThrow(teamId));
  }

  public List<UserResponse> getTeamMembers(Long teamId) {
    getTeamOrThrow(teamId);

    return teamMemberDao.getUsersByTeamId(teamId).stream()
        .map(TeamMember::getUserId)
        .map(userDao::getUserById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(UserResponse::new)
        .toList();
  }

  public List<TeamMembershipResponse> getTeamMembershipRequests(Long teamId) {
    getTeamOrThrow(teamId);

    return teamMembershipRequestDao.getRequestsByTeamId(teamId).stream()
        .map(TeamMembershipResponse::new)
        .toList();
  }

  public TeamResponse transferTeamOwnership(Long teamId, Long newOwnerId) {
    Team team = getTeamOrThrow(teamId);
    User newOwner =
        userDao.getUserById(newOwnerId).orElseThrow(() -> new NotFoundException("User not found"));

    if (!newOwner.isFullyVerified()) {
      throw new BadRequestException("New owner must be fully verified");
    }

    teamMemberDao
        .findByTeamIdAndUserId(teamId, newOwnerId)
        .orElseThrow(() -> new NotFoundException("New owner must be a member of the team"));

    team.setOwnerId(newOwnerId);
    return new TeamResponse(teamDao.update(team));
  }

  public AdminMessageResponse removeUserFromTeam(Long teamId, Long userId) {
    Team team = getTeamOrThrow(teamId);
    if (team.getOwnerId().equals(userId)) {
      throw new BadRequestException("Owner cannot be removed from team");
    }

    TeamMember membership =
        teamMemberDao
            .findByTeamIdAndUserId(teamId, userId)
            .orElseThrow(() -> new NotFoundException("Team member not found"));

    teamMemberDao.delete(membership);

    TeamMembershipRequest request =
        teamMembershipRequestDao.findByTeamIdAndUserId(teamId, userId).orElse(null);
    if (request != null) {
      teamMembershipRequestDao.delete(request);
    }

    return new AdminMessageResponse("User removed from team successfully");
  }

  public AdminMessageResponse deleteTeam(Long teamId) {
    Team team = getTeamOrThrow(teamId);

    teamMemberDao.deleteByTeamId(teamId);
    teamMembershipRequestDao.deleteByTeamId(teamId);
    teamDao.delete(team);

    return new AdminMessageResponse("Team deleted successfully");
  }

  private Team getTeamOrThrow(Long teamId) {
    return teamDao.getTeamById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));
  }

  public List<EventResponse> getAllEvents() {
    return eventDao.getAllEvents().stream().map(EventResponse::new).toList();
  }
}
