package com.rupesh.ems.api.User.res;

public class LoginResponse {
    private String token;

    public LoginResponse(String token){
        this.token = token;
    }

    public LoginResponse(){}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
