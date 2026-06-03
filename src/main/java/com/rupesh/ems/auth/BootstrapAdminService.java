package com.rupesh.ems.auth;

import java.util.Optional;

import com.rupesh.ems.Util.PasswordUtil;
import com.rupesh.ems.core.Role;
import com.rupesh.ems.core.User;
import com.rupesh.ems.db.UserDao;

public class BootstrapAdminService {
    private UserDao userDao;

    private final PasswordUtil passwordUtil;

    public BootstrapAdminService(UserDao userDao,PasswordUtil passwordUtil){
        this.userDao=userDao;
        this.passwordUtil = passwordUtil;
    }

    public void ensureAdminExists(String name,String email,String password,boolean isEnabled){
        if(!isEnabled){
            return;
        }
        Optional<User> exisingUser = userDao.findByEmail(email);
        if(exisingUser.isPresent()){
            User user = exisingUser.get();
            user.setRole(Role.ADMIN);
            userDao.update(user);
            return;
        }
        User user = new User(email,name,passwordUtil.generateHash(password),Role.ADMIN);
        userDao.create(user);
    }
}
