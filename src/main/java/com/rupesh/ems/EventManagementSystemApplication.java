package com.rupesh.ems;

import org.hibernate.SessionFactory;

import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;

public class EventManagementSystemApplication extends Application<EventManagementSystemConfiguration> {

    private final HibernateBundle<EventManagementSystemConfiguration> hibernateBundle =   new HibernateBundle<EventManagementSystemConfiguration>(User.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(EventManagementSystemConfiguration configuration){
            return configuration.getDataSourceFactory();
        }
    };

    public static void main(final String[] args) throws Exception {
        new EventManagementSystemApplication().run(args);
    }

    @Override
    public String getName() {
        return "EventManagementSystem";
    }

    @Override
    public void initialize(final Bootstrap<EventManagementSystemConfiguration> bootstrap) {
        bootstrap.addBundle(hibernateBundle);
    }

    @Override
    public void run(final EventManagementSystemConfiguration configuration,
                    final Environment environment) {
        
        SessionFactory sessionFactory = hibernateBundle.getSessionFactory();

        UserDao userDao = new UserDao(sessionFactory);
        
    }

}
