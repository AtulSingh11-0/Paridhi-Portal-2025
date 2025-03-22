package com.megatronix.paridhi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.Event;
import com.megatronix.paridhi.constant.Domain;
import com.megatronix.paridhi.constant.EventType;


@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
  List<Event> findByDomain(Domain domain);
  List<Event> findByIsRegistrationOpen(boolean isOpen);
  List<Event> findByEventType(EventType eventType);
}
