package com.rupesh.ems;

import com.rupesh.ems.auth.JWTAuthenticator;
import com.rupesh.ems.auth.RoleAuthorizer;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.Event;
import com.rupesh.ems.core.EventRegistration;
import com.rupesh.ems.core.Payment;
import com.rupesh.ems.core.Team;
import com.rupesh.ems.core.TeamMember;
import com.rupesh.ems.core.TeamMembershipRequest;
import com.rupesh.ems.core.User;
import com.rupesh.ems.core.VerificationCode;
import com.rupesh.ems.db.EventDao;
import com.rupesh.ems.db.EventRegistrationDao;
import com.rupesh.ems.db.PaymentDao;
import com.rupesh.ems.db.TeamDao;
import com.rupesh.ems.db.TeamMemberDao;
import com.rupesh.ems.db.TeamMembershipRequestDao;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.db.VerificationDao;
import com.rupesh.ems.mappers.ApiExceptionMapper;
import com.rupesh.ems.ratelimit.RateLimitFilter;
import com.rupesh.ems.ratelimit.RateLimitService;
import com.rupesh.ems.resources.AdminResource;
import com.rupesh.ems.resources.AuthResource;
import com.rupesh.ems.resources.EventRegistrationResource;
import com.rupesh.ems.resources.EventResource;
import com.rupesh.ems.resources.EventTeamResource;
import com.rupesh.ems.resources.PaymentResource;
import com.rupesh.ems.resources.SwaggerDocsResource;
import com.rupesh.ems.resources.UserTeamResource;
import com.rupesh.ems.resources.WebhookResource;
import com.rupesh.ems.service.AdminService;
import com.rupesh.ems.service.AuthService;
import com.rupesh.ems.service.BootstrapAdminService;
import com.rupesh.ems.service.EventRegistrationService;
import com.rupesh.ems.service.EventService;
import com.rupesh.ems.service.EventTeamRequestService;
import com.rupesh.ems.service.EventTeamService;
import com.rupesh.ems.service.JWTService;
import com.rupesh.ems.service.PaymentService;
import com.rupesh.ems.service.VerificationService;
import com.rupesh.ems.service.email.EmailService;
import com.rupesh.ems.service.email.SMTPEmailService;
import com.rupesh.ems.service.sms.ConsoleSmsService;
import com.rupesh.ems.service.sms.SmsService;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.hibernate.SessionFactory;

public class EventManagementSystemApplication
    extends Application<EventManagementSystemConfiguration> {

  private final HibernateBundle<EventManagementSystemConfiguration> hibernateBundle =
      new HibernateBundle<EventManagementSystemConfiguration>(
          User.class,
          VerificationCode.class,
          Team.class,
          TeamMember.class,
          TeamMembershipRequest.class,
          Event.class,
          EventRegistration.class,
          Payment.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(
            EventManagementSystemConfiguration configuration) {
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
  public void run(
      final EventManagementSystemConfiguration configuration, final Environment environment) {

    SessionFactory sessionFactory = hibernateBundle.getSessionFactory();

    UserDao userDao = new UserDao(sessionFactory);
    VerificationDao verificationDao = new VerificationDao(sessionFactory);
    TeamDao teamDao = new TeamDao(sessionFactory);
    TeamMemberDao teamMemberDao = new TeamMemberDao(sessionFactory);
    TeamMembershipRequestDao teamMembershipRequestDao =
        new TeamMembershipRequestDao(sessionFactory);
    EventDao eventDao = new EventDao(sessionFactory);
    EventRegistrationDao eventRegistrationDao = new EventRegistrationDao(sessionFactory);
    PaymentDao paymentDao = new PaymentDao(sessionFactory);

    UnitOfWorkAwareProxyFactory proxyFactory = new UnitOfWorkAwareProxyFactory(hibernateBundle);

    BootstrapAdminService bootstrapAdminService =
        proxyFactory.create(BootstrapAdminService.class, UserDao.class, userDao);

    bootstrapAdminService.ensureAdminExists(configuration.getBootstrapAdminConfiguration());

    JWTService jwtService = new JWTService(configuration.getJwtConfig());
    EmailService emailService = new SMTPEmailService(configuration.getEmailServiceConfiguration());
    SmsService smsService = new ConsoleSmsService();
    VerificationService verificationService =
        new VerificationService(verificationDao, userDao, emailService, smsService);
    AuthService authService = new AuthService(userDao, jwtService, verificationService);
    AdminService adminService =
        new AdminService(userDao, teamDao, teamMemberDao, teamMembershipRequestDao, eventDao);

    EventService eventService = new EventService(eventDao);
    EventTeamService eventTeamService =
        new EventTeamService(
            eventDao, teamDao, teamMemberDao, teamMembershipRequestDao, eventRegistrationDao);
    EventRegistrationService eventRegistrationService =
        new EventRegistrationService(eventDao, teamDao, teamMemberDao, eventRegistrationDao);
    EventTeamRequestService eventTeamRequestService =
        new EventTeamRequestService(
            userDao, teamMembershipRequestDao, eventTeamService, eventRegistrationService);
    PaymentService paymentService =
        new PaymentService(
            paymentDao, eventRegistrationDao, eventDao, configuration.getRazorpayConfig());
    RateLimitService rateLimitService = new RateLimitService();

    JWTAuthenticator authenticator =
        proxyFactory.create(
            JWTAuthenticator.class,
            new Class<?>[] {JWTService.class, UserDao.class},
            new Object[] {jwtService, userDao});
    RoleAuthorizer authorizer = new RoleAuthorizer();

    environment
        .jersey()
        .register(
            new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<UserPrincipal>()
                    .setAuthenticator(authenticator)
                    .setAuthorizer(authorizer)
                    .setPrefix("Bearer")
                    .buildAuthFilter()));

    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(UserPrincipal.class));
    environment.jersey().register(RolesAllowedDynamicFeature.class);
    environment.jersey().register(ApiExceptionMapper.class);
    environment.jersey().register(new AuthResource(authService, verificationService));
    environment.jersey().register(new AdminResource(adminService));
    environment.jersey().register(new EventResource(eventService));
    environment.jersey().register(new EventTeamResource(eventTeamService, eventTeamRequestService));
    environment.jersey().register(new UserTeamResource(eventTeamService, eventTeamRequestService));
    environment.jersey().register(new EventRegistrationResource(eventRegistrationService));
    environment.jersey().register(new PaymentResource(paymentService));
    environment.jersey().register(new WebhookResource(paymentService));
    environment.jersey().register(new SwaggerDocsResource());
    environment.jersey().register(new RateLimitFilter(rateLimitService));
  }
}
