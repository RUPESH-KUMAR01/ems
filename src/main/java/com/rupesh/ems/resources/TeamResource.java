package com.rupesh.ems.resources;

import com.rupesh.ems.service.TeamService;

public class TeamResource {
    private TeamService teamService;

    public TeamResource(TeamService teamService){
        this.teamService=teamService;
    }
}
