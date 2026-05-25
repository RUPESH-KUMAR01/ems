package com.rupesh.ems.core;

public enum Role {
    USER(0),
    MODERATOR(1),
    ADMIN(2);

    private final int level;

    Role(int i) {
        this.level = i;
    }

    public int getlevel(){
        return level;
    }
    
    public boolean hasPermission(Role requiredRole){
        return this.level >= requiredRole.level;
    }
}
