package com.rupesh.ems.auth;

public class BootstrapAdminConfiguration {

    private String name;
    private String email;
    private String password;
    private boolean isEnabled;

    public BootstrapAdminConfiguration() {
    }

    public BootstrapAdminConfiguration(String name,
                                       String email,
                                       String password,
                                       boolean isEnabled) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.isEnabled = isEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}