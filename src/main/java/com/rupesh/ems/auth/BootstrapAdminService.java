package com.rupesh.ems.auth;

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
        User exisingUser = userDao.findByEmail(email);
        if(exisingUser!=null){
            exisingUser.setRole(Role.ADMIN);
            userDao.update(exisingUser);
            return;
        }
        User user = new User(email,name,passwordUtil.generateHash(password),Role.ADMIN);
        userDao.create(user);
    }
}
