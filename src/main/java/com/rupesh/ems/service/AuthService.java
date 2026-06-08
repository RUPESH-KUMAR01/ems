package com.rupesh.ems.service;

import com.rupesh.ems.Util.PasswordUtil;
import com.rupesh.ems.api.auth.req.CreateUserRequest;
import com.rupesh.ems.api.auth.req.LoginRequest;
import com.rupesh.ems.api.auth.res.LoginResponse;
import com.rupesh.ems.api.auth.res.RegisterResponse;
import com.rupesh.ems.api.auth.res.UserResponse;
import com.rupesh.ems.auth.JWTService;
import com.rupesh.ems.auth.UserPrincipal;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import com.rupesh.ems.exceptions.UnauthorizedException;

public class AuthService {
    private final UserDao userDao;
    private final JWTService jwtService;
    private final VerificationService verificationService;

    public AuthService(UserDao userDao, JWTService jwtService,VerificationService verificationService) {
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.verificationService = verificationService;
    }

    public RegisterResponse register(CreateUserRequest req) {
        if (userDao.findByEmail(req.getEmail()).isPresent()) {
            throw new ConflictException("User already exists");
        }

        if (userDao.findByPhone(req.getPhone()).isPresent()) {
            throw new ConflictException("Phone number already in use");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setName(req.getName());
        user.setPasswordHash(PasswordUtil.hash(req.getPassword()));
        user.setPhone(req.getPhone());

        User savedUser = userDao.create(user);
        String token = jwtService.generateJWT(new UserPrincipal(savedUser));

        return new RegisterResponse(token);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userDao.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!PasswordUtil.verify(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateJWT(new UserPrincipal(user));
        return new LoginResponse(token);
    }

    public UserResponse getUserInfo(UserPrincipal user) {
        User dbUser = userDao.getUserById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not Found"));
        return new UserResponse(dbUser);
    }

    public void verifyEmail(Long userId, String otp) {
        verificationService.verifyEmail(userId, otp);
    }

    public void verifyPhone(Long userId, String otp) {
        verificationService.verifyPhone(userId, otp);
    }

    public void generateEmailOtp(Long userId) {
        verificationService.generateEmailOtp(userId);
    }

    public void generatePhoneOtp(Long userId) {
        verificationService.generatePhoneOtp(userId);
    }

}