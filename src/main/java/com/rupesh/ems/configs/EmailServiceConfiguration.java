package com.rupesh.ems.configs;

public class EmailServiceConfiguration {
    private String username;
    private String password;

    public EmailServiceConfiguration(){}

    public EmailServiceConfiguration(String username,String password){
        this.username=username;
        this.password=password;
    }


    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username=username;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password=password;
    }
}
