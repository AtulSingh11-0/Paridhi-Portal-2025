package com.megatronix.paridhi.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.model.EventCombo;

@Repository
public interface ComboRepository extends JpaRepository<EventCombo, Long> {
  
  @Query("SELECT c FROM EventCombo c LEFT JOIN FETCH c.events")
  List<EventCombo> findAllWithEvents();
  
  @Query("SELECT c FROM EventCombo c LEFT JOIN FETCH c.events WHERE c.id = :id")
  Optional<EventCombo> findByIdWithEvents(@Param("id") Long id);
  
  @Query("SELECT c FROM EventCombo c LEFT JOIN FETCH c.events WHERE c.domain = :domain")
  List<EventCombo> findByDomainWithEvents(@Param("domain") Domain domain);
  
  @Query("SELECT c FROM EventCombo c LEFT JOIN FETCH c.events WHERE c.isRegistrationOpen = :isOpen")
  List<EventCombo> findByIsRegistrationOpenWithEvents(@Param("isOpen") boolean isOpen);

  @Query("SELECT DISTINCT c FROM EventCombo c LEFT JOIN FETCH c.events e WHERE e = :event")
  Set<EventCombo> findByEventsContainingWithEvents(@Param("event") Event event);
  
  List<EventCombo> findByDomain(Domain domain);
  List<EventCombo> findByIsRegistrationOpen(boolean isRegistrationOpen);
	Set<EventCombo> findByEventsContaining(Event event);
}
