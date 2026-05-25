package com.rupesh.ems.auth;

import java.util.Optional;

import com.auth0.jwt.interfaces.DecodedJWT;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

public class JWTAuthenticator implements Authenticator<String,UserPrincipal> {
    private final JWTService jwtService;

    public JWTAuthenticator(JWTService jwtService){
        this.jwtService = jwtService;
    }

    @Override
    public Optional<UserPrincipal> authenticate(String token) throws AuthenticationException {
        try{
            DecodedJWT decodedJWT = jwtService.verifyJwt(token);

            UserPrincipal user = new UserPrincipal(
                jwtService.getUserId(decodedJWT),
                jwtService.getEmail(decodedJWT),
                jwtService.getName(decodedJWT),
                jwtService.getRole(decodedJWT)
            );
            return Optional.of(user);
        }catch(Exception e){
            return Optional.empty();
        }
    }
    
}
