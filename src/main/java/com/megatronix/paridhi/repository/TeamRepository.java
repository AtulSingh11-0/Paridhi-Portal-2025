package com.megatronix.paridhi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.model.Team;
import com.megatronix.paridhi.model.User;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
  List< Team> findByEvent ( Event event );

  List< Team> findByTeamLeader ( User teamLeader );

  Optional< Team> findByTid ( String tid );

  List< Team> findByIsPaid ( boolean paid );

  List< Team> findByHasPlayed ( boolean hasPlayed );

  boolean existsByTeamNameAndEvent ( String teamName, Event event );

  boolean existsByGidListContaining(String gid);
}
