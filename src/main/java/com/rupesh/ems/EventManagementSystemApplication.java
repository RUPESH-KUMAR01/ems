package com.rupesh.ems;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

public class EventManagementSystemApplication extends Application<EventManagementSystemConfiguration> {

    public static void main(final String[] args) throws Exception {
        new EventManagementSystemApplication().run(args);
    }

    @Override
    public String getName() {
        return "EventManagementSystem";
    }

    @Override
    public void initialize(final Bootstrap<EventManagementSystemConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final EventManagementSystemConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
