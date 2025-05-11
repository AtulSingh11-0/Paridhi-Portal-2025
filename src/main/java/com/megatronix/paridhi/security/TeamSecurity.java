package com.megatronix.paridhi.security;

import java.util.List;

import org.springframework.stereotype.Component;

import com.megatronix.paridhi.dto.response.TeamResponse;
import com.megatronix.paridhi.repository.MRDRepository;
import com.megatronix.paridhi.service.TeamService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamSecurity {
	private final TeamService teamService;
	private final MRDRepository mrdRepository;

	public boolean isTeamMember(String tid, String email) {
		try {
			TeamResponse team = teamService.getTeamByTid(tid);

			// get GIDs for the email
			List<String> gids = mrdRepository.findGidListByUserEmail(email);

			// check if any of the user's GIDs are in the team
			return team.getGidList().stream().anyMatch(gids::contains);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isGidOwner(String gid, String email) {
		try {
			return mrdRepository.existsByGidAndUserEmail(gid, email);
		} catch (Exception e) {
			return false;
		}
	}
}
