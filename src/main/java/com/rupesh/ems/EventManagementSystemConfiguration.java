package com.rupesh.ems;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rupesh.ems.configs.BootstrapAdminConfiguration;
import com.rupesh.ems.configs.EmailServiceConfiguration;
import com.rupesh.ems.configs.JWTConfig;
import com.rupesh.ems.configs.RazorpayConfig;
import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class EventManagementSystemConfiguration extends Configuration {

  @Valid @NotNull private DataSourceFactory database = new DataSourceFactory();

  @JsonProperty("database")
  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  @JsonProperty("database")
  public void setDataSourceFactory(DataSourceFactory database) {
    this.database = database;
  }

  @Valid @NotNull private JWTConfig jwtConfig = new JWTConfig();

  @JsonProperty("jwt")
  public JWTConfig getJwtConfig() {
    return jwtConfig;
  }

  @JsonProperty("jwt")
  public void setJWTConfig(JWTConfig jwtConfig) {
    this.jwtConfig = jwtConfig;
  }

  @Valid
  private BootstrapAdminConfiguration bootstrapAdminConfiguration =
      new BootstrapAdminConfiguration();

  @JsonProperty("bootstrapadmin")
  public BootstrapAdminConfiguration getBootstrapAdminConfiguration() {
    return bootstrapAdminConfiguration;
  }

  @JsonProperty("bootstrapadmin")
  public void setBootstrapAdminConfiguration(BootstrapAdminConfiguration configuration) {
    bootstrapAdminConfiguration = configuration;
  }

  @Valid
  private EmailServiceConfiguration emailServiceConfiguration = new EmailServiceConfiguration();

  @JsonProperty("emailservice")
  public EmailServiceConfiguration getEmailServiceConfiguration() {
    return emailServiceConfiguration;
  }

  @JsonProperty("emailservice")
  public void setEmailServiceConfiguration(EmailServiceConfiguration emailServiceConfiguration) {
    this.emailServiceConfiguration = emailServiceConfiguration;
  }

  @Valid @NotNull private RazorpayConfig razorpayConfig = new RazorpayConfig();

  @JsonProperty("razorpay")
  public RazorpayConfig getRazorpayConfig() {
    return razorpayConfig;
  }

  @JsonProperty("razorpay")
  public void setRazorpayConfig(RazorpayConfig razorpayConfig) {
    this.razorpayConfig = razorpayConfig;
  }
}
