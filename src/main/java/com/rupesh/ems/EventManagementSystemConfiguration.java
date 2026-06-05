package com.rupesh.ems;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rupesh.ems.auth.BootstrapAdminConfiguration;
import com.rupesh.ems.auth.JWTConfig;

import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


public class EventManagementSystemConfiguration extends Configuration {
    
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();
    
    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory database) {
        this.database = database;
    }

    @Valid
    private JWTConfig jwtConfig = new JWTConfig();

    @JsonProperty("jwt")
    public JWTConfig getJwtConfig(){
        return jwtConfig;
    }

    @JsonProperty("jwt")
    public void setJWTConfig(JWTConfig jwtConfig){
        this.jwtConfig = jwtConfig;
    }

    @Valid
    private BootstrapAdminConfiguration bootstrapAdminConfiguration = new BootstrapAdminConfiguration();

    @JsonProperty("bootstrapadmin")
    public BootstrapAdminConfiguration getBootstrapAdminConfiguration(){
        return bootstrapAdminConfiguration;
    }

    @JsonProperty("bootstrapadmin")
    public void setBootstrapAdminConfiguration(BootstrapAdminConfiguration configuration){
        bootstrapAdminConfiguration = configuration;
    }


}
