package com.rupesh.ems.db;

import com.rupesh.ems.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

public class UserDao extends AbstractDAO<User> {

    public UserDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public User create(User user) {
        return persist(user);
    }

    public User update(User user) {
        currentSession().merge(user);
        return user;
    }

    public User findByEmail(String email) {

        return currentSession()
                .createQuery(
                        "FROM User WHERE email = :email",
                        User.class
                )
                .setParameter("email", email)
                .uniqueResult();
    }
}