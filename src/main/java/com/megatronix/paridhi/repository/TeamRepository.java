package com.megatronix.paridhi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
  List< Team> findByEvent ( Event event );

  @Query("SELECT DISTINCT t FROM Team t JOIN FETCH t.contacts JOIN t.gidList gid WHERE gid IN :gidList")
  List<Team> findByGidListContainingWithContacts(@Param("gidList") List<String> gidList);
  
  @Query("SELECT t FROM Team t JOIN FETCH t.contacts WHERE t.tid = :tid")
  Optional<Team> findByTidWithContacts(@Param("tid") String tid);
  
  @Query("SELECT t FROM Team t JOIN FETCH t.contacts WHERE t.event = :event")
  List<Team> findByEventWithContacts(@Param("event") Event event);

  List< Team> findByIsPaid ( boolean paid );
  
  List< Team> findByHasPlayed ( boolean hasPlayed );

  boolean existsByTeamNameAndEvent ( String teamName, Event event );

  boolean existsByGidListContaining(String gid);
}
