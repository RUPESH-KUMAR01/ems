package com.rupesh.ems.resources;

import com.rupesh.ems.api.auth.req.CreateUserRequest;
import com.rupesh.ems.api.auth.req.LoginRequest;
import com.rupesh.ems.api.auth.res.LoginResponse;
import com.rupesh.ems.api.auth.res.RegisterResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.service.AuthService;

import io.dropwizard.auth.Auth;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api/auth")
public class AuthResource {
    private final AuthService authService;

    public AuthResource(AuthService authService){
        this.authService = authService;
    }


    @POST
    @Path("/register")
    public RegisterResponse creatUser(@Valid CreateUserRequest req){
        return authService.register(req);
    }
    
    @POST
    @Path("/login")
    public LoginResponse loginUser(@Valid LoginRequest req) {
        return authService.login(req);
    }

    
    
    @GET
    @Path("/me")
    public UserResponse getUserInfo(@Auth UserPrincipal user){
        return authService.getUserInfo(user);
    }
}
