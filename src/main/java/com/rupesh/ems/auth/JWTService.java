package com.rupesh.ems.auth;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rupesh.ems.core.Role;

public class JWTService {

    private volatile Long expireInMilliSec;
    private final JWTVerifier verifier;
    private final Algorithm algorithm;

    public JWTService(String secret,Long expireInMilliSec) {
        this.expireInMilliSec = expireInMilliSec;
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = com.auth0.jwt.JWT.require(algorithm)
        .withIssuer("ems").build();
    }

    public String generateJWT(UserPrincipal user){
        Builder builder =JWT.create().withIssuer("ems")
        .withClaim("id", user.getId())
        .withClaim("name",user.getName())
        .withClaim("email",user.getEmail()).
        withClaim("role",user.getRole().name());

        if(expireInMilliSec!=null && expireInMilliSec>0){
            builder.withExpiresAt(
                new Date(System.currentTimeMillis()+expireInMilliSec)
            );
        }

        return builder.sign(algorithm);
    }

    public DecodedJWT verifyJwt(String jwt){
        try{
            DecodedJWT decodedJWT =  verifier.verify(jwt);
            return decodedJWT;
        }catch(JWTVerificationException e){
            throw new RuntimeException("Invalid JWT token",e);
        }
    }

    public Long getUserId(DecodedJWT jwt) {
        return jwt.getClaim("id").asLong();
    }

    public String getName(DecodedJWT jwt) {
        return jwt.getClaim("name").asString();
    }
    
    public String getEmail(DecodedJWT jwt){
        return jwt.getClaim("email").asString();
    }

    public Role getRole(DecodedJWT jwt) {
        String roleStr = jwt.getClaim("role").asString();
        return roleStr != null ? Role.valueOf(roleStr) : null;
    }
}
