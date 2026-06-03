package com.rupesh.ems.resources;

import java.util.List;

import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;


@Path("/api/admin")
public class AdminResource {
    private final UserDao userDao;

    public AdminResource(UserDao userDao){
        this.userDao = userDao;
    }

    @GET
    @RolesAllowed("ADMIN")
    public List<User> getUsers(){
        return userDao.findAll();
    }

    

}
