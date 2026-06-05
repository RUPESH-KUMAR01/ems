package com.rupesh.ems.resources;

import com.rupesh.ems.Util.PasswordUtil;
import com.rupesh.ems.api.User.req.CreateUserRequest;
import com.rupesh.ems.api.User.req.LoginRequest;
import com.rupesh.ems.api.User.res.LoginResponse;
import com.rupesh.ems.api.User.res.UserResponse;
import com.rupesh.ems.auth.JWTService;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import com.rupesh.ems.exceptions.UnauthorizedException;

import io.dropwizard.auth.Auth;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api/users")
public class UserResource {
    private final UserDao userDao;
    private final JWTService jwtService;

    public UserResource(UserDao userDao, JWTService jwtService){
        this.userDao=userDao;
        this.jwtService=jwtService;
    }


    @POST
    @Path("/register")
    public UserResponse creatUser(@Valid CreateUserRequest req){
        if(userDao.findByEmail(req.getEmail()).isPresent()){
            throw new ConflictException("User already exists");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setName(req.getName());
        user.setPasswordHash(PasswordUtil.generateHash(req.getPassword()));

        User savedUser = userDao.create(user);
        return new UserResponse(savedUser);
    }
    
    @POST
    @Path("/login")
    public LoginResponse loginUser(@Valid LoginRequest req) {

        User user = userDao.findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password"));

        if (!PasswordUtil.compareHash(
                req.getPassword(),
                user.getPasswordHash())) {

            throw new UnauthorizedException(
                    "Invalid email or password");
        }

        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );

        String token = jwtService.generateJWT(userPrincipal);

        return new LoginResponse(token);
    }
    
    @GET
    @Path("/me")
    public UserResponse getUserInfo(@Auth UserPrincipal user){
        User dbUser = userDao.getUserById(user.getId()).orElseThrow(()->new NotFoundException("User not Found"));
        return new UserResponse(dbUser);
    }
}
