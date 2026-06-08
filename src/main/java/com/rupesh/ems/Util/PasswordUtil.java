package com.rupesh.ems.Util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    
    private static final int rounds =12;
    
    public static String hash(String password){
        String salt = BCrypt.gensalt(rounds);
        String passwordHash = BCrypt.hashpw(password, salt);

        return passwordHash;
    }

    public static boolean verify(String password,String hashpw){
        return BCrypt.checkpw(password, hashpw);
    }

}
