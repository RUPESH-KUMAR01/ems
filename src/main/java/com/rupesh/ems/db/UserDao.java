package com.rupesh.ems.db;

import com.rupesh.ems.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;

public class UserDao extends AbstractDAO<User> {

  public UserDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public User create(User user) {
    return persist(user);
  }

  public Optional<User> getUserById(Long id) {
    return Optional.ofNullable(get(id));
  }

  public Optional<User> findByEmail(String email) {

    return currentSession()
        .createQuery("FROM User WHERE email = :email", User.class)
        .setParameter("email", email)
        .uniqueResultOptional();
  }

  public Optional<User> findByPhone(String phone) {

    return currentSession()
        .createQuery("FROM User WHERE phone = :phone", User.class)
        .setParameter("phone", phone)
        .uniqueResultOptional();
  }

  public List<User> findAll() {
    return currentSession().createQuery("FROM User", User.class).getResultList();
  }

  public User update(User user) {
    currentSession().merge(user);
    return user;
  }

  public boolean delete(Long id) {
    User user = get(id);
    if (user != null) {
      currentSession().remove(user);
      return true;
    }
    return false;
  }
}
