package com.rupesh.ems.auth;

import java.util.Optional;

import com.rupesh.ems.Util.PasswordUtil;
import com.rupesh.ems.core.Role;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;

public class BootstrapAdminService {
    private final UserDao userDao;


    public BootstrapAdminService(UserDao userDao){
        this.userDao=userDao;
    }

    public void ensureAdminExists(String name,String email,String password,boolean isEnabled,String phone){
        if(!isEnabled){
            return;
        }
        Optional<User> existingUser = userDao.findByEmail(email);
        if(existingUser.isPresent()){
            User user = existingUser.get();
            user.setRole(Role.ADMIN);
            userDao.update(user);
            user.setEmailVerified(true);
            user.setPhoneVerified(true);
            return;
        }

        User user = new User(email,name,PasswordUtil.hash(password),Role.ADMIN,phone);


        user.setEmailVerified(true);
        user.setPhoneVerified(true);

        userDao.create(user);
    }
}
