package com.megatronix.paridhi.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.model.EventCombo;

@Repository
public interface ComboRepository extends JpaRepository<EventCombo, Long> {
  List<EventCombo> findByDomain(Domain domain);
  List<EventCombo> findByIsRegistrationOpen(boolean isRegistrationOpen);
	Set<EventCombo> findByEventsContaining(Event event);
}
