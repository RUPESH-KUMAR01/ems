package com.rupesh.ems.db;

import com.rupesh.ems.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao extends AbstractDAO<User> {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

  public UserDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public User create(User user) {
    LOGGER.info("DAO: Creating user with email={}", user.getEmail());
    return persist(user);
  }

  public Optional<User> getUserById(Long id) {
    LOGGER.debug("DAO: Fetching user by id={}", id);
    return Optional.ofNullable(get(id));
  }

  public Optional<User> findByEmail(String email) {
    LOGGER.debug("DAO: Fetching user by email={}", email);
    return currentSession()
        .createQuery("FROM User WHERE email = :email", User.class)
        .setParameter("email", email)
        .uniqueResultOptional();
  }

  public Optional<User> findByPhone(String phone) {
    LOGGER.debug("DAO: Fetching user by phone={}", phone);
    return currentSession()
        .createQuery("FROM User WHERE phone = :phone", User.class)
        .setParameter("phone", phone)
        .uniqueResultOptional();
  }

  public List<User> findAll() {
    LOGGER.debug("DAO: Fetching all users");
    return currentSession().createQuery("FROM User", User.class).getResultList();
  }

  public User update(User user) {
    LOGGER.info("DAO: Updating user id={}", user.getId());
    currentSession().merge(user);
    return user;
  }

  public boolean delete(Long id) {
    LOGGER.info("DAO: Deleting user id={}", id);
    User user = get(id);
    if (user != null) {
      currentSession().remove(user);
      return true;
    }
    return false;
  }
}
