package com.rupesh.ems.service;

import com.rupesh.ems.Util.PasswordUtil;
import com.rupesh.ems.configs.BootstrapAdminConfiguration;
import com.rupesh.ems.core.Role;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.Optional;

public class BootstrapAdminService {
  private final UserDao userDao;

  public BootstrapAdminService(UserDao userDao) {
    this.userDao = userDao;
  }

  @UnitOfWork
  public void ensureAdminExists(BootstrapAdminConfiguration bootstrapAdminConfiguration) {
    String name = bootstrapAdminConfiguration.getName();
    String email = bootstrapAdminConfiguration.getEmail();
    String password = bootstrapAdminConfiguration.getPassword();
    boolean isEnabled = bootstrapAdminConfiguration.isEnabled();
    String phone = bootstrapAdminConfiguration.getPhone();
    if (!isEnabled) {
      return;
    }
    Optional<User> existingUser = userDao.findByEmail(email);
    if (existingUser.isPresent()) {
      User user = existingUser.get();
      user.setRole(Role.ADMIN);
      userDao.update(user);
      user.setEmailVerified(true);
      user.setPhoneVerified(true);
      return;
    }

    User user = new User(email, name, PasswordUtil.hash(password), Role.ADMIN, phone);

    user.setEmailVerified(true);
    user.setPhoneVerified(true);

    userDao.create(user);
  }
}
