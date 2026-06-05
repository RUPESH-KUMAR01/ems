package com.rupesh.ems.api.User.req;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email,String password){
        this.email = email;
        this.password = password;
    }

    public LoginRequest(){}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
