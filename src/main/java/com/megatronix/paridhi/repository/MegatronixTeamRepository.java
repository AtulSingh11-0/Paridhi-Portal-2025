package com.megatronix.paridhi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.MegatronixTeam;

@Repository
public interface MegatronixTeamRepository extends JpaRepository<MegatronixTeam, Long> {
	boolean existsByEmail(String email);
}
