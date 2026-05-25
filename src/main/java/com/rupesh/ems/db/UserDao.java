package com.rupesh.ems.db;

import org.hibernate.SessionFactory;

import com.rupesh.ems.core.User;

import io.dropwizard.hibernate.AbstractDAO;

public class UserDao extends AbstractDAO<User>{

    public UserDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }


    public User create(User user){
        return persist(user);
    }
    
}
