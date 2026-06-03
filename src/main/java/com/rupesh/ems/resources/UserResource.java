package com.rupesh.ems.resources;

import java.util.List;

import com.rupesh.ems.Util.PasswordUtil;
import com.rupesh.ems.api.User.req.CreateUserRequest;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.ConflictException;

import io.dropwizard.auth.Auth;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api/users")
public class UserResource {
    private final UserDao userDao;

    public UserResource(UserDao userDao){
        this.userDao=userDao;
    }


    @POST
    @Path("/register")
    public User creatUser(@Valid CreateUserRequest req){
        if(userDao.findByEmail(req.getEmail()).isPresent()){
            throw new ConflictException("User already exists");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setName(req.getName());
        user.setPasswordHash(PasswordUtil.generateHash(req.getPassword()));

        return userDao.create(user);
    }
    
    
    @GET
    @Path("/me")
    public User getUserInfo(@Auth UserPrincipal user){
        return userDao.getUserById(user.getId()).orElseThrow(()->new RuntimeException("User not Found"));
    }
}
