package com.rupesh.ems;

import org.hibernate.SessionFactory;

import com.rupesh.ems.auth.BootstrapAdminService;
import com.rupesh.ems.auth.JWTService;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.resources.AdminResource;
import com.rupesh.ems.resources.AuthResource;
import com.rupesh.ems.service.AdminService;
import com.rupesh.ems.service.AuthService;


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

        JWTService jwtService = new JWTService(configuration.getJwtConfig());

        UserDao userDao = new UserDao(sessionFactory);
        AuthService authService = new AuthService(userDao, jwtService);
        AdminService adminService = new AdminService(userDao);

        BootstrapAdminService bootstrapAdminService= new BootstrapAdminService(userDao);
        
        bootstrapAdminService.ensureAdminExists(configuration.getBootstrapAdminConfiguration().getName(),
    configuration.getBootstrapAdminConfiguration().getEmail(),configuration.getBootstrapAdminConfiguration().getPassword(),
    configuration.getBootstrapAdminConfiguration().isEnabled(),configuration.getBootstrapAdminConfiguration().getPhone());
        
        environment.jersey().register(new AuthResource(authService));
        environment.jersey().register(new AdminResource(adminService));
        
    }

}
