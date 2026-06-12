package com.rupesh.ems.service;

import com.rupesh.ems.db.TeamDao;

public class TeamService {
    private final TeamDao teamDao;

    public TeamService(TeamDao teamDao){
        this.teamDao=teamDao;
    }

    
}
