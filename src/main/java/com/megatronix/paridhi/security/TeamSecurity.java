package com.megatronix.paridhi.security;

import org.springframework.stereotype.Component;

import com.megatronix.paridhi.dto.response.TeamResponse;
import com.megatronix.paridhi.service.TeamService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamSecurity {
    private final TeamService teamService;
    
    public boolean isTeamMember(String tid, String email) {
        try {
            TeamResponse team = teamService.getTeamByTid(tid);
            // Check if the user is the team leader or a team member
            return team.getTeamLeader().getEmail().equals(email);
        } catch (Exception e) {
            return false;
        }
    }
}
